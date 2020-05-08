 // ========================================================================
// Copyright 2008-2010 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 
 package org.cipango.example;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SdpParser {
 
     public RtpConnection getRtpConnection(String sdp)
     {
         StringReader stringReader = new StringReader(sdp);
         BufferedReader bufferedReader = new BufferedReader(stringReader);
         String host = null;
         List<Integer> payloadTypes = new ArrayList<Integer>();
         int port = -1;
         String line;
         try
         {
             while ((line = bufferedReader.readLine()) != null)
             {
                 if (line.startsWith("c="))
                 {
                     String[] c = line.split(" ");
                     if (c.length < 3)
                         return null;
                     host = c[c.length - 1];
                 }
                 if (line.startsWith("m=audio"))
                 {
                     String[] m = line.split(" ");
                     for (String element: m)
                     {
                         try
                         {
                             int integer = Integer.parseInt(element);
                             if (port == -1)
                                 port = integer;
                             else
                                 payloadTypes.add(integer);
                         }
                         catch (NumberFormatException e) { }
                     }
                 }
             }
         }
         catch (IOException e) {
             System.err.println("IOException: " + e.getMessage());
         }
         try
         {
             InetAddress.getByName(host);
         }
         catch (UnknownHostException e)
         {
             return null;
         }
         if (host == null || port == -1 || payloadTypes.size() == 0)
             return null;
         return new RtpConnection(host, port, payloadTypes);
     }
 
 }
