 /* $Id$
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.hampelratte.svdrp.util;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import de.hampelratte.svdrp.responses.highlevel.Channel;
 
 /**
  * Parses a list of channels received from VDR by the LSTC command
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  *
  */
 public class ChannelParser {
     /**
      * Parses a list of channels received from VDR by the LSTC command
      * @param channelData A list of channels received from VDR by LSTC command
      * @return A list of Channel objects
      */
     public static ArrayList<Channel> parse(String channelData) {
         ArrayList<Channel> list = new ArrayList<Channel>();
         StringTokenizer st = new StringTokenizer(channelData, "\n");
         while (st.hasMoreTokens()) {
             Channel channel = new Channel();
             String line = st.nextToken();
             // parse channelNumber
             channel.setChannelNumber(Integer.parseInt(line.substring(0, line.indexOf(" "))));
             // remove channelNumber
             line = line.substring(line.indexOf(" ") + 1); 
             String[] parts = line.split(":");
             channel.setName(parts[0]);
             channel.setFrequency(Integer.parseInt(parts[1]));
             parseParameters(channel, parts[2]);
             channel.setSource(parts[3]);
             channel.setSymbolRate(Integer.parseInt(parts[4]));
             channel.setVPID(parts[5]);
             channel.setAPID(parts[6]);
             channel.setTPID(parts[7]);
             channel.setConditionalAccess(parts[8]);
             channel.setSID(Integer.parseInt(parts[9]));
             channel.setNID(Integer.parseInt(parts[10]));
             channel.setTID(Integer.parseInt(parts[11]));
             channel.setRID(Integer.parseInt(parts[12]));
             list.add(channel);
         }
         return list;
     }
 
     private static void parseParameters(Channel channel, String string) {
         for (int i = 0; i < string.length(); i++) {
             char c = string.charAt(i);
             switch(c) {
             case 'B':
                 channel.setBandwidth(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 break;
             case 'C':
                if(Character.isDigit(string.charAt(i+2))) {
                     channel.setCodeRateHP(Integer.parseInt(string.substring(i+1, i+3)));
                 } else {
                     channel.setCodeRateHP(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 }
                 break;
             case 'D':
                if(Character.isDigit(string.charAt(i+2))) {
                     channel.setCodeRateLP(Integer.parseInt(string.substring(i+1, i+3)));
                 } else {
                     channel.setCodeRateLP(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 }
                 break;
             case 'G':
                if(Character.isDigit(string.charAt(i+2))) {
                     channel.setGuardInterval(Integer.parseInt(string.substring(i+1, i+3)));
                 } else {
                     channel.setGuardInterval(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 }
                 break;
             case 'h':
                 channel.setHorizontalPolarization(true);
                 break;
             case 'I':
                 channel.setInversion(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 break;
             case 'L':
                 channel.setLeftCircularPolarization(true);
                 break;
             case 'M':
                if(Character.isDigit(string.charAt(i+3))) {
                     channel.setModulation(Integer.parseInt(string.substring(i+1, i+4)));
                 } else if(Character.isDigit(string.charAt(i+2))) { 
                     channel.setModulation(Integer.parseInt(string.substring(i+1, i+3)));
                 } else {
                     channel.setModulation(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 }
                 break;
             case 'R':
                 channel.setRightCircularPolarization(true);
                 break;
             case 'T':
                 channel.setTransmissionMode(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 break;
             case 'v':
                 channel.setVerticalPolarization(true);
                 break;
             case 'Y':
                 channel.setHierarchy(Integer.parseInt(Character.toString(string.charAt(i+1))));
                 break;
             default:
                 break;
             }
         }
         
     }
 }
