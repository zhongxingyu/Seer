 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.inside;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.Test;
 
 import java.lang.reflect.Method;
 
 public class TestWrapperTest {
 
 	String className = "java.lang.String";
 	String methodName = "toString";
 	String exceptionName = "java.lang.AssertionError";
 
 	@Test
 	public void parseArgsTestClass() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className });
 		assertThat(tw.testClassName, equalTo(className));
 	}
 
 	@Test
 	public void parseArgsTesMethod() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testmethod=" + methodName });
 		assertThat(tw.testMethodName, equalTo(methodName));
 	}
 
 	@Test
 	public void parseArgsExpectedException() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--expectedexception=" + exceptionName });
 		assertThat(tw.expectedExceptionName, equalTo(exceptionName));
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsWrongArgument() {
 		new TestWrapper(new String[] { "asdf" });
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsNoValue() {
 		new TestWrapper(new String[] { "--testclass=" });
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsNoArguments() {
 		new TestWrapper((String[]) null);
 	}
 
 	@Test
 	public void createTestObject() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className });
 		tw.createTestObject();
 		assertThat(tw.target, instanceOf(String.class));
 	}
 
 	@Test
 	public void createMethod() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className });
 		tw.createTestObject();
 		Method m = tw.createMethod(methodName);
 		assertThat(m.getName(), equalTo(methodName));
 		assertThat(m.getDeclaringClass().getName(), equalTo(className));
 	}
 
 	@Test
 	public void createTestMethod() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className,
 				"--testmethod=" + methodName
 				});
 		tw.createTestObject();
 		tw.createTestMethod();
 		assertThat(tw.method.getName(), equalTo(methodName));
 		assertThat(tw.method.getDeclaringClass().getName(),
 				equalTo(className));
 	}
 
 	public class ATestException extends Throwable {
 		private static final long serialVersionUID = 1L;
 	}
 
 	public class BTestException extends Throwable {
 		private static final long serialVersionUID = 1L;
 	}
 
 	@Test
 	public void isExpectedExceptionTrue() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--expectedexception="
 					+ ATestException.class.getName()
 				});
 		assertThat(tw.isExpectedException(new ATestException()),
 				equalTo(true));
 	}
 
 	@Test
 	public void isExpectedExceptionFalse() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--expectedexception="
 					+ ATestException.class.getName()
 				});
 		assertThat(tw.isExpectedException(new BTestException()),
 				equalTo(false));
 	}
 
 	@Test(expected=AssertionError.class)
 	public void testExpectingExceptionButNoneIsThrown() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testmethod=" + methodName,
 				"--expectedexception="
 					+ ATestException.class.getName()
 				}) {
 			public void createTestObject() {
 				target = new Object();
 			}
 		};
 
 		tw.createTest();
 		tw.runTest();
 	}
 
 	public class TestClass {
 		public void method() throws Throwable { }
 	}
 
 	@Test
 	public void testExpectedExceptionIsCatched() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testmethod=method",
 				"--expectedexception="
 					+ ATestException.class.getName()
 				}) {
 			public void createTestObject() {
 				target = new TestClass() {
 					public void method() throws Throwable {
 						throw new ATestException();
 					}
 				};
 			}
 		};
 
 		tw.createTest();
 		tw.runTest();
 	}
 
	@Test(expected=Exception.class)
 	public void testUnexpectedExceptionIsThrown() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testmethod=method",
 				"--expectedexception="
 					+ ATestException.class.getName()
 				}) {
 			public void createTestObject() {
 				target = new TestClass() {
 					public void method() throws Throwable {
 						throw new BTestException();
 					}
 				};
 			}
 		};
 
 		tw.createTest();
 		tw.runTest();
 	}
 }
