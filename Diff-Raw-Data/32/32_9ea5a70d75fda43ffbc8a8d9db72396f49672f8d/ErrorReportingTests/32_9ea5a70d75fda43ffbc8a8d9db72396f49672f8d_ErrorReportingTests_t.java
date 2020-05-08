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
 
 import org.eclipse.dltk.ast.utils.ASTUtil;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserProblems;
 
 public class ErrorReportingTests extends AbstractJSParserTest {
 
 	public void test1() {
 		parseRaw("a.");
 		assertTrue(reporter.hasErrors());
 		final List<IProblem> errors = reporter.getErrors();
 		assertEquals(1, errors.size());
 		assertEquals("missing name after . operator", errors.get(0)
 				.getMessage());
 	}
 
 	public void testNotFinishedComment() {
 		StringList code = new StringList();
 		code.add("function a() {}");
 		code.add("/* not finished comment");
 		code.add("function b() {}");
 		Script script = parseRaw(code.toString());
 		assertTrue(reporter.hasErrors());
 		assertTrue(ASTUtil.select(script, FunctionStatement.class).size() > 0);
 	}
 
 	public void testMisplacedDiv() {
 		StringList code = new StringList();
 		code.add("function a() {}");
 		code.add("/");
 		code.add("function b() {}");
 		Script script = parseRaw(code.toString());
 		assertTrue(reporter.hasErrors());
 		assertTrue(ASTUtil.select(script, FunctionStatement.class).size() > 0);
 	}
 
 	public void testMisplacedMul() {
 		StringList code = new StringList();
 		code.add("function a() {}");
 		code.add("***");
 		code.add("function b() {}");
 		Script script = parseRaw(code.toString());
 		assertTrue(reporter.hasErrors());
 		assertTrue(ASTUtil.select(script, FunctionStatement.class).size() > 0);
 	}
 
 	public void testNotFinishedStringLiteral() {
 		StringList code = new StringList();
 		code.add("function a() {}");
 		code.add("\"");
 		code.add("function b() {}");
 		Script script = parseRaw(code.toString());
 		assertTrue(reporter.hasErrors());
 		assertTrue(ASTUtil.select(script, FunctionStatement.class).size() > 0);
 	}
 
 	public void testNotFinishedCall() {
 		StringList code = new StringList();
 		code.add("function test(event) {");
 		code.add("application.output(e");
 		code.add("}");
 		Script script = parseRaw(code.toString());
 		assertTrue(reporter.hasErrors());
 		assertEquals(JavaScriptParserProblems.SYNTAX_ERROR, getProblemId());
 		assertTrue(ASTUtil.select(script, FunctionStatement.class).size() > 0);
 	}
 
 	public void testNoArrayIndex() {
 		parseRaw("var x = a[]");
 		assertTrue(reporter.hasErrors());
 		assertEquals(1, reporter.getProblems().size());
 	}
 
 	public void testEmptyParentheses() {
 		parseRaw("var x = x * ()");
 		assertTrue(reporter.hasErrors());
 		assertEquals(1, reporter.getProblems().size());
 	}
 
	public void testNotFinishedConditionalOperator() {
		parse("var x = x ? 0:");
		assertEquals(1, reporter.getProblems().size());
		assertEquals(JavaScriptParserProblems.SYNTAX_ERROR, getProblemId());
		parse("var x = x ? 0:;");
		assertEquals(1, reporter.getProblems().size());
		assertEquals(JavaScriptParserProblems.SYNTAX_ERROR, getProblemId());
	}

 }
