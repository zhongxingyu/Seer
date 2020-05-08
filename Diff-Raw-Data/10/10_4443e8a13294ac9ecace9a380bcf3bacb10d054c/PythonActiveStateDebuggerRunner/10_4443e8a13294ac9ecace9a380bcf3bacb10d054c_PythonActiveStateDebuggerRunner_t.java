 package org.eclipse.dltk.python.activestatedebugger;
 
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.launching.ExternalDebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.python.internal.debug.PythonDebugPlugin;
 
 /**
  * Debugging engine implementation for ActiveState's python debugging engine.
  * 
  * <p>
  * This implementation does not require you to install the python debugging
  * engine as described in the install documentation. Instead, the path to the
  * debuggign engine will be passed to the python interpreter.
  * </p>
  * 
  * <p>
  * see: <a
  * href="http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugpython.html">
  * http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugpython.html</a>
  * </p>
  */
 public class PythonActiveStateDebuggerRunner extends
 		ExternalDebuggingEngineRunner {
 
 	public static final String ENGINE_ID = "org.eclipse.dltk.python.activestatedebugger";
 
 	public PythonActiveStateDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.ExternalDebugginEngineRunner#alterConfig(java.lang.String,
 	 *      org.eclipse.dltk.launching.InterpreterConfig, java.lang.String)
 	 */
 	protected InterpreterConfig alterConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) {
 
		IFileHandle debugEnginePath = getDebuggingEnginePath(delegate);
 		DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(config);
 		final String host = dbgpConfig.getHost();
 		final int port = dbgpConfig.getPort();
 		final String sessionId = dbgpConfig.getSessionId();
 
 		// python -S path/to/pydbgp.py -d host:port -k ide_key your-script.py
 		config.addInterpreterArg("-S");
		config.addInterpreterArg(debugEnginePath.toString());
 		config.addInterpreterArg("-d");
 		config.addInterpreterArg(host + ":" + port);
 		config.addInterpreterArg("-k");
 		config.addInterpreterArg(sessionId);
 
 		return config;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebuggingEngineId()
 	 */
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.ExternalDebuggingEngineRunner#getDebuggingEnginePreferenceKey()
 	 */
 	protected String getDebuggingEnginePreferenceKey() {
 		return PythonActiveStateDebuggerConstants.DEBUGGING_ENGINE_PATH_KEY;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.ExternalDebuggingEngineRunner#getDebuggingEnginePreferenceQualifier()
 	 */
 	protected String getDebuggingEnginePreferenceQualifier() {
 		return PythonActiveStateDebuggerPlugin.PLUGIN_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebugPreferenceQualifier()
 	 */
 	protected String getDebugPreferenceQualifier() {
 		return PythonDebugPlugin.PLUGIN_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLoggingEnabledPreferenceKey()
 	 */
 	protected String getLoggingEnabledPreferenceKey() {
 		return PythonActiveStateDebuggerConstants.ENABLE_LOGGING;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFileNamePreferenceKey()
 	 */
 	protected String getLogFileNamePreferenceKey() {
 		return PythonActiveStateDebuggerConstants.LOG_FILE_NAME;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFilePathPreferenceKey()
 	 */
 	protected String getLogFilePathPreferenceKey() {
 		return PythonActiveStateDebuggerConstants.LOG_FILE_PATH;
 	}
 }
