 package html2windows.dom;
 
 import html2windows.dom.*;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.mockito.Mockito.*;
 
 import java.util.*;
 
 public class ElementTest{
 	Document document;
 	ElementInter element;
 
 	@Before
 	public void before(){
 		document = mock(Document.class);
 		element = new ElementInter("div");
 	}
 
 	@Test
 	public void testOwnerDocument(){
 		element.setOwnerDocument(document);
 		assertEquals(null, document, element.ownerDocument());
 	}
 
 	@Test
 	public void testTagName(){
 		assertEquals(null, "div", element.tagName());
 	}
 
 	@Test
 	public void testGetElementsByTagName(){
 		Element element1 = new ElementInter("div");
 		Element element2 = new ElementInter("h1");
 		Element element1_1 = new ElementInter("p");
 		Element element1_2 = new ElementInter("div");
 		Element element2_1 = new ElementInter("div");
 		Element element2_1_1 = new ElementInter("div");
 
 		element.appendChild(element1);
 		element.appendChild(element2);
 		element1.appendChild(element1_1);
 		element1.appendChild(element1_2);
 		element2.appendChild(element2_1);
 		element2_1.appendChild(element2_1_1);
 
 		NodeList list = element.getElementsByTagName("div");
 		
 		assertEquals("list should have 4 elements", 4, list.length());
 		assertTrue("element 1 should in list", list.contains(element1));
 		assertFalse("element 1 1 should in list", list.contains(element1_1));
 		assertTrue("element 1 2 should in list", list.contains(element1_2));
 		assertFalse("element 2 should in list", list.contains(element2));
 		assertTrue("element 2 1 should in list", list.contains(element2_1));
 		assertTrue("element 2 1 1 should in list", list.contains(element2_1_1));
 	}
 
 	@Test
 	public void attribute(){
 		HashMap<String, String> attr = new HashMap<String, String>();
 		attr.put("id", "container");
 		attr.put("class", "red-box");
 
 		assertNull("Not existing name should return null", element.getAttribute("id"));
 
 		assertNull("Pass null should return null", element.getAttribute(null));
 
 		for(String name : attr.keySet()){
 			String value = attr.get(name);
 			element.setAttribute(name, value);
 			assertEquals("'" + name + "' should get value '" + value + "'", value, element.getAttribute(name));
 		}
 
 		for(String name : attr.keySet()){
 			String value = attr.get(name);
 			element.removeAttribute(name);
 			assertNull("'" + name + "' should get null", element.getAttribute(name));
 		}
 	}
 
 	@Test
 	public void testNodeName(){
 		assertEquals(null, "div", element.nodeName());
 	}
 
 	@Test
 	public void testNodeValue(){
 		assertNull(null, element.nodeValue());
 	}
 
 	@Test
 	public void testNodeType(){
 		assertEquals("Expect ELEMENT_NODE", Node.ELEMENT_NODE, element.nodeType());
 	}
 
 	@Test
 	public void testparentNode(){
 		Element parent = new ElementInter("div");
 		parent.appendChild(element);
 		assertEquals(null, parent, element.parentNode());
 	}
 	
 	@Test
 	public void appendChildNodes(){
 		ArrayList<Node> children = new ArrayList<Node>();
 		
 		NodeInter childNodeElement = mock(NodeInter.class);
 		children.add(childNodeElement);
 		children.add(document.createTextNode("text"));
 		
 		for(Node child : children){
 			element.appendChild(child);
 		}
 		
 		NodeList childNodes = element.childNodes();
 		for(int i = 0; i < children.size(); i++){
 			assertEquals(children.get(i), childNodes.get(i));
 		}
 	}
 
 	@Test
 	public void testFirstChildAndLastChild(){
 		assertNull("Expect no child(null)", element.firstChild());
		assertNull("Expect no child(null)", element.lastChild());
 
 		Node child1 = mock(Node.class);
 		Node child2 = mock(Node.class);
 		element.appendChild(child1);
 		element.appendChild(child2);
 		
 		assertEquals(null, child1, element.firstChild());
 		assertEquals(null, child2, element.lastChild());
 	}
 
 	@Test
 	public void testSibling(){
 		assertNull(null, element.previousSibling());
 		assertNull(null, element.nextSibling());
 
 		Node prevSibling = mock(Node.class);
 		Node nextSibling = mock(Node.class);
 		Element parent = new ElementInter("div");
 
 		parent.appendChild(prevSibling);
 		parent.appendChild(element);
 		parent.appendChild(nextSibling);
 
 		assertEquals(null, prevSibling, element.previousSibling());
 		assertEquals(null, nextSibling, element.nextSibling());
 	}
 }
