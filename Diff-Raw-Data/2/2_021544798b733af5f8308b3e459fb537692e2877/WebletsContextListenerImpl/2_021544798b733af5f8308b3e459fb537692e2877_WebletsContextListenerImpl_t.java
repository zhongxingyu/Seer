 /*
  * WebletContextListener.java
  *
  * Created on November 29, 2006, 12:40 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package net.java.dev.weblets.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.regex.Pattern;
 
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import net.java.dev.weblets.WebletContainer;
 import net.java.dev.weblets.WebletsServlet;
 import net.java.dev.weblets.impl.parse.DisconnectedEntityResolver;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xml.sax.SAXException;
 
 /**
  * @author john.fallows
  * @changes Werner Punz
  * 
  * Changes from 0.4 to 1.0
  *
  * Clear calling order of weblets servlets
  * and jsf servlets
  * 
  * Last references into the view handler have been removed
  * 
  * We have enforced a path trigger pattern
  * Weblets servlet overrides any other entry
  * it has higher priority than the faces 
  * servlet pattern
  * 
  * A simplified regexp handling of the pattern
  * parsing parts
  * 
  */
 public class WebletsContextListenerImpl implements ServletContextListener {
 
  
     public void contextInitialized(ServletContextEvent event) {
         ServletContext context = event.getServletContext();
         WebletContainer container = createContainer(context);
     }
 
     public void contextDestroyed(ServletContextEvent event) {
         WebletContainerImpl container = (WebletContainerImpl) WebletContainerImpl.getInstance();
         if(container != null)
         	container.destroy();
     }
 
     private WebletContainer createContainer(ServletContext context) {
         try {
             URL webXml = context.getResource("/WEB-INF/web.xml");
 
             String triggerPattern = "/faces/*";
             String contextPath = "";
             if (webXml != null) {
                 InputStream in = webXml.openStream();
                 try {
                     WebXmlParser parser = new WebXmlParser();
                     Digester digester = new Digester();
                     digester.setValidating(false);
                     digester.setEntityResolver(DisconnectedEntityResolver.sharedInstance());
                     digester.push(parser);
                     digester.addCallMethod("web-app/servlet", "addServlet", 2);
                     digester.addCallParam("web-app/servlet/servlet-name", 0);
                     digester.addCallParam("web-app/servlet/servlet-class", 1);
                     digester.addCallMethod("web-app/servlet-mapping", "addServletMapping", 2);
                     digester.addCallParam("web-app/servlet-mapping/servlet-name", 0);
                     digester.addCallParam("web-app/servlet-mapping/url-pattern", 1);
 
                     digester.addCallMethod("web-app/context-param", "addContextParam", 2);
                     digester.addCallParam("web-app/context-param/param-name", 0);
                     digester.addCallParam("web-app/context-param/param-value", 1);
 
                     digester.parse(in);
  
                     if (parser.getWebletPattern() != null && !parser.getWebletPattern().trim().equals(""))
                         triggerPattern = parser.getWebletPattern();
                    else if(parser.getFacesPattern() != null && !parser.getFacesPattern().trim().equals(""))
                         triggerPattern = parser.getFacesPattern();
 
                     contextPath = calculateContextPath(parser, context);
                     handlePathPatternWarnings(parser);
                 } catch (SAXException e) {
                     throw new RuntimeException(e);
                 } finally {
                     in.close();
                 }
             }
 
             // TODO: determine Faces Weblets ViewIds, assumes /weblets/*
             String webletsViewIds = "/weblets/*";// we add a dedicated
                                                     // weblets/ to our url for
                                                     // the filter
 
             // auto-prepend leading slash in case it is missing from web.xml
             // entry
 
             if (!triggerPattern.startsWith("/")) {
                 triggerPattern = "/" + triggerPattern;
             }
 
             // to avoid any conflicts we reserve the weblets subnamespace
             // anything under /weblets is a clear
             // reference into a weblet resource url, anything before is the
             // trigger and anything after
             // is the version and path
             String formatPattern = triggerPattern.replaceFirst("/\\*", webletsViewIds).replaceFirst("/\\*", "{0}");
            
             //TODO remove some double weblets pattern to reduce urls
             String webletsPattern = triggerPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", "weblets(/.*)");
             
             
             MessageFormat format = new MessageFormat(formatPattern);
             WebletContainerImpl container = new WebletContainerImpl(context, contextPath, format, Pattern.compile(webletsPattern));
             try {
                 URL resource = context.getResource("/WEB-INF/weblets-config.xml");
                 if (resource != null)
                     container.registerConfig(resource);
             } catch (MalformedURLException e) {
                 context.log("Unabled to register /WEB-INF/weblets-config.xml", e);
             }
             return container;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void handlePathPatternWarnings(WebXmlParser parser) {
         if (!isPathPattern(parser.getFacesPattern()) && parser.isJSFEnabled()) {
             Log logger = LogFactory.getLog(this.getClass());
             logger.warn("JSF Enabled Weblets but path pattern is missing, some relatively referenced resources might not load ");
         } else if (!isPathPattern(parser.getWebletPattern()) && parser.isServletEnabled()) {
             Log logger = LogFactory.getLog(this.getClass());
             logger.warn("Servlet Enabled Weblets but path pattern is missing, some relatively referenced resources might not load ");
         }
     }
 
     private String calculateContextPath(WebXmlParser parser, ServletContext context) {
         String contextPath;
         contextPath = parser.getWebletsContextPath();
         if (contextPath == null || contextPath.trim().equals("")) {
             try {
                 // lets check if we are in JEE 5 so that we can execute a
                 // getServletContextPath methid
                 Method[] methods = context.getClass().getMethods();
 
                 for (int cnt = 0; cnt < methods.length; cnt++) {
                     if (methods[cnt].getName().equals("getContextPath")) {
 
                         return (String) methods[cnt].invoke(context, null);
                     }
                 }
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(this.getClass());
                 log.error("Error, trying to invoke getContextPath ", e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(this.getClass());
                 log.error("Error, trying to invoke getContextPath ", e);
             }
         } else {
             return contextPath;
         }
         return "";
     }
 
     static public class WebXmlParser {
         public void addServlet(String servletName, String servletClass) {
         	if ("javax.faces.webapp.FacesServlet".equals(servletClass))
                 _facesServletName = servletName;
             if (WebletsServlet.class.getName().equals(servletClass))
                 _webletServletName = servletName;
         }
 
         public void addServletMapping(String servletName, String urlPattern) {
             if (servletName.equals(_facesServletName))
                 if (_facesPattern == null || _facesPattern.trim().equals("") || isPathPattern(urlPattern))
                     _facesPattern = urlPattern;
             if (servletName.equals(_webletServletName))
                 if (_webletPattern == null || _webletPattern.trim().equals("") || isPathPattern(urlPattern))
                     _webletPattern = urlPattern;
         }
 
         public void addContextParam(String contextName, String contextValue) {
             if (contextName != null && contextName.matches(_contextPathPattern)) {
                 _webletsContextPath = contextValue.trim();
             }
         }
 
         public boolean isServletEnabled() {
             return _webletServletName != null && !_webletServletName.trim().equals("");
         }
 
         public boolean isJSFEnabled() {
             return _facesServletName != null && !_facesServletName.trim().equals("");
         }
 
         public String getFacesPattern() {
             return _facesPattern;
         }
 
         public String getWebletPattern() {
             return _webletPattern;
         }
 
         public String getWebletsContextPath() {
             return _webletsContextPath;
         }
 
         private String _facesServletName;
         private String _facesPattern;
 
         private String _webletServletName;
         private String _webletPattern;
 
         private String _webletsContextPath;
         private String _contextPathPattern = "^\\s*net\\.java\\.dev\\.weblets\\.contextpath\\s*$";
     }
 
     private static boolean isPathPattern(String in) {
         if (in == null)
             return false;
         return in.trim().matches("^(.)*\\/.*\\/(\\*){0,1}$");
     }
 
 }
