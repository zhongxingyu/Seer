 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.ValidationException;
 import org.vfny.geoserver.config.featureType.FeatureType;
 
 /**
  * Reads all necessary feature type information to abstract away from servlets.
  * 
  * @author Rob Hranac, TOPP
  * @version $VERSION$
  */
 public class TypeRepository {
         
     /** Class logger */
     private static Logger LOG = Logger.getLogger("org.vfny.geoserver.config");
 
     /** Castor-specified type to hold all the  */
     private static TypeRepository repository = null;
 
     private static ConfigInfo config = ConfigInfo.getInstance();
 
     private Random keyMaster = null;
 
     private Map types = new HashMap();
 
     private Map locks = new HashMap();
 
     
     /** Initializes the database and request handler. */ 
     private TypeRepository() {
         Date seed = new Date();
         keyMaster = new Random(seed.getTime());
     }
     
     /**
      * Initializes the database and request handler.
      * @param featureTypeName The query from the request object.
      */ 
     public static TypeRepository getInstance() {
         if(repository == null) {
             File startDir = new File(config.getTypeDir());
             repository = new TypeRepository();
             repository.readTypes(startDir, config);
         }
         return repository;
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param version The version of the request (0.0.14 or 0.0.15)
      */ 
     public TypeInfo getType(String typeName) { 
         return (TypeInfo) types.get(typeName);
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param type The version of the request (0.0.14 or 0.0.15)
      */ 
     private void addType(TypeInfo type) { 
         types.put(type.getName(), type);
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      */
     public int typeCount() {        
         return types.size();
     }
 
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param typeName The version of the request (0.0.14 or 0.0.15)
      */ 
     public boolean isLocked(String typeName) {        
         return locks.containsKey(typeName);
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param typeName The version of the request (0.0.14 or 0.0.15)
      */ 
     public synchronized String lock(String typeName) {        
         if(locks.containsKey(typeName)) {
             LOG.finest("no lock on: " + typeName);
             return null;
         } else {
             String lock = String.valueOf(keyMaster.nextInt(Integer.MAX_VALUE));
             locks.put(typeName, lock);
             LOG.finest("locked " + typeName + " with: " + lock);
             return lock;
         }
     }
 
     /**
      * Returns a capabilities XML fragment for a specific feature type.
      * @param typeName The version of the request (0.0.14 or 0.0.15)
      * @param key The version of the request (0.0.14 or 0.0.15)
      */ 
     public synchronized boolean unlock(String typeName, String lock) {        
         if(locks.containsKey(typeName)) {
             String targetLock = (String) locks.get(typeName);
             if(targetLock.equals(lock)) {
                 locks.remove(typeName);
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
 
     /**
      * This function lists all files in HTML for the meta-data pages.
      * 
      * Simple recursive function to read through all subdirectories and 
      * add all XML files with the name 'info.XXX' to the repository.
      * @param currentFile The top directory from which to start 
      * reading files.
      */
     private void readTypes(File currentFile, ConfigInfo config) {
         LOG.finest("examining: " + currentFile.getAbsolutePath());
         LOG.finest("is dir: " + currentFile.isDirectory());
         if(currentFile.isDirectory()) {
             File[] file = currentFile.listFiles();
             for(int i = 0, n = file.length; i < n; i++) {
                 readTypes(file[i], config);
             } 
         } else if(isInfoFile(currentFile, config)) {
             LOG.finest("adding: " + currentFile.getAbsolutePath());
             addType(new TypeInfo(currentFile.getAbsolutePath()));
         }
     }
 
     private static boolean isInfoFile(File testFile, ConfigInfo config){
         String testName = testFile.getAbsolutePath();
        int start = testName.length() - 9;
         int end = testName.length();
         return testName.substring(start, end).equals(config.INFO_FILE);
     }
 
     public String toString() {
         StringBuffer returnString = new StringBuffer("\n  TypeRepository:");
         Collection typeList = types.values();
         Iterator i = typeList.iterator();
         while(i.hasNext()) {
             TypeInfo type = (TypeInfo) i.next();
             returnString.append("\n   " + type.getName());
             if(locks.containsValue(type.getName())) {
                 returnString.append(" (" + type.getName() + ")");
             }
         }
         return returnString.toString();
     }
 
 }
