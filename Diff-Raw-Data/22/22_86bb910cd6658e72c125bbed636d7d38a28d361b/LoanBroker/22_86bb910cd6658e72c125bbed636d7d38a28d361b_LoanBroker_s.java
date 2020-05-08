 /**
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package loanbroker;
 
import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.MessageExchangeListener;
 import org.apache.servicemix.components.util.ComponentSupport;
 
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.InOut;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.xml.namespace.QName;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class LoanBroker extends ComponentSupport implements MessageExchangeListener {
     
     private static final Log log = LogFactory.getLog(LoanBroker.class); 
 
     public LoanBroker() {
         super(new QName(Constants.LOANBROKER_NS, Constants.LOANBROKER_SERVICE), "input");
     }
     
     public void onMessageExchange(MessageExchange exchange) throws MessagingException {
         // Provider role
         if (exchange.getRole() == Role.PROVIDER) {
             processInputRequest(exchange);
         // Consumer role
         } else {
             ServiceEndpoint ep = exchange.getEndpoint();
             // Credit agency response
             if (ep.getServiceName().getLocalPart().equals(Constants.CREDITAGENCY_SERVICE)) {
                 processCreditAgencyResponse(exchange);
             } else if (ep.getServiceName().getLocalPart().equals(Constants.LENDERGATEWAY_SERVICE)) {
                 processLenderGatewayResponse(exchange);
             } else {
                 processLoanQuote(exchange);
             }
         }
     }
 
     private void processLoanQuote(MessageExchange exchange) throws MessagingException {
         log.info("Receiving loan quote");
         // Get aggregation
         String id = (String) getProperty(exchange, Constants.PROPERTY_CORRELATIONID);
         Aggregation ag = (Aggregation) aggregations.get(id);
         // Get info from quote
         LoanQuote q = new LoanQuote();
         q.bank = exchange.getEndpoint().getServiceName().getLocalPart();
         q.rate = (Double) getOutProperty(exchange, Constants.PROPERTY_RATE);
         done(exchange);
         // Check if all quotes are received
         synchronized (ag) {
             ag.quotes.add(q);
             if (ag.quotes.size() == ag.numbers) {
                 LoanQuote best = null;
                 for (Iterator iter = ag.quotes.iterator(); iter.hasNext();) {
                     q = (LoanQuote) iter.next();
                     if (best == null || q.rate.doubleValue() < best.rate.doubleValue()) {
                         best = q;
                     }
                 }
                 NormalizedMessage response = ag.request.createMessage();
                 response.setProperty(Constants.PROPERTY_RATE, best.rate);
                 response.setProperty(Constants.PROPERTY_BANK, best.bank);
                 ag.request.setMessage(response, "out");
                 send(ag.request);
                 aggregations.remove(id);
             }
         }
     }
 
     private void processLenderGatewayResponse(MessageExchange exchange) throws MessagingException {
         log.info("Receiving lender gateway response");
         // Get aggregation
         String id = (String) getProperty(exchange, Constants.PROPERTY_CORRELATIONID);
         Aggregation ag = (Aggregation) aggregations.get(id);
         QName[] recipients = (QName[]) getOutProperty(exchange, Constants.PROPERTY_RECIPIENTS);
         ag.numbers = recipients.length;
         for (int i = 0; i < recipients.length; i++) {
             InOut inout = createInOutExchange(recipients[i], null, null);
             inout.setProperty(Constants.PROPERTY_CORRELATIONID, id);
             NormalizedMessage msg = inout.createMessage();
             msg.setProperty(Constants.PROPERTY_SSN, ag.ssn);
             msg.setProperty(Constants.PROPERTY_AMOUNT, ag.amount);
             msg.setProperty(Constants.PROPERTY_DURATION, ag.duration);
             msg.setProperty(Constants.PROPERTY_SCORE, ag.score);
             msg.setProperty(Constants.PROPERTY_HISTORYLENGTH, ag.hlength);
             inout.setInMessage(msg);
             send(inout);
         }
         done(exchange);
     }
 
     private void processCreditAgencyResponse(MessageExchange exchange) throws MessagingException {
         log.info("Receiving credit agency response");
         // Get aggregation
         String id = (String) getProperty(exchange, Constants.PROPERTY_CORRELATIONID);
         Aggregation ag = (Aggregation) aggregations.get(id);
         // Fill with infos
         ag.score  = (Integer) getOutProperty(exchange, Constants.PROPERTY_SCORE);
         ag.hlength = (Integer) getOutProperty(exchange, Constants.PROPERTY_HISTORYLENGTH);
         // Send to lender gateway
         InOut inout = createInOutExchange(new QName(Constants.LOANBROKER_NS, Constants.LENDERGATEWAY_SERVICE), null, null);
         inout.setProperty(Constants.PROPERTY_CORRELATIONID, id);
         NormalizedMessage msg = inout.createMessage();
         msg.setProperty(Constants.PROPERTY_SCORE, ag.score);
         msg.setProperty(Constants.PROPERTY_HISTORYLENGTH, ag.hlength);
         msg.setProperty(Constants.PROPERTY_AMOUNT, ag.amount);
         inout.setInMessage(msg);
         send(inout);
         done(exchange);
     }
 
     private void processInputRequest(MessageExchange exchange) throws MessagingException {
         if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
             log.info("Receiving loan request");
             // Create aggregation
             String id = exchange.getExchangeId();
             Aggregation ag = new Aggregation();
             ag.request = exchange;
             ag.ssn = (String) getInProperty(exchange, Constants.PROPERTY_SSN);
             ag.amount = (Double) getInProperty(exchange, Constants.PROPERTY_AMOUNT);
             ag.duration = (Integer) getInProperty(exchange, Constants.PROPERTY_DURATION);
             aggregations.put(id, ag);
             
             InOut inout = createInOutExchange(new QName(Constants.LOANBROKER_NS, Constants.CREDITAGENCY_SERVICE), null, null);
             inout.setProperty(Constants.PROPERTY_CORRELATIONID, id);
             NormalizedMessage msg = inout.createMessage();
             msg.setProperty(Constants.PROPERTY_SSN, ag.ssn);
             inout.setInMessage(msg);
             send(inout);
         }
     }
     
     protected Object getProperty(MessageExchange me, String name) {
         return me.getProperty(name);
     }
     
     protected Object getInProperty(MessageExchange me, String name) {
         return me.getMessage("in").getProperty(name);
     }
     
     protected Object getOutProperty(MessageExchange me, String name) {
         return me.getMessage("out").getProperty(name);
     }
     
     private Map aggregations = new ConcurrentHashMap();
     
     public static class Aggregation {
         public MessageExchange request;
         public int numbers;
         public String ssn;
         public Double amount;
         public Integer duration;
         public Integer score;
         public Integer hlength;
         public List quotes = new ArrayList();
     }
     
     public static class LoanQuote {
         public String bank;
         public Double rate;
     }
 
 }
