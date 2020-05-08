 /*
  * Copyright (c) 2012, the authors.
  *
  *   This file is part of 'Nextflow'.
  *
  *   Nextflow is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Nextflow is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.dxfs;
 
 import java.nio.file.attribute.BasicFileAttributes;
 import java.nio.file.attribute.FileTime;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 
 /**
  * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
  */
 public class DxFileAttributes implements BasicFileAttributes {
 
     private Map<String,Object> attrs = new HashMap<>();
 
     public DxFileAttributes( JsonNode node ) {
 
         // parse the node and put the values into local map
         JsonNode value;
 
         // the name attribute
         value = node.get("name");
         if( value != null ) {
             attrs.put("name", value.textValue() );
         }
 
         value = node.get("links");
         if( value != null && value instanceof ArrayNode ) {
             ArrayNode array = (ArrayNode)value;
             String[] items = new String[ array.size() ];
             for(int i=0 ; i< array.size(); i++){
                items[i]= String.valueOf(array.get(i).textValue());
             }
 
             attrs.put("links", items);
         }
 
         value = node.get("tags");
         if( value != null && value instanceof ArrayNode ) {
             ArrayNode array = (ArrayNode)value;
             String[] items = new String[ array.size() ];
             for(int i=0 ; i< array.size(); i++){
                items[i]= String.valueOf(array.get(i).textValue());
             }
 
             attrs.put("tags", items);
         }
 
         value = node.get("media");
         if( value != null ) {
             attrs.put("media", value.textValue() );
         }
 
         value = node.get("created");
         if( value != null ) {
             attrs.put("created", value.longValue());
         }
 
         value = node.get("modified");
         if( value != null ) {
             attrs.put("modified", value.longValue() );
         }
 
         value = node.get("class");
         if( value != null) {
             attrs.put("class", value.textValue());
         }
 
         value = node.get("project");
         if( value != null) {
             attrs.put("project", value.textValue());
         }
 
         value = node.get("state");
         if( value != null) {
             attrs.put("state", value.textValue());
         }
 
         value = node.get("folder");
         if( value != null) {
             attrs.put("folder", value.textValue());
         }
 
         value = node.get("sponsored");
         if( value != null) {
             attrs.put("sponsored", value.booleanValue());
         }
 
         value = node.get("hidden");
         if( value != null) {
             attrs.put("hidden", value.booleanValue());
         }
 
         value = node.get("id");
         if( value != null) {
             attrs.put("id", value.textValue());
         }
 
         value = node.get("types");
         if( value != null && value instanceof ArrayNode ) {
             ArrayNode array = (ArrayNode)value;
             String[] items = new String[ array.size() ];
             for(int i=0 ; i< array.size(); i++){
                items[i]= String.valueOf(array.get(i).textValue());
             }
 
             attrs.put("types", items);
         }
 
         value = node.get("size");
         if( value != null) {
             attrs.put("size", value.longValue());
         }
     }
 
     /**
      * @return A map containing all attributes
      */
     public Map<String,Object> asMap() {
         return new HashMap<>(attrs);
     }
 
     /**
      * @return An attribute value by its name
      */
     public Object get(String name) {
         return attrs.get(name);
     }
 
     public String getName() {
 
         String name = (String)attrs.get("name");
         if( name != null ) {
             return name;
         }
         else {
             return null;
         }
     }
 
     public String[] getLinks() {
 
         String[] links = (String[])attrs.get("links");
         if ( links != null){
             return links;
         }else{
             return null;
         }
     }
 
     public String[] getTags() {
 
        return (String[])attrs.get("tags");
     }
 
     public String getMedia() {
 
         String media = (String)attrs.get("media");
         if( media != null ) {
             return media;
         }
         else {
             return null;
         }
     }
 
     @Override
     public FileTime creationTime() {
 
         Long lastModifiedMillis = (Long)attrs.get("created");
         if( lastModifiedMillis != null ) {
             return FileTime.from( lastModifiedMillis, TimeUnit.MILLISECONDS );
         }
         else {
             return null;
         }
     }
 
     @Override
     public FileTime lastModifiedTime() {
 
         Long lastModifiedMillis = (Long)attrs.get("modified");
         if( lastModifiedMillis != null ) {
             return FileTime.from( lastModifiedMillis, TimeUnit.MILLISECONDS );
         }
         else {
             return null;
         }
     }
 
     @Override
     public FileTime lastAccessTime() {
        throw new UnsupportedOperationException("Attribute 'lastAccessTime' not supported by DNAnexus file system");
     }
 
     public String getClassType(){
 
         String type = (String)attrs.get("class");
         if( type != null ) {
             return type;
         }
         else {
             return null;
         }
 
     }
 
     public String getProjectId() {
 
         String project = (String)attrs.get("project");
         if( project != null ) {
             return project;
         }
         else {
             return null;
         }
     }
 
     public String getState() {
 
         String state = (String)attrs.get("state");
         if( state != null ) {
             return state;
         }
         else {
             return null;
         }
     }
 
     public String getFolder() {
 
         String folder = (String)attrs.get("folder");
         if( folder != null ) {
             return folder;
         }
         else {
             return null;
         }
     }
 
     public boolean isSponsored() {
 
         Boolean sponsored = (Boolean)attrs.get("sponsored");
         if( sponsored != null ) {
             return sponsored;
         }
         else {
             return false;
         }
     }
 
     public boolean isHidden() {
 
         Boolean hidden = (Boolean)attrs.get("hidden");
         if( hidden != null ) {
             return hidden;
         }
         else {
             return false;
         }
     }
 
     @Override
     public Object fileKey() {
         String id = (String)attrs.get("id");
         if( id != null ) {
             return id;
         }
         else {
             return null;
         }
     }
 
     public String[] getTypes() {
 
         String[] types = (String[])attrs.get("types");
         if ( types != null){
             return types;
         }else{
             return null;
         }
     }
 
     @Override
     public long size() {
 
         Long size = (Long)attrs.get("size");
         if( size != null ) {
             return size;
         }
         else {
             return 0;
         }
     }
 
     @Override
     public boolean isRegularFile() {
         return true;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean isDirectory() {
          return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean isSymbolicLink() {
        return false;
     }
 
     @Override
     public boolean isOther() {
        return false;
     }
 
 }
