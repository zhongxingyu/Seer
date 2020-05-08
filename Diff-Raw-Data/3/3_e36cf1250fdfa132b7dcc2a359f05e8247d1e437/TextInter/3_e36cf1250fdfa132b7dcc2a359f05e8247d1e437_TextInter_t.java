 package html2windows.dom;
 
 import java.awt.Component;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventException;
 import org.w3c.dom.events.EventListener;
 
 
 /**
  * This is the class that implements all abstract method in Text and the Interface NodeInter.
  * All method is defined in http://www.w3.org/TR/DOM-Level-2-Core/core.html
  * If you want to see detail how this function work.
  * See above website.
  * 
  * @author cfwei
  */
 class TextInter extends Text implements NodeInter {
 
 	private String dataValue = null;
 	private Document ownerDocument=null;
 
 	/**
 	 * Constructor of this class
 	 * 
 	 * @param data The data you want to set to this text. 
 	 */
 	public TextInter(String data) {
 		dataValue = data;
 	}
 
 	
 	/**
 	 * Breaks this node into two nodes at the specified offset.
 	 * 
 	 * @param offset The offset at which to split, starting from 0.
 	 * @return The new node, of the same type as this node.
 	 * @throws DOMException INDEX_SIZE_ERR: Raised if the specified offset is negative or greater than the length of data of this text.
 	 */
 	public Text splitText(long offset) throws DOMException {
 		if (offset < 0 || offset >= dataValue.length())
 			throw new DOMException(
 					DOMException.INDEX_SIZE_ERR,
 					"The specified offset is negative or greater than the number of 16-bit units in data");
 
 		String newString1 = dataValue.substring(0, (int) offset);
 		String newString2 = dataValue.substring((int) offset + 1);
 		
 		dataValue=newString1;
 		
 		TextInter newTextNode = new TextInter(newString2);
 		
 		Node parentNode=parentNode();
 		if (parentNode != null) {
 			newTextNode.setParentNode(parentNode);
 			NodeList parentsChildNodeList = parentNode.childNodes();
 			int index = parentsChildNodeList.indexOf(this);
 			parentsChildNodeList.add(index + 1, newTextNode);
 		}
 		return newTextNode;
 	}
 
 	
 	/**
 	 * Return the data value of this Text
 	 * 
 	 * @return The data value of this Text
 	 */
 	public String data() {
 		return dataValue;
 	}
 	
 	/**
 	 * Set the data value of this text
 	 * 
 	 * @param data The data value you want to set.
 	 */
 	public void setData(String data) {
 		dataValue = data;
 	}
 	
 	/**
 	 * Return the length of this data value.
 	 * 
 	 * @return The length of data value of this text.
 	 */
 
 	public long length() {
 		return dataValue.length();
 	}
 
 	/**
 	 * Extracts a range of data from the text node.
 	 * 
 	 * @param offset Start offset of substring to extract.
 	 * @param count The number of to extract.
 	 * 
 	 * @return The specified substring. 
 	 */
 	public String substringData(long offset, long count) throws DOMException {
 		return dataValue.substring((int) offset, (int) offset + (int) count);
 	}
 	
 	/**
 	 * Append the string to the end of the character data of the node.
 	 * 
 	 * @param arg The string to append.
 	 * 
 	 */
 	public void appendData(String arg) throws DOMException {
 		dataValue=dataValue.concat(arg);
 	}
 
 	/**
 	 * Insert a string at the specified offset.
 	 * 
 	 * @param offset The character offset at which to insert.
 	 * @param The string to insert.
 	 * 
 	 */
 	public void insertData(long offset, String arg) throws DOMException {
 		String newData = null;
 		newData=newData.concat(dataValue.substring(0, (int) offset));
 		newData=newData.concat(arg);
 		newData=newData.concat(dataValue.substring((int) offset + 1));
 		dataValue = newData;
 	}
 	
 	
 	/**
 	 * Remove a range from the node.
 	 * 
 	 * @param offset The offset from which to start removing.
 	 * @param count The number of 16-bit units to delete.
 	 * 
 	 * @exception DOMException INDEX_SIZE_ERR: Raised if the specified offset is negative or greater than the number in data, or if the specified count is negative.
 	 */
 	public void deleteData(long offset, long count) throws DOMException {
 		if (offset < 0 || count < 0 || offset >= dataValue.length())
 			throw new DOMException(
 					DOMException.INDEX_SIZE_ERR,
 					" The specified offset is negative or greater than the number of 16-bit units in data, or if the specified count is negative");
 		String newData = null;
 		newData=newData.concat(dataValue.substring(0, (int) offset));
 		newData=newData.concat(dataValue.substring((int) offset + (int) count));
 		dataValue = newData;
 	}
 	
 	/**
 	 * Replace the characters starting at the specified offset with the specified string.
 	 * 
 	 * @param offset The offset from which to start replacing.
 	 * @param count The number to replace.
 	 * @param arg The string with which the range must be replaced.
 	 * 
 	 * @exception DOMException INDEX_SIZE_ERR: Raised if the specified offset is negative or greater than the number in data, or if the specified count is negative.
 	 */
 	public void replaceData(long offset, long count, String arg)
 			throws DOMException {
 		if (offset < 0 || count < 0 || offset >= dataValue.length())
 			throw new DOMException(
 					DOMException.INDEX_SIZE_ERR,
 					" The specified offset is negative or greater than the number of 16-bit units in data, or if the specified count is negative");
 
 		String newData = null;
 		newData.concat(dataValue.substring(0, (int) offset));
 		newData.concat(arg);
 		newData.concat(dataValue.substring((int) offset + (int) count));
 		dataValue = newData;
 	}
 	
 	
 	/**
 	 * Return this node name.
 	 * 
 	 * @return The definition name of text.  
 	 * 
 	 */
 	@Override
 	public String nodeName() {
 		
 		return "#text";
 	}
 	
 	
 	/**
 	 * Return the data value of this text
 	 * 
 	 * @return The data value of this text
 	 */
 	@Override
 	public String nodeValue() {
 		
 		return dataValue;
 	}
 	
 	/**
 	 * A code representing the type of the underlying object.
 	 * 
 	 * @return The code defined in DOM
 	 */
 	@Override
 	public short nodeType() {
 	
 		return TEXT_NODE;
 	}
 	
 	
 	/**
 	 * The parent of this node.
 	 * 
 	 * @return The parent of this node
 	 * 
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
 	 * @param newParent The parent of this element.
 	 */
 	@Override
 	public void setParentNode(Node newParent){
 		Node oldParentNode = parentNode();
		if (oldParentNode != null)
			oldParentNode.removeChild(this);
 	 
 	    newParent.appendChild(this);
 	    		
 	}
 	
 	/**
 	 * Get the child list of this text.
 	 * 
 	 * @return A node list of this text's child.
 	 */
 	@Override
 	public NodeList childNodes() {
 		
 		return null;
 	}
 	
 	/**
 	 * Get the first child of this text.
 	 * 
 	 * @return The first child of this text.
 	 */
 	@Override
 	public Node firstChild() {
 		return null;
 	}
 	
 	/**
 	 * Get the last child of this text.
 	 * 
 	 * @return The last child of this text.
 	 */
 	@Override
 	public Node lastChild() {
 		
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
 	 * @return The previous sibling of this element.
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
 	 * @return The next sibling of this element.
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
 	 * A NamedNodeMap containing the attributes of this text.
 	 * 
 	 *  @return A NamedNodeMap containing the attributes of this text.
 	 */
 	@Override
 	public NamedNodeMap attributes() {
 		
 		return null;
 	}
 	
 	/**
 	 * Get the ownerDocument of this text.
 	 * 
 	 * @return The owner document of this text.
 	 */
 	@Override
 	public Document ownerDocument() {
 		
 		return this.ownerDocument;
 	}
 	
 	/**
 	 * Set the ownerDocument of this text.
 	 * 
 	 * @param The ownerDocument of this text. 
 	 */
 	@Override
 	public void setOwnerDocument(Document newOwnerDocument) {
 		
 		this.ownerDocument=newOwnerDocument;
 
 	}
 	
 	/**
 	 * Inserts the node newChild before the existing child node refChild. 
      * If refChild is null, insert newChild at the end of the list of children.
 	 * 
      * @param newChild The node to insert.
      * @param refChild The node before which the new node must be inserted.
      * 
      * @return The node being inserted.
 	 */
 	@Override
 	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
 		
 		return null;
 	}
 	
 	
 	/**
 	 *  Replaces the child node oldChild with newChild in the list of children, and returns the oldChild node.
 	 * 
 	 * @param newChild The new node to put in the child list.
      * @param oldChild The node being replaced in the list.
      * 
      * @return The node replaced.
 	 */
 	@Override
 	public Node replaceChild(Node newChilde, Node oldChild) throws DOMException {
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Removes the child node indicated by oldChild from the list of children, and returns it.
      * 
      * @param oldChild The node being removed.
      * 
      * @return The node removed.
      * 
 	 */
 	@Override
 	public Node removeChild(Node oldChild) throws DOMException {
 		return null;
 	}
 	
 
     /**
      * Adds the node newChild to the end of the list of children of this node. 
      * If the newChild is already in the tree, it is first removed.
      * 
      * @param newChild The node to add.
      * @return The node added.
      */
 	@Override
 	public Node appendChild(Node newChild) throws DOMException {
 		
 		return null;
 	}
 	
 	
 	/**
      * Returns whether this node has any children.
      * 
      * @return true if this node has any children, false otherwise.
      */
 	@Override
 	public boolean hasChildNodes() {
 		
 			return false;
 	}
 	
 	
 	/**
      * Returns whether this element has any attributes.
      * 
      * @return true if this node has any attributes, false otherwise.
      */
 	@Override
 	public boolean hasAttributes() {
 		
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
