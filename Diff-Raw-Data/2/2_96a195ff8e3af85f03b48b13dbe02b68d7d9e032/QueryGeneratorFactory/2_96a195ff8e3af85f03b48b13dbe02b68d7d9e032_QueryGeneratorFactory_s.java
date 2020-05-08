 
 package edu.wustl.common.query.factory;
 
 import edu.wustl.common.util.Utility;
 import edu.wustl.query.queryengine.impl.IQueryGenerator;
 import edu.wustl.query.util.global.Variables;
 
 /**
  * Factory to return the SqlGenerator's instance. 
  * @author deepti_shelar
  *
  */
 public abstract class QueryGeneratorFactory
 {
 
 	/**
 	 * Method to create instance of class SqlGenerator. 
 	 * @return The reference of SqlGenerator. 
 	 */
 	public static IQueryGenerator getDefaultQueryGenerator()
 	{
 
 		return (IQueryGenerator) Utility.getObject(Variables.queryGeneratorClassName);
 	}
 
 	/**
 	 * Method to create instance of class SqlGenerator. 
 	 * @return The reference of SqlGenerator. 
 	 */
	public static IQueryGenerator ConfigureQueryGenerator(String className)
 	{
 
 		return (IQueryGenerator) Utility.getObject(className);
 	}
 
 }
