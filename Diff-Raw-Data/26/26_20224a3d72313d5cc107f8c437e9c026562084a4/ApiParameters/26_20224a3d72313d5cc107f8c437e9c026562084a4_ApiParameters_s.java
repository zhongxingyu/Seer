 package com.embedly.api;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 
 import org.apache.commons.lang.StringUtils;
 
 class ApiParameters {
     private Map<String, ArrayList<String>> params;
 
     public ApiParameters() {
         params = new HashMap<String, ArrayList<String>>();
     }
 
     public void push(String name, String value) {
         ArrayList<String> param = getParam(name);
         param.add(value);
     }
 
     public void push(String name, String[] value) {
         ArrayList<String> param = getParam(name);
         param.addAll(Arrays.asList(value));
     }
 
     public ArrayList<String> getParam(String name) {
         ArrayList<String> param = params.get(name);
         if (param == null) {
             param = new ArrayList<String>();
             params.put(name, param);
         }
         return param;
     }
 
     public String toQuery() throws UnsupportedEncodingException {
         ArrayList<String> query = new ArrayList<String>();
         for (Map.Entry<String, ArrayList<String>> entry : params.entrySet()) {
             String key = entry.getKey();
            if ("url".equals(key)) {
                key = "urls";
            }
             for (String value : entry.getValue()) {
                 query.add(URLEncoder.encode(key, "utf-8") + 
                         "=" + URLEncoder.encode(value, "utf-8"));
             }
         }
 
         return StringUtils.join(query, "&");
     }
 
     public String toString() {
         return "com.embedly.api.ApiParameters["+this.params+"]";
     }
 }
