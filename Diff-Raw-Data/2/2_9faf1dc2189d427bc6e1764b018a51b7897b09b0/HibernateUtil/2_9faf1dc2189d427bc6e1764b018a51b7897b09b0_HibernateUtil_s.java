 package net.sf.gilead.core.hibernate;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.beanlib.hibernate.UnEnhancer;
 import net.sf.gilead.core.IPersistenceUtil;
 import net.sf.gilead.exception.ComponentTypeException;
 import net.sf.gilead.exception.NotPersistentObjectException;
 import net.sf.gilead.exception.TransientObjectException;
 import net.sf.gilead.pojo.base.IUserType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.EntityMode;
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.collection.AbstractPersistentCollection;
 import org.hibernate.collection.PersistentBag;
 import org.hibernate.collection.PersistentCollection;
 import org.hibernate.collection.PersistentList;
 import org.hibernate.collection.PersistentMap;
 import org.hibernate.collection.PersistentSet;
 import org.hibernate.impl.SessionFactoryImpl;
 import org.hibernate.impl.SessionImpl;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.persister.collection.CollectionPersister;
 import org.hibernate.proxy.HibernateProxy;
 import org.hibernate.type.AbstractComponentType;
 import org.hibernate.type.AssociationType;
 import org.hibernate.type.CollectionType;
 import org.hibernate.type.ComponentType;
 import org.hibernate.type.CompositeCustomType;
 import org.hibernate.type.MapType;
 import org.hibernate.type.Type;
 
 /**
  * Persistent helper for Hibernate implementation
  * Centralizes the SessionFactory and add some needed methods.
  * Not really a singleton, since there can be as many HibernateUtil instance as different sessionFactories
  * @author BMARCHESSON
  */
 public class HibernateUtil implements IPersistenceUtil
 {
 	//----
 	// Serialized proxy informations map constants
 	//----
 	/**
 	 * Proxy id
 	 */
 	private static final String ID="id";
 	
 	/**
 	 * Persistent collection class name
 	 */
 	private static final String CLASS_NAME="class";
 	
 	/**
 	 * Persistent collection role
 	 */
 	private static final String ROLE="role";
 	
 	/**
 	 * Persistent collection PK ids
 	 */
 	private static final String KEY="key";
 	
 	/**
 	 * Persistent collection ids list
 	 */
 	private static final String ID_LIST="idList";
 	
 	/**
 	 * Persistent map values list
 	 */
 	private static final String VALUE_LIST="valueList";
 	
 	//----
 	// Attributes
 	//----	
 	/**
 	 * Log channel
 	 */
 	private static Log _log = LogFactory.getLog(HibernateUtil.class);
 	
 	/**
 	 * The pseudo unique instance of the singleton
 	 */
 	private static HibernateUtil _instance = null;
 	
 	/**
 	 * The Hibernate session factory
 	 */
 	private SessionFactoryImpl _sessionFactory;	
 	
 	/**
 	 * The persistance map, with persistance status of all classes
 	 * including persistent component classes
 	 */
 	private Map<Class<?>, Boolean> _persistenceMap;
 	
 	/**
 	 * The current opened session
 	 */
 	private ThreadLocal<Session> _session;
 	
 	//----
 	// Properties
 	//----
 	/**
 	 * @return the unique instance of the singleton
 	 */
 	public static HibernateUtil getInstance()
 	{
 		if (_instance == null)
 		{
 			_instance = new HibernateUtil();
 		}
 		return _instance;
 	}
 	
 	/**
 	 * @return the hibernate session Factory
 	 */
 	public SessionFactory getSessionFactory()
 	{
 		return _sessionFactory;
 	}
 
 	/**
 	 * @param sessionFactory the factory to set
 	 */
 	public void setSessionFactory(SessionFactory sessionFactory)
 	{
 		if (sessionFactory instanceof SessionFactoryImpl == false)
 		{
 		//	Probably a Spring injected session factory
 		//
 			sessionFactory = searchHibernateImplementation(sessionFactory);
 			if (sessionFactory == null)
 			{
 				throw new IllegalArgumentException("Cannot find Hibernate session factory implementation !");
 			}
 		}
 		_sessionFactory = (SessionFactoryImpl) sessionFactory;
 	}
 	
 	
 	//-------------------------------------------------------------------------
 	//
 	// Constructor
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Default constructor
 	 */
 	public HibernateUtil()
 	{
 		_session = new ThreadLocal<Session>();
 		_persistenceMap = new HashMap<Class<?>, Boolean>();
 		
 		// Filling persistence map with primitive types
 		_persistenceMap.put(Byte.class, false);
 		_persistenceMap.put(Short.class, false);
 		_persistenceMap.put(Integer.class, false);
 		_persistenceMap.put(Long.class, false);
 		_persistenceMap.put(Float.class, false);
 		_persistenceMap.put(Double.class, false);
 		_persistenceMap.put(Boolean.class, false);
 		_persistenceMap.put(String.class, false);
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Public interface
 	//
 	//-------------------------------------------------------------------------
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getId(java.lang.Object)
 	 */
 	public Serializable getId(Object pojo)
 	{
 		return getId(pojo, pojo.getClass());
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getId(java.lang.Object, java.lang.Class)
 	 */
 	public Serializable getId(Object pojo, Class<?> hibernateClass)
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Unenhance Class<?> if needed
 	//
 		hibernateClass = UnEnhancer.unenhanceClass(hibernateClass);
 		
 	//	Persistence checking
 	//
 		if (isPersistentClass(hibernateClass) == false)
 		{
 		//	Not an hibernate Class !
 		//
			throw new NotPersistentObjectException(hibernateClass);			
 		}
 		
 	//	Retrieve Class<?> hibernate metadata
 	//
 		ClassMetadata hibernateMetadata = _sessionFactory.getClassMetadata(hibernateClass);
 		if (hibernateMetadata == null)
 		{
 		//	Component class (persistent but not metadata) : no associated id
 		//	So must be considered as transient
 		//
 			throw new ComponentTypeException(pojo);
 		}
 		
 	//	Retrieve ID
 	//
 		Serializable id = null;
 		
 		Class<?> pojoClass = UnEnhancer.unenhanceClass(pojo.getClass());
 		if (hibernateClass.equals(pojoClass))
 		{
 		//	Same class for pojo and hibernate class
 		//
 			if (pojo instanceof HibernateProxy)
 			{
 			//	To prevent LazyInitialisationException
 			//
 				id = ((HibernateProxy)pojo).getHibernateLazyInitializer().getIdentifier();
 			}
 			else
 			{
 			//	Otherwise : use metada
 			//
 				id = hibernateMetadata.getIdentifier(pojo, EntityMode.POJO);
 			}
 		}
 		else
 		{
 		//	DTO case : invoke the method with the same name
 		//
 			String property = hibernateMetadata.getIdentifierPropertyName();
 			
 			try
 			{
 				// compute getter method name
 				property = property.substring(0,1).toUpperCase() + 
 						   property.substring(1);
 				String getter = "get" + property;
 				
 				// Find getter method
 				Method method = pojoClass.getMethod(getter, (Class[])null);
 				if (method == null)
 				{
 					throw new RuntimeException("Cannot find method " + getter + " for Class<?> " + pojoClass);
 				}
 				id = (Serializable) method.invoke(pojo,(Object[]) null);
 			}
 			catch (Exception ex)
 			{
 				throw new RuntimeException("Invocation exception ", ex);
 			}
 		}
 		
 	//	Post condition checking
 	//
 		if ((id == null) ||
 			(id.toString().equals("0") == true))
 		{
 			throw new TransientObjectException(pojo);
 		}
 		return id;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isHibernatePojo(java.lang.Object)
 	 */
 	public boolean isPersistentPojo(Object pojo)
 	{
 	//	Precondition checking
 	//
 		if (pojo == null)
 		{
 			return false;
 		}
 		
 	//	Try to get the ID : if an exception is thrown
 	//	the pojo is not persistent...
 	//
 		try
 		{
 			getId(pojo);
 			return true;
 		}
 		catch(TransientObjectException ex)
 		{
 			return false;
 		}
 		catch(NotPersistentObjectException ex)
 		{
 			return false;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isHibernateClass(java.lang.Class)
 	 */
 	public boolean isPersistentClass(Class<?> clazz)
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Check proxy (based on beanlib Unenhancer class)
 	//
 		clazz = getUnenhancedClass(clazz);
 		
 	//	Look into the persistence map
 	//
 		synchronized (_persistenceMap)
 		{
 			Boolean persistent = _persistenceMap.get(clazz);
 			if (persistent != null)
 			{
 				return persistent.booleanValue();
 			}
 		}
 		
 	//	First clall for this Class<?> : compute persistence class
 	//
 		computePersistenceForClass(clazz);
 		return _persistenceMap.get(clazz).booleanValue();
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#getPersistentClass(java.lang.Class)
 	 */
 	public Class<?> getUnenhancedClass(Class<?> clazz)
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Check proxy (based on beanlib Enhancer class)
 	//
 		if (isEnhanced(clazz))
 		{
 			clazz = clazz.getSuperclass();
 		}
 		
 		return clazz;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#isEnhanced(java.lang.Class)
 	 */
 	public boolean isEnhanced(Class<?> clazz)
 	{
 	//	Check proxy (based on beanlib unEnhancer class)
 	//
 		return (clazz != UnEnhancer.unenhanceClass(clazz));
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#openSession()
 	 */
 	public void openSession()
 	{
 	//	Precondition checking
 	//
 		if (_sessionFactory == null)
 		{
 			throw new NullPointerException("No Hibernate Session Factory defined !");
 		}
 		
 	//	Open a new session
 	//
 		Session session = _sessionFactory.openSession();
 		
 	//	Store the session in ThreadLocal
 	//
 		_session.set(session);
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.sf.gilead.core.hibernate.IPersistenceUtil#closeSession(java.lang.Object)
 	 */
 	public void closeCurrentSession()
 	{
 		Session session = _session.get();
 		if (session != null)
 		{
 			session.close();
 			_session.remove();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#load(java.io.Serializable, java.lang.Class)
 	 */
 	public Object load(Serializable id, Class<?> persistentClass)
 	{
 	//	Get current opened session
 	//
 		Session session = _session.get();
 		if (session == null)
 		{
 			throw new NullPointerException("Cannot load : no session opened !");
 		}
 		
 	//	Unenhance persistent class if needed
 	//
 		persistentClass = getUnenhancedClass(persistentClass);
 		
 	//	Load the entity
 	//
 		return session.get(persistentClass, id);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#serializeEntityProxy(java.lang.Object)
 	 */
 	public Map<String, Serializable> serializeEntityProxy(Object proxy)
 	{
 	//	Precondition checking
 	//
 		if (proxy == null)
 		{
 			return null;
 		}
 		
 	//	Serialize needed proxy informations
 	//
 		Map<String, Serializable> result = new HashMap<String, Serializable>();
 		result.put(CLASS_NAME, getUnenhancedClass(proxy.getClass()).getName());
 		result.put(ID, getId(proxy));
 		
 		return result;
 	}
 	
 	/**
 	 * Create a proxy for the argument class and id
 	 */
 	public Object createEntityProxy(Map<String, Serializable> proxyInformations)
 	{
 	//	Get current opened session
 	//
 		Session session = _session.get();
 		if (session == null)
 		{
 			throw new NullPointerException("Cannot load : no session opened !");
 		}
 	
 	//	Get needed proxy inforamtions
 	//
 		Serializable id = proxyInformations.get(ID);
 		String className = (String) proxyInformations.get(CLASS_NAME);
 		Class<?> persistentClass = null;
 		try
 		{
 			persistentClass = Thread.currentThread().getContextClassLoader().loadClass(className);
 		}
 		catch(Exception e)
 		{
 		// Should not happen !
 		//
 			throw new RuntimeException("Cannot find persistent class : " + className, e);
 		}
 		
 	//	Create the associated proxy
 	//
 		return session.load(persistentClass, id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#serializePersistentCollection(java.lang.Object)
 	 */
 	public Map<String, Serializable> serializePersistentCollection(Object persistentCollection)
 	{
 	//	Create serialization map
 	//
 		Map<String, Serializable> result = new HashMap<String, Serializable>();
 		
 	//	Get parameters
 	//
 		AbstractPersistentCollection collection = (AbstractPersistentCollection) persistentCollection;
 		result.put(CLASS_NAME, collection.getClass().getName());
 		result.put(ROLE, collection.getRole());
 		result.put(KEY, collection.getKey());
 		
 	//	Store ids
 	//
 		if (isInitialized(collection) == true)
 		{
 			if (collection instanceof Collection)
 			{
 				result.put(ID_LIST, createIdList((Collection)collection));
 			}
 			else if (collection instanceof Map)
 			{
 			//	Store keys
 			//
 				Map map = (Map) collection;
 				ArrayList<SerializableId> keyList = createIdList(map.keySet());
 				if (keyList != null)
 				{
 					result.put(ID_LIST, keyList);
 					
 				//	Store values (only if keys are persistents)
 				//
 					ArrayList<SerializableId> valueList = createIdList(map.values());
 					if (keyList != null)
 					{
 						result.put(VALUE_LIST, valueList);
 					}
 				}
 			}
 			else
 			{
 				throw new RuntimeException("Unexpected Persistent collection : " + collection.getClass());
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Create a persistent collection
 	 * @param proxyInformations serialized proxy informations 
 	 * @param underlyingCollection the filled underlying collection
 	 * @return
 	 */
 	public Object createPersistentCollection(Map<String, Serializable> proxyInformations,
 											 Object underlyingCollection)
 	{
 	//	Get current opened session
 	//
 		Session session = _session.get();
 		if (session == null)
 		{
 			throw new NullPointerException("Cannot load : no session opened !");
 		}
 		
 	//	Get deleted items
 	//
 		Collection<?> deletedItems = addDeletedItems(proxyInformations, underlyingCollection);
 		
 	//	Create collection for the class name
 	//
 		String className = (String) proxyInformations.get(CLASS_NAME);
 
 		PersistentCollection collection = null;
 		if (PersistentBag.class.getName().equals(className))
 		{
 		//	Persistent bag creation
 		//
 			if (underlyingCollection == null)
 			{
 				collection = new PersistentBag((SessionImpl) session);
 			}
 			else
 			{
 				collection =  new PersistentBag((SessionImpl) session,
 										 		(Collection<?>) underlyingCollection);
 			}
 		}
 		else if (PersistentList.class.getName().equals(className))
 		{
 		//	Persistent list creation
 		//
 			if (underlyingCollection == null)
 			{
 				collection = new PersistentList((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentList((SessionImpl) session,
 										  		(List<?>) underlyingCollection);
 			}
 		}
 		else if (PersistentSet.class.getName().equals(className))
 		{
 		//	Persistent set creation
 		//
 			if (underlyingCollection == null)
 			{
 				collection = new PersistentSet((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentSet((SessionImpl) session,
 						 				 	   (Set<?>) underlyingCollection);
 			}
 		}
 		else if (PersistentMap.class.getName().equals(className))
 		{
 		//	Persistent map creation
 		//
 			if (underlyingCollection == null)
 			{
 				collection = new PersistentMap((SessionImpl) session);
 			}
 			else
 			{
 				collection = new PersistentMap((SessionImpl) session,
 						 				 	   (Map<?, ?>) underlyingCollection);
 			}
 		}
 		else
 		{
 			throw new RuntimeException("Unknown persistent collection class name : " + className);
 		}
 		
 	//	Fill with serialized parameters
 	//
 		String role = (String) proxyInformations.get(ROLE);
 		Serializable snapshot = null;
 		if (underlyingCollection != null)
 		{
 		//	Create snpashot
 		//
 			CollectionPersister collectionPersister = _sessionFactory.getCollectionPersister(role);
 			snapshot = collection.getSnapshot(collectionPersister);
 		}
 		
 		collection.setSnapshot(proxyInformations.get(KEY), 
 							   role, snapshot);
 		
 	//	Remove deleted items
 	//
 		if (deletedItems != null)
 		{
 			if (collection instanceof Collection)
 			{
 				((Collection)collection).removeAll(deletedItems);
 			}
 			else if (collection instanceof Map)
 			{
 				for (Object key : deletedItems)
 				{
 					((Map)collection).remove(key);
 				}
 			}
 		}
 		return collection;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#isPersistentCollection(java.lang.Class)
 	 */
 	public boolean isPersistentCollection(Class<?> collectionClass)
 	{
 		return (PersistentCollection.class.isAssignableFrom(collectionClass));
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#isProxy(java.lang.Object)
 	 */
 	public boolean isInitialized(Object proxy)
 	{
 		return Hibernate.isInitialized(proxy);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.gilead.core.IPersistenceUtil#initialize(java.lang.Object)
 	 */
 	public void initialize(Object proxy)
 	{
 		Hibernate.initialize(proxy);
 	}
 
 	//-------------------------------------------------------------------------
 	//
 	// Internal methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Compute embedded persistence (Component, UserType) for argument class
 	 */
 	private void computePersistenceForClass(Class<?> clazz)
 	{
 	//	Precondition checking
 	//
 		synchronized (_persistenceMap)
 		{
 			if (_persistenceMap.get(clazz) != null)
 			{
 			//	already computed
 			//
 				return;
 			}
 		}
 		
 	//	Get associated metadata
 	//
 		ClassMetadata metadata = _sessionFactory.getClassMetadata(clazz);
 		if (metadata == null)
 		{
 		//	Not persistent : check implemented interfaces (they can be declared as persistent !!)
 		//
 			Class<?>[] interfaces = clazz.getInterfaces();
 			if (interfaces != null)
 			{
 				for (int index = 0; index < interfaces.length ; index ++)
 				{
 					if (isPersistentClass(interfaces[index]))
 					{
 						markClassAsPersistent(clazz, true);
 						return;
 					}
 						
 				}
 			}
 			
 		//	Not persistent and no persistent interface!
 		//
 			markClassAsPersistent(clazz, false);
 			return;
 		}
 
 	//	Persistent class
 	//
 		markClassAsPersistent(clazz, true);
 		
 	//	Look for component classes
 	//
 		Type[] types = metadata.getPropertyTypes();
 		for (int index = 0; index < types.length; index++)
 		{
 			Type type = types[index];
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Scanning type " + type.getName() + " from " + clazz);
 			}
 			computePersistentForType(type);
 		}
 	}
 	
 	/**
 	 * Mark class as persistent or not
 	 * @param clazz
 	 * @param persistent
 	 */
 	private void markClassAsPersistent(Class<?> clazz, boolean persistent)
 	{
 		if (_log.isDebugEnabled())
 		{
 			if (persistent)
 			{
 				_log.debug("Marking " + clazz + " as persistent");
 			}
 			else
 			{
 				_log.debug("Marking " + clazz + " as not persistent");
 			}
 		}
 		synchronized (_persistenceMap)
 		{
 			_persistenceMap.put(clazz, persistent);
 		}
 	}
 	
 	/**
 	 * Compute persistent for Hibernate type
 	 * @param type
 	 */
 	private void computePersistentForType(Type type)
 	{
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("Scanning type " + type.getName());
 		}
 		
 		if (type.isComponentType())
 		{
 		//	Add the Class to the persistent map
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is component type");
 			}
 			
 			markClassAsPersistent(type.getReturnedClass(), true);
 			
 			Type[] subtypes = ((AbstractComponentType) type).getSubtypes();
 			for (int index = 0; index < subtypes.length; index++)
 			{
 				computePersistentForType(subtypes[index]);
 			}
 		}	
 		else if (IUserType.class.isAssignableFrom(type.getReturnedClass()))
 		{
 		//	Add the Class to the persistent map
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is user type");
 			}
 			
 			markClassAsPersistent(type.getReturnedClass(), true);
 		}
 		else if (type.isCollectionType())
 		{
 		//	Collection handling
 		//
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is collection type");
 			}
 			computePersistentForType(((CollectionType) type).getElementType(_sessionFactory));
 		}
 		else if (type.isEntityType())
 		{
 			if (_log.isDebugEnabled())
 			{
 				_log.debug("Type " + type.getName() + " is entity type");
 			}
 			computePersistenceForClass(type.getReturnedClass());
  		}
 	}
 	
 	/**
 	 * Seach the underlying Hibernate session factory implementation.
 	 * @param object
 	 * @return the session factory if found, null otherwise
 	 */
 	private static SessionFactoryImpl searchHibernateImplementation(Object object)
 	{
 	//	Precondition checking
 	//
 		if ((object == null) ||
 			(object.getClass().getName().startsWith("java.")))
 		{
 			return null;
 		}
 	//	Iterate over fields
 	//
 		Field[] fields = getRecursiveDeclaredFields(object.getClass());
 		for (Field field : fields)
 		{
 		//	Check current value 
 		//
 			field.setAccessible(true);
 			try
 			{
 				Object value = field.get(object);
 				if (value instanceof SessionFactoryImpl)
 				{
 					return (SessionFactoryImpl) value;
 				}
 				value = searchHibernateImplementation(value);
 				if (value != null)
 				{
 					return (SessionFactoryImpl) value;
 				}
 			}
 			catch (Exception e)
 			{
 			//	Should not happen
 			//
 				e.printStackTrace();
 			}
 		}
 		
 	//	Session Factory not found
 	//
 		return null;
 	}
 	
 	/**
 	 * Recursively get declared fields
 	 */
 	private static Field[] getRecursiveDeclaredFields(Class<?> clazz)
 	{
 	//	Create field list
 	//
 		List<Field> fieldList = new ArrayList<Field>();
 		
 	//	Recursive get superclass declared fields
 	//
 		while(clazz != null)
 		{
 			fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
 			clazz = clazz.getSuperclass();
 		}
 	
 	//	Convert field list to array
 	//
 		return fieldList.toArray(new Field[fieldList.size()]);
 	}
 	
 	private ArrayList<SerializableId> createIdList(Collection collection)
 	{
 		int size = collection.size();
 		ArrayList<SerializableId> idList = new ArrayList<SerializableId>(size);
 		
 		Iterator<Object> iterator = ((Collection) collection).iterator();
 		while(iterator.hasNext())
 		{
 			Object item = iterator.next();
 			if (isPersistentPojo(item))
 			{
 				SerializableId id = new SerializableId();
 				id.id = getId(item);
 				id.className = item.getClass().getName();
 			
 				idList.add(id);
 			}
 		}
 		
 		if (idList.isEmpty())
 		{
 			return null;
 		}
 		else
 		{
 			return idList;
 		}
 	}
 	
 	/**
 	 * Compute deleted items for collection recreation
 	 * @param collection
 	 * @param idList
 	 * @return
 	 */
 	private List<Object> getDeletedItemsForCollection(Collection collection,
 										 			  ArrayList<SerializableId> idList)
 	{
 	//	Get current opened session
 	//
 		Session session = _session.get();
 		if (session == null)
 		{
 			throw new NullPointerException("Cannot load : no session opened !");
 		}
 	
 		ArrayList<Object> deletedItems = new ArrayList<Object>();
 		for (SerializableId sid : idList)
 		{
 			boolean found = false;
 		//	Search item
 		//
 			Iterator iterator = collection.iterator();
 			while (iterator.hasNext())
 			{
 				try
 				{
 					if (sid.id.equals(getId(iterator.next())))
 					{
 						found = true;
 						break;
 					}
 				}
 				catch(TransientObjectException ex)
 				{
 					// Transient objet, go to next one
 				}
 			}
 			
 			if (found == false)
 			{
 			//	Create associated proxy
 			//
 				try
 				{
 					Class<?> itemClass = Thread.currentThread().getContextClassLoader().loadClass(sid.className);
 					itemClass = UnEnhancer.unenhanceClass(itemClass);
 					Object proxy = session.load(itemClass, sid.id);
 					deletedItems.add(proxy);
 				}
 				catch(Exception e)
 				{
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		
 		if (deletedItems.isEmpty())
 		{
 			return null;
 		}
 		else
 		{
 			return deletedItems;
 		}
 	}
 
 	/**
 	 * Add deleted items to the underlying collection, so the Hibernate PersistentCollection
 	 * snapshot will take care of deleted items
 	 * @param proxyInformations
 	 * @param underlyingCollection
 	 * @return the deleted items list
 	 */
 	private Collection<?> addDeletedItems(Map<String, Serializable> proxyInformations,
 										  Object underlyingCollection)
 	{
 		if (underlyingCollection instanceof Collection)
 		{
 			Collection<?> collection = (Collection<?>) underlyingCollection;
 			ArrayList<SerializableId> idList = (ArrayList<SerializableId>) proxyInformations.get(ID_LIST);
 			if (idList != null)
 			{
 				Collection deletedItemList = getDeletedItemsForCollection(collection, idList);
 				
 				if (deletedItemList != null)
 				{
 				//	Add deleted items to the underlying collection so the persistent collection
 				//	snapshot is created properly and deleted items can be removed from db
 				//	if delete-orphan option is enabled
 				//
 					collection.addAll(deletedItemList);
 				}
 				
 				return deletedItemList;
 			}
 		}
 		else if (underlyingCollection instanceof Map)
 		{
 			Map map = (Map) underlyingCollection;
 			ArrayList<SerializableId> idList = (ArrayList<SerializableId>) proxyInformations.get(ID_LIST);
 			if (idList != null)
 			{
 			//	Find delete keys
 			//
 				List deletedKeyList = getDeletedItemsForCollection(map.keySet(), idList);
 				
 				if (deletedKeyList != null)
 				{
 				//	Is there any persistent value ?
 				//
 					List deletedValueList = null;
 					ArrayList<SerializableId> valueList = (ArrayList<SerializableId>) proxyInformations.get(VALUE_LIST);
 					if (valueList != null)
 					{
 						deletedValueList = getDeletedItemsForCollection(map.values(), idList);
 					}
 					
 				//	Add deleted keys and values
 				//
 					int deleteCount = deletedKeyList.size();
 					for (int index = 0 ; index < deleteCount ; index ++)
 					{
 						Object key = deletedKeyList.get(index);
 						Object value = null;
 						if ((deletedValueList != null) &&
 							(index < deletedValueList.size()))
 						{
 							value = deletedValueList.get(index);
 						}
 						map.put(key, value);
 					}
 				}
 				return deletedKeyList;
 			}
 		}
 		
 		return null;
 	}
 }
 
 /**
  * Id / Class structure for collection handling
  * @author bruno.marchesson
  *
  */
 class SerializableId implements Serializable
 {
 	public Serializable id;
 	public String className;
 }
