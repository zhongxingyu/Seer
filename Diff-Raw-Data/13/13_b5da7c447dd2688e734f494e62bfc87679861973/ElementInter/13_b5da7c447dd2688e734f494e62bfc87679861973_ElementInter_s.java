 package html2windows.dom;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 
class ElementInner extends Element implements NodeInter{
     
	public ElementInner(String tagName){
		
	}
	
	public String tagName(){
         return null;
     }
     
     public String getAttribute(String name){
         return name;
     }
     
     public void setAttribute(String name, String value) throws DOMException{}
     
     public void removeAttribute(String name) throws DOMException{}
     
     public Attr getAttributeNode(String name){
         return null;
     }
     
     public Attr setAttributeNode(Attr newAttr) throws DOMException{
         return newAttr;
     }
     
     public Attr removeAttributeNode(Attr oldAttr) throws DOMException{
         return oldAttr;
     }
     
     public NodeList getElementsByTagName(String name){
         return null;
     }
     
     public boolean hasAttribute(String name){
         return false;
     }
 
     @Override
     public String nodeName() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String nodeValue() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public short nodeType() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public Node parentNode() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void setParentNode(Node newParent) {
         // TODO
     }
 
     @Override
     public NodeList childNodes() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node firstChild() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node lastChild() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node previousSibling() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node nextSibling() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public NamedNodeMap attributes() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Document ownerDocument() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void setOwnerDocument(Document newOwnerDocument) {
         // TODO
     }
 
     @Override
     public Node insertBefore(Node newChild, Node refChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node replaceChild(Node newChilde, Node oldChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node removeChild(Node oldChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node appendChild(Node newChild) throws DOMException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean hasChildNodes() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean hasAttributes() {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void addEventListener(String type, EventListener listener,
             boolean useCapture) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void removeEventListener(String type, EventListener listener,
             boolean useCapture) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public boolean dispatchEvent(Event evt) throws EventException {
         // TODO Auto-generated method stub
         return false;
     }
 }
