 package org.prot.app.services.db;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import org.apache.log4j.Logger;
import org.prot.app.services.PrivilegedServiceException;
 import org.prot.appserver.config.Configuration;
 import org.springframework.remoting.rmi.RmiProxyFactoryBean;
 
 public class DbBrowserServiceFactory
 {
 	private static final Logger logger = Logger.getLogger(DbBrowserServiceFactory.class);
 
 	private static final String CONTROLLER_ADDRESS = "localhost";
 
 	private static DbBrowserService service;
 
 	private static final int getRmiPort()
 	{
 		return Configuration.getInstance().getRmiRegistryPort();
 	}
 
 	private static DbBrowserService createDbBrowserService()
 	{
 		DbBrowserService browserService = AccessController
 				.doPrivileged(new PrivilegedAction<DbBrowserService>()
 				{
 					public DbBrowserService run()
 					{
 						RmiProxyFactoryBean proxyFactory = new RmiProxyFactoryBean();
 						proxyFactory.setServiceInterface(org.prot.controller.services.db.DbService.class);
 						proxyFactory.setServiceUrl("rmi://" + CONTROLLER_ADDRESS + ":" + getRmiPort()
 								+ "/appserver/DbService");
 						proxyFactory.afterPropertiesSet();
 
 						Object object = proxyFactory.getObject();
 						if (object == null)
 						{
 							logger.error("Could not connect with the DbServices");
 							throw new NullPointerException();
 						}
 
 						DbBrowserService dbService = new DbBrowserService(
 								(org.prot.controller.services.db.DbService) object);
 
 						// Object o = userService;
 						return dbService;
 					}
 
 				});
 
 		return browserService;
 	}
 
 	private static DbBrowserService createMockDbBrowserService()
 	{
		throw new PrivilegedServiceException();
 	}
 
 	public static final DbBrowserService getDbBrowserService()
 	{
 		if (service == null)
 		{
 			switch (Configuration.getInstance().getServerMode())
 			{
 			case DEVELOPMENT:
 				service = createMockDbBrowserService();
 				break;
 			case SERVER:
 				service = createDbBrowserService();
 				break;
 			}
 		}
 
 		return service;
 	}
 }
