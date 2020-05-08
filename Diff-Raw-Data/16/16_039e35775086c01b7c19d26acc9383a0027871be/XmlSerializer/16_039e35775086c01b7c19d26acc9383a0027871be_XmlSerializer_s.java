 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg.
  * All rights reserved.
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
 
 package org.rapidcontext.core.data;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * A data serializer for XML. This class currently only attempts to
  * render a machine-readable version of a data object, without any
  * efforts of parsing input data. The following basic requirements
  * must be met in order to serialize an object:<p>
  *
  * <ul>
  *   <li>No circular references are permitted.
  *   <li>String, Integer, Boolean, Array and Dict objects are supported.
  *   <li>Key names must only consist of valid XML tag characters.
  * </ul>
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class XmlSerializer {
 
     /**
      * Serializes an object into an XML representation. The string
      * returned can be used (without escaping) inside an XML document.
      *
      * @param obj            the object to convert, or null
      *
      * @return an XML representation
      */
     public static String serialize(Object obj) {
         StringBuilder  buffer = new StringBuilder();
 
         serialize(obj, buffer);
         return buffer.toString();
     }
 
     /**
      * Serializes an object into an XML representation.
      *
      * @param obj            the object to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Object obj, StringBuilder buffer) {
         if (obj == null) {
             buffer.append("<null/>");
         } else if (obj instanceof Dict) {
             serialize((Dict) obj, buffer);
         } else if (obj instanceof Array) {
             serialize((Array) obj, buffer);
         } else {
             serialize(obj.toString(), buffer);
         }
     }
 
     /**
      * Serializes a dictionary into an XML representation.
      *
      * @param dict           the dictionary to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Dict dict, StringBuilder buffer) {
         String[]  keys = dict.keys();
 
         buffer.append("<object>");
         for (int i = 0; i < keys.length; i++) {
             buffer.append("<");
             buffer.append(keys[i]);
             buffer.append(">");
             serialize(dict.get(keys[i]), buffer);
             buffer.append("</");
             buffer.append(keys[i]);
             buffer.append(">");
         }
         buffer.append("</object>");
     }
 
     /**
      * Serializes an array into an XML representation.
      *
      * @param arr            the array to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Array arr, StringBuilder buffer) {
         buffer.append("<array>");
         for (int i = 0; i < arr.size(); i++) {
             buffer.append("<item>");
             serialize(arr.get(i), buffer);
             buffer.append("</item>");
         }
         buffer.append("</array>");
     }
 
     /**
      * Serializes a text string into an HTML representation. If the
      * string contains a newline character, it will be wrapped in a
      * pre-tag. Otherwise it will only be properly HTML escaped. This
      * method also makes some rudimentary efforts to detect HTTP
      * links.
      *
      * @param str            the text string to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(String str, StringBuilder buffer) {
         if (str == null) {
             buffer.append("<null/>");
         } else if (str.contains("\n")) {
             buffer.append("<![CDATA[");
             buffer.append(str);
             buffer.append("]]>");
         } else {
             buffer.append(StringEscapeUtils.escapeXml(str));
         }
     }
 }
