 package ch.inftec.ju.util.xml;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.StringReader;
import java.io.StringWriter;
 import java.net.URL;
 
 import javax.xml.XMLConstants;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 
 import ch.inftec.ju.util.IOUtil;
 import ch.inftec.ju.util.JuException;
 import ch.inftec.ju.util.JuRuntimeException;
 
 /**
  * Utility class containing XML related helper methods.
  * @author tgdmemae
  *
  */
 public class XmlUtils {
 	/**
 	 * Don't instantiate.
 	 */
 	private XmlUtils() {
 		throw new AssertionError("use only statically");
 	}
 	
 	/**
 	 * Loads and parses an XML into a DOM structure. This method doesn't perform schema
 	 * validation.
 	 * @param xmlUrl URL to the XML
 	 * @return Document instance
 	 * @throws JuException If the XML cannot be loaded
 	 */
 	public static Document loadXml(URL xmlUrl) throws JuException {
 		return XmlUtils.loadXml(xmlUrl, null);
 	}
 	
 	/**
 	 * Loads and parses an XML into a DOM structure.
 	 * @param xmlUrl URL to the XML
 	 * @param schemaUrl URL to an optional XML validation schema. If null, no validation is done.
 	 * @return Document instance
 	 * @throws JuException If the XML cannot be loaded
 	 */
 	public static Document loadXml(URL xmlUrl, URL schemaUrl) throws JuException {
 		InputStream xmlStream = null;
 		
 		try {
 			xmlStream = new BufferedInputStream(xmlUrl.openStream());
 			
 			return XmlUtils.loadXml(new InputSource(xmlStream), XmlUtils.loadSchema(schemaUrl));
 		} catch (Exception ex) {
 			throw new JuException(String.format("Couldn't load XML from URL: %s (Schema URL: %s)",
 					xmlUrl, schemaUrl), ex);
 		} finally {
 			IOUtil.close(xmlStream);
 		}
 	}
 
 	/**
 	 * Loads and parses an XML from an InputStream into a DOM structure.
 	 * <p>
 	 * The input stream will be wrapped in a BufferedInputStream and closed at the end.
 	 * @param inputStream InputStream of the XML
 	 * @param schema Optional Schema. If null, no validation is performed
 	 * @return Document instance
 	 * @throws JuException If the XML cannot be loaded or fails validation
 	 */
 	public static Document loadXml(InputStream inputStream, Schema schema) throws JuException {
 		InputStream xmlStream = null;
 		
 		try {
 			xmlStream = new BufferedInputStream(inputStream);			
 			return XmlUtils.loadXml(new InputSource(xmlStream), schema);
 		} catch (Exception ex) {
 			throw new JuException("Couldn't load XML from InputStream", ex);
 		} finally {
 			IOUtil.close(xmlStream);
 		}
 	}
 	
 	/**
 	 * Loads and parses an XML into a DOM structure.
 	 * @param xmlStream InputStream of the XML
 	 * @param schema Optional Schema. If null, no validation is performed
 	 * @return Document instance
 	 * @throws JuException If the XML cannot be loaded or fails validation
 	 */
 	public static Document loadXml(InputSource xmlSource, Schema schema) throws JuException {
 		try {
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 			
 			// There's a bug in JDK >= 6, ignoring whitespace is not working
 			// docBuilderFactory.setIgnoringElementContentWhitespace(true);
 			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 
 			// Parse XML
 			Document doc = docBuilder.parse(xmlSource);
 
 			// Remove whitespace manually
 			XmlUtils.removeWhitespaceNodes(doc.getDocumentElement());
 			
 			// Validate if we have a Schema
 			if (schema != null) {
 				schema.newValidator().validate(new DOMSource(doc));
 			}
 
 			return doc;
 		} catch (Exception ex) {
 			throw new JuException("Couldn't load XML" , ex);
 		}
 	}
 	
 	/**
 	 * Removes all Whitespace Noted from the specified element.
 	 * @param e Element
 	 */
 	private static void removeWhitespaceNodes(Element e) {
 		NodeList children = e.getChildNodes();
 		for (int i = children.getLength() - 1; i >= 0; i--) {
 			Node child = children.item(i);
 			if (child instanceof Text
 					&& ((Text) child).getData().trim().length() == 0) {
 				e.removeChild(child);
 			} else if (child instanceof Element) {
 				removeWhitespaceNodes((Element) child);
 			}
 		}
 	}
 	
 	/**
 	 * Loads an XML from a String.
 	 * @param xmlString XML String
 	 * @param schema Optional Schema. If null, no validation is performed
 	 * @return Document instance 
 	 * @throws JuException If the String cannot be converted to a DOM Document
 	 */
 	public static Document loadXml(String xmlString, Schema schema) throws JuException {
 		return XmlUtils.loadXml(new InputSource(new StringReader(xmlString)), schema);
 	}
 	
 	/**
 	 * Loads an XML Schema from the specified url.
 	 * @param url URL to load schema from
 	 * @return Schema instance or null if the url is null
 	 * @throws JuException If the schema cannot be loaded
 	 */
 	public static Schema loadSchema(URL url) throws JuException {
 		if (url == null) return null;
 		
 		try {
 			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			Schema schema = schemaFactory.newSchema(url);
 
 			return schema;
 		} catch (Exception ex) {
 			throw new JuException("Couldn't load XML schema: " + url, ex);
 		}
 	}
 	
 	/**
 	 * Indents the specified XML.
 	 * @param xmlString XML string to be indented
 	 * @param includeXmlDeclaration If true, the &lt;?xml ... ?> declaration is included.
 	 * @return Indented XML
 	 */
 	public static String indentXml(String xmlString, boolean includeXmlDeclaration) throws JuException {
 		Document doc = XmlUtils.loadXml(xmlString, null);
 		return XmlUtils.toString(doc, includeXmlDeclaration, true);
 	}
 	
 	/**
 	 * Converts an XML to a String.
 	 * <p>
 	 * This will create an XML string with encoding="UTF-8" and a standalone declaration,
 	 * as specified in the Document.
 	 * <p>
 	 * Indentation will be two blanks - if true.
 	 * @param document XML Document
 	 * @param includeXmlDeclaration If true, the &lt;?xml ... ?> declaration is included.
 	 * @param indent If true, result will be indented (pretty-printed), using two blanks
 	 * for child indentation
 	 * @return String representation of the XML
 	 * @throws JuRuntimeException If the conversion fails
 	 */
 	public static String toString(Document document, boolean includeXmlDeclaration, boolean indent) throws JuRuntimeException {
 		try {
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
 			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, (includeXmlDeclaration ? "no" : "yes"));
 			
 			if (indent) {
 				transformer.setOutputProperty(OutputKeys.INDENT, (indent ? "yes" : "no"));
 				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 			}
 			
			try (StringWriter writer = new StringWriter()) {
				transformer.transform(new DOMSource(document), new StreamResult(writer));
				return writer.toString();
 			}			
 		} catch (Exception ex) {
 			throw new JuRuntimeException("Couldn't convert XML to String", ex);
 		}
 	}
 	
 	/**
 	 * Creates a new XmlBuilder to construct an XML document based on DOM (document
 	 * object model)
 	 * @param rootElementName Name of the root element
 	 * @return XmlBuilder of the root element to construct the XML
 	 */
 	public static XmlBuilder buildXml(String rootElementName) {
 		return XmlBuilder.createRootBuilder(rootElementName);
 	}
 }
