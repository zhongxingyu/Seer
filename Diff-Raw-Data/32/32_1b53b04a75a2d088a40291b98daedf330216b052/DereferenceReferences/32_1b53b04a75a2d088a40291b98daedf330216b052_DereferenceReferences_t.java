 package org.demond.java.dereference;
 
 import org.apache.log4j.Logger;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: degtyarev.dm
  */
 public class DereferenceReferences
 {
     private static Logger logger = Logger.getLogger(DereferenceReferences.class);
 
     private static final String USER_AGENT="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2";
     private int maxNum = 10;
     private Pattern pattern = Pattern.compile("HTTP\\/1\\.\\d\\s+(\\d{3})\\s+\\w+");
     protected class Response
     {
        String code;
        String location;
     }
 
     public Map<String,String> getHTTPHeader(String urlString)
     {
         Map<String,String> responseMap = new HashMap<String, String>();
         try
         {
             URL url = new URL(urlString);
             HttpURLConnection connection = (HttpURLConnection)url.openConnection();
             connection.setInstanceFollowRedirects(false);
             System.setProperty("http.agent", USER_AGENT);
             Map<String,List<String>> headerMap = connection.getHeaderFields();
             for(String name: headerMap.keySet())
             {
                 for(String value : headerMap.get(name))
                 {
                     responseMap.put(name,value);
                 }
             }
         }
         catch (Exception e)
         {
             logger.warn("Warning",e);
         }
         return responseMap;
     }
     
     protected String getLocation(String referenceUrl)
            throws Exception
     {
         Response response = null;
         do
         {
             if(response!=null && response.location!=null)
                 referenceUrl = response.location;
             response = getResponse(referenceUrl);
             if(logger.isDebugEnabled())
             {
                 logger.debug("Location = "+response.location);
                 logger.debug("Code = "+response.code);
                 logger.debug("maxNum = "+ maxNum);
             }
             maxNum--;
         }
         while(!response.code.equals("200") && maxNum>0);
         return referenceUrl;
     }
 
     private Response getResponse(String referenceUrl)
     {
         Response response = new Response();
         Map<String,String> headerMap = getHTTPHeader(referenceUrl);
         for(String key : headerMap.keySet())
         {
 
             if(key == null)
             {
                 Matcher matcher = pattern.matcher(headerMap.get(key));
                 if(matcher.find())
                     response.code = matcher.group(1);
 
             }
             else
             {
                 if(key.equalsIgnoreCase("Location"))
                 response.location = headerMap.get(key);
             }
         }
         return response;
     }
 }
