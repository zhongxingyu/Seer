 package com.magenta.guice.jpa;
 
 import static com.magenta.guice.jpa.DB.Transaction.NOT_REQUIRED;
 
 import com.google.inject.Binding;
 import com.google.inject.BindingAnnotation;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 import com.google.inject.spi.TypeEncounter;
 import com.google.inject.spi.TypeListener;
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.WeakHashMap;
 
 /**
  * Project: Maxifier
  * Author: Aleksey Didik
  * Created: 24.05.2010
  * <p/>
  * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
  * Magenta Technology proprietary and confidential.
  * Use is subject to license terms.
  */
 final class DBInterceptor implements MethodInterceptor, TypeListener {
 
     /**
      * Annotation for default @DB usage.
      * Unique name to be not overridden.
      */
     private final static Named DEFAULT = Names.named(UUID.randomUUID().toString());
 
     /**
      * EntityManagers contexts of current thread.
      * One context per one binding annotation.
      */
     private final Map<Object, ThreadLocal<EntityManager>> contexts = new HashMap<Object, ThreadLocal<EntityManager>>();
 
     /**
      * Last used context of this thread is here
      */
     private final ThreadLocal<EntityManager> currentContext = new ThreadLocal<EntityManager>();
 
     /**
      * Cache for EMFs
      */
     private final Map<Object, Provider<EntityManagerFactory>> emfsCache = new HashMap<Object, Provider<EntityManagerFactory>>();
 
     /**
      * Method cache for annotations.
      * Annotation[] array: [0] - @DB, [1] - binding Annotation.
      */
     private final Map<Method, Annotation[]> methodsCache = new WeakHashMap<Method, Annotation[]>();
 
     /**
      * List of used with annotations needed to be checked.
      */
     private final List<Annotation[]> awaiting = new LinkedList<Annotation[]>();
 
     /**
      * Does this interceptor prepared to work?
      */
     private volatile boolean prepared = false;
 
     @Inject
     synchronized void prepare(Injector injector) {
         //find all EMFs
         List<Binding<EntityManagerFactory>> bindingsByType
                 = injector.findBindingsByType(TypeLiteral.get(EntityManagerFactory.class));
         //prepare emfs cache and contexts
         for (Binding<EntityManagerFactory> binding : bindingsByType) {
             Key<EntityManagerFactory> key = binding.getKey();
             if (key.getAnnotation() != null) {
                 emfsCache.put(key.getAnnotation(), binding.getProvider());
                 contexts.put(key.getAnnotation(), new ThreadLocal<EntityManager>());
             } else if (key.getAnnotationType() != null) {
                 emfsCache.put(key.getAnnotationType(), binding.getProvider());
                 contexts.put(key.getAnnotationType(), new ThreadLocal<EntityManager>());
             } else {
                 //default binding
                 emfsCache.put(DEFAULT, binding.getProvider());
                 contexts.put(DEFAULT, new ThreadLocal<EntityManager>());
             }
         }
         prepared = true;
     }
 
 
     @Override
     public Object invoke(MethodInvocation invocation) throws Throwable {
         //check is prepared?
         if (!prepared) {
             throw new IllegalStateException("DBInterceptor is not ready but called from method");
         }
         //get method info
         Method method = invocation.getMethod();
         Annotation[] annotations = getAnnotations(method);
         DB dbAnnotation = (DB) annotations[0];
         Annotation bindingAnnotation = annotations[1];
         DB.Transaction transactionType = dbAnnotation.transaction();
 
         boolean owner = false;
 
         ThreadLocal<EntityManager> context = get(bindingAnnotation);
         EntityManager entityManager = context.get();
         if (entityManager == null) {
             EntityManagerFactory factory = getEntityManagerFactory(bindingAnnotation);
             entityManager = factory.createEntityManager();
             context.set(entityManager);
             owner = true;
         }
         //set current EM for this thread and save previous one
         EntityManager previousContext = currentContext.get();
         currentContext.set(entityManager);
 
         try {
             EntityTransaction transaction = entityManager.getTransaction();
             boolean alreadyInTransaction = transaction.isActive();
             //already have transaction
             if (alreadyInTransaction) {
                 Object result;
                 try {
                     result = invocation.proceed();
                 } catch (Throwable e) {
                     transaction.setRollbackOnly();
                     throw e;
                 }
                 return result;
             }
             //transaction not needed
             if (transactionType == NOT_REQUIRED) {
                 return invocation.proceed();
             }
             //need transaction
             transaction.begin();
             Object result;
             try {
                 result = invocation.proceed();
             } catch (Throwable e) {
                 transaction.rollback();
                 throw e;
             }
             transaction.commit();
             return result;
         } finally {
             //restore previous used context
             currentContext.set(previousContext);
             //make owner responsibilities
             if (owner) {
                 entityManager.close();
                 context.remove();
             }
         }
     }
 
 
     EntityManagerFactory getEntityManagerFactory(Annotation bindingAnnotation) {
         //may be cached?
         Provider<EntityManagerFactory> factoryProvider = emfsCache.get(bindingAnnotation);
         if (factoryProvider == null) {
             //may be we have to look by class?
             factoryProvider = emfsCache.get(bindingAnnotation.annotationType());
             if (factoryProvider == null) {
                 //looks like we have no such EMF, incredible, but error
                 throw new IllegalStateException(
                         String.format("EntityManagerFactory not found for this @DB method annotated with%s",
                                 bindingAnnotation != DEFAULT ? " " + bindingAnnotation : "out annotation"));
             }
         }
         return factoryProvider.get();
     }
 
     ThreadLocal<EntityManager> get(Annotation bindingAnnotation) {
         //get cached context
         ThreadLocal<EntityManager> context = contexts.get(bindingAnnotation);
         if (context == null) {
             context = contexts.get(bindingAnnotation.annotationType());
             if (context == null) {
                 //looks like we have no such EMF, incredible, but error
                 throw new IllegalStateException(
                         String.format("EntityManager context not found for this @DB method annotated with%s",
                                 bindingAnnotation != DEFAULT ? " " + bindingAnnotation : "out annotation"));
             }
         }
         return context;
     }
 
     @Deprecated
    EntityManager _getEntityManager() {
        ThreadLocal<EntityManager> context = contexts.get(DEFAULT);
         EntityManager entityManager = context.get();
         if (entityManager == null) {
            EntityManagerFactory factory = getEntityManagerFactory(DEFAULT);
             entityManager = factory.createEntityManager();
             context.set(entityManager);
         }
         return entityManager;
     }
 
     @Deprecated
    void _removeEntityManager() {
        contexts.get(DEFAULT).remove();
     }
 
     Annotation[] getAnnotations(Method method) {
         Annotation[] annotations = methodsCache.get(method);
         if (annotations == null) {
             DB db = method.getAnnotation(DB.class);
             if (db == null) {
                 throw new IllegalStateException("It's illegal state, this method must not be intercepted." +
                         " Use necessary matcher for DBInterceptor");
             }
             Annotation bindingAnnotation = getBindingAnnotation(method);
             annotations = new Annotation[]{db, bindingAnnotation};
             methodsCache.put(method, annotations);
 
         }
         return annotations;
     }
 
     Annotation getBindingAnnotation(Method method) {
         Annotation[] annotations = method.getAnnotations();
         Annotation found = null;
         for (Annotation annotation : annotations) {
             if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
                 if (found == null) {
                     found = annotation;
                 } else {
                     throw new IllegalStateException(String.format("At least two binding annotations used with @DB method." +
                             " Should be one only. Annotations: %s, %s", annotation.toString(), found.toString()));
                 }
             }
         }
         return found != null ? found : DEFAULT;
     }
 
 
     @Override
     public synchronized <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
         Class<? super I> rawType = type.getRawType();
         while (rawType != Object.class) {
             for (Method method : rawType.getDeclaredMethods()) {
                 if (method.isAnnotationPresent(DB.class)) {
                     try {
                         Annotation[] annotations = getAnnotations(method);
                         if (!prepared) {
                             awaiting.add(annotations);
                         } else {
                             checkAndCache(annotations, method);
                             if (!awaiting.isEmpty()) {
                                 for (Annotation[] v : awaiting) {
                                     checkAndCache(v, method);
                                 }
                                 awaiting.clear();
                             }
                         }
                     } catch (Exception e) {
                         encounter.addError(e);
                     }
                 }
             }
             //noinspection unchecked
             rawType = rawType.getSuperclass();
         }
     }
 
     private void checkAndCache(Annotation[] annotations, Method method) {
         Annotation binding = annotations[1];
         if (binding == null) {
             binding = DEFAULT;
         }
         if (!emfsCache.containsKey(binding) && !emfsCache.containsKey(binding.annotationType())) {
             String s;
             if (binding == DEFAULT) {
                 s = "Container contains @DB without binding annotations," +
                         " but no one EntityManagerFactory without binding annotations declared.";
             } else {
                 s = String.format("Container contains @DB method annotated with %s," +
                         " but no one EntityManagerFactory declared  with this annotations", binding);
             }
             throw new IllegalStateException(s);
         } else {
             methodsCache.put(method, annotations);
         }
     }
 
     public EntityManager current() {
         return currentContext.get();
     }
 }
