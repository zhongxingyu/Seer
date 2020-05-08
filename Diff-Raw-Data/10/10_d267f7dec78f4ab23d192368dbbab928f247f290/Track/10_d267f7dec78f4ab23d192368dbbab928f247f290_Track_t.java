 package models;
 
 import javax.persistence.Entity;
 import com.google.gson.JsonObject;
 import models.Session;
 import models.Version;
 import util.JsonUtils;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Track extends Model {
 
    public static JsonObject create(String body, Long sessionId) {
        Session seshion = Session.findById(sessionId);
         JsonObject jsonTrack = JsonUtils.getJsonEObject(body);
         JsonObject root = JsonUtils.getJsonEObject(seshion.data);
         Version version = new Version().save();
         jsonTrack.addProperty("id", version.id);
        JsonObject mergedSession = JsonUtils.mergeJsonObjects(root, jsonTrack, "tracks");
         seshion.data = mergedSession.toString();
         seshion.save();
        return mergedSession;
     }
 }
