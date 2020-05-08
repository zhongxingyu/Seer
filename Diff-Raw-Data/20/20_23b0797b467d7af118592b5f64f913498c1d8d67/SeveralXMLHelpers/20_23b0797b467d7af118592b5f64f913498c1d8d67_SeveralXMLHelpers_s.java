 package org.vpac.grisu.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Helper methods that do convert/extract xml documents and vice versa.
  * 
  * @author Markus Binsteiner
  * 
  */
 public final class SeveralXMLHelpers {
 
 	/**
 	 * Converts a xml element to a string.
 	 * 
 	 * @param element
 	 *            the element
 	 * @return the string
 	 */
 	private static String convertElementToString(final Element element) {
 
 		StringBuffer result = new StringBuffer();
 		String tagName = element.getTagName();
 		result.append("<" + tagName + " ");
 		NamedNodeMap attributes = element.getAttributes();
 		for (int i = 0; i < attributes.getLength(); i++) {
 			Node node = attributes.item(i);
 			Attr attr = (Attr) node;
 			result.append(attr.getName());
 			String value = attr.getValue();
 			result.append("=" + value + " ");
 		}
 		result.append(">\n");
 
 		NodeList childs = element.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			Node node = childs.item(i);
 			Element child = (Element) node;
 			result.append(convertElementToString(child));
 		}
 		result.append("</" + tagName + ">\n");
 		return result.toString();
 	}
 
 	/**
 	 * Converts a xml element to a string.
 	 * 
 	 * @param element
 	 *            the element
 	 * @return the string
 	 */
 	public static String convertToString(final Element element) {
 
 		return convertElementToString(element);
 	}
 
 	// /**
 	// * This needs to be called if the cxf backend is used. It's a workaround
 	// for
 	// * the bug where cxf wraps a document parameter into return or arg0
 	// * elements.
 	// *
 	// * @param doc
 	// * the xml document
 	// * @param expectedElementName
 	// * the element name that you would expect for the first child
 	// * @return either a new xml document or the unchanged old one
 	// */
 	// public static Document cxfWorkaround(Document doc,
 	// final String expectedElementName) {
 	// Element element = (Element) doc.getFirstChild();
 	// if (!element.getTagName().equals(expectedElementName)) {
 	// try {
 	// doc = createDocumentFromElement((Element) element
 	// .getFirstChild());
 	// } catch (Exception e) {
 	// throw new RuntimeException("Could not parse jsdl document.", e);
 	// }
 	// }
 	// return doc;
 	// }
 
 	/**
 	 * Creates a new xml document from an xml element.
 	 * 
 	 * @param element
 	 *            the element
 	 * @return the new document
 	 * @throws Exception
 	 *             if the document can't be created
 	 */
 	public static Document createDocumentFromElement(final Element element)
 	throws Exception {
 		try {
 			final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 			final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 			final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 
 			// File schemaFile = new
 			// File("/home/markus/workspace/nw-core/jsdl.xsd");
 
 			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory
 			.newInstance();
 			docBuildFactory
 			.setAttribute(
 					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
 					false);
 			docBuildFactory.setNamespaceAware(true);
 			docBuildFactory.setValidating(false);
 
 			docBuildFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); // use
 			// LANGUAGE
 			// here
 			// instead
 			// of
 			// SOURCE
 			// docBuildFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
 
 			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
 
 			DOMImplementation impl = docBuilder.getDOMImplementation();
 
 			Document doc = impl.createDocument(null, null, null);
 
 			Node tempNode = doc.importNode(element, true);
 
 			doc.appendChild(tempNode);
 
 			return doc;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 
 	}
 
 	/**
 	 * Creates a xml document from an input stream.
 	 * 
 	 * @param input
 	 *            the input stream
 	 * @return the xml document
 	 * @throws Exception
 	 *             if the conversion fails
 	 */
 	public static Document fromInputStream(final InputStream input)
 	throws Exception {
 		try {
 			final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 			final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 			final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 
 			// File schemaFile = new
 			// File("/home/markus/workspace/nw-core/jsdl.xsd");
 
 			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory
 			.newInstance();
 			docBuildFactory
 			.setAttribute(
 					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
 					false);
 			docBuildFactory.setNamespaceAware(true);
 			docBuildFactory.setValidating(false);
 
 			docBuildFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); // use
 			// LANGUAGE
 			// here
 			// instead
 			// of
 			// SOURCE
 			// docBuildFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
 
 			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
 			return docBuilder.parse(input);
 		} catch (Exception e) {
 			// e.printStackTrace();
 			throw e;
 		}
 	}
 
 	/**
 	 * Parses a string into a xml document.
 	 * 
 	 * @param jsdl_string
 	 *            the string
 	 * @return the xml document
 	 * @throws Exception
 	 *             if the conversion fails
 	 */
 	public static Document fromString(final String jsdl_string) {
 
		if ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".equals(jsdl_string)  ) {
 			throw new RuntimeException("Only an empty string...");
 		}
 
 		try {
 			final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 			final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 			final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 
 			// File schemaFile = new
 			// File("/home/markus/workspace/nw-core/jsdl.xsd");
 
 			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory
 			.newInstance();
 			docBuildFactory
 			.setAttribute(
 					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
 					false);
 			docBuildFactory.setNamespaceAware(true);
 			docBuildFactory.setValidating(false);
 
 			//			docBuildFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); // use
 			// LANGUAGE
 			// here
 			// instead
 			// of
 			// SOURCE
 			// docBuildFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
 
 			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
 			Document result = docBuilder.parse(new ByteArrayInputStream(jsdl_string
 					.getBytes()));
 
 			return result;
 		} catch (Exception e) {
 			// e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Loads xml data from a file.
 	 * 
 	 * @param file
 	 *            the (xml-)file
 	 * @return the xml document
 	 */
 	public static Document loadXMLFile(final File file) {
 
 		Document jsdl = null;
 
 		final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 		final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 		final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 
 		// File schemaFile = new
 		// File("/home/markus/workspace/nw-core/jsdl.xsd");
 
 		DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory
 		.newInstance();
 		docBuildFactory.setAttribute(
 				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
 				false);
 		docBuildFactory.setNamespaceAware(true);
 		docBuildFactory.setValidating(false);
 
 		docBuildFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); // use
 		// LANGUAGE
 		// here
 		// instead
 		// of
 		// SOURCE
 		// docBuildFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
 
 		try {
 			DocumentBuilder documentBuilder = docBuildFactory
 			.newDocumentBuilder();
 			jsdl = documentBuilder.parse(file);
 			// JsdlHelpers.validateJSDL(jsdl);
 
 		} catch (ParserConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SAXException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		return jsdl;
 	}
 
 	/**
 	 * Converts a xml document to a string.
 	 * 
 	 * @param xml
 	 *            the xml document
 	 * @return the string
 	 * @throws TransformerFactoryConfigurationError
 	 *             xml error
 	 * @throws TransformerException
 	 *             xml error
 	 */
 	public static String toString(final Document xml) {
 
 		try {
 			// TODO use static transformer to reduce overhead?
 			Transformer transformer = TransformerFactory.newInstance()
 			.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 			// initialize StreamResult with InputFile object to save to file
 			StreamResult result = new StreamResult(new StringWriter());
 			DOMSource source = new DOMSource(xml);
 			transformer.transform(source, result);
 
 			String jsdl_string = result.getWriter().toString();
 			return jsdl_string;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Converts a xml document to a string. Surpresses exceptions.
 	 * 
 	 * @param xml
 	 *            the xml document
 	 * @return the string
 	 */
 	public static String toStringWithoutAnnoyingExceptions(final Document xml) {
 
 		String result;
 		try {
 			result = toString(xml);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			result = e.getLocalizedMessage();
 		}
 
 		return result;
 
 	}
 
 	private SeveralXMLHelpers() {
 	}
 }
