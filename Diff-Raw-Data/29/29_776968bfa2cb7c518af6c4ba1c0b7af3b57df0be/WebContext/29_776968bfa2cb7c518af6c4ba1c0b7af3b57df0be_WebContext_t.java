 /*
  * jMaki Version 2
  *
  * Copyright (c) 2009 jmaki.org
  * 
  * Licensed under the MIT License:
  * 
  *  http://www.opensource.org/licenses/mit-license.php
  *
  */
 package org.jmaki.model.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jmaki.model.IConfig;
 
 public class WebContext extends BaseContext {
     
     private IConfig globalConfig;
     private HttpServletRequest req = null;
     private HttpServletResponse resp = null;
     private String contextRoot = null;
     private ServletContext sctx;
     private String webRoot = null;
     private String uuidScope = "request";
     private boolean devMode = false;
 
     public WebContext(HttpServletRequest req,
                       HttpServletResponse resp,
                       IConfig globalConfig) {
         this.req = req;
         this.resp = resp;
         this.globalConfig = globalConfig;
         this.sctx = req.getSession().getServletContext();
         if (globalConfig.getAttribute(IConfig.DEV_MODE) != null) {
             this.devMode = ((Boolean)globalConfig.getAttribute(IConfig.DEV_MODE)).booleanValue();
         }
     }
 
     public boolean isDevMode() {
         return devMode;
     }
 
     public String getContextRoot() {
         if (contextRoot == null ) {
             if (globalConfig.getAttribute(IConfig.CONTEXT_ROOT) == null) {
                 contextRoot = req.getContextPath();
                 globalConfig.setAttribute(IConfig.CONTEXT_ROOT, contextRoot);
             } else {
                 contextRoot = (String)globalConfig.getAttribute(IConfig.CONTEXT_ROOT);
             }
         }
         return contextRoot;
     }
 
     public IConfig getGlobalConfig() {
         return globalConfig;
     }
 
     public long getLastUpdated(String name) {
         URL url = null;
         try {
             url = sctx.getResource(name);
             if (url != null) {
                 URLConnection uc = url.openConnection();
                 long lastMod = uc.getLastModified();
                 return lastMod;
             } else {
                 getLogger().warning("Error locating resource : " + name);
                 return -1;
             }
         } catch (MalformedURLException e) {
         } catch (IOException e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     public StringBuffer getResource(String baseDir, String name) throws IOException {
 
         String resourceName = null;
 
         if (name.startsWith("/")) {
             resourceName = name;
         } else {
             resourceName = baseDir + name;
         }
             InputStream is = getResourceAsStream(resourceName);
             if (is != null) {
                 StringBuffer contents = loadStringFromInputStream(is, (String)globalConfig.getAttribute(IConfig.ENCODING));
                    return new StringBuffer(contents);
             } else {
                 // don't throw out the resource name to end user
                 getLogger().log(Level.SEVERE, "Error loading " + resourceName);
                 throw new IOException("Error loading resource. Please notify the administrator that there was an issue.");
             }
     }
 
     /*
      * 
      * Parses a expression like request.user.name
      * Which translates to : http servlet request.getAttribute("user").getName();
      * 
      * The scopes are: 
      * 
      * request - HttpServlet Request
      * session - HttpSession
      * context - ServletContext
      * none - protorabbit context (similar to request) 
      *
      */
     public Object parseExpression(String expression) {
 
         String scope = null;
         String[] path = {expression};
         if (expression.indexOf(".") != -1) {
             path = expression.split("\\.");
         }
         int start = 1;
         
         Object target = null;
         if (path.length > 1) {
 
             scope = path[0];
 
             if ("request".equals(scope)) {
                 target = getRequest().getAttribute(path[1]);
             } else if ("session".equals(scope) && getRequest().getSession() != null) {
                 target = getRequest().getSession().getAttribute(path[1]);
             } else if ("context".equals(scope) && getRequest().getSession() != null) {
                 target = getRequest().getSession().getServletContext().getAttribute(path[1]);
             } else if ("static".equals(scope)) {
                 String className = "";
                 for (int i=1; i < path.length -1; i++) {
                     className += path[i];
                     start += 1;
                     // check if we are a Class (assuming classes start with Upper Case Character
                     if (path[i].length() > 0 &&
                         (Character.isUpperCase(path[i].charAt(0)))) {
                         break;
                     } else {
                         if (i < path.length -2) {
                             className += ".";
                         }
                     }
                 }
                 String targetMethod = null;
                 if (start < path.length) {
                     targetMethod = path[start];
                 } else {
                     getLogger().warning("Non Fatal Error looking up property : " + className + ". No property.");
                     return null;
                 }
                try {
                    Class<?> c = Class.forName(className);
                    target = getObject(c, null,targetMethod);
                } catch (ClassNotFoundException cfe) {
                    getLogger().warning("Non Fatal Error looking up property : " + className);
                    return null;
                }
             } else {
                 target = getAttribute(expression);
             }
         } else {
             target = getAttribute(expression);
         }
         if (scope != null) {
             start += 1;
         }
         // if there is anything below the scope and object
         for (int i=start; target != null && i < path.length; i++) {
             target = getObject(target.getClass(), target, path[i]);
         }
 
         return target;
     }
 
     public Object getObject(Class<?> c, Object pojo, String target) {
         String getTarget = "get" + target.substring(0,1).toUpperCase() + target.substring(1);
         String isTarget = "is" + target.substring(0,1).toUpperCase() + target.substring(1);
         Object[] args = {};
         Method[] methods = c.getMethods();
 
         for (int i=0; i < methods.length;i++) {
             try {
                 Method m = methods[i];
                 if (Modifier.isPublic(m.getModifiers()) &&
                     m.getParameterTypes().length == 0 &&
                    ( m.getName().equals(getTarget) ||
                      m.getName().equals(target) ||
                      m.getName().equals(isTarget)
                      
                      )) {
                     // change the case of the property from camelCase
                     Object value = null;
                     if (pojo == null) {
                         value = m.invoke(pojo);
                     } else {
                         value = m.invoke(pojo, args);
                     }
                     return value;
                 }
             } catch (IllegalArgumentException e) {
                 getLogger().warning("Non Fatal Error looking up property : " + target + " on object " + pojo + " " + e);
             } catch (IllegalAccessException e) {
                getLogger().warning("Non Fatal Error looking up property : " + target + " on object " + pojo + " " + e);
             } catch (InvocationTargetException e) {
                 getLogger().warning("Non Fatal Error looking up property : " + target + " on object " + pojo + " " + e);
             } catch (Exception e) {
                 getLogger().warning("Non Fatal Error looking up property : " + target + " on object " + pojo + " " + e);
             }
         }
         return null;
     }
 
     /*
      * Test whether an expression matches the user agent
      */
     public boolean test(String test) {
         boolean notTest = false;
         boolean matches = false;
         Object lvalue = null;
         Object rvalue = null;
 
         int equalStart = test.indexOf("==");
         int notEqualStart = test.indexOf("!=");
 
         String rvalueString = null;
         String lvalueString = null;
   
         // we are an equal test
         if (equalStart != -1) {
             lvalueString = test.substring(0, equalStart);
             rvalueString = test.substring(equalStart+2);
         } else if (notEqualStart != -1){
             notTest = true;
             lvalueString = test.substring(0, notEqualStart);
             rvalueString = test.substring(notEqualStart+2);
         }
 
         lvalue = findValue(lvalueString);
         rvalue = findValue(rvalueString);
 
         if (getLogger().isLoggable(Level.FINEST )) {
             getLogger().log(Level.FINEST, "rvalue=" + lvalueString + " result=" + lvalue);
             getLogger().log(Level.FINEST, "lvalue=" + lvalueString + " result=" + rvalue);
         }
 
         if (lvalue != null && rvalue != null &&
             String.class.isAssignableFrom(lvalue.getClass())) {
             if (String.class.isAssignableFrom(rvalue.getClass())) {
                 matches = ((String)lvalue).equals((String)rvalue);
             }
         } else if (lvalue != null && rvalue != null &&
                 Boolean.class.isAssignableFrom(lvalue.getClass())) {
             if (Boolean.class.isAssignableFrom(rvalue.getClass())) {
                 matches = ((Boolean)lvalue).equals((Boolean)rvalue);
             }
         } else if (lvalue != null && rvalue != null &&
                 Number.class.isAssignableFrom(lvalue.getClass())) {
             if (Number.class.isAssignableFrom(rvalue.getClass())) {
                 matches = ((Number)lvalue).byteValue() == ((Number)rvalue).byteValue();
             }
         } else {
             matches = (lvalue == rvalue);
         }
         if (notTest) {
             return (!matches);
         } else {
             return matches;
         }
     }
 
     /*
      * Test whether an expression matches the user agent
      */
     public boolean uaTest(String test) {
         boolean matches = false;
         String userAgent = req.getHeader("User-Agent");
         if (userAgent != null) {
             Pattern p = Pattern.compile(test);
             Matcher m = p.matcher(userAgent);
             return m.find();
         }
         return matches;
     }
 
     public HttpServletRequest getRequest() {
         return req;
     }
 
     public HttpServletResponse getResponse() {
         return resp;
     }
 
     public static StringBuffer loadStringFromInputStream(InputStream in, String encoding) throws IOException {
         ByteArrayOutputStream out = null;
         try {
 
             byte[] buffer = new byte[1024];
             int read = 0;
             out = new ByteArrayOutputStream();
             while (true) {
                 read = in.read(buffer);
                 if (read <= 0)
                     break;
                 out.write(buffer, 0, read);
             }
             StringBuffer buff = null;
             if (encoding == null) {
                 buff = new StringBuffer(out.toString());
             } else {
                buff = new StringBuffer(out.toString(encoding));
             }
             return buff;
         } finally {
             if (in != null) {
                 in.close();
             }
             if (out != null) {
                 out.flush();
                 out.close();
             }
         }
     }
 
     public InputStream getResourceAsStream( String resourceName)
             throws IOException {
         return sctx.getResourceAsStream(resourceName);
     }
 
     public boolean isUpdated(String fullURI, long lastUpdated) {
         long lastCheck = getLastUpdated(fullURI);
         if (lastCheck != -1) {
             return (lastUpdated > lastCheck);
         } else {
             return false;
         }
     }
 
     public boolean resourceExists(String name) {
         try {
             return (getResourceAsStream(name) != null);
         } catch (IOException e) {
             return false;
         }
     }
 
     /**
      * @return the &ltprotocoll&gt://&lthost&gt:&ltport&gt portion<BR>
      * of the current request
      * to overwrite use initparameter 'jmaki-webroot'
      */
     public String getWebRoot() {
         if (webRoot == null) {
             StringBuffer webRootBuffer = new StringBuffer(req.getScheme() + "://").append(req.getServerName());
             // don't add the port if https on the default port
             if (req.getServerPort() != 80 && 
                 !("https".equals(req.getScheme()) && req.getServerPort() != 443) ) {
                 webRootBuffer.append(":").append(req.getServerPort());
             }
             webRoot = webRootBuffer.toString();
         }
         return webRoot;
     }
     public String generateUuid(String prefix) {
         String uuid = null;
         if ("request".equals(uuidScope)) {
             Long counter = (Long)getAttribute("counter");
             if (counter == null) {
                 counter = new Long(0);
             }
             long clong = counter.longValue();
             clong++;
             setAttribute("counter", new Long(clong));
             return prefix + "_" + counter;
         }
         return uuid;
     }
 
     public Object getGlobalAttribute(String key) {
         return sctx.getAttribute(key);
     }
 
     public void setGlobalAttribute(String key, Object value) {
         sctx.setAttribute(key, value);
         
     }
 
     private static Logger logger;
 
    static Logger getLogger() {
         if (logger == null) {
             logger = Logger.getLogger("org.jmaki");
         }
         return logger;
     }
 }
