 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.docs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.internal.environment.LocalEnvironment;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallChangedListener;
 import org.eclipse.dltk.launching.PropertyChangeEvent;
 import org.eclipse.dltk.launching.ScriptLaunchUtil;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 
 public class RiHelper {
 	private final static String DOC_TERMINATION_LINE = "DLTKDOCEND"; //$NON-NLS-1$
 
 	private static RiHelper instance;
 
 	public static RiHelper getInstance() {
 		if (instance == null) {
 			instance = new RiHelper();
 		}
 
 		return instance;
 	}
 
 	private WeakHashMap cache = new WeakHashMap();
 
 	private Process riProcess;
 	private OutputStreamWriter writer;
 	private BufferedReader reader;
 	private BufferedReader errorReader;
 
 	private IDeployment deployment;
 
 	protected static boolean isTerminated(Process riProcess) {
 		try {
 			riProcess.exitValue();
 			return true;
 		} catch (IllegalThreadStateException e) {
 			return false;
 		}
 	}
 
 	protected synchronized void runRiProcess() throws CoreException,
 			IOException {
 		IInterpreterInstall install = ScriptLaunchUtil
 				.getDefaultInterpreterInstall(RubyNature.NATURE_ID, LocalEnvironment.ENVIRONMENT_ID);
 
 		if (install == null) {
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 
 		IEnvironment env = install.getEnvironment();
 		IExecutionEnvironment exeEnv = (IExecutionEnvironment) env
 				.getAdapter(IExecutionEnvironment.class);
 		deployment = exeEnv.createDeployment();
 
 		IPath path = deployment
 				.add(RubyUI.getDefault().getBundle(), "support/") //$NON-NLS-1$
 				.append("dltkri.rb"); //$NON-NLS-1$
 		IFileHandle script = deployment.getFile(path);
 
 		riProcess = ScriptLaunchUtil.runScriptWithInterpreter(exeEnv, install
				.getInstallLocation().getAbsolutePath(), script, null, null,
 				null, install.getEnvironmentVariables());
 
 		writer = new OutputStreamWriter(riProcess.getOutputStream());
 		reader = new BufferedReader(new InputStreamReader(riProcess
 				.getInputStream()));
 		errorReader = new BufferedReader(new InputStreamReader(riProcess
 				.getErrorStream()));
 	}
 
 	protected synchronized void destroyRiProcess() {
 		if (riProcess != null) {
 			riProcess.destroy();
 			riProcess = null;
 
 			// Cache should be cleared if we change interpreter
 			cache.clear();
 		}
 		if (deployment != null) {
 			deployment.dispose();
 			deployment = null;
 		}
 
 	}
 
 	protected String readStderr() throws IOException {
 		StringBuffer sb = new StringBuffer();
 		String errorLine = null;
 		while ((errorLine = errorReader.readLine()) != null) {
 			sb.append(errorLine);
 			sb.append("\n"); //$NON-NLS-1$
 		}
 		String error = sb.toString().trim();
 
 		return error.length() > 0 ? error : null;
 	}
 
 	protected String readStdout() throws IOException {
 		StringBuffer sb = new StringBuffer();
 		do {
 			String line = reader.readLine();
 			if (line == null || line.equals(DOC_TERMINATION_LINE)) {
 				break;
 			}
 
 			sb.append(line);
 			sb.append('\n');
 		} while (true);
 
 		return sb.toString();
 	}
 
 	protected String loadRiDoc(String keyword) throws IOException {
 		// Write
 		writer.write(keyword + "\n"); //$NON-NLS-1$
 		writer.flush();
 
 		// Stderr
 		// TODO: checking error!
 
 		// Stdout
 		return readStdout();
 	}
 
 	private boolean checkRiProcess() {
 		if (riProcess == null || isTerminated(riProcess)) {
 			try {
 				runRiProcess();
 			} catch (Exception e) {
 				// TODO: log exception
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	protected RiHelper() {
 		ScriptRuntime
 				.addInterpreterInstallChangedListener(new IInterpreterInstallChangedListener() {
 					public void defaultInterpreterInstallChanged(
 							IInterpreterInstall previous,
 							IInterpreterInstall current) {
 						destroyRiProcess();
 					}
 
 					public void interpreterAdded(IInterpreterInstall Interpreter) {
 					}
 
 					public void interpreterChanged(PropertyChangeEvent event) {
 					}
 
 					public void interpreterRemoved(
 							IInterpreterInstall Interpreter) {
 					}
 				});
 	}
 
 	public synchronized String getDocFor(String keyword) {
 		String doc = (String) cache.get(keyword);
 
 		if (doc == null) {
 			if (checkRiProcess()) {
 				try {
 					doc = loadRiDoc(keyword);
 					cache.put(keyword, doc);
 				} catch (IOException e) {
 					destroyRiProcess();
 				}
 			}
 		}
 
 		return doc;
 	}
 }
