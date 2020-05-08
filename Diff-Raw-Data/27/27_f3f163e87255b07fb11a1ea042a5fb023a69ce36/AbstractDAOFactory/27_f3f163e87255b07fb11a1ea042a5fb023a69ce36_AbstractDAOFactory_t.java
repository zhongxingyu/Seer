 package com.alexrnl.commons.database;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 
 /**
  * The abstract factory for the {@link DAO}s.<br />
  * It manages the creation of the factory through the {@link #createFactory(String)} method.<br />
 * The user of this class should extend it and add the required {@link DAO} methods needed for its
  * needs.
  * @author Alex
  */
 public abstract class AbstractDAOFactory implements Closeable {
 	/** Logger */
 	private static Logger				lg	= Logger.getLogger(AbstractDAOFactory.class.getName());
 	
 	/** Implementation of the factory to be used */
 	private static AbstractDAOFactory	implementation;
 	
 	/**
 	 * Return the implementation of the factory to be used.
 	 * @return the concrete implementation
 	 */
 	public static AbstractDAOFactory getImplementation () {
 		return implementation;
 	}
 	
 	/**
	 * Create the factory from the class specified.
 	 * @param factoryClass
 	 *        the type of DAO required.
 	 */
	public static synchronized void createFactory (final Class<? extends AbstractDAOFactory> factoryClass) {
 		try {
 			if (implementation != null) {
 				try {
 					implementation.close();
 				} catch (final IOException e) {
 					lg.warning("Could not close previous implementation (" + e.getMessage() + ")");
 				}
 			}
			implementation = factoryClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			lg.severe("Cannot instantiate DAO factory class (" + factoryClass + "). " + ExceptionUtils.display(e));
			throw new DAOInstantiationError(factoryClass.getName(), e);
		}
	}
	
	/**
	 * Retrieve and create the appropriate factory.
	 * @param factoryClass
	 *        the type of DAO required.
	 */
	public static void createFactory (final String factoryClass) {
		try {
			createFactory(Class.forName(factoryClass).asSubclass(AbstractDAOFactory.class));
		} catch (ClassNotFoundException | ClassCastException e) {
 			lg.severe("Cannot instantiate DAO factory class (" + factoryClass + "). " + ExceptionUtils.display(e));
 			throw new DAOInstantiationError(factoryClass, e);
 		}
 	}
 	
 }
