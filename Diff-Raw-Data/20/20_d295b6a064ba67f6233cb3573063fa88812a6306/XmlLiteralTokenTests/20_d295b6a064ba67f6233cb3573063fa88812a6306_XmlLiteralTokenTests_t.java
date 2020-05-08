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
 package org.eclipse.dltk.javascript.parser.tests;
 
 import java.util.List;
 
 import org.antlr.runtime.Token;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.builder.ISourceLineTracker;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.JSTokenStream;
 import org.eclipse.dltk.javascript.parser.JavaScriptParser;
 import org.eclipse.dltk.utils.TextUtils;
 
 @SuppressWarnings("nls")
 public class XmlLiteralTokenTests extends AbstractJSParserTest {
 
 	private static class TestParser extends JavaScriptParser {
 
 		@Override
 		public Script parse(JSTokenStream stream,
 				ISourceLineTracker lineTracker, IProblemReporter reporter) {
 			return super.parse(stream, lineTracker, reporter);
 		}
 
 	}
 
 	public void testXmlLiteralTokens() {
 		StringList code = new StringList();
 		code.add("var TOC = <xml>");
 		code.add("<entry label={homePageTitle}/>");
 		code.add("</xml>");
 		code.add("");
 		code.add("");
 
 		final JSTokenStream stream = JavaScriptParser.createTokenStream(code
 				.toString());
 		new TestParser().parse(stream,
 				TextUtils.createLineTracker(code.toString()), null);
 
 		final List<Token> tokens = stream.getTokens();
 		assertEquals(14, tokens.size());
 		assertEquals(JSParser.VAR, tokens.get(0).getType());
 		assertEquals(JSParser.WhiteSpace, tokens.get(1).getType());
 		assertEquals(JSParser.Identifier, tokens.get(2).getType());
 		assertEquals(JSParser.WhiteSpace, tokens.get(3).getType());
 		assertEquals(JSParser.ASSIGN, tokens.get(4).getType());
 		assertEquals(JSParser.WhiteSpace, tokens.get(5).getType());
 		assertEquals(JSParser.XMLFragment, tokens.get(6).getType());
 		assertEquals(JSParser.LBRACE, tokens.get(7).getType());
 		assertEquals(JSParser.Identifier, tokens.get(8).getType());
 		assertEquals(JSParser.RBRACE, tokens.get(9).getType());
 		assertEquals(JSParser.XMLFragmentEnd, tokens.get(10).getType());
 		assertEquals(JSParser.EOL, tokens.get(11).getType());
 		assertEquals(JSParser.EOL, tokens.get(12).getType());
 		assertEquals(JSParser.EOL, tokens.get(13).getType());
 	}

 }
