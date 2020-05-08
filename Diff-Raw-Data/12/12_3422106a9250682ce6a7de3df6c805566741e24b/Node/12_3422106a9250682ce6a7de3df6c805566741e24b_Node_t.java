 package com.operativus.senacrs.audit.graph.nodes;
 
 
 public interface Node {
 	
 	public static final Node START = new Node() {
		
		public String toString() {
			
			return "START";
		};
	};
	public static final Node END = new Node() {
		
		public String toString() {
			
			return "END";
		};
 	};
 }
