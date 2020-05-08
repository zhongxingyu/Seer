 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.IStatusHandler;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IScriptModelMarker;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.InterpreterRuntimeBuildpathEntryResolver;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 
 import com.ibm.icu.text.MessageFormat;
 
 /**
  * Abstract implementation of a Script launch configuration delegate. Provides
  * convenience methods for accessing and verifying launch configuration
  * attributes.
  * <p>
  * Clients implementing Script launch configuration delegates should subclass
  * this class.
  * </p>
  */
 public abstract class AbstractScriptLaunchConfigurationDelegate extends
 		LaunchConfigurationDelegate {
 
 	/**
 	 * A list of prerequisite projects ordered by their build order.
 	 */
 	private IProject[] fOrderedProjects;
 
 	/**
 	 * Convenience method to get the launch manager.
 	 * 
 	 * @return the launch manager
 	 */
 	protected ILaunchManager getLaunchManager() {
 		return DebugPlugin.getDefault().getLaunchManager();
 	}
 
 	/**
 	 * Throws a core exception with an error status object built from the given
 	 * message, lower level exception, and error code.
 	 * 
 	 * @param message
 	 *            the status message
 	 * @param exception
 	 *            lower level exception associated with the error, or
 	 *            <code>null</code> if none
 	 * @param code
 	 *            error code
 	 * @throws CoreException
 	 *             the "abort" core exception
 	 */
 	protected void abort(String message, Throwable exception, int code)
 			throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR,
 				DLTKLaunchingPlugin.PLUGIN_ID, code, message, exception));
 	}
 
 	protected void abort(String message, Throwable exception)
 			throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR,
 				DLTKLaunchingPlugin.PLUGIN_ID,
 				ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message,
 				exception));
 	}
 
 	/**
 	 * Returns the Interpreter install specified by the given launch
 	 * configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Interpreter install specified by the given launch
 	 *         configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public IInterpreterInstall getInterpreterInstall(
 			ILaunchConfiguration configuration) throws CoreException {
 		return ScriptRuntime.computeInterpreterInstall(configuration);
 	}
 
 	/**
 	 * Verifies the Interpreter install specified by the given launch
 	 * configuration exists and returns the Interpreter install.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Interpreter install specified by the given launch
 	 *         configuration
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute, the attribute is
 	 *                unspecified, or if the home location is unspecified or
 	 *                does not exist
 	 */
 	public IInterpreterInstall verifyInterpreterInstall(
 			ILaunchConfiguration configuration) throws CoreException {
 		IInterpreterInstall interpreter = getInterpreterInstall(configuration);
 		if (interpreter == null) {
 			abort(
 					LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_The_specified_InterpreterEnvironment_installation_does_not_exist_4,
 					null,
 					ScriptLaunchConfigurationConstants.ERR_INTERPRETER_INSTALL_DOES_NOT_EXIST);
 
 		}
 		File location = interpreter.getInstallLocation();
 		if (location == null) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_InterpreterEnvironment_home_directory_not_specified_for__0__5,
 									new String[] { interpreter.getName() }),
 					null,
 					ScriptLaunchConfigurationConstants.ERR_INTERPRETER_INSTALL_DOES_NOT_EXIST);
 		}
 		if (!location.exists()) {
 			abort(
 					MessageFormat
 							.format(
 									LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_InterpreterEnvironment_home_directory_for__0__does_not_exist___1__6,
 									new String[] { interpreter.getName(),
 											location.getAbsolutePath() }),
 					null,
 					ScriptLaunchConfigurationConstants.ERR_INTERPRETER_INSTALL_DOES_NOT_EXIST);
 		}
 		return interpreter;
 	}
 
 	/**
 	 * Returns the Interpreter connector identifier specified by the given
 	 * launch configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Interpreter connector identifier specified by the given
 	 *         launch configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public String getDebugConnectorId(ILaunchConfiguration configuration)
 			throws CoreException {
 		return configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_DEBUG_CONNECTOR,
 				(String) null);
 	}
 
 	/**
 	 * Returns the entries that should appear on the user portion of the
 	 * buildpath as specified by the given launch configuration, as an array of
 	 * resolved strings. The returned array is empty if no buildpath is
 	 * specified.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the buildpath specified by the given launch configuration,
 	 *         possibly an empty array
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public String[] getBuildpath(ILaunchConfiguration configuration)
 			throws CoreException {
 
 		// Get entries
 		IRuntimeBuildpathEntry[] entries = ScriptRuntime
 				.computeUnresolvedRuntimeBuildpath(configuration);
 		entries = ScriptRuntime.resolveRuntimeBuildpath(entries, configuration);
 
 		// Get USER_ENTRY
 		Set userEntries = new HashSet();
 		for (int i = 0; i < entries.length; i++) {
 			if (entries[i].getBuildpathProperty() == IRuntimeBuildpathEntry.USER_ENTRY) {
 				userEntries.add(entries[i].getLocation());
 			}
 		}
 		return (String[]) userEntries.toArray(new String[userEntries.size()]);
 	}
 
 	/**
 	 * Returns entries that should appear on the bootstrap portion of the
 	 * buildpath as specified by the given launch configuration, as an array of
 	 * resolved strings. The returned array is <code>null</code> if all
 	 * entries are standard (i.e. appear by default), or empty to represent an
 	 * empty bootpath.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the bootpath specified by the given launch configuration. An
 	 *         empty bootpath is specified by an empty array, and
 	 *         <code>null</code> represents a default bootpath.
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public String[] getBootpath(ILaunchConfiguration configuration)
 			throws CoreException {
 		String[][] paths = getBootpathExt(configuration);
 		String[] pre = paths[0];
 		String[] main = paths[1];
 		String[] app = paths[2];
 		if (pre == null && main == null && app == null) {
 			// default
 			return null;
 		}
 		IRuntimeBuildpathEntry[] entries = ScriptRuntime
 				.computeUnresolvedRuntimeBuildpath(configuration);
 		entries = ScriptRuntime.resolveRuntimeBuildpath(entries, configuration);
 		List bootEntries = new ArrayList(entries.length);
 		boolean empty = true;
 		boolean allStandard = true;
 		for (int i = 0; i < entries.length; i++) {
 			if (entries[i].getBuildpathProperty() != IRuntimeBuildpathEntry.USER_ENTRY) {
 				String location = entries[i].getLocation();
 				if (location != null) {
 					empty = false;
 					bootEntries.add(location);
 					allStandard = allStandard
 							&& entries[i].getBuildpathProperty() == IRuntimeBuildpathEntry.STANDARD_ENTRY;
 				}
 			}
 		}
 
 		if (empty) {
 			return new String[0];
 		} else if (allStandard) {
 			return null;
 		} else {
 			return (String[]) bootEntries
 					.toArray(new String[bootEntries.size()]);
 		}
 	}
 
 	/**
 	 * Returns three sets of entries which represent the boot buildpath
 	 * specified in the launch configuration, as an array of three arrays of
 	 * resolved strings. The first array represents the buildpath that should be
 	 * prepended to the boot buildpath. The second array represents the main
 	 * part of the boot buildpath -<code>null</code> represents the default
 	 * bootbuildpath. The third array represents the buildpath that should be
 	 * appended to the boot buildpath.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return a description of the boot buildpath specified by the given launch
 	 *         configuration.
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 * 
 	 */
 	public String[][] getBootpathExt(ILaunchConfiguration configuration)
 			throws CoreException {
 		String[][] bootpathInfo = new String[3][];
 		IRuntimeBuildpathEntry[] entries = ScriptRuntime
 				.computeUnresolvedRuntimeBuildpath(configuration);
 		List bootEntriesPrepend = new ArrayList();
 		int index = 0;
 		IRuntimeBuildpathEntry InterpreterEnvironmentEntry = null;
 		while (InterpreterEnvironmentEntry == null && index < entries.length) {
 			IRuntimeBuildpathEntry entry = entries[index++];
 			if (entry.getBuildpathProperty() == IRuntimeBuildpathEntry.BOOTSTRAP_ENTRY
 					|| entry.getBuildpathProperty() == IRuntimeBuildpathEntry.STANDARD_ENTRY) {
 				if (ScriptRuntime.isInterpreterInstallReference(
 						getLanguageId(), entry)) {
 					InterpreterEnvironmentEntry = entry;
 				} else {
 					bootEntriesPrepend.add(entry);
 				}
 			}
 		}
 		IRuntimeBuildpathEntry[] bootEntriesPrep = ScriptRuntime
 				.resolveRuntimeBuildpath(
 						(IRuntimeBuildpathEntry[]) bootEntriesPrepend
 								.toArray(new IRuntimeBuildpathEntry[bootEntriesPrepend
 										.size()]), configuration);
 		String[] entriesPrep = null;
 		if (bootEntriesPrep.length > 0) {
 			entriesPrep = new String[bootEntriesPrep.length];
 			for (int i = 0; i < bootEntriesPrep.length; i++) {
 				entriesPrep[i] = bootEntriesPrep[i].getLocation();
 			}
 		}
 		if (InterpreterEnvironmentEntry != null) {
 			List bootEntriesAppend = new ArrayList();
 			for (; index < entries.length; index++) {
 				IRuntimeBuildpathEntry entry = entries[index];
 				if (entry.getBuildpathProperty() == IRuntimeBuildpathEntry.BOOTSTRAP_ENTRY) {
 					bootEntriesAppend.add(entry);
 				}
 			}
 			bootpathInfo[0] = entriesPrep;
 			IRuntimeBuildpathEntry[] bootEntriesApp = ScriptRuntime
 					.resolveRuntimeBuildpath(
 							(IRuntimeBuildpathEntry[]) bootEntriesAppend
 									.toArray(new IRuntimeBuildpathEntry[bootEntriesAppend
 											.size()]), configuration);
 			if (bootEntriesApp.length > 0) {
 				bootpathInfo[2] = new String[bootEntriesApp.length];
 				for (int i = 0; i < bootEntriesApp.length; i++) {
 					bootpathInfo[2][i] = bootEntriesApp[i].getLocation();
 				}
 			}
 			IInterpreterInstall install = getInterpreterInstall(configuration);
 			LibraryLocation[] libraryLocations = install.getLibraryLocations();
 			if (libraryLocations != null) {
 				// determine if explicit bootpath should be used
 				// TODO: this test does not tell us if the bootpath entries are
 				// different (could still be
 				// the same, as a non-bootpath entry on the
 				// InterpreterEnvironment may have been removed/added)
 				// We really need a way to ask a Interpreter type for its
 				// default bootpath library locations and
 				// compare that to the resolved entries for the
 				// "InterpreterEnvironmentEntry" to see if they
 				// are different (requires explicit bootpath)
 				if (!InterpreterRuntimeBuildpathEntryResolver.isSameArchives(
 						libraryLocations, install.getInterpreterInstallType()
 								.getDefaultLibraryLocations(
 										install.getInstallLocation()))) {
 					// resolve bootpath entries in InterpreterEnvironment entry
 					IRuntimeBuildpathEntry[] bootEntries = null;
 					if (InterpreterEnvironmentEntry.getType() == IRuntimeBuildpathEntry.CONTAINER) {
 						IRuntimeBuildpathEntry bootEntry = ScriptRuntime
 								.newRuntimeContainerBuildpathEntry(
 										InterpreterEnvironmentEntry.getPath(),
 										IRuntimeBuildpathEntry.BOOTSTRAP_ENTRY,
 										getScriptProject(configuration));
 						bootEntries = ScriptRuntime
 								.resolveRuntimeBuildpathEntry(bootEntry,
 										configuration);
 					} else {
 						bootEntries = ScriptRuntime
 								.resolveRuntimeBuildpathEntry(
 										InterpreterEnvironmentEntry,
 										configuration);
 					}
 
 					// non-default InterpreterEnvironment libraries - use
 					// explicit bootpath only
 					String[] bootpath = new String[bootEntriesPrep.length
 							+ bootEntries.length + bootEntriesApp.length];
 					if (bootEntriesPrep.length > 0) {
 						System.arraycopy(bootpathInfo[0], 0, bootpath, 0,
 								bootEntriesPrep.length);
 					}
 					int dest = bootEntriesPrep.length;
 					for (int i = 0; i < bootEntries.length; i++) {
 						bootpath[dest] = bootEntries[i].getLocation();
 						dest++;
 					}
 					if (bootEntriesApp.length > 0) {
 						System.arraycopy(bootpathInfo[2], 0, bootpath, dest,
 								bootEntriesApp.length);
 					}
 					bootpathInfo[0] = null;
 					bootpathInfo[1] = bootpath;
 					bootpathInfo[2] = null;
 				}
 			}
 		} else {
 			if (entriesPrep == null) {
 				bootpathInfo[1] = new String[0];
 			} else {
 				bootpathInfo[1] = entriesPrep;
 			}
 		}
 		return bootpathInfo;
 	}
 
 	/**
 	 * Returns the Script project specified by the given launch configuration,
 	 * or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Script project specified by the given launch configuration,
 	 *         or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public static IScriptProject getScriptProject(
 			ILaunchConfiguration configuration) throws CoreException {
 		String projectName = getScriptProjectName(configuration);
 		if (projectName != null) {
 			projectName = projectName.trim();
 			if (projectName.length() > 0) {
 				IProject project = ResourcesPlugin.getWorkspace().getRoot()
 						.getProject(projectName);
 				IScriptProject scriptProject = DLTKCore.create(project);
 				if (scriptProject != null && scriptProject.exists()) {
 					return scriptProject;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the Script project name specified by the given launch
 	 * configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Script project name specified by the given launch
 	 *         configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public static String getScriptProjectName(ILaunchConfiguration configuration)
 			throws CoreException {
 		return configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 				(String) null);
 	}
 
 	/**
 	 * Returns the main type name specified by the given launch configuration,
 	 * or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the main type name specified by the given launch configuration,
 	 *         or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
	public static String getMainScriptName(ILaunchConfiguration configuration)
 			throws CoreException {
 		String script = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 				(String) null);
 		if (script == null) {
 			return null;
 		}
 		return VariablesPlugin.getDefault().getStringVariableManager()
 				.performStringSubstitution(script);
 	}
 
 	/**
 	 * Returns the program arguments specified by the given launch
 	 * configuration, as a string. The returned string is empty if no program
 	 * arguments are specified.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the program arguments specified by the given launch
 	 *         configuration, possibly an empty string
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public String[] getScriptArguments(ILaunchConfiguration configuration)
 			throws CoreException {
 		String arguments = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SCRIPT_ARGUMENTS, ""); //$NON-NLS-1$
 		String args = VariablesPlugin.getDefault().getStringVariableManager()
 				.performStringSubstitution(arguments);
 
 		return DebugPlugin.parseArguments(args);
 	}
 
 	/**
 	 * Returns the Interpreter arguments specified by the given launch
 	 * configuration, as a string. The returned string is empty if no
 	 * Interpreter arguments are specified.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the Interpreter arguments specified by the given launch
 	 *         configuration, possibly an empty string
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	protected final String[] getInterpreterArguments(
 			ILaunchConfiguration configuration) throws CoreException {
 		String arguments = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_ARGUMENTS,
 				""); //$NON-NLS-1$
 
 		String args = VariablesPlugin.getDefault().getStringVariableManager()
 				.performStringSubstitution(arguments);
 
 		return DebugPlugin.parseArguments(args);
 	}
 
 	/**
 	 * Returns the Map of Interpreter-specific attributes specified by the given
 	 * launch configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the <code>Map</code> of Interpreter-specific attributes
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public Map getInterpreterSpecificAttributesMap(
 			ILaunchConfiguration configuration) throws CoreException {
 		Map map = configuration
 				.getAttribute(
 						ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
 						(Map) null);
 		return map;
 	}
 
 	/**
 	 * Returns the working directory specified by the given launch
 	 * configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the working directory specified by the given launch
 	 *         configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public File getWorkingDirectory(ILaunchConfiguration configuration)
 			throws CoreException {
 		return verifyWorkingDirectory(configuration);
 	}
 
 	/**
 	 * Returns the working directory path specified by the given launch
 	 * configuration, or <code>null</code> if none.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the working directory path specified by the given launch
 	 *         configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public IPath getWorkingDirectoryPath(ILaunchConfiguration configuration)
 			throws CoreException {
 		String path = configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
 				(String) null);
 		if (path != null) {
 			path = VariablesPlugin.getDefault().getStringVariableManager()
 					.performStringSubstitution(path);
 			return new Path(path);
 		}
 		return null;
 	}
 
 	/**
 	 * Verifies the working directory specified by the given launch
 	 * configuration exists, and returns the working directory, or
 	 * <code>null</code> if none is specified.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the working directory specified by the given launch
 	 *         configuration, or <code>null</code> if none
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute
 	 */
 	public File verifyWorkingDirectory(ILaunchConfiguration configuration)
 			throws CoreException {
 		IPath path = getWorkingDirectoryPath(configuration);
 		if (path == null) {
 			File dir = getDefaultWorkingDirectory(configuration);
 			if (dir != null) {
 				if (!dir.isDirectory()) {
 					abort(
 							MessageFormat
 									.format(
 											LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12,
 											new String[] { dir.toString() }),
 							null,
 							ScriptLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
 				}
 				return dir;
 			}
 		} else {
 			if (path.isAbsolute()) {
 				File dir = new File(path.toOSString());
 				if (dir.isDirectory()) {
 					return dir;
 				}
 				// This may be a workspace relative path returned by a variable.
 				// However variable paths start with a slash and thus are
 				// thought to
 				// be absolute
 				IResource res = ResourcesPlugin.getWorkspace().getRoot()
 						.findMember(path);
 				if (res instanceof IContainer && res.exists()) {
 					return res.getLocation().toFile();
 				}
 				abort(
 						MessageFormat
 								.format(
 										LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12,
 										new String[] { path.toString() }),
 						null,
 						ScriptLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
 			} else {
 				IResource res = ResourcesPlugin.getWorkspace().getRoot()
 						.findMember(path);
 				if (res instanceof IContainer && res.exists()) {
 					return res.getLocation().toFile();
 				}
 				abort(
 						MessageFormat
 								.format(
 										LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_Working_directory_does_not_exist___0__12,
 										new String[] { path.toString() }),
 						null,
 						ScriptLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Verifies a main script name is specified by the given launch
 	 * configuration, and returns the script type name.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @return the main type name specified by the given launch configuration
 	 * @exception CoreException
 	 *                if unable to retrieve the attribute or the attribute is
 	 *                unspecified
 	 */
 	public String verifyMainScriptName(ILaunchConfiguration configuration)
 			throws CoreException {
 		final String name = getMainScriptName(configuration);
 		if (name == null) {
 			abort(
 					LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_Main_type_not_specified_11,
 					null,
 					ScriptLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_SCRIPT);
 		}
 		return name;
 	}
 
 	// Project path + script path
 	protected String getScriptLaunchPath(ILaunchConfiguration configuration)
 			throws CoreException {
 		String mainScriptName = verifyMainScriptName(configuration);
 		IProject project = getScriptProject(configuration).getProject();
 		IPath location = project.getLocation();
 		if( location == null ) {
 			return null;
 		}
 		return location.append(mainScriptName).toPortableString();
 	}
 
 	// Should be overriden in for any language
 	protected InterpreterConfig createInterpreterConfig(
 			ILaunchConfiguration configuration, ILaunch launch)
 			throws CoreException {
 
 		// Validation already included
 		String scriptLaunchPath = getScriptLaunchPath(configuration);
 		if( scriptLaunchPath == null ) {
 			return null;
 		}
 		final IPath mainScript = new Path(scriptLaunchPath);
 		final IPath workingDirectory = new Path(getWorkingDirectory(
 				configuration).getAbsolutePath());
 
 		InterpreterConfig config = new InterpreterConfig(mainScript,
 				workingDirectory);
 
 		// Script arguments
 		String[] scriptArgs = getScriptArguments(configuration);
 		config.addScriptArgs(scriptArgs);
 
 		// Interpreter argument
 		String[] interpreterArgs = getInterpreterArguments(configuration);
 		config.addInterpreterArgs(interpreterArgs);
 
 		// Environment
 		// config.addEnvVars(DebugPlugin.getDefault().getLaunchManager()
 		// .getNativeEnvironmentCasePreserved());
 		Map configEnv = configuration.getAttribute(
 				ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap());
 		// build base environment
 		Map env = DebugPlugin.getDefault().getLaunchManager()
 				.getNativeEnvironmentCasePreserved();
 		boolean append = configuration.getAttribute(
 				ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
 		if (configEnv != null) {
 			for (Iterator iterator = configEnv.keySet().iterator(); iterator
 					.hasNext();) {
 				String name = (String) iterator.next();
 				if (!env.containsKey(name) || !append) {
 					env.put(name, configEnv.get(name));
 				}
 			}
 		}
 		config.addEnvVars(env);
 
 		return config;
 	}
 
 	protected void validateLaunchConfiguration(
 			ILaunchConfiguration configuration, String mode)
 			throws CoreException {
 
 		// Validation of available debugging engine
 		if (ILaunchManager.DEBUG_MODE.equals(mode)) {
 
 			if (!DebuggingEngineManager.getInstance()
 					.hasSelectedDebuggingEngine(getNatureId(configuration))) {
 				abort(
 						"Debugging engine not selected.",
 						null,
 						ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_DEBUGGING_ENGINE);
 			}
 		}
 	}
 
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		try {
 			if (monitor == null) {
 				monitor = new NullProgressMonitor();
 			}
 
 			monitor.beginTask(MessageFormat.format(
 					"Starting launch configuration {0}...",
 					new Object[] { configuration.getName() }), 10);
 			if (monitor.isCanceled()) {
 				return;
 			}
 
 			monitor.subTask(MessageFormat.format(
 					"Validating launch configuration {0}...",
 					new Object[] { configuration.getName() }));
 			validateLaunchConfiguration(configuration, mode);
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return;
 			}
 
 			// Getting InterpreterConfig
 			monitor.subTask("Generating interpreter config...");
 			final InterpreterConfig config = createInterpreterConfig(
 					configuration, launch);
 			if(config == null ) {
 				monitor.setCanceled(true);
 				return;
 			}
 			if (monitor.isCanceled()) {
 				return;
 			}
 			monitor.worked(1);
 
 			// Getting IInterpreterRunner
 			monitor.subTask("Getting interpreter runner...");
 			final IInterpreterRunner runner = getInterpreterRunner(
 					configuration, mode);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			monitor.worked(1);
 
 			// Real run
 			monitor.subTask("Executing runner...");
 			runRunner(configuration, runner, config, launch,
 					new SubProgressMonitor(monitor, 7));
 
 		} catch (CoreException e) {
 			tryHandleStatus(e, this);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	protected static void tryHandleStatus(CoreException e, Object source)
 			throws CoreException {
 		final IStatus status = e.getStatus();
 
 		final IStatusHandler handler = DebugPlugin.getDefault()
 				.getStatusHandler(status);
 
 		if (handler == null) {
 			throw e;
 		}
 
 		handler.handleStatus(status, source);
 	}
 
 	protected void runRunner(ILaunchConfiguration configuration,
 			IInterpreterRunner runner, InterpreterConfig config,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		try {
 			runner.run(config, launch, monitor);
 		} catch (CoreException e) {
 			tryHandleStatus(e, runner);
 		}
 	}
 
 	protected String getWorkingDir(ILaunchConfiguration configuration)
 			throws CoreException {
 		File workingDir = verifyWorkingDirectory(configuration);
 		String workingDirName = null;
 		if (workingDir != null) {
 			workingDirName = workingDir.getAbsolutePath();
 		}
 		return workingDirName;
 	}
 
 	protected IPath[] createBuildPath(ILaunchConfiguration configuration)
 			throws CoreException {
 		List paths = new ArrayList();
 
 		// Buildpath
 		String[] buildpath = getBuildpath(configuration);
 		for (int i = 0; i < buildpath.length; i++) {
 			paths.add(new Path(buildpath[i]));
 		}
 
 		// Bootpath
 		String[] bootpath = getBootpath(configuration);
 		if (bootpath != null) {
 			// it may be null, if bootpath is standard
 			for (int i = 0; i < bootpath.length; i++) {
 				paths.add(new Path(bootpath[i]));
 			}
 		}
 
 		return (IPath[]) paths.toArray(new IPath[paths.size()]);
 	}
 
 	protected String createNativeBuildPath(IPath[] paths) {
 		// TODO: refactor this
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < paths.length; ++i) {
 			sb.append(paths[i].toOSString());
 			if (i < paths.length - 1) {
 				sb.append(Path.DEVICE_SEPARATOR);
 			}
 		}
 		return sb.toString();
 	}
 
 	protected IProject[] getBuildOrder(ILaunchConfiguration configuration,
 			String mode) throws CoreException {
 		return fOrderedProjects;
 	}
 
 	protected IProject[] getProjectsForProblemSearch(
 			ILaunchConfiguration configuration, String mode)
 			throws CoreException {
 		return fOrderedProjects;
 	}
 
 	protected boolean isLaunchProblem(IMarker problemMarker)
 			throws CoreException {
 		return super.isLaunchProblem(problemMarker)
 				&& problemMarker.getType().equals(
 						IScriptModelMarker.DLTK_MODEL_PROBLEM_MARKER);
 	}
 
 	public boolean preLaunchCheck(ILaunchConfiguration configuration,
 			String mode, IProgressMonitor monitor) throws CoreException {
 
 		if (monitor != null) {
 			monitor
 					.subTask(LaunchingMessages.AbstractScriptLaunchConfigurationDelegate_20);
 		}
 
 		fOrderedProjects = null;
 		IScriptProject scriptProject = ScriptRuntime
 				.getScriptProject(configuration);
 
 		if (scriptProject != null) {
 			fOrderedProjects = computeReferencedBuildOrder(new IProject[] { scriptProject
 					.getProject() });
 		}
 
 		return super.preLaunchCheck(configuration, mode, monitor);
 	}
 
 	protected IBreakpoint[] getBreakpoints(ILaunchConfiguration configuration) {
 		// TODO
 		return new IBreakpoint[] {};
 	}
 
 	/**
 	 * Returns the Interpreter runner for the given launch mode to use when
 	 * launching the given configuration.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @param mode
 	 *            launch node
 	 * @return Interpreter runner to use when launching the given configuration
 	 *         in the given mode
 	 * @throws CoreException
 	 *             if a Interpreter runner cannot be determined
 	 * 
 	 */
 	public IInterpreterRunner getInterpreterRunner(
 			ILaunchConfiguration configuration, String mode)
 			throws CoreException {
 
 		final IInterpreterInstall install = verifyInterpreterInstall(configuration);
 
 		final IInterpreterRunner runner = install.getInterpreterRunner(mode);
 
 		if (runner == null) {
 			abort(
 					MessageFormat.format(
 							LaunchingMessages.InterpreterRunnerDoesntExist,
 							new String[] { install.getName(), mode }),
 					null,
 					ScriptLaunchConfigurationConstants.ERR_INTERPRETER_RUNNER_DOES_NOT_EXIST);
 		}
 
 		return runner;
 	}
 
 	/**
 	 * Returns an array of environment variables to be used when launching the
 	 * given configuration or <code>null</code> if unspecified.
 	 * 
 	 * @param configuration
 	 *            launch configuration
 	 * @throws CoreException
 	 *             if unable to access associated attribute or if unable to
 	 *             resolve a variable in an environment variable's value
 	 * 
 	 */
 	public String[] getEnvironment(ILaunchConfiguration configuration)
 			throws CoreException {
 		return DebugPlugin.getDefault().getLaunchManager().getEnvironment(
 				configuration);
 	}
 
 	/**
 	 * Returns an array of paths to be used for the
 	 * <code>java.library.path</code> system property, or <code>null</code>
 	 * if unspecified.
 	 * 
 	 * @param configuration
 	 * @return an array of paths to be used for the
 	 *         <code>java.library.path</code> system property, or
 	 *         <code>null</code>
 	 * @throws CoreException
 	 *             if unable to determine the attribute
 	 * 
 	 */
 	public String[] getScriptLibraryPath(ILaunchConfiguration configuration)
 			throws CoreException {
 		IScriptProject project = getScriptProject(configuration);
 		if (project != null) {
 			String[] paths = ScriptRuntime.computeScriptLibraryPath(project,
 					true);
 			if (paths.length > 0) {
 				return paths;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the default working directory for the given launch configuration,
 	 * or <code>null</code> if none. Subclasses may override as necessary.
 	 * 
 	 * @param configuration
 	 * @return default working directory or <code>null</code> if none
 	 * @throws CoreException
 	 *             if an exception occurs computing the default working
 	 *             directory
 	 * 
 	 */
 	protected File getDefaultWorkingDirectory(ILaunchConfiguration configuration)
 			throws CoreException {
 		// default working directory is the project if this config has a project
 		IScriptProject jp = getScriptProject(configuration);
 		if (jp != null) {
 			IProject p = jp.getProject();
 			return p.getLocation().toFile();
 		}
 		return null;
 	}
 
 	protected String getNatureId(ILaunchConfiguration configuration)
 			throws CoreException {
 		return configuration.getAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SCRIPT_NATURE,
 				(String) null);
 	}
 
 	abstract public String getLanguageId();
 }
