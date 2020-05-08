 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
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
 
 import org.apache.servicemix.expression.Expression;
 import org.apache.servicemix.expression.PropertyExpression;
 import org.apache.servicemix.jbi.NoMessageContentAvailableException;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.w3c.dom.Node;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 /**
  * A default file transformer which assumes the file is already in XML format and
  * requires no transformation other than to be wrapped in a normalized message..
  *
  * @version $Revision$
  */
 public class DefaultFileMarshaler extends MarshalerSupport implements FileMarshaler {
 
     public static final String FILE_NAME_PROPERTY = "org.apache.servicemix.file.name";
     public static final String FILE_PATH_PROPERTY = "org.apache.servicemix.file.path";
     public static final String FILE_CONTENT = "org.apache.servicemix.file.content";
 
     protected static final PropertyExpression FILE_NAME_EXPRESSION = new PropertyExpression(FILE_NAME_PROPERTY);
     protected static final PropertyExpression FILE_CONTENT_EXPRESSION = new PropertyExpression(FILE_CONTENT);
 
     private Expression fileName = FILE_NAME_EXPRESSION;
     private Expression content = FILE_CONTENT_EXPRESSION;
 
     public void readMessage(MessageExchange exchange, NormalizedMessage message, InputStream in, String path) throws IOException, JBIException {
         message.setContent(new StreamSource(in, path));
         message.setProperty(FILE_NAME_PROPERTY, new File(path).getName());
         message.setProperty(FILE_PATH_PROPERTY, path);
     }
 
     public String getOutputName(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
         return asString(fileName.evaluate(exchange, message));
     }
 
     public void writeMessage(MessageExchange exchange, NormalizedMessage message, OutputStream out, String path) throws IOException, JBIException {
         try {
             Object value = content.evaluate(exchange, message);
             if (value != null) {
                 writeValue(value, out);
             }
             else {
                 writeMessageContent(exchange, message, out, path);
             }
         }
         catch (IOException e) {
             throw new MessagingException(e);
         }
     }
 
     // Properties
     //-------------------------------------------------------------------------
     public Expression getContent() {
         return content;
     }
 
     public void setContent(Expression content) {
         this.content = content;
     }
 
     public Expression getFileName() {
         return fileName;
     }
 
     public void setFileName(Expression fileName) {
         this.fileName = fileName;
     }
 
     // Implementation methods
     //-------------------------------------------------------------------------
 
     /**
      * Writes the given value to the output stream
      *
      * @param value the value to be written to the stream
      * @param out   the output stream
      */
     protected void writeValue(Object value, OutputStream out) throws IOException, MessagingException {
         if (value instanceof String) {
             OutputStreamWriter writer = new OutputStreamWriter(out);
             writer.write((String) value);
             writer.flush();
         }
         else if (value instanceof byte[]) {
             out.write((byte[]) value);
         }
         else {
             ObjectOutputStream objectOut = new ObjectOutputStream(out);
             objectOut.writeObject(value);
         }
     }
 
     /**
      * Writes the message content to the given output stream
      *
      * @param message the message who's content we are about to write
      * @param out     the destination of the output
     * @param name    the name of the output resource (file, uri, url)
      */
     protected void writeMessageContent(MessageExchange exchange, NormalizedMessage message, OutputStream out, String path) throws MessagingException {
         Source content = null;
         Node document = (Node) message.getProperty(SourceTransformer.CONTENT_DOCUMENT_PROPERTY);
         if (document != null) {
             content = new DOMSource(document);
         }
         else {
             content = message.getContent();
         }
         if (content == null) {
             throw new NoMessageContentAvailableException(exchange);
         }
         try {
         	getTransformer().toResult(content, new StreamResult(out));
         } catch (TransformerException e) {
         	throw new MessagingException(e);
         }
     }
 }
