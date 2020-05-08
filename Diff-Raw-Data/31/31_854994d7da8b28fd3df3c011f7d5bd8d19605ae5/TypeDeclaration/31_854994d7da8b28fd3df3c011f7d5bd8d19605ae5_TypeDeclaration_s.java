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
 
 import org.eclipse.dltk.ast.ASTListNode;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.references.Reference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.utils.ASTUtil;
 import org.eclipse.dltk.utils.CorePrinter;
 
 /**
  * Used to represent types or classes.
  */
 public class TypeDeclaration extends Declaration {
 	/**
 	 * Body start position in associated file.
 	 */
 	protected int bodyEnd;
 
 	/**
 	 * Body end position in associated file.
 	 */
 	protected int bodyStart;
 
 	/**
 	 * Parent classes end position in associated file.
 	 */
 	protected int parentEnd;
 
 	/**
 	 * Parent classes start position in associated file.
 	 */
 	protected int parentStart;
 
 	/**
 	 * List of all super classes. Expression may be complex such as templates or
 	 * other constructions.
 	 */
 	protected ASTListNode fSuperClasses;
 
 	/**
 	 * List of body statements.
 	 */
 	protected Block fBody;
 
 	protected List fMethods;
 
 	protected List fTypes;
 
 	protected List fVariables;
 
 	protected String enclosingTypeName;
 
 	public TypeDeclaration(DLTKToken name, int start, int end) {
 
 		super(name, start, end);
 	}
 
 	/**
 	 * Creates new type declaration from type name ANTLR token, start and end
 	 * position.
 	 * 
 	 * @param name -
 	 *            type name ANTLR token.
 	 * @param start -
 	 *            type start position in associated file.
 	 * @param end -
 	 *            type end position in associated file.
 	 */
 	public TypeDeclaration(String name, int nameStart, int nameEnd, int start,
 			int end) {
 
 		super(start, end);
 		setName(name);
 		setNameStart(nameStart);
 		setNameEnd(nameEnd);
 		this.enclosingTypeName = "";
 	}
 
 	public void setEnclosingTypeName(String name) {
 		if (name.startsWith("$")) {
 			name = name.substring(1);
 		}
 		if (name != null && name.length() > 0) {
 			this.enclosingTypeName = name;
 		}
 	}
 
 	public String getEnclosingTypeName() {
 		return this.enclosingTypeName;
 	}
 
 	/**
 	 * Creates type declaration from name token.
 	 * 
 	 * @param name -
 	 *            name ANTRL token.
 	 */
 	public TypeDeclaration(DLTKToken name) {
 
 		super();
 		this.setName(name.getText());
 	}
 
 	/**
 	 * Used to walk on tree. traverse order: superclasses, body.
 	 */
 	public void traverse(ASTVisitor visitor) throws Exception {
 
 		if (visitor.visit(this)) {
 			if (this.getSuperClasses() != null) {
 				this.getSuperClasses().traverse(visitor);
 			}
 			if (this.fBody != null) {
 				fBody.traverse(visitor);
 			}
 			visitor.endvisit(this);
 		}
 	}
 
 	/**
 	 * Return list of superclass declaration expressions.
 	 * 
 	 * @return
 	 */
 	public ASTListNode getSuperClasses() {
 
 		return this.fSuperClasses;
 	}
 
 	/**
 	 * Set superclases expression list.
 	 * 
 	 * @param exprList
 	 */
 	public void setSuperClasses(ASTListNode exprList) {
 
 		this.fSuperClasses = exprList;
 	}
 
 	/**
 	 * Add superclass expression to list of superclasses. List would be created
 	 * if not yet.
 	 * 
 	 * @param expression
 	 */
 	public void addSuperClass(ASTNode expression) {
 
 		if (this.fSuperClasses == null) {
 			this.fSuperClasses = new ASTListNode();
 		}
 
 		this.fSuperClasses.addNode(expression);
 
 	}
 
 	/**
 	 * Return TypeDeclaration kind.
 	 */
 
 	public int getKind() {
 
 		return D_CLASS;
 	}
 
 	/**
 	 * Return body end position in associated file.
 	 * 
 	 * @return
 	 */
 	public int getBodyEnd() {
 		if (getBody() != null) {
 			return getBody().sourceEnd();
 		}
 		return bodyEnd;
 	}
 
 	/**
 	 * Sets body end position in associated file.
 	 * 
 	 * @param bodyEnd
 	 */
 	protected void setBodyEnd(int bodyEnd) {
 
 		this.bodyEnd = bodyEnd;
 	}
 
 	/**
 	 * Return body start position in associated file.
 	 * 
 	 * @return
 	 */
 	public int getBodyStart() {
 		if (getBody() != null) {
 			return getBody().sourceStart();
 		}
 		return bodyStart;
 	}
 
 	/**
 	 * Set body start position in associated file.
 	 */
 	protected void setBodyStart(int bodyStart) {
 
 		this.bodyStart = bodyStart;
 	}
 
 	/**
 	 * Use sourceEnd() instead.
 	 * 
 	 * @return
 	 * @deprecated
 	 */
 
 	public int getDeclarationSourceEnd() {
 
 		return this.sourceEnd();
 	}
 
 	/**
 	 * Use setEnd instead
 	 * 
 	 * @param declarationSourceEnd
 	 * @deprecated
 	 */
 
 	protected void setDeclarationSourceEnd(int declarationSourceEnd) {
 		this.setEnd(declarationSourceEnd);
 	}
 
 	/**
 	 * Use sourceStart instead.
 	 * 
 	 * @return
 	 * @deprecated
 	 */
 
 	public int getDeclarationSourceStart() {
 
 		return this.sourceStart();
 	}
 
 	/**
 	 * Used setStart instead
 	 * 
 	 * @param declarationSourceStart
 	 * @deprecated
 	 */
 
 	protected void setDeclarationSourceStart(int declarationSourceStart) {
 
 		this.setStart(declarationSourceStart);
 	}
 
 	/**
 	 * Return parents end position in associated file.
 	 * 
 	 * @return
 	 */
 	public int getParentEnd() {
 
 		return parentEnd;
 	}
 
 	/**
 	 * Sets parents end position in associated file.
 	 * 
 	 * @param parentEnd
 	 */
 	protected void setParentEnd(int parentEnd) {
 
 		this.parentEnd = parentEnd;
 	}
 
 	/**
 	 * Return parents start position in associated file.
 	 * 
 	 * @return
 	 */
 	public int getParentStart() {
 
 		return parentStart;
 	}
 
 	/**
 	 * Sets parents start position in associated file.
 	 * 
 	 * @param parentStart
 	 */
 	protected void setParentStart(int parentStart) {
 
 		this.parentStart = parentStart;
 	}
 
 	/**
 	 * Set inner statements.
 	 * 
 	 * @param body
 	 */
 	public void setBody(Block body) {
 
 		this.fBody = body;
 		if (body != null) {
 			this.bodyStart = body.sourceStart();
 			this.bodyEnd = body.sourceEnd();
 			// this.setEnd(body.sourceEnd()); //XXX: why?
 		}
 	}
 
 	public Block getBody() {
 		return this.fBody;
 	}
 
 	/**
 	 * Set inner statements with start and end position in associated file.
 	 * 
 	 * @param startBody -
 	 *            start position.
 	 * @param body -
 	 *            inner statements.
 	 * @param endBody -
 	 *            end position.
 	 */
 	public void setBody(int startBody, Block body, int endBody) {
 
 		this.setBody(body);
 		this.setBodyStart(startBody);
 		this.setBodyEnd(endBody);
 	}
 
 	/**
 	 * Return super class names.
 	 * 
 	 * @return
 	 */
 	public List/* <String> */getSuperClassNames() {
 		List/* < String > */names = new ArrayList/* < String > */();
 		if (this.fSuperClasses != null) {
 			List/* < Expression > */superClasseExpressions = this.fSuperClasses
 					.getChilds();
 			Iterator i = superClasseExpressions.iterator();
 			while (i.hasNext()) {
 				ASTNode expr = (ASTNode) i.next();
 				if (expr instanceof Reference) {
 					names.add(((SimpleReference) expr).getName());
 				}
 
 			}
 		}
 		return names;
 	}
 
 	/**
 	 * Testing purpose only. Prints type and all inner statements to printer.
 	 */
 	public void printNode(CorePrinter output) {
 
 		output.formatPrintLn("Type" + this.getSourceRange().toString()
 				+ this.getNameSourceRange().toString() + ":");
 		String name = this.getName();
 		if (name != null) {
 			output.formatPrintLn(name);
 		}
 		if (this.fSuperClasses != null) {
 			output.formatPrintLn("(");
 			this.fSuperClasses.printNode(output);
 			output.formatPrintLn(")");
 		}
 		if (this.fBody != null) {
 			this.fBody.printNode(output);
 		}
 	}
 
 	public MethodDeclaration[] getMethods() {
 		if (this.fMethods == null) {
 			initInners();
 		}
 		return ASTUtil.getMethods(this.getStatements(), this.fMethods);
 	}
 
 	public TypeDeclaration[] getTypes() {
 		if (this.fTypes == null) {
 			initInners();
 		}
 		return ASTUtil.getTypes(this.getStatements(), this.fTypes);
 	}
 
 	private void initInners() {
 		this.fMethods = new ArrayList();
 		this.fTypes = new ArrayList();
 		this.fVariables = new ArrayList();
 	}
 
 	public List getStatements() {
 		if (this.fBody == null) {
 			this.fBody = new Block(this.sourceStart(), this.sourceEnd(), null);
 		}
 		return this.fBody.getStatements();
 	}
 
 	public ASTNode[] getNonTypeOrMethodNode() {
 		List statements = getStatements();
 		if (statements != null) {
 			Iterator i = statements.iterator();
 			List results = new ArrayList();
 			while (i.hasNext()) {
 				ASTNode node = (ASTNode) i.next();
 				if (!(node instanceof TypeDeclaration)
 						&& !(node instanceof MethodDeclaration)) {
 					results.add(node);
 				}
 			}
 			return (ASTNode[]) results.toArray(new ASTNode[results.size()]);
 		}
 		return null;
 	}
 
 	public FieldDeclaration[] getVariables() {
 		if (this.fVariables == null) {
 			initInners();
 		}
 		return ASTUtil.getVariables(this.getStatements(), this.fVariables);
 	}
 
 	public String debugString() {
 		String prev = super.debugString();
 		if ((this.getModifiers() & Modifiers.AccModule) != 0)
 			prev += "(module)";
 		return prev;
 	}
 
 }
