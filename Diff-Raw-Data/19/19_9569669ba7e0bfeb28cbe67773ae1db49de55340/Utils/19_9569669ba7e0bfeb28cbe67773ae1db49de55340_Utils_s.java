 package utils;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import json.JsonExclude;
 import org.apache.commons.lang.StringUtils;
 import play.i18n.Messages;
 
 import java.lang.reflect.Modifier;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: sheldon
  * Date: 3/21/12
  * Time: 4:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Utils {
     /**
      * prepare for the right json structure when returning response
      * 
      * @param obj
      * @return
      */
     public static String toJson(Object obj) {
         Map returnObj = new HashMap();
         List list = new ArrayList();
         if (obj instanceof List) {
             list = (List) obj;
         } else {
             list.add(obj);
         }
 
         returnObj.put("list", list);
         returnObj.put("total", list.size());
 
         return (new GsonBuilder())
                .setDateFormat("MM/dd/yy")
                 .setExclusionStrategies(new ExclusionStrategy() {
             @Override
             public boolean shouldSkipField(FieldAttributes field) {
                 return field.getAnnotation(JsonExclude.class) != null;
             }
 
             @Override
             public boolean shouldSkipClass(Class<?> clazz) {
                 return clazz.getAnnotation(JsonExclude.class) != null;
             }
         }).create().toJson(returnObj);
     }
 
     /**
      * total only
      * @param total
      * @return
      */
     public static String toJson(int total) {
         Map returnObj = new HashMap();
         returnObj.put("total", total);
         return new Gson().toJson(returnObj);
     }
     /**
      * converts java message properties to javascript map
      *
      * @param locale
      * @return
      */
     public static String makeJSMessages(String locale) {
         Properties properties = Messages.all(locale);
         Iterator keys = properties.keySet().iterator();
         List<String> keyValuePairs = new ArrayList<String>();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             String value = (String) properties.get(key);
             keyValuePairs.add("\"" + key.replaceAll("\"", "\\\"") + "\":\"" + value.replaceAll("\"", "\\\"") + "\""); // TODO how about escaping :
         }
         
         String s = "{\n" + StringUtils.join(keyValuePairs, ",\n") + "\n}";
         return s;
     }
 
     /**
      * Formats dates into easy to read formats, such as: "3 days ago", "tomorrow"
      * <br/>
      * Assume date passed is always midnight 12am
      */
     public String easyToReadDateFormat(Date date) {
         long now = System.currentTimeMillis();
         long diff = date.getTime() - now;
         float inDays = diff/86400000L;
         if (inDays > 0) {
             if (inDays < 1) {
                 return "today";
             } else if (inDays <  2) {
                 return "tomorrow";
             } else if (inDays < 3) {
                 return "in two days";
             } else if (inDays < 8) {
                 return "in " + inDays + " days";
             } else {
                 return (new SimpleDateFormat("MMM dd")).format(date);
             }
         } else {
             if (inDays > -1) {
                 return "yesterday";
             } else if (inDays > -2) {
                 return "two days ago";
             } else if (inDays > -7) {
                 return inDays + " days ago";
             } else {
                 return (new SimpleDateFormat("MMM dd")).format(date);
             }
         }
     }
 }
