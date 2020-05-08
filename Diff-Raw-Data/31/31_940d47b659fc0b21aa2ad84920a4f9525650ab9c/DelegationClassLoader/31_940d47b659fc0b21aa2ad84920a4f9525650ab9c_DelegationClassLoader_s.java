 package org.openengsb.labs.delegation.service;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class DelegationClassLoader extends ClassLoader {
 
     private static final long DEFAULT_TIMEOUT = 30000L;
     private final BundleContext bundleContext;
     private String delegationContext;
     private long timeout = DEFAULT_TIMEOUT;
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
        ServiceTracker serviceTracker =
            new ServiceTracker(bundleContext,
                String.format("(&(%s=%s)(%s=%s))",
                    Constants.PROVIDED_CLASSES_KEY, name,
                    Constants.DELEGATION_CONTEXT, delegationContext),
                null);
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
