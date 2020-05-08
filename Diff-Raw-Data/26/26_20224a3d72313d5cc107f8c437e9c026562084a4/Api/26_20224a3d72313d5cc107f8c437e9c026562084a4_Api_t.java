 package com.embedly.api;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 class Api {
     private String key;
     private String host;
     private String userAgent;
 
     private static Log log = LogFactory.getLog(Api.class);
 
     public Api(String userAgent) {
         this(userAgent, null, null);
     }
 
     public Api(String userAgent, String key) {
         this(userAgent, key, null);
     }
 
     public Api(String userAgent, String key, String host) {
         this.userAgent = userAgent;
         this.key = key;
         this.host = host;
 
         if (this.userAgent == null) {
             throw new RuntimeException(
                     "You must specify a userAgent when constructing an " +
                     "Api object");
         }
 
         if (this.key == null && this.host == null) {
             this.host = "http://api.embed.ly";
         } else if (this.host == null) {
             this.host = "http://pro.embed.ly";
         }
     }
 
     public JSONArray oembed(Map<String, Object> params) {
         return this.apicall("1", "oembed", params);
     }
 
     public JSONArray objectify(Map<String, Object> params) {
         return this.apicall("2", "objectify", params);
     }
 
     public JSONArray preview(Map<String, Object> params) {
         return this.apicall("1", "preview", params);
     }
 
     /**
      * params values should be something that has toString, or a String[]
      */
     public JSONArray apicall(String version, String action,
             Map<String, Object> params) {
         JSONArray resp = null;
         try {
             // fail safe response
             resp = new JSONArray("[]");
 
             ApiParameters query = new ApiParameters();
             for (Map.Entry<String, Object> entry : params.entrySet()) {
                 if (entry.getValue() instanceof String[]) {
                     query.push(entry.getKey().toString(),
                             (String[])entry.getValue());
                 } else {
                     query.push(entry.getKey().toString(),
                             entry.getValue().toString());
                 }
             }
 
             ArrayList<String> urls = query.getParam("urls");
             if (key != null) {
                 query.push("key", key);
                 resp = filterByServices(urls, Pattern.compile(".*"));
             } else {
                 log.debug("checking urls against services");
                 resp = filterByServices(urls, servicesPattern());
             }
 
             if (urls.size() > 0) {
                 String call = this.host+"/"+version+"/"+action+"?"+
                     query.toQuery();
                 String json_text = simpleHTTP(call, null);
                 fillResponse(resp, new JSONArray(json_text));
             }
 
         } catch (JSONException e) {
             log.error("Failed to parse JSON in response", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("Failed to parse JSON in response", e);
         } catch (UnsupportedEncodingException e) {
             log.error("Parameters couldn't be encoded with utf-8", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException(
                     "Parameters couldn't be encoded with utf-8", e);
         } catch (IOException e) {
             log.error("HTTP call failed", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("HTTP call failed", e);
         }
        log.debug("Returning >> "+resp);
         return resp;
     }
 
     /**
      * Filters invalid urls and prepares a JSONArray for response.
      *
      * The JSON array will have null values where responses will be inserted
      * and 401 responses where urls were invalid.  After calling the embedly
      * api, you can fill in the null values with the responses in the order
      * they come back in.
      */
     public JSONArray filterByServices(ArrayList<String> urls, Pattern regex)
                                                          throws JSONException {
         log.debug("checking urls against services");
         JSONArray response = new JSONArray();
         for (int i = urls.size() - 1; i >= 0; --i) {
             String url = urls.get(i);
             Matcher match = regex.matcher(url);
             if (match.matches()) {
                 log.debug("url: "+url+" is valid");
                 response.put(i, (JSONObject)null);
             } else {
                 log.debug("url: "+url+" isn't valid");
                 response.put(i, new JSONObject("" +
                     "{ url: \""+url+"\"" +
                     ", error_code: \"401\"" +
                     ", error_message: \"This service requires an Embedly Pro" +
                                        " account\"" +
                     ", type: \"error\"" +
                     ", version: \"1.0\"" +
                     "}"
                 ));
                 urls.remove(i);
             }
         }
         return response;
     }
 
     public void fillResponse(JSONArray toFill, JSONArray filler)
                                                 throws JSONException {
         int filler_index = 0;
         for (int i = 0; i < toFill.length(); ++i) {
             if (toFill.isNull(i)) {
                 toFill.put(i, filler.getJSONObject(filler_index));
                 if (filler_index >= filler.length()) {
                     // This should _never_ happen
                     log.error("we're on index "+filler_index+
                             " but real_resp only has "+
                             filler.length()+" members.");
                     log.debug("Current response: "+toFill.toString());
                     throw new RuntimeException("Something went " +
                             "terribly wrong parsing the response");
                 }
                 filler_index++;
             }
         }
     }
 
     public JSONArray services() {
         JSONArray resp = null;
         try {
             // fail safe response
             resp = new JSONArray("[]");
 
             if (key != null) {
                 log.error("Pro doesn't support services");
                 throw new RuntimeException("Pro doesn't support services");
             }
 
             String call = this.host+"/1/services/javascript";
             resp = new JSONArray(simpleHTTP(call, null));
         } catch (JSONException e) {
             log.error("Failed to parse JSON in response", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("Failed to parse JSON in response", e);
         } catch (IOException e) {
             log.error("HTTP call failed", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("HTTP call failed", e);
         }
         return resp;
     }
 
     public Pattern servicesPattern() {
         try {
             JSONArray services = services();
             ArrayList<String> regexList = new ArrayList<String>();
             for (int i = 0; i < services.length(); ++i) {
                 JSONObject obj = services.getJSONObject(i);
                 JSONArray regexes = obj.getJSONArray("regex");
                 for (int j = 0; j < regexes.length(); ++j) {
                     regexList.add(regexes.getString(j));
                 }
             }
             Pattern ret = Pattern.compile(StringUtils.join(regexList, "|"));
             return ret;
         } catch (PatternSyntaxException e) {
             log.error("Unexpected issue with services response", e);
             // TODO: add more details of exception and stack trace
             throw new RuntimeException(
                     "Unexpected issue with services response", e);
         } catch (JSONException e) {
             log.error("Unexpected issue with services response", e);
             // TODO: add more details of exception and stack trace
             throw new RuntimeException(
                     "Unexpected issue with services response", e);
         }
     }
 
     private String simpleHTTP(String url, Map<String, String> headers)
                                                        throws IOException {
         HttpClient httpclient = new DefaultHttpClient();
         log.debug("calling  >> "+url);
         HttpGet httpget = new HttpGet(url);
         ResponseHandler<String> responseHandler = new BasicResponseHandler();
         String response = httpclient.execute(httpget, responseHandler);
        log.debug("response << "+response);
         return response;
     }
 
     public String toString() {
         return "com.embedly.api.Api[key="+key+",host="+host+",userAgent="+
             userAgent+"]";
     }
 }
