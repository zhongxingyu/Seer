 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.web;
 
 import java.io.File;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * A MIME type helper class.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Mime {
 
     /**
      * The MIME types commonly used for text files.
      */
     public static final String[] TEXT = {
         "text/plain"
     };
 
     /**
      * The MIME types commonly used for HTML files.
      */
     public static final String[] HTML = {
         "text/html",
         "text/xhtml",
         "application/xhtml",
         "application/xhtml+xml"
     };
 
     /**
      * The MIME types commonly used for CSS files.
      */
     public static final String[] CSS = {
         "text/css"
     };
 
     /**
      * The MIME types commonly used for JavaScript files.
      */
     public static final String[] JS = {
         "text/javascript",
         "text/x-javascript",
         "application/x-javascript"
     };
 
     /**
      * The MIME types commonly used for JSON files and data.
      */
     public static final String[] JSON = {
         "application/json",
         "application/x-javascript",
         "text/json",
         "text/x-json",
         "text/javascript",
         "text/x-javascript"
     };
 
     /**
      * The MIME types commonly used for XML files.
      */
     public static final String[] XML = {
         "text/xml",
         "application/xml"
     };
 
     /**
      * The MIME types commonly used for GIF images.
      */
     public static final String[] GIF = {
         "image/gif"
     };
 
     /**
      * The MIME types commonly used for JPEG images.
      */
     public static final String[] JPEG = {
         "image/jpeg"
     };
 
     /**
      * The MIME types commonly used for PNG images.
      */
     public static final String[] PNG = {
         "image/png"
     };
 
     /**
      * The MIME types commonly used for SVG images.
      */
     public static final String[] SVG = {
         "image/svg+xml"
     };
 
     /**
      * The MIME types commonly used for binary files and data.
      */
     public static final String[] BIN = {
         "application/octet-stream"
     };
 
     /**
      * The helper servlet context. This can be set by the application
      * to access the servlet container MIME type configuration.
      */
     public static ServletContext context = null;
 
     /**
      * Attempts to guess the MIME type for a file, based on the file
      * name (extension). This method will always return a valid MIME
      * type, defaulting to the binary MIME type if unknown.
      *
      * @param file           the file to check
      *
      * @return the file MIME type, or
      *         a binary MIME type if unknown
      */
     public static String type(File file) {
         return type(file.getName());
     }
 
     /**
      * Attempts to guess the MIME type for a file name (extension).
      * This method will always return a valid MIME type, defaulting
      * to the binary MIME type if unknown.
      *
      * @param fileName           the file name to check
      *
      * @return the file MIME type, or
      *         a binary MIME type if unknown
      */
     public static String type(String fileName) {
         fileName = fileName.toLowerCase();
         if (context != null) {
             String mime = context.getMimeType(fileName);
             if (mime != null && mime.trim().length() > 0) {
                 return mime;
             }
         }
         if (fileName.endsWith(".txt")) {
             return TEXT[0];
         } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
             return HTML[0];
         } else if (fileName.endsWith(".css")) {
             return CSS[0];
         } else if (fileName.endsWith(".js")) {
             return JS[0];
         } else if (fileName.endsWith(".json")) {
             return JSON[0];
         } else if (fileName.endsWith(".xml")) {
             return XML[0];
         } else if (fileName.endsWith(".gif")) {
             return GIF[0];
         } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
             return JPEG[0];
         } else if (fileName.endsWith(".png")) {
             return PNG[0];
         } else if (fileName.endsWith(".svg")) {
             return SVG[0];
         } else {
             return BIN[0];
         }
     }
 
     /**
      * Checks if a request contains an 'Accept' header with one of
      * the specified MIME types. Note that this method checks for any
      * match, without regard for any quality value in the header.
      *
      * @param request        the request to analyze
      * @param mimes          the MIME types to check for
      *
      * @return true if one of the MIME types is accepted, or
      *         false otherwise
      */
     public static boolean isMatch(Request request, String[] mimes) {
         String    header = request.getHeader("Accept");
        String[]  accept = header.split(",");
         String    mime;
 
        for (int i = 0; i < accept.length; i++) {
            mime = accept[i].split(";")[0];
            if (ArrayUtils.contains(mimes, mime)) {
                return true;
             }
         }
         return false;
     }
 }
