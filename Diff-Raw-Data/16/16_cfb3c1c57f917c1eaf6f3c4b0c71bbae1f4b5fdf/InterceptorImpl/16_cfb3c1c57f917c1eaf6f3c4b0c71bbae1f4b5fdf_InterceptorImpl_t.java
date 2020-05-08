 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.ioc;
 
 import org.greatage.ioc.annotations.Intercept;
 import org.greatage.ioc.annotations.Order;
 import org.greatage.ioc.logging.Logger;
 import org.greatage.ioc.proxy.MethodAdvice;
 import org.greatage.util.CollectionUtils;
 import org.greatage.util.StringUtils;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class InterceptorImpl<T> implements Interceptor<T> {
 	private final Class<?> moduleClass;
 	private final Method interceptMethod;
 
 	private final Class<T> serviceClass;
 	private final String serviceId;
 
 	private final String orderId;
 	private final List<String> orderConstraints;
 
 	InterceptorImpl(final Class<?> moduleClass, final Method interceptMethod) {
 		this.moduleClass = moduleClass;
 		this.interceptMethod = interceptMethod;
 
 		final Intercept intercept = interceptMethod.getAnnotation(Intercept.class);
 		serviceId = !StringUtils.isEmpty(intercept.serviceId()) ? intercept.serviceId() : null;
 		//noinspection unchecked
 		serviceClass = intercept.value();
 
 		final Order order = interceptMethod.getAnnotation(Order.class);
 		if (order != null) {
 			orderId = order.value();
 			orderConstraints = Arrays.asList(order.constraints());
 		} else {
 			orderId = "";
 			orderConstraints = CollectionUtils.newList();
 		}
 	}
 
 	public boolean supports(final Service service) {
 		return serviceId != null ?
 				service.getServiceId().equals(serviceId) :
				serviceClass.isAssignableFrom(service.getServiceClass());
 	}
 
 	public String getOrderId() {
 		return orderId;
 	}
 
 	public List<String> getOrderConstraints() {
 		return orderConstraints;
 	}
 
 	public MethodAdvice intercept(final ServiceResources<T> resources) {
 		final Logger logger = resources.getLogger();
 		if (logger != null) {
 			logger.info("Intercepting service (%s, %s) from module (%s, %s)", resources.getServiceId(), resources.getServiceClass(), moduleClass, interceptMethod);
 		}
 
 		try {
 			final Object moduleInstance = Modifier.isStatic(interceptMethod.getModifiers()) ? null : resources.getResource(moduleClass);
 			final Object[] parameters = InternalUtils.calculateParameters(resources, interceptMethod);
 			return (MethodAdvice) interceptMethod.invoke(moduleInstance, parameters);
 		} catch (Exception e) {
 			throw new RuntimeException(String.format("Can't create service interceptor with id '%s'", resources.getServiceId()), e);
 		}
 	}
 }
