 package org.restmodules.jersey.internal;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.restmodules.ApplicationProvider;
 import org.restmodules.ApplicationRegistrar;
 import org.restmodules.ApplicationRegistrarFactory;
 import org.restmodules.jersey.JerseyApplicationRegistrar;
 
 /**
 * @author Mathias Brkelmann
  */
 public class Activator implements BundleActivator {
 
    public void start(final BundleContext bc) throws Exception {
         bc.registerService(ApplicationRegistrarFactory.class.getName(), new ApplicationRegistrarFactory() {
 
            public ApplicationRegistrar create(final ApplicationProvider context) {
                 return new JerseyApplicationRegistrar(context);
             }
         }, null);
     }
 
    public void stop(final BundleContext bc) throws Exception {
     }
 }
