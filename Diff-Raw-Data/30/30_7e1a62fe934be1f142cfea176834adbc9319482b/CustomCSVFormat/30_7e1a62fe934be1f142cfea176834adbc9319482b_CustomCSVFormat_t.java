 package filemanagmentsystem;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class is responsible for creating a custom CSV format for storing
  * records.
  *
  * @author bspor
  * @param <T> This accepts a type and creates a LinkedHashMap
  */
 public class CustomCSVFormat<T> implements FileFormatStrategy {
     //Constants
 
     private static final String LEFT_PREN = "(";
     private static final String RIGHT_PREN = ")";
     private static final String REGEX_PATTERN = ".*\\(|\\).*";
     private static final String SPLIT = ":";
     private String SPLIT_COMMA = ",";
     private static final String CRLF = "\n";
     //Key value
     private String keyVal = "";
 
     /**
      * This method takes a List of any type and casts it to a LinkedHashmap
      *
      * @param type takes any type of list.
      * @return LinkedHashMap as type T.
      */
     @Override
     public List<T> decode(List type) {
         if (type == null || type.isEmpty()) {
             throw new IllegalArgumentException();
         } else {
             //Put my type list into an array list, making it of type String
             List<String> myList = new ArrayList<>(type);
             //The custom format will store each record as a map.
             Map<String, String> record = new LinkedHashMap<>();
             //Each record will have a key to make easy queries
             Map<String, Map> key = new LinkedHashMap<>();
             //Each record will then be placed into my record list 
             List<Map> recordList = new ArrayList<>();
 
             //Loop through the string
             for (String s : myList) {
                 Pattern pattern = Pattern.compile(REGEX_PATTERN);
                 Matcher matcher = pattern.matcher(s);
                 if (matcher.find()) {
                     //Create my keys
                     keyVal = s;
                 } else {
                     //Create new records 
                     String[] parts = s.split(SPLIT);
                     record.put(parts[0], parts[1]);
                     key.put(keyVal, new LinkedHashMap(record));
                 }
             }
 
             recordList.add(key);
             return (List<T>) recordList;
         }
     }
 
     /**
      * Takes a generic list and encodes it to my custom format
      *
      * @param type any object in a list
      * @return a list of Maps
      */
     @Override
     public List encode(List type) {
         if (type == null || type.isEmpty()) {
             throw new IllegalArgumentException();
         } else {
             //Put my type list into an array list, making it of type String
             List<String> myList = new ArrayList<>(type);
             //The custom format will store each record as a map.
             Map<String, String> record = new LinkedHashMap<>();
             //Each record will have a key to make easy queries
             Map<String, Map> key = new LinkedHashMap<>();
             //Each record will then be placed into my record list 
             List<Map> recordList = new ArrayList<>();
             //assumes header for now
             String possibleHeader = myList.get(0);
             String[] headerList = possibleHeader.split(SPLIT_COMMA);
 
             //Loop through the list
             for (int i = 1; i < myList.size(); i++) {
                 String temp = myList.get(i);
                 String[] values = temp.split(SPLIT_COMMA);
                 for (int j = 0; j < headerList.length; j++) {
                     record.put(headerList[j] + SPLIT, values[j]);
                 }
                 keyVal = LEFT_PREN + i + RIGHT_PREN;
                 key.put(keyVal,
                         new LinkedHashMap(record));
             }
             
             recordList.add(key);
             return recordList;
         }
     }
 
     /**
      * Returns a formated string in the custom format to be written to a file.
      * @param type any type of list will be parsed to a string and written.
      * @return a StringBuilder type string.
      */
     @Override
     public List writeToFile(List type) {
         if (type == null || type.isEmpty()) {
             throw new IllegalArgumentException();
         } else {
             Collection<T> myCollection = encode(type);
             List<StringBuilder> myList = new ArrayList<>();
             StringBuilder sb = new StringBuilder();
             Iterator itr = myCollection.iterator();
            //Loop through the string.
             while (itr.hasNext()) {
                 Map element = (Map) itr.next();
                 String[] key;
                 for (Object o : element.keySet()) {
                     sb.append(o).append(CRLF);
                     Map myRecord = (Map) element.get(o);
                     for (Object o2 : myRecord.keySet()) {
                         sb.append(o2).append(myRecord.get(o2));
                         sb.append(CRLF);
                     }
                 }
                 myList.add(sb);
             }
             return myList;
         }
     }
 }
