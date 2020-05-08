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
 package org.eclipse.dltk.ast.expressions;
 
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.utils.CorePrinter;
 
 /**
  * Boolean literal representation.
  * 
  */
 public class BooleanLiteral extends Literal {
 
 	private boolean value;
 
 	/**
 	 * Construct from ANTLR token.
 	 * 
 	 * @param t
 	 */
 	public BooleanLiteral(DLTKToken t) {
 		super(t);
 	}
 
 	public BooleanLiteral(int start, int end, boolean value) {
 		super(start, end);
 		this.value = value;
 	}
 
 	public boolean boolValue() {
 		return value;
 	}
 
 	public void setValue(boolean value) {
 		this.value = value;
 	}
 
 	/**
 	 * Return expression kind.
 	 */
 	public int getKind() {
 		return BOOLEAN_LITERAL;
 	}
 
 	/**
 	 * Testing purposes only. Print boolean value.
 	 */
 	public void printNode(CorePrinter output) {
 		output.formatPrintLn("Boolean:" + this.getValue());
 
 	}
 
 }
