 package com.agilogy.dto;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 import java.util.Map;
 
 public class DTOInvocationHandler implements java.lang.reflect.InvocationHandler {
 	final String NULL = "null";
 
 	Map<String, Object> values;
 	//key=method name, value = property name
 	Map<String, String> getters;
 	Map<String, String> setters;
 	Map<String, String> checkers;
 	
 	Class<?> type;
 
 	DTOInvocationHandler(Class<?> type) {
 		this(type, null);
 	}
 
 	public DTOInvocationHandler(Class<?> type, Object source) {
 		super();
 		this.type = type;
 		this.values = new HashMap<String, Object>();
 		this.getters = new HashMap<String, String>();
 		this.setters = new HashMap<String, String>();
 		this.checkers = new HashMap<String, String>();
 		Method[] methods = type.getMethods();
 		for (Method method: methods) {
 			if (isGetter(method)) {
 				getters.put(method.getName(), getPropertyName(method));
 			} else if (isChecker(method)) {
 				checkers.put(method.getName(), getPropertyName(method));
 			} else if (isSetter(method)) {
 				setters.put(method.getName(), getPropertyName(method));
 				//If we have a source object, let's set the property value from the object
 				if (source != null) {
 					//Little hack: As I'm not using propertyName but PropertyName I can 
 					//construct the getter name by prepending get (would not work with booleans)
 					String propertyName = getPropertyName(method);
 					try {
 						Method getter = source.getClass().getMethod("get" + propertyName);
 						Object value = getter.invoke(source);
 						setProperty(propertyName, value);
 					} catch (SecurityException e) {
 						throw new RuntimeException("Error finding getter. Is it not public?", e);
 					} catch (NoSuchMethodException e) {
 						//We can't initialize this property from the object, no problem
 					} catch (Exception e) {
 						throw new RuntimeException("Error invoking getter for property " + propertyName + " on object " + source, e);
 					}
 				}
 			}
 		}
 		
 	}
 
 	private void setProperty(String propertyName, Object value) {
 		if (value == null) {
 			value = NULL;
 		}
 		values.put(propertyName, value);
 	}
 
 	private Object getProperty(String propertyName) {
 		Object result = values.get(propertyName);
 		if (NULL == result) {
 			result = null;
 		}
 		return result;
 	}	
 	
 	private boolean checkProperty(String propertyName) {
 		return values.get(propertyName) != null;
 	}
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args)
 			throws Throwable {
 		String methodName = method.getName();
 		if (setters.containsKey(methodName)) {
 			setProperty(setters.get(methodName), args[0]);
 		} else if (getters.containsKey(methodName)) {
 			return getProperty(getters.get(methodName));
 		} else if (checkers.containsKey(methodName)) {
 			return checkProperty(checkers.get(methodName));
 		} else if (isEquals(method)) {
 			if (!(args[0] instanceof Proxy)) {
 				return false;
 			}
 			InvocationHandler theOtherInvocationHandler = Proxy
 					.getInvocationHandler(args[0]);
 			if (!(theOtherInvocationHandler instanceof DTOInvocationHandler)) {
 				return false;
 			}
 			DTOInvocationHandler odiv  = (DTOInvocationHandler) theOtherInvocationHandler;
 			return odiv.type.isAssignableFrom(this.type) && odiv.values.equals(this.values);
 		} else if (isHashCode(method)) {
 			return values.hashCode();
 		}
 		//Raise an exception ?
 		return null;
 	}
 
 	private boolean isHashCode(Method method) {
 		return method.getName().equals("hashCode")
 			&& method.getParameterTypes().length == 0;
 	}
 
 	private boolean isEquals(Method method) {
 		return method.getName().equals("equals")
 				&& method.getParameterTypes().length == 1;
 	}
 
 	private String getPropertyName(Method method) {
 		String propName = method.getName().substring(3);
 		return propName;
 	}
 
 	private boolean isChecker(Method method) {
 		return method.getName().startsWith("has")
 				&& method.getParameterTypes().length == 0
 				&& !method.getName().equals("hashCode");
 	}
 
 	private boolean isGetter(Method method) {
 		return method.getName().startsWith("get")
 				&& method.getParameterTypes().length == 0;
 	}
 
 	private boolean isSetter(Method method) {
 		return method.getName().startsWith("set")
 				&& method.getParameterTypes().length == 1;
 	}
 }
