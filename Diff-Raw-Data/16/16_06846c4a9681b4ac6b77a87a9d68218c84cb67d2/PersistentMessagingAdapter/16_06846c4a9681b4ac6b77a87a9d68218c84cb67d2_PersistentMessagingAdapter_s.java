 package net.sf.gilead.blazeds.adapter;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.core.IPersistenceUtil;
 import net.sf.gilead.core.store.stateful.HttpSessionProxyStore;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import flex.messaging.FlexContext;
 import flex.messaging.config.ConfigMap;
 import flex.messaging.messages.AsyncMessage;
 import flex.messaging.messages.Message;
 import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
 
 /**
  * Persistent adapter for BlazeDS Messaging
  * @author bruno.marchesson
  *
  */
 public class PersistentMessagingAdapter extends ActionScriptAdapter
 {
 	//----
 	// Attribute
 	//----
 	/**
 	 * Log channel
 	 */
 	private static Log _log = LogFactory.getLog(PersistentMessagingAdapter.class);
 	
 	/**
 	 * The Hibernate bean manager
 	 */
 	private PersistentBeanManager _beanManager;
 
 	/**
 	 * Stateless store flag
 	 */
 	private boolean _stateless;	
 	
 	//-------------------------------------------------------------------------
 	//
 	// Java adapter override
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Adapter initialisation
 	 */
 	@Override
 	public void initialize(String name, ConfigMap config) 
 	{
 		_log.info("Initializing " + name);
 		
 	//	Call base method
 	//
 		super.initialize(name, config);
 		
 		if (config.getPropertyAsMap(PersistenceUtilManager.PERSISTENCE_FACTORY, null) != null)
 		{
 			try 
 			{
 				//	Get stateless mode property value
 				//
 					_stateless = config.getPropertyAsBoolean(PersistenceUtilManager.STATELESS_STORE, true);
 					
 				//	Create Persistence util
 				//
 					if (_log.isDebugEnabled())
 					{
 						_log.debug("Creating Persistence bean manager");
 					}
 					
 					IPersistenceUtil persistenceUtil = PersistenceUtilManager.createPersistenceUtil(config);
 					_beanManager = new PersistentBeanManager();
 					
 					if (_stateless == false)
 					{
						_beanManager.setProxyStore(new HttpSessionProxyStore());
 					}
 					
 					_beanManager.setPersistenceUtil(persistenceUtil);
 					
 					_log.info(name + " initialization ok");
 			}
 			catch (Exception e)
 			{
 				_log.error(name + " configuration error", e);
 				
 				throw new RuntimeException(e);
 			}
 		}
 	}
 	
 	/**
 	 * Invoke adapter
 	 */
 	@Override
 	public Object invoke(Message message)
 	{
 		AsyncMessage asyncMessage = (AsyncMessage) message;
 		
 		try
 		{
 		//	Set HTTP session in associated pojo store
 		//
 			if (_stateless == false)
 			{
 				HttpSessionProxyStore.setHttpSession(FlexContext.getHttpRequest().getSession(true));
 			}
 		
 		//	Clone body to remove proxies
 		//
 			Object body = asyncMessage.getBody();
 			_log.info("Cloning body " + body);
 			body = _beanManager.clone(body);
 			
 			asyncMessage.setBody(body);
 		
 		// 	Call ActionScript adapter
 		//
 			return super.invoke(message);
 		}
 		catch (RuntimeException e)
 		{
 		// 	Log error on server console and rethrow it
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Received runtime error on server", e.getCause());
 			}
 			
 			throw e;
 		}
 		finally
 		{
 			if (_stateless == false)
 			{
 			//	Cleanup associated HTTP session in pojo store
 			//
 				HttpSessionProxyStore.setHttpSession(null);
 			}
 		}
 	}
 }
