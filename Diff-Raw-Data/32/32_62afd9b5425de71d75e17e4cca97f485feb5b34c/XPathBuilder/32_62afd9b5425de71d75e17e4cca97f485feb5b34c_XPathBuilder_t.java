 /*
 	XPathBuilder.java
 
 	Author: David Fogel
 	Copyright 2009 David Fogel
 	All rights reserved.
 */
 
 package net.markout.content;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.markout.support.Namespace;
 import net.markout.types.Attribute;
 import net.markout.types.Name;
 import net.markout.types.NamespaceURI;
 
 /**
  * XPathBuilder
  * 
  * Comment here.
  */
 public class XPathBuilder {
 	// *** Class Members ***
 	public enum What {first_element, all_elements, first_element_content, all_element_content}
 	public enum Match {equals, starts_with, contains}
 
 	// *** Instance Members ***
 	private What what;
 	private Name name;
 	private Name[] path;
 	private Map<Attribute, Match> atts;
 
 	// *** Constructors ***
 	public XPathBuilder(What what) {
 		this.what = what;
 		atts = new HashMap<Attribute, Match>();
 	}
 
 	// *** Interface Methods ***
 
 	// *** Public Methods ***
 	
 	public XPathBuilder named(Name name) {
 		this.name = name;
 		return this;
 	}
 	
 	public XPathBuilder withPath(Name... path) {
 		this.path = path;
 		return this;
 	}
 	
 	public XPathBuilder withAttribute(Attribute att) {
 		this.atts.put(att, Match.equals);
 		return this;
 	}
 	
 	public XPathBuilder withAttributeStartingWith(Attribute att) {
 		this.atts.put(att, Match.starts_with);
 		return this;
 	}
 	
 	public XPathBuilder withAttributeContaining(Attribute att) {
 		this.atts.put(att, Match.contains);
 		return this;
 	}
 	
 	public XPath xpath() {
 		
		Namespace ns = new Namespace();
 		
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append('/');
 		
 		if (path != null) {
 			for (Name n : path) {
 				appendName(sb, n, ns);
 				sb.append('/');
 			}
 		} else {
 			
 			sb.append('/');
 		}
 		
 		if (name != null)
 			appendName(sb, name, ns);
 		else
 			sb.append('*');
 		
		if (!atts.isEmpty()) {
 			sb.append('[');
 			
 			boolean first = true;
 			for (Attribute a : atts.keySet()) {
 				if (first)
 					first = false;
 				else
 					sb.append(" and ");
 				
 				Match m = atts.get(a);
 				switch(m) {
 				case equals:
 					sb.append('@');
 					appendAttributeName(sb, a.getName(), ns);
 					sb.append('=');
 					sb.append(a.quotedValueString());
 					break;
 				
 				case starts_with:
 					sb.append("starts-with( @");
 					appendAttributeName(sb, a.getName(), ns);
 					sb.append(", ");
 					sb.append(a.quotedValueString());
 					sb.append(')');
 					break;
 					
 				case contains:
 					sb.append("contains( @");
 					appendAttributeName(sb, a.getName(), ns);
 					sb.append(", ");
 					sb.append(a.quotedValueString());
 					sb.append(')');
 					break;
 				}
 			}
 			
 			sb.append(']');
 		}
 		
 		switch (what) {
 		case first_element:
 			sb.append("[1]");
 			break;
 			
 		case first_element_content:
 			sb.append("[1]/*");
 			break;
 			
 		case all_element_content:
 			sb.append("/*");
 			break;
 			
 		case all_elements:
 			//nothing
 			break;
 		}
 		
 		List<NamespaceURI> uris = ns.getOwnPrefixedNamespaceURIs();
 		
 		return new XPath(sb.toString(), uris.toArray(new NamespaceURI[uris.size()]));
 	}
 
 	// *** Protected Methods ***
 
 	// *** Package Methods ***
 
 	// *** Private Methods ***
 	
 	private void appendName(StringBuilder sb, Name n, Namespace ns) {
 		NamespaceURI uri = n.getNamespaceURI();
 		if (uri != null)
 			sb.append(ns.namespaceURIPrefix(uri).toString()).append(':');
 		sb.append(n.toString());
 	}
 	
 	private void appendAttributeName(StringBuilder sb, Name n, Namespace ns) {
 		// Really not sure how to do this
 		// it looks like we're not supposed to render the prefix of the att name
 		// if it's namespace is the same as it's containing element.
 		// but sometimes we don't know what that element is!
 		// current strategy: use the path, if present, to guess...
 		
 		NamespaceURI uri = n.getNamespaceURI();
 		
 		if (uri == null) {
 			// no namespace, no problem
 			sb.append(n.toString());
 			return;
 		}
 		
 		// check the name of the element we're querying for
 		if (name != null) {
 			NamespaceURI elURI = name.getNamespaceURI();
 			if (elURI != null && elURI.equals(uri)) // same namespace
 				sb.append(n.toString());
 			else
 				sb.append(ns.namespaceURIPrefix(uri).toString()).append(':').append(n.toString());
 			
 			return;
 		}
 		
 		// here's where we get a little crazy:
 		if (path != null && path.length > 0) {
 			Name lastEl = path[path.length - 1];
 			NamespaceURI elURI = lastEl.getNamespaceURI();
 			if (elURI != null && elURI.equals(uri)) // same namespace
 				sb.append(n.toString());
 			else
 				sb.append(ns.namespaceURIPrefix(uri).toString()).append(':').append(n.toString());
 			
 			return;
 		}
 		
 		// give up and just print the namespace.
 		// it probably won't work unless the attribute is actually 
 		// not in the same namespace as it's containing element.
 		sb.append(ns.namespaceURIPrefix(uri).toString()).append(':').append(n.toString());
 	}
 
 	// *** Private Classes ***
 }
