 package parser;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.sax.SAXSource;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 public class XmppParserMessage {
 	private ByteBuffer buf;
 	private int bytesRead;
	private JAXBContext context;
 	
	public XmppParserMessage(ByteBuffer buf, int bytesRead) throws JAXBException {
 		this.buf = buf;
 		this.bytesRead = bytesRead;
		context = JAXBContext.newInstance(jabber.client.Message.class.getPackage().getName());
 	}
 	
 //	public XmppMessage marshalResponse() throws JAXBException, IOException {
 //		JAXBContext context = JAXBContext.newInstance(XmppMessage.class);
 //	    // get variables from our xml file, created before
 //		ByteArrayInputStream bais = new ByteArrayInputStream(buf.array(), 0, bytesRead);
 //	    System.out.println();
 //	    System.out.println("Output from our XML File: ");
 //	    Unmarshaller um = context.createUnmarshaller();
 //	    XmppMessage xmppMessage = (XmppMessage) um.unmarshal(bais);
 //	    System.out.println("XMPP MESSAGE");
 //	    System.out.println("from: " + xmppMessage.toString());
 //	    return xmppMessage;
 //	}
 	
 	public <T> T unmarshal(Class<T> clazz) throws JAXBException {
 		ByteArrayInputStream bais = new ByteArrayInputStream(buf.array(), 0, bytesRead);
 	    Unmarshaller um = context.createUnmarshaller();
 	    @SuppressWarnings("unchecked")
 		T xmppMessage = (T) um.unmarshal(bais);
 		return xmppMessage;
 	}
 	
 	public <T> ByteBuffer marshal(T message, Class<T> clazz) throws JAXBException {
 		Marshaller m = context.createMarshaller();
 		m.setProperty( Marshaller.JAXB_FRAGMENT, Boolean.TRUE );
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		m.marshal(message, baos);
 		String str = baos.toString();
 		ByteBuffer buf = ByteBuffer.allocate(str.getBytes().length);
 		buf.put(str.getBytes());
 		return buf;
 	}
 }
