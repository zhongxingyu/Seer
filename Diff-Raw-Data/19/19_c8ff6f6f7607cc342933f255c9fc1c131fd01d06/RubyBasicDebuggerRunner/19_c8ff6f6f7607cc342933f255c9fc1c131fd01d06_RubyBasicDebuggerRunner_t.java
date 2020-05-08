 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ruby.basicdebugger;
 
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
 
 public class RubyBasicDebuggerRunner extends DebuggingEngineRunner {
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.basicdebugger";
 	
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST";
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT";
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY";
 	private static final String RUBY_SCRIPT_VAR = "DBGP_RUBY_SCRIPT";
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG";
 
 	private static final String DEBUGGER_SCRIPT = "simple_runner.rb";
 
 	private final boolean logging;
 
 	protected IPath getLogFilename() {
 		// TODO:customize log file name, may be to preferences
 		return RubyBasicDebuggerPlugin.getDefault().getStateLocation().append(
 				"debug_log.txt");
 	}
 
 	protected IPath deploy() throws CoreException {
 		try {
 			return RubyBasicDebuggerPlugin.getDefault().deployDebuggerSource();
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyBasicDebuggerPlugin.PLUGIN_ID,
 					"Can't deploy debugger source", e));
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
 
 		final IPath scriptFile = sourceLocation.append(DEBUGGER_SCRIPT);
		
		
 
 		// Creating new config
 		InterpreterConfig newConfig = new InterpreterConfig();
 
 		// Interpreter arguments
 		newConfig.addInterpreterArgs(config.getInterpreterArgs());
 		newConfig.addInterpreterArg("-I" + sourceLocation.toPortableString());
 
 		// Script
 		newConfig.setScriptFile(scriptFile);
 
 		// Script arguments
 		newConfig.addScriptArgs(config.getScriptArgs());
 
 		// Working directory
 		newConfig.setWorkingDirectory(config.getWorkingDirectoryPath());
 
 		// Environment
 		String host = (String) config.getProperty(DbgpConstants.HOST_PROP);
 		String port = (String) config.getProperty(DbgpConstants.PORT_PROP);
 		String sessionId = (String) config
 				.getProperty(DbgpConstants.SESSION_ID_PROP);
 
 		newConfig.addEnvVars(config.getEnvVars());
 		newConfig.addEnvVar(RUBY_HOST_VAR, host);
 		newConfig.addEnvVar(RUBY_PORT_VAR, port);
 		newConfig.addEnvVar(RUBY_KEY_VAR, sessionId);
		newConfig.addEnvVar(RUBY_SCRIPT_VAR, config.getScriptFilePath().toPortableString());
		
 		if (logging) {
 			newConfig.addEnvVar(RUBY_LOG_VAR, getLogFilename()
 					.toPortableString());
 		}
				
 		return newConfig;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 }
