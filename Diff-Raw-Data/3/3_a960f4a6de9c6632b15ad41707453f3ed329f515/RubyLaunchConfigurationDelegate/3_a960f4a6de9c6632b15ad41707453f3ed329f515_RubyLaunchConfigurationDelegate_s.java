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
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
 public class RubyLaunchConfigurationDelegate extends
 		AbstractScriptLaunchConfigurationDelegate {
 
 	public String getLanguageId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	protected String createNativeBuildPath(IPath[] paths) {
 		StringBuffer sb = new StringBuffer();
 
 		char separator = Platform.getOS().equals(Platform.OS_WIN32) ? ';' : ':';
 
 		for (int i = 0; i < paths.length; ++i) {
 			IPath path = paths[i];
 
 			sb.append(path.toOSString());
 
 			if (i < paths.length - 1) {
 				sb.append(separator);
 			}
 		}
 
 		return sb.toString();
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
 
 	public String getInterpreterArguments(ILaunchConfiguration configuration)
 			throws CoreException {
 		String args = super.getInterpreterArguments(configuration);
 
 		// Encoding
 		IProject project = getScriptProject(configuration).getProject();
 		IResource resource = project
 				.findMember(getMainScriptName(configuration));
 		if (resource instanceof IFile) {
 			IFile file = (IFile) resource;
 			String charset = file.getCharset();
 			if (args.indexOf("-K") == -1) {
 				args += " " + getCharsetInterpreterFlag(charset) + " ";
 			}
 		}
 
 		// Library path
 		args += " -I" + createBuildPath(configuration);
 
 		return args;
 	}
 }
