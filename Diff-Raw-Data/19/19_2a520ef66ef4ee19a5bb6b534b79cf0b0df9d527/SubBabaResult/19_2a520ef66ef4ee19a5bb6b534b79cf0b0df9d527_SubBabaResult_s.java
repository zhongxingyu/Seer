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
 import org.apache.log4j.Logger;
 
 public class SubBabaResult {
 
     private static final Logger logger = Logger.getLogger(SubBabaResult.class);
     // Serial Version
     private static final long serialVersionUID = 1L;
     // Object properties
    private SubBabaMovie movie;
 
     public SubBabaMovie getMovie() {
         return this.movie;
     }
 
     public void setMovie(SubBabaMovie movie) {
         this.movie = movie;
     }
 
     @Override
     public String toString() {
         return "SubBabaResults{" + "movie=" + movie + '}';
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
