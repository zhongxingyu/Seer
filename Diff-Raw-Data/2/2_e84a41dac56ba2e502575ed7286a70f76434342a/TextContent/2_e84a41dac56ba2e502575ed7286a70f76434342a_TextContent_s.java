 /*-
  * Copyright (c) 2009, Derek Konigsberg
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
 import java.io.UnsupportedEncodingException;
 
 import org.logicprobe.LogicMail.util.StringFactory;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * Represents message content of the text type.
  * It is assumed that the raw (byte[]) form of text content is UTF-8.
  */
 public class TextContent extends MimeMessageContent {
     private static String UTF_8 = "UTF-8";
     private String text;
 	private byte[] rawData;
 	
 	public TextContent(TextPart textPart, String text) {
 		super(textPart);
 		this.text = text;
 	}
 	
     public TextContent(
     		TextPart textPart,
             String encoding,
             String charset,
             byte[] data) throws UnsupportedContentException {
     	super(textPart);
         // Check for any encodings that need to be handled
         if (encoding.equalsIgnoreCase(ENCODING_QUOTED_PRINTABLE)) {
             this.rawData = StringParser.decodeQuotedPrintableBytes(data);
         }
         else if (encoding.equalsIgnoreCase(ENCODING_BASE64)) {
             try {
                 this.rawData = decodeBase64(data);
             } catch (IOException exp) {
                 throw new UnsupportedContentException("Unable to decode");
             }
         }
         else {
             this.rawData = data;
         }
         
         // Create a string based on the raw data
         try {
             this.text = StringFactory.create(rawData, charset);
         } catch (UnsupportedEncodingException e) {
             // If encoding type is bad, use the default platform charset.
             // This may result in the user seeing garbage, but at least
             // they'll know there was a decoding problem.
             this.text = new String(rawData);
         }
     }
     
     public TextContent(TextPart textPart, byte[] rawData, boolean decode) {
         super(textPart);
         if(decode) {
             putRawData(rawData);
         }
         else {
             this.rawData = rawData;
         }
     }
 	
     /**
      * Instantiates a new text content object for deserialization.
      */
     public TextContent() {
 		super(null);
 	}
     
 	/**
 	 * Find out if a content object can be created for the provided
 	 * MIME structure part.
 	 * @param textPart MIME part to check.
 	 * @return True if the content type is supported, false otherwise.
 	 */
 	public static boolean isPartSupported(TextPart textPart) {
 		String mimeSubtype = textPart.getMimeSubtype();
         return (mimeSubtype.equalsIgnoreCase(TextPart.SUBTYPE_PLAIN) ||
                 mimeSubtype.equalsIgnoreCase(TextPart.SUBTYPE_HTML));
 	}
 	
 	public void accept(MimeMessageContentVisitor visitor) {
 		visitor.visit(this);
 	}
 	
 	public String getText() {
 		return this.text;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.message.MessageContent#getRawData()
 	 */
 	public byte[] getRawData() {
 	    byte[] result;
 	    if(rawData != null) {
 	        result = this.rawData;
 	    }
 	    else {
 	        try {
 	            result = this.text.getBytes(UTF_8);
             } catch (UnsupportedEncodingException e) {
                 result = this.text.getBytes();
             }
 	    }
 	    return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.message.MimeMessageContent#putRawData(byte[])
 	 */
 	protected void putRawData(byte[] rawData) {
 	    this.rawData = rawData;
 		try {
            this.text = new String(rawData, ((TextPart)getMessagePart()).getCharset());
         } catch (UnsupportedEncodingException e) {
             this.text = new String(rawData);
         }
 	}
 }
