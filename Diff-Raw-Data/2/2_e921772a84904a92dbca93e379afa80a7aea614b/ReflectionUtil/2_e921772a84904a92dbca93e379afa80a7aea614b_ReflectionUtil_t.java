 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sun.facelets.compiler;
 
 import java.lang.reflect.Array;
 import java.util.Arrays;
 
 /**
  * Utilities for Managing Serialization and Reflection
  * 
  * @author Jacob Hookom [jacob@hookom.net]
  * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: jhook $
  */
 class ReflectionUtil {
 
     protected static final String[] EMPTY_STRING = new String[0];
 
     protected static final String[] PRIMITIVE_NAMES = new String[] { "boolean",
             "byte", "char", "double", "float", "int", "long", "short", "void" };
 
     protected static final Class[] PRIMITIVES = new Class[] { boolean.class,
             byte.class, char.class, double.class, float.class, int.class,
             long.class, short.class, Void.TYPE };
 
     /**
      * 
      */
     private ReflectionUtil() {
         super();
     }
 
     public static Class forName(String name) throws ClassNotFoundException {
         if (null == name || "".equals(name)) {
             return null;
         }
         Class c = forNamePrimitive(name);
         if (c == null) {
             if (name.endsWith("[]")) {
                 String nc = name.substring(0, name.length() - 2);
                 c = Class.forName(nc, true, Thread.currentThread().getContextClassLoader());
                 c = Array.newInstance(c, 0).getClass();
             } else {
                c = Class.forName(name, true, Thread.currentThread().getContextClassLoader());
             }
         }
         return c;
     }
 
     protected static Class forNamePrimitive(String name) {
         if (name.length() <= 8) {
             int p = Arrays.binarySearch(PRIMITIVE_NAMES, name);
             if (p >= 0) {
                 return PRIMITIVES[p];
             }
         }
         return null;
     }
 }
