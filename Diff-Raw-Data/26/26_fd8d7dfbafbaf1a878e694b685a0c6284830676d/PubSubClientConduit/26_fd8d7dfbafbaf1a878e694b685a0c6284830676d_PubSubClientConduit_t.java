 package org.apache.cxf.transport.xmpp.pubsub;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.cxf.io.CachedOutputStream;
 import org.apache.cxf.message.Message;
 import org.apache.cxf.transport.xmpp.common.AbstractConduit;
 import org.apache.cxf.transport.xmpp.smackx.soap.SoapPacket;
 import org.apache.cxf.ws.addressing.EndpointReferenceType;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
 
 public class PubSubClientConduit extends AbstractConduit {
     
     private LeafNode targetNode;
     
     public PubSubClientConduit(EndpointReferenceType type) {
         super(type);
     }
     
     public void setNode(LeafNode node) {
         targetNode = node;
     }
 
     @Override
     public void prepare(Message msg) throws IOException {
         super.prepare(msg);
         msg.getExchange().setOneWay(true);
     }
     
     @Override
     public void close(Message msg) throws IOException {
         CachedOutputStream output = (CachedOutputStream)msg.getContent(OutputStream.class);
         if (targetNode != null && output != null) {
             StringBuilder soapEnvelope = new StringBuilder();
             output.writeCacheTo(soapEnvelope);
 
//            SoapPacket soapOverXmpp = new SoapPacket();
//            soapOverXmpp.setEnvelope(soapEnvelope.toString());
             
            SimplePayload payload = new SimplePayload("Envelope", "http://www.w3.org/2003/05/soap-envelope", soapEnvelope.toString());
            
            PayloadItem<SimplePayload> pi = new PayloadItem<SimplePayload>(payload);
             try {
                 targetNode.send(pi);
             } catch (XMPPException e) {
                 throw new IOException(e);
             }
         }
     }
 }
