 package com.operativus.senacrs.audit.graph.edges;
 
 import com.operativus.senacrs.audit.graph.nodes.Node;
 
 
 public interface Edge {
 	
	void traverse(Node source);
 }
