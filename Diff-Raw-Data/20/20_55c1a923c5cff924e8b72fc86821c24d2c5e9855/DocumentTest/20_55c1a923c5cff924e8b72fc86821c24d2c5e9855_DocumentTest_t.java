 package html2windows.dom;
 import java.io.File;
 
 import html2windows.dom.UIParser;
 import static org.junit.Assert.*;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import html2windows.dom.Document;
 import html2windows.dom.DocumentFragmentInter;
 import html2windows.dom.Element;
 import html2windows.dom.ElementInter;
 import html2windows.dom.TextInter;
 import org.w3c.dom.DOMException;
 
 /**
  * Test some case of document 
  * 
  * @author 		Jason Kuo
  */
 
 
 public class DocumentTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
     
     /**
      * Test document's createElement and ownerDocument function
      * 
      * Expect body's owner to be document
      */
     @Test 
     public void testCreateElement(){
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
 
         /**
          * Test
          * 
          * Created body should not be null
          * Test whether body's owner is doc
          */
         assertNotNull(body);
         assertEquals("body's owner should be document",doc,body.ownerDocument());
     }
     
     /**
      * Test append child to document
      * 
      * Expect body be appended to document
      */
     @Test
     public void testAppendChild(){
 
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to doc
     	 */
         Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**
          * Test
          * 
          * Body's first child should be body
          * 
          * Expect body be appended to document
          */
         assertEquals("document's element should be body",body,doc.documentElement());
     }
     
     /**
      * Test append null child to document
      * 
      * Expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testAppendNullChild(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc 
     	 */
     	Document doc = new Document();
     	
     	/**
     	 * Test
     	 * 
     	 * Document append null child
     	 * 
     	 * Expect NullPointerException
     	 */
     	doc.appendChild(null);
     }
     
     /**
      * Test create a text node in document
      * 
      * Expect a text node created in document
      */
     @Test 
     public void testCreateTextNode(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create a text node content with value "asdf" 
     	 */
         Document doc = new Document();
         Text content= doc.createTextNode("asdf");
         
         /**
          * Test
          * 
          * Expect content not to be null
          * Expect content's data and node value to be "asdf"
          */
         assertNotNull(content);
         assertEquals("value of content should be asdf", "asdf", content.data());
         assertEquals("value of content should be asdf", "asdf", content.nodeValue());
     }
 
     /**
      * Test document's document element
      * 
      * Expect return child element of document
      */
     @Test
     public void testDocumentElement(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to document
     	 */
         Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**
          * Test
          * 
          * documentElement() should return child element of document
          * 
          * Expect child element of document
          */
         assertEquals("documentElement should be body", body, doc.documentElement());
     }
     
     /**
      * Test document's getElementByTagName()
      * 
      * Expect nodeList of expected tag name
      */
     @Test 
     public void testGetElementsByTagName(){
         
     	/**
     	 * Setup
     	 * 
     	 * Create a docuemnt doc
     	 * Create an nodeList to be compared
     	 */
     	//test when there is no element in document
     	Document doc = new Document();
         NodeList nodeList = new NodeList();
         
         /**
          * Test
          * 
          * getElementByTag should return a null nodeList 
          * when there is no element in document
          * 
          * Expect null nodeList
          */
         assertEquals("get element by tag name should return null nodeList when there is no element", nodeList, doc.getElementsByTagName("div"));
         
         /**
          * Setup
          * 
          * Create an element body and append it to document
          * Create an element div and append it to body
          * Create an element span and append it to div twice
          * Add two div to nodeList to compare with the result
          */
         Element body = doc.createElement("body");
        ElementInter div1 = new ElementInter("div");
        ElementInter div2 = new ElementInter("div");
         ElementInter span=new ElementInter("span");
         
         body.appendChild(span);
        body.appendChild(div1);
        body.appendChild(div2);
         doc.appendChild(body);
         
        nodeList.add(div1);
        nodeList.add(div2);
         
         /**
          * Test
          * 
          * getElementByTag(div) should return a nodeList 
          * with two div when there are two div in document
          * 
          * Expect nodeList with expected tag name
          */
		NodeList divResultList = doc.getElementsByTagName("div");

		Node[] expect = new Node[nodeList.size()];
		Node[] result = new Node[divResultList.size()];
        assertArrayEquals("get element by tag name should return nodelist with element", nodeList.toArray(expect), divResultList.toArray(result));
     }
     
     
     /**
      * Test get child nodes list of document
      * 
      * Expect a list of document's child nodes
      */
     @Test 
     public void testChildNodes(){
         
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create a nodeList to be compared
     	 */
     	Document doc = new Document();
         NodeList nodeList = new NodeList();
         
         /**
          * Test
          * 
          * childNodes() should return null nodeList 
          * when there is no child in document
          * 
          * Expect a null nodeList
          */
         assertEquals("childNodes should return nodeList when there is no child", nodeList, doc.childNodes());
         
         /**
          * Setup
          * 
          * Create an element body and append it to document
          * and add body to nodeList to compare with result
          */
         Element body = doc.createElement("body");
         doc.appendChild(body);
         nodeList.add(body);
         
         /**
          * Test
          * 
          * ChildNodes should return a node list of its children
          * 
          * Expect nodeList with a element body in it
          */
         assertEquals("childNodes should return nodeList when there is a child", nodeList, doc.childNodes());
     }
     
     /**
      * Test insert before null 
      * 
      * Expect document element should be replaced
      */
     @Test 
     public void testInsertBefore(){
         
     	/**
     	 * Setup
          * 
          * Create a document doc
          * Create an element body 
          */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         
         /**
          * Action
          * 
          * Insert body to document before null
          */
         doc.insertBefore(body,null);
         
         /**
          * Test
          * 
          * Document's element shold be body due to replacement
          */
         assertEquals("node should be document element when insert before null", body, doc.documentElement());
     }
     
     /**
      * Test insert before child that is not in document
      * 
      * Expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testInsertChildToDocument() {
         
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to doc
     	 * Create an element div 
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div=doc.createElement("div");
         
         /**Test
          * 
          * Insert body before div that is not in document
          * 
          * Expect DOMException
          */
         doc.insertBefore(div, body);
     }
     
     /**
      * Test insert new child before null
      * 
      * Expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testInsertNullBefore() {
         
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to document
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**
          * Test
          * 
          * Insert body before null
          * 
          * Expect NullPointerException
          */
         doc.insertBefore(null, body);
     }
     
     /**
      * Test replaceChild
      * 
      * Expect old child replaced by new child
      */
     @Test
     public void testReplaceChild(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create a body element body and append it to document
     	 * Create an element div
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div = doc.createElement("div");
         
         /**
          * Action
          * 
          * Replace old child body by new child div
          */
         doc.replaceChild(div, body);
         
         /**
          * Test
          * 
          * Document's first child should be new child div
          */
         assertEquals("document's firstChild should be div",div,doc.firstChild());
     }
     
     /**
      * Test replace null child by null child
      * 
      * Expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testReplaceChildByNull(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to document
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
 
         /**
          * Test
          * 
          * Replace body by null
          * 
          * Expect NullPointerException
          */
         doc.replaceChild(null, body);
     }
     
     /**
      * Test document replaceChild element when old child parameter is null
      * 
      * Expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testReplaceNullChild(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a document doc
     	 * Create an element body and append it to document
     	 * Create an element div and append it to body
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         Element div = doc.createElement("div");
         body.appendChild(div);
         doc.appendChild(body);
         
         /**
          * Test
          * 
          * Replace null child in document
          * 
          * Expect DOMException
          */
         doc.replaceChild(div, null);
     }
     
     /**
      * Test document's replaceChild element when child is not in document
      * 
      * Expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testReplaceChildNotExist(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a body element and append it to document
     	 * Create an element div
     	 * Create an element span
     	 */
     	Document doc = new Document();
     	Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div = doc.createElement("div");
         Element span = doc.createElement("span");
         
         /**
          * Test
          * 
          * Replace element when element is not in document
          * 
          * Expect DOMException when replace element by iteself
          */
         doc.replaceChild(div, span);
     }
 }
 
