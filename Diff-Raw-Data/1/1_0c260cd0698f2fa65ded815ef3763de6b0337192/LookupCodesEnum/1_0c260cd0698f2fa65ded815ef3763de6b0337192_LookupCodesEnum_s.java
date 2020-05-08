 package org.agmip.ace;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 
 public enum LookupCodesEnum {
     INSTANCE;
 
     private final HashMap<String,String> modelLookupMap = new HashMap<String, String>();
     private final HashMap<String, HashMap<String,String>> aceLookupMap = new HashMap<String, HashMap<String, String>>();
     private final Logger LOG = LoggerFactory.getLogger("org.agmip.ace.LookupCodesEnum");
 
     LookupCodesEnum() {
         InputStream metadata = getClass().getClassLoader().getResourceAsStream("metadata_codes.csv");
         InputStream crops = getClass().getClassLoader().getResourceAsStream("crop_codes.csv");
         InputStream management = getClass().getClassLoader().getResourceAsStream("management_codes.csv");
         InputStream other = getClass().getClassLoader().getResourceAsStream("other_codes.csv");
 
         loadEmbeddedCSVFile(metadata);
         loadEmbeddedCSVFile(crops);
         loadEmbeddedCSVFile(management);
         loadEmbeddedCSVFile(other);
     }
 
     private void loadEmbeddedCSVFile(InputStream stream) {
         LOG.debug("Loading embedded CSV for Lookup Codes");
         try {
             if (stream != null) {
                 LOG.debug("Found streaming file");
                 CSVReader reader = new CSVReader(new InputStreamReader(stream));
                 ArrayList<String> columnIndex = new ArrayList<String>();
                 ArrayList<String> modelIndex = new ArrayList<String>();
                 String[] line;
                 String[] columnNames = reader.readNext(); // First line should be the column names                
                 
                 int l = columnNames.length;
 
                 for (int i=0; i < l; i++){
                     String currentCol = columnNames[i].toLowerCase();
                     LOG.debug("currentCol: {}", currentCol);
                     if (currentCol.equals("code_display")) {
                         columnIndex.add("variable");
                     } else if (currentCol.equals("code")) {
                         columnIndex.add("code");
                     } else if (currentCol.startsWith("desc")) {
                         columnIndex.add("cn");
                     } else if (currentCol.startsWith("common")) {
                         columnIndex.add("cn");
                     } else if (currentCol.startsWith("latin")) {
                         columnIndex.add("ln");
                     } else if (currentCol.endsWith("_code")) {
                         if (currentCol.equals("crop_code")) {
                             columnIndex.add("code");
                         } else {
                             columnIndex.add("model");
                             String[] tmp = currentCol.split("[_]");
                             modelIndex.add(tmp[0]);
                         }
                     } else {
                         columnIndex.add("");
                     }
                 }
 
                 if (columnIndex.isEmpty()) {
                     LOG.error("Invalid embedded CSV file for configuration. Lookup Codes will be blank.");
                     return;
                 }
 
                 while ((line = reader.readNext()) != null) {
                     HashMap<String, String> entries = new HashMap<String, String>();
                     ArrayList<String> currentVars = new ArrayList<String>();
                     String currentCode = "";
                     int modelId = 0;
                     int colId = line.length;
                     for(int i=0; i < colId; i++) {
                         if (columnIndex.get(i).equals("")) {
                         } else if(columnIndex.get(i).equals("variable")) {
                             // This SHOULD come before the code (always)
                             String[] vars = line[i].toLowerCase().split("[,]");
                             int varsLength = vars.length;
                             for (int vi = 0; vi < varsLength; vi++) {
                                 String v = vars[vi].trim();
                                 if (! v.equals("")) {
                                     currentVars.add(vars[vi].trim());
                                 }
                             }
                         } else if (columnIndex.get(i).equals("code")) {
                             currentCode = line[i].toLowerCase().trim();
                         } else if (columnIndex.get(i).equals("model")) {
                             entries.put(modelIndex.get(modelId), line[i].toLowerCase());
                             modelId++;
                         } else {
                             entries.put(columnIndex.get(i), line[i]);
                         }
                     }
                     // First, put the entries into the main lookup.
                     if (!currentCode.equals("")) {
                         for (String currentVar : currentVars) {
                             currentVar += "_";
                             currentVar += currentCode;
                             LOG.debug("Current Code: {}", currentVar);                        
                             aceLookupMap.put(currentVar, entries);
                         }
                     }
 
                     // IF there are models, build the model-specific mappings
                     if (modelId != 0) {
                         for (String currentVar : currentVars) {
                             for (String model : modelIndex) {
                                 String modelVar = model+"_"+currentVar+"_"+entries.get(model);
                                 modelLookupMap.put(modelVar, currentCode);
                             }
                         }
                     }
                 }
                 reader.close();
             } else {
                 LOG.error("Missing embedded CSV file for configuration. Lookup Codes will be blank.");
             }
         } catch (IOException ex) {
             LOG.debug(ex.toString());
             throw new RuntimeException(ex);
         }
     }
 
     
     public String modelLookup(String key) {
         if (modelLookupMap.containsKey(key)) {
             return modelLookupMap.get(key);
         } else {
             return "";
         }
     }
 
     public HashMap<String, String> aceLookup(String key) {
         if (aceLookupMap.containsKey(key)) {
             return aceLookupMap.get(key);
         } else {
             return new HashMap<String, String>();
         }
     }
 }
