 package com.conga.nu;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 /**
  * Typesafe facade for a properties file using a dynamic proxy
  *
  * @author Todd Fast
  */
 public class PropertiesBackedServiceProvider<S> implements InvocationHandler {
 
 	/**
 	 *
 	 *
 	 */
 	protected PropertiesBackedServiceProvider(Class<S> serviceClass,
 			String resourceName)
 			throws IOException
 	{
 		this(serviceClass,serviceClass,null,resourceName);
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	protected PropertiesBackedServiceProvider(Class<S> serviceClass,
 			Class<?> contextClass, String resourceName)
 			throws IOException
 	{
 		this(serviceClass,contextClass,null,resourceName);
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	protected PropertiesBackedServiceProvider(Class<S> serviceClass,
 			Class<?> contextClass, File overrideDirectory,
 			String resourceName)
 			throws IOException {
 
 		boolean foundProperties=false;
 
 		this.serviceClass=serviceClass;
 		this.resourceName=resourceName;
 		this.overrideDirectory=overrideDirectory;
 
 		// First load from the filesystem, if specified
 		if (overrideDirectory!=null) {
			File file=new File(overrideDirectory,resourceName);
 			if (file.exists()) {
 				if (!file.canRead()) {
 					System.err.println("Found service provider properties "+
 						"file at \""+file+"\" but the file's access rights "+
 						"don't allow it to be read");
 				}
 				else {
 					loadProperties(file.getCanonicalPath(),
 						new FileInputStream(file));
 					foundProperties=true;
 				}
 			}
 		}
 
 		// Then load from the classpath
		foundProperties = loadFromClasspath(contextClass,resourceName)
 			|| foundProperties;
 
 		// Last, try to load default property file from the classpath
		String defaultResourceName=resourceName.replace(
			".properties",".default.properties");
		foundProperties = loadFromClasspath(contextClass,defaultResourceName)
 			|| foundProperties;
 
 
 		if (!foundProperties) {
 			final String ERROR_MESSAGE=
 				"No service provider property file \""+resourceName+
 				"\" could be loaded.";
 			throw new IllegalArgumentException(ERROR_MESSAGE);
 		}
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	private boolean loadFromClasspath(Class<?> contextClass,
 			String resourceName)
 			throws IOException {
 
 		// Then load form the classpath
 		Enumeration<URL> e=contextClass.getClassLoader()
 			.getResources(resourceName);
 		if (e==null || !e.hasMoreElements()) {
 			return false;
 		}
 
 		// Load all property files on the classpath
 		while (e.hasMoreElements()) {
 			loadProperties(resourceName,e.nextElement().openStream());
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * Loads the properties from the input stream and populates the map of
 	 * properties. Note that properties found first in the classpath will
 	 * take precedence to same-named properties found later.
 	 *
 	 */
 	private void loadProperties(String source, InputStream in)
 			throws IOException {
 
 		Properties loadedProperties=new Properties();
 
 		try {
 			// Merge these properties with the base properties
 			loadedProperties.load(in);
 
 			// We have to load all the values into a Map<String,String>
 			// manually. We know the cast is safe because we're loading from
 			// the properties file directly ourselves. Existing properties
 			// take precedence.
 			HashMap<String,String> map=new HashMap<String,String>();
 			for (Entry<Object,Object> entry: loadedProperties.entrySet()) {
 				String key=(String)entry.getKey();
 				if (properties.get(key)==null) {
 					properties.put(key,(String)entry.getValue());
 				}
 			}
 
 			System.out.println(String.format(
 				"Loaded service provider properties file for service "+
 				"%s from %s: %s",serviceClass.getName(),source,
 				properties.toString()));
 		}
 		finally {
 			try {
 				if (in!=null) {
 					in.close();
 				}
 			}
 			catch (IOException ioe) {
 				// Ignore
 			}
 		}
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	public Class<S> getServiceClass() {
 		return serviceClass;
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	public String getResourceName() {
 		return resourceName;
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	public Map<String,String> getProperties() {
 		return properties;
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) {
 
 		String methodName=method.getName();
 
 		// Decapitalize the property name
 		String propertyName=null;
 		if (methodName.equals("toString") && args==null) {
 			return serviceClass!=null
 				? serviceClass.getName().toString()+
 					"["+PropertiesBackedServiceProvider.this.toString()+"]"
 				: PropertiesBackedServiceProvider.this.toString();
 		}
 		else
 		if (methodName.equals("getProperties") && args==null) {
 			return this.getProperties();
 		}
 		else
 		if (methodName.startsWith("get") && methodName.length() > 3) {
 			String initialChar=methodName.substring(3,4);
 			propertyName=initialChar.toLowerCase()+methodName.substring(4);
 		}
 		else
 		if (methodName.startsWith("is") && methodName.length() > 2) {
 			String initialChar=methodName.substring(2,3);
 			propertyName=initialChar.toLowerCase()+methodName.substring(3);
 		}
 		else {
 			// Ignore all other method calls
 			Class<?>[] paramTypes=method.getParameterTypes();
 			StringBuilder paramTypesString=new StringBuilder();
 			if (paramTypes!=null) {
 				for (int i=0; i<paramTypes.length; i++) {
 					if (i > 0) {
 						paramTypesString.append(", ");
 					}
 
 					paramTypesString.append(paramTypes[i].getName());
 				}
 			}
 
 			System.err.println("Ignoring call to method "+methodName+"("+
 				paramTypesString+")::"+method.getReturnType().getName()+
 				" on properties-based service provider for service class "+
 				serviceClass.getName());
 		}
 
 		if (propertyName!=null) {
 			// Convert the value to the target type
 			String value=getProperties().get(propertyName);
 
 			if ((value==null || value.trim().isEmpty())
 					&& method.getReturnType().isPrimitive()) {
 				// This will result in an NPE that is very hard to trace
 				// Instead, throw a decent exceptions
 				throw new RuntimeException("No value was found for the "+
 					"key \""+propertyName+"\" in resource \""+
 					getResourceName()+"\"");
 			}
 			else {
 				return TypeConverter.asType(method.getReturnType(),value);
 			}
 		}
 		else {
 			return null;
 		}
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	protected int getInteger(String property, Integer defaultValue) {
 		String value=(String)getProperties().get(property);
 		try {
 			if (value==null) {
 				return defaultValue;
 			}
 			else {
 				return Integer.parseInt(value);
 			}
 		}
 		catch (NumberFormatException e) {
 			System.err.println("WARNING: Value for configuration property "+
 				property+" was not a number (value = \""+value+"\")");
 			return defaultValue;
 		}
 	}
 
 
 
 
 	////////////////////////////////////////////////////////////////////////////
 	// Factory methods
 	////////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Create a proxied configuration object using the config interface as
 	 * the context class for loading the specified resource
 	 *
 	 */
 	public static <C> C get(Class<C> serviceClass,
 			String resourceName)
 			throws IOException {
 
 		return serviceClass.cast(Proxy.newProxyInstance(
 			serviceClass.getClassLoader(),
 			new Class<?>[] { serviceClass },
 			new PropertiesBackedServiceProvider<C>(
 				serviceClass,serviceClass,null,resourceName){}));
 	}
 
 
 	/**
 	 * Create a proxied configuration object using the specified context class
 	 * as the context class for loading the specified resource
 	 *
 	 */
 	public static <C> C get(Class<C> serviceClass,
 			Class<?> contextClass, String resourceName)
 			throws IOException {
 
 		return serviceClass.cast(Proxy.newProxyInstance(
 			contextClass.getClassLoader(),
 			new Class<?>[] {serviceClass},
 			new PropertiesBackedServiceProvider<C>(
 				serviceClass,contextClass,null,resourceName){}));
 	}
 
 
 	/**
 	 * Create a proxied configuration object using the specified context class
 	 * as the context class for loading the specified resource
 	 *
 	 */
 	public static <C> C get(Class<C> serviceClass,
 			Class<?> contextClass, File overrideDirectory,
 			String resourceName)
 			throws IOException {
 
 		return serviceClass.cast(Proxy.newProxyInstance(
 			contextClass.getClassLoader(),
 			new Class<?>[] {serviceClass},
 			new PropertiesBackedServiceProvider<C>(
 				serviceClass,contextClass,overrideDirectory,resourceName){}));
 	}
 
 
 
 
 	////////////////////////////////////////////////////////////////////////////
 	// Variables
 	////////////////////////////////////////////////////////////////////////////
 
 	private Class<S> serviceClass=null;
 	private String resourceName=null;
 	private File overrideDirectory=null;
 	private Map<String,String> properties=new HashMap<String,String>();
 }
