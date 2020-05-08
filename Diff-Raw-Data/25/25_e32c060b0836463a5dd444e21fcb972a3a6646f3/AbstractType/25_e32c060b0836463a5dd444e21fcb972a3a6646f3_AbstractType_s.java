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
 
 /**
  * The Class AbstractType.
  *
  * @author srengarajan
  */
public class AbstractType {
 
 	/**
 	 * type name 
 	 */
 	public String name;
 	
 	public static final int SIMPLE_TYPE = 0;
 	public static final int COMPLEX_TYPE = 1;
 	
 	private int schemaType = -1;
 
 	public ParsedAnnotationInfo annotations;
 	
 	
 	/**
 	 * captures the attributes of this Type
 	 */
 	private List<Attribute> attributes = new ArrayList<Attribute>();
 	
 	
 	/**
 	 * Gets the attributes.
 	 *
 	 * @return collection of this elements attributes
 	 */
 	public List<Attribute> getAttributes() {
 		return attributes;
 	}
 	
 	/**
 	 * setter for collection of attributes.
 	 *
 	 * @param attributes the new attributes
 	 */
 	public void setAttributes(List<Attribute> attributes) {
 		this.attributes = attributes;
 	}
 	
 	
 	/**
 	 * Gets the name.
 	 *
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	
 	/**
 	 * Sets the name.
 	 *
 	 * @param name the new name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Sets the schema type.
 	 *
 	 * @param type {SIMPLE_TYPE, COMPLEX_TYPE}
 	 */
 	public void setSchemaType(int type) {
 		schemaType = type;
 	}
 	
 	/**
 	 * Gets the schema type.
 	 *
 	 * @return schemaType
 	 */
 	public int getSchemaType() {
 		return schemaType;
 	}
 	
 	
 	/**
 	 * Gets the annotations.
 	 *
 	 * @return annotations contains the parsed content
 	 * of the annotation markup
 	 */
 	public ParsedAnnotationInfo getAnnotations() {
 		return annotations;
 	}
 
 	/**
 	 * setter for annotations.
 	 *
 	 * @param annotations the new annotation info
 	 */
 	public void setAnnotationInfo(ParsedAnnotationInfo annotations) {
 		this.annotations = annotations;
 	}
 
 }
