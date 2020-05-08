 package me.tehbeard.BeardAch.dataSource.json;
 
 import java.lang.reflect.Type;
 
 import org.bukkit.Location;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.triggers.ITrigger;
 import me.tehbeard.BeardAch.dataSource.AchievementLoader;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 import me.tehbeard.utils.cuboid.Cuboid;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 public class TriggerJSONParser implements JsonSerializer<ITrigger>,JsonDeserializer<ITrigger>{
 
     private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeHierarchyAdapter(Cuboid.class,new CuboidJSONParser()).registerTypeHierarchyAdapter(Location.class,new LocationJSONParser()).create();
     
     public JsonElement serialize(ITrigger trigger, Type type,
             JsonSerializationContext context) {
         JsonElement element = gson.toJsonTree(trigger);
         element.getAsJsonObject().addProperty("_type", trigger.getClass().getAnnotation(Configurable.class).tag());
         return element;
     }
 
     public ITrigger deserialize(JsonElement element, Type type,
             JsonDeserializationContext context) throws JsonParseException {
         
     	try{
             return gson.fromJson(element, AchievementLoader.triggerFactory.get(element.getAsJsonObject().get("_type").getAsString()));
             }
             catch (NoClassDefFoundError e){
             	BeardAch.printCon("Trigger type failed: " + element.getAsJsonObject().get("_type").getAsString());
             	BeardAch.printDebugCon("Dumping JSON");
             	BeardAch.printDebugCon(element.toString());
             	BeardAch.printDebugCon("END Dumping JSON");
             }
             return null;
     }
 
 }
