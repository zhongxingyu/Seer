 package org.eclipse.dltk.launching;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.core.IPreferencesLookupDelegate;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.dbgp.DbgpSessionIdGenerator;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
 import org.eclipse.dltk.debug.core.ExtendedDebugEventDetails;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.ScriptDebugManager;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugThreadConfigurator;
 import org.eclipse.dltk.internal.debug.core.model.DebugEventHelper;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
 import org.eclipse.dltk.launching.debug.DbgpConnectionConfig;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 import org.eclipse.dltk.launching.process.ScriptRuntimeProcessFactory;
 
 public abstract class DebuggingEngineRunner extends AbstractInterpreterRunner {
 	// Launch attributes
 	public static final String LAUNCH_ATTR_DEBUGGING_ENGINE_ID = "debugging_engine_id"; //$NON-NLS-1$
 
 	public static final String OVERRIDE_EXE = "OVERRIDE_EXE"; //$NON-NLS-1$
 
 	protected String getSessionId(ILaunchConfiguration configuration)
 			throws CoreException {
 		return DbgpSessionIdGenerator.generate();
 	}
 
 	@Deprecated
 	protected final IScriptDebugTarget addDebugTarget(ILaunch launch,
 			IDbgpService dbgpService) throws CoreException {
 		return null;
 	}
 
 	protected IScriptDebugTarget createDebugTarget(ILaunch launch,
 			IDbgpService dbgpService) throws CoreException {
 		return new ScriptDebugTarget(getDebugModelId(), dbgpService,
 				getSessionId(launch.getLaunchConfiguration()), launch, null);
 	}
 
 	public DebuggingEngineRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected void initializeLaunch(ILaunch launch, InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
 		final IDbgpService service = DLTKDebugPlugin.getDefault()
 				.getDbgpService();
 
 		if (!service.available()) {
 			abort(InterpreterMessages.errDbgpServiceNotAvailable, null);
 		}
 		final IScriptDebugTarget target = createDebugTarget(launch, service);
 		launch.addDebugTarget(target);
		IScriptDebugThreadConfigurator configurator = createThreadConfigurator(launch
				.getLaunchConfiguration());
 		if (configurator != null) {
 			((ScriptDebugTarget) target)
 					.setScriptDebugThreadConfigurator(configurator);
 		}
 
 		String qualifier = getDebugPreferenceQualifier();
 
 		target.toggleGlobalVariables(delegate.getBoolean(qualifier,
 				showGlobalVarsPreferenceKey()));
 		target.toggleClassVariables(delegate.getBoolean(qualifier,
 				showClassVarsPreferenceKey()));
 		target.toggleLocalVariables(delegate.getBoolean(qualifier,
 				showLocalVarsPreferenceKey()));
 
 		// Debugging engine id
 		launch.setAttribute(LAUNCH_ATTR_DEBUGGING_ENGINE_ID,
 				getDebuggingEngineId());
 
 		// Configuration
 		DbgpConnectionConfig.save(config, getBindAddress(), service.getPort(),
 				target.getSessionId());
 	}
 
 	private String getBindAddress() {
 		return DLTKDebugPlugin.getDefault().getBindAddress();
 	}
 
 	/**
 	 * @see #addEngineConfig(InterpreterConfig,PreferencesLookupDelegate,ILaunch)
 	 */
 	@Deprecated
 	protected final InterpreterConfig addEngineConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) {
 		return null;
 	}
 
 	/**
 	 * Add the debugging engine configuration.
 	 * 
 	 * @param launch
 	 */
 	protected abstract InterpreterConfig addEngineConfig(
 			InterpreterConfig config, PreferencesLookupDelegate delegate,
 			ILaunch launch) throws CoreException;
 
 	@Override
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		monitor.beginTask(InterpreterMessages.DebuggingEngineRunner_launching,
 				4);
 		if (monitor.isCanceled()) {
 			return;
 		}
 		try {
			PreferencesLookupDelegate delegate = createPreferencesLookupDelegate(launch);
 
 			initializeLaunch(launch, config, delegate);
 			final ScriptDebugTarget target = (ScriptDebugTarget) launch
 					.getDebugTarget();
 			final DebugSessionAcceptor acceptor = new DebugSessionAcceptor(
 					target, monitor);
 			try {
 				monitor.worked(1);
 				target.setProcess(startProcess(config, launch, monitor,
 						delegate));
 				monitor.worked(1);
 
 				// Waiting for debugging engine to connect
 				waitDebuggerConnected(launch, acceptor);
 			} finally {
 				acceptor.disposeStatusHandler();
 			}
 		} catch (CoreException e) {
 			launch.terminate();
 			throw e;
 		} finally {
 			monitor.done();
 		}
 		// Happy debugging :)
 	}
 
 	protected IProcess startProcess(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor, PreferencesLookupDelegate delegate)
			throws CoreException {
 		InterpreterConfig newConfig = addEngineConfig(config, delegate, launch);
 
 		// Starting debugging engine
 		IProcess process = null;
 		try {
 			DebugEventHelper.fireExtendedEvent(newConfig,
 					ExtendedDebugEventDetails.BEFORE_VM_STARTED);
 
 			// Running
 			monitor.subTask(InterpreterMessages.DebuggingEngineRunner_running);
 			process = rawRun(launch, newConfig);
 		} catch (CoreException e) {
 			abort(InterpreterMessages.errDebuggingEngineNotStarted, e);
 		}
 		return process;
 	}
 
 	@Override
 	protected String[] renderCommandLine(InterpreterConfig config) {
 		String exe = (String) config.getProperty(OVERRIDE_EXE);
 		if (exe != null) {
 			return config.renderCommandLine(getInstall().getEnvironment(), exe);
 		}
 
 		return config.renderCommandLine(getInstall());
 	}
 
 	/**
 	 * Used to create new script thread configurator.
 	 * 
 	 * @return
 	 */
 	@Deprecated
 	protected final IScriptDebugThreadConfigurator createThreadConfigurator() {
 		return null;
 	}
 
 	/**
 	 * Used to create new script thread configurator.
 	 * 
 	 * @param configuration
 	 */
 	protected IScriptDebugThreadConfigurator createThreadConfigurator(
 			ILaunchConfiguration configuration) {
 		return null;
 	}
 
 	/**
 	 * @param process
 	 * @param launch
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	@Deprecated
 	protected void waitDebuggerConnected(IProcess process, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		ScriptDebugTarget target = (ScriptDebugTarget) launch.getDebugTarget();
		waitDebuggerConnected(launch, new DebugSessionAcceptor(target, monitor));
 	}
 
 	/**
 	 * Waiting debugging process to connect to current launch
 	 * 
 	 * @param launch
 	 *            launch to connect to
 	 * @param acceptor
 	 * @param monitor
 	 *            progress monitor
 	 * @throws CoreException
 	 *             if debuggingProcess terminated, monitor is canceled or // *
 	 *             timeout
 	 */
 	protected void waitDebuggerConnected(ILaunch launch,
 			DebugSessionAcceptor acceptor) throws CoreException {
 
 		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
 		int timeout = LaunchConfigurationUtils.getConnectionTimeout(
 				configuration, DLTKDebugPlugin.getConnectionTimeout());
 		if (!acceptor.waitConnection(timeout)) {
 			launch.terminate();
 			return;
 			// abort(InterpreterMessages.errDebuggingEngineNotConnected, null);
 		}
 		if (!acceptor.waitInitialized(60 * 60 * 1000)) {
 			launch.terminate();
 			abort(InterpreterMessages.errDebuggingEngineNotInitialized, null);
 		}
 	}
 
 	public String getDebugModelId() {
 		return ScriptDebugManager.getInstance().getDebugModelByNature(
 				getInstall().getNatureId());
 	}
 
 	public IDebuggingEngine getDebuggingEngine() {
 		return DebuggingEngineManager.getInstance().getDebuggingEngine(
 				getDebuggingEngineId());
 	}
 
 	protected String showGlobalVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_GLOBAL;
 	}
 
 	protected String showClassVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS;
 	}
 
 	protected String showLocalVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_LOCAL;
 	}
 
 	protected abstract String getDebuggingEngineId();
 
 	protected PreferencesLookupDelegate createPreferencesLookupDelegate(
 			ILaunch launch) throws CoreException {
 		IScriptProject sProject = ScriptRuntime.getScriptProject(launch
 				.getLaunchConfiguration());
		return new PreferencesLookupDelegate(sProject.getProject());
 	}
 
 	/**
 	 * Returns the id of the plugin whose preference store contains general
 	 * debugging preference settings.
 	 */
 	protected abstract String getDebugPreferenceQualifier();
 
 	/**
 	 * Returns the id of the plugin whose preference store contains debugging
 	 * engine preferences.
 	 */
 	protected abstract String getDebuggingEnginePreferenceQualifier();
 
 	/**
 	 * Tests if logging is enabled for this engine.
 	 * 
 	 * <p>
 	 * Note: this method controls logging for the actual debugging engine, and
 	 * not the DBGP protocol output.
 	 * </p>
 	 */
 	protected boolean isLoggingEnabled(IPreferencesLookupDelegate delegate) {
 		return true;
 	}
 
 	/**
 	 * Returns the preference key usd to store the log file name
 	 */
 	protected abstract String getLogFileNamePreferenceKey();
 
 	/**
 	 * Returns a fully qualified path to a log file name or <code>null</code> if
 	 * logging is not enabled.
 	 */
 	protected String getLogFileName(PreferencesLookupDelegate delegate,
 			String sessionId) {
 		if (!isLoggingEnabled(delegate)) {
 			return null;
 		}
 		String qualifier = getDebuggingEnginePreferenceQualifier();
 		String keyValue = delegate.getString(qualifier,
 				getLogFileNamePreferenceKey());
 
 		Map<IEnvironment, String> logFileNames = EnvironmentPathUtils
 				.decodePaths(keyValue);
 		IEnvironment env = getInstall().getEnvironment();
 		String pathString = logFileNames.get(env);
 		if (pathString != null && pathString.length() > 0) {
 			return pathString;
 		} else {
 			return null;
 		}
 	}
 
 	private String getProcessFactoryId(ILaunch launch) {
 		final ILaunchConfiguration config = launch.getLaunchConfiguration();
 		if (config != null) {
 			try {
 				return config.getAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID,
 						(String) null);
 			} catch (CoreException e) {
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected IProcess newProcess(ILaunch launch, Process p, String label,
 			Map<String, String> attributes) throws CoreException {
 		if (getProcessFactoryId(launch) == null
 				&& ScriptRuntimeProcessFactory.isSupported(launch)) {
 			/*
 			 * Process factory not specified, but it should be.
 			 */
 			return new ScriptRuntimeProcessFactory().newProcess(launch, p,
 					label, attributes);
 		}
 		return super.newProcess(launch, p, label, attributes);
 	}
 }
