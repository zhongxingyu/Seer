 package org.jboss.weld.environment.osgi.integration;
 
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 
 /**
  *
  * @author mathieu
  */
 public class ServicesProducer {
 
     @Produces @OSGiService
     public <T> Collection<T> OSGiServices(InjectionPoint p) throws Exception {
         Bundle bundle = FrameworkUtil.getBundle(p.getMember().getDeclaringClass());
         Class serviceClass = ((Class)((ParameterizedType)p.getType()).getActualTypeArguments()[0]);
         String serviceName = serviceClass.getName();
         List result = new ArrayList();
         ServiceReference[] refs = bundle.getBundleContext().getServiceReferences(serviceName, null);
         for (ServiceReference ref : refs) {
             if (!serviceClass.isInterface()) {
                 result.add(bundle.getBundleContext().getService(ref));
             } else { // Totally dumb, dynamic is for single injection point
                 // TODO : here provide a dynamic collection and not a collection of dynamic services
                 result.add(Proxy.newProxyInstance(
                             getClass().getClassLoader(),
                             new Class[]{(Class) serviceClass},
                             new DynamicServiceHandler(bundle, serviceName)));
             }
         }
         return result;
     }
 }
