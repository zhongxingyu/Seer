 package org.amanzi.awe.catalog.json;
 
 import java.io.File;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;
 
 public class JSONServiceExtension implements ServiceExtension {
     /* CSV service key, URL to the CSV file */
     public static final String URL_KEY = "org.amanzi.awe.catalog.json.url";
     public static final String CLASS_KEY = "org.amanzi.awe.catalog.json.class";
     
     public Map<String, Serializable> createParams( URL url ) {
         try {
             if (url.getProtocol().equals("file")) {
                 // the URL represent a normal file on disk
                File file = URLUtils.urlToFile(url);
                 if (file.exists()) {
                     // check the filename, is it a CSV file?
                     if (file.getName().toLowerCase().endsWith(".json") || file.getName().toLowerCase().endsWith(".geo_json")) {
                         Map<String, Serializable> params = new HashMap<String, Serializable>();
                         params.put(URL_KEY, url);
                         params.put(CLASS_KEY, File.class);
                         return params;
                     }
                 }
             } else if (url.getProtocol().equals("http") && url.getPath().toLowerCase().endsWith("json")) {
                 // the URL represents a web service accessible through HTTP
                 url.openConnection(); // throws exception if it fails, and we return null below
                 Map<String, Serializable> params = new HashMap<String, Serializable>();
                 params.put(URL_KEY, url);
                 params.put(CLASS_KEY, URL.class);
                 return params;
             }
         } catch (Throwable t) {
             // something went wrong, URL must be for another service
            System.err.println("Failed to initialize parameters for url["+url+"]: "+t.toString());
            t.printStackTrace(System.err);
         }
 
         // unable to create the parameters, URL must be for another service
         return null;
     }
 
     public IService createService( URL id, Map<String, Serializable> params ) {
         // good defensive programming
         if (params == null) {
             return null;
         }
 
         // check for the property service key
         if (params.containsKey(URL_KEY)) {
             // found it, create the service handle
             return new JSONService(params);
         }
 
         // key not found
         return null;
     }
 }
