 package org.clubrockisen.service.abstracts;
 
 import java.util.logging.Logger;
 
 import org.clubrockisen.common.error.ServiceInstantiationError;
 
 /**
  * Factory for the services of the application.<br />
  * @author Alex
  */
 public abstract class ServiceFactory {
 	/** Logger */
 	private static Logger			lg	= Logger.getLogger(ServiceFactory.class.getName());
 	
 	/** Implementation of the factory to be used */
 	private static ServiceFactory	implementation;
 	
 	/**
 	 * Return the implementation of the factory to be used.
 	 * @return the concrete implementation
 	 */
 	public static ServiceFactory getImplementation () {
 		return implementation;
 	}
 	
 	/**
 	 * Static method used to load the implementation to use in the application.
 	 * @param factoryClass
 	 *        the class of the factory to use.
 	 */
 	public static void createFactory (final String factoryClass) {
 		try {
			implementation = (ServiceFactory) Class.forName(factoryClass).newInstance();
 		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
 			lg.severe("Cannot instantiate service factory class (" + factoryClass + "). "
 					+ e.getClass() + ", details: " + e.getMessage());
 			throw new ServiceInstantiationError(factoryClass, e);
 		}
 	}
 	
 	/**
 	 * Return the parameter manager to use.
 	 * @return the parameter manager.
 	 */
 	public abstract IParametersManager getParameterManager ();
 	
 	/**
 	 * Return the translator to use.
 	 * @return the translator.
 	 */
 	public abstract ITranslator getTranslator ();
 	
 	/**
 	 * Return the entry manager to use.
 	 * @return the entry manager.
 	 */
 	public abstract IEntryManager getEntryManager ();
 	
 	/**
 	 * Return the file manager to use
 	 * @return the file manager.
 	 */
 	public abstract IFileManager getFileManager ();
 }
