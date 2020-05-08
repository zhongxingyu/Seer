 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.exceptioninfo;
 
 import gov.nasa.jpf.jvm.ElementInfo;
 
 
 public class ExceptionWrapper extends ElementInfoWrapper
 		implements ExceptionInfo {
 
 	StackTraceElementWrapper[] stackTrace;
 	ExceptionWrapper cause;
 	boolean causeInitialized;
 
 	public ExceptionWrapper(ElementInfo elementInfo) {
 		super(elementInfo, ExceptionInfoDefaultImpl.class);
 	}
 
 	@Override
 	public String getClassName() {
 		return getStringField("className");
 	}
 
 	@Override
 	public String getMessage() {
 		return getStringField("message");
 	}
 
 	public int getStackTraceDepth() {
 		return getArrayLength("stackTrace");
 	}
 
 	@Override
 	public StackTraceElementInfo[] getStackTrace() {
 		if (stackTrace != null) {
 			return stackTrace;
 		}
 
 		ElementInfo[] array = getReferenceArray("stackTrace");
		stackTrace = new StackTraceElementWrapper[array.length];
 
 		int i = 0;
 		for (ElementInfo ei : array) {
 			stackTrace[i] = new StackTraceElementWrapper(ei);
 			i++;
 		}
 
 		return stackTrace;
 	}
 
 	@Override
 	public ExceptionInfo getCause() {
 		if (causeInitialized) {
 			return cause;
 		}
 
 		ElementInfo ei = getElementInfoForField("cause");
 		if (ei == null) {
 			cause = null;
 		} else {
 			cause = new ExceptionWrapper(ei);
 		}
 		causeInitialized = true;
 
 		return cause;
 	}
 
 	@Override
 	public boolean hasCause() {
 		return getCause() != null;
 	}
 }
