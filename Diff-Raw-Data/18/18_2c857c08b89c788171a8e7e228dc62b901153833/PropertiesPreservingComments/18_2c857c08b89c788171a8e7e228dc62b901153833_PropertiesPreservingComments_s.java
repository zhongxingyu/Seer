 package gov.nih.nci.cagrid.common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /** 
  *  PropertiesPreservingComments
  *  Properties which maintains comment lines, blank lines, and ordering when stored
  * 
  * @author David Ervin
  * 
  * @created Jun 15, 2007 11:28:40 AM
  * @version $Id: PropertiesPreservingComments.java,v 1.2 2007-06-18 14:20:46 dervin Exp $ 
  */
 public class PropertiesPreservingComments {
     
     private Map<String, Integer> propertyIndices = null;
     private List<String> lines = null;
     
     public PropertiesPreservingComments() {
         propertyIndices = new HashMap<String, Integer>();
         lines = new ArrayList<String>();
     }
     
     
     public void load(File file) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         load(fis);
         fis.close();
     }
     
     
     public void load(InputStream stream) throws IOException {
         StringBuffer rawProps = Utils.inputStreamToStringBuffer(stream);
         BufferedReader reader = new BufferedReader(new StringReader(rawProps.toString()));
         String line = null;
         while ((line = reader.readLine()) != null) {
             if (!"\n".equals(line)) {
                 line = line + "\n";
             }
             lines.add(line);
             if (!line.startsWith("#")) {
                 String key = getPropertyKeyFromLine(line);
                 if (key != null) {
                     propertyIndices.put(key, Integer.valueOf(lines.size() - 1));
                 }
             }
         }
     }
     
     
    public void write(OutputStream stream) {
         PrintWriter writer = new PrintWriter(stream);
         for (String line : lines) {
             writer.print(line);
         }
         writer.flush();
     }
     
     
     public String getProperty(String key) {
         return getProperty(key, null);
     }
     
     
     public String getProperty(String key, String defaultValue) {
         Integer lineIndex = propertyIndices.get(key);
         if (lineIndex == null) {
             return defaultValue;
         }
         String line = lines.get(lineIndex.intValue());
         return getPropertyValueFromLine(line);
     }
     
     
     public Object setProperty(String key, String value) {
         // if the property already exists but is just commented out, uncomment it
         uncommentProperty(key);
         String line = key + "=" + value + "\n";
         String oldValue = null;
         // see if the property exists
         Integer lineIndex = propertyIndices.get(key);
         if (lineIndex != null) {
             oldValue = getProperty(key);
             lines.set(lineIndex.intValue(), line);
         } else {
             lines.add(line);
             propertyIndices.put(key, Integer.valueOf(lines.size() - 1));
         }
         return oldValue;
     }
     
     
     public Object remove(Object key) {
         // if the property exists but is commented, uncomment it so it can be found
         uncommentProperty((String) key);
         Object oldValue = getProperty((String) key);
         Integer lineIndex = propertyIndices.remove(key);
         if (lineIndex != null) {
             lines.remove(lineIndex.intValue());
         }
         return oldValue;
     }
     
     
     /**
      * Comments out the property.
      * 
      * @param key
      *      The key of the property to comment out
      * @return
      *      True if the property existed and was commented out, false otherwise
      */
     public boolean commentOutProperty(String key) {
         boolean commented = false;
         if (propertyIndices.containsKey(key)) {
             Integer lineNum = propertyIndices.remove(key);
             String line = "#" + lines.get(lineNum.intValue());
             lines.set(lineNum.intValue(), line);
             commented = true;
         }
         return commented;
     }
     
     
     /**
      * Uncomments a property
      * 
      * @param key
      *      The key of the property to uncomment
      * @return
      *      True if the property existed and was uncommented
      */
     public boolean uncommentProperty(String key) throws IllegalStateException {
         // since the key is no longer in the properties index, we have to search for it
         int lineNum = 0;
         int foundIndex = -1;
         for (; lineNum < lines.size(); lineNum++) {
             String line = lines.get(lineNum);
             String uncommentedLine = trimLeadingHashes(line);
             String lineKey = getPropertyKeyFromLine(uncommentedLine);
             boolean found = lineKey != null && lineKey.equals(key);
             if (found) {
                 if (foundIndex != -1) {
                     throw new IllegalStateException("A property with key " + key + " was found multiple times!");
                 } else {
                     foundIndex = lineNum;
                 }
             }
         }
         if (foundIndex != -1) {
             lines.set(foundIndex, trimLeadingHashes(lines.get(foundIndex)));
             propertyIndices.put(key, Integer.valueOf(foundIndex));
         }
         return foundIndex != -1;
     }
     
     
     private String getPropertyKeyFromLine(String line) {
         int split = line.indexOf('=');
         return split != -1 ? line.substring(0, split) : null;
     }
     
     
     private String getPropertyValueFromLine(String line) {
         int split = line.indexOf('=');
         if (line.length() > split) {
             return line.substring(split + 1, line.length() - 1);
         }
         return "";
     }
     
     
     private String trimLeadingHashes(String line) {
         int i = 0;
         for (; i < line.length(); i++) {
             if (line.charAt(i) != '#') {
                 break;
             }
         }
         return line.substring(i);
     }
 }
