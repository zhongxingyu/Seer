 package html2windows.dom;
 import java.awt.Component;
 
 import javax.swing.JFrame;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 
 /**
  * Document is top window.
  *
  * The Document is not standard DOM Document. It removes some methods which
  * handle namespace and prefix.
  * 
  * One Document contains only one Node, whose type is Element.
  */
  
 @SuppressWarnings("serial")
 
 public class Document extends JFrame implements Node{
 	public Element documentElement(){
 		try{
			return (Element)getContentPane().getComponent(0);
 		}
 		catch(Exception ex){
 			return null;
 		}
 	}
 	
 	public Element createElement(String tagName) throws DOMException{
 		ElementInter element = new ElementInter(tagName);
 		element.setOwnerDocument(this);
 		return element;
 	}
 	
 	public DocumentFragment createDocumentFragment(){
 		DocumentFragmentInter documentFragement = new DocumentFragmentInter();
 		documentFragement.setOwnerDocument(this);
 		return documentFragement;
 	}
 	
 	public Text createTextNode(String data){
 		TextInter text = new TextInter(data);
 		text.setOwnerDocument(this);
 		return text;
 	}
 	
 	public Attr createAttribute(String name) throws DOMException{
 		AttrInter attr = new AttrInter(name);
 		attr.setOwnerDocument(this);
 		return attr;
 	}
 	
 	/**
 	 * Get list of elements which have specific tag name.
 	 *
 	 * Now we do not support live NodeList but a static NodeList so you need to
 	 * call this function right before you use it to get the correct snapshot.
 	 */
 	public NodeList getElementsByTagName(String tagname){
 		if(documentElement() != null)
 			return documentElement().getElementsByTagName(tagname);
 		else
 			return new NodeList();
 	}
 	
 	public Node importNode(Node importedNode, boolean deep) throws DOMException{
 		NodeInter newNode = null;
 	
 		switch(importedNode.nodeType()){
 		case ATTRIBUTE_NODE :
 			newNode = importNode((Attr)importedNode, deep);
 			break;
 			
 		case DOCUMENT_FRAGMENT_NODE :
 			newNode = importNode((DocumentFragment)importedNode, deep);
 			break;
 			
 		case ELEMENT_NODE :
 			newNode = importNode((Element)importedNode, deep);
 			break;
 			
 		case TEXT_NODE :
 			newNode = importNode((Text)importedNode, deep);
 			break;
 			
 		default :
 			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "The type of node cannot be imported");
 		}
 		
 		newNode.setOwnerDocument(this);
 		
 		return newNode;
 	}
 	
 	public Element getElementById(String elementId){
 		return getElementByIdInternal(documentElement(), elementId);
 	}
 
 	@Override
 	public String nodeName() {
 		return "#document";
 	}
 
 	@Override
 	public String nodeValue() {
 		return null;
 	}
 
 	@Override
 	public short nodeType() {
 		return DOCUMENT_NODE;
 	}
 
 	@Override
 	public Node parentNode() {
 		return null;
 	}
 
 	@Override
 	public NodeList childNodes() {
 		NodeList list = new NodeList();
 		Node child = documentElement();
 		if(child != null)
 			list.add(child);
 		return list;
 	}
 	
 	@Override
 	public Node firstChild() {
 		return documentElement();
 	}
 
 	@Override
 	public Node lastChild() {
 		return documentElement();
 	}
 
 	@Override
 	public Node previousSibling() {
 		return null;
 	}
 
 	@Override
 	public Node nextSibling() {
 		return null;
 	}
 
 	@Override
 	public NamedNodeMap attributes() {
 		return null;
 	}
 
 	@Override
 	public Document ownerDocument() {
 		return null;
 	}
 
 	@Override
 	public Node appendChild(Node newChild) throws DOMException {
 		if(newChild != null){
 			setDocumentElement(newChild);
 		}
 		else{
 			throw new NullPointerException("newChild is null");
 		}
 		return newChild;
 	}
 
 	@Override
 	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
 		if(refChild == null){
 			appendChild(newChild);
 		}
 		else{
 			if(refChild != documentElement()){
 				throw new DOMException(DOMException.NOT_FOUND_ERR, "refChild is not found");
 			}
 			else{
 				if(newChild != null){
 					if(newChild != documentElement()){
 						throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document has only one child node");
 					}
 					else{
 						setDocumentElement(newChild);
 					}
 				}
 				else{
 					throw new NullPointerException("newChild is null");
 				}
 			}
 		}
 		return newChild;
 	}
 
 	@Override
 	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
 		if(oldChild != documentElement()){
 			throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
 		}
 		if(newChild == null){
 			throw new NullPointerException("newChild is null");
 		}
 		
 		setDocumentElement(newChild);
 		return newChild;
 	}
 
 	@Override
 	public Node removeChild(Node oldChild) throws DOMException {
 		if(oldChild != documentElement()){
 			throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
 		}
 		setDocumentElement((Element)null);
 		
 		return oldChild;
 	}
 
 	@Override
 	public boolean hasChildNodes() {
 		return documentElement() != null;
 	}
 
 	@Override
 	public boolean hasAttributes() {
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
 	
 	@Override
 	protected void addImpl(Component comp, Object constraints, int index){
 		if(documentElement() == null){
 			if(comp instanceof Element){
 				super.addImpl(comp, constraints, index);
 			}
 		}
 	}
 
 	private Element getElementByIdInternal(Element parent, String elementId){
 		if(parent != null){
 			String id = parent.getAttribute("id");
 			if(id != null && id.equals(elementId)){
 				return parent;
 			}
 			else{
 				for(Node childNode : childNodes()){
 					if(childNode.nodeType() == ELEMENT_NODE){
 						Element match = getElementByIdInternal((Element)childNode, elementId);
 						if(match != null){
 							return (Element) childNode;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private void setDocumentElement(Node node) throws DOMException{
 		switch(node.nodeType()){
 			case ELEMENT_NODE : 
 				setDocumentElement((Element)node);
 				break;
 			case DOCUMENT_FRAGMENT_NODE : 
 				setDocumentElement((DocumentFragment)node);
 				break;
 			default :
 				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Unacceptable node type");
 		}
 	}
 
	private void setDocumentElement(Element element) throws DOMException{
 		if(element.ownerDocument() != this){
 			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Need import the node first");
 		}
 		Element oldChild = documentElement();
 		if(oldChild != null)
			getContentPane().remove(oldChild);
 		if(element != null){
			getContentPane().add(element);
 		}
 	}
 
 	private void setDocumentElement(DocumentFragment documentFragement) throws DOMException{
 		NodeList children = documentFragement.childNodes();
 		if(children.length() > 1){
 			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Document has only one child node");
 		}
 		setDocumentElement(children.item(0));
 	}
 	
 	private NodeInter importNode(Attr importedNode, boolean deep){
 		AttrInter attr = (AttrInter)createAttribute(importedNode.nodeName());
 		for(Node child : attr.childNodes()){
 			Node newChild = importNode(child, deep);
 			attr.appendChild(newChild);
 		}
 		return attr;
 	}
 	
 	private NodeInter importNode(DocumentFragment importedNode, boolean deep){
 		DocumentFragmentInter df = (DocumentFragmentInter)createDocumentFragment();
 		if(deep){
 			for(Node child : importedNode.childNodes()){
 				Node newChild = importNode(child, deep);
 				df.appendChild(newChild);
 			}
 		}
 		return df;
 	}
 	
 	private NodeInter importNode(Element importedNode, boolean deep){
 		ElementInter element = (ElementInter)createElement(importedNode.nodeName());
 		
 		// Import attributes
 		NamedNodeMap attributes = element.attributes();
 		for(int i = 0; i < attributes.length(); i++){
 			Attr newAttr = (Attr)importNode(attributes.item(i), deep);
 			element.setAttributeNode(newAttr);
 		}
 		
 		// Import children
 		if(deep){
 			for(Node child : element.childNodes()){
 				Node newChild = importNode(child, deep);
 				element.appendChild(newChild);
 			}
 		}
 		
 		return element;
 	}
 	
 	private NodeInter importNode(Text importedNode, boolean deep){
 		TextInter text = (TextInter)createTextNode(importedNode.data());
 		return text;
 	}
 }
