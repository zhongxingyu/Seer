 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.outside;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.Test;
 
 import gov.nasa.jpf.jvm.JVM;
 import gov.nasa.jpf.jvm.MethodInfo;
 import gov.nasa.jpf.jvm.bytecode.ATHROW;
 import gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL;
 import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
 
 import de.fzi.cjunit.jpf.exceptioninfo.ExceptionInfoDefaultImpl;
 import de.fzi.cjunit.jpf.inside.NotifierMethods;
 import de.fzi.cjunit.testutils.Counter;
 import de.fzi.cjunit.testutils.TestException;
 
 
 public class TestFailedPropertyTest {
 
 	@Test
 	public void handleInstructionInvokesHandleInvokeInstruction() {
 		final Counter invocationCounter = new Counter();
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected void handleInvokeInstruction(JVM vm,
 					InvokeInstruction insn) {
 				invocationCounter.increment();
 			}
 		};
 		tfp.handleInstruction(null, new INVOKEVIRTUAL());
 		assertThat("handleInvokeInstruction() invoked",
 				invocationCounter.getValue(), equalTo(1));
 	}
 
 	@Test
 	public void handleInstructionDoesNotInvokeHandleInvokeInstruction() {
 		final Counter invocationCounter = new Counter();
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected void handleInvokeInstruction(JVM vm,
 					InvokeInstruction insn) {
 				invocationCounter.increment();
 			}
 		};
 		tfp.handleInstruction(null, new ATHROW());
 		assertThat("handleInvokeInstruction() not invoked",
 				invocationCounter.getValue(), equalTo(0));
 	}
 
 	@Test
 	public void handleMethodInvocationNotFailsWithNullCallee() {
 		final Counter invocationCounter = new Counter();
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected void testFailed(JVM vm) {
 				invocationCounter.increment();
 			}
 		};
 		tfp.handleMethodInvocation(null, null);
 		assertThat("testFailed() not invoked",
 				invocationCounter.getValue(), equalTo(0));
 	}
 
 	@Test
 	public void handleMethodInvocationWhenNotNotifierMethod() {
 		final class NotNotifierMethodInfo extends MethodInfo {
 			@Override
 			public String getClassName() {
 				return "some random invalid class";
 			}
 		}
 		final Counter invocationCounter = new Counter();
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected void testFailed(JVM vm) {
 				invocationCounter.increment();
 			}
 		};
 		tfp.handleMethodInvocation(null, new NotNotifierMethodInfo());
 		assertThat("testFailed() not invoked",
 				invocationCounter.getValue(), equalTo(0));
 	}
 
 	@Test
 	public void handleMethodInvocationInvokesTestFailed() {
 		final class TestFailedMethodInfo extends MethodInfo {
 			@Override
 			public String getClassName() {
 				return NotifierMethods.class.getName();
 			}
 			@Override
 			public String getName() {
 				return "testFailed";
 			}
 		}
 		final Counter invocationCounter = new Counter();
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected void testFailed(JVM vm) {
 				invocationCounter.increment();
 			}
 		};
 		tfp.handleMethodInvocation(null, new TestFailedMethodInfo());
 		assertThat("testFailed() invoked", invocationCounter.getValue(),
 				equalTo(1));
 	}
 
 	@Test
 	public void testFailedSetsException() {
 		final Throwable testException = new TestException("asdf");
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected Throwable reconstructException(JVM vm) {
 				return testException;
 			}
 		};
 		tfp.testFailed(null);
 		assertThat(tfp.exception, equalTo(testException));
 	}
 
 	@Test
 	public void testFailedSetsErrorMessage() {
		final TestException testException = new TestException("asdf");
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			protected Throwable reconstructException(JVM vm) {
				return testException;
 			}
 		};
 		tfp.testFailed(null);
		assertThat(tfp.errorMessage, containsString("test failed"));
		assertThat(tfp.errorMessage,
				containsString(tfp.exception.getClass().getName()));
		assertThat(tfp.errorMessage,
				containsString(tfp.exception.getMessage()));
 	}
 
 	@Test
 	public void reportExceptionDuringCollectingExceptionInfo()
 			throws Throwable {
 		TestFailedProperty tfp = new TestFailedProperty() {
 			@Override
 			public ExceptionInfoDefaultImpl collectExceptionInfo(JVM vm)
 					throws Exception {
 				throw new Exception("exception in TestFailedProperty");
 			}
 		};
 		tfp.testFailed(null);
 
 		assertThat("test result", tfp.getTestResult(), equalTo(false));
 
 		Throwable t = tfp.getException();
 		assertThat("exception type", t, instanceOf(
 				ExceptionReconstructionException.class));
 		assertThat("has cause", t.getCause(), notNullValue());
 		assertThat("causing exception's type", t.getCause(),
 				instanceOf(Exception.class));
 		assertThat("causing exception's message",
 				t.getCause().getMessage(),
 				equalTo("exception in TestFailedProperty"));
 	}
 
 	@Test
 	public void searchStartedDoesNotRegisterAtSearch() {
 		TestFailedProperty tfp = new TestFailedProperty();
 		// Registering at Search means invoking a method on a null
 		// reference, which would cause a NPE here.
 		tfp.searchStarted(null);
 	}
 }
