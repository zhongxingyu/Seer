 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.briantroy.AlertServer;
 
 import org.apache.log4j.Category;
 
 import java.io.*;
 import java.util.Scanner;
 
 import org.apache.log4j.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author brian.roy
  */
 public class ConfigFileReader {
     ConfigItems tCfg;
 
     /*
      * List of ConfigFileItem objects containing the KVP based
      * config items found in the XML file.
      *
      */
     ArrayList<ConfigFileItem> cfgList = new ArrayList<ConfigFileItem>();
 
    static org.apache.log4j.Logger myLog = org.apache.log4j.Logger.getLogger("main");
    /*
     * Constructor - takes a command line config items object.
     * If the -confFile item is set on command line get the file
     * and parse it into the object.
     */
 
    public ConfigFileReader() {

    }
 
     public Boolean fetchConfig(ConfigItems cfg) {
         tCfg = cfg;
         try{
             loadConfig();
             myLog.info("Config File " + cfg.getConfigItem(AlertServer.CONFIGFILE) + " was read without errors.");
             return true;
         } catch (FileNotFoundException fnE) {
             myLog.error(fnE.getMessage());
            myLog.error("The specified Configuraiton file: " + cfg.getConfigItem(AlertServer.CONFIGFILE) +
                     "does not exist.");
             return false;
         }
         
 
     }
 
     public String getConfigItem(String name) {
         String ret = null;
         int i;
         for(i=0; i<cfgList.size(); ++i) {
             ConfigFileItem c = cfgList.get(i);
             if(c.getName().equals(name)) {
                myLog.info("The value: " + c.getValue()+ " for configuration item: " + name + " was found.");
                 return c.getValue();
             }
         }
         myLog.info("The value for configuration item: " + name + " was not found.");
         return ret;
 
     }
 
     private void loadConfig() throws FileNotFoundException {
         int j = 0;
         if(!tCfg.getConfigItem(AlertServer.CONFIGFILE).equals("-1")){
             // We have a config file to load.
             File cFile = new File(tCfg.getConfigItem(AlertServer.CONFIGFILE));
 
             Scanner cScan = new Scanner(new FileReader(cFile));
 
             try {
                 while(cScan.hasNextLine()) {
                     ConfigFileItem cNew = processLine(cScan.nextLine());
                     if(cNew != null) {
                         myLog.info("Found configuration item: " + cNew.getName() + " with value: " + cNew.getValue());
                         cfgList.add(cNew);
                         ++j;
                     }
                 }
             } finally {
                 cScan.close();
             }
 
         }
     }
 
     private ConfigFileItem processLine(String tLine) {
         tLine = tLine.trim();
         // See if the first character is a # which denotes a comment.
         if(tLine.length() == 0 || tLine.charAt(0) == '#') {
             // This is a comment... disregard.
             return null;
         } else {
             // This should be a real kvp config item
             Scanner cLine = new Scanner(tLine);
             cLine.useDelimiter("=");
             if(cLine.hasNext()) {
                 String key = cLine.next().trim();
                 String val = cLine.next().trim();
                 ConfigFileItem cNew = new ConfigFileItem();
                 cNew.setName(key);
                 cNew.setValue(val);
                 return cNew;
             } else {
                 // Empty Line?
                 return null;
             }
         }
 
     }
 
 
 }
