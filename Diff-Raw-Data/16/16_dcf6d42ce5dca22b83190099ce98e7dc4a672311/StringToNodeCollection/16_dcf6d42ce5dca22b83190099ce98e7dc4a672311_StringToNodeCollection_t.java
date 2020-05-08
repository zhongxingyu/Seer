 package org.jboss.pressgangccms.utils.structures;
 
 import java.util.ArrayList;
 
 import org.jboss.pressgangccms.utils.common.CollectionUtilities;
 import org.w3c.dom.Node;
 
 /**
  * This class is used to map a translation string to collections of XML Nodes.
  * This way the Nodes can be replaced with the XML formed by the translation
  * string.
  */
 public class StringToNodeCollection
 {
 	private String translationString;
 	/**
 	 * The translationString may be unique, while mapping to several sequences
 	 * of nodes
 	 */
 	private ArrayList<ArrayList<Node>> nodeCollections;
 
 	public ArrayList<ArrayList<Node>> getNodeCollections()
 	{
 		return nodeCollections;
 	}
 
 	public void setNodeCollections(ArrayList<ArrayList<Node>> nodeCollections)
 	{
 		this.nodeCollections = nodeCollections;
 	}
 
 	public String getTranslationString()
 	{
 		return translationString;
 	}
 
 	public void setTranslationString(String translationString)
 	{
 		this.translationString = translationString;
 	}
 	
 	public StringToNodeCollection()
 	{
 		
 	}
 	
 	public StringToNodeCollection(final String translationString, final ArrayList<ArrayList<Node>> nodeCollections)
 	{
 		this.translationString = translationString;
 		this.nodeCollections = nodeCollections;
 	}
 	
 	public StringToNodeCollection(final String translationString, final Node node)
 	{
 		this.translationString = translationString;
 		addNode(node);
 	}
 	
 	public StringToNodeCollection(final String translationString)
 	{
 		this.translationString = translationString;
 	}
 	
 	public StringToNodeCollection addNodeCollection(final ArrayList<Node> nodes)
 	{
 		if (this.nodeCollections == null)
 			this.nodeCollections = new ArrayList<ArrayList<Node>>();
 		this.nodeCollections.add(nodes);
 		return this;
 	}
 	
 	public StringToNodeCollection addNode(final Node node)
 	{
 		if (this.nodeCollections == null)
 			this.nodeCollections = new ArrayList<ArrayList<Node>>();
 		this.nodeCollections.add(CollectionUtilities.toArrayList(new Node[] {node}));
 		return this;
 	}
 	
 	@Override
 	public boolean equals(final Object o)
 	{
 	    if (o == null) return false;
 	    if (!(o instanceof StringToNodeCollection)) return false;
 	    if (o == this) return true;
 	    
 	    final StringToNodeCollection stringToNodeCollection = (StringToNodeCollection) o;
 	    if (this.translationString == null && stringToNodeCollection.translationString != null) return false;
 	    if (this.translationString == null && stringToNodeCollection.translationString == null) return true;
 	    if (!this.translationString.equals(stringToNodeCollection.translationString)) return false;
 	    
	    return true;
 	}
 }
