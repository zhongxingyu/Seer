 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import org.glite.authz.pap.common.exceptions.PAPConfigurationException;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.xml.ConfigurationException;
 
 public class XACMLPolicyCLIUtils {
     
     private static boolean notInitilized = true;
 
     public static void initOpenSAML() {
 
         if (notInitilized) {
             try {
                 DefaultBootstrap.bootstrap();
             } catch (ConfigurationException e) {
                 throw new PAPConfigurationException("Error initializing OpenSAML library", e);
             }
         }
     }
     
 }
