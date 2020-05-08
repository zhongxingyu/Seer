 /*
 * Copyright (c) 2005, 2008 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.expression.codeassist;
 
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.PrsStream;
 
 import org.eclipse.gmf.internal.xpand.parser.XpandLexer;
 import org.eclipse.gmf.internal.xpand.parser.XpandParsersym;
 
 @SuppressWarnings("restriction")
 public class ReverseScanner {
 
 	private String internal;
 
 	private int offset;
 
 	public ReverseScanner(final String s) {
 		internal = s;
 		offset = internal.length();
 	}
 
 	public int getOffset() {
 		return offset;
 	}
 
 	public IToken previousToken() {
 
 		int os = offset - 1;
 		if (os < 0) {
 			return null;
 		}
 		IToken t = null;
 		while (os >= 0) {
 			final String temp = internal.substring(os--, offset);
 			if (isEndOfString(temp)) {
 				final String wholeString = getString(internal.substring(0, offset));
 				if (wholeString == null) {
 					// incorrect string
 					return null;
 				}
 				offset = internal.substring(0, offset).lastIndexOf(wholeString);
 				return lexToTokenStream(wholeString).getTokenAt(1);
 			} else if (temp.trim().length() > 0) {
 				final PrsStream tokenStream = lexToTokenStream(temp);
 				final IToken tempToken = tokenStream.getTokenAt(1);
 				if (tempToken.getKind() == 0) { //bad token
 					offset = os + 2;
 					return t;
 				}
 				if ((t == null) || (tempToken.toString().length() > t.toString().length())) {
 					t = tempToken;
 				} else {
 					offset = os + 2;
 					return t;
 				}
 			}
 		}
 		offset = 0;
 		return t;
 
 	}
 
 	/**
 	 * is public only for testing purposes
 	 */
 	public String getString(final String string) {
 		final String t = string.trim();
 		if (t.length() > 0) {
 			if (t.endsWith("\"")) {
 				int i = t.substring(0, t.length() - 1).lastIndexOf('"');
 				while ((i > 0) && (t.charAt(i - 1) == '\\')) {
 					i = t.substring(0, t.length() - i).lastIndexOf('"');
 				}
 				return string.substring(i);
 			} else if (t.endsWith("'")) {
 				int i = t.substring(0, t.length() - 1).lastIndexOf('\'');
 				while ((i > 0) && (t.charAt(i - 1) == '\\')) {
 					i = t.substring(0, i).lastIndexOf('\'');
 				}
 				if (i >= 0) {
 					return t.substring(i);
 				}
 			}
 		}
 		// passed string is either empty or incorrect string
 		return null;
 	}
 
 	private boolean isEndOfString(final String temp) {
 		final String t = temp.trim();
 		return (t.length() > 0) && ((t.charAt(t.length() - 1) == '"') || (t.charAt(t.length() - 1) == '\''));
 	}
 
 	public IToken nextToken() {
 		final String temp = internal.substring(offset);
 		final IToken tempToken = lexToTokenStream(temp).getTokenAt(1);
 		if (tempToken.getKind() == XpandParsersym.TK_EOF_TOKEN) {
 			return null;
 		}
 		final String tokenText = tempToken.toString();
 		offset = temp.indexOf(tokenText) + tokenText.length() + offset;
 		return tempToken;
 	}
 
 	private static PrsStream lexToTokenStream(String sequence) {
 		XpandLexer scanner = new XpandLexer(sequence.toCharArray(), "nofile");
 		scanner.lexer(new PrsStream(scanner));
 		// there are at least bad token in the head and eof in the tail
 		assert scanner.getPrsStream().getSize() >= 2;
 		return scanner.getPrsStream();
 	}
 }
