 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail.imap;
 
 import java.util.Calendar;
 import java.util.Vector;
 import org.logicprobe.LogicMail.mail.*;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * This class contains all static parser functions
  * needed when using the IMAP protocol
  */
 class ImapParser {
     /**
      * Simple container for a parsed message structure tree
      */
     public static class MessageSection {
         public String address;
         public String type;
         public String subtype;
         public String encoding;
         public String charset;
         public int size;
         public MessageSection[] subsections;
     }
     
     private ImapParser() { }
     
     static ImapProtocol.MessageFlags parseMessageFlags(String rawText) {
         ImapProtocol.MessageFlags flags = new ImapProtocol.MessageFlags();
         flags.seen     = (rawText.indexOf("\\Seen") != -1);
         flags.answered = (rawText.indexOf("\\Answered") != -1);
         flags.flagged  = (rawText.indexOf("\\Flagged") != -1);
         flags.deleted  = (rawText.indexOf("\\Deleted") != -1);
         flags.draft    = (rawText.indexOf("\\Draft") != -1);
         flags.recent   = (rawText.indexOf("\\Recent") != -1);
         return flags;
     }
 
     
     
     static MessageEnvelope parseMessageEnvelope(Vector parsedEnv) {
         // Sanity checking
         if(parsedEnv.size() < 10)
            return generateDummyEnvelope();
             
         MessageEnvelope env = new MessageEnvelope();
 
         if(parsedEnv.elementAt(0) instanceof String) {
             env.date = StringParser.parseDateString((String)parsedEnv.elementAt(0));
         }
         
         if(parsedEnv.elementAt(1) instanceof String) {
             env.subject = (String)parsedEnv.elementAt(1);
         }
 
         if(parsedEnv.elementAt(2) instanceof Vector) {
             env.from = parseAddressList((Vector)parsedEnv.elementAt(2));
         }
         
         if(parsedEnv.elementAt(3) instanceof Vector) {
             env.sender = parseAddressList((Vector)parsedEnv.elementAt(3));
         }
 
         if(parsedEnv.elementAt(4) instanceof Vector) {
             env.replyTo = parseAddressList((Vector)parsedEnv.elementAt(4));
         }
 
         if(parsedEnv.elementAt(5) instanceof Vector) {
             env.to = parseAddressList((Vector)parsedEnv.elementAt(5));
         }
 
         if(parsedEnv.elementAt(6) instanceof Vector) {
             env.cc = parseAddressList((Vector)parsedEnv.elementAt(6));
         }
 
         if(parsedEnv.elementAt(7) instanceof Vector) {
             env.bcc = parseAddressList((Vector)parsedEnv.elementAt(7));
         }
 
         if(parsedEnv.elementAt(8) instanceof String) {
             env.inReplyTo = (String)parsedEnv.elementAt(8);
             if(env.inReplyTo.equals("NIL")) env.inReplyTo = "";
         }
 
         if(parsedEnv.elementAt(9) instanceof String) {
             env.messageId = (String)parsedEnv.elementAt(9);
             if(env.messageId.equals("NIL")) env.messageId = "";
         }
         return env;
     }
 
     static String[] parseAddressList(Vector addrVec) {
         // Find the number of addresses, and allocate the array
         String[] addrList = new String[addrVec.size()];
         int index = 0;
         
         for(int i=0;i<addrVec.size();i++) {
             if((addrVec.elementAt(i) instanceof Vector) &&
                ((Vector)addrVec.elementAt(i)).size() >= 4) {
                 
                 Vector entry = (Vector)addrVec.elementAt(i);
 
                 String realName = "NIL";
                 if(entry.elementAt(0) instanceof String)
                     realName = (String)entry.elementAt(0);
 
                 String mbName = "NIL";
                 if(entry.elementAt(2) instanceof String)
                     mbName = (String)entry.elementAt(2);
 
                 String hostName = "NIL";
                 if(entry.elementAt(3) instanceof String)
                     hostName = (String)entry.elementAt(3);
                 // Now assemble these into a single address entry
                 // (possibly eventually storing them separately)
                 if(realName.length() > 0 && !realName.equals("NIL"))
                     addrList[index] = realName + " <" + mbName + "@" + hostName + ">";
                 else
                     addrList[index] = mbName + "@" + hostName;
                 index++;
             }
         }
         return addrList;
     }
     
     static MessageEnvelope generateDummyEnvelope() {
         MessageEnvelope env = new MessageEnvelope();
         env.date = Calendar.getInstance().getTime();
         env.from = new String[1];
         env.from[0] = "<sender>";
         env.subject = "<subject>";
         return env;
     }
 
     /**
      * Parse the IMAP message structure tree.
      *
      * @param Raw text returned from the server
      * @return Root of the message structure tree
      */
     static MessageSection parseMessageStructure(String rawText) {
         Vector parsedText = null;
         try {
             parsedText = StringParser.nestedParenStringLexer(rawText.substring(rawText.indexOf('(')));
         } catch (Exception exp) {
             return null;
         }
 
         // Sanity checking
         if(parsedText.size() < 2 ||
            !(parsedText.elementAt(1) instanceof Vector))
            return null;
         
         Vector parsedStruct = (Vector)parsedText.elementAt(1);
         MessageSection msgStructure = parseMessageStructureHelper(null, 1, parsedStruct);
         fixMessageStructure(msgStructure);
         return msgStructure;
     }
 
     /**
      * This method implements a kludge to fix body part addresses
      */
     private static void fixMessageStructure(MessageSection msgStructure) {
         if(msgStructure == null) return;
         int p = msgStructure.address.indexOf('.');
         if(p != -1 && p+1 < msgStructure.address.length())
             msgStructure.address = msgStructure.address.substring(p+1);
         
         if(msgStructure.subsections != null && msgStructure.subsections.length > 0)
             for(int i=0;i<msgStructure.subsections.length;i++)
                 fixMessageStructure(msgStructure.subsections[i]);
     }
     
     private static MessageSection parseMessageStructureHelper(String parentAddress,
                                                               int index,
                                                               Vector parsedStruct) {
         // Determine the address of this body part
         String address;
         if(parentAddress == null) {
             address = Integer.toString(index);
         }
         else {
             address = parentAddress + "." + Integer.toString(index);
         }
         // Determine the number of body parts and parse
         if(parsedStruct.elementAt(0) instanceof String) {
             // The first element is a string, so we hit a simple message part
             MessageSection section = parseMessageStructureSection(parsedStruct);
             section.address = address;
             return section;
         }
         else if(parsedStruct.elementAt(0) instanceof Vector) {
             // The first element is a vector, so we hit a multipart message part
             int size = parsedStruct.size();
            MessageSection[] subSections = new MessageSection[size-4];
             for(int i=0;i<size;++i) {
                 // Iterate through the message parts
                 if(parsedStruct.elementAt(i) instanceof Vector)
                     subSections[i] = parseMessageStructureHelper(address, i+1, (Vector)parsedStruct.elementAt(i));
                 else if(parsedStruct.elementAt(i) instanceof String) {
                     MessageSection section = new MessageSection();
                     section.type = "multipart";
                     section.subtype = ((String)parsedStruct.elementAt(i)).toLowerCase();
                     section.subsections = subSections;
                     section.address = address;
                     return section;
                 }
             }
         }
         return null;
     }
     
     private static MessageSection parseMessageStructureSection(Vector sectionList) {
         MessageSection sec = new MessageSection();
         Vector tmpVec;
         
         if(sectionList.elementAt(0) instanceof String) {
             sec.type = ((String)sectionList.elementAt(0)).toLowerCase();
         }
 
         if(sectionList.elementAt(1) instanceof String) {
             sec.subtype = ((String)sectionList.elementAt(1)).toLowerCase();
         }
 
         sec.charset = null;
         if(sectionList.elementAt(2) instanceof Vector) {
             tmpVec = (Vector)sectionList.elementAt(2);
             if(tmpVec.size() >= 2) {
                 if((tmpVec.elementAt(0) instanceof String) &&
                    ((String)tmpVec.elementAt(0)).equalsIgnoreCase("charset") &&
                    tmpVec.elementAt(1) instanceof String)
                     sec.charset = (String)tmpVec.elementAt(1);                    
             }
         }
         
         if(sectionList.elementAt(5) instanceof String) {
             sec.encoding = ((String)sectionList.elementAt(5)).toLowerCase();
         }
 
         if(sectionList.elementAt(6) instanceof String) {
             try {
                 sec.size = Integer.parseInt((String)sectionList.elementAt(6));
             } catch (Exception exp) {
                 sec.size = -1;
             }
         }
 
         return sec;
     }
 }
