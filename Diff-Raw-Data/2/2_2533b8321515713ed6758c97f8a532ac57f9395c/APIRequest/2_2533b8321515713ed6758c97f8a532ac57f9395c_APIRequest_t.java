 package com.ideas.api.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Request object.
  */
 public class APIRequest
 {
 
     public static final String JSON_DATA = "JSON_DATA";
 
    private static final String baseRequestUrl = "http://coreapi.net:8080/SNAPSHOTS/scott/1%2E0%2E2%2DSNAPSHOT";
 
     private RequestType requestType;
     private Class requestEntityClass;
     private String requestUrl;
     private Map<String, String> requestParametersMap = new HashMap<String, String>();
 
 
     public Class getRequestEntityClass()
     {
         return requestEntityClass;
     }
 
     public void setRequestEntityClass(Class requestEntityClass)
     {
         this.requestEntityClass = requestEntityClass;
     }
 
     /**
      * Get the api request type.
      *
      * @return The api request type.
      */
     public RequestType getRequestType()
     {
         return requestType;
     }
 
     /**
      * Set the api request type.
      *
      * @param requestType The api request type.
      */
     public void setRequestType(RequestType requestType)
     {
         this.requestType = requestType;
     }
 
     /**
      * Get the api url of the requested resource.
      *
      * <p />
      *
      * This method will use the base request url and append the request url set through
      * {@link APIRequest#setRequestUrl(String)} to the end of it.
      *
      * <p />
      *
      * If the request url contains variables the request parameters map will be used to replace
      * those variables with their corresponding values.
      *
      * @return The API url.
      *
      * @todo Properly handle malformed urls in api requests.
      * @todo Ensure that request urls do not contain variables when they are returned.
      */
     public URL getRequestUrl()
     {
 
         String requestUrl = this.requestUrl;
 
         Iterator<String> keySetIterator = getRequestParametersMap().keySet().iterator();
 
         while (keySetIterator.hasNext())
         {
             String key = keySetIterator.next();
             String regex = "\\{\\$" + key + "\\}";
             requestUrl = requestUrl.replaceAll(regex, getRequestParametersMap().get(key));
         }
 
 
         URL url;
         try
         {
             url = new URL(APIRequest.baseRequestUrl + requestUrl);
         }
         catch (MalformedURLException e)
         {
             e.printStackTrace();
             url = null;
         }
         return url;
     }
 
     /**
      * Set the api url of the requested resource.
      *
      * @param requestUrl The API url.
      */
     public void setRequestUrl(String requestUrl)
     {
         this.requestUrl = requestUrl;
     }
 
     /**
      * Add a request parameter to the api request.
      *
      * @param parameterName Name of the parameter.
      * @param parameterValue The value of the parameter.
      */
     public void addRequestParameter (String parameterName, String parameterValue)
     {
         getRequestParametersMap().put(parameterName, parameterValue);
     }
 
     /**
      * Get the request parameter map.
      *
      * @return The request parameter map.
      */
     public Map<String, String> getRequestParametersMap()
     {
         return requestParametersMap;
     }
 }
