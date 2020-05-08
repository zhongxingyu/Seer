 package XMLParsing;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import XMLParsing.XMLTree.XMLNode.XMLAttribute;
 
 /**
  * A class that contains the data that was read from (or that will be written
  * to) an XML file.
  * 
  * The class also contains functions (like cNode(), finishNode() and reset())
  * and support (see newNode(), newAttribute()) to keep a current position
  * (current node) in the tree. See the JavaDoc for the functions for particular
  * details.
  * 
  * Most functions return the tree itself, so that chained calls can be used.
  * E.g. new
  * XMLTree(root).newNode(node1).newAttribute(someAttribute).finishNode()
  * .newNode(nodeSibilingWithNode1);
  * 
  * @author Andrei Olaru
  * 
  */
 public class XMLTree {
 	/**
 	 * A class that contains the data corresponding to a node (element) in an
 	 * {@link XMLTree} / XML file.
 	 * 
 	 * Contains a name, a string / integer value, attributes, and child nodes.
 	 * 
 	 * Some functions return the node itself, so that chained calls can be used.
 	 * E.g. new XMLNode(name).setValue("value").addNode("child");
 	 * 
 	 * @author Andrei Olaru
 	 * 
 	 */
 	public static class XMLNode {
 		/**
 		 * An iterator that iterates only over the child nodes that have a
 		 * certain name (specified in constructor).
 		 * 
 		 * @author Andrei Olaru
 		 * 
 		 */
 		public class NodeIterator implements Iterator<XMLNode> {
 			String	search	= null;
 			int		cIndex	= 0;
 
 			public NodeIterator(String searchName) {
 				search = searchName;
 			}
 
 			@Override
 			public boolean hasNext() {
 				while (cIndex < nodes.size()) {
 					if (nodes.get(cIndex).name.equals(search))
 						return true;
 					cIndex++;
 				}
 				return false;
 			}
 
 			@Override
 			public XMLNode next() {
 				while (cIndex < nodes.size()) {
 					if (nodes.get(cIndex).name.equals(search))
 						return nodes.get(cIndex++);
 					cIndex++;
 				}
 				return null;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		}
 
 		/**
 		 * 
 		 * @author Andrei Olaru
 		 * 
 		 */
 		public static class XMLAttribute {
 			String	name		= null;
 			String	value		= null;
 			String	namespace	= null;
 
 			public XMLAttribute(String attNS, String attrName, String attrValue) {
 				super();
 				this.name = attrName;
 				this.value = attrValue;
 				this.namespace = attNS; 
 			}
 		}
 
 		protected String			name		= null;
 		protected String			namespace	= null;
 		protected String			fullName	= null;
 
 		protected Set<XMLAttribute>	attributes	= new HashSet<XMLAttribute>();
 
 		protected String			valueString	= null;
 		protected Integer			valueInt	= null;
 		protected Double			valueDouble	= null;
 		protected List<XMLNode>		nodes		= new LinkedList<XMLNode>();
 
 		public XMLNode(String fullNodeName) {
 			parseName(fullNodeName);
 		}
 
 		public XMLNode(XMLNode node) {
 			name = new String(node.name);
 			namespace = new String(node.namespace);
 			fullName = new String(node.fullName);
 			attributes.addAll(node.attributes);
 
 			if (node.valueString != null)
 				valueString = new String(node.valueString);
 			if (node.valueInt != null)
 				valueInt = new Integer(node.valueInt.intValue());
 			if (node.valueDouble != null)
 				valueDouble = new Double(node.valueDouble.doubleValue());
 
 			nodes.addAll(node.nodes);
 		}
 
 		protected void parseName(String fullNodeName) {
 			int columnIndex = fullNodeName.lastIndexOf(':');
 			if (columnIndex < 0) { // no prefix
 				namespace = null;
 				name = fullNodeName;
 			} else {
 				namespace = fullNodeName.substring(0, columnIndex);
 				name = fullNodeName.substring(columnIndex + 1);
 			}
 			this.fullName = fullNodeName;
 		}
 
 		public XMLNode setValue(String string) {
 			valueString = string;
 			return this;
 		}
 
 		public XMLNode setValue(int integer) {
 			valueInt = new Integer(integer);
 			return this;
 		}
 
 		public XMLNode setValue(double dou) {
 			valueDouble = new Double(dou);
 			return this;
 		}
 
 		public XMLNode addNode(XMLNode node) {
 			nodes.add(node);
 			return this;
 		}
 
 		public XMLNode setAttribute(String attrName, String attrValue) {
 			attributes.remove(attrName);
 			addAttribute(new XMLAttribute("", attrName, attrValue));
 			return this;
 		}
 
 		public XMLNode addAttribute(XMLAttribute attr) {
 			attributes.add(attr);
 			return this;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Object getValue() {
 			if (valueString != null)
 				return valueString;
 			if (valueInt != null)
 				return valueInt;
 			if (valueDouble != null)
 				return valueDouble;
 			return null;
 		}
 
 		public List<XMLNode> getNodes() {
 			return nodes;
 		}
 
 		/**
 		 * @param childName
 		 *            the name (a {@link String}) of the searched node.
 		 * @return the first child node with the specified name.
 		 */
 		public XMLNode getFirstNode(String childName) {
 			return getNode(childName, 0);
 		}
 
 		/**
 		 * @param childName
 		 *            the name (a {@link String}) of the searched node.
 		 * @param index
 		 *            index (0 - based) of the child with the specified name
 		 *            that should be returned. Only non-negative values are
 		 *            accepted.
 		 * @return the n-th child node with the specified name (n is the given
 		 *         index). Returns null if there is no child with this name or
 		 *         at this index.
 		 */
 		public XMLNode getNode(String childName, int index) {
 			if (index < 0)
 				throw new IllegalArgumentException("Index should be non-negative.");
 
 			int count = -1;
 			for (XMLNode child : nodes) {
 				if (child.getName().equals(childName))
 					count++;
 
 				if (index == count)
 					return child;
 			}
 			// child not found
 			return null;
 		}
 
 		public Set<XMLAttribute> getAttributes() {
 			return attributes;
 		}
 
 		public String getAttributeValue(String attributeName) {
 			for (XMLAttribute attr : attributes)
 				if (attr.name.equals(attributeName))
 					return attr.value;
 			return null;
 		}
 
 		/**
 		 * @return a {@link NodeIterator} for all child nodes
 		 */
 		public Iterator<XMLNode> getNodeIterator() {
 			return nodes.iterator();
 		}
 
 		/**
 		 * @param search
 		 *            the name of the nodes to iterate over.
 		 * @return a {@link NodeIterator} for child nodes with the name given by
 		 *         search.
 		 */
 		public Iterator<XMLNode> getNodeIterator(String search) {
 			return new NodeIterator(search);
 		}
 
 		/**
 		 * @return a {@link String} representation of the node's name, value and
 		 *         attributes (one line).
 		 */
 		@Override
 		public String toString() {
 			String ret = "";
 			ret += namespace + ":" + name;
 			if (valueString != null)
 				ret += "[ String : " + valueString + "]";
 			if (valueInt != null)
 				ret += "[ Integer : " + valueInt + "]";
 			if (valueDouble != null)
 				ret += "[ Double : " + valueDouble + "]";
 			for (XMLAttribute attr : attributes)
 				ret += " [" + attr.name + ":" + attr.value + "]";
 			return ret;
 		}
 
 		/**
 		 * @return a {@link String} representation of this node's name value,
 		 *         attributes and children (recursive). Each node has one line.
 		 */
 		public String toStringDepth() {
 			System.out.println();
 			return this.toStringDepth("");
 		}
 
 		/**
 		 * @param indent
 		 *            to apply to the output.
 		 * @return a {@link String} representation of this node's name value,
 		 *         attributes and children (recursive). Each node has one line.
 		 */
 		public String toStringDepth(String indent) {
 			String ret = indent + this.toString() + "\n";
 			for (XMLNode node : this.nodes)
 				ret += node.toStringDepth(indent + "\t");
 			// ret += node.toStringDepth(indent + "|\t"); // alternative
 			// printout, with vertical lines
 			return ret;
 		}
 
 		public Node toElement(Document document) {
 
 			Element node = document.createElementNS(this.namespace, this.name);
 			
 			if (valueDouble != null){
 				node.appendChild(document.createTextNode(valueDouble.toString()));
 			}
 			else if (valueInt != null){
 				node.appendChild(document.createTextNode(valueInt.toString()));
 			}
 			else if (valueString != null){
 				node.appendChild(document.createTextNode(valueString));
 			}
 			else {
 				for (XMLAttribute attribute : attributes){
 //					System.out.println(attribute.value);
 //					System.out.println(attribute.name);
 					String split[] = attribute.name.split(":");
 					if(split.length < 2){
 						node.setAttribute(split[0], attribute.value);
 					}
 					else{
 						node.setAttributeNS(split[0], split[1], attribute.value);
 					}
 				}
 				for (XMLNode child : nodes)
 					node.appendChild(child.toElement(document));
 			}
 			return node ;
 		}
 		
 	}
 
 	XMLNode			root	= null;
 
 	/**
 	 * Keeps information for finding the current position in the tree (and
 	 * allowing going up and down in the tree). stack.peek() is the current
 	 * node.
 	 */
 	Stack<XMLNode>	stack	= new Stack<XMLNode>();
 
 	/**
 	 * Creates a new {@link XMLTree}. The current node becomes the root.
 	 * 
 	 * @param rootNode
 	 *            the root {@link XMLNode} of the new tree
 	 */
 	public XMLTree(XMLNode rootNode) {
 		this.root = rootNode;
 		stack.push(rootNode);
 	}
 
 	public XMLNode getRoot() {
 		return root;
 	}
 
 	/**
 	 * @return the current node (see {@link XMLTree}).
 	 */
 	public XMLNode currentNode() {
 		if (stack.empty())
 			return null;
 		return stack.peek();
 	}
 
 	/**
 	 * Inserts a new node in the tree, as a child of the current node. The new
 	 * node becomes current node.
 	 * 
 	 * @param node
 	 *            the {@link XMLNode} to insert.
 	 * @return the updated {@link XMLTree}.
 	 */
 	public XMLTree newNode(XMLNode node) {
 		stack.peek().addNode(node);
 		stack.push(node);
 		return this;
 	}
 
 	/**
 	 * Inserts a new attribute in the current node.
 	 * 
 	 * @param attr
 	 *            the {@link XMLAttribute} to insert.
 	 * @return the updated {@link XMLTree}.
 	 */
 	public XMLTree newAttribute(XMLAttribute attr) {
 		stack.peek().addAttribute(attr);
 		return this;
 	}
 
 	/**
 	 * Alias of moveToChild()
 	 */
 	public XMLTree moveDown(XMLNode node) {
 		return moveToChild(node);
 	}
 
 	/**
 	 * The specified child of the current node becomes current node.
 	 * 
 	 * @param node
 	 *            {@link XMLNode} to become current node; must be a child of the
 	 *            current node.
 	 * @return the {@link XMLTree}.
 	 * @throws IllegalArgumentException
 	 *             if the specified node is not a child of the current node.
 	 */
 	public XMLTree moveToChild(XMLNode node) {
 		if (stack.peek().getNodes().contains(node)) {
 			stack.push(node);
 			return this;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	/**
 	 * Alias of finishNode().
 	 */
 	public XMLTree moveUp() {
 		return finishNode();
 	}
 
 	/**
 	 * Goes up one level in the tree: the parent of the current node becomes
 	 * current node.
 	 * 
 	 * @return the {@link XMLTree}.
 	 */
 	public XMLTree finishNode() {
 		stack.pop();
 		return this;
 	}
 
 	/**
 	 * The root of the {@link XMLTree} becomes current node.
 	 * 
 	 * @return the {@link XMLTree}.
 	 */
 	public XMLTree reset() {
 		stack.clear();
 		stack.push(root);
 		return this;
 	}
 
 	/**
 	 * The function does not affect the current node.
 	 * 
 	 * @return a {@link String} representation of the tree, one node on each
 	 *         line, correctly indented.
 	 */
 	@Override
 	public String toString() {
 		return root.toStringDepth();
 	}
 
 	public Document toDocument() {
 		Document document = null;
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			document = builder.newDocument();
 			document.appendChild(root.toElement(document));
 			displayDocument(document);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		
//		// Debugging....
//		Document document2 = null;
//		DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
//		try {
//			DocumentBuilder builder2 = factory2.newDocumentBuilder();
//			document2 = builder2.newDocument();
//			Element em = document2.createElement("test");
//			em.appendChild(document2.createTextNode("ceci est du texte"));
//			document2.appendChild(em);
//			displayDocument(document);
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		}		
 		
 		return document;
 	}
 
 	/**
 	 * Display an XML {@link Document} with the correct indentation
 	 * @param document
 	 */
 	public void displayDocument(Document document){
 		NodeList nodes = document.getChildNodes();
 		displayDocument(nodes, 0);
 	}
 	
 	/**
 	 * Display a {@link NodeList} with indentation specified by depth
 	 * @param nodes nodes to display
 	 * @param depth number of tabulation in order to indent correctly
 	 */
 	public void displayDocument(NodeList nodes, int depth){
 		for(int i=0 ; i<nodes.getLength() ; i++){
 			for(int j=0 ; j<depth ; j++)
 				System.out.print('\t');
 			System.out.print(nodes.item(i).getNamespaceURI() + ":" + nodes.item(i).getNodeName() + "=" + nodes.item(i).getNodeValue());
 			NamedNodeMap attributes = nodes.item(i).getAttributes();
 			if(attributes != null){
 				for(int k=0 ; k<attributes.getLength() ; k++){
 					Attr attribute = (Attr) attributes.item(k);
 					System.out.print("[" +  attribute.getNamespaceURI() + ":" + attributes.item(k).getNodeName() + "=" + attributes.item(k).getNodeValue() + "]");
 				}
 			}
 			System.out.println("");
 			displayDocument(nodes.item(i).getChildNodes(), depth+1);
 		}
 
 	}
 }
