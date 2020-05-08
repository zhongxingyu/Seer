 /*******************************************************************************
  * Copyright (c) 2000, 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.corext.codemanipulation;
 
 import static org.eclipse.dltk.javascript.ast.MultiLineComment.JSDOC_PREFIX;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.InvalidInputException;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.DLTKUIStatus;
 import org.eclipse.dltk.javascript.scriptdoc.PublicScanner;
 import org.eclipse.dltk.javascript.scriptdoc.ScriptdocContentAccess;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.text.edits.InsertEdit;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 
 /**
  * Add javadoc stubs to members. All members must belong to the same compilation
  * unit. If the parent type is open in an editor, be sure to pass over its
  * working copy.
  */
 public class AddJavaDocStubOperation implements IWorkspaceRunnable {
 
 	private IMember[] fMembers;
 
 	public AddJavaDocStubOperation(IMember[] members) {
 		Assert.isLegal(members.length > 0);
 		fMembers = members;
 	}
 
 	private String createTypeComment(IType type, String lineDelimiter)
 			throws CoreException {
 		String[] typeParameterNames = CharOperation.NO_STRINGS;
 		// StubUtility.getTypeParameterNames(type.getTypeParameters());
 		return JSCodeGeneration.getTypeComment(type.getSourceModule(),
 				type.getTypeQualifiedName("."), typeParameterNames,
 				lineDelimiter);
 	}
 
 	private String createMethodComment(IMethod meth, String lineDelimiter)
 			throws CoreException {
 		// IType declaringType = meth.getDeclaringType();
 
 		IMethod overridden = null;
 		if (!meth.isConstructor()) {
 			// ITypeHierarchy hierarchy = SuperTypeHierarchyCache
 			// .getTypeHierarchy(declaringType);
 			// MethodOverrideTester tester = new MethodOverrideTester(
 			// declaringType, hierarchy);
 			// overridden = tester.findOverriddenMethod(meth, true);
 		}
 		return JSCodeGeneration.getMethodComment(meth, overridden,
 				lineDelimiter);
 	}
 
 	private String createFieldComment(IField field, String lineDelimiter)
 			throws ModelException, CoreException {
 		String typeName = Signature.toString(field.getType());
 		String fieldName = field.getElementName();
 		return JSCodeGeneration.getFieldComment(field.getSourceModule(),
 				typeName, fieldName, lineDelimiter);
 	}
 
 	/**
 	 * @return Returns the scheduling rule for this operation
 	 */
 	public ISchedulingRule getScheduleRule() {
 		return fMembers[0].getResource();
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime
 	 * .IProgressMonitor)
 	 */
 	public void run(IProgressMonitor monitor) throws CoreException,
 			OperationCanceledException {
 		if (monitor == null)
 			monitor = new NullProgressMonitor();
 
 		try {
 			monitor.beginTask("Create Javadoc stub...", fMembers.length + 2);
 
 			addJavadocComments(monitor);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	private void addJavadocComments(IProgressMonitor monitor)
 			throws CoreException {
 		ISourceModule cu = fMembers[0].getSourceModule();
 
 		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
 		IPath path = cu.getPath();
 
 		manager.connect(path, LocationKind.IFILE, new SubProgressMonitor(
 				monitor, 1));
 		try {
 			IDocument document = manager.getTextFileBuffer(path,
 					LocationKind.IFILE).getDocument();
 
 			String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
 			MultiTextEdit edit = new MultiTextEdit();
 
 			for (int i = 0; i < fMembers.length; i++) {
 				IMember curr = fMembers[i];
 				int memberStartOffset = getMemberStartOffset(curr, document);
 
 				String comment = null;
 				switch (curr.getElementType()) {
 				case IModelElement.TYPE:
 					comment = createTypeComment((IType) curr, lineDelim);
 					break;
 				case IModelElement.FIELD:
 					comment = createFieldComment((IField) curr, lineDelim);
 					break;
 				case IModelElement.METHOD:
 					comment = createMethodComment((IMethod) curr, lineDelim);
 					break;
 				}
 				if (comment == null) {
 					StringBuffer buf = new StringBuffer();
 					buf.append(JSDOC_PREFIX).append(lineDelim);
 					buf.append(" *").append(lineDelim); //$NON-NLS-1$
 					buf.append(" */").append(lineDelim); //$NON-NLS-1$
 					comment = buf.toString();
 				} else {
 					if (!comment.endsWith(lineDelim)) {
 						comment = comment + lineDelim;
 					}
 				}
 
 				final IScriptProject project = cu.getScriptProject();
 				IRegion region = document
 						.getLineInformationOfOffset(memberStartOffset);
 
 				String line = document.get(region.getOffset(),
 						region.getLength());
 				String indentString = JSCodeGeneration.getIndentString(line,
 						project);
 
 				String indentedComment = JSCodeGeneration.changeIndent(comment,
 						0, project, indentString, lineDelim);
 
 				ISourceRange range = ScriptdocContentAccess
 						.getJavadocRange(curr);
 				if (range != null) {
 					if (curr.getElementType() == IModelElement.TYPE) {
 						// don't override type comment
 						continue;
 					}
 					final int begin = range.getOffset();
 					int end = begin + range.getLength();
 					while (end < document.getLength()
 							&& Character.isWhitespace(document.getChar(end))) {
 						++end;
 					}
 					if (!indentedComment.equals(document
 							.get(begin, end - begin))) {
 						edit.addChild(new ReplaceEdit(begin, end - begin,
 								indentedComment));
 					}
 				} else {
					edit.addChild(new InsertEdit(region.getOffset(),
 							indentedComment));
 				}
 
 				monitor.worked(1);
 			}
 			edit.apply(document); // apply all edits
 		} catch (BadLocationException e) {
 			throw new CoreException(DLTKUIStatus.createError(IStatus.ERROR, e));
 		} finally {
 			manager.disconnect(path, LocationKind.IFILE,
 					new SubProgressMonitor(monitor, 1));
 		}
 	}
 
 	private int getMemberStartOffset(IMember curr, IDocument document)
 			throws ModelException {
 		int offset = curr.getSourceRange().getOffset();
 		final PublicScanner scanner = new PublicScanner();
 		scanner.setSource(document.get().toCharArray());
 
 		try {
 			// read to the first real non comment token
 			scanner.resetTo(offset, scanner.getSource().length - 1);
 			scanner.getNextToken();
 			return scanner.getCurrentTokenStartPosition();
 		} catch (InvalidInputException e) {
 			// ignore
 		}
 		return offset;
 	}
 
 }
