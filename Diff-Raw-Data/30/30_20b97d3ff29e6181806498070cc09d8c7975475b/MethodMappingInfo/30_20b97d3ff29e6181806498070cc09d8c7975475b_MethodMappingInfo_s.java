 package de.age.util.duck;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 class MethodMappingInfo {
 	
 	private final Map<Method, CallTarget> mapping;
 	
 	private MethodMappingInfo(Class<?> originalClass) {
 		mapping = new HashMap<>();
 		for (Method method : originalClass.getMethods()) {
 			mapping.put(method, null);
 		}
 	}
 	
 	static MethodMappingInfo determineMapping(Class<?> originalClass, Object ... targetObjects) {
 		MethodMappingInfo info = new MethodMappingInfo(originalClass);
 		for (Method method : info.mapping.keySet()) {
 			CallTarget target = null;
 			for (Object targetObject : targetObjects) {
 				Method targetMethod = findMethod(method, targetObject);
 				if (targetMethod != null) {
 					if (target != null) {
 						throw new AmbiguousMethodException(target.getTargetMethod(), targetMethod);
 					}
 					target = new CallTarget(targetObject, targetMethod);
 				}
 			}
 			if (target == null) {
 				throw new MissingMethodException(method);
 			}
 			info.mapping.put(method, target);
 		}
 		return info;
 	}
 	
 	private static Method findMethod(Method targetMethod, Object targetObject) {
 		for (Method method : targetObject.getClass().getMethods()) {
 			if (methodsAreMatching(targetMethod, method)) {
 				return method;
 			}
 		}
 		return null;
 	}
 
 	private static boolean methodsAreMatching(Method declaredMethod, Method callerMethod) {
		Class<?>[] declaredParams = callerMethod.getParameterTypes();
		Class<?>[] callerParams = declaredMethod.getParameterTypes();
 		return callerMethod.getName().equals(declaredMethod.getName())
 				&& paramsMatching(declaredParams, callerParams)
 				&& declaredMethod.getReturnType().isAssignableFrom(
 						callerMethod.getReturnType());
 	}
 
 	private static boolean paramsMatching(Class<?>[] declaredParams, Class<?>[] callerParams) {
 		if (declaredParams.length != callerParams.length) {
 			return false;
 		}
 		for (int i = 0; i < callerParams.length; i++) {
			if (!declaredParams[i].isAssignableFrom(callerParams[i])) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	CallTarget getTarget(Method declaredMethod) {
 		return mapping.get(declaredMethod);
 	}
 
 }
