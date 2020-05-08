 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.runners;
 
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
 
 import de.fzi.cjunit.ConcurrentTest;
 import de.fzi.cjunit.runners.model.ConcurrentFrameworkMethod;
 import de.fzi.cjunit.runners.statements.ConcurrentStatement;
 
 public class ConcurrentRunnerTest {
 
 	static public class TestClass {
 		@Test public void method1() { }
 		@Test public void method2() { }
 		public void notTestMethod() { }
 	}
 
 	@Test
 	public void computeConcurrentTestMethodsForTestClass()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(TestClass.class);
 		List<FrameworkMethod> methods
 				= runner.computeConcurrentTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(0));
 	}
 
 	@Test
 	public void computeTestMethodsForTestClass() throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(TestClass.class);
 		List<FrameworkMethod> methods = runner.computeTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(2));
 		assertThat(methods, hasItems(
 				new FrameworkMethod(TestClass.class.getMethod(
 						"method1")),
 				new FrameworkMethod(TestClass.class.getMethod(
 						"method2"))
 				));
 	}
 
 	@Test
 	public void computeTestMethodsReturnsTheSame() throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(TestClass.class);
 		List<FrameworkMethod> methods1 = runner.computeTestMethods();
 		List<FrameworkMethod> methods2 = runner.computeTestMethods();
 
 		assertThat(methods1, sameInstance(methods2));
 	}
 
 	static public class ConcurrentTestClass {
 		@ConcurrentTest public void methodInConcurrent1() { }
 		@ConcurrentTest public void methodInConcurrent2() { }
 		public void notTestMethod() { }
 	}
 
 	@Test
 	public void computeConcurrentTestMethodsForConcurrentTestClass()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				ConcurrentTestClass.class);
 		List<FrameworkMethod> methods
 				= runner.computeConcurrentTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(2));
 		assertThat(methods, hasItems(
 				new FrameworkMethod(
 					ConcurrentTestClass.class.getMethod(
 						"methodInConcurrent1")),
 				new FrameworkMethod(
 					ConcurrentTestClass.class.getMethod(
 						"methodInConcurrent2"))
 				));
 	}
 
 	@Test
 	public void computeTestMethodsForConcurrentTestClass()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				ConcurrentTestClass.class);
 		List<FrameworkMethod> methods
 				= runner.computeConcurrentTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(2));
 		assertThat(methods, hasItems(
 				new FrameworkMethod(
 					ConcurrentTestClass.class.getMethod(
 						"methodInConcurrent1")),
 				new FrameworkMethod(
 					ConcurrentTestClass.class.getMethod(
 						"methodInConcurrent2"))
 				));
 	}
 
 	static public class MixedTestClass {
 		@Test public void methodInMixed1() { }
 		@Test public void methodInMixed2() { }
 		@ConcurrentTest public void concurrentMethodInMixed1() { }
 		@ConcurrentTest public void concurrentMethodInMixed2() { }
 		public void notTestMethod() { }
 	}
 
 	@Test
 	public void computeConcurrentTestMethodsForMixedTestClass()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				MixedTestClass.class);
 		List<FrameworkMethod> methods
 				= runner.computeConcurrentTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(2));
 		assertThat(methods, hasItems(
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"concurrentMethodInMixed1")),
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"concurrentMethodInMixed2"))
 				));
 	}
 
 	@Test
 	public void computeTestMethodsForMixedTestClass()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				MixedTestClass.class);
 		List<FrameworkMethod> methods
 				= runner.computeTestMethods();
 
 		assertThat("number of returned methods",
 				methods.size(), equalTo(4));
 		assertThat(methods, hasItems(
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"methodInMixed1")),
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"methodInMixed2")),
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"concurrentMethodInMixed1")),
 				new FrameworkMethod(
 					MixedTestClass.class.getMethod(
 						"concurrentMethodInMixed2"))
 				));
 	}
 
	static public class TestAndConcurrentTestClass {
		@Test @ConcurrentTest public void testMethod() { }
	}

	@Test(expected=InitializationError.class)
	public void errorOnTestAndConcurrentTest() throws Throwable {
		ConcurrentRunner runner = new ConcurrentRunner(
				TestAndConcurrentTestClass.class);
		runner.hashCode();	// to avoid unused variable warning
	}

 	@Test
 	public void returnStatementForTestMethod() throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				MixedTestClass.class);
 		FrameworkMethod method = new FrameworkMethod(
 				MixedTestClass.class.getMethod(
 						"methodInMixed1"));
 		Statement statement = runner.methodInvoker(method, null);
 
 		assertThat(statement, instanceOf(Statement.class));
 		assertThat(statement,
 				not(instanceOf(ConcurrentStatement.class)));
 	}
 
 	@Test
 	public void returnConcurrentStatementForConcurrentTestMethod()
 			throws Throwable {
 		ConcurrentRunner runner = new ConcurrentRunner(
 				MixedTestClass.class);
 		FrameworkMethod method = new ConcurrentFrameworkMethod(
 				MixedTestClass.class.getMethod(
 						"concurrentMethodInMixed1"));
 		Statement statement = runner.methodInvoker(method, null);
 
 		assertThat(statement, instanceOf(ConcurrentStatement.class));
 	}
 }
