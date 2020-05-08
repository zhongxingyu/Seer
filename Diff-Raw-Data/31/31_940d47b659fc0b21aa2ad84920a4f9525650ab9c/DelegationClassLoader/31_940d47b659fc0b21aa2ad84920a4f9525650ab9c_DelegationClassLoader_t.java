 package org.openengsb.labs.delegation.service;
 
 import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class DelegationClassLoader extends ClassLoader {
 
     private static final long DEFAULT_TIMEOUT = 30000L;
     private final BundleContext bundleContext;
     private String delegationContext;
     private long timeout = DEFAULT_TIMEOUT;
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
        String filterString = String.format("(%s=%s)", Constants.PROVIDED_CLASSES_KEY, name);
        if (delegationContext != null) {
            filterString = String.format("(&%s(%s=%s))",
                filterString,
                Constants.DELEGATION_CONTEXT, delegationContext);
        }
        Filter filter;
        try {
            filter = FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            throw new ClassNotFoundException(name, e);
        }
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, filter, null);
         serviceTracker.open();
         try {
             return doFindClass(name, serviceTracker);
         } finally {
             serviceTracker.close();
         }
     }
 
     private Class<?> doFindClass(String name, ServiceTracker serviceTracker) throws ClassNotFoundException {
         ClassProvider service;
         try {
             service = (ClassProvider) serviceTracker.waitForService(timeout);
         } catch (InterruptedException e) {
             throw new ClassNotFoundException(name, e);
         }
         if (service == null) {
             throw new ClassNotFoundException(name);
         }
         return service.loadClass(name);
     }
 
     public DelegationClassLoader(BundleContext bundleContext) {
         this.bundleContext = bundleContext;
     }
 
     public DelegationClassLoader(BundleContext bundleContext, ClassLoader parent) {
         super(parent);
         this.bundleContext = bundleContext;
     }
 
     public DelegationClassLoader(BundleContext bundleContext, String delegationContext) {
         this.bundleContext = bundleContext;
     }
 
     public DelegationClassLoader(BundleContext bundleContext, String delegationContext, ClassLoader parent) {
         super(parent);
         this.bundleContext = bundleContext;
     }
 
     public void setTimeout(long timeout) {
         this.timeout = timeout;
     }
 
 }
