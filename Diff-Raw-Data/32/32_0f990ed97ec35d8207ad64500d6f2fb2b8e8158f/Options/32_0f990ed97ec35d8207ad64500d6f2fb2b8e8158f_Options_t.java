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
 import java.util.HashMap;
 import java.util.Iterator;
 
 /* 
  * Thin wrapper for a HashMap<String,Object> with some convenience methods
  * 
  * @author Tracy Flynn
  * @version 3.0
  * @since 3.0
  */
 public class Options {
   
   /*
    * Internal map
    */
   private HashMap<String,Object> settings = null;
   
   /**
    * Create a default Options instance
    */
   public Options() {
     this(null);
   }
   
   /**
    * Create a Options instance using the specified HashMap as source
    * 
   * @param settings HashMap to use as source
    */
   public Options(HashMap<String,Object> settings) {
     this.settings = settings == null ? new HashMap<String,Object>() : settings ; 
   }
   
   /**
    * Set a property value - alias for setProperty
    * 
    * @param propertyName Property name
    * @param propertyValue Property value
    */
   public void put(String propertyName, Object propertyValue) {
     setProperty(propertyName,propertyValue);
   }
   
   /**
    * Set a property value 
    * 
    * @param propertyName Property name
    * @param propertyValue Property value
    */
   public void setProperty(String propertyName, Object propertyValue) {
     this.settings.put(propertyName, propertyValue);
   }
   
   /**
    * Get a property as a string value
    * 
    * @param propertyName Property name
    * 
    * @return Property value - null if cast to String fails
    */
   public String getString(String propertyName) {
     String propertyValue = null;
     try {
       propertyValue = (String) this.settings.get(propertyName);
     }
     catch (ClassCastException cce) {
       // Ignore errors - return null
       propertyValue = null;
     }
     return propertyValue;
   }
   
   /**
    * Get a property as an object
    * 
    * @param propertyName Property name
    * 
    * @return Property value as an object
    */
   public Object get(String propertyName) {
     return this.settings.get(propertyName);
   }
   
   /**
    * Dump all properties to System.out
    */
   public void dump() {
     dump(System.out);
   }
   
   /**
    * Dump all properties to the specified PrintStream
    * @param out PrintStream
    */
   public void dump(PrintStream out) {
     String lineTerm = System.getProperty("line.separator");
     Iterator<String> it = this.settings.keySet().iterator();
     while (it.hasNext()) {
       String currentKey = it.next().toString();
       String currentValue = this.settings.get(currentKey).toString();
       out.print(String.format("%s=%s%s",currentKey,currentValue,lineTerm));
     }
   }
 }
