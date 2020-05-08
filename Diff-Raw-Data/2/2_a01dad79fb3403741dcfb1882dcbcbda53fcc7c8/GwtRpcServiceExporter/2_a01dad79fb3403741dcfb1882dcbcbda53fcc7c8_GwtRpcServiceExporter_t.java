 package org.mule.galaxy.web;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.server.rpc.SerializationPolicy;
 import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.gwtwidgets.server.spring.GWTRPCServiceExporter;
 
 /**
  * A service exporter which also looks on the classpath for serialization policy
  * files (in "galaxy/web/XXXX")
  */
 public class GwtRpcServiceExporter extends GWTRPCServiceExporter {
 
     private ClassLoader classLoader;
     
 
     public GwtRpcServiceExporter(ClassLoader classLoader) {
         super();
         this.classLoader = classLoader;
     }
 
     @Override
     public String processCall(String payload) throws SerializationException {
         Thread.currentThread().setContextClassLoader(classLoader);
         
         return super.processCall(payload);
     }
     
     protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
                                                            String strongName) {
         // The request can tell you the path of the web app relative to the
         // container root.
         String contextPath = request.getContextPath();
 
         String modulePath = null;
         if (moduleBaseURL != null) {
             try {
                 modulePath = new URL(moduleBaseURL).getPath();
             } catch (MalformedURLException ex) {
                 // log the information, we will default
                 log("Malformed moduleBaseURL: " + moduleBaseURL, ex);
             }
         }
 
         SerializationPolicy serializationPolicy = null;
 
         /*
          * Check that the module path must be in the same web app as the servlet itself. If you need to
          * implement a scheme different than this, override this method.
          */
         if (modulePath == null || !modulePath.startsWith(contextPath)) {
             String message = "ERROR: The module path requested, "
                              + modulePath
                              + ", is not in the same web application as this servlet, "
                              + contextPath
                              + ".  Your module may not be properly configured or your client and server code maybe out of date.";
             log(message, null);
         } else {
             // Strip off the context path from the module base URL. It should be a
             // strict prefix.
             String contextRelativePath = modulePath.substring(contextPath.length());
 
             String serializationPolicyFilePath = SerializationPolicyLoader
                 .getSerializationPolicyFileName(contextRelativePath + strongName);
 
             // Open the RPC resource file read its contents.
             InputStream is = getServletContext().getResourceAsStream(serializationPolicyFilePath);
             
             if (is == null && serializationPolicyFilePath.startsWith("/galaxy-plugins")) {
                 for (File plugin : WebPluginManager.getPluginLocations()) {
                    File file = new File(plugin, serializationPolicyFilePath.substring("/galaxy-plugins".length()));
                     if (file.exists()) {
                         try {
                             is = new FileInputStream(file);
                         } catch (FileNotFoundException e) {
                             throw new RuntimeException(e);
                         }
                         break;
                     }
                 }
             }
             
             try {
                 if (is != null) {
                     try {
                         serializationPolicy = SerializationPolicyLoader.loadFromStream(is, null);
                     } catch (ParseException e) {
                         log("ERROR: Failed to parse the policy file '" + serializationPolicyFilePath + "'", e);
                     } catch (IOException e) {
                         log("ERROR: Could not read the policy file '" + serializationPolicyFilePath + "'", e);
                     }
                 } else {
                     String message = "ERROR: The serialization policy file '" + serializationPolicyFilePath
                                      + "' was not found; did you forget to include it in this deployment?";
                     log(message, null);
                 }
             } finally {
                 if (is != null) {
                     try {
                         is.close();
                     } catch (IOException e) {
                         // Ignore this error
                     }
                 }
             }
         }
 
         return serializationPolicy;
     }
 
     /**
      * Because we aren't embedded as a real servlet, we need this or else NPEs will occur when
      * calling log().
      */
     @Override
     public String getServletName() {
         return "GwtRpcServiceExorter";
     }
     
 }
