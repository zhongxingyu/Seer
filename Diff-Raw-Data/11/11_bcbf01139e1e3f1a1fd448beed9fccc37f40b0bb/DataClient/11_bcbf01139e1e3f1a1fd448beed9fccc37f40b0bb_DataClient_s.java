 package story.book.dataclient;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
 import story.book.*;
 import story.book.model.Illustration;
 import story.book.model.Story;
 import story.book.model.StoryInfo;
 import android.util.Log;
 
 import com.google.gson.*;
 
 /**
  * DataClient is an abstract class that represents 
  * a storage location that contains Stories. Stories
  * can be saved and retrieved using a DataClient.
  * 
  * @author Anthony Ou
  * 
  * @author Vina Nguyen
  * 
  */
 public abstract class DataClient {
 
 	protected static Gson Gsonclient;
 
 	protected DataClient() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Illustration.class, new IllustrationDeserialiser())
		.setPrettyPrinting();
		Gsonclient = gsonBuilder.create();
 	}
 	
 	/**
 	 * 
 	 * Custom deserailizer for illustration class
 	 * http://stackoverflow.com/questions/3629596/deserializing-an-abstract-class-in-gson
 	 */
 	public class IllustrationDeserialiser implements JsonDeserializer<Illustration<?>>, JsonSerializer<Illustration<?>> {
 
 		@Override
 		public Illustration<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) {
 
 			JsonObject jsonObj = jsonElement.getAsJsonObject();
 			String className = jsonObj.get("ILLUSTRATIONSTORY").getAsString();
 			try {
 				Class<?> clz = Class.forName(className);
 				return context.deserialize(jsonElement, clz);
 			} catch (ClassNotFoundException e) {
 				Log.d("error parsing Illustration", "DataClient");
 				return null;
 				//throw new JsonParseException(e);
 			}
 		}
 
 		@Override
 		public JsonElement serialize(Illustration<?> object, Type type,
 				JsonSerializationContext jsonSerializationContext) {
 				JsonElement jsonEle = jsonSerializationContext.serialize(object, object.getClass());
 				jsonEle.getAsJsonObject().addProperty("ILLUSTRATIONSTORY",
 						object.getClass().getCanonicalName());
 				return jsonEle;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param an object
 	 * @return object as a serialized string.
 	 */
 	protected String serialize(Object data){
 		return Gsonclient.toJson(data); 
 	}
 
 	/**
 	 * 
 	 * @param Serial is a string of serialized object of Type type
 	 * @return a null on failure or an object (responsibility of caller to cast it)
 	 */
 	protected Object unSerialize(String serial, Type type){
 		return Gsonclient.fromJson(serial, type);
 	}
 
 	/**
 	 * 
 	 * @param aStory, the story to be saved
 	 */
 	public abstract void saveStory(Story aStory);
 
 	/**
 	 * 
 	 * @param SID of story
 	 * @returns A Story with the given SID
 	 */
 	public abstract Story getStory(int SID);
 
 	/**
 	 * 
 	 * @return true if the SID is not in use, false other-wise
 	 */
 	public abstract Boolean checkSID(int SID);
 
 	/**
 	 * 
 	 * @return -1 on failure, a free SID other-wise
 	 */
 	public abstract int getSID();
 
 	/**
 	 * 
 	 * @return an array list of StoryInfos
 	 */
 	public abstract ArrayList<StoryInfo> getStoryInfoList();
 
 }
