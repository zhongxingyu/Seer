 package com.duckasteroid.ratxml;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.strangegizmo.cdb.Cdb;
 
 /**
  * This represents the read interface to data in the rat-xml. It is something equivalent
  * to the Node in DOM. It represents data in elements and attributes.
  */
 public class Node {
 	protected Node parent;
 	protected Cdb cdb;
 	protected Key key;
 	protected String name;
 	
 	/**
 	 * Create the node in the given document and at the given path
 	 * @param document The owner Rat XML document 
 	 * @param path The path into the document
 	 */
 	public Node(Cdb cdb, Key key, Node parent,  String name) {
 		this.cdb = cdb;
 		this.parent = parent;
 		this.key = key;
 		this.name = name;
 	}
 
 	/**
 	 * The name of the node (e.g. element name or attribute name)
 	 * @return The node name
 	 */
 	public String getName() {
 		int endIndex = name.lastIndexOf('[');
 		if (endIndex < 0) {
 			endIndex = name.length();
 		}
 		return name.substring(0, endIndex);
 	}
 	
 	/**
 	 * The document of this node
 	 * @return The document of this node
 	 */
 	public Document getOwnerDocument() {
 		if (parent instanceof Document) {
 			return (Document)parent;
 		}
 		return parent.getOwnerDocument();
 	}
 	
 	/**
 	 * Get the parent node of this node
 	 * @return The parent or <code>null</code> if this is the root/document
 	 */
 	public Node getParent() {
 		return parent;
 	}
 
 	/**
 	 * Get the child elements of this node. 
 	 * @return The names of the child elements, if any. The list may be empty but never <code>null</code>.
 	 */
 	public Map<String, Node> getChildElements() {
 		final HashMap<String, Node> children = new HashMap<String, Node>();
 		processMetaData(key.getChildMetaDataKey(), new MetaDataHandler() {
 			public void handle(String name, long id) {
 				children.put(name, new Node(cdb, Key.createElementDataKey(id), Node.this, name));
 			}
 		});
 		return children;
 	}
 	
 	/**
 	 * A list of the elements with a given name (in the order they are declared)
 	 * @param elementName the name of the elements to get
 	 * @return A collection with the nodes in it (if any)
 	 */
 	public List<Node> getChildElements(String elementName) {
 		return new NodeList(elementName);
 	}
 	
 	/**
 	 * Returns a collection of all children - attributes and elements
 	 * @return A list of all children (attributes and elements)
 	 */
 	public List<Node> getAllChildren() {
 		final ArrayList<Node> children = new ArrayList<Node>();
 		processMetaData(key.getChildMetaDataKey(), new MetaDataHandler() {
 			public void handle(String name, long id) {
 				children.add(new Node(cdb, Key.createElementDataKey(id), Node.this, name));
 			}
 		});
 		processMetaData(key.getAttributeMetaDataKey(), new MetaDataHandler() {
 			public void handle(String name, long id) {
 				children.add(new Node(cdb, Key.createAttributeDataKey(id), Node.this, name));
 			}
 		});
 		return children;
 	}
 	
 	public List<Node> getOrderedChildElements() {
 		final ArrayList<Node> children = new ArrayList<Node>();
 		processMetaData(key.getChildMetaDataKey(), new MetaDataHandler() {
 			public void handle(String name, long id) {
 				children.add(new Node(cdb, Key.createElementDataKey(id), Node.this, name));
 			}
 		});
 		if (children.size() > 1) {
 			Collections.reverse(children);
 		}		
 		return children;
 	}
 	
 	/**
 	 * Get the attributes on this node. 
 	 * If this node is an attribute or a document, the list will be empty. 
 	 * @return A list of attribute names (if any). List may be empty but never <code>null</code>.
 	 */
 	public Map<String, Node> getAttributes() {
 		final HashMap<String, Node> result = new HashMap<String, Node>();
 		processMetaData(key.getAttributeMetaDataKey(), new MetaDataHandler() {
 			public void handle(String name, long id) {
 				Node n = new Node(cdb, Key.createAttributeDataKey(id), Node.this, name);
 				result.put(name, n);
 			}
 		});
 		return result;
 	}
 
 	private interface MetaDataHandler {
 		public void handle(String name, long id);
 	}
 	/**
 	 * A utility method to gather together all values for a given meta data key
 	 * @param metaDataKey The key to retrieve
 	 * @return A list of all values in that meta data key.
 	 */
 	private void processMetaData(Key metaDataKey, MetaDataHandler handler) {
 		String metaData = null;
 		byte[] b = null;
 		do {
 			if (b == null) {
 				b = cdb.find(metaDataKey.value);
 			}
 			else {
 				b = cdb.findnext(metaDataKey.value);
 			}
 			if (b == null) {
 				metaData = null;
 			} else {
 				metaData = new String(b);
 				String[] element = metaData.split(":");
 				handler.handle(element[0], Long.parseLong(element[1]));
 			}
 		} while (metaData != null);
 
 
 	}
 
 	/**
 	 * Get the text content of this element
 	 * @return The text in this element
 	 */
 	public String getText() {
 		byte[] data = cdb.find(key.value);
 		if (data == null) {
 			return "";
 		}
 		return new String(data);
 	}
 	
 	public boolean isAttribute() {
 		return key.getType() == Key.TYPE_ATTRIBUTE;
 	}
 
 	public String toString() {
 		return Node.class.getName()+"{"+key.toString()+"}";
 	}
 
 	public String getAttributeValue(String key) {
 		Node attr = getAttributes().get(key);
 		if (attr == null)
 		{
 			return null;
 		}
 		return attr.getText();
 	}
 	
 	private class NodeList extends AbstractList<Node>
 	{
 		private String elementName;
 		private Map<String, Node> elements;
 		private Integer size = null;
 		
 		public NodeList(String elementName)
 		{
 			this.elementName = elementName;
 			this.elements = getChildElements();
 		}
 		
 		@Override
         public Node get(int index)
         {
 	        return elements.get(elementName + "["+index+"]");
         }
 
 		@Override
         public synchronized int size()
         {
 	        if (size == null)
 	        {
 	        	size = 0;
 	        	for(Map.Entry<String, Node> entry : elements.entrySet())
 	        	{
 	        		String keyName = entry.getKey();
	        		keyName = keyName.substring(keyName.indexOf('['));
 	        		if (keyName.equals(elementName))
 	        		{
 	        			size ++;
 	        		}
 	        	}
 	        }
 	        return size;
         }
 		
 	}
 }
