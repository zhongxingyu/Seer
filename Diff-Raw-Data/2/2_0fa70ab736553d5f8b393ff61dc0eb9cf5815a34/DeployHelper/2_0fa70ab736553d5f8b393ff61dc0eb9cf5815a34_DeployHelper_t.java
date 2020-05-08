 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.utils;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Enumeration;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.Bundle;
 
 public class DeployHelper {
 	public static void copy(InputStream input, OutputStream output)
 			throws IOException {
 		int ch = -1;
 		while ((ch = input.read()) != -1) {
 			output.write(ch);
 		}
 	}
 
 	public static void copy(InputStream input, File file) throws IOException {
 		OutputStream output = null;
 		try {
 			output = new BufferedOutputStream(new FileOutputStream(file));
 			copy(input, output);
 		} finally {
 			if (output != null) {
 				output.close();
 			}
 		}
 	}
 
 	public static void copy(URL url, File file) throws IOException {
 		InputStream input = null;
 		try {
 			input = url.openStream();
 			copy(input, file);
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 		}
 	}	
 
 	public static IPath deploy(Bundle bundle, String bundlePath, IPath diskPath)
 			throws IOException {
 
 		File dir = diskPath.append(bundlePath).toFile();
 		dir.mkdir();
 
 		Enumeration paths = bundle.getEntryPaths(bundlePath);
 		if (paths != null) {
 			while (paths.hasMoreElements()) {
 				String path = (String) paths.nextElement();
 				if (path.endsWith("/")) {
 					deploy(bundle, path, diskPath);
				} else { 
 					File file = diskPath.append(path).toFile();
 					DeployHelper.copy(bundle.getEntry(path), file);
 				}
 			}
 		}
 
 		return new Path(dir.getAbsolutePath());
 	}
 	
 	public static IPath deploy(Plugin plugin, String bundlePath) throws IOException {
 		return deploy(plugin.getBundle(), bundlePath, plugin.getStateLocation());
 	}
 }
