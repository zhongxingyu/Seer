 package org.eclipse.swordfish.plugins.cxf.support;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jws.WebService;
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.cxf.io.CachedOutputStream;
 import org.apache.cxf.message.Exchange;
 import org.apache.cxf.message.Message;
 import org.apache.cxf.message.MessageImpl;
 import org.apache.cxf.service.model.BindingOperationInfo;
 import org.apache.cxf.service.model.InterfaceInfo;
 import org.apache.cxf.ws.addressing.EndpointReferenceType;
 import org.apache.cxf.wsdl.EndpointReferenceUtils;
 import org.apache.servicemix.cxf.transport.nmr.NMRConduit;
 import org.apache.servicemix.cxf.transport.nmr.NMRMessageHelper;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.nmr.api.Channel;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.api.Pattern;
 import org.apache.servicemix.nmr.api.Reference;
 import org.apache.servicemix.nmr.api.Status;
 
 public class NMROutputStream extends CachedOutputStream {
 
     private static final Log LOG = LogFactory.getLog(NMROutputStream.class);
 
     private Message message;
     private boolean isOneWay;
     private Channel channel;
     private NMRConduit conduit;
     private EndpointReferenceType target;
 
     public NMROutputStream(Message m, NMR nmr, EndpointReferenceType target,
                                   NMRConduit conduit) {
         message = m;
         this.channel = nmr.createChannel();
         this.conduit = conduit;
         this.target = target;
 
     }
 
     @Override
     protected void doFlush() throws IOException {
 
     }
 
     @Override
     protected void doClose() throws IOException {
         isOneWay = message.getExchange().isOneWay();
         sendOutputMessage();
         if (target != null) {
             target.getClass();
         }
         channel.close();
     }
 
     protected void sendOutputMessage() throws IOException {        try {
 
         	org.apache.servicemix.nmr.api.Exchange xchng = createNMRMessageExchange();
             Source source = message.getContent(Source.class);
             String contentSrc = new SourceTransformer().toString(source);
         	LOG.info("Sending message\n" + contentSrc);
 
             if (!isOneWay) {
 
             	channel.sendSync(xchng);
                 Source content = null;
                 if (xchng.getFault(false) != null) {
                     content = xchng.getFault().getBody(Source.class);
                 } else {
                     content = xchng.getOut().getBody(Source.class);
                 }
                 Message inMessage = new MessageImpl();
                 message.getExchange().setInMessage(inMessage);
                 InputStream ins = NMRMessageHelper.convertMessageToInputStream(content);
                 if (ins == null) {
                     throw new IOException("Unable to retrive message");
                 }
                 inMessage.setContent(InputStream.class, ins);
                 conduit.getMessageObserver().onMessage(inMessage);
 
                 xchng.setStatus(Status.Done);
                 channel.send(xchng);
             } else {
                 channel.sendSync(xchng);
             }
         } catch (IOException e) {
             throw e;
         } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw  new IOException(e.toString());
         }
     }
 
 	private org.apache.servicemix.nmr.api.Exchange createNMRMessageExchange()
 			throws IOException {
 		Member member = (Member) message.get(Method.class.getName());
 		Class<?> clz = member.getDeclaringClass();
 		Exchange exchange = message.getExchange();
 		BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
 
 		LOG.info("Invoking service" + clz);
 
 		WebService ws = clz.getAnnotation(WebService.class);
 		InterfaceInfo info = message.getExchange().get(InterfaceInfo.class);
 		QName interfaceName = null;
		if (ws != null && ws.name() != null && !ws.name().isEmpty()) {
 			interfaceName = new QName(ws.targetNamespace(), ws.name());
 		} else {
 			if (info != null) {
 				interfaceName = info.getName();
 			}
 		}
 		QName serviceName;
 		if (target != null) {
 		    serviceName = EndpointReferenceUtils.getServiceName(target, conduit.getBus());
 		} else {
 		    serviceName = message.getExchange().get(org.apache.cxf.service.Service.class).getName();
 		}
 
 		LOG.info("Create messageExchange" + serviceName);
 		org.apache.servicemix.nmr.api.Exchange xchng;
 		if (isOneWay) {
 		    xchng = channel.createExchange(Pattern.InOnly);
 		} else if (bop.getOutput() == null) {
 		    xchng = channel.createExchange(Pattern.RobustInOnly);
 		} else {
 		    xchng = channel.createExchange(Pattern.InOut);
 		}
 
 		org.apache.servicemix.nmr.api.Message inMsg = xchng.getIn();
 		LOG.info("Exchange endpoint " + serviceName);
 		LOG.info("setup message contents on " + inMsg);
 		inMsg.setBody(getMessageContent(message));
 		LOG.info("service for exchange " + serviceName);
 
 		Map<String,Object> refProps = new HashMap<String,Object>();
 		if (interfaceName != null) {
 			refProps.put(Endpoint.INTERFACE_NAME, interfaceName.toString());
 		}
 		refProps.put(Endpoint.SERVICE_NAME, serviceName.toString());
 		Reference ref = channel.getNMR().getEndpointRegistry().lookup(refProps);
 		xchng.setTarget(ref);
 		xchng.setOperation(bop.getName());
 		return xchng;
 	}
 
     private Source getMessageContent(Message message2) throws IOException {
         return new StreamSource(this.getInputStream());
 
     }
 
     @Override
     protected void onWrite() throws IOException {
 
     }
 
 }
