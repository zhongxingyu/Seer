 /**
  * Copyright 2012 Impetus Infotech.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.impetus.client.cassandra;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javassist.Modifier;
 
 import javax.persistence.PersistenceException;
 import javax.persistence.Transient;
 import javax.persistence.metamodel.Attribute;
 import javax.persistence.metamodel.EmbeddableType;
 import javax.persistence.metamodel.EntityType;
 import javax.persistence.metamodel.ManagedType;
 
 import org.apache.cassandra.thrift.Cassandra;
 import org.apache.cassandra.thrift.CfDef;
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnDef;
 import org.apache.cassandra.thrift.ColumnOrSuperColumn;
 import org.apache.cassandra.thrift.ColumnParent;
 import org.apache.cassandra.thrift.ColumnPath;
 import org.apache.cassandra.thrift.Compression;
 import org.apache.cassandra.thrift.ConsistencyLevel;
 import org.apache.cassandra.thrift.CounterColumn;
 import org.apache.cassandra.thrift.CounterSuperColumn;
 import org.apache.cassandra.thrift.CqlResult;
 import org.apache.cassandra.thrift.CqlRow;
 import org.apache.cassandra.thrift.IndexClause;
 import org.apache.cassandra.thrift.IndexExpression;
 import org.apache.cassandra.thrift.IndexType;
 import org.apache.cassandra.thrift.InvalidRequestException;
 import org.apache.cassandra.thrift.KeySlice;
 import org.apache.cassandra.thrift.KsDef;
 import org.apache.cassandra.thrift.Mutation;
 import org.apache.cassandra.thrift.NotFoundException;
 import org.apache.cassandra.thrift.SchemaDisagreementException;
 import org.apache.cassandra.thrift.SuperColumn;
 import org.apache.cassandra.thrift.TimedOutException;
 import org.apache.cassandra.thrift.UnavailableException;
 import org.apache.cassandra.utils.ByteBufferUtil;
 import org.apache.commons.lang.StringUtils;
 import org.apache.thrift.TException;
 import org.scale7.cassandra.pelops.Bytes;
 import org.scale7.cassandra.pelops.ColumnOrSuperColumnHelper;
 import org.scale7.cassandra.pelops.pool.IThriftPool.IPooledConnection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.impetus.client.cassandra.common.CassandraConstants;
 import com.impetus.client.cassandra.common.CassandraUtilities;
 import com.impetus.client.cassandra.config.CassandraPropertyReader;
 import com.impetus.client.cassandra.datahandler.CassandraDataHandler;
 import com.impetus.client.cassandra.pelops.PelopsUtils;
 import com.impetus.client.cassandra.schemamanager.CassandraValidationClassMapper;
 import com.impetus.client.cassandra.thrift.CQLTranslator;
 import com.impetus.client.cassandra.thrift.CQLTranslator.TranslationType;
 import com.impetus.client.cassandra.thrift.ThriftClientFactory.Connection;
 import com.impetus.client.cassandra.thrift.ThriftDataResultHelper;
 import com.impetus.client.cassandra.thrift.ThriftRow;
 import com.impetus.kundera.Constants;
 import com.impetus.kundera.KunderaException;
 import com.impetus.kundera.PersistenceProperties;
 import com.impetus.kundera.client.Client;
 import com.impetus.kundera.client.ClientBase;
 import com.impetus.kundera.client.ClientPropertiesSetter;
 import com.impetus.kundera.client.EnhanceEntity;
 import com.impetus.kundera.db.DataRow;
 import com.impetus.kundera.db.RelationHolder;
 import com.impetus.kundera.db.SearchResult;
 import com.impetus.kundera.graph.Node;
 import com.impetus.kundera.lifecycle.states.RemovedState;
 import com.impetus.kundera.metadata.KunderaMetadataManager;
 import com.impetus.kundera.metadata.model.EntityMetadata;
 import com.impetus.kundera.metadata.model.EntityMetadata.Type;
 import com.impetus.kundera.metadata.model.ApplicationMetadata;
 import com.impetus.kundera.metadata.model.KunderaMetadata;
 import com.impetus.kundera.metadata.model.MetamodelImpl;
 import com.impetus.kundera.metadata.model.PersistenceUnitMetadata;
 import com.impetus.kundera.metadata.model.TableGeneratorDiscriptor;
 import com.impetus.kundera.metadata.model.annotation.DefaultEntityAnnotationProcessor;
 import com.impetus.kundera.metadata.model.attributes.AbstractAttribute;
 import com.impetus.kundera.metadata.model.type.AbstractManagedType;
 import com.impetus.kundera.property.PropertyAccessException;
 import com.impetus.kundera.property.PropertyAccessorFactory;
 import com.impetus.kundera.property.PropertyAccessorHelper;
 
 /**
  * Base Class for all Cassandra Clients Contains methods that are applicable to
  * (but not specific to) different Cassandra clients.
  * 
  * @author amresh.singh
  */
 public abstract class CassandraClientBase extends ClientBase implements ClientPropertiesSetter
 {
 
     /** log for this class. */
     private static Logger log = LoggerFactory.getLogger(CassandraClientBase.class);
 
     /** The cql version. */
     private String cqlVersion = CassandraConstants.CQL_VERSION_2_0;
 
     /** The consistency level. */
     private ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
 
     private boolean ttlPerRequest = false;
 
     private boolean ttlPerSession = false;
 
     private Map<String, Object> ttlValues = new HashMap<String, Object>();
 
     /** The closed. */
     private boolean closed = false;
 
     /** list of nodes for batch processing. */
     private List<Node> nodes = new ArrayList<Node>();
 
     /** batch size. */
     private int batchSize;
 
     private Map<String, Object> externalProperties;
 
     protected CQLClient cqlClient;
 
     /**
      * constructor using fields.
      * 
      * @param persistenceUnit
      *            the persistence unit
      * @param externalProperties
      */
     protected CassandraClientBase(String persistenceUnit, Map<String, Object> externalProperties)
     {
         this.externalProperties = externalProperties;
         this.cqlClient = new CQLClient();
         setBatchSize(persistenceUnit, this.externalProperties);
         populateCqlVersion(externalProperties);
     }
 
     /**
      * Populates foreign key as column.
      * 
      * @param rlName
      *            relation name
      * @param rlValue
      *            relation value
      * @param timestamp
      *            the timestamp
      * @return the column
      * @throws PropertyAccessException
      *             the property access exception
      */
     protected Column populateFkey(String rlName, Object rlValue, long timestamp) throws PropertyAccessException
     {
         Column col = new Column();
         col.setName(PropertyAccessorFactory.STRING.toBytes(rlName));
         col.setValue(PropertyAccessorHelper.getBytes(rlValue));
         col.setTimestamp(timestamp);
         return col;
     }
 
     /**
      * On counter column.
      * 
      * @param m
      *            the m
      * @param isRelation
      *            the is relation
      * @param relations
      *            the relations
      * @param ks
      *            the ks
      * @return the list
      */
     protected List<Object> onCounterColumn(EntityMetadata m, boolean isRelation, List<String> relations,
             List<KeySlice> ks)
     {
         List<Object> entities;
 
         if (m.getType().isSuperColumnFamilyMetadata())
         {
             if (log.isInfoEnabled())
             {
                 log.info("On counter column for super column family of entity {}.", m.getEntityClazz());
             }
 
             Map<Bytes, List<CounterSuperColumn>> qCounterSuperColumnResults = ColumnOrSuperColumnHelper
                     .transformKeySlices(ks, ColumnOrSuperColumnHelper.COUNTER_SUPER_COLUMN);
             entities = new ArrayList<Object>(qCounterSuperColumnResults.size());
 
             for (Bytes key : qCounterSuperColumnResults.keySet())
             {
                 Object e = null;
                 Object id = PropertyAccessorHelper.getObject(m.getIdAttribute().getJavaType(), key.toByteArray());
                 // e = PelopsUtils.initialize(m, e, id);
 
                 List<CounterSuperColumn> counterSuperColumns = qCounterSuperColumnResults.get(key);
                 ThriftRow tr = new ThriftRow(id, m.getTableName(), new ArrayList<Column>(0),
                         new ArrayList<SuperColumn>(0), new ArrayList<CounterColumn>(0), counterSuperColumns);
                 e = getDataHandler().populateEntity(tr, m, CassandraUtilities.getEntity(e), relations, isRelation);
                 // e = isRelation && !relationValue.isEmpty() ? new
                 // EnhanceEntity(e, PropertyAccessorHelper.getId(e, m),
                 // relationValue) : e;
                 entities.add(e);
             }
 
         }
         else
         {
 
             if (log.isInfoEnabled())
             {
                 log.info("On counter column for column family of entity {}", m.getEntityClazz());
             }
 
             Map<Bytes, List<CounterColumn>> qCounterColumnResults = ColumnOrSuperColumnHelper.transformKeySlices(ks,
                     ColumnOrSuperColumnHelper.COUNTER_COLUMN);
             entities = new ArrayList<Object>(qCounterColumnResults.size());
 
             for (Bytes key : qCounterColumnResults.keySet())
             {
                 Object e = null;
                 Object id = PropertyAccessorHelper.getObject(m.getIdAttribute().getJavaType(), key.toByteArray());
                 // e = PelopsUtils.initialize(m, e, id);
 
                 List<CounterColumn> counterColumns = qCounterColumnResults.get(key);
                 ThriftRow tr = new ThriftRow(id, m.getTableName(), new ArrayList<Column>(0),
                         new ArrayList<SuperColumn>(0), counterColumns, new ArrayList<CounterSuperColumn>(0));
                 e = getDataHandler().populateEntity(tr, m, CassandraUtilities.getEntity(e), relations, isRelation);
 
                 if (e != null)
                 {
                     entities.add(e);
                 }
             }
         }
         return entities;
     }
 
     /**
      * Compute entity via columns.
      * 
      * @param m
      *            the m
      * @param isRelation
      *            the is relation
      * @param relations
      *            the relations
      * @param entities
      *            the entities
      * @param qResults
      *            the q results
      */
     protected void computeEntityViaColumns(EntityMetadata m, boolean isRelation, List<String> relations,
             List<Object> entities, Map<Bytes, List<Column>> qResults)
     {
 
         MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                 m.getPersistenceUnit());
 
         EntityType entityType = metaModel.entity(m.getEntityClazz());
 
         List<AbstractManagedType> subManagedType = ((AbstractManagedType) entityType).getSubManagedType();
 
         for (Bytes key : qResults.keySet())
         {
             if (!qResults.get(key).isEmpty())
             {
                 List<Column> columns = qResults.get(key);
                 Object id = PropertyAccessorHelper.getObject(m.getIdAttribute().getJavaType(), key.toByteArray());
                 ThriftRow tr = new ThriftRow(id, m.getTableName(), columns, new ArrayList<SuperColumn>(0),
                         new ArrayList<CounterColumn>(0), new ArrayList<CounterSuperColumn>(0));
                 Object o = null;
 
                 if (!subManagedType.isEmpty())
                 {
                     for (AbstractManagedType subEntity : subManagedType)
                     {
                         EntityMetadata subEntityMetadata = KunderaMetadataManager.getEntityMetadata(subEntity
                                 .getJavaType());
 
                         o = getDataHandler().populateEntity(tr, subEntityMetadata, CassandraUtilities.getEntity(o),
                                 subEntityMetadata.getRelationNames(), isRelation);
                         if (o != null)
                         {
                             break;
                         }
                     }
 
                 }
                 else
                 {
                     o = getDataHandler().populateEntity(tr, m, CassandraUtilities.getEntity(o), relations, isRelation);
                 }
 
                 if (log.isInfoEnabled())
                 {
                     log.info("Populating data for entity of clazz {} and row key {}.", m.getEntityClazz(), tr.getId());
                 }
 
                 if (o != null)
                 {
                     entities.add(o);
                 }
             }
         }
     }
 
     /**
      * Compute entity via super columns.
      * 
      * @param m
      *            the m
      * @param isRelation
      *            the is relation
      * @param relations
      *            the relations
      * @param entities
      *            the entities
      * @param qResults
      *            the q results
      */
     protected void computeEntityViaSuperColumns(EntityMetadata m, boolean isRelation, List<String> relations,
             List<Object> entities, Map<Bytes, List<SuperColumn>> qResults)
     {
         for (Bytes key : qResults.keySet())
         {
             Object e = null;
             Object id = PropertyAccessorHelper.getObject(m.getIdAttribute().getJavaType(), key.toByteArray());
             // e = PelopsUtils.initialize(m, e, id);
 
             List<SuperColumn> superColumns = qResults.get(key);
 
             ThriftRow tr = new ThriftRow(id, m.getTableName(), new ArrayList<Column>(0), superColumns,
                     new ArrayList<CounterColumn>(0), new ArrayList<CounterSuperColumn>(0));
 
             e = getDataHandler().populateEntity(tr, m, CassandraUtilities.getEntity(e), relations, isRelation);
             // e = isRelation && !relationValue.isEmpty() ? new EnhanceEntity(e,
             // PropertyAccessorHelper.getId(e, m),
             // relationValue) : e;
 
             if (log.isInfoEnabled())
             {
                 log.info("Populating data for super column family of clazz {} and row key {}.", m.getEntityClazz(),
                         tr.getId());
             }
 
             if (e != null)
             {
                 entities.add(e);
             }
 
         }
     }
 
     /**
      * Adds relation foreign key values as thrift column/ value to thrift row.
      * 
      * @param metadata
      *            the metadata
      * @param tf
      *            the tf
      * @param relations
      *            the relations
      */
     protected void addRelationsToThriftRow(EntityMetadata metadata, ThriftRow tf, List<RelationHolder> relations)
     {
         if (relations != null)
         {
             long timestamp = System.currentTimeMillis();
             MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                     metadata.getPersistenceUnit());
             for (RelationHolder rh : relations)
             {
                 String linkName = rh.getRelationName();
                 Object linkValue = rh.getRelationValue();
 
                 if (linkName != null && linkValue != null)
                 {
                     if (metaModel.getEmbeddables(metadata.getEntityClazz()).isEmpty())
                     {
                         if (metadata.isCounterColumnType())
                         {
                             CounterColumn col = populateCounterFkey(linkName, linkValue);
                             tf.addCounterColumn(col);
                         }
                         else
                         {
                             Column col = populateFkey(linkName, linkValue, timestamp);
                             tf.addColumn(col);
                         }
 
                     }
                     else
                     {
                         if (metadata.isCounterColumnType())
                         {
                             CounterSuperColumn counterSuperColumn = new CounterSuperColumn();
                             counterSuperColumn.setName(linkName.getBytes());
                             CounterColumn column = populateCounterFkey(linkName, linkValue);
                             counterSuperColumn.addToColumns(column);
                             tf.addCounterSuperColumn(counterSuperColumn);
                         }
                         else
                         {
                             SuperColumn superColumn = new SuperColumn();
                             superColumn.setName(linkName.getBytes());
                             Column column = populateFkey(linkName, linkValue, timestamp);
                             superColumn.addToColumns(column);
                             tf.addSuperColumn(superColumn);
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Populate counter fkey.
      * 
      * @param rlName
      *            the rl name
      * @param rlValue
      *            the rl value
      * @return the counter column
      */
     private CounterColumn populateCounterFkey(String rlName, Object rlValue)
     {
         CounterColumn counterCol = new CounterColumn();
         counterCol.setName(PropertyAccessorFactory.STRING.toBytes(rlName));
         counterCol.setValue((Long) rlValue);
         return counterCol;
     }
 
     /**
      * Deletes record for given primary key from counter column family.
      * 
      * @param pKey
      *            the key
      * @param metadata
      *            the metadata
      * @param consistencyLevel
      *            the consistency level
      */
     protected void deleteRecordFromCounterColumnFamily(Object pKey, String tableName, EntityMetadata metadata,
             ConsistencyLevel consistencyLevel)
     {
         ColumnPath path = new ColumnPath(tableName);
 
         Cassandra.Client conn = null;
         Object pooledConnection = null;
         try
         {
             pooledConnection = getConection();
             conn = getConnection(pooledConnection);
 
             if (log.isInfoEnabled())
             {
                 log.info("Removing data for counter column family {}.", tableName);
             }
 
             conn.remove_counter((CassandraUtilities.toBytes(pKey, metadata.getIdAttribute().getJavaType())).getBytes(),
                     path, consistencyLevel);
 
         }
         catch (Exception e)
         {
             log.error("Error during executing delete, Caused by: .", e);
             throw new PersistenceException(e);
         }
         finally
         {
             releaseConnection(pooledConnection);
         }
     }
 
     /**
      * Creates secondary indexes on columns if not already created.
      * 
      * @param tableName
      *            Column family name
      * @param poolName
      *            Pool Name
      * @param columns
      *            List of columns
      * @param columnType
      */
     protected void createIndexesOnColumns(EntityMetadata m, String tableName, List<Column> columns, Class columnType)
     {
         Object pooledConnection = null;
         try
         {
             Cassandra.Client api = null;
             pooledConnection = getConection();
             api = getConnection(pooledConnection);
             KsDef ksDef = api.describe_keyspace(m.getSchema());
             List<CfDef> cfDefs = ksDef.getCf_defs();
 
             // Column family definition on which secondary index creation is
             // required
             CfDef columnFamilyDefToUpdate = null;
             boolean isUpdatable = false;
             // boolean isNew=false;
 
             for (CfDef cfDef : cfDefs)
             {
                 if (cfDef.getName().equals(tableName))
                 {
                     columnFamilyDefToUpdate = cfDef;
                     // isNew=false;
                     break;
                 }
             }
 
             if (columnFamilyDefToUpdate == null)
             {
                 log.error("Join table {} not available.", tableName);
                 throw new PersistenceException("table" + tableName + " not found!");
             }
             // create a column family, in case it is not already available.
 
             // Get list of indexes already created
             List<ColumnDef> columnMetadataList = columnFamilyDefToUpdate.getColumn_metadata();
             List<String> indexList = new ArrayList<String>();
 
             if (columnMetadataList != null)
             {
                 for (ColumnDef columnDef : columnMetadataList)
                 {
                     indexList.add(Bytes.toUTF8(columnDef.getName()));
                 }
                 // need to set them to null else it is giving problem on update
                 // column family and trying to add again existing indexes.
                 // columnFamilyDefToUpdate.column_metadata = null;
             }
 
             // Iterate over all columns for creating secondary index on them
             for (Column column : columns)
             {
 
                 ColumnDef columnDef = new ColumnDef();
 
                 columnDef.setName(column.getName());
                 columnDef.setValidation_class(CassandraValidationClassMapper.getValidationClass(columnType, false));
                 columnDef.setIndex_type(IndexType.KEYS);
 
                 // Add secondary index only if it's not already created
                 // (if already created, it would be there in column family
                 // definition)
                 if (!indexList.contains(Bytes.toUTF8(column.getName())))
                 {
                     isUpdatable = true;
                     columnFamilyDefToUpdate.addToColumn_metadata(columnDef);
                 }
             }
 
             // Finally, update column family with modified column family
             // definition
             if (isUpdatable)
             {
                 columnFamilyDefToUpdate.setKey_validation_class(CassandraValidationClassMapper.getValidationClass(m
                         .getIdAttribute().getJavaType(), isCql3Enabled(m)));
                 api.system_update_column_family(columnFamilyDefToUpdate);
             }
 
         }
         catch (Exception e)
         {
             log.warn("Could not create secondary index on column family {}, Caused by: . ", tableName, e);
 
         }
         finally
         {
             releaseConnection(pooledConnection);
         }
     }
 
     /**
      * Finds an entiry from database.
      * 
      * @param entityClass
      *            the entity class
      * @param rowId
      *            the row id
      * @return the object
      */
     public Object find(Class entityClass, Object rowId)
     {
         EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(entityClass);
         List<String> relationNames = entityMetadata.getRelationNames();
         return find(entityClass, entityMetadata, rowId, relationNames);
     }
 
     /**
      * Finds a {@link List} of entities from database.
      * 
      * @param <E>
      *            the element type
      * @param entityClass
      *            the entity class
      * @param columnsToSelect
      *            TODO
      * @param rowIds
      *            the row ids
      * @return the list
      */
     public <E> List<E> findAll(Class<E> entityClass, String[] columnsToSelect, Object... rowIds)
     {
         EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(entityClass);
         List<E> results = new ArrayList<E>();
         results = find(entityClass, entityMetadata.getRelationNames(), entityMetadata.getRelationNames() != null
                 && !entityMetadata.getRelationNames().isEmpty(), entityMetadata, rowIds);
         return results.isEmpty() ? null : results;
     }
 
     /**
      * Find.
      * 
      * @param clazz
      *            the clazz
      * @param metadata
      *            the metadata
      * @param rowId
      *            the row id
      * @param relationNames
      *            the relation names
      * @return the object
      */
     private final Object find(Class<?> clazz, EntityMetadata metadata, Object rowId, List<String> relationNames)
     {
 
         List<Object> result = null;
         try
         {
             MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                     metadata.getPersistenceUnit());
 
             EntityType entityType = metaModel.entity(clazz);
 
             List<ManagedType> subTypes = ((AbstractManagedType) entityType).getSubManagedType();
 
             if (!subTypes.isEmpty())
             {
                 for (ManagedType subEntity : subTypes)
                 {
                     EntityMetadata subEntityMetadata = KunderaMetadataManager
                             .getEntityMetadata(subEntity.getJavaType());
                     result = populate(clazz, subEntityMetadata, rowId, subEntityMetadata.getRelationNames(), metaModel);
                     if (result != null && !result.isEmpty())
                     {
                         break;
                     }
 
                 }
             }
             else
             {
                 result = populate(clazz, metadata, rowId, relationNames, metaModel);
             }
 
         }
         catch (Exception e)
         {
             log.error("Error while retrieving records from database for entity {} and key {}, Caused by: .", clazz,
                     rowId, e);
 
             throw new PersistenceException(e);
         }
 
         return result != null && !result.isEmpty() ? result.get(0) : null;
     }
 
     private List<Object> populate(Class<?> clazz, EntityMetadata metadata, Object rowId, List<String> relationNames,
             MetamodelImpl metaModel)
     {
         List<Object> result;
         if (isCql3Enabled(metadata))
         {
             result = cqlClient.find(metaModel, metadata, rowId, relationNames);
         }
         else
         {
             result = (List<Object>) find(clazz, relationNames, relationNames != null, metadata, rowId);
         }
         return result;
     }
 
     /**
      * Returns true in case of, composite Id and if cql3 opted and not a
      * embedded entity.
      * 
      * @param metadata
      * @param metaModel
      * @return
      */
     public boolean isCql3Enabled(EntityMetadata metadata)
     {
         if (metadata != null)
         {
 
             MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                     metadata.getPersistenceUnit());
 
             if (metaModel.isEmbeddable(metadata.getIdAttribute().getBindableJavaType()))
             {
                 return true;
             }
             else if (getCqlVersion().equalsIgnoreCase(CassandraConstants.CQL_VERSION_3_0)
                     && metadata.getType().equals(Type.SUPER_COLUMN_FAMILY))
             {
                 log.warn("Super Columns not supported by cql, Any operation on supercolumn family will be executed using thrift, returning false.");
                 return false;
             }
             return getCqlVersion().equalsIgnoreCase(CassandraConstants.CQL_VERSION_3_0);
         }
         return getCqlVersion().equalsIgnoreCase(CassandraConstants.CQL_VERSION_3_0);
     }
 
     /**
      * Returns true in case of, composite Id and if cql3 opted and not a
      * embedded entity.
      * 
      * @param metadata
      * @param metaModel
      * @return
      */
     private boolean isCql3Enabled()
     {
         return isCql3Enabled(null);
     }
 
     /**
      * Find.
      * 
      * @param <E>
      *            the element type
      * @param entityClass
      *            the entity class
      * @param superColumnMap
      *            the super column map
      * @param dataHandler
      *            the data handler
      * @return the list
      */
     public <E> List<E> find(Class<E> entityClass, Map<String, String> superColumnMap, CassandraDataHandler dataHandler)
     {
         List<E> entities = null;
         String entityId = null;
         try
         {
             EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(getPersistenceUnit(), entityClass);
             entities = new ArrayList<E>();
             for (String superColumnName : superColumnMap.keySet())
             {
                 entityId = superColumnMap.get(superColumnName);
                 List<SuperColumn> superColumnList = loadSuperColumns(entityMetadata.getSchema(),
                         entityMetadata.getTableName(), entityId,
                         new String[] { superColumnName.substring(0, superColumnName.indexOf("|")) });
                 E e = (E) dataHandler.fromThriftRow(entityMetadata.getEntityClazz(), entityMetadata,
                         new DataRow<SuperColumn>(entityId, entityMetadata.getTableName(), superColumnList));
                 if (e != null)
                 {
                     entities.add(e);
                 }
             }
         }
         catch (Exception e)
         {
             log.error("Error while retrieving records from database for entity {} and key {}, Caused by: . ",
                     entityClass, entityId, e);
             throw new KunderaException(e);
         }
         return entities;
     }
 
     /**
      * Executes Select CQL Query.
      * 
      * @param cqlQuery
      *            the cql query
      * @param clazz
      *            the clazz
      * @param relationalField
      *            the relational field
      * @param dataHandler
      *            the data handler
      * @return the list
      */
     public List executeSelectQuery(Class clazz, List<String> relationalField, CassandraDataHandler dataHandler,
             boolean isNative, String cqlQuery)
     {
         if (log.isInfoEnabled())
         {
             log.info("Executing cql query {}.", cqlQuery);
         }
 
         List entities = new ArrayList<Object>();
 
         EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(clazz);
 
         MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                 entityMetadata.getPersistenceUnit());
 
         EntityType entityType = metaModel.entity(entityMetadata.getEntityClazz());
 
         List<AbstractManagedType> subManagedType = ((AbstractManagedType) entityType).getSubManagedType();
 
         if (subManagedType.isEmpty())
         {
             entities.addAll(cqlClient.executeQuery(clazz, relationalField, dataHandler, true, isNative, cqlQuery));
         }
         else
         {
             for (AbstractManagedType subEntity : subManagedType)
             {
                 EntityMetadata subEntityMetadata = KunderaMetadataManager.getEntityMetadata(subEntity.getJavaType());
 
                 entities.addAll(cqlClient.executeQuery(subEntityMetadata.getEntityClazz(), relationalField,
                         dataHandler, true, isNative, cqlQuery));
             }
         }
         return entities;
     }
 
     /**
      * Executes Update/ Delete CQL query
      * 
      * @param cqlQuery
      * @return
      */
     public int executeUpdateDeleteQuery(String cqlQuery)
     {
         if (log.isInfoEnabled())
         {
             log.info("Executing cql query {}.", cqlQuery);
         }
         try
         {
             CqlResult result = executeCQLQuery(cqlQuery, true);
             return result.getNum();
         }
         catch (Exception e)
         {
             log.error("Error while executing updated query: {}, Caused by: . ", cqlQuery, e);
             return 0;
         }
 
     }
 
     public Map<String, Object> getExternalProperties()
     {
         return externalProperties;
     }
 
     /**
      * Populate data.
      * 
      * @param m
      *            the m
      * @param qResults
      *            the q results
      * @param entities
      *            the entities
      * @param isRelational
      *            the is relational
      * @param relationNames
      *            the relation names
      * @param dataHandler
      *            the data handler
      */
     protected List populateData(EntityMetadata m, Map<Bytes, List<Column>> qResults, List<Object> entities,
             boolean isRelational, List<String> relationNames, CassandraDataHandler dataHandler)
     {
         if (m.getType().isSuperColumnFamilyMetadata())
         {
             Set<Bytes> primaryKeys = qResults.keySet();
 
             if (primaryKeys != null && !primaryKeys.isEmpty())
             {
                 Object[] rowIds = new Object[primaryKeys.size()];
                 int i = 0;
                 for (Bytes b : primaryKeys)
                 {
                     rowIds[i] = PropertyAccessorHelper.getObject(b, (Field) m.getIdAttribute().getJavaMember());
                     i++;
                 }
                 entities.addAll(findAll(m.getEntityClazz(), null, rowIds));
             }
 
         }
         else
         {
             Iterator<Bytes> rowIter = qResults.keySet().iterator();
             while (rowIter.hasNext())
             {
                 Bytes rowKey = rowIter.next();
                 List<Column> columns = qResults.get(rowKey);
                 try
                 {
                     Object id = PropertyAccessorHelper
                             .getObject(m.getIdAttribute().getJavaType(), rowKey.toByteArray());
                     Object e = null;
                     // e = PelopsUtils.initialize(m, e, id);
                     e = dataHandler.populateEntity(new ThriftRow(id, m.getTableName(), columns,
                             new ArrayList<SuperColumn>(0), new ArrayList<CounterColumn>(0),
                             new ArrayList<CounterSuperColumn>(0)), m, CassandraUtilities.getEntity(e), relationNames,
                             isRelational);
                     if (e != null)
                     {
                         // e = isRelational && !relationValue.isEmpty() ? new
                         // EnhanceEntity(e,
                         // PropertyAccessorHelper.getId(e, m), relationValue) :
                         // e;
                         entities.add(e);
                     }
                 }
                 catch (IllegalStateException e)
                 {
                     log.error("Error while returning entities for {}, Caused by: .", m.getEntityClazz(), e);
                     throw new KunderaException(e);
                 }
                 catch (Exception e)
                 {
                     log.error("Error while returning entities for {}, Caused by: .", m.getEntityClazz(), e);
                     throw new KunderaException(e);
                 }
             }
         }
 
         return entities;
 
     }
 
     /**
      * Populate entities from key slices.
      * 
      * @param m
      *            the m
      * @param isWrapReq
      *            the is wrap req
      * @param relations
      *            the relations
      * @param keys
      *            the keys
      * @param dataHandler
      *            the data handler
      * @return the list
      * @throws Exception
      *             the exception
      */
     protected List populateEntitiesFromKeySlices(EntityMetadata m, boolean isWrapReq, List<String> relations,
             List<KeySlice> keys, CassandraDataHandler dataHandler) throws Exception
     {
         List results;
         MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                 m.getPersistenceUnit());
 
         // List<String> superColumnNames = m.getEmbeddedColumnFieldNames();
         Set<String> superColumnAttribs = metaModel.getEmbeddables(m.getEntityClazz()).keySet();
         results = new ArrayList(keys.size());
 
         ThriftDataResultHelper dataGenerator = new ThriftDataResultHelper();
         for (KeySlice key : keys)
         {
             List<ColumnOrSuperColumn> columns = key.getColumns();
 
             byte[] rowKey = key.getKey();
 
             Object id = PropertyAccessorHelper.getObject(m.getIdAttribute().getJavaType(), rowKey);
 
             Object e = null;
             // e = PelopsUtils.initialize(m, e, id);
 
             Map<ByteBuffer, List<ColumnOrSuperColumn>> data = new HashMap<ByteBuffer, List<ColumnOrSuperColumn>>(1);
             data.put(ByteBuffer.wrap(rowKey), columns);
             ThriftRow tr = new ThriftRow();
             tr.setId(id);
             tr.setColumnFamilyName(m.getTableName());
             tr = dataGenerator.translateToThriftRow(data, m.isCounterColumnType(), m.getType(), tr);
 
             e = dataHandler.populateEntity(tr, m, CassandraUtilities.getEntity(e), relations, isWrapReq);
 
             if (e != null)
             {
                 // e = isWrapReq && !relationValue.isEmpty() ? new
                 // EnhanceEntity(e, PropertyAccessorHelper.getId(e, m),
                 // relationValue) : e;
                 results.add(e);
             }
         }
         return results;
     }
 
     /**
      * Return insert query string for given entity.
      * 
      * @param entityMetadata
      * @param entity
      * @param cassandra_client
      * @param rlHolders
      * @param ttlColumns
      *            TTL values for each columns
      * @return
      */
     protected List<String> createInsertQuery(EntityMetadata entityMetadata, Object entity,
             Cassandra.Client cassandra_client, List<RelationHolder> rlHolders, Object ttlColumns)
     {
         List<String> insert_Queries = new ArrayList<String>();
         CQLTranslator translator = new CQLTranslator();
         HashMap<TranslationType, Map<String, StringBuilder>> translation = translator.prepareColumnOrColumnValues(
                 entity, entityMetadata, TranslationType.ALL, externalProperties);
 
         Map<String, StringBuilder> columnNamesMap = translation.get(TranslationType.COLUMN);
         Map<String, StringBuilder> columnValuesMap = translation.get(TranslationType.VALUE);
 
         for (String tableName : columnNamesMap.keySet())
         {
             String insert_Query = translator.INSERT_QUERY;
 
             insert_Query = StringUtils.replace(insert_Query, CQLTranslator.COLUMN_FAMILY,
                     translator.ensureCase(new StringBuilder(), tableName, false).toString());
             String columnNames = columnNamesMap.get(tableName).toString();
             String columnValues = columnValuesMap.get(tableName).toString();
 
             StringBuilder columnNameBuilder = new StringBuilder(columnNames);
             StringBuilder columnValueBuilder = new StringBuilder(columnValues);
 
             for (RelationHolder rl : rlHolders)
             {
                 columnNameBuilder.append(",");
                 columnValueBuilder.append(",");
                 translator.appendColumnName(columnNameBuilder, rl.getRelationName());
                 translator.appendValue(columnValueBuilder, rl.getRelationValue().getClass(), rl.getRelationValue(),
                         true, false);
             }
 
             insert_Query = StringUtils
                     .replace(insert_Query, CQLTranslator.COLUMN_VALUES, columnValueBuilder.toString());
             insert_Query = StringUtils.replace(insert_Query, CQLTranslator.COLUMNS, columnNameBuilder.toString());
 
             if (log.isInfoEnabled())
             {
                 log.info("Returning cql query {}.", insert_Query);
             }
 
             if (ttlColumns != null && ttlColumns instanceof Integer)
             {
                 int ttl = ((Integer) ttlColumns).intValue();
                 if (ttl != 0)
                 {
                     insert_Query = insert_Query + " USING TTL " + ttl;
                 }
             }
             insert_Queries.add(insert_Query);
         }
         return insert_Queries;
     }
 
     /**
      * Return update query string for given entity intended for counter column
      * family.
      * 
      * @param entityMetadata
      * @param entity
      * @param cassandra_client
      * @param rlHolders
      * @return
      */
     protected List<String> createUpdateQueryForCounter(EntityMetadata entityMetadata, Object entity,
             Cassandra.Client cassandra_client, List<RelationHolder> rlHolders)
     {
         Map<String, String> builders = new HashMap<String, String>();
 
         CQLTranslator translator = new CQLTranslator();
 
         Object rowId = PropertyAccessorHelper.getId(entity, entityMetadata);
         MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                 entityMetadata.getPersistenceUnit());
 
         EntityType entityType = metaModel.entity(entityMetadata.getEntityClazz());
 
         Set<Attribute> attributes = entityType.getAttributes();
 
         for (Attribute attrib : attributes)
         {
             if (!entityMetadata.getIdAttribute().getName().equals(attrib.getName())
                     && !metaModel.isEmbeddable(attrib.getJavaType()) && !attrib.isAssociation())
             {
                 String tableName = ((AbstractAttribute) attrib).getTableName() != null ? ((AbstractAttribute) attrib)
                         .getTableName() : entityMetadata.getTableName();
 
                 String queryString = builders.get(tableName);
                 StringBuilder builder;
                 if (queryString == null)
                 {
                     builder = new StringBuilder();
                 }
                 else
                 {
                     builder = new StringBuilder(queryString);
                 }
                 translator.buildSetClauseForCounters(builder, ((AbstractAttribute) attrib).getJPAColumnName(),
                         PropertyAccessorHelper.getObject(entity, attrib.getName()));
                 builders.put(tableName, builder.toString());
             }
         }
         for (RelationHolder rl : rlHolders)
         {
             translator.buildSetClauseForCounters(new StringBuilder(builders.get(entityMetadata.getTableName())),
                     rl.getRelationName(), rl.getRelationValue());
         }
 
         for (String tableName : builders.keySet())
         {
             StringBuilder builder = new StringBuilder(builders.get(tableName));
 
             String update_Query = translator.UPDATE_QUERY;
 
             update_Query = StringUtils.replace(update_Query, CQLTranslator.COLUMN_FAMILY,
                     translator.ensureCase(new StringBuilder(), tableName, false).toString());
 
             // strip last "," clause.
             builder.delete(builder.lastIndexOf(CQLTranslator.COMMA_STR), builder.length());
             onWhereClause(entityMetadata, rowId, translator, builder, metaModel);
 
             StringBuilder queryBuilder = new StringBuilder(update_Query);
             queryBuilder.append(CQLTranslator.ADD_SET_CLAUSE);
             queryBuilder.append(builder);
 
             if (log.isInfoEnabled())
             {
                 log.info("Returning update query {}.", queryBuilder.toString());
             }
 
             builders.put(tableName, queryBuilder.toString());
         }
         return new ArrayList(builders.values());
     }
 
     /**
      * Gets the cql version.
      * 
      * @return the cqlVersion
      */
     protected String getCqlVersion()
     {
         return cqlVersion;
     }
 
     /**
      * Sets the cql version.
      * 
      * @param cqlVersion
      *            the cqlVersion to set
      */
     public void setCqlVersion(String cqlVersion)
     {
         this.cqlVersion = cqlVersion;
     }
 
     /**
      * Sets the consistency level.
      * 
      * @param cLevel
      *            the new consistency level
      */
     public void setConsistencyLevel(ConsistencyLevel cLevel)
     {
         if (cLevel != null)
         {
             this.consistencyLevel = cLevel;
         }
         else
         {
             log.warn("Invalid consistency level {null} provided, default level will be used.");
         }
     }
 
     /**
      * Close.
      */
     public void close()
     {
         clear();
         setCqlVersion(CassandraConstants.CQL_VERSION_2_0);
         closed = true;
         externalProperties = null;
     }
 
     /**
      * Checks if is open.
      * 
      * @return true, if is open
      */
     protected final boolean isOpen()
     {
         return !closed;
     }
 
     /**
      * Gets the consistency level.
      * 
      * @return the consistency level
      */
     protected ConsistencyLevel getConsistencyLevel()
     {
         return consistencyLevel;
     }
 
     /**
      * On delete query.
      * 
      * @param metadata
      *            the metadata
      * @param tableName
      *            TODO
      * @param metaModel
      *            the meta model
      * @param keyObject
      *            the compound key object
      * @param compoundKey
      *            the compound key
      */
     protected String onDeleteQuery(EntityMetadata metadata, String tableName, MetamodelImpl metaModel, Object keyObject)
     {
         CQLTranslator translator = new CQLTranslator();
         String deleteQuery = CQLTranslator.DELETE_QUERY;
 
         deleteQuery = StringUtils.replace(deleteQuery, CQLTranslator.COLUMN_FAMILY,
                 translator.ensureCase(new StringBuilder(), tableName, false).toString());
 
         StringBuilder deleteQueryBuilder = new StringBuilder(deleteQuery);
         onWhereClause(metadata, keyObject, translator, deleteQueryBuilder, metaModel);
 
         if (log.isInfoEnabled())
         {
             log.info("Returning delete query {}.", deleteQueryBuilder.toString());
         }
         return deleteQueryBuilder.toString();
     }
 
     /**
      * On where clause.
      * 
      * @param metadata
      *            the metadata
      * @param key
      *            the compound key object
      * @param translator
      *            the translator
      * @param queryBuilder
      *            the query builder
      * @param compoundKey
      *            the compound key
      */
     private void onWhereClause(EntityMetadata metadata, Object key, CQLTranslator translator,
             StringBuilder queryBuilder, MetamodelImpl metaModel)
     {
         queryBuilder.append(CQLTranslator.ADD_WHERE_CLAUSE);
 
         if (metaModel.isEmbeddable(metadata.getIdAttribute().getBindableJavaType()))
         {
             Field[] fields = metadata.getIdAttribute().getBindableJavaType().getDeclaredFields();
             EmbeddableType compoundKey = metaModel.embeddable(metadata.getIdAttribute().getBindableJavaType());
 
             for (Field field : fields)
             {
                 if (field != null && !Modifier.isStatic(field.getModifiers())
                         && !Modifier.isTransient(field.getModifiers()) && !field.isAnnotationPresent(Transient.class))
                 {
                     Attribute attribute = compoundKey.getAttribute(field.getName());
                     String columnName = ((AbstractAttribute) attribute).getJPAColumnName();
                     translator.buildWhereClause(queryBuilder, columnName, field, key);
                 }
             }
         }
         else
         {
             Attribute attribute = metadata.getIdAttribute();
             translator.buildWhereClause(queryBuilder,
                     ((AbstractAttribute) metadata.getIdAttribute()).getBindableJavaType(),
                     CassandraUtilities.getIdColumnName(metadata, getExternalProperties()), key, translator.EQ_CLAUSE,
                     false);
         }
 
         // strip last "AND" clause.
         queryBuilder.delete(queryBuilder.lastIndexOf(CQLTranslator.AND_CLAUSE), queryBuilder.length());
     }
 
     /**
      * Find.
      * 
      * @param entityClass
      *            the entity class
      * @param relationNames
      *            the relation names
      * @param isWrapReq
      *            the is wrap req
      * @param metadata
      *            the metadata
      * @param rowIds
      *            the row ids
      * @return the list
      */
     public abstract List find(Class entityClass, List<String> relationNames, boolean isWrapReq,
             EntityMetadata metadata, Object... rowIds);
 
     /**
      * Load super columns.
      * 
      * @param keyspace
      *            the keyspace
      * @param columnFamily
      *            the column family
      * @param rowId
      *            the row id
      * @param superColumnNames
      *            the super column names
      * @return the list
      */
     protected abstract List<SuperColumn> loadSuperColumns(String keyspace, String columnFamily, String rowId,
             String... superColumnNames);
 
     /**
      * Query related methods.
      * 
      * @param cqlQuery
      *            the cql query
      * @param clazz
      *            the clazz
      * @param relationalField
      *            the relational field
      * @return the list
      */
     public abstract List executeQuery(Class clazz, List<String> relationalField, boolean isNative, String cqlQuery);
 
     /**
      * Find.
      * 
      * @param ixClause
      *            the ix clause
      * @param m
      *            the m
      * @param isRelation
      *            the is relation
      * @param relations
      *            the relations
      * @param maxResult
      *            the max result
      * @param columns
      *            the columns
      * @return the list
      */
     public abstract List find(List<IndexClause> ixClause, EntityMetadata m, boolean isRelation, List<String> relations,
             int maxResult, List<String> columns);
 
     /**
      * Find by range.
      * 
      * @param muinVal
      *            the muin val
      * @param maxVal
      *            the max val
      * @param m
      *            the m
      * @param isWrapReq
      *            the is wrap req
      * @param relations
      *            the relations
      * @param columns
      *            the columns
      * @param conditions
      *            the conditions
      * @return the list
      * @throws Exception
      *             the exception
      */
     public abstract List findByRange(byte[] muinVal, byte[] maxVal, EntityMetadata m, boolean isWrapReq,
             List<String> relations, List<String> columns, List<IndexExpression> conditions, int maxResults)
             throws Exception;
 
     /**
      * Search in inverted index.
      * 
      * @param columnFamilyName
      *            the column family name
      * @param m
      *            the m
      * @param indexClauseMap
      *            the index clause map
      * @return the list
      */
     public abstract List<SearchResult> searchInInvertedIndex(String columnFamilyName, EntityMetadata m,
             Map<Boolean, List<IndexClause>> indexClauseMap);
 
     /**
      * Find.
      * 
      * @param m
      *            the m
      * @param relationNames
      *            the relation names
      * @param conditions
      *            the conditions
      * @param maxResult
      *            the max result
      * @param columns
      *            the columns
      * @return the list
      */
     public abstract List<EnhanceEntity> find(EntityMetadata m, List<String> relationNames,
             List<IndexClause> conditions, int maxResult, List<String> columns);
 
     /**
      * Gets the data handler.
      * 
      * @return the data handler
      */
     protected abstract CassandraDataHandler getDataHandler();
 
     /**
      * Delete.
      * 
      * @param entity
      *            the entity
      * @param pKey
      *            the key
      */
     protected abstract void delete(Object entity, Object pKey);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.impetus.kundera.persistence.api.Batcher#addBatch(com.impetus.kundera
      * .graph.Node)
      */
     /**
      * Adds the batch.
      * 
      * @param node
      *            the node
      */
     public void addBatch(Node node)
     {
 
         if (node != null)
         {
             nodes.add(node);
         }
 
         onBatchLimit();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.impetus.kundera.persistence.api.Batcher#getBatchSize()
      */
     /**
      * Gets the batch size.
      * 
      * @return the batch size
      */
     public int getBatchSize()
     {
         return batchSize;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.impetus.kundera.persistence.api.Batcher#clear()
      */
     public void clear()
     {
         if (nodes != null)
         {
             nodes.clear();
             nodes = new ArrayList<Node>();
         }
 
         if (ttlPerSession)
         {
             ttlValues.clear();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.impetus.kundera.persistence.api.Batcher#executeBatch()
      */
     /**
      * Execute batch.
      * 
      * @return the int
      */
     public int executeBatch()
     {
         Cassandra.Client conn = null;
         Object pooledConnection = null;
 
         /**
          * Key -> Entity Class Value -> Map containing Row ID as Key and
          * Mutation List as Value
          */
         Map<Class<?>, Map<ByteBuffer, Map<String, List<Mutation>>>> batchMutationMap = new HashMap<Class<?>, Map<ByteBuffer, Map<String, List<Mutation>>>>();
 
         int recordsExecuted = 0;
         String batchQuery = CQLTranslator.BATCH_QUERY;
         batchQuery = StringUtils.replace(batchQuery, CQLTranslator.STATEMENT, "");
         StringBuilder batchQueryBuilder = new StringBuilder(batchQuery);
         try
         {
             boolean isCql3Enabled = false;
             for (Node node : nodes)
             {
                 if (node.isDirty())
                 {
                     node.handlePreEvent();
                     Object entity = node.getData();
                     Object id = node.getEntityId();
                     EntityMetadata metadata = KunderaMetadataManager.getEntityMetadata(node.getDataClass());
                     persistenceUnit = metadata.getPersistenceUnit();
                     isUpdate = node.isUpdate();
 
                     MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata()
                             .getMetamodel(metadata.getPersistenceUnit());
 
                     // delete can not be executed in batch
 
                     if (isCql3Enabled(metadata))
                     {
                         isCql3Enabled = true;
                         List<RelationHolder> relationHolders = getRelationHolders(node);
                         // onPersist(metadata, entity, id, relationHolders);
                         if (node.isInState(RemovedState.class))
                         {
                             String query;
                             query = onDeleteQuery(metadata, metadata.getTableName(), metaModel, id);
                             batchQueryBuilder.append(query);
                         }
                         else
                         {
                             List<String> insertQueries = createInsertQuery(metadata, entity, conn, relationHolders,
                                     getTtlValues().get(metadata.getTableName()));
                             for (String query : insertQueries)
                             {
                                 batchQueryBuilder.append(query);
                             }
                         }
 
                     }
                     else
                     {
                         if (node.isInState(RemovedState.class))
                         {
                             delete(entity, id);
                         }
                         else
                         {
                             List<RelationHolder> relationHolders = getRelationHolders(node);
                             Map<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
                             mutationMap = prepareMutation(metadata, entity, id, relationHolders, mutationMap);
 
                             recordsExecuted += mutationMap.size();
                             if (!batchMutationMap.containsKey(metadata.getEntityClazz()))
                             {
                                 batchMutationMap.put(metadata.getEntityClazz(), mutationMap);
                             }
                             else
                             {
                                 batchMutationMap.get(metadata.getEntityClazz()).putAll(mutationMap);
                             }
 
                             indexNode(node, metadata);
                         }
                     }
                     node.handlePostEvent();
                 }
             }
 
             // Write Mutation map to database
 
             if (!batchMutationMap.isEmpty())
             {
                 pooledConnection = getConection();
                 conn = getConnection(pooledConnection);
 
                 for (Class<?> entityClass : batchMutationMap.keySet())
                 {
                     conn.batch_mutate(batchMutationMap.get(entityClass), consistencyLevel);
                 }
 
             }
 
             if (!nodes.isEmpty() && isCql3Enabled)
             {
                 batchQueryBuilder.append(CQLTranslator.APPLY_BATCH);
                 executeCQLQuery(batchQueryBuilder.toString(), isCql3Enabled);
             }
         }
         catch (Exception e)
         {
             log.error("Error while persisting record. Caused by: .", e);
             throw new KunderaException(e);
         }
         finally
         {
             clear();
             if (pooledConnection != null)
             {
                 releaseConnection(pooledConnection);
             }
         }
 
         return recordsExecuted;
     }
 
     /**
      * Prepare mutation.
      * 
      * @param entityMetadata
      *            the entity metadata
      * @param entity
      *            the entity
      * @param id
      *            the id
      * @param relationHolders
      *            the relation holders
      * @param mutationMap
      *            the mutation map
      * @return the map
      */
     protected Map<ByteBuffer, Map<String, List<Mutation>>> prepareMutation(EntityMetadata entityMetadata,
             Object entity, Object id, List<RelationHolder> relationHolders,
             Map<ByteBuffer, Map<String, List<Mutation>>> mutationMap)
     {
 
         if (!isOpen())
         {
             throw new PersistenceException("ThriftClient is closed.");
         }
 
         // check for counter column
         if (isUpdate && entityMetadata.isCounterColumnType())
         {
             log.warn("Invalid operation! {} is not possible over counter column of entity {}.", "Merge",
                     entityMetadata.getEntityClazz());
             throw new UnsupportedOperationException("Invalid operation! Merge is not possible over counter column.");
         }
 
         Collection<ThriftRow> tfRows = null;
         try
         {
             String columnFamily = entityMetadata.getTableName();
             tfRows = getDataHandler().toThriftRow(entity, id, entityMetadata, columnFamily,
                     getTtlValues().get(columnFamily));
         }
         catch (Exception e)
         {
             log.error("Error during persisting record for entity {}, Caused by: .", entityMetadata.getEntityClazz(),
                     entityMetadata.getTableName(), e);
             throw new KunderaException(e);
         }
 
         Map<String, List<Mutation>> columnFamilyValues = new HashMap<String, List<Mutation>>();
 
         for (ThriftRow tf : tfRows)
         {
             if (tf.getColumnFamilyName().equals(entityMetadata.getTableName()))
             {
                 addRelationsToThriftRow(entityMetadata, tf, relationHolders);
             }
 
             String columnFamily = tf.getColumnFamilyName();
             // Create Insertion List
             List<Mutation> mutationList = new ArrayList<Mutation>();
 
             /*********** Handling for counter column family ************/
 
             if (entityMetadata.isCounterColumnType())
             {
                 List<CounterColumn> thriftCounterColumns = tf.getCounterColumns();
                 List<CounterSuperColumn> thriftCounterSuperColumns = tf.getCounterSuperColumns();
 
                 if (thriftCounterColumns != null && !thriftCounterColumns.isEmpty())
                 {
                     for (CounterColumn column : thriftCounterColumns)
                     {
                         Mutation mut = new Mutation();
                         mut.setColumn_or_supercolumn(new ColumnOrSuperColumn().setCounter_column(column));
                         mutationList.add(mut);
                     }
                 }
 
                 if (thriftCounterSuperColumns != null && !thriftCounterSuperColumns.isEmpty())
                 {
                     for (CounterSuperColumn sc : thriftCounterSuperColumns)
                     {
                         Mutation mut = new Mutation();
                         mut.setColumn_or_supercolumn(new ColumnOrSuperColumn().setCounter_super_column(sc));
                         mutationList.add(mut);
                     }
                 }
             }
             else
             /********* Handling for column family and super column family *********/
             {
                 List<Column> thriftColumns = tf.getColumns();
                 List<SuperColumn> thriftSuperColumns = tf.getSuperColumns();
 
                 // Populate Insertion list for columns
                 if (thriftColumns != null && !thriftColumns.isEmpty())
                 {
                     for (Column column : thriftColumns)
                     {
                         Mutation mut = new Mutation();
                         mut.setColumn_or_supercolumn(new ColumnOrSuperColumn().setColumn(column));
                         mutationList.add(mut);
                     }
                 }
 
                 // Populate Insertion list for super columns
                 if (thriftSuperColumns != null && !thriftSuperColumns.isEmpty())
                 {
                     for (SuperColumn superColumn : thriftSuperColumns)
                     {
                         Mutation mut = new Mutation();
                         mut.setColumn_or_supercolumn(new ColumnOrSuperColumn().setSuper_column(superColumn));
                         mutationList.add(mut);
                     }
                 }
             }
             columnFamilyValues.put(columnFamily, mutationList);
         }
         // Create Mutation Map
 
         Bytes b = CassandraUtilities.toBytes(id, entityMetadata.getIdAttribute().getBindableJavaType());
         mutationMap.put(b.getBytes(), columnFamilyValues);
 
         return mutationMap;
     }
 
     /**
      * Check on batch limit.
      */
     private void onBatchLimit()
     {
         if (batchSize > 0 && batchSize == nodes.size())
         {
             executeBatch();
             nodes.clear();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.impetus.kundera.client.ClientPropertiesSetter#populateClientProperties
      * (com.impetus.kundera.client.Client, java.util.Map)
      */
     @Override
     public void populateClientProperties(Client client, Map<String, Object> properties)
     {
         new CassandraClientProperties().populateClientProperties(client, properties);
     }
 
     /**
      * Returns raw cassandra client from thrift connection pool.
      * 
      * @param persistenceUnit
      *            persistence unit.
      * @param schema
      *            schema or keyspace.
      * @return raw cassandra client.
      */
     protected Cassandra.Client getRawClient(final String persistenceUnit, final String schema)
     {
         Cassandra.Client client = null;
         Object pooledConnection;
         pooledConnection = getConection();
         client = getConnection(pooledConnection);
         try
         {
             client.set_cql_version(getCqlVersion());
         }
         catch (Exception e)
         {
             log.error("Error during borrowing a connection for persistence unit {}, Caused by: .", persistenceUnit, e);
             throw new KunderaException(e);
         }
         finally
         {
             releaseConnection(pooledConnection);
         }
         return client;
 
     }
 
     /**
      * Return the generated value of id.
      * 
      * @param descriptor
      * @param pu
      * @return
      */
     public Long getGeneratedValue(TableGeneratorDiscriptor descriptor, String pu)
     {
         Cassandra.Client conn = getRawClient(pu, descriptor.getSchema());
         try
         {
             conn.set_keyspace(descriptor.getSchema());
             ColumnPath columnPath = new ColumnPath(descriptor.getTable());
             columnPath.setColumn(descriptor.getValueColumnName().getBytes());
             long latestCount = 0l;
 
             try
             {
                 latestCount = conn.get(ByteBuffer.wrap(descriptor.getPkColumnValue().getBytes()), columnPath,
                         getConsistencyLevel()).counter_column.value;
             }
             catch (NotFoundException e)
             {
                 log.warn("Counter value not found for {}, resetting it to zero.", descriptor.getPkColumnName());
                 latestCount = 0;
             }
             ColumnParent columnParent = new ColumnParent(descriptor.getTable());
 
             CounterColumn counterColumn = new CounterColumn(
                     ByteBuffer.wrap(descriptor.getValueColumnName().getBytes()), 1);
 
             conn.add(ByteBuffer.wrap(descriptor.getPkColumnValue().getBytes()), columnParent, counterColumn,
                     getConsistencyLevel());
 
             if (latestCount == 0)
             {
                 return (long) descriptor.getInitialValue();
             }
             else
             {
                 return (latestCount + 1) * descriptor.getAllocationSize();
             }
         }
         catch (UnavailableException e)
         {
             log.error("Error while reading counter value from table{}, Caused by: .", descriptor.getTable(), e);
             throw new KunderaException(e);
         }
         catch (TimedOutException e)
         {
             log.error("Error while reading counter value from table{}, Caused by: .", descriptor.getTable(), e);
             throw new KunderaException(e);
         }
         catch (Exception e)
         {
             log.error("Error while using keyspace. Caused by: .", e);
             throw new KunderaException(e);
         }
     }
 
     /**
      * Executes query string using cql3.
      * 
      * @param cqlQuery
      * @return
      * @throws InvalidRequestException
      * @throws UnavailableException
      * @throws TimedOutException
      * @throws SchemaDisagreementException
      * @throws TException
      */
     protected CqlResult executeCQLQuery(String cqlQuery, boolean isCql3Enabled)
     {
         Cassandra.Client conn = null;
         Object pooledConnection = null;
         pooledConnection = getConection();
         conn = getConnection(pooledConnection);
         try
         {
             if (isCql3Enabled || isCql3Enabled())
             {
                 return conn.execute_cql3_query(ByteBufferUtil.bytes(cqlQuery),
                         org.apache.cassandra.thrift.Compression.NONE, consistencyLevel);
             }
 
             if (log.isInfoEnabled())
             {
                 log.info("Executing cql query {}.", cqlQuery);
             }
             return conn.execute_cql_query(ByteBufferUtil.bytes(cqlQuery), org.apache.cassandra.thrift.Compression.NONE);
         }
         catch (Exception ex)
         {
            if(log.isErrorEnabled())
            {
                log.error("Error during executing query {}, Caused by: {} .", cqlQuery, ex);
            }
             throw new PersistenceException(ex);
         }
         finally
         {
             releaseConnection(pooledConnection);
         }
     }
 
     /**
      * Find List of objects based on value {@columnValue} of column
      * {@columnName}
      * 
      * @param m
      * @param columnName
      * @param columnValue
      * @param clazz
      * @param dataHandler
      * @return
      */
     protected List<Object> findByRelationQuery(EntityMetadata m, String columnName, Object columnValue, Class clazz,
             CassandraDataHandler dataHandler)
     {
         return cqlClient.findByRelationQuery(m, columnName, columnValue, clazz, dataHandler);
     }
 
     private void setBatchSize(String persistenceUnit, Map<String, Object> puProperties)
     {
         String batch_Size = null;
 
         PersistenceUnitMetadata puMetadata = KunderaMetadataManager.getPersistenceUnitMetadata(persistenceUnit);
 
         String externalBatchSize = puProperties != null ? (String) puProperties
                 .get(PersistenceProperties.KUNDERA_BATCH_SIZE) : null;
 
         Integer intbatch = null;
         if (puMetadata.getBatchSize() > 0)
         {
             intbatch = new Integer(puMetadata.getBatchSize());
         }
         batch_Size = (String) (externalBatchSize != null ? externalBatchSize : intbatch != null ? intbatch.toString()
                 : null);
 
         setBatchSize(batch_Size);
     }
 
     void setBatchSize(String batch_Size)
     {
         if (!StringUtils.isBlank(batch_Size))
         {
             batchSize = Integer.valueOf(batch_Size);
             if (batchSize == 0)
             {
                 throw new IllegalArgumentException("kundera.batch.size property must be numeric and > 0.");
             }
         }
     }
 
     private void populateCqlVersion(Map<String, Object> externalProperties)
     {
         String cqlVersion = externalProperties != null ? (String) externalProperties
                 .get(CassandraConstants.CQL_VERSION) : null;
         if (cqlVersion == null
                 || !(cqlVersion != null && (cqlVersion.equals(CassandraConstants.CQL_VERSION_2_0) || cqlVersion
                         .equals(CassandraConstants.CQL_VERSION_3_0))))
         {
             cqlVersion = (CassandraPropertyReader.csmd != null ? CassandraPropertyReader.csmd.getCqlVersion()
                     : CassandraConstants.CQL_VERSION_2_0);
         }
 
         if (cqlVersion.equals(CassandraConstants.CQL_VERSION_3_0))
         {
             setCqlVersion(CassandraConstants.CQL_VERSION_3_0);
         }
         else
         {
             setCqlVersion(CassandraConstants.CQL_VERSION_2_0);
         }
     }
 
     /**
      * Return cassandra client instance.
      * 
      * @param connection
      * @return
      */
     private Cassandra.Client getConnection(Object connection)
     {
         if (connection != null)
         {
             if (connection.getClass().isAssignableFrom(Connection.class))
             {
                 return ((Connection) connection).getClient();
             }
             else
             {
                 return ((IPooledConnection) connection).getAPI();
             }
         }
 
         throw new KunderaException("Invalid configuration!, no available pooled connection found for:"
                 + this.getClass().getSimpleName());
     }
 
     protected abstract Object getConection();
 
     protected abstract void releaseConnection(Object conn);
 
     /**
      * Use CqlClient class for crud when cql enable.
      * 
      * 
      * @author Kuldeep Mishra
      * 
      */
     protected class CQLClient
     {
         /**
          * 
          * @param entityMetadata
          * @param entity
          * @param conn
          * @param rlHolders
          * @param ttlColumns
          * @throws UnsupportedEncodingException
          * @throws InvalidRequestException
          * @throws TException
          * @throws UnavailableException
          * @throws TimedOutException
          * @throws SchemaDisagreementException
          */
         public void persist(EntityMetadata entityMetadata, Object entity,
                 org.apache.cassandra.thrift.Cassandra.Client conn, List<RelationHolder> rlHolders, Object ttlColumns)
                 throws UnsupportedEncodingException, InvalidRequestException, TException, UnavailableException,
                 TimedOutException, SchemaDisagreementException
         {
             List<String> queries;
             if (entityMetadata.isCounterColumnType())
             {
                 queries = createUpdateQueryForCounter(entityMetadata, entity, conn, rlHolders);
             }
             else
             {
                 queries = createInsertQuery(entityMetadata, entity, conn, rlHolders, ttlColumns);
             }
 
             for (String query : queries)
             {
                 conn.execute_cql3_query(ByteBuffer.wrap(query.getBytes(Constants.CHARSET_UTF8)), Compression.NONE,
                         consistencyLevel);
             }
         }
 
         /**
          * Execute query and Return list of Objects.
          * 
          * @param cqlQuery
          * @param clazz
          * @param relationalField
          * @param dataHandler
          * @return
          */
         public List executeQuery(Class clazz, List<String> relationalField, CassandraDataHandler dataHandler,
                 boolean isCql3Enabled, boolean isNative, String cqlQuery)
         {
             EntityMetadata entityMetadata = KunderaMetadataManager.getEntityMetadata(clazz);
             ApplicationMetadata appMetadata = KunderaMetadata.INSTANCE.getApplicationMetadata();
 
             CqlResult result = null;
             List returnedEntities = new ArrayList();
             try
             {
                 if (log.isInfoEnabled())
                 {
                     log.info("Executing query {}.", cqlQuery);
                 }
                 result = executeCQLQuery(cqlQuery, isCql3Enabled);
                 if (result != null && (result.getRows() != null || result.getRowsSize() > 0))
                 {
                     returnedEntities = new ArrayList<Object>(result.getRowsSize());
                     Iterator<CqlRow> iter = result.getRowsIterator();
                     while (iter.hasNext())
                     {
                         Object e = null;
                         // e = PelopsUtils.initialize(entityMetadata, e,
                         // null);
                         CqlRow row = iter.next();
                         Object rowKey = null;
 
                         ThriftRow thriftRow = null;
                         thriftRow = new ThriftRow(rowKey, entityMetadata.getTableName(), row.getColumns(),
                                 new ArrayList<SuperColumn>(0), new ArrayList<CounterColumn>(0),
                                 new ArrayList<CounterSuperColumn>(0));
 
                         e = dataHandler.populateEntity(thriftRow, entityMetadata, CassandraUtilities.getEntity(e),
                                 relationalField, relationalField != null && !relationalField.isEmpty());
 
                         e = populateSecondaryTableData(relationalField, dataHandler, isCql3Enabled, entityMetadata, e);
 
                         if (e != null)
                         {
                             returnedEntities.add(e);
                         }
                         else if (isNative)
                         {
                             returnedEntities.add(row.getColumns().get(0));
                         }
                     }
                 }
             }
             catch (Exception e)
             {
                 log.error("Error while executing native CQL query Caused by: .", e);
                 throw new PersistenceException(e);
             }
             return returnedEntities;
         }
 
         /**
          * 
          * @param relationalField
          * @param dataHandler
          * @param isCql3Enabled
          * @param cqlQuery
          * @param entityMetadata
          * @param iter
          * @param e
          * @return
          */
         private Object populateSecondaryTableData(List<String> relationalField, CassandraDataHandler dataHandler,
                 boolean isCql3Enabled, EntityMetadata entityMetadata, Object e)
         {
             CqlResult result;
             // For secondary tables.
             MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                     entityMetadata.getPersistenceUnit());
             if (!metaModel.isEmbeddable(entityMetadata.getIdAttribute().getBindableJavaType()))
             {
                 AbstractManagedType managedType = (AbstractManagedType) metaModel.entity(entityMetadata
                         .getEntityClazz());
                 List<String> secondaryTables = ((DefaultEntityAnnotationProcessor) managedType.getEntityAnnotation())
                         .getSecondaryTablesName();
 
                 CQLTranslator translator = new CQLTranslator();
 
                 for (String tableName : secondaryTables)
                 {
                     // Building query.
                     StringBuilder queryBuilder = new StringBuilder("select * from \"" + tableName + "\" where ");
                     Attribute attribute = entityMetadata.getIdAttribute();
                     translator.buildWhereClause(queryBuilder,
                             ((AbstractAttribute) entityMetadata.getIdAttribute()).getBindableJavaType(),
                             CassandraUtilities.getIdColumnName(entityMetadata, getExternalProperties()),
                             PropertyAccessorHelper.getId(e, entityMetadata), translator.EQ_CLAUSE, false);
                     // strip last "AND" clause.
                     queryBuilder.delete(queryBuilder.lastIndexOf(CQLTranslator.AND_CLAUSE), queryBuilder.length());
 
                     // Executing.
                     result = executeCQLQuery(queryBuilder.toString(), isCql3Enabled);
 
                     if (result != null && (result.getRows() != null || result.getRowsSize() > 0))
                     {
                         Iterator<CqlRow> iterator = result.getRowsIterator();
                         while (iterator.hasNext())
                         {
                             // null);
                             CqlRow cqlRow = iterator.next();
 
                             ThriftRow tr = null;
                             tr = new ThriftRow(null, entityMetadata.getTableName(), cqlRow.getColumns(),
                                     new ArrayList<SuperColumn>(0), new ArrayList<CounterColumn>(0),
                                     new ArrayList<CounterSuperColumn>(0));
 
                             e = dataHandler.populateEntity(tr, entityMetadata, CassandraUtilities.getEntity(e),
                                     relationalField, relationalField != null && !relationalField.isEmpty());
                             break;
                         }
                     }
                 }
             }
             return e;
         }
 
         /**
          * Finds entity on the basis of rowid and return list of objects.
          * 
          * @param metaModel
          * @param metadata
          * @param rowId
          * @param relationNames
          * @return
          */
         public List<Object> find(MetamodelImpl metaModel, EntityMetadata metadata, Object rowId,
                 List<String> relationNames)
         {
             CQLTranslator translator = new CQLTranslator();
 
             // List<String> queries = new ArrayList<String>();
 
             String tableName = metadata.getTableName();
             // for (String tableName : secondaryTables)
             // {
             String select_Query = translator.SELECTALL_QUERY;
             select_Query = StringUtils.replace(select_Query, CQLTranslator.COLUMN_FAMILY,
                     translator.ensureCase(new StringBuilder(), tableName, false).toString());
             StringBuilder builder = new StringBuilder(select_Query);
             onWhereClause(metadata, rowId, translator, builder, metaModel);
             // queries.add(builder.toString());
             // }
 
             return CassandraClientBase.this.executeQuery(metadata.getEntityClazz(), relationNames, false,
                     builder.toString());
 
             // String select_Query = translator.SELECTALL_QUERY;
             // select_Query = StringUtils.replace(select_Query,
             // CQLTranslator.COLUMN_FAMILY,
             // translator.ensureCase(new StringBuilder(),
             // metadata.getTableName(), false).toString());
             // StringBuilder builder = new StringBuilder(select_Query);
             // onWhereClause(metadata, rowId, translator, builder, metaModel);
             // return CassandraClientBase.this.executeQuery(builder.toString(),
             // metadata.getEntityClazz(), relationNames,false);
 
         }
 
         /**
          * Find List of objects based on value {@columnValue} of column
          * {@columnName}
          * 
          * @param m
          * @param columnName
          * @param columnValue
          * @param clazz
          * @param dataHandler
          * @return
          */
         protected List<Object> findByRelationQuery(EntityMetadata m, String columnName, Object columnValue,
                 Class clazz, CassandraDataHandler dataHandler)
         {
             CQLTranslator translator = new CQLTranslator();
             String selectQuery = translator.SELECTALL_QUERY;
             selectQuery = StringUtils.replace(selectQuery, CQLTranslator.COLUMN_FAMILY,
                     translator.ensureCase(new StringBuilder(), m.getTableName(), false).toString());
 
             StringBuilder selectQueryBuilder = new StringBuilder(selectQuery);
             selectQueryBuilder.append(CQLTranslator.ADD_WHERE_CLAUSE);
 
             translator.buildWhereClause(selectQueryBuilder, columnValue.getClass(), columnName, columnValue,
                     CQLTranslator.EQ_CLAUSE, false);
             selectQueryBuilder.delete(selectQueryBuilder.lastIndexOf(CQLTranslator.AND_CLAUSE),
                     selectQueryBuilder.length());
             return executeQuery(clazz, m.getRelationNames(), dataHandler, true, false, selectQueryBuilder.toString());
         }
     }
 
     /**
      * @return the ttlPerRequest
      */
     public boolean isTtlPerRequest()
     {
         return ttlPerRequest;
     }
 
     /**
      * @param ttlPerRequest
      *            the ttlPerRequest to set
      */
     public void setTtlPerRequest(boolean ttlPerRequest)
     {
         this.ttlPerRequest = ttlPerRequest;
     }
 
     /**
      * @return the ttlPerSession
      */
     public boolean isTtlPerSession()
     {
         return ttlPerSession;
     }
 
     /**
      * @param ttlPerSession
      *            the ttlPerSession to set
      */
     public void setTtlPerSession(boolean ttlPerSession)
     {
         this.ttlPerSession = ttlPerSession;
     }
 
     /**
      * @return the ttlValues
      */
     public Map<String, Object> getTtlValues()
     {
         return ttlValues;
     }
 
     /**
      * @param ttlValues
      *            the ttlValues to set
      */
     public void setTtlValues(Map<String, Object> ttlValues)
     {
         this.ttlValues = ttlValues;
     }
 
     /**
      * Finds a {@link List} of entities from database
      */
     public final List findByRowKeys(Class entityClass, List<String> relationNames, boolean isWrapReq,
             EntityMetadata metadata, Object... rowIds)
     {
         List entities = null;
 
         MetamodelImpl metaModel = (MetamodelImpl) KunderaMetadata.INSTANCE.getApplicationMetadata().getMetamodel(
                 metadata.getPersistenceUnit());
 
         EntityType entityType = metaModel.entity(metadata.getEntityClazz());
 
         List<AbstractManagedType> subManagedType = ((AbstractManagedType) entityType).getSubManagedType();
 
         try
         {
 
             if (!subManagedType.isEmpty())
             {
                 for (AbstractManagedType subEntity : subManagedType)
                 {
                     EntityMetadata subEntityMetadata = KunderaMetadataManager
                             .getEntityMetadata(subEntity.getJavaType());
                     entities = getDataHandler().fromThriftRow(entityClass, subEntityMetadata,
                             subEntityMetadata.getRelationNames(), isWrapReq, getConsistencyLevel(), rowIds);
 
                     if (entities != null && !entities.isEmpty())
                     {
                         break;
                     }
                 }
             }
             else
             {
                 entities = getDataHandler().fromThriftRow(entityClass, metadata, relationNames, isWrapReq,
                         getConsistencyLevel(), rowIds);
             }
         }
         catch (Exception e)
         {
             log.error("Error while retrieving records for entity {}, row keys {}", entityClass, rowIds);
             throw new KunderaException(e);
         }
         return entities;
     }
 
 }
