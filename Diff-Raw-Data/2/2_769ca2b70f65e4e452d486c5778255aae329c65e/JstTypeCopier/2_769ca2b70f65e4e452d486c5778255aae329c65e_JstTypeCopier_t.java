 /**
  * /*******************************************************************************
  * Copyright (c) 2005-2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.ts.util;
 
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 
 
 public class JstTypeCopier  {
 
 	
 	/** this copier will take a jsttype 
 	 * clear out the contents of the type
 	 * and replace it with the content of another
 	 * 
 	 */
 	public static void replace(JstType replace, JstType copy){
 		replace.clearAll();
 		replace.setName(copy.getName());
 		replace.setPackage(copy.getPackage());
 		replace.setSource(copy.getSource());
 		replace.setAlias(copy.getAlias());
 		replace.setAliasTypeName(copy.getAliasTypeName());
 		replace.setAnnotations(copy.getAnnotations());
 		replace.setCategory(copy.getCategory());
 		replace.setCommentLocations(copy.getCommentLocations());
 		replace.setConstructor(copy.getConstructor());
 		replace.setDoc(copy.getDoc());
 		replace.setEmbeddedTypes(copy.getEmbededTypes());
 		replace.setEnumValues(copy.getEnumValues());
 		replace.setExpects(copy.getExpects());
 		replace.setFakeType(copy.isFakeType());
 		replace.setExtends(copy.getExtends());
 		replace.setImports(copy.getImports());
 		replace.setInactiveImports(copy.getInactiveImports());
 		replace.setInitBlock(copy.getInitBlock());
 		replace.setInstanceInitializers(copy.getInstanceInitializers());
 		replace.setImpliedImport(copy.isImpliedImport());
 		replace.setGlobalVars(copy.getGlobalVars());
 		replace.setStaticInitializers(copy.getStaticInitializers());
 		replace.setMixins(copy.getMixins());
 		replace.setModifers(copy.getModifiers());
 		replace.setOptions(copy.getOptions());
 		replace.setOTypes(copy.getOTypes());
 		replace.setOuterType(copy.getOuterType());
 		replace.setParent(copy.getParentNode());
 //		replace.setParamTypes(copy.getParam());
 		replace.setSatisfies(copy.getSatisfies());
 		replace.setSecondaryTypes(copy.getSecondaryTypes());
 		replace.setSiblingTypes(copy.getSiblingTypes());
 		replace.setStatus(copy.getStatus());
 		replace.setSingleton(copy.isSingleton());
 		replace.setMethods(copy.getMethods());
 		replace.setProperties(copy.getProperties());
		replace.setJstBlockList(copy.getJstBlockList());
		replace.setProblems(copy.getProblems());
 	}
 
 
 
 
 }
