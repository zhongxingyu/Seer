 package org.eclipse.b3.backend.core;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BInvocationContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BSystemContext;
 import org.eclipse.b3.backend.functions.ArithmeticFunctions;
 import org.eclipse.b3.backend.functions.RelationalFunctions;
 import org.eclipse.b3.backend.functions.StringFunctions;
 import org.eclipse.b3.backend.functions.SystemFunctions;
 
 public class B3Engine {
 	protected BSystemContext systemContext;
 	protected BInvocationContext invocationContext;
 	
 	public B3Engine () {
 		systemContext = B3backendFactory.eINSTANCE.createBSystemContext();
 		invocationContext = B3backendFactory.eINSTANCE.createBInvocationContext();
 		invocationContext.setParentContext(systemContext);
 		try {
 		invocationContext.loadFunctions(ArithmeticFunctions.class);
 		invocationContext.loadFunctions(RelationalFunctions.class);
 		invocationContext.loadFunctions(StringFunctions.class);
 		invocationContext.loadFunctions(SystemFunctions.class);
 		} catch(B3EngineException e) {
 			// should not really happen as this is loading default functions
 			e.printStackTrace();
 		}
 		loadInstanceMethods(); // experimental
 	}
 	public BExecutionContext getContext() {
 		return invocationContext;
 	}
 	/** 
 	 * Experimental code - to be removed
 	 */
 	private void loadInstanceMethods() {
 		Method m = null;
 		try {
 			m = String.class.getMethod("format", String.class, Array.newInstance(Object.class, 0).getClass());
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} 
 		try {
			invocationContext.loadFunction(m);
 		} catch (B3EngineException e) {
 			e.printStackTrace();
 		}
 	}
 }
