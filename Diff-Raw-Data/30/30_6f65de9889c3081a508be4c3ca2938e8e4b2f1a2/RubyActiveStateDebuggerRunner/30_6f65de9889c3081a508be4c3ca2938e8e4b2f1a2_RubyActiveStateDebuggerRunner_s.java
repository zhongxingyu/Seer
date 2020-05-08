 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 
 package org.eclipse.dltk.ruby.activestatedebugger;
 
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.launching.ExternalDebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 
 /**
  * Debugging engine implementation for ActiveState's ruby debugging engine.
  * 
  * <p>
 * see: <a
 * href="http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugruby.html">
  * http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugruby.html</a>
  * </p>
  */
 public class RubyActiveStateDebuggerRunner extends
 		ExternalDebuggingEngineRunner {
 
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.activestatedebugger"; //$NON-NLS-1$
 
 	public RubyActiveStateDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig alterConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) {
 
 		IFileHandle debugEnginePath = getDebuggingEnginePath(delegate);
 		DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(config);
 		final String host = dbgpConfig.getHost();
 		final int port = dbgpConfig.getPort();
 		final String sessionId = dbgpConfig.getSessionId();
 
 		final String dir = debugEnginePath.getParent().toString();
 
 		/*
 		 * TODO: handle RUBYOPT support for rubygems
 		 * 
 		 * unset if not explicity set to a value by user? see:
		 * http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugruby.html
 		 */
 		config.addEnvVar("RUBYDB_OPTS", "RemotePort=" + host + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		config.addEnvVar("DBGP_IDEKEY", sessionId); //$NON-NLS-1$
 		// ruby -I"$dbgdir" -r "$dbgdir"/rdbgp.rb <Program_To_Debug.rb>
 		config.addInterpreterArg("-I"); //$NON-NLS-1$
 		config.addInterpreterArg(dir);
 
 		config.addInterpreterArg("-r"); //$NON-NLS-1$
		config.addInterpreterArg(debugEnginePath.toString());
 
 		return config;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 
 	protected String getDebuggingEnginePreferenceKey() {
 		return RubyActiveStateDebuggerConstants.DEBUGGING_ENGINE_PATH_KEY;
 	}
 
 	protected String getDebuggingEnginePreferenceQualifier() {
 		return RubyActiveStateDebuggerPlugin.PLUGIN_ID;
 	}
 
 	/*
	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebugPreferenceQualifier()
 	 */
 	protected String getDebugPreferenceQualifier() {
 		return RubyDebugPlugin.PLUGIN_ID;
 	}
 
 	/*
	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLoggingEnabledPreferenceKey()
 	 */
 	protected String getLoggingEnabledPreferenceKey() {
 		return RubyActiveStateDebuggerConstants.ENABLE_LOGGING;
 	}
 
 	/*
	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFileNamePreferenceKey()
 	 */
 	protected String getLogFileNamePreferenceKey() {
 		return RubyActiveStateDebuggerConstants.LOG_FILE_NAME;
 	}
 
 	/*
	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFilePathPreferenceKey()
 	 */
 	protected String getLogFilePathPreferenceKey() {
 		return RubyActiveStateDebuggerConstants.LOG_FILE_PATH;
 	}
 }
