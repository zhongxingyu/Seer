 package org.otherobjects.cms.util;
 
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.springframework.util.Assert;
 
 /**
  * Utility methods for working with request data.
  * 
  * @author rich
  */
 public class RequestUtils
 {
     private static final String XHR_REQUEST_HEADER_KEY = "XMLHttpRequest";
     private static final String XHR_REQUEST_HEADER_VALUE = "X-Requested-With";
 
     /**
      * Returns the last part of the path info (after the last slash).
      * 
      * @param request
      * @return
      */
     public static String getId(HttpServletRequest request)
     {
         String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.contains("/"))
            return null;
         return pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
     }
 
     public static String getId(HttpServletRequest request, String defaultValue)
     {
         String id = getId(request);
         return id != null ? id : defaultValue;
     }
 
     public static long getLong(HttpServletRequest request, String paramName)
     {
         String parameter = request.getParameter(paramName);
         Assert.notNull(parameter, "No parameter found called: " + paramName);
         return Long.parseLong(parameter);
     }

     public static long getLong(HttpServletRequest request, String paramName, long defaultValue)
     {
         String parameter = request.getParameter(paramName);
         if (parameter == null)
             return defaultValue;
         else
             return Long.parseLong(parameter);
     }

     public static int getInt(HttpServletRequest request, String paramName)
     {
         String parameter = request.getParameter(paramName);
         Assert.notNull(parameter, "No parameter found called: " + paramName);
         return Integer.parseInt(parameter);
     }
 
     public static int getInt(HttpServletRequest request, String paramName, int defaultValue)
     {
         String parameter = request.getParameter(paramName);
         if (parameter == null)
             return defaultValue;
         else
             return Integer.parseInt(parameter);
     }
 
     /**
      * Returns true if this request is generated from XHR.
      * 
      * @param request
      * @return
      */
     public static boolean isXhr(HttpServletRequest request)
     {
         String requestedWith = request.getHeader(XHR_REQUEST_HEADER_VALUE);
         return StringUtils.equals(requestedWith, XHR_REQUEST_HEADER_KEY);
     }
 
     /**
      * Logs query string and all paramters from the request to the provider logger.
      * 
      * <p>Note: be careful using this with controllers that handle sensitive data,
      * that should not end up in log files (passwords, credit card details etc).
      * 
      * @param log
      * @param request
      */
     @SuppressWarnings("unchecked")
     public static void logParameters(Logger log, HttpServletRequest request)
     {
         // Debug parameters
         Enumeration parameterNames = request.getParameterNames();
         log.debug("Query String: ", request.getQueryString());
         while (parameterNames.hasMoreElements())
         {
             String name = (String) parameterNames.nextElement();
             log.error("Parameter: {} = {}", name, request.getParameter(name));
         }
     }
 
     //    /**
     //     * Returns the last part of the path info after the method string.
     //     * 
     //     * @param method
     //     * @param request
     //     * @return
     //     */
     //    public static String getId(String method, HttpServletRequest request)
     //    {
     //        String pathInfo = request.getPathInfo();
     //        int startPos = pathInfo.indexOf("/" + method + "/") + method.length() + 2;
     //        return pathInfo.substring(startPos);
     //    }
 
 }
