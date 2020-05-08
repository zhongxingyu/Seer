 package com.agilogy.dto;
 
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import test.com.agilogy.dto.model.Person;
import test.com.agilogy.dto.model.PersonMessage;
 
 public class DTOFactory {
 	public static <T> T createEmpty(Class<T> type) {
 		Object result = Proxy.newProxyInstance(DTOFactory.class
 				.getClassLoader(), new Class<?>[] { type },
 				new DTOInvocationHandler(type));
 		return (T) result;
 
 	}
 
 	public static <T> T createFromObject(Class<T>type, Object source) {
 		Object result = Proxy.newProxyInstance(DTOFactory.class
 				.getClassLoader(), new Class<?>[] { type },
 				new DTOInvocationHandler(type, source));
 		return (T) result;
 	}
 }
