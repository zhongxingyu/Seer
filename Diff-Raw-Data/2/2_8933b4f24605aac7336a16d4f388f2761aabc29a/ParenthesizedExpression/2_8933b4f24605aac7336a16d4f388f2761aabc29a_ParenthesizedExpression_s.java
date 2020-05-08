 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.ast;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 
 public class ParenthesizedExpression extends Expression {
 
 	private Expression expression;
 	private int LP = -1;
 	private int RP = -1;
 
 	public ParenthesizedExpression(ASTNode parent) {
 		super(parent);
 	}
 
 	/**
 	 * @see org.eclipse.dltk.ast.ASTNode#traverse(org.eclipse.dltk.ast.ASTVisitor)
 	 */
 	@Override
 	public void traverse(ASTVisitor visitor) throws Exception {
 		if (visitor.visit(this)) {
 			if (expression != null)
 				expression.traverse(visitor);
 			visitor.endvisit(this);
 		}
 	}
 
 	public Expression getExpression() {
 		return this.expression;
 	}
 
 	public void setExpression(Expression expression) {
 		this.expression = expression;
 	}
 
 	public int getLP() {
 		return this.LP;
 	}
 
 	public void setLP(int LP) {
 		this.LP = LP;
 	}
 
 	public int getRP() {
 		return this.RP;
 	}
 
 	public void setRP(int RP) {
 		this.RP = RP;
 	}
 
 	@Override
 	public String toSourceString(String indentationString) {
 
 		Assert.isTrue(sourceStart() >= 0);
 		Assert.isTrue(sourceEnd() > 0);
		Assert.isTrue(LP > 0);
 		Assert.isTrue(RP > 0);
 
 		StringBuffer buffer = new StringBuffer();
 
 		buffer.append("(");
 		buffer.append(expression.toSourceString(indentationString));
 		buffer.append(")");
 
 		return buffer.toString();
 	}
 
 }
