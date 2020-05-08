 package org.tsaikd.java.utils;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
import java.util.ArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
 
 public class DynamicClientProxyFactoryBean implements InvocationHandler, Closeable {
 
 	static Log log = LogFactory.getLog(DynamicClientProxyFactoryBean.class);
 
 	private org.apache.cxf.endpoint.Client epClient = null;
 	private Object proxy = null;
 	private int retryMax = 1;
 	private int retryNow = 0;
 	private long retryWaitMs = 0;
 
 	public <T> T create(Class<T> serviceClass, String wsdlUrl) {
 		if (proxy == null) {
 			DynamicClientFactory dcf = DynamicClientFactory.newInstance();
 			epClient = dcf.createClient(wsdlUrl);
 			// set thread safe, http://cxf.apache.org/faq.html
 			epClient.getRequestContext().put("thread.local.request.context", "true");
 			proxy = Proxy.newProxyInstance(serviceClass.getClassLoader(), getImplementingClasses(serviceClass), this);
 		}
 		return serviceClass.cast(proxy);
 	}
 
 	public <T> T get(Class<T> serviceClass) {
 		return serviceClass.cast(proxy);
 	}
 
 	protected Class<?>[] getImplementingClasses(Class<?> cls) {
 		try {
 			if (cls.getMethod("close") != null) {
 				return new Class[] {cls};
 			}
 		} catch (Exception e) {
 			//ignore - doesn't have a close method so we
 			//can implement Closeable
 		}
 		return new Class[] {cls, Closeable.class};
 	}
 
 	public void setRetry(int retryMax, long retryWaitMs) {
 		this.retryMax = retryMax;
 		this.retryWaitMs = retryWaitMs;
 	}
 
 	@Override
 	public void close() throws IOException {
 		if (epClient != null) {
 			epClient.destroy();
 			epClient = null;
 		}
 		proxy = null;
 		retryMax = 1;
 		retryNow = 0;
 		retryWaitMs = 0;
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		if (null == args) {
 			args = new Object[0];
 		}
 		Object[] ret = null;
 		Exception eRet = null;
 
 		while (retryNow < retryMax) {
 			try {
 				ret = epClient.invoke(method.getName(), args);
 				retryNow = 0;
 				eRet = null;
 				break;
 			} catch (Exception e) {
 				retryNow++;
 				eRet = e;
 				if (retryWaitMs > 0) {
 					Thread.sleep(retryWaitMs);
 				}
 			}
 		}
 
 		if (eRet != null) {
 			throw eRet;
 		}
 
		if (ret == null) {
			return ret;
		}

		if (method.getReturnType().isArray()) {
			try {
				Object newret = ret[0];
				Method retMethod = newret.getClass().getMethod("getString");
				newret = retMethod.invoke(newret);
				newret = ((ArrayList<?>) newret).toArray(new String[0]);
				return newret;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		return ret[0];
 	}
 
 }
