 // Copyright 2011 Sociodyne LLC. All rights reserved.
 
 package com.sociodyne.test;
 
 import static org.easymock.EasyMock.verify;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 import org.easymock.IMockBuilder;
 import org.easymock.IMocksControl;
 
 import junit.framework.TestCase;
 
 public class MockTest extends TestCase {
 	private List<Object> objectsToVerify = new ArrayList<Object>();
 
 	@Override
 	public void setUp() throws Exception {
		Class<?> clazz = this.getClass();
 		while (clazz != null && ! MockTest.class.equals(clazz)) {
 			for (Field field : clazz.getDeclaredFields()) {
 				Mock mockAnnotation = field.getAnnotation(Mock.class);
 				if (mockAnnotation != null) {
 					Object mock;
 					if (mockAnnotation.value() != null) {
 						switch(mockAnnotation.value()) {
 							case STRICT:
 								mock = EasyMock.createStrictMock(field.getType());
 								break;
 							case NICE:
 								mock = EasyMock.createNiceMock(field.getType());
 								break;
 							case DEFAULT:
 							default:
 								mock = EasyMock.createMock(field.getType());
 						}
 					} else {
 						mock = EasyMock.createMock(field.getType());
 					}
 	
 					field.setAccessible(true);
 					field.set(this, mock);
 					objectsToVerify.add(mock);
 				}
 			}
 
 			clazz = clazz.getSuperclass();
 		}
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		for (Object o : objectsToVerify) {
 			try {
 				verify(o);
 			} catch (Error e) {
 				throw new Error("Verification of " + o + " failed", e);
 			}
 		}
 	}
 
 	protected <T> T createMock(Class<T> clazz) {
 		T object = EasyMock.createMock(clazz);
 		objectsToVerify.add(object);
 		return object;
 	}
 
 	protected <T> T createStrictMock(Class<T> clazz) {
 		T object = EasyMock.createStrictMock(clazz);
 		objectsToVerify.add(object);
 		return object;
 	}
 
 	protected <T> T createNiceMock(Class<T> clazz) {
 		T object = EasyMock.createNiceMock(clazz);
 		objectsToVerify.add(object);
 		return object;
 	}
 
 	protected <T> IMockBuilder<T> createMockBuilder(Class<T> clazz) {
 		return new DelegatingIMockBuilder<T>(EasyMock.createMockBuilder(clazz));
 	}
 
 	protected void replay() {
 		for (Object object : objectsToVerify) {
 			EasyMock.replay(object);
 		}
 	}
 
 	private class DelegatingIMockBuilder<T> implements IMockBuilder<T> {
 		IMockBuilder<T> delegate;
 
 		public DelegatingIMockBuilder(IMockBuilder<T> delegate) {
 			this.delegate = delegate;
 		}
 
 		public IMockBuilder<T> addMockedMethod(Method arg0) {
 			return delegate.addMockedMethod(arg0);
 		}
 
 		public IMockBuilder<T> addMockedMethod(String arg0) {
 			return delegate.addMockedMethod(arg0);
 		}
 
 		public IMockBuilder<T> addMockedMethod(String arg0, Class<?>... arg1) {
 			return delegate.addMockedMethod(arg0, arg1);
 		}
 
 		public IMockBuilder<T> addMockedMethods(String... arg0) {
 			return delegate.addMockedMethods(arg0);
 		}
 
 		public IMockBuilder<T> addMockedMethods(Method... arg0) {
 			return delegate.addMockedMethods(arg0);
 		}
 
 		public T createMock() {
 			T mock = delegate.createMock();
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createMock(IMocksControl arg0) {
 			T mock = delegate.createMock(arg0);
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createMock(String arg0) {
 			T mock = delegate.createMock(arg0);
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createMock(String arg0, IMocksControl arg1) {
 			T mock = delegate.createMock(arg0, arg1);
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createNiceMock() {
 			T mock = delegate.createNiceMock();
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createNiceMock(String arg0) {
 			T mock = delegate.createNiceMock(arg0);
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createStrictMock() {
 			T mock = delegate.createStrictMock();
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public T createStrictMock(String arg0) {
 			T mock = delegate.createStrictMock(arg0);
 			objectsToVerify.add(mock);
 			return mock;
 		}
 
 		public IMockBuilder<T> withArgs(Object... arg0) {
 			return delegate.withArgs(arg0);
 		}
 
 		public IMockBuilder<T> withConstructor() {
 			return delegate.withConstructor();
 		}
 
 		public IMockBuilder<T> withConstructor(Constructor<?> arg0) {
 			return delegate.withConstructor(arg0);
 		}
 
 		public IMockBuilder<T> withConstructor(Object... arg0) {
 			return delegate.withConstructor(arg0);
 		}
 
 		public IMockBuilder<T> withConstructor(Class<?>... arg0) {
 			return delegate.withConstructor(arg0);
 		}
 	}
 }
