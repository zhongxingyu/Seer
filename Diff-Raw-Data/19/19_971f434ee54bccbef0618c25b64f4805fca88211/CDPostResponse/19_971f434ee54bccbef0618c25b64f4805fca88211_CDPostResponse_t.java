 /*
  *
  * Copyright (c) Critical Links S.A., All Rights Reserved.
  * (www.critical-links.com)
  *
  * This software is the proprietary information of Critical Links S.A. Use is
  * subject to license terms.
  *
  */
 package com.edgebox.eacds.net;
 
 import com.edgebox.eacds.data.CDBaseData;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  *
  * @author Augusto Campos <a-campos at critical-links.com>
  */
 public class CDPostResponse extends CDBaseData {
 
     public static CDPostResponse build(String json) {
         CDPostResponse rt = new CDPostResponse();
         JsonElement e = new JsonParser().parse(json);
         rt.json = e.getAsJsonObject();
         rt.success = rt.json.get("success").getAsBoolean();
         rt.message = rt.json.get("message").getAsString();
         rt.errorTrace = rt.json.get("errorTrace").getAsString();

        try {
            rt.data = rt.json.getAsJsonObject("data").getAsJsonObject();
        } catch (java.lang.ClassCastException ex) {
            rt.data = rt.json.get("data").getAsString();
        }
 
         return rt;
     }
     /**
      * Action Success status.
      */
     public Boolean success;
 
     /**
      * Error message
     * <p>
     * Only filled on case <success> is false.
      */
     public String message;
 
     /**
      * More Error Information
     * <p>
     * Only filled on case <success> is false.
      */
     public String errorTrace;
 
     /**
      * Object Returned data
     * <p>
     * Only filled on case <success> is true.
      */
     public Object data;
 
     private transient JsonObject json;
 
     /**
      * Constructor.
      */
     public CDPostResponse() {
     }
 
     /**
      * Error log information
      *
      * @return <String> "message - errorTrace"
      */
     public String log() {
         return String.format("%s - %s", message, errorTrace);
     }
 
 }
