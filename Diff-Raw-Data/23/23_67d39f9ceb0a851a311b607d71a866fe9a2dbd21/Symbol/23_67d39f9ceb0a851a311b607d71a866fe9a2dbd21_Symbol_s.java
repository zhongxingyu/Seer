 package com.cs3240.parsergenerator.Domain;
 
 /**
  * @author Bobby
  * This will be the base for the terminal and non-terminal objects.  Since
  * both of these will be used in the grammar together, I figure that they should
  * both inherit this class.  Also, they both have names, might as well add that method
  * here.
  */
 public abstract class Symbol {
 	private String name;
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		return name.equals(obj);
 	}
 	
 	@Override
 	public int hashCode() {
 		return name.hashCode();
 	}
 	
 
 }
