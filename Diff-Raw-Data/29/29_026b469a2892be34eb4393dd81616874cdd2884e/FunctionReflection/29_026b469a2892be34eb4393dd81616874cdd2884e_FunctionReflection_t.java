 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class FunctionReflection {
 	Map<String, Object> objectMap;
 	Map<String, Method> methodMap;
 	List<String> methodList;
 
 	FunctionReflection() {
 		objectMap = new HashMap<String, Object>();
 		methodMap = new HashMap<String, Method>();
 		methodList = new ArrayList<String>();
 	}
 
 	public Object methodInvoke(Object targetObject, Method executeMethod,
 			Object... args) throws Throwable {
		Throwable failureReason = null;
		Object resultObject = null;
 		try {
 			executeMethod.setAccessible(true);
			resultObject = executeMethod.invoke(targetObject, (Object[]) args);
 		} catch (InvocationTargetException e) {
 			failureReason = e.getCause();
 		} catch (IllegalArgumentException e) {
			failureReason = e;
 		} catch (IllegalAccessException e) {
			failureReason = e;
 		}
		if (failureReason != null) {
			throw failureReason;
		}
		return resultObject;
 
 	}
 
 	private String splitMethodName(Method method) {
 		String[] methodNames = method.getName().split(".");
 		return methodNames[methodNames.length - 1];
 	}
 
 	public void setMethodMap(Method[] methods) {
 		methodMap.clear();
 		methodList.clear();
 		for (Method method : methods) {
 			method.setAccessible(true);
 			String methodName = splitMethodName(method);
 			if (!methodMap.containsKey(methodName)) {
 				methodMap.put(methodName, method);
 				methodList.add(methodName);
 			}
 		}
 		Collections.sort(methodList);
 	}
 
 	public Method getMethodMap(String methodName) {
 		Method resultMethod = methodMap.get(methodName);
 		if (resultMethod == null) {
 			throw new NullPointerException();
 		}
 		return resultMethod;
 	}
 
 	public List<String> getFieldList(Object targeObject) {
 		List<String> resultList = new ArrayList<String>();
 		Field[] fields = targeObject.getClass().getDeclaredFields();
 		for (Field field : fields) {
 			field.setAccessible(true);
 			resultList.add(field.getName());
 		}
 		return resultList;
 	}
 
	public void setField(Object targetObject, String fieldName, Object value)
 			throws Throwable {
 		Throwable failureReason = null;
 		try {
 			Field targetField = targetObject.getClass().getDeclaredField(
 					fieldName);
 			targetField.setAccessible(true);
			//Object inputValue = changeType(targetField.getClass(), value);
			targetField.set(targetObject, value);
 
 		} catch (SecurityException e) {
 			failureReason = e.getCause();
 		} catch (NoSuchFieldException e) {
 			failureReason = e.getCause();
 		}
 		if (failureReason != null) {
 			throw failureReason;
 		}
 
 	}
 
 	public Object getField(Object targetObject, String fieldName)
 			throws Throwable {
 		Throwable failureReason = null;
 		try {
 			Field targetField = targetObject.getClass().getDeclaredField(
 					fieldName);
 			targetField.setAccessible(true);
 			return targetField.get(targetObject);
 		} catch (SecurityException e) {
 			failureReason = e.getCause();
 		} catch (NoSuchFieldException e) {
 			failureReason = e.getCause();
 		}
 		throw failureReason;
 
 	}
 
 	public Object changeType(Type objectType, String value)
 			throws NullPointerException {
 		Object resultObject = null;
 		if (int.class.equals(objectType)) {
 			resultObject = new Integer(value);
 		} else if (double.class.equals(objectType)) {
 			resultObject = new Double(value);
 		} else if (float.class.equals(objectType)) {
 			resultObject = new Float(value);
 		} else if (long.class.equals(objectType)) {
 			resultObject = new Long(value);
 		} else if (byte.class.equals(objectType)) {
 			resultObject = new Byte(value);
 		} else if (short.class.equals(objectType)) {
 			resultObject = new Short(value);
 		} else if (char.class.equals(objectType)) {
 			resultObject = new Character(value.charAt(0));
 		} else if (boolean.class.equals(objectType)) {
 			resultObject = new Boolean(value);
 		} else if (String.class.equals(objectType)) {
 			resultObject = new String(value);
 		} else {
 			resultObject = objectMap.get(value);
 			if (resultObject == null) {
 				throw new NullPointerException();
 			}
 		}
 		return resultObject;
 	}

 }
