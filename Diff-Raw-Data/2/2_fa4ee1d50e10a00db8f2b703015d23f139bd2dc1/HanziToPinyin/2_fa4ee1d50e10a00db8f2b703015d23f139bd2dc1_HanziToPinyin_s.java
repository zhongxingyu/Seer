 /*
  * Copyright (C) 2014 The MoKee OpenSource Project
  *
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
 
 package com.android.dialer.util;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.chinese.pinyin.PinyinHelper;
 import org.chinese.pinyin.format.HanyuPinyinCaseType;
 import org.chinese.pinyin.format.HanyuPinyinOutputFormat;
 import org.chinese.pinyin.format.HanyuPinyinToneType;
 import org.chinese.pinyin.format.HanyuPinyinVCharType;
 
 /**
  * An object to convert Chinese character to its corresponding pinyin string.
  * For characters with multiple possible pinyin string, only one is selected
  * according to collator. Polyphone is not supported in this implementation.
  * This class is implemented to achieve the best runtime performance and minimum
  * runtime resources with tolerable sacrifice of accuracy. This implementation
  * highly depends on zh_CN ICU collation data and must be always synchronized
  * with ICU. Currently this file is aligned to zh.txt in ICU 4.6
  */
 public class HanziToPinyin {
     public static Set<String> getPinyin(String src) {
         if (src != null && !src.trim().equalsIgnoreCase("")) {
             char[] srcChar;
             srcChar = src.toCharArray();
             // 汉语拼音格式输出类
             HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
             hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
             hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
             hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
             String[][] firstTemp = new String[src.length()][];
             String[][] pinyinTemp = new String[src.length()][];
             for (int i = 0; i < srcChar.length; i++) {
                 char c = srcChar[i];
                 if (PinyinHelper.matchesCheck(c)) {
                     try {
                         pinyinTemp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i],
                                 hanYuPinOutputFormat);
                         if (pinyinTemp[i] != null) {
                             String[] temp = new String[pinyinTemp[i].length];
                             for (int j = 0; j < pinyinTemp[i].length; j++) {
                                 temp[j] = String.valueOf((pinyinTemp[i])[j].charAt(0));
                             }
                             firstTemp[i] = temp;
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 } else {
                     String str = String.valueOf(srcChar[i]).toLowerCase();
                     pinyinTemp[i] = new String[] {
                         str
                     };
                     firstTemp[i] = new String[] {
                         str
                     };
                 }
             }
             String[] pingyinArray = Exchange(pinyinTemp);
             String[] firstArray = Exchange(firstTemp);
             Set<String> pinyinSet = new HashSet<String>();
             for (int i = 0; i < pingyinArray.length; i++) {
                 pinyinSet.add(pingyinArray[i]);
             }
             for (int i = 0; i < firstArray.length; i++) {
                 pinyinSet.add(firstArray[i]);
             }
             return pinyinSet;
         }
         return null;
     }
 
     public static String[] Exchange(String[][] strJaggedArray) {
         String[][] temp = DoExchange(strJaggedArray);
         return temp[0];
     }
 
     private static String[][] DoExchange(String[][] strJaggedArray) {
         int len = strJaggedArray.length;
        if (len >= 2) {
             int len1 = strJaggedArray[0].length;
             int len2 = strJaggedArray[1].length;
             int newlen = len1 * len2;
             String[] temp = new String[newlen];
             int Index = 0;
             for (int i = 0; i < len1; i++) {
                 for (int j = 0; j < len2; j++) {
                     temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
                     Index++;
                 }
             }
             String[][] newArray = new String[len - 1][];
             for (int i = 2; i < len; i++) {
                 newArray[i - 1] = strJaggedArray[i];
             }
             newArray[0] = temp;
             return DoExchange(newArray);
         } else {
             return strJaggedArray;
         }
     }
 }
