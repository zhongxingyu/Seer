 /*
  *  Copyright 2009-2010 Mathieu ANCELIN
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.dependencyshot.injection;
 
 import cx.ath.mancel01.dependencyshot.util.ShutdownThread;
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import cx.ath.mancel01.dependencyshot.api.InjectionPoint;
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.api.event.Event;
 import cx.ath.mancel01.dependencyshot.api.event.EventListener;
 import cx.ath.mancel01.dependencyshot.api.event.EventManager;
 import cx.ath.mancel01.dependencyshot.event.EventImpl;
 import cx.ath.mancel01.dependencyshot.event.EventManagerImpl;
 import cx.ath.mancel01.dependencyshot.exceptions.DSCyclicDependencyDetectedException;
 import cx.ath.mancel01.dependencyshot.exceptions.DSException;
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.dependencyshot.graph.BinderAccessor;
 import cx.ath.mancel01.dependencyshot.graph.Binding;
 import cx.ath.mancel01.dependencyshot.injection.handlers.ClassHandler;
 import cx.ath.mancel01.dependencyshot.injection.handlers.ConstructorHandler;
 import cx.ath.mancel01.dependencyshot.injection.util.InstanceProvider;
 import cx.ath.mancel01.dependencyshot.scope.simple.SimpleScope;
 import cx.ath.mancel01.dependencyshot.spi.CustomScopeHandler;
 import cx.ath.mancel01.dependencyshot.spi.InstanceLifecycleHandler;
 import cx.ath.mancel01.dependencyshot.spi.PluginsLoader;
 import cx.ath.mancel01.dependencyshot.util.CircularProxy;
 import cx.ath.mancel01.dependencyshot.util.ReflectionUtil;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.inject.Provider;
 import javax.inject.Qualifier;
 
 /**
  * This class represent an injector configured by coded
  * binders.
  * 
  * @author Mathieu ANCELIN
  */
 public class InjectorImpl implements DSInjector {
 
    private boolean allowCircularDependencies = false;
     private static final Logger logger = Logger.getLogger(InjectorImpl.class.getSimpleName());
     /**
      * Binders linked to the project.
      */
     private List<Binder> binders;
     /**
      * Bindings of the injector.
      */
     private Map<Binding<?>, Binding<?>> bindings = null;
     /**
      * Singleton scoped object singletonContext.
      */
     private Map<Class<?>, Object> singletonContext;
     /**
      * Marked classes for circular dependencies checking.
      * Will be used for circular deps. issues resolving.
      */
     private Map<Class<?>, Object> instanciatedClasses;
     /**
      * Stage of the injector.
      */
     private Stage stage = Stage.NONE;
     private boolean bindingsChanged = false;
     private EventManagerImpl eventManager;
     private ClassHandler classHandler;
     private ConstructorHandler constructorHandler;
     private PluginsLoader loader;
     private Class actualFromClass;
     private List<Class> circularClasses;
     private Map<Class<?>, Object> circularConstructorArgumentsInstances;
     private Map<Class<? extends Annotation>, CustomScopeHandler> scopeHandlers;
     private List<Class<?>> staticInjection;
 
     /**
      * The constructor.
      */
     public InjectorImpl(PluginsLoader loader) {
         initialize(loader, Stage.NONE);
     }
 
     /**
      * The constructor.
      *
      * @param stage of the injector.
      */
     public InjectorImpl(PluginsLoader loader, Stage stage) {
         initialize(loader, stage);
     }
 
     private void initialize(PluginsLoader loader, Stage stage) {
         binders = new ArrayList();
         singletonContext = new HashMap<Class<?>, Object>();
         instanciatedClasses = new HashMap<Class<?>, Object>();
         circularConstructorArgumentsInstances = new HashMap<Class<?>, Object>();
         circularClasses = new ArrayList<Class>();
         this.stage = stage;
         classHandler = new ClassHandler();
         constructorHandler = new ConstructorHandler();
         this.loader = loader;
         loader.loadPlugins(this);
         scopeHandlers = loader.getScopeHandlers();
         eventManager = new EventManagerImpl();
         eventManager.registerListeners(loader.getEventListeners());
     }
 
     /**
      * Configure all present binders of the injector.
      */
     public final void configureBinders() {
         if (binders.size() > 0) {
             for (Binder binder : binders) {
                 binder.configureBindings();
                 //binder.configureLastBinding();
                 BinderAccessor.configureLastBinding(binder);
                 if (binder.isEmpty()) {
                     Logger.getLogger(InjectorImpl.class.getName()).
                             log(Level.SEVERE, "Ooops, no bindings presents, "
                             + "can't inject your app ...");
                     throw new DSException("No bindings loaded");
                 }
             }
         }
     }
 
     /**
      * @return if there are new bindings in the binder.
      */
     public final boolean getBindingsChanged() {
         return bindingsChanged;
     }
 
     /**
      * Set if there are new bindings in the binder.
      * 
      * @param bindingsChanged new value.
      */
     public final void setBindingsChanged(boolean bindingsChanged) {
         this.bindingsChanged = bindingsChanged;
     }
 
     /**
      * Add a binder in the injector.
      *
      * @param binder the binder to add.
      */
     public final void addBinder(final Binder binder) {
         //binder.setInjector(this);
         BinderAccessor.setInjector(binder, this);
         binders.add((Binder) binder);
     }
 
     /**
      * Get the defined bindings in all binders of the injector.
      * 
      * @return current bindings
      */
     public final Map<Binding<?>, Binding<?>> bindings() { //TODO : replace for real multi-binder and better perf
         if (bindings == null || bindingsChanged) {
             bindings = new HashMap<Binding<?>, Binding<?>>();
             for (Binder binder : binders) {
                 for (Binding<?> binding : binder.getBindings().keySet()) {
                     bindings.put(binding, binder.getBindings().get(binding));
                 }
             }
             // extension point -> provided bindings
             for (Binding binding : loader.getProvidedBindings()) {
                 bindings.put(binding, binding);
             }
 
             final DSInjector injector = this;
             Binding injectorBinding = new Binding(null, null, DSInjector.class,
                     DSInjector.class, new Provider<DSInjector>() {
 
                 @Override
                 public DSInjector get() {
                     return injector;
                 }
             }, null);
             bindings.put(injectorBinding, injectorBinding);
 
             final Stage stag = stage;
             Binding stageBinding = new Binding(null, null, Stage.class,
                     Stage.class, new Provider<Stage>() {
 
                 @Override
                 public Stage get() {
                     if (stag == null) {
                         return Stage.NONE;
                     }
                     return stag;
                 }
             }, null);
             bindings.put(stageBinding, stageBinding);
 
             Binding eventManagerBinding = new Binding(null, null, EventManager.class,
                     EventManagerImpl.class, new InstanceProvider(eventManager), null);
             bindings.put(eventManagerBinding, eventManagerBinding);
 
             Binding eventBinding = new Binding(null, null, Event.class,
                     EventImpl.class, null, null);
             bindings.put(eventBinding, eventBinding);
 
             bindingsChanged = false;
         }
         return bindings;
     }
 
     /**
      * Get an injected instance of c
      *
      * @param <T> type
      * @param c class of the instance
      * @return instance of c
      */
     @Override
     public final <T> T getInstance(Class<T> c) {
         return getInstance(c, true);
     }
 
     public final <T> T getUnscopedInstance(Class<T> c) {
         return getInstance(c, false);
     }
 
     public final <T> T getInstance(Class<T> c, boolean scoped) {
         long start = System.currentTimeMillis();
         try {
             return getInstance(c, null, null, scoped);
         } finally {
             if (DependencyShot.DEBUG) {
                 logger.info(new StringBuilder().append("Time elapsed for injection : ").append(System.currentTimeMillis() - start).append(" ms.").toString());
             }
         }
     }
 
     /**
      * Get an injected qualified instance of c
      *
      * @param <T> type
      * @param c class of the instance
      * @param qualifier instance qualifier
      * @return instance of c
      */
     private <T> T getInstance(Class<T> c, Annotation qualifier, InjectionPoint point, boolean scoped) {
         Binding<T> binding = getBinding(c, qualifier);
         actualFromClass = binding.getFrom();
         return binding.getInstance(this, point, scoped);
     }
 
     /**
      * Get an existing binding in current bindings.
      *
      * @param <T> type
      * @param c class of the binding
      * @param annotation annotation of the binding
      * @return exsiting binding
      */
     private <T> Binding<T> getBinding(Class<T> c, Annotation annotation) {
         Binding<T> b = (Binding<T>) bindings().get(Binding.lookup(c, annotation));
         if (b != null) {
             return b;
         } else {
             if (!c.isInterface()) {
                 // add binding for single bindings
                 this.bindings.put(new Binding<T>(null, null,
                         c, c, null, null), new Binding<T>(null, null,
                         c, c, null, null));
                 b = getBinding(c, annotation);
             } else {
                 // TODO : search for classes implementing interface
             }
             if (b != null) {
                 return b;
             }
             throw new IllegalStateException("No binding for " + c + " and " + annotation);
         }
         //throw new IllegalStateException("No binding for " + c + " and " + annotation);
     }
 
     /**
      * Get a singleton instance of a class @Singleton
      *
      * @param <T> type
      * @param c class of the new instance
      * @return singleton instance of c
      */
     public final <T> T getSingleton(Class<T> c) {
         // check if the singleton is present in the singleton context
         T result = c.cast(singletonContext.get(c));
         // if not, create one
         if (result == null) {
             result = createInstance(c);
             singletonContext.put(c, result);
         }
         System.out.println(((Object) result).toString());
         return result;
     }
 
     /**
      * Create a new instance of a class
      *
      * @param <T> type
      * @param c class of the new instance
      * @return new instance of c
      */
     public final <T> T createInstance(Class<T> c) {
         // manage circular dependencies
         synchronized (this) {
             if (!instanciatedClasses.containsKey(c)) {
                 try {
                     instanciatedClasses.put(c, null);
                     // create a new instance of a class
                     T result = constructorHandler.getConstructedInstance(c, this);
                     instanciatedClasses.put(c, result);
                     // and inject it !!
                     classHandler.classInjection(result, c, new ArrayList<Method>(), false, this);
                     instanciatedClasses.remove(c);
                     if (circularClasses.contains(c)) {
                         circularClasses.remove(c);
                     } else {
                         if (circularClasses.size() > 0) {
                             circularConstructorArgumentsInstances.put(c, result);
                         }
                     }
                     return result;
                 } catch (Exception e) {
                     throw new DSException(e);
                 }
             } else {
                 if (allowCircularDependencies || ReflectionUtil.isSingleton(c)) {
                     if (actualFromClass.isInterface() && instanciatedClasses.get(c) == null) {
                         circularClasses.add(c);
                         System.out.println("Circular dependency detected in constructor of " + c.getSimpleName() + ".java. Trying to proxy it ... ");
                         CircularProxy proxy = new CircularProxy();
                         proxy.setInjector(this);
                         proxy.setClazz(c);
                         proxy.setCircularConstructorArgumentsInstances(circularConstructorArgumentsInstances);
                         T instance = (T) Proxy.newProxyInstance(
                                 Thread.currentThread().getContextClassLoader(),
                                 new Class[]{actualFromClass}, proxy);
                         instanciatedClasses.put(c, instance);
                     }
                     if (!actualFromClass.isInterface() && instanciatedClasses.get(c) == null) {
                         throw new DSException("Can't proxy circular dependencies without interface.");
                     }
                     try {
                         return (T) instanciatedClasses.get(c);
                     } finally {
                         circularClasses.remove(c);
                     }
                 } else {
                     throw new DSCyclicDependencyDetectedException(
                             "Circular dependency detected on " + c.getName());
                 }
             }
         }
     }
 
     /**
      * Injection on an instance.
      * WARNING : Constructor injection doesn't work
      * with this kind of injection.
      *
      * @param <T> type
      * @param instance of T
      * @return injected instance
      */
     @Override
     public final <T> T injectInstance(T instance) {
         try {
             T result = instance;
             // and inject it !!
             classHandler.classInjection(result,
                     instance.getClass(), new ArrayList<Method>(), false, this);
             return result;
         } catch (Exception e) {
             throw new DSException(e);
         }
     }
 
     /**
      * Inject the static members of a class.
      * 
      * @param c the class to inject.
      */
     @Override
     public final void injectStatics(Class<?> c) {
         try {
             List<Method> emptyList = Collections.emptyList();
             classHandler.classInjection(null, c, emptyList, true, this);
         } catch (Exception e) {
             throw new DSException("Could not inject static members for " + c, e);
         }
     }
 
     /**
      * Get an object instance of a qualified injection from a normal binding or
      * a defined provider.
      * 
      * @param type class of the object.
      * @param genericType type of the object.
      * @param annotations annotations present on the field or the method
      * @return
      */
     public final Object getProviderOrInstance(Class<?> type, Type genericType, Annotation[] annotations, Member m) {
         Object value;
         Annotation qualifier = null;
         // search in custom annotations wich one is a qualifier
         for (int i = 0; i < annotations.length; i++) {
             Annotation annotation = annotations[i];
             if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                 // when found, do the rest
                 qualifier = annotation;
                 break;
             }
         }
         // if provided, then return an injected provider
         if (Provider.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
             ParameterizedType providerType = (ParameterizedType) genericType;
             final Class<?> providedType = (Class<?>) providerType.getActualTypeArguments()[0];
             final Annotation finalQualifier = qualifier;
             final InjectionPoint point = new InjectionPointImpl(providerType,
                     new HashSet(Arrays.asList(annotations)), m, type);
             value = new Provider() {
 
                 @Override
                 public Object get() {
                     return getInstance(providedType, finalQualifier, point, true);
                 }
             };
         } else { // or get a simple instance
             value = getInstance(type, qualifier, new InjectionPointImpl(genericType,
                     new HashSet(Arrays.asList(annotations)), m, type), true);
         }
         return value;
     }
 
     /**
      * Reset all the binders of an injector.
      */
     public final void resetBinders() {
         this.binders = new ArrayList();
         this.singletonContext = new HashMap<Class<?>, Object>();
     }
 
     /**
      * Register a shutdown hook to shutdown container when JVM stop.
      */
     @Override
     public final void registerShutdownHook() {
         ShutdownThread thread = new ShutdownThread();
         thread.setInjector(this);
         Runtime.getRuntime().addShutdownHook(thread);
     }
 
     @Override
     public final void registerEventListener(EventListener<? extends Event> listener) {
         eventManager.registerListener(listener);
     }
 
     /**
      * Trigger all lifecycle handlers preDestroy method.
      */
     public void triggerLifecycleDestroyCallbacks() {
         for (InstanceLifecycleHandler handler : loader.getLifecycleHandlers()) {
             for (Object o : handler.getManagedInstances()) {
                 handler.handlePreDestroy(o);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final String toString() {
         StringBuilder builder = new StringBuilder();
         if (bindings != null) {
             for (Binding b : bindings.values()) {
                 builder.append(b);
                 builder.append("\n");
             }
         } else {
             builder.append("No bindings in this binder.");
         }
         return builder.toString();
     }
 
     public Map<Class<?>, Object> getInstanciatedClasses() {
         return instanciatedClasses;
     }
 
     /**
      * @return the stage of the injector.
      */
     @Override
     public final Stage getStage() {
         return stage;
     }
 
     @Override
     public void allowCircularDependencies(boolean allowCircularDependencies) {
         this.allowCircularDependencies = allowCircularDependencies;
     }
 
     @Override
     public boolean areCircularDependenciesAllowed() {
         return allowCircularDependencies;
     }
 
     public PluginsLoader getLoader() {
         return loader;
     }
 
     public CustomScopeHandler getScopeHandler(Class<? extends Annotation> clazz) {
         CustomScopeHandler handler = scopeHandlers.get(clazz);
         if (handler != null) {
             return scopeHandlers.get(clazz);
         } else {
             return new SimpleScope();
         }
     }
 
     public EventManagerImpl getEventManager() {
         return eventManager;
     }
 
     public List<Binder> getBinders() {
         return binders;
     }
 }
