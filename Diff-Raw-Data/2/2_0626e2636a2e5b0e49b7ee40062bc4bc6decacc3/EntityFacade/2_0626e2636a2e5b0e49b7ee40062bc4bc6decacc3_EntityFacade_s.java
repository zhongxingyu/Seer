 package com.datastax.hectorjpa.store;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.DynamicComposite;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import org.apache.openjpa.kernel.OpenJPAStateManager;
 import org.apache.openjpa.meta.ClassMetaData;
 import org.apache.openjpa.meta.FieldMetaData;
 import org.apache.openjpa.meta.JavaTypes;
 import org.apache.openjpa.util.OpenJPAId;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.datastax.hectorjpa.index.AbstractIndexOperation;
 import com.datastax.hectorjpa.index.FieldOrder;
 import com.datastax.hectorjpa.index.IndexDefinition;
 import com.datastax.hectorjpa.index.IndexOperation;
 import com.datastax.hectorjpa.index.IndexOrder;
 import com.datastax.hectorjpa.index.SubclassIndexOperation;
 import com.datastax.hectorjpa.meta.BigDecimalColumnField;
 import com.datastax.hectorjpa.meta.DiscriminatorColumn;
 import com.datastax.hectorjpa.meta.MetaCache;
 import com.datastax.hectorjpa.meta.ObjectTypeColumnStrategy;
 import com.datastax.hectorjpa.meta.SimpleColumnField;
 import com.datastax.hectorjpa.meta.StaticColumn;
 import com.datastax.hectorjpa.meta.StringColumnField;
 import com.datastax.hectorjpa.meta.ToOneColumn;
 import com.datastax.hectorjpa.meta.collection.AbstractCollectionField;
 import com.datastax.hectorjpa.meta.collection.OrderedCollectionField;
 import com.datastax.hectorjpa.meta.collection.UnorderedCollectionField;
 import com.datastax.hectorjpa.meta.embed.EmbeddedCollectionColumnField;
 import com.datastax.hectorjpa.meta.embed.EmbeddedColumnField;
 import com.datastax.hectorjpa.meta.key.KeyStrategy;
 import com.datastax.hectorjpa.serialize.EmbeddedSerializer;
 import com.datastax.hectorjpa.service.IndexQueue;
 
 public class EntityFacade implements Serializable {
   private static final Logger log = LoggerFactory.getLogger(EntityFacade.class);
 
   private static final long serialVersionUID = 4777260639119126462L;
 
   private final String columnFamilyName;
   private final Class<?> clazz;
   private final ObjectTypeColumnStrategy strategy;
   private final KeyStrategy keyStrategy;
   private final Map<IndexDefinition, AbstractIndexOperation> indexOps;
 
   /**
    * Fields indexed by id
    */
   private final Map<Integer, StringColumnField> columnFieldIds;
   private final Map<Integer, AbstractCollectionField> collectionFieldIds;
 
   /**
    * Default constructor
    * 
    * @param classMetaData
    *          The class meta data
    * @param mappingUtils
    *          The mapping utils to use for byte mapping
    */
   public EntityFacade(ClassMetaData classMetaData, EmbeddedSerializer serializer) {
 
     CassandraClassMetaData cassMeta = (CassandraClassMetaData) classMetaData;
 
     this.columnFamilyName = MappingUtils.getColumnFamily(cassMeta);
 
     clazz = cassMeta.getDescribedType();
 
     columnFieldIds = new HashMap<Integer, StringColumnField>();
     collectionFieldIds = new HashMap<Integer, AbstractCollectionField>();
 
     FieldMetaData[] fmds = cassMeta.getFields();
     StringColumnField columnField = null;
 
     CassandraFieldMetaData field = null;
 
     // parse all columns, we only want to do this on the first inti
     for (int i = 0; i < fmds.length; i++) {
 
       field = (CassandraFieldMetaData) fmds[i];
 
       // not in the bit set to use, isn't managed or saved, or is a
       // primary key,
       // ignore
       if (field.getManagement() == FieldMetaData.MANAGE_NONE
           || field.isPrimaryKey()) {
         continue;
       }
 
       if (field.getAssociationType() == FieldMetaData.ONE_TO_MANY
           || field.getAssociationType() == FieldMetaData.MANY_TO_MANY) {
 
         AbstractCollectionField collection = null;
 
         if (field.getOrders().length > 0) {
           collection = new OrderedCollectionField(field);
         } else {
           collection = new UnorderedCollectionField(field);
         }
 
         // TODO if field.getAssociationType() > 0 .. we found an
         // attached
         // entity
         // and need to find it's entityFacade
         collectionFieldIds.put(collection.getFieldId(), collection);
 
         // indexMetaData.add(new ManyEntityIndex(field,
         // mappingUtils));
 
         continue;
       }
 
       if (field.getAssociationType() == FieldMetaData.MANY_TO_ONE
           || field.getAssociationType() == FieldMetaData.ONE_TO_ONE) {
 
         ToOneColumn toOne = new ToOneColumn(field);
 
         columnFieldIds.put(i, toOne);
 
         continue;
       }
 
       if (field.isEmbeddedEntity()) {
         EmbeddedColumnField embedded = new EmbeddedColumnField(field);
         columnFieldIds.put(embedded.getFieldId(), embedded);
         continue;
       }
 
       if (field.isEmbeddedCollectionEntity()) {
         EmbeddedCollectionColumnField embedded = new EmbeddedCollectionColumnField(
             field, serializer);
         columnFieldIds.put(embedded.getFieldId(), embedded);
         continue;
       }
 
       if (log.isDebugEnabled()) {
         log.debug(
             "field name {} typeCode {} associationType: {} declaredType: {} embeddedMetaData: {}",
             new Object[] { field.getName(), field.getTypeCode(),
                 field.getAssociationType(), field.getDeclaredType().getName(),
                 field.getElement().getDeclaredTypeMetaData() });
       }
 
       // some primitives require special persistence, detect the correct type
       switch (field.getDeclaredTypeCode()) {
 
       case JavaTypes.BIGDECIMAL:
         columnField = new BigDecimalColumnField(field);
         break;
 
       default:
         columnField = new SimpleColumnField(field);
       }
 
       // TODO if field.getAssociationType() > 0 .. we found an attached
       // entity
      // and need to find it's entityFacade
       columnFieldIds.put(columnField.getFieldId(), columnField);
 
     }
 
     String discriminator = cassMeta.getDiscriminatorColumn();
 
     if (discriminator != null || cassMeta.isAbstract()) {
       strategy = new DiscriminatorColumn(discriminator);
     } else {
       strategy = new StaticColumn();
     }
 
     // this class has index definitions. Retrieve them.
     if (cassMeta.getAllDefinitions() != null) {
       this.indexOps = new HashMap<IndexDefinition, AbstractIndexOperation>();
 
       for (IndexDefinition indexDef : cassMeta.getAllDefinitions()
           .getDefinitions()) {
 
         // construct an index with subclass queries
         if (cassMeta.getDiscriminatorColumn() != null) {
           indexOps
               .put(indexDef, new SubclassIndexOperation(cassMeta, indexDef));
         }
 
         // construct and index without discriminator for subclass
         // queries
         else {
           indexOps.put(indexDef, new IndexOperation(cassMeta, indexDef));
         }
       }
     } else {
       this.indexOps = null;
     }
 
     keyStrategy = MappingUtils.getKeyStrategy(cassMeta);
 
   }
 
   /**
    * Delete the entity with the given statemanager. The given clock time is used
    * for the delete of indexes
    * 
    * @param stateManager
    * @param mutator
    * @param clock
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void delete(OpenJPAStateManager stateManager, Mutator mutator,
       long clock, IndexQueue queue) {
 
     byte[] keyBytes = keyStrategy.toByteArray(stateManager.fetchObjectId());
 
     // queue up direct column deletes
     for (AbstractCollectionField field : collectionFieldIds.values()) {
       field.removeCollection(stateManager, mutator, clock, keyBytes);
     }
 
     mutator.addDeletion(keyBytes, columnFamilyName, null,
         StringSerializer.get());
 
     // queue all index removals
     for (AbstractIndexOperation current : indexOps.values()) {
       current.removeIndexes(stateManager, queue, clock);
     }
 
   }
 
   /**
    * Load all columns for this class specified in the bit set
    * 
    * @param stateManager
    * @param fieldSet
    * @return true if the entity was present (I.E the marker column was found)
    *         otherwise false is returned.
    */
   public boolean loadColumns(OpenJPAStateManager stateManager, BitSet fieldSet,
       Keyspace keyspace, MetaCache metaCache) {
 
     List<String> fields = new ArrayList<String>();
 
     StringColumnField field = null;
     AbstractCollectionField collectionField = null;
 
     byte[] key = keyStrategy.toByteArray(stateManager.fetchObjectId());
 
   
     // load all collections as we encounter them since they're seperate row
     // reads and construct columns for sliceQuery in primary CF
     for (int i = fieldSet.nextSetBit(0); i >= 0; i = fieldSet.nextSetBit(i + 1)) {
       field = columnFieldIds.get(i);
 
       if (field == null) {
 
         collectionField = collectionFieldIds.get(i);
 
         // nothting to do
         if (collectionField == null) {
           continue;
         }
 
         int size = stateManager.getContext().getFetchConfiguration()
             .getFetchBatchSize();
 
         // now query and load this field
         SliceQuery<byte[], DynamicComposite, byte[]> query = collectionField
             .createQuery(key, keyspace, size);
 
         collectionField.readField(stateManager, query.execute());
 
         continue;
       }
 
       field.addFieldNames(fields);
     }
 
     fields.add(this.strategy.getColumnName());
 
     // now load all the columns in the CF.
     SliceQuery<byte[], String, byte[]> query = MappingUtils.buildSliceQuery(
         key, fields, columnFamilyName, keyspace);
 
     QueryResult<ColumnSlice<String, byte[]>> result = query.execute();
 
     // read the field
     for (int i = fieldSet.nextSetBit(0); i >= 0; i = fieldSet.nextSetBit(i + 1)) {
       field = columnFieldIds.get(i);
 
       if (field == null) {
         continue;
       }
 
       field.readField(stateManager, result);
     }
 
     return result.get().getColumns().size() > 0;
 
   }
 
   /**
    * Loads only the jpa marker column to check for cassandra existence
    * 
    * @param stateManager
    * @param fieldSet
    * @return true if the entity was present (I.E the marker column was found)
    *         otherwise false is returned.
    */
   public boolean exists(OpenJPAStateManager stateManager, Keyspace keyspace,
       MetaCache metaCache) {
     return getStoredEntityType(stateManager, keyspace, metaCache) != null;
   }
 
   /**
    * Get the stored entity and it's object id if it exists in the datastore
    * 
    * @param stateManager
    * @param keyspace
    * @return
    */
   public Class<?> getStoredEntityType(OpenJPAStateManager sm,
       Keyspace keyspace, MetaCache metaCache) {
 
     Object oid = sm.fetchObjectId();
 
     Class<?> oidType = ((OpenJPAId) oid).getType();
 
     // This entity has never been persisted, we can't possibly load it
     if (oidType == null) {
       return null;
     }
 
     byte[] rowKey = keyStrategy.toByteArray(oid);
 
     if (rowKey == null) {
       return null;
     }
 
     String descrim = strategy.getStoredType(rowKey, columnFamilyName, keyspace);
 
     if (descrim == null) {
       return null;
     }
 
     return strategy.getClass(descrim, oidType, metaCache);
 
   }
 
   /**
    * Add the columns from the bit set to the mutator with the given time
    * 
    * @param stateManager
    * @param fieldSet
    * @param m
    * @param clockTime
    *          The time to use for write and deletes
    * @param audits
    *          A set to add all audit operations to for collection and index
    *          verification
    */
   public void addColumns(OpenJPAStateManager stateManager, BitSet fieldSet,
       Mutator<byte[]> m, long clockTime, IndexQueue queue) {
 
     byte[] keyBytes = keyStrategy.toByteArray(stateManager.fetchObjectId());
 
     for (int i = fieldSet.nextSetBit(0); i >= 0; i = fieldSet.nextSetBit(i + 1)) {
       StringColumnField field = columnFieldIds.get(i);
 
       if (field == null) {
 
         AbstractCollectionField collection = collectionFieldIds.get(i);
 
         // nothing to do
         if (collection == null) {
           continue;
         }
 
         // we have a collection, persist it
         collection.addField(stateManager, m, clockTime, keyBytes,
             this.columnFamilyName, queue);
 
         continue;
 
       }
 
       field.addField(stateManager, m, clockTime, keyBytes,
           this.columnFamilyName, queue);
 
     }
 
     // add our object type column strategy
     this.strategy.write(m, clockTime, keyBytes, columnFamilyName);
 
     // We have indexes, write them
     if (indexOps != null) {
       for (AbstractIndexOperation op : indexOps.values()) {
         op.writeIndex(stateManager, m, clockTime, queue);
       }
 
     }
 
   }
 
   /**
    * Return an indexOperation that matches this criteria
    * 
    * @param fields
    * @param orders
    * @return
    */
   public AbstractIndexOperation getIndexOperation(FieldOrder[] fields,
       IndexOrder[] orders) {
 
     if (indexOps == null) {
       return null;
     }
 
     IndexDefinition def = new IndexDefinition(fields, orders);
 
     return indexOps.get(def);
   }
 
   @Override
   public String toString() {
 
     return String.format(
         "EntityFacade[class: %s, columnFamily: %s, columnNames: %s]",
         clazz.getName(), columnFamilyName,
         Arrays.toString(columnFieldIds.entrySet().toArray()));
 
   }
 
 }
