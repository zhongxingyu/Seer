 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.ioc;
 
 import org.greatage.ioc.annotations.Decorate;
 import org.greatage.ioc.annotations.Order;
 import org.greatage.ioc.logging.Logger;
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
 public class DecoratorImpl<T> implements Decorator<T> {
 	private final Class<?> moduleClass;
 	private final Method decorateMethod;
 
 	private final Class<T> serviceClass;
 	private final String serviceId;
 
 	private final String orderId;
 	private final List<String> orderConstraints;
 
 	DecoratorImpl(final Class<?> moduleClass, final Method decorateMethod) {
 		this.moduleClass = moduleClass;
 		this.decorateMethod = decorateMethod;
 
 		//noinspection unchecked
 		serviceClass = (Class<T>) decorateMethod.getReturnType();
 		if (!serviceClass.equals(decorateMethod.getParameterTypes()[0])) {
 			throw new IllegalArgumentException("Decorate method must have equals return type and first argument type");
 		}
 
 		final Decorate decorate = decorateMethod.getAnnotation(Decorate.class);
 		serviceId = !StringUtils.isEmpty(decorate.serviceId()) ? decorate.serviceId() : null;
 
 		final Order order = decorateMethod.getAnnotation(Order.class);
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
 
 	public T decorate(final ServiceResources<T> resources) {
 		final Logger logger = resources.getLogger();
 		if (logger != null) {
 			logger.info("Decorating service (%s, %s) from module (%s, %s)", resources.getServiceId(), resources.getServiceClass(), moduleClass, decorateMethod);
 		}
 
 		final Class<T> serviceClass = resources.getServiceClass();
 		try {
 			final Object moduleInstance = Modifier.isStatic(decorateMethod.getModifiers()) ? null : resources.getResource(moduleClass);
 			final Object[] parameters = InternalUtils.calculateParameters(resources, decorateMethod);
 			return serviceClass.cast(decorateMethod.invoke(moduleInstance, parameters));
 		} catch (Exception e) {
 			throw new RuntimeException(String.format("Can't create service configuration with id '%s'", resources.getServiceId()), e);
 		}
 	}
 }
