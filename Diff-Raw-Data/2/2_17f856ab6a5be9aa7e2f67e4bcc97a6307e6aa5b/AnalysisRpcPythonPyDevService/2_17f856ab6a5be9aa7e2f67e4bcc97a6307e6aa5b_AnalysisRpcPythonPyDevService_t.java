 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.dawnsci.python.rpc;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawb.common.util.eclipse.BundleUtils;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.core.IInterpreterManager;
 import org.python.pydev.core.IPythonNature;
 import org.python.pydev.core.MisconfigurationException;
 import org.python.pydev.core.NotConfiguredInterpreterException;
 import org.python.pydev.core.PythonNatureWithoutProjectException;
 import org.python.pydev.plugin.PydevPlugin;
 import org.python.pydev.plugin.nature.PythonNature;
 import org.python.pydev.runners.SimpleRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;
 
 import com.python.pydev.debug.remote.client_api.PydevRemoteDebuggerServer;
 
 /**
  * Subclass of {@link AnalysisRpcPythonService} that uses PyDev's
  * InterpreterInfos to generate PYTHONPATHs and path to Python executable.
  *
  * TODO Instead of having concrete constructors of AnalysisRpcPythonPyDevService
  * around, this service should be contributed using OSGI and have an
  * associated interface.
  */
 public class AnalysisRpcPythonPyDevService extends AnalysisRpcPythonService {
 	private static final Logger logger = LoggerFactory
 			.getLogger(AnalysisRpcPythonPyDevService.class);
 
 	// TODO should we add bundle dependency on uk.ac.diamond.scisoft.python?
 	private static final String UK_AC_DIAMOND_SCISOFT_PYTHON = "uk.ac.diamond.scisoft.python";
 
 	/**
 	 * Create new service using the default (first listed) Python
 	 * InterpreterInfo.
 	 *
 	 * @param autoConfig
 	 *            if true, prompt user to configure a new Python Interpreter
 	 * @throws NotConfiguredInterpreterException
 	 *             if no interpreters are configured. As autoConfig is a long
 	 *             running process, PyDev runs this asynchronously so this
 	 *             exception is still thrown at this point. This is a
 	 *             recoverable error that should generally be handled in the
 	 *             code, with a user prompt.
 	 * @throws MisconfigurationException
 	 *             if any error occurs in the configuration of the python
 	 *             interpreter. NOTE NotConfiguredInterpreterException is a
 	 *             subclass of MisconfigurationException. This is a recoverable
 	 *             error that should generally be handled in the code, with a
 	 *             user prompt.
 	 * @throws AnalysisRpcException
 	 *             if an error occurs setting up the AnalysisRpc remote Python
 	 *             server or the Java client
 	 */
 	public AnalysisRpcPythonPyDevService(boolean autoConfig)
 			throws MisconfigurationException, AnalysisRpcException {
 		this(getDefaultInfo(autoConfig), null);
 	}
 
 	/**
 	 * Create new service using the named Python InterpreterInfo.
 	 *
 	 * @param interpreterName
 	 *            name of the interpreter to use (as listed in Python
 	 *            Interpreters)
 	 * @throws MisconfigurationException
 	 *             if any error occurs in the configuration of the Python
 	 *             interpreter. This is raised if the interpreterName is not
 	 *             found. This is a recoverable error that should generally be
 	 *             handled in the code, with a user prompt.
 	 * @throws AnalysisRpcException
 	 *             if an error occurs setting up the AnalysisRpc remote Python
 	 *             server or the Java client
 	 */
 	public AnalysisRpcPythonPyDevService(String interpreterName)
 			throws MisconfigurationException, AnalysisRpcException {
 		this(getInfoFromName(interpreterName), null);
 	}
 
 	/**
 	 * Create new service using the Python InterpreterInfo as configured for the
 	 * given project.
 	 *
 	 * @param project
 	 *            project to use for InterpreterInfo. This means that the
 	 *            PYTHONPATH used for the launched Python will match that of the
 	 *            project.
 	 * @throws MisconfigurationException
 	 *             if any error occurs in the configuration of the Python
 	 *             interpreter. This is raised if the project does not have a
 	 *             PythonNature. This is a recoverable error that should
 	 *             generally be handled in the code, with a user prompt.
 	 * @throws AnalysisRpcException
 	 *             if an error occurs setting up the AnalysisRpc remote Python
 	 *             server or the Java client
 	 */
 	public AnalysisRpcPythonPyDevService(IProject project)
 			throws MisconfigurationException, AnalysisRpcException {
 		this(getInfoFromProject(project), project);
 	}
 
 	/**
 	 * Create new service using the Python InterpreterInfo as configured for the
 	 * given project.
 	 *
 	 * @param project
 	 *            project to use for InterpreterInfo. This means that the
 	 *            PYTHONPATH used for the launched Python will match that of the
 	 *            project.
 	 * @throws MisconfigurationException
 	 *             if any error occurs in the configuration of the Python
 	 *             interpreter. This is raised if the project does not have a
 	 *             PythonNature. This is a recoverable error that should
 	 *             generally be handled in the code, with a user prompt.
 	 * @throws AnalysisRpcException
 	 *             if an error occurs setting up the AnalysisRpc remote Python
 	 *             server or the Java client
 	 */
 	public AnalysisRpcPythonPyDevService(IInterpreterInfo interpreter,
 			IProject project) throws AnalysisRpcException {
 		this(getJobUserDescription(interpreter), getPythonExe(interpreter), getEnv(interpreter, project));
 	}
 
 	private AnalysisRpcPythonPyDevService(String jobUserDescription,
 			File pythonExe, Map<String, String> env)
 			throws AnalysisRpcException {
 		super(jobUserDescription, pythonExe, null, env);
 
 		// Default the port in the launched PyDev to this server
 		getClient().setPyDevSetTracePort(getPyDevDebugServerPort());
 	}
 
 	private static IInterpreterInfo getDefaultInfo(boolean autoConfig)
 			throws MisconfigurationException {
 		IInterpreterManager pythonInterpreterManager = PydevPlugin
 				.getPythonInterpreterManager();
 		return pythonInterpreterManager.getDefaultInterpreterInfo(autoConfig);
 	}
 
 	private static IInterpreterInfo getInfoFromName(String interpreterName)
 			throws MisconfigurationException {
 		IInterpreterManager pythonInterpreterManager = PydevPlugin
 				.getPythonInterpreterManager();
 		return pythonInterpreterManager.getInterpreterInfo(interpreterName,
 				new NullProgressMonitor());
 	}
 
 	private static IInterpreterInfo getInfoFromProject(IProject project)
 			throws MisconfigurationException {
 		PythonNature nature = PythonNature.getPythonNature(project);
 		if (nature == null) {
 			throw new MisconfigurationException(
 					"The project does not appear to have a "
 							+ "valid Python Nature, it needs to "
 							+ "be set as a PyDev Project");
 		}
 		IInterpreterInfo info;
 		try {
 			info = nature.getProjectInterpreter();
 		} catch (PythonNatureWithoutProjectException e) {
 			// Simplify the interface of the users of getInfoFromProject.
 			// PythonNatureWithoutProjectException is only thrown from one place
 			// and it probably should be a subclass of MisconfigurationException
 			throw new MisconfigurationException(e.getMessage(), e);
 		}
 		return info;
 	}
 
 	private static String getJobUserDescription(IInterpreterInfo interpreter) {
 		return "Python Service (" + interpreter.getExecutableOrJar() + ")";
 	}
 
 	private static File getPythonExe(IInterpreterInfo interpreter) {
 		return new File(interpreter.getExecutableOrJar());
 	}
 
 	private static Map<String, String> getEnv(IInterpreterInfo interpreter,
 			IProject project) {
 		IPythonNature pythonNature = null;
 		if (project != null) {
 			pythonNature = PythonNature.getPythonNature(project);
 		}
 
 		IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
 
 		String[] envp = null;
 		try {
 			envp = SimpleRunner.getEnvironment(pythonNature, interpreter,
 					manager);
 		} catch (CoreException e) {
 			// Should be unreachable
 			logger.error("exception occurred while setting environemt", e);
 		}
 
 		final Map<String, String> env = new HashMap<String, String>(
 				System.getenv());
 		for (String s : envp) {
 			String kv[] = s.split("=", 2);
 			env.put(kv[0], kv[1]);
 		}
 
 		// To support this flow, we need both Diamond and PyDev's python
 		// paths in the PYTHONPATH. We add the expected ones here.
 		// NOTE: This can be problematic in cases where the user really
 		// wanted a different Diamond or PyDev python path. Therefore we
 		// force the paths in here.
 		// TODO consider if scisoftpath should be added
 		// in AnalysisRpcPythonService instead
 		String path = env.get("PYTHONPATH");
 		if (path == null) {
 			path = "";
 		}
		StringBuilder pythonpath = new StringBuilder(path);
 		if (pythonpath.length() > 0) {
 			pythonpath.append(File.pathSeparator);
 		}
 
 		String pyDevPySrc = getPyDevPySrc();
 		if (pyDevPySrc != null) {
 			pythonpath.append(pyDevPySrc).append(File.pathSeparator);
 		}
 
 		String scisoftpath = getScisoftPath();
 		if (scisoftpath != null) {
 			pythonpath.append(scisoftpath).append(File.pathSeparator);
 			pythonpath.append(scisoftpath + "/src").append(File.pathSeparator);
 		}
 
 		env.put("PYTHONPATH", pythonpath.toString());
 
 		return env;
 	}
 
 	private static String getScisoftPath() {
 		String scisoftpath = null;
 		try {
 			scisoftpath = BundleUtils.getBundleLocation(
 					UK_AC_DIAMOND_SCISOFT_PYTHON).getAbsolutePath();
 		} catch (IOException e) {
 			logger.error(UK_AC_DIAMOND_SCISOFT_PYTHON
 					+ " not available, import of scisoftpy.rpc may fail", e);
 		} catch (NullPointerException e) {
 			logger.error(UK_AC_DIAMOND_SCISOFT_PYTHON
 					+ " not available, import of scisoftpy.rpc may fail", e);
 		}
 		return scisoftpath;
 	}
 
 	private static String getPyDevPySrc() {
 		String pyDevPySrc = null;
 		try {
 			pyDevPySrc = PydevPlugin.getPySrcPath().getAbsolutePath();
 		} catch (CoreException e) {
 			logger.error(
 					"PydevPlugin's Src Path not available, debugging launched Python may not work",
 					e);
 		}
 		return pyDevPySrc;
 	}
 
 	/**
 	 * Start the PyDev Debug Server.
 	 */
 	public static void startPyDevDebugServer() {
 		PydevRemoteDebuggerServer.startServer();
 	}
 
 	/**
 	 * Stop the PyDev Debug Server.
 	 */
 	public static void stopPyDevDebugServer() {
 		PydevRemoteDebuggerServer.stopServer();
 	}
 
 	/**
 	 * Get the PyDev Debug Server Listening Port.
 	 * XXX This method is a reimplementation of:
 	 * DebugPluginPrefsInitializer.getRemoteDebuggerPort
 	 * which suffers from two problems:
 	 * 1) com.python.pydev.debug is not an exported package
 	 * 2) The DebugPluginPrefsInitializer is a preference initializer, but it
 	 *    violates this rule for preferences:
 	 *       Note: Clients should only set default preference values for their own bundle.
 	 * Therefore this method attempts to get the current value set for the port,
 	 * but in the case that the default-default value of 0 is returned (meaning
 	 * preference has not been initialised) we return the default value.
 	 */
 	public static int getPyDevDebugServerPort() {
 		IPreferenceStore store = PydevPlugin.getDefault().getPreferenceStore();
 		// XXX should use DebugPluginPrefsInitializer.PYDEV_REMOTE_DEBUGGER_PORT
 		int port = store.getInt("PYDEV_REMOTE_DEBUGGER_PORT");
 		if (port == 0) {
 			// XXX should use DebugPluginPrefsInitializer.DEFAULT_REMOTE_DEBUGGER_PORT
 			port = 5678;
 		}
 		return port;
 	}
 }
