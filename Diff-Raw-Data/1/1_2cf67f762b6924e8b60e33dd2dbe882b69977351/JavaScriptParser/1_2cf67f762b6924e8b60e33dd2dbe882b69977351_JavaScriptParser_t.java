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
 
 import java.io.CharArrayReader;
 
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.TokenStream;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemReporterProxy;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.javascript.ast.Script;
 
 public class JavaScriptParser extends AbstractSourceParser {
 
 	private static class JSInternalLexer extends JSLexer {
 		private IProblemReporter reporter;
 
 		public JSInternalLexer(CharStream input, IProblemReporter reporter) {
 			super(input);
 
 			this.reporter = reporter;
 		}
 
 		public void reportError(RecognitionException e) {
 			super.reportError(e);
 
 			if (!errorRecovery) {
 				reporter.reportProblem(new JSProblem(e));
 			}
 		}
 
 		public void emitErrorMessage(String msg) {
 			if (DLTKCore.DEBUG)
 				System.err.println(msg);
 		}
 	}
 
 	private static class JSInternalParser extends JSParser {
 
 		private IProblemReporter reporter;
 
 		public JSInternalParser(TokenStream input, IProblemReporter reporter) {
 			super(input);
 
 			this.reporter = reporter;
 		}
 
 		public void reportError(RecognitionException e) {
 			super.reportError(e);
 
 			if (!errorRecovery) {
 				reporter.reportProblem(new JSProblem(e));
 			}
 		}
 
 		public void emitErrorMessage(String msg) {
 			if (DLTKCore.DEBUG)
 				System.err.println(msg);
 		}
 	}
 
 	private static class JSInternalProblemReporterProxy extends
 			ProblemReporterProxy {
 
 		private boolean errorReported = false;
 
 		public JSInternalProblemReporterProxy(IProblemReporter original) {
 			super(original);
 		}
 
 		public void reportProblem(IProblem problem) {
 			errorReported = true;
 			super.reportProblem(problem);
 		}
 
 		public boolean isErrorReported() {
 			return this.errorReported;
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public Script parse(char[] fileName, char[] source,
 			IProblemReporter reporter) {
 		Assert.isNotNull(source);
 		JSInternalProblemReporterProxy reporterProxy = new JSInternalProblemReporterProxy(
 				reporter);
 		try {
 			CharStream charStream = new ANTLRReaderStream(new CharArrayReader(
 					source));
 			JSLexer lexer = new JSInternalLexer(charStream, reporterProxy);
 			CommonTokenStream stream = new CommonTokenStream(
 					new JavaScriptTokenSource(lexer));
 			JSParser parser = new JSInternalParser(stream, reporterProxy);
 			RuleReturnScope root = parser.program();
 			if (reporterProxy.isErrorReported())
 				return null;
 			return new JSTransformer(root, stream.getTokens()).transform();
 		} catch (Exception e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 			reporterProxy.reportProblem(new JSProblem(e));
 			return null;
 		}
 	}
 
 }
