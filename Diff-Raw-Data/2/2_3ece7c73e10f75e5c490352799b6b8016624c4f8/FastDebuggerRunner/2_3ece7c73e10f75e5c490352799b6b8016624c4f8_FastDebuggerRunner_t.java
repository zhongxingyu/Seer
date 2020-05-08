 package org.eclipse.dltk.ruby.fastdebugger;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.ruby.fastdebugger.preferences.FastDebuggerPreferenceConstants;
 
 public class FastDebuggerRunner extends DebuggingEngineRunner {
	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.fastdebugger";
 
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST";
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT";
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY";
 	private static final String RUBY_SCRIPT_VAR = "DBGP_RUBY_SCRIPT";
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG";
 
 	private static final String DEBUGGER_SCRIPT = "fast_runner.rb";
 
 	protected IPath getLogFilename() {
 		Preferences preferences = FastDebuggerPlugin.getDefault()
 				.getPluginPreferences();
 		
 		String logFilePath = preferences
 				.getString(FastDebuggerPreferenceConstants.LOG_FILE_PATH);
 
 		String logFileName = preferences
 				.getString(FastDebuggerPreferenceConstants.LOG_FILE_NAME);
 
 		return Path.fromOSString(logFilePath).append(logFileName);
 	}
 
 	protected boolean isLoggingEnabled() {
 		return FastDebuggerPlugin.getDefault().getPluginPreferences()
 				.getBoolean(FastDebuggerPreferenceConstants.ENABLE_LOGGING);
 	}
 
 	protected IPath deploy() throws CoreException {
 		try {
 			return FastDebuggerPlugin.getDefault().deployDebuggerSource();
 		} catch (IOException e) {
 			// TODO: add code for handler
 			throw new CoreException(new Status(IStatus.ERROR,
 					FastDebuggerPlugin.PLUGIN_ID,
 					"Can't deploy debugger source", e));
 		}
 	}
 
 	public FastDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig alterConfig(String exe, InterpreterConfig config)
 			throws CoreException {
 		// DBGP specific configuration
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
 		// New configuration
 		final InterpreterConfig newConfig = (InterpreterConfig) config.clone();
 
 		// Customization
 		final IPath sourceLocation = deploy();
 		newConfig.setScriptFile(sourceLocation.append(DEBUGGER_SCRIPT));
 		newConfig.addInterpreterArg("-I" + sourceLocation.toPortableString());
 
 		newConfig.addEnvVar(RUBY_HOST_VAR, dbgpConfig.getHost());
 		newConfig.addEnvVar(RUBY_PORT_VAR, Integer.toString(dbgpConfig
 				.getPort()));
 		newConfig.addEnvVar(RUBY_KEY_VAR, dbgpConfig.getSessionId());
 		newConfig.addEnvVar(RUBY_SCRIPT_VAR, config.getScriptFilePath()
 				.toPortableString());
 
 		if (isLoggingEnabled()) {
 			newConfig.addEnvVar(RUBY_LOG_VAR, getLogFilename()
 					.toPortableString());
 		}
 
 		return newConfig;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 }
