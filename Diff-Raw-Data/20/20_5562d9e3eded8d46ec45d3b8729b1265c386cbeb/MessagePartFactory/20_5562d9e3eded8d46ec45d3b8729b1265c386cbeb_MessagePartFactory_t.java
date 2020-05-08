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
 
 package org.logicprobe.LogicMail.message;
 
 import java.io.IOException;
 import net.rim.device.api.io.Base64InputStream;
 import net.rim.device.api.system.EncodedImage;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * Creates message parts, doing the necessary decoding.
  */
 public class MessagePartFactory {
     /**
      * Create a new message part
      * @param mimeType MIME type
      * @param mimeSubType MIME subtype
      * @param encoding Encoding type (i.e. 7bit, base64)
      * @param param Type-specific parameter (i.e. charset, filename)
      * @param data Actual text data for the section
      */
     public static MessagePart createMessagePart(String mimeType,
                                                 String mimeSubtype,
                                                 String encoding,
                                                 String param,
                                                 String data)
     {
         if(mimeType.equalsIgnoreCase("multipart"))
             return createMultiPart(mimeSubtype);
         else if(mimeType.equalsIgnoreCase("text"))
             return createTextPart(mimeSubtype, encoding, param, data);
         else if(mimeType.equalsIgnoreCase("image"))
             return createImagePart(mimeSubtype, encoding, data);
         else
             return createUnsupportedPart(mimeType, mimeSubtype, data);
     }
     
     /**
      * Find out if a particular message part type is supported
      * without having to create it.  This is useful to optimize
      * downloads on protocols that support selective retrieval
      * of message parts.
      * Right now the supported subtypes are hard-coded, but they
      * should ultimately be programmatically determined upon
      * initialization of this factory.
      *
      * @param mimeType MIME type
      * @param mimeSubtype MIME subtype
      * @return True if supported, false if unsupported
      */
     public static boolean isMessagePartSupported(String mimeType, String mimeSubtype) {
         if(mimeType.equalsIgnoreCase("multipart"))
             return isMultiPartSupported(mimeSubtype);
         else if(mimeType.equalsIgnoreCase("text"))
             return isTextPartSupported(mimeSubtype);
         else if(mimeType.equalsIgnoreCase("image"))
             return isImagePartSupported(mimeSubtype);
         else
             return false;
     }
     
     private static MessagePart createMultiPart(String mimeSubtype) {
         return new MultiPart(mimeSubtype);
     }
     
     private static MessagePart createTextPart(String mimeSubtype, String encoding, String charset, String data) {
         // Check for any encodings that need to be handled
         if(encoding.equalsIgnoreCase("quoted-printable")) {
             data = StringParser.decodeQuotedPrintable(data);
         }
         else if(encoding.equalsIgnoreCase("base64")) {
             try {
                 byte[] textBytes = Base64InputStream.decode(data);
                 data = new String(textBytes, StringParser.parseValidCharsetString(charset));
             }
             catch (IOException exp) {
                 return createUnsupportedPart("text", mimeSubtype, data);
             }
         }
         // Check for a supported text sub-type and decode if necessary
         if(mimeSubtype.equalsIgnoreCase("plain")) {
             TextPart textPart = new TextPart(mimeSubtype, data);
             textPart.setCharset(charset);
             return textPart;
         }
         else {
             return createUnsupportedPart("text", mimeSubtype, data);
         }
     }
     
     private static MessagePart createImagePart(String mimeSubtype, String encoding, String data) {
         // Decode the binary data, and create an image
         if(encoding.equalsIgnoreCase("base64")) {
             try {
                 byte[] imgBytes = Base64InputStream.decode(data);
                 EncodedImage encImage =
                         EncodedImage.createEncodedImage(imgBytes, 0, imgBytes.length,
                                                         "image/"+mimeSubtype.toLowerCase());
                 return new ImagePart(mimeSubtype, encImage);
             } catch (Exception exp) {
                 return createUnsupportedPart("image", mimeSubtype, data);
             }
         }
         else {
             return createUnsupportedPart("image", mimeSubtype, data);
         }
     }
     
     private static MessagePart createUnsupportedPart(String mimeType, String mimeSubtype, String data) {
         UnsupportedPart part = new UnsupportedPart(mimeType, mimeSubtype);
         return part;
     }
 
     private static boolean isMultiPartSupported(String mimeSubtype) {
         return (mimeSubtype.equalsIgnoreCase("mixed") ||
                 mimeSubtype.equalsIgnoreCase("related") ||
                mimeSubtype.equalsIgnoreCase("alternative") ||
                mimeSubtype.equalsIgnoreCase("signed"));
     }
 
     private static boolean isTextPartSupported(String mimeSubtype) {
         return (mimeSubtype.equalsIgnoreCase("plain"));
     }
 
     private static boolean isImagePartSupported(String mimeSubtype) {
         return (mimeSubtype.equalsIgnoreCase("gif") ||
                 mimeSubtype.equalsIgnoreCase("png") ||
                 mimeSubtype.equalsIgnoreCase("vnd.wap.wbmp") ||
                 mimeSubtype.equalsIgnoreCase("jpeg") ||
                 mimeSubtype.equalsIgnoreCase("jpg") ||
                 mimeSubtype.equalsIgnoreCase("pjpeg") ||
                 mimeSubtype.equalsIgnoreCase("bmp") ||
                 mimeSubtype.equalsIgnoreCase("tif"));
     }
     
 }
