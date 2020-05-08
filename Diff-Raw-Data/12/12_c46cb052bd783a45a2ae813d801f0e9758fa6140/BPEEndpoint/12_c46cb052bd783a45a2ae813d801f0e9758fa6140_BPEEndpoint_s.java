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
 package org.apache.servicemix.bpe;
 
 import java.util.Map;
 
 import javax.jbi.component.ComponentContext;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.Fault;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.InOptionalOut;
 import javax.jbi.messaging.InOut;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.jbi.messaging.RobustInOnly;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.wsdl.Operation;
 import javax.wsdl.PortType;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.ode.bpe.bped.EventDirector;
 import org.apache.ode.bpe.client.IFormattableValue;
 import org.apache.ode.bpe.event.BPELStaticKey;
 import org.apache.ode.bpe.event.IResponseMessage;
 import org.apache.ode.bpe.event.SimpleRequestMessageEvent;
 import org.apache.ode.bpe.interaction.IInteraction;
 import org.apache.ode.bpe.interaction.InteractionException;
 import org.apache.ode.bpe.interaction.InvocationFactory;
 import org.apache.ode.bpe.interaction.XMLInteractionObject;
 import org.apache.ode.bpe.scope.service.BPRuntimeException;
 import org.apache.servicemix.common.Endpoint;
 import org.apache.servicemix.common.ExchangeProcessor;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.w3c.dom.Document;
 
 public class BPEEndpoint extends Endpoint implements ExchangeProcessor {
 
     protected ServiceEndpoint activated;
     protected DeliveryChannel channel;
     protected SourceTransformer transformer = new SourceTransformer();
 	
     private static final ThreadLocal ENDPOINT = new ThreadLocal();
     
     public static BPEEndpoint getCurrent() {
         return (BPEEndpoint) ENDPOINT.get();
     }
     
     public static void setCurrent(BPEEndpoint endpoint) {
         ENDPOINT.set(endpoint);
     }
     
 	public Role getRole() {
 		return Role.PROVIDER;
 	}
 
 	public void activate() throws Exception {
         logger = this.serviceUnit.getComponent().getLogger();
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         activated = ctx.activateEndpoint(service, endpoint);
         channel = ctx.getDeliveryChannel();
 	}
 
 	public void deactivate() throws Exception {
         ServiceEndpoint ep = activated;
         activated = null;
         ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
         ctx.deactivateEndpoint(ep);
 	}
 
 	public ExchangeProcessor getProcessor() {
 		return this;
 	}
 
 	public void process(MessageExchange exchange) throws Exception {
         if (exchange.getStatus() == ExchangeStatus.DONE) {
             return;
         } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
             return;
         }
         
         String inputPartName = BPEComponent.PART_PAYLOAD;
         String outputPartName = BPEComponent.PART_PAYLOAD;
         if (exchange.getOperation() != null) {
             PortType pt = getDefinition().getPortType(getInterfaceName());
             Operation oper = pt.getOperation(exchange.getOperation().getLocalPart(), null, null);
             if (oper.getInput() != null && oper.getInput().getMessage() != null) {
                 Map parts = oper.getInput().getMessage().getParts();
                 inputPartName = (String) parts.keySet().iterator().next(); 
             }
             if (oper.getOutput() != null && oper.getOutput().getMessage() != null) {
                 Map parts = oper.getOutput().getMessage().getParts();
                 outputPartName = (String) parts.keySet().iterator().next(); 
             }
         }
         
         
 		BPELStaticKey bsk = new BPELStaticKey();
 		bsk.setTargetNamespace(getInterfaceName().getNamespaceURI());
 		bsk.setPortType(getInterfaceName().getLocalPart());
 		if (exchange.getOperation() != null) {
 			bsk.setOperation(exchange.getOperation().getLocalPart());
 		}
 		SimpleRequestMessageEvent msg = new SimpleRequestMessageEvent();
 		msg.setStaticKey(bsk);
 		XMLInteractionObject interaction = new XMLInteractionObject();
 		interaction.setDocument(transformer.toDOMDocument(exchange.getMessage("in")));
 		msg.setPart(inputPartName, interaction);
         
         EventDirector ed = ((BPEComponent) getServiceUnit().getComponent()).getEventDirector();
         try {
             IResponseMessage response;
             try {
                 BPEEndpoint.setCurrent(this);
                 response = ed.sendEvent(msg, true);
             } finally {
                 BPEEndpoint.setCurrent(null);
             }
             IInteraction payload = response.getPart(outputPartName);
             if (response.getFault() != null) {
                 Exception e = response.getFault().getFaultException();
                 if (e != null) {
                     throw e;
                 }
                 // TODO: handle simple fault
                 throw new BPRuntimeException(response.getFault().getFaultString(), "");
             } else if (exchange instanceof InOnly || exchange instanceof RobustInOnly) {
                 if (payload != null) {
                     throw new UnsupportedOperationException("Did not expect return value for in-only or robust-in-only");
                 }
                 exchange.setStatus(ExchangeStatus.DONE);
                 channel.send(exchange);
             } else if (exchange instanceof InOptionalOut) {
                 if (payload == null) {
                     exchange.setStatus(ExchangeStatus.DONE);
                     channel.send(exchange);
                 } else {
                     NormalizedMessage out = exchange.createMessage();
                     out.setContent(new DOMSource(getDocument(payload)));
                     exchange.setMessage(out, "out");
                     channel.send(exchange);
                 }
             } else if (exchange instanceof InOut) {
                 if (payload == null) {
                     throw new UnsupportedOperationException("Expected return data for in-out"); 
                 }
                 NormalizedMessage out = exchange.createMessage();
                 out.setContent(new DOMSource(getDocument(payload)));
                 exchange.setMessage(out, "out");
                 channel.send(exchange);
             } else {
                 throw new UnsupportedOperationException("Unhandled mep: " + exchange.getPattern());
             }
         } catch (BPRuntimeException e) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Exception caught", e);
             }
            IInteraction payload = (IInteraction) e.getPartMessage(BPEComponent.PART_PAYLOAD);
            if (payload != null) {
                 Fault fault = exchange.createFault();
                fault.setContent(new DOMSource(getDocument(payload)));
                 exchange.setFault(fault);
             } else {
                 exchange.setError(e);
             }
             channel.send(exchange);
         }
 	}
 
 	public void start() throws Exception {
 	}
 
 	public void stop() throws Exception {
 	}
 	
 	protected Document getDocument(IInteraction interaction) throws InteractionException {
 		Object obj = interaction.invoke(InvocationFactory.newInstance().createGetObjectInvocation());
         if (obj instanceof Document) {
         	return (Document) obj;
         } else if (obj instanceof IFormattableValue) {
         	return (Document) ((IFormattableValue) obj).getValueAs(Document.class);
         } else {
         	throw new IllegalStateException("Unable to handle object of type: " + obj.getClass().getName());
         }
 	}
 
 
 }
