 /*-
  * Copyright (c) 2008, Derek Konigsberg
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
 package org.logicprobe.LogicMail.util;
 
 import net.rim.device.api.io.IOUtilities;
 import net.rim.device.api.io.LineReader;
 import net.rim.device.api.io.SharedInputStream;
 import net.rim.device.api.mime.MIMEInputStream;
 import net.rim.device.api.mime.MIMEParsingException;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.DataBuffer;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessageContentFactory;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartFactory;
 import org.logicprobe.LogicMail.message.MultiPart;
 import org.logicprobe.LogicMail.message.TextPart;
 import org.logicprobe.LogicMail.message.UnsupportedContentException;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 /**
  * This class contains static parser functions used for
  * parsing raw message source text.
  */
 public class MailMessageParser {
     private static String strCRLF = "\r\n";
     private static final byte[] CRLF = new byte[] { (byte)'\r', (byte)'\n' };
     private static final byte[] CONTENT_TYPE_KEY = "Content-Type:".getBytes();
     private static final byte[] BOUNDARY_EQ = "boundary=".getBytes();
 
     private MailMessageParser() {
     }
 
     /**
      * Parses the message envelope from the message headers.
      *
      * @param rawHeaders The raw header text, separated into lines.
      * @return The message envelope.
      */
     public static MessageEnvelope parseMessageEnvelope(String[] rawHeaders) {
         Hashtable headers = StringParser.parseMailHeaders(rawHeaders);
         MessageEnvelope env = new MessageEnvelope();
 
         // Populate the common header field bits of the envelope
         env.subject = StringParser.parseEncodedHeader((String) headers.get(
                     "subject"));
 
         if (env.subject == null) {
             env.subject = "";
         }
 
         env.from = parseAddressList((String) headers.get("from"));
         env.sender = parseAddressList((String) headers.get("sender"));
         env.to = parseAddressList((String) headers.get("to"));
         env.cc = parseAddressList((String) headers.get("cc"));
         env.bcc = parseAddressList((String) headers.get("bcc"));
 
         try {
             env.date = StringParser.parseDateString((String) headers.get("date"));
         } catch (Exception e) {
             env.date = Calendar.getInstance().getTime();
         }
 
         env.replyTo = parseAddressList((String) headers.get("reply-to"));
         env.messageId = (String) headers.get("message-id");
         env.inReplyTo = (String) headers.get("in-reply-to");
 
         return env;
     }
 
     /**
      * Generates the message headers corresponding to the provided envelope.
      *
      * @param envelope The message envelope.
      * @param includeUserAgent True to include the User-Agent line.
      * @return The headers, one per line, with CRLF line separators.
      */
     public static String generateMessageHeaders(MessageEnvelope envelope,
         boolean includeUserAgent) {
         StringBuffer buffer = new StringBuffer();
 
         // Create the message headers
         buffer.append(StringParser.createEncodedRecipientHeader("From:", envelope.from));
         buffer.append(strCRLF);
 
         buffer.append(StringParser.createEncodedRecipientHeader("To:", envelope.to));
         buffer.append(strCRLF);
 
         if ((envelope.cc != null) && (envelope.cc.length > 0)) {
             buffer.append(StringParser.createEncodedRecipientHeader("Cc:", envelope.cc));
             buffer.append(strCRLF);
         }
 
         if ((envelope.replyTo != null) && (envelope.replyTo.length > 0)) {
             buffer.append(StringParser.createEncodedRecipientHeader("Reply-To:", envelope.replyTo));
             buffer.append(strCRLF);
         }
 
         buffer.append("Date: ");
         buffer.append(StringParser.createDateString(envelope.date));
         buffer.append(strCRLF);
 
         if (includeUserAgent) {
             buffer.append("User-Agent: ");
             buffer.append(AppInfo.getName());
             buffer.append('/');
             buffer.append(AppInfo.getVersion());
             buffer.append(strCRLF);
         }
 
         buffer.append(StringParser.createEncodedHeader("Subject:", envelope.subject));
         buffer.append(strCRLF);
 
         if (envelope.inReplyTo != null) {
             buffer.append("In-Reply-To: ");
             buffer.append(envelope.inReplyTo);
             buffer.append(strCRLF);
         }
 
         return buffer.toString();
     }
 
     /**
      * Separates a list of addresses contained within a message header.
      * This is slightly more complicated than a string tokenizer, as it
      * has to deal with quoting and escaping.
      *
      * @param text The header line containing the addresses.
      * @return The separated addresses.
      */
     private static String[] parseAddressList(String text) {
         String[] addresses = StringParser.parseCsvString(text);
 
         for (int i = 0; i < addresses.length; i++) {
             addresses[i] = StringParser.parseEncodedHeader(addresses[i]);
 
             if ((addresses[i].length() > 0) && (addresses[i].charAt(0) == '"')) {
                 int p = addresses[i].indexOf('<');
 
                 while ((p > 0) && (addresses[i].charAt(p) != '"'))
                     p--;
 
                 if ((p > 0) && ((p + 1) < addresses[i].length())) {
                     addresses[i] = addresses[i].substring(1, p) +
                         addresses[i].substring(p + 1);
                 }
             }
         }
 
         return addresses;
     }
     
     /**
      * Convert string-format raw message source to an InputStream that is
      * compatible with {@link #parseRawMessage(Hashtable, InputStream)}.
      *
      * @param messageSource the message source
      * @return the input stream to be passed to the parser code
      */
     public static InputStream convertMessageResultToStream(String messageSource) {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(messageSource.getBytes());
         
         Vector lines = new Vector();
         try {
             LineReader reader = new LineReader(inputStream);
             byte[] line;
             while((line = reader.readLine()) != null) {
                 lines.addElement(line);
             }
         } catch (IOException e) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("Error converting message to stream: " + e.getMessage()).getBytes(),
                     EventLogger.WARNING);
         }
         
         byte[][] resultLines = new byte[lines.size()][];
         lines.copyInto(resultLines);
         return convertMessageResultToStream(resultLines);
     }
     
     /**
      * Convert the server result to an InputStream wrapping a byte[] buffer
      * that includes the CRLF markers that were stripped out by the socket
      * reading code, and has any other necessary pre-processing applied.
      *
      * @param resultLines the lines of message data returned by the server
      * @return the input stream to be passed to the parser code
      */
     public static InputStream convertMessageResultToStream(byte[][] resultLines) {
         DataBuffer buf = new DataBuffer();
         
         boolean inHeaders = true;
         boolean inInitialHeaders = true;
         boolean firstHeaderLine = true;
         byte[] boundary = null;
         for(int i=0; i<resultLines.length; i++) {
             if(inHeaders) {
                 // Special logic to unfold message headers and replace HTAB
                 // indentations in folded headers with spaces.
                 // This is a workaround for a bug in MIMEInputStream that
                 // causes it to fail to parse certain messages with folded
                 // headers.  (The bug appears to be fixed in OS 6.0, but the
                 // workaround is always invoked because it should not have
                 // any harmful side-effects.)
                 if(resultLines[i].length == 0) {
                     inHeaders = false;
                     if(!firstHeaderLine) {
                         removeTrailingColon(buf);
                         buf.write(CRLF);
                     }
                     buf.write(CRLF);
                     
                     if(inInitialHeaders) {
                         boundary = getContentBoundary(buf.getArray(), buf.getArrayStart(), buf.getLength());
                         inInitialHeaders = false;
                     }
                 }
                 else if(resultLines[i][0] == (byte)'\t' || resultLines[i][0] == (byte)' ') {
                     for(int j=1; j<resultLines[i].length; j++) {
                         if(resultLines[i][j] != (byte)'\t' && resultLines[i][j] != (byte)' ') {
                             buf.write((byte)' ');
                             buf.write(resultLines[i], j, resultLines[i].length - j);
                             break;
                         }
                     }
                 }
                 else {
                     if(!firstHeaderLine) {
                         removeTrailingColon(buf);
                         buf.write(CRLF);
                     }
                     buf.write(resultLines[i]);
                 }
                 if(firstHeaderLine) { firstHeaderLine = false; }
             }
             else {
                 buf.write(resultLines[i]);
                 buf.write(CRLF);
                 if(Arrays.equals(resultLines[i], boundary)) {
                     inHeaders = true;
                     firstHeaderLine = true;
                 }
             }
         }
         
         ByteArrayInputStream inputStream = new ByteArrayInputStream(
                 buf.getArray(), buf.getArrayStart(), buf.getLength());
         
         return inputStream;
     }
     
     private static void removeTrailingColon(DataBuffer buf) {
         int len = buf.getLength();
         if(len > 0 && buf.getArray()[buf.getArrayStart() + len - 1] == (byte)';') {
             buf.setLength(len - 1);
         }
     }
 
     private static byte[] getContentBoundary(byte[] buf, int offset, int length) {
         int p = StringArrays.indexOf(buf, CONTENT_TYPE_KEY, offset, length, true);
         if(p == -1) { return null; } else { p += CONTENT_TYPE_KEY.length; }
         
         int q = StringArrays.indexOf(buf, CRLF, p, length, false);
         if(q == -1) { return null; }
         
         int r = StringArrays.indexOf(buf, BOUNDARY_EQ, p, q, true);
         if(r == -1) { return null; } else { r += BOUNDARY_EQ.length; }
         
         int s = StringArrays.indexOf(buf, (byte)' ', r);
         if(s == -1 || s > q) { s = q; }
         
         if(buf[r] == (byte)'\"') { r++; }
         if(buf[s - 1] == (byte)';') { s--; }
         if((s - r) <= 0) { return null; }
         if(buf[s - 1] == (byte)'\"') { s--; }
         if((s - r) <= 0) { return null; }
         
         byte[] result = new byte[(s - r) + 2];
         result[0] = (byte)'-';
         result[1] = (byte)'-';
         System.arraycopy(buf, r, result, 2, s - r);
         
         return result;
     }
 
     /**
      * Parses the raw message body.
      * There will be a single entry in the content map that does not match the
      * type information described below.  This entry will have a key of
      * <code>Boolean.TRUE</code>, and an <code>Integer</code> value representing
      * the result of {@link MIMEInputStream#isPartComplete()} for the message.
      *
      * @param contentMap Map to populate with MessagePart-to-MessageContent data.
      * @param inputStream The stream to read the raw message from
      * @return The root message part.
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public static MimeMessagePart parseRawMessage(Hashtable contentMap, InputStream inputStream)
         throws IOException {
         MIMEInputStream mimeInputStream = null;
 
         try {
             mimeInputStream = new MIMEInputStream(inputStream);
         } catch (MIMEParsingException e) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("Unable to parse MIME encoded message: " + e.getMessage()).getBytes(),
                     EventLogger.WARNING);
             return null;
         } catch (ArrayIndexOutOfBoundsException e) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("Unable to parse MIME encoded message: " + e.getMessage()).getBytes(),
                     EventLogger.WARNING);
             return null;
         }
 
         contentMap.put(Boolean.TRUE, new Integer(mimeInputStream.isPartComplete()));
         
         MimeMessagePart rootPart = getMessagePart(contentMap, mimeInputStream);
 
         return rootPart;
     }
 
     /**
      * Recursively walk the provided MIMEInputStream, building a message
      * tree in the process.
      *
      * @param contentMap Map to populate with MessagePart-to-MessageContent data.
      * @param mimeInputStream MIMEInputStream of the downloaded message data
      * @return Root MessagePart element for this portion of the message tree
      */
     private static MimeMessagePart getMessagePart(Hashtable contentMap, MIMEInputStream mimeInputStream)
         throws IOException {
         // Parse out the MIME type and relevant header fields
         String mimeType = mimeInputStream.getContentType();
         String type = mimeType.substring(0, mimeType.indexOf('/'));
         String subtype = mimeType.substring(mimeType.indexOf('/') + 1);
         String encoding = mimeInputStream.getHeader("Content-Transfer-Encoding");
         String charset = mimeInputStream.getContentTypeParameter("charset");
         String name = StringParser.parseEncodedHeader(mimeInputStream.getContentTypeParameter("name"), false);
         String disposition = mimeInputStream.getHeader("Content-Disposition");
         String contentId = mimeInputStream.getHeader("Content-ID");
 
         // Default parameters used when headers are missing
         if (encoding == null) {
             encoding = "7bit";
         }
 
         // Clean up the disposition field
         if(disposition != null) {
 	        int p = disposition.indexOf(';');
 	        if(p != -1) {
 	        	disposition = disposition.substring(0, p);
 	        }
         	disposition = disposition.toLowerCase();
         }
         
         // Handle the multi-part case
         if (mimeInputStream.isMultiPart() &&
                 type.equalsIgnoreCase("multipart")) {
             MimeMessagePart part = MimeMessagePartFactory.createMimeMessagePart(
             		type, subtype, null, null, null, null, null, -1);
             MIMEInputStream[] mimeSubparts = mimeInputStream.getParts();
 
             for (int i = 0; i < mimeSubparts.length; i++) {
                 MimeMessagePart subPart = getMessagePart(contentMap, mimeSubparts[i]);
 
                 if (subPart != null) {
                     ((MultiPart) part).addPart(subPart);
                 }
             }
 
             return part;
         }
         // Handle the single-part case
         else {
             // Decode the data if the part is complete or indeterminate (as is
             // common if this is the message's only part), or is a text part
             // where partial decoding still yields usable output.
             byte[] buffer;
             if(mimeInputStream.isPartComplete() != 0
                     || type.equalsIgnoreCase(TextPart.TYPE)) {
                 buffer = readRawData(mimeInputStream);
             }
             else {
                 buffer = null;
             }
             
             MimeMessagePart part = MimeMessagePartFactory.createMimeMessagePart(
                     type, subtype, name, encoding, charset, disposition, contentId,
                     (buffer != null) ? buffer.length : 0);
 
             if(buffer != null && buffer.length > 0) {
                 try {
                     MimeMessageContent content = MimeMessageContentFactory.createContentEncoded(part, buffer);
                     content.setPartComplete(mimeInputStream.isPartComplete());
                     contentMap.put(part, content);
                 } catch (UnsupportedContentException e) {
                     EventLogger.logEvent(AppInfo.GUID,
                             ("UnsupportedContentException: " + e.getMessage()).getBytes(),
                             EventLogger.WARNING);
                 }
             }
             return part;
         }
     }
     
     private static byte[] readRawData(MIMEInputStream mimeInputStream) throws IOException {
         byte[] buffer;
         SharedInputStream sis = mimeInputStream.getRawMIMEInputStream();
         buffer = IOUtilities.streamToBytes(sis);
 
         int offset = 0;
         while (((offset + 3) < buffer.length) &&
                 !((buffer[offset] == '\r') &&
                         (buffer[offset + 1] == '\n') &&
                         (buffer[offset + 2] == '\r') &&
                         (buffer[offset + 3] == '\n'))) {
             offset++;
         }
         offset += 4;
 
         try {
             return Arrays.copy(buffer, offset, buffer.length - offset);
         } catch (IndexOutOfBoundsException e) {
             return new byte[0];
         }
     }    
 }
