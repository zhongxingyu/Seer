 package org.fedoraproject.eclipse.packager.koji.preferences;
 
 /**
  * Constant definitions for plug-in preferences
  */
 public final class PreferencesConstants {
 
 	/***************************************************
 	 * Prefences keys
 	 **************************************************/
 
 	/**
 	 * Preference key for the Web interface of the build system
 	 */
 	public static final String PREF_KOJI_WEB_URL = "kojiWebURL"; //$NON-NLS-1$
 	
 	/**
 	 * Preference key for the Hub/XMLRPC interface of the build system
 	 */
 	public static final String PREF_KOJI_HUB_URL = "kojiHubURL"; //$NON-NLS-1$
 	
 	/***************************************************
 	 * Preferences default values
 	 **************************************************/
 	
 	/**
 	 * Default URL of the build system's Web interface
 	 */
 	public static final String DEFAULT_KOJI_WEB_URL = 
		"http://koji.fedoraproject.org/koji"; //$NON-NLS-1$ //$NON-NLS-2$
 	
 	/**
 	 * Default URL of the build system's Hub/XMLRPC interface
 	 */
 	public static final String DEFAULT_KOJI_HUB_URL = 
 		"https://koji.fedoraproject.org/kojihub"; //$NON-NLS-1$ //$NON-NLS-2$
 }
