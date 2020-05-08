 /**
  * 
  */
 package net.sf.gilead.gwt;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import javax.servlet.http.HttpSession;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.core.beanlib.mapper.ProxyClassMapper;
 import net.sf.gilead.core.store.stateful.HttpSessionProxyStore;
 import net.sf.gilead.exception.NotAssignableException;
 import net.sf.gilead.exception.TransientObjectException;
 import net.sf.gilead.proxy.ProxyClassLoader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.gwt.user.server.rpc.RPCRequest;
 
 /**
  * Static helper class for PersistentRemoteService and HibernateRPCServiceExporter (GWT-SL)
  * @author bruno.marchesson
  *
  */
 public class GileadRPCHelper
 {
 	//----
 	// Attributes
 	//----
 	/**
 	 * The log channel
 	 */
 	private static Log log = LogFactory.getLog(GileadRPCHelper.class);
 	
 	//-------------------------------------------------------------------------
 	//
 	// Public interface
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Proxy class loader initialisation
 	 */
 	public static void initClassLoader()
 	{
 	// 	Set Proxy class loader (privileged code needed)
 	//
		 ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		 if (contextClassLoader instanceof ProxyClassLoader == false)
 		 {
			 log.info("Setting proxy class loader for thread " + Thread.currentThread());
			 Thread.currentThread().setContextClassLoader(new ProxyClassLoader(contextClassLoader));
    	 }
 	}
 	
 	/**
 	 * Parse RPC input parameters.
 	 * Must be called before GWT service invocation.
 	 * @param rpcRequest the input GWT RPC request
 	 * @param beanManager the Hibernate bean manager
 	 * @param session the HTTP session (for HTTP Pojo store)
 	 */
 	public static void parseInputParameters(RPCRequest rpcRequest, 
 											PersistentBeanManager beanManager,
 											HttpSession session)
 	{
 	//	Init classloader for proxy mode
 	//
 		if (beanManager.getClassMapper() instanceof ProxyClassMapper)
 		{
 			initClassLoader();
 		}
 		
 	//	Set HTTP session of Pojo store in thread local
 	//
 		HttpSessionProxyStore.setHttpSession(session);
 		
 	//	Merge parameters if needed
 	//
 		if ((rpcRequest != null) &&
 			(rpcRequest.getParameters() != null))
 		{
 			Object[] parameters = rpcRequest.getParameters();
 			for (int index = 0 ; index < parameters.length; index ++)
 			{
 				if (parameters[index] != null)
 				{
 					try
 					{
 						parameters[index] = beanManager.merge(parameters[index], true);
 					}
 					catch (NotAssignableException ex)
 					{
 						log.debug(parameters[index] + " not assignable");
 					}
 					catch (TransientObjectException ex)
 					{
 						log.info(parameters[index] + " is transient : cannot merge...");
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Clone the service result.
 	 * Must be called after successful service invocation
 	 * @param returnValue the service return value
 	 * @param beanManager the Hibernate bean manager
 	 * @return the cloned service value
 	 */
 	public static final Object parseReturnValue(Object returnValue,
 											    PersistentBeanManager beanManager)
 	{
 	//	Clone if needed
 	//
 		if (returnValue != null)
 		{
 			try
 			{
 				returnValue = beanManager.clone(returnValue, true);
 			}
 			catch (NotAssignableException ex)
 			{
 				log.debug(returnValue + " not assignable");
 			}
 			catch (TransientObjectException ex)
 			{
 				log.info(returnValue + " is transient : cannot clone...");
 			}
 		}
 		
 	//	Remove HTTP session of Pojo store thread local
 	//
 		HttpSessionProxyStore.setHttpSession(null);
 		
 		return returnValue;
 	}
 }
