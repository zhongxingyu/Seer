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
 
 import static de.fzi.cjunit.internal.util.LineSeparator.lineSeparator;
 
 import gov.nasa.jpf.PropertyListenerAdapter;
 import gov.nasa.jpf.jvm.JVM;
 import gov.nasa.jpf.jvm.MethodInfo;
 import gov.nasa.jpf.jvm.ThreadInfo;
 import gov.nasa.jpf.jvm.bytecode.Instruction;
 import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
 import gov.nasa.jpf.search.Search;
 
 import de.fzi.cjunit.jpf.exceptioninfo.ExceptionInfo;
 import de.fzi.cjunit.jpf.inside.NotifierMethods;
 import de.fzi.cjunit.jpf.util.ExceptionFactory;
 
 
 public class TestFailedProperty extends PropertyListenerAdapter
 		implements TestProperty {
 
 	public final static String errorMessageOnSuccessAfterFailure
 			= "Test succeeded after previous failure";
 
 	protected boolean reportSuccessAsFailure = false;
 
 	protected boolean foundFailedPath = false;
 	protected boolean foundSucceededPath = false;
 
 	protected Throwable exception;
 	protected String errorMessage;
 
 
 	public void reportSuccessAsFailure() {
 		reportSuccessAsFailure = true;
 	}
 
 	public boolean foundFailedPath() {
 		return foundFailedPath;
 	}
 
 	public boolean foundSucceededPath() {
 		return foundSucceededPath;
 	}
 
 	// from TestProperty
 	@Override
 	public boolean getTestResult() {
 		return exception == null;
 	}
 
 	@Override
 	public Throwable getException() {
 		return exception;
 	}
 
 	public class TestSucceededException extends Exception {
 		private static final long serialVersionUID = 1L;
 	}
 
 	protected void testSucceeded(JVM vm) {
 		foundSucceededPath = true;
 		if (reportSuccessAsFailure) {
 			exception = new TestSucceededException();
 			errorMessage = errorMessageOnSuccessAfterFailure;
 		}
 	}
 
 	protected void testFailed(JVM vm) {
 		foundFailedPath = true;
 		try {
 			exception = reconstructException(vm);
 		} catch (Throwable t) {
 			// JPF catches exceptions thrown in property listeners
 			// during execution and eats them.  But we would like
 			// to report the exception eventually thrown during
 			// the reconstruction of the exception thrown by the
 			// test.  Therefore, we store that exception here as if
 			// it would have been thrown in the test.
 			exception = new ExceptionReconstructionException(
 					"could not reconstruct the exception "
 					+ "thrown during the test", t);
 		}
 		errorMessage = createErrorMessage();
 	}
 
 	protected String createErrorMessage() {
 		return "test failed: " + lineSeparator
 				+ exception.getClass().getName() + ":"
 				+ lineSeparator + exception.getMessage();
 	}
 
 	protected Throwable reconstructException(JVM vm) throws Exception {
 		return new ExceptionFactory().createException(
 				collectExceptionInfo(vm));
 	}
 
 	protected ExceptionInfo collectExceptionInfo(JVM vm)
 			throws Exception {
 		return new ExceptionInfoCollector().collectFromStack(vm);
 	};
 
 	// from Property
 	@Override
 	public boolean check(Search search, JVM jvm) {
 		return getTestResult();
 	}
 
 	@Override
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 	@Override
 	public void reset() {
 		exception = null;
 		errorMessage = null;
 	}
 
 	// from VMListener
 	@Override
 	public void executeInstruction(JVM vm) {
 		handleInstruction(vm, vm.getLastInstruction());
 	}
 
 	protected void handleInstruction(JVM vm, Instruction insn) {
 		if (insn instanceof InvokeInstruction) {
 			handleInvokeInstruction(vm, (InvokeInstruction) insn);
 		}
 	}
 
 	protected void handleInvokeInstruction(JVM vm, InvokeInstruction insn) {
 		ThreadInfo ti = vm.getLastThreadInfo();
 		MethodInfo callee = insn.getInvokedMethod(ti);
 
 		handleMethodInvocation(vm, callee);
 	}
 
 	protected void handleMethodInvocation(JVM vm, MethodInfo callee) {
		if (callee != null)
			handleRealMethodInvocation(vm, callee.getClassName(),
					callee.getName());
	}

	protected void handleRealMethodInvocation(JVM vm,
			String calleeClassName, String calleeMethodName) {
		if (!calleeClassName.equals(
 				NotifierMethods.class.getName())) {
 			return;
 		}
 
		if (calleeMethodName.equals("testFailed")) {
 			testFailed(vm);
		} else if (calleeMethodName.equals("testSucceeded")) {
 			testSucceeded(vm);
 		}
 	}
 
 	// from SearchListener
 	@Override
 	public void searchStarted(Search search) {
 		// do not register as property
 	}
 }
