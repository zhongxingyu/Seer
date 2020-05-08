 package gov.usgs.cida.coastalhazards.gson.adapter;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import gov.usgs.cida.coastalhazards.model.summary.Full;
 import gov.usgs.cida.coastalhazards.model.summary.Publication;
 import gov.usgs.cida.coastalhazards.model.summary.Publication.PublicationType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This is here because we want to keep the database simple but the JSON clear
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class FullSummaryAdapter implements JsonSerializer<Full>, JsonDeserializer<Full> {
 
     @Override
     public JsonElement serialize(Full src, Type typeOfSrc, JsonSerializationContext context) {
         JsonObject fullSummary = new JsonObject();
         
         fullSummary.add("title", context.serialize(src.getTitle()));
         fullSummary.add("text", context.serialize(src.getText()));
         
         JsonObject publications = new JsonObject();
         for (PublicationType type : PublicationType.values()) {
             List<Publication> typedPubs = Publication.getTypedPublications(src.getPublications(), type);
             publications.add(type.name(), context.serialize(typedPubs));
         }
         fullSummary.add("publications", publications);
         
         return fullSummary;
     }
 
     @Override
     public Full deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         Full result = new Full();
         if (json instanceof JsonObject) {
             JsonObject fullJson = (JsonObject)json;
             result.setTitle(fullJson.getAsJsonPrimitive("title").getAsString());
             result.setText(fullJson.getAsJsonPrimitive("text").getAsString());
             
             List<Publication> fullPubList = new LinkedList<>();
             JsonObject publications = fullJson.getAsJsonObject("publications");
             for (PublicationType type : PublicationType.values()) {
                 JsonArray typedArray = publications.getAsJsonArray(type.name());
                 if (typedArray != null) {
                     List<Map<String,Object>> typeList = context.deserialize(typedArray, ArrayList.class);
                     for(Map<String,Object> pubMap : typeList) {
                         if (!pubMap.containsKey(Publication.ID) ||
                                 !pubMap.containsKey(Publication.TITLE) ||
                                 !pubMap.containsKey(Publication.LINK)) {
                             throw new IllegalStateException("Expected publication, was not a publication");
                         }
                         Publication pub = new Publication();
                         Number id = (Number)pubMap.get(Publication.ID);
                         pub.setId(id.longValue());
                         pub.setTitle((String)pubMap.get(Publication.TITLE));
                         pub.setLink((String)pubMap.get(Publication.LINK));
                         pub.setType(type);
                         fullPubList.add(pub);
                     }
                 }
             }
             result.setPublications(fullPubList);
         }
         return result;
     }
 
 }
