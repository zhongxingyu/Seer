 package ddth.dasp.framework.utils;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ddth.dasp.common.utils.JsonUtils;
 
 public class DPathUtils {
 
     private final static Pattern PATTERN_INDEX = Pattern.compile("^\\[(\\d+)\\]$");
 
     /**
      * Extracts a value from the target object using DPath expression.
      * 
      * @param target
      * @param dPath
      */
     public static Object getValue(final Object target, final String dPath) {
         String[] paths = dPath.split("\\.");
         Object result = target;
         for (String path : paths) {
             result = extractValue(result, path);
         }
         return result;
     }
 
     /**
      * Sets a value to the target object using DPath expression.
      * 
      * @param target
      * @param dPath
      * @param value
      */
     @SuppressWarnings("unchecked")
     public static void setSetValue(final Object target, final String dPath, final Object value) {
         String[] paths = dPath.split("\\.");
         Object cursor = target;
         // "seek"to the correct position
         for (int i = 0; i < paths.length - 1; i++) {
             cursor = extractValue(cursor, paths[i]);
         }
         String index = paths[paths.length - 1];
         Matcher m = PATTERN_INDEX.matcher(index);
         if (m.matches() || "[]".equals(index)) {
             int i = "[]".equals(index) ? Integer.MAX_VALUE : Integer.parseInt(m.group(1));
             if (cursor instanceof List<?>) {
                 List<Object> temp = (List<Object>) cursor;
                 if (i < 0) {
                     throw new IllegalArgumentException("Invalid index [" + i + "]!");
                 }
                 if (i >= temp.size()) {
                     temp.add(value);
                 } else {
                     temp.remove(i);
                     temp.add(i, value);
                 }
             } else {
                 throw new IllegalArgumentException("Target object is not a list or readonly!");
             }
        } else if (target instanceof Map<?, ?>) {
            ((Map<Object, Object>) target).put(index, value);
         } else {
             throw new IllegalArgumentException("Target object is not writable!");
         }
     }
 
     private static Object extractValue(Object target, String index) {
         if (target == null) {
             return null;
         }
         Matcher m = PATTERN_INDEX.matcher(index);
         if (m.matches()) {
             int i = Integer.parseInt(m.group(1));
             if (target instanceof Object[]) {
                 return ((Object[]) target)[i];
             }
             if (target instanceof List<?>) {
                 return ((List<?>) target).get(i);
             }
             throw new IllegalArgumentException("Expect an array or list!");
         }
         if (target instanceof Map<?, ?>) {
             return ((Map<?, ?>) target).get(index);
         }
         throw new IllegalArgumentException();
     }
 
     public static void main(String[] args) {
        String jsonString = "{\"DB\":{\"TYPE\":\"MYSQL\", \"HOST\":\"127.0.0.1\", \"USER\":\"vcatalog_local\", \"PASSWORD\":\"vcatalog_local\", \"DATABASE\":\"vcatalog_local\", \"SETUP_SQLS\":[\"SET NAMES 'utf8'\", \"HEHE\"]}}";
         Map<?, ?> data = JsonUtils.fromJson(jsonString, Map.class);
 
        String path = "DB.SETUP_SQLS.[0]";
         System.out.println(getValue(data, path));
        setSetValue(data, path, "EMPTY");
         System.out.println(getValue(data, path));
 
         System.out.println(data);
     }
 }
