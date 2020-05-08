 /*
  * Copyright 2014 Robert 'Bobby' Zenz. All rights reserved.
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
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Creates a new {@link URLClassLoader} with the jars which are set.
  * <p/>
  * This is a mere and simple wrapper around the constructor of the
  * {@link URLClassLoader}.
  * <p/>
  * Usage example:
  * 
  * <pre>
  * ClassLoaderCreator classLoaderCreator = new ClassLoaderCreator();
  * 
  * classLoaderCreator.add(someUrl);
  * classLoaderCreator.add(anotherUrl);
  * classLoaderCreator.add(&quot;/path/to/the.jar&quot;);
  * 
  * ClassLoader classLoader = creator.createClassLoader();
  * </pre>
  */
 public final class ClassLoaderCreator {
 	
 	private List<URL> jars = new ArrayList<URL>();
 	
 	/**
 	 * Creates a new instance of {@link ClassLoaderCreator}.
 	 */
 	public ClassLoaderCreator() {
 	}
 	
 	/**
 	 * Adds the given {@link URL} to the list.
 	 * 
 	 * @param jar the {@link URL} to add.
 	 */
 	public void add(URL jar) {
 		jars.add(jar);
 	}
 	
 	/**
 	 * Adds the given {@link File} to the list after converting it into an
 	 * {@link URL}.
 	 * 
 	 * @param jar the {@link File} to add.
 	 * @throws MalformedURLException If a protocol handler for the URL could not
 	 *             be found, or if some other error occurred while constructing
 	 *             the URL
 	 */
 	public void add(File jar) throws MalformedURLException {
 		add(jar.toURI().toURL());
 	}
 	
 	/**
 	 * Adds the given path to the list after converting it into an {@link URL}.
 	 * 
 	 * @param jar the path to add.
 	 * @throws MalformedURLException If a protocol handler for the URL could not
 	 *             be found, or if some other error occurred while constructing
 	 *             the URL
 	 */
 	public void add(String jar) throws MalformedURLException {
 		add(new File(jar));
 	}
 	
 	/**
 	 * Searches through the given path downwards and adss all jars that are
 	 * found.
 	 * 
 	 * @param dir the directory in which to start.
 	 * @throws MalformedURLException If a protocol handler for the URL could not
 	 *             be found, or if some other error occurred while constructing
 	 *             the URL
 	 */
 	public void addRecursively(File dir) throws MalformedURLException {
 		for (File file : dir.listFiles()) {
 			if (file.isFile() && file.getName().endsWith(".jar")) {
 				add(file);
 			} else if (file.isDirectory()) {
 				addRecursively(file);
 			}
 		}
 	}
 	
 	/**
 	 * Searches through the given path downwards and adss all jars that are
 	 * found.
 	 * 
 	 * @param dir the directory in which to start.
 	 * @throws MalformedURLException If a protocol handler for the URL could not
 	 *             be found, or if some other error occurred while constructing
 	 *             the URL
 	 */
	public void addRecursively(String dir) throws MalformedURLException {
 		addRecursively(new File(dir));
 	}
 	
 	/**
 	 * Creates a new {@link ClassLoader} from the list.
 	 * 
 	 * @return the {@link ClassLoadeR} you wanted all along.
 	 */
 	public URLClassLoader createClassLoader() {
 		URL[] urls = new URL[jars.size()];
 		return new URLClassLoader(jars.toArray(urls));
 	}
 }
