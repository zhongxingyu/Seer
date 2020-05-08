 /* ******************************************************** 
 ** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
 ** $Header$
 ******************************************************** */ 
 
 package org.dianexus.triceps;
 
 import java.lang.String;
 
 /*public*/ interface VersionIF extends DeveloperLicenseIF {
     /*public*/ final static boolean DEBUG = true;
     /*public*/ final static boolean AUTHORABLE = false;
     /*public*/ final static boolean DEPLOYABLE = true;
	/*public*/ final static boolean WEB_SERVER = false;
    /*public*/ final static boolean USE_VERBOSE_LICENSE_MSG = (!WEB_SERVER && false);
     /*public*/ final static boolean DEMOABLE = (!AUTHORABLE && !DEPLOYABLE);
     /*public*/ final static boolean DEVELOPERABLE = (AUTHORABLE && DEPLOYABLE);
     /*public*/ final static String VERSION_MAJOR = "2.9";
    /*public*/ final static String VERSION_MINOR = "4";
     /*public*/ final static String VERSION_TYPE = ((DEVELOPERABLE) ? "Development System" : ((AUTHORABLE) ? "Authoring System" : ((DEPLOYABLE) ? "Interviewing System" : "Demo")));
     /*public*/ final static String VERSION_NAME = STUDY_ALIAS + " version of Dialogix " + VERSION_TYPE + " version " + VERSION_MAJOR + "." + VERSION_MINOR;
     /*public*/ final static String VERBOSE_LICENSE_MSG = "This <B>" + VERSION_NAME +
 		"</B> is <A HREF='/" + STUDY_ALIAS + "/triceps-license.html'>licensed</A> to " + PRINCIPAL_INVESTIGATOR + 
 		" exclusively for the " + STUDY_NAME + 
 		" [" + GRANT_NAME + 
 		":  " + GRANT_TITLE + 
 		"]";
 	/*public*/ final static String BRIEF_LICENSE_MSG = "Dialogix " + VERSION_TYPE + " version " + VERSION_MAJOR + "." + VERSION_MINOR;
 	/*public*/ final static String LICENSE_MSG = (USE_VERBOSE_LICENSE_MSG) ? VERBOSE_LICENSE_MSG : BRIEF_LICENSE_MSG;
 	/*public*/ final static boolean XML = false;
 	/*public*/ final static boolean DISPLAY_WORKING = (!WEB_SERVER);	// controls whether see working files
 	/*public*/ final static boolean DISPLAY_SPLASH = true;	// controls whether see splash screen
 }
