 package it.essepuntato.earmark.core;
 
 import it.essepuntato.earmark.core.xml.EARMARKNamespaceContext;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathFactoryConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * This class represents an abstract xpath range in the EARMARK model.
  * 
  * @author Silvio Peroni
  *
  */
 public abstract class XPathRange extends Range {
 	
 	private String context = null;
 
 	/**
 	 * Create an XPath range.
 	 * 
 	 * @param d the document to which this item is associated.
 	 * @param docuverse the docuverse the range refers to.
 	 * @param begin the begin non-negative integer point (if negative, it consider its
 	 * absolute value) of the range.
 	 * @param end the end non-negative integer point (if negative, it consider its
 	 * absolute value) of the range. 
 	 * @param context the xpath expression used to extract the sequence of XML text nodes that will be
 	 * used as content for calculating the text content of the range.
 	 * @param id the identifier for the new range. 
 	 */
 	protected XPathRange(EARMARKDocument d, Docuverse docuverse, Object begin,
 			Object end, String context, URI id) {
 		super(d, docuverse, begin, end, id);
 		this.context = context;
 	}
 
 	/**
 	 * This method returns the XPath context for the range.
 	 * 
 	 * @return a string representing an XPath expression.
 	 */
 	public String hasXPathContext() {
 		return context;
 	}
 	
 	/**
 	 * <p>This method retrieves the textual content of the range simply applying the XPath expression
 	 * specified as context to the docuverse (XML) content and then taking all the value of all the
 	 * nodes (elements, attributes and text nodes only) returned by the expression, considering the
 	 * sequence order.</p>
 	 * <p>The possible reason of failure in retrieving the text content (i.e., result = "") are:</p>
 	 * <ul>
 	 * <li>the docuverse does not contain a well-formed XML document</li>
 	 * <li>the XPath context specified is not a valid XPath expression</li>
 	 * </ul>
 	 * @return the textual content retrieved by applying the XPath context onto the docuverse content, or null
 	 * if some problem happens.
 	 */
 	public String getTextContent() {
 		String result = "";
 		
 		try {
 			String content = getDocuverseContent();
			String textualResult = "";
 			
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
 			factory.setNamespaceAware(true); 
 		
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document newDocument = builder.parse(new InputSource(new StringReader(content)));
 			
 			XPathFactory xpathFactory = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL);
 			XPath xpath = xpathFactory.newXPath();
 			xpath.setNamespaceContext(new EARMARKNamespaceContext(newDocument));
 			
 			XPathExpression query = xpath.compile((context == null ? "//text()" : context));
 			NodeList matched = (NodeList) query.evaluate(newDocument, XPathConstants.NODESET);
 			
 			int size = matched.getLength();
 			for (int i = 0; i < size; i++) {
				textualResult += getTextualContentFromNode(matched.item(i));
 			}
 			
 		} catch (XPathFactoryConfigurationException e) {
 			result = "";
 		} catch (XPathExpressionException e) {
 			result = "";
 		} catch (ParserConfigurationException e) {
 			/* Problem in loading the builder: no content available */
 			result = "";
 		} catch (SAXException e) {
 			/* It is not XML: no content available */
 			result = "";
 		} catch (IOException e) {
 			/* The string is not well-formed: no content available */
 			result = "";
 		}
 		
 		return result;
 	}
 	
 	/* Get the textual content from a node if it is an attribute, an element or a text node. */
 	private String getTextualContentFromNode(Node n) {
 		String result = "";
 		
 		if (n.getNodeType() == Node.ATTRIBUTE_NODE || n.getNodeType() == Node.TEXT_NODE) {
 			result += n.getNodeValue();
 		} else if (n.getNodeType() == Node.ELEMENT_NODE) {
 			NodeList children = n.getChildNodes();
 			int size = children.getLength();
 			for (int i = 0; i < size; i++) {
 				result += getTextualContentFromNode(children.item(i));
 			}
 		}
		
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + " (xpath: " + hasXPathContext() + " )";
 	}
 }
