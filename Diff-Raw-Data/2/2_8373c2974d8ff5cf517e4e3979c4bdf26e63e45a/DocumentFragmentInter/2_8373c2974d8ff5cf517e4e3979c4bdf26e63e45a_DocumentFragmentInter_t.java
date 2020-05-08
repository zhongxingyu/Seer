 package html2windows.dom;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 import java.util.List;
 import java.util.ArrayList;
 
 class DocumentFragmentInter implements DocumentFragment, NodeInter{
 	private Document ownerDocument;
 
 	private List<Node> childNodes = new ArrayList<Node>();
 
     @Override
     public String nodeName() {
         return "#document-fragment";
     }
 
     @Override
     public String nodeValue() {
         return null;
     }
 
     @Override
     public short nodeType() {
         return DOCUMENT_FRAGMENT_NODE;
     }
 
     @Override
     public Node parentNode() {
         return null;
     }
     
     @Override
     public void setParentNode(Node newParentNode) {
     }
 
     @Override
     public NodeList childNodes() {
     	NodeList list = new NodeList();
     	for(Node node : childNodes){
     		list.add(node);
     	}
         return list;
     }
 
     @Override
     public Node firstChild() {
     	if(!childNodes.isEmpty())
 	        return childNodes.get(0);
         else
         	return null;
     }
 
     @Override
     public Node lastChild() {
     	if(!childNodes.isEmpty()){
        	return childNodes.get(childNodes.size() - 1);
         }
         else
         	return null;
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
         return this.ownerDocument;
     }
     
     @Override
     public void setOwnerDocument(Document newOwnerDocument){
     	this.ownerDocument = newOwnerDocument;
     }
 
     @Override
     public Node insertBefore(Node newChild, Node refChild) throws DOMException {
     	if(refChild != null){
 			if(!childNodes.contains(refChild)){
 				throw new DOMException(DOMException.NOT_FOUND_ERR, "refChild is not found");
 			}
 			int index = childNodes.indexOf(refChild);
 			add(index, newChild);
     	}
     	else{
     		appendChild(newChild);
     	}
         return null;
     }
 
     @Override
     public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
     	if(!childNodes.contains(oldChild)){
     		throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
     	}
     	
     	int index = childNodes.indexOf(oldChild);
     	
     	childNodes.remove(oldChild);
     	((NodeInter)oldChild).setParentNode(null);
     	
     	add(index, newChild);
         
         return oldChild;
     }
 
     @Override
     public Node removeChild(Node oldChild) throws DOMException {
     	if(!childNodes.contains(oldChild)){
     		throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not found");
     	}
     	childNodes.remove(oldChild);
     	((NodeInter)oldChild).setParentNode(null);
         return oldChild;
     }
 
     @Override
     public Node appendChild(Node newChild) throws DOMException {
     	add(childNodes.size() - 1, newChild);
         return newChild;
     }
 
     @Override
     public boolean hasChildNodes() {
         return !childNodes.isEmpty();
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
     
     private void add(int index, Node newChild){
     	if(newChild.ownerDocument() != this.ownerDocument()){
 			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Need import node first");
 		}
 		
     	switch(newChild.nodeType()){
     	case ELEMENT_NODE :
     	case TEXT_NODE :
     	{
     		if(newChild.parentNode() == this && childNodes.indexOf(newChild) > index)
     			index--;
     		newChild.parentNode().removeChild(newChild);
     		childNodes.add(index, newChild);
     		
     		NodeInter newChildInternal = (NodeInter)newChild;
     		newChildInternal.setParentNode(this);
     	}
     	break;
     	
     	case DOCUMENT_FRAGMENT_NODE :
     	{
     		if(newChild == this){
     			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Insert a ancester");
     		}
     		DocumentFragment df = (DocumentFragment)newChild;
     		for(Node child : newChild.childNodes()){
     			add(index++, child);
     		}
 		}
     	break;
     	
     	default :
     		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Unacceptable node type");
     	}
     }
 }
