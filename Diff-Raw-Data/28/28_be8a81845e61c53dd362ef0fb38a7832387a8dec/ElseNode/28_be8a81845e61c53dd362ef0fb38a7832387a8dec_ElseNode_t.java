 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.jruby.ast.ext;
 
 import java.util.List;
 
 import org.jruby.ast.Node;
 import org.jruby.ast.NodeTypes;
 import org.jruby.ast.visitor.NodeVisitor;
 import org.jruby.evaluator.Instruction;
import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.ISourcePositionHolder;
 
 public class ElseNode extends Node {
 
 	private final ISourcePositionHolder elseKeyword;
	private final Node statement;
 
 	/**
 	 * @return the elseKeyword
 	 */
 	public ISourcePositionHolder getElseKeyword() {
 		return elseKeyword;
 	}
 
	public ElseNode(ISourcePosition position,
			ISourcePositionHolder elseKeyword, Node statement) {
		super(position, NodeTypes.EXT_ELSENODE);
 		this.elseKeyword = elseKeyword;
		this.statement = statement;
 	}
 
 	/*
 	 * @see org.jruby.ast.Node#accept(org.jruby.ast.visitor.NodeVisitor)
 	 */
 	public Instruction accept(NodeVisitor visitor) {
 		throw new RuntimeException(getNodeName() + " should never be evaluated");
 	}
 
 	/*
 	 * @see org.jruby.ast.Node#childNodes()
 	 */
 	public List childNodes() {
 		return createList(statement);
 	}
 
 	public Node getStatement() {
 		return statement;
 	}
 
 }
