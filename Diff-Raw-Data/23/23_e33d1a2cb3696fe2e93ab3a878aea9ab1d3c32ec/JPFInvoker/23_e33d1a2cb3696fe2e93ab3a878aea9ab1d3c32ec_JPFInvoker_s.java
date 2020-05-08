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
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.nasa.jpf.Config;
 import gov.nasa.jpf.Error;
 import gov.nasa.jpf.JPF;
 import gov.nasa.jpf.report.Publisher;
 
 import de.fzi.cjunit.jpf.inside.TestWrapper;
 import de.fzi.cjunit.jpf.outside.TestFailedProperty;
 import de.fzi.cjunit.jpf.util.ArgumentCreator;
 import de.fzi.cjunit.jpf.util.OnFailurePublisher;
 import de.fzi.cjunit.jpf.util.TestReporter;
 
 
 public class JPFInvoker {
 
 	protected Config conf;
 	protected JPF jpf;
 	protected ResultCollector rc;
 
 	public void run(Object target, Method method,
 			List<Method> beforeMethods, List<Method> afterMethods,
 			Class<? extends Throwable> exceptionClass)
 			throws Throwable {
 		initJPF(createJPFArgs(target, method, beforeMethods,
 				afterMethods, exceptionClass));
 		runJPF();
 		checkResult();
 	}
 
 	/**
 	 * Returns whether the test was successful (none of the properties was
 	 * violated) or failed (at least one of the properties was violated).
 	 *
 	 * @return	<tt>true</tt> if the test succeeded, <tt>false</tt> if
 	 *		failed.
 	 */
 	public boolean getTestResult() {
 		return rc.getTestResult();
 	}
 
 	public void checkResult() throws Throwable {
 		if (rc.getTestResult() == false) {
 			throw rc.getException();
 		}
 	}
 
 	protected void initJPF(String[] args) {
 		conf = JPF.createConfig(args);
 		jpf = new JPF(conf);
 		createTestProperties();
 		registerAtPublisher();
 	}
 
 	protected void runJPF() {
 		jpf.run();
 	}
 
 	protected List<Error> getJPFSearchErrors() {
 		return jpf.getSearchErrors();
 	}
 
 	void createTestProperties() {
 		TestFailedProperty tfp = new TestFailedProperty();
 		jpf.addPropertyListener(tfp);
 		rc = new ResultCollector(jpf.getReporter(), tfp);
 		jpf.addListener(rc);
 	}
 
 	protected void registerAtPublisher() {
 		for (Publisher p : jpf.getReporter().getPublishers()) {
 			if (p instanceof OnFailurePublisher) {
 				((OnFailurePublisher) p).setJPFInvoker(this);
 			}
 		}
 	}
 
 	protected String[] createJPFArgs(Object target, Method method,
 			List<Method> beforeMethods, List<Method> afterMethods,
 			Class<? extends Throwable> exceptionClass) {
 		List<String> testArgs = new ArrayList<String>();
 		testArgs.add("--testclass=" + target.getClass().getName());
 		testArgs.add("--testmethod=" + method.getName());
 		for (Method beforeMethod : beforeMethods) {
 			testArgs.add("--beforemethod=" +
 					beforeMethod.getName());
 		}
 		for (Method afterMethod : afterMethods) {
 			testArgs.add("--aftermethod=" +
 					afterMethod.getName());
 		}
 		if (exceptionClass != null) {
 			testArgs.add("--expectedexception=" +
 					exceptionClass.getName());
 		}
 
 		return new ArgumentCreator()
 			.publisher(OnFailurePublisher.class)
 			.reporter(TestReporter.class)
 			.jpfArgs(new String[] {
 					"+search.multiple_errors=true",
 					"+jpf.report.console.start=",
 					"+jpf.report.console.finished=result",
 					"+jpf.report.console.show_steps=true"
 				})
 			.app(TestWrapper.class)
 			.appArgs(testArgs)
 			.getArgs();
 	}
 }
