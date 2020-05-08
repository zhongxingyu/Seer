 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.dataobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * The Class ComplexType.
  *
  * @author srengarajan
  */
 
public class ComplexType extends AbstractType implements
		Comparable<ComplexType> {
 
 	/**
      *  Direct children elements(type <Element>) of this ComplexType
      *  Note that inherited children can be collected by traversing 
      *  up the hierarchy through parentType  
      */
 	private Set<Element> childElements;
 	
 	private Set<AttributeElement> simpleAttributeContent;
 
 	/**
      *  this collection relates the complex type to all its instance elements
      *  its extremely useful in traversals for building Usage paths
      */
 	private List<Element> instanceElements;
 
 	/**
 	 * points to the name of the parent Complex Type element. Used for search 
 	 * and retrieval of the Complex Type
 	 */
 	private String parentType = null;
 
 	private String packageName = null;
 	
 	/**
 	 * @return the packageName
 	 */
 	public String getPackageName() {
 		return packageName;
 	}
 
 	/**
 	 * @param packageName the packageName to set
 	 */
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 	}
 
 	/**
 	 * Gets the simple attribute content.
 	 *
 	 * @return the simple attribute content
 	 */
 	public Set<AttributeElement> getSimpleAttributeContent() {
 		return simpleAttributeContent;
 	}
 
 	/**
 	 * Adds the simple attribute content.
 	 *
 	 * @param simpleAttributeContent the simple attribute content
 	 */
 	public void addSimpleAttributeContent(AttributeElement simpleAttributeContent) {
 		if(this.simpleAttributeContent==null){
 			this.simpleAttributeContent=new TreeSet<AttributeElement>();
 		}
 		this.simpleAttributeContent.add(simpleAttributeContent);
 	}
 	
 	/**
 	 * Gets the parent type.
 	 *
 	 * @return getter for parent type
 	 */
 	public String getParentType() {
 		return parentType;
 	}
 
 	/**
 	 * Sets the parent type.
 	 *
 	 * @param parentType the new parent type
 	 */
 	public void setParentType(String parentType) {
 		this.parentType = parentType;
 	}
 
 	/**
 	 * inheriting child ComplexTypes. Useful for traversal of inherited 
 	 * types
 	 */
 	private List<String> childType = new ArrayList<String>();
 
 	/**
 	 * Sets the child type.
 	 *
 	 * @param childType the new child type
 	 */
 	public void setChildType(List<String> childType) {
 		this.childType = childType;
 	}
 
 	/**
 	 * Gets the child type.
 	 *
 	 * @return Collection of child types
 	 */
 	public List<String> getChildType() {
 		return childType;
 	}
 
 	/**
 	 * Gets the child elements.
 	 *
 	 * @return Collection of child elements ( Composition relationship )
 	 */
 	public Set<Element> getChildElements() {
 		return childElements;
 	}
 
 	
 	/**
 	 * Sets the child elements.
 	 *
 	 * @param elements the new child elements
 	 */
 	public void setChildElements(Set<Element> elements) {
 		this.childElements = elements;
 	}
 
 	/**
 	 * Gets the instance elements.
 	 *
 	 * @return collection of instances of this complex type
 	 */
 	public List<Element> getInstanceElements() {
 		return instanceElements;
 	}
 
 	
 	/**
 	 * Sets the instance elements.
 	 *
 	 * @param elements the new instance elements
 	 */
 	public void setInstanceElements(List<Element> elements) {
 		this.instanceElements = elements;
 	}
 	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ComplexType object) {
		return this.getName().toUpperCase().compareTo(
				object.getName().toUpperCase());
	}
 	
 	@Override
 	public String toString() {
 		StringBuffer retVal=new StringBuffer();
 		retVal.append("Complextype Name: " +name + "\n");
 		retVal.append("Complextype Base Type: " +parentType+ "\n");
 		retVal.append("Attributes: "+ "\n");
 		if(super.getAttributes()!=null){
 			for(Attribute attr: super.getAttributes()) {
 				if(attr != null) {
 					retVal.append(attr.toString()+ "\n");
 				}
 			}
 		}
 		if(super.getAnnotations()!=null){
 			retVal.append("Annotations: \n");
 			retVal.append(super.getAnnotations().toString());
 		}
 		retVal.append("Child Attributes: "+ "\n");
 		if(this.getSimpleAttributeContent()!=null){
 			for(AttributeElement attr: this.getSimpleAttributeContent()) {
 				if(attr != null) {
 					retVal.append(attr.toString()+ "\n");
 				}
 			}
 		}
 		retVal.append("Child Elements: "+ "\n");
 		if(this.getChildElements()!=null){
 			for(Element attr: this.getChildElements()) {
 				if(attr != null) {
 					retVal.append(attr.toString()+ "\n");
 				}
 			}
 		}
 		
 		return retVal.toString();
 	}
 }
