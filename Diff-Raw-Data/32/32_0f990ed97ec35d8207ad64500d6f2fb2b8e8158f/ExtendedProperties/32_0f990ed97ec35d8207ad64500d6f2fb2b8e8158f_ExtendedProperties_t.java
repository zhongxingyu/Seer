 /* Copyright 2009-2013 Tracy Flynn
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.verymuchme.appconfig;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 /**
  * Manage internal properties settings
  * 
  * @author Tracy Flynn
  * @version 2.0
  * @since 2.0
  * 
  */
 //public class ExtendedProperties extends Properties {
 public class ExtendedProperties {
 
   /*
    * Logger instance
    */
   private static Logger logger = null;
 
   /*
    * Internal properties storage
    */
   private HashMap<String,ExtendedPropertiesEntry> internalProperties = new HashMap<String,ExtendedPropertiesEntry>();
   
   
   private static final String TYPE_STRING = "String";
   private static final String TYPE_BOOLEAN = "Boolean";
   private static final String TYPE_OBJECT = "Object";
   private static final String TYPE_ARRAYLIST = "ArrayList";
   private static final String TYPE_ARRAYLIST_STRING = "ArrayListString";
   
   
   /**
    * Creates an empty property list with no default values.
    */
   public ExtendedProperties() {
   }
 
   /**
    * Get value of a property as a String value
    * 
    * @param key Property name
    * 
    * @return String value or null if not a string, or property is null
    */
   public String getProperty(String key) {
     ExtendedPropertiesEntry extendedPropertiesEntry = internalProperties.get(key);
     String returnVal = null;
     if (extendedPropertiesEntry != null) {
       Object entryValue = extendedPropertiesEntry.getEntryValue();
       if (entryValue != null) {
         if (extendedPropertiesEntry.getTargetType().equals(ExtendedPropertiesEntry.TYPE_STRING)) {
           returnVal = (String) entryValue;
         }
       }
     }
     return returnVal;
   }
   
   /**
    * Set value of a property as a String
    * 
    * @param propertyName Property Name
    * @param propertyValue Property Value
    */
   public void setProperty(String propertyName, String propertyValue) {
     put(propertyName, propertyValue);
   }
   
   /**
    * Get a property as a boolean. 
    * Returns the value true if the string argument is not null and is equal, ignoring case, to the string "true". 
    * Returns false otherwise.
    * 
    * @param key Property name
    * @return true or false
    */
   public boolean getBooleanProperty(String key) {
     ExtendedPropertiesEntry extendedPropertiesEntry = internalProperties.get(key);
     boolean returnVal = false;
     if (extendedPropertiesEntry != null) {
       Object entryValue = extendedPropertiesEntry.getEntryValue();
       if (entryValue != null) {
         if (extendedPropertiesEntry.getTargetType().equals(ExtendedPropertiesEntry.TYPE_BOOLEAN)) {
           returnVal = ((Boolean) entryValue).booleanValue();
         }
       }
     }
     return returnVal;
   }
   
   
   /**
    * Get a property as an ArrayList<String>. Uses ',' as the default separator. 
    * 
    * @param key Property name
    * 
    * @return Array of string values, or null if no property present
    */
   public ArrayList<String> getListProperty(String key) {
     ExtendedPropertiesEntry extendedPropertiesEntry = internalProperties.get(key);
     ArrayList<String> returnVal = null;
     if (extendedPropertiesEntry != null) {
       Object entryValue = extendedPropertiesEntry.getEntryValue();
       if (entryValue != null) {
         if (extendedPropertiesEntry.getTargetType().equals(ExtendedPropertiesEntry.TYPE_ARRAY_LIST_STRING)) {
           returnVal = (ArrayList<String>) entryValue;
         }
       }
     }
     return returnVal;
   }
   
   /**
    * Get a property as an object value
    * 
    * @param key Property name
    * @return Property value as an object, or null if no entry
    */
   public Object get(String key) {
     ExtendedPropertiesEntry extendedPropertiesEntry = internalProperties.get(key);
     Object returnValue = null;
     if (extendedPropertiesEntry != null) {
       returnValue = extendedPropertiesEntry.getEntryValue();
     }
     return returnValue;
   }
   
   /**
    * Get the property names for the internal properties
    * 
    * @return Iterator over property names
    */
   public Iterator<String> propertyNames() {
     Set<String> propertyNamesSet = this.internalProperties.keySet();
     return propertyNamesSet.iterator();
   }
 
   /**
    * Store a property name and value
    * 
    * @param propertyName
    * @param propertyValue
    */
   public void put(String propertyName, Object propertyValue) {
     ExtendedPropertiesEntry extendedPropertiesEntry = convertObjectToEntry(propertyName,propertyValue);
     internalProperties.put(propertyName, extendedPropertiesEntry);
   }
   
   /**
    * Load all the properties in the specified properties instance. Assumes all property keys and values are strings
    * 
    * @param properties Properties instance
    */
   public void loadStringProperties(Properties properties) {
     Set<String> propertyNamesSet = properties.stringPropertyNames();
     Iterator<String> propertyNames = propertyNamesSet.iterator();
     while(propertyNames.hasNext()) {
       String propertyName = propertyNames.next();
       String propertyValueString = properties.getProperty(propertyName);
       ExtendedPropertiesEntry extendedPropertiesEntry = convertToEntry(propertyName,propertyValueString);
       internalProperties.put(propertyName, extendedPropertiesEntry);
     }
   }
   
   /**
    * Convert a property value to an ExtendedPropertiesEntry
    * 
    * @param propertyName Property name
    * @param propertyValueString Property value as a String
    * 
    * @return ExtendedPropertiesEntry instance
    * 
    */
   public ExtendedPropertiesEntry convertToEntry(String propertyName, String propertyValueString) {
     ExtendedPropertiesEntry entry = new ExtendedPropertiesEntry();
     entry.setEntryName(propertyName);
     if (propertyValueString == null) {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (propertyValueString == "") {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (AppConfigUtils.isNullStringValue(propertyValueString)) {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (AppConfigUtils.isListValue(propertyValueString)) {
       entry.setEntryValue(AppConfigUtils.getListValue(propertyValueString));
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_ARRAY_LIST_STRING);
     }
     else if (AppConfigUtils.isBooleanValue(propertyValueString)) {
       entry.setEntryValue(AppConfigUtils.getBooleanValue(propertyValueString));
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_BOOLEAN);
     }
     else {
       entry.setEntryValue(propertyValueString);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     return entry;
   }
 
   /**
    * Convert a property value to an ExtendedPropertiesEntry
    * 
    * @param propertyName Property name
   * @param propertyValue Property value as an object
    * 
    * @return ExtendedPropertiesEntry instance
    * 
    */
   public ExtendedPropertiesEntry convertObjectToEntry(String propertyName, Object propertyValue) {
     ExtendedPropertiesEntry entry = new ExtendedPropertiesEntry();
     entry.setEntryName(propertyName);
     String propertyType = TYPE_OBJECT;
     if (propertyValue instanceof String) {
       propertyType = TYPE_STRING;
     }
     else if (propertyValue instanceof Boolean) {
       propertyType = TYPE_BOOLEAN;
     }
     else if (propertyValue instanceof ArrayList) {
       propertyType = TYPE_ARRAYLIST;
       ArrayList arrList = (ArrayList) propertyValue;
       if (! (arrList.isEmpty())) {
         Object arrListValue = arrList.get(0);
         if (arrListValue instanceof String) {
           propertyType = TYPE_ARRAYLIST_STRING;
         }
       }
     }
     if (propertyValue == null) {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_OBJECT);
     }
     else if (propertyType == TYPE_STRING && "".equals((String) propertyValue)) {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (propertyType == TYPE_STRING  && AppConfigUtils.isNullStringValue((String) propertyValue)) {
       entry.setEntryValue(null);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (propertyType == TYPE_STRING && AppConfigUtils.isListValue((String) propertyValue)) {
       entry.setEntryValue(AppConfigUtils.getListValue((String) propertyValue));
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_ARRAY_LIST_STRING);
     }
     else if (propertyType == TYPE_STRING && AppConfigUtils.isBooleanValue((String) propertyValue)) {
       entry.setEntryValue(AppConfigUtils.getBooleanValue((String) propertyValue));
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_BOOLEAN);
     }
     else if (propertyType == TYPE_STRING) {
       entry.setEntryValue((String) propertyValue);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_STRING);
     }
     else if (propertyType == TYPE_BOOLEAN) {
       entry.setEntryValue(propertyValue);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_BOOLEAN);
     }
     else if (propertyType == TYPE_ARRAYLIST_STRING) {
       entry.setEntryValue(propertyValue);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_ARRAY_LIST_STRING);
     }
     else if (propertyType == TYPE_ARRAYLIST) {
       entry.setEntryValue(propertyValue);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_ARRAY_LIST);
     }
     else {
       entry.setEntryValue(propertyValue);
       entry.setTargetType(ExtendedPropertiesEntry.TYPE_OBJECT);
     }
     return entry;
   }
   
   
   /**
    * Set the active logger for this class
    */
   public static void setActiveLogger() {
     setActiveLogger(null);
   }
   
   /**
    * Set the active logger for this class
    * 
    * @param activeLogger
    */
   public static void setActiveLogger(Logger activeLogger) {
     logger = activeLogger == null ? LoggerFactory.getLogger(ExtendedProperties.class) : activeLogger;
   }
   
   /**
    * Dump the properties to System.out
    */
   public void dumpProperties() {
     dumpProperties(System.out);
   }
 
   /**
    * Dump the properties to the specified print stream
    *
    * @param printStream Print stream
    */
   public void dumpProperties(PrintStream printStream) {
     ArrayList<String> propertyNamesList = new ArrayList<String>();
     Iterator<String> propertyNamesItr = propertyNames();
     while (propertyNamesItr.hasNext()) {
       String propertyName = propertyNamesItr.next();
       propertyNamesList.add(propertyName);
     }
     Collections.sort(propertyNamesList);
     for (String propertyName : propertyNamesList) {
       Object propertyValueObject = get(propertyName);
       String propertyValue = null;
       if (propertyValueObject == null) {
         propertyValue = "null value";
       }
       else {
         propertyValue = propertyValueObject.toString();
       }
       printStream.println(String.format("%s = %s",propertyName, propertyValue));
     }
   }
   
   
   
 }
