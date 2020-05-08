 package com.mtgi.analytics.xml;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.saxon.dom.NodeWrapper;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 public class XSLLibraryTest {
 
 	@Test
 	public void testQuoteNull() throws IOException {
 		assertEquals("null returns SQL null literal", "null", XSLLibrary.quoteSql(null));
 	}
 	
 	@Test
 	public void testQuoteEmpty() throws IOException {
 		assertEquals("empty returns empty SQL literal", "''", XSLLibrary.quoteSql(""));
 	}
 	
 	@Test
 	public void testQuoteXml() throws IOException, ParserConfigurationException {
 		
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Element elt = doc.createElement("foo");
 		elt.setAttribute("bar", "'bar'");
 		Element child = doc.createElement("baz");
 		child.appendChild(doc.createTextNode("inner 'quotes'"));
 		elt.appendChild(child);
 		
 		assertEquals("xml is serialized, with internal quotes escaped",
 					 "'<foo bar=\"''bar''\"><baz>inner ''quotes''</baz></foo>'",
 					 XSLLibrary.quoteSql(new NodeWrapper(elt, null, 1) {}));
 		
 	}
 	
 	@Test
 	public void testEscapeCsv() throws IOException, ParserConfigurationException {
 		
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Element elt = doc.createElement("foo");
 		elt.setAttribute("bar", "\"b\\ar\"");
 		Element child = doc.createElement("baz");
 		child.appendChild(doc.createTextNode("inner\rlines\r\nnormalized\ncompletely"));
 		elt.appendChild(child);
 		
		assertEquals("xml is serialized, with internal quotes stuttered",
					 "\"<foo bar=\"\"&quot;b\\ar&quot;\"\"><baz>inner&#xd;lines&#xd;\rnormalized\rcompletely</baz></foo>\"",
 					 XSLLibrary.quoteCsv(new NodeWrapper(elt, null, 1) {}));
 		
 	}
 	
 	@Test
 	public void testNullCsv() throws IOException {
 		assertEquals("Null CSV is converted to blank", "", XSLLibrary.quoteCsv(null));
 	}
 	
 	@Test
 	public void testEmptyCsv() throws IOException {
 		assertEquals("Empty CSV is quoted", "\"\"", XSLLibrary.quoteCsv(""));
 	}
 	
 	@Test
 	public void testSerializeNull() throws IOException {
 		assertNull("null object serializes to null", XSLLibrary.serialize(null));
 	}
 	
 	@Test
 	public void testSerializeAttribute() throws Exception {
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Element elt = doc.createElement("foo");
 		elt.setAttribute("bar", "'bar'");
 		
 		assertEquals("attribute value serializes directly",
 					 "'bar'", XSLLibrary.serialize(elt.getAttributeNode("bar")));
 	}
 
 	@Test
 	public void testSerializeText() throws Exception {
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Text elt = doc.createTextNode("inner 'quotes'");
 		
 		assertEquals("text value serializes directly",
 					 "inner 'quotes'", XSLLibrary.serialize(elt));
 	}
 	
 	@Test
 	public void testSerializeLiteral() throws Exception {
 		assertEquals("literal values are serialized as toString", "72", XSLLibrary.serialize(72));
 	}
 	
 	@Test
 	public void testSerializeWrapper() throws Exception {
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Element elt = doc.createElement("foo");
 		elt.setAttribute("bar", "'bar'");
 		Element child = doc.createElement("baz");
 		child.appendChild(doc.createTextNode("inner 'quotes'"));
 		elt.appendChild(child);
 		
 		assertEquals("xml is serialized, with internal quotes escaped",
 					 "<foo bar=\"'bar'\"><baz>inner 'quotes'</baz></foo>",
 					 XSLLibrary.serialize(new NodeWrapper(elt, null, 1) {}));
 	}
 	
 	@Test
 	public void testSerializeCollection() throws Exception {
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		Element elt = doc.createElement("foo");
 		elt.setAttribute("bar", "'bar'");
 		Element child = doc.createElement("baz");
 		child.appendChild(doc.createTextNode("inner 'quotes'"));
 		elt.appendChild(child);
 		
 		ArrayList<Object> coll = new ArrayList<Object>();
 		coll.add(elt);
 		coll.add(79);
 		
 		assertEquals("collection elements are appended together",
 					 "<foo bar=\"'bar'\"><baz>inner 'quotes'</baz></foo>79",
 					 XSLLibrary.serialize(coll));
 	}
 	
 	@Test
 	public void testSerializeEmptyCollection() throws Exception {
 		assertNull("empty collection serializes to null", 
 					XSLLibrary.serialize(new ArrayList<Object>()));
 	}
 }
