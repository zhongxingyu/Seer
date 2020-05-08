 /*
  * Copyright 2007-2009 Alexander Fabisch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ev.datamodel;
 
 import com.ev.util.Util;
 import java.io.File;
 import java.util.*;
 import org.apache.log4j.Logger;
 import static com.ev.global.Constants.*;
 
 /**
  * There can be many profiles running on the same computer. Each profile can belong to another device.
  *
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  * @since 0.8.2
  */
 public final class Profile implements Storable {
     private static final Logger logger = Logger.getLogger(Profile.class);
 
     private String name;
     private String path;
     private String description;
     private int squareMeters;
 
     /** Data will be stored in a properties file. */
     private Properties store;
     private boolean newFile;
 
     public static List<String> listProfiles() {
         List<String> result = new Vector<String>();
         File directory = new File(".");
         if (Util.isDirectoryAccessible(directory)) {
             result = findAvailableProfiles(directory);
         }
         Collections.sort(result);
         return result;
     }
 
     private static List<String> findAvailableProfiles(File directory) {
         List<String> result = new Vector<String>();
         for (String fileName : directory.list()) {
             if (fileIsProfile(fileName)) {
                 result.add(fileName.substring(0, fileName.length() - PROPERTIES_SUFFIX.length()));
             }
         }
         return result;
     }
 
     private static boolean fileIsProfile(String fileName) {
         return fileName.endsWith(PROPERTIES_SUFFIX) && !fileName.equals("logging" + PROPERTIES_SUFFIX)
                 && !fileName.equals("config" + PROPERTIES_SUFFIX);
     }
 
     public Profile(final String name) {
         this.name = name;
         store = new Properties();
         load();
     }
 
     @Override
     public final void load() {
         try {
             store = new Properties();
             Util.readFile(name + PROPERTIES_SUFFIX, store);
             path = store.getProperty("path");
             description = store.getProperty("description");
            squareMeters = Integer.valueOf(store.getProperty("squareMeters"));
         } catch (Exception e) {
             logger.trace("File for profile " + name + " does not exist yet.");
             newFile = true;
         }
     }
 
     @Override
     public final void save() {
         try {
             store.setProperty("name", name);
             store.setProperty("path", path);
             store.setProperty("description", description);
             store.setProperty("squareMeters", Integer.toString(squareMeters));
             Util.writeFile(name + PROPERTIES_SUFFIX, store, "");
         } catch (Exception e) {
            logger.debug("Could not save data.");
         }
     }
 
     public final String getName() {
         return name;
     }
 
     public final String getPath() {
         return path;
     }
 
     public final void setPath(final String path) {
         this.path = path;
     }
 
     public final String getDescription() {
         return description;
     }
 
     public final void setDescription(final String description) {
         this.description = description;
     }
 
     public int getSquareMeters() {
         return squareMeters;
     }
 
     public void setSquareMeters(int squareMeters) {
         this.squareMeters = squareMeters;
     }
 
     public boolean isNewFile() {
         return newFile;
     }
 
     @Override
     public boolean equals(final Object obj) {
         return obj instanceof Profile ? ((Profile) obj).getPath().equals(path) : false;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 53 * hash + (this.path != null ? this.path.hashCode() : 0);
         hash = 53 * hash + (this.description != null ? this.description.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
 }
