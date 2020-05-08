 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ast.statements;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.DLTKToken;
 
 /**
  * Base class for all statements.
  * 
  */
 public abstract class Statement extends ASTNode implements StatementConstants {
 	protected Statement(int start, int end) {
 		super(start, end);
 	}
 
 	protected Statement() {
 		super();
 	}
 
 	protected Statement(DLTKToken token) {
 		super(token);
 	}
 
 	public abstract int getKind();
 
 	public boolean equals(Object obj) {
 		if (obj == this)
 			return true;
 		if (obj instanceof Statement) {
 			Statement s = (Statement) obj;
 			if (s.sourceEnd() < 0 || s.sourceStart() < 0) {
 				return false;
 			}				
 			return sourceStart() == s.sourceStart()
 					&& sourceEnd() == s.sourceEnd();
 		}
 
 		return false;
 	}
	public int hashCode() {
		return this.sourceStart()*1000 + this.sourceEnd();
	}
 }
