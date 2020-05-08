 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.auditlog.api.db.hibernate.interceptor;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.CallbackException;
 import org.hibernate.EmptyInterceptor;
 import org.hibernate.Transaction;
 import org.hibernate.collection.PersistentCollection;
 import org.hibernate.type.StringType;
 import org.hibernate.type.TextType;
 import org.hibernate.type.Type;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.auditlog.AuditLog;
 import org.openmrs.module.auditlog.AuditLog.Action;
 import org.openmrs.module.auditlog.api.db.AuditLogDAO;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  * A hibernate {@link org.hibernate.Interceptor} implementation, intercepts any database inserts,
  * updates and deletes and creates audit log entries for Monitored Objects, it logs changes for a
  * single session meaning that if User A and B concurrently make changes to the same object, there
  * will be 2 log entries in the DB, one for each user's session. Any changes/inserts/deletes made to
  * the DB that are not made through the application won't be detected by the module.
  */
 public class HibernateAuditLogInterceptor extends EmptyInterceptor {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final Log log = LogFactory.getLog(HibernateAuditLogInterceptor.class);
 	
 	//Use stacks to take care of nested transactions to avoid NPE since on each transaction 
 	//completion the ThreadLocals get nullified, see code below, i.e a stack of two elements implies
 	//the element at the top of the stack is the inserts made in the inner/nested transaction
 	private ThreadLocal<Stack<HashSet<OpenmrsObject>>> inserts = new ThreadLocal<Stack<HashSet<OpenmrsObject>>>();
 	
 	private ThreadLocal<Stack<HashSet<OpenmrsObject>>> updates = new ThreadLocal<Stack<HashSet<OpenmrsObject>>>();
 	
 	private ThreadLocal<Stack<HashSet<OpenmrsObject>>> deletes = new ThreadLocal<Stack<HashSet<OpenmrsObject>>>();
 	
 	//Mapping between object uuids and maps of their changed property names and their older values, 
 	//the first item in the array is the old value while the the second is the new value
 	private ThreadLocal<Stack<Map<String, Map<String, String[]>>>> objectChangesMap = new ThreadLocal<Stack<Map<String, Map<String, String[]>>>>();
 	
 	//Mapping between entities and lists of their Collections in the current session
 	private ThreadLocal<Stack<Map<Object, List<Collection<?>>>>> entityCollectionsMap = new ThreadLocal<Stack<Map<Object, List<Collection<?>>>>>();
 	
 	//Mapping between parent entity uuids and lists of AuditLogs for their collection elements
 	private ThreadLocal<Stack<Map<String, List<AuditLog>>>> ownerUuidChildLogsMap = new ThreadLocal<Stack<Map<String, List<AuditLog>>>>();
 	
 	//Mapping between collection element uuids and their AuditLogs, will use 
 	//this to avoid creating logs for collections elements multiple times
 	private ThreadLocal<Stack<Map<String, AuditLog>>> childbjectUuidAuditLogMap = new ThreadLocal<Stack<Map<String, AuditLog>>>();
 	
 	//Mapping between parent entities and sets of removed collection elements
 	private ThreadLocal<Stack<Map<OpenmrsObject, HashSet<OpenmrsObject>>>> entityRemovedChildrenMap = new ThreadLocal<Stack<Map<OpenmrsObject, HashSet<OpenmrsObject>>>>();
 	
 	private ThreadLocal<Stack<Date>> date = new ThreadLocal<Stack<Date>>();
 	
 	private AuditLogDAO auditLogDao;
 	
 	//Ignore these properties because they match auditLog.user and auditLog.dateCreated
 	private static final String[] IGNORED_PROPERTIES = new String[] { "changedBy", "dateChanged", "creator", "dateCreated",
 	        "voidedBy", "dateVoided", "retiredBy", "dateRetired", "personChangedBy", "personDateChanged" };
 	
 	/**
 	 * @return the dao
 	 */
 	public AuditLogDAO getAuditLogDao() {
 		if (auditLogDao == null)
 			auditLogDao = Context.getRegisteredComponents(AuditLogDAO.class).get(0);
 		
 		return auditLogDao;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#afterTransactionBegin(org.hibernate.Transaction)
 	 */
 	@Override
 	public void afterTransactionBegin(Transaction tx) {
 		initializeStacksIfNecessary();
 		
 		inserts.get().push(new HashSet<OpenmrsObject>());
 		updates.get().push(new HashSet<OpenmrsObject>());
 		deletes.get().push(new HashSet<OpenmrsObject>());
 		objectChangesMap.get().push(new HashMap<String, Map<String, String[]>>());
 		entityCollectionsMap.get().push(new HashMap<Object, List<Collection<?>>>());
 		ownerUuidChildLogsMap.get().push(new HashMap<String, List<AuditLog>>());
 		childbjectUuidAuditLogMap.get().push(new HashMap<String, AuditLog>());
 		entityRemovedChildrenMap.get().push(new HashMap<OpenmrsObject, HashSet<OpenmrsObject>>());
 		date.get().push(new Date());
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onSave(Object, java.io.Serializable, Object[], String[],
 	 *      org.hibernate.type.Type[])
 	 */
 	@Override
 	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
 		if (isAuditable(entity)) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			if (log.isDebugEnabled())
 				log.debug("Creating log entry for created object with uuid:" + openmrsObject.getUuid() + " of type:"
 				        + entity.getClass().getName());
 			
 			inserts.get().peek().add(openmrsObject);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(Object, java.io.Serializable, Object[],
 	 *      Object[], String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
 	                            String[] propertyNames, Type[] types) {
 		
 		if (propertyNames != null && isAuditable(entity)) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			Map<String, String[]> propertyChangesMap = null;//Map<propertyName, Object[]{currentValue, PreviousValue}>
 			for (int i = 0; i < propertyNames.length; i++) {
 				//we need to ignore dateChanged and changedBy fields in any case they
 				//are actually part of the Auditlog in form of user and dateCreated
 				if (ArrayUtils.contains(IGNORED_PROPERTIES, propertyNames[i]))
 					continue;
 				
 				Object previousValue = (previousState != null) ? previousState[i] : null;
 				Object currentValue = (currentState != null) ? currentState[i] : null;
 				if (!types[i].isCollectionType() && !OpenmrsUtil.nullSafeEquals(currentValue, previousValue)) {
 					//For string properties, ignore changes from null to blank and vice versa
 					//TODO This should be user configurable via a module GP
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
 						propertyChangesMap = new HashMap<String, String[]>();
 					
 					String serializedPreviousValue = InterceptorUtil.serializeObject(previousValue, getAuditLogDao());
 					String serializedCurrentValue = InterceptorUtil.serializeObject(currentValue, getAuditLogDao());
 					
 					propertyChangesMap.put(propertyNames[i],
 					    new String[] { serializedCurrentValue, serializedPreviousValue });
 				}
 			}
 			
 			if (MapUtils.isNotEmpty(propertyChangesMap)) {
 				if (log.isDebugEnabled())
 					log.debug("Creating log entry for updated object with uuid:" + openmrsObject.getUuid() + " of type:"
 					        + entity.getClass().getName());
 				
 				updates.get().peek().add(openmrsObject);
 				objectChangesMap.get().peek().put(openmrsObject.getUuid(), propertyChangesMap);
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onDelete(Object, java.io.Serializable, Object[],
 	 *      String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
 		if (isAuditable(entity)) {
 			OpenmrsObject openmrsObject = (OpenmrsObject) entity;
 			if (log.isDebugEnabled())
 				log.debug("Creating log entry for deleted object with uuid:" + openmrsObject.getUuid() + " of type:"
 				        + entity.getClass().getName());
 			
 			deletes.get().peek().add(openmrsObject);
 		}
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#onCollectionUpdate(Object, java.io.Serializable)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
 		if (collection != null) {
 			PersistentCollection persistentColl = ((PersistentCollection) collection);
 			if (isAuditable(persistentColl.getOwner())) {
 				OpenmrsObject owningObject = (OpenmrsObject) persistentColl.getOwner();
 				updates.get().peek().add(owningObject);
 				Map previousStoredSnapshotMap = (Map) persistentColl.getStoredSnapshot();
 				String ownerUuid = owningObject.getUuid();
 				String propertyName = persistentColl.getRole().substring(persistentColl.getRole().lastIndexOf('.') + 1);
 				
 				if (objectChangesMap.get().peek().get(ownerUuid) == null) {
 					objectChangesMap.get().peek().put(ownerUuid, new HashMap<String, String[]>());
 				}
 				
 				String previousSerializedItems = null;
 				String newSerializedItems = null;
 				if (Collection.class.isAssignableFrom(collection.getClass())) {
 					Collection currentColl = (Collection) collection;
 					previousSerializedItems = InterceptorUtil.serializeCollection(previousStoredSnapshotMap.values(),
 					    getAuditLogDao());
 					newSerializedItems = InterceptorUtil.serializeCollection(currentColl, getAuditLogDao());
 					
 					//Track removed items so that when we create logs for them,
 					//and link them to the parent's log
 					Set<Object> removedItems = new HashSet<Object>();
 					removedItems.addAll(CollectionUtils.subtract(previousStoredSnapshotMap.values(), currentColl));
 					if (!removedItems.isEmpty()) {
 						Class<?> elementClass = removedItems.iterator().next().getClass();
 						if (OpenmrsObject.class.isAssignableFrom(elementClass)) {
 							if (entityRemovedChildrenMap.get().peek().get(owningObject) == null) {
 								entityRemovedChildrenMap.get().peek().put(owningObject, new HashSet<OpenmrsObject>());
 							}
 							for (Object removedItem : removedItems) {
 								OpenmrsObject removed = (OpenmrsObject) removedItem;
 								entityRemovedChildrenMap.get().peek().get(owningObject).add(removed);
 							}
 						}
 					}
 				} else if (Map.class.isAssignableFrom(collection.getClass())) {
 					previousSerializedItems = InterceptorUtil.serializeMap(previousStoredSnapshotMap, getAuditLogDao());
 					newSerializedItems = InterceptorUtil.serializeMap((Map) collection, getAuditLogDao());
 				}
 				
 				objectChangesMap.get().peek().get(ownerUuid)
 				        .put(propertyName, new String[] { newSerializedItems, previousSerializedItems });
 			}
 		}
 	}
 	
 	@Override
 	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
 		//We need to get all collection elements and link their childlogs to the parent's
 		if (collection != null) {
 			PersistentCollection persistentColl = (PersistentCollection) collection;
 			if (isAuditable(persistentColl.getOwner())) {
 				OpenmrsObject owningObject = (OpenmrsObject) persistentColl.getOwner();
 				if (Collection.class.isAssignableFrom(collection.getClass())) {
 					Collection coll = (Collection) collection;
 					if (!coll.isEmpty()) {
 						Class<?> elementClass = coll.iterator().next().getClass();
 						if (OpenmrsObject.class.isAssignableFrom(elementClass)) {
 							if (entityRemovedChildrenMap.get().peek().get(owningObject) == null) {
 								entityRemovedChildrenMap.get().peek().put(owningObject, new HashSet<OpenmrsObject>());
 							}
 							for (Object removedItem : coll) {
 								OpenmrsObject removed = (OpenmrsObject) removedItem;
 								entityRemovedChildrenMap.get().peek().get(owningObject).add(removed);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This is a hacky way to find all loaded persistent objects in this session that have
 	 * collections
 	 * 
 	 * @see org.hibernate.EmptyInterceptor#findDirty(Object, java.io.Serializable, Object[],
 	 *      Object[], String[], org.hibernate.type.Type[])
 	 */
 	@Override
 	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
 	                       String[] propertyNames, Type[] types) {
 		if (getAuditLogDao().isMonitored(entity.getClass())) {
 			if (entityCollectionsMap.get().peek().get(entity) == null) {
 				//This is the first time we are trying to find collection elements for this object
 				if (log.isDebugEnabled())
 					log.debug("Finding collections for object:" + entity.getClass() + " #" + id);
 				
 				for (int i = 0; i < propertyNames.length; i++) {
 					if (types[i].isCollectionType()) {
 						Object coll = currentState[i];
 						//For now ignore maps because still cant imagine a logical case where the
 						//keys or values are Persistent objects that can't exist on their own
 						if (coll != null && Collection.class.isAssignableFrom(coll.getClass())) {
 							Collection<?> collection = (Collection<?>) coll;
 							if (!collection.isEmpty()) {
 								if (entityCollectionsMap.get().peek().get(entity) == null) {
 									entityCollectionsMap.get().peek().put(entity, new ArrayList<Collection<?>>());
 								}
 								
 								entityCollectionsMap.get().peek().get(entity).add(collection);
 							}
 						} //else {
 						  //TODO handle maps too because hibernate treats maps to be of CollectionType
 						  //}
 					}
 				}
 			}
 		}
 		
 		return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
 	}
 	
 	/**
 	 * @see org.hibernate.EmptyInterceptor#beforeTransactionCompletion(org.hibernate.Transaction)
 	 */
 	@Override
 	public void beforeTransactionCompletion(Transaction tx) {
 		try {
 			if (inserts.get().peek().isEmpty() && updates.get().peek().isEmpty() && deletes.get().peek().isEmpty())
 				return;
 			
 			try {
 				//TODO handle daemon or un authenticated operations
 				
 				//If we have any entities in the session that have child collections and there were some updates, 
 				//check all collection items to find dirty ones so that we can mark the the owners as dirty too
 				//I.e if a ConceptName/Mapping/Description was edited, mark the the Concept as dirty too
 				for (Map.Entry<Object, List<Collection<?>>> entry : entityCollectionsMap.get().peek().entrySet()) {
 					for (Collection<?> coll : entry.getValue()) {
 						for (Object obj : coll) {
 							boolean isInsert = OpenmrsUtil.collectionContains(inserts.get().peek(), obj);
 							boolean isUpdate = OpenmrsUtil.collectionContains(updates.get().peek(), obj);
 							
 							//We handle the removed collections items below because either way they
 							//are nolonger in the current collection
 							//This is an IDEA specific comment to suppress warnings
 							if (isInsert || isUpdate) {
 								OpenmrsObject owner = (OpenmrsObject) entry.getKey();
 								boolean ownerHasUpdates = OpenmrsUtil.collectionContains(updates.get().peek(), owner);
 								boolean isOwnerNew = OpenmrsUtil.collectionContains(inserts.get().peek(), owner);
 								if (ownerHasUpdates) {
 									if (log.isDebugEnabled()) {
 										log.debug("There is already an auditlog for owner:" + owner.getClass() + " - "
 										        + owner.getUuid());
 									}
 								} else if (!isOwnerNew) {
 									//A collection item was updated and no other update had been made on the owner
 									if (log.isDebugEnabled()) {
 										log.debug("Creating log entry for edited owner object with uuid:" + owner.getUuid()
 										        + " of type:" + owner.getClass().getName()
 										        + " due to an update for a item in a child collection");
 									}
 									updates.get().peek().add(owner);
 								}
 								
 								if (getAuditLogDao().isMonitored(obj.getClass())) {
 									if (ownerUuidChildLogsMap.get().peek().get(owner.getUuid()) == null)
 										ownerUuidChildLogsMap.get().peek().put(owner.getUuid(), new ArrayList<AuditLog>());
 									
 									OpenmrsObject collElement = (OpenmrsObject) obj;
 									AuditLog childLog = instantiateAuditLog(collElement, isInsert ? Action.CREATED
 									        : Action.UPDATED);
 									
 									childbjectUuidAuditLogMap.get().peek().put(collElement.getUuid(), childLog);
 									ownerUuidChildLogsMap.get().peek().get(owner.getUuid()).add(childLog);
 								}
 								
 								//TODO add this collection to the list of changes properties
 								/*Map<String, String[]> propertyValuesMap = objectChangesMap.get().peek().get(owner.getUuid());
 								if(propertyValuesMap == null)
 									propertyValuesMap = new HashMap<String, String[]>();
 									propertyValuesMap.put(arg0, arg1);*/
 							}
 						}
 					}
 				}
 				
 				for (Map.Entry<OpenmrsObject, HashSet<OpenmrsObject>> entry : entityRemovedChildrenMap.get().peek()
 				        .entrySet()) {
 					OpenmrsObject removedItemsOwner = entry.getKey();
 					for (OpenmrsObject removed : entry.getValue()) {
 						//This should fail for collections that don't have all-delete-orphan cascade
 						//this is idea specific to suppress a warning
 						boolean isDelete = OpenmrsUtil.collectionContains(deletes.get().peek(), removed);
 						if (isDelete) {
 							if (getAuditLogDao().isMonitored(removed.getClass())) {
 								if (ownerUuidChildLogsMap.get().peek().get(removedItemsOwner.getUuid()) == null)
 									ownerUuidChildLogsMap.get().peek()
 									        .put(removedItemsOwner.getUuid(), new ArrayList<AuditLog>());
 								
 								AuditLog childLog = instantiateAuditLog(removed, Action.DELETED);
 								
 								childbjectUuidAuditLogMap.get().peek().put(removed.getUuid(), childLog);
 								ownerUuidChildLogsMap.get().peek().get(removedItemsOwner.getUuid()).add(childLog);
 							}
 						}
 					}
 				}
 				
 				for (OpenmrsObject insert : inserts.get().peek()) {
 					createIfNecessaryAndSaveAuditLog(insert, Action.CREATED);
 				}
 				
 				for (OpenmrsObject delete : deletes.get().peek()) {
 					createIfNecessaryAndSaveAuditLog(delete, Action.DELETED);
 				}
 				
 				for (OpenmrsObject update : updates.get().peek()) {
 					createIfNecessaryAndSaveAuditLog(update, Action.UPDATED);
 				}
 			}
 			catch (Exception e) {
 				//error should not bubble out of the interceptor
 				log.error("An error occured while creating audit log(s):", e);
 			}
 		}
 		finally {
 			//cleanup
 			inserts.get().pop();
 			updates.get().pop();
 			deletes.get().pop();
 			objectChangesMap.get().pop();
 			entityCollectionsMap.get().pop();
 			ownerUuidChildLogsMap.get().pop();
 			childbjectUuidAuditLogMap.get().pop();
 			entityRemovedChildrenMap.get().pop();
 			date.get().pop();
 			
 			removeStacksIfEmpty();
 		}
 	}
 	
 	/**
 	 * Creates if necessary and saves an auditLog in the DB for the specified object
 	 * 
 	 * @param object the object to create for the AuditLog
 	 * @param action see {@link org.openmrs.module.auditlog.AuditLog.Action}
 	 */
 	private void createIfNecessaryAndSaveAuditLog(OpenmrsObject object, Action action) {
 		//If this is a collection element, we already created a log for it
 		AuditLog auditLog = childbjectUuidAuditLogMap.get().peek().get(object.getUuid());
 		if (auditLog == null) {
 			auditLog = instantiateAuditLog(object, action);
 		}
		//save so that it gets an id otherwise hibernate won't be able to associate the child logs if any
		if (auditLog.getAuditLogId() == null) {
			auditLog = getAuditLogDao().save(auditLog);
		}
 		
 		if ((ownerUuidChildLogsMap != null && ownerUuidChildLogsMap.get().peek().containsKey(object.getUuid()))) {
			for (AuditLog child : ownerUuidChildLogsMap.get().peek().get(object.getUuid())) {
				auditLog.addChildAuditLog(child);
				getAuditLogDao().save(child);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new instance of an {@link org.openmrs.module.auditlog.AuditLog} for the specified
 	 * object and Action
 	 * 
 	 * @param object the object to create for the AuditLog
 	 * @param action see {@link org.openmrs.module.auditlog.AuditLog.Action}
 	 * @return the created AuditLog
 	 */
 	private AuditLog instantiateAuditLog(OpenmrsObject object, Action action) {
 		AuditLog auditLog = new AuditLog(object.getClass().getName(), object.getUuid(), action,
 		        Context.getAuthenticatedUser(), date.get().peek());
 		if (action == Action.UPDATED) {
 			Map<String, String[]> propertyValuesMap = objectChangesMap.get().peek().get(object.getUuid());
 			if (propertyValuesMap != null) {
 				auditLog.setChangesData(InterceptorUtil.serializeToJson(propertyValuesMap));
 			}
 		}
 		return auditLog;
 	}
 	
 	/**
 	 * Checks if a class is marked as monitored or is explicitly monitored
 	 * 
 	 * @param entity the entity to check
 	 * @return true if is auditable otherwise false
 	 */
 	private boolean isAuditable(Object entity) {
 		return getAuditLogDao().isMonitored(entity.getClass()) || getAuditLogDao().isImplicitlyMonitored(entity.getClass());
 	}
 	
 	private void initializeStacksIfNecessary() {
 		if (inserts.get() == null) {
 			inserts.set(new Stack<HashSet<OpenmrsObject>>());
 		}
 		if (updates.get() == null) {
 			updates.set(new Stack<HashSet<OpenmrsObject>>());
 		}
 		if (deletes.get() == null) {
 			deletes.set(new Stack<HashSet<OpenmrsObject>>());
 		}
 		if (objectChangesMap.get() == null) {
 			objectChangesMap.set(new Stack<Map<String, Map<String, String[]>>>());
 		}
 		if (entityCollectionsMap.get() == null) {
 			entityCollectionsMap.set(new Stack<Map<Object, List<Collection<?>>>>());
 		}
 		if (ownerUuidChildLogsMap.get() == null) {
 			ownerUuidChildLogsMap.set(new Stack<Map<String, List<AuditLog>>>());
 		}
 		if (childbjectUuidAuditLogMap.get() == null) {
 			childbjectUuidAuditLogMap.set(new Stack<Map<String, AuditLog>>());
 		}
 		if (entityRemovedChildrenMap.get() == null) {
 			entityRemovedChildrenMap.set(new Stack<Map<OpenmrsObject, HashSet<OpenmrsObject>>>());
 		}
 		if (date.get() == null) {
 			date.set(new Stack<Date>());
 		}
 	}
 	
 	private void removeStacksIfEmpty() {
 		if (inserts.get().empty()) {
 			inserts.remove();
 		}
 		if (updates.get().empty()) {
 			updates.remove();
 		}
 		if (deletes.get().empty()) {
 			deletes.remove();
 		}
 		if (objectChangesMap.get().empty()) {
 			objectChangesMap.remove();
 		}
 		if (entityCollectionsMap.get().empty()) {
 			entityCollectionsMap.remove();
 		}
 		if (ownerUuidChildLogsMap.get().empty()) {
 			ownerUuidChildLogsMap.remove();
 		}
 		if (childbjectUuidAuditLogMap.get().empty()) {
 			childbjectUuidAuditLogMap.remove();
 		}
 		if (entityRemovedChildrenMap.get().empty()) {
 			entityRemovedChildrenMap.remove();
 		}
 		if (date.get().empty()) {
 			date.remove();
 		}
 	}
 }
