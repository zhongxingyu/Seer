 package com.insightfullogic.vertx.promises.codegen;
 
 import static java.util.Arrays.asList;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.vertx.java.core.Handler;
 
 public class ClassInspector {
 
     private Class<?> klass;
 	private ClassGenerator backend;
 
     public ClassInspector(Class<?> klass, ClassGenerator backend) {
         this.klass = klass;
 		this.backend = backend;
     }
 
     public void inspect() {
     	backend.newClass(klass);
         for (Method method : klass.getDeclaredMethods()) {
             inspectMethod(method);
         }
     }
 
 	public void inspectMethod(Method method) {
 		List<Class<?>> parameterTypes = new ArrayList<>(asList(method.getParameterTypes()));
		if (!requiresConversion(parameterTypes) && !wouldCauseClash(method)) {
 			backend.wrapMethod(method);
 		    return;
 		}
 
 		if(multiplehandlers(parameterTypes)) {
 		    return;
 		}
 
 		if (!hasSimpleReturnType(method)) {
 			// TODO: figure out if this ever happens?
 		    return;
 		}
 
 		int lastIndex = lastIndex(parameterTypes);
 		parameterTypes.remove(lastIndex);
 		Type lastType = method.getGenericParameterTypes()[lastIndex];
 		Type returnBound = getReturnBound(lastType);
 		backend.convertMethod(method.getName(), returnBound, parameterTypes);
 
 		return;
 	}
 
	private boolean wouldCauseClash(Method method) {
		List<Class<?>> actualParameterTypes = new ArrayList<>(asList(method.getParameterTypes()));
		actualParameterTypes.add(Handler.class);
		Class<?>[] clashingParameterTypes = actualParameterTypes.toArray(new Class<?>[0]);
		try {
			klass.getMethod(method.getName(), clashingParameterTypes);
			return true;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}

 	public Type getReturnBound(Type lastType) {
 		if (lastType instanceof ParameterizedType) {
 			ParameterizedType paramHolder = (ParameterizedType) lastType;
 			return paramHolder.getActualTypeArguments()[0];
 		} else if (lastType instanceof Class<?>) {
 			return null;
 		}
 		throw new IllegalArgumentException("Don't know what to do with: " + lastType);
 	}
 
 	private boolean hasSimpleReturnType(Method method) {
         Class<?> returnType = method.getReturnType();
         return Void.TYPE == returnType || returnType == klass;
     }
 
     boolean requiresConversion(List<Class<?>> parameterTypes) {
         return parameterTypes.contains(Handler.class);
     }
 
     boolean multiplehandlers(List<Class<?>> parameterTypes) {
         int lastIndex = lastIndex(parameterTypes);
         return lastIndex != parameterTypes.indexOf(Handler.class);
     }
 
     private int lastIndex(List<Class<?>> parameterTypes) {
         return parameterTypes.size() - 1;
     }
 
 }
