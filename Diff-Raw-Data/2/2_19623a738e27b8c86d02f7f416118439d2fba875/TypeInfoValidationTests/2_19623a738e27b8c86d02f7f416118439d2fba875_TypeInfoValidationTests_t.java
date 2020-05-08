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
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.validation.TypeInfoValidator;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 
 @SuppressWarnings("nls")
 public class TypeInfoValidationTests extends TestCase {
 
 	private List<IProblem> validate(IBuildParticipant validator, String content) {
 		final TestBuildContext context = new TestBuildContext(content);
 		try {
 			validator.build(context);
 		} catch (CoreException e) {
 			fail(e.getMessage());
 		}
 		return context.getProblems();
 	}
 
 	public void testKnownType() throws CoreException {
 		final List<IProblem> problems = validate(new TypeInfoValidator(),
 				"var x:String");
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testUnknownType() throws CoreException {
 		final List<IProblem> problems = validate(new TypeInfoValidator(),
 				"var x:LongString");
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testDeprecatedType() throws CoreException {
 		final List<IProblem> problems = validate(new TypeInfoValidator(),
 				"var x:ExampleService2");
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_TYPE, problems.get(0)
 				.getID());
 	}
 
 	public void testValidMethodCall() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.execute()");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testUndefinedMethodCall() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.run()");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testMethodCallWrongParamCount() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.execute(1)");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETER_COUNT, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedMethodCall() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.executeCompatible()");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedMethodCall_TypeInference() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var q = x.execute().service");
 		code.add("q.executeCompatible()");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testPropertyAccess() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.name");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testDeprecatedPropertyAccess() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
		code.add("var name = x.nameCompatible");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testUndefinedPropertyAccess() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.noname");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testMethodAsPropertyAccess() throws CoreException {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.execute");
 		final List<IProblem> problems = validate(new TypeInfoValidator(), code
 				.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 }
