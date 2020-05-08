 /*
  *      Copyright (c) 2004-2012 YAMJ Members
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
 package com.moviejukebox.fanarttv.model;
 
 import com.moviejukebox.fanarttv.FanartTv;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.annotate.JsonAnySetter;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class FanartTvArtwork {
 
     private static final Logger LOGGER = Logger.getLogger(FanartTvArtwork.class);
     /*
      * Static Types
      */
     public static final String UNKNOWN = "UNKNOWN";
     public static final String TYPE_CLEARART = "clearart";
     public static final String TYPE_CLEARLOGO = "clearlogo";
     public static final String TYPE_SEASONTHUMB = "seasonthumb";
     public static final String TYPE_TVTHUMB = "tvthumb";
     public static final String TYPE_CHARACTERART = "characterart";
     public static final String TYPE_CDART = "cdart";
     public static final String SORT_NAME_ASC = "nameasc";
     public static final String SORT_NAME_DESC = "namedesc";
     public static final String SORT_FAV_ASC = "favasc";
     public static final String SORT_FAV_DESC = "favdesc";
     public static final int VERSION_IMAGE = 3;
     public static final int VERSION_CHARACTER = 4;
     /*
      * Validation data
      */
     private static List<String> artworkTypes = new ArrayList<String>();
     private static List<String> artworkSorts = new ArrayList<String>();
     /*
      * Data model
      */
     private String type;
     @JsonProperty("id")
     private int id;
     @JsonProperty("url")
     private String url;
     @JsonProperty("lang")
     private String language;
     @JsonProperty("likes")
     private int likes;
     private int season;
 
     public FanartTvArtwork(String type, String url) {
         this.type = type;
         this.url = url;
     }
 
     public FanartTvArtwork() {
         this.type = UNKNOWN;
         this.url = UNKNOWN;
     }
 
     /**
      * @return the type
      */
     public String getType() {
         return type;
     }
 
     /**
      * @param type the type to set
      */
     public void setType(String type) {
         if (validateType(type)) {
             this.type = type.toLowerCase();
         }
     }
 
     /**
      * @return the URL
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url the URL to set
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public int getLikes() {
         return likes;
     }
 
     public void setLikes(int likes) {
         this.likes = likes;
     }
 
     public int getSeason() {
         return season;
     }
 
     @JsonProperty("season")
     public void setSeason(String season) {
         if (StringUtils.isNumeric(season)) {
             this.season = Integer.parseInt(season);
         } else {
             this.season = -1;
         }
     }
 
     /**
      * Validate that the artworkType is one of the known values
      *
      * @param artworkType
      * @return
      */
     public static boolean validateType(String artworkType) {
         if (!FanartTv.isValidString(artworkType)) {
             return false;
         }
 
         populateArtworkTypes();
 
         if (artworkTypes.contains(artworkType.toLowerCase())) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Validate that the artworkSort is one of the known values
      *
      * @param artworkSort
      * @return
      */
     public static boolean validateSort(String artworkSort) {
         if (!FanartTv.isValidString(artworkSort)) {
             return false;
         }
 
         populateArtworkSorts();
 
         if (artworkSorts.contains(artworkSort.toLowerCase())) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Populate the artwork types
      */
     private static void populateArtworkTypes() {
         if (artworkTypes.isEmpty()) {
             artworkTypes.add(TYPE_CLEARART);
             artworkTypes.add(TYPE_CLEARLOGO);
             artworkTypes.add(TYPE_SEASONTHUMB);
             artworkTypes.add(TYPE_TVTHUMB);
             artworkTypes.add(TYPE_CHARACTERART);
             artworkTypes.add(TYPE_CDART);
         }
     }
 
     /**
      * Populate the artwork srorts
      */
     private static void populateArtworkSorts() {
         if (artworkSorts.isEmpty()) {
             artworkSorts.add(SORT_FAV_ASC);
             artworkSorts.add(SORT_FAV_DESC);
             artworkSorts.add(SORT_NAME_ASC);
             artworkSorts.add(SORT_NAME_DESC);
         }
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("[FanartTvArtwork=[");
         builder.append("[type=").append(type);
        builder.append("[id=").append(id);
        builder.append("][url=").append(url);
        builder.append("][lang=").append(url);
        builder.append("][likes=").append(url);
        builder.append("][season=").append(url);
         builder.append("]]");
         return builder.toString();
     }
 
     /**
      * Handle unknown properties and print a message
      *
      * @param key
      * @param value
      */
     @JsonAnySetter
     public void handleUnknown(String key, Object value) {
         StringBuilder sb = new StringBuilder();
         sb.append("Unknown property: '").append(key);
         sb.append("' value: '").append(value).append("'");
         LOGGER.warn(sb.toString());
     }
 }
