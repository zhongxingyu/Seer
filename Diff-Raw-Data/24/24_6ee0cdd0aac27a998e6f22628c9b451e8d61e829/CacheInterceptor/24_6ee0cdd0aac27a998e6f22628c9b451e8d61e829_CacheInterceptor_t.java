 /*
  * Copyright 2013 Red Hat, Inc.
  * Author: Dennis Crissman
  *
  * Licensed under the GNU Lesser General Public License, version 3 or
  * any later version.
  *
  * In addition to the conditions of LGPLv3, you must preserve author
  * attributions in source code distributions and must accompany binary
  * distributions with such author attributions.
  */
 
 package cacher.aop;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cacher.fetcher.FetchManager;
 import cacher.fetcher.FetchMultiple;
 import cacher.fetcher.FetchSingle;
 
 /**
  * <p>Allows caching to be added to an existing method by simply adding the
  * {@link FetcherMethod} annotation to a method</p>
  * 
  * TODO: Complete documentation
  * 
  * <p><b>NOTE:</b> Bulk fetches require that the annotated method returns a Map&#60;String, Object&#62;</p>
  * 
  * @author Dennis Crissman
  * 
  * @see FetcherMethod
  */
 public class CacheInterceptor implements MethodInterceptor{
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(CacheInterceptor.class);
 
 	private FetchManager fetchManager;
 
 	public CacheInterceptor(){}
 
 	@Inject
 	public CacheInterceptor(FetchManager fetchManger){
 		this.fetchManager = fetchManger;
 	}
 
 	@Inject
 	public void setFetchManager(FetchManager fetchManager){
 		this.fetchManager = fetchManager;
 	}
 
 	public FetchManager getFetchManager(){
 		return fetchManager;
 	}
 
 	@Override
 	public Object invoke(MethodInvocation invocation) throws Throwable { //NOSONAR
 		Object[] safetyArgs = invocation.getArguments().clone();
 
 		try{
 			if(getFetchManager() == null){
 				throw new IllegalStateException("A FetchManager instance must set.");
 			}
 
 			Method method = invocation.getMethod();
 			FetcherMethod cacheAnnotation = method.getAnnotation(FetcherMethod.class);
 			if(cacheAnnotation == null){
 				throw new IllegalStateException(
 						"Unable to intercept a method without the FetcherMethod annotation: " + method.getName());
 			}
 
 			KeyGenerator keyGenerator = cacheAnnotation.keyGenerator().newInstance();
 			ensureMethodInvocationOnAwareObject(keyGenerator, invocation);
 
 			if(cacheAnnotation.fetchBulk()){
 				KeyCleaner keyCleaner = cacheAnnotation.keyCleaner().newInstance();
 				ensureMethodInvocationOnAwareObject(keyCleaner, invocation);
 
 				return getFetchManager().fetchMultiple(
 						cacheAnnotation.prefix(),
 						keyGenerator.generateKeys(invocation.getArguments()),
 						new MultipleFetcher(invocation, keyCleaner));
 			}
 			else{
 				return getFetchManager().fetchSingle(
 						cacheAnnotation.prefix(),
 						keyGenerator.generateKey(invocation.getArguments()),
 						new SingleFetcher(invocation));
 			}
 		}
 		catch(AopFetcherInvocationException e){
 			LOGGER.info("Fetcher threw an Exception that should be handled by client code, see following stacktrace:", e);
 			throw e.getCause();
 		}
 		catch(Exception e){
 			/*
 			 * General catch all in case something unexpected happens, this will ensure that the
 			 * FetcherMethod is still invoked.
 			 */
 			LOGGER.info("An unexpected exception was thrown while attempting to cache '" + invocation.getMethod().getName()
 					+ "'. Method will now be invoked without caching enabled.", e);
 
 			/*
 			 * This is an attempt to restore the arguments to their original values
 			 * should any changes have occurred during the interception. The idea being
 			 * that if we are aborting the cache, then we want the FetchMethod to retrieve
 			 * all values.
 			 */
			System.arraycopy(safetyArgs, 0, invocation.getArguments(), 0, safetyArgs.length);
 
 			return invocation.proceed();
 		}
 	}
 
 	/**
 	 * Ensures that the {@link MethodInvocation} is populated on any {@link MethodInvocationAware} classes.
 	 * @param obj - Object to be checked
 	 * @param invocation - {@link MethodInvocation}
 	 */
 	private void ensureMethodInvocationOnAwareObject(Object obj, MethodInvocation invocation){
 		if(obj instanceof MethodInvocationAware){
 			((MethodInvocationAware)obj).setMethodInvocation(invocation);
 		}
 	}
 
 	/**
 	 * {@link FetchSingle} implementation used to wrap single result {@link FetcherMethod}s.
 	 * 
 	 * @author Dennis Crissman
 	 */
 	private static class SingleFetcher implements FetchSingle<Object>{
 
 		private final MethodInvocation invocation;
 
 		public SingleFetcher(MethodInvocation invocation){
 			this.invocation = invocation;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see cacher.fetcher.FetchSingle#fetch(java.lang.String)
 		 */
 		@Override
 		public Object fetch(String uncachedKey) {
 			try {
 				return invocation.proceed();
 			}
 			catch (Throwable e) { //NOSONAR
 				throw new AopFetcherInvocationException(
 						"Unable to fetch key for method " + invocation.getMethod().toString() + ": " + uncachedKey, e);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see cacher.fetcher.FetchSingle#getType()
 		 */
 		@Override
 		@SuppressWarnings("unchecked")
 		public Class<Object> getType() {
 			return (Class<Object>)invocation.getMethod().getReturnType();
 		}
 
 	}
 
 	/**
 	 * {@link FetchMultiple} implementation used to wrap a bulk {@link FetcherMethod}s.
 	 * 
 	 * @author Dennis Crissman
 	 */
 	private static class MultipleFetcher implements FetchMultiple<Object>{
 
 		private final MethodInvocation invocation;
 		private final KeyCleaner cleaner;
 
 		public MultipleFetcher(MethodInvocation invocation, KeyCleaner cleaner){
 			this.invocation = invocation;
 			this.cleaner = cleaner;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see cacher.fetcher.FetchMultiple#fetch(java.util.List)
 		 */
 		@Override
 		public Map<String, Object> fetch(List<String> uncachedKeys) {
 			try {
 				cleaner.clean(invocation.getArguments(), uncachedKeys);
 
 				Object obj;
 				try{
 					obj = invocation.proceed();
 				}
 				catch(Throwable e){ //NOSONAR
 					throw new AopFetcherInvocationException(
 							"Unable to fetch keys for method " + invocation.getMethod().toString() + ": " + uncachedKeys, e);
 				}
 
 				if(obj instanceof Map){
 					Map<String, Object> results = new HashMap<String, Object>();
 
 					@SuppressWarnings("unchecked")
 					Map<Object, Object> fetchedResults = (Map<Object, Object>) obj;
 					for(Entry<Object, Object> entry : fetchedResults.entrySet()){
 						results.put(entry.getKey().toString(), entry.getValue());
 					}
 
 					return results;
 				}
 				else{
 					throw new InvalidBulkReturnTypeException("@FetcherMethod must return a Map<String, Object> in order to utilize Caching: "
 							+ invocation.getMethod().toString());
 				}
 
 			}
 			catch(AopFetcherInvocationException e){ //NOSONAR
 				throw e;
 			}
 			catch (Exception e) {
 				throw new CacheInterceptorException(
 						"Unable to fetch keys for method " + invocation.getMethod().toString() + ": " + uncachedKeys, e);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see cacher.fetcher.FetchMultiple#getType()
 		 */
 		@Override
 		public Class<Object> getType() {
 			return Object.class;
 		}
 
 	}
 
 	/**
 	 * <p>Exception that is thrown when the {@link FetcherMethod} throws an {@link Exception} of it's own.</p>
 	 * <p>This helps ensure that the Fetcher will not be executed a second time.</p>
 	 * 
 	 * @author Dennis Crissman
 	 */
 	private static class AopFetcherInvocationException extends RuntimeException{
 
 		private static final long serialVersionUID = -7822168108464126289L;
 
 		public AopFetcherInvocationException(String message, Throwable cause) {
 			super(message, cause);
 		}
 
 	}
 
 	/**
 	 * General purpose {@link CacheInterceptor} exception.
 	 * 
 	 * @author Dennis Crissman
 	 */
 	private static class CacheInterceptorException extends RuntimeException{
 
 		private static final long serialVersionUID = -2880937590798593384L;
 
 		public CacheInterceptorException(String message, Throwable cause) {
 			super(message, cause);
 		}
 
 	}
 
 	/**
 	 * Thrown for Bulk Fetches when the {@link FetcherMethod} returns an invalid type.
 	 * 
 	 * @author Dennis Crissman
 	 */
 	private static class InvalidBulkReturnTypeException extends RuntimeException{
 
 		private static final long serialVersionUID = -4638802706989516499L;
 
 		public InvalidBulkReturnTypeException(String message) {
 			super(message);
 		}
 
 	}
 
 }
