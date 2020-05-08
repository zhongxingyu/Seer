 package org.merriew.core;
 
 import java.io.Serializable;
 
 public class Repository implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4364196964928909131L;
 
 	private String uri;
 	
 	private String name;
 
 	public String getUri() {
 		return uri;
 	}
 
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
