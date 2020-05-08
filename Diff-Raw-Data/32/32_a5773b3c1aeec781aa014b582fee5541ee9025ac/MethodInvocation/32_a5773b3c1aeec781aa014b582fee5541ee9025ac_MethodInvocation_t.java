 package org.kalibro.core.concurrent;
 
 import java.lang.reflect.Method;
 
 class MethodInvocation {
 
 	private Object object;
 	private Method method;
 	private Object[] arguments;
 
 	private boolean done;
 	private Object result;
 	private Throwable error;
 
 	protected MethodInvocation(Object object, Method method, Object... arguments) {
 		this.object = object;
 		this.method = method;
 		this.arguments = arguments;
 	}
 
 	protected void invoke() {
 		try {
 			result = method.invoke(object, arguments);
 		} catch (Exception exception) {
 			error = exception;
 		}
 		done = true;
 	}
 
 	protected boolean done() {
 		return done;
 	}
 
	protected Object getResult() throws Throwable {
		if (error != null)
			throw error;
 		return result;
 	}
 }
