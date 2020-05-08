 package org.filterinterceptor.proxy.spring;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.filterinterceptor.FilterService;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  * This class can be used by spring interceptor (part of spring AOP) to redirect
  * all service methods call on ServiceFilter invoke method
  * 
 * @See FilterService
  */
 public class FilterInterceptor implements MethodInterceptor {
 
 	private FilterService filterService;
 	private boolean extendToInterfaces;
 
 	/**
 	 * This method is call automatically by the AOP layer and delegate treatment
 	 * to FilterService
 	 */
 	@Override
 	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
 		return filterService.invoke(methodInvocation.getThis(), extendToInterfaces, methodInvocation.getMethod(),
 				methodInvocation.getArguments());
 	}
 
 	/*
 	 * BEAN
 	 */
 	/**
 	 * Set the filter service use to search filter
 	 * 
 	 * @param filterService
 	 *            the Service filter to use
 	 */
 	@Required
 	public void setFilterService(FilterService filterService) {
 		this.filterService = filterService;
 	}
 
 	/**
 	 * Set the extended search status, default value is false
 	 * 
 	 * @param extendToInterfaces
 	 *            indicate if the search of filters is done on the service class
 	 *            only or if it is done with the interfaces of the services too
 	 */
 	// @Required //default value is false
 	public void setExtendToInterfaces(boolean extendToInterfaces) {
 		this.extendToInterfaces = extendToInterfaces;
 	}
 }
