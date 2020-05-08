 /* 
  * Copyright 2012 Matthias Huebner
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.apache.commons.lang;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Emulation of org.apache.commons.lang.StringUtils
  * 
  * @author Matthias Hübner
  *
  */
 public class StringUtils {
 
   public static final String EMPTY = "";
 
   public static final int INDEX_NOT_FOUND = -1;
 
   private static final int PAD_LIMIT = 8192;
 
   public static boolean isEmpty(String str) {
     return str == null || str.length() == 0;
   }
 
   public static boolean isNotEmpty(String str) {
     return !StringUtils.isEmpty(str);
   }
 
   public static boolean isBlank(String str) {
     int strLen;
     if (str == null || (strLen = str.length()) == 0) {
       return true;
     }
     for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == false)) {
         return false;
       }
     }
     return true;
   }
 
   public static boolean isNotBlank(String str) {
     return !StringUtils.isBlank(str);
   }
 
   public static String trim(String str) {
     return str == null ? null : str.trim();
   }
 
   public static String trimToNull(String str) {
     String ts = trim(str);
     return isEmpty(ts) ? null : ts;
   }
 
   public static String trimToEmpty(String str) {
     return str == null ? EMPTY : str.trim();
   }
 
   public static String strip(String str) {
     return strip(str, null);
   }
 
   public static String stripToNull(String str) {
     if (str == null) {
       return null;
     }
     str = strip(str, null);
     return str.length() == 0 ? null : str;
   }
 
   public static String stripToEmpty(String str) {
     return str == null ? EMPTY : strip(str, null);
   }
 
   public static String strip(String str, String stripChars) {
     if (isEmpty(str)) {
       return str;
     }
     str = stripStart(str, stripChars);
     return stripEnd(str, stripChars);
   }
 
   public static String stripStart(String str, String stripChars) {
     int strLen;
     if (str == null || (strLen = str.length()) == 0) {
       return str;
     }
     int start = 0;
     if (stripChars == null) {
      while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
         start++;
       }
     }
     else if (stripChars.length() == 0) {
       return str;
     }
     else {
       while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND)) {
         start++;
       }
     }
     return str.substring(start);
   }
 
   public static String stripEnd(String str, String stripChars) {
     int end;
     if (str == null || (end = str.length()) == 0) {
       return str;
     }
 
     if (stripChars == null) {
      while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
         end--;
       }
     }
     else if (stripChars.length() == 0) {
       return str;
     }
     else {
       while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND)) {
         end--;
       }
     }
     return str.substring(0, end);
   }
 
   public static String[] stripAll(String[] strs) {
     return stripAll(strs, null);
   }
 
   public static String[] stripAll(String[] strs, String stripChars) {
     int strsLen;
     if (strs == null || (strsLen = strs.length) == 0) {
       return strs;
     }
     String[] newArr = new String[strsLen];
     for (int i = 0; i < strsLen; i++) {
       newArr[i] = strip(strs[i], stripChars);
     }
     return newArr;
   }
 
   public static boolean equals(String str1, String str2) {
     return str1 == null ? str2 == null : str1.equals(str2);
   }
 
   public static boolean equalsIgnoreCase(String str1, String str2) {
     return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
   }
 
   public static int indexOf(String str, char searchChar) {
     if (isEmpty(str)) {
       return INDEX_NOT_FOUND;
     }
     return str.indexOf(searchChar);
   }
 
   public static int indexOf(String str, char searchChar, int startPos) {
     if (isEmpty(str)) {
       return INDEX_NOT_FOUND;
     }
     return str.indexOf(searchChar, startPos);
   }
 
   public static int indexOf(String str, String searchStr) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     return str.indexOf(searchStr);
   }
 
   public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
     return ordinalIndexOf(str, searchStr, ordinal, false);
   }
 
   private static int ordinalIndexOf(String str, String searchStr, int ordinal, boolean lastIndex) {
     if (str == null || searchStr == null || ordinal <= 0) {
       return INDEX_NOT_FOUND;
     }
     if (searchStr.length() == 0) {
       return lastIndex ? str.length() : 0;
     }
     int found = 0;
     int index = lastIndex ? str.length() : INDEX_NOT_FOUND;
     do {
       if (lastIndex) {
         index = str.lastIndexOf(searchStr, index - 1);
       }
       else {
         index = str.indexOf(searchStr, index + 1);
       }
       if (index < 0) {
         return index;
       }
       found++;
     } while (found < ordinal);
     return index;
   }
 
   public static int indexOf(String str, String searchStr, int startPos) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     // JDK1.2/JDK1.3 have a bug, when startPos > str.length for "", hence
     if (searchStr.length() == 0 && startPos >= str.length()) {
       return str.length();
     }
     return str.indexOf(searchStr, startPos);
   }
 
   public static int indexOfIgnoreCase(String str, String searchStr) {
     return indexOfIgnoreCase(str, searchStr, 0);
   }
 
   public static int indexOfIgnoreCase(String str, String searchStr, int startPos) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     if (startPos < 0) {
       startPos = 0;
     }
     int endLimit = (str.length() - searchStr.length()) + 1;
     if (startPos > endLimit) {
       return INDEX_NOT_FOUND;
     }
     if (searchStr.length() == 0) {
       return startPos;
     }
     for (int i = startPos; i < endLimit; i++) {
       if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
         return i;
       }
     }
     return INDEX_NOT_FOUND;
   }
 
   public static int lastIndexOf(String str, char searchChar) {
     if (isEmpty(str)) {
       return INDEX_NOT_FOUND;
     }
     return str.lastIndexOf(searchChar);
   }
 
   public static int lastIndexOf(String str, char searchChar, int startPos) {
     if (isEmpty(str)) {
       return INDEX_NOT_FOUND;
     }
     return str.lastIndexOf(searchChar, startPos);
   }
 
   public static int lastIndexOf(String str, String searchStr) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     return str.lastIndexOf(searchStr);
   }
 
   public static int lastOrdinalIndexOf(String str, String searchStr, int ordinal) {
     return ordinalIndexOf(str, searchStr, ordinal, true);
   }
 
   public static int lastIndexOf(String str, String searchStr, int startPos) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     return str.lastIndexOf(searchStr, startPos);
   }
 
   public static int lastIndexOfIgnoreCase(String str, String searchStr) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     return lastIndexOfIgnoreCase(str, searchStr, str.length());
   }
 
   public static int lastIndexOfIgnoreCase(String str, String searchStr, int startPos) {
     if (str == null || searchStr == null) {
       return INDEX_NOT_FOUND;
     }
     if (startPos > (str.length() - searchStr.length())) {
       startPos = str.length() - searchStr.length();
     }
     if (startPos < 0) {
       return INDEX_NOT_FOUND;
     }
     if (searchStr.length() == 0) {
       return startPos;
     }
 
     for (int i = startPos; i >= 0; i--) {
       if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
         return i;
       }
     }
     return INDEX_NOT_FOUND;
   }
 
   public static boolean contains(String str, char searchChar) {
     if (isEmpty(str)) {
       return false;
     }
     return str.indexOf(searchChar) >= 0;
   }
 
   public static boolean contains(String str, String searchStr) {
     if (str == null || searchStr == null) {
       return false;
     }
     return str.indexOf(searchStr) >= 0;
   }
 

  public static String join(Object[] items, String separator) {
    return join(Arrays.asList(items,separator));
  }
  
  public static String join(Collection<?> items, String separator) {
    StringBuilder buff = new StringBuilder();
    while (items.hasNext()) {
      String next = items.next();
      if (next != null)
        buff.append(next);
      if (items.hasNext())
        buff.append(separator);
    }
    return buff.toString();
   }
 }
