 package org.agmip.translators.dssat;
 
 import java.io.BufferedReader;
 import java.io.CharArrayReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import org.agmip.core.types.AdvancedHashMap;
 import org.agmip.core.types.TranslatorInput;
 
 /**
  * DSSAT Experiment Data I/O API Class
  *
  * @author Meng Zhang
  * @version 1.0
  */
 public abstract class DssatCommonInput implements TranslatorInput {
 
     protected String[] flg = new String[3];
     protected String defValR = "-99.0";
     protected String defValC = "";
     protected String defValI = "-99";
     protected String defValD = "20110101";
     protected String jsonKey = "unknown";
 
     /**
      * DSSAT Data Output method for Controller using
      * 
      * @param m  The holder for BufferReader objects for all files
      * @return result data holder object
      */
     protected abstract AdvancedHashMap readFile(HashMap m) throws IOException;
 
     /**
      * DSSAT XFile Data input method
      * 
      * @param arg0  file name
      * @return result data holder object
      */
     @Override
     public AdvancedHashMap readFile(String arg0) {
 
         AdvancedHashMap ret = new AdvancedHashMap();
         String filePath = arg0;
 
         try {
             // read file by file
             ret = readFile(getBufferReader(filePath));
 
         } catch (Exception e) {
             //System.out.println(e.getMessage());
             e.printStackTrace();
         }
 
         return ret;
     }
 
     /**
      * Set reading flgs for reading lines
      * 
      * @param line  the string of reading line
      */
     protected void judgeContentType(String line) {
         // Section Title line
         if (line.startsWith("*")) {
 
             setTitleFlgs(line);
 
         } // Data title line
         else if (line.startsWith("@")) {
 
             flg[1] = line.substring(1).trim().toLowerCase();
             flg[2] = "title";
 
         } // Comment line
         else if (line.startsWith("!")) {
 
             flg[2] = "comment";
 
         } // Data line
         else if (!line.trim().equals("")) {
 
             flg[2] = "data";
 
         } // Continued blank line
         else if (flg[2].equals("blank")) {
 
             flg[1] = "";
             flg[2] = "blank";
 
         } else {
 
            flg[0] = "";
             flg[1] = "";
             flg[2] = "blank";
         }
     }
 
     /**
      * Set reading flgs for title lines (the line marked with *)
      * 
      * @param line  the string of reading line
      */
     protected abstract void setTitleFlgs(String line);
 
     /**
      * Translate data str from "yyddd" to "yyyymmdd"
      *
      * @param str date string with format of "yyddd"
      * @return result date string with format of "yyyymmdd"
      */
     protected String translateDateStr(String str) {
 
         return translateDateStr(str, "0");
     }
 
     /**
      * Translate data str from "yyddd" to "yyyymmdd" plus days you want
      *
      * @param startDate date string with format of "yyydd"
      * @param strDays the number of days need to be added on
      * @return result date string with format of "yyyymmdd"
      */
     protected String translateDateStr(String startDate, String strDays) {
 
         // Initial Calendar object
         Calendar cal = Calendar.getInstance();
         int days;
         int year;
         if (startDate.length() > 5 || startDate.length() < 4) {
             //throw new Exception("");
             return "-99"; //defValD;
         }
         try {
             startDate = String.format("%05d", Integer.parseInt(startDate));
             days = Double.valueOf(strDays).intValue();
             // Set date with input value
             year = Integer.parseInt(startDate.substring(0, 2));
             year += year <= 15 ? 2000 : 1900; // P.S. 2015 is the cross year for the current version 
             cal.set(Calendar.YEAR, year);
             cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(startDate.substring(2)));
             cal.add(Calendar.DATE, days);
             // translatet to yyddd format
             return String.format("%1$04d%2$02d%3$02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
         } catch (Exception e) {
             // if tranlate failed, then use default value for date
             // sbError.append("! Waring: There is a invalid date [").append(startDate).append("]");
             return "-99"; //formatDateStr(defValD);
         }
 
     }
 
     /**
      * Divide the data in the line into a map
      *
      * @param line The string of line read from data file
      * @param formats The defination of lenght for each data field (String itemName : Integer length)
      * @return the map contains divided data with keys from original string
      */
     protected AdvancedHashMap readLine(String line, LinkedHashMap<String, Integer> formats) {
 
         AdvancedHashMap ret = new AdvancedHashMap();
         int length;
 
         for (String key : formats.keySet()) {
             length = Math.min((Integer) formats.get(key), line.length());
             if (!((String) key).equals("")) {
                 ret.put(key, line.substring(0, length).trim());
             }
             line = line.substring(length);
         }
 
         return ret;
     }
 
     /**
      * Get exname with normal format
      *
      * @return exname
      */
     protected String getExName() {
 
         // TODO
         String ret = "";
 
         return ret;
     }
 
     /**
      * Check if input is a valid value
      *
      * @return check result
      */
     protected boolean checkValidValue(String value) {
         if (value.trim().equals(defValC) || value.equals(defValI) || value.equals(defValR)) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Get BufferReader for each type of file
      *
      * @param filePath the full path of the input file
      * @return result the holder of BufferReader for different type of files
      * @throws FileNotFoundException
      * @throws IOException
      */
     protected HashMap getBufferReader(String filePath) throws FileNotFoundException, IOException {
 
         HashMap result = new HashMap();
         InputStream in;
         LinkedHashMap mapW = new LinkedHashMap();
         LinkedHashMap mapS = new LinkedHashMap();
         String[] tmp = filePath.split("[\\/]");
 
         // If input File is ZIP file
         if (filePath.toUpperCase().endsWith(".ZIP")) {
 
             ZipEntry entry;
             in = new ZipInputStream(new FileInputStream(filePath));
 
             while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                 if (!entry.isDirectory()) {
                     if (entry.getName().matches(".+\\.\\w{2}[Xx]")) {
                         result.put("X", getBuf(in, (int) entry.getSize()));
                     } else if (entry.getName().toUpperCase().endsWith(".WTH")) {
                         mapW.put(entry.getName().toUpperCase(), getBuf(in, (int) entry.getSize()));
                     } else if (entry.getName().toUpperCase().endsWith(".SOL")) {
                         mapS.put(entry.getName().toUpperCase(), getBuf(in, (int) entry.getSize()));
                     } else if (entry.getName().matches(".+\\.\\w{2}[Aa]")) {
                         result.put("A", getBuf(in, (int) entry.getSize()));
                     } else if (entry.getName().matches(".+\\.\\w{2}[Tt]")) {
                         result.put("T", getBuf(in, (int) entry.getSize()));
                     }
                 }
             }
         } // If input File is not ZIP file
         else {
             in = new FileInputStream(filePath);
             if (filePath.matches(".+\\.\\w{2}[Xx]")) {
                 result.put("X", new BufferedReader(new InputStreamReader(in)));
             } else if (filePath.toUpperCase().endsWith(".WTH")) {
                 mapW.put(filePath, new BufferedReader(new InputStreamReader(in)));
             } else if (filePath.toUpperCase().endsWith(".SOL")) {
                 mapS.put(filePath, new BufferedReader(new InputStreamReader(in)));
             } else if (filePath.matches(".+\\.\\w{2}[Aa]")) {
                 result.put("A", new BufferedReader(new InputStreamReader(in)));
             } else if (filePath.matches(".+\\.\\w{2}[Tt]")) {
                 result.put("T", new BufferedReader(new InputStreamReader(in)));
             }
         }
 
         result.put("W", mapW);
         result.put("S", mapS);
         result.put("Z", tmp[tmp.length - 1]);
 
         return result;
     }
 
     /**
      * Get BufferReader object from Zip entry
      *
      * @param in The input stream of zip file
      * @param size The entry size
      * @return result The BufferReader object for current entry
      * @throws IOException
      */
     private BufferedReader getBuf(InputStream in, int size) throws IOException {
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         char[] buf = new char[size];
         br.read(buf);
         return new BufferedReader(new CharArrayReader(buf));
     }
 
     /**
      * compress the data in a map object
      *
      * @param m input map
      */
     protected void compressData(AdvancedHashMap m) {
 
         for (Object key : m.keySet()) {
             if (m.get(key).getClass().equals(ArrayList.class)) {
                 // iterate sub array nodes
                 compressData((ArrayList) m.get(key));
             } else if (m.get(key).getClass().equals(AdvancedHashMap.class)) {
                 // iterate sub data nodes
                 compressData((AdvancedHashMap) m.get(key));
             } else {
                 // ignore other type nodes
             }
         }
 
     }
 
     /**
      * compress the data in an ArrayList object
      *
      * @param arr input ArrayList
      *
      */
     protected void compressData(ArrayList arr) {
 
         AdvancedHashMap fstData = null; // The first data record (Map type)
         AdvancedHashMap cprData = null; // The following data record which will be compressed
 
         for (int i = 0; i < arr.size(); i++) {
             if (arr.get(i).getClass().equals(ArrayList.class)) {
                 // iterate sub array nodes
                 compressData((ArrayList) arr.get(i));
 
             } else if (arr.get(i).getClass().equals(AdvancedHashMap.class)) {
                 // iterate sub data nodes
                 compressData((AdvancedHashMap) arr.get(i));
 
                 // Compress data for current array
                 if (fstData == null) {
                     // Get first data node
                     fstData = (AdvancedHashMap) arr.get(i);
                 } else {
                     cprData = (AdvancedHashMap) arr.get(i);
                     Object[] keys = cprData.keySet().toArray();
                     // The missing data will be given a "" value; Only data item (String type) will be processed
                     for (Object key : fstData.keySet()) {
                         if (fstData.get(key).getClass().equals(String.class) && !cprData.containsKey(key)) {
                             cprData.put(key, "");
                         }
                     }
                     // The repeated data will be deleted; Only data item (String type) will be processed
                     for (Object key : keys) {
                         if (cprData.get(key).getClass().equals(String.class) && cprData.get(key).equals(fstData.get(key))) {
                             cprData.remove(key);
                         }
                     }
                 }
             } else {
             }
         }
     }
 
     /**
      * Add the new item into array by having same key value
      * 
      * @param arr   the target array
      * @param item  the input item which will be added into array
      * @param key   the primary key item's name
      */
     protected void addToArray(ArrayList arr, AdvancedHashMap item, Object key) {
         AdvancedHashMap elem;
         boolean unmatchFlg = true;
         for (int i = 0; i < arr.size(); i++) {
             elem = (AdvancedHashMap) arr.get(i);
             if (!key.getClass().isArray()) {
                 if (elem.get(key).equals(item.get(key))) {
                     elem.put(item);
                     unmatchFlg = false;
                     break;
                 }
             } else {
                 Object[] keys = (Object[]) key;
                 boolean equalFlg = true;
                 for (int j = 0; j < keys.length; j++) {
                     if (!elem.get(keys[j]).equals(item.get(keys[j]))) {
                         equalFlg = false;
                         break;
                     }
                 }
                 if (equalFlg) {
                     elem.put(item);
                     unmatchFlg = false;
                     break;
                 }
             }
         }
         if (unmatchFlg) {
             arr.add(item);
         }
     }
 }
