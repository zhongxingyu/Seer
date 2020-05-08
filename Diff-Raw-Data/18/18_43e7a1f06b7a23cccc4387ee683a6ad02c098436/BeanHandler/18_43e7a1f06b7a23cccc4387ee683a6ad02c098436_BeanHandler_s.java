 package org.lightmare.ejb.handlers;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.transaction.UserTransaction;
 
 import org.lightmare.cache.ConnectionData;
 import org.lightmare.cache.InjectionData;
 import org.lightmare.cache.InterceptorData;
 import org.lightmare.cache.MetaContainer;
 import org.lightmare.cache.MetaData;
 import org.lightmare.ejb.EjbConnector;
 import org.lightmare.ejb.interceptors.InvocationContextImpl;
 import org.lightmare.jpa.jta.BeanTransactions;
 import org.lightmare.utils.ObjectUtils;
 import org.lightmare.utils.reflect.MetaUtils;
 
 /**
  * Handler class to intercept bean method calls to provide database transactions
  * 
  * @author Levan
  * 
  */
 public class BeanHandler implements InvocationHandler {
 
     private final Object bean;
 
     private final Field transactionField;
 
     private final Collection<ConnectionData> connections;
 
     private final Collection<InjectionData> injects;
 
     private final MetaData metaData;
 
     public BeanHandler(final MetaData metaData, final Object bean) {
 
 	this.bean = bean;
 	this.transactionField = metaData.getTransactionField();
 	this.connections = metaData.getConnections();
 	this.injects = metaData.getInjects();
 	this.metaData = metaData;
     }
 
     /**
      * Sets each injected ejb bean as value to annotated field respectively for
      * passed {@link InjectionData} object
      */
     private void configureInject(InjectionData inject) throws IOException {
 
 	MetaData injectMetaData = inject.getMetaData();
 	if (injectMetaData == null) {
 	    String beanName;
 	    String mappedName = inject.getMappedName();
 	    if (mappedName == null || mappedName.isEmpty()) {
 		beanName = inject.getName();
 	    } else {
 		beanName = inject.getMappedName();
 	    }
 	    injectMetaData = MetaContainer.getSyncMetaData(beanName);
 	    injectMetaData.setInterfaceClasses(inject.getInterfaceClasses());
 
 	    inject.setMetaData(injectMetaData);
 	}
 	EjbConnector ejcConnector = new EjbConnector();
 	Object injectBean = ejcConnector.connectToBean(injectMetaData);
 
 	MetaUtils.setFieldValue(inject.getField(), bean, injectBean);
     }
 
     /**
      * Sets injected ejb bean as values to {@link javax.ejb.EJB} annotated
      * fields respectively
      * 
      * @throws IOException
      */
     private void configureInjects() throws IOException {
 
 	if (ObjectUtils.available(injects)) {
 	    for (InjectionData inject : injects) {
 		configureInject(inject);
 	    }
 	}
     }
 
     /**
      * Method to configure (injections {@link javax.ejb.EJB} or
      * {@link PersistenceUnit} annotated fields and etc.) {@link BeanHandler}
      * after initialization
      * 
      * @throws IOException
      */
     public void configure() throws IOException {
 	// TODO Add other configurations
 	configureInjects();
     }
 
     /**
      * Sets passed value to beans {@link Field}
      * 
      * @param field
      * @param value
      * @throws IOException
      */
     private void setFieldValue(Field field, Object value) throws IOException {
 	MetaUtils.setFieldValue(field, bean, value);
     }
 
     /**
      * Invokes passed bean {@link Method}
      * 
      * @param method
      * @param arguments
      * @return {@link Object}
      * @throws IOException
      */
     private Object invoke(Method method, Object... arguments)
 	    throws IOException {
 	return MetaUtils.invoke(method, bean, arguments);
     }
 
     /**
      * Sets {@link EntityManager} at beans's annotated field
      * 
      * @param em
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      */
     private void setConnection(Field connectionField, EntityManager em)
 	    throws IOException {
 	setFieldValue(connectionField, em);
     }
 
     /**
      * Creates and caches {@link UserTransaction} per thread
      * 
      * @param em
      * @return {@link UserTransaction}
      */
     private UserTransaction getTransaction(Collection<EntityManager> ems) {
 
 	UserTransaction transaction = BeanTransactions.getTransaction(ems);
 
 	return transaction;
     }
 
     private void setTransactionField(Collection<EntityManager> ems)
 	    throws IOException {
 
 	if (ObjectUtils.notNull(transactionField)) {
 	    UserTransaction transaction = getTransaction(ems);
 	    setFieldValue(transactionField, transaction);
 	}
     }
 
     /**
      * Creates {@link EntityManager} if passed {@link EntityManagerFactory} is
      * not null
      * 
      * @return {@link EntityManager}
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      */
     private EntityManager createEntityManager(ConnectionData connection)
 	    throws IOException {
 
 	EntityManagerFactory emf = connection.getEmf();
 	Field connectionField = connection.getConnectionField();
 	Field unitField = connection.getUnitField();
 	EntityManager em = null;
 	if (ObjectUtils.notNull(emf)) {
 	    em = emf.createEntityManager();
 	    if (ObjectUtils.notNull(unitField)) {
 		setFieldValue(unitField, emf);
 	    }
 	    setConnection(connectionField, em);
 	}
 
 	return em;
     }
 
     private Collection<EntityManager> createEntityManagers() throws IOException {
 
 	Collection<EntityManager> ems = null;
 	if (ObjectUtils.available(connections)) {
 	    ems = new ArrayList<EntityManager>();
 	    for (ConnectionData connection : connections) {
 		EntityManager em = createEntityManager(connection);
 		ems.add(em);
 	    }
 	}
 
 	return ems;
     }
 
     /**
      * Closes {@link EntityManager} if there is not
      * {@link javax.annotation.Resource} annotation in current bean
      * 
      * @param transaction
      * @param em
      */
     private void close(Method method) throws IOException {
 
 	try {
 	    if (ObjectUtils.notNull(method)) {
 		if (transactionField == null) {
 		    BeanTransactions.commitTransaction(this, method);
 		} else {
 		    BeanTransactions.closeEntityManagers();
 		}
 	    }
 	} finally {
 	    BeanTransactions.remove(this, method);
 	}
     }
 
     /**
      * Fills {@link Queue} of methods and targets for specified bean
      * {@link Method} and {@link InterceptorData} object
      * 
      * @param interceptorData
      * @param methods
      * @param targets
      * @throws IOException
      */
     private void fillInterceptor(InterceptorData interceptorData,
 	    Queue<Method> methods, Queue<Object> targets) throws IOException {
 
 	Class<?> interceptorClass = interceptorData.getInterceptorClass();
 	Object interceptor = MetaUtils.instantiate(interceptorClass);
 	Method method = interceptorData.getInterceptorMethod();
 	methods.offer(method);
 	targets.offer(interceptor);
     }
 
     /**
      * Checks if current {@link javax.interceptor.Interceptors} data is valid
      * for specified {@link Method} call
      * 
      * @param interceptor
      * @param method
      * @return <code>boolean</code>
      */
     private boolean checkInterceptor(InterceptorData interceptor, Method method) {
 
 	boolean valid;
 	Method beanMethod = interceptor.getBeanMethod();
 	if (ObjectUtils.notNull(beanMethod)) {
 	    valid = beanMethod.equals(method);
 	} else {
 	    valid = Boolean.TRUE;
 	}
 
 	return valid;
     }
 
     /**
      * Invokes first method from {@link javax.interceptor.Interceptors}
      * annotated data
      * 
      * @param method
      * @param parameters
      * @throws IOException
      */
    private void callInterceptors(Method method, Object[] parameters)
 	    throws IOException {
 
 	Collection<InterceptorData> interceptorsCollection = metaData
 		.getInterceptors();
 	if (ObjectUtils.available(interceptorsCollection)) {
 
 	    Iterator<InterceptorData> interceptors = interceptorsCollection
 		    .iterator();
 	    InterceptorData interceptor;
 	    Queue<Method> methods = new LinkedList<Method>();
 	    Queue<Object> targets = new LinkedList<Object>();
 	    boolean valid;
 	    while (interceptors.hasNext()) {
 		interceptor = interceptors.next();
 		valid = checkInterceptor(interceptor, method);
 		if (valid) {
 		    fillInterceptor(interceptor, methods, targets);
 		}
 	    }
 
 	    InvocationContextImpl context = new InvocationContextImpl(methods,
 		    targets, parameters);
 	    try {
 		context.proceed();
 	    } catch (Exception ex) {
 		throw new IOException(ex);
 	    }
 	}
     }
 
     /**
      * Invokes method surrounded with {@link UserTransaction} begin and commit
      * 
      * @param em
      * @param method
      * @param arguments
      * @return Object
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      * @throws InvocationTargetException
      */
     private Object invokeBeanMethod(final Collection<EntityManager> ems,
 	    final Method method, Object[] arguments) throws IOException {
 
 	if (transactionField == null) {
 	    BeanTransactions.addTransaction(this, method, ems);
 	} else {
 	    setTransactionField(ems);
 	}
 
 	// Calls interceptors for this method or bean instance
	callInterceptors(method, arguments);
 
 	// Calls for bean method
	Object value = invoke(method, arguments);
 
 	return value;
     }
 
     /**
      * Calls {@link BeanTransactions#rollbackTransaction(BeanHandler, Method))}
      * is case of {@link Throwable} is thrown at passed {@link Method} execution
      * time
      * 
      * @param method
      * @throws IOException
      */
     private void rollback(Method method) throws IOException {
 
 	if (ObjectUtils.notNull(method)) {
 	    BeanTransactions.rollbackTransaction(this, method);
 	}
     }
 
     @Override
     public Object invoke(Object proxy, Method method, Object[] arguments)
 	    throws Throwable {
 
 	Collection<EntityManager> ems = createEntityManagers();
 	Method realMethod = null;
 
 	try {
 	    Class<?> beanClass = metaData.getBeanClass();
 	    String methodName = method.getName();
 	    Class<?>[] parameterTypes = method.getParameterTypes();
 
 	    // Gets real method of bean class
 	    realMethod = MetaUtils.getDeclaredMethod(beanClass, methodName,
 		    parameterTypes);
 
 	    Object value;
 
 	    value = invokeBeanMethod(ems, realMethod, arguments);
 
 	    return value;
 
 	} catch (Throwable th) {
 	    rollback(realMethod);
 	    throw new Throwable(th);
 	} finally {
 	    close(realMethod);
 	}
     }
 
     public MetaData getMetaData() {
 
 	return metaData;
     }
 }
