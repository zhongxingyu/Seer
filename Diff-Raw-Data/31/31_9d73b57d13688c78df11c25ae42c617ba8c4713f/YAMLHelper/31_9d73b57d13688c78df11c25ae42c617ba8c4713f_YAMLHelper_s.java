 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.legendsdev.legends.library.database.helper;
 
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Provides a simple API for accessing YAML data.
  * <p>
  * Note: This class is not thread safe!
  *
  * @author B2OJustin
  */
 @SuppressWarnings("unchecked")
 public class YAMLHelper {
     private static final Logger logger = Logger.getLogger(YAMLHelper.class.getName());
     private LinkedHashMap<String, Object> dataMap;
     private Yaml yaml = new Yaml();
 
     public YAMLHelper(String filePath) throws FileNotFoundException {
         loadFile(filePath);
     }
 
     public YAMLHelper(LinkedHashMap dataMap) {
         this.dataMap = dataMap;
     }
 
     public YAMLHelper loadFile(String filePath) throws FileNotFoundException, ClassCastException {
         InputStream fileStream = new FileInputStream(filePath);
         dataMap = ((LinkedHashMap<String, Object>)yaml.load(fileStream));
         try {
             fileStream.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return this;
     }
 
     public Object getObject(String path) throws ClassCastException {
         LinkedHashMap<String, Object> currentMap = dataMap;
         String[] pathArray = path.split("\\.");
         for (int i = 0; i < pathArray.length; i++) {
             if(i == pathArray.length - 1) {
                 return currentMap.get(pathArray[i]);
             }
             else currentMap = (LinkedHashMap) currentMap.get(pathArray[i]);
         }
         return null;
     }
 
     public <T> T getObject(Class<T> clazz, String path) throws ClassCastException {
         return (T) getObject(path);
     }
 
     public ArrayList<String> getKeys(String path) throws ClassCastException {
         ArrayList<String> keys = new ArrayList<>();
        LinkedHashMap<String, Object> currentMap = dataMap;
        for (String pathKey : path.split("\\.")) {
            currentMap = (LinkedHashMap) currentMap.get(pathKey);
         }
        keys.addAll(currentMap.keySet());
         return keys;
     }
 
     public String getString(String path) throws ClassCastException {
         String string = (String) getObject(path);
         return string != null ? string : "";
     }
 
     public ArrayList<String> getStringList(String path) throws ClassCastException {
         ArrayList<String> stringList = (ArrayList<String>) getObject(path);
         return stringList != null ? stringList : new ArrayList<String>();
     }
 
     public ArrayList<LinkedHashMap<String, Object>> getMapList(String path) throws ClassCastException {
         return (ArrayList<LinkedHashMap<String, Object>>)getObject(path);
     }
 
     public int getInt(String path) {
         return (int) getObject(path);
     }
 }
 
