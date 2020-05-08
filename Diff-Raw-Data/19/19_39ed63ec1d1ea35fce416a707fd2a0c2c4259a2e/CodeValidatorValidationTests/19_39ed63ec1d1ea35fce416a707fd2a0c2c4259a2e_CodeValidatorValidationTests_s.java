 package org.eclipse.dltk.javascript.core.tests.validation;
 
 import java.util.Set;
 
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.validation.TypeInfoValidator;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.core.tests.AbstractValidationTest;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserProblems;
 
 public class CodeValidatorValidationTests extends AbstractValidationTest {
 
 	@Override
 	protected IBuildParticipant createValidator() {
 		return new TypeInfoValidator();
 	}
 
 	public void testFunctionHidesNestedFunction() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add(" function test() {");
 		code.add(" }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_HIDES_FUNCTION));
 
 	}
 
 	public void testFunctionHidesFunction() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptParserProblems.DUPLICATE_FUNCTION));
 
 	}
 
 	public void testFunctionHidesVariable() {
 		StringList code = new StringList();
 		code.add("var test;");
 		code.add("function test() {");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertTrue(
 				problemIds.toString(),
 				problemIds
 						.contains(JavaScriptParserProblems.FUNCTION_DUPLICATES_OTHER));
 	}
 
 	public void testArgumentHidesVariable() {
 		StringList code = new StringList();
 		code.add("var test;");
 		code.add("function test2(test) {");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.toString(),
 				problemIds
 						.contains(JavaScriptProblems.PARAMETER_HIDES_VARIABLE));
 	}
 
 	public void testArgumentHidesFunciton() {
 		StringList code = new StringList();
 		code.add("function test(){}");
 		code.add("function test2(test) {");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.toString(),
 				problemIds
 						.contains(JavaScriptProblems.PARAMETER_HIDES_FUNCTION));
 	}
 
 	public void testVariableHidesArgument() {
 		StringList code = new StringList();
 		code.add("function test2(test) {");
 		code.add("var test;");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertTrue(
 				problemIds.toString(),
 				problemIds
 						.contains(JavaScriptParserProblems.VAR_DUPLICATES_OTHER));
 	}
 
 	public void testVariableHidesOwnFunction() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add("var test;");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.toString(),
 				problemIds.contains(JavaScriptProblems.VAR_HIDES_FUNCTION));
 	}
 
 	public void testVariableHidesFunction() {
 		StringList code = new StringList();
 		code.add("function test() {}");
 		code.add("function test1() {");
 		code.add("var test;");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.toString(),
 				problemIds.contains(JavaScriptProblems.VAR_HIDES_FUNCTION));
 
 	}
 
 }
