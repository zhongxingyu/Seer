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
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallChangedListener;
 import org.eclipse.dltk.launching.PropertyChangeEvent;
 import org.eclipse.dltk.launching.ScriptLaunchUtil;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.utils.DeployHelper;
 
 public class RiHelper {
 	private final static String DOC_TERMINATION_LINE = "DLTKDOCEND";
 
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
 
 	protected static boolean isTerminated(Process process) {
 		try {
 			process.exitValue();
 			return true;
 		} catch (IllegalThreadStateException e) {
 			return false;
 		}
 	}
 
 	protected synchronized void runRiProcess() throws CoreException,
 			IOException {
 		IInterpreterInstall install = ScriptLaunchUtil
 				.getDefaultInterpreterInstall(RubyNature.NATURE_ID);
 
 		if (install == null) {
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 
 		File script = DeployHelper.deploy(RubyUI.getDefault(), "support/")
 				.append("dltkri.rb").toFile();
 		riProcess = ScriptLaunchUtil.runScriptWithInterpreter(install
 				.getInstallLocation().getAbsolutePath(), script, null, null,
 				null);
 
 		writer = new OutputStreamWriter(riProcess.getOutputStream());
 		reader = new BufferedReader(new InputStreamReader(riProcess
 				.getInputStream()));
 	}
 
 	protected synchronized void destroyRiProcess() {
 		if (riProcess != null) {
 			riProcess.destroy();
 			riProcess = null;
 		}
 	}
 
 	protected String loadRiDoc(String keyword) throws IOException {
 		// Write
 		writer.write(keyword + "\n");
 		writer.flush();
 
 		// Read
 		StringBuffer sb = new StringBuffer();
 		do {
 			String line = reader.readLine();
 			if (line == null || line.equals(DOC_TERMINATION_LINE)) {
 				break;
 			}
 
 			sb.append(line);
 
 		} while (true);
 
 		return sb.toString();
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
