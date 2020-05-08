 package edu.wustl.common.query.factory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import javax.servlet.http.HttpServletRequest;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.util.Utility;
 import edu.wustl.query.util.global.Variables;
 import edu.wustl.query.util.querysuite.AbstractQueryUIManager;
 import edu.wustl.query.util.querysuite.QueryModuleError;
 import edu.wustl.query.util.querysuite.QueryModuleException;
 
 /**
  * Factory to return the AbstractQueryUIManager instance.
  * 
  * @author maninder_randhawa
  *
  */
 public class AbstractQueryUIManagerFactory {
 	/**
 	 * Method to create instance of class AbstractQueryUIManager. 
 	 * @return The reference of AbstractQueryUIManager. 
 	 */
 	public static AbstractQueryUIManager getDefaultAbstractUIQueryManager()
 	{
 		return (AbstractQueryUIManager) Utility.getObject(Variables.abstractQueryUIManagerClassName);
 	}
 	
 	/**
 	 * Method to create instance of class AbstractQueryUIManager. 
 	 * @return The reference of AbstractQueryUIManager. 
 	 */
 	public static AbstractQueryUIManager configureDefaultAbstractUIQueryManager(Class className,HttpServletRequest request
 			, IQuery iquery) throws QueryModuleException
 	{
 		
         String securityManagerClass = Variables.abstractQueryUIManagerClassName;
          QueryModuleException queryModuleException = null;
          AbstractQueryUIManager abstractQueryUIManager = null;
         if(securityManagerClass!=null)
 			try
 			{
				className = Class.forName(securityManagerClass);
 				if(className != null)
 		         {
 		         	Constructor[] cons = className.getConstructors();
 		         	abstractQueryUIManager =  (AbstractQueryUIManager)cons[0].newInstance(request,iquery);
 		         }
 			}
 			catch (ClassNotFoundException e)
 			{
 				queryModuleException = new QueryModuleException(e.getMessage(),QueryModuleError.CLASS_NOT_FOUND);
 				throw queryModuleException;
 			}
 			catch (IllegalArgumentException e)
 			{
 				queryModuleException = new QueryModuleException(e.getMessage(),QueryModuleError.SQL_EXCEPTION);
 				throw queryModuleException;
 			}
 			catch (InstantiationException e)
 			{
 				queryModuleException = new QueryModuleException(e.getMessage(),QueryModuleError.SQL_EXCEPTION);
 				throw queryModuleException;
 			}
 			catch (IllegalAccessException e)
 			{
 				queryModuleException = new QueryModuleException(e.getMessage(),QueryModuleError.SQL_EXCEPTION);
 				throw queryModuleException;
 			}
 			catch (InvocationTargetException e)
 			{
 				queryModuleException = new QueryModuleException(e.getMessage(),QueryModuleError.SQL_EXCEPTION);
 				throw queryModuleException;
 			}
         
 		return abstractQueryUIManager;
 	}
 }
