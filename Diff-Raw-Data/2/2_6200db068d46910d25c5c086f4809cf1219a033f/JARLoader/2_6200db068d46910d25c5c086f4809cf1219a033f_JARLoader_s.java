 /*
  * Copyright 2011 Jonathan Anderson
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
 package me.footlights.boot;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.JarURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.CodeSource;
 import java.security.PrivilegedAction;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 
 /** Loads classes from a single JAR file. */
 class JARLoader
 {
 	/**
 	 * Open a JAR file, from which we will load classes later.
 	 *
 	 * @param url      a URL, no "jar:" prefix (e.g. "file:///home/...", "http://foo.com/...")
 	 */
 	public static JARLoader open(final URL url) throws IOException
 	{
 		if (!url.getProtocol().equals("jar"))
 			throw new MalformedURLException("JAR URL does not start with 'jar:' '" + url + "'");
 
 		JarFile jar = new JAROpener().open(url);
 		if (jar.getManifest() == null)
 			throw new SecurityException("The jar file is not signed");
 
 		return new JARLoader(jar, url);
 	}
 
 
 	public JarFile getJarFile() { return jar; }
 
 
 	/** Read a class' bytecode. */
 	public Bytecode readBytecode(String className)
 		throws ClassNotFoundException, IOException
 	{
 		for (Enumeration<JarEntry> i = jar.entries() ; i.hasMoreElements() ;)
 		{
 			JarEntry entry = i.nextElement();
 
 			if (entry.isDirectory()) continue;
 			if (entry.getName().startsWith("META-INF/")) continue;
 
 			// read the JAR entry (to make sure it's actually signed)
 			InputStream is = jar.getInputStream(entry);
 			int avail = is.available();
 			byte[] buffer = new byte[avail];
 			is.read(buffer);
 			is.close();
 
 			if (entry.getName().equals(className.replace('.', '/') + ".class"))
 			{
 				if (entry.getCodeSigners() == null)
 					throw new Error(entry.toString() + " not signed");
 
 				Bytecode bytecode = new Bytecode();
 				bytecode.raw = buffer;
 				bytecode.source = new CodeSource(url, entry.getCodeSigners());
 
 				return bytecode;
 			}
 		}
 
		throw new ClassNotFoundException();
 	}
 
 
 	/** Private constructor. */
 	private JARLoader(JarFile jar, URL url) throws MalformedURLException
 	{
 		this.jar = jar;
 		this.url = url;
 	}
 
 
 	/** Opens a {@link JarFile}, or on failure, provides a means to return an {@link Exception}. */
 	private static class JAROpener implements PrivilegedAction<JarFile>
 	{
 		synchronized JarFile open(URL url) throws IOException
 		{
 			if (url.toExternalForm().startsWith("jar:")) this.url = url;
 			else this.url = new URL("jar:" + url + "!/");
 
 			JarFile jar = AccessController.doPrivileged(this);
 
 			if (jar == null) throw error;
 			else return jar;
 		}
 
 		@Override public synchronized JarFile run()
 		{
 			try { return ((JarURLConnection) url.openConnection()).getJarFile(); }
 			catch(IOException e)
 			{
 				error = e;
 				return null;
 			}
 		}
 
 		private URL url;
 		private IOException error;
 	};
 
 
 	/** The {@link JarFile} that we are loading classes from. */
 	private final JarFile jar;
 
 	/** Where {@link #jar} came from. */
 	private final URL url;
 }
