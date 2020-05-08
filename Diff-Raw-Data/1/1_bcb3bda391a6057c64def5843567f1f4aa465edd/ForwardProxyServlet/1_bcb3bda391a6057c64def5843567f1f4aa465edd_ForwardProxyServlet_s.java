 /**
  * 
  */
 package org.uli;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.api.ProxyConfiguration;
 import org.eclipse.jetty.client.api.Request;
 import org.eclipse.jetty.proxy.ProxyServlet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author uli
  *
  */
 public class ForwardProxyServlet extends ProxyServlet {
 
     private final Logger myLogger = LoggerFactory.getLogger(ForwardProxyServlet.class);
     
     private final static String FORWARD_PROXY_PROPERTIES = "/forward-proxy.properties";
     private final static String PARENT_PROXY_HOST = "parentProxyHost";
     private final static String PARENT_PROXY_PORT = "parentProxyPort";
     private final static String REPLACE_HEADERS = "replaceHeaders";
 
     private Properties properties;
     private String parentProxyHost = null;
     private int parentProxyPort = 0;
     private List<header> headers = new LinkedList<header>();
     private ProxyConfiguration proxyConfiguration = null;
     
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * 
      */
     public ForwardProxyServlet() {
         super();
         initProperties();
     }
     
     @Override
     protected HttpClient createHttpClient() throws ServletException {
         myLogger.debug("-> createHttpClient()");
         try {
             HttpClient httpClient = super.createHttpClient();
             if (proxyConfiguration != null) {
                 myLogger.info(":. Using parent proxy {}:{}", this.parentProxyHost, this.parentProxyPort);
                 httpClient.setProxyConfiguration(proxyConfiguration);
             }
             return httpClient;
         } finally {
             myLogger.debug("<- createHttpClient()");
         }
     }
     
     @Override
     protected HttpClient newHttpClient() {
         myLogger.debug("-> newHttpClient()");
         HttpClient httpClient = super.newHttpClient();
         myLogger.debug("<- newHttpClient()");
         return httpClient;
     }
     
     @Override
     protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request)
     {
         for (header h : this.headers) {
             proxyRequest.header(h.name, null); // remove the old header
             proxyRequest.header(h.name, h.value);
         }
     }
 
     private final void initProperties() {
         InputStream is = this.getClass().getResourceAsStream(FORWARD_PROXY_PROPERTIES);
         this.properties = new Properties();
         try {
             properties.load(is);
         } catch (IOException e) {
             myLogger.warn("Unable to load properties from {}", FORWARD_PROXY_PROPERTIES);
         }
         this.parentProxyHost = this.properties.getProperty(PARENT_PROXY_HOST);
         String parentProxyPortString = this.properties.getProperty(PARENT_PROXY_PORT);
         if (parentProxyPortString != null) {
             this.parentProxyPort = Integer.parseInt(parentProxyPortString);
         }
         if (this.parentProxyHost != null) {
             this.proxyConfiguration = new ProxyConfiguration(this.parentProxyHost, this.parentProxyPort);
         }
         String headersString = this.properties.getProperty(REPLACE_HEADERS);
         if (headersString != null && headersString.trim().length() > 0) {
             String[] headerNames = headersString.split(",");
             for (String headerName : headerNames) {
                 String headerValue = this.properties.getProperty(headerName.trim(), "");
                 if (headerValue.trim().length() > 0) {
                     header h = new header(headerName, headerValue);
                     this.headers.add(h);
                 } else {
                     myLogger.warn("No value defined for header {} -> ignored", headerName);
                 }
             }
         }
     }
     
     final class header {
         final String name;
         final String value;
         
         public header(final String name, final String value) {
             this.name = name;
             this.value = value;
         }
     }
 }
