 package net.sf.gilead.blazeds.adapter;
 
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import net.sf.gilead.core.IPersistenceUtil;
 import net.sf.gilead.core.hibernate.HibernateUtil;
import net.sf.gilead.core.hibernate.jpa.HibernateJpaUtil;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.SessionFactory;
 
 import flex.messaging.config.ConfigMap;
 
 /**
  * Manager to create persistence utils from BlazeDS configuration
  * @author bruno.marchesson
  *
  */
 public class PersistenceUtilManager
 {
 	//----
 	// Constants
 	//----
 	/**
 	 * Parameter for factory parameters
 	 */
 	public static final String PERSISTENCE_FACTORY = "persistence-factory";
 	
 	/**
 	 * Parameter for persistence util implementation
 	 */
 	public static final String PERSISTENCE_UTIL = "persistenceUtil";
 	
 	/**
 	 * Parameter for Hibernate persistence util implementation
 	 */
 	public static final String HIBERNATE_PERSISTENCE_UTIL = "Hibernate";
 	
 	/**
 	 * Parameter for Hibernate JPA persistence util implementation
 	 */
 	public static final String HIBERNATE_JPA_PERSISTENCE_UTIL = "HibernateJPA";
 	
 	/**
 	 * Parameter for entity manager factory
 	 */
 	public static final String ENTITY_MANAGER_FACTORY = "entityManagerFactory";
 	
 	/**
 	 * Parameter for retrieve entity manager factory from JNDI
 	 */
 	public static final String JNDI_NAME = "jndi";
 	
 	/**
 	 * Parameter for singleton helper
 	 */
 	public static final String HELPER_SINGLETON = "singleton";
 	
 	/**
 	 * Parameter for class helper
 	 */
 	public static final String HELPER_CLASS = "class";
 	
 	/**
 	 * Parameter for method helper
 	 */
 	public static final String HELPER_METHOD = "method";
 	
 	/**
 	 * Parameter for stateless/stateful store
 	 */
 	public static final String STATELESS_STORE = "stateless";
 	
 	//----
 	// Attributes
 	//----
 	/**
 	 * Log channel
 	 */
 	private static Log _log = LogFactory.getLog(PersistenceUtilManager.class);
 	
 	//-------------------------------------------------------------------------
 	//
 	// Public interface
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Create the persistence util from the BlazeDS configuration
 	 * @param configMap
 	 * @return
 	 */
 	public static IPersistenceUtil createPersistenceUtil(ConfigMap config) throws Exception
 	{
 		ConfigMap configMap = config.getPropertyAsMap(PERSISTENCE_FACTORY, null);
 		if (configMap.getProperty(ENTITY_MANAGER_FACTORY) != null)
 		{
 			return createUtilFromEntityManagerFactory(configMap);
 		}
 		else if (configMap.getProperty(HELPER_CLASS) != null)
 		{
 			return createUtilFromHelper(configMap);
 		}
 		else
 		{
 			// DEBUG information
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Found " + configMap.size() + " item(s) in config.");
 				Iterator iterator = configMap.entrySet().iterator();
 				while(iterator.hasNext())
 				{
 					Map.Entry entry = (Map.Entry) iterator.next();
 					_log.debug("Config[" + entry.getKey() + "] = " + entry.getValue());
 				}
 			}
 			
 			// Exception
 			throw new RuntimeException("No persistence util parameter defined in config");
 		}
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Internal methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Create Persistence Util from Entity Manager Factory JNDI name
 	 */
 	protected static IPersistenceUtil createUtilFromEntityManagerFactory(ConfigMap configMap) throws Exception
 	{
 	//	Get EntityManagerFactory name and retrieve mode
 	//
 		String factoryName = configMap.getProperty(ENTITY_MANAGER_FACTORY);
 		boolean jndi = configMap.getPropertyAsBoolean(JNDI_NAME, false);
 		
 	//	Retrieve entity manager factory
 	//
 		EntityManagerFactory factory; 
 		if (jndi == false)
 		{
 			factory = Persistence.createEntityManagerFactory(factoryName);
 		}
 		else
 		{
 			factory = getEntityManagerFactoryFromJNDI(factoryName);
 		}
 		
 	//	Create JPA persistence util
 	//
 		String persistenceUtilName = configMap.getPropertyAsString(PERSISTENCE_UTIL, HIBERNATE_PERSISTENCE_UTIL);
 		if (persistenceUtilName.equals(HIBERNATE_PERSISTENCE_UTIL))
 		{
 		//	Hibernate JPA
 		//
 			HibernateJpaUtil hibernateJpaUtil = new HibernateJpaUtil();
 			hibernateJpaUtil.setEntityManagerFactory(factory);
 			
 			return hibernateJpaUtil;
 		}
 		else
 		{
 			throw new RuntimeException("Unknown persistence util implementation : " + persistenceUtilName);
 		}
 	}
 	
 	/**
 	 * Get Entity Manager Factory from JNDI
 	 * @param jndiName JNDI name of Entity Manager Factory 
 	 * @return
 	 */
 	protected static EntityManagerFactory getEntityManagerFactoryFromJNDI(String jndiName) throws Exception
 	{
 	//	JNDI Lookup for Entity Manager Factory
 	//
 		Context context = new InitialContext();
 	 	 
 		EntityManagerFactory entityManagerFactory = (EntityManagerFactory) context.lookup(jndiName);
 		if (entityManagerFactory == null)
 		{
 			throw new RuntimeException("Unable to find EntityManagerFactory");
 		}
 		
 	//	JBoss specific
 	//
 		if (entityManagerFactory.getClass().getCanonicalName() == "org.jboss.ejb3.entity.InjectedEntityManagerFactory")
 		{
 		//	Need to call 'getDelegate' method
 		//
 			Method getDelegate = entityManagerFactory.getClass().getMethod("getDelegate", (Class[]) null);
 			entityManagerFactory = (EntityManagerFactory) 
 									getDelegate.invoke(entityManagerFactory, (Object[]) null);
 		}
 		
 		return entityManagerFactory;
 	}
 	
 	/**
 	 * Create Persistence Util from introspection
 	 * @param configMap
 	 * @return
 	 * @throws ClassNotFoundException 
 	 */
 	protected static IPersistenceUtil createUtilFromHelper(ConfigMap configMap) throws Exception
 	{
 	//	Get Helper class
 	//
 		String className = configMap.getProperty(HELPER_CLASS);
 		
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		Class<?> helperClass = classLoader.loadClass(className);
 		
 	//	Singleton handling
 	//
 		boolean singleton = configMap.getPropertyAsBoolean(HELPER_SINGLETON, false);
 		Object instance = null;
 		if (singleton == true)
 		{
 		//	Get singleton instance
 		//
 			Method instanceMethod = helperClass.getMethod("getInstance", (Class[])null);
 			if (instanceMethod == null)
 			{
 				throw new RuntimeException("No 'getInstance' method for singleton " + className);
 			}
 			instance = instanceMethod.invoke(null, (Object[])null);
 		}
 		
 	//	Getter method
 	//
 		String getter = configMap.getPropertyAsString(HELPER_METHOD, "getSessionFactory");
 		Method getterMethod = helperClass.getMethod(getter, (Class[]) null);
 		if (getterMethod == null)
 		{
 			throw new RuntimeException("No '" + getter + "' method for singleton " + className);
 		}
 		
 		Object factory = getterMethod.invoke(instance, (Object[]) null);
 		if (factory == null)
 		{
 			throw new RuntimeException(getter + "' method for singleton " + className + " returns null !");
 		}
 		
 	//	Create persistence util from implementation
 	//
 		String persistenceUtilName = configMap.getPropertyAsString(PERSISTENCE_UTIL, HIBERNATE_PERSISTENCE_UTIL);
 		if (persistenceUtilName.equals(HIBERNATE_PERSISTENCE_UTIL))
 		{
 		//	Hibernate
 		//
 			HibernateUtil hibernateUtil = new HibernateUtil();
 			hibernateUtil.setSessionFactory((SessionFactory)factory);
 			
 			return hibernateUtil;
 		}
 		else if (persistenceUtilName.equals(HIBERNATE_JPA_PERSISTENCE_UTIL))
 		{
 		//	Hibernate JPA
 		//
 			HibernateJpaUtil hibernateJpaUtil = new HibernateJpaUtil();
 			hibernateJpaUtil.setEntityManagerFactory((EntityManagerFactory)factory);
 			
 			return hibernateJpaUtil;
 		}
 		else
 		{
 			throw new RuntimeException("Unknown persistence util implementation : " + persistenceUtilName);
 		}
 	}
 }
