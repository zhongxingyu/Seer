 package ixcode.platform.json;
 
 import net.nextquestion.json.*;
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 
 import java.util.*;
 
 public class JsonParser {
     public <T> T parse(String jsonString) {
         return parseFromLexer(createStringLexer(jsonString));
     }
 
     private static <T> T parseFromLexer(JSONLexer jsonLexer) {
         JSONTree jsonTree = parseTree(jsonLexer);
         try {
             Object value = jsonTree.value();
             return parseObject(value);
         } catch (RecognitionException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static JSONTree parseTree(JSONLexer lexer) {
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         JSONParser parser = new JSONParser(tokens);
 
         try {
             JSONParser.value_return r = parser.value();
             return createTreeFrom(tokens, r);
         } catch (RecognitionException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     private static Map<String, JsonValue> parseMap(Map<Object, Object> valueMap) {
         Map<String, JsonValue> resultingMap = new LinkedHashMap<String, JsonValue>();
         for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
             resultingMap.put((String) entry.getKey(), (JsonValue) parseObject(entry.getValue()));
         }
         return resultingMap;
     }
 
     private static <T> T parseObject(Object value) {
         if (value == null) {
             return (T)new JsonNull();
         }
 
         Class<?> valueClass = value.getClass();
 
         if (Map.class.isAssignableFrom(valueClass)) {
             return (T) jsonObjectFrom((Map) value);
         } else if (List.class.isAssignableFrom(valueClass)) {
             return (T) jsonArrayFrom((List) value);
         } else if (String.class.isAssignableFrom(valueClass)) {
             return (T) jsonStringFrom((String) value);
         } else if (Integer.class.isAssignableFrom(valueClass)
                 || Double.class.isAssignableFrom(valueClass)) {
             return (T) jsonNumberFrom((Number)value);
         } else if (Boolean.class.isAssignableFrom(valueClass)) {
             return (T) jsonBooleanFrom((Boolean)value);
         }
 
         throw new RuntimeException("Could not parse a JsonObject or a JsonArray from valueClass " + valueClass.getName());
     }
 
 
 
     private static JSONTree createTreeFrom(CommonTokenStream tokens, JSONParser.value_return r) {
         CommonTree t = (CommonTree) r.getTree();
         CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
         nodes.setTokenStream(tokens);
 
         return new JSONTree(nodes);
     }
 
     private static JSONLexer createStringLexer(String text) {
         return new JSONLexer(new ANTLRStringStream(text));
     }
 
     private static JsonArray jsonArrayFrom(List values) {
         List<JsonValue> jsonValues = new ArrayList<JsonValue>();
         for (Object value : values) {
             jsonValues.add((JsonValue) parseObject(value));
         }
         return new JsonArray(jsonValues);
     }
 
     private static JsonObject jsonObjectFrom(Map valueMap) {
        Map<String, JsonValue> childs = parseMap(valueMap);
        return new JsonObject(childs);
     }
 
     private static JsonString jsonStringFrom(String value) {
         return new JsonString(value);
     }
 
     private static JsonNumber jsonNumberFrom(Number value) {
         return new JsonNumber(value);
     }
 
     private static JsonBoolean jsonBooleanFrom(Boolean value) {
         return new JsonBoolean(value);
     }
 
 }
