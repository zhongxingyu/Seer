 /*
  * Copyright (C) 2012 Lasse Dissing Hansen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 
 package volpes.ldk;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Lasse Dissing Hansen
  * @since 0.2
  */
 public class Settings {
 
     private Map<String,Object> data = new HashMap<String,Object>();
 
     private boolean updated;
 
 
     /**
      * Creates a new settings object, given a file name
      * @param filename The name of the engine configurations file
      */
     public Settings(String filename) {
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename)));
 
             try {
                 String line;
                 String parts[];
                 int index = 0;
                 while((line = reader.readLine()) != null) {
                     index++;
                     if (line.contains(":")) {
                         parts = line.split(":");
                         parts[0] = parts[0].trim();
                         parts[1] = parts[1].trim();
                         if (parts[1].matches("-?\\d+")) {
                             data.put(parts[0],Integer.parseInt(parts[1]));
                         } else if (parts[1].matches("-?\\d+(\\.\\d+)?")) {
                             data.put(parts[0],Float.parseFloat(parts[1]));
                         } else if (parts[1].equalsIgnoreCase("true") || parts[1].equalsIgnoreCase("false")) {
                             data.put(parts[0],Boolean.parseBoolean(parts[1]));
                         } else {
                             data.put(parts[0],parts[1]);
                         }
                     } else {
                         throw new LDKException("Line " + index + " expected a :");
                     }
                 }
             } finally {
                 reader.close();
             }
         } catch (IOException e) {
             System.err.println("Unable to locate the engine configuration file \"" + filename + "\" at the games root directory");
             System.exit(1);
         }
 
     }
 
     /**
      * Checks if such a key is to be found in the settings map
      * @param key The key to lookup
      * @return True if the map contains such a key
      */
     public boolean has(String key) {
         return data.containsKey(key);
     }
 
     /**
      * Returns a boolean from the settings map
      * If there is no such key to be found or the value is not a boolean
      * the function will return an unspecified exception
      * Please use the ternary operator in combination with {@link #has(String)}
      *
      * boolean result = has(key) ? getBool(key) : default
      *
      * @param key The key to lookup in the map
      * @return The value of the setting
      */
     public boolean getBool(String key) {
         return (boolean)data.get(key);
     }
 
     /**
      * Returns a integer from the settings map
      * If there is no such key to be found or the value is not a integer
      * the function will return an unspecified exception
      * Please use the ternary operator in combination with {@link #has(String)}
      *
      * boolean result = has(key) ? getInt(key) : default
      *
      * @param key The key to lookup in the map
      * @return The value of the setting
      */
     public int getInt(String key) {
         return (int)data.get(key);
     }
 
     /**
      * Returns a float from the settings map
      * If there is no such key to be found or the value is not a float
      * the function will return an unspecified exception
      * Please use the ternary operator in combination with {@link #has(String)}
      *
      * boolean result = has(key) ? getFloat(key) : default
      *
      * @param key The key to lookup in the map
      * @return The value of the setting
      */
     public float getFloat(String key) {
         return (float)data.get(key);
     }
 
     /**
      * Returns a String from the settings map
      * If there is no such key to be found or the value is not a String
      * the function will return an unspecified exception
      * Please use the ternary operator in combination with {@link #has(String)}
      *
      * boolean result = has(key) ? getString(key) : default
      *
      * @param key The key to lookup in the map
      * @return The value of the setting
      */
     public String getString(String key) {
         return (String)data.get(key);
     }
 
 
     /**
      * Sets the entry with the specified key to the value
      * If no such entry exist a new one will be created
      * @param key The key of the entry
      * @param value The value of the entry
      */
     public void setBool(String key, boolean value) {
         updated = true;
         data.put(key,value);
     }
 
     /**
      * Sets the entry with the specified key to the value
      * If no such entry exist a new one will be created
      * @param key The key of the entry
      * @param value The value of the entry
      */
     public void setInt(String key, int value) {
         updated = true;
         data.put(key,value);
     }
 
     /**
      * Sets the entry with the specified key to the value
      * If no such entry exist a new one will be created
      * @param key The key of the entry
      * @param value The value of the entry
      */
     public void setFloat(String key, float value) {
         updated = true;
         data.put(key,value);
     }
 
     /**
      * Sets the entry with the specified key to the value
      * If no such entry exist a new one will be created
      * @param key The key of the entry
      * @param value The value of the entry
      */
     public void setString(String key, String value) {
         updated = true;
         data.put(key,value);
     }
 
     protected boolean isUpdated() {
         return updated;
     }
 }
