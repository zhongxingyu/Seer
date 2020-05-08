 /*
  * Created on Jan 17, 2007
  * Copyright 2006 by Patrick Moore
  */
 package org.amplafi.hivemind.factory.mock;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.amplafi.hivemind.annotations.NotService;
 import org.amplafi.hivemind.factory.ServiceTranslator;
 import org.amplafi.hivemind.factory.facade.FacadeServiceProxy;
 import org.amplafi.hivemind.util.SwitchableThreadLocal;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.logging.Log;
 import org.apache.hivemind.ApplicationRuntimeException;
 import org.apache.hivemind.InterceptorStack;
 import org.apache.hivemind.ServiceImplementationFactory;
 import org.apache.hivemind.ServiceImplementationFactoryParameters;
 import org.apache.hivemind.ServiceInterceptorFactory;
 import org.apache.hivemind.impl.ServiceImplementationFactoryParametersImpl;
 import org.apache.hivemind.internal.Module;
 import org.apache.hivemind.internal.ServicePoint;
 import org.apache.hivemind.service.impl.LoggingUtils;
 import org.apache.hivemind.util.PropertyAdaptor;
 import org.apache.hivemind.util.PropertyUtils;
 import org.easymock.classextension.EasyMock;
 import org.easymock.classextension.IMocksControl;
 
 
 /**
  * Builds mock services for testing, if the actual service does not exist.
  * @author Patrick Moore
  */
 public class MockBuilderFactoryImpl implements MockBuilderFactory {
     private ServiceImplementationFactory builderFactory;
     /**
      * Whether we have to share the same mocks across all threads or hold to the thread-separate
      * principle.
      */
     private boolean shareMocksAcrossThreads;
 
     /**
      * each thread has its own batch of mock objects.
      */
     private SwitchableThreadLocal<Map<Class<?>, Object>> mockObjectsMap;
     /**
      * handle explicitly named services
      */
     private SwitchableThreadLocal<Map<String, Object>> mockObjectsByNameMap;
 
     /**
      * these are the classes that even if they exist in the underlying
      * hivemodule should have mock objects generated for them.
      */
     private SwitchableThreadLocal<Set<Class<?>>> mockOverride;
     private SwitchableThreadLocal<Set<Class<?>>> dontMockOverride;
 
 
     public MockBuilderFactoryImpl() {
         this(false);
     }
 
     public MockBuilderFactoryImpl(boolean shareMocksAcrossThreads) {
         this.shareMocksAcrossThreads = shareMocksAcrossThreads;
 
         mockOverride = new SwitchableThreadLocal<Set<Class<?>>>(this.shareMocksAcrossThreads) {
             @Override
             protected Set<Class<?>> initialValue() {
                 return Collections.synchronizedSet(new HashSet<Class<?>>());
             }
         };
 
         dontMockOverride = new SwitchableThreadLocal<Set<Class<?>>>(this.shareMocksAcrossThreads) {
             @Override
             protected Set<Class<?>> initialValue() {
                 return Collections.synchronizedSet(new HashSet<Class<?>>());
             }
         };
 
         mockObjectsMap = new SwitchableThreadLocal<Map<Class<?>, Object>>(this.shareMocksAcrossThreads) {
             @Override
             protected Map<Class<?>, Object> initialValue() {
                 return new ConcurrentHashMap<Class<?>, Object>();
             }
         };
         mockObjectsByNameMap = new SwitchableThreadLocal<Map<String, Object>>(this.shareMocksAcrossThreads) {
             @Override
             protected Map<String, Object> initialValue() {
                 return new ConcurrentHashMap<String, Object>();
             }
         };
     }
 
     public void setShareMocksAcrossThreads(boolean shareMocksAcrossThreads) {
         this.shareMocksAcrossThreads = shareMocksAcrossThreads;
         // simply change the mode thread locals are woking in from now.
         mockOverride.setMode(shareMocksAcrossThreads);
         dontMockOverride.setMode(shareMocksAcrossThreads);
         mockObjectsMap.setMode(shareMocksAcrossThreads);
         mockObjectsByNameMap.setMode(shareMocksAcrossThreads);
     }
 
     /**
      * @param builderFactory the builderFactory to set
      */
     public void setBuilderFactory(ServiceImplementationFactory builderFactory) {
         this.builderFactory = builderFactory;
     }
     /**
      * @return the builderFactory
      */
     public ServiceImplementationFactory getBuilderFactory() {
         return builderFactory;
     }
     private IMocksControl getMockControl() {
         IMocksControl control = EasyMock.createControl();
         return control;
     }
     public Map<Class<?>, Object> getMockMap() {
         return mockObjectsMap.get();
     }
 
     /**
      * every class in the set will be mocked even if there is an existing
      * implementation.
      * @param mockOverride
      */
     public void setMockOverride(Set<Class<?>> mockOverride) {
         this.mockOverride.set(mockOverride);
     }
 
     public Set<Class<?>> getMockOverride() {
         return mockOverride.get();
     }
 
     public void setMockOverride(Class<?>...classes) {
         Set<Class<?>> override = new HashSet<Class<?>>();
         CollectionUtils.addAll(override, classes);
         setMockOverride(override);
     }
 
     public void addMockOverride(Class<?>...classes) {
         Set<Class<?>> override = getMockOverride();
         if ( override == null ) {
             setMockOverride(classes);
         } else {
             CollectionUtils.addAll(override, classes);
         }
     }
 
     /**
      * every class not in the set will be mocked.
      * @param dontMockOverride
      */
     public void setDontMockOverride(Set<Class<?>> dontMockOverride) {
         this.dontMockOverride.set(dontMockOverride);
     }
 
     /**
      * every class not in the set will be mocked.
      * @return set of classes that are NOT mocked.
      */
     public Set<Class<?>> getDontMockOverride() {
         return dontMockOverride.get();
     }
 
     public void setDontMockOverride(Class<?>...classes) {
         Set<Class<?>> dontOverride = new HashSet<Class<?>>();
         CollectionUtils.addAll(dontOverride, classes);
         setDontMockOverride(dontOverride);
     }
 
     public void addDontMockOverride(Class<?>...classes) {
         Set<Class<?>> dontOverride = getDontMockOverride();
         if ( dontOverride == null ) {
             setDontMockOverride(classes);
         } else {
             CollectionUtils.addAll(dontOverride, classes);
         }
     }
     /**
      * replay the EasyMock at the serviceInterface.
      * @param serviceInterfaces
      */
     public void replay(Class<?>... serviceInterfaces) {
         for(Class<?>serviceInterface : serviceInterfaces) {
             Object mock = getMockMap().get(serviceInterface);
             if ( mock != null ) {
                 EasyMock.replay(mock);
             }
         }
     }
     public void replay() {
         Collection<Object> mocks = getMocks();
         for(Object mock : mocks) {
             if ( mock != null ) {
                 try {
                     EasyMock.replay(mock);
                 } catch( IllegalStateException e) {
                     // should be way to detect in replay mode already
                 }
             }
         }
     }
 
     /**
      * verify the EasyMock at the serviceInterface.
      * @param serviceInterfaces
      */
     public void verify(Class<?>... serviceInterfaces) {
         for(Class<?>serviceInterface : serviceInterfaces) {
             Object mock = getMockMap().get(serviceInterface);
             if ( mock != null ) {
                 EasyMock.verify(mock);
             }
         }
     }
 
     public void verify() {
         Collection<Object> mocks = getMocks();
         for(Object mock : mocks) {
             if ( mock != null ) {
                 try {
                     EasyMock.verify(mock);
                 } catch(IllegalStateException e) {
                     // happens when do a verify of a object that was
                     // created on the fly.
                 }
             }
         }
     }
 
     /**
      * @return
      */
     private Collection<Object> getMocks() {
         ArrayList<Object> mocks = new ArrayList<Object>();
         mocks.addAll(getMockMap().values());
         mocks.addAll(this.mockObjectsByNameMap.get().values());
         return mocks;
     }
     /**
      * replay the EasyMock at the serviceInterface.
      * @param serviceInterfaces
      */
     public void reset(Class<?>... serviceInterfaces) {
         for(Class<?>serviceInterface : serviceInterfaces) {
             Object mock = getMockMap().get(serviceInterface);
             if ( mock != null ) {
                 EasyMock.reset(mock);
             }
         }
     }
 
     /**
      * reset all mocks and clear the dontMockOverride and the
      * mockOverride sets for this thread.
      *
      */
     public void reset() {
         Collection<Object> mocks = getMocks();
         for(Object mock : mocks) {
             if ( mock != null ) {
                 EasyMock.reset(mock);
             }
         }
 
         dontMockOverride.get().clear();
         mockOverride.get().clear();
     }
     @SuppressWarnings("unchecked")
     public Object createCoreServiceImplementation(
             ServiceImplementationFactoryParameters factoryParameters) {
         Class interfaceClass = factoryParameters.getServiceInterface();
         Object createdObject;
         if ( factoryParameters.getFirstParameter() == null ) {
             // no construction information so create a mock
             createdObject = getThreadsMock(interfaceClass);
         } else {
             createdObject = createCoreServiceImplementation(builderFactory, factoryParameters);
         }
         return createdObject;
     }
 
     /**
      * Used for classes that where not created by hivemind, but we still want to have all
      * service class objects accessed by this object be mocks.
      *
      * @see #getDontMockOverride()
      * @see #getMockOverride()
      * @param objectToMockWrap
      */
     @SuppressWarnings("unchecked")
     public void wrapWithMocks(Object objectToMockWrap) {
         List<String> writableProperties = PropertyUtils.getWriteableProperties(objectToMockWrap);
         for(String prop : writableProperties) {
             PropertyAdaptor adaptor = PropertyUtils.getPropertyAdaptor(objectToMockWrap, prop);
             Class<?> interfaceClass = adaptor.getPropertyType();
             if (adaptor.isReadable() && isMockable(interfaceClass)) {
                 adaptor.write(objectToMockWrap, getServiceToUse(interfaceClass,
                         adaptor.read(objectToMockWrap), true));
             }
         }
     }
 
     /**
      * <ol>
      * <li>If the service is in the explicit
      * {@link #mockOverride} set, then mock.</li>
      * <li>If the service is in the explicit
      * {@link #dontMockOverride} set, then don't mock (unless on explicit Mock set).
      * </li><li>
      * if {@link #dontMockOverride} has values and this interfaceClass is not in that
      * set then mock.
      * </li><li>if real service is null and mockByDefault is true then mock.
      * </li>
      * </ol>
      * @param <T>
      * @param interfaceClass
      * @param realService
      * @param mockByDefault
      * @return either a mock or the realService.
      */
     <T> T getServiceToUse(Class<? extends T> interfaceClass, T realService, boolean mockByDefault) {
         T underlyingObject;
         if ( mockOverride.get().contains(interfaceClass) ) {
             underlyingObject = getThreadsMock(interfaceClass);
         } else if ( dontMockOverride.get().contains(interfaceClass)) {
             underlyingObject = realService;
         } else if ( !dontMockOverride.get().isEmpty()){
             // if dontMockOverride is not empty then everything else
             // must be mocked.
             underlyingObject = getThreadsMock(interfaceClass);
         } else if ( realService == null && mockByDefault){
             underlyingObject = getThreadsMock(interfaceClass);
         } else {
             // otherwise whatever the real object is ( which may be a mock ).
             underlyingObject = realService;
         }
         return underlyingObject;
     }
 
     /**
      * Method that calls the underlying applicationBuilderFactory
      * to create the real service.
      * @param applicationBuilderFactory
      * @param factoryParameters
      * @return real service
      */
     Object createCoreServiceImplementation(
             ServiceImplementationFactory applicationBuilderFactory,
             ServiceImplementationFactoryParameters factoryParameters) {
         Object createdObject;
         // wrap the parameters so that ModuleInterceptor
         // can always do it's thing.
         Module realModule = factoryParameters.getInvokingModule();
         ClassLoader loader = realModule.getClassResolver().getClassLoader();
         Module moduleWrapper = (Module) Proxy.newProxyInstance(loader,
                 new Class[] {Module.class},
                 new ModuleInterceptor(realModule));
         String serviceId = factoryParameters.getServiceId();
         ServicePoint servicePoint = realModule.getServicePoint(serviceId);
         ServiceImplementationFactoryParametersImpl replacement =
             new ServiceImplementationFactoryParametersImpl(servicePoint,
                     moduleWrapper, factoryParameters.getParameters());
         createdObject = applicationBuilderFactory.createCoreServiceImplementation(replacement);
         return createdObject;
     }
     /**
      * In interceptor factory mode, only knows how to create an interceptor for
      * BuilderFactory.
      * @see org.apache.hivemind.ServiceInterceptorFactory#createInterceptor(org.apache.hivemind.InterceptorStack, org.apache.hivemind.internal.Module, java.util.List)
      */
     @SuppressWarnings({ "unused", "unchecked" })
     public void createInterceptor(InterceptorStack stack, Module invokingModule, List parameters) {
         Log log = stack.getServiceLog();
 
         ServiceImplementationFactory delegate = (ServiceImplementationFactory) stack.peek();
         InvocationHandler handler = new ServiceImplementationFactoryInterceptor(log, delegate);
 
         Object interceptor =
             Proxy.newProxyInstance(
                 invokingModule.getClassResolver().getClassLoader(),
                 new Class[] { stack.getServiceInterface()},
                 handler);
 
         stack.push(interceptor);
     }
 
     /**
      * @param interfaceClass
      * @return mock object implementing interfaceClass.
      */
     @SuppressWarnings("unchecked")
     protected <T> T getThreadsMock(Class<T> interfaceClass) {
         T mock = (T) getMockMap().get(interfaceClass);
         if ( mock != null ) {
             return mock;
         }
         IMocksControl mockControl = getMockControl();
         try {
             mock = mockControl.createMock(interfaceClass);
         } catch (IllegalStateException e) {
             throw new IllegalStateException("Need to program mock first for interface "+interfaceClass, e);
         }
         getMockMap().put(interfaceClass, mock);
         return mock;
     }
     @SuppressWarnings("unchecked")
     protected <T> T getThreadsMockByName(String serviceId, Class<T> interfaceClass) {
         T mock = (T) this.mockObjectsByNameMap.get().get(serviceId);
         if ( mock != null ) {
             return mock;
         }
         IMocksControl mockControl = getMockControl();
         try {
             mock = mockControl.createMock(interfaceClass);
         } catch (IllegalStateException e) {
             throw new IllegalStateException("Need to program mock first for interface "+interfaceClass, e);
         }
         this.mockObjectsByNameMap.get().put(serviceId, mock);
         return mock;
     }
     /**
      * used to get the mock objects so they can be programmed.
      * @param <T>
      * @param implementationClass
      * @return the implementation instance (usually a mock object)
      */
     public <T> T getImplementation(Class<T> implementationClass) {
         T mock = getThreadsMock(implementationClass);
         return mock;
     }
     /**
      * Determines if it is possible for this interface to be mocked.
      *
      * @param serviceInterface
      * @return false if the class is a primitive, an array, a java standard class,
      * or is labeled with the {@link NotService} annotation.
      */
     private boolean isMockable(Class<?> serviceInterface) {
         if ( !serviceInterface.isAnnotationPresent(NotService.class)) {
             return serviceInterface.getCanonicalName() != null
                 && !serviceInterface.getCanonicalName().startsWith("java")
                 && !serviceInterface.isPrimitive()
                 && !serviceInterface.isArray()
                 && !serviceInterface.isEnum()
                 && !serviceInterface.isAnnotation();
         } else {
             return false;
         }
     }
     /**
      * Because of MockSwitcher the object that external tests have is not actually a mock in some cases.
      * @param mock an EasyMock or a Proxy with a MockSwitcher as the Proxy handler.
      * @return actual mock object
      */
     @SuppressWarnings("unchecked")
     public Object getMock(final Object mock) {
         try {
             // check to see if this is a hivemind proxy.
             @SuppressWarnings("unused")
             final Field f = mock.getClass().getDeclaredField("_inner");
             // TODO note it would be best to get the value of _inner
             List<Class<?>> interfaces = ClassUtils.getAllInterfaces(mock.getClass());
             Map<Class<?>, Object> mockMap = getMockMap();
             for(Class interfaceClass: interfaces) {
                 Object threadMock = mockMap.get(interfaceClass);
                 if ( threadMock != null ) {
                     return threadMock;
                 }
             }
         } catch (NoSuchFieldException e) {
             // ignore -- mock was not a hivemind proxy
         }
         return mock;
     }
 
     /**
      *
      * @param serviceClass
      * @return if the interface is being mocked.
      */
     public boolean isBeingMocked(Class<?> serviceClass) {
         return mockOverride.get().contains(serviceClass);
     }
 
     /**
      * this intercepts calls to another {@link ServiceImplementationFactory}
      * so that it is guaranteed that an object with the requested service interface is created.
      *
      *  Instances of this object are created when the {@link MockBuilderFactoryImpl}
      *  is invoked as a {@link ServiceInterceptorFactory}.
      */
     private class ServiceImplementationFactoryInterceptor
         implements InvocationHandler {
         private ServiceImplementationFactory delegate;
         private Log log;
 
         public ServiceImplementationFactoryInterceptor(Log log,
                 ServiceImplementationFactory delegate) {
             this.log = log;
             this.delegate = delegate;
         }
 
         @SuppressWarnings("unused")
         public Object invoke(Object proxy, Method method, Object[] args)
                 throws Throwable {
             boolean debug = log.isDebugEnabled();
 
             if (debug) {
                 LoggingUtils.entry(log, method.getName(), args);
             }
             try {
                 Object result;
                 if ("createCoreServiceImplementation".equals(method.getName())
                         && args.length == 1) {
                     ServiceImplementationFactoryParameters p = (ServiceImplementationFactoryParameters) args[0];
                     InvocationHandler handler = new MockSwitcher(delegate, p);
                     result =
                         Proxy.newProxyInstance(
                                 p.getServiceInterface().getClassLoader(),
                                 new Class[] { p.getServiceInterface()},
                                 handler);
                 } else {
                     result = method.invoke(delegate, args);
                 }
                 if (debug) {
                     if (method.getReturnType() == void.class) {
                         LoggingUtils.voidExit(log, method.getName());
                     } else {
                         LoggingUtils.exit(log, method.getName(), result);
                     }
                 }
 
                 return result;
             } catch (InvocationTargetException ex) {
                 Throwable targetException = ex.getTargetException();
 
                 if (debug) {
                     LoggingUtils.exception(log, method.getName(),
                             targetException);
                 }
                 throw targetException;
             }
         }
     }
     /**
      * intercept calls to getService() and containsService so that if the
      * wrapped Module doesn't have the requested service a mock is created.
      */
     private class ModuleInterceptor implements InvocationHandler {
 
         private Module realModule;
 
         public ModuleInterceptor(Module realModule) {
             this.realModule = realModule;
         }
 
         @SuppressWarnings({ "unused", "unchecked" })
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             Class serviceInterface;
             if ( "containsService".equals(method.getName())) {
                 // this module always has the service if the class in question is
                 // not a java class.
                 serviceInterface = (Class) args[0];
                 return isMockable(serviceInterface);
             } else if ( "getService".equals(method.getName())) {
                 Object createdObject = null;
                 switch( args.length ) {
                 case 1:
                     serviceInterface = (Class) args[0];
                     if ( realModule.containsService(serviceInterface) ) {
                         createdObject = realModule.getService(serviceInterface);
                     } else if ( isMockable(serviceInterface)) {
                         // no such service supply a proxy (unless it is a java class)
                         createdObject = getThreadsMock(serviceInterface);
                     } else {
                         createdObject = null;
                     }
                     break;
                 case 2:
                     serviceInterface = (Class) args[1];
                     String serviceId = (String) args[0];
                     // Do this look because the class may be specified too specifically. Usually this will result in an error
                     // when actually trying to set the property. But the classcast exception that occurs at that point is more informative.
                     for(Class<?> clazz : new Class<?>[] { serviceInterface, Object.class }) {
                         try {
                             createdObject = realModule.getService(serviceId, clazz);
                             break;
                         } catch(ApplicationRuntimeException e) {
                         }
                     }
                     if ( createdObject == null) {
                         createdObject = getThreadsMockByName(serviceId, serviceInterface);
                     }
                     break;
                 default:
                     createdObject = method.invoke(realModule, args);
                 }
                 return createdObject;
             } else if ( "getTranslator".equals(method.getName())){
                 String translator = (String) args[0];
                 if ( "service".equals(translator)) {
                     return ServiceTranslator.INSTANCE;
                 } else {
                     return realModule.getTranslator(translator);
                 }
             } else {
                 return method.invoke(realModule, args);
             }
         }
     }
     /**
      * This proxy intercepts all calls to a service. This allows
      * mock objects to mask existing objects.
      */
     public class MockSwitcher extends FacadeServiceProxy {
         /**
          * this may in fact be a Mock if the underlying delegate factory
          * does not have a defined object.
          */
         private final Class<?> interfaceClass;
         private ServiceImplementationFactoryParameters factoryParameters;
         private ServiceImplementationFactory delegate;
         MockSwitcher(ServiceImplementationFactory delegate, ServiceImplementationFactoryParameters factoryParameters) {
             super();
             this.delegate = delegate;
             this.factoryParameters = factoryParameters;
             interfaceClass = factoryParameters.getServiceInterface();
         }
 
         /**
          * visible so it can be tested.
          * @return the real service
          */
         @Override
         public Object getUnderlyingService() {
             Object underlyingObject;
             underlyingObject = getServiceToUse(interfaceClass, getRealService(), false);
             return underlyingObject;
         }
 
         public Object getRealService() {
             return super.getUnderlyingService();
         }
         /**
          * @return the interfaceClass
          */
         public Class<?> getInterfaceClass() {
             return interfaceClass;
         }
         @Override
         protected Object createUnderlyingService() {
             try {
                 return createCoreServiceImplementation(delegate, factoryParameters);
             } catch (Exception e) {
                 return null;
             }
         }
     }
 }
