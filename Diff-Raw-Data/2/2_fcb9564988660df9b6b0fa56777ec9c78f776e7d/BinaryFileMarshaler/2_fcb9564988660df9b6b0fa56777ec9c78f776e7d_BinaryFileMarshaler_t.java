 /*
  * Copyright 2005-2006 The Apache Software Foundation.
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
 package org.apache.servicemix.components.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.jbi.JBIException;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.apache.servicemix.jbi.util.StreamDataSource;
 
 /**
  * A FileMarshaler that converts the given input stream into a binary attachment.
  * 
  * @author Guillaume Nodet
  * @since 3.0
  */
 public class BinaryFileMarshaler extends DefaultFileMarshaler {
 
 	private String attachment = "content";
 	private String contentType = null;
 
 	public String getAttachment() {
 		return attachment;
 	}
 
 	public void setAttachment(String attachment) {
 		this.attachment = attachment;
 	}
 	
 	public String getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
     public void readMessage(MessageExchange exchange, NormalizedMessage message, InputStream in, String path) throws IOException, JBIException {
     	DataSource ds = new StreamDataSource(in, contentType);
     	DataHandler handler = new DataHandler(ds);
     	message.addAttachment(attachment, handler);
         message.setProperty(FILE_NAME_PROPERTY, new File(path).getName());
         message.setProperty(FILE_PATH_PROPERTY, path);
     }
 
 	public void writeMessage(MessageExchange exchange, NormalizedMessage message, OutputStream out, String path) throws IOException, JBIException {
 		DataHandler handler = message.getAttachment(attachment);
		if (handler == null) {
 			throw new MessagingException("Could not find attachment: " + attachment);
 		}
 		InputStream is = handler.getInputStream();
 		FileUtil.copyInputStream(is, out);
 	}
 
 }
