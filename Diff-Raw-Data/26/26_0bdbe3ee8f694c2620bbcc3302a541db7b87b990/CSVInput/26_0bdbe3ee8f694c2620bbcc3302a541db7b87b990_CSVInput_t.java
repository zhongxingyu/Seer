 package org.agmip.translators.csv;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.agmip.ace.AcePathfinder;
 import org.agmip.ace.util.AcePathfinderUtil;
 import org.agmip.core.types.TranslatorInput;
 
 /**
  * This class converts CSV formatted files into the AgMIP ACE JSON
  * format. It uses a common file pattern as described below.
  *
  * <p><b>First Column Descriptors</b></p>
  * <p># - Lines with the first column text containing only a "#" is considered 
  * a header row</p>
  * <p>! - Lines with the first column text containing only a "!" are considered
  * a comment and not parsed.
  *
  * The first header/datarow(s) are metadata (or global data) if there are multiple
  * rows of metadata, they are considered to be a collection of experiments. 
  *
  */
 public class CSVInput implements TranslatorInput {
 
     private static Logger LOG = LoggerFactory.getLogger(CSVInput.class);
     private HashMap<String, HashMap<String, HashMap<String, Object>>> finalMap;
     private HashMap<String, HashMap<String, Object>> expMap, weatherMap, soilMap; // Storage maps
     private HashMap<String, String> idMap;
    private ArrayList<String> orderring;
     private String listSeparator;
     private AcePathfinder pathfinder = AcePathfinderUtil.getInstance();
 
     private enum HeaderType {
 
         UNKNOWN, // Probably uninitialized
         SUMMARY, // #
         SERIES   // %
     }
 
     private static class CSVHeader {
 
         private final ArrayList<String> headers;
         private final ArrayList<Integer> skippedColumns;
 
         public CSVHeader(ArrayList<String> headers, ArrayList<Integer> sc) {
             this.headers = headers;
             this.skippedColumns = sc;
         }
 
         public CSVHeader() {
             this.headers = new ArrayList<String>();
             this.skippedColumns = new ArrayList<Integer>();
         }
 
         public ArrayList<String> getHeaders() {
             return headers;
         }
 
         public ArrayList<Integer> getSkippedColumns() {
             return skippedColumns;
         }
     }
 
     public CSVInput() {
         expMap = new HashMap<String, HashMap<String, Object>>();
         weatherMap = new HashMap<String, HashMap<String, Object>>();
         soilMap = new HashMap<String, HashMap<String, Object>>();
         idMap = new HashMap<String, String>();
        orderring = new ArrayList<String>();
         finalMap = new HashMap<String, HashMap<String, HashMap<String, Object>>>();
         this.listSeparator = ",";
         finalMap.put("experiments", expMap);
         finalMap.put("weather", weatherMap);
         finalMap.put("soil", soilMap);
     }
 
     public Map readFile(String fileName) throws Exception {
         if (fileName.toUpperCase().endsWith("CSV")) {
             readCSV(new FileInputStream(fileName));
         } else if (fileName.toUpperCase().endsWith("ZIP")) {
             //Handle a ZipInputStream instead
             LOG.debug("Launching zip file handler");
             ZipFile zf = new ZipFile(fileName);
             Enumeration<? extends ZipEntry> e = zf.entries();
             while (e.hasMoreElements()) {
                 ZipEntry ze = (ZipEntry) e.nextElement();
                 LOG.debug("Entering file: " + ze);
                 readCSV(zf.getInputStream(ze));
             }
             zf.close();
         }
         return cleanUpFinalMap();
     }
 
     protected void readCSV(InputStream fileStream) throws Exception {
         HeaderType section = HeaderType.UNKNOWN;
         CSVHeader currentHeader = new CSVHeader();
         String[] nextLine;
         BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
         
         // Check to see if this is an international CSV. (;, vs ,.)
         setListSeparator(br);
         CSVReader reader = new CSVReader(br, this.listSeparator.charAt(0));
 
         // Clear out the idMap for every file created.
         idMap.clear();
         int ln = 0;
 
         while ((nextLine = reader.readNext()) != null) {
             ln++;
             LOG.debug("Line number: " + ln);
             if (nextLine[0].startsWith("!")) {
                 LOG.debug("Found a comment line");
                 continue;
             } else if (nextLine[0].startsWith("#")) {
                 LOG.debug("Found a summary header line");
                 section = HeaderType.SUMMARY;
                 currentHeader = parseHeaderLine(nextLine);
             } else if (nextLine[0].startsWith("%")) {
                 LOG.debug("Found a series header line");
                 section = HeaderType.SERIES;
                 currentHeader = parseHeaderLine(nextLine);
             } else if (nextLine[0].startsWith("*")) {
                 LOG.debug("Found a complete experiment line");
                 section = HeaderType.SUMMARY;
                 parseDataLine(currentHeader, section, nextLine, true);
             } else if (nextLine[0].startsWith("&")) {
                 LOG.debug("Found a DOME line, skipping");
             } else if (nextLine.length == 1) {
                 LOG.debug("Found a blank line, skipping");
             } else {
                 LOG.debug("Found a data line with [" + nextLine[0] + "] as the index");
                 parseDataLine(currentHeader, section, nextLine, false);
             }
         }
         reader.close();
     }
 
     protected CSVHeader parseHeaderLine(String[] data) {
         ArrayList<String> h = new ArrayList<String>();
         ArrayList<Integer> sc = new ArrayList<Integer>();
 
         int l = data.length;
         for (int i = 1; i < l; i++) {
             if (data[i].startsWith("!")) {
                 sc.add(i);
             }
             if (data[i].trim().length() != 0) {
                 h.add(data[i]);
             }
         }
         return new CSVHeader(h, sc);
     }
 
     protected void parseDataLine(CSVHeader header, HeaderType section, String[] data, boolean isComplete) throws Exception {
         ArrayList<String> headers = header.getHeaders();
         int l = headers.size();
         String dataIndex;
         dataIndex = UUID.randomUUID().toString();
 
         if (!isComplete) {
             if (idMap.containsKey(data[0])) {
                 dataIndex = idMap.get(data[0]);
             } else {
                 idMap.put(data[0], dataIndex);
             }
         }
         if (data[1].toLowerCase().equals("event")) {
             for (int i = 3; i < data.length; i++) {
                 String var = data[i].toLowerCase();
                 i++;
                 if (i < data.length) {
                     String val = data[i];
                     LOG.debug("Trimmed var: " + var.trim() + " and length: " + var.trim().length());
                     if (var.trim().length() != 0 && val.trim().length() != 0) {
                         LOG.debug("INSERTING! Var: "+var+" Val: "+val);
                         insertValue(dataIndex, var, val);
                     }
                 }
             }
             LOG.debug("Leaving event loop");
         } else if (header.getSkippedColumns().isEmpty()) {
             for (int i = 0; i < l; i++) {
                 if (!data[i + 1].equals("")) {
                     insertValue(dataIndex, headers.get(i), data[i + 1]);
                 }
             }
         } else {
             ArrayList<Integer> skipped = header.getSkippedColumns();
             for (int i = 0; i < l; i++) {
                 if (!data[i + 1].equals("")) {
                     if (!skipped.contains(i + 1)) {
                         insertValue(dataIndex, headers.get(i), data[i + 1]);
                     }
                 }
             }
         }
     }
 
     protected void insertValue(String index, String variable, String value) throws Exception {
         try {
             String var = variable.toLowerCase();
             HashMap<String, HashMap<String, Object>> topMap;
             if (var.equals("wst_id") || var.equals("soil_id")) {
                insertIndex(expMap, index, true);
                 HashMap<String, Object> temp = expMap.get(index);
                 temp.put(var, value);
             } else {
                 if (pathfinder.isDate(var)) {
                     LOG.debug("Converting date from: " + value);
                     value = value.replace("/", "-");
                     DateFormat f = new SimpleDateFormat("yyyymmdd");
                     Date d = new SimpleDateFormat("yyyy-mm-dd").parse(value);
                     value = f.format(d);
                     LOG.debug("Converting date to: " + value);
 
                 }
             }
            boolean isExperimentMap = false;
             switch (AcePathfinderUtil.getVariableType(var)) {
                 case WEATHER:
                     topMap = weatherMap;
                     break;
                 case SOIL:
                     topMap = soilMap;
                     break;
                 case UNKNOWN:
                     LOG.warn("Putting unknow variable into root: [" + var + "]");
                 default:
                    isExperimentMap = true;
                     topMap = expMap;
                     break;
             }
            insertIndex(topMap, index, isExperimentMap);
             HashMap<String, Object> currentMap = topMap.get(index);
             AcePathfinderUtil.insertValue(currentMap, var, value);
         } catch (Exception ex) {
             throw new Exception(ex);
         }
     }
 
    protected void insertIndex(HashMap<String, HashMap<String, Object>> map, String index, boolean isExperimentMap) {
         if (!map.containsKey(index)) {
             map.put(index, new HashMap<String, Object>());
            if (isExperimentMap) {
                orderring.add(index);
            }

         }
     }
 
     protected HashMap<String, ArrayList<HashMap<String, Object>>> cleanUpFinalMap() {
         HashMap<String, ArrayList<HashMap<String, Object>>> base = new HashMap<String, ArrayList<HashMap<String, Object>>>();
         ArrayList<HashMap<String, Object>> experiments = new ArrayList<HashMap<String, Object>>();
         ArrayList<HashMap<String, Object>> weathers = new ArrayList<HashMap<String, Object>>();
         ArrayList<HashMap<String, Object>> soils = new ArrayList<HashMap<String, Object>>();
 
        for (String id : orderring) {
        //for (HashMap<String, Object> ex : expMap.values()) {
            HashMap<String, Object> ex = expMap.get(id);
             ex.remove("weather");
             ex.remove("soil");
             if (ex.size() == 2 && ex.containsKey("wst_id") && ex.containsKey("soil_id")) {
             } else if (ex.size() == 1 && (ex.containsKey("wst_id") || ex.containsKey("soil_id"))) {
             } else {
                 experiments.add(ex);
             }
         }
 
         for (Object wth : weatherMap.values()) {
             if (wth instanceof HashMap) {
                 @SuppressWarnings("unchecked")
                 HashMap<String, Object> temp = (HashMap<String, Object>) wth;
                 if (temp.containsKey("weather")) {
                     @SuppressWarnings("unchecked")
                     HashMap<String, Object> weather = (HashMap<String, Object>) temp.get("weather");
                     if (weather.size() == 1 && weather.containsKey("wst_id")) {
                     } else {
                         weathers.add(weather);
                     }
                 }
             }
         }
 
         for (Object sl : soilMap.values()) {
             if (sl instanceof HashMap) {
                 @SuppressWarnings("unchecked")
                 HashMap<String, Object> temp = (HashMap<String, Object>) sl;
                 if (temp.containsKey("soil")) {
                     @SuppressWarnings("unchecked")
                     HashMap<String, Object> soil = (HashMap<String, Object>) temp.get("soil");
                     if (soil.size() == 1 && soil.containsKey("soil_id")) {
                     } else {
                         soils.add(soil);
                     }
                 }
             }
         }
 
         base.put("experiments", experiments);
         base.put("weathers", weathers);
         base.put("soils", soils);
         return base;
     }
     
     protected void setListSeparator(BufferedReader in) throws Exception {
         // Set a mark at the beginning of the file, so we can get back to it.
         in.mark(7168);
         String sample = "";
         while ((sample = in.readLine()) != null) {
             if (sample.startsWith("#")) {
                 String listSeperator = sample.substring(1,2);
                 LOG.info("FOUND SEPARATOR: "+listSeperator);
                 this.listSeparator = listSeperator;
                 break;
             }
         }
         in.reset();
     }
 }
