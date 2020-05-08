 /*
  *      Copyright (c) 2004-2011 YAMJ Members
  *      http://code.google.com/p/moviejukebox/people/list 
  *  
  *      Web: http://code.google.com/p/moviejukebox/
  *  
  *      This software is licensed under a Creative Commons License
  *      See this page: http://code.google.com/p/moviejukebox/wiki/License
  *  
  *      For any reuse or distribution, you must make clear to others the 
  *      license terms of this work.  
  */
 package com.moviejukebox.rottentomatoes.model;
 
 public class Artwork {
     private String type;
     private String url;
     
     public Artwork(String type, String url) {
         this.type = type;
         this.url = url;
     }
 
     public Artwork() {
         this.type = "";
        this.type = "";
     }
     
     public String getType() {
         return type;
     }
     
     public void setType(String type) {
         this.type = type;
     }
     
     public String getUrl() {
         return url;
     }
     
     public void setUrl(String url) {
         this.url = url;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("[Artwork=[type=");
         builder.append(type);
         builder.append("], [url=");
         builder.append(url);
         builder.append("]]");
         return builder.toString();
     }
     
 }
