 /*
  * Copyright (c) 2011-2013 Flite, Inc
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.flite.cach3.config;
 
 import java.io.*;
 import java.net.*;
 import java.text.*;
import org.apache.commons.codec.digest.DigestUtils;
 
 public class Slugifier {
 
     public static String slugify(final String input) {
         try {
             if (input == null || input.length() == 0) { return ""; }
             String toReturn = normalize(input);
             toReturn = toReturn.replace(" ", "-");
             toReturn = toReturn.toLowerCase();
             toReturn = URLEncoder.encode(toReturn, "UTF-8");
             return toReturn;
         } catch (UnsupportedEncodingException ex) {
             throw new IllegalStateException("Problem slugifying the string: " + input, ex);
         }
     }
 
    public static String hash(String input) {
        if (input == null || input.length() == 0) return "";
        return DigestUtils.md5Hex(input);
    }

     private static String normalize(String input) {
         if (input == null || input.length() == 0) return "";
         return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
     }
 }
