 /*
  * This file is part of MinecartRevolutionTags.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * MinecartRevolutionTags is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MinecartRevolutionTags is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MinecartRevolutionTags. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.minecartrevolutiontags.util;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import org.bukkit.entity.Minecart;
 import com.quartercode.minecartrevolution.core.exception.MinecartRevolutionException;
 import com.quartercode.minecartrevolutiontags.MinecartRevolutionTags;
 import com.quartercode.quarterbukkit.api.exception.ExceptionHandler;
 
 public class TagManager {
 
     private final MinecartRevolutionTags minecartRevolutionTags;
     private final Properties             tags;
 
     public TagManager(MinecartRevolutionTags minecartRevolutionTags) {
 
         this.minecartRevolutionTags = minecartRevolutionTags;
 
         tags = new Properties();
 
         if (minecartRevolutionTags.getTagFile().exists()) {
             FileReader reader = null;
             try {
                 reader = new FileReader(minecartRevolutionTags.getTagFile());
                 tags.load(reader);
             }
             catch (IOException e) {
                 ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolutionTags.getMinecartRevolution(), e, "Can't load tag file!"));
                 save();
             }
             finally {
                 if (reader != null) {
                     try {
                         reader.close();
                     }
                     catch (IOException e) {
                         ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolutionTags.getMinecartRevolution(), e, "Error while closing stream for loading tag file!"));
                     }
                 }
             }
         } else {
             save();
         }
     }
 
     private String getKey(Minecart minecart) {
 
         return minecart.getUniqueId().toString();
     }
 
     public List<String> getTags(Minecart minecart) {
 
         if (tags.keySet().contains(getKey(minecart))) {
             String data = tags.getProperty(getKey(minecart));
             if (data.contains(",")) {
                 return new ArrayList<String>(Arrays.asList(data.split(",")));
             } else {
                 return new ArrayList<String>(Arrays.asList(data));
             }
         } else {
             return new ArrayList<String>();
         }
     }
 
     public void setTags(Minecart minecart, List<String> tags) {
 
         removeTags(minecart);
 
         String tagString = "";
         for (String tag : tags) {
             tagString += tag + ",";
         }
         tagString = tagString.substring(0, tagString.length() - 1);
         this.tags.setProperty(getKey(minecart), tagString);
     }
 
     public void addTag(Minecart minecart, String tag) {
 
         List<String> tags = getTags(minecart);
         tags.add(tag);
         setTags(minecart, tags);
     }
 
     public void removeTag(Minecart minecart, String tag) {
 
         List<String> tags = getTags(minecart);
         tags.remove(tag);
         setTags(minecart, tags);
     }
 
     public void removeTags(Minecart minecart) {
 
         tags.remove(getKey(minecart));
     }
 
     public void save() {
 
         FileWriter writer = null;
         try {
             writer = new FileWriter(minecartRevolutionTags.getTagFile());
             tags.store(writer, "Tag Store File; Do not edit!");
         }
         catch (IOException e) {
             ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolutionTags.getMinecartRevolution(), e, "Can't save tag file!"));
         }
         finally {
             if (writer != null) {
                 try {
                     writer.close();
                 }
                 catch (IOException e) {
                     ExceptionHandler.exception(new MinecartRevolutionException(minecartRevolutionTags.getMinecartRevolution(), e, "Error while closing stream for saving tag file"));
                 }
             }
         }
     }
 
 }
