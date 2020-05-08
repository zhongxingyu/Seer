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
 package org.eclipse.dltk.javascript.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.BitSet;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.IntStream;
 import org.antlr.runtime.MismatchedSetException;
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.NoViableAltException;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.TokenStream;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.SourceRange;
 import org.eclipse.dltk.core.builder.ISourceLineTracker;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.internal.parser.NodeTransformerManager;
 import org.eclipse.dltk.javascript.parser.Reporter.Severity;
 import org.eclipse.dltk.utils.TextUtils;
 import org.eclipse.osgi.util.NLS;
 
 public class JavaScriptParser extends AbstractSourceParser {
 
 	public static final String PARSER_ID = "org.eclipse.dltk.javascript.NewParser";
 
 	private boolean typeInformationEnabled = false;
 
 	public boolean isTypeInformationEnabled() {
 		return typeInformationEnabled;
 	}
 
 	public void setTypeInformationEnabled(boolean typeInformationEnabled) {
 		this.typeInformationEnabled = typeInformationEnabled;
 	}
 
 	private static class JSInternalParser extends JSParser {
 
 		private final Reporter reporter;
 
 		public JSInternalParser(TokenStream input, Reporter reporter) {
 			super(input);
 			this.reporter = reporter;
 		}
 
 		@Override
 		protected void reportFailure(Throwable t) {
 			if (reporter != null && !peekState().hasErrors()) {
 				reporter.reportProblem(new JSProblem(t));
 			}
 		}
 
 		private JSParserMessages messages = null;
 
 		private JSParserMessages getMessages() {
 			if (messages == null) {
 				messages = new JSParserMessages();
 			}
 			return messages;
 		}
 
 		private String getTokenName(int token) {
 			String message = getMessages().get(token);
 			if (message == null) {
 				message = getTokenNames()[token];
 			}
 			return message;
 		}
 
 		@Override
 		public String getTokenErrorDisplay(Token t) {
 			final String message = getMessages().get(t.getType());
 			if (message != null) {
 				return message;
 			}
 			return super.getTokenErrorDisplay(t);
 		}
 
 		@Override
 		public void displayRecognitionError(String[] tokenNames,
 				RecognitionException re) {
 			peekState().incrementErrorCount();
 			if (reporter == null)
 				return;
 			String message;
 			ISourceRange range;
 			if (re instanceof NoViableAltException) {
 				range = convert(re.token);
 				final Token token = getLastToken(re.token);
 				message = getMessages().get(peekState().rule, token.getType());
 				if (message == null) {
 					message = "Unexpected " + getTokenErrorDisplay(re.token);
 				}
 			} else if (re instanceof MismatchedTokenException) {
 				MismatchedTokenException mte = (MismatchedTokenException) re;
 				if (re.token == Token.EOF_TOKEN) {
 					message = getTokenName(mte.expecting) + " expected";
 				} else {
 					message = "Mismatched input "
 							+ getTokenErrorDisplay(re.token);
 					if (mte.expecting >= 0 && mte.expecting < tokenNames.length) {
 						message += ", " + getTokenName(mte.expecting)
 								+ " expected";
 					}
 				}
 				range = convert(re.token);
 				if (range.getLength() + range.getOffset() >= inputLength()) {
 					int stop = inputLength() - 1;
 					int start = Math.min(stop - 1, range.getOffset() - 2);
 					range = new SourceRange(start, stop - start);
 				}
 			} else if (re instanceof MismatchedSetException) {
 				MismatchedSetException mse = (MismatchedSetException) re;
 				message = "Mismatched input " + getTokenErrorDisplay(re.token);
 				if (mse.expecting != null) {
 					message += " expecting set " + mse.expecting;
 				}
 				range = convert(re.token);
 			} else {
 				message = "Syntax Error:" + re.getMessage();
 				range = convert(re.token);
 				// stop = start + 1;}
 			}
 			reporter.setMessage(JavaScriptParserProblems.SYNTAX_ERROR, message);
 			reporter.setSeverity(Severity.ERROR);
 			if (range != null) {
 				reporter.setRange(range.getOffset(),
 						range.getOffset() + range.getLength());
 			}
 			reporter.setLine(re.line - 1);
 			reporter.report();
 		}
 
 		private Token getLastToken(Token token) {
 			if (token == Token.EOF_TOKEN) {
 				final TokenStream stream = getTokenStream();
 				int index = stream.index();
 				while (index > 0) {
 					--index;
 					final Token prevToken = stream.get(index);
 					if (prevToken.getType() != JSParser.WhiteSpace
 							&& prevToken.getType() != JSParser.EOL) {
 						token = prevToken;
 						break;
 					}
 				}
 			}
 			return token;
 		}
 
 		private ISourceRange convert(Token token) {
 			token = getLastToken(token);
 			if (token == Token.EOF_TOKEN) {
 				return null;
 			}
 			return reporter.toSourceRange(token);
 		}
 
 		private int inputLength() {
 			return reporter.getLength();
 		}
 
 		/*
 		 * Standard implementation contains forgotten debug System.err.println()
 		 * and we don't need it at all.
 		 */
 		@Override
 		public void recoverFromMismatchedToken(IntStream input,
 				RecognitionException e, int ttype, BitSet follow)
 				throws RecognitionException {
 			// if next token is what we are looking for then "delete" this token
 			if (input.LA(2) == ttype) {
 				reportError(e);
 				beginResync();
 				input.consume(); // simply delete extra token
 				endResync();
 				input.consume(); // move past ttype token as if all were ok
 				return;
 			}
 			// insert "}" if expected
 			if (ttype == JSParser.RBRACE) {
 				displayRecognitionError(getTokenNames(), e);
 				return;
 			}
 			if (!recoverFromMismatchedElement(input, e, follow)) {
 				throw e;
 			}
 		}
 
 		@Override
 		protected void syncToSet() {
 			final BitSet follow = following[_fsp];
 			int mark = input.mark();
 			try {
 				Token first = null;
 				Token last = null;
 				// final StringBuilder sb = new StringBuilder();
 				while (!follow.member(input.LA(1))) {
 					if (input.LA(1) == Token.EOF) {
 						input.rewind();
 						mark = -1;
 						return;
 					}
 					last = input.LT(1);
 					// String tokenText = getTokenErrorDisplay(last);
 					// if (tokenText.startsWith("'") && tokenText.endsWith("'"))
 					// {
 					// tokenText = tokenText.substring(1,
 					// tokenText.length() - 1);
 					// }
 					// sb.append(tokenText);
 					if (first == null) {
 						first = last;
 					}
 					input.consume();
 				}
 				if (first != null && reporter != null) {
 					final ISourceRange end = convert(last);
 					reporter.setMessage(JavaScriptParserProblems.SYNTAX_ERROR,
 							"Unexpected input was discarded");
 					reporter.setSeverity(Severity.ERROR);
 					reporter.setRange(convert(first).getOffset(),
 							end.getOffset() + end.getLength());
					reporter.setLine(first.getLine() - 1);
 					reporter.report();
 				}
 			} finally {
 				if (mark != -1) {
 					input.release(mark);
 				}
 			}
 		}
 
 		@Override
 		protected void typeRefExpected() {
 			final Token colon = input.LT(-1);
 			final ISourceRange colonRange = convert(colon);
 			reporter.setMessage(JavaScriptParserProblems.SYNTAX_ERROR,
 					"Type name expected after colon");
 			reporter.setSeverity(Severity.ERROR);
 			reporter.setRange(colonRange.getOffset(), colonRange.getOffset()
 					+ colonRange.getLength());
			reporter.setLine(colon.getLine() - 1);
 			reporter.report();
 		}
 
 		@Override
 		protected void reportReservedKeyword(Token token) {
			final ISourceRange range = convert(token);
 			reporter.setMessage(JavaScriptParserProblems.RESERVED_KEYWORD,
 					NLS.bind("\"{0}\" is reserved keyword", token.getText()));
 			reporter.setSeverity(Severity.ERROR);
			reporter.setRange(range.getOffset(),
					range.getOffset() + range.getLength());
			reporter.setLine(token.getLine() - 1);
 			reporter.report();
 		}
 
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public Script parse(IModuleSource input, IProblemReporter reporter) {
 		Assert.isNotNull(input);
 		if (input.getModelElement() != null) {
 			setTypeInformationEnabled(JavaScriptParserPreferences
 					.isTypeInfoEnabled(input.getModelElement()
 							.getScriptProject().getProject()));
 		}
 		char[] source = input.getContentsAsCharArray();
 		return parse(
 				input.getModelElement(),
 				createTokenStream(source),
 				reporter == null ? null : new Reporter(TextUtils
 						.createLineTracker(source), reporter,
 						new JavaScriptParserSeverityReporter()));
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public Script parse(String source, IProblemReporter reporter) {
 		Assert.isNotNull(source);
 		return parse(null, createTokenStream(source),
 				TextUtils.createLineTracker(source), reporter);
 	}
 
 	protected Script parse(IModelElement element, JSTokenStream stream,
 			ISourceLineTracker lineTracker, IProblemReporter reporter) {
 		return parse(element, stream, new Reporter(lineTracker, reporter,
 				new JavaScriptParserSeverityReporter()));
 	}
 
 	protected Script parse(IModelElement element, JSTokenStream stream,
 			Reporter reporter) {
 		try {
 			stream.setReporter(reporter);
 			JSInternalParser parser = new JSInternalParser(stream, reporter);
 			parser.setTypeInformationEnabled(typeInformationEnabled);
 			RuleReturnScope root = parser.program();
 			final NodeTransformer[] transformers = NodeTransformerManager
 					.createTransformers(element, reporter);
 			JSTransformer transformer = new JSTransformer(transformers,
 					stream.getTokens(), parser.peekState().hasErrors());
 			transformer.setReporter(reporter);
 			return transformer.transform(root);
 		} catch (Exception e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			if (reporter != null) {
 				reporter.reportProblem(new JSProblem(e));
 			}
 			// create empty output
 			return new Script();
 		}
 	}
 
 	public static JSTokenStream createTokenStream(char[] source) {
 		CharStream charStream = new ANTLRStringStream(source, source.length);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 	public static JSTokenStream createTokenStream(String source) {
 		CharStream charStream = new ANTLRStringStream(source);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 	/**
 	 * @param input
 	 * @param encoding
 	 * @return
 	 * @throws IOException
 	 */
 	public static JSTokenStream createTokenStream(InputStream input,
 			String encoding) throws IOException {
 		CharStream charStream = new ANTLRInputStream(input, encoding);
 		return new DynamicTokenStream(new JavaScriptTokenSource(charStream));
 	}
 
 }
