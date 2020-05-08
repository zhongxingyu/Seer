 /*
  * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.util;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 /**
  * Helper class for working with various datatypes.
  */
 public final class DatatypeHelper {
 
     /** JAXP DatatypeFactory. */
     private static DatatypeFactory dataTypeFactory;
 
     /** Constructor. */
     private DatatypeHelper() {
 
     }
 
     /**
      * Gets a static instance of a JAXP DatatypeFactory.
      * 
      * @return the factory or null if the factory could not be created
      */
     public static DatatypeFactory getDataTypeFactory() {
         if (dataTypeFactory == null) {
             try {
                 dataTypeFactory = DatatypeFactory.newInstance();
             } catch (DatatypeConfigurationException e) {
                 // do nothing
             }
         }
 
         return dataTypeFactory;
     }
 
     /**
      * A "safe" null/empty check for strings.
      * 
      * @param s The string to check
      * 
      * @return true if the string is null or the trimmed string is length zero
      */
     public static boolean isEmpty(String s) {
         if (s != null) {
             String sTrimmed = s.trim();
             if (sTrimmed.length() > 0) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Compares two strings for equality, allowing for nulls.
      * 
      * @param <T> type of object to compare
      * @param s1 The first operand
      * @param s2 The second operand
      * 
      * @return true if both are null or both are non-null and the same strng value
      */
     public static <T> boolean safeEquals(T s1, T s2) {
         if (s1 == null || s2 == null) {
             return s1 == s2;
         }
 
         return s1.equals(s2);
     }
 
     /**
      * A safe string trim that handles nulls.
      * 
      * @param s the string to trim
      * 
      * @return the trimmed string or null if the given string was null
      */
     public static String safeTrim(String s) {
         if (s != null) {
             return s.trim();
         }
 
         return null;
     }
 
     /**
      * Removes preceeding or proceeding whitespace from a string or return null if the string is null or of zero length
      * after trimming (i.e. if the string only contained whitespace).
      * 
      * @param s the string to trim
      * 
      * @return the trimmed string or null
      */
     public static String safeTrimOrNullString(String s) {
         if (s != null) {
             String sTrimmed = s.trim();
             if (sTrimmed.length() > 0) {
                 return sTrimmed;
             }
         }
 
         return null;
     }
     
     /**
      * Reads an input stream into a string.  The provide stream is <strong>not</strong> closed.
      * 
      * @param input the input stream to read
      * @param decoder character decoder to use, if null, system default character set is used
      * 
      * @return the string read from the stream
      * 
      * @throws IOException thrown if there is a problem reading from the stream and decoding it
      */
     public static String inputstreamToString(InputStream input, CharsetDecoder decoder) throws IOException{
         if(decoder == null){
            decoder = Charset.defaultCharset().newDecoder();
         }
         
        StringBuffer stringBuffer = new StringBuffer(1024);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, decoder));
                 
         char[] chars = new char[1024];
        int numRead = 0;
        while( (numRead = reader.read(chars)) > -1){
            stringBuffer.append(String.valueOf(chars));   
         }
 
         reader.close();
 
         return stringBuffer.toString();
     }
 }
