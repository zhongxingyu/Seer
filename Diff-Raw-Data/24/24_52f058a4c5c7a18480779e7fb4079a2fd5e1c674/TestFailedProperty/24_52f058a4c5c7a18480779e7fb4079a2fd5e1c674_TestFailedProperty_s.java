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
 
 import java.util.Stack;
 
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
 
 	protected boolean result = true;
 	protected boolean testSucceeded = true;
 
 	protected Throwable exception;
 
 	protected Stack<Boolean> stateStack = new Stack<Boolean>();
 
 	// from TestProperty
 	@Override
 	public boolean getTestResult() {
 		return testSucceeded;
 	}
 
 	@Override
 	public Throwable getException() {
 		return exception;
 	}
 
 	protected void testFailed(JVM vm) {
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
 		result = false;
 		testSucceeded = false;
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
 		return result;
 	}
 
 	@Override
 	public String getErrorMessage() {
		return "test failed";
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
 		if (callee == null || !callee.getClassName().equals(
 				NotifierMethods.class.getName())) {
 			return;
 		}
 
 		if (callee.getName().equals("testFailed")) {
 			testFailed(vm);
 		}
 	}
 
 	// from SearchListener
 	@Override
 	public void stateAdvanced(Search search) {
 		stateStack.push(result);
 	}
 
 	@Override
 	public void stateBacktracked(Search search) {
 		result = stateStack.pop();
 	}
 
 	@Override
 	public void searchStarted(Search search) {
 		// do not register as property
 	}
 }
