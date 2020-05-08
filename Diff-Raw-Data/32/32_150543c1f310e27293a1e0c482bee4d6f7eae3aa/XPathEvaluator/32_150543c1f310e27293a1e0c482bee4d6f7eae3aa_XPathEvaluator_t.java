 package net.sue445.s3tiger.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathVariableResolver;
 
 import org.slim3.util.StringUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
 * wrapping XPath
  * via {@link http://d.hatena.ne.jp/int128/20100126/1264437133}
  * @author int128
  *
  */
 public class XPathEvaluator {
	private static String PROPERTIES_NAME = "name";
 
	private static String PROPERTIES_VALUE = "value";
 
 	private final Document document;

 	private final XPath xpath;
 
 	/**
 	 * @param stream
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	public XPathEvaluator(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		document = factory.newDocumentBuilder().parse(stream);
 		xpath = XPathFactory.newInstance().newXPath();
 	}
 
 	public Iterable<Node> getNodeList(String expression) {
 		try {
 			final NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
 			return new Iterable<Node>() {
 				@Override
 				public Iterator<Node> iterator() {
 					return new Iterator<Node>() {
 						private int index = 0;
 
 						@Override
 						public boolean hasNext() {
 							return index < nodeList.getLength();
 						}
 
 						@Override
 						public Node next() {
 							return nodeList.item(index++);
 						}
 
 						@Override
 						public void remove() {
 							throw new UnsupportedOperationException("NodeList is read-only");
 						}
 					};
 				}
 			};
 		} catch (XPathExpressionException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public Iterable<String> getNodeValueList(String expression) {
 		try {
 			final NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
 			return new Iterable<String>() {
 				@Override
 				public Iterator<String> iterator() {
 					return new Iterator<String>() {
 						private int index = 0;
 
 						@Override
 						public boolean hasNext() {
 							return index < nodeList.getLength();
 						}
 
 						@Override
 						public String next() {
 							return nodeList.item(index++).getNodeValue();
 						}
 
 						@Override
 						public void remove() {
 							throw new UnsupportedOperationException("NodeList is read-only");
 						}
 					};
 				}
 			};
 		} catch (XPathExpressionException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 *
 	 * @param expression
 	 * @return
 	 */
 	public String getString(String expression) {
 		try {
 			return (String) xpath.evaluate(expression, document, XPathConstants.STRING);
 		} catch (XPathExpressionException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * get trimed string
 	 * @param expression
 	 * @return
 	 */
 	public String getTrimedString(String expression){
 		String str = getString(expression);
 
 		if(StringUtil.isEmpty(str)){
 			return "";
 		}
 
 		str = Pattern.compile("(^\\s+|\\s+$)", Pattern.MULTILINE).matcher(str).replaceAll("");
 
 		return str;
 	}
 
 	public void setVariable(final Map<QName, Object> variableMap) {
 		xpath.setXPathVariableResolver(new XPathVariableResolver() {
 			@Override
 			public Object resolveVariable(QName variableName) {
 				return variableMap.get(variableName);
 			}
 		});
 	}
 
 	/**
 	 *
 	 * @param expression
 	 * @param variableMap
 	 * @return
 	 */
	public Map<String, String> getProperties(String expression, Map<QName, Object> variableMap){
 		if(variableMap != null && variableMap.size() > 0){
 			setVariable(variableMap);
 		}
 
 		Iterable<Node> nodeList = getNodeList(expression);
 		if(nodeList == null){
 			return null;
 		}
 
 		Map<String, String> map = new LinkedHashMap<String, String>();
 		for(Node node : nodeList){
 			NamedNodeMap attr = node.getAttributes();
			String key   = attr.getNamedItem(PROPERTIES_NAME).getNodeValue();
			String value = attr.getNamedItem(PROPERTIES_VALUE).getNodeValue();
 			map.put(key, value);
 		}
 
 		return map;
 	}
 }
