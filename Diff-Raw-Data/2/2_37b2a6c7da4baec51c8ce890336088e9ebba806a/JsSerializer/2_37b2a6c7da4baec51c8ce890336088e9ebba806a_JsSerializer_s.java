 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2009 Per Cederberg & Dynabyte AB.
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
 
 package org.rapidcontext.core.js;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.Undefined;
 import org.mozilla.javascript.WrappedException;
 import org.rapidcontext.core.data.Data;
 import org.rapidcontext.core.env.AdapterConnection;
 
 /**
 * An object serializer and unserializer for theJavaScript object
  * notation (JSON) format. This class also provides methods for
  * wrapping data objects for execution inside the JavaScript engine.
  * The data object mapping to JavaScript is not exact, and may omit
  * serialization of data in some cases. The following basic
  * requirements must be met in order to serialize a data object:<p>
  *
  * <ul>
  *   <li>No circular references are permitted.
  *   <li>String, Integer, Boolean and Data objects are supported.
  *   <li>Any Data object should be either an array or a map.
  * </ul>
  *
  * @author   Per Cederberg, Dynabyte AB
  * @version  1.0
  */
 public class JsSerializer {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(JsSerializer.class.getName());
 
     /**
      * Serializes an object into a JavaScript literal. I.e. the
      * string returned can be used as a constant inside JavaScript
      * code.
      *
      * @param obj            the object to convert, or null
      *
      * @return a JavaScript literal
      */
     public static String serialize(Object obj) {
         StringBuffer  buffer = new StringBuffer();
 
         serialize(unwrap(obj), buffer);
         return buffer.toString();
     }
 
     /**
      * Serializes an object into a JavaScript literal. I.e. the
      * serialized result can be used as a constant inside JavaScript
      * code. The serialized result will be written into the specified
      * string buffer.
      *
      * @param obj            the object to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Object obj, StringBuffer buffer) {
         if (obj == null) {
             buffer.append("null");
         } else if (obj instanceof Data) {
             serialize((Data) obj, buffer);
         } else if (obj instanceof Boolean) {
             buffer.append(obj.toString());
         } else if (obj instanceof Number) {
             serialize((Number) obj, buffer);
         } else {
             serialize(obj.toString(), buffer);
         }
     }
 
     /**
      * Serializes a data object into a JavaScript literal. I.e. the
      * serialized result can be used as a constant inside JavaScript
      * code. The serialized result will be written into the specified
      * string buffer.
      *
      * @param data           the data object to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Data data, StringBuffer buffer) {
         String[]  keys;
 
         if (data == null) {
             buffer.append("null");
         } else if (data.arraySize() >= 0) {
             buffer.append("[");
             for (int i = 0; i < data.arraySize(); i++) {
                 if (i > 0) {
                     buffer.append(", ");
                 }
                 serialize(data.get(i), buffer);
             }
             buffer.append("]");
         } else if (data.mapSize() <= 0) {
             buffer.append("{}");
         } else {
             keys = data.keys();
             buffer.append("{");
             for (int i = 0; i < keys.length; i++) {
                 if (i > 0) {
                     buffer.append(", ");
                 }
                 serialize(keys[i], buffer);
                 buffer.append(": ");
                 serialize(data.get(keys[i]), buffer);
             }
             buffer.append(" }");
         }
     }
 
     /**
      * Serializes a string into a JavaScript string literal. I.e.
      * the serialized result can be used as a string constant inside
      * JavaScript code. The serialized result will be written into
      * the specified string buffer.
      *
      * @param str            the string to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(String str, StringBuffer buffer) {
         if (str == null) {
             buffer.append("null");
         } else {
             buffer.append("'");
             for (int i = 0; i < str.length(); i++) {
                 switch (str.charAt(i)) {
                 case '\\':
                     buffer.append("\\\\");
                     break;
                 case '\'':
                     buffer.append("\\'");
                     break;
                 case '\"':
                     buffer.append("\\\"");
                     break;
                 case '\n':
                     buffer.append("\\n");
                     break;
                 case '\r':
                     buffer.append("\\r");
                     break;
                 case '\t':
                     buffer.append("\\t");
                     break;
                 default:
                     buffer.append(str.charAt(i));
                 }
             }
             buffer.append("'");
         }
     }
 
     /**
      * Serializes a number into a JavaScript number literal. I.e.
      * the serialized result can be used as a number constant inside
      * JavaScript code. The serialized result will be written into
      * the specified string buffer.
      *
      * @param num            the number to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Number num, StringBuffer buffer) {
         int     i = num.intValue();
         double  d = num.doubleValue();
 
         if (i == d) {
             buffer.append(i);
         } else {
             // TODO: proper number formatting should be used
             buffer.append(num);
         }
     }
 
     /**
      * Unserializes a JavaScript literal into a Java object. I.e.
      * this method converts a JSON object into the corresponding
      * String, Number, Boolean and/or Data objects.
      *
      * @param str            the string to convert, or null
      *
      * @return the corresponding Java object
      *
      * @throws JsException if the unserialization failed
      */
     public static Object unserialize(String str) throws JsException {
         Context  cx;
         Object   obj;
         String   msg;
 
         cx = ContextFactory.getGlobal().enterContext();
         try {
             str = "(" + str + ")";
             obj = cx.evaluateString(cx.initStandardObjects(),
                                     str,
                                     "unserialize",
                                     1,
                                     null);
             return unwrap(obj);
         } catch (WrappedException e) {
             msg = "Caught unserialization exception for text: " + str;
             LOG.log(Level.WARNING, msg, e);
             throw new JsException(msg, e.getWrappedException());
         } catch (Exception e) {
             msg = "Caught unserialization exception for text: " + str;
             LOG.log(Level.WARNING, msg, e);
             throw new JsException(msg, e);
         } finally {
             Context.exit();
         }
     }
 
     /**
      * Wraps a Java object for JavaScript access. This method only
      * handles String, Number, Boolean and Data instances.
      *
      * @param obj            the object to wrap
      * @param scope          the parent scope
      *
      * @return the wrapped object
      *
      * @see org.rapidcontext.core.data.Data
      */
     public static Object wrap(Object obj, Scriptable scope) {
         if (obj instanceof Data) {
             return new DataWrapper((Data) obj, scope);
         } else if (obj instanceof AdapterConnection) {
             return new ConnectionWrapper((AdapterConnection) obj, scope);
         } else {
             return obj;
         }
     }
 
     /**
      * Removes all JavaScript classes and replaces them with the
      * corresponding Java objects. This method will use instances of
      * Data replace native JavaScript objects and arrays. Also, it
      * will replace both JavaScript "null" and "undefined" with
      * null. Other objects will be returned as-is.
      *
      * @param obj            the object to unwrap
      *
      * @return the unwrapped object
      *
      * @see org.rapidcontext.core.data.Data
      */
     public static Object unwrap(Object obj) {
         Data         data;
         NativeArray  arr;
         Scriptable   scr;
         int          length;
         Object[]     keys;
         Object       value;
         String       str;
 
         if (obj instanceof DataWrapper) {
             return ((DataWrapper) obj).getData();
         } else if (obj instanceof ConnectionWrapper) {
             return ((ConnectionWrapper) obj).getConnection();
         } else if (obj instanceof NativeArray) {
             arr = (NativeArray) obj;
             length = (int) arr.getLength();
             data = new Data(length);
             for (int i = 0; i < length; i++) {
                 data.set(i, unwrap(arr.get(i, arr)));
             }
             return data;
         } else if (obj instanceof Scriptable) {
             scr = (Scriptable) obj;
             data = new Data();
             keys = scr.getIds();
             for (int i = 0; i < keys.length; i++) {
                 str = keys[i].toString();
                 if (keys[i] instanceof Integer) {
                     value = scr.get(((Integer) keys[i]).intValue(), scr);
                 } else {
                     value = scr.get(str, scr);
                 }
                 data.set(str, unwrap(value));
             }
             return data;
         } else if (obj instanceof Undefined || obj == Scriptable.NOT_FOUND) {
             return null;
         } else {
             return obj;
         }
     }
 }
