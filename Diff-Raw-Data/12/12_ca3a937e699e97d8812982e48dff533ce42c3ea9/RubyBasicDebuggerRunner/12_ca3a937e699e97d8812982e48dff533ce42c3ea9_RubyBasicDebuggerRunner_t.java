 package org.eclipse.dltk.ruby.internal.launching.debug;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpConstants;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 
 public class RubyBasicDebuggerRunner extends DebuggingEngineRunner {
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST";
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT";
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY";
 	private static final String RUBY_SCRIPT_VAR = "DBGP_RUBY_SCRIPT";
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG";
 
	private static final String DEBUGGER_SCRIPT = "simple_runner.rb";
 
 	private final boolean logging;
 
 	protected String getLogFilename() {
 		// TODO:customize log file name, may be to preferences
 		return RubyDebugPlugin.getDefault().getStateLocation().append(
 				"debug_log.txt").toOSString();
 	}
 
 	protected IPath deploy() throws CoreException {
 		try {
 			return RubyDebugPlugin.getDefault().deployDebuggerSource();
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyDebugPlugin.PLUGIN_ID, "Can't deploy debugger source",
 					e));
 		}
 	}
 
 	public RubyBasicDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 
 		this.logging = true;
 	}
 
 	protected InterpreterConfig alterConfig(String exe, InterpreterConfig config)
 			throws CoreException {
 		// Get debugger source location
 		final IPath sourceLocation = deploy();
 
		final File scriptFile = sourceLocation.append(DEBUGGER_SCRIPT).toFile();
 
 		// Creating new config
 		InterpreterConfig newConfig = new InterpreterConfig();
 
 		// Interpreter arguments
 		newConfig.addInterpreterArgs(config.getInterpreterArgs());
		newConfig.addInterpreterArg("-I" + sourceLocation.toOSString());
 
 		// Script
 		newConfig.setScriptFile(scriptFile);
 
 		// Script arguments
 		newConfig.addScriptArgs(config.getScriptArgs());
 
 		// Working directory
 		newConfig.setWorkingDirectory(config.getWorkingDirectory());
 
 		// Environment
 		String host = (String) config.getProperty(DbgpConstants.HOST_PROP);
 		String port = (String) config.getProperty(DbgpConstants.PORT_PROP);
 		String sessionId = (String) config
 				.getProperty(DbgpConstants.SESSION_ID_PROP);
 
 		newConfig.addEnvVars(config.getEnvVars());
 		newConfig.addEnvVar(RUBY_HOST_VAR, host);
 		newConfig.addEnvVar(RUBY_PORT_VAR, port);
 		newConfig.addEnvVar(RUBY_KEY_VAR, sessionId);
 		newConfig.addEnvVar(RUBY_SCRIPT_VAR, config.getScriptFile().toString());
 
 		if (logging) {
 			newConfig.addEnvVar(RUBY_LOG_VAR, getLogFilename());
 		}
 
 		return newConfig;
 	}
 }
