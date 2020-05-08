 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
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
 
 package org.cipango;
 
 import java.util.Iterator;
 
 import javax.servlet.sip.SipServletMessage.HeaderForm;
 
 import org.mortbay.io.Buffer;
 
 public class SipGenerator 
 {
 	public static final int
 		STATE_START_LINE = 0,
 		STATE_HEADER = 1,
 		STATE_CONTENT = 2;
 	
 	public static final String UNKNOWN_REASON = "Unknown";
 	
 	private static byte[] SERVER = "Server: cipango(2.0.x)\r\n".getBytes();
 	private static byte[] USER_AGENT = "User-Agent: cipango(2.0.x)\r\n".getBytes();
 
     private static byte[] CONTENT_LENGTH_0 = "Content-Length: 0\r\n".getBytes();
     private static byte[] CONTENT_LENGTH_0_COMPACT = "l: 0\r\n".getBytes();
     
     public static void setServerVersion(String version) 
     {
     	SERVER = new String("Server: cipango(" + version + ")\r\n").getBytes();
     	USER_AGENT = new String("User-Agent: cipango(" + version + ")\r\n").getBytes();
     }
     
 	public SipGenerator() { }
 	
 	public void generate(Buffer buffer, SipMessage message) 
 	{
 		if (message.isRequest())
 			generateRequest(buffer, (SipRequest) message);
 		else
 			generateResponse(buffer, (SipResponse) message);
 	}
 
 	public void generateRequest(Buffer buffer, SipRequest request) 
 	{		
 		buffer.put(SipMethods.CACHE.lookup(request.getMethod()));
 		buffer.put(SipGrammar.SPACE);
 		byte[] b = request.getRequestURIAsString().getBytes(); // TODO buffer
         buffer.put(b, 0, b.length);
 		buffer.put(SipGrammar.SPACE);
 		buffer.put(SipVersions.SIP_2_0_BUFFER);
 		buffer.put(SipGrammar.CRLF);
 		
 		generateHeader(buffer, request.getFields(), false, request.getHeaderForm());
 		
 		if (request.getRawContent() != null) 
 			buffer.put(request.getRawContent());
 	}
 
 	public void generateResponse(Buffer buffer, SipResponse response) 
 	{
 		int status = response.getStatus();
		String reason = response.getReason();
 		
         Buffer line = SipStatus.getResponseLine(status);
         
         if (line != null && reason == null)
         { 
         	buffer.put(line);
         } 
         else 
         {
         	if (reason == null) 
         		reason = UNKNOWN_REASON;
         	
         	if (line == null) 
         	{
         		buffer.put(SipVersions.SIP_2_0_BUFFER);
                 buffer.put(SipGrammar.SPACE);
                 buffer.put((byte) ('0' + status / 100));
                 buffer.put((byte) ('0' + (status % 100) / 10));
                 buffer.put((byte) ('0' + (status % 10)));
                 buffer.put(SipGrammar.SPACE);
                 byte[] r = reason.getBytes();
                 buffer.put(r, 0, r.length);
                 buffer.put(SipGrammar.CRLF);
         	} 
         	else 
         	{
         		buffer.put(line.array(), 0, SipVersions.SIP_2_0_BUFFER.length() + 5);
                 byte[] r = reason.getBytes();
                 buffer.put(r, 0, r.length);
                 buffer.put(SipGrammar.CRLF);
         	}
         }
         
         generateHeader(buffer, response.getFields(), true, response.getHeaderForm());
         
         if (response.getRawContent() != null) 
 			buffer.put(response.getRawContent());
     }
 	
     protected void generateHeader(Buffer buffer, SipFields fields, boolean response, HeaderForm form) 
     {
         long contentLength = -1;
             
         if (fields != null) 
         {
         	Iterator<SipFields.Field> it = fields.getFields();
         	while (it.hasNext()) 
         	{	
         		SipFields.Field field = it.next();
         		
         		switch (field.getNameOrdinal()) 
         		{
                 case SipHeaders.CONTENT_LENGTH_ORDINAL:
                     contentLength = field.getLong();
                     break;
         		}
         		
         		boolean merge = false;
         		if (field.getNameOrdinal() != -1)
         			merge = SipHeaders.__types[field.getNameOrdinal()].isMerge();
         		
         		SipFields.put(field, buffer, form, merge);
         	}
         }
         
         if (contentLength == -1)
         {
         	if (form == HeaderForm.COMPACT)
                 buffer.put(CONTENT_LENGTH_0_COMPACT);
         	else
                 buffer.put(CONTENT_LENGTH_0);
         }
         
         buffer.put(SipGrammar.CRLF);
     }
 
 }
