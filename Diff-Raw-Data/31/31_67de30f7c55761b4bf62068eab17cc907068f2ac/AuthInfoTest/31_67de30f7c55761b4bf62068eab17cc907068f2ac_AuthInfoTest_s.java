 package org.uddi.api_v3;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 import javax.xml.transform.stream.StreamSource;
 
 import static junit.framework.Assert.fail;
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 import org.uddi.api_v3.AuthToken;
 import org.uddi.api_v3.ObjectFactory;
 
 
 public class AuthInfoTest {
 
	private final static String EXPECTED_XML_FRAGMENT = "<fragment xmlns:ns3=\"urn:uddi-org:api_v3\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\">\n"
                                                        +"    <ns3:authInfo>AuthInfo String</ns3:authInfo>\n"
                                                        +"</fragment>";
 	private final static String UTF8_WORD = "メインページ";
	private final static String EXPECTED_UTF8_XML_FRAGMENT = "<fragment xmlns:ns3=\"urn:uddi-org:api_v3\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\">\n"
         +"    <ns3:authInfo>" + UTF8_WORD + "</ns3:authInfo>\n"
         +"</fragment>";
 	/**
 	 * Testing going from object to XML using JAXB using a XML Fragment.
 	 */
 	@Test 
 	public void marshall()
 	{
 		try {
 			JAXBContext jaxbContext=JAXBContext.newInstance("org.uddi.api_v3");
 			Marshaller marshaller = jaxbContext.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
 			ObjectFactory factory = new ObjectFactory();
 			AuthToken authToken = factory.createAuthToken();
 			authToken.setAuthInfo("AuthInfo String");
 			StringWriter writer = new StringWriter();
 			JAXBElement<AuthToken> element = new JAXBElement<AuthToken>(new QName("","fragment"),AuthToken.class,authToken);
 			marshaller.marshal(element,writer);
 			String actualXml=writer.toString();
			assertEquals(EXPECTED_XML_FRAGMENT, actualXml);
 		} catch (JAXBException jaxbe) {
 			fail("No exception should be thrown");
 		}
 	}
 	/**
 	 * Unmarshall an xml fragment.
 	 */
 	@Test 
 	public void unmarshall()
 	{
 		try {
 			JAXBContext jaxbContext=JAXBContext.newInstance("org.uddi.api_v3");
 			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(EXPECTED_XML_FRAGMENT);
 			JAXBElement<AuthToken> element = unMarshaller.unmarshal(new StreamSource(reader),AuthToken.class);
 			String infoString = element.getValue().getAuthInfo();
 			assertEquals("AuthInfo String", infoString);
 		} catch (JAXBException jaxbe) {
 			fail("No exception should be thrown");
 		}
 	}
 	/**
 	 * Test handling of utf8 characters
 	 */
 	@Test
 	public void marshallUTF8()
 	{
 		try {
 			JAXBContext jaxbContext=JAXBContext.newInstance("org.uddi.api_v3");
 			Marshaller marshaller = jaxbContext.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
 			ObjectFactory factory = new ObjectFactory();
 			AuthToken authToken = factory.createAuthToken();
 			authToken.setAuthInfo(UTF8_WORD);
 			StringWriter writer = new StringWriter();
 			JAXBElement<AuthToken> element = new JAXBElement<AuthToken>(new QName("","fragment"),AuthToken.class,authToken);
 			marshaller.marshal(element,writer);
 			String actualXml=writer.toString();
			assertEquals(EXPECTED_UTF8_XML_FRAGMENT, actualXml);
 		} catch (JAXBException jaxbe) {
 			fail("No exception should be thrown");
 		}
 	}
 	
 	/**
 	 * Test handling of utf8 characters
 	 */
 	@Test
 	public void unmarshallUTF8()
 	{
 		try {
 			JAXBContext jaxbContext=JAXBContext.newInstance("org.uddi.api_v3");
 			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(EXPECTED_UTF8_XML_FRAGMENT);
 			JAXBElement<AuthToken> utf8Element = unMarshaller.unmarshal(new StreamSource(reader),AuthToken.class);
 			String infoString = utf8Element.getValue().getAuthInfo();
 			assertEquals(UTF8_WORD, infoString);
 		} catch (JAXBException jaxbe) {
 			fail("No exception should be thrown");
 		}
 	}
 }
