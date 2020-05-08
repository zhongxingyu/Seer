 package com.bizosys.unstructured;
 
 public abstract class DocumentMetadata {
	public byte[] filter;
 	
	public DocumentMetadata(byte[] filter) {
 		this.filter = filter;
 	}
 
 	public abstract String getTexualFilterLine();
 }
