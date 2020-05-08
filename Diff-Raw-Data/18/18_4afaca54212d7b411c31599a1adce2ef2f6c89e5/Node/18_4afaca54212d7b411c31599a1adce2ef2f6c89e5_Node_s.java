 package com.operativus.senacrs.audit.graph.nodes;
 

 public interface Node {
	
 	String END_TO_STRING = "END";
 	String START_TO_STRING = "START";
 	Node START = new Node() {
		
 		public String toString() {
			
 			return START_TO_STRING;
 		};
 	};
 	Node END = new Node() {
		
 		public String toString() {
			
 			return END_TO_STRING;
 		};
 	};
 }
