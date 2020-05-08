 package org.mule.galaxy.web;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gwtwidgets.server.spring.GWTRPCServiceExporter;
 
 import com.google.gwt.user.server.rpc.RPCRequest;
 import com.google.gwt.user.server.rpc.SerializationPolicy;
 import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
 
 /**
  * A service exporter which also looks on the classpath for serialization policy
  * files (in "galaxy/web/XXXX")
  */
 public class GwtRpcServiceExporter extends GWTRPCServiceExporter {
 
     private static final long serialVersionUID = 1L;
 
     private static final Log logger = LogFactory.getLog("org.mule.galaxy.web.services");
     private final ClassLoader classLoader;
     private final long maxExpectedExecutionTime;
     private static final long DEFAULT_MAX_EXPECTED_EXECUTION_TIME = 5000L;
 
     public GwtRpcServiceExporter(final ClassLoader classLoader) {
         this(classLoader, GwtRpcServiceExporter.DEFAULT_MAX_EXPECTED_EXECUTION_TIME);
     }
 
     public GwtRpcServiceExporter(final ClassLoader classLoader, final long maxExpectedExecutionTime) {
         this.classLoader = classLoader;
         this.maxExpectedExecutionTime = maxExpectedExecutionTime;
     }
 
     protected String extractMethodInvocationIdentifier(final Method method) {
         return method.getDeclaringClass().getSimpleName()+":"+method.getName()+"("+Arrays.toString(method.getParameterTypes());
     }
 
     @Override
     protected final String invokeMethodOnService(final Object service, final Method targetMethod, final Object[] targetParameters, final RPCRequest rpcRequest) throws Exception {
         final long before = System.currentTimeMillis();
         final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(this.classLoader);
         try {
             return super.invokeMethodOnService(service, targetMethod, targetParameters, rpcRequest);
         } finally {
             Thread.currentThread().setContextClassLoader(classLoader);
             final long time = System.currentTimeMillis()-before;
             final String message = "Execution of <"+extractMethodInvocationIdentifier(targetMethod)+")> with parameters <"+Arrays.toString(targetParameters)+"> took <"+time+"> ms.";
             if (time > this.maxExpectedExecutionTime) {
                 if (GwtRpcServiceExporter.logger.isWarnEnabled()) {
                     GwtRpcServiceExporter.logger.warn(message);
                 }
             } else {
                 if (GwtRpcServiceExporter.logger.isDebugEnabled()) {
                     GwtRpcServiceExporter.logger.debug(message);
                 }
             }
         }
     }
 
     @Override
    protected String handleServiceException(final Exception e, final Object service, final Method targetMethod, final RPCRequest rpcRequest) throws Exception {
         if (GwtRpcServiceExporter.logger.isWarnEnabled()) {
            GwtRpcServiceExporter.logger.warn("Got exception while executing <"+extractMethodInvocationIdentifier(targetMethod)+">: "+e.toString());
         }
        return super.handleServiceException(e, service, targetMethod, rpcRequest);
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
