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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
import org.junit.Test;
 
 import de.fzi.cjunit.ConcurrentTest;
 import de.fzi.cjunit.runners.model.ConcurrentFrameworkMethod;
 import de.fzi.cjunit.runners.statements.ConcurrentStatement;
 
 public class ConcurrentRunner extends BlockJUnit4ClassRunner {
 
 	List<FrameworkMethod> testMethods;
 
 	public ConcurrentRunner(Class<?> klass) throws InitializationError {
 		super(klass);
 	}
 
 	@Override
 	protected void collectInitializationErrors(List<Throwable> errors) {
 		super.collectInitializationErrors(errors);
 		validateConcurrentTestMethods(errors);
 	}
 
 	@Override
 	protected List<FrameworkMethod> computeTestMethods() {
 		if (testMethods == null) {
 			testMethods = new ArrayList<FrameworkMethod>(
 					super.computeTestMethods());
 			testMethods.addAll(computeConcurrentTestMethods());
 		}
 		return testMethods;
 	}
 
 	protected List<FrameworkMethod> computeConcurrentTestMethods() {
 		List<FrameworkMethod> methods
 				= getTestClass().getAnnotatedMethods(
 						ConcurrentTest.class);
 		List<FrameworkMethod> concurrentMethods
 				= new ArrayList<FrameworkMethod>();
 		for (FrameworkMethod eachMethod : methods) {
 			concurrentMethods.add(new ConcurrentFrameworkMethod(
 						eachMethod.getMethod()));
 		}
 		return concurrentMethods;
 	}
 
 	protected void validateConcurrentTestMethods(List<Throwable> errors) {
 		validatePublicVoidNoArgMethods(ConcurrentTest.class, false,
 				errors);

		List<FrameworkMethod> methods
				= getTestClass().getAnnotatedMethods(
						ConcurrentTest.class);
		for (FrameworkMethod eachMethod : methods) {
			if (eachMethod.getAnnotation(Test.class) != null) {
				String gripe = "@Test and @ConcurrentTest " +
						"annotations are mutually " +
						"exclusive";
				errors.add(new Exception(gripe));
			}
		}
 	}
 
 	@Override
 	protected Statement methodInvoker(FrameworkMethod method, Object test) {
 		if (method.getClass() == ConcurrentFrameworkMethod.class) {
 			return new ConcurrentStatement(method, test);
 		} else {
 			return super.methodInvoker(method, test);
 		}
 	}
 }
