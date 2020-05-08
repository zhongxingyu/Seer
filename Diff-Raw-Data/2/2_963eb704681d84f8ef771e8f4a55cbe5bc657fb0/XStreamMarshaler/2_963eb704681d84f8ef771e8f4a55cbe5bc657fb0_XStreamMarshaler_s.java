 /**
  *
  * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **/
 package org.servicemix.components.util.xstream;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomReader;
 import com.thoughtworks.xstream.io.xml.DomWriter;
 
 import org.logicblaze.lingo.LingoInvocation;
 import org.servicemix.jbi.jaxp.SourceTransformer;
 import org.servicemix.jbi.jaxp.StringSource;
 import org.servicemix.jbi.messaging.DefaultMarshaler;
 import org.servicemix.jbi.messaging.Marshaler;
 import org.w3c.dom.Document;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 /**
  * A {@link Marshaler} for <a href="http://xStream.codehaus.org/">XStream</a> which
  * streams the object to an a W3C DOM Document so that other components can access
  * the XML without an extra parse.
  *
  * @version $Revision$
  */
 public class XStreamMarshaler extends DefaultMarshaler {
     private XStream xStream;
     private SourceTransformer transformer = new SourceTransformer();
     private boolean useDOM = false;
 
     public void marshal(MessageExchange exchange, NormalizedMessage message, Object body) throws MessagingException {
         if (useDOM) {
             try {
                 Document document = transformer.createDocument();
                 getXStream().marshal(body, new DomWriter(document));
                 message.setContent(new DOMSource(document));
             }
             catch (ParserConfigurationException e) {
                 throw new MessagingException("Failed to marshal: " + body + " to DOM document: " + e, e);
             }
         }
         else {
             String xml = getXStream().toXML(body);
             message.setContent(new StringSource(xml));
         }
     }
 
     public Object unmarshal(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
         Source content = message.getContent();
         if (content != null) {
             if (content instanceof StreamSource) {
                 StreamSource source = (StreamSource) content;
                return getXStream().fromXML(source.getReader(), source.getSystemId());
             }
             Document document = null;
             if (content instanceof DOMSource) {
                 DOMSource domSource = (DOMSource) content;
                 document = (Document) domSource.getNode();
             }
             else {
                 DOMResult result = new DOMResult();
                 try {
                     transformer.toResult(content, result);
                 }
                 catch (TransformerException e) {
                     throw new MessagingException("Failed to transform content: " + content + " to DOMResult: " + e, e);
                 }
                 document = (Document) result.getNode();
             }
             return getXStream().unmarshal(new DomReader(document));
         }
         return super.unmarshal(exchange, message);
     }
 
     // Properties
     //-------------------------------------------------------------------------
     public XStream getXStream() {
         if (xStream == null) {
             xStream = createXStream();
         }
         return xStream;
     }
 
     public void setXStream(XStream xStream) {
         this.xStream = xStream;
     }
 
     public SourceTransformer getTransformer() {
         return transformer;
     }
 
     public void setTransformer(SourceTransformer transformer) {
         this.transformer = transformer;
     }
 
     /**
      * Whether or not DOM should be used for marshalling XML - which is preferable if another component
      * in the pipeline will want to parse the body.
      *
      * @return
      */
     public boolean isUseDOM() {
         return useDOM;
     }
 
     /**
      * Enables DOM output when marshalling in case other components in the pipeline wish to perform
      * parsing.
      *
      * @param useDOM
      */
     public void setUseDOM(boolean useDOM) {
         this.useDOM = useDOM;
     }
 
     protected XStream createXStream() {
         XStream answer = new XStream();
         answer.alias("invoke", LingoInvocation.class);
         return answer;
     }
 
 }
