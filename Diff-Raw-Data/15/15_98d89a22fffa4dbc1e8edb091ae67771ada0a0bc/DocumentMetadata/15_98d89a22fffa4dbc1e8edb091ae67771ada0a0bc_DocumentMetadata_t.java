 package com.bizosys.unstructured;
 
 public abstract class DocumentMetadata {
	public String filter;
 	
	public DocumentMetadata(String filter) {
 		this.filter = filter;
 	}
 
 	public abstract String getTexualFilterLine();
 }
