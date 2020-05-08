 package de.devboost.natspec.library.documentation;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class HTMLValidator {
 
 	/**
 	 * Check that the given HTML is valid XML because otherwise the some browser
 	 * (e.g., the Eclipse built-in browser) won't show anything but an
 	 * exception.
 	 * 
 	 * @param html
 	 *            the HTML code to check
 	 * @return <code>true</code> is the HTML code is valid XML, otherwise
 	 *         <code>false</code>
 	 */
 	public boolean isValidXML(String html) {
 
 		try {
			byte[] bytes = html.getBytes();
 			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
 
 			DocumentBuilderFactory dbFactory = createDocumentBuilderFactory();
 			DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
 			Document document = documentBuilder.parse(inputStream);
 			document.getDocumentElement().normalize();
 			return true;
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return false;
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private DocumentBuilderFactory createDocumentBuilderFactory()
 			throws ParserConfigurationException {
 		
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		dbFactory.setValidating(false);
 		dbFactory.setNamespaceAware(true);
 		dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
 		dbFactory.setFeature("http://xml.org/sax/features/validation", false);
 		dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
 		dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 		return dbFactory;
 	}
 }
