 /*
 	Namespace.java
 
 	Author: David Fogel
 	Copyright 2009 David Fogel
 	All rights reserved.
 */
 
 package net.markout.support;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.markout.types.Name;
 import net.markout.types.NamespaceURI;
 
 /**
  * Namespace
  * 
  * Comment here.
  */
 public class Namespace {
 	// *** Class Members ***
 	private static final String GENERATED_PREFIX_STRING = "ns";
 	
 	private static final Namespace MANDATORY_NAMESPACE = new Namespace();
 	static {
 		MANDATORY_NAMESPACE.namespaceURIPrefix(NamespaceURI.XML_URI);
 		MANDATORY_NAMESPACE.close();
 	}
 
 	// *** Instance Members ***
 	private Namespace parent;
 	private List<NamespaceURI> prefixURIs;
 	private List<NamespaceURI> unmodPrefixURIs;
 	private NamespaceURI defaultURI;
 	private boolean open;
 	private int lastGeneratedPrefixCount;
 
 	// *** Constructors ***
 	public Namespace() {
 		this(MANDATORY_NAMESPACE);
 	}
 	
 	public Namespace(Namespace parent) {
 		this.parent = parent != null ? parent : MANDATORY_NAMESPACE;
 		clear();
 	}
 
 	// *** Interface Methods ***
 
 	// *** Public Methods ***
 	public NamespaceURI getDefaultNamespaceURI() {
 		return defaultURI != null ? defaultURI : parent != null ? parent.getDefaultNamespaceURI() : null;
 	}
 	
 	public boolean isDefaultNamespaceURI(NamespaceURI uri) {
 		return uri != null && uri.equals(getDefaultNamespaceURI());
 	}
 	
 	public void setDefaultNamespaceURI(NamespaceURI uri) {
 		if (!open)
 			throw new IllegalStateException("can't change a Namespace after it's been closed (declared in start tag)");
 		
 		defaultURI = uri;
 	}
 	
 	public Name namespaceURIPrefix(NamespaceURI uri) {
 		if (prefixURIs != null) {
 			int i = prefixURIs.indexOf(uri);
 			if (i >= 0)
 				return prefixURIs.get(i).getPreferredPrefix();
 		}
 		
 		Name prefix = parent == null ? null : parent.namespaceURIPrefix(uri);
 		
 		if (prefix != null)
 			return prefix;
 		
 		if (!open) // we're closed to new mappings, so we return empty-handed
 			return null;
 		
 		// else we're still open for business, and can add this namespaceURI to our list.
 		if (prefixURIs == null)
 			prefixURIs = new ArrayList<NamespaceURI>();
 		// find out if there's a preferred prefix and if the prefix is already being used:
 		prefix = uri.getPreferredPrefix();
 		if (prefix == null || findNamespaceURI(prefix) != null)
 			uri = uri.withPreferredPrefix(generatePrefix());
 		
 		prefixURIs.add(uri);
 		
 		return uri.getPreferredPrefix();
 	}
 	
 	public boolean isOpen() {return open;}
 	
 	public void close() {open = false;}
 	
 	public void clear() {
 		defaultURI = null;
 		if (prefixURIs != null)
 			prefixURIs.clear();
 		open = true;
		lastGeneratedPrefixCount = parent != null ? parent.lastGeneratedPrefixCount : 0;
 	}
 	
 	public Namespace getParent() {return parent;}
 	
 	public void setParent(Namespace parent) {
 		this.parent = parent != null ? parent : MANDATORY_NAMESPACE;
		lastGeneratedPrefixCount = parent != null ? parent.lastGeneratedPrefixCount : 0;
 	}
 	
 	public NamespaceURI getOwnDefaultNamespaceURI() {
 		return defaultURI;
 	}
 	
 	public List<NamespaceURI> getOwnPrefixedNamespaceURIs() {
 		if (prefixURIs == null)
 			return Collections.emptyList();
 		if (unmodPrefixURIs == null)
 			unmodPrefixURIs = Collections.unmodifiableList(prefixURIs);
 		return unmodPrefixURIs;
 	}
 	
 	public NamespaceURI findNamespaceURI(Name prefix) {
 		if (prefixURIs != null) {
 			int len = prefixURIs.size();
 			for (int i = 0 ; i < len ; i++) {
 				NamespaceURI n = prefixURIs.get(i);
 				if (prefix.toString().equals(n.getPreferredPrefix().toString()))
 					return n;
 			}
 		}
 		return parent == null ? null : parent.findNamespaceURI(prefix);
 	}
 	
 	public List<NamespaceURI> getAllPrefixedNamespaceURIs() {
 		return collectAllPrefixedNamespaceURIs(new ArrayList<NamespaceURI>());
 	}
 
 	// *** Protected Methods ***
 
 	// *** Package Methods ***
 
 	// *** Private Methods ***
 	private List<NamespaceURI> collectAllPrefixedNamespaceURIs(List<NamespaceURI> uris) {
 		if (parent != null)
 			parent.collectAllPrefixedNamespaceURIs(uris);
 		
 		if (prefixURIs != null)
 			uris.addAll(prefixURIs);
 		
 		return uris;
 	}
 	
 	private String generatePrefix() {
 		lastGeneratedPrefixCount++;
 		return GENERATED_PREFIX_STRING + lastGeneratedPrefixCount;
 	}
 
 	// *** Private Classes ***
 }
