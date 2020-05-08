 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.util;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import static de.fzi.cjunit.util.LineSeparator.lineSeparator;
 
 import de.fzi.cjunit.jpf.exceptioninfo.ExceptionInfo;
 
 
 public class ExceptionFactory {
 
 	public Throwable createException(ExceptionInfo exceptionInfo)
 			throws ClassNotFoundException, IllegalArgumentException,
 				InstantiationException, IllegalAccessException,
				InvocationTargetException,
				NoSuchMethodException {
 		if (exceptionInfo == null) {
 			return null;
 		}
 
 		Throwable cause = null;
 		if (exceptionInfo.hasCause()) {
 			cause = createException(exceptionInfo.getCause());
 		}
 
 		Class<?> exceptionClass = Class.forName(
 				exceptionInfo.getClassName());
 		Constructor<?> constructor = getExceptionConstructor(
 				exceptionClass);
 		if (constructor == null) {
			throw new NoSuchMethodException(
 					"exception with no suitable " +
 					"constructor thrown inside JPF" +
 					lineSeparator +
 					"type: " +
 					exceptionInfo.getClassName() +
 					lineSeparator +
 					"message: " +
 					exceptionInfo.getMessage());
 		}
 
 		Throwable t = (Throwable) constructor.newInstance(
 				new Object[] { exceptionInfo.getMessage() });
 		t.initCause(cause);
 		t.setStackTrace(new StackFrameConverter().toStackTrace(
 					exceptionInfo.getStackTrace()));
 		return t;
 	}
 
 	protected Constructor<?> getExceptionConstructor(
 			Class<?> exceptionClass) {
 		Constructor<?> constructor = getCheckedConstructor(
 				exceptionClass, String.class);
 		if (constructor != null)
 			return constructor;
 		return getCheckedConstructor(
 				exceptionClass, Object.class);
 	}
 
 	protected Constructor<?> getCheckedConstructor(
 			Class<?> exceptionClass, Class<?> constructorArg) {
 		Class<?>[] args = new Class[] { constructorArg };
 		try {
 			return exceptionClass.getConstructor(args);
 		} catch (NoSuchMethodException e) { }
 		return null;
 	}
 }
