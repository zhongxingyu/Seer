 package org.eclipse.jst.server.generic.core.internal;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Helper class to get messages
  * 
  * @author Gorkem Ercan
  */
 public class GenericServerCoreMessages extends NLS{
 
 	private static final String RESOURCE_BUNDLE= "org.eclipse.jst.server.generic.core.internal.GenericServerCoreMessages";//$NON-NLS-1$
 	public static String cancelNoPublish;
 	public static String moduleNotCompatible;
 	public static String errorPortInUse;
 	public static String errorJRE;
 	public static String errorNoServerType;
 	public static String errorNoClasspath;
 	public static String errorMissingClasspathEntry;
 	public static String errorRemoveModuleAntpublisher;
 	public static String errorPublishAntpublisher;
 	public static String commandlineUnspecified;
 	public static String workingdirUnspecified;
 	public static String errorLaunchingExecutable;
 	public static String missingServer;
 	public static String externalStopLauncher;
 	public static String debugPortUnspecified;
 	public static String errorStartingExternalDebugging;
 	public static String invalidPath;
 	
 	static{
 		  NLS.initializeMessages(RESOURCE_BUNDLE, GenericServerCoreMessages.class);
 	}
 
 }
