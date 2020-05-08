 /*******************************************************************************
  * Copyright (c) 2011 Petri Tuononen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Petri Tuononen - Initial implementation
  *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.pkgconfig.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
 import org.eclipse.cdt.managedbuilder.pkgconfig.preferences.PreferenceStore;
 
 /**
  * Runs pkg-config utility in the command line and outputs necessary
  * options to build the given package.
  *
  */
 public class PkgConfigUtil {
 
 	//Constant variables
 	private static final String PKG_CONFIG = "pkg-config";
 	private static final String LIST_PACKAGES = "--list-all";
 	private static final String OUTPUT_LIBS = "--libs";
 	private static final String OUTPUT_CFLAGS = "--cflags";
 	private static final String OUTPUT_ALL = "--cflags --libs";
 	private static final String OUTPUT_ONLY_LIB_PATHS = "--libs-only-L";
 	private static final String OUTPUT_ONLY_LIB_FILES = "--libs-only-l";
 
 	/**
 	 * Get options needed to build the given package.
 	 * Does not like spaces on paths except that getAllPackages seem to work.
 	 * 
 	 * @param command
 	 * @param pkg
 	 * @return
 	 */
 	private static String getPkgOutput(String command, String pkg) {
 		ProcessBuilder pb = null;
 		String confPath = PreferenceStore.getPkgConfigPath();
 		if (OSDetector.isUnix() || OSDetector.isMac()) {
 			if (confPath!=null && !confPath.equals("")) {
 				pb = new ProcessBuilder("bash", "-c", confPath+
						Separators.getFileSeparator()+PKG_CONFIG+" "+command+" "+pkg); //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
				pb = new ProcessBuilder("bash", "-c", PKG_CONFIG+" "+command+" "+pkg); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		} else if (OSDetector.isWindows()) {
 			if (confPath!=null && !confPath.equals("")) {
 				pb = new ProcessBuilder("cmd", "/c", "\""+confPath+
 						Separators.getFileSeparator()+PKG_CONFIG+"\"", command, pkg);	//$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				pb = new ProcessBuilder("cmd", "/c", PKG_CONFIG, command, pkg);	//$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		Process p = null;
 		try {
 			p = pb.start();
 		} catch (IOException e) {
 			Activator.getDefault().log(e, "Starting a process (executing a command line script) failed.");
 		}
 		if (p != null) {
 			String line;
 			BufferedReader input = new BufferedReader
 					(new InputStreamReader(p.getInputStream()));
 			try {
 				line = input.readLine();
 				if (line != null) {
 					return line;
 				}
 				input.close();
 			} catch (IOException e) {
 				Activator.getDefault().log(e, "Reading a line from the input failed.");
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Get cflags and libraries needed to build the given package.
 	 * 
 	 * @param pkg
 	 * @return
 	 */
 	public static String getAll(String pkg) {
 		return getPkgOutput(OUTPUT_ALL, pkg);
 	}
 	
 	/**
 	 * Get libraries (files and paths) needed to build the given package.
 	 * 
 	 * @param pkg
 	 * @return
 	 */
 	public static String getLibs(String pkg) {
 		return getPkgOutput(OUTPUT_LIBS, pkg);
 	}
 	
 	/**
 	 * Get library paths needed to build the given package.
 	 * 
 	 * @param pkg
 	 * @return
 	 */
 	public static String getLibPathsOnly(String pkg) {
 		return getPkgOutput(OUTPUT_ONLY_LIB_PATHS, pkg);
 	}
 
 	/**
 	 * Get library files needed to build the given package.
 	 * 
 	 * @param pkg
 	 * @return
 	 */
 	public static String getLibFilesOnly(String pkg) {
 		return getPkgOutput(OUTPUT_ONLY_LIB_FILES, pkg);
 	}
 	
 	/**
 	 * Get cflags needed to build the given package.
 	 * 
 	 * @param pkg
 	 * @return
 	 */
 	public static String getCflags(String pkg) {
 		return getPkgOutput(OUTPUT_CFLAGS, pkg);
 	}
 	
 	/**
 	 * Get all packages that pkg-config utility finds (package name with description).
 	 * 
 	 * @return
 	 */
 	public static ArrayList<String> getAllPackages() {
 		ProcessBuilder pb = null;
 		String confPath = PreferenceStore.getPkgConfigPath();
 		if (OSDetector.isUnix() || OSDetector.isMac()) {
 			if (confPath!=null && !confPath.equals("")) {
 				pb = new ProcessBuilder("bash", "-c", confPath+
						Separators.getFileSeparator()+PKG_CONFIG+" "+LIST_PACKAGES); //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
				pb = new ProcessBuilder("bash", "-c", PKG_CONFIG+" "+LIST_PACKAGES); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		} else if (OSDetector.isWindows()) {
 			if (confPath!=null && !confPath.equals("")) {
 				pb = new ProcessBuilder("cmd", "/c", "\""+confPath+
 						Separators.getFileSeparator()+PKG_CONFIG+"\"", LIST_PACKAGES); //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				pb = new ProcessBuilder("cmd", "/c", PKG_CONFIG, LIST_PACKAGES); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		try {
 			Process p = pb.start();
 			String line;
 			BufferedReader input = new BufferedReader
 					(new InputStreamReader(p.getInputStream()));
 			ArrayList<String> packageList = new ArrayList<String>();
 			do {
 				line = input.readLine();
 				if (line != null) {
 					packageList.add(line);
 				}
 			} while(line != null);
 			input.close();
 			return packageList;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 }
