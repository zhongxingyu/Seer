 /*
  * Copyright 2010 CodeGist.org
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *
  * ===================================================================
  *
  * More information at http://www.codegist.org.
  */
 
 package org.codegist.common.lang;
 
 /**
  * @author Laurent Gilles (laurent.gilles@codegist.org)
  */
 public final class Strings {
     private Strings() {
         throw new IllegalStateException();
     }
 
     /**
      * Checks if the given string is null or blank by trimming it and checking that the length it not 0
      *
      * @param str String to check
      * @return return if the string is blank
      */
     public static boolean isBlank(String str) {
         return str == null || str.trim().length() == 0;
     }
 
     public static boolean isEmpty(String str) {
         return str == null || str.length() == 0;
     }
 
 
     /**
      * Checks if the given string is not null and not blank
      *
      * @param str String to check
      * @return return if the string is blank
      * @see Strings#isBlank(String)
      */
     public static boolean isNotBlank(String str) {
         return !isBlank(str);
     }
     public static boolean isNotEmpty(String str) {
         return !isEmpty(str);
     }
 
     /**
      * Returns either str if not blank otherwise def
      *
      * @param str String to check
      * @param def Default to return if str is blank
      * @return either str or def
      */
     public static String defaultIfBlank(String str, String def) {
         return isBlank(str) ? def : str;
     }
 
     public static String defaultIfEmpty(String str, String def) {
         return isEmpty(str) ? def : str;
     }
 
 }
