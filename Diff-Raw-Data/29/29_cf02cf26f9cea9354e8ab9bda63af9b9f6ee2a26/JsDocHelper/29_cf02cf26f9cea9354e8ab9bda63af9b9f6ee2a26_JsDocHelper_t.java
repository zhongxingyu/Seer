 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstDoc;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.util.JstCommentHelper;
 
 public class JsDocHelper {
 
 	public static void addJsDoc(IJsCommentMeta meta, JstMethod jstMethod) {
 		if (meta !=null && meta.getCommentSrc() != null) {
 			String jsdoc = getJsDocFromStructuredComment(meta);
 			if (jsdoc != "" ) {
 				JstDoc jsDocNode = createJsDocNode(jsdoc);
 				if(jsDocNode!=null){
 					jstMethod.setDoc(jsDocNode);
 				}
 			}
 		}
 	}
 	
 	public static void addJsDoc(IJsCommentMeta meta, JstProperty property) {
 		if (meta !=null &&meta.getCommentSrc() != null) {
 			String jsdoc = getJsDocFromStructuredComment(meta);
 			property.setDoc(createJsDocNode(jsdoc));
 
 		}
 	}
 
 	public static void addJsDoc(String jsdoc, JstProperty property) {
 		if (jsdoc != null) {
 			if(!jsdoc.trim().startsWith("/**")){
 				return;
 			}
 			property.setDoc(createJsDocNode(jsdoc));
 
 		}
 	}
 
 	private static String getJsDocFromStructuredComment(IJsCommentMeta meta) {
 		String[] split = meta.getCommentSrc().split(";");
 		StringBuilder jsdoc = new StringBuilder();
 		if (split.length >= 2) {
 			for (int i = 1; i < split.length; i++) {
 				jsdoc.append( split[i]);
 			}
 		}else{
 			return null;
 		}
 		return jsdoc.toString().trim();
 	}
 
 	public static void addJsDoc(String jsdoc, JstType property) {
 		if (jsdoc != null) {
 			if(!jsdoc.trim().startsWith("/**")){
 				return;
 			}
 			property.setDoc(createJsDocNode(jsdoc));
 
 		}
 	}
 
 	public static void addJsDoc(IJsCommentMeta meta, JstType type) {
 
 		if (meta !=null && meta.getCommentSrc() != null) {
 
 			String jsdoc = getJsDocFromStructuredComment(meta);
 			if (jsdoc != "") {
 				type.setDoc(createJsDocNode(jsdoc));
 			}
 		}
 
 	}
 
 	private static JstDoc createJsDocNode(String jsdoc) {
 		jsdoc = cleanJsDocComment(jsdoc);
 		return new JstDoc(jsdoc);
 	}
 	
 	public static String getJsDocFromVjetComment(String vjetDoc){
 		if(vjetDoc==null){
 			return null;
 		}
 		if(vjetDoc.indexOf(";")!=-1){
 			String[] vjetDoc2 = vjetDoc.split(";");
 			if(vjetDoc2.length>1){
 				return cleanJsDocComment(vjetDoc2[1]);
 			}
 		}
 		return "";
 	}
 
 	public static String cleanJsDocComment(String jsdoc) {
 		if(jsdoc==null){
 			return null;
 		}
 		jsdoc = jsdoc.trim();
 		jsdoc = jsdoc.replaceAll("^/\\*\\*", "");
 		jsdoc = jsdoc.replaceAll("^\\*", "");
 		jsdoc = jsdoc.replaceAll("\\*/$", "");
 		// commentSrc = commentSrc.replaceAll("^(\t)*(\\*)*", "");
 		jsdoc = jsdoc.replaceAll("\\s+\\*", "\n");
 		return jsdoc;
 	}
 	
 	
 	
 	public static void addJsDoc(String jsdoc, JstMethod method) {
 		if (jsdoc != null) {
 			if(!jsdoc.trim().startsWith(("/**"))){
 				return;
 			}
 		
 			method.setDoc(createJsDocNode(jsdoc));
 			
 
 		}
 	}
 	
 	
 	/**
 	 * converts docs from jst comment locations to 
 	 * jstdoc nodes. 
 	 * @param jstTypes
 	 */
 	public static void saveDocs(Collection<IJstType> jstTypes){
 		for (IJstType iJstType : jstTypes) {
 			saveDocForJstType(iJstType);
 		}
 	}
 	private static void saveDocForJstType(IJstType iJstType) {
 		
 		// for each property
 		for (IJstProperty property : iJstType.getProperties()) {
 			List<String> comments = JstCommentHelper.getCommentsAsString(iJstType, property.getCommentLocations(), true);
 			addCommentsAsDoc(property, comments);
 		}
 		// for each method save comments
 		for (IJstMethod method : iJstType.getMethods()) {
 			List<String> comments = JstCommentHelper.getCommentsAsString(iJstType, method.getCommentLocations(), true);
 			addCommentsAsDoc(method, comments);
 		}
 		
 		// nested types?
 		
 		
 	}
 	
 	
 	private  static void addCommentsAsDoc(IJstMethod node, List<String> comments) {
		if(node instanceof JstMethod){
			JstMethod mtd = (JstMethod)node;
			for(String comment: comments){
				addJsDoc(comment, mtd);
			}
			
		}
 		
 	}
 
 	private static void addCommentsAsDoc(IJstProperty node, List<String> comments) {
		if(node instanceof JstProperty){
			JstProperty property = (JstProperty)node;
			for(String comment: comments){
				addJsDoc(comment, property);
			}
		}
		// TODO what to do with proxy properties?
 		
 	}
 	
 	
 	
 }
