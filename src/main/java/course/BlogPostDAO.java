package course;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BlogPostDAO {
    private MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    public Document findByPermalink(String permalink) {
        BasicDBObject dbObject = new BasicDBObject("permalink", permalink);
        return postsCollection.find(dbObject).first();
    }

    public List<Document> findByDateDescending(int limit) {
        return postsCollection.find()
                .sort(Sorts.descending("date"))
                .limit(limit)
                .into(new ArrayList<Document>());
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        Document post = new Document();
        post.append("title", title)
                .append("author", username)
                .append("body", body)
                .append("permalink", permalink)
                .append("tags", tags)
                .append("comments", Collections.<Document>emptyList())//
                .append("date", new Date());

        postsCollection.insertOne(post);

        return permalink;
    }


    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {
        Document comment = new Document().append("author", name).append("body", body);
        if (email != null) {
            comment.append("email", email);
        }

        Bson update = new Document("$push", new Document("comments", comment));
        postsCollection.updateOne(Filters.eq("permalink", permalink), update, new UpdateOptions().upsert(true));

    }

}
