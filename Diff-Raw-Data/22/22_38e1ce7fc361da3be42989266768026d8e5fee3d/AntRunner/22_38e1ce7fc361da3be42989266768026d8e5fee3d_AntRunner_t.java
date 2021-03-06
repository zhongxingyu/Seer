 package org.eclipse.ant.core;
 
 /**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.*;
 
 import org.eclipse.ant.internal.core.AntClassLoader;
 import org.eclipse.ant.internal.core.IAntCoreConstants;
 import org.eclipse.ant.internal.core.InternalCoreAntMessages;
 import org.eclipse.core.boot.BootLoader;
 import org.eclipse.core.boot.IPlatformRunnable;
 import org.eclipse.core.runtime.*;
 
 /**
 * Entry point for running Ant builds inside Eclipse.
  */
 public class AntRunner implements IPlatformRunnable {
 
 	protected String buildFileLocation = IAntCoreConstants.DEFAULT_BUILD_FILENAME;
 	protected List buildListeners;
 	protected String[] targets;
 	protected Map userProperties;
 	protected int messageOutputLevel = 2; // Project.MSG_INFO
 	protected String buildLoggerClassName;
 	protected String inputHandlerClassName;
 	protected String[] arguments;
 	protected String[] propertyFiles;
 	protected URL[] customClasspath;
 	protected boolean reuseClassLoader= true;
 
 	/**
 	 * Sets the build file location on the file system.
 	 * 
 	 * @param buildFileLocation the file system location of the build file
 	 */
 	public void setBuildFileLocation(String buildFileLocation) {
 		if (buildFileLocation == null) {
 			this.buildFileLocation = IAntCoreConstants.DEFAULT_BUILD_FILENAME;
 		} else {
 			this.buildFileLocation = buildFileLocation;
 		}
 	}
 
 	/**
 	 * Set the message output level.
 	 * <p>
 	 * Valid values are:
 	 * <ul>
 	 * <li><code>org.apache.tools.ant.Project.ERR</code>, 
 	 * <li><code>org.apache.tools.ant.Project.WARN</code>,
 	 * <li><code>org.apache.tools.ant.Project.INFO</code>,
 	 * <li><code>org.apache.tools.ant.Project.VERBOSE</code> or
 	 * <li><code>org.apache.tools.ant.Project.DEBUG</code>
 	 * </ul>
 	 * 
 	 * @param level the message output level
 	 */
 	public void setMessageOutputLevel(int level) {
 		messageOutputLevel = level;
 	}
 
 	/**
 	 * Sets the arguments to be passed to the script (e.g. -Dos=win32 -Dws=win32 -verbose).
 	 * 
 	 * @param arguments the arguments to be passed to the script
 	 */
 	public void setArguments(String arguments) {
 		this.arguments = getArray(arguments);
 	}
 
 	/**
 	 * Helper method to ensure an array is converted into an ArrayList.
 	 */
 	private String[] getArray(String args) {
 		StringBuffer sb = new StringBuffer();
 		boolean waitingForQuote = false;
 		ArrayList result = new ArrayList();
 		for (StringTokenizer tokens = new StringTokenizer(args, ", \"", true); tokens.hasMoreTokens();) { //$NON-NLS-1$
 			String token = tokens.nextToken();
 			if (waitingForQuote) {
 				if (token.equals("\"")) { //$NON-NLS-1$
 					result.add(sb.toString());
 					sb.setLength(0);
 					waitingForQuote = false;
 				} else {
 					sb.append(token);
 				}
 			} else {
 				if (token.equals("\"")) { //$NON-NLS-1$
 					// test if we have something like -Dproperty="value"
 					if (result.size() > 0) {
 						int index = result.size() - 1;
 						String last = (String) result.get(index);
 						if (last.charAt(last.length() - 1) == '=') {
 							result.remove(index);
 							sb.append(last);
 						}
 					}
 					waitingForQuote = true;
 				} else {
 					if (!(token.equals(",") || token.equals(" "))) //$NON-NLS-1$ //$NON-NLS-2$
 						result.add(token);
 				}
 			}
 		}
 		return (String[]) result.toArray(new String[result.size()]);
 	}
 
 	/**
 	 * Sets the arguments to be passed to the script (e.g. -Dos=win32 -Dws=win32 -verbose).
 	 * 
 	 * @param arguments the arguments to be passed to the script
 	 * @since 2.1
 	 */
 	public void setArguments(String[] arguments) {
 		this.arguments = arguments;
 	}
 
 	/**
 	 * Sets the targets and execution order.
 	 * 
 	 * @param executionTargets which targets should be run and in which order
 	 */
 	public void setExecutionTargets(String[] executionTargets) {
 		this.targets = executionTargets;
 	}
 
 	/**
 	 * Adds a build listener. The parameter <code>className</code>
 	 * is the class name of a <code>org.apache.tools.ant.BuildListener</code>
 	 * implementation. The class will be instantiated at runtime and the
 	 * listener will be called on build events
 	 * (<code>org.apache.tools.ant.BuildEvent</code>).
 	 *
 	 * @param className a build listener class name
 	 */
 	public void addBuildListener(String className) {
 		if (className == null) {
 			return;
 		}
 		if (buildListeners == null) {
 			buildListeners = new ArrayList(5);
 		}
 		buildListeners.add(className);
 	}
 
 	/**
 	 * Sets the build logger. The parameter <code>className</code>
 	 * is the class name of a <code>org.apache.tools.ant.BuildLogger</code>
 	 * implementation. The class will be instantiated at runtime and the
 	 * logger will be called on build events
 	 * (<code>org.apache.tools.ant.BuildEvent</code>).  
 	 * Only one build logger is permitted for any build.
 	 * 
 	 *
 	 * @param className a build logger class name
 	 */
 	public void addBuildLogger(String className) {
 		buildLoggerClassName = className;
 	}
 
 	/**
 	 * Adds user-defined properties. Keys and values must be String objects.
 	 * 
 	 * @param properties a Map of user-defined properties
 	 */
 	public void addUserProperties(Map properties) {
 		userProperties = properties;
 	}
 
 	/**
 	 * Returns the build file target information.
 	 * 
 	 * @return an array containing the target information
 	 * 
 	 * @see TargetInfo
 	 * @since 2.1
 	 */
 	public TargetInfo[] getAvailableTargets() throws CoreException {
 		Class classInternalAntRunner= null;
 		Object runner= null;
 		try {
 			ClassLoader loader = AntCorePlugin.getPlugin().getClassLoader();
 			classInternalAntRunner = loader.loadClass("org.eclipse.ant.internal.core.ant.InternalAntRunner"); //$NON-NLS-1$
 			runner = classInternalAntRunner.newInstance();
 			// set build file
 			Method setBuildFileLocation = classInternalAntRunner.getMethod("setBuildFileLocation", new Class[] { String.class }); //$NON-NLS-1$
 			setBuildFileLocation.invoke(runner, new Object[] { buildFileLocation });
 			// get the info for each targets
 			Method getTargets = classInternalAntRunner.getMethod("getTargets", null); //$NON-NLS-1$
 			Object results = getTargets.invoke(runner, null);
 			// get the default target
 			Method getDefault= classInternalAntRunner.getMethod("getDefaultTarget", null); //$NON-NLS-1$
 			String defaultName= (String)getDefault.invoke(runner, null);
 			// collect the info into target objects
 			List infos = (List) results;
 			Iterator iter= infos.iterator();
 			List info;
 			TargetInfo[] targets= new TargetInfo[infos.size()];
 			int i= 0;
 			while (iter.hasNext()) {
 				info= (List)iter.next();
 				targets[i++] = new TargetInfo((String)info.get(0), (String)info.get(1), (String)info.get(2), (String[])info.get(3), info.get(0).equals(defaultName));
 			}
 			return targets;
 		} catch (NoClassDefFoundError e) {
 			problemLoadingClass(e);
 			//not possible to reach this line
 			return new TargetInfo[0];
 		} catch (ClassNotFoundException e) {
 			problemLoadingClass(e);
 			//not possible to reach this line
 			return new TargetInfo[0];
 		} catch (InvocationTargetException e) {
 			handleInvocationTargetException(runner, classInternalAntRunner, e);
 			//not possible to reach this line
 			return new TargetInfo[0];
 		} catch (Exception e) {
 			String message = (e.getMessage() == null) ? InternalCoreAntMessages.getString("AntRunner.Build_Failed._3") : e.getMessage(); //$NON-NLS-1$
 			throw new CoreException(new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_SCRIPT, message, e));
 		}
 	}
 
 	/**
 	 * Runs the build script. If a progress monitor is specified it will
 	 * be available during the script execution as a reference in the
 	 * Ant Project (<code>org.apache.tools.ant.Project.getReferences()</code>).
 	 * A long-running task could, for example, get the monitor during its
 	 * execution and check for cancellation. The key value to retrieve the
 	 * progress monitor instance is <code>AntCorePlugin.ECLIPSE_PROGRESS_MONITOR</code>.
 	 * 
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	public void run(IProgressMonitor monitor) throws CoreException {
 		long startTime = 0;
 		if (IAntCoreConstants.DEBUG_BUILDFILE_TIMING) {
 			startTime = System.currentTimeMillis();
 		}
 		Object runner= null;
 		Class classInternalAntRunner= null;
 		try {
 			ClassLoader loader = getClassLoader();
 			classInternalAntRunner = loader.loadClass("org.eclipse.ant.internal.core.ant.InternalAntRunner"); //$NON-NLS-1$
 			runner = classInternalAntRunner.newInstance();
 			// set build file
 			Method setBuildFileLocation = classInternalAntRunner.getMethod("setBuildFileLocation", new Class[] { String.class }); //$NON-NLS-1$
 			setBuildFileLocation.invoke(runner, new Object[] { buildFileLocation });
 			// add listeners
 			if (buildListeners != null) {
 				Method addBuildListeners = classInternalAntRunner.getMethod("addBuildListeners", new Class[] { List.class }); //$NON-NLS-1$
 				addBuildListeners.invoke(runner, new Object[] { buildListeners });
 			}
 			
 			if (buildLoggerClassName == null) {
 				//indicate that the default logger is not to be used
 				buildLoggerClassName= ""; //$NON-NLS-1$
 			}
 			// add build logger
 			Method addBuildLogger = classInternalAntRunner.getMethod("addBuildLogger", new Class[] { String.class }); //$NON-NLS-1$
 			addBuildLogger.invoke(runner, new Object[] { buildLoggerClassName });
 			
 			if (inputHandlerClassName != null) {	
 				// add the input handler
 				Method setInputHandler = classInternalAntRunner.getMethod("setInputHandler", new Class[] { String.class }); //$NON-NLS-1$
 				setInputHandler.invoke(runner, new Object[] { inputHandlerClassName });
 			}
 			
 			// add progress monitor
 			if (monitor != null) {
 				Method setProgressMonitor = classInternalAntRunner.getMethod("setProgressMonitor", new Class[] { IProgressMonitor.class }); //$NON-NLS-1$
 				setProgressMonitor.invoke(runner, new Object[] { monitor });
 			}
 			// add properties
 			if (userProperties != null) {
 				Method addUserProperties = classInternalAntRunner.getMethod("addUserProperties", new Class[] { Map.class }); //$NON-NLS-1$
 				addUserProperties.invoke(runner, new Object[] { userProperties });
 			}
 			
 			// add property files
 			if (propertyFiles != null) {
 				Method addPropertyFiles = classInternalAntRunner.getMethod("addPropertyFiles", new Class[] { String[].class }); //$NON-NLS-1$
 				addPropertyFiles.invoke(runner, new Object[] { propertyFiles });
 			}
 			
 			// set message output level
 			Method setMessageOutputLevel = classInternalAntRunner.getMethod("setMessageOutputLevel", new Class[] { int.class }); //$NON-NLS-1$
 			setMessageOutputLevel.invoke(runner, new Object[] { new Integer(messageOutputLevel)});
 			// set execution targets
 			if (targets != null) {
 				Method setExecutionTargets = classInternalAntRunner.getMethod("setExecutionTargets", new Class[] { String[].class }); //$NON-NLS-1$
 				setExecutionTargets.invoke(runner, new Object[] { targets });
 			} 
 			// set extra arguments
 			if (arguments != null && arguments.length > 0) {
 				Method setArguments = classInternalAntRunner.getMethod("setArguments", new Class[] { String[].class }); //$NON-NLS-1$
 				setArguments.invoke(runner, new Object[] { arguments });
 			}
 			// run
 			Method run = classInternalAntRunner.getMethod("run", null); //$NON-NLS-1$
 			run.invoke(runner, null);
 		} catch (NoClassDefFoundError e) {
 			problemLoadingClass(e);
 		} catch (ClassNotFoundException e) {
 			problemLoadingClass(e);
 		} catch (InvocationTargetException e) {
 			handleInvocationTargetException(runner, classInternalAntRunner, e);
 		} catch (Exception e) {
 			String message = (e.getMessage() == null) ? InternalCoreAntMessages.getString("AntRunner.Build_Failed._3") : e.getMessage(); //$NON-NLS-1$
 			IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_SCRIPT, message, e);
 			throw new CoreException(status);
 		} finally {
 			if (IAntCoreConstants.DEBUG_BUILDFILE_TIMING) {
 				long finishTime = System.currentTimeMillis();
 				System.out.println(InternalCoreAntMessages.getString("AntRunner.Buildfile_run_took___9") + (finishTime - startTime) + InternalCoreAntMessages.getString("AntRunner._milliseconds._10")); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	/**
 	 * Handles exceptions that are loaded by the Ant Class Loader by
 	 * asking the Internal Ant Runner class for the correct error message.
 	 * 
 	 * Handles nested NoClassDefFoundError and nested ClassNotFoundException	 */
 	protected void handleInvocationTargetException(Object runner, Class classInternalAntRunner, InvocationTargetException e) throws CoreException {
 		Throwable realException = e.getTargetException();
 		String message= null;
 		if (runner != null) {
 			try {
 				Method getBuildErrorMessage = classInternalAntRunner.getMethod("getBuildExceptionErrorMessage", new Class[] { Throwable.class }); //$NON-NLS-1$
 				message= (String)getBuildErrorMessage.invoke(runner, new Object[] { realException });
 			} catch (Exception ex) {
 				//do nothing as already in error state
 			}
 		}
 		// J9 throws NoClassDefFoundError nested in a InvocationTargetException
 		if (message == null && ((realException instanceof NoClassDefFoundError) || (realException instanceof ClassNotFoundException))) {
 			problemLoadingClass(realException);
 			return;
 		}
 		boolean internalError= false;
 		if (message == null) {
 			//error did not result from a BuildException
 			internalError= true;
 			message = (realException.getMessage() == null) ? InternalCoreAntMessages.getString("AntRunner.Build_Failed._3") : realException.getMessage(); //$NON-NLS-1$
 		}
 		IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_SCRIPT, message, realException);
 		if (internalError) {
 			AntCorePlugin.getPlugin().getLog().log(status);
 		}
 		throw new CoreException(status);
 	}
 
 	protected void problemLoadingClass(Throwable e) throws CoreException {
 		String missingClassName= e.getMessage();
 		String message;
 		if (missingClassName != null) {
 			missingClassName= missingClassName.replace('/', '.');
 			message= InternalCoreAntMessages.getString("AntRunner.Could_not_find_one_or_more_classes._Please_check_the_Ant_classpath._2"); //$NON-NLS-1$
 			message= MessageFormat.format(message, new String[]{missingClassName});
 		} else {
 			message= InternalCoreAntMessages.getString("AntRunner.Could_not_find_one_or_more_classes._Please_check_the_Ant_classpath._1"); //$NON-NLS-1$
 		}
 		IStatus status= new Status(IStatus.ERROR, AntCorePlugin.PI_ANTCORE, AntCorePlugin.ERROR_RUNNING_SCRIPT, message, e);
 		AntCorePlugin.getPlugin().getLog().log(status);
 		throw new CoreException(status);
 	}
 
 	/**
 	 * Runs the build script.
 	 */
 	public void run() throws CoreException {
 		run((IProgressMonitor) null);
 	}
 
 	/**
 	 * Invokes the building of a project object and executes a build using either a given
 	 * target or the default target. This method is called when running Eclipse headless
 	 * and specifying <code>org.eclipse.ant.core.antRunner</code> as the application.
 	 *
 	 * @param argArray the command line arguments
 	 * @exception Exception if a problem occurred during the script execution
 	 */
 	public Object run(Object argArray) throws Exception {
 		// Add debug information if necessary - fix for bug 5672.
 		// Since the platform parses the -debug command line arg
 		// and removes it from the args passed to the applications,
 		// we have to check if Eclipse is in debug mode in order to
 		// forward the -debug argument to Ant.
 		if (BootLoader.inDebugMode()) {
 			String[] args = (String[]) argArray;
 			String[] newArgs = new String[args.length + 1];
 			for (int i = 0; i < args.length; i++) {
 				newArgs[i] = args[i];
 			}
 			newArgs[args.length] = "-debug"; //$NON-NLS-1$
 			argArray = newArgs;
 		}
 		ClassLoader loader = getClassLoader();
 		Class classInternalAntRunner = loader.loadClass("org.eclipse.ant.internal.core.ant.InternalAntRunner"); //$NON-NLS-1$
 		Object runner = classInternalAntRunner.newInstance();
 		Method run = classInternalAntRunner.getMethod("run", new Class[] { Object.class }); //$NON-NLS-1$
 		run.invoke(runner, new Object[] { argArray });
 		return null;
 	}
 	
 	private ClassLoader getClassLoader() {
 		if (customClasspath == null) {
 			if (reuseClassLoader) {
 				return AntCorePlugin.getPlugin().getClassLoader();
 			} else {
 				return AntCorePlugin.getPlugin().getNewClassLoader();
 			}
 		} else {
 			AntCorePreferences preferences = AntCorePlugin.getPlugin().getPreferences();
 			List fullClasspath= new ArrayList();
 			fullClasspath.addAll(Arrays.asList(preferences.getDefaultURLs()));
 			fullClasspath.addAll(Arrays.asList(customClasspath));
 			return new AntClassLoader((URL[])fullClasspath.toArray(new URL[fullClasspath.size()]), preferences.getPluginClassLoaders());
 		}
 	}
 	
 	/**
 	 * Sets the input handler. The parameter <code>className</code>
 	 * is the class name of a <code>org.apache.tools.ant.input.InputHandler</code>
 	 * implementation. The class will be instantiated at runtime and the
 	 * input handler will be used to respond to <input> requests
 	 * Only one input handler is permitted for any build.
 	 * 
 	 * @param className an input handler class name
 	 */
 	public void setInputHandler(String className) {
 		inputHandlerClassName= className;
 	}
 	
 	/**
 	 * Sets the user specified property files.
 	 * @param array of property file paths
 	 * @since 2.1
 	 */
 	public void setPropertyFiles(String[] propertyFiles) {
 		this.propertyFiles= propertyFiles;
 	}
 
 	/**
 	 * Sets the custom classpath to use for this build
 	 * @param array of URLs that define the custom classpath
 	 */
 	public void setCustomClasspath(URL[] customClasspath) {
 		this.customClasspath = customClasspath;
 	}
 
 	/**
 	 * Sets whether or not to reuse the cached classloader for this build
 	 */
 	public void setReuseClassLoader(boolean reuseClassLoader) {
 		this.reuseClassLoader = reuseClassLoader;
 	}
 }
