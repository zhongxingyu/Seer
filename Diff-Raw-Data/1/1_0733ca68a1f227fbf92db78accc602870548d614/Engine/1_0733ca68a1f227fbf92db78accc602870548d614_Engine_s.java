 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.codeassist.impl;
 
 import java.util.Map;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.internal.compiler.impl.ITypeRequestor;
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 import org.eclipse.dltk.internal.compiler.lookup.SourceModuleScope;
 
 public abstract class Engine implements ITypeRequestor {
 	public LookupEnvironment lookupEnvironment;
 
 	protected ISearchableEnvironment nameEnvironment;
 
 	protected SourceModuleScope unitScope;
 
 	public AssistOptions options;
 
 	protected static int EXACT_RULE = SearchPattern.R_EXACT_MATCH
 			| SearchPattern.R_CASE_SENSITIVE;
 
 	public Engine(Map settings) {
 		this.options = new AssistOptions(settings);
 	}
 
 	// TODO: move this to other class!!!
 	/*
 	 * Find the node (a field, a method or an initializer) at the given position
 	 * and parse its block statements if it is a method or an initializer.
 	 * Returns the node or null if not found
 	 */
 	protected ASTNode parseBlockStatements(ModuleDeclaration unit, int position) {
 		TypeDeclaration types[] = unit.getTypes();
 		int length = types.length;
 		for (int i = 0; i < length; i++) {
 			TypeDeclaration type = types[i];
 			if (type.sourceStart() <= position && type.sourceEnd() >= position) {
 				getParser().setSource(unit);
 				return parseBlockStatements(type, unit, position);
 			}
 		}
 		MethodDeclaration[] methods = unit.getFunctions();
 		length = methods.length;
 		for (int i = 0; i < length; i++) {
 			MethodDeclaration method = methods[i];
 			if (method.sourceStart() <= position
 					&& method.sourceEnd() >= position) {
 				getParser().setSource(unit);
 				return parseMethod(method, unit, position);
 			}
 		}
 
 		ASTNode[] nodes = unit.getNonTypeOrMethodNode();
 		length = nodes.length;
 		for (int i = 0; i < length; i++) {
 			ASTNode node = nodes[i];
 			if (node.sourceStart() <= position && node.sourceEnd() >= position) {
 				getParser().setSource(unit);
 				getParser().parseBlockStatements(node, unit, position);
 				return node;
 			}
 		}
 		getParser().handleNotInElement(unit, position);
 		// Non type elements
 		return null;
 	}
 
 	private ASTNode parseBlockStatements(TypeDeclaration type,
 			ModuleDeclaration unit, int position) {
 		// members
 		TypeDeclaration[] memberTypes = type.getTypes();
 		if (memberTypes != null) {
 			int length = memberTypes.length;
 			for (int i = 0; i < length; i++) {
 				TypeDeclaration memberType = memberTypes[i];
 				if (memberType.getNameStart() <= position
 						&& memberType.getNameEnd() >= position) {
 					getParser().handleNotInElement(memberType, position);
 				}
 				if (memberType.getBodyStart() > position)
 					continue;
 				if (memberType.sourceEnd() >= position) {
 					return parseBlockStatements(memberType, unit, position);
 				}
 			}
 		}
 		// methods
 		MethodDeclaration[] methods = type.getMethods();
 		if (methods != null) {
 			int length = methods.length;
 			for (int i = 0; i < length; i++) {
 				MethodDeclaration method = methods[i];
 				ASTNode node = parseMethod(method, unit, position);
 				if (node != null) {
 					return node;
 				}
 			}
 		}
 		ASTNode[] nodes = type.getNonTypeOrMethodNode();
 		int length = nodes.length;
 		for (int i = 0; i < length; i++) {
 			ASTNode node = nodes[i];
 			if (node.sourceStart() <= position && node.sourceEnd() >= position) {
 				getParser().setSource(unit);
 				getParser().parseBlockStatements(node, type, position);
 				return node;
 			}
 		}
 
 		getParser().handleNotInElement(type, position);
 		if (DLTKCore.DEBUG) {
 			System.err.println("TODO: Engine: Add fields support.");
 		}
 
 		return null;
 	}
 
 	private ASTNode parseMethod(MethodDeclaration method,
 			ModuleDeclaration unit, int position) {
 		if (method != null) {
 			if (method.sourceStart() > position)
 				return null;
 			if (method.sourceEnd() >= position) {
 				getParser().parseBlockStatements(method, unit, position);
 				return method;
 			}
 		}
 		return null;
 	}
 
 	// TODO: remove this!!!
 	public abstract IAssistParser getParser();

 }
