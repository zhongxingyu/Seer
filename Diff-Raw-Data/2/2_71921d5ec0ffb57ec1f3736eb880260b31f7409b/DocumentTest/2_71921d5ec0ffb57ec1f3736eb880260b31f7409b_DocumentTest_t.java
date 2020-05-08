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
 
 public class DocumentTest {
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
     
     /**
      * test document's createElement and ownerDocument function
      * 
      * expect body's owner to be document
      */
     @Test 
     public void testCreateElement(){
     	/**setup
     	 * create a document doc
     	 * create an element body
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
 
         /**test
          * created body should not be null
          * test whether body's owner is doc
          */
         assertNotNull(body);
         assertEquals("body's owner should be document",doc,body.ownerDocument());
     }
     
     /**
      * test append child to document
      * 
      * expect body be appended to document
      */
     @Test
     public void testAppendChild(){
 
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to doc
     	 */
         Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**test
          * body's first child should be body
          * 
          * expect body be appended to document
          */
        assertEquals("document's element should be body",body,doc.documentElement());
     }
     
     /**
      * test append null child to document
      * 
      * expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testAppendNullChild(){
     	
     	/**setup
     	 * create a document doc 
     	 */
     	Document doc = new Document();
     	
     	/**test
     	 * document append null child
     	 * 
     	 * expect NullPointerException
     	 */
     	doc.appendChild(null);
     }
     
     /**
      * test create a text node in document
      * 
      * expect a text node created in document
      */
     @Test 
     public void testCreateTextNode(){
     	
     	/**setup
     	 * create a document doc
     	 * create a text node content with value "asdf" 
     	 */
         Document doc = new Document();
         Text content= doc.createTextNode("asdf");
         
         /**test
          * expect content not to be null
          * expect content's data and node value to be "asdf"
          */
         assertNotNull(content);
         assertEquals("value of content should be asdf", "asdf", content.data());
         assertEquals("value of content should be asdf", "asdf", content.nodeValue());
     }
 
     /**
      * test document's document element
      * 
      * expect return child element of document
      */
     @Test
     public void testDocumentElement(){
     	
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to document
     	 */
         Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**test
          * documentElement() should return child element of document
          * 
          * expect child element of document
          */
         assertEquals("documentElement should be body", body, doc.documentElement());
     }
     
     /**
      * test document's getElementByTagName()
      * 
      * expect nodeList of expected tag name
      */
     @Test 
     public void testGetElementsByTagName(){
         
     	/**setup
     	 * create a docuemnt doc
     	 * create an nodeList to be compared
     	 */
     	//test when there is no element in document
     	Document doc = new Document();
         NodeList nodeList = new NodeList();
         
         /**test
          * getElementByTag should return a null nodeList 
          * when there is no element in document
          * 
          * expect null nodeList
          */
         assertEquals("get element by tag name should return null nodeList when there is no element", nodeList, doc.getElementsByTagName("div"));
         
         /**setup
          * create an element body and append it to document
          * create an element div and append it to body
          * create an element span and append it to div twice
          * 
          * add two div to nodeList to compare with the result
          */
         Element body = doc.createElement("body");
         ElementInter div=new ElementInter("div");
         ElementInter span=new ElementInter("span");
         
         body.appendChild(span);
         body.appendChild(div);
         body.appendChild(div);
         doc.appendChild(body);
         
         nodeList.add(div);
         nodeList.add(div);
         
         /**test
          * getElementByTag(div) should return a nodeList 
          * with two div when there are two div in document
          * 
          * expect nodeList with expected tag name
          */
         assertEquals("get element by tag name should return nodelist with element", nodeList, doc.getElementsByTagName("div"));
     }
     
     
     /**
      * test get child nodes list of document
      * 
      * expect a list of document's child nodes
      */
     @Test 
     public void testChildNodes(){
         
     	/**setup
     	 * create a document doc
     	 * create a nodeList to be compared
     	 */
     	Document doc = new Document();
         NodeList nodeList = new NodeList();
         
         /**test
          * childNodes() should return null nodeList 
          * when there is no child in document
          * 
          * expect a null nodeList
          */
         assertEquals("childNodes should return nodeList when there is no child", nodeList, doc.childNodes());
         
         /**setup
          * create an element body and append it to document
          * and add body to nodeList to compare with result
          */
         Element body = doc.createElement("body");
         doc.appendChild(body);
         nodeList.add(body);
         
         /**test
          * childNodes should return a node list of its children
          * 
          * expect nodeList with a element body in it
          */
         assertEquals("childNodes should return nodeList when there is a child", nodeList, doc.childNodes());
     }
     
     /**
      * test insert before null 
      * 
      * expect document element should be replaced
      */
     @Test 
     public void testInsertBefore(){
         
     	/**setup
          * create a document doc
          * create an element body 
          */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         
         /**action
          * insert body to document before null
          */
         doc.insertBefore(body,null);
         
         /**test
          * document's element shold be body due to replacement
          */
         assertEquals("node should be document element when insert before null", body, doc.documentElement());
     }
     
     /**
      * test insert before child that is not in document
      * 
      * expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testInsertChildToDocument() {
         
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to doc
     	 * create an element div 
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div=doc.createElement("div");
         
         /**test
          * insert body before div that is not in document
          * 
          * expect DOMException
          */
         doc.insertBefore(div, body);
     }
     
     /**
      * test insert new child before null
      * 
      * expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testInsertNullBefore() {
         
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to document
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         
         /**test
          * insert body before null
          * 
          * expect NullPointerException
          */
         doc.insertBefore(null, body);
     }
     
     /**
      * test replaceChild
      * 
      * expect old child replaced by new child
      */
     @Test
     public void testReplaceChild(){
     	
     	/**setup
     	 * create a document doc
     	 * create a body element body and append it to document
     	 * create an element div
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div = doc.createElement("div");
         
         /**action
          * replace old child body by new child div
          */
         doc.replaceChild(div, body);
         
         /**test
          * document's first child should be new child div
          */
         assertEquals("document's firstChild should be div",div,doc.firstChild());
     }
     
     /**
      * test replace null child by null child
      * 
      * expect NullPointerException
      */
     @Test (expected=NullPointerException.class)
     public void testReplaceChildByNull(){
     	
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to document
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         doc.appendChild(body);
 
         /**test
          * replace body by null
          * 
          * expect NullPointerException
          */
         doc.replaceChild(null, body);
     }
     
     /**
      * test document replaceChild element when old child parameter is null
      * 
      * expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testReplaceNullChild(){
     	
     	/**setup
     	 * create a document doc
     	 * create an element body and append it to document
     	 * create an element div and append it to body
     	 */
     	Document doc = new Document();
         Element body = doc.createElement("body");
         Element div = doc.createElement("div");
         body.appendChild(div);
         doc.appendChild(body);
         
         /**test
          * replace null child in document
          * 
          * expect DOMException
          */
         doc.replaceChild(div, null);
     }
     
     /**
      * test document's replaceChild element when child is not in document
      * 
      * expect DOMException
      */
     @Test (expected=DOMException.class)
     public void testReplaceChildNotExist(){
     	
     	/**setup
     	 * create a body element and append it to document
     	 * create an element div
     	 * create an element span
     	 */
     	Document doc = new Document();
     	Element body = doc.createElement("body");
         doc.appendChild(body);
         Element div = doc.createElement("div");
         Element span = doc.createElement("span");
         
         /**test
          * replace element when element is not in document
          * 
          * expect DOMException when replace element by iteself
          */
         doc.replaceChild(div, span);
     }
 }
 
