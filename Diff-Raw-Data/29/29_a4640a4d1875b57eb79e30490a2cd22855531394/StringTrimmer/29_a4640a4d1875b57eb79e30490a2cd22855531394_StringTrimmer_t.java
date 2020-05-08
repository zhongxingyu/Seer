 /**
  * Copyright 2010 David L. Whitehurst
  * 
  * Licensed under the Apache License, Version 2.0 
  * (the "License"); You may not use this file except 
  * in compliance with the License. You may obtain a 
  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific 
  * language governing permissions and limitations under the 
  * License.
  * 
  */
 package org.dlw.ai.blackboard.util;
 
 /**
  * This class is used to provide string utility functions for trimming String
  * objects.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0-RC
  */
 public final class StringTrimmer {
 
     /**
      * Hidden constructor
      */
     private StringTrimmer() {
     }
 
     /**
      * Public method to remove leading whitespace
      * 
      * @param source
      *            the source String object to trim
     * @return left trimmed String
      */
     public static String ltrim(String source) {
         return source.replaceAll("^\\s+", "");
     }
 
     /**
      * Public method to remove trailing whitespace
      * 
      * @param source
      *            the source String object to trim
     * @return right trimmed String
      */
     public static String rtrim(String source) {
         return source.replaceAll("\\s+$", "");
     }
 
     /**
      * Public method to replace multiple whitespaces between words with a single
      * whitespace or blank
      * 
      * @param source
      *            the source String object to revise
     * @return space defragged String
      */
     public static String itrim(String source) {
         return source.replaceAll("\\b\\s{2,}\\b", " ");
     }
 
     /* remove all superfluous whitespaces in source string */
     /**
      * Public method to trim whitespace on ends and to fix any multiple
      * whitespace issues within the string
      */
     public static String trim(String source) {
         return itrim(ltrim(rtrim(source)));
     }
 
     /**
      * Public method to trim whitespace from ends only
      * 
      * @param source
     * @return left and right trimmed String
      */
     public static String lrtrim(String source) {
         return ltrim(rtrim(source));
     }
 
 }
