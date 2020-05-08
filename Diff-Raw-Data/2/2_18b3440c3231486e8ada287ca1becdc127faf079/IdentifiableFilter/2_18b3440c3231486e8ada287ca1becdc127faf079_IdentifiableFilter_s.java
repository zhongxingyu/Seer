 package org.eclipse.stem.core;
 
 /******************************************************************************* 
  * Copyright (c) 2006, 2009 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  *     IBM Corporation - initial API and implementation 
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Label;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 
 /**
  * The class is used to filter out nodes and edges that does not match a pattern in the URI. The pattern is
  * specified in the "coverage" property of the Dublin Core. The format of this property is a list of patterns
  * separated by ';'. 
  * 
  * @author edlund
  *
  */
 public class IdentifiableFilter {
 
 	private String []patterns;
 	public IdentifiableFilter(String filter) {
 		if(filter == null)return;
 		if(filter.contains(":"))return; // Internal use by STEM, not specified by the user
 		StringTokenizer st = new StringTokenizer(filter.toString(), ";");
 		patterns = new String[st.countTokens()];
 		for(int i=0;i<patterns.length;++i) {
 			String pat = st.nextToken();
 			patterns[i]=pat;
 		}
 	}
 	
 	public EMap<URI, Node> filterNodes(EMap<URI, Node>map) {
 		if(patterns == null)return map;
 		ArrayList<URI>remove = new ArrayList<URI>();
 		for(URI uri:map.keySet()) {
 			String lastSegment = uri.lastSegment();
 			boolean keep = false;
 		
 			for(int i=0;i<patterns.length;++i) {
 				String pat = patterns[i];
 				boolean wildcard = false;
 				if(pat.endsWith("*")) {pat = pat.substring(0, pat.length()-1);wildcard = true;}
 				if(wildcard && lastSegment.contains(pat)
 						|| !wildcard && lastSegment.equals(pat)) {keep = true;break;}
 			}
 			if(!keep)remove.add(uri);
 		}
 		for(URI u:remove)map.remove(u);
 		return map;
 	}
 	public EMap<URI, Edge> filterEdges(EMap<URI, Edge>map) {
 		if(patterns == null)return map;
 		ArrayList<URI>remove = new ArrayList<URI>();
 		for(URI uri:map.keySet()) {
 			Edge edge = map.get(uri);
 			boolean foundSource=false, foundDest = false;
 			for(int i=0;i<patterns.length;++i) {
 				String pat = patterns[i];
 				boolean wildcard = false;
 				if(pat.endsWith("*")) {pat = pat.substring(0, pat.length()-1);wildcard = true;}
 				
 				if(wildcard && edge.getNodeAURI().lastSegment().contains(pat)
 						|| !wildcard && edge.getNodeAURI().lastSegment().equals(pat)) foundSource = true;
 				if(wildcard && edge.getNodeBURI().lastSegment().contains(pat)
						|| !wildcard && edge.getNodeBURI().lastSegment().equals(pat)) foundSource = true;			
 			}
 			if(!(foundSource && foundDest))remove.add(uri);
 		}
 		for(URI u:remove)map.remove(u);
 		return map;
 	}
 	public EMap<URI, NodeLabel> filterNodeLabels(EMap<URI, NodeLabel>map) {
 		if(patterns == null)return map;
 		ArrayList<URI>remove = new ArrayList<URI>();
 		for(URI uri:map.keySet()) {
 			String lastSegment = uri.lastSegment();
 			boolean keep = false;
 			for(int i=0;i<patterns.length;++i) {
 				String pat = patterns[i];
 				boolean wildcard = false;
 				if(pat.endsWith("*")) {pat = pat.substring(0, pat.length()-1);wildcard = true;}
 				if(wildcard && lastSegment.contains(pat) 
 						|| !wildcard && lastSegment.equals(pat)) {keep = true;break;}
 			}
 			if(!keep)remove.add(uri);
 		}
 		for(URI u:remove)map.remove(u);
 		return map;
 	}
 	public EMap<URI, Label> filterLabels(EMap<URI, Label>map) {
 		if(patterns == null)return map;
 		ArrayList<URI>remove = new ArrayList<URI>();
 		for(URI uri:map.keySet()) {
 			String lastSegment = uri.lastSegment();
 			boolean keep = false;
 			for(int i=0;i<patterns.length;++i) {		
 				String pat = patterns[i];
 				boolean wildcard = false;
 				if(pat.endsWith("*")) {pat = pat.substring(0, pat.length()-1);wildcard = true;}
 		
 				if(wildcard && lastSegment.contains(pat) ||
 						!wildcard && lastSegment.equals(pat)) {keep = true;break;}
 			if(!keep)remove.add(uri);
 			}
 		}
 		for(URI u:remove)map.remove(u);
 		return map;
 	}
 	public void restrict(IdentifiableFilter other) {
 		// ToDO. The idea is that we need to consolidate a list of patterns into a single
 		// list if there are duplicates or more general patterns
 /*		if(other.patterns == null)return;
 		ArrayList<String>restrictedPatterns = new ArrayList<String>();
 		if(this.patterns != null) for(String mypat:this.patterns)restrictedPatterns.add(mypat);
 		for(String otherpat:other.patterns) if(!restrictedPatterns.contains(otherpat)) restrictedPatterns.add(otherpat);
 		// Now go through and remove patterns that are more general than others.
 		for(String pat:other.patterns) {
 			for(String p:restrictedPatterns)
 				if(!p.equals(pat) && p.contains(pat)) restrictedPatterns.remove(p);
 		}
 		if(this.patterns != null) 
 			for(String pat:this.patterns) {
 				for(String p:restrictedPatterns)
 					if(!p.equals(pat) && p.contains(pat)) restrictedPatterns.remove(p);
 			}
 		this.patterns = new String[restrictedPatterns.size()];
 		for(int i=0;i<restrictedPatterns.size();++i) this.patterns[i] = restrictedPatterns.get(i);
 	*/
 	}
 }
