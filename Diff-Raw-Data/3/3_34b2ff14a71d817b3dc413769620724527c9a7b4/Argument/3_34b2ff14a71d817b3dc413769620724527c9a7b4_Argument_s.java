 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 /*
  * (c) 2002, 2005 xored software and others all rights reserved. http://www.xored.com
  */
 
 package org.eclipse.dltk.ast.declarations;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class Argument extends Declaration {
 
 	protected ASTNode initialization;
 
 	public Argument(DLTKToken name, int start, int end, ASTNode init) {
 		super(name, start, end);
 		this.initialization = init;
 	}
 
 	public Argument(SimpleReference name, int start, ASTNode init, int mods) {
 		super(start, 0);
 
 		if (name != null) {
 			this.setName(name.getName());
 			this.setEnd(start + name.getName().length());
 		}
 		this.modifiers = mods;
 		this.initialization = init;
 		if (init != null) {
 			this.setEnd(init.sourceEnd());
 		}
 	}
 	
 	public Argument(SimpleReference name, int start, int end, ASTNode init, int mods) {
 		super(start, 0);
 
 		if (name != null) {
 			this.setName(name.getName());
 			this.setEnd(start + name.getName().length());
 		}
 		this.modifiers = mods;
 		this.initialization = init;
 		if (init != null) {
 			this.setEnd(init.sourceEnd());
 		}
 	}
 
 	public Argument() {
 		super();
 		this.setStart(0);
 		this.setEnd(-1);
 	}
 
 	public int getKind() {
 		return D_ARGUMENT;
 	}
 
 	/**
 	 * Please don't use this function. Helper method for initializing Argument
 	 * 
 	 */
 	public final void set(SimpleReference mn, ASTNode initialization) {
 		this.initialization = initialization;
		this.setName(mn.getName());
 		this.setStart(mn.sourceStart());
 		this.setEnd(mn.sourceEnd());
 	}
 
 	public final ASTNode getInitialization() {
 		return initialization;
 	}
 
 	public final void setInitializationExpression(ASTNode initialization) {
 		this.initialization = initialization;
 	}
 
 	public void traverse(ASTVisitor visitor) throws Exception {
 		if (visitor.visit(this)) {
 			if (initialization != null) {
 				initialization.traverse(visitor);
 			}
 			visitor.endvisit(this);
 		}
 	}
 
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append(getName());
 		if (initialization != null) {
 			sb.append('=');
 			sb.append(initialization);
 		}
 		return sb.toString();
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrint("Argument" + this.getSourceRange().toString() + ":");
 		output.formatPrintLn(super.toString());
 	}
 
 	public void setArgumentName(String name) {
 		this.setName(name);
 	}
 }
