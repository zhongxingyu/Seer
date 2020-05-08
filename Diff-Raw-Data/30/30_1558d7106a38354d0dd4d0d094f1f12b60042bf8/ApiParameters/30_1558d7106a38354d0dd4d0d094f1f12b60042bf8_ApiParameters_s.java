 package com.embedly.api;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * A helper class for Api.  Used to make query strings for Embedly
  * API and Pro endpoints.
  *
  * All parameters are kept in ArrayLists.  When the query string is formed,
  * all parameters are URI escaped.  If there are multiple values for a key, 
  * then they are simply appended to the query string as seperate name=value
  * pairs.  ex::
  *
  *     ApiParameters p = new ApiParameters();
  *     p.push("urls", "http://www.google.com");
  *     p.push("urls", "http://www.bing.com");
  *     System.out.println(p.toQuery());
  *     
  *     &gt;&gt; urls=http%3A%2F%2Fwww.google.com%2F&amp;urls=http%3A%2F%2Fwww.bing.com%2F
  *
  * If you are reading the source, &amp; is for javadocs.  It is an ampersand 
  * in reality.
  */
 class ApiParameters {
     private Map<String, ArrayList<String>> params;
 
     public ApiParameters() {
         params = new HashMap<String, ArrayList<String>>();
     }
 
     /**
      * Add a parameter value.
      *
      * @param name
      *
      * @param value
      */
     public void push(String name, String value) {
         name = filterName(name);
         ArrayList<String> param = getParam(name);
         param.add(value);
     }
 
     /**
      * Add an array of parameter values.
      *
      * @param name
      * 
      * @param values
      */
     public void push(String name, String[] value) {
         name = filterName(name);
         ArrayList<String> param = getParam(name);
         param.addAll(Arrays.asList(value));
     }
 
     /**
      * Get an array of parameter values.  Returns an empty
      * array if the name is not set.
      *
      * @param name Parameter name
      *
     * @returns ArrayList<String> of values
      */
     public ArrayList<String> getParam(String name) {
         name = filterName(name);
         ArrayList<String> param = params.get(name);
         if (param == null) {
             param = new ArrayList<String>();
             params.put(name, param);
         }
         return param;
     }
 
 
     /**
      * Used internally to modify value names.  We always pass urls
      * and never url to make it easier to parse responses.
      */
     private String filterName(String name) {
         if ("url".equals(name)) {
             return "urls";
         }
         return name;
     }
 
     /**
      * Returns a query string representing the parameters.
      *
     * @returns query string
      */
     public String toQuery() throws UnsupportedEncodingException {
         ArrayList<String> query = new ArrayList<String>();
         for (Map.Entry<String, ArrayList<String>> entry : params.entrySet()) {
             String key = entry.getKey();
             for (String value : entry.getValue()) {
                 query.add(URLEncoder.encode(key, "utf-8") + 
                         "=" + URLEncoder.encode(value, "utf-8"));
             }
         }
 
         return stringJoin(query, "&");
     }
 
     public String toString() {
         return "com.embedly.api.ApiParameters["+this.params+"]";
     }
 
     
     /**
      * Utility method to join strings.  We don't use StringUtils because it
      * isn't available on the Android platform.
      */
     private String stringJoin(ArrayList<String> parts, String seperator) {
     	StringBuffer buffer = new StringBuffer();
     	for (int i = 0; i < parts.size() - 1; ++i) {
     		buffer.append(parts.get(i));
     		buffer.append(seperator);
     	}
     	buffer.append(parts.get(parts.size() - 1));
     	return buffer.toString();
     }
 }
