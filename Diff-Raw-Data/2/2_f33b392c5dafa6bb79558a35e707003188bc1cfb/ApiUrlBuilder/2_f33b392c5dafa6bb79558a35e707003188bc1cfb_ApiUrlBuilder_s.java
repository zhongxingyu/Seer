 package com.stackexchange.stacman;
 
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.HashMap;
 
 final class ApiUrlBuilder {
     private final String baseUrl;
     private final HashMap<String, String> queryStringParameters;
 
     ApiUrlBuilder(String relativeUrl, boolean useHttps) {
         baseUrl =
             String.format(
                 "%1$S://api.stackexchange.com/2.0%2$S%3$S",
                     useHttps ? "https" : "http",
                     relativeUrl.startsWith("/") ? "" : "/",
                     relativeUrl
             );
 
         queryStringParameters = new HashMap<String, String>();
     }
 
     void addParameter(String name, Object value){
         if(value != null) {
             queryStringParameters.put(name, value.toString());
         }
     }
 
     void addParameter(String name, Date dt) {
         if(dt != null) {
            addParameter(name, dt.getTime());
         }
     }
 
     void addParameter(String name, Iterable<String> values) {
         if(values != null && values.iterator().hasNext()){
             String val = "";
             for(String v : values) {
                 if(val.length() > 0) val += ";";
                 val += v;
             }
 
             addParameter(name, val);
         }
     }
 
     @Override
     public String toString(){
         String ret = baseUrl;
 
         if(queryStringParameters.size() > 0) {
             String tail ="";
             for(String key : queryStringParameters.keySet()) {
                 if(tail.length() > 0) {
                     tail += "&";
                 }else{
                     tail += "?";
                 }
                 try{
                     tail+= URLEncoder.encode(key, "UTF-8") + "="+URLEncoder.encode(queryStringParameters.get(key), "UTF-8");
                 }catch(Throwable t){
                     throw new RuntimeException(t);
                 }
             }
 
             ret += tail;
         }
 
         return ret;
     }
 }
