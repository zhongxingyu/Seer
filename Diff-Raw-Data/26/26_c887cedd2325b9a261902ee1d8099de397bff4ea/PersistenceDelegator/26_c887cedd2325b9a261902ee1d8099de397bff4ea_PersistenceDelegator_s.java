 /*******************************************************************************
  * * Copyright 2011 Impetus Infotech.
  *  *
  *  * Licensed under the Apache License, Version 2.0 (the "License");
  *  * you may not use this file except in compliance with the License.
  *  * You may obtain a copy of the License at
  *  *
  *  *      http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  ******************************************************************************/
 
 package com.impetus.kundera.persistence;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.PersistenceException;
 import javax.persistence.PostPersist;
 import javax.persistence.PostUpdate;
 import javax.persistence.PrePersist;
 import javax.persistence.PreRemove;
 import javax.persistence.PreUpdate;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.impetus.kundera.PersistenceProperties;
 import com.impetus.kundera.client.Client;
 import com.impetus.kundera.client.ClientResolver;
 import com.impetus.kundera.client.ClientType;
 import com.impetus.kundera.client.EnhanceEntity;
 import com.impetus.kundera.index.DocumentIndexer;
 import com.impetus.kundera.index.IndexManager;
 import com.impetus.kundera.metadata.KunderaMetadataManager;
 import com.impetus.kundera.metadata.model.EntityMetadata;
 import com.impetus.kundera.metadata.model.JoinTableMetadata;
 import com.impetus.kundera.metadata.model.KunderaMetadata;
 import com.impetus.kundera.metadata.model.PersistenceUnitMetadata;
 import com.impetus.kundera.metadata.model.Relation;
 import com.impetus.kundera.metadata.model.Relation.ForeignKey;
 import com.impetus.kundera.persistence.event.EntityEventDispatcher;
 import com.impetus.kundera.persistence.handler.impl.EntityInterceptor;
 import com.impetus.kundera.persistence.handler.impl.EntitySaveGraph;
 import com.impetus.kundera.property.PropertyAccessException;
 import com.impetus.kundera.property.PropertyAccessorHelper;
 import com.impetus.kundera.proxy.EnhancedEntity;
 import com.impetus.kundera.query.QueryResolver;
 
 /**
  * The Class PersistenceDelegator.
  */
 public class PersistenceDelegator
 {
 
     /** The Constant log. */
     private static final Log log = LogFactory.getLog(PersistenceDelegator.class);
 
     /** The closed. */
     private boolean closed = false;
 
     /** The session. */
     private EntityManagerSession session;
 
     /** The client map. */
     private Map<String, Client> clientMap;
 
     /** The persistence units. */
     String[] persistenceUnits;
 
     /** The event dispatcher. */
     private EntityEventDispatcher eventDispatcher;
 
     boolean isRelationViaJoinTable;
     /**
      * Instantiates a new persistence delegator.
      * 
      * @param session
      *            the session
      * @param persistenceUnits
      *            the persistence units
      */
     public PersistenceDelegator(EntityManagerSession session, String... persistenceUnits)
     {
         super();
         this.persistenceUnits = persistenceUnits;
         this.session = session;
         eventDispatcher = new EntityEventDispatcher();
     }
 
     // TODO : This method needs serious attention!
     /**
      * Gets the client.
      * 
      * @param m
      *            the m
      * @return the client
      */
     public Client getClient(EntityMetadata m)
     {
         Client client = null;
 
         // Persistence Unit used to retrieve client
         String persistenceUnit = null;
 
         if (getPersistenceUnits().length == 1)
         {
             //
             persistenceUnit = getPersistenceUnits()[0];
 
             String puInMetadata = m.getPersistenceUnit();
 
             // Case if pu is blank or pu passed is not equal '@'
             if (!StringUtils.isBlank(puInMetadata))
             {
                 if (StringUtils.isBlank(persistenceUnit) || !persistenceUnit.equals(puInMetadata))
                 {
                     throw new PersistenceException(
                             "Persistence Unit defined at entity can't differ from the one provided for EMF");
                 }
             }
             else
             {
                 // If '@' not given and pu supplied is not of RDBMS
                 Map<String, PersistenceUnitMetadata> puMetadataMap = KunderaMetadata.INSTANCE.getApplicationMetadata()
                         .getPersistenceUnitMetadataMap();
                 boolean found = false;
                 for (PersistenceUnitMetadata puMetadata : puMetadataMap.values())
                 {
                     Properties props = puMetadata.getProperties();
                     String clientName = props.getProperty(PersistenceProperties.KUNDERA_CLIENT);
                     if (ClientType.RDBMS.name().equalsIgnoreCase(clientName))
                     {
                         if (persistenceUnit.equals(puMetadata.getPersistenceUnitName()))
                         {
                             found = true;
                             break;
 
                         }
                     }
 
                 }
 
                 if (!found)
                 {
                     throw new PersistenceException(
                             "Invalid persistence unit configuration! should be intended for RDBMS, else must annotate @Table(name = table_col_family_name, schema = keyspace@pu");
                 }
 
             }
 
         }
         else
         {
             // TODO : this must not be handled here.
             String puInMetadata = m.getPersistenceUnit();
 
             if (StringUtils.isEmpty(puInMetadata))
             {
                 Map<String, PersistenceUnitMetadata> puMetadataMap = KunderaMetadata.INSTANCE.getApplicationMetadata()
                         .getPersistenceUnitMetadataMap();
                 for (PersistenceUnitMetadata puMetadata : puMetadataMap.values())
                 {
                     Properties props = puMetadata.getProperties();
                     String clientName = props.getProperty(PersistenceProperties.KUNDERA_CLIENT);
                     if (ClientType.RDBMS.name().equalsIgnoreCase(clientName))
                     {
                         persistenceUnit = puMetadata.getPersistenceUnitName();
                         break;
                     }
 
                 }
             }
             else
             {
                 persistenceUnit = puInMetadata;
             }
 
             if (persistenceUnit == null || !Arrays.asList(getPersistenceUnits()).contains(persistenceUnit))
             {
                 throw new PersistenceException("Invalid persistence configuration!");
             }
 
         }
 
         // single persistence unit given and entity is annotated with '@'.
         // validate persistence unit given is same
 
         // If client has already been created, return it, or create it and put
         // it into client map
         if (clientMap == null || clientMap.isEmpty())
         {
             clientMap = new HashMap<String, Client>();
             client = ClientResolver.getClient(persistenceUnit);
             clientMap.put(persistenceUnit, client);
 
         }
         else if (clientMap.get(persistenceUnit) == null)
         {
             client = ClientResolver.getClient(persistenceUnit);
             clientMap.put(persistenceUnit, client);
         }
         else
         {
             client = clientMap.get(persistenceUnit);
         }
 
         return client;
     }
 
     /**
      * Gets the session.
      * 
      * @return the session
      */
     private EntityManagerSession getSession()
     {
         return session;
     }
 
     /**
      * Gets the event dispatcher.
      * 
      * @return the event dispatcher
      */
     private EntityEventDispatcher getEventDispatcher()
     {
         return eventDispatcher;
     }
 
     /**
      * Find.
      * 
      * @param <E>
      *            the element type
      * @param entityClass
      *            the entity class
      * @param primaryKeys
      *            the primary keys
      * @return the list
      */
     public <E> List<E> find(Class<E> entityClass, Object... primaryKeys)
     {
         List<E> entities = new ArrayList<E>();
         Set pKeys = new HashSet(Arrays.asList(primaryKeys));
         for (Object primaryKey : pKeys)
         {
             entities.add(find(entityClass, primaryKey));
         }
         return entities;
     }
 
     /**
      * Find.
      * 
      * @param <E>
      *            the element type
      * @param entityClass
      *            the entity class
      * @param embeddedColumnMap
      *            the embedded column map
      * @return the list
      */
     public <E> List<E> find(Class<E> entityClass, Map<String, String> embeddedColumnMap)
     {
         EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(entityClass, getPersistenceUnits());
 
         List<E> entities = new ArrayList<E>();
         try
         {
             entities = getClient(entityMetadata).find(entityClass, embeddedColumnMap);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         return entities;
     }
 
     /**
      * Merge.
      * 
      * @param <E>
      *            the element type
      * @param e
      *            the e
      * @return the e
      */
     public <E> E merge(E e)
     {
         try
         {
             List<EnhancedEntity> reachableEntities = EntityResolver
                     .resolve(e, CascadeType.MERGE, getPersistenceUnits());
 
             // save each one
 //            for (EnhancedEntity o : reachableEntities)
 //            {
                 log.debug("Merging Entity : " + e);
 
                 EntityMetadata m = KunderaMetadataManager.getEntityMetadata(e.getClass(),
                         getPersistenceUnits());
 
                 // TODO: throw OptisticLockException if wrong version and
                 // optimistic locking enabled
 
                 // fire PreUpdate events
                 getEventDispatcher().fireEventListeners(m, e, PreUpdate.class);
 
                 persist(e);
 
                 // fire PreUpdate events
                 getEventDispatcher().fireEventListeners(m, e, PostUpdate.class);
 //            }
         }
         catch (Exception exp)
         {
             throw new PersistenceException(exp);
         }
 
         return e;
     }
 
     /**
      * Removes the.
      * 
      * @param e
      *            the e
      */
     public void remove(Object e)
     {
         try
         {
             EntityMetadata metadata = getMetadata(e.getClass());
             getEventDispatcher().fireEventListeners(metadata, e, PreRemove.class);
 
             EntityInterceptor interceptor = new EntityInterceptor();
             List<EntitySaveGraph> objectGraphs = interceptor.handleRelation(e, metadata);
             for (EntitySaveGraph objectGraph : objectGraphs)
             {
                 removeGraph(objectGraph);
                 // If cascade delete then delete parent and child. Else delete
                 // marked entity only.
                 // If parent entity is marked for delete
             }
             getEventDispatcher().fireEventListeners(metadata, e, PostPersist.class);
             log.debug("Data removed successfully for entity : " + e.getClass());
         }
 
         catch (Exception exp)
         {
             throw new PersistenceException(exp);
         }
     }
 
     /**
      * Save graph.
      * 
      * @param objectGraph
      *            the object graph
      * @throws Exception
      *             the exception
      */
     private void removeGraph(EntitySaveGraph objectGraph) throws Exception
     {
         EntityMetadata metadata = null;
         Object parentEntity = objectGraph.getParentEntity();
         if (parentEntity != null)
         {
             metadata = getMetadata(objectGraph.getParentClass());
             objectGraph.setParentId(getId(parentEntity, metadata));
         }
 
 
         // if (getSession().lookup(parentEntity.getClass(),
         // objectGraph.getParentId()) != null)
         // {
         Client pClient = getClient(metadata);
         pClient.delete(parentEntity, objectGraph.getParentId(), metadata);
 
         Object childEntity = objectGraph.getChildEntity();
 
         // If any association exists.
         if (childEntity != null)
         {
             onClientHandle(objectGraph, childEntity);
         }
 
         session.remove(parentEntity.getClass(), objectGraph.getParentEntity());
 
         // } else
         // {
         // // throw new
         // PersistenceException("invalid operation on detached entity:" +
         // parentEntity.getClass() + " for id :" + objectGraph.getParentId());
         // }
 
     }
 
     /**
      * On client persist.
      * 
      * @param objectGraph
      *            the object graph
      * @param childEntity
      *            the child entity
      * @throws Exception
      *             the exception
      */
     private void onClientHandle(EntitySaveGraph objectGraph, Object childEntity) throws Exception
     {
         if (childEntity instanceof Collection<?>)
         {
             Collection<?> childCol = (Collection<?>) childEntity;
             for (Object ch : childCol)
             {
                 onClientDelete(ch, objectGraph);
             }
 
         }
         else
         {
             onClientDelete(childEntity, objectGraph);
         }
     }
 
     /**
      * Handle client.
      * 
      * @param child
      *            the child
      * @param objectGraph
      *            the object graph
      * @throws Exception
      *             the exception
      */
     private void onClientDelete(Object child, EntitySaveGraph objectGraph) throws Exception
     {
         // If child entity doesn't have any further relations, just delete it from database
         // Otherwise treat it as parent entity for its related entities,
         // determine graph and delete that graph recursively.
 
         EntityMetadata metadata = getMetadata(objectGraph.getChildClass());
         List<Relation> relations = metadata.getRelations();
         
         boolean imChildProcessed = false;
         
         List<EntitySaveGraph> objectGraphs = getGraph(child, metadata);
         if (!((relations == null || relations.isEmpty()) || objectGraph.isIsswapped()))
         {
             for (EntitySaveGraph graph : objectGraphs)
             {
                 // This this graph is for an entity that has it's own
                 // parent,
                 // set reverse Foreign Key
                 // i.e. Foreign key that refers to its parent
                 if (!graph.equals(objectGraph))
                 {
                     graph.setRevFKeyName(objectGraph.getfKeyName());
                     graph.setRevFKeyValue(objectGraph.getParentId());
                     graph.setRevParentClass(objectGraph.getParentClass());
                     imChildProcessed = true;
                     removeGraph(graph);
                 }
             }
         }
 
         // In case immediate child is not yet processed!
         if (!imChildProcessed)
         {           
             String id = getId(child, metadata);
             objectGraph.setChildId(id);
             // if (getSession().lookup(child.getClass(), id) == null)
             // {
             Client chClient = getClient(metadata);
             chClient.delete(child, id, metadata);
             // session.store(id, child);
             // }
         }          
 
     }
 
     /**
      * Persist.
      * 
      * @param e
      *            the e
      */
     public void persist(Object e)
     {
         try
         {
             // Invoke Pre Persist Events
             EntityMetadata metadata = getMetadata(e.getClass());
             getEventDispatcher().fireEventListeners(metadata, e, PrePersist.class);
 
             // Get Object graph list for this top level entity, and save them
             // one by one.
             List<EntitySaveGraph> objectGraphs = getGraph(e, metadata);
             for (EntitySaveGraph objectGraph : objectGraphs)
             {
                 saveGraph(objectGraph);
             }
 
             // Invoke Post Persist Events
             getEventDispatcher().fireEventListeners(metadata, e, PostPersist.class);
             log.debug("Data persisted successfully for entity : " + e.getClass());
         }
         catch (Exception exp)
         {
             exp.printStackTrace();
             throw new PersistenceException(exp);
 
         }
     }
 
     /**
      * Find.
      * 
      * @param <E>
      *            the element type
      * @param entityClass
      *            the entity class
      * @param primaryKey
      *            the primary key
      * @return the e
      */
     public <E> E find(Class<E> entityClass, Object primaryKey)
     {
         try
         {
             // Look up in session first
            E e = getSession().lookup(entityClass, primaryKey);
             isRelationViaJoinTable = false;
 
            if (null != e)
            {
                log.debug(entityClass.getName() + "_" + primaryKey + " is loaded from cache!");
                return e;
            }
 
             // Find top level entity first
             EntityMetadata entityMetadata = KunderaMetadataManager
                     .getEntityMetadata(entityClass, getPersistenceUnits());
 
             Client client = getClient(entityMetadata);
 
             List<EntitySaveGraph> objectGraphs = getGraph(entityMetadata.getEntityClazz().newInstance(), entityMetadata);
             Map<Boolean, List<String>> relations = getRelations(objectGraphs, entityMetadata.getEntityClazz());
 
             EntityReader reader = getReader(client);
             List<String> relationNames = relations.values().iterator().next();
 
             String rowKey = primaryKey + "";
 
             EnhanceEntity enhanceEntity = reader.findById(rowKey, entityMetadata, relationNames, client);
 
             Map<Object, Object> relationalValues = new HashMap<Object, Object>();
             if(enhanceEntity == null || enhanceEntity.getEntity() == null)
             {
                 return null;
             }
             E entity = (E) enhanceEntity.getEntity();
             if (relationNames.isEmpty() && !entityMetadata.isRelationViaJoinTable())
             {
                 return entity;
             }
             else
             {
                 entity = (E) reader.computeGraph(enhanceEntity, objectGraphs, relationalValues, client, entityMetadata,
                         this);
             }
             boolean isCacheableToL2 = entityMetadata.isCacheable();
             getSession().store(primaryKey, entity, isCacheableToL2);
 
             // Populate Association,
             return entity;
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
             throw new PersistenceException(exception);
         }
     }
 
     /**
      * Creates the query.
      * 
      * @param jpaQuery
      *            the jpa query
      * @return the query
      */
     public Query createQuery(String jpaQuery)
     {
         Query query = new QueryResolver().getQueryImplementation(jpaQuery, this, persistenceUnits);
 
         return query;
 
     }
 
     /**
      * Checks if is open.
      * 
      * @return true, if is open
      */
     public final boolean isOpen()
     {
         return !closed;
     }
 
     /**
      * Close.
      */
     public final void close()
     {
         eventDispatcher = null;
         persistenceUnits = null;
 
         // Close all clients created in this session
         if (clientMap != null && !clientMap.isEmpty())
         {
             for (Client client : clientMap.values())
             {
                 client.close();
             }
             clientMap.clear();
             clientMap = null;
         }
 
         closed = true;
     }
 
     /**
      * On compute graph.
      *
      * @param entity the entity
      * @param objectGraph the object graph
      * @param client the client
      * @param rowId the row id
      * @param entityClass the entity class
      */
 
     private void onComputeGraph(Object entity, EntitySaveGraph objectGraph, Client client, String rowId,
             Class<?> entityClass)
     {
         IndexManager mgr = null;
         Set<?> chids = new HashSet();
         EntityMetadata childMetadata = null;
         Client childClient = null;
         Class<?> childClazz = null;
         try
         {
 
             if (entity.getClass().equals(objectGraph.getChildClass()))
             {
                 String query = AssociationBuilder.getQuery(DocumentIndexer.PARENT_ID_CLASS, 
                                                            objectGraph.getParentClass().getCanonicalName().toLowerCase(), 
                                                            DocumentIndexer.ENTITY_ID_FIELD, rowId);
                 
                 childClazz = objectGraph.getParentClass();
                 childMetadata = getMetadata(childClazz);
                 childClient = getClient(childMetadata);
 
                 chids = populateAssociation(entity, objectGraph.getProperty(), childClient, query, objectGraph.getParentClass(), true);
 
             }
             else
             {
                 childClazz = objectGraph.getChildClass();
                 childMetadata = getMetadata(childClazz);
                 childClient = getClient(childMetadata);
 
                 String query = AssociationBuilder.getQuery(DocumentIndexer.PARENT_ID_CLASS, 
                                                            entity.getClass().getCanonicalName().toLowerCase(), 
                                                            DocumentIndexer.PARENT_ID_FIELD, rowId);
                
                 //if it is a case of shared primary key then it will share same primary key!
                 
                 if(objectGraph.isSharedPrimaryKey())
                 {
                     List c = new ArrayList();
                     c.add(childClient.find(childClazz, rowId));
                     chids =  onReflect(entity, objectGraph.getProperty(), c);
                 }
                 else
                 {
                     chids = populateAssociation(entity, objectGraph.getProperty(), childClient, query, childClazz, false);
                 }
             }
 
 
             onBiDirection(entity, objectGraph, client, rowId, entityClass, chids, childMetadata, childClient);
         }
         catch (Exception e)
         {
             // TODO Proper handling is must.
             e.printStackTrace();
         }
 
     }
 
     /**
      * Populate association.
      *
      * @param entity the entity
      * @param f the f
      * @param childClient the child client
      * @param query the query
      * @param clazz the clazz
      * @return the sets the
      * @throws PropertyAccessException the property access exception
      */
     private Set<?> populateAssociation(Object entity, Field f, Client childClient, String query, Class<?> clazz, boolean fetchRelation)
             throws PropertyAccessException
     {
 //        Set<?> chids;
         List<?> childs = onAssociation(clazz, childClient, fetchRelation, query, false, null);
 //        chids = new HashSet(childs);
 //        Field f = objectGraph.getProperty();
         
 //        PropertyAccessorHelper.set(entity, f, PropertyAccessorHelper.isCollection(f.getType()) ? getFieldInstance(childs, f)
 //                : childs.get(0));
         return onReflect(entity, f, childs);
     }
 
     
     private Set<?>  onReflect(Object entity, Field f, List<?> childs) throws PropertyAccessException
     {
         Set chids = new HashSet();
         if(childs != null)
         {
         chids = new HashSet(childs);
         //TODO: need to store object in sesion.
 //        getSession().store(id, entity)
         PropertyAccessorHelper.set(entity, f, PropertyAccessorHelper.isCollection(f.getType()) ? getFieldInstance(childs, f)
                 : childs.get(0));
         }
         return chids; 
     }
     
     /**
      * On association.
      *
      * @param clazz the clazz
      * @param client the client
      * @param fetchRelation the fetch relation
      * @param query the query
      * @return the list
      */
     private List<Object> onAssociation(Class<?> clazz, Client client, boolean fetchRelation, String query, boolean biDirectional, String rowId)
     {
         try
         {
             IndexManager ixManager = client.getIndexManager();
             Map<String, String> results = fetchRelation ? ixManager.fetchRelation(query) : ixManager.search(query);
 //            System.out.println(results);
             Set<String> rsSet = new HashSet<String>(results.values());
             if(biDirectional)
             {
                 rsSet.remove(rowId);
             }
             return rsSet.isEmpty()? null:(List<Object>) client.find(clazz, rsSet.toArray(new String[] {}));
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
         return null;
     }
     
     /**
      * On bi direction.
      *
      * @param entity the entity
      * @param objectGraph the object graph
      * @param client the client
      * @param rowId the row id
      * @param entityClass the entity class
      * @param chids the chids
      * @param childMetadata the child metadata
      * @param childClient the child client
      * @throws Exception the exception
      */
     private void onBiDirection(Object entity, EntitySaveGraph objectGraph, Client client, String rowId,
             Class<?> entityClass, Set<?> chids, EntityMetadata childMetadata, Client childClient) throws Exception
     {
         IndexManager mgr;
         if (!objectGraph.isUniDirectional())
         {
 
             for (Object child : chids)
             {
                 //Add original fetched entity.
                 List obj = new ArrayList();
                 obj.add(entity);
 
                 Relation relation = childMetadata.getRelation(objectGraph.getBidirectionalProperty().getName());
          
                 
                 //If relation is One to Many or MANY TO MANY for associated entity. Require to fetch all associated entity
                 if (relation.getType().equals(ForeignKey.ONE_TO_MANY)
                         || 
                     relation.getType().equals(ForeignKey.MANY_TO_MANY))
                 {
                     String query = null;
                     try
                     {
                         String id = PropertyAccessorHelper.getId(child, childMetadata);
                         query = AssociationBuilder.getQuery(DocumentIndexer.PARENT_ID_CLASS, child.getClass()
                                 .getCanonicalName().toLowerCase(), DocumentIndexer.PARENT_ID_FIELD, id);
                         List<Object> results = onAssociation(entityClass, client, false, query, true, rowId);
 						if (results != null) {
 							obj.addAll(results);
 						}
                     }
                     catch (PropertyAccessException e)
                     {
                         throw new RuntimeException(e.getMessage());
                     }
                     
                     //In case of other parent object found for given bidirectional.
                     for (Object o : obj)
                     {
                         Field f = objectGraph.getProperty();
                         if (PropertyAccessorHelper.isCollection(f.getType()))
                         {
                             List l = new ArrayList();
                             l.add(child);
                             Object oo = getFieldInstance(l, f);
                             PropertyAccessorHelper.set(o, f, oo);
                         }
                         else
                         {
                             PropertyAccessorHelper.set(o, f, child);
                         }
 
                     }
                 }
                 try
                 {
                     PropertyAccessorHelper.set(child, objectGraph.getBidirectionalProperty(),
                                                 PropertyAccessorHelper.isCollection(
                                                 objectGraph.getBidirectionalProperty().getType()) ? 
                                                 getFieldInstance(obj, objectGraph.getBidirectionalProperty()): entity);
                 }
                 catch (PropertyAccessException e)
                 {
                     throw new RuntimeException(e.getMessage());
                 }
             }
         }
     }
 
 
     /**
      * Gets the field instance.
      *
      * @param chids the chids
      * @param f the f
      * @return the field instance
      */
     private Object getFieldInstance(List chids, Field f)
     {
 
         if (Set.class.isAssignableFrom(f.getType()))
         {
             Set col = new HashSet(chids);
             return col;
         }
         return chids;
     }
 
    
 
     /**
      * Gets the persistence units.
      * 
      * @return the persistence units
      */
     private String[] getPersistenceUnits()
     {
         return persistenceUnits;
     }
 
     /**
      * Gets the metadata.
      * 
      * @param clazz
      *            the clazz
      * @return the metadata
      */
     public EntityMetadata getMetadata(Class<?> clazz)
     {
         return KunderaMetadataManager.getEntityMetadata(clazz, getPersistenceUnits());
     }
 
     /**
      * Saves an object graph to persistence stores. An object graph contains a
      * parent entity and one or more child entities at a time. There are other
      * attributes too that represent their relationship. *
      * 
      * @param objectGraph
      *            the object graph
      */
     private void saveGraph(EntitySaveGraph objectGraph)
     {
 
         // Persist parent entity
         Object parentEntity = objectGraph.getParentEntity();
         EntityMetadata metadata = getMetadata(objectGraph.getParentClass());
         if (parentEntity != null)
         {
 
             objectGraph.setParentId(getId(parentEntity, metadata));
 
 //            if (getSession().lookup(parentEntity.getClass(), objectGraph.getParentId()) == null)
 //            {
                 Client pClient = getClient(metadata);
                 pClient.persist(objectGraph, metadata);
                 session.store(objectGraph.getParentId(), objectGraph.getParentEntity());
 
 //            }
         }
 
         // Persist child entity(ies)
         Object childEntity = objectGraph.getChildEntity();
         if (childEntity != null)
         {
             persistChildEntity(objectGraph, childEntity);
         }
 
         // Persist Join Table
         for (Relation relation : metadata.getRelations())
         {
             if (relation.isRelatedViaJoinTable())
             {
 
                 JoinTableMetadata jtMetadata = relation.getJoinTableMetadata();
                 String joinTableName = jtMetadata.getJoinTableName();
 
                 Set<String> joinColumns = jtMetadata.getJoinColumns();
                 Set<String> inverseJoinColumns = jtMetadata.getInverseJoinColumns();
 
                 String joinColumnName = (String) joinColumns.toArray()[0];
                 String inverseJoinColumnName = (String) inverseJoinColumns.toArray()[0];
 
                 EntityMetadata relMetadata = getMetadata(objectGraph.getChildClass());
 
                 Client pClient = getClient(metadata);
                 pClient.persistJoinTable(joinTableName, joinColumnName, inverseJoinColumnName, relMetadata, objectGraph);
 
             }
         }
 
     }
 
     /**
      * Persist child entities of a given object graph into persistence store.
      * 
      * @param objectGraph
      *            the object graph
      * @param childEntity
      *            the child entity, It maybe one entity or a collection of
      *            entities.
      */
     private void persistChildEntity(EntitySaveGraph objectGraph, Object childEntity)
     {
         if (childEntity instanceof Collection<?>)
         {
             Collection<?> childCol = (Collection<?>) childEntity;
             for (Object ch : childCol)
             {
                 persistOneChildEntity(ch, objectGraph);
             }
 
         }
         else
         {
             persistOneChildEntity(childEntity, objectGraph);
         }
     }
 
     /**
      * Persist one child entity into persistence store. Also checks whether this
      * child entity has further relationships. If yes, it generates a graph for
      * them and saves them recursively up to "n" level.
      * 
      * @param child
      *            the child Entity Object.
      * @param objectGraph
      *            the object graph to which this child entity belongs.
      */
     private void persistOneChildEntity(Object child, EntitySaveGraph objectGraph)
     {
         if (objectGraph.getChildId() != null || getSession().lookup(child.getClass(), objectGraph.getChildId()) == null)
         {
             EntityMetadata metadata = getMetadata(objectGraph.getChildClass());
 
             boolean imChildProcessed = false;
 
             List<Relation> relations = metadata.getRelations();
 
             // If child entity doesn't have any further relations, just persist
             // it
             // into database
             // Otherwise treat it as parent entity for its related entities,
             // determine graph and save that graph recursively.
 
             List<EntitySaveGraph> objectGraphs = getGraph(child, metadata);
             if (!((relations == null || relations.isEmpty()) || objectGraph.isIsswapped()))
             {
                 for (EntitySaveGraph graph : objectGraphs)
                 {
                     // This this graph is for an entity that has it's own
                     // parent,
                     // set reverse Foreign Key
                     // i.e. Foreign key that refers to its parent
                     if (!graph.equals(objectGraph))
                     {
                         graph.setRevFKeyName(objectGraph.getfKeyName());
                         graph.setRevFKeyValue(objectGraph.getParentId());
                         graph.setRevParentClass(objectGraph.getParentClass());
                         imChildProcessed = true;
                         saveGraph(graph);
                     }
                 }
             }
 
             // In case immediate child is not yet processed!
             if (!imChildProcessed)
             {
                 saveImmediateChild(child, objectGraph, metadata);
             }            
        
         }
     }
 
     private void saveImmediateChild(Object child, EntitySaveGraph objectGraph, EntityMetadata metadata)
     {
         String id = getId(child, metadata);
         objectGraph.setChildId(id);
 //                    if (getSession().lookup(child.getClass(), id) == null)
 //                    {
             Client chClient = getClient(metadata);
             chClient.persist(child, objectGraph, metadata);
             session.store(id, child);
     }
 
     /**
      * Gets the id.
      * 
      * @param entity
      *            the entity
      * @param metadata
      *            the metadata
      * @return the id
      */
     public String getId(Object entity, EntityMetadata metadata)
     {
         try
         {
             return PropertyAccessorHelper.getId(entity, metadata);
         }
         catch (PropertyAccessException e)
         {
             throw new PersistenceException(e.getMessage());
         }
 
     }
 
     /**
      * Returns entity save graph collection for given entity.
      * 
      * @param entity
      *            entity in question
      * @param metadata
      *            entity's metadata
      * @return Collection of entity save graph
      */
     public List<EntitySaveGraph> getGraph(Object entity, EntityMetadata metadata)
     {
         EntityInterceptor interceptor = new EntityInterceptor();
         return interceptor.handleRelation(entity, metadata);
     }
 
     /**
      * Store.
      * 
      * @param id
      *            the id
      * @param entity
      *            the entity
      */
     public void store(Object id, Object entity)
     {
         session.store(id, entity);
     }
 
     /**
      * Store.
      * 
      * @param entities
      *            the entities
      * @param entityMetadata
      *            the entity metadata
      */
     public void store(List entities, EntityMetadata entityMetadata)
     {
         for (Object o : entities)
             session.store(getId(o, entityMetadata), o);
     }
 
     /**
      * Gets the relations.
      * 
      * @param graphs
      *            the graphs
      * @param clazz
      *            the clazz
      * @return the relations
      */
     public Map<Boolean, List<String>> getRelations(List<EntitySaveGraph> graphs, Class clazz)
     {
         List<String> relationNames = new ArrayList<String>(graphs.size());
         boolean isParent = false;
         Map<Boolean, List<String>> relationHolder = new HashMap<Boolean, List<String>>(1);
         // TODO need to check if there is any relation?
         for (EntitySaveGraph g : graphs)
         {
             if (clazz.equals(g.getParentClass()))
             {
                 isParent = true;
                 // Means entity is parent
             }
 
             if (g.getfKeyName() != null)
             {
                 relationNames.add(g.getfKeyName());
             }
         }
 
         relationHolder.put(isParent, relationNames);
         return relationHolder;
 
     }
 
     /**
      * Gets the reader.
      * 
      * @param client
      *            the client
      * @return the reader
      */
     public EntityReader getReader(Client client)
     {
         return client.getReader();
     }
 
 }
