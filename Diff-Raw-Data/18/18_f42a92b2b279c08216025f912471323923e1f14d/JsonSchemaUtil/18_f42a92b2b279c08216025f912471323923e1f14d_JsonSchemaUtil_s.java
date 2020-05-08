 package ee.stacc.transformer.client.util;
 
 import com.google.gwt.json.client.JSONObject;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class JsonSchemaUtil {
   public static JSONObject findElementByPath(JSONObject jsonSchema, String path) {
     Queue<String> pathElements = new LinkedList<String>();
    pathElements.addAll(Arrays.asList(path.split("/")));
     return findElemetsByPath(jsonSchema, pathElements);
   }
 
   private static JSONObject findElemetsByPath(JSONObject jsonSchema, Queue<String> pathElements) {
     if (pathElements.size() == 0) {
       return jsonSchema;
     }
 
     if (jsonSchema.containsKey("type") && jsonSchema.get("type").isString().stringValue().equals("array")){
       JSONObject items = (JSONObject)jsonSchema.get("items");
       // if path is not empty, then array items must be objects, otherwise it is impossible to access fields
       return findElementFromObjectByPath(items, pathElements);
     }
     else {
       return findElementFromObjectByPath(jsonSchema, pathElements);
     }
   }
 
   private static JSONObject findElementFromObjectByPath(JSONObject jsonSchema, Queue<String> pathElements) {
     // get first element
     String elementName = pathElements.remove();
     if (jsonSchema.containsKey("type") && jsonSchema.get("type").isString().stringValue().equals("object")) {
       JSONObject properties = (JSONObject)jsonSchema.get("properties");
       if (!properties.containsKey(elementName)) {
         return null;
       } else {
         JSONObject schemaElement = (JSONObject) properties.get(elementName);
         return findElemetsByPath(schemaElement, pathElements);
       }
     }
     else {
       return null;
     }
   }
 
   public static boolean isSchemaElementRequired(JSONObject schemaElement) {
     if (schemaElement.containsKey("required")
         && schemaElement.get("required").isBoolean() != null) {
       return schemaElement.get("required").isBoolean().booleanValue();
     }
     return false;
   }
 }
