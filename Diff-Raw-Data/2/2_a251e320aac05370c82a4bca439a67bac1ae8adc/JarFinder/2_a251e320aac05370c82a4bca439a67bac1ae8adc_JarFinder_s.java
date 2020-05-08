 /*
  * Copyright 2012 Mozilla Foundation
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mozilla.jarfinder;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public class JarFinder {
 	boolean searchCompleted = false;
 	List<JarMatch> matches = new ArrayList<JarMatch>();
 	String target;
 	String targetClass;
 	String searchPath;
 	
 	public JarFinder(String target, String searchPath) {
 		this.target = target;
 		this.targetClass = target.replaceAll("[.]", "/") + ".class";
 		this.searchPath = searchPath;
 	}
 	
 	public static void printUsage() {
 		System.out.println(String.format("Usage: %s classname dir [ dir2 ] [ dir3 ] [ ... ]", JarFinder.class.getCanonicalName()));
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args == null || args.length < 2) {
 			printUsage();
 			System.exit(-1);
 		}
 		
 		boolean found = false;
 		String target = args[0];
 		for (int i = 1; i < args.length; i++) {
 			String searchPath = args[i];
 			JarFinder finder = new JarFinder(target, searchPath);
 			finder.doSearch();
 			if (finder.matchCount() > 0) {
 				finder.printMatches();
 				found = true;
 			}
 		}
 		
 		if (!found) {
 			System.out.println(String.format("Could not find class '%s' in any jar in the given path(s)", target));
 		}
 	}
 	
 	public void printMatches() {
 		if (!searchCompleted) {
 			doSearch();
 		}
 		
 		System.out.println(String.format("Search results for '%s':", target));
 		for(JarMatch match : matches) {
 			System.out.println(String.format("%s contains the class '%s'", match.getJarName(), match.getClassName()));
 		}
 	}
 	
 	public int matchCount() {
 		if (!searchCompleted) {
 			doSearch();
 		}
 		
 		return matches.size();
 	}
 	
 	public void refreshSearch() {
 		searchCompleted = false;
 		doSearch();
 	}
 	
 	public void doSearch() {
 		if (!searchCompleted) {
 			File searchFile = new File(searchPath);
 			walk(searchFile);
 			searchCompleted = true;
 		}
 	}
 	
 	public void walk(File fileOrDir) {
 		String pattern = ".jar";
 
 		if (fileOrDir == null)
 		    throw new IllegalArgumentException("Can't search null...");
 
 		if (fileOrDir.isFile() && fileOrDir.getName().endsWith(pattern)) {
 		    matches.addAll(searchJar(fileOrDir));
 		} else if (fileOrDir.isDirectory()) {
 		    for (File listFile : fileOrDir.listFiles()) {
 		        if(listFile.isDirectory()) {
 		            walk(listFile);
 		        } else {
 		            if(listFile.getName().endsWith(pattern)) {
 		                matches.addAll(searchJar(listFile));
 		            }
 		        }
 		    }
 		} else {
		    throw new IllegalArgumentException(String.format("Error, '%s' is neither a file nor a directory.", fileOrDir.getName()));
 		}
 	}
 
 	public List<JarMatch> searchJar(File listFile) {
 		JarFile jarFile;
 		List<JarMatch> matches = new ArrayList<JarMatch>();
 		try {
 			jarFile = new JarFile(listFile);
 
 			JarEntry targetJarEntry = jarFile.getJarEntry(targetClass);
 			if (targetJarEntry != null) {
 				matches.add(new JarMatch(listFile.getCanonicalPath(), target));
 			} else {
 			    // If we just have a bare class name, search the whole jar
 			    if (target.matches("^[^.]+$")) {
 			        // No package names.  Search the whole thing.
 			        Enumeration<JarEntry> entries = jarFile.entries();
 			        while (entries.hasMoreElements()) {
 			            JarEntry entry = entries.nextElement();
 			            String name = entry.getName();
 			            if (name.endsWith("/" + targetClass)) {
 			                //System.out.println("Found name: " + name + " in jar " + listFile.getCanonicalPath());
 			                matches.add(new JarMatch(listFile.getCanonicalPath(), pathToClassName(name)));
 			            }
 			        }
 			    }
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return matches;
 	}
 
     private String pathToClassName(String name) {
         if (name == null) return null;
         String className = name.replaceAll("[/]", ".");
         className = className.replaceFirst("^\\.", "");
         className = className.replaceFirst("\\.class$", "");
         return className;
     }
 }
 
 class JarMatch {
     private String jarName;
     private String className;
 
     public String getJarName() { return jarName; }
     public void setJarName(String jarName) { this.jarName = jarName; }
     public String getClassName() { return className; }
     public void setClassName(String className) { this.className = className; }
 
     public JarMatch(String jarName, String className) {
         this.jarName = jarName;
         this.className = className;
     }
 }
