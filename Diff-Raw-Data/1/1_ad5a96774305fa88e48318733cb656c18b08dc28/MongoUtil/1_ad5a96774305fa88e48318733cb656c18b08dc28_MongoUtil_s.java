 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.ua.dicoogle.mongoplugin;
 
 import java.util.HashMap;
 import java.net.URI;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.ArrayList;
 import org.dcm4che2.data.Tag;
 import pt.ua.dicoogle.sdk.datastructs.SearchResult;
 
 /**
  *
  * @author Louis
  */
 public class MongoUtil {
 
     public static List<SearchResult> getListFromResult(List<DBObject> dbObjs, URI location, float score) {
         ArrayList<SearchResult> result = new ArrayList<SearchResult>();
         String strSOPUID = Dictionary.getInstance().tagName(Tag.SOPInstanceUID);
         for (int i = 0; i < dbObjs.size(); i++) {
             SearchResult searchResult;
             if (dbObjs.get(i).get(strSOPUID) != null) {
                 String str = location.toString() + dbObjs.get(i).get(strSOPUID);
                 URI uri = null;
                 try {
                     uri = new URI(str);
                 } catch (URISyntaxException e) {
                 }
                 HashMap<String, Object> map = new HashMap<String, Object>();
                 HashMap<String, Object> mapTemp = (HashMap<String, Object>) dbObjs.get(i).toMap();
                 for (String mapKey : mapTemp.keySet()) {
                     if (mapTemp.get(mapKey) == null) {
                         map.put(mapKey, mapTemp.get(mapKey));
                     } else {
                         map.put(mapKey, mapTemp.get(mapKey).toString());
                     }
                 }
                 searchResult = new SearchResult(uri, score, map);
                 result.add(searchResult);
             }
         }
         return result;
     }
 
     private static BasicDBObject decodeStringToQuery(String strQuery) {
         BasicDBObject query;
         Object obj, lowObj, highObj;
         String str = "", field = "", lowValue = "", highValue = "";
         int length = strQuery.length(), cmp = 0;
         char currentChar;
         boolean isField = true, isInclusiveBetween = false, isExclusiveBetween = false, isNot = false;
         while (cmp < length) {
             currentChar = strQuery.charAt(cmp);
             cmp++;
             switch (currentChar) {
                 case ':':
                     if (isField) {
                         isField = false;
                         field = str;
                         str = "";
                     }
                     if (str.equalsIgnoreCase("Numeric")) {
                         str = "";
                     }
                     break;
                 case '[':
                     isInclusiveBetween = true;
                     str = "";
                     break;
                 case ']':
                     highValue = str;
                     break;
                 case '{':
                     isExclusiveBetween = true;
                     str = "";
                     break;
                 case '}':
                     highValue = str;
                     break;
                 case ' ':
                     if (str.equalsIgnoreCase("NOT")) {
                         isNot = true;
                         str = "";
                         break;
                     }
                     String temp = strQuery.substring(cmp, cmp + 2);
                     if (temp.equalsIgnoreCase("TO")) {
                         lowValue = str;
                         str = "";
                         cmp += 2;
                     }
                     break;
                 default:
                     str += currentChar;
                     break;
             }
         }
         if (isInclusiveBetween || isExclusiveBetween) {
             try {
                 lowObj = Double.parseDouble(lowValue);
             } catch (NumberFormatException e) {
                 lowObj = lowValue;
             }
             try {
                 highObj = Double.parseDouble(highValue);
             } catch (NumberFormatException e) {
                 highObj = highValue;
             }
             query = madeQueryIsBetween(field, lowObj, highObj, isInclusiveBetween);
             return query;
         }
         try {
             obj = Double.parseDouble(str);
             query = madeQueryIsValue(field, obj, isNot);
         } catch (NumberFormatException e) {
             obj = str;
             query = madeQueryIsValueRegexInsensitive(field, obj, isNot);
         }
         return query;
     }
 
     public static BasicDBObject parseStringToQuery(String strQuery) {
         BasicDBObject query = null;
         String str = "";
         char currentChar;
         int cmp = 0, length, nbParOpen = 0, nbBrackets = 0;
         boolean and = false, or = false, isBlank = true;
         if (strQuery == null || strQuery.equalsIgnoreCase("")) {
             return madeQueryFindAll();
         }
         length = strQuery.length();
         for (int i = 0; i < length; i++) {
             currentChar = strQuery.charAt(i);
             if (currentChar != ' ' && currentChar != '*' && currentChar != '"' && currentChar != ':') {
                 isBlank = false;
             }
         }
         if (isBlank) {
             return madeQueryFindAll();
         }
         while (cmp != length) {
             currentChar = strQuery.charAt(cmp);
             cmp++;
             switch (currentChar) {
                 case '{':
                 case '[':
                     str += currentChar;
                     nbBrackets++;
                     break;
                 case '}':
                 case ']':
                     str += currentChar;
                     nbBrackets--;
                     break;
                 case '(':
                     if (nbParOpen != 0) {
                         str += currentChar;
                     }
                     nbParOpen++;
                     break;
                 case ')':
                     nbParOpen--;
                     if (nbParOpen == 0) {
                         if (!and && !or) {
                             query = parseStringToQuery(str);
                         }
                         if (and) {
                             query = madeQueryAND(query, parseStringToQuery(str));
                             and = false;
                         }
                         if (or) {
                             query = madeQueryOR(query, parseStringToQuery(str));
                             or = false;
                         }
                         str = "";
                     } else {
                         str += currentChar;
                     }
                     break;
                 case ' ':
                     if (str.equalsIgnoreCase("NOT")) {
                         str += currentChar;
                         break;
                     }
                     if (nbBrackets != 0) {
                         str += currentChar;
                         break;
                     }
                     if (nbParOpen != 0) {
                         str += currentChar;
                         break;
                     }
                     if (str.equalsIgnoreCase("AND") || str.equalsIgnoreCase("OR")) {
                         if (str.equalsIgnoreCase("AND")) {
                             and = true;
                         } else {
                             or = true;
                         }
                         str = "";
                     } else {
                         String temp = "";
                         if (cmp + 3 < length) {
                             temp = strQuery.substring(cmp, cmp + 3);
                         }
                         if (temp.equalsIgnoreCase("AND") || temp.equalsIgnoreCase("OR ")) {
                             if (!and && !or) {
                                 if (!str.equals("")) {
                                     query = decodeStringToQuery(str);
                                 }
                             }
                             if (and) {
                                 query = madeQueryAND(query, decodeStringToQuery(str));
                                 and = false;
                             }
                             if (or) {
                                 query = madeQueryOR(query, decodeStringToQuery(str));
                                 or = false;
                             }
                             str = "";
                         }
                     }
                     break;
                 default:
                     str += currentChar;
                     break;
             }
         }
         if (!str.equals("")) {
             if (!and && !or) {
                 query = decodeStringToQuery(str);
             }
             if (and) {
                 query = madeQueryAND(query, decodeStringToQuery(str));
             }
             if (or) {
                 query = madeQueryOR(query, decodeStringToQuery(str));
             }
         }
         return query;
     }
 
     private static BasicDBObject madeQueryFindAll() {
         BasicDBObject query = new BasicDBObject();
         return query;
     }
 
     private static BasicDBObject madeQueryIsValue(String field, Object value, boolean isNot) {
         BasicDBObject query = new BasicDBObject();
         String str = field;
         if (!isNot) {
             query.put(str, value);
         } else {
             query.put(str, new BasicDBObject("$ne", value));
         }
         return query;
     }
 
     private static BasicDBObject madeQueryIsValueRegexInsensitive(String field, Object value, boolean isNot) {
         BasicDBObject query = new BasicDBObject();
         String str = field;
         String strValue = (String) value;
         if (strValue.endsWith(".*")) {
             strValue = strValue.substring(0, strValue.length() - 1);
         } else if (strValue.endsWith("*")) {
             strValue = strValue.substring(0, strValue.length() - 1);
         }
         if (!isNot) {
             query.put(str, new BasicDBObject("$regex", "^" + strValue + ".*").append("$options", "i"));
         } else {
             query.put(str, new BasicDBObject("$ne", strValue));
         }
         return query;
     }
 
     private static BasicDBObject madeQueryIsBetween(String field, Object lowValue, Object highValue, boolean isInclusive) {
         BasicDBObject query = new BasicDBObject();
         String str = field;
         if (!isInclusive) {
             query.put(str, new BasicDBObject("$gt", lowValue).append("$lt", highValue));
         } else {
             query.put(str, new BasicDBObject("$gte", lowValue).append("$lte", highValue));
         }
         return query;
     }
 
     private static BasicDBObject madeQueryAND(BasicDBObject dbObj1, BasicDBObject dbObj2) {
         BasicDBObject query = new BasicDBObject();
         List<BasicDBObject> listObj = new ArrayList<BasicDBObject>();
         listObj.add(dbObj1);
         listObj.add(dbObj2);
         query.put("$and", listObj);
         return query;
     }
 
     private static BasicDBObject madeQueryOR(BasicDBObject dbObj1, BasicDBObject dbObj2) {
         BasicDBObject query = new BasicDBObject();
         List<BasicDBObject> listObj = new ArrayList<BasicDBObject>();
         listObj.add(dbObj1);
         listObj.add(dbObj2);
         query.put("$or", listObj);
         return query;
     }
 
     public static void printResult(DBObject[] result) {
         System.out.println("Result :");
         for (int i = 0; i < result.length; i++) {
             System.out.println(result[i]);
         }
     }
 }
