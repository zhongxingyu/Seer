 package util;
 
 import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
 
 import net.jxta.document.AdvertisementFactory;
 import net.jxta.document.Element;
 import net.jxta.document.StructuredDocument;
 import net.jxta.document.StructuredDocumentFactory;
 import net.jxta.document.XMLDocument;
 import net.jxta.endpoint.Message;
 import net.jxta.endpoint.MessageElement;
 import net.jxta.protocol.PeerAdvertisement;
 
 @SuppressWarnings("rawtypes")
 public class AkkaUtil {
 
 	
 	public static XMLDocument structuredDocumentToXmldocument(StructuredDocument strDoc){
 		return (XMLDocument)strDoc;
 	}
 	
 	public static PeerAdvertisement getConnectAdvertisement(Message msg){		
 		MessageElement elem = msg.getMessageElement("jxta", "Connect");
 		XMLDocument asDoc = null;
 		try {
 			asDoc = (XMLDocument) StructuredDocumentFactory.newStructuredDocument(elem);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return (PeerAdvertisement) AdvertisementFactory.newAdvertisement(asDoc);		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void append(XMLDocument doc, Element el){
 		doc.appendChild(el);
 	}
 	
	public static ChannelBuffer wrappedBuffer(ByteBuffer[] buffer){
		return ChannelBuffers.wrappedBuffer(buffer);
	}
	
 }
