 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.launching.AbstractInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.IInterpreterRunner;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.ScriptLaunchUtil;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 
 public class RubyGenericInstall extends AbstractInterpreterInstall {
 
 	public class BuiltinsHelper {
 		private static final String PREFIX = "#### DLTK RUBY BUILTINS ####"; //$NON-NLS-1$
 
 		private Map sources;
 
 		private String[] generateLines() throws IOException, CoreException {
 			IExecutionEnvironment exeEnv = getExecEnvironment();
 			IDeployment deployment = exeEnv.createDeployment();
 			final IPath builder = deployment.add(RubyLaunchingPlugin
 					.getDefault().getBundle(), "scripts/builtin.rb"); //$NON-NLS-1$
 
 			final List lines = new ArrayList();
 
 			IFileHandle builderFile = deployment.getFile(builder);
 			InterpreterConfig config = ScriptLaunchUtil
 					.createInterpreterConfig(exeEnv, builderFile, builderFile
 							.getParent());
 			// config.addInterpreterArg("-KU"); //$NON-NLS-1$
 			final Process process = ScriptLaunchUtil.runScriptWithInterpreter(
 					exeEnv, RubyGenericInstall.this.getInstallLocation()
							.getAbsolutePath(), config);
 
 			Thread readerThread = new Thread(new Runnable() {
 				public void run() {
 					BufferedReader input = null;
 					try {
 						input = new BufferedReader(new InputStreamReader(
 								process.getInputStream()));
 
 						String line = null;
 						try {
 							while ((line = input.readLine()) != null) {
 								lines.add(line);
 							}
 						} catch (IOException e) {
 							if (DLTKCore.DEBUG) {
 								e.printStackTrace();
 							}
 						}
 
 					} finally {
 						if (input != null) {
 							try {
 								input.close();
 							} catch (IOException e) {
 								if (DLTKCore.DEBUG) {
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 				}
 			});
 			try {
 				readerThread.start();
 				readerThread.join(10000);
 			} catch (InterruptedException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 			return (String[]) lines.toArray(new String[lines.size()]);
 		}
 
 		private void parseLines(String[] lines) {
 			String fileName = null;
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < lines.length; ++i) {
 				String line = lines[i];
 
 				int index = line.indexOf(PREFIX);
 				if (index != -1) {
 					if (fileName != null) {
 						String old = (String) sources.get(fileName);
 						if (old == null)
 							sources.put(fileName, sb.toString());
 						else
 							sources.put(fileName, old + "\n\n" + sb.toString()); //$NON-NLS-1$
 						sb.setLength(0);
 					}
 
 					fileName = line.substring(index + PREFIX.length());
 
 				} else {
 					sb.append(line);
 					sb.append("\n"); //$NON-NLS-1$
 				}
 			}
 		}
 
 		public Map getSources() {
 			if (sources == null) {
 				sources = new HashMap();
 
 				try {
 					String[] lines = generateLines();
 					if (lines != null) {
 						parseLines(lines);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 
 			return sources;
 		}
 	}
 
 	private BuiltinsHelper helper = new BuiltinsHelper();
 
 	public RubyGenericInstall(IInterpreterInstallType type, String id) {
 		super(type, id);
 	}
 
 	public IInterpreterRunner getInterpreterRunner(String mode) {
 		final IInterpreterRunner runner = super.getInterpreterRunner(mode);
 
 		if (runner != null) {
 			return runner;
 		}
 
 		if (mode.equals(ILaunchManager.RUN_MODE)) {
 			return new RubyInterpreterRunner(this);
 		}
 
 		return null;
 	}
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	// Builtins
 	public String getBuiltinModuleContent(String name) {
 		final Map sources = helper.getSources();
 		return (String) sources.get(name);
 	}
 
 	public String[] getBuiltinModules() {
 		final Map sources = helper.getSources();
 		return (String[]) sources.keySet().toArray(new String[sources.size()]);
 	}
 }
