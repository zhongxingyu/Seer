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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.ast.utils.ASTUtil;
 import org.eclipse.dltk.internal.compiler.lookup.SourceModuleScope;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class ModuleDeclaration extends ASTNode {
 	private List types;
 	private List functions;
 	protected List variables;
 
 	private Block body;
 
 	private boolean rebuildEnabled;
 
 	// TODO: make private
 	public SourceModuleScope scope;
 
	public List getTypeList() {
 		return types;
 	}
 
	public List getFunctionList() {
 		return functions;
 	}
 
	public List getVariablesList() {
 		return variables;
 	}
 
 	public ModuleDeclaration(int sourceLength) {
 		this(sourceLength, false);
 	}
 
 	public ModuleDeclaration(int sourceLength, boolean rebuildEnabled) {
 		super(0, sourceLength);
 
 		this.body = new Block();
 		this.body.setEnd(sourceLength);
 		this.types = new ArrayList();
 		this.functions = new ArrayList();
 		this.variables = new ArrayList();
 
 		this.rebuildEnabled = rebuildEnabled;
 	}
 
 	public final void traverse(ASTVisitor visitor) throws Exception {
 		if (visitor.visit(this)) {
 			body.traverse(visitor);
 			visitor.endvisit(this);
 		}
 	}
 
 	public void setStatements(List statements) {
 		body = new Block(sourceStart(), sourceEnd(), statements);
 	}
 
 	public void addStatement(Statement statement) {
 		body.addStatement(statement);
 	}
 
 	public List getStatements() {		
 		return body.getStatements();
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrintLn("Module" + this.getSourceRange().toString() + ":");
 		body.printNode(output);
 	}
 
 	public boolean isEmpty() {
 		return body.getStatements().isEmpty();
 	}
 
 	protected void doRebuild() {
 		// Iterator i = this.getStatements().iterator();
 		// while( i.hasNext()) {
 		// ASTNode node = (ASTNode)i.next();
 		// if( node instanceof MethodDeclaration ) {
 		// this.functions.add( node );
 		// }
 		// else if( node instanceof TypeDeclaration ) {
 		// this.types.add(node);
 		// }
 		// }
 	}
 
 	public final void rebuild() {
 		if (rebuildEnabled) {
 			doRebuild();
 		}
 	}
 
 	protected boolean isRebuildEnabled() {
 		return rebuildEnabled;
 	}
 
 	public void disableRebuild() {
 		rebuildEnabled = false;
 	}
 
 	public TypeDeclaration[] getTypes() {
 		return ASTUtil.getTypes(this.getStatements(), this.types);
 	}
 
 	public MethodDeclaration[] getFunctions() {
 		return ASTUtil.getMethods(this.getStatements(), this.functions);
 	}
 
 	public FieldDeclaration[] getVariables() {
 		return ASTUtil.getVariables(this.getStatements(), this.variables );
 	}
 
 	public ASTNode[] getNonTypeOrMethodNode() {
 		List statements = getStatements();
 		List results = new ArrayList();
 		if (statements != null) {
 			Iterator it = statements.iterator();
 			while (it.hasNext()) {
 				ASTNode node = (ASTNode) it.next();
 				if (!(node instanceof TypeDeclaration)
 						&& !(node instanceof MethodDeclaration)) {
 					results.add(node);
 				}
 			}
 		}
 		return (ASTNode[]) results.toArray(new ASTNode[results.size()]);
 	}
 
 	public void setEnd(int end) {		
 		super.setEnd(end);
 		body.setEnd(end);
 	}
 
 	public void setStart(int start) {
 		super.setStart(start);
 		body.setStart(start);
 	}
 	
 	
 }
