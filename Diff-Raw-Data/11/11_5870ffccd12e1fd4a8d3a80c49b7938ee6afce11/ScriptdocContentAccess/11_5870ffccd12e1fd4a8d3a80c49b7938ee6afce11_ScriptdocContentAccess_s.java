 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.scriptdoc;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 import org.eclipse.dltk.compiler.InvalidInputException;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.SourceRange;
 import org.eclipse.dltk.corext.documentation.SingleCharReader;
 import org.eclipse.dltk.internal.javascript.typeinference.IProposalHolder;
 import org.eclipse.dltk.javascript.ast.MultiLineComment;
 
 class JavaDocCommentReader extends SingleCharReader {
 
 	private IBuffer fBuffer;
 
 	private int fCurrPos;
 	private int fStartPos;
 	private int fEndPos;
 
 	private boolean fWasNewLine;
 
 	public JavaDocCommentReader(IBuffer buf, int start, int end) {
 		fBuffer = buf;
 		fStartPos = start + 3;
 		fEndPos = end - 2;
 
 		reset();
 	}
 
 	/**
 	 * @see java.io.Reader#read()
 	 */
 	public int read() {
 		if (fCurrPos < fEndPos) {
 			char ch;
 			if (fWasNewLine) {
 				do {
 					ch = fBuffer.getChar(fCurrPos++);
 				} while (fCurrPos < fEndPos && Character.isWhitespace(ch));
 				if (ch == '*') {
 					if (fCurrPos < fEndPos) {
 						do {
 							ch = fBuffer.getChar(fCurrPos++);
 						} while (ch == '*');
 					} else {
 						return -1;
 					}
 				}
 			} else {
 				ch = fBuffer.getChar(fCurrPos++);
 			}
 			fWasNewLine = IndentManipulation.isLineDelimiterChar(ch);
 
 			return ch;
 		}
 		return -1;
 	}
 
 	/**
 	 * @see java.io.Reader#close()
 	 */
 	public void close() {
 		fBuffer = null;
 	}
 
 	/**
 	 * @see java.io.Reader#reset()
 	 */
 	public void reset() {
 		fCurrPos = fStartPos;
 		fWasNewLine = true;
 	}
 
 	/**
 	 * Returns the offset of the last read character in the passed buffer.
 	 */
 	public int getOffset() {
 		return fCurrPos;
 	}
 
 }
 
 /**
  * Helper needed to get the content of a Javadoc comment.
  * 
  * <p>
  * This class is not intended to be subclassed or instantiated by clients.
  * </p>
  * 
  * @since 3.1
  */
 
 public class ScriptdocContentAccess {
 
 	private static final String JAVADOC_BEGIN = MultiLineComment.JSDOC_PREFIX;
 
 	// private static final String JAVADOC_END = "*/";
 
 	private ScriptdocContentAccess() {
 		// do not instantiate
 	}
 
 	public static IScanner createScanner(boolean tokenizeComments,
 			boolean tokenizeWhiteSpace, boolean assertMode,
 			boolean recordLineSeparator) {
 		PublicScanner scanner = new PublicScanner(tokenizeComments,
 				tokenizeWhiteSpace, false/* nls */, 4/* sourceLevel */,
 				null/* taskTags */, null/* taskPriorities */, true/* taskCaseSensitive */);
 		scanner.recordLineSeparator = recordLineSeparator;
 		return scanner;
 	}
 
 	private static class PreviousMemberDetector implements IModelElementVisitor {
 
 		private int currentEnd = 0;
 		private final IMember target;
 		private final int targetStart;
 		private final int targetEnd;
 
 		public PreviousMemberDetector(IMember target, int targetStart,
 				int targetEnd) {
 			this.target = target;
 			this.targetStart = targetStart;
 			this.targetEnd = targetEnd;
 		}
 
 		public boolean visit(IModelElement element) {
 			if (!element.equals(target) && element instanceof ISourceReference) {
 				try {
 					final ISourceRange range = ((ISourceReference) element)
 							.getSourceRange();
 					if (SourceRange.isAvailable(range)) {
 						final int end = range.getOffset() + range.getLength();
 						if (end < targetStart && end > currentEnd) {
 							currentEnd = end;
 						} else if (range.getOffset() <= targetStart
 								&& end >= targetEnd
 								&& range.getOffset() > currentEnd) {
 							currentEnd = range.getOffset();
 						}
 					}
 				} catch (ModelException e) {
 					// ignore
 				}
 			}
 			return true;
 		}
 
 		public static int execute(int start, int end, IMember member) {
 			final PreviousMemberDetector detector = new PreviousMemberDetector(
 					member, start, end);
 			try {
 				member.getSourceModule().accept(detector);
 			} catch (ModelException e) {
 				// ignore
 			}
 			return detector.currentEnd;
 		}
 	}
 
 	public static ISourceRange getJavadocRange(IMember member)
 			throws ModelException {
 		ISourceRange range = member.getSourceRange();
 		if (range == null)
 			return null;
 		IBuffer buf = null;
 		// if (this.isBinary()) {
 		// buf = this.getClassFile().getBuffer();
 		// } else
 		{
 			ISourceModule compilationUnit = member.getSourceModule();
 			if (!compilationUnit.isConsistent()) {
 				return null;
 			}
 			buf = compilationUnit.getBuffer();
 		}
 		final int possibleDocEnd = range.getOffset();
 		final int possibleDocStart = PreviousMemberDetector.execute(
 				possibleDocEnd, possibleDocEnd + range.getLength(), member);
		final String sm = buf.getText(possibleDocStart,
				possibleDocEnd + range.getLength() - possibleDocStart);
 		int start = sm.indexOf(JAVADOC_BEGIN);
 		if (start == -1) {
 			return null;
 		}
 		// int end = sm.indexOf(JAVADOC_END, start);
 		// if (end == -1) {
 		// return null;
 		// }
 		IScanner scanner = createScanner(true, false, false, false);
 		scanner.setSource(buf.getText(possibleDocStart + start,
 				possibleDocEnd - (possibleDocStart + start)).toCharArray());
 		try {
 			int docOffset = -1;
 			int docEnd = -1;
 
 			int terminal = scanner.getNextToken();
 			loop: while (true) {
 				switch (terminal) {
 				case ITerminalSymbols.TokenNameCOMMENT_JAVADOC:
 					docOffset = scanner.getCurrentTokenStartPosition();
 					docEnd = scanner.getCurrentTokenEndPosition() + 1;
 					terminal = scanner.getNextToken();
 					break;
 				case ITerminalSymbols.TokenNameCOMMENT_LINE:
 				case ITerminalSymbols.TokenNameCOMMENT_BLOCK:
 					terminal = scanner.getNextToken();
 					continue loop;
 				default:
 					break loop;
 				}
 			}
 			if (docOffset != -1) {
 				return new SourceRange(docOffset + possibleDocStart + start,
 						docEnd - docOffset + 1);
 			}
 		} catch (InvalidInputException ex) {
 			// try if there is inherited Javadoc
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a reader for an IMember's Javadoc comment content from the source
 	 * attachment. The content does contain only the text from the comment
 	 * without the Javadoc leading star characters. Returns <code>null</code> if
 	 * the member does not contain a Javadoc comment or if no source is
 	 * available.
 	 * 
 	 * @param member
 	 *            The member to get the Javadoc of.
 	 * @param allowInherited
 	 *            For methods with no (Javadoc) comment, the comment of the
 	 *            overridden class is returned if <code>allowInherited</code> is
 	 *            <code>true</code>.
 	 * @return Returns a reader for the Javadoc comment content or
 	 *         <code>null</code> if the member does not contain a Javadoc
 	 *         comment or if no source is available
 	 * @throws JavaModelException
 	 *             is thrown when the elements javadoc can not be accessed
 	 */
 	public static Reader getContentReader(IMember member, boolean allowInherited)
 			throws ModelException {
 		IOpenable openable = member.getOpenable();
 		if (openable != null) {
 			IBuffer buf = openable.getBuffer();
 			if (buf == null) {
 				return null; // no source attachment found
 			}
 			try {
 				ISourceRange javadocRange = getJavadocRange(member);
 
 				if (javadocRange != null) {
 
 					JavaDocCommentReader reader = new JavaDocCommentReader(buf,
 							javadocRange.getOffset(), javadocRange.getOffset()
 									+ javadocRange.getLength() - 1);
 					if (!containsOnlyInheritDoc(reader,
 							javadocRange.getLength())) {
 						reader.reset();
 
 						if (member instanceof IProposalHolder
 								&& ((IProposalHolder) member).getProposalInfo() != null) {
 							String proposalInfo = ((IProposalHolder) member)
 									.getProposalInfo();
 							try {
 								return new StringReader("<p>" + proposalInfo
 										+ "</p>" + reader.getString());
 							} catch (IOException ex) {
 								ex.printStackTrace();
 							}
 						}
 
 						return reader;
 					}
 				}
 
 				if (allowInherited
 						&& (member.getElementType() == IModelElement.METHOD)) {
 					return findDocInHierarchy((IMethod) member);
 				}
 			} catch (ModelException e) {
 				return null;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Checks whether the given reader only returns the inheritDoc tag.
 	 * 
 	 * @param reader
 	 *            the reader
 	 * @param length
 	 *            the length of the underlying content
 	 * @return <code>true</code> if the reader only returns the inheritDoc tag
 	 * @since 3.2
 	 */
 	private static boolean containsOnlyInheritDoc(Reader reader, int length) {
 		char[] content = new char[length];
 		try {
 			reader.read(content, 0, length);
 		} catch (IOException e) {
 			return false;
 		}
 		return new String(content).trim().equals("{@inheritDoc}"); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * Gets a reader for an IMember's Javadoc comment content from the source
 	 * attachment. and renders the tags in HTML. Returns <code>null</code> if
 	 * the member does not contain a Javadoc comment or if no source is
 	 * available.
 	 * 
 	 * @param member
 	 *            the member to get the Javadoc of.
 	 * @param allowInherited
 	 *            for methods with no (Javadoc) comment, the comment of the
 	 *            overridden class is returned if <code>allowInherited</code> is
 	 *            <code>true</code>
 	 * @param useAttachedJavadoc
 	 *            if <code>true</code> Javadoc will be extracted from attached
 	 *            Javadoc if there's no source
 	 * @return a reader for the Javadoc comment content in HTML or
 	 *         <code>null</code> if the member does not contain a Javadoc
 	 *         comment or if no source is available
 	 * @throws JavaModelException
 	 *             is thrown when the elements Javadoc can not be accessed
 	 * @since 3.2
 	 */
 	public static Reader getHTMLContentReader(IMember member,
 			boolean allowInherited, boolean useAttachedJavadoc)
 			throws ModelException {
 		Reader contentReader = getContentReader(member, allowInherited);
 		if (contentReader != null)
 			return new JavaDoc2HTMLTextReader(contentReader);
 
 		IOpenable openable = member.getOpenable();
 		if (useAttachedJavadoc && openable != null
 				&& openable.getBuffer() == null) { // only
 			// if
 			// no
 			// source
 			// available
 			// String s= member.getAttachedJavadoc(null);
 			// if (s != null)
 			// return new StringReader(s);
 		}
 		if (member instanceof IProposalHolder
 				&& ((IProposalHolder) member).getProposalInfo() != null) {
 			return new StringReader(
 					((IProposalHolder) member).getProposalInfo());
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a reader for an IMember's Javadoc comment content from the source
 	 * attachment. and renders the tags in HTML. Returns <code>null</code> if
 	 * the member does not contain a Javadoc comment or if no source is
 	 * available.
 	 * 
 	 * @param member
 	 *            The member to get the Javadoc of.
 	 * @param allowInherited
 	 *            For methods with no (Javadoc) comment, the comment of the
 	 *            overridden class is returned if <code>allowInherited</code> is
 	 *            <code>true</code>.
 	 * @return Returns a reader for the Javadoc comment content in HTML or
 	 *         <code>null</code> if the member does not contain a Javadoc
 	 *         comment or if no source is available
 	 * @throws JavaModelException
 	 *             is thrown when the elements javadoc can not be accessed
 	 * @deprecated As of 3.2, replaced by
 	 *             {@link #getHTMLContentReader(IMember, boolean, boolean)}
 	 */
 	public static Reader getHTMLContentReader(IMember member,
 			boolean allowInherited) throws ModelException {
 		return getHTMLContentReader(member, allowInherited, false);
 	}
 
 	private static Reader findDocInHierarchy(IMethod method)
 			throws ModelException {
 		return null;
 	}
 
 }
