 /*
  * Copyright 2012 Twitter, Inc.
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
 package com.twitter.tokyo.kucho.daemon;
 
 import java.io.*;
 import java.util.List;
 
 public class EHillsImpl implements EHills {
     private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EHillsImpl.class.getName());
     private static final EHills ehills = new EHillsImpl();
 
     private EHillsImpl() {
     }
 
     public static EHills getInstance() {
         return ehills;
     }
 
     public boolean adjust(int value, List<String> areas) {
         return callHigepon("./api/call_remote.rb", value,areas);
     }
 
     private boolean callHigepon(String command, int value, List<String> areas) {
         try {
            logger.info(command + " " + String.valueOf(value) + " " + join(areas, " "));
            ProcessBuilder pb = new ProcessBuilder("ruby", command,String.valueOf(value),  join(areas, " "));
            pb.redirectErrorStream(true);
             if (new File(".").getAbsolutePath().contains("kucho-daemon")) {
                 pb.directory(new File("../"));
             }
             Process p = pb.start();
             p.waitFor();
             InputStream is = p.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             StringBuilder result = new StringBuilder();
             String line;
             while (null != (line = br.readLine())) {
                 result.append(line).append('\n');
             }
             logger.info("response from the API:" + result);
             return result.toString().contains("ok");
         } catch (IOException e) {
             logger.error("failed:", e);
             return false;
         } catch (InterruptedException e) {
             logger.error("failed:", e);
             return false;
         }
     }
 
     private static String join(List<String> strs, String separator) {
         StringBuilder joined = new StringBuilder();
         boolean first = true;
         for (String str : strs) {
             if (first) {
                 first = false;
             } else {
                 joined.append(separator);
             }
             joined.append(str);
         }
         return joined.toString();
     }
 }
