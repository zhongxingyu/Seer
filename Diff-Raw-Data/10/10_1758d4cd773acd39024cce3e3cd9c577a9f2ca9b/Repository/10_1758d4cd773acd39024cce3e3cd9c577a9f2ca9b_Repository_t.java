 package org.merriew.core;
 
 import java.io.Serializable;
 
 public class Repository implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4364196964928909131L;
 
 	private String uri;
 	
 	private String name;
	
	private Project project;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
 
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
