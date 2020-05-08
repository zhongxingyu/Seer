 package com.conga.nu;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Finds service providers as instances of dynamic proxies backed by
  * properties file
  *
  * @author Todd Fast
  */
 public class PropertiesServiceFinder extends ServiceFinder {
 
 	/**
 	 *
 	 *
 	 */
 	public PropertiesServiceFinder() {
 		super();
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	public PropertiesServiceFinder(File overrideDirectory) {
 		super();
 		this.overrideDirectory=overrideDirectory;
 	}
 
 
 	/**
 	 *
 	 *
 	 */
 	@Override
 	public <S> Result<S> find(Class<S> serviceClass)
 			throws ServiceInstantiationException {
 
 		try {
 			// Look on the classpath
			String resourceName="META-INF/services/"+
				serviceClass.getName()+".properties";
 			S provider=PropertiesBackedServiceProvider.get(
 				serviceClass,serviceClass,overrideDirectory,resourceName);
 			return new Result(serviceClass,provider,Scope.APPLICATION);
 		}
 		catch (IOException e) {
 			return null;
 		}
 		catch (IllegalArgumentException e) {
 			return null;
 		}
 	}
 
 
 
 
 	////////////////////////////////////////////////////////////////////////////
 	// Fields
 	////////////////////////////////////////////////////////////////////////////
 
 	@AllowField
 	private File overrideDirectory;
 }
