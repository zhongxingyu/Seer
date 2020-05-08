 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.client;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Admin Web Client Utilities.
  */
 public class Utils extends AdminClientConstants {
 
     /**
      * Attempts to get the value from a request paramter having the key given. If the request
      * parameter exists for the given key, and a non-null value is associated with the key, the
      * method attempts to transfrom the retured value into a integer. If the operation succeeds
      * the value is added to the session, if not the value prevelant in the session is used. If
      * no value is on session as well, the default value will be saved to session. This method
      * returs what ever value that will be at present stored in session.
      *
      * @param session      Associated HTTP Session object
      * @param request      HTTP Servlet Request
      * @param key          Key of parameter
      * @param defaultValue Default value of parameter
      * @return             Integer value corresponding to request parameter with given key or
      *                     the value stored in session under the given key.
      */
     public static int getValueFromRequestParam(HttpSession session, HttpServletRequest request,
         String key, int defaultValue) {
         int response = 0;
         String reqParam = null;
         reqParam = request.getParameter(key);
         if (reqParam != null) {
             try {
                 response = Integer.parseInt(reqParam);
                 if (response < 0) {
                     response = 0;
                 }
                 session.setAttribute(key, response);
                 return response;
             } catch (NumberFormatException e) {
                 // Simply ignore the exception and carry on.
             }
         }
         Object attr = null;
         attr = session.getAttribute(key);
         if (attr != null) {
             if (attr instanceof Integer) {
                 response = (Integer)attr;
             } else {
                 response = Integer.parseInt((String)attr);
             }
         } else {
             response = defaultValue;
             if (response < 0) {
                 response = 0;
             }
             session.setAttribute(key, response);
         }
         return response;
     }
 
     /**
      * Attempts to get the value from a request paramter having the key given. If the request
      * parameter exists for the given key, and a non-null value is associated with the key, the
      * method will return it. If the operation succeeds the value is added to the session, if 
      * not the value prevelant in the session is used. If no value is on session as well, the 
      * default value will be saved to session. This method returs what ever value that will be 
     * at present stored in session. This is similar to
     * {@link #getValueFromRequestParam(HttpSession, HttpServletRequest, String, int)} in the
      * way in which it works, except that it operates on string values instead of integers.
      *
      * @param session      Associated HTTP Session object
      * @param request      HTTP Servlet Request
      * @param key          Key of parameter
      * @param defaultValue Default value of parameter
      * @return             String value corresponding to request parameter with given key or
      *                     the value stored in session under the given key.
      */
     public static String getValueStringFromRequestParam(HttpSession session, HttpServletRequest request,
         String key, String defaultValue) {
         String response = null;
         response = request.getParameter(key);
         if (response != null && !response.equals("")) {
             session.setAttribute(key, response);
             return response;
         } else if (defaultValue != null && !defaultValue.equals("")) {
             response = defaultValue;
         } else {
             response = null;
         }
         Object attr = null;
         attr = session.getAttribute(key);
         if (attr != null && !((String)attr).equals("")) {
             return (String)attr;
         } else if (response != null) {
             session.setAttribute(key, response);
         }
         return defaultValue;
     }
 
     /**
      * Attempts to get the value from a request paramter having the key given. If the request
      * parameter exists for the given key, and a non-null value is associated with the key, the
      * method will return it. If the operation succeeds the value is added to the session, if 
      * not the value prevelant in the session is used. If no value is on session as well, the 
      * default value will be saved to session. This method returs what ever value that will be 
      * at present stored in session. This is similar to 
      * {@link #getValueFromRequestParam(HttpSession, HttpServletRequest, String, int)} in the
      * way in which it works, except that it operates on boolean values instead of integers.
      *
      * @param session      Associated HTTP Session object
      * @param request      HTTP Servlet Request
      * @param key          Key of parameter
      * @param defaultValue Default value of parameter
      * @return             boolean value corresponding to request parameter with given key or
      *                     the value stored in session under the given key.
      */
     public static boolean getValueFromRequestParam(HttpSession session, HttpServletRequest request,
         String key, boolean defaultValue) {
         boolean response = false;
         String reqParam = null;
         reqParam = request.getParameter(key);
         if (reqParam != null) {
             response = Boolean.parseBoolean(reqParam);
             session.setAttribute(key, response);
             return response;
         }
         Object attr = null;
         attr = session.getAttribute(key);
         if (attr != null) {
             if (attr instanceof Boolean) {
                 response = (Boolean)attr;
             } else {
                 response = Boolean.parseBoolean((String)attr);
             }
         } else {
             response = defaultValue;
             session.setAttribute(key, response);
         }
         return response;
     }
 
     /**
      * Shortens the given string to the specified length
      * and adds three dots at the end.
      *
      * @param length must be greater than 3
      * @param input  input string
      * @return       short string
      */
     public static String shorten(int length, String input) {
         if (input.length() <= length) {
             return input;
         } else if (length < 3) {
             return null;
         } else if (length == 3) {
             return "...";
         }
         return input.substring(0, length - 3) + "...";
     }
 
     /**
      * Shortens the given string to a default length and adds 
      * three dots at the end. The default length is given by
      * {@link AdminClientConstants#DEFAULT_SHORT_STRING_LENGTH}.
      *
      * @param input  input string
      * @return       short string
      */
     public static String shorten(String input) {
         return shorten(AdminClientConstants.DEFAULT_SHORT_STRING_LENGTH, input);
     }
 
     /**
      * Method to get the URL of the underlying server.
      *
      * @param context The servlet context to be used
      * @param session Associated HTTP session object
      * @return        The URL of the server.
      */
     public static String getServerURL(ServletContext context, HttpSession session){
         Object object = session.getAttribute(AdminClientConstants.SERVER_URL);
         if (object != null && object instanceof String ){
             // Server URL is present in the servlet session
             return (String)object;
         }
         // if not on servlet session try the servlet context
         return (String) context.getAttribute(AdminClientConstants.SERVER_URL);
     }
 }
