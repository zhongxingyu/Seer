 package org.easymetrics.easymetrics.cglib;
 
 import java.lang.reflect.Method;
 
 import org.easymetrics.easymetrics.model.Measurable;
 import org.easymetrics.easymetrics.model.annotation.ProxyMetrics;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 
 public class MetricsProxyProcessor implements BeanPostProcessor {
 
 	private ProxyInterceptor	proxyInterceptor	= new MetricsProxyInterceptor();
 
 	@Override
 	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
 		if (bean instanceof Measurable) {
 			return proxyInterceptor.proxyObject(bean);
 		}
 
 		Class<? extends Object> clazz = bean.getClass();
 		if (isMetricsProxied(clazz)) {
			return proxyInterceptor.proxyObject(clazz);
 		}
 
 		return bean;
 	}
 
 	@Override
 	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
 		return bean;
 	}
 
 	protected boolean isMetricsProxied(Class<?> clazz) {
 		Class<?> itr = clazz;
 		while (!itr.equals(Object.class)) {
 			for (Method m : itr.getDeclaredMethods()) {
 				if (m.getAnnotation(ProxyMetrics.class) != null) {
 					return true;
 				}
 			}
 			itr = itr.getSuperclass();
 		}
 		return false;
 	}
 
 	public void setProxyInterceptor(ProxyInterceptor proxyInterceptor) {
 		this.proxyInterceptor = proxyInterceptor;
 	}
 
 }
