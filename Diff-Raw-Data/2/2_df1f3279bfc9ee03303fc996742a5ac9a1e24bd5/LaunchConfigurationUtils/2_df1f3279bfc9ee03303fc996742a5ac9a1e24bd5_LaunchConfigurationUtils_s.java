 package org.eclipse.dltk.internal.launching;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 
 public final class LaunchConfigurationUtils {
 
 	/*
 	 * does a class like this exist elsewhere?
 	 */
 
 	public static interface ILaunchConfigDefaultStringProvider {
 		String getDefault();
 	}
 
 	public static interface ILaunchConfigDefaultBooleanProvider {
 		boolean getDefault();
 	}
 
 	/**
 	 * Retrieve a boolean value from a launch configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @param name
 	 *            launch attribute name
 	 * @param defaultValue
 	 *            default value to use if attribute does not exist
 	 * 
 	 * @return boolean value
 	 */
 	public static boolean getBoolean(ILaunchConfiguration configuration,
 			String name, boolean defaultValue) {
 		boolean value = defaultValue;
 		try {
 			value = configuration.getAttribute(name, defaultValue);
 		} catch (CoreException e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 
 		return value;
 	}
 
 	/**
 	 * Returns the project associated with the launch configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * 
 	 * @return project instance associated with the configuration, or
 	 *         <code>null</code> if the project can not be found
 	 */
 	public static IProject getProject(ILaunchConfiguration configuration) {
 		String projectName = getProjectName(configuration);
 
 		IProject project = null;
 		if (projectName != null) {
 			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
 					projectName);
 		}
 
 		return project;
 	}
 
 	/**
 	 * Returns the project name associated with the launch configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * 
 	 * @return project name or <code>null</code> if no project has been
 	 *         associated
 	 */
 	public static String getProjectName(ILaunchConfiguration configuration) {
 		String projectName = null;
 		try {
 			projectName = configuration.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 					(String) null);
 		} catch (CoreException e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 
 		return projectName;
 	}
 
 	/**
 	 * Returns the 'break on first line' setting for the specified launch
 	 * configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * 
 	 * @return <code>true</code> if the option is enabled, <code>false</code>
 	 *         otherwise
 	 */
 	public static boolean isBreakOnFirstLineEnabled(
 			ILaunchConfiguration configuration,
 			ILaunchConfigDefaultBooleanProvider defaultProvider) {
 		return getBoolean(configuration,
 				ScriptLaunchConfigurationConstants.ENABLE_BREAK_ON_FIRST_LINE,
 				defaultProvider.getDefault());
 	}
 
 	/**
 	 * Returns the 'Dbgp logging enabled' setting for the specified launch
 	 * configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * 
 	 * @return <code>true</code> if the option is enabled, <code>false</code>
 	 *         otherwise
 	 */
 	public static boolean isDbgpLoggingEnabled(
 			ILaunchConfiguration configuration) {
 		ILaunchConfigDefaultBooleanProvider provider = new ILaunchConfigDefaultBooleanProvider() {
 			public boolean getDefault() {
 				return false;
 			}
 		};
		return isBreakOnFirstLineEnabled(configuration, provider);
 	}
 
 	public static boolean isDbgpLoggingEnabled(
 			ILaunchConfiguration configuration,
 			ILaunchConfigDefaultBooleanProvider defaultProvider) {
 		return getBoolean(configuration,
 				ScriptLaunchConfigurationConstants.ENABLE_DBGP_LOGGING,
 				defaultProvider.getDefault());
 	}
 
 	/**
 	 * Retrieve a string value from a launch configuration
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @param name
 	 *            launch attribute name
 	 * @param defaultValue
 	 *            default value to use if attribute does not exist
 	 * 
 	 * @return String
 	 */
 	public static String getString(ILaunchConfiguration configuration,
 			String name, String defaultValue) {
 		String value = defaultValue;
 		try {
 			value = configuration.getAttribute(name, defaultValue);
 		} catch (CoreException e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 
 		return value;
 	}
 }
