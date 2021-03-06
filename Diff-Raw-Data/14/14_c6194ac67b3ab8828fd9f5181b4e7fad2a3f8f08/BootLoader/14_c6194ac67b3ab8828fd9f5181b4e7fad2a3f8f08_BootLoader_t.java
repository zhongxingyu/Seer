 package org.eclipse.core.boot;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
 
 import org.eclipse.core.internal.boot.*;
 import java.net.URL;
 
 /**
  * Special boot loader class for the Eclipse Platform. This class cannot
  * be instantiated; all functionality is provided by static methods.
  * <p>
  * The Eclipse Platform makes heavy use of Java class loaders for
  * loading plug-ins. Even the Platform Core Runtime itself, including
  * the <code>Platform</code> class, needs to be loaded by a special 
  * class loader. The upshot is that a client program (such as a Java main
  * program, a servlet) cannot directly reference even the 
  * <code>Platform</code> class. Instead, a client must use this
  * loader class for initializing the platform, invoking functionality
  * defined in plug-ins, and shutting down the platform when done.
  * </p>
  *
  * @see org.eclipse.core.runtime.Platform
  */
 public final class BootLoader {
 
 	/**
 	 * The unique identifier constant (value "<code>org.eclipse.core.boot</code>")
 	 * of the Core Boot (pseudo-) plug-in.
 	 */
 	public static final String PI_BOOT = "org.eclipse.core.boot";
 
 	/**
 	 * Constant string (value "win32") indicating the platform is running on a
 	 * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
 	 */
 	public static final String OS_WIN32 = "win32";
 
 	/**
 	 * Constant string (value "linux") indicating the platform is running on a
 	 * Linux-based operating system.
 	 */
 	public static final String OS_LINUX = "linux";
 
 	/**
 	 * Constant string (value "aix") indicating the platform is running on an
 	 * AIX-based machine.
 	 */
 	public static final String OS_AIX = "aix";
 
 	/**
 	 * Constant string (value "solaris") indicating the platform is running on a
 	 * Solaris-based machine.
 	 */
 	public static final String OS_SOLARIS = "solaris";
 
 	/**
 	 * Constant string (value "hpux") indicating the platform is running on an
 	 * HP/UX-based machine.
 	 */
 	public static final String OS_HPUX = "hpux";
 
 	/**
	 * Constant string (value "qnx") indicating the platform is running on a
	 * QNX-based machine.
 	 */
	public static final String OS_QNX = "qnx";
 
 	/**
 	 * Constant string (value "unknown") indicating the platform is running on a
 	 * machine running an unknown operating system.
 	 */
 	public static final String OS_UNKNOWN = "unknown";
 
 	/**
 	 * Constant string (value "win32") indicating the platform is running on a
 	 * machine using the Windows windowing system.
 	 */
 	public static final String WS_WIN32 = "win32";
 
 	/**
 	 * Constant string (value "motif") indicating the platform is running on a
 	 * machine using the Motif windowing system.
 	 */
 	public static final String WS_MOTIF = "motif";
 
 	/**
 	 * Constant string (value "gtk") indicating the platform is running on a
 	 * machine using the GTK windowing system.
 	 */
 	public static final String WS_GTK = "gtk";
 
 	/**
 	 * Constant string (value "photon") indicating the platform is running on a
 	 * machine using the Photon windowing system.
 	 */
 	public static final String WS_PHOTON = "photon";
 
 	/**
 	 * Constant string (value "unknown") indicating the platform is running on a
 	 * machine running an unknown windowing system.
 	 */
 	public static final String WS_UNKNOWN = "unknown";
 /**
  * Private constructor to block instance creation.
  */
 private BootLoader() {
 }
 /**
  * Returns whether the given location (typically a directory in the
  * local file system) contains the saved data for a platform. The
  * saved data for the platform is recognizable by the presence of
  * a special platform metadata subdirectory; however, this metadata
  * directory is not usually created unless there is some reason to do so
  * (for example, so that an active plug-in can save its state).
  *
  * @return <code>true</code> if the location contains the 
  *		saved data for a platform, and <code>false</code> otherwise
  */
 public static boolean containsSavedPlatform(String location) {
 	return InternalBootLoader.containsSavedPlatform(location);
 }
 /**
  * Returns the command line args provided to the platform when it was first run.
  * Note that individual platform runnables may be provided with different arguments
  * if they are being run individually rather than with <code>Platform.run()</code>.
  * 
  * @return the command line used to start the platform
  */
 public static String[] getCommandLineArgs() {
 	return InternalBootLoader.getCommandLineArgs();
 }
 /**
  * Returns the installation information for this invocation of the platform.
  *
  * @return the installation information.
  */
 public static IInstallInfo getInstallationInfo() {
 	return InternalBootLoader.getInstallationInfo();
 }
 /**
  * Returns URL at which the Platform runtime executables and libraries are installed.
  * The returned value is distinct from the location of any given platform's data.
  *
  * @return the URL indicating where the platform runtime is installed.
  */
 public static URL getInstallURL() {
 	return InternalBootLoader.getInstallURL();
 }
 /**
  * Returns the string name of the current locale for use in finding files
  * whose path starts with <code>$nl$</code>.  <code>null</code> is returned
  * if the locale cannot be determined.
  *
  * @return the string name of the current locale or <code>null</code>
  */
 public static String getNL() {
 	return InternalBootLoader.getNL();
 }
 /**
  * Returns the string name of the current operating system for use in finding files
  * whose path starts with <code>$os$</code>.  <code>OS_UNKNOWN</code> is returned
  * if the operating system cannot be determined.  The value may indicate one of the following
  * operating systems known to the platform (<code>OS_WIN32</code>, <code>OS_LINUX</code>,
  * <code>OS_AIX</code>, <code>OS_SOLARIS</code>, <code>OS_HPUX</code> or
  * a user-defined string if the operating system name is specified on the command line.
  *
  * @return the string name of the current operating system
  * 
  */
 public static String getOS() {
 	return InternalBootLoader.getOS();
 }
 /**
  * Returns the complete plugin path defined by the file at the given location.
  * If the given location is <code>null</code> or does not indicate a valid 
  * plug-in path definition file, <code>null</code> is returned.
  *
  * @return the complete set of URLs which locate plug-ins
  */
 public static URL[] getPluginPath(URL pluginPathLocation) {
 	return InternalBootLoader.getPluginPath(pluginPathLocation);
 }
 /**
  * Instantiates and returns an instance of the named application's 
  * runnable entry point.
  * <code>null</code> is returned if the runnable cannot be found.
  *
  * @param applicationName the fully qualified name of an 
  * 		extension installed in the platform's <code>applications</code> 
  *		extension point (i.e., <code>org.eclipse.core.applications</code>).
  * @return a platform runnable
  * @exception Exception if there is a problem instantiating the specified runnable
  */
 public static IPlatformRunnable getRunnable(String applicationName) throws Exception {
 	return InternalBootLoader.getRunnable(applicationName);
 }
 /**
  * Instantiates and returns an instance of the named class.  The class
  * must implement <code>IPlatformRunnable</code>.
  * If the class implements <code>IExecutableExtension</code>, the created
  * instance is initialized with the given arguments.
  * <code>null</code> is returned if the runnable cannot be found.
  *
  * @param pluginId the unique identifier of the plug-in containing the given class
  * @param className the fully qualified name of the class to instantiate
  * @param args the initialization arguments passed to the new instance
  * @return a platform runnable, or <code>null</code> if the runnable cannot
  *    be found
  * @exception Exception if there is a problem instantiating the specified runnable
  */
 public static IPlatformRunnable getRunnable(String pluginId, String className, Object args) throws Exception {
 	return InternalBootLoader.getRunnable(pluginId, className, args);
 }
 /**
  * Returns the string name of the current window system for use in finding files
  * whose path starts with <code>$ws$</code>.  <code>null</code> is returned
  * if the window system cannot be determined.
  *
  * @return the string name of the current window system or <code>null</code>
  */
 public static String getWS() {
 	return InternalBootLoader.getWS();
 }
 /**
  * Returns <code>true</code> if the platform is currently running in 
  * debug mode.  The platform is run
  * in debug mode using the "-debug" command line argument.
  *
  * @return whether or not the platform is running in debug mode
  */
 public static boolean inDebugMode() {
 	return InternalBootLoader.inDebugMode();
 }
 /**
  * Returns <code>true</code> if the platform is currently running in 
  * development mode.  That is, if special procedures are to be 
  * taken when defining plug-in class paths.  The platform is run
  * in development mode using the "-dev" command line argument.
  *
  * @return whether or not the platform is running in development mode
  */
 public static boolean inDevelopmentMode() {
 	return InternalBootLoader.inDevelopmentMode();
 }
 /**
  * Returns whether the platform is running.
  * The <code>startup</code> method starts the platform running;
  * the <code>shutdown</code> method stops it.
  *
  * @return <code>true</code> if the platform is running, 
  *		and <code>false</code> otherwise
  * @see #startup
  * @see #shutdown
  */
 public static boolean isRunning() {
 	return InternalBootLoader.isRunning();
 }
 /**
  * Launches the Platform to run a single application. 
  * This convenince method starts up the Platform,
  * runs the indicated application, and then shuts down
  * the Platform. The Platform must not be running already.
  *
  * @param applicationName The fully qualified name of an 
  * 		extension installed in the Platform plug-in's <code>applications</code> 
  *		extension-point (i.e., <code>org.eclipse.core.runtime.applications</code>).
  * @param pluginPathLocation the URL of the plug-in path; this is where
  *		the Platform is to find the code for plug-ins
  * @param location the location (usually a string path in the local file
  *		file system) for the saved platform state
  * @param args the array of command-line style argments which are passed
  *		to the Platform on initialization.  The arguments which are consumed by the
  * 		Platform's initialization are removed from the arg list.  This modified arg list is
  *		the return value of this method.  
  *@return the list of <code>args</code> which were supplied but not consumed
  *		by this method.  
  * @return the result, or <code>null</code> if none
  * @exception Exception if anything goes wrong
  * @see #startup
  */
 public static Object run(String applicationName, URL pluginPathLocation, String location, String[] args) throws Exception {
 	return InternalBootLoader.run(applicationName, pluginPathLocation, location, args);
 }
 /**
  * Shuts down the Platform. The Platform must be running. In the process,
  * each active plug-in is told to shutdown via <code>Plugin.shutdown</code>.
  * <p>
  * Note that the state of the Platform is not automatically saved
  * before shutting down.
  * </p>
  * <p>
  * On return, the Platform will no longer be running (but could
  * be re-launched with another call to <code>startup</code>). 
  * Any objects handed out by running Platform, including
  * Platform runnables obtained via <code>getRunnable</code>,
  * will be permanently invalid. The effects of attempting to invoke
  * methods on invalid objects is undefined.
  * </p>
  * @exception Exception if there were problems shutting down
  */
 public static void shutdown() throws Exception {
 	InternalBootLoader.shutdown();
 }
 /**
  * Launches the Eclipse Platform. The Platform must not be running.
  * <p>
  * The location of the started Platform is defined as follows:
  * <ul>
  * <li>If the <code>location</code> argument is specified, that value is used.  
  * <li>If <code>location</code> is <code>null</code> but <code>args</code> 
  *		contains a <code>-platform &ltlocation&gt</code> pair, then the given value is used.  
  * <li> If neither is specified, <code>System.getProperty("user.dir")</code> is used.
  * </ul>
  * The plug-in path of the started Platform is defined as follows:
  * <ul>
  * <li>If the <code>pluginPathLocation</code> argument is specified, that value is tried.
  * <li>If <code>pluginPathLocation</code> is <code>null</code> but <code>args</code> 
  *		contains a <code>-plugins &ltlocation&gt</code> pair, then the given value is tried.  
  * <li>If neither value is specified or a given location does not exist, 
  * 		the Platform's location is searched.  
  * <li>Finally, the default plug-in path is used.  This value identifies the plug-ins in the 
  *		Platform's install location.
  * </ul>
  * @param pluginPathLocation the URL of the plug-in path; this is where
  *		the Platform is to find the code for plug-ins
  * @param location the location (usually a string path in the local file
  *		file system) for the saved Platform state
  * @param args the array of command-line style argments which are passed
  *		to the platform on initialization.  The arguments which are consumed by the
  * 		Platform's initialization are removed from the arg list.  This modified arg list is
  *		the return value of this method.  
  *	@return the list of <code>args</code> which were supplied but not consumed
  *		by this method.  
  * @exception Exception if there are problems starting the platform
  */
 public static String[] startup(URL pluginPathLocation, String location, String[] args) throws Exception {
 	return InternalBootLoader.startup(pluginPathLocation, location, args);
 }
 }
