 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ruby.activestatedebugger;
 
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
 
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.activestatedebugger";
 
 	public RubyActiveStateDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig alterConfig(InterpreterConfig config,
			String debugEnginePath) {
 
 		DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(config);
 		final String host = dbgpConfig.getHost();
 		final int port = dbgpConfig.getPort();
 		final String sessionId = dbgpConfig.getSessionId();
 
		final String dir = debugEnginePath.substring(0, debugEnginePath
				.lastIndexOf('/'));
 
 		/*
 		 * TODO: handle RUBYOPT support for rubygems
 		 * 
 		 * unset if not explicity set to a value by user? see:
 		 * http://aspn.activestate.com/ASPN/docs/Komodo/komodo-doc-debugruby.html
 		 */
 		config.addEnvVar("RUBYDB_OPTS", "RemotePort=" + host + ":" + port);
 		config.addEnvVar("DBGP_IDEKEY", sessionId);
 		// ruby -I"$dbgdir" -r "$dbgdir"/rdbgp.rb <Program_To_Debug.rb>
 		config.addInterpreterArg("-I");
 		config.addInterpreterArg(dir);
 
 		config.addInterpreterArg("-r");
		config.addInterpreterArg(debugEnginePath);
 
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
 }
