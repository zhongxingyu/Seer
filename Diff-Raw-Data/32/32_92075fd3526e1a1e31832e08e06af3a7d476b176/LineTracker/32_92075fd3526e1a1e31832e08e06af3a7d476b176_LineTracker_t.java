 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.parser;
 
import org.antlr.runtime.CommonToken;
 import org.antlr.runtime.Token;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.builder.ISourceLineTracker;
 import org.eclipse.dltk.internal.core.SourceRange;
 
 class LineTracker {
 
 	private final ISourceLineTracker lineTracker;
 
 	public LineTracker(ISourceLineTracker lineTracker) {
 		this.lineTracker = lineTracker;
 	}
 
 	public int getOffset(int line, int column) {
 		return line > 0 ? lineTracker.getLineOffset(line - 1)
 				+ Math.max(column, 0) : 0;
 	}
 
	public final int getLineNumberOfOffset(int offset) {
 		return lineTracker.getLineNumberOfOffset(offset);
 	}
 
	public final int getLength() {
 		return lineTracker.getLength();
 	}
 
	public final int getNumberOfLines() {
		return lineTracker.getNumberOfLines();
	}

 	public int getOffset(Token token) {
		if (token.getLine() >= lineTracker.getNumberOfLines()
				&& token instanceof CommonToken) {
			final CommonToken commonToken = (CommonToken) token;
			if (commonToken.getStartIndex() <= lineTracker.getLength()) {
				return commonToken.getStartIndex();
			}
		}
 		return getOffset(token.getLine(), token.getCharPositionInLine());
 	}
 
 	public int length(Token token) {
 		final String sm = token.getText();
 		return sm != null ? sm.length() : 1;
 	}
 
 	public ISourceRange toSourceRange(Token token) {
 		return new SourceRange(getOffset(token), length(token));
 	}
 
 }
