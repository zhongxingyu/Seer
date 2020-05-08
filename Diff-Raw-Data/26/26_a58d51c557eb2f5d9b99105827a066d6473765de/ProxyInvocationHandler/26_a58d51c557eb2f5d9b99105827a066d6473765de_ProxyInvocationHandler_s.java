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
     	if(handler != null) {
     		handler.handle(method);
         }
         
         Object invoke = method.invoke(bundleContext.getService(serviceReference),
                 args);
 
         return invoke;
     }
 }
 
