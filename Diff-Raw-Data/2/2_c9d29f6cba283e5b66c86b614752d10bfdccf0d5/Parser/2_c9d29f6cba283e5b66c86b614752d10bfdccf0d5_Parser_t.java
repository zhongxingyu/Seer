 /*
  * Copyright (C) 2011 cybertk
  *
  * -- https://github.com/kyan-he/picologger/raw/master/jsyslogd/src/com/github/picologger/syslog/Parser.java--
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
 package com.github.picologger.syslog;
 
 public abstract class Parser
 {
     
     public static Syslog parse(String record) throws IllegalArgumentException
     {
        if (null == record || "".equals(record))
         {
             throw new IllegalArgumentException("no record.");
         }
         
         Syslog log = new Syslog();
         int pos0 = 0;
         int pos = 0;
         
         // Validate format.
         pos = record.indexOf('>');
         if (record.charAt(0) != '<' || pos > 4)
         {
             throw new IllegalArgumentException("Malformed syslog record.");
         }
         
         // Parse Header.
         
         // Parse facility and severity.
         try
         {
             int pri = Integer.parseInt((record.substring(1, pos)));
             log.setFacility(pri >> 3);
             log.setSeverity(pri & 0x7);
         }
         catch (NumberFormatException e)
         {
             throw new IllegalArgumentException("Malformed syslog record.");
         }
         
         // Parse Version.
         ++pos;
         final int version = record.charAt(pos) - 0x30;
         
         // Validate Version.
         if (version != 1)
         {
             throw new IllegalArgumentException(
                     "Malformed syslog record. RFC3164?");
         }
         
         log.setVersion(version);
         String[] token = record.split(" ", 7);
         
         log.setTimestamp(token[1]);
         log.setHostname(token[2]);
         log.setAppname(token[3]);
         log.setProcid(token[4]);
         log.setMsgid(token[5]);
         
         // Parse SD
         if (token[6].charAt(0) == '[')
         {
             while (true)
             {
                 pos0 = token[6].indexOf(']', pos0);
                 if (pos0 == -1)
                 {
                     break;
                 }
                 
                 ++pos0;
                 
                 // Record the index.
                 if (token[6].charAt(pos0 - 2) != '\\')
                 {
                     // Make sure it's not a escaped "]".
                     pos = pos0;
                 }
             }
         }
         else
         {
             // NILVAULE, "-".
             
             pos = 1;
         }
         log.setSd(token[6].substring(0, pos));
         
         // Parse message.
         if (pos < token[6].length())
         {
             log.setMsg(token[6].substring(pos + 1));
         }
         else
         {
             log.setMsg("");
         }
         
         return log;
     }
 }
