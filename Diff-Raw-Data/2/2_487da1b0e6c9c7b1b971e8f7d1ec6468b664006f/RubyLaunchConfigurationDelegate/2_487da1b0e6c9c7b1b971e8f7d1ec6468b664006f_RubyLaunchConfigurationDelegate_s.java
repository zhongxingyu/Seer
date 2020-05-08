 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.launching;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
 public class RubyLaunchConfigurationDelegate extends
 		AbstractScriptLaunchConfigurationDelegate {
 
 	public String getLanguageId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	protected String getCharset(ILaunchConfiguration configuration)
 			throws CoreException {
 		IProject project = getScriptProject(configuration).getProject();
 		IResource resource = project
 				.findMember(getMainScriptName(configuration));
 
 		if (resource instanceof IFile) {
 			IFile file = (IFile) resource;
 			return file.getCharset();
 		}
 
 		return null;
 	}
 
 	protected String getCharsetInterpreterFlag(String charset) {
 		if (charset.equals("UTF-8")) {
 			return "-KU";
 		} else if (charset.equals("EUC")) {
 			return "-KE";
 		} else if (charset.equals("SJIS")) {
 			return "-KS";
 		}
 
 		return "-KA";
 	}
 
 	protected void addEncodingInterpreterArg(InterpreterConfig config,
 			ILaunchConfiguration configuration) throws CoreException {
 		if (!config.hasMatchedInterpreterArg("-K.*")) {
 			String charset = getCharset(configuration);
 			if (charset != null) {
 				config.addInterpreterArg(getCharsetInterpreterFlag(charset));
 			}
 		}
 	}
 
 	protected void addIncludePathInterpreterArg(InterpreterConfig config,
 			ILaunchConfiguration configuration) throws CoreException {
 		IPath[] paths = createBuildPath(configuration);
 
 		char separator = Platform.getOS().equals(Platform.OS_WIN32) ? ';' : ':';
 
 		StringBuffer sb = new StringBuffer("-I");
 		for (int i = 0; i < paths.length; ++i) {
 
 			sb.append(paths[i].toOSString());
 			if (i < paths.length - 1) {
 				sb.append(separator);
 			}
 		}
 
 		config.addInterpreterArg(sb.toString());
 	}
 
 	protected void addStreamSync(InterpreterConfig config,
 			ILaunchConfiguration configuration) {
 		config.addInterpreterArg("-e");
 		config
				.addInterpreterArg("STDOUT.sync=true;STDERR.sync=true;load($0=ARGV.shift)");
 	}
 
 	protected InterpreterConfig createInterpreterConfig(
 			ILaunchConfiguration configuration, ILaunch launch)
 			throws CoreException {
 
 		InterpreterConfig config = super.createInterpreterConfig(configuration,
 				launch);
 
 		addEncodingInterpreterArg(config, configuration);
 		addIncludePathInterpreterArg(config, configuration);
 		addStreamSync(config, configuration);
 
 		return config;
 	}
 }
