 /**
  * Copyright (C) 2009-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.googlecode.flyway.core.util;
 
 /**
  * Various string-related utilities.
  */
 public class StringUtils {
     /**
      * Prevents instantiation.
      */
     private StringUtils() {
         // Do nothing.
     }
 
     /**
      * Trims or pads (with spaces) this string, so it has this exact length.
      *
      * @param str The string to adjust.
      * @param length The exact length to reach.
      * @return The adjusted string.
      */
     public static String trimOrPad(String str, int length) {
         if (str.length() > length) {
            return str.substring(length);
         }
 
         String result = str;
         while (result.length() < length) {
             result += " ";
         }
         return result;
     }
 }
