 /**
  * Copyright 2013 Digital Reasoning Systems, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.digitalreasoning.herman;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOError;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.net.URLStreamHandler;
 import java.net.URLStreamHandlerFactory;
 import java.util.HashMap;
 import java.util.Map;
import java.util.UUID;
 import java.util.regex.Pattern;
 
 public class HermanUrlStreamHandler extends URLStreamHandler
 {
 	public static final String PROTOCOL = "herman";
 
 	public static final String HERMAN_SEPARATOR = "^/";
 	private static final String JAR_SEPARATOR = "!/";
 
	private static File EXTRACT_DIR;
 	static {
 		doRegister();

		EXTRACT_DIR = new File(System.getProperty("java.io.tmpdir"), "herman-" + UUID.randomUUID());
		EXTRACT_DIR.mkdirs();
 	}
 

 	static void doRegister()
 	{
 		URL.setURLStreamHandlerFactory(new HermanUrlStreamHandler.HermanURLStreamHandlerFactory());
 	}
 
 	public static void register()
 	{
 		// do nothing! - this still registers due to static block if this is the first time that this is called
 	}
 
 	public static class HermanURLStreamHandlerFactory implements URLStreamHandlerFactory
 	{
 		public URLStreamHandler createURLStreamHandler(String protocol)
 		{
 			if (protocol.equals(PROTOCOL))
 			{
 				return new HermanUrlStreamHandler();
 			}
 			return null;
 		}
 	}
 
 	private final Map<String, File> jarFileCache = new HashMap<String, File>();
 
 	private File getJarFile(String jarUrl) throws IOException
 	{
 		if (!jarFileCache.containsKey(jarUrl))
 		{
 			URL url = new URL(jarUrl);
 			URLConnection embededJarCon = url.openConnection();
 			InputStream input = embededJarCon.getInputStream();
			File tempJar = File.createTempFile(PROTOCOL + "-", ".jar", EXTRACT_DIR);
 			tempJar.deleteOnExit();
 			OutputStream output = new FileOutputStream(tempJar);
 
 			try
 			{
 				byte[] buffer = new byte[4096];
 				int n = 0;
 				while (-1 != (n = input.read(buffer)))
 				{
 					output.write(buffer, 0, n);
 				}
 			}
 			catch (Exception e)
 			{
 				throw new IOError(e);
 			}
 			finally
 			{
 				try
 				{
 					if (input != null)
 					{
 						input.close();
 					}
 				}
 				catch (IOException ioe)
 				{
 					throw new IOError(ioe);
 				}
 				try
 				{
 					if (output != null)
 					{
 						output.close();
 					}
 				}
 				catch (IOException ioe)
 				{
 					throw new IOError(ioe);
 				}
 			}
 
 			jarFileCache.put(jarUrl, tempJar);
 
 		}
 		return jarFileCache.get(jarUrl);
 	}
 
 	final Pattern hermanUrlSplitter = Pattern.compile(HERMAN_SEPARATOR, Pattern.LITERAL);
 
 	@Override
 	protected URLConnection openConnection(final URL url) throws IOException
 	{
 		String urlFile = URLDecoder.decode(url.getFile(), "UTF-8");
 
 		if (!urlFile.contains(HERMAN_SEPARATOR))
 		{
 			return new URL(urlFile).openConnection();
 		}
 		String[] parts = hermanUrlSplitter.split(urlFile);
 		if (parts.length > 2)
 		{
 			throw new MalformedURLException("Url " + url + " contains multiple '^' separators.  We cannot handle that.");
 		}
 		String jarUrl = parts[0];
 		String resource = parts.length < 2 ? "" : parts[1];
 
 		File jarFile = getJarFile(jarUrl);
 		return new URL("jar:" + jarFile.toURI().toURL() + HermanUrlStreamHandler.JAR_SEPARATOR + resource).openConnection();
 	}
 }
