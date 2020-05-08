 package jp.co.flect.xml;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.xml.sax.SAXException;
 import org.xml.sax.InputSource;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.io.StringWriter;
 import java.util.Iterator;
 
 import org.apache.xerces.util.XMLChar;
 
 public class XMLUtils {
 	
 	public static final String XMLNS_WSDL          = "http://schemas.xmlsoap.org/wsdl/";
 	public static final String XMLNS_WSDL_SOAP     = "http://schemas.xmlsoap.org/wsdl/soap/";
 	
 	public static final String XMLNS_XSD           = "http://www.w3.org/2001/XMLSchema";
 	public static final String XMLNS_XSI           = "http://www.w3.org/2001/XMLSchema-instance";
 	
 	public static final String XMLNS_SOAP          = "http://schemas.xmlsoap.org/soap/";
 	public static final String XMLNS_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
 	
 	public static final String XMLNS_SOAP12          = "http://schemas.xmlsoap.org/wsdl/soap12/";
	public static final String XMLNS_SOAP12_ENVELOPE = "http://www.w3.org/2003/05/soapenvelope";
 	
 	public static final DocumentBuilderFactory createDefaultDocumentBuilderFactory() {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setCoalescing(true);
 		dbf.setExpandEntityReferences(true);
 		dbf.setIgnoringComments(true);
 		dbf.setIgnoringElementContentWhitespace(true);
 		dbf.setNamespaceAware(true);
 		dbf.setValidating(false);
 		dbf.setXIncludeAware(true);
 		return dbf;
 	}
 	
 	/**
 	 * 空のDocumentを作成します
 	 */
 	public static Document newDocument() {
 		try {
 			DocumentBuilder builder = createDefaultDocumentBuilderFactory().newDocumentBuilder();
 			return builder.newDocument();
 		} catch (ParserConfigurationException e) {
 			//not occur
 			e.printStackTrace();
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	/**
 	 * 指定の文書要素を持つDocumentを作成します<br>
 	 * 作成される文書要素には適切な名前空間宣言がつきます。
 	 * @param nsuri 名前空間URI
 	 * @param qname 要素名
 	 */
 	public static Document newDocument(String nsuri, String qname) {
 		Document doc = newDocument();
 		Element el = doc.createElementNS(nsuri, qname);
 		if (nsuri != null) {
 			int idx = qname.indexOf(':');
 			if (idx == -1) {
 				el.setAttribute("xmlns", nsuri);
 			} else {
 				el.setAttribute("xmlns:" + qname.substring(0, idx), nsuri);
 			}
 		}
 		doc.appendChild(el);
 		return doc;
 	}
 	
 	/**
 	 * 簡易DocumentBuilder作成メソッド。
 	 * @param ignoreWhitespace 空白のみのテキストを無視する場合trueを指定
 	 * @param ignoreComment コメントノードを無視する場合trueを指定
 	 */
 	public static DocumentBuilder newDocumentBuilder(boolean ignoreWhitespace, boolean ignoreComment) {
 		try {
 			DocumentBuilderFactory dbf = createDefaultDocumentBuilderFactory();
 			dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
 			dbf.setIgnoringComments(ignoreComment);
 			return dbf.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			//not occur
 			e.printStackTrace();
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	/**
 	 * 簡易XMLパースメソッド。<br>
 	 * プログラムで扱いやすいように整形してパースするので正確なパースが必要な場合は
 	 * DocumentBuilderを使用してください。<br>
 	 * - コメントは無視します。<br>
 	 * - 空白のみのテキストはストリップします。
 	 */
 	public static Document parse(File f) throws IOException, SAXException {
 		return parse(new FileInputStream(f));
 	}
 	
 	/**
 	 * 簡易XMLパースメソッド。<br>
 	 * parse(File)を参照
 	 */
 	public static Document parse(InputStream is) throws IOException, SAXException {
 		try {
 			DocumentBuilder builder = createDefaultDocumentBuilderFactory().newDocumentBuilder();
 			return builder.parse(is);
 		} catch (ParserConfigurationException e) {
 			//not occur
 			e.printStackTrace();
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	/**
 	 * 簡易XMLパースメソッド。<br>
 	 * parse(File)を参照
 	 */
 	public static Document parse(Reader reader) throws IOException, SAXException {
 		try {
 			DocumentBuilder builder = createDefaultDocumentBuilderFactory().newDocumentBuilder();
 			return builder.parse(new InputSource(reader));
 		} catch (ParserConfigurationException e) {
 			//not occur
 			e.printStackTrace();
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	/**
 	 * 要素から指定の名前空間URIとローカルネームを持つ子要素を返します。
 	 */
 	public static Element getElementNS(Element el, String nsuri, String name) {
 		Node node = el.getFirstChild();
 		while (node != null) {
 			if (matchNS(node, nsuri, name)) {
 				return (Element)node;
 			}
 			node = node.getNextSibling();
 		}
 		return null;
 	}
 	
 	/**
 	 * 要素が指定の名前空間URIとローカルネームかどうかを返します。
 	 */
 	public static boolean matchNS(Node node, String nsuri, String name) {
 		if (nsuri == null) {
 			return node.getNamespaceURI() == null && node.getLocalName().equals(name);
 		} else {
 			return nsuri.equals(node.getNamespaceURI()) && node.getLocalName().equals(name);
 		}
 	}
 	
 	/**
 	 * 文字列がNCNameかどうかを返します。
 	 */
 	public static boolean isNCName(String s) {
 		return XMLChar.isValidNCName(s);
 	}
 	
 	public static String getAsString(Document doc, boolean indent) {
 		return doGetAsString(doc, indent);
 	}
 	
 	public static String getAsString(Element el, boolean indent) {
 		return doGetAsString(el, indent);
 	}
 	
 	public static void saveToFile(Document doc, File f, boolean indent) throws IOException {
 		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
 		try {
 			int n = indent ? 4 : 0;
 			writeByDom(doc, writer, n);
 		} finally {
 			writer.close();
 		}
 	}
 	
 	private static String doGetAsString(Node node, boolean indent) {
 		StringWriter writer = new StringWriter();
 		try {
 			int n = indent ? 4 : 0;
 			writeByDom(node, writer, n);
 		} catch (IOException e) {
 			//not occur
 			e.printStackTrace();
 			throw new IllegalStateException(e);
 		}
 		return writer.toString();
 	}
 	
 	private static void writeByXMLWriter(Node node, Writer writer, int indent) throws IOException {
 		XMLWriter xmlWriter = new XMLWriter(writer, "utf-8", indent);
 		try {
 			if (node.getNodeType() == Node.DOCUMENT_NODE) {
 				xmlWriter.write((Document)node);
 			} else {
 				xmlWriter.write((Element)node);
 			}
 		} finally {
 			xmlWriter.close();
 		}
 	}
 	
 	private static void writeByDom(Node node, Writer writer, int indent) throws IOException {
 		try {
 			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
 			DOMImplementationLS domImpLS = (DOMImplementationLS)registry.getDOMImplementation("LS 3.0");
 			
 			LSOutput output = domImpLS.createLSOutput();
 			output.setCharacterStream(writer);
 			output.setEncoding("utf-8");
 			
 			LSSerializer serializer = domImpLS.createLSSerializer();
 			serializer.setNewLine("\n");
 			if (indent > 0) {
 				serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
 			}
 			serializer.write(node, output);
 		} catch (ClassNotFoundException e) {
 			throw new IllegalStateException(e);
 		} catch (InstantiationException e) {
 			throw new IllegalStateException(e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	private static void writeByTransformer(Node node, Writer writer, int indent) throws IOException {
 		if (node.getNodeType() == Node.DOCUMENT_NODE) {
 			((Document)node).setXmlStandalone(true);
 		}
 		try {
 			Transformer transformer = TransformerFactory.newInstance().newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, indent == 0 ? "no" : "yes");
 			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
 			if (indent > 0) {
 				transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", Integer.toString(indent));
 			}
 			transformer.setOutputProperty("{http://xml.apache.org/xalan}line-separator", "\n");
 			transformer.transform(new DOMSource(node), new StreamResult(writer));
 		} catch (TransformerConfigurationException e) {
 			throw new IllegalStateException(e);
 		} catch (TransformerException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T> T evalXPath(Node node, String expr, Class<T> classOfT) throws XPathExpressionException {
 		QName returnType = getXPathType(classOfT);
 		if (returnType == null) {
 			throw new IllegalArgumentException(classOfT.toString());
 		}
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		xpath.setNamespaceContext(new NamespaceContextImpl(node));
 		return (T)xpath.evaluate(expr, node, returnType);
 	}
 	
 	private static QName getXPathType(Class c) {
 		if (c == Node.class) return XPathConstants.NODE;
 		if (c == NodeList.class) return XPathConstants.NODESET;
 		if (c == String.class) return XPathConstants.STRING;
 		if (c == Boolean.class) return XPathConstants.BOOLEAN;
 		if (c == Double.class) return XPathConstants.NUMBER;
 		
 		return null;
 	}
 	
 	private static class NamespaceContextImpl implements NamespaceContext {
 		
 		private Node node;
 		
 		public NamespaceContextImpl(Node node) {
 			this.node = node;
 		}
 		
 		public String getNamespaceURI(String prefix) {
 			return this.node.lookupNamespaceURI(prefix);
 		}
 		
 		public String getPrefix(String namespaceURI) {
 			return this.node.lookupPrefix(namespaceURI);
 		}
 		
 		public Iterator getPrefixes(String namespaceURI) {
 			throw new UnsupportedOperationException();
 		}
 	}
 }
