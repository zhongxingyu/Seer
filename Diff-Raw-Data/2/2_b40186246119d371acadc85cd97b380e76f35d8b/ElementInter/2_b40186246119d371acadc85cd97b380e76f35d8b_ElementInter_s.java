 package html2windows.dom;
 
 import html2windows.css.Style;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.util.ArrayList;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 /** This is the class that implements all abstract method in Element and the Interface NodeInter.
  *  All method is defined in http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-745549614
  *  If you want to see detail how this function work.
  *  See above website.
  * 
  *  @author	CFWei
  */
 @SuppressWarnings(value = { "serial" })
 public class ElementInter extends Element implements NodeInter {
 
     private ArrayList<AttrInter> attributeList = new ArrayList<AttrInter>();
     private String tagNameValue = null;
     private Document ownerDocument;
     private Style elementStyle;
     
     /**
      * This is one of the constructor of this class.
      * You must input one parameter tagName to set this element's tagName.
      * And it will also new its style automatically.
      * 
      * @param	tagName This element's tagName
      */
     public ElementInter(String tagName) {
     	 tagNameValue = tagName;
     	 setStyle(null);
     }
     
     /**
      * This is one of the constructor of this class.
      * You must input two parameter.
      * One is this Element's tagName.
      * One is this Element's style.
      * And the constructor will set this two for you.
      * 
      * @param	tagName This element's tagName
      * @param	style This element's style
      */
     public ElementInter(String tagName,Style style) {
         tagNameValue = tagName;
         setStyle(style);
     }
     
     /**
      * Get this element's tagName.
      * 
      * @return	Retrun this element's tagName
      * 
      */
     public String tagName() {
         return tagNameValue;
     }
     
     /**
      * Retrieves an attribute value by name.
      * 
      * @param	name What attribute you want to get.
      * 
      * @return	Attribute's value as a string. Null will be returned if this element doesn't have this attribute.
      */
     public String getAttribute(String name) {
         for (int i = 0; i < attributeList.size(); i++) {
             if (attributeList.get(i).name().equals(name))
                 return attributeList.get(i).value();
 
         }
         return null;
     }
     
     /**
      * Set one attribute of this element.You send what attribute you want to set and its value.
      * The old attribute will be replace.
      * 
      * @param	name attribute
      * @param	value attribute's value
      * 
      * @exception DOMException 
      */
     public void setAttribute(String name, String value) throws DOMException {
     	Document document=ownerDocument();
     	
     	AttrInter newAttr=(AttrInter)document.createAttribute(name);
 		newAttr.setValue(value);
 		
 		removeAttribute(name);
     	attributeList.add(newAttr);
     }
 
     /** 
      * Removes an attribute by name. 
      * 
      * @param	The name of the attribute to remove.
      */
     public void removeAttribute(String name) throws DOMException {
         for (int i = 0; i < attributeList.size(); i++) {
             if (attributeList.get(i).name().equals(name)) {
                 attributeList.remove(i);
             }
         }
 
     }
 
     /**
      * Retrieves an attribute node by name.
      * 
      * @param	name The name of the attribute to retrieve.
      * @return	The Attr node with the specified name (nodeName) or null if there is no such attribute
      */
     public Attr getAttributeNode(String name) {
         for (int i = 0; i < attributeList.size(); i++) {
             Attr AttributeNode = attributeList.get(i);
             if (AttributeNode.name().equals(name))
                 return AttributeNode;
         }
         return null;
     }
 
     
     /**
      * Adds a new attribute node. 
      * If an attribute with that name (nodeName) is already present in the element, it is replaced by the new one.
      * 
      * @param	newAttr The Attr node to add to the attribute list.
      * @return	If the newAttr attribute replaces an existing attribute, the replaced Attr node is returned, otherwise null is returned
      */
     public Attr setAttributeNode(Attr newAttr) throws DOMException {
 
         Attr returnAttr = null;
         String newAttrName = newAttr.name();
         for (int i = 0; i < attributeList.size(); i++) {
             Attr attributeNode = attributeList.get(i);
             if (attributeNode.name().equals(newAttrName)) {
                 returnAttr = attributeNode;
                 attributeList.remove(i);
                 break;
             }
         }
         attributeList.add((AttrInter)newAttr);
         return returnAttr;
 
     }
 
 
     /**
      * Removes the specified attribute node. 
      * 
      * @param	oldAttr The Attr node to remove from the attribute list.
      * @exception	DOMException NOT_FOUND_ERR: Raised if oldAttr is not an attribute of the element.
      * @return	The Attr node that was removed.
      */
     public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
         if(!attributeList.contains(oldAttr))
             throw new DOMException(DOMException.NOT_FOUND_ERR, "oldAttr is not an attribute of the element.");
         attributeList.remove(oldAttr);
         return oldAttr;
     }
 
     
     /**
      * Returns a NodeList of all the Elements with a given tag name in the order in which they are encountered in a preorder traversal of the Document tree.
      * 
      * @param	name The name of the tag to match on
      * @return	A new NodeList object containing all the matched Elements.
      * 
      */
     public NodeList getElementsByTagName(String name) {
 
         NodeList elementList = new NodeList();
         NodeList childNodeList=childNodes();
         
         for(int i=0;i<childNodeList.length();i++){
             Node child=childNodeList.get(i);
             if(child instanceof Element){
                 NodeList childGetElementsByTagName=((ElementInter) child).getElementsByTagName(name);
                 if(!childGetElementsByTagName.isEmpty())
                     elementList.addAll(childGetElementsByTagName);
 
                 String childTagName=((ElementInter) child).tagName();
                 if(childTagName.equals(name)||name.equals("*"))
                     elementList.add(child);
             }
 
         }
         return elementList;
     }
 
     
     /**
      * Returns whether this node has any attributes.
      * 
      * @param	name boolean true if this node has any attributes, false otherwise.
      * 
      */
     public boolean hasAttribute(String name) {
 
         for (int i = 0; i < attributeList.size(); i++) {
             if (attributeList.get(i).name().equals(name))
                 return true;
         }
 
         return false;
     }
     
     /**
      * Set the style for this element. 
      * If parameter style is null,it will automatically create a new Style for this element.
      * 
      * @param	style style you want to set to this element
      */
     private void setStyle(Style style) {
 
     	if(style==null){
     		elementStyle=new Style(this);
     	}
     	else {
     		elementStyle=style;
     	}
 		
 	
     }
 	
 	
     /**
      * Return this element's style.
      * 
      * @return This element's style.
      */
     public Style getStyle() {
 		
 	return elementStyle;
     }
     
     /**
      * Return this elemet's tag name.
      * 
      * @return This element's tag name. 
      */
     @Override
     public String nodeName() {
         
         return tagName();
     }
     
     /**
      * Return this element's node value.
      * 
      * @return	It is defined null in DOM.
      */
     @Override
     public String nodeValue() {
         
         return null;
     }
     
     
     /**
      * A code representing the type of the underlying object.
      * 
      * @return	The code defined in DOM.
      */
     @Override
     public short nodeType() {
         
         return ELEMENT_NODE;
     }
     
     /**
      * The parent of this node.
      * 
      * @return	The parent of this node
      */
     @Override
     public Node parentNode() {
         
     	Component parent = getParent();
     	if(parent instanceof Node)
     		return (Node)parent;
     	else
     		return null;
     }
     
     
     /**
      * Set the parent of this element.
      * 
      * @param	newParent The parent of this element.
      */
     @Override
     public void setParentNode(Node newParent){
     	Node oldParentNode = parentNode();
     	oldParentNode.removeChild(this);
  
     	newParent.appendChild(this);
     		
     }
     
     /** 
      * Get the child list of this element.
      * 
      * @return	A node list of this element's child.
      * 
      */
     @Override
     public NodeList childNodes(){
     	Component[] components=getComponents();
     	NodeList childNodeList=new NodeList(); 
     	
         for(int i=0;i<components.length;i++){
         	Component eachComponent=components[i];
        	if(eachComponent instanceof Element)
         		childNodeList.add(i,(Node)eachComponent);
         }
            
         return childNodeList;
            
     }
     
     
     /**
      * Get the first child of this element.
      * 
      * @return	The first child of this element.
      */
     @Override
     public Node firstChild(){
     	int count = getComponentCount();
     	if(count==0)
     		return null;
     	Component component=getComponent(0);
     	if(component instanceof Node)
     		return (Node)component;
     	else
     		return null;
 
    }
     
     
     /**
      * Get the last child of this element.
      * 
      * @return	The last child of this element.
      */
     @Override
     public Node lastChild() {
     	
     	int count=getComponentCount();
     	if(count==0)
     		return null;
     	
     	Component component = getComponent(count-1);
     	if(component instanceof Node)
     		return (Node)component;
     	else
     		return null;
     	
     }
     
     /**
      * Find this element's index in the parent's child list.
      * 
      * @return	The index of this element in parent's child list.
      */
     private int findIndexInParent(){
     	
     	Node parent = parentNode();
     	if(parent==null)
     		return -1;
     	
     	NodeList parentChildNodeList = parent.childNodes();
     	for(int i=0;i<parentChildNodeList.size();i++){
     		Node node = parentChildNodeList.item(i);
     		if(node.equals(this))
     			return i;
     	}
     	
     	return -1;
     }
     
     /**
      * Get the previous sibling node in parent's child list.
      * 
      * @return	The previous sibling of this element.
      */
     @Override
     public Node previousSibling() {
     	
     	int thisIndex=findIndexInParent();
     	
     	if(thisIndex!=-1&&thisIndex!=0){
     		Node parent = parentNode();
     		NodeList parentChildNodeList = parent.childNodes();
     		
     		Node previousSiblingComponent = parentChildNodeList.item(thisIndex-1);
     		return previousSiblingComponent;
     		
     	}
 		
     	return null;
     	
     }
     
     
     /**
      * Get the next sibling node in parent's child list.
      * 
      * @return	The next sibling of this element.
      */
     @Override
     public Node nextSibling() {
     	int thisIndex=findIndexInParent();
     	
     	if(thisIndex!=-1){
     		Node parent = parentNode();
     		NodeList parentChildNodeList = parent.childNodes();
     		
     		Node nextSiblingComponent = parentChildNodeList.item(thisIndex+1);
     		return nextSiblingComponent;
     	}
 
     	return null;
 
 
     }
     
     /**
      * A NamedNodeMap containing the attributes of this element.
      * 
      * @return	A NamedNodeMap containing the attributes of this element.
      * 
      */
     @Override
     public NamedNodeMap attributes() {
         
         if(attributeList.size()==0)
             return null;
 
         NamedNodeMap returNamedNodeMap=new NamedNodeMap();
         for(int i=0;i<attributeList.size();i++)
             returNamedNodeMap.setNamedItem(attributeList.get(i));
 
         return returNamedNodeMap;
 
     }
     
     /**
      * Get the ownerDocument of this element.
      * 
      * @return	The owner document of this element.
      */
     @Override
     public Document ownerDocument() {
         
         return ownerDocument;
 
     }
 
     /**
      * Set the ownerDocument of this element.
      * 
      * @param	The ownerDocument of this element. 
      */
     @Override
     public void setOwnerDocument(Document newOwnerDocument) {
         
         ownerDocument=newOwnerDocument;
         
 
     }
     
     /**
      * Find the child index in this element's child list. 
      * 
      * @param	refChild The child you want to find.
      * @return	The index of refChild.
      */
     public int findChildIndex(Node refChild){
     	
     	
     	NodeList childNodeList=childNodes();
     	for(int i=0;i<childNodeList.length();i++){
     		Node node = childNodeList.item(i);
     		if(node.equals(refChild))
     			return i;
     		
     	}
     	
     
     	
     	return -1;
     }
     
     
     /**
      * Inserts the node newChild before the existing child node refChild. 
      * If refChild is null, insert newChild at the end of the list of children.
      * 
      * @param	newChild The node to insert.
      * @param	refChild The node before which the new node must be inserted.
      * 
      * @return	The node being inserted.
      */
     @Override
     public Node insertBefore(Node newChild, Node refChild) throws DOMException {
         int index=findChildIndex(refChild);
         
         if(index!=-1){
         	if(newChild instanceof Element || newChild instanceof Text){
         		add((Component)newChild, index);
         		return refChild;
         	}
         	else{
         		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"HIERARCHY_REQUEST_ERR");
         		
         	}
         	
         }
 
         else{
         	appendChild(newChild);
         	return null;
         }
 
     }
     
     /**
      * Replaces the child node oldChild with newChild in the list of children, and returns the oldChild node.
      * 
      * @param	newChild The new node to put in the child list.
      * @param	oldChild The node being replaced in the list.
      * 
      * @return	The node replaced.
      * 
      */
     @Override
     public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
         
     	 int index=findChildIndex(oldChild);
          if(index!=-1){
          	if(newChild instanceof Element || newChild instanceof Text){
          		remove(index);
          		add((Component)newChild, index);
          		return oldChild;
          	}
          	else{
          		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"HIERARCHY_REQUEST_ERR");
          	}
          	
          	
          }
 
          return null;
 
     }
     
     
     /**
      * Removes the child node indicated by oldChild from the list of children, and returns it.
      * 
      * @param	oldChild The node being removed.
      * 
      * @return	The node removed.
      * 
      */
     @Override
     public Node removeChild(Node oldChild) throws DOMException {
         if(oldChild instanceof Element || oldChild instanceof Text){
         	remove((Component)oldChild);
         	return oldChild;
         }
         else{
         	throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"HIERARCHY_REQUEST_ERR");
         	
         }
     }
     
     /**
      * Adds the node newChild to the end of the list of children of this node. 
      * If the newChild is already in the tree, it is first removed.
      * 
      * @param	newChild The node to add.
      * @return	The node added.
      */
     @Override
     public Node appendChild(Node newChild) throws DOMException {
     	if(newChild instanceof Element ||newChild instanceof Text){
     		add((Component)newChild);
         	return newChild;
     	}
     	else{
     		throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,"HIERARCHY_REQUEST_ERR");
     	}
     	
     	
     	
 
     }
     
     /**
      * Returns whether this node has any children.
      * 
      * @return	true if this node has any children, false otherwise.
      */
     @Override
     public boolean hasChildNodes() {
         
     	int childCount=getComponentCount();
     	
         if (childCount== 0)
             return false;
         else
             return true;
 
     }
     
     /**
      * Returns whether this element has any attributes.
      * 
      * @return	true if this node has any attributes, false otherwise.
      */
     @Override
     public boolean hasAttributes() {
         
         if (attributeList.size() > 0)
             return true;
         else
             return false;
 
     }
     
     
     
     
     @Override
     public void addEventListener(String type, EventListener listener,
             boolean useCapture) {
         
 
 
     }
 
     @Override
     public void removeEventListener(String type, EventListener listener,
             boolean useCapture) {
         
     }
 
     @Override
     public boolean dispatchEvent(Event evt) throws EventException {
         
         return false;
 
     }
 
 
 
 	
 }
 
