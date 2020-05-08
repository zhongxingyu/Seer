 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ruby.basicdebugger;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 
 public class RubyBasicDebuggerRunner extends DebuggingEngineRunner {
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.basicdebugger";
 
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST";
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT";
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY";
 	private static final String RUBY_SCRIPT_VAR = "DBGP_RUBY_SCRIPT";
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG";
 
 	private static final String DEBUGGER_SCRIPT = "BasicRunner.rb";
 
 	private final boolean logging;
 
 	protected boolean isLoggingEnabled() {
 		return logging;
 	}
 
 	protected IPath getLogFilename() {
 		// TODO:customize log file name, may be to preferences
 		return RubyBasicDebuggerPlugin.getDefault().getStateLocation().append(
 				"debug_log.txt");
 	}
 
 	protected IPath deploy() throws CoreException {
 		try {
 			return RubyBasicDebuggerPlugin.getDefault().deployDebuggerSource();
 		} catch (IOException e) {
 			abort("Can't deploy debugger source", e);
 		}
 
 		return null;
 	}
 
 	public RubyBasicDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 
 		this.logging = true;
 	}
 
 	protected InterpreterConfig addEngineConfig(InterpreterConfig config, PreferencesLookupDelegate delegate)
 			throws CoreException {
 		// Get debugger source location
 		final IPath sourceLocation = deploy();
 
 		final IPath scriptFile = sourceLocation.append(DEBUGGER_SCRIPT);
 
 		// Creating new config
 		InterpreterConfig newConfig = (InterpreterConfig) config.clone();
 		newConfig.addInterpreterArg("-r" + scriptFile.toPortableString());
 		newConfig.addInterpreterArg("-I" + sourceLocation.toPortableString());
 		
 		// Environment
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
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
