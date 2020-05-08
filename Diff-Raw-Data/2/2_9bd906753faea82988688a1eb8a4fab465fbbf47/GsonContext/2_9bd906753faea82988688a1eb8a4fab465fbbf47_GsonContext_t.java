 package com.siu.android.andutils.gson;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class GsonContext {
 
     private static GsonContext instance;
     private Gson gson;
 
     public static GsonContext getInstance() {
         if (null == instance) {
             instance = new GsonContext();
         }
 
         return instance;
     }
 
    protected GsonContext() {
 
     }
 
     protected void configure(GsonBuilder builder) {
 
     }
 
     public Gson getGson() {
         if (null == gson) {
             GsonBuilder builder = new GsonBuilder();
             configure(builder);
             gson = builder.create();
         }
         return gson;
     }
 }
