 package org.openmrs.module.auditlog.api.db.hibernate.interceptor;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.CallbackException;
 import org.hibernate.EmptyInterceptor;
 import org.hibernate.EntityMode;
 import org.hibernate.FlushMode;
 import org.hibernate.Interceptor;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.collection.PersistentCollection;
 import org.hibernate.collection.PersistentMap;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.type.StringType;
 import org.hibernate.type.TextType;
 import org.hibernate.type.Type;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.User;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.auditlog.AuditLog;
 import org.openmrs.module.auditlog.AuditLog.Action;
 import org.openmrs.module.auditlog.api.db.AuditLogDAO;
 import org.openmrs.module.auditlog.util.AuditLogUtil;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.util.Reflect;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 /**
  * A hibernate {@link Interceptor} implementation, intercepts any database inserts, updates and
  * deletes and creates audit log entries for Monitored Objects, it logs changes for a single session
  * meaning that if User A and B concurrently make changes to the same object, there will be 2 log
  * entries in the DB, one for each user's session. Any changes/inserts/deletes made to the DB that
  * are not made through the application won't be detected by the module.
  */
 public class HibernateAuditLogInterceptor extends EmptyInterceptor implements ApplicationContextAware {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final Log log = LogFactory.getLog(HibernateAuditLogInterceptor.class);
 	
 	//we use a set because the same object can be loaded multiple times
 	private ThreadLocal<HashSet<OpenmrsObject>> inserts = new ThreadLocal<HashSet<OpenmrsObject>>();
 	
 	private ThreadLocal<HashSet<OpenmrsObject>> updates = new ThreadLocal<HashSet<OpenmrsObject>>();
 	
 	private ThreadLocal<HashSet<OpenmrsObject>> deletes = new ThreadLocal<HashSet<OpenmrsObject>>();
 	
 	//Mapping between object uuid to the map of its changed property names and their older values, the first item in the array is the old value while the the second is the new value
 	private ThreadLocal<Map<String, Map<String, Object[]>>> objectChangesMap = new ThreadLocal<Map<String, Map<String, Object[]>>>();
 	
 	//we will need to disable the interceptor when saving the auditlog to avoid going in circles
 	private ThreadLocal<Boolean> disableInterceptor = new ThreadLocal<Boolean>();
 	
 	private static Set<String> monitoredClassNames = null;
 	
 	private AuditLogDAO auditLogDao;
 	
 	/**
 	 * We need access to this to get the auditLogDao bean, the saveAuditLog method is not available
 	 * to in auditLogservice to ensure no other code creates log entries. We also need the
 	 * sessionFactory instance to be able to get class metadata of mapped classes,
 	 */
 	private ApplicationContext applicationContext;
 	
 	/**
 	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
 	 */
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 	
 	/**
 	 * @return the dao
 	 */
 	public AuditLogDAO getAuditLogDao() {
 		if (auditLogDao == null)
 			auditLogDao = applicationContext.getBean(AuditLogDAO.class);
 		
 		return auditLogDao;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable,
 	 *      java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
 		if (isMonitored(entity)) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			if (log.isDebugEnabled())
 				log.debug("Creating log entry for CREATED object with uuid:" + openmrsObject.getUuid() + " of type:"
 				        + entity.getClass().getName());
 			
 			if (inserts.get() == null)
 				inserts.set(new HashSet<OpenmrsObject>());
 			
 			inserts.get().add(openmrsObject);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable,
 	 *      java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
 	                            String[] propertyNames, Type[] types) {
 		if (isMonitored(entity) && propertyNames != null) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			Map<String, Object[]> propertyChangesMap = null;
 			SessionFactory sessionFactory = ((SessionFactory) applicationContext.getBean("sessionFactory"));
 			for (int i = 0; i < propertyNames.length; i++) {
 				//we need ignore dateChanged and changedBy fields since they saved along with the auditlog
 				//TODO Should we take care of personDateChanged and personDateChangedBy
 				if ("dateChanged".equals(propertyNames[i]) || "changedBy".equals(propertyNames[i]))
 					continue;
 				
 				//TODO Ignore user defined ignored properties
 				
 				Object previousValue = (previousState != null) ? previousState[i] : null;
 				Object currentValue = (currentState != null) ? currentState[i] : null;
 				Class<?> propertyType = BeanUtils.getPropertyDescriptor(entity.getClass(), propertyNames[i])
 				        .getPropertyType();
 				if (!OpenmrsUtil.nullSafeEquals(currentValue, previousValue) && !Reflect.isCollection(propertyType)) {
 					//For string properties, ignore changes from null to blank and vice versa
 					//TODO This should user configurable via a module GP
 					if (StringType.class.getName().equals(types[i].getClass().getName())
 					        || TextType.class.getName().equals(types[i].getClass().getName())) {
 						String currentStateString = null;
 						if (currentValue != null && !StringUtils.isBlank(currentValue.toString()))
 							currentStateString = currentValue.toString();
 						
 						String previousValueString = null;
 						if (previousValue != null && !StringUtils.isBlank(previousValue.toString()))
 							previousValueString = previousValue.toString();
 						
 						//TODO Case sensibility here should be configurable via a GP by admin
 						if (OpenmrsUtil.nullSafeEqualsIgnoreCase(previousValueString, currentStateString))
 							continue;
 					}
 					
 					if (propertyChangesMap == null)
 						propertyChangesMap = new HashMap<String, Object[]>();
 					
 					ClassMetadata metadata = sessionFactory.getClassMetadata(propertyType);
 					Object flattenedPreviousValue = null;
 					Object flattenedCurrentValue = null;
 					
 					if (BeanUtils.isSimpleValueType(propertyType)) {
 						//TODO take care of Dates, Enums, Class, Locale
 						flattenedPreviousValue = previousValue;
 						flattenedCurrentValue = currentValue;
 					} else {
 						//this is a compound property, store the primary key value
 						//TODO take care of composite primary keys
 						
 						//value = PropertyUtils.getProperty(previousValue, metadata.getIdentifierPropertyName());
 						if (previousValue != null)
 							flattenedPreviousValue = metadata.getIdentifier(previousValue, EntityMode.POJO);
 						if (currentValue != null)
 							flattenedCurrentValue = metadata.getIdentifier(currentValue, EntityMode.POJO);
 					}
 					
 					propertyChangesMap.put(propertyNames[i], new Object[] { flattenedPreviousValue, flattenedCurrentValue });
 				}
 			}
 			
 			if (MapUtils.isNotEmpty(propertyChangesMap)) {
 				if (log.isDebugEnabled())
 					log.debug("Creating log entry for EDITED object with uuid:" + openmrsObject.getUuid() + " of type:"
 					        + entity.getClass().getName());
 				
 				if (updates.get() == null)
 					updates.set(new HashSet<OpenmrsObject>());
 				
 				updates.get().add(openmrsObject);
 				
 				if (objectChangesMap.get() == null)
 					objectChangesMap.set(new HashMap<String, Map<String, Object[]>>());
 				
 				objectChangesMap.get().put(openmrsObject.getUuid(), propertyChangesMap);
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onDelete(java.lang.Object, java.io.Serializable,
 	 *      java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
 		if (isMonitored(entity)) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			if (log.isDebugEnabled())
 				log.debug("Creating log entry for DELETED object with uuid:" + openmrsObject.getUuid() + " of type:"
 				        + entity.getClass().getName());
 			
 			if (deletes.get() == null)
 				deletes.set(new HashSet<OpenmrsObject>());
 			
 			deletes.get().add(openmrsObject);
 		}
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onCollectionRecreate(java.lang.Object,
 	 *      java.io.Serializable)
 	 */
 	@Override
 	public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
 		//if (collection != null && !((AbstractPersistentCollection) collection).empty())
 		//	System.err.println("\nAdded new collection:" + ((AbstractPersistentCollection) collection).getRole());
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onCollectionRemove(java.lang.Object,
 	 *      java.io.Serializable)
 	 */
 	@Override
 	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
 		//System.err.println("\nRemoved collection:" + ((AbstractPersistentCollection) collection).getRole());
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onCollectionUpdate(java.lang.Object,
 	 *      java.io.Serializable)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
 		if (collection != null && Collection.class.isAssignableFrom(collection.getClass())) {
 			PersistentCollection persistentColl = ((PersistentCollection) collection);
 			Object owningObject = persistentColl.getOwner();
 			if (isMonitored(owningObject)) {
 				Set<Object> addedItems = new HashSet<Object>();
 				Set<Object> removedItems = new HashSet<Object>();
 				Collection currentColl = (Collection) collection;
 				Map previousMap = (Map) persistentColl.getStoredSnapshot();
 				addedItems.addAll(CollectionUtils.subtract(currentColl, previousMap.values()));
 				removedItems.addAll(CollectionUtils.subtract(previousMap.values(), currentColl));
 				
 				if (addedItems.size() > 0) {
 					System.out.println("Added:" + addedItems);
 					//TODO Generate xml
 					
 				}
 				if (removedItems.size() > 0) {
 					System.out.println("Removed:" + removedItems);
 					//TODO Generate xml
 					
 				}
 				
 				if (updates.get() == null)
 					updates.set(new HashSet<OpenmrsObject>());
 				
 				updates.get().add((OpenmrsObject) owningObject);
 			}
 		} else if (collection != null) {
 			//TODO Handle persistent maps
 			PersistentMap persistentMap = (PersistentMap) collection;
 			Object owningObject = persistentMap.getOwner();
 			if (isMonitored(owningObject)) {
 				log.error("PersistentMaps not supported: Can't create log entry for updated map:" + persistentMap.getRole()
 				        + " in class:" + persistentMap.getOwner().getClass());
 			}
 		}
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
 	 */
 	@Override
 	public void afterTransactionCompletion(Transaction tx) {
 		Date date = new Date();
 		try {
 			if (disableInterceptor.get() == null && tx.wasCommitted()) {
 				if (inserts.get() == null && updates.get() == null && deletes.get() == null)
 					return;
 				
 				try {
 					User user = Context.getAuthenticatedUser();
 					//TODO handle daemon or un authenticated operations
 					
 					if (inserts.get() != null) {
 						for (OpenmrsObject insert : inserts.get()) {
 							AuditLog auditLog = new AuditLog(insert.getClass().getName(), insert.getUuid(), Action.CREATED,
 							        user, date);
 							auditLog.setUuid(UUID.randomUUID().toString());
 							getAuditLogDao().save(auditLog);
 						}
 					}
 					
 					if (updates.get() != null) {
 						for (OpenmrsObject update : updates.get()) {
 							AuditLog auditLog = new AuditLog(update.getClass().getName(), update.getUuid(), Action.UPDATED,
 							        user, date);
 							auditLog.setUuid(UUID.randomUUID().toString());
 							if (objectChangesMap.get() != null) {
 								Map<String, Object[]> propertyValuesMap = objectChangesMap.get().get(update.getUuid());
 								if (propertyValuesMap != null) {
 									auditLog.setChangesXml(AuditLogUtil.generateChangesXml(propertyValuesMap));
 								}
 							}
 							
 							getAuditLogDao().save(auditLog);
 						}
 					}
 					
 					if (deletes.get() != null) {
 						for (OpenmrsObject delete : deletes.get()) {
 							AuditLog auditLog = new AuditLog(delete.getClass().getName(), delete.getUuid(), Action.DELETED,
 							        user, date);
 							auditLog.setUuid(UUID.randomUUID().toString());
 							getAuditLogDao().save(auditLog);
 						}
 					}
 					
 					//Ensure we don't step through the interceptor methods again when saving the auditLog
 					disableInterceptor.set(true);
 					
 					//at this point, the transaction is already committed, 
 					//so we need to call commit() again to sync to the DB
 					tx.commit();
 				}
 				catch (Exception e) {
 					//error should not bubble out of the intercepter
 					log.error("An error occured while creating audit log(s):", e);
 				}
 			}
 		}
 		finally {
 			//cleanup
 			if (inserts.get() != null)
 				inserts.remove();
 			if (updates.get() != null)
 				updates.remove();
 			if (deletes.get() != null)
 				deletes.remove();
 			if (disableInterceptor.get() != null)
 				disableInterceptor.remove();
			if (objectChangesMap.get() != null)
				objectChangesMap.remove();
 		}
 	}
 	
 	/**
 	 * Checks if specified object is among the ones that are monitored and is an
 	 * {@link OpenmrsObject}
 	 * 
 	 * @param obj the object the check
 	 * @return true if the object is a monitored one otherwise false
 	 */
 	private boolean isMonitored(Object obj) {
 		if (monitoredClassNames == null) {
 			SessionFactory sessionFactory = ((SessionFactory) applicationContext.getBean("sessionFactory"));
 			Session session = sessionFactory.getCurrentSession();
 			//If monitoredClassNames is still null, we can't load it yet because in case there are 
 			//any new objects, hibernate will flush and them bomb since they have no ids yet
 			FlushMode originalFlushMode = session.getFlushMode();
 			session.setFlushMode(FlushMode.MANUAL);
 			try {
 				monitoredClassNames = AuditLogUtil.getMonitoredClassNames();
 			}
 			finally {
 				//reset
 				session.setFlushMode(originalFlushMode);
 			}
 		}
 		
 		return OpenmrsObject.class.isAssignableFrom(obj.getClass())
 		        && OpenmrsUtil.collectionContains(monitoredClassNames, obj.getClass().getName());
 	}
 }
