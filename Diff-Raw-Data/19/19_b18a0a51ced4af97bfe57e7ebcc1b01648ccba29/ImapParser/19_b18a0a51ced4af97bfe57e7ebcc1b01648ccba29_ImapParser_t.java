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
 
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.StringParser;
 
 import java.io.UnsupportedEncodingException;
 
 import java.util.Calendar;
 import java.util.Vector;
 
 
 /**
  * This class contains all static parser functions
  * needed when using the IMAP protocol
  */
 class ImapParser {
     private static String strNIL = "NIL";
     private static final String MODIFIED_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+,";
 
     private ImapParser() {
     }
 
     static ImapProtocol.MessageFlags parseMessageFlags(Vector flagsVec) {
         ImapProtocol.MessageFlags flags = new ImapProtocol.MessageFlags();
 
         String text;
         int size = flagsVec.size();
 
         for (int i = 0; i < size; i++) {
             if (flagsVec.elementAt(i) instanceof String) {
                 text = (String) flagsVec.elementAt(i);
 
                 if (text.equalsIgnoreCase("\\Seen")) {
                     flags.seen = true;
                 } else if (text.equalsIgnoreCase("\\Answered")) {
                     flags.answered = true;
                 } else if (text.equalsIgnoreCase("\\Flagged")) {
                     flags.flagged = true;
                 } else if (text.equalsIgnoreCase("\\Deleted")) {
                     flags.deleted = true;
                 } else if (text.equalsIgnoreCase("\\Draft")) {
                     flags.draft = true;
                 } else if (text.equalsIgnoreCase("\\Recent")) {
                     flags.recent = true;
                 } else if (text.equalsIgnoreCase("Junk") ||
                         text.equalsIgnoreCase("$Junk")) {
                     flags.junk = true;
                 }
             }
         }
 
         return flags;
     }
 
     static String createMessageFlagsString(ImapProtocol.MessageFlags flags) {
         StringBuffer buf = new StringBuffer();
 
         if (flags.seen) {
             buf.append("\\Seen");
         }
 
         if (flags.answered) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append("\\Answered");
         }
 
         if (flags.flagged) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append("\\Flagged");
         }
 
         if (flags.deleted) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append("\\Deleted");
         }
 
         if (flags.draft) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append("\\Draft");
         }
 
         if (flags.recent) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append("\\Recent");
         }
 
         return buf.toString();
     }
 
     static MessageEnvelope parseMessageEnvelope(Vector parsedEnv) {
         // Sanity checking
         if (parsedEnv.size() < 10) {
             EventLogger.logEvent(AppInfo.GUID,
                 "ImapParser.parseMessageEnvelope: Sanity check failed".getBytes(),
                 EventLogger.WARNING);
 
             return generateDummyEnvelope();
         }
 
         MessageEnvelope env = new MessageEnvelope();
 
         if (parsedEnv.elementAt(0) instanceof String) {
             try {
                 env.date = StringParser.parseDateString((String) parsedEnv.elementAt(
                             0));
             } catch (Exception e) {
                 env.date = Calendar.getInstance().getTime();
             }
         }
 
         if (parsedEnv.elementAt(1) instanceof String) {
             String subject = (String) parsedEnv.elementAt(1);
 
             if (subject.equals(strNIL)) {
                 env.subject = "";
             } else {
                 env.subject = StringParser.parseEncodedHeader(subject);
             }
         }
 
         if (parsedEnv.elementAt(2) instanceof Vector) {
             env.from = parseAddressList((Vector) parsedEnv.elementAt(2));
         }
 
         if (parsedEnv.elementAt(3) instanceof Vector) {
             env.sender = parseAddressList((Vector) parsedEnv.elementAt(3));
         }
 
         if (parsedEnv.elementAt(4) instanceof Vector) {
             env.replyTo = parseAddressList((Vector) parsedEnv.elementAt(4));
         }
 
         if (parsedEnv.elementAt(5) instanceof Vector) {
             env.to = parseAddressList((Vector) parsedEnv.elementAt(5));
         }
 
         if (parsedEnv.elementAt(6) instanceof Vector) {
             env.cc = parseAddressList((Vector) parsedEnv.elementAt(6));
         }
 
         if (parsedEnv.elementAt(7) instanceof Vector) {
             env.bcc = parseAddressList((Vector) parsedEnv.elementAt(7));
         }
 
         if (parsedEnv.elementAt(8) instanceof String) {
             env.inReplyTo = (String) parsedEnv.elementAt(8);
 
             if (env.inReplyTo.equals(strNIL)) {
                 env.inReplyTo = "";
             }
         }
 
         if (parsedEnv.elementAt(9) instanceof String) {
             env.messageId = (String) parsedEnv.elementAt(9);
 
             if (env.messageId.equals(strNIL)) {
                 env.messageId = "";
             }
         }
 
         return env;
     }
 
     static String[] parseAddressList(Vector addrVec) {
         // Find the number of addresses, and allocate the array
         String[] addrList = new String[addrVec.size()];
         int index = 0;
 
         for (int i = 0; i < addrVec.size(); i++) {
             if ((addrVec.elementAt(i) instanceof Vector) &&
                     (((Vector) addrVec.elementAt(i)).size() >= 4)) {
                 Vector entry = (Vector) addrVec.elementAt(i);
 
                 String realName = strNIL;
 
                 if (entry.elementAt(0) instanceof String) {
                     realName = StringParser.parseEncodedHeader((String) entry.elementAt(
                                 0));
                 }
 
                 String mbName = strNIL;
 
                 if (entry.elementAt(2) instanceof String) {
                     mbName = (String) entry.elementAt(2);
                 }
 
                 String hostName = strNIL;
 
                 if (entry.elementAt(3) instanceof String) {
                     hostName = (String) entry.elementAt(3);
                 }
 
                 String addrStr = (mbName.equals(strNIL) ? "" : mbName) +
                     (hostName.equals(strNIL) ? "" : ('@' + hostName));
 
                 // Now assemble these into a single address entry
                 // (possibly eventually storing them separately)
                 if ((realName.length() > 0) && !realName.equals(strNIL)) {
                     addrList[index] = realName + " <" + addrStr + ">";
                 } else {
                     addrList[index] = addrStr;
                 }
 
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
             parsedText = StringParser.nestedParenStringLexer(rawText.substring(
                         rawText.indexOf('(')));
         } catch (Exception exp) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapParser.parseMessageStructure: " +
                 "Caught exception when parsing input:\r\n" + exp.toString()).getBytes(),
                 EventLogger.WARNING);
 
             return null;
         }
 
         // Find the BODYSTRUCTURE portion of the reply
         Vector parsedStruct = null;
         int size = parsedText.size();
 
         for (int i = 0; i < size; i++) {
             if (parsedText.elementAt(i) instanceof String) {
                 String label = (String) parsedText.elementAt(i);
 
                 if (label.equalsIgnoreCase("BODYSTRUCTURE") &&
                         (i < (size - 1)) &&
                         parsedText.elementAt(i + 1) instanceof Vector) {
                     parsedStruct = (Vector) parsedText.elementAt(i + 1);
                 }
             }
         }
 
         // Sanity checking
         if (parsedStruct == null) {
             EventLogger.logEvent(AppInfo.GUID,
                 "ImapParser.parseMessageStructure: Sanity check failed".getBytes(),
                 EventLogger.WARNING);
 
             return null;
         }
 
         MessageSection msgStructure = parseMessageStructureHelper(null, 1,
                 parsedStruct);
         fixMessageStructure(msgStructure);
 
         return msgStructure;
     }
 
     /**
      * This method implements a kludge to fix body part addresses
      */
     private static void fixMessageStructure(MessageSection msgStructure) {
         if (msgStructure == null) {
             return;
         }
 
         int p = msgStructure.address.indexOf('.');
 
         if ((p != -1) && ((p + 1) < msgStructure.address.length())) {
             msgStructure.address = msgStructure.address.substring(p + 1);
         }
 
         if ((msgStructure.subsections != null) &&
                 (msgStructure.subsections.length > 0)) {
             for (int i = 0; i < msgStructure.subsections.length; i++) {
                 fixMessageStructure(msgStructure.subsections[i]);
             }
         }
     }
 
     private static MessageSection parseMessageStructureHelper(
         String parentAddress, int index, Vector parsedStruct) {
         // Determine the address of this body part
         String address;
 
         if (parentAddress == null) {
             address = Integer.toString(index);
         } else {
             address = parentAddress + "." + Integer.toString(index);
         }
 
         // Determine the number of body parts and parse
         if (parsedStruct.elementAt(0) instanceof String) {
             // The first element is a string, so we hit a simple message part
             MessageSection section = parseMessageStructureSection(parsedStruct);
             section.address = address;
 
             return section;
         } else if (parsedStruct.elementAt(0) instanceof Vector) {
             // The first element is a vector, so we hit a multipart message part
             int size = parsedStruct.size();
             Vector subSectionsVector = new Vector();
 
             for (int i = 0; i < size; ++i) {
                 // Iterate through the message parts
                 if (parsedStruct.elementAt(i) instanceof Vector) {
                     subSectionsVector.addElement(parseMessageStructureHelper(
                             address, i + 1, (Vector) parsedStruct.elementAt(i)));
                 } else if (parsedStruct.elementAt(i) instanceof String) {
                     MessageSection section = new MessageSection();
                     section.type = "multipart";
                     section.subtype = ((String) parsedStruct.elementAt(i)).toLowerCase();
                     section.subsections = new MessageSection[subSectionsVector.size()];
                     subSectionsVector.copyInto(section.subsections);
                     section.address = address;
 
                     return section;
                 }
             }
         }
 
         return null;
     }
 
     private static MessageSection parseMessageStructureSection(
         Vector sectionList) {
         MessageSection sec = new MessageSection();
         Vector tmpVec;
        int size;
 
         if (sectionList.elementAt(0) instanceof String) {
             sec.type = ((String) sectionList.elementAt(0)).toLowerCase();
         }
 
         if (sectionList.elementAt(1) instanceof String) {
             sec.subtype = ((String) sectionList.elementAt(1)).toLowerCase();
         }
 
         sec.charset = null;
 
         if (sectionList.elementAt(2) instanceof Vector) {
             tmpVec = (Vector) sectionList.elementAt(2);
 
            size = tmpVec.size();
            for(int i=0; i < size - 1; i+=2) {
            	if(tmpVec.elementAt(i) instanceof String &&
            	   tmpVec.elementAt(i+1) instanceof String) {
            		String key = (String)tmpVec.elementAt(i);
            		String value = (String)tmpVec.elementAt(i+1);
            		if(key.equalsIgnoreCase("charset")) {
            			sec.charset = value;
            		}
            	}
             }
         }
 
         if (sectionList.elementAt(5) instanceof String) {
             sec.encoding = ((String) sectionList.elementAt(5)).toLowerCase();
         }
 
         if (sectionList.elementAt(6) instanceof String) {
             try {
                 sec.size = Integer.parseInt((String) sectionList.elementAt(6));
             } catch (Exception exp) {
                 sec.size = -1;
             }
         }
 
         return sec;
     }
 
     /**
      * Takes in the raw IMAP folder name, and outputs a string that
      * has been properly decoded according to section 5.1.3 of
      * RFC 3501.
      *
      * @param rawText Text from the server.
      * @return Decoded result.
      */
     static String parseFolderName(String rawText) {
         StringBuffer buf = new StringBuffer();
         StringBuffer intlBuf = null;
         int index = 0;
         int len = rawText.length();
         boolean usMode = true;
 
         while (index < len) {
             char ch = rawText.charAt(index);
 
             if (usMode) {
                 if (ch != '&') {
                     buf.append(ch);
                     index++;
                 } else if ((ch == '&') && (index < (len - 1)) &&
                         (rawText.charAt(index + 1) == '-')) {
                     buf.append(ch);
                     index += 2;
                 } else {
                     usMode = false;
                     index++;
                 }
             } else {
                 if (intlBuf == null) {
                     intlBuf = new StringBuffer();
                 }
 
                 if (ch == '-') {
                     buf.append(decodeModifiedBase64(intlBuf.toString()));
                     intlBuf = null;
                     usMode = true;
                     index++;
                 } else {
                     intlBuf.append(ch);
                     index++;
                 }
             }
         }
 
         return buf.toString();
     }
 
     /**
      * Decodes the IMAP modification of the UTF-7 modification of Base64.
      *
      * This is probably a very sloppy and inefficient implementation,
      * which is why it is only used for decoding folder names.  This operation
      * is extremely infrequent, and usually only happens on select characters.
      * While the code should be improved a bit, it is hopefully sufficient
      * for now.  Propper Base64 decoding will still be used everywhere else.
      *
      * @param input Encoded string
      * @return Decoded string
      */
     private static String decodeModifiedBase64(String input) {
         boolean[] bits = new boolean[input.length() * 6];
         int len = input.length();
         int bitsIndex = 0;
 
         for (int i = 0; i < len; i++) {
             byte val = (byte) MODIFIED_BASE64_ALPHABET.indexOf(input.charAt(i));
             bits[bitsIndex++] = (val & (byte) 0x20) != 0;
             bits[bitsIndex++] = (val & (byte) 0x10) != 0;
             bits[bitsIndex++] = (val & (byte) 0x08) != 0;
             bits[bitsIndex++] = (val & (byte) 0x04) != 0;
             bits[bitsIndex++] = (val & (byte) 0x02) != 0;
             bits[bitsIndex++] = (val & (byte) 0x01) != 0;
         }
 
         byte[] decodeData = new byte[(bits.length - (bits.length % 16)) / 8];
         bitsIndex = 0;
 
         for (int i = 0; i < decodeData.length; i++) {
             decodeData[i] = 0;
 
             for (int j = 7; j >= 0; j--) {
                 decodeData[i] += (bits[bitsIndex] ? (1 << j) : 0);
                 bitsIndex++;
 
                 if (bitsIndex >= bits.length) {
                     break;
                 }
             }
         }
 
         try {
             String result = new String(decodeData, "UTF-16BE");
 
             return result;
         } catch (UnsupportedEncodingException e) {
             return input;
         }
     }
 
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
 }
