 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 
 /**
  * A simple filereader.
  * @author corysmith
  */
 public class SimpleFileReader implements FileReader {
 
   public String readFile(String file)  {
     InputStreamReader reader = null;
     try {
      reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), Charset.forName("UTF-8"));
       StringBuilder sb = new StringBuilder();
 
      for (int c = reader.read(); c != -1; c = reader.read()) {
         sb.append((char) c);
       }
      String contents = sb.toString();
 
       return contents;
     } catch (IOException e) {
       throw new RuntimeException("Impossible to read file: " + file, e);
     } finally {
       if (reader != null) {
         try {
           reader.close();
         } catch (IOException e) {
           // ignore
         }
       }
     }
   }
 }
