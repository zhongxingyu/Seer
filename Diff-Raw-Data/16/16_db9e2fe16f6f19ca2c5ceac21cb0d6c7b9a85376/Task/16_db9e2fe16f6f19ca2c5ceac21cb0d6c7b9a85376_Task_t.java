 package models;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Id;
 
 import play.data.validation.Required;
 import play.modules.objectify.Datastore;
 import play.modules.objectify.ObjectifyModel;
 
 import com.google.appengine.repackaged.com.google.common.collect.Maps;
 import com.google.appengine.repackaged.org.json.JSONObject;
 import com.google.gson.Gson;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Parent;
 
 public class Task extends ObjectifyModel<Task> {
 	@Id 
 	public Long id;
     @Required 
     public String name;
     public String description;
     public Integer index;
     public Integer points;
     public Boolean completed;
     @Required @Parent public Key<Story> story;
     public Key<User> assignee;
     
     public static Task findById(Long storyId, Long taskId) {
     	Key<Task> key = Datastore.key(Story.class, storyId, Task.class, taskId);
         return Datastore.find(key);
 
     }
     
     
     public static Task findById(Key<Task> key) {
         return Datastore.find(key);
     }
     
     
     public Key<Task> save() {
         return Datastore.put(this);
     }
     
     public void delete() {
         Datastore.delete(this);
     }
     
     public static List<Task> findAllByStory(Long storyId) {
     	if (storyId != null) {
             return Datastore.query(Task.class)
                     .ancestor(Datastore.key(Story.class, storyId))
                     .order("index").list();
         }
         else {
             return null;
         }
 
     }
     
     /**
 	 * Class used to serialize to json 
 	 * @author asalvadore
 	 *
 	 */
 	public static class TaskJsonSerializer implements JsonSerializer<Task>{
 		@Override
 		public JsonElement serialize(Task task, Type typeOf, JsonSerializationContext context) {
 			JsonElement jsonElement = new Gson().toJsonTree(task);
 			if (task.assignee != null){
 				User user = User.findByKey(task.assignee);
 				JsonObject jsonObject = jsonElement.getAsJsonObject();
 				jsonObject.remove("assignee");
 				jsonObject.addProperty("assignee", user.email);
 			}
 			return jsonElement;
 		}
 	}
 	
 	/**
 	 * Class used to deserialize from json 
 	 * @author asalvadore
 	 *
 	 */
 	public static class TaskJsonDeserializer implements JsonDeserializer<Task>{
 		@Override
 		public Task deserialize(JsonElement jsonElement, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
 			
 			JsonObject jsonObject = jsonElement.getAsJsonObject();
 			JsonElement assigneeElement = jsonObject.get("assignee");
 			Key<User> key = null;
 			if(assigneeElement != null){
 				String userEmail = assigneeElement.getAsString();
				User byEmail = User.findByEmail(userEmail);
				if(byEmail != null){
					key = byEmail.getKey();
				}
 				jsonObject.remove("assignee");
 			}
 			Task fromJson = new Gson().fromJson(jsonObject, Task.class);
 			fromJson.assignee = key;
 			return fromJson;
 		}
 		
 	}
     
 
 }
