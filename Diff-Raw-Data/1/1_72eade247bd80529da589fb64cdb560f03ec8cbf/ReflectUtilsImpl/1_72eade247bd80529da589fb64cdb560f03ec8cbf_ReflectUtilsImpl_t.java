 package net.java.dev.weblets.impl.util;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.ServletContext;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.io.OutputStream;
 import java.io.IOException;
 
 import net.java.dev.weblets.impl.WebletsContextListenerImpl;
 
 /**
  * @author werpu
  * @date: 13.11.2008
  */
 public class ReflectUtilsImpl {
     public static String calculateContextPath(WebletsContextListenerImpl.WebXmlParser parser, ServletContext context) {
         String contextPath;
         contextPath = parser.getWebletsContextPath();
         if (contextPath == null || contextPath.trim().equals("")) {
             try {
                 // lets check if we are in JEE 5 so that we can execute a
                 // getServletContextPath methid
                 Method[] methods = context.getClass().getMethods();
                 for (int cnt = 0; cnt < methods.length; cnt++) {
                     if (methods[cnt].getName().equals("getContextPath")) {
                         return (String) methods[cnt].invoke(context, new String[0]);
                     }
                 }
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(WebletsContextListenerImpl.class);
                 log.error("Error, trying to invoke getContextPath ", e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(WebletsContextListenerImpl.class);
                 log.error("Error, trying to invoke getContextPath ", e);
             }
         } else {
             return contextPath;
         }
         return "";
     }
 
     public static OutputStream getOutputStream(Object response) throws IOException {
         // return _httpResponse.getOutputStream();
         Method m = null;
         try {
             m = response.getClass().getMethod("getOutputStream", (Class[]) new Class[0]);
             try {
                 return (OutputStream) m.invoke(response, new Class[]{});
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             }
             return null;
         } catch (NoSuchMethodException e) {
             try {
                 // this should work because we are in a prerender stage but already
                 // have the response object
                 // this needs further testing of course!
                 m = response.getClass().getMethod("getPortletOutputStream", (Class[]) null);
                 try {
                     return (OutputStream) m.invoke(response, new Class[]{});
                 } catch (IllegalAccessException e1) {
                     Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                     log.error(e1);
                 } catch (InvocationTargetException e2) {
                     Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                     log.error(e2);
                 }
                 return null;
             } catch (NoSuchMethodException ex) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(ex);
             }
         }
         return null;
     }
 
     public static String getParameter(Object implementor, String name) {
         // return _httpResponse.getOutputStream();
         Method m = null;
         try {
             m = implementor.getClass().getMethod("getParameter", (Class[]) new Class[0]);
             try {
                 String[] params = new String[1];
                 params[0] = name;
                 return (String) m.invoke(implementor, params);
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             }
             return null;
         } catch (NoSuchMethodException e) {
             Log log = LogFactory.getLog(ReflectUtilsImpl.class);
             log.error(e);
         }
         return null;
     }
 
     public static Object getAttribute(Object implementor, String name) {
         // return _httpResponse.getOutputStream();
         Method m = null;
         try {
             m = implementor.getClass().getMethod("getAttribute", (Class[]) new Class[0]);
             try {
                 String[] params = new String[1];
                 params[0] = name;
                 return m.invoke(implementor, params);
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             }
             return null;
         } catch (NoSuchMethodException e) {
             Log log = LogFactory.getLog(ReflectUtilsImpl.class);
             log.error(e);
         }
         return null;
     }
 
     public static void setAttribute(Object implementor, String name, Object value) {
         // return _httpResponse.getOutputStream();
         Method m = null;
         try {
             m = implementor.getClass().getMethod("setAttribute", (Class[]) new Class[0]);
             try {
                 Object[] params = new Object[2];
                 params[0] = name;
                 params[1] = value;
                 m.invoke(implementor, params);
             } catch (IllegalAccessException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             } catch (InvocationTargetException e) {
                 Log log = LogFactory.getLog(ReflectUtilsImpl.class);
                 log.error(e);
             }
         } catch (NoSuchMethodException e) {
             Log log = LogFactory.getLog(ReflectUtilsImpl.class);
             log.error(e);
         }
     }
 }
