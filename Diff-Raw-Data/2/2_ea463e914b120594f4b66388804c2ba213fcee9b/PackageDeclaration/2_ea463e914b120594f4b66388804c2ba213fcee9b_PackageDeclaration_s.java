 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package descent.internal.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import descent.core.*;
 import descent.core.compiler.IScanner;
 import descent.core.compiler.ITerminalSymbols;
 import descent.core.compiler.InvalidInputException;
 import descent.core.dom.AST;
 
 /**
  * @see IPackageDeclaration
  */
 
 /* package */ class PackageDeclaration extends SourceRefElement implements IPackageDeclaration {
 	
 	String name;
 	
 protected PackageDeclaration(CompilationUnit parent, String name) {
 	super(parent);
 	this.name = name;
 }
 public boolean equals(Object o) {
 	if (!(o instanceof PackageDeclaration)) return false;
 	return super.equals(o);
 }
 public String getElementName() {
 	return this.name;
 }
 /**
  * @see IJavaElement
  */
 public int getElementType() {
 	return PACKAGE_DECLARATION;
 }
 /**
  * @see JavaElement#getHandleMemento()
  */
 protected char getHandleMementoDelimiter() {
 	return JavaElement.JEM_PACKAGEDECLARATION;
 }
 /*
  * @see JavaElement#getPrimaryElement(boolean)
  */
 public IJavaElement getPrimaryElement(boolean checkOwner) {
 	CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
 	if (checkOwner && cu.isPrimary()) return this;
 	return cu.getPackageDeclaration(this.name);
 }
 public ISourceRange[] getJavadocRanges() throws JavaModelException {
 	ISourceRange range= this.getSourceRange();
 	if (range == null) return null;
 	IBuffer buf= null;
 	ICompilationUnit compilationUnit = this.getCompilationUnit();
 	if (!compilationUnit.isConsistent()) {
 		return null;
 	}
 	buf = compilationUnit.getBuffer();
 	
 	List<ISourceRange> sourceRanges = new ArrayList<ISourceRange>();
 	
 	final int start= range.getOffset();
 	final int length= range.getLength();
 	if (length > 0 && buf.getChar(start) == '/') {
 		IScanner scanner= ToolFactory.createScanner(true, false, false, false, AST.LATEST);
 		scanner.setSource(buf.getText(start, length).toCharArray());
 		try {
 			int terminal= scanner.getNextToken();
 			loop: while (true) {
 				switch(terminal) {
 					case ITerminalSymbols.TokenNameCOMMENT_DOC_LINE:
 					case ITerminalSymbols.TokenNameCOMMENT_DOC_BLOCK:
 					case ITerminalSymbols.TokenNameCOMMENT_DOC_PLUS:
 						sourceRanges.add(new SourceRange(
								scanner.getCurrentTokenStartPosition(),
 								scanner.getCurrentTokenEndPosition() - scanner.getCurrentTokenStartPosition()
 								));
 						terminal= scanner.getNextToken();
 						continue loop;
 					case ITerminalSymbols.TokenNameCOMMENT_LINE :
 					case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
 					case ITerminalSymbols.TokenNameCOMMENT_PLUS :
 						terminal= scanner.getNextToken();
 						continue loop;
 					default :
 						break loop;
 				}
 			}
 		} catch (InvalidInputException ex) {
 			// try if there is inherited Javadoc
 		}
 	}
 	return sourceRanges.toArray(new ISourceRange[sourceRanges.size()]);
 }
 /**
  * @private Debugging purposes
  */
 protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
 	buffer.append(this.tabString(tab));
 	buffer.append("package "); //$NON-NLS-1$
 	toStringName(buffer);
 	if (info == null) {
 		buffer.append(" (not open)"); //$NON-NLS-1$
 	}
 }
 }
