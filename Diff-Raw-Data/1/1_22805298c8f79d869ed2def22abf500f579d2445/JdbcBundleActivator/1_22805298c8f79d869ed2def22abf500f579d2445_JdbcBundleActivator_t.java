 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db.jdbc.service;
 
 import org.jbundle.base.model.DBParams;
 import org.jbundle.model.util.Util;
 import org.jbundle.util.osgi.BundleActivatorModel;
 import org.jbundle.util.osgi.bundle.BaseBundleActivator;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 /**
  * The bundle start activator. 
  * Create and register the class creation utility.
  * @author don
  *
  */
 public class JdbcBundleActivator extends BaseBundleActivator
 	implements BundleActivator
 {    
     /**
      * Bundle starting up.
      */
     public void start(BundleContext context) throws Exception {
     	Util.getLogger().info("Starting Jdbc bundle");
         
         //xthis.setProperty(BundleActivatorModel.PACKAGE_NAME, Util.getPackageName(JdbcDatabase.class.getName()));
         //x this.setProperty(BundleService.INTERFACE, BaseDatabase.class.getName());
         this.setProperty(BundleActivatorModel.TYPE, DBParams.JDBC);
         
         super.start(context);
     }    
     /**
      * Bundle stopping.
      */
     public void stop(BundleContext context) throws Exception {
     	Util.getLogger().info("Stopping Jdbc bundle");
 //        Automatically unregistered.
         super.stop(context);
     }
 
 }
