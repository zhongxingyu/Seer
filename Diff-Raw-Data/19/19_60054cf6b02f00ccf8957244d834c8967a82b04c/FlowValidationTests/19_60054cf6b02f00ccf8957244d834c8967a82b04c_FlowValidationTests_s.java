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
 package org.eclipse.dltk.javascript.core.tests.validation;
 
 import java.util.Set;
 
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.validation.FlowValidation;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.core.tests.AbstractValidationTest;
import org.eclipse.dltk.javascript.parser.JSProblem;
 
 public class FlowValidationTests extends AbstractValidationTest {
 
 	@Override
 	protected IBuildParticipant createValidator() {
 		return new FlowValidation();
 	}
 
 	public void testUnreachableCodeAfterReturn() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else return 0");
 		code.add("  var x = 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testUnreachableCodeAfterBreak() {
 		StringList code = new StringList();
 		code.add("function x() {");
 		code.add("  for (;;) {");
 		code.add("    var q = 0");
 		code.add("    break;");
 		code.add("    ++q");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testUnreachableCodeAfterContinue() {
 		StringList code = new StringList();
 		code.add("function x() {");
 		code.add("  for (;;) {");
 		code.add("    var q = 0");
 		code.add("    continue;");
 		code.add("    ++q");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testFunctionAlwaysReturnValue1() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 	}
 
 	public void testFunctionAlwaysReturnValue2() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  return");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(2, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 		assertTrue(problemIds.contains(JavaScriptProblems.RETURN_INCONSISTENT));
 	}
 
 	public void testFunctionWithIfThenNotAllReturningValuesWithReturn() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else a = 1");
 		code.add("  return 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionWithIfThenNotAllReturningValues() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else if (a == 2) a = 1");
 		code.add("  else if (a == 3) a = 1");
 		code.add("  else return 2");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 	}
 
 	public void testFunctionWithIfThenAllReturningValues() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else if (a == 2) return 2");
 		code.add("  else if (a == 3) return 3");
 		code.add("  else return -1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionWithThrowsStatement() {
 
 		StringList code = new StringList();
 		code.add("/**");
 		code.add("* @return {String} some result info");
 		code.add("* @throws {String} some error info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("var result_1 = '';");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return result_1;}");
 		code.add("throw 'Some error message';");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionReturnInFinally() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  try {");
 		code.add("    if (a ==1) return 1");
 		code.add("  } finally {");
 		code.add("    return 0;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testThrowCatch() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    throw new Error('aaa')");
 		code.add("  } catch (e) {");
 		code.add("    x++");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testReturnCatch() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    return 0");
 		code.add("  } catch (e) {");
 		code.add("    x++");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testReturnCatchReturn() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    return 0");
 		code.add("  } catch (e) {");
 		code.add("    return 1");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertEquals(JavaScriptProblems.UNREACHABLE_CODE, problemIds.iterator()
 				.next());
 	}
 }
