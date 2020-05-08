 /*
  * --------------------------------------------------------
  *  Module Name : service-manager
  *  Version : 0.1-SNAPSHOT
  *
  *  Software Name : HomeNap
  *  Version : 0.1-SNAPSHOT
  *
  *  Copyright © 28/06/2012 – 28/06/2012 France Télécom
  *  This software is distributed under the Apache 2.0 license,
  *  the text of which is available at http://www.apache.org/licenses/LICENSE-2.0.html
  *  or see the "LICENSE-2.0.txt" file for more details.
  *
  * --------------------------------------------------------
  *  File Name   : ServiceManager.java
  *
  *  Created     : 28/06/2012
  *  Author(s)   : Remi Druilhe
  *
  *  Description :
  *
  * --------------------------------------------------------
  */
 
 package com.orange.homenap.localmanager.bundlemanager;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 
 public class BundleManager implements BundleManagerItf
 {
     // iPOJO injection
     private BundleContext bundleContext;
 
     public BundleManager(BundleContext bundleContext)
     {
         this.bundleContext = bundleContext;
     }
 
     public boolean start(String url)
     {
         Bundle bundle = null;
 
         System.setProperty("org.apache.felix.ipojo.handler.auto.primitive", "com.orange.homenap.localmanager.migrationservice.handler:migration-handler");
 
         try {
             bundle = bundleContext.installBundle(url);
         } catch (BundleException e) {
             e.printStackTrace();
             return false;
         }
 
         //System.out.println("Bundle " + bundle.getSymbolicName() + " installed");
 
         try {
             bundle.start();
         } catch (BundleException e) {
             e.printStackTrace();
             return false;
         }
 
        System.out.println("Bundle " + bundle.getSymbolicName() + " started");
 
         return true;
     }
 
     public boolean stop(String componentName)
     {
         Bundle bundle = null;
 
         Bundle[] temp = bundleContext.getBundles();
 
         for(int i = 0; i < temp.length; i++)
             if(temp[i].getSymbolicName().equals(componentName))
                 bundle = temp[i];
 
         try {
             bundle.stop();
         } catch (BundleException e) {
             e.printStackTrace();
             return false;
         }
 
         //System.out.println("Bundle " + componentName + " stopped");
 
         try {
             bundle.uninstall();
         } catch (BundleException e) {
             e.printStackTrace();
             return false;
         }
 
         //System.out.println("Bundle " + componentName + " uninstalled");
 
         return true;
     }
 }
