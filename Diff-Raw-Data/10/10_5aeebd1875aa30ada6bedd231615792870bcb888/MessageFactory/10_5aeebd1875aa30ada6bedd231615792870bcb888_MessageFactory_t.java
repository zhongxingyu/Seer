 package org.bitrepository.protocol;
 
 import java.io.StringReader;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MessageFactory {
 	private static final Logger log = LoggerFactory.getLogger(MessageFactory.class);
 
 	private MessageFactory () {
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T createMessage(Class<T> messageClass, String xmlMessage) throws JAXBException {
 		try {
 			JAXBContext context = JAXBContext.newInstance(
			"org.bitrepository.bitrepositorymessages:org.bitrepository.bitrepositoryelements:org.bitrepository.bitrepositorydata");
 			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (T) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlMessage)));
 		} catch(JAXBException jex) {
 			log.error("Failed to create message object from string: {}", xmlMessage);
 			throw jex;
 		}
 	}
 }
