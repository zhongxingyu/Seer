 package nl.sense_os.commonsense.lib.client.communication;
 
 import com.google.gwt.core.client.GWT;
 
 class Constants {
 
     /**
      * Flag for Stable mode. <code>true</code> if the app is deployed to common.sense-os.nl.
      */
     public static final boolean STABLE_MODE = GWT.getModuleBaseURL().contains("common.sense-os.nl");
 
     /**
      * Flag for dev mode. <code>true</code> if the app is deployed to common.dev.sense-os.nl.
      */
    public static final boolean DEV_MODE = GWT.getModuleBaseURL().contains("dev.sense-os.nl");
 
     /**
      * Flag for Release Candidate mode. <code>true</code> if the app is deployed to rc.sense-os.nl.
      */
     public static final boolean RC_MODE = GWT.getModuleBaseURL().contains("rc.sense-os.nl");
 
     /**
      * Flag for local mode. <code>true</code> if the app is deployed to an unknown location.
      */
     public static final boolean GENERIC_MODE = !STABLE_MODE && !DEV_MODE && !RC_MODE;
 
     private Constants() {
         // Private constructor to make sure this class is not instantiated.
     }
 }
