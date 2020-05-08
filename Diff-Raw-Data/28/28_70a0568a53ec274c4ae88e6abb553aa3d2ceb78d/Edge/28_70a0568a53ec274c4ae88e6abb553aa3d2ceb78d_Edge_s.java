 package com.operativus.senacrs.audit.graph.edges;
 
 import com.operativus.senacrs.audit.graph.nodes.Node;
 
 
 public interface Edge {
 	
	Node traverse(Node source);
 }
