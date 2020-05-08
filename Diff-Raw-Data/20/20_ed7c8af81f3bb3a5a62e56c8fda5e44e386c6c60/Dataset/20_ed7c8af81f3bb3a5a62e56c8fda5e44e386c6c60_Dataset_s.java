 package com.kdcloud.server.entity;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 public class Dataset implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 
 	Long id;
 	
 	String name;
 	
 	String description;
 	
 	LinkedList<String> committers = new LinkedList<String>();
 	
 	int size;
 	
 	public Dataset() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public Dataset(String name, String description) {
 		super();
 		this.name = name;
 		this.description = description;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public LinkedList<String> getCommitters() {
 		return committers;
 	}
 	
 }
