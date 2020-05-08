 package org.sidl.herenow.search;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.sidl.herenow.Venue;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URL;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *  Provides HTTP access for searching.
  */
 public class URLSearchMethod extends SearchMethod
 {
 
   public URLSearchMethod(SearchProvider provider)
   {
     super(provider);
   }
 
 
   @Override
   public SearchResponse invoke(URI uri)
   {
     int errorCode = 200;
     int available = 0;
     String message = null;
     JSONObject rawJSON = null;
     HttpURLConnection connection = null;
     InputStreamReader reader;
     URL url;
     
     try
     {
       url = uri.toURL();
           
       // make the connection and wait for the headers to come back
       connection = (HttpURLConnection) url.openConnection();
       errorCode = connection.getResponseCode();
       message = connection.getResponseMessage();
 
       logResponse(connection, errorCode, message);
 
       // check how many queries are left in our quota
       available = getProvider().getAvailableRequests(connection);
     
       if (errorCode == 200)
       {
         //read and parse the entire result
         reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
         rawJSON = readJSONResponse(reader);
         List<Venue> venues = getProvider().getLocations(rawJSON);
         return new SearchResponse(venues, available);
       }
       else
       {
         return new SearchResponse(errorCode, message, available);
       }
     }
     catch (JSONException jsone)
     {
       message = jsone.getMessage();
       errorCode = IO_ERROR_CODE;
       LOG.log(Level.WARNING, "Error parsing JSON response " + rawJSON , jsone);
     }
     catch (IOException e)
     {
       if (message == null)
       {
         message = e.getMessage();
         errorCode = IO_ERROR_CODE;
       }
 
       String err = String.format("URL failed with %d/%s to %s",
           errorCode, message, getProvider().getId());
       LOG.log(Level.WARNING, err, e);
     }    
     finally
     {
       if (null != connection)
         connection.disconnect();
     }
     return new SearchResponse(errorCode, message, available);
   }
 
 
   private void logResponse(HttpURLConnection connection, int errorCode, String message)
   {
     String status = String.format("URL responded with %d/%s to %s with http headers: %s",
       errorCode, message,  getProvider().getId(),  connection.getHeaderFields().toString());
     if (errorCode != 200)
       LOG.log(Level.INFO, status);
     else
       LOG.log(Level.FINE, status);
     LOG.log(Level.FINER, connection.getURL().toString());
   }
 
   
   private static final Logger LOG = Logger.getLogger(URLSearchMethod.class.getName());
 }
