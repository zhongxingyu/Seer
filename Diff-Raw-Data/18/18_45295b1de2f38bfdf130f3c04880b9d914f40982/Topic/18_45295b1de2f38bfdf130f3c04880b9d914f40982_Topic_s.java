 package models;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 import play.libs.Json;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 public class Topic extends Model{
    @Id
    public Long id;
 
    @Constraints.Required
    @Column(unique = true)
    public String title;
 
    public String type;
    public String status;
    public String summary;
 
    public DateTime createdOn;
 
    @Version
    public int version;
 
    public DateTime lastModified;
 
    public Float latitude;
    public Float longitude;
 
    @Lob
    public String content;
 
    @Lob
    public String visuals;
 
    public String imageUrl;
 
    public static final Model.Finder<Long, Topic> find = new Model.Finder<Long, Topic>(Long.class, Topic.class);
 
    public static Topic findByTitle(String title) {
        if (title == null){
            return null;
        }
        return find.where().eq("title", title).findUnique();
    }
 
    public Topic (String title, String content, String imageUrl, DateTime date, Float latitude, Float longitude) {
       this.title = title;
       this.content = content;
       this.imageUrl = imageUrl;
       this.latitude = latitude;
       this.longitude = longitude;
       this.createdOn = DateTime.now();
    }
 
    public ObjectNode getFullJson() {
       ObjectNode node = getSimpleJson();
 
       node.put("id", id);
       node.put("content", content);
       node.put("visuals", visuals);
       node.put("status", status);
 
       List<Topic> subTopics = TopicRelationship.findSubTopics(this);
       ArrayNode subArray = new ArrayNode(JsonNodeFactory.instance);
 
       for (Topic subTopic : subTopics) {
          if (subTopic.isPublic()) {
             subArray.add(subTopic.getSimpleJson());
          }
       }
       node.put("sub_topics", subArray);
 
       List<Topic> superTopics = TopicRelationship.findSuperTopics(this);
        ArrayNode superArray = new ArrayNode(JsonNodeFactory.instance);
       for (Topic superTopic : superTopics) {
           if (superTopic.isPublic()) {
             superArray.add(superTopic.getSimpleJson());
           }
       }
 
       node.put("super_topics", superArray);
       return node;
    }
 
    public ObjectNode getSimpleJson() {
        ObjectNode node = Json.newObject();
 
        node.put("title", title);
        node.put("latitude", latitude);
        node.put("longitude", longitude);
        node.put("image", imageUrl);
        node.put("summary", summary);
        node.put("type", type);
 
        return node;
    }
 
    public static String handleForm(Topic newTopic, String supers, String subs) {
        if (newTopic.createdOn == null) {
            newTopic.createdOn = DateTime.now();
            newTopic.save();
        } else {
            newTopic.lastModified = DateTime.now();
            newTopic.save();
        }
 
        String[] superTitles = supers.split(",");
        String[] subTitles = subs.split(",");
 
        TopicRelationship.updateSupers(newTopic.title, superTitles);
        TopicRelationship.updateSubs(newTopic.title, subTitles);
 
        return newTopic.title;
    }
 
    public boolean isPublic() {
        return status != null && status.equals("public");
    }
 
     public void updateFields(Topic temp) {
         title = temp.title;
         content = temp.content;
         imageUrl = temp.imageUrl;
         latitude = temp.latitude;
         longitude = temp.longitude;
         summary = temp.summary;
         visuals = temp.visuals;
         type = temp.type;
         status = temp.status;
     }
 }
