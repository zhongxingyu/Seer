 package edu.wustl.common.query.factory;
 /**
  * Factory to return the AbstractQueryManager instance.
  * 
  */
 import edu.wustl.common.util.Utility;
 import edu.wustl.query.querymanager.AbstractQueryManager;
 import edu.wustl.query.util.global.Variables;
 
 public class AbstractQueryManagerFactory {
 
 	/**
 	 * Method to create instance of class AbstractQueryManager. 
 	 * @return The reference of AbstractQueryManager. 
 	 */
 	public static AbstractQueryManager getDefaultAbstractQueryManager()
 	{
 		return (AbstractQueryManager) Utility.getObject(Variables.abstractQueryManagerClassName);
 	}
 	
 	/**
 	 * Method to create instance of class AbstractQueryManager. 
 	 * @return The reference of AbstractQueryManager. 
 	 */
	public static AbstractQueryManager configureDefaultAbstractQueryManager(String className)
 	{
 		return (AbstractQueryManager) Utility.getObject(className);
 	}
 }
