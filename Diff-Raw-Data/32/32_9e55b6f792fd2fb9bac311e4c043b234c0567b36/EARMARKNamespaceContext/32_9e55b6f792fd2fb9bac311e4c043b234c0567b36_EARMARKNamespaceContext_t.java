 package it.essepuntato.earmark.core.xml;
 
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * This class defines an XML namespace context to be used within EARMARK documents, in particular
  * within methods concerning XPath ranges.
  * 
  * @author Silvio Peroni
  *
  */
 public class EARMARKNamespaceContext implements NamespaceContext {
 	
	private Map<String,String> prefixNamespace = new HashMap<String, String>();
	private Map<String,Set<String>> namespacePrefix = new HashMap<String, Set<String>>();
 	
 	/**
 	 * A namespace context for EARMARK documents.
 	 * 
 	 * @param document the XML document to be considered for retrieving the prefix-namespace pairs.
 	 */
 	public EARMARKNamespaceContext(Document document) {
 		Set<String> prefixSetXML = new HashSet<String>();
 		prefixSetXML.add(XMLConstants.XML_NS_PREFIX);
 		
 		Set<String> prefixSetXMLNS = new HashSet<String>();
 		prefixSetXMLNS.add(XMLConstants.XMLNS_ATTRIBUTE);
 		
 		prefixNamespace.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
 		prefixNamespace.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
 		namespacePrefix.put(XMLConstants.XML_NS_URI, prefixSetXML);
 		namespacePrefix.put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefixSetXMLNS);
 		
 		findNamespaces(document);
 	}
 
 	@Override
 	public String getNamespaceURI(String prefix) {
 		return prefixNamespace.get(prefix);
 	}
 
 	@Override
 	public String getPrefix(String ns) {
 		Iterator<String> ite = getPrefixes(ns);
 		return (ite.hasNext() ? ite.next() : null);
 	}
 
 	@Override
 	public Iterator<String> getPrefixes(String ns) {
 		Set<String> set = namespacePrefix.get(ns);
 		if (set == null) {
 			set = new HashSet<String>();
 		}
 		return set.iterator();
 	}
 	
 	/* Fine all the namespaces of a node and add it to the proper lists */
 	private void findNamespaces(Node node) {
 		if (node != null) {
 			String ns = node.getNamespaceURI();
 			String prefix = node.getPrefix();
 		
 			if (prefix == null) {
 				prefix = "";
 			}
 			
 			if (ns == null) {
 				ns = "";
 			}
 		
 			
 			if (!prefixNamespace.containsKey(prefix)) {
 				prefixNamespace.put(prefix, ns);
 			}
 			
 			Set<String> prefixes = namespacePrefix.get(ns);
 			if (prefixes == null) {
 				prefixes = new HashSet<String>();
 				namespacePrefix.put(ns, prefixes);
 			}
 			prefixes.add(prefix);
 			
 			NamedNodeMap attributes = node.getAttributes();
 			if (attributes != null) {
 				int attributesSize = attributes.getLength();
 				for (int i = 0; i < attributesSize ; i++) {
 					findNamespaces(attributes.item(i));
 				}
 			}
 			
 			NodeList children = node.getChildNodes();
 			int childrenSize = children.getLength();
 			for (int i = 0; i < childrenSize ; i++) {
 				findNamespaces(children.item(i));
 			}
 		}
 	}
 
 }
