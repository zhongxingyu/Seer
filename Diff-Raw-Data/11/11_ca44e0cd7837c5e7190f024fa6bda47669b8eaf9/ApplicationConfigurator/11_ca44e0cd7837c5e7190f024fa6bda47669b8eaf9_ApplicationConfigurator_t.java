 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.application;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import com.nginious.http.annotation.Controller;
 import com.nginious.http.annotation.Service;
 
 class ApplicationConfigurator {
 	
 	private File warFileOrAppDir;
 	
 	private String name;
 	
 	ApplicationConfigurator(File warFileOrAppDir) {
 		this.warFileOrAppDir = warFileOrAppDir;
 		this.name = extractApplicationName(warFileOrAppDir);
 	}
 	
 	ApplicationConfigurator(String name, File warFileOrAppDir) {
 		this.name = name;
 		this.warFileOrAppDir = warFileOrAppDir;
 	}
 	
 	private String extractApplicationName(File warFileOrAppDir) {
 		String localName = warFileOrAppDir.getName();
 		
 		if(localName.endsWith(".war")) {
 			localName = localName.substring(0, localName.length() - 4);
 		}
 		
 		return localName;
 	}
 	
 	boolean isWarApp() {
 		return warFileOrAppDir.isFile();
 	}
 	
 	ApplicationImpl configure() throws ApplicationException {
 		return configure(this.warFileOrAppDir);
 	}
 	
 	private ApplicationImpl configure(File warFileOrAppDir) throws ApplicationException {
 		ApplicationImpl application = new ApplicationImpl(this.name);
 		
 		HashSet<ClassInfo> classes = new HashSet<ClassInfo>();
 		
 		if(warFileOrAppDir.isFile()) {
 			deployFromWar(application, warFileOrAppDir, classes);
 		} else {
 			deployFromDir(application, warFileOrAppDir, classes);
 		}
 		
 		boolean done = false;
 		
 		try {
 			ClassLoader classLoader = application.getClassLoader(); 
 			findServiceClasses(application, classLoader, classes);
 			done = true;
 			return application;
 		} catch(IOException e) {
 			throw new ApplicationException("Unable to publish application '" + this.name + "'", e);
 		} finally {
 			if(!done) {
 				application.unpublish();
 			}
 		}
 	}
 	
 	private void deployFromWar(ApplicationImpl application, File warFile, HashSet<ClassInfo> classes) throws ApplicationException {
 		File tmpDir = createTempDir();
 		boolean done = false;
 		JarFile jar = null;
 				
 		try {
 			jar = new JarFile(warFile);
 			Enumeration<JarEntry> entries = jar.entries();
 			
 			while(entries.hasMoreElements()) {
 				JarEntry entry = entries.nextElement();
 				String name = entry.getName();
 				
 				if(!entry.isDirectory()) {
 					writeEntry(jar, entry, tmpDir);
 					
 					if(name.startsWith("WEB-INF/lib/") && name.endsWith(".jar")) {
 						findJarClasses(tmpDir, name, classes);
 					}
 				} else if(name.equals("WEB-INF/classes/")) {
 					File classPathDir = new File(tmpDir, name);
 					String classPathDirName = classPathDir.getAbsolutePath();
 					
 					if(!classPathDirName.endsWith("/")) {
 						classPathDirName = classPathDirName + "/";
 					}
 				}
 				
 				if(name.startsWith("WEB-INF/classes/") && name.endsWith(".class")) {
 					String className = name.substring(16, name.length() - 6).replace('/', '.');
 					ClassInfo classFile = new ClassInfo(className, null);
 					classes.add(classFile);
 				}
 			}
 			
			application.setBaseDir(tmpDir);
			application.setWar(true);			
 			done = true;
 		} catch(IOException e) {
 			throw new ApplicationException("Unable to publish '" + this.name + "' from war archive " + warFile.getAbsolutePath(), e);
 		} finally {
 			if(jar != null) {
 				try { jar.close(); } catch(IOException e) {}
 			}
 			
 			if(!done) {
 				application.unpublish();
 			}
 		}
 	}
 	
 	private void deployFromDir(ApplicationImpl application, File appDir, HashSet<ClassInfo> classes) throws ApplicationException {
 		application.setBaseDir(appDir);
 		application.setDirectory(true);
 		ArrayList<String> files = new ArrayList<String>();
 		createFileList(appDir, appDir, files);
 		
 		try {
 			for(String file : files) {
 				if(file.startsWith("/WEB-INF/lib/") && file.endsWith(".jar")) {
 					findJarClasses(appDir, file, classes);
 				} else if(file.equals("/WEB-INF/classes")) {
 					File classPathDir = new File(appDir, file);
 					String classPathDirName = classPathDir.getAbsolutePath();
 					
 					if(!classPathDirName.endsWith("/")) {
 						classPathDirName = classPathDirName + "/";
 					}
 				}
 				
 				if(file.startsWith("/WEB-INF/classes/") && file.endsWith(".class")) {
 					String className = file.substring(17, file.length() - 6).replace('/', '.');
 					File classFile = new File(appDir, file);
 					ClassInfo classInfo = new ClassInfo(className, classFile);
 					classes.add(classInfo);
 				}
 			}
 		} catch(IOException e) {
 			throw new ApplicationException("Unable to publish '" + this.name + "' from directory " + appDir.getAbsolutePath(), e);
 		}
 	}
 	
 	private void createFileList(File baseDir, File dir, List<String> files) {
 		File[] subFiles = dir.listFiles();
 		
 		for(File subFile : subFiles) {
 			String subFileName = subFile.getAbsolutePath();
 			String baseDirName = baseDir.getAbsolutePath();
 			files.add(subFileName.substring(baseDirName.length()));
 			
 			if(subFile.isDirectory()) {
 				createFileList(baseDir, subFile, files);
 			}
 		}
 	}
 	
 	private void findJarClasses(File appDir, String jarName, HashSet<ClassInfo> classes) throws IOException {
 		File jarFile = new File(appDir, jarName);
 		JarFile jar = new JarFile(jarFile);
 		Enumeration<JarEntry> entries = jar.entries();
 		
 		while(entries.hasMoreElements()) {
 			JarEntry entry = entries.nextElement();
 			String name = entry.getName();
 			
 			if(name.endsWith(".class")) {
 				String className = name.substring(0, name.length() - 6).replace('/', '.');
 				ClassInfo classFile = new ClassInfo(className, null);
 				classes.add(classFile);
 			}
 		}
 	}
 	
 	private void findServiceClasses(ApplicationImpl application, ClassLoader classLoader, HashSet<ClassInfo> classes) throws ApplicationException, IOException {
 		try {
 			for(ClassInfo classFile : classes) {
 				try {
 					String className = classFile.getClassName();
 					Class<?> clazz = classLoader.loadClass(className);
 					
 					if(clazz.isAnnotationPresent(Controller.class)) {
 						Object controller = clazz.newInstance();
 						File file = classFile.getClassFile();
 						application.addController(controller, file, file != null);
 					} else if(clazz.isAnnotationPresent(Service.class)) {
 						Object service = clazz.newInstance();
 						application.addService(service);
 					}
 				} catch(NoClassDefFoundError e) {
 					// Ignore classes that can't be loaded
 				} catch(ClassNotFoundException e) {
 					// Ignore classes that can't be loaded
 				}
 			}
 		} catch(Exception e) {
 			throw new IOException("Unable to load classes", e);
 		}
 	}
 	
 	private void writeEntry(JarFile jar, JarEntry entry, File appDir) throws IOException {
 		InputStream in = null;
 		FileOutputStream out = null;
 		
 		try {
 			String name = entry.getName();
 			File destFile = new File(appDir, name);
 			
 			if(!destFile.exists()) {
 				String parent = destFile.getParent();
 				File parentDir = new File(parent);
 				
 				parentDir.mkdirs();
 				
 				if(!parentDir.exists()) {
 					throw new IOException("Unable to create directory '" + parentDir.getAbsolutePath() + "' to extract jar entry '" + entry.getName() + "'");
 				}
 			}
 			
 			in = jar.getInputStream(entry);
 			out = new FileOutputStream(destFile);
 			byte[] buff = new byte[1024];
 			int len = 0;
 			
 			while((len = in.read(buff)) > 0) {
 				out.write(buff, 0, len);
 			}
 			
 			out.flush();
 		} finally {
 			if(in != null) {
 				try { in.close(); } catch(IOException e) {}
 			}
 			
 			if(out != null) {
				try { out.close(); } catch(IOException e) {}
 			}
 		}
 	}
 	
 	private File createTempDir() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
 		StringBuffer appDirName = new StringBuffer("ProjectX_");
 		File file = new File(this.name);
 		appDirName.append(file.getName());
 		appDirName.append("__");
 		appDirName.append(System.currentTimeMillis());
 		File appDir = new File(tmpDir, appDirName.toString());
 		appDir.mkdir();
 		return appDir;
 	}
 	
 	private class ClassInfo {
 		
 		private String className;
 		
 		private File classFile;
 		
 		private ClassInfo(String className, File classFile) {
 			this.className = className;
 			this.classFile = classFile;
 		}
 		
 		private String getClassName() {
 			return this.className;
 		}
 		
 		private File getClassFile() {
 			return this.classFile;
 		}
 	}
 }
