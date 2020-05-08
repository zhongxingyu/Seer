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
 package gov.nih.nci.caxchange.servicemix.bean.target;
 
 import java.io.StringReader;
 
 import org.apache.servicemix.MessageExchangeListener;
 
 import javax.annotation.Resource;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessageExchangeFactory;
 import javax.jbi.messaging.NormalizedMessage;
 import org.apache.servicemix.jbi.util.MessageUtil;
 import javax.jbi.messaging.MessagingException;
 
 import javax.security.auth.Subject;
 
 import javax.xml.namespace.QName;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 
 import org.w3c.dom.Document;
 
 import org.xml.sax.InputSource;
 
 public class PscProcess implements MessageExchangeListener {
 
     @Resource
     private DeliveryChannel channel;

     static Logger logger = LogManager.getLogger(PscProcess.class);
 
    String businessResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"            <targetResponse>\n" +
"                <targetServiceIdentifier>targetServiceIdentifier2</targetServiceIdentifier>\n" +
"                <targetServiceOperation>targetServiceOperation2</targetServiceOperation>\n" +
"                <targetMessageStatus>RESPONSE</targetMessageStatus>\n" +
"                    <targetBusinessMessage>\n" +
"                        <xmlSchemaDefinition>http://www.oxygenxml.com/</xmlSchemaDefinition>\n" +
"                    </targetBusinessMessage>\n" +
 "            </targetResponse>";
 
     public void onMessageExchange(MessageExchange exchange) throws MessagingException {
         System.out.println("Received exchange: " + exchange);
         if ((exchange.getStatus()== ExchangeStatus.DONE)
             ||(exchange.getStatus()== ExchangeStatus.ERROR)){
 			return;
         }
         //sendResponse(exchange);
         //exchange.setStatus(ExchangeStatus.DONE);
         NormalizedMessage message = exchange.getMessage("in");
         Subject subject = message.getSecuritySubject();
         if (subject != null){
            logger.debug("Got the security subject "+subject);
         }
         NormalizedMessage out = exchange.createMessage();
         MessageUtil.transfer(message, out);
         Document document= null;
         try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(businessResponse)));
         }catch(Exception e){
            e.printStackTrace();
         }
         out.setContent(new DOMSource(document));
         exchange.setMessage(out, "out");
         channel.send(exchange);
     }
 
     public void sendResponse(MessageExchange exchange)throws MessagingException {
         try{
             MessageExchangeFactory mef = channel.createExchangeFactory();
             InOnly inOnly = mef.createInOnlyExchange();
             NormalizedMessage in = inOnly.createMessage();
             String corrId = (String)exchange.getProperty("org.apache.servicemix.correlationId");
             StringSource ss = new StringSource("<corrID>"+corrId+"</corrID>");
             in.setContent(ss);
             inOnly.setInMessage(in);
             inOnly.setService(new  QName("http://nci.nih.gov/caXchange","ResponseQueueService") );
             System.out.println("Sending "+inOnly);
             channel.sendSync(inOnly);
         }catch(Exception e) {
             System.out.println("Error sending response.");
             throw new MessagingException("Error sending response.",e);
         }
     }
 
 }
