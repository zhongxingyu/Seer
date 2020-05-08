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
 
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.PositionInformation;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.internal.core.SourceRange;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public abstract class Declaration extends Statement implements Modifiers {
 	public final static int D_ARGUMENT = 3000;
 
 	public final static int D_CLASS = 3001;
 
 	public final static int D_METHOD = 3002;
 
 	public final static int D_DECLARATOR = 3004;
 
 	SimpleReference ref;
 
 	protected int modifiers;
 	
 	private String comments;
 
 	protected Declaration() {
 		this.modifiers = 0;
 		this.ref = new SimpleReference(0, 0, null);
 	}
 
 	protected Declaration(int start, int end) {
 		super(start, end);
 		this.modifiers = 0;
 	}
 
 	protected Declaration(DLTKToken name, int start, int end) {
 		super(start, end);
 		if (name != null) {
 			this.ref = new SimpleReference(name);
 		}
 		else {
 			this.ref = new SimpleReference(start, end, null);
 		}
 	}
 
 	public final int getNameStart() {
 		return this.ref.sourceStart();
 	}
 
 	public final int getNameEnd() {
 		return this.ref.sourceEnd();
 	}
 
 	protected ISourceRange getNameSourceRange () {
 		return new SourceRange(this.getNameStart(), this.getNameEnd() - this.getNameStart() + 1);
 	}
 
	public String getName() {
 		return this.ref.getName();
 	}
 
 	public final int getModifiers() {
 		return this.modifiers;
 	}
 
 	public final PositionInformation getPositionInformation() {
 		return new PositionInformation(this.getNameStart(), this.getNameEnd(), this.sourceStart(),
 				this.sourceEnd());
 	}
 
 	public final void setModifier(int mods) {
 		this.modifiers |= mods;
 	}
 
 	public final void setModifiers(int mods) {
 		this.modifiers = mods;
 	}
 
 	public final void setName(String name) {
 		if( this.ref == null ) {
 			this.ref = new SimpleReference(0, 0, name);
 		}
 		else {
 			this.ref.setName(name);
 		}
 	}
 
 	public void setNameEnd(int end) {
 		this.ref.setEnd(end);
 	}
 
 	public void setNameStart(int start) {
 		this.ref.setStart(start);
 	}
 
 	public boolean isStatic() {
 		return (this.modifiers & AccStatic) != 0;
 	}
 
 	public boolean isPublic() {
 		return (this.modifiers & AccPublic) != 0;
 	}
 
 	public boolean isPrivate() {
 		return (this.modifiers & AccPrivate) != 0;
 	}
 
 	public boolean isProtected() {
 
 		return (this.modifiers & AccProtected) != 0;
 	}
 
 	public boolean isFinal() {
 		return (this.modifiers & AccFinal) != 0;
 	}
 
 	public boolean isAbstract() {
 		return (this.modifiers & AccAbstract) != 0;
 	}
 
 	public boolean isInterface() {
 		return (this.modifiers & AccInterface) != 0;
 	}
 
 	public String toString() {
 		String buf = "";
 
 		if (this.isStatic()) {
 			buf += "static ";
 		}
 		if (this.ref.getName() != null) {
 			buf += " " + this.ref.getName();
 		}
 		return buf;
 	}
 
 	public int getKind() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void printNode(CorePrinter output) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void traverse(ASTVisitor pVisitor) throws Exception {
 		if (pVisitor.visit(this)) {
 			this.ref.traverse(pVisitor);
 			pVisitor.endvisit(this);
 		}
 	}
 
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Declaration)) {
 			return false;
 		}
 		Declaration d = (Declaration) obj;
 		// Only name.
 		return d.ref.getName().equals(this.ref.getName()) && d.ref.sourceStart() == this.ref.sourceStart()
 				&& d.ref.sourceEnd() == this.ref.sourceEnd() && super.equals(obj);
 	}
 
 	public int hashCode() {
 		return this.ref.getName().hashCode();
 	}
 
 	public String debugString () {
 		return super.debugString() + this.getNameSourceRange().toString();
 	}
 	public SimpleReference getRef() {
 		return this.ref;
 	}
 
 	/**
 	 * @param comments the comments to set
 	 */
 	public void setComments(String comments)
 	{
 		this.comments = comments;
 	}
 
 	/**
 	 * @return the comments
 	 */
 	public String getComments()
 	{
 		return comments;
 	}
 }
