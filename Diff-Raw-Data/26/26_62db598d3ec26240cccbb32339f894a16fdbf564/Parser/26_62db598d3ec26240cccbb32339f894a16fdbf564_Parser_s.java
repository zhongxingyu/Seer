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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 /**
  * Parses pkg-config utility output.
  *
  */
 public class Parser {
 	
 	//for testing only
 	public static void main(String[] args) {
 		System.out.println("Options\n######################");
 		
 		String options = PkgConfigUtil.pkgOutputCflags("gtk+-2.0");
 		String[] optionsArray = parseCflagOptions(options);
 		for (String l : optionsArray) {
 			System.out.println(l);
 		}
 		
 		System.out.println("\nInclude paths\n######################");
 		
 		String incPaths = PkgConfigUtil.pkgOutputCflags("gtk+-2.0");
 		String[] incPathArray = parseIncPaths(incPaths);
 		for (String l : incPathArray) {
 			System.out.println(l);
 		}
 		
 //		System.out.println("\nLibrary search paths\n######################");
 //		
 //		String libsPaths = PkgConfigUtil.pkgOutputLibs("gtk+-2.0");
 //		String[] libPathArray = parseLibPaths(libsPaths);
 //		for (String l : libPathArray) {
 //			System.out.println(l);
 //		}
 //		
 //		System.out.println("\nLibraries\n######################");
 //		
 //		String libs = PkgConfigUtil.pkgOutputLibs("gtk+-2.0");
 //		String[] libArray = parseLibs(libs);
 //		ArrayList<String> sorted = new ArrayList<String>(Arrays.asList(libArray)); 
 //		Collections.sort(sorted);
 //		for (String l : sorted) {
 //			System.out.println(l);
 //		}
 		
 		System.out.println("\nLibrary search paths\n######################");
 		
 		String libsPaths = PkgConfigUtil.pkgOutputLibPathsOnly("gtk+-2.0");
 		String[] libPathArray = parseLibPaths2(libsPaths);
 		for (String l : libPathArray) {
 			System.out.println(l);
 		}
 		
 		System.out.println("\nLibraries\n######################");
 		
 		String libs = PkgConfigUtil.pkgOutputLibFilesOnly("gtk+-2.0");
 		String[] libArray = parseLibs2(libs);
 		ArrayList<String> sorted = new ArrayList<String>(Arrays.asList(libArray)); 
 		Collections.sort(sorted);
 		for (String l : sorted) {
 			System.out.println(l);
 		}
 		
 		System.out.println("\nDescriptions\n######################");
 		
 		ArrayList<String> pkgs = PkgConfigUtil.getAllPackages();
 		ArrayList<String> parsedDesc = parseDescription(pkgs);
 		for (String s : parsedDesc) {
 			System.out.println(s);
 		}
 	}
 	
 	/**
 	 * Parses options from "pkg-config --cflags" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseCflagOptions(String s) {
 		//find the index where include list starts
 		int end = s.indexOf("-I");
		if (end != -1) {
			//truncate include paths
			s = s.substring(0, end-1);
			//insert options to an array
			String[] options = s.split(" ");
			return options;
		} else {
 			//check if any flags found
 			int flagStart = s.indexOf("-");
			if (flagStart != -1) {
 				s = s.substring(flagStart, s.length()-1);
 				//insert options into an array
 				String[] options = s.split(" ");
 				return options;
 			} else {
 				String[] emptyList = {""};
 				return emptyList;
 			}
 		}
 	}
 	
 	/**
 	 * Parses include paths from "pkg-config --cflags" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseIncPaths(String s) {
 		//find the index where include list starts
 		int start = s.indexOf("-I");
 		if (start != -1) { //if include paths found
 			//truncate other than include paths
 			s = s.substring(start, s.length()-1);
 			//remove library search path flags
 			String s2 = s.replace("-I", "");
 			//insert include paths into an array
 			String[] incPaths = s2.split(" ");
 			return incPaths;
 		} else {
 			String[] emptyList = {""};
 			return emptyList;
 		}
 	}
 	
 	/**
 	 * Parses library search paths from "pkg-config --libs" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseLibPaths(String s) {
 		//find the index where library path list starts
 		int start = s.indexOf("-L");
 		if (start != -1) { //if library paths found
 			//find the index where library list starts
 			int end = s.indexOf(" -l");
 			//truncate other than library paths
 			s = s.substring(start, end);
 			//remove library search path flags
 			String s2 = s.replace("-L", "");
 			//insert lib paths into an array
 			String[] libPaths = s2.split(" ");
 			return libPaths;
 		} else {
 			String[] emptyList = {""};
 			return emptyList;
 		}
 	}
 	
 	/**
 	 * Parses library search paths from "pkg-config --libs-only-L" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseLibPaths2(String s) {
 		//remove library search path flags
 		String s2 = s.replace("-L", "");
 		//insert lib paths into an array
 		String[] libPaths = s2.split(" ");
 		return libPaths;
 	}
 	
 	/**
 	 * Parses libraries from "pkg-config --libs" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseLibs(String s) {
 		//special case if pkg-config --libs output starts with -l
 		int start = s.indexOf("-l");
 		if (start != 0) {
 			start = s.indexOf(" -l");
 		}
 		if (start != -1) { //if libraries found
 			//truncate library search paths
 			s = s.substring(start+1, s.length()-1);
 			//remove lib flags
 			String s2 = s.replace("-l", "");
 			//insert libs into an array
 			String[] libs = s2.split(" ");
 			return libs;
 		} else {
 			String[] emptyList = {""};
 			return emptyList;
 		}
 	}
 	
 	/**
 	 * Parses libraries from "pkg-config --libs-only-l" input.
 	 * 
 	 * @param s Output from pkg-config.
 	 * @return Parsed String array.
 	 */
 	public static String[] parseLibs2(String s) {
 		//remove lib flags
 		String s2 = s.replace("-l", "");
 		//insert libs into an array
 		String[] libs = s2.split(" ");
 		return libs;
 	}
 	
 	/**
 	 * Parse package list so that only package names are added to ArrayList.
 	 * 
 	 * @param packages
 	 * @return
 	 */
 	public static ArrayList<String> parsePackageList(ArrayList<String> packages) {
 		ArrayList<String> operated = new ArrayList<String>();
 		for (String s : packages) {
 			//cut the string after the first white space
 			int end = s.indexOf(" ");
 			operated.add(s.substring(0, end));
 		}
 		return operated;
 	}
 	
 	/**
 	 * Parse package list that only package descriptions are added to ArrayList.
 	 * 
 	 * @param packages
 	 * @return
 	 */
 	public static ArrayList<String> parseDescription(ArrayList<String> packages) {
 		ArrayList<String> operated = new ArrayList<String>();
 		int ws, start = 0;
 		for (String s : packages) {
 			ws = s.indexOf(" ");
 			//read as many characters forward that non white space is found
 			find: for (int i=1; i+ws<s.length(); i++) {
 				if (s.charAt(ws+i) != ' ') {
 					start = ws+i;
 					break find;
 				}
 			}
 			operated.add(s.substring(start, s.length()));
 		}
 		return operated;
 	}
 	
 }
