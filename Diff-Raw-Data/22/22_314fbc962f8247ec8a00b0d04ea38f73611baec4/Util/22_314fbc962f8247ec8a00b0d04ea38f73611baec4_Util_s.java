 /*
  * Authored By Julian Chu <walkingice@0xlab.org>
  *
  * Copyright (c) 2012 0xlab.org - http://0xlab.org/
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
 
 package org.zeroxlab.momome.util;
 
 import org.zeroxlab.momome.Momo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 
 public class Util implements Momo {
 
     private final static String ENCODE = "UTF-8";
 
     public static int randomInt(int range) {
         return (int)(Math.random() * range);
     }
 
     public static int randomInt() {
         return randomInt(10000000);
     }
 
     public static boolean isFileAccessible(String path) {
         File file = new File(path);
 
         if (file.isDirectory()) {
             return false;
         }
 
         if (!file.exists()) {
             return false;
         }
 
         return true;
     }
 
     public static void writeStrToFile(String path, CharSequence data) throws IOException {
         File file = new File(path);
         FileOutputStream fos = new FileOutputStream(file);
         writeStrToStream(fos, data);
     }
 
     public static CharSequence readStrFromFile(String path) throws IOException {
         File file = new File(path);
         FileInputStream fis = new FileInputStream(file);
         return readStrFromStream(fis);
     }
 
     public static CharSequence readStrFromStream(InputStream str) throws IOException {
        InputStreamReader isr = new InputStreamReader(str, ENCODE);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null) {
            builder.append(line);
         }
 
        return builder;
     }
 
     public static void writeStrToStream(OutputStream str, CharSequence data) throws IOException {
         OutputStreamWriter osr = new OutputStreamWriter(str, ENCODE);
         BufferedWriter writer  = new BufferedWriter(osr);
         System.out.println("length:" + data.length());
         writer.write(data.toString(), 0, data.length());
         writer.close();
         return;
     }
 }
 
