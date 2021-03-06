 /*
  * Copyright (c) 2007 Mockito contributors
  * This program is made available under the terms of the MIT License.
  */
 package org.mockito.internal.creation;
 
 import java.lang.reflect.Modifier;
 import java.util.List;
 
 import net.sf.cglib.core.CollectionUtils;
 import net.sf.cglib.core.VisibilityPredicate;
 import net.sf.cglib.proxy.Callback;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.Factory;
 
 import org.mockito.exceptions.Reporter;
 
 /**
  * Factory generating a mock for a class.
  */
 public class MockFactory<T> {
 
     @SuppressWarnings("unchecked")
     public T createMock(Class<T> toMock, final MethodInterceptorFilter filter) {
         validateClass(toMock);
         Enhancer enhancer = createEnhancer(toMock);
         enhancer.setCallbackType(filter.getClass());
 
         Class mockClass = enhancer.createClass();
         Enhancer.registerCallbacks(mockClass, new Callback[] { filter });
 
         Factory mock = createMock(mockClass);
 
         filter.setMock(mock);
         return (T) mock;
     }
 
     private void validateClass(Class<T> toMock) {
         if (Modifier.isFinal(toMock.getModifiers())) {
             new Reporter().cannotMockFinalClass(toMock);
         }
     }
 
     private Enhancer createEnhancer(Class<T> toMock) {
         Enhancer enhancer = new Enhancer() {
             @SuppressWarnings("unchecked")
             //Filter all private constructors but do not check that there are some left
             protected void filterConstructors(Class sc, List constructors) {
                 CollectionUtils.filter(constructors, new VisibilityPredicate(
                         sc, true));
             }
         };
 
         if (toMock.isInterface()) {
             enhancer.setInterfaces(new Class[] { toMock });
         } else {
             enhancer.setSuperclass(toMock);
         }
         return enhancer;
     }
 
     private Factory createMock(Class<?> mockClass) {
         Factory mock;
         try {
             mock = (Factory) ObjenesisClassInstantiator.newInstance(mockClass);
         } catch (InstantiationException e) {
             throw new RuntimeException("Fail to instantiate mock for " + mockClass
                     + " on " + System.getProperty("java.vm.vendor") + " JVM");
         }
 
         // This call is required. Cglib has some "magic code" making sure a
         // callback is used by only one instance of a given class. So only the
         // instance created right after registering the callback will get it.
         // However, this is done in the construtor which I'm bypassing to
         // allow class instantiation without calling a constructor.
         // Fortunatly, the "magic code" is also called in getCallback which is
         // why I'm calling it here mock.getCallback(0);
 
         mock.getCallback(0);
         return mock;
     }
 }
