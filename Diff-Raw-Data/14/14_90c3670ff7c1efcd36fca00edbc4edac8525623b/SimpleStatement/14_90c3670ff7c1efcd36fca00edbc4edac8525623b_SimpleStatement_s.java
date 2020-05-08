 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.parser.ast.statements;
 
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.statements.Statement;
 
 /**
  * Abstract superclass for all simple statement;
  */
 public abstract class SimpleStatement extends Statement {
 
 	protected Expression fExpression;
 
 	protected SimpleStatement(int start, int end, Expression expression) {
 		super(start, end);
 		this.fExpression = expression;
 	}
 
 	public SimpleStatement(DLTKToken token, Expression expression) {
 		super(token);
 		this.fExpression = expression;
		if( expression != null || expression.sourceEnd() > this.sourceEnd() ) {
 			this.setEnd(expression.sourceEnd());
 		}
 	}
 
 	public void traverse(ASTVisitor pVisitor) throws Exception {
 		if (pVisitor.visit(this)) {
 			if (fExpression != null) {
 				fExpression.traverse(pVisitor);
 			}
 			pVisitor.endvisit(this);
 		}
 	}
 
 	public Expression getExpression() {
 		return fExpression;
 	}
 }
