 package com.alexrnl.commons.database;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.error.ExceptionUtils;
 
 /**
  * The abstract factory for the {@link DAO}s.<br />
  * It manages the creation of the factory through the {@link #createFactory(String)} method.<br />
 * The user of this class should extend id and add the required {@link DAO} methods needed for its
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
	 * Retrieve and create the appropriate factory.
 	 * @param factoryClass
 	 *        the type of DAO required.
 	 */
	public static synchronized void createFactory (final String factoryClass) {
 		try {
 			if (implementation != null) {
 				try {
 					implementation.close();
 				} catch (final IOException e) {
 					lg.warning("Could not close previous implementation (" + e.getMessage() + ")");
 				}
 			}
			implementation = Class.forName(factoryClass).asSubclass(AbstractDAOFactory.class).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | ClassCastException e) {
 			lg.severe("Cannot instantiate DAO factory class (" + factoryClass + "). " + ExceptionUtils.display(e));
 			throw new DAOInstantiationError(factoryClass, e);
 		}
 	}
 	
 }
