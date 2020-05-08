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
 
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.utils.IntList;
 
 public class ArrayInitializer extends Expression {
 
 	private List<ASTNode> items;
 	private IntList commas;
 	private int LB = -1;
 	private int RB = -1;
 
 	public ArrayInitializer(ASTNode parent) {
 		super(parent);
 	}
 
 	/**
 	 * @see org.eclipse.dltk.ast.ASTNode#traverse(org.eclipse.dltk.ast.ASTVisitor)
 	 */
 	@Override
 	public void traverse(ASTVisitor visitor) throws Exception {
 		if (visitor.visit(this)) {
 			if (items != null) {
 				for (ASTNode node : items) {
 					node.traverse(visitor);
 				}
 			}
 			visitor.endvisit(this);
 		}
 	}
 
 	public List<ASTNode> getItems() {
 		return this.items;
 	}
 
 	public void setItems(List<ASTNode> items) {
 		this.items = items;
 	}
 
 	public IntList getCommas() {
 		return this.commas;
 	}
 
 	public void setCommas(IntList commas) {
 		this.commas = commas;
 	}
 
 	public int getLB() {
 		return this.LB;
 	}
 
 	public void setLB(int pos) {
 		this.LB = pos;
 	}
 
 	public int getRB() {
 		return this.RB;
 	}
 
 	public void setRB(int pos) {
 		this.RB = pos;
 	}
 
 	@Override
 	public String toSourceString(String indentionString) {
 
 		Assert.isTrue(sourceStart() >= 0);
 		Assert.isTrue(sourceEnd() > 0);
		Assert.isTrue(LB > 0);
 		Assert.isTrue(RB > 0);
 		Assert.isTrue(items.size() == 0 || commas.size() == items.size() - 1);
 
 		StringBuffer buffer = new StringBuffer();
 
 		buffer.append(Keywords.LB);
 
 		for (int i = 0; i < items.size(); i++) {
 			if (i > 0)
 				buffer.append(", ");
 
 			ISourceable item = (ISourceable) items.get(i);
 			buffer.append(item.toSourceString(indentionString));
 		}
 
 		buffer.append(Keywords.RB);
 
 		return buffer.toString();
 	}
 
 }
