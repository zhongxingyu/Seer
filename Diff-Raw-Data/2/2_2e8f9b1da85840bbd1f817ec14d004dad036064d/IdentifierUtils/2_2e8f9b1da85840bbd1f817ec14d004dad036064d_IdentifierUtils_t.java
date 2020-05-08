 package org.otherobjects.cms.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.otherobjects.cms.model.CompositeDatabaseId;
 
 public class IdentifierUtils
 {
     private static final Pattern UUID_PATTERN = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
 
    //private static final Pattern DB_COMPOSITE_ID_PATTERN = Pattern.compile("^([^-]+)-.*$");
 
     /**
      *
      * @param potentialUuid
      * @return true if the potentialUuid matches the uuid pattern as per rfc4122 @see <a href="http://tools.ietf.org/html/rfc4122"> A Universally Unique IDentifier (UUID) URN Namespace</a>
      */
     public static boolean isUUID(String potentialUuid)
     {
         return UUID_PATTERN.matcher(potentialUuid).matches();
     }
 
     /**
      * Creates a CompositeDatabaseIt object from an id string. Returns null if not in the correct format.
      * 
      * @param compositeId compositeId of form com.some.package.Class-123
      * @return CompositeDatabaseId object or null if compositeId was of wrong format or some error occurred
      */
     public static CompositeDatabaseId getCompositeDatabaseId(String compositeId)
     {
         CompositeDatabaseId compositeDatabaseId = null;
         try
         {
             Pattern DB_COMPOSITE_ID_PATTERN2 = Pattern.compile("^([^-]+)-(.*)$");
             Matcher matcher = DB_COMPOSITE_ID_PATTERN2.matcher(compositeId);
             if (matcher.matches())
                 return new CompositeDatabaseId(matcher.group(1), matcher.group(2));
         }
         catch (Exception e)
         {
             // TODO Explain why we ignore exception
             return null;
         }
 
         return compositeDatabaseId;
     }
 
 }
