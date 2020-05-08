 package com.agilogy.dto;
 
 import java.lang.reflect.Proxy;
 
 public class DTOFactory {
	@SuppressWarnings("unchecked")
 	public static <T> T createEmpty(Class<T> type) {
 		Object result = Proxy.newProxyInstance(DTOFactory.class
 				.getClassLoader(), new Class<?>[] { type },
 				new DTOInvocationHandler(type));
 		return (T) result;
 
 	}
 
	@SuppressWarnings("unchecked")
 	public static <T> T createFromObject(Class<T>type, Object source) {
 		Object result = Proxy.newProxyInstance(DTOFactory.class
 				.getClassLoader(), new Class<?>[] { type },
 				new DTOInvocationHandler(type, source));
 		return (T) result;
 	}
 }
