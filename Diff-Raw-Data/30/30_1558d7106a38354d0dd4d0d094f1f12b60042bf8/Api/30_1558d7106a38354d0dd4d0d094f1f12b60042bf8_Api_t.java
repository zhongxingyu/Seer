 package com.embedly.api;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.commons.logging.Log;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * Api is used to access the Embedly API and Pro endpoints.
  */
 public class Api {
     private String key;
     private String host;
     private String userAgent;
 
     private HttpClient _httpClient;
     private ResponseHandler<String> _responseHandler;
     
     /**
      * We use this somewhat strange pattern for logging so things will
      * work on Android, which doesn't provide LogFactory in it's platform.
      *
      * To enable logging, do something like this before using the Api class::
      *
      *     Api.setLog(LogFactory.getLog(Api.class));
      */
     private static Log noopLog = new Api.NoopLog();
 
     private static Log log = null;
 
     public static Log getLog() {
     	if (log == null) {
     	    return noopLog;
     	}
 		return log;
 	}
 
 	public static void setLog(Log log) {
 		Api.log = log;
 	}
 
 	public Api(String userAgent) {
         this(userAgent, null, null);
     }
 
     public Api(String userAgent, String key) {
         this(userAgent, key, null);
     }
 
     /**
      * The Api object is used to access both Embedly API
      * and Embedly Pro.  When a key is used, the Pro endpoints
      * oembed, preview and objectify are available.  When no
      * key is used, the API endpoints services and oembed are
      * available.
      *
      * A userAgent containing, at the very least, a contact
      * email address for the client applications operator.  This
      * is used so that Embedly can contact the client in the case
      * of any issues or improper use.  Your client may be blocked
      * by embedly if a contact address is not provided.
      *
      * ex. "Mozilla/5.0 (compatible; myapp/1.0; +my@email.com)
      *
      * @param userAgent A userAgent string containing contact information
      *
      * @param key       Your Embedly PRO key
      * 
      * @param host      An alternative hostname, used for debugging.  
      *                  ex. "http://localhost"
      */
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
 
         // prime these
         getHttpClient();
         getResponseHandler();
     }
 
     /**
      * Call the oembed endpoint.  Valid for both API and Pro instances.
      *
      * @param params A map of name value pairs.  The value should be either
      *               a String, or a String[].  At this time, String[] is only
      *               valid for the "urls" parameter.
      *
     * @return JSONArray of JSONObjects.  
      *          @see {@link https://pro.embed.ly/docs/oembed}
      */
     public JSONArray oembed(Map<String, Object> params) {
         return this.apicall("1", "oembed", params);
     }
 
     /**
      * Call the objectify endpoint.  Valid for Pro instances.
      *
      * @param params A map of name value pairs.  The value should be either
      *               a String, or a String[].  At this time, String[] is only
      *               valid for the "urls" parameter.
      *
     * @return JSONArray of JSONObjects.  
      *          @see {@link https://pro.embed.ly/docs/objectify}
      */
     public JSONArray objectify(Map<String, Object> params) {
         return this.apicall("2", "objectify", params);
     }
 
     /**
      * Call the preview endpoint.  Valid for Pro instances.
      *
      * @param params A map of name value pairs.  The value should be either
      *               a String, or a String[].  At this time, String[] is only
      *               valid for the "urls" parameter.
      *
     * @return JSONArray of JSONObjects.  
      *          @see {@link https://pro.embed.ly/docs/preview}
      */
     public JSONArray preview(Map<String, Object> params) {
         return this.apicall("1", "preview", params);
     }
 
     /**
      * Generic Embedly endpoint call.  This shouldn't be called directly, but
      * is used internally by the oembed, objectify and preview endpoint
      * methods.
      *
      * @param version Endpoint version
      * 
      * @param action  Endpoint name
      *
      * @param params  A map of name value pairs.  The value should be either
      *                a String, or a String[].  At this time, String[] is only
      *                valid for the "urls" parameter.
      *
     * @return JSONArray of JSONObjects.  
      *          @see {@link https://pro.embed.ly/docs/}
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
                 getLog().debug("checking urls against services");
                 resp = filterByServices(urls, servicesPattern());
             }
 
             if (urls.size() > 0) {
                 String call = this.host+"/"+version+"/"+action+"?"+
                     query.toQuery();
                 String json_text = simpleHTTP(call, null);
                 fillResponse(resp, new JSONArray(json_text));
             }
 
         } catch (JSONException e) {
             getLog().error("Failed to parse JSON in response", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("Failed to parse JSON in response", e);
         } catch (UnsupportedEncodingException e) {
         	getLog().error("Parameters couldn't be encoded with utf-8", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException(
                     "Parameters couldn't be encoded with utf-8", e);
         } catch (IOException e) {
         	getLog().error("HTTP call failed", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("HTTP call failed", e);
         }
         if (getLog().isDebugEnabled()) {
             getLog().debug("Returning >> "+resp);
         }
         return resp;
     }
 
     /**
      * Filters invalid urls and prepares a JSONArray for response.  This is
      * used internally by apicall and should not be called directly.
      * 
      * SIDE EFFECT WARNING! This method modifies the urls parameter!
      *
      * The JSON array will have null values where responses will be inserted
      * and 401 responses where urls were invalid.  After calling the embedly
      * api, you can fill in the null values with the responses in the order
      * they come back in.
      *
      * @param urls     An ArrayList<String> of urls to check.  This will be
      *                 stripped of invalid urls.
      *
      * @param regex    A pattern to check urls against
      *
     * @return        A JSONArray with 401 error responses filled in for
      *                 invalid urls.
      */
     public JSONArray filterByServices(ArrayList<String> urls, Pattern regex)
                                                          throws JSONException {
     	getLog().debug("checking urls against services");
         JSONArray response = new JSONArray();
         for (int i = urls.size() - 1; i >= 0; --i) {
             String url = urls.get(i);
             Matcher match = regex.matcher(url);
             if (match.matches()) {
             	getLog().debug("url: "+url+" is valid");
                 response.put(i, (JSONObject)null);
             } else {
             	getLog().debug("url: "+url+" isn't valid");
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
 
     /**
      * Takes a JSONArray with empty place holders and fills it with filler.
      *
      * This method modifies toFill
      *
      * @param toFill  JSONArray will null values where values should be written
      *
      * @param filler  JSONArray with values that will be written to toFill
      */
     public void fillResponse(JSONArray toFill, JSONArray filler)
                                                 throws JSONException {
         int filler_index = 0;
         for (int i = 0; i < toFill.length(); ++i) {
             if (toFill.isNull(i)) {
                 toFill.put(i, filler.getJSONObject(filler_index));
                 if (filler_index >= filler.length()) {
                     // This should _never_ happen
                 	getLog().error("we're on index "+filler_index+
                             " but real_resp only has "+
                             filler.length()+" members.");
                 	getLog().debug("Current response: "+toFill.toString());
                     throw new RuntimeException("Something went " +
                             "terribly wrong parsing the response");
                 }
                 filler_index++;
             }
         }
     }
 
     /**
      * Returns a JSON array from the API services endpoint.  This method
      * is only applicable to API hosts.
      *
     * @return JSONArray of services @see {@link http://api.embed.ly/docs/service}
      */
     public JSONArray services() {
         JSONArray resp = null;
         try {
             // fail safe response
             resp = new JSONArray("[]");
 
             if (key != null) {
             	getLog().error("Pro doesn't support services");
                 throw new RuntimeException("Pro doesn't support services");
             }
 
             String call = this.host+"/1/services/javascript";
             resp = new JSONArray(simpleHTTP(call, null));
         } catch (JSONException e) {
         	getLog().error("Failed to parse JSON in response", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("Failed to parse JSON in response", e);
         } catch (IOException e) {
         	getLog().error("HTTP call failed", e);
             // TODO: add more details of call and stack trace
             throw new RuntimeException("HTTP call failed", e);
         }
         return resp;
     }
 
     /**
      * Returns a pattern object to match against URLs.  This method is only
      * applicable to API hosts, since Pro accepts any URL.
      *
     * @return Pattern for URLs valid on API endpoints
      */
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
             Pattern ret = Pattern.compile(stringJoin(regexList, "|"));
             return ret;
         } catch (PatternSyntaxException e) {
         	getLog().error("Unexpected issue with services response", e);
             // TODO: add more details of exception and stack trace
             throw new RuntimeException(
                     "Unexpected issue with services response", e);
         } catch (JSONException e) {
         	getLog().error("Unexpected issue with services response", e);
             // TODO: add more details of exception and stack trace
             throw new RuntimeException(
                     "Unexpected issue with services response", e);
         }
     }
 
     private HttpClient getHttpClient() {
         if (_httpClient == null) {
             _httpClient = new DefaultHttpClient();
         }
         return _httpClient;
     }
 
     private ResponseHandler<String> getResponseHandler() {
         if (_responseHandler == null) {
             _responseHandler = new BasicResponseHandler();
         }
         return _responseHandler;
     }
 
     private String simpleHTTP(String url, Map<String, String> headers)
                                                        throws IOException {
         getLog().debug("calling  >> "+url);
         HttpGet httpget = new HttpGet(url);
         String response = getHttpClient().execute(httpget, getResponseHandler());
         if (getLog().isDebugEnabled()) {
             getLog().debug("response << "+response);
         }
         return response;
     }
 
     public String toString() {
         return "com.embedly.api.Api[key="+key+",host="+host+",userAgent="+
             userAgent+"]";
     }
     
     private String stringJoin(ArrayList<String> parts, String seperator) {
     	StringBuffer buffer = new StringBuffer();
     	for (int i = 0; i < parts.size() - 1; ++i) {
     		buffer.append(parts.get(i));
     		buffer.append(seperator);
     	}
     	buffer.append(parts.get(parts.size() - 1));
     	return buffer.toString();
     }
     
     private static class NoopLog implements Log {
 		public void debug(Object arg0) {}
 		public void debug(Object arg0, Throwable arg1) {}
 		public void error(Object arg0) {}
 		public void error(Object arg0, Throwable arg1) {}
 		public void fatal(Object arg0) {}
 		public void fatal(Object arg0, Throwable arg1) {}
 		public void info(Object arg0) {}
 		public void info(Object arg0, Throwable arg1) {}
 		public boolean isDebugEnabled() {return false;}
 		public boolean isErrorEnabled() {return false;}
 		public boolean isFatalEnabled() {return false;}
 		public boolean isInfoEnabled() {return false;}
 		public boolean isTraceEnabled() {return false;}
 		public boolean isWarnEnabled() {return false;}
 		public void trace(Object arg0) {}
 		public void trace(Object arg0, Throwable arg1) {}
 		public void warn(Object arg0) {}
 		public void warn(Object arg0, Throwable arg1) {}
 	}
 }
