 package org.kiji.ohm.dao;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 
 import com.google.common.base.Defaults;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;

 import org.apache.commons.lang.NotImplementedException;
 import org.apache.hadoop.hbase.HConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.kiji.ohm.annotations.EntityIdField;
 import org.kiji.ohm.annotations.KijiColumn;
 import org.kiji.ohm.annotations.KijiEntity;
 import org.kiji.schema.ColumnVersionIterator;
 import org.kiji.schema.EntityId;
 import org.kiji.schema.Kiji;
 import org.kiji.schema.KijiCell;
 import org.kiji.schema.KijiDataRequest;
 import org.kiji.schema.KijiDataRequestBuilder;
 import org.kiji.schema.KijiDataRequestBuilder.ColumnsDef;
 import org.kiji.schema.KijiRowData;
 import org.kiji.schema.KijiTable;
 import org.kiji.schema.KijiTableReader;
 import org.kiji.schema.KijiTableReader.KijiScannerOptions;
 import org.kiji.schema.avro.RowKeyComponent;
 import org.kiji.schema.avro.RowKeyFormat2;
 import org.kiji.schema.layout.KijiTableLayout;
 import org.kiji.schema.layout.KijiTableLayout.LocalityGroupLayout.FamilyLayout;
 import org.kiji.schema.layout.KijiTableLayout.LocalityGroupLayout.FamilyLayout.ColumnLayout;
 
 /**
  * Kiji Data Access Object (DAO).
  */
 public final class KijiDao implements Closeable {
   private static final Logger LOG = LoggerFactory.getLogger(KijiDao.class);
 
   /** Kiji instance. */
   private final Kiji mKiji;
 
   /** Map of entity specifications. */
   private final Map<Class<?>, EntitySpec<?>> mEntitySpec = Maps.newHashMap();
 
   /**
    * Initializes a new instance of Kiji Data Access Object.
    *
    * @param kiji Kiji instance to wrap.
    */
   public KijiDao(Kiji kiji) {
     mKiji = kiji;
 
     mKiji.retain();
   }
 
   /** {@inheritDoc} */
   @Override
   public void close() throws IOException {
     mKiji.release();
   }
 
   // -----------------------------------------------------------------------------------------------
 
   /**
    * Reports the specification for an entity class.
    *
    * @param klass Class of the entity to specify.
    * @return the specification for the given entity class.
    * @throws IOException on I/O error.
    */
   private <T> EntitySpec<T> getEntitySpec(Class<T> klass) throws IOException {
     synchronized(mEntitySpec) {
       @SuppressWarnings("unchecked")
       final EntitySpec<T> spec = (EntitySpec<T>) mEntitySpec.get(klass);
       if (spec != null) {
         return spec;
       }
       final EntitySpec<T> newSpec = new EntitySpec<T>(klass, mKiji);
       mEntitySpec.put(klass, newSpec);
       return newSpec;
     }
   }
 
   // -----------------------------------------------------------------------------------------------
 
   /**
    * Shortcut for {@link #select(Class, EntityId, long, long).
    *
    * @param klass
    * @param entityId
    * @return
    */
   public <T> T select(Class<T> klass, EntityId entityId) throws IOException {
     return select(
         klass, entityId,
         0 /*HConstants.OLDEST_TIMESTAMP*/, HConstants.LATEST_TIMESTAMP);
   }
 
   /**
    * Shortcut for {@link #selectAll(Class, KijiScannerOptions, long, long).
    *
    * @param klass
    * @param options
    * @return
    */
   public <T> Iterator<T> selectAll(Class<T> klass, KijiScannerOptions options) throws IOException {
     return selectAll(
         klass, options,
         0 /*HConstants.OLDEST_TIMESTAMP*/, HConstants.LATEST_TIMESTAMP);
   }
 
   /**
    * <p> Equivalent of a Kiji get request. </p>
    *
    * @param klass
    * @param entityId
    * @param startTime
    * @param endTime
    * @return
    */
   public <T> T select(Class<T> klass, EntityId entityId, long startTime, long endTime)
       throws IOException {
     final EntitySpec<T> spec = getEntitySpec(klass);
     final T entity = spec.newEntity();
     return populateFromRow(spec, entity, entityId, startTime, endTime);
   }
 
   public <T> T populateFromRow(T entity, EntityId entityId, long startTime, long endTime)
       throws IOException {
     @SuppressWarnings("unchecked")
     final Class<T> klass = (Class<T>) entity.getClass();
     final EntitySpec<T> spec = getEntitySpec(klass);
     return populateFromRow(spec, entity, entityId, startTime, endTime);
   }
 
   private <T> T populateFromRow(
       EntitySpec<T> spec, T entity, EntityId entityId, long startTime, long endTime)
       throws IOException {
 
     // TODO: Use a pool of tables and/or table readers
     final KijiTable table = mKiji.openTable(spec.getTableName());
     try {
       final KijiTableReader reader = table.openTableReader();
       try {
         final KijiDataRequestBuilder builder = KijiDataRequest.builder();
         builder.withTimeRange(startTime, endTime);
         spec.populateColumnRequests(builder);
         final KijiDataRequest dataRequest = builder.build();
 
         final KijiRowData row = reader.get(entityId, dataRequest);
 
         try {
           return spec.populateEntityFromRow(entity, row);
         } catch (IllegalAccessException iae) {
           throw new RuntimeException(iae);
         }
 
       } finally {
         reader.close();
       }
     } finally {
       table.release();
     }
   }
 
   /**
    * <p> Equivalent of a Kiji scan. </p>
    *
    * @param klass
    * @param options
    * @param startTime
    * @param endTime
    * @return
    */
   public <T> Iterator<T> selectAll(
       Class<T> klass,
       KijiScannerOptions options,
       long startTime,
       long endTime)
       throws IOException {
     throw new NotImplementedException();
   }
 
   // -----------------------------------------------------------------------------------------------
 
   /**
    * Specification of an annotated entity class.
    */
   private static final class EntitySpec<T> {
     private final Class<T> mClass;
 
     private final String mTableName;
 
     /** Fields populated from the row columns. */
     private final ImmutableList<Field> mColumnFields;
 
     /** Fields populated from the row entity ID components. */
     private final ImmutableList<Field> mEntityIdFields;
 
     /** Map from row key component name to row key component specs. */
     private final ImmutableMap<String, RowKeyComponent> mRowKeyComponentMap;
 
     /** Map from row key component name to row key component index. */
     private final ImmutableMap<String, Integer> mRowKeyComponentIndexMap;
 
     /**
      * Initializes a new specification for an Entity from an annotated Java class.
      *
      * @param klass Annotated Java class to derive an entity specification from.
      * @param kiji Kiji instance where to fetch entities from.
      * @throws IOException on I/O error.
      */
     public EntitySpec(Class<T> klass, Kiji kiji) throws IOException {
       mClass = klass;
 
       final KijiEntity entity = klass.getAnnotation(KijiEntity.class);
       Preconditions.checkArgument(entity != null,
           "Class '{}' has no @KijiEntity annotation.", klass);
       mTableName = entity.table();
 
       final KijiTable table = kiji.openTable(mTableName);
       try {
         final KijiTableLayout layout = table.getLayout();
 
         // TODO: Support deprecated RowKeyFormat?
         final RowKeyFormat2 rowKeyFormat = (RowKeyFormat2) layout.getDesc().getKeysFormat();
 
         final Map<String, RowKeyComponent> rkcMap = Maps.newHashMap();
         final Map<String, Integer> rkcIndexMap = Maps.newHashMap();
         for (int index = 0; index < rowKeyFormat.getComponents().size(); ++index) {
           final RowKeyComponent rkc = rowKeyFormat.getComponents().get(index);
           rkcMap.put(rkc.getName(), rkc);
           rkcIndexMap.put(rkc.getName(), index);
         }
         mRowKeyComponentMap = ImmutableMap.copyOf(rkcMap);
         mRowKeyComponentIndexMap = ImmutableMap.copyOf(rkcIndexMap);
 
         // --------------------------------------------------------------------
         // Parse fields with annotations from the entity class:
 
         final List<Field> columnFields = Lists.newArrayList();
         final List<Field> entityIdFields = Lists.newArrayList();
 
         for (final Field field : mClass.getDeclaredFields()) {
           final KijiColumn column = field.getAnnotation(KijiColumn.class);
           final EntityIdField eidField = field.getAnnotation(EntityIdField.class);
 
           if ((column != null) && (eidField != null)) {
             throw new IllegalArgumentException(String.format(
                 "Field '%s' cannot have both @KijiColumn and @EntityIdField annotations.", field));
 
           } else if (column != null) {
             LOG.debug("Validating column field '{}'.", field);
             field.setAccessible(true);
             columnFields.add(field);
 
             final FamilyLayout flayout = layout.getFamilyMap().get(column.family());
             Preconditions.checkArgument(flayout != null,
                 "Field '%s' maps to non-existing family '%s' from table '%s'.",
                 field, column.family(), mTableName);
 
             if (column.qualifier().isEmpty()) {
               // Request for a map-type family:
               Preconditions.checkArgument(flayout.isMapType(),
                   "Field '%s' maps to family '%s' from table '%s' which is not a map-type family.",
                   field, column.family(), mTableName);
 
               // TODO: Validate types
 
             } else {
               // Request for a fully-qualified column:
               final ColumnLayout clayout = flayout.getColumnMap().get(column.qualifier());
               Preconditions.checkArgument(flayout != null,
                   "Field '%s' maps to non-existing column '%s:%s' from table '%s'.",
                   field, column.family(), column.qualifier(), mTableName);
 
               // TODO: Validate types
             }
 
           } else if (eidField != null) {
             LOG.debug("Validating entity ID field '{}'.", field);
             field.setAccessible(true);
             entityIdFields.add(field);
 
             final RowKeyComponent rkc = mRowKeyComponentMap.get(eidField.component());
             Preconditions.checkArgument(rkc != null,
                 "Field '%s' maps to unknown entity ID component '%s'.",
                 field, eidField.component());
 
           } else {
             LOG.debug("Ignoring field '{}' with no annotation.", field);
           }
         }
 
         mColumnFields = ImmutableList.copyOf(columnFields);
         mEntityIdFields = ImmutableList.copyOf(entityIdFields);
 
       } finally {
         table.release();
       }
     }
 
     public String getTableName() {
       return mTableName;
     }
 
     /**
      * Populates a KijiDataRequest from this entity specification.
      *
      * @param builder Builder for the KijiDataRequest to populate.
      */
     public void populateColumnRequests(KijiDataRequestBuilder builder) {
       for (final Field field : mColumnFields) {
         final KijiColumn column = field.getAnnotation(KijiColumn.class);
         Preconditions.checkState(column != null);
 
         final ColumnsDef def = ColumnsDef.create()
             .withMaxVersions(column.maxVersions())
             .withPageSize(column.pageSize());
         if (column.qualifier().isEmpty()) {
           def.addFamily(column.family());
         } else {
           def.add(column.family(), column.qualifier());
         }
         builder.addColumns(def);
       }
     }
 
     /**
      * Populates an entity from a row.
      *
      * @param entity Entity object to populate from a row.
      * @param row Kiji row to populate the entity from.
      * @return the populated entity.
      * @throws IllegalAccessException
      * @throws IOException
      */
     public T populateEntityFromRow(T entity, KijiRowData row)
         throws IllegalAccessException, IOException {
 
       // Populate fields from the row columns:
       for (final Field field : mColumnFields) {
         final KijiColumn column = field.getAnnotation(KijiColumn.class);
         Preconditions.checkState(column != null);
 
         if (column.qualifier().isEmpty()) {
           // Field is populated from a map-type family:
           populateFieldFromMapTypeFamily(entity, field, column, row);
 
         } else {
           // Field is populated from a fully-qualified column:
           populateFieldFromFullyQualifiedColumn(entity, field, column, row);
         }
       }
 
       // Populate fields from the row entity ID:
       for (final Field field : mEntityIdFields) {
         final EntityIdField eidField = field.getAnnotation(EntityIdField.class);
         Preconditions.checkState(eidField != null);
 
         final int index = mRowKeyComponentIndexMap.get(eidField.component());
         field.set(entity, row.getEntityId().getComponentByIndex(index));
       }
       return entity;
     }
 
     private final void populateFieldFromFullyQualifiedColumn(
         T entity, Field field, KijiColumn column, KijiRowData row)
         throws IOException, IllegalAccessException {
 
       if (column.maxVersions() == 1) {
         // Field represents a single value from a fully-qualified column:
         LOG.debug("Populating field '{}' from column '{}:{}'.",
             field, column.family(), column.qualifier());
         Object value = row.getMostRecentValue(column.family(), column.qualifier());
         if (field.getType() == String.class && value != null) {
           // Automatically converts CharSequence to java String if necessary:
           value = value.toString();
         }
 
         // If there is no cell for a field with a primitive type, use the default value:
         if ((null == value) && field.getType().isPrimitive()) {
           value = Defaults.defaultValue(field.getType());
         }
         field.set(entity, value);
 
       } else {
         // Field represents a time-series from a fully-qualified column:
         if (column.pageSize() > 0) {
           final ColumnVersionIterator<?> iterator =
               new ColumnVersionIterator<T>(
                   row, column.family(), column.qualifier(), column.pageSize());
           field.set(entity, iterator);
         } else {
           final TimeSeries<Object> timeseries = new TimeSeries<Object>();
           for(final KijiCell<Object> cell : row.asIterable(column.family(), column.qualifier())) {
             timeseries.put(cell.getTimestamp(), cell.getData());
           }
           field.set(entity, timeseries);
         }
       }
     }
 
     private final void populateFieldFromMapTypeFamily(
         T entity, Field field, KijiColumn column, KijiRowData row)
         throws IOException, IllegalAccessException {
 
       LOG.debug("Populating field '{}' from map-type family '{}'.", field, column.family());
 
       final MapTypeValue<Object> mapValues = new MapTypeValue<Object>();
       if (column.maxVersions() == 1) {
         // Field is a map: qualifier -> single value:
 
         LOG.debug("Populating single version map field '{}'.", field);
 
         final NavigableMap<String, NavigableMap<Long, KijiCell<Object>>> cells =
             row.getCells(column.family());
         for (Entry<String, NavigableMap<Long, KijiCell<Object>>> entry : cells.entrySet()) {
           final String qualifier = entry.getKey();
           final NavigableMap<Long, KijiCell<Object>> cellSeries = entry.getValue();
 
           // Build a time-series with a single data point:
           final TimeSeries<Object> timeseries = new TimeSeries<Object>();
 
           for (Entry<Long, KijiCell<Object>> tsEntry : cellSeries.entrySet()) {
             // There should be only one iteration here, ie. one value:
             timeseries.put(tsEntry.getKey(), tsEntry.getValue().getData());
           }
           mapValues.put(qualifier, timeseries);
         }
         field.set(entity, mapValues);
 
       } else {
         // Field is a map: qualifier -> time-series
         // TODO: Let's find a way to decorate the underlying NavigableMap instead of iterating
         // over it to construct this MapTypeValue.
         final NavigableMap<String, NavigableMap<Long, Object>> cells =
             row.getValues(column.family());
         for (Entry<String, NavigableMap<Long, Object>> entry : cells.entrySet()) {
           final String qualifier = entry.getKey();
           final NavigableMap<Long, Object> tCells = entry.getValue();
           final TimeSeries<Object> timeseries = TimeSeries.fromMap(tCells);
           mapValues.put(qualifier, timeseries);
         }
         LOG.debug("Populating map field '{}'.", field);
         field.set(entity, mapValues);
       }
 
     }
 
     /**
      * Creates a new blank entity instance.
      *
      * @return a new blank entity instance.
      */
     private T newEntity() {
       try {
         return mClass.newInstance();
       } catch (InstantiationException ie) {
         throw new RuntimeException(ie);
       } catch (IllegalAccessException iae) {
         throw new RuntimeException(iae);
       }
     }
   }
 }
