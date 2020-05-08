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
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import static de.fzi.cjunit.jpf.inside.NotifierMethods.*;
 
 
 public class TestWrapper {
 
 	String testClassName;
 	String testMethodName;
 	String expectedExceptionName;
 
 	Object target;
 	Method method;
 
 	public TestWrapper(String... args) {
 		parseArgs(args);
 	}
 
 	public void parseArgs(String... args) {
 		if (args == null) {
 			throw new RuntimeException("no command line arguments");
 		}
 
 		for (String arg : args) {
 			if (arg.startsWith("--testclass=")) {
 				testClassName = getArgumentValue(arg);
 			} else if (arg.startsWith("--testmethod=")) {
 				testMethodName = getArgumentValue(arg);
 			} else if (arg.startsWith("--expectedexception=")) {
 				expectedExceptionName = getArgumentValue(arg);
 			} else {
 				throw new RuntimeException("wrong command " +
 						"line parameter: " + arg);
 			}
 		}
 	}
 
 	String getArgumentValue(String arg) {
 		String value = arg.substring(arg.indexOf('=')+1);
 		if (value.length() == 0) {
 			throw new RuntimeException(
 					"wrong command line parameter: " +
 					"option without value: " + arg);
 		}
 		return value;
 	}
 
 	public void run() {
 		try {
 			createTest();
 			runTest();
 		} catch (Throwable t) {
 			testFailed();
 		}
 	}
 
 	public void createTest() throws IllegalArgumentException,
 			SecurityException, InstantiationException,
 			IllegalAccessException, InvocationTargetException,
 			NoSuchMethodException, ClassNotFoundException {
 		// The testing framework calling this has already checked
 		// that both the class and the method exists, so they won't
 		// throw any exceptions
 		createTestObject();
 		createTestMethod();
 	}
 
 	public void runTest() throws IllegalArgumentException,
 			IllegalAccessException, AssertionError, Throwable {
 		try {
 			method.invoke(target);
 			if (isExpectingException()) {
 				throw new AssertionError(
 						"Expected exception: " +
 						expectedExceptionName);
 			}
 		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (!isExpectingException()) {
				throw cause;
			}
			if (!isExpectedException(cause)) {
				String message
					= "Unexpected exception, expected<"
					+ expectedExceptionName + "> but was<"
					+ cause.getClass().getName() + ">";
				throw new Exception(message, cause);
 			}
 		}
 	}
 
 	public boolean isExpectingException() {
 		return expectedExceptionName != null;
 	}
 
 	public boolean isExpectedException(Throwable t) {
 		return t.getClass().getName().equals(expectedExceptionName);
 	}
 
 	public void createTestObject() throws
 			IllegalArgumentException, SecurityException,
 			InstantiationException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException,
 			ClassNotFoundException {
 		target = Class.forName(testClassName)
 				.getConstructor()
 				.newInstance();
 	}
 
 	public void createTestMethod() throws SecurityException,
 			NoSuchMethodException {
 		method = createMethod(testMethodName);
 	}
 
 	public Method createMethod(String methodName) throws SecurityException,
 			NoSuchMethodException {
 		return target.getClass().getMethod(methodName);
 	}
 
 	public static void main(String... args) {
 		new TestWrapper(args).run();
 	}
 }
