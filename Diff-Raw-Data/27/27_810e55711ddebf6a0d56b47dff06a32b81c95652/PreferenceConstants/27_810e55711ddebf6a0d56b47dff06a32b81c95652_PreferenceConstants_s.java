 package edu.depaul.cdm.madl.eclipse.ui;
 
 /**
  * Constant definitions for plug-in preferences
  */
 public class PreferenceConstants {
 
     private PreferenceConstants() {
         // uninstantiable
     }
 
     public static String APP_BUILDER_HOME = 
    		Activator.getDefault().getPreferenceStore().getString("APP_BUILDER_HOME");
     
 }
