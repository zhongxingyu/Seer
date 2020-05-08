 /*
  * Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of
  * conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list
  * of conditions and the following disclaimer in the documentation and/or other materials
  * provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Robert 'Bobby' Zenz ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Robert 'Bobby' Zenz OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Robert 'Bobby' Zenz.
  */
 package org.bonsaimind.minecraftmiddleknife;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A static helper that let's you extend the System- and Thread-Classloader.
  * This is mostly from here: http://stackoverflow.com/questions/252893/how-do-you-change-the-classpath-within-java
  */
 public final class ClassLoaderExtender {
 
 	private ClassLoaderExtender() {
 		throw new AssertionError(); // You're not supposed to instanciate this class.
 	}
 
 	/**
 	 * Adds the given URLs to the classloeaders.
 	 * @param urls
 	 * @throws ClassLoaderExtensionException
 	 */
 	public static void extend(URL... urls) throws ClassLoaderExtensionException {
 		// Extend the ClassLoader of the current thread.
 		URLClassLoader loader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
 
 		// Extend the SystemClassLoader...this is needed for mods which will
 		// use the WhatEver.getClass().getClassLoader() method to retrieve
 		// a ClassLoader.
 		URLClassLoader systemLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 
 		// Get the method via reflection.
 		Method addURLMethod;
 		try {
 			addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
 			addURLMethod.setAccessible(true);
 
 			for (URL url : urls) {
 				addURLMethod.invoke(systemLoader, url);
 				addURLMethod.invoke(loader, url);
 			}
 		} catch (IllegalAccessException ex) {
 			throw new ClassLoaderExtensionException("Failed to extend the ClassLoader.", ex);
 		} catch (IllegalArgumentException ex) {
 			throw new ClassLoaderExtensionException("Failed to extend the ClassLoader.", ex);
 		} catch (InvocationTargetException ex) {
 			throw new ClassLoaderExtensionException("Failed to extend the ClassLoader.", ex);
 		} catch (NoSuchMethodException ex) {
 			throw new ClassLoaderExtensionException("Failed to extend the ClassLoader.", ex);
 		} catch (SecurityException ex) {
 			throw new ClassLoaderExtensionException("Failed to extend the ClassLoader.", ex);
 		}
 	}
 
 	/**
 	 * Walks recursively through the given paths and loads all jars.
 	 * @param paths
 	 * @exception ClassLoaderExtensionException
 	 */
 	public static void extendFrom(String... paths) throws ClassLoaderExtensionException {
 		try {
 			List<URL> jars = findJars(paths);
 			extend(jars.toArray(new URL[jars.size()]));
 		} catch (MalformedURLException ex) {
 			throw new ClassLoaderExtensionException("Seems like the gods are against you today.", ex);
 		}
 	}
 
 	/**
 	 * Walks recursively through all given paths and returns the jars.
 	 * @param paths
 	 * @return
 	 * @throws MalformedURLException
 	 */
 	public static List<URL> findJars(String... paths) throws MalformedURLException {
 		List<URL> urls = new ArrayList<URL>();
 
 		for (String path : paths) {
 			for (String child : new File(path).list()) {
 				File file = new File(path, child);
 
 				if (file.isDirectory()) {
					urls.addAll(findJars(file.getAbsolutePath()));
 				} else if (file.isFile()) {
 					urls.add(file.toURI().toURL());
 				}
 			}
 		}
 
 		return urls;
 	}
 }
