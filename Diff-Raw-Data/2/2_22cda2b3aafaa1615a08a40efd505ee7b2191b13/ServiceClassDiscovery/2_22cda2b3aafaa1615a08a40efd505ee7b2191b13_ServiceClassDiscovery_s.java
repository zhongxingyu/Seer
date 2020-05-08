 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.discover;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.jbasics.checker.ContractCheck;
 import org.jbasics.configuration.properties.SystemProperty;
 
 /**
  * Helper to discover an concrete class for an abstract one base on the java services concept.
  * 
  * @author Stephan Schloepke
  * @since 1.0
  */
 public class ServiceClassDiscovery {
 	public final static String RESOURCE_BASE = "META-INF/services/"; //$NON-NLS-1$
 	public static final String SERVICE_FILE_ENCODING = "UTF-8"; //$NON-NLS-1$
 	public static final char COMMENT_CHARACTER = '#';
 
 	public static <T> Class<? extends T> discoverImplementation(final Class<T> abstractClass) {
 		return ServiceClassDiscovery.discoverImplementation(abstractClass, null, ServiceClassDiscovery.getClassLoader(abstractClass));
 	}
 
 	public static <T> Class<? extends T> discoverImplementation(final Class<T> abstractClass, final Class<? extends T> defaultImplementation) {
 		return ServiceClassDiscovery
 				.discoverImplementation(abstractClass, defaultImplementation, ServiceClassDiscovery.getClassLoader(abstractClass));
 	}
 
 	public static <T> Class<? extends T> discoverImplementation(final Class<T> abstractClass, final Class<? extends T> defaultImplementation,
 			final ClassLoader loader) {
 		Class<?> temp = SystemProperty.classProperty(ContractCheck.mustNotBeNull(abstractClass, "abstractClass").getName(), null).value(); //$NON-NLS-1$
 		if (temp == null) {
 			String resourceName = ServiceClassDiscovery.RESOURCE_BASE + abstractClass.getName();
 			URL resource = ContractCheck.mustNotBeNull(loader, "loader").getResource(resourceName); //$NON-NLS-1$
 			if (resource != null) {
 				try {
 					Set<String> found = ServiceClassDiscovery.parseURL(resource, new LinkedHashSet<String>());
 					if (found.size() > 0) {
 						String clazzName = found.iterator().next();
						temp = Class.forName(clazzName);
 					}
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				} catch (ClassNotFoundException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		return temp == null ? defaultImplementation : temp.asSubclass(abstractClass);
 	}
 
 	public static <T> Set<Class<? extends T>> discoverClasses(final Class<T> abstractClass) throws IOException, ClassNotFoundException {
 		return ServiceClassDiscovery.discoverClasses(abstractClass, ServiceClassDiscovery.getClassLoader(abstractClass));
 	}
 
 	public static <T> Set<Class<? extends T>> discoverClasses(final Class<T> abstractClass, final ClassLoader loader) throws IOException,
 			ClassNotFoundException {
 		Set<String> classNames = ServiceClassDiscovery.discoverClassNames(abstractClass, loader);
 		Set<Class<? extends T>> classes = new LinkedHashSet<Class<? extends T>>();
 		for (String className : classNames) {
 			classes.add(Class.forName(className, true, loader).asSubclass(abstractClass));
 		}
 		return classes;
 	}
 
 	public static Set<String> discoverClassNames(final Class<?> abstractClass) throws IOException {
 		return ServiceClassDiscovery.discoverClassNames(abstractClass, ServiceClassDiscovery.getClassLoader(abstractClass));
 	}
 
 	public static Set<String> discoverClassNames(final Class<?> abstractClass, final ClassLoader loader) throws IOException {
 		String resourceName = ServiceClassDiscovery.RESOURCE_BASE + ContractCheck.mustNotBeNull(abstractClass, "abstractClass").getName(); //$NON-NLS-1$
 		Enumeration<URL> resources = ContractCheck.mustNotBeNull(loader, "loader").getResources(resourceName); //$NON-NLS-1$
 		Set<String> classNames = new LinkedHashSet<String>();
 		while (resources.hasMoreElements()) {
 			ServiceClassDiscovery.parseURL(resources.nextElement(), classNames);
 		}
 		return classNames;
 	}
 
 	private static Set<String> parseURL(final URL url, final Set<String> found) throws IOException {
 		assert found != null;
 		InputStream in = url.openStream();
 		InputStreamReader inputReader = null;
 		LineNumberReader reader = null;
 		try {
 			inputReader = new InputStreamReader(in, ServiceClassDiscovery.SERVICE_FILE_ENCODING);
 			reader = new LineNumberReader(inputReader);
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				line = ServiceClassDiscovery.stripComment(line);
 				if (!Character.isJavaIdentifierStart(line.charAt(0))) {
 					throw new RuntimeException("Illegal class name found (" + line + ") at line " + reader.getLineNumber() + " in service file " //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 							+ url);
 				}
 				for (int i = 1; i < line.length(); i++) {
 					char c = line.charAt(i);
 					if (c != '.' && !Character.isJavaIdentifierPart(c)) {
 						throw new RuntimeException("Illegal class name found (" + line + ") at line " + reader.getLineNumber() + " in service file " //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 								+ url);
 					}
 				}
 				found.add(line);
 			}
 		} finally {
 			try {
 				if (reader != null) {
 					reader.close();
 				} else if (inputReader != null) {
 					inputReader.close();
 				} else {
 					in.close();
 				}
 			} catch (IOException e) {
 				// Ignore any close exception
 			}
 		}
 		return found;
 	}
 
 	private static String stripComment(String line) {
 		line = line.trim();
 		if (line.length() == 0) {
 			return ""; //$NON-NLS-1$
 		}
 		int commentStart = line.indexOf(ServiceClassDiscovery.COMMENT_CHARACTER);
 		if (commentStart == 0) {
 			return ""; //$NON-NLS-1$
 		} else if (commentStart > 0) {
 			line = line.substring(0, commentStart);
 		}
 		return line;
 	}
 
 	private static ClassLoader getClassLoader(final Class<?> abstractClass) {
 		try {
 			return Thread.currentThread().getContextClassLoader();
 		} catch (Throwable e) {
 			if (abstractClass != null) {
 				return abstractClass.getClassLoader();
 			} else {
 				return ServiceClassDiscovery.class.getClassLoader();
 			}
 		}
 	}
 }
