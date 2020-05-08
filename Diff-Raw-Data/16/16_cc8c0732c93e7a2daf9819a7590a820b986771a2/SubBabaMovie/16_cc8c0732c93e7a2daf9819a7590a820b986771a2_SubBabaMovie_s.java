 /*
  *      Copyright (c) 2004-2012 Stuart Boston
  *
  *      This software is licensed under a Creative Commons License
  *      See the LICENCE.txt file included in this package
  *
  *      For any reuse or distribution, you must make clear to others the
  *      license terms of this work.
  */
 package com.omertron.subbabaapi.model;
 
 import com.fasterxml.jackson.annotation.JsonAnySetter;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonRootName;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.log4j.Logger;
 
 @JsonRootName("movie")
 public class SubBabaMovie implements Serializable {
 
     private static final Logger logger = Logger.getLogger(SubBabaMovie.class);
     // Serial Version
     private static final long serialVersionUID = 1L;
     // Object properties
     @JsonProperty("original_name")
     private String originalName = "";
     private String type = "";
     private int id = 0;
     @JsonProperty("imdb_id")
     private String imdbId = "";
    private List<SubBabaContent> content = new ArrayList<SubBabaContent>();
 
     //<editor-fold defaultstate="collapsed" desc="Setter Methods">
     public void setOriginalName(String originalName) {
         this.originalName = originalName;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void setImdbId(String imdbId) {
         this.imdbId = imdbId;
     }
 
     public void setContent(List<SubBabaContent> content) {
         this.content = content;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Getter Methods">
     public static long getSerialVersionUID() {
         return serialVersionUID;
     }
 
     public String getOriginalName() {
         return originalName;
     }
 
     public String getType() {
         return type;
     }
 
     public int getId() {
         return id;
     }
 
     public String getImdbId() {
         return imdbId;
     }
 
     public List<SubBabaContent> getContent() {
         return content;
     }
     //</editor-fold>
 
     @Override
     public String toString() {
         return "SubBabaMovie{" + "originalName=" + originalName + ", type=" + type + ", id=" + id + ", imdbId=" + imdbId + ", content=" + content + '}';
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
         logger.trace(sb.toString());
     }
 }
