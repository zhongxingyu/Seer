 package story.book.dataclient;
 
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
import story.book.model.Annotation;
 import story.book.model.Illustration;
 import story.book.model.Story;
 import story.book.model.StoryInfo;
import story.book.model.TextIllustration;
 import android.util.Log;
 
 import com.google.gson.*;
 
 /**
  * DataClient is an abstract class that represents 
  * a storage location that contains Stories. Stories
  * can be serialized and deserialized using a DataClient.
  * 
  * @author Anthony Ou
  * @author Vina Nguyen
  * 
  */
 public abstract class DataClient {
 
 	protected static Gson Gsonclient;
 
 	protected DataClient() {
 		Gsonclient = new GsonBuilder()
 		.registerTypeAdapter(Illustration.class, new IllustrationDeserialiser())
 		.excludeFieldsWithModifiers(Modifier.TRANSIENT)
 		.setPrettyPrinting()
 		.create();
 	}
 
 	/**
 	 * Custom deserializer for illustration class
 	 * http://stackoverflow.com/questions/3629596/deserializing-an-abstract-class-in-gson
 	 * 
 	 */
 	public class IllustrationDeserialiser 
 	implements JsonDeserializer<Illustration<?>>, JsonSerializer<Illustration<?>> {
 
 		@Override
 		public Illustration<?> deserialize(
 				JsonElement jsonElement, 
 				Type typeOfT, 
 				JsonDeserializationContext context) {
 
 			JsonObject jsonObj = jsonElement.getAsJsonObject();
 			String className = jsonObj.get("ILLUSTRATIONSTORY").getAsString();
 			try {
 				Class<?> clz = Class.forName(className);
 				return context.deserialize(jsonElement, clz);
 			} catch (ClassNotFoundException e) {
 				Log.d("error parsing Illustration", "DataClient");
 				e.printStackTrace();
				return new TextIllustration("error deserializing");
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
 	protected <T> T unSerialize(String serial, Type type){
 		return Gsonclient.fromJson(serial, type);
 	}
 	
 	/**
 	 * Really basic search of some search term which depends on 
 	 * subclasses implementing getStoryInfoList()
 	 * 
 	 * @param searchTerm the term to search for as a string
 	 * @return an array list of story info's that match this search term
 	 */
 	public ArrayList<StoryInfo> search(String searchTerm) {
 		ArrayList<StoryInfo> hits = new ArrayList<StoryInfo>();
 		for(StoryInfo i : getStoryInfoList()) {
 			if(	i.getAuthor().toLowerCase().contains(
 					searchTerm.toLowerCase()) 			||
 					i.getGenre().toLowerCase().contains(
 							searchTerm.toLowerCase()) 	||
 					i.getSynopsis().toLowerCase().contains(
 							searchTerm.toLowerCase()) 	||
 					i.getTitle().toLowerCase().contains(
 							searchTerm.toLowerCase()))
 				hits.add(i);
 		}
 		return hits;
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
