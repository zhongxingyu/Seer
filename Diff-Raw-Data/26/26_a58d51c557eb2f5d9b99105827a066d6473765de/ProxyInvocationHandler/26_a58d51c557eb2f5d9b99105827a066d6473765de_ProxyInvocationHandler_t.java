 package security.proxy;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 import security.api.MethodHandler;
 
 public class ProxyInvocationHandler implements InvocationHandler, Serializable {
 
 	private static final long serialVersionUID = 7228714913913047614L;
 	
 	private ServiceReference<?> serviceReference;
     private BundleContext bundleContext;
     private MethodHandler handler;
 
     public ProxyInvocationHandler(BundleContext bundleContext,
             ServiceReference<?> serviceReference, MethodHandler handler) {
         this.serviceReference = serviceReference;
         this.bundleContext = bundleContext;
         this.handler = handler;
     }
 
     @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { 
    	String methodName = method.getName();
    	
    	if("equals".equals(methodName)) {
    		return proxyEquals(proxy, args[0]);
    	} else if("toString".equals(methodName)) {
    		return proxyToString(proxy);
    	} else if ("hashCode".equals(methodName)){
    		return proxyHashCode(proxy);
    	}
    	
     	if(handler != null) {
     		handler.handle(method);
         }
         
         Object invoke = method.invoke(bundleContext.getService(serviceReference),
                 args);
 
         return invoke;
     }
    
    protected Integer proxyHashCode(Object proxy) {
    	return new Integer(System.identityHashCode(proxy));
    }

    protected Boolean proxyEquals(Object proxy, Object other) {
    	return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }

    protected String proxyToString(Object proxy) {
    	return proxy.getClass().getName() + '@' +
    	    Integer.toHexString(proxy.hashCode());
    }
 }
 
