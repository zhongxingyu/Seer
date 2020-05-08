 package org.aldeon.jetty;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import org.aldeon.protocol.query.ExampleQuery;
 import org.aldeon.protocol.query.Query;
 
import java.util.HashMap;
import java.util.Map;

 class JsonQueryMapper {
 
     private Gson gson;
    private Map<String, Class<? extends Query>> classes;
 
     public JsonQueryMapper() {
         gson = new Gson();

        classes = new HashMap<>();
        classes.put("example", ExampleQuery.class);
     }
 
     public Query parse(JsonObject json) throws Exception{
 
         String action = json.get("action").getAsString();
 
        if(classes.containsKey(action)) {
            return gson.fromJson(json, classes.get(action));
        } else {
            throw new IllegalArgumentException("Given JSON object does not contain a valid action key");
         }
     }
 }
