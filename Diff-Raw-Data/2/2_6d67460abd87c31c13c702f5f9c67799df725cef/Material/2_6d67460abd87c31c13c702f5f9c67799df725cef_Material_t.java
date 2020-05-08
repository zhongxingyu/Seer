 package org.matin.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A class to represent a physical material (Ti6Al4V, etc.)
  * 
  * @author Dave Turner
  *
  */
 public class Material extends MatINWriteableObject {
 
 	protected String name;
 
 	protected String description;
 
	public Material() {}
	
 	public Material(String name, String description) {
 		this.name = name;
 		this.description = description;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	protected List<String> imageURLs = new ArrayList<String>();
 
 	protected List<String> referenceURLs = new ArrayList<String>();
 
 
 }
