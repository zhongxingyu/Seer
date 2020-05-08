 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.common.repository;
 
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import org.apache.ibatis.session.SqlSession;
 import org.sola.common.SOLAException;
 import org.sola.common.messaging.ServiceMessage;
 import org.sola.services.common.EntityAction;
 import org.sola.services.common.LocalInfo;
 import org.sola.services.common.ejbs.AbstractEJBLocal;
 import org.sola.services.common.faults.FaultUtility;
 import org.sola.services.common.repository.entities.AbstractCodeEntity;
 import org.sola.services.common.repository.entities.AbstractEntity;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 import org.sola.services.common.repository.entities.ChildEntityInfo;
 import org.sola.services.common.repository.entities.ColumnInfo;
 
 /**
  * Implementation of the {@linkplain CommonRepository} interface that uses the
  * Mybatis library to connect to the database and execute SQL statements.
  *
  * @author soladev
  */
 public class CommonRepositoryImpl implements CommonRepository {
 
     /**
      * The default name of the mybatis configuation file -
      * mybatisConnectionConfig.xml
      */
     private static final String LOAD_INHIBITORS = "Repository.loadInhibitors";
     private DatabaseConnectionManager dbConnectionManager = null;
 
     /**
      * Loads the myBatis configuration file and initializes a connection to the
      * database.
      *
      * @param connectionConfigFileUrl URL of the myBatis config file to load
      */
     public CommonRepositoryImpl(URL connectionConfigFileUrl) {
 
         if (connectionConfigFileUrl == null) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     // Capture the specific details so they are added to the log
                     new Object[]{"File named " + CONNECT_CONFIG_FILE_NAME + " is not located in "
                 + "the default resource package for " + this.getClass().getSimpleName()});
         }
         dbConnectionManager = new DatabaseConnectionManager(connectionConfigFileUrl.toString(),
                 CommonMapper.class);
     }
 
     /**
      * Returns the {@linkplain DatabaseConnectionManager} used for this instance
      * of the repository.
      */
     public DatabaseConnectionManager getDbConnectionManager() {
         return dbConnectionManager;
     }
 
     /**
      * Allows the {@linkplain DatabaseConnectionManager} used for the repository
      * to be replaced. This is useful for Unit testing and allows simple mocking
      * of the connection to the database.
      *
      * @param dbConnectionManager the Mock Database Connection Manager.
      */
     @Override
     public void setDbConnectionManager(DatabaseConnectionManager dbConnectionManager) {
         this.dbConnectionManager = dbConnectionManager;
     }
 
     /**
      * Returns the MyBatis SQL session retrieved from the
      * {@linkplain DatabaseConnectionManager}
      */
     protected SqlSession getSqlSession() {
         return getDbConnectionManager().getSqlSession();
     }
 
     /**
      * Returns the MyBatis Mapper Class retrieved from the
      * {@linkplain DatabaseConnectionManager}
      */
     protected CommonMapper getMapper(SqlSession session) {
         return session.getMapper(getDbConnectionManager().getMapperClass());
     }
 
     /**
      * Sets the loaded flag on the entity to indicate it has been loaded from
      * the database.
      *
      * @param entity The entity to flag
      */
     protected void markAsLoaded(AbstractReadOnlyEntity entity) {
         if (entity != null) {
             entity.setLoaded(true);
             // Don't reset the EntityAction if the entity is in the process of being saved as it
             // is probably getting refreshed. 
             if (entity instanceof AbstractEntity && !((AbstractEntity) entity).isSaving()) {
                 ((AbstractEntity) entity).resetEntityAction();
             }
         }
     }
 
     /**
      * Overloaded version of {@linkplain #markAsLoaded(AbstractReadOnlyEntity)}
      * that marks every entity in a list as loaded.
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param entities The list of entities to mark as loaded.
      */
     protected <T extends AbstractReadOnlyEntity> void markAsLoaded(List<T> entities) {
         if (entities != null) {
             for (AbstractReadOnlyEntity entity : entities) {
                 markAsLoaded(entity);
             }
         }
     }
 
     /**
      * Clears the load inhibitors set on the repository. See
      * {@linkplain #setLoadInhibitors()}.
      */
     @Override
     public void clearLoadInhibitors() {
         if (LocalInfo.get(LOAD_INHIBITORS) != null) {
             LocalInfo.set(LOAD_INHIBITORS, null, true);
         }
     }
 
     /**
      * Allows an array of entity classes to be set as inhibitors for a given SQL
      * Query. <p> The Common Repository will attempt to eagerly load all child
      * entities of an entity when that entity is loaded via a getEntity or
      * getEntityList method. In some cases, the child entities are not required
      * and the additional load is an unnecessary performance overhead. The Load
      * Inhibitors allows the developer to indicate which child entities should
      * not be loaded based on the child entity class. </p> <p> Once set, the
      * load inhibitors remain set until the developer calls the {@linkplain
      * #clearLoadInhibitors()} method. This is to ensure any level of the child
      * hierarchy can be inhibited, but also means that the clear method should
      * be called once the necessary loading is complete </p>
      *
      * @param entityClasses The array of child entity classes that should not be
      * loaded.
      */
     @Override
     public void setLoadInhibitors(Class<?>[] entityClasses) {
         LocalInfo.set(LOAD_INHIBITORS, Arrays.asList(entityClasses));
     }
 
     /**
      * Checks if the entity class has been flagged as load inhibited.
      *
      * @param entityClass The entity class to check
      * @return true if load of the entity class is inhibited, false otherwise.
      */
     private Boolean isInhibitLoad(Class<?> entityClass) {
         Boolean result = false;
         List<Class<?>> inhibitors = LocalInfo.get(LOAD_INHIBITORS, List.class);
         if (inhibitors != null && !inhibitors.isEmpty()) {
             result = inhibitors.contains(entityClass);
         }
         return result;
     }
 
     /**
      * Retrieves a child entity that is in a one to one relationship with its
      * parent entity. <p> To customize the default join criteria used to load
      * the child entity, override the parent entity
      * {@linkplain AbstractReadOnlyEntity#getChildJoinSqlParams} to return the
      * appropriately configured SQL Parameters. </p>
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param <V> The generic type of the child entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}.
      * @param parentEntity The parent entity that references the child entity to
      * load
      * @param childEntityClass The class of the child entity to load.
      * @param childInfo Provides the details from the {@linkplain ChildEntity}
      * annotation that are used to assist loading of the child entity.
      * @param mapper The Mybatis mapper class used for this loading process.
      * @return The child entity or null if there is no child to load.
      */
     private <T extends AbstractReadOnlyEntity, U extends CommonMapper, V extends AbstractReadOnlyEntity> V getChildEntity(
             T parentEntity, Class<V> childEntityClass, ChildEntityInfo childInfo, U mapper) {
 
         // This is a one to one child. Check if the parent entity has customized join criteria
         // for the child. 
         Map params = parentEntity.getChildJoinSqlParams(childInfo);
         V child = null;
         boolean loadChild = true;
         if (params == null) {
             // The parent does not have any customized criteria. Use the default join logic to 
             // retrieve the child entity. 
             params = new HashMap<String, Object>();
             loadChild = false;
             if (childInfo.isInsertBeforeParent()) {
                 // The parent holds the id of the child entity
                 String childId = (String) parentEntity.getEntityFieldValue(
                         parentEntity.getColumnInfo(childInfo.getChildIdField()));
                 if (childId != null) {
                     params.put(CommonSqlProvider.PARAM_WHERE_PART, "id = #{childId}");
                     params.put("childId", childId);
                     loadChild = true;
                 }
             } else {
                 // The child entity holds a reference back to the parent entity. Construct
                 // the query to return the child              
                 String parentIdColumn = RepositoryUtility.getColumnInfo(childEntityClass,
                         childInfo.getParentIdField()).getColumnName();
                 params.put(CommonSqlProvider.PARAM_WHERE_PART,
                         parentIdColumn + " = #{parentId}");
                 params.put("parentId", parentEntity.getEntityId());
                 loadChild = true;
             }
         }
         if (loadChild) {
             child = getEntity(childEntityClass, params, mapper);
         }
         return child;
     }
 
     /**
      * Processes a row of the the generic result set returned from Mybatis after
      * executing an SQL query. Each column of the result is mapped to the entity
      * field based on the name of the column specified in the
      *
      * @Column annotation.
      *
      * @param <T> The generic type of the entity to populate. Must be a
      * descendent of {@linkplain AbstractReadOnlyEntity}
      * @param entity The entity to populate with details from the result row
      * @param row The Mybatis Map object representing one row of a result set
      * @return The entity with its field values populated from the row map
      */
     private <T extends AbstractReadOnlyEntity> T mapToEntity(T entity, Map<String, Object> row) {
         if (row != null && !row.isEmpty()) {
             for (ColumnInfo columnInfo : entity.getColumns()) {
                 if (row.containsKey(columnInfo.getColumnName().toLowerCase())) {
                     entity.setEntityFieldValue(columnInfo,
                             row.get(columnInfo.getColumnName().toLowerCase()));
                 }
             }
             markAsLoaded(entity);
 
         }
         return entity;
     }
 
     /**
      * Processes a row of the the generic result set returned from Mybatis after
      * executing an SQL query. Each column of the result is mapped to the entity
      * field based on the name of the column specified in the
      *
      * @Column annotation. <p> Overloaded version of
      * {@linkplain #mapToEntity(.AbstractReadOnlyEntity, Map)} that creates a
      * new instance of the entity to populate based on the entity class. </p>
      *
      * @param <T> The generic type of the entity to populate. Must be a
      * descendent of {@linkplain AbstractReadOnlyEntity}
      * @param entityClass The class of the entity to populate with details from
      * the result row
      * @param row The Mybatis Map object representing one row of a result set
      * @return The entity with its field values populated from the row map
      */
     private <T extends AbstractReadOnlyEntity> T mapToEntity(Class<T> entityClass, Map<String, Object> row) {
         T entity = null;
         try {
             entity = mapToEntity(entityClass.newInstance(), row);
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     // Capture the specific details so they are added to the log
                     new Object[]{"Failed to map entity to result " + entityClass.getSimpleName(), ex});
         }
         return entity.isLoaded() ? entity : null;
     }
 
     /**
      * Overloaded version of
      * {@linkplain #mapToEntity(.AbstractReadOnlyEntity, Map)} that maps a
      * complete generic result set to an entity list.
      *
      * @param <T> The generic type of the entity to populate. Must be a
      * descendent of {@linkplain AbstractReadOnlyEntity}
      * @param entityClass The class of the entity to populate with details from
      * the result row
      * @param resultList The generic result set returned from the MyBatis query
      * @return A list of populated entities.
      */
     private <T extends AbstractReadOnlyEntity> List<T> mapToEntityList(
             Class<T> entityClass, ArrayList<HashMap> resultList) {
 
         List<T> entityList = new ArrayList<T>();
         if (resultList != null && !resultList.isEmpty()) {
             for (Map<String, Object> row : resultList) {
                 T entity = mapToEntity(entityClass, row);
                 if (entity != null) {
                     entityList.add(entity);
                 }
             }
         }
         return entityList;
     }
 
     /**
      * Performs the save processing for an entity. Recursively saves all child
      * entities. Note that the save only supports {@linkplain AbstractEntity}.
      *
      * @param <T> The generic type of the entity to save. Must be a descendent
      * of {@linkplain AbstractEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param entity The entity to save.
      * @param mapper The Mybatis mapper class used for this saving process.
      * @return The saved entity. This entity may have changed from the one
      * passed in due to default database column values and/or foreign keys.
      */
     protected <T extends AbstractEntity, U extends CommonMapper> T saveEntity(T entity, U mapper) {
         if (entity == null) {
             return null;
         }
 
         if (entity.isLoaded() && entity.hasIdChanged()) {
             // The Id of the entity has changed since it was loaded. This probably means that 
             // the details of a different entity have been copied over the original during translation. 
             // Refresh the entity to ensure the correct details are loaded from the DB.
             // NOTE this means any edits to the entity will be lost, however it is more important
             // to ensure the entity is not saved with details from the original. 
             entity = refreshEntity(entity, mapper);
         }
 
         boolean loaded = entity.isLoaded();
         entity.setSaving(true);
         entity.preSave();
         saveChildren(entity, mapper, true);
 
         if (entity.isModified()) {
             // The entity has at least one modified value, so mark it for save. 
             entity.markForSave();
         } else if (!entity.toRemove()) {
             // No changes have been made to the entity and it is not marked for delete, so reset
             // the entity action to prevent any unnecessary updates. 
             entity.resetEntityAction();
         }
 
         if (entity.toInsert()) {
             int rowsInserted = mapper.insert(entity);
             loaded = rowsInserted > 0;
         }
         if (entity.toUpdate() || entity.isUpdateBeforeDelete()) {
             int rowsUpdated = mapper.update(entity);
             loaded = rowsUpdated > 0;
             entity.setUpdateBeforeDelete(false);
         }
         if (entity.toDelete()) {
             mapper.delete(entity);
         }
 
         if (entity.isForceRefresh()) {
             // Entity may have had some DB default values assigned so refresh the entity from
             // the database.  refreshEntity resets the entity action so need to do some extra 
             // steps to ensure the entity action is persisted after the refresh for subsequent
             // save processing. 
             EntityAction action = entity.getEntityAction();
             entity = refreshEntity(entity, mapper);
             entity.setEntityAction(action);
         }
         entity.setRemoved(entity.toRemove());
         saveChildren(entity, mapper, false);
         entity.postSave();
         // Set the loaded flag and snapshot the field values after the postSave so that the
         // snapshot includes any fields updated by the postSave (e.g. RowVersion). 
         entity.setLoaded(loaded);
         entity.setForceRefresh(false);
         entity.resetEntityAction();
         entity.setSaving(false);
         return entity.isRemoved() ? null : entity;
     }
 
     /**
      * Processes all child entities of the parent saving them as appropriate.
      * Child entities are identified using the {@linkplain ChildEntity} and
      * {@linkplain ChildEntityList} annotations.
      *
      * @param <T> The generic type of the parent entity. Must extend
      * {@linkplain AbstractEntity}.
      * @param <U> The generic type of the mybatis mapper class. Must extend
      * {@linkplain CommonMapper}.
      * @param entity The parent entity.
      * @param mapper The mybatis mapper class used for the saving process.
      * @param beforeSave Flag to indicate if the save of the parent entity has
      * occurred (true) or not (false).
      */
     private <T extends AbstractEntity, U extends CommonMapper> void saveChildren(T entity,
             U mapper, boolean beforeSave) {
 
         for (ChildEntityInfo childInfo : entity.getChildEntityInfo()) {
             if (AbstractEntity.class.isAssignableFrom(childInfo.getEntityClass())) {
                 if (childInfo.isListField() && !childInfo.isManyToMany()) {
                     // One to many child list
                     saveOneToManyChildList(entity, childInfo, beforeSave, mapper);
                 } else if (childInfo.isListField() && childInfo.isManyToMany()) {
                     // Many to many child list
                     saveManyToManyChildList(entity, childInfo, beforeSave, mapper);
                 } else {
                     // One to one child
                     saveChild(entity, childInfo, beforeSave, mapper);
                 }
             } else {
                 // This child entity or entity list may be a read only entity or it may not
                 // be a descendent of SOLA repository abstract entities. Redirect the save so that
                 // alternative save logic can be implemented in a descendent repository.
                 saveOtherEntity(entity, childInfo, beforeSave, mapper);
             }
         }
     }
 
     /**
      * Save child entities that have a one to one association with the parent.
      *
      * @param <T> The generic type of the parent entity. Must extend
      * {@linkplain AbstractEntity}.
      * @param <U> The generic type of the mybatis mapper class. Must extend
      * {@linkplain CommonMapper}.
      * @param entity The parent entity.
      * @param childInfo Child entity information that describes the association
      * between the child and the parent.
      * @param beforeSave Flag to indicate if the save of the parent entity has
      * occurred (true) or not
      * @param mapper The mybatis mapper.
      */
     private <T extends AbstractEntity, U extends CommonMapper> void saveChild(T entity,
             ChildEntityInfo childInfo, boolean beforeSave, U mapper) {
 
         AbstractEntity child = (AbstractEntity) entity.getEntityFieldValue(childInfo);
 
         // Note that the correct way to disassociate a child from the parent is to use
         // EntityAction.DISASSOCIATE rather than attempting to set the child to null. 
         if (child != null && !childInfo.isReadOnly()) {
 
             // Determine if the child should be saved. There are four possiblities.
             // 1) If the child references the parent (isInsertBeforeParent == false) it should be
             //    saved after the parent unless
             // 2) The child references the parent (isInsertBeforeParent == false) and the child
             //    is being removed. In this case the child should be saved before the parent.
             // 3) If the parent references the child (isInsertBeforeParent == true) it should be
             //    saved before the parent unless
             // 4) The parent references the child (isInsertBeforeParet == true) and the child is
             //    being removed. In this case the child should be saved after the parent.  
             if (((beforeSave == childInfo.isInsertBeforeParent()) && !child.toRemove())
                     || ((beforeSave != childInfo.isInsertBeforeParent()) && child.toRemove())) {
 
                 if (!childInfo.isInsertBeforeParent()) {
                     // Need to set the parent Id on the child before inserting/updating the child. 
                     Object parentIdValue = entity.getEntityId();
                     child.setEntityFieldValue(child.getColumnInfo(
                             childInfo.getParentIdField()), parentIdValue);
                 }
                 if (childInfo.isExternalEntity()) {
                     if (!childInfo.getSaveMethod().isEmpty()) {
                         child = saveExternalEntity(child, childInfo);
                     }
                 } else {
                     child = saveEntity(child, mapper);
                 }
 
                 entity.setEntityFieldValue(childInfo, child);
             }
 
             if (beforeSave && childInfo.isInsertBeforeParent()) {
                 // The parent references the child. Perform some additional steps prior to saving
                 // the parent to ensure the reference is correctly set up. 
 
                 boolean versionUpdated = entity.isModified() && entity.toRemove();
                 boolean entityUpdated = false;
 
                 String childId = (String) entity.getEntityFieldValue(
                         entity.getColumnInfo(childInfo.getChildIdField()));
 
                 if (child.toRemove() && childId != null) {
                     // Clear the child id on the parent entity as the child is going to be removed
                     // following the save of the parent. 
                     entity.setEntityFieldValue(entity.getColumnInfo(
                             childInfo.getChildIdField()), null);
                     entityUpdated = true;
                 } else if (!child.toRemove() && !child.getEntityId().equals(childId)) {
                     // Make sure child id is set on the parent
                     entity.setEntityFieldValue(entity.getColumnInfo(
                             childInfo.getChildIdField()), child.getEntityId());
                     entityUpdated = true;
                 }
                 if (!versionUpdated && entityUpdated && entity instanceof AbstractVersionedEntity) {
                     // PreSave has already fired on the parent, so update the changeUser. 
                     ((AbstractVersionedEntity) entity).setChangeUser(LocalInfo.getUserName());
                 }
             }
         }
     }
 
     /**
      * Saves the child entities that are in a One to Many association with the
      * parent entity.
      *
      * @param <T> The generic type of the parent entity. Must extend
      * {@linkplain AbstractEntity}.
      * @param <U> The generic type of the Mybatis mapper class. Must extend
      * {@linkplain CommonMapper}.
      * @param entity The parent entity.
      * @param childInfo Child entity information that describes the association
      * between the child and the parent.
      * @param beforeSave Flag to indicate if the save of the parent entity has
      * occurred (true) or not
      * @param mapper The mybatis mapper.
      */
     private <T extends AbstractEntity, U extends CommonMapper> void saveOneToManyChildList(T entity,
             ChildEntityInfo childInfo, boolean beforeSave, U mapper) {
 
         List<AbstractEntity> childList = (List<AbstractEntity>) entity.getEntityFieldValue(childInfo);
 
         if (childList != null) {
 
             ListIterator<AbstractEntity> it = childList.listIterator();
             while (it.hasNext()) {
                 AbstractEntity child = it.next();
 
                 if (entity.toDelete() && childInfo.isCascadeDelete() && !childInfo.isReadOnly()) {
                     // Mark the child for delete if the parent is marked for delete and cascade
                     // delete applies. 
                     child.markForDelete();
                 }
 
                 // List children should always be saved after the parent (i.e beforeSave = false)
                 // unless it is necessary to remove the child. Child removal must occur before
                 // saving the parent to allow deletion of the parent if necessary. 
                 // Issue #248 Allow read only one to many entities to be disassociated
                 // from their parent entity. 
                 if (beforeSave == child.toRemove()
                         && (child.toDisassociate() || !childInfo.isReadOnly())) {
                     it.remove();
                     // If the child is being disassociated, set the parentIdValue to null.
                     // Note this may cause a database exception if the parent id is not null. 
                     Object parentIdValue = child.toDisassociate() ? null : entity.getEntityId();
                     child.setEntityFieldValue(child.getColumnInfo(
                             childInfo.getParentIdField()), parentIdValue);
 
                     if (childInfo.isExternalEntity()) {
                         if (!childInfo.getSaveMethod().isEmpty()) {
                             child = saveExternalEntity(child, childInfo);
                         }
                     } else {
                         child = saveEntity(child, mapper);
                     }
 
                     if (child != null) {
                         it.add(child);
                     }
                 }
             }
         }
     }
 
     /**
      * Uses the information provided in the {@linkplain ChildEntityList}
      * annotation to create a many to many association entity to link the parent
      * and child.
      *
      * @param <T> The generic type of the parent entity. Must extend
      * {@linkplain AbstractEntity}.
      * @param childInfo The child entity information describing the many to many
      * association.
      * @param entity The parent entity.
      * @param child The child entity.
      * @return The many to many entity.
      */
     private <T extends AbstractEntity> AbstractEntity createManyToManyEntity(ChildEntityInfo childInfo,
             T entity, AbstractEntity child) {
         AbstractEntity manyToMany = null;
         try {
             manyToMany = childInfo.getManyToManyClass().newInstance();
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     // Capture the specific details so they are added to the log
                     new Object[]{"Failed to create Many to Many entity class "
                 + childInfo.getManyToManyClass().getSimpleName(), ex});
         }
         manyToMany.setEntityFieldValue(manyToMany.getColumnInfo(
                 childInfo.getParentIdField()), entity.getEntityId());
         manyToMany.setEntityFieldValue(manyToMany.getColumnInfo(
                 childInfo.getChildIdField()), child.getEntityId());
 
         // Allow for additional configuration of the manyToMany entity
         manyToMany = entity.initializeManyToMany(manyToMany, child);
         return manyToMany;
     }
 
     /**
      * Performs save of child entities that are associated to the parent entity
      * via a many to many association table. This method uses the details from
      * the {@linkplain ChildEntityList} annotation manage (i.e. create and
      * delete) the many to many entity.
      *
      * @param <T> The generic type of the parent entity. Must extends
      * {@linkplain AbstractEntity}.
      * @param <U> The generic type of the mybatis mapper class. Must extend
      * {@linkplain CommonMapper}.
      * @param entity The parent entity.
      * @param childInfo Describes the child entity list to be processed
      * including details of the many to many entity to use to associate the
      * child entities with the parent.
      * @param beforeSave Flag indicating if the parent entity is about to be
      * saved (true) or has been saved (false)
      * @param mapper The mybtis mapper class to use for the save.
      */
     private <T extends AbstractEntity, U extends CommonMapper> void saveManyToManyChildList(T entity,
             ChildEntityInfo childInfo, boolean beforeSave, U mapper) {
 
         List<AbstractEntity> childList = (List<AbstractEntity>) entity.getEntityFieldValue(childInfo);
 
         if (childList != null && !childList.isEmpty()) {
 
             List<String> childIdList = null;
             if (!beforeSave) {
                 // Get the list of child ids from the many to many table in the database to make it
                 // easier to determine whether a new association entity needs to be ceated or not. 
                 childIdList = getChildIdList(childInfo, entity.getEntityId());
             }
 
             ListIterator<AbstractEntity> it = childList.listIterator();
             while (it.hasNext()) {
                 AbstractEntity child = it.next();
 
                 if (beforeSave && (entity.toDelete() || child.toRemove())) {
                     // The entity is being deleted and/or the child is being removed. Remove the 
                     // association (many to many) entity. 
                     AbstractEntity manyToMany = createManyToManyEntity(childInfo, entity, child);
                     manyToMany = refreshEntity(manyToMany, mapper);
                     if (manyToMany.isLoaded()) {
                         // Check if the refresh actually loaded the many to many entity from the DB. 
                         // If it does not exist, then do not try to delete it. This situation can
                         // occur when the many to many is in a 3 way relationship and the many
                         // to many is deleted by one of the other branches in the relationship
                         // e.g. rrr and rrr_share both reference party_for_rrr. 
                         manyToMany.markForDelete();
                         saveEntity(manyToMany, mapper);
                     }
                     it.remove();
 
                     if (!childInfo.isReadOnly()) {
                         if (entity.toDelete() && childInfo.isCascadeDelete()) {
                             // Cascade delete the child entity as well
                             child.markForDelete();
                         }
                         // Update / delete the child entity. 
                         if (childInfo.isExternalEntity()) {
                             if (!childInfo.getSaveMethod().isEmpty()) {
                                 child = saveExternalEntity(child, childInfo);
                             }
                         } else {
                             child = saveEntity(child, mapper);
                         }
                     }
 
                 } else if (!beforeSave) {
                     AbstractEntity manyToMany = null;
                     boolean saveChild = true;
                     if (!childIdList.contains(child.getEntityId())) {
                         // Need to add the association to the child into the DB
                         manyToMany = createManyToManyEntity(childInfo, entity, child);
                         manyToMany.markForSave();
                         // A many to many association is being created to the child entity. Only
                         // save the child if it is new. If it is an existing entity, trying to
                         // save it may cause the loss of some original information as Dozer would 
                         // not have been able to translate into the entity retrieved from the 
                         // database.
                         saveChild = child.isNew();
                     } else {
                         // Issue #248 Allow update of many to many entities. 
                         // The many to many association already exists for this child, but it may
                         // have some additional attributes that need to be saved. Check the number of
                         // columns on the many to many. If it is an Versioned Entity and has more than
                         // 5 columns, then it should be updated. If its not a Versioned Entity, but has
                         // more than 2 columns, then it should be updated as well. 
                         // (e.g. spatial_unit_in_parcel)
                         AbstractEntity manyToManyTmp = createManyToManyEntity(childInfo, entity, child);
                         if (manyToManyTmp.getColumns().size() > 5
                                 || (!AbstractVersionedEntity.class.isAssignableFrom(manyToManyTmp.getClass())
                                 && manyToManyTmp.getColumns().size() > 2)) {
                             {
                                 manyToMany = refreshEntity(manyToManyTmp, mapper);
                                 // The refresh would ensure the rowVersion is correct, as well as reset
                                 // the extra many to many fields. Re-intialize the many to many to
                                 // ensure the necessary data is configured.
                                 manyToMany = entity.initializeManyToMany(manyToMany, child);
                             }
                         }
                     }
 
                     // Determine if the child entity should be saved. Do not save if the child
                     // is ReadOnly from the parent, there is not external entity save method 
                     // identified or the saveChild flag is set to false. 
                     if (!childInfo.isReadOnly() && saveChild) {
                         it.remove();
                         if (childInfo.isExternalEntity()) {
                             if (!childInfo.getSaveMethod().isEmpty()) {
                                 child = saveExternalEntity(child, childInfo);
                             }
                         } else {
                             child = saveEntity(child, mapper);
                         }
                         it.add(child);
                     }
 
                     if (manyToMany != null) {
                         // Save the association after the child as the child needs to be 
                         // inserted first. 
                         saveEntity(manyToMany, mapper);
                     }
                 }
             }
 
         }
     }
 
     /**
      * Delegates saving the child entity to an different EJB based using the
      * details provided in the {@linkplain ExternalEJB} annotation.
      *
      * @param <T> The generic type of the child entity. Must be a descendent of
      * {@linkplain AbstractEntity}
      * @param childEntity The child entity to save using the external EJB
      * @param childInfo Details of the child entity (or entity list) that can be
      * used to obtain the external EJB name and save method details
      * @return The saved child entity.
      */
     private <T extends AbstractReadOnlyEntity> T saveExternalEntity(
             T childEntity, ChildEntityInfo childInfo) {
         AbstractEJBLocal ejb = RepositoryUtility.getEJB(childInfo.getEJBLocalClass());
         try {
             Method saveMethod = null;
             if (childInfo.getSaveMethod().equals("saveEntity")) {
                 saveMethod = ejb.getClass().getMethod(childInfo.getSaveMethod(), AbstractEntity.class);
             } else {
                 saveMethod = ejb.getClass().getMethod(childInfo.getSaveMethod(),
                         childInfo.getEntityClass());
             }
             childEntity = (T) saveMethod.invoke(ejb, childEntity);
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     // Capture the specific details so they are added to the log. Note that
                     // any exception raised when invoking the ejb method will be wrapped in an
                     // InvocationTargetException. The true cause can be masked by this exception. 
                     new Object[]{"Unable to invoke save  method " + childInfo.getSaveMethod()
                 + " on " + childInfo.getEJBLocalClass().getSimpleName(),
                 "Field=" + childInfo.getFieldName(), FaultUtility.getStackTraceAsString(ex)});
         }
         return childEntity;
     }
 
     /**
      * This is a placeholder method that can be optionally
      * overridden/implemented in descendent repositories. This method can be
      * used to save entities that do not inherit from the SOLA repository
      * abstract entity classes.
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain AbstractMapper}
      * @param entity The parent entity that references the child entity or child
      * entity list to save
      * @param childInfo Details of the child entity (or entity list) that can be
      * used to identify the child entity to be processed.
      * @param beforeSave Flag to indicate if the child is being processed before
      * (true) or after (false) the parent entity is saved.
      * @param mapper The Mybatis mapper class used for this save process.
      */
     protected <T extends AbstractEntity, U extends CommonMapper> void saveOtherEntity(T entity,
             ChildEntityInfo childInfo, boolean beforeSave, U mapper) {
     }
 
     /**
      * Reloads an entity from the database, overwriting any data values the
      * entity previously had.
      *
      * @param <T> The generic type of the entity to refresh. Must be a
      * descendent of {@linkplain AbstractEntity}
      * @param entity The entity to refresh.
      * @param mapper The Mybatis mapper to use for the refresh
      * @return The refreshed entity.
      */
     protected <T extends AbstractReadOnlyEntity> T refreshEntity(T entity, CommonMapper mapper) {
 
         if (entity != null) {
             // Set loaded to false before refresh so that any locked fields are updated with 
             // information from the database. 
             entity.setLoaded(false);
 
             String whereClause = "";
             Map params = new HashMap<String, Object>();
             for (ColumnInfo idColumn : entity.getIdColumns()) {
                 // Build a WHERE clause using the id fields of the entity
                 whereClause = whereClause + idColumn.getColumnName()
                         + " = #{" + idColumn.getFieldName() + "} AND ";
                 params.put(idColumn.getFieldName(), entity.getEntityFieldValue(idColumn));
             }
             whereClause = whereClause.substring(0, whereClause.length() - 5);
             params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
             params.put(CommonSqlProvider.PARAM_ENTITY_CLASS, entity.getClass());
 
             SqlSession session = getSqlSession();
             try {
                 Map result = getMapper(session).getEntity(params);
                 mapToEntity(entity, result);
             } finally {
                 session.close();
             }
             return entity;
         }
         return entity;
     }
 
     /**
      * Saves the specified entity and any child entities.
      *
      * @param <T> Generic type of the entity. Must extend
      * {@linkplain AbstractEntity}.
      * @param entity The entity to save.
      * @return The saved entity.
      */
     @Override
     public <T extends AbstractEntity> T saveEntity(T entity) {
         if (entity != null) {
             SqlSession session = getSqlSession();
             try {
                 entity = saveEntity(entity, getMapper(session));
             } finally {
                 session.close();
             }
         }
         return entity;
     }
 
     /**
      * Executes an SQL query against the database that returns a single
      * primative type value E.g. String, boolean, integer, etc.
      *
      * @param <T> The generic Boxed type of the scalar value.
      * @param scalarClass The boxed class of the scalar. e.g. Boolean.class,
      * Integer.class, etc
      * @param params The SQL parameters to use for executing the query.
      * @return The scalar value returned from the query or null if no value is
      * returned.
      */
     @Override
     public <T> T getScalar(Class<T> scalarClass, Map params) {
 
         T result = null;
         SqlSession session = getSqlSession();
         try {
             result = getScalar(scalarClass, params, getMapper(session));
         } finally {
             session.close();
         }
         return result;
     }
 
     /**
      * Overloaded version of
      * {@linkplain #getScalar(java.lang.Class, java.util.Map)} that also
      * specifies the mapper to use
      *
      * @param <T> The generic Boxed type of the scalar value.
      * @param <U> The generic type of the mapper. Must extend
      * {@linkplain CommonMapper}.
      * @param scalarClass The boxed class of the scalar. e.g. Boolean.class,
      * Integer.class, etc
      * @param params The SQL parameters to use for executing the query.
      * @param mapper The Mybatis Mapper to use when executing the SQL query.
      * @return The scalar value returned from the query or null if no value is
      * returned.
      */
     private <T, U extends CommonMapper> T getScalar(Class<T> scalarClass, Map params,
             U mapper) {
         return (T) mapper.getScalar(params);
     }
 
     @Override
     public List<String> getChildIdList(ChildEntityInfo childInfo, String parentId) {
 
         List<String> result = null;
         SqlSession session = getSqlSession();
         try {
             result = getChildIdList(childInfo, parentId, getMapper(session));
         } finally {
             session.close();
         }
         return result;
     }
 
     private <U extends CommonMapper> List<String> getChildIdList(ChildEntityInfo childInfo,
             String parentId, U mapper) {
 
         Map<String, Object> params = new HashMap<String, Object>();
 
         String parentIdField = childInfo.getParentIdField();
         Class<? extends AbstractEntity> entityClass =
                 (Class<? extends AbstractEntity>) childInfo.getEntityClass();
 
         if (childInfo.isManyToMany()) {
             // Get the details of the Many to Many class and the select the child id column
             entityClass = (Class<? extends AbstractEntity>) childInfo.getManyToManyClass();
 
             params.put(CommonSqlProvider.PARAM_SELECT_PART,
                     RepositoryUtility.getColumnInfo(entityClass,
                     childInfo.getChildIdField()).getColumnName());
 
         } else {
             // One to many relationship so just select the id column
             params.put(CommonSqlProvider.PARAM_SELECT_PART, "id");
         }
 
         // Identify the table to query
         params.put(CommonSqlProvider.PARAM_FROM_PART,
                 RepositoryUtility.getTableName(entityClass));
 
         // Construct the WHERE clause
         String parentIdColumn = RepositoryUtility.getColumnInfo(entityClass,
                 parentIdField).getColumnName();
         String whereClause = parentIdColumn + " = #{parentId}";
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
 
         // Set the parent id parameter
         params.put("parentId", parentId);
 
         return getScalarList(String.class, params, mapper);
     }
 
     @Override
     public <T> List<T> getScalarList(Class<T> scalarClass, Map params) {
 
         List<T> result = null;
         SqlSession session = getSqlSession();
         try {
             result = getScalarList(scalarClass, params, getMapper(session));
         } finally {
             session.close();
         }
         return result;
     }
 
     private <T, U extends CommonMapper> List<T> getScalarList(Class<T> scalarClass, Map params,
             U mapper) {
         return (List<T>) mapper.getScalarList(params);
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> T refreshEntity(T entity) {
 
         SqlSession session = getSqlSession();
         try {
             entity = refreshEntity(entity, getMapper(session));
         } finally {
             session.close();
         }
         return entity;
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> T getEntity(Class<T> entityClass, String id) {
 
         ArrayList<ColumnInfo> ids = (ArrayList<ColumnInfo>) RepositoryUtility.getIdColumns(entityClass, RepositoryUtility.getColumns(entityClass));
 
         HashMap<String, Object> params = new HashMap<String, Object>();
         String whereClause = ids.get(0).getColumnName() + " = #{idValue}";
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
         params.put("idValue", id);
         return getEntity(entityClass, params);
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> T getEntity(Class<T> entityClass, String id, String lang) {
 
         ArrayList<ColumnInfo> ids = (ArrayList<ColumnInfo>) RepositoryUtility.getIdColumns(entityClass, RepositoryUtility.getColumns(entityClass));
 
         HashMap<String, Object> params = new HashMap<String, Object>();
         String whereClause = ids.get(0).getColumnName() + " = #{idValue}";
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
         params.put("idValue", id);
         params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, lang);
         return getEntity(entityClass, params);
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> T getEntity(Class<T> entityClass,
             String whereClause, Map params) {
 
         params = params == null ? new HashMap<String, Object>() : params;
 
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
         return getEntity(entityClass, params);
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> T getEntity(Class<T> entityClass,
             Map params) {
 
         params = params == null ? new HashMap<String, Object>() : params;
         T entity = null;
 
         SqlSession session = getSqlSession();
         try {
             entity = getEntity(entityClass, params, getMapper(session));
         } finally {
             session.close();
         }
         return entity;
     }
 
     private <T extends AbstractReadOnlyEntity, U extends CommonMapper> T getEntity(Class<T> entityClass,
             Map params, U mapper) {

         HashMap<String, Object> result = null;
         T entity = null;
         params.put(CommonSqlProvider.PARAM_ENTITY_CLASS, entityClass);
         // Make sure the Language Code is passed to all children if it has been set
         if (LocalInfo.get(CommonSqlProvider.PARAM_LANGUAGE_CODE) != null
                 && !params.containsKey(CommonSqlProvider.PARAM_LANGUAGE_CODE)) {
             params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                     LocalInfo.get(CommonSqlProvider.PARAM_LANGUAGE_CODE));

         } else if (params.containsKey(CommonSqlProvider.PARAM_LANGUAGE_CODE)) {
             LocalInfo.set(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                    params.get(CommonSqlProvider.PARAM_LANGUAGE_CODE), true);
         }
         result = mapper.getEntity(params);
         entity = mapToEntity(entityClass, result);
         if (entity != null) {
             loadChildren(entity, mapper);
         }

         return entity;
     }
 
     @Override
     public <T extends AbstractCodeEntity> List<T> getCodeList(Class<T> codeListClass,
             String languageCode) {
 
         HashMap<String, Object> params = new HashMap<String, Object>();
         if (languageCode != null) {
             params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
         }
 
         return getEntityList(codeListClass, params);
     }
 
     @Override
     public <T extends AbstractCodeEntity> T getCode(Class<T> codeListClass,
             String entityCode, String languageCode) {
 
         HashMap<String, Object> params = new HashMap<String, Object>();
         if (languageCode != null) {
             params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE, languageCode);
         }
         params.put(CommonSqlProvider.PARAM_WHERE_PART, "code = #{entityCode}");
         params.put("entityCode", entityCode);
 
         return getEntity(codeListClass, params);
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> getEntityList(Class<T> entityClass) {
         return getEntityList(entityClass, new HashMap<String, Object>());
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> getEntityList(Class<T> entityClass,
             String whereClause, Map params) {
 
         params = params == null ? new HashMap<String, Object>() : params;
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
         return getEntityList(entityClass, params);
 
     }
 
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> getEntityList(Class<T> entityClass,
             Map params) {
 
         params = params == null ? new HashMap<String, Object>() : params;
         SqlSession session = getSqlSession();
         List<T> entityList = null;
         try {
             entityList = getEntityList(entityClass, params, getMapper(session));
         } finally {
             session.close();
         }
         return entityList;
     }
 
     private <T extends AbstractReadOnlyEntity, U extends CommonMapper> List<T> getEntityList(Class<T> entityClass,
             Map params, U mapper) {
 
         List<T> entityList = null;
         ArrayList<HashMap> resultList = null;
         params.put(CommonSqlProvider.PARAM_ENTITY_CLASS, entityClass);
         // Make sure the Language Code is passed to all children if it has been set
         if (LocalInfo.get(CommonSqlProvider.PARAM_LANGUAGE_CODE) != null
                 && !params.containsKey(CommonSqlProvider.PARAM_LANGUAGE_CODE)) {
             params.put(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                     LocalInfo.get(CommonSqlProvider.PARAM_LANGUAGE_CODE));

         } else if (params.containsKey(CommonSqlProvider.PARAM_LANGUAGE_CODE)) {
             LocalInfo.set(CommonSqlProvider.PARAM_LANGUAGE_CODE,
                    params.get(CommonSqlProvider.PARAM_LANGUAGE_CODE), true);
         }
         resultList = mapper.getEntityList(params);
         entityList = mapToEntityList(entityClass, resultList);
         if (entityList != null && !entityList.isEmpty()) {
             for (T entity : entityList) {
                 loadChildren(entity, mapper);
             }
         }

         return entityList;
     }
 
     /**
      * Retrieves a list of entities by generating a where clause based on the
      * list of entity ids.
      * <p> Uses and IN clause for the SQL query. </p> <p> Overloaded version of {@linkplain #getEntityListByIds(java.lang.Class, java.util.List,
      * java.util.Map) } that defaults the parameter map to null. </p>
      *
      * @param <T> The generic type of the entity being loaded. Must be a
      * descendent of {@linkplain AbstractReadOnlyEntity}
      * @param entityClass The class of the entity list to load.
      * @param ids The list identifiers to query the entity table with.
      * @return THe list of entities returned from the SQL query.
      */
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> getEntityListByIds(Class<T> entityClass,
             List<String> ids) {
         return getEntityListByIds(entityClass, ids, new HashMap<String, Object>());
     }
 
     /**
      * Retrieves a list of entities by generating a where clause based on the
      * list of entity ids.
      * <p> Uses and IN clause for the SQL query. If a PARAM_WHERE_PART is
      * provided in the params map, the IN clause is ANDed to the existing
      * PARAM_WHERE_PART.</p>
      *
      * @param <T> The generic type of the entity being loaded. Must be a
      * descendent of {@linkplain AbstractReadOnlyEntity}
      * @param entityClass The class of the entity list to load.
      * @param ids The list identifiers to query the entity table with.
      * @param params Any additional parameters that are required for the SQL
      * query.
      * @return THe list of entities returned from the SQL query.
      */
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> getEntityListByIds(Class<T> entityClass,
             List<String> ids, Map params) {
 
         if (ids == null || ids.isEmpty()) {
             return new ArrayList<T>();
         }
         if (params == null) {
             params = new HashMap<String, Object>();
         }
 
         String whereClause = (String) params.get(CommonSqlProvider.PARAM_WHERE_PART);
 
         if (whereClause == null || whereClause.isEmpty()) {
             whereClause = "id IN (";
         } else {
             whereClause = whereClause + " and id IN (";
         }
 
         // Build the IN clause with parameter values rather than hard coded ids to 
         // ensure the generated SQL can be treated as a prepared statement. 
         int i = 0;
         for (String id : ids) {
             whereClause = whereClause + "#{idVal" + i + "}, ";
             params.put("idVal" + i, id);
             i++;
         }
 
         whereClause = whereClause.substring(0, whereClause.length() - 2) + ")";
         params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
 
         return getEntityList(entityClass, params);
     }
 
     /**
      * Loads the child entity lists for both One to Many and Many to Many
      * associations. <p> To customize the default join criteria used to load the
      * child entity list, override
      * {@linkplain AbstractReadOnlyEntity#getChildJoinSqlParams} to return the
      * appropriately configured SQL Parameters. </p>
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <V> The generic type of the child entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}.
      * @param parentEntity The parent entity that references the child entity or
      * child entity list to load
      * @param childEntityClass The class of the child entity to load.
      * @param childInfo Provides the details from the
      * {@linkplain ChildEntityList} annotation that are used to assist loading
      * of the child entity list.
      * @return The list of child entities or null if there are none to load.
      */
     @Override
     public <T extends AbstractReadOnlyEntity, V extends AbstractReadOnlyEntity> List<V> getChildEntityList(
             T parentEntity, Class<V> childEntityClass, ChildEntityInfo childInfo) {
 
         SqlSession session = getSqlSession();
         List<V> entityList = null;
         try {
             entityList = getChildEntityList(parentEntity, childEntityClass, childInfo,
                     getMapper(session));
         } finally {
             session.close();
         }
         return entityList;
     }
 
     /**
      * Loads the child entity lists for both One to Many and Many to Many
      * associations. <p> To customize the default join criteria used to load the
      * child entity list, override
      * {@linkplain AbstractReadOnlyEntity#getChildJoinSqlParams} to return the
      * appropriately configured SQL Parameters. </p>
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param <V> The generic type of the child entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}.
      * @param parentEntity The parent entity that references the child entity or
      * child entity list to load
      * @param childEntityClass The class of the child entity to load.
      * @param childInfo Provides the details from the
      * {@linkplain ChildEntityList} annotation that are used to assist loading
      * of the child entity list.
      * @param mapper The Mybatis mapper class used for this loading process.
      * @return The list of child entities or null if there are none to load.
      */
     private <T extends AbstractReadOnlyEntity, U extends CommonMapper, V extends AbstractReadOnlyEntity> List<V> getChildEntityList(
             T parentEntity, Class<V> childEntityClass, ChildEntityInfo childInfo, U mapper) {
 
         // Determine if the parent has customized join criteria for the child list. 
         Map<String, Object> params = parentEntity.getChildJoinSqlParams(childInfo);
         if (params == null) {
             // Use the default child join critiera (i.e. where FK column = parentId)
             params = new HashMap<String, Object>();
             String parentIdField = childInfo.getParentIdField();
 
             if (childInfo.isManyToMany()) {
                 // Get the details of the Many to Many class
                 Class<? extends AbstractEntity> manyToManyClass =
                         (Class<? extends AbstractEntity>) childInfo.getManyToManyClass();
 
                 // Get the parent and child column names on the Many to Many class
                 String parentIdColumn = RepositoryUtility.getColumnInfo(manyToManyClass,
                         parentIdField).getColumnName();
                 String childIdColumn = RepositoryUtility.getColumnInfo(manyToManyClass,
                         childInfo.getChildIdField()).getColumnName();
 
                 // Create a WHERE clause that will use a nested select on the Many to Many entity
                 // to restrict the selection of records from the target child entity table. 
                // #307 Determine the correct name of the primary key column on the child entity. 
                String childPKColumnName = RepositoryUtility.getIdColumns(childEntityClass).get(0).getColumnName(); 
                String whereClause = childPKColumnName + " IN ( SELECT a." + childIdColumn
                         + " FROM " + RepositoryUtility.getTableName(manyToManyClass) + " a "
                         + " WHERE a." + parentIdColumn + " = #{parentId})";
                 params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
 
             } else {
                 // Construct the WHERE clause for a One to Many association
                 String parentIdColumn = RepositoryUtility.getColumnInfo(childEntityClass,
                         parentIdField).getColumnName();
                 String whereClause = parentIdColumn + " = #{parentId}";
                 params.put(CommonSqlProvider.PARAM_WHERE_PART, whereClause);
             }
 
             // Set the parent id parameter
             params.put("parentId", parentEntity.getEntityId());
         }
         List<V> result = getEntityList(childEntityClass, params, mapper);
         return result == null ? new ArrayList<V>() : result;
     }
 
     /**
      * Processes each child of the parent entity that has been marked with a
      * {@linkplain ChildEntity} or {@linkplain ChildEntityList} annotation and
      * uses the details from those annotations to load the child entities.
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param entity The parent entity that references the child entity or child
      * entity list to load
      * @param mapper The Mybatis mapper class used for this loading process.
      */
     public <T extends AbstractReadOnlyEntity, U extends CommonMapper> void loadChildren(T entity, U mapper) {
         for (ChildEntityInfo childInfo : entity.getChildEntityInfo()) {
             if (AbstractReadOnlyEntity.class.isAssignableFrom(childInfo.getEntityClass())) {
                 Class<? extends AbstractReadOnlyEntity> childEntityClass =
                         (Class<? extends AbstractReadOnlyEntity>) childInfo.getEntityClass();
                 // Check to determine if loading of this child class should be skipped or not
                 if (!isInhibitLoad(childEntityClass)) {
                     Object child = null;
                     if (childInfo.isExternalEntity()) {
                         // External Entity
                         child = getExternalEntity(entity, childInfo, mapper);
                     } else if (childInfo.isListField()) {
                         // Load the child list for the one to many or many to many list. 
                         child = getChildEntityList(entity, childEntityClass, childInfo, mapper);
                     } else {
                         // One to One relationship, so load the child
                         child = getChildEntity(entity, childEntityClass, childInfo, mapper);
                     }
                     entity.setEntityFieldValue(childInfo, child);
                 } else {
                     // The child entity does not inherit from the SOLA Abstract Entity classes. 
                     // Allow the loading of the external need to be managed by the parent repository. 
                     loadOtherEntity(entity, childInfo, mapper);
                 }
             }
         }
     }
 
     /**
      * Loads an entity or entity list from another EJB using the details
      * provided in the {@linkplain ExternalEJB} annotation.
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param entity The parent entity that references the child entity or child
      * entity list to load
      * @param childInfo Details of the child entity (or entity list) that can be
      * used to identify the child entity to be processed.
      * @param mapper The Mybatis mapper class used for this loading process.
      * @return The entity or entity list loaded from the external EJB, or null
      * if there are no child entities.
      */
     private <T extends AbstractReadOnlyEntity, U extends CommonMapper> Object getExternalEntity(
             T entity, ChildEntityInfo childInfo, U mapper) {
         Object child = null;
         Class<?> argType = null;
         Object argValue = null;
         AbstractEJBLocal ejb = RepositoryUtility.getEJB(childInfo.getEJBLocalClass());
 
         if (childInfo.isListField() && childInfo.isManyToMany()) {
             //Many to Many, so get the list of child ids from the many to many table in this
             //EJB using the parent id and pass the list of child ids to the external EJB.  
             argValue = getChildIdList(childInfo, entity.getEntityId(), mapper);
             argType = List.class;
         } else {
             // A One to One or One to Many list. If One to One, it may be necessray to pass th
             // child id on the parent. If One to Many, pass the parent id to return the list 
             // of children as the child must reference the parent. 
             if (childInfo.isInsertBeforeParent()) {
                 // Parent refrences the child entity, so get the child id value from the parent
                 argValue = (String) entity.getEntityFieldValue(
                         entity.getColumnInfo(childInfo.getChildIdField()));
             } else {
                 argValue = entity.getEntityId();
             }
             argType = String.class;
         }
         try {
             Method loadMethod = ejb.getClass().getMethod(childInfo.getLoadMethod(), argType);
             child = loadMethod.invoke(ejb, argValue);
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED,
                     // Capture the specific details so they are added to the log. Note that
                     // any exception raised when invoking the ejb method will be wrapped in an
                     // InvocationTargetException. The true cause can be masked by this exception.
                     new Object[]{"Unable to invoke method " + childInfo.getLoadMethod(),
                 FaultUtility.getStackTraceAsString(ex)});
         }
         return child;
     }
 
     /**
      * This is a placeholder method that can be optionally
      * overridden/implemented in descendent repositories. This method can be
      * used to load entities that do not inherit from the SOLA repository
      * abstract entity classes.
      *
      * @param <T> The generic type of the parent entity. Must be a descendent of
      * {@linkplain AbstractReadOnlyEntity}
      * @param <U> The generic type of the mapper. Must be a descendent of
      * {@linkplain CommonMapper}
      * @param entity The parent entity that references the child entity or child
      * entity list to load
      * @param childInfo Details of the child entity (or entity list) that can be
      * used to identify the child entity to be processed.
      * @param mapper The Mybatis mapper class used for this save process.
      */
     protected <T extends AbstractReadOnlyEntity, U extends CommonMapper> void loadOtherEntity(T entity,
             ChildEntityInfo childInfo, U mapper) {
     }
 
     /**
      * Executes function with given parameters.
      *
      * @param params Parameters list needed to form SQL statement.
      * {@link CommonSqlProvider#PARAM_QUERY} should be supplied as a select
      * statement to run function.
      */
     @Override
     public ArrayList<HashMap> executeFunction(Map params) {
         return executeSql(params);
     }
 
     /**
      * Executes function with given parameters.
      *
      * @param params Parameters list needed to form SQL statement.
      * {@link CommonSqlProvider#PARAM_QUERY} should be supplied as a select
      * statement to run function.
      * @param entityClass The class of the entity to cast results to.
      */
     @Override
     public <T extends AbstractReadOnlyEntity> List<T> executeFunction(Map params, Class<T> entityClass) {
         return mapToEntityList(entityClass, executeSql(params));
     }
 
     /**
      * Executes dynamic SQL queries using the specified parameters.
      *
      * @param params Parameters list needed to form SQL statement.
      * {@link CommonSqlProvider#PARAM_QUERY} must be supplied as a select
      * statement to run the SQL.
      */
     @Override
     public ArrayList<HashMap> executeSql(Map params) {
 
         if (params == null || !params.containsKey(CommonSqlProvider.PARAM_QUERY)) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED, new Object[]{
                 "No dynamic SQL to execute!", "params=" + params
             });
         }
 
         ArrayList<HashMap> result = null;
         SqlSession session = getSqlSession();
         try {
             result = getMapper(session).executeSql(params);
         } finally {
             session.close();
         }
         return result;
     }
 
     /**
      * Issue #248 Add Bulk Update capability to the repository. 
      * Executes a dynamic bulk update command using the specified parameters.
      *
      * @param params {@link CommonSqlProvider#PARAM_QUERY} must be supplied with
      * the template for the bulk update statement. Values for the update can be
      * provided as additional parameters.
      * @return the number of rows updated.
      */
     @Override
     public int bulkUpdate(Map params) {
         if (params == null || !params.containsKey(CommonSqlProvider.PARAM_QUERY)) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED, new Object[]{
                 "No dynamic SQL to execute!", "params=" + params
             });
         }
 
         int result = 0;
         SqlSession session = getSqlSession();
         try {
             result = getMapper(session).bulkUpdate(params);
         } finally {
             session.close();
         }
         return result;
     }
 }
