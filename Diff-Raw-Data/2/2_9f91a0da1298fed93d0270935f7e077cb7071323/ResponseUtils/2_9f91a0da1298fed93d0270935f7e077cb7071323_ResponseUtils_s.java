 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows.util;
 
 import java.io.IOException;
 import java.io.Writer;
 
 
 /**
  * Utility class performing operations related to http respones.
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class ResponseUtils {
     /**
      * Parses the passed string, and encodes the special characters (used in
      * xml for special purposes) with the appropriate codes. e.g. '<' is
      * changed to '&lt;'
      *
      * @param inData The string to encode into xml.
      *
      * @return the encoded string. Returns null, if null is passed as argument
      *
      */
     public static String encodeXML(String inData) {
         //return null, if null is passed as argument
         if (inData == null) {
             return null;
         }
 
         //if no special characters, just return
         //(for optimization. Though may be an overhead, but for most of the
         //strings, this will save time)
         if ((inData.indexOf('&') == -1) && (inData.indexOf('<') == -1)
                 && (inData.indexOf('>') == -1) && (inData.indexOf('\'') == -1)
                 && (inData.indexOf('\"') == -1)) {
             return inData;
         }
 
         //get the length of input String
         int length = inData.length();
 
         //create a StringBuffer of double the size (size is just for guidance
         //so as to reduce increase-capacity operations. The actual size of
         //the resulting string may be even greater than we specified, but is
         //extremely rare)
         StringBuffer buffer = new StringBuffer(2 * length);
 
         char charToCompare;
 
         //iterate over the input String
         for (int i = 0; i < length; i++) {
             charToCompare = inData.charAt(i);
 
             //if the ith character is special character, replace by code
             if (charToCompare == '&') {
                 buffer.append("&amp;");
             } else if (charToCompare == '<') {
                 buffer.append("&lt;");
             } else if (charToCompare == '>') {
                 buffer.append("&gt;");
             } else if (charToCompare == '\"') {
                 buffer.append("&quot;");
             } else if (charToCompare == '\'') {
                 buffer.append("&apos;");
             } else {
                 buffer.append(charToCompare);
             }
         }
 
         //return the encoded string
         return buffer.toString();
     }
 
     /**
      * Writes <CODE>string</CODE> into writer, escaping &, ', ", <, and >
      * with the XML excape strings.
      */
     public static void writeEscapedString(Writer writer, String string)
         throws IOException {
         for (int i = 0; i < string.length(); i++) {
             char c = string.charAt(i);
 
             if (c == '<') {
                 writer.write("&lt;");
             } else if (c == '>') {
                 writer.write("&gt;");
             } else if (c == '&') {
                 writer.write("&amp;");
             } else if (c == '\'') {
                 writer.write("&apos;");
             } else if (c == '"') {
                 writer.write("&quot;");
             } else {
                 writer.write(c);
             }
         }
     }
 
     /**
      * Appends a query string to a url.
      * <p>
      * This method checks <code>url</code> to see if the appended query string requires a '?' or
      * '&' to be prepended.
      * </p>
      *
      * @param url The base url.
      * @param queryString The query string to be appended, should not contain the '?' character.
      *
      * @return A full url with the query string appended.
      * 
      * TODO: remove this and replace with Requetss.appendQueryString
      */
     public static String appendQueryString(String url, String queryString) {
         if (url.endsWith("?") || url.endsWith("&")) {
             return url + queryString;
         }
 
         if (url.indexOf('?') != -1) {
             return url + "&" + queryString;
         }
 
         return url + "?" + queryString;
     }
 
     /**
      * Strips the query string off a request url.
      *
      * @param url The url.
      *
      * @return The original minus the query string.
      */
     public static String stripQueryString(String url) {
         int index = url.indexOf('?');
 
         if (index == -1) {
             return url;
         }
 
         return url.substring(0, index);
     }
     
     /**
      * Returns the query string part of a request url.
      * <p>
      * If the url does not have a query string compopnent, the empty string is 
      * returned. 
      * </p>
      * 
      * @param url The url.
      * 
      * @return The query string part of the url.
      */
     public static String getQueryString(String url) {
         int index = url.indexOf('?');
 
         if (index == -1 || index == url.length()-1 ) {
             return "";
         }
 
         return url.substring(index+1);
     }
 
     /**
      * Appends a path tpo a url.
      * <p>
      * This method checks <code>url</code> to see if the appended path requires a '/' to be
      * prepended.
      * </p>
      * @param url The base url.
      * @param path The path to be appended to the url.
      *
      * @return The full url with the path appended.
      * TODO: remove this and replace with Requetss.appendContextPath
      */
     public static String appendPath(String url, String path) {
         if (path.startsWith("/")) {
             path = path.substring(1);
         }
 
         return url.endsWith("/") ? (url + path) : (url + "/" + path);
     }
     
     /**
      * Strips any remaining part from a path, returning only the first component.
      * <p>
      * Examples: 
      * <ul>
      *   <li>foo/bar -> foo
      *   <li>/foo/bar -> /foo
      * </ul>
      * </p>
      * @param url
      * @return
      */
     public static String stripRemainingPath(String path) {
         int i = 0;
         if  (path.startsWith("/")) {
             i = 1;
         }
         
         int index = path.indexOf('/',i);
        if ( i > -1 ) {
             return path.substring( 0, index );
         }
         return path;
     }
     
     /**
      * Ensures a path is absolute (starting with '/').
      * 
      * @param path The path.
      * 
      * @return The path starting with '/'.
      */
     public static String makePathAbsolute(String path) {
         if ( path.startsWith("/") ) {
             return path;
         }
         
         return "/" + path;
     }
 }
