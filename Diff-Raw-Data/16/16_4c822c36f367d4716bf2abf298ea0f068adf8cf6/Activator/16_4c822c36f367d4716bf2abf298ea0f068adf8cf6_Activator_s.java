/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package org.restmodules.jersey.internal;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.restmodules.ApplicationProvider;
 import org.restmodules.ApplicationRegistrar;
 import org.restmodules.ApplicationRegistrarFactory;
 import org.restmodules.jersey.JerseyApplicationRegistrar;
 
 /**
 * @author Mathias Brökelmann
  */
 public class Activator implements BundleActivator {
 
    public void start(BundleContext bc) throws Exception {
         bc.registerService(ApplicationRegistrarFactory.class.getName(), new ApplicationRegistrarFactory() {
 
            public ApplicationRegistrar create(ApplicationProvider context) {
                 return new JerseyApplicationRegistrar(context);
             }
         }, null);
     }
 
    public void stop(BundleContext bc) throws Exception {
     }
 }
