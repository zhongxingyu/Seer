 package genericEventProcessor.eventDeserialization;
 
 import java.lang.Class;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class XMLDeserializationStrategy implements DeserializationStrategy {
   private String objectClass;
   private Set<String> fieldNames;
   private Map<String, String> fieldTypes;
   private Map<String, String> fieldValues;
 
  public XMLDeserializationStrategy(String input) {
     objectClass = "";
     fieldNames = new HashSet<String>();
     fieldTypes = new HashMap<String, String>();
     fieldValues = new HashMap<String, String>();
   }
 
   public void parse(String input) {
     String[] lines = input.split("\n");
     for(String line : lines) {
       if(line.startsWith("<object")) {
         parseObjectTag(line);
       } else if(line.startsWith("<field")) {
         parseFieldTag(line);
       }
     }
   }
 
   public Set<String> fieldNames() {
     return fieldNames;
   }
 
   public String fieldType(String fieldName) {
     return fieldTypes.get(fieldName);
   }
 
   public String fieldValue(String fieldName) {
     return fieldValues.get(fieldName);
   }
 
   public String objectClass() {
     return objectClass;
   }
 
   private void parseObjectTag(String line) {
     Pattern pattern = Pattern.compile("class='([^']+)'");
     Matcher matcher = pattern.matcher(line);
     matcher.find();
     objectClass = matcher.group(1);
   }
 
   private void parseFieldTag(String line) {
     String name = "";
     String type = "";
     String value = "";
     {
       Pattern pattern = Pattern.compile(" (\\w+)='([^']+)'");
       Matcher matcher = pattern.matcher(line);
       while(matcher.find()) {
         String k = matcher.group(1);
         String v = matcher.group(2);
         if(k.equals("name")) {
           name = v;
         } else if(k.equals("class")) {
           type = v;
         }
       }
     }
     {
       Pattern pattern = Pattern.compile(">([^<>]+)<");
       Matcher matcher = pattern.matcher(line);
       matcher.find();
       value = matcher.group(1);
     }
     fieldNames.add(name);
     fieldTypes.put(name, type);
     fieldValues.put(name, value);
   }
 }
