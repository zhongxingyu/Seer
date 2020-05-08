 package org.aldeon.jetty;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import org.aldeon.protocol.query.ExampleQuery;
 import org.aldeon.protocol.query.Query;
 
 class JsonQueryMapper {
 
     private Gson gson;
 
     public JsonQueryMapper() {
         gson = new Gson();
     }
 
     public Query parse(JsonObject json) throws Exception{
 
         String action = json.get("action").getAsString();
 
        if(action.equals("example")) {
            return gson.fromJson(json, ExampleQuery.class);
         }

        throw new IllegalArgumentException();
     }
 }
