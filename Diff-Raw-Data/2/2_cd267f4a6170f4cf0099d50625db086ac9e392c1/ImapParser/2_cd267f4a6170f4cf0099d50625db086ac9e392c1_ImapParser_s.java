 /*-
  * Copyright (c) 2010, Derek Konigsberg
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
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.ByteVector;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessagePart;
 import org.logicprobe.LogicMail.message.MultiPart;
 import org.logicprobe.LogicMail.message.TextPart;
 import org.logicprobe.LogicMail.util.StringArrays;
 import org.logicprobe.LogicMail.util.StringParser;
 
 import java.io.UnsupportedEncodingException;
 
 import java.util.Calendar;
 import java.util.Vector;
 
 
 /**
  * This class contains all static parser functions
  * needed when using the IMAP protocol
  */
 class ImapParser {
     private static String NIL = "NIL";
     private static String BODYSTRUCTURE = "BODYSTRUCTURE";
     private static String MODIFIED_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+,";
     private static Character LPAREN = new Character('(');
     private static Character RPAREN = new Character(')');
     private static String US_ASCII = "US-ASCII";
     private static String NAME = "name";
     private static String CHARSET = "charset";
     static String FLAG_SEEN = "\\Seen";
     static String FLAG_ANSWERED = "\\Answered";
     static String FLAG_FLAGGED = "\\Flagged";
     static String FLAG_DELETED = "\\Deleted";
     static String FLAG_DRAFT = "\\Draft";
     static String FLAG_RECENT = "\\Recent";
     static String FLAG_FORWARDED = "$Forwarded";
     static String FLAG_JUNK0 = "Junk";
     static String FLAG_JUNK1 = "$Junk";
     
     private ImapParser() {
     }
 
     static ImapProtocol.MessageFlags parseMessageFlags(Vector flagsVec) {
         ImapProtocol.MessageFlags flags = new ImapProtocol.MessageFlags();
 
         String text;
         int size = flagsVec.size();
 
         for (int i = 0; i < size; i++) {
             if (flagsVec.elementAt(i) instanceof String) {
                 text = (String) flagsVec.elementAt(i);
 
                 if (text.equalsIgnoreCase(FLAG_SEEN)) {
                     flags.seen = true;
                 } else if (text.equalsIgnoreCase(FLAG_ANSWERED)) {
                     flags.answered = true;
                 } else if (text.equalsIgnoreCase(FLAG_FLAGGED)) {
                     flags.flagged = true;
                 } else if (text.equalsIgnoreCase(FLAG_DELETED)) {
                     flags.deleted = true;
                 } else if (text.equalsIgnoreCase(FLAG_DRAFT)) {
                     flags.draft = true;
                 } else if (text.equalsIgnoreCase(FLAG_RECENT)) {
                     flags.recent = true;
                 } else if (text.equalsIgnoreCase(FLAG_FORWARDED)) {
                     flags.forwarded = true;
                 } else if (text.equalsIgnoreCase(FLAG_JUNK0) ||
                         text.equalsIgnoreCase(FLAG_JUNK1)) {
                     flags.junk = true;
                 }
             }
         }
 
         return flags;
     }
 
     static String createMessageFlagsString(ImapProtocol.MessageFlags flags) {
         StringBuffer buf = new StringBuffer();
 
         if (flags.seen) {
             buf.append(FLAG_SEEN);
         }
 
         if (flags.answered) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_ANSWERED);
         }
 
         if (flags.flagged) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_FLAGGED);
         }
 
         if (flags.deleted) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_DELETED);
         }
 
         if (flags.draft) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_DRAFT);
         }
 
         if (flags.recent) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_RECENT);
         }
 
         if (flags.forwarded) {
             if (buf.length() > 0) {
                 buf.append(' ');
             }
 
             buf.append(FLAG_FORWARDED);
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
 
         if (parsedEnv.elementAt(0) instanceof byte[]) {
             try {
                 env.date = StringParser.parseDateString(
                         new String((byte[]) parsedEnv.elementAt(0)));
             } catch (Exception e) {
                 env.date = Calendar.getInstance().getTime();
             }
         }
         else {
             env.date = Calendar.getInstance().getTime();
         }
 
         if (parsedEnv.elementAt(1) instanceof byte[]) {
             env.subject = StringParser.parseEncodedHeader(
                     new String((byte[])parsedEnv.elementAt(1)));
         }
         else {
             env.subject = "";
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
 
         if (parsedEnv.elementAt(8) instanceof byte[]) {
             env.inReplyTo = new String((byte[])parsedEnv.elementAt(8));
         }
         else {
             env.inReplyTo = "";
         }
 
         if (parsedEnv.elementAt(9) instanceof byte[]) {
             env.messageId = new String((byte[])parsedEnv.elementAt(9));
         }
         else {
             env.messageId = "";
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
 
                 String realName = NIL;
 
                 if (entry.elementAt(0) instanceof byte[]) {
                     realName = StringParser.parseEncodedHeader(
                             new String((byte[]) entry.elementAt(0)));
                 }
 
                 String mbName = NIL;
 
                 if (entry.elementAt(2) instanceof byte[]) {
                     mbName = new String((byte[]) entry.elementAt(2));
                 }
 
                 String hostName = NIL;
 
                 if (entry.elementAt(3) instanceof byte[]) {
                     hostName = new String((byte[]) entry.elementAt(3));
                 }
 
                 String addrStr = (mbName.equals(NIL) ? "" : mbName) +
                     (hostName.equals(NIL) ? "" : ('@' + hostName));
 
                 // Now assemble these into a single address entry
                 // (possibly eventually storing them separately)
                 if ((realName.length() > 0) && !realName.equals(NIL)) {
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
      * @param rawText Raw text returned from the server
      * @return Root of the message structure tree
      */
     static MessageSection parseMessageStructure(byte[] rawText) {
         Vector parsedText = null;
 
         try {
             int offset = Arrays.getIndex(rawText, (byte)'(');
             parsedText = ImapParser.parenListParser(rawText, offset, rawText.length - offset);
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
 
                 if (label.equalsIgnoreCase(BODYSTRUCTURE) &&
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
 
         MessageSection msgStructure = parseMessageStructureParameter(parsedStruct);
 
         return msgStructure;
     }
 
     /**
      * Parse the IMAP message structure tree from a prepared object tree
      * generated by {@link ImapParser#parenListParser(String)}.
      *
      * @param parsedStruct Tree containing the {@link Vector} that follows a BODYSTRUCTURE string
      * @return Root of the message structure tree
      */
     static MessageSection parseMessageStructureParameter(Vector parsedStruct) {
         MessageSection msgStructure = parseMessageStructureHelper(
                 null, 1, parsedStruct);
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
         if (parsedStruct.elementAt(0) instanceof byte[]) {
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
                 } else if (parsedStruct.elementAt(i) instanceof byte[]) {
                     MessageSection section = new MessageSection();
                     section.type = MultiPart.TYPE;
                     section.subtype = (new String((byte[]) parsedStruct.elementAt(i))).toLowerCase();
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
         int sectionListSize = sectionList.size();
 
         if (sectionList.elementAt(0) instanceof byte[]) {
             sec.type = (new String((byte[])sectionList.elementAt(0))).toLowerCase();
         }
 
         if (sectionList.elementAt(1) instanceof byte[]) {
             sec.subtype = (new String((byte[])sectionList.elementAt(1))).toLowerCase();
         }
 
         sec.charset = null;
 
         if (sectionList.elementAt(2) instanceof Vector) {
             tmpVec = (Vector) sectionList.elementAt(2);
 
             int size = tmpVec.size();
 
             for (int i = 0; i < (size - 1); i += 2) {
                 if (tmpVec.elementAt(i) instanceof byte[] &&
                         tmpVec.elementAt(i + 1) instanceof byte[]) {
                     String key = new String((byte[]) tmpVec.elementAt(i));
                     String value = new String((byte[]) tmpVec.elementAt(i + 1));
 
                     if (key.equalsIgnoreCase(CHARSET)) {
                         sec.charset = value;
                     } else if (key.equalsIgnoreCase(NAME)) {
                         sec.name = StringParser.parseEncodedHeader(value);
                     }
                 }
             }
         }
 
         if (sectionList.elementAt(3) instanceof byte[]) {
             sec.contentId = new String((byte[]) sectionList.elementAt(3));
         }
 
         if (sectionList.elementAt(5) instanceof byte[]) {
             sec.encoding = new String(((byte[]) sectionList.elementAt(5))).toLowerCase();
         }
 
         if (sectionList.elementAt(6) instanceof String) {
             try {
                 sec.size = Integer.parseInt((String) sectionList.elementAt(6));
             } catch (Exception exp) {
                 sec.size = -1;
             }
         }
 
         int dispositionIndex;
         if(TextPart.TYPE.equalsIgnoreCase(sec.type)) {
             dispositionIndex = 9;
         }
         else if(MessagePart.TYPE.equalsIgnoreCase(sec.type)) {
             dispositionIndex = 11;
         }
         else {
             dispositionIndex = 8;
         }
         
        if ((sectionListSize >= dispositionIndex) &&
                 sectionList.elementAt(dispositionIndex) instanceof Vector) {
             tmpVec = (Vector) sectionList.elementAt(dispositionIndex);
 
             if (tmpVec.elementAt(0) instanceof byte[]) {
                 sec.disposition = new String((byte[]) tmpVec.elementAt(0)).toLowerCase();
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
      * for now.  Proper Base64 decoding will still be used everywhere else.
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
 
     static Vector parenListLexer(byte[] rawText, int offset, int length) {
         if(offset + length > rawText.length) {
             throw new ArrayIndexOutOfBoundsException();
         }
 
         Vector result = new Vector();
         ByteVector buf = new ByteVector();
 
         int size = offset + length;
         int i = offset;
 
         while (i < size) {
             byte ch = rawText[i];
 
             if (ch == (byte)'(') {
                 result.addElement(LPAREN);
                 i++;
             } else if (ch == (byte)')') {
                 result.addElement(RPAREN);
                 i++;
             } else if (ch == (byte)'"') {
                 // Quoted string
                 i++;
                 buf.setSize(0);
 
                 while (i < (size - 1)) {
                     ch = rawText[i];
 
                     if (ch == (byte)'\\') {
                         byte ch1 = rawText[i + 1];
 
                         if (ch1 == (byte)'\\' || ch1 == (byte)'"') {
                             buf.addElement(ch1);
                         }
 
                         i += 2;
                     } else if (ch == (byte)'\r' || ch == (byte)'\n') {
                         i++;
                     } else if (ch == '"') {
                         result.addElement(buf.toArray());
                         i++;
 
                         break;
                     } else {
                         buf.addElement(ch);
                         i++;
                     }
                 }
             } else if (ch == '{') {
                 // Literal string
                 int p = StringArrays.indexOf(rawText, (byte)'}', i);
                 int len = StringArrays.parseInt(rawText, i + 1, p - i - 1);
                 i = p + 3;
                 result.addElement(Arrays.copy(rawText, i, len));
                 i += len;
 
                 // Check for invalid length, etc
             } else if (ch == ' ') {
                 // Skip the space
                 i++;
             } else {
                 // Keyword
                 int p1 = StringArrays.indexOf(rawText, (byte)' ', i);
                 int p2 = StringArrays.indexOf(rawText, (byte)')', i);
                 int p;
 
                 if (p1 == -1) {
                     p = p2;
                 } else if (p2 == -1) {
                     p = p1;
                 } else {
                     p = Math.min(p1, p2);
                 }
 
                 // check if p == -1, and bomb out
                 if(p == -1) {
                     p = size;
                 }
                 try {
                     result.addElement(new String(rawText, i, p - i, US_ASCII));
                 } catch (UnsupportedEncodingException e) {
                     result.addElement(new String(rawText, i, p - i));
                 }
 
                 i = p;
             }
         }
 
         return result;
     }
 
     /**
      * Recursively parse an IMAP parenthesized string.
      * <p>
      * Parses through a string of the form "(A (B C (D) E F))" and
      * returns a tree of representing its contents.  Each element will either
      * be a <code>Vector</code>, <code>String</code>, or a <code>byte[]</code>.
      * Vectors are elements that contain other elements, strings are unquoted
      * elements (e.g. keywords, flags, numbers), and byte arrays are quoted
      * text or literals.
      * </p>
      *
      * @param rawText The raw text to be parsed
      * @return A tree containing the parsed data
      */
     static Vector parenListParser(byte[] rawText) {
         return parenListParser(rawText, 0, rawText.length);
     }
     
     /**
      * Recursively parse an IMAP parenthesized string.
      * <p>
      * Parses through a string of the form "(A (B C (D) E F))" and
      * returns a tree of representing its contents.  Each element will either
      * be a <code>Vector</code>, <code>String</code>, or a <code>byte[]</code>.
      * Vectors are elements that contain other elements, strings are unquoted
      * elements (e.g. keywords, flags, numbers), and byte arrays are quoted
      * text or literals.
      * </p>
      *
      * @param rawText The raw text to be parsed
      * @param The offset to start parsing from
      * @param length The length of the data to parse
      * @return A tree containing the parsed data
      */
     static Vector parenListParser(byte[] rawText, int offset, int length) {
         Vector tokenized = parenListLexer(rawText, offset, length);
         MutableInteger mutableInteger = new MutableInteger();
         Vector result = parenListParserImpl(tokenized, mutableInteger);
         return result;
     }
 
     static Vector parenListParserImpl(Vector tokenized, MutableInteger index) {
         Vector result = new Vector();
         Object element = tokenized.elementAt(++index.value);
 
         while (element != RPAREN) {
             if (element == LPAREN) {
                 result.addElement(parenListParserImpl(tokenized, index));
             } else {
                 result.addElement(element);
             }
 
             element = tokenized.elementAt(++index.value);
         }
 
         return result;
     }
 
     /**
      * Simple container for a parsed message structure tree
      */
     public static class MessageSection {
         public String address;
         public String type;
         public String name;
         public String subtype;
         public String encoding;
         public String charset;
         public String disposition;
         public String contentId;
         public int size;
         public MessageSection[] subsections;
     }
 
     private static class MutableInteger {
         public int value = 0;
     }
 }
