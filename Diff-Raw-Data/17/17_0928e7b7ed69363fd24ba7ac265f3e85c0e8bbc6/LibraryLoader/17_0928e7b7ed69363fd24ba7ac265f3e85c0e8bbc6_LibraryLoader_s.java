 package org.lightmare.libraries;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Class for load jar or class files from specified path
  * 
  * @author levan
  * 
  */
 public class LibraryLoader {
 
 	/**
 	 * Gets all jar or class subfiles from specified {@link File} recursively
 	 * 
 	 * @param file
 	 * @param urls
 	 * @throws MalformedURLException
 	 */
 	public static void getSubfiles(File file, Set<URL> urls)
 			throws MalformedURLException {
 		if (file.isDirectory()) {
 			File[] subFiles = file.listFiles(new FilenameFilter() {
 
 				@Override
 				public boolean accept(File file, String name) {
 					return name.endsWith(".jar") || name.endsWith(".class")
 							|| file.isDirectory();
 				}
 			});
 			if (subFiles.length == 0) {
 				return;
 			}
 			for (File subFile : subFiles) {
 				if (subFile.isDirectory()) {
 					getSubfiles(subFile, urls);
 				} else {
 					urls.add(subFile.toURI().toURL());
 				}
 			}
 		} else {
 			urls.add(file.toURI().toURL());
 		}
 	}
 
 	public static ClassLoader getEnrichedLoader(File file, Set<URL> urls)
 			throws MalformedURLException {
 		getSubfiles(file, urls);
 		URL[] paths = urls.toArray(new URL[urls.size()]);
 		URLClassLoader urlLoader = URLClassLoader.newInstance(paths, Thread
 				.currentThread().getContextClassLoader());
 		return urlLoader;
 	}
 
 	public static ClassLoader getEnrichedLoader(URL[] urls)
 			throws MalformedURLException {
 		URLClassLoader urlLoader = null;
 		if (urls.length > 0) {
 			urlLoader = URLClassLoader.newInstance(urls, Thread.currentThread()
 					.getContextClassLoader());
 		}
 		return urlLoader;
 	}
 
 	public static void loadCurrentLibraries(ClassLoader loader) {
 		if (loader != null) {
 			Thread.currentThread().setContextClassLoader(loader);
 		}
 	}
 
 	public static void loadEarLibraries() {
 
 	}
 
 	/**
 	 * Loads jar or class files to the current thread from libraryPath
 	 * recursively
 	 * 
 	 * @param libraryPath
 	 * @throws MalformedURLException
 	 */
 	public static void loadLibraries(String libraryPath)
 			throws MalformedURLException {
 		Thread currentThread = Thread.currentThread();
 		ClassLoader loader = currentThread.getContextClassLoader();
 		File file = new File(libraryPath);
 		if (file.exists()) {
 			Set<URL> urls = new HashSet<URL>();
 			getSubfiles(file, urls);
 			URL[] paths = urls.toArray(new URL[urls.size()]);
 			URLClassLoader urlLoader = URLClassLoader
 					.newInstance(paths, loader);
 			currentThread.setContextClassLoader(urlLoader);
 		}
 	}
 }
