 package me.smecsia.cassajem;
 
 import me.prettyprint.cassandra.model.BasicColumnDefinition;
 import me.prettyprint.cassandra.model.IndexedSlicesQuery;
 import me.prettyprint.cassandra.service.ThriftCfDef;
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.*;
 import me.prettyprint.hector.api.ddl.*;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.MultigetSliceQuery;
 import me.prettyprint.hector.api.query.MultigetSuperSliceQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.smecsia.cassajem.api.BasicEntity;
 import me.smecsia.cassajem.api.BasicService;
 import me.smecsia.cassajem.api.CompositeKey;
 import me.smecsia.cassajem.api.Conditions;
 import me.smecsia.cassajem.meta.*;
 import me.smecsia.cassajem.meta.annotations.ColumnFamily;
 import me.smecsia.cassajem.service.Connection;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import static me.smecsia.cassajem.util.TypesUtil.*;
 
 /**
  * Entity manager implementation class that simplifies the entity management via Hector
  *
  * @param <T> - Class of the managed entity
  *            <p/>
  *            Date: 18.01.12 16:28
  */
 @SuppressWarnings({"NullArgumentToVariableArgMethod", "ConstantConditions", "SuspiciousMethodCalls"})
 public class EntityManagerImpl<T extends BasicEntity> extends BasicService implements EntityManager<T> {
 
     public static final int maximumColumnsPerQuery = 100000;
     public static final int maximumRowsPerQuery = 100000;
 
     private Logger logger = LoggerFactory.getLogger(getClass());
 
     protected Connection connection = null;
 
     private Class<T> entityClass = null;
     private Metadata<T> entityMetadata = null;
 
     @Override
     public Connection getConnection() {
         return connection;
     }
 
     public EntityManagerImpl(Connection connection, Class<T> entityClass) {
         this.entityClass = entityClass;
         this.connection = connection;
         readMetadata();
     }
 
     private void readMetadata() {
         entityMetadata = MetaCache.instance().forClass(getEntityClass());
     }
 
     /**
      * Returns the metadata of the provided entity class
      *
      * @return entityMetadata
      */
     public Metadata<T> getEntityMetadata() {
         return entityMetadata;
     }
 
     /**
      * Creates the column family by the provided entity class metadata
      */
     @Override
     public void initColumnFamily() {
         if (connection.getCluster() != null) {
             String cfName = getEntityMetadata().getEntityInfo().getCfName();
             String keyspaceName = connection.getKeyspace().getKeyspaceName();
             logger.debug("Checking column family '" + cfName + "' existance in keyspace '" + keyspaceName
                     + "'");
             KeyspaceDefinition keyspaceDef = connection.getCluster().describeKeyspace(keyspaceName);
             if (keyspaceDef != null) {
                 boolean cfExists = false;
                 for (ColumnFamilyDefinition def : keyspaceDef.getCfDefs()) {
                     cfExists = cfExists || def.getName().equals(cfName);
                 }
                 if (!cfExists) {
                     createColumnFamily(keyspaceName, cfName, getEntityMetadata().getEntityInfo()
                             .getColumnType());
                 }
             }
         }
     }
 
     /**
      * Creates the column family by the provided type, name in the given keyspace
      *
      * @param keyspaceName - name of the keyspace
      * @param name         - name of the columnn family
      * @param type         - type of the columns (SUPER/STANDARD)
      */
     @SuppressWarnings("unchecked")
     protected synchronized void createColumnFamily(String keyspaceName, String name, ColumnType type) {
         Metadata md = getEntityMetadata();
         List<ColumnDefinition> list = new ArrayList<ColumnDefinition>();
         logger.info("Creating new column family '" + name + "' in keyspace '" + keyspaceName + "'");
         if (type == ColumnType.STANDARD && !md.getEntityInfo().usesComposites()) {
             for (Map.Entry<String, ColumnInfo> cInfo : getEntityMetadata().getAllColumns().entrySet()) {
                 BasicColumnDefinition colDef = new BasicColumnDefinition();
                 colDef.setName(cInfo.getValue().getKeySerializer().toByteBuffer(cInfo.getValue().getFieldName()));
                 colDef.setIndexType(ColumnIndexType.KEYS);
                 colDef.setValidationClass(getEntityMetadata().getDefaultValidationClassName());
                 colDef.setIndexName(md.getEntityInfo().getCfName() + "_" + cInfo.getValue().getFieldName());
                 list.add(colDef);
             }
         }
         ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(keyspaceName, name);
         ((ThriftCfDef) cfDef).setColumnMetadata(list);
         cfDef.setColumnType(type);
         cfDef.setKeyValidationClass(getEntityMetadata().getKeyValidationClass().getName());
         cfDef.setDefaultValidationClass(getEntityMetadata().getDefaultValidationClassName());
         cfDef.setComparatorType(getEntityMetadata().getComparatorType());
         cfDef.setComparatorTypeAlias(getEntityMetadata().getComparatorTypeAlias());
         if (type == ColumnType.SUPER) {
             cfDef.setSubComparatorType(ComparatorType.UTF8TYPE);
         }
         connection.getCluster().addColumnFamily(cfDef);
     }
 
     /**
      * Returns the class of the managed entity
      *
      * @return entity class
      */
     protected Class<T> getEntityClass() {
         return this.entityClass;
     }
 
     /**
      * Creates HColumn for the given name, value and their serializers
      *
      * @param name            - column name
      * @param value           - column value
      * @param keySerializer   - name serializer
      * @param valueSerializer - value serializer
      * @return HColumn
      */
     @SuppressWarnings("unchecked")
     protected HColumn<Object, Object> createColumn(Object name, Object value, Serializer keySerializer,
                                                    Serializer valueSerializer) {
         return HFactory.createColumn(name, value, keySerializer, valueSerializer);
     }
 
     /**
      * Creates the HColumn by ColumnInfo
      *
      * @param cInfo - metadata of the column
      * @param name  - column name
      * @param value - column value
      * @return HColumn
      */
     @SuppressWarnings("unchecked")
     protected HColumn<Object, Object> createColumn(ColumnInfo cInfo, Object name, Object value) {
         Serializer keySerializer = cInfo.getKeySerializer();
         Serializer valueSerializer = cInfo.getValueSerializer();
         // it is composite
         if (cInfo.isComposite()) {
             keySerializer = compositeSerializer;
             name = constructCompositeColumnName((String) name);
         }
         return createColumn(name, value, keySerializer, valueSerializer);
     }
 
     /**
      * Constructs composite column name from the common string column name by adding the composite default values
      *
      * @param columnName - Common column name
      * @return composite key
      */
     protected CompositeKey constructCompositeColumnName(String columnName) {
         Metadata<T> md = getEntityMetadata();
         if (!md.getEntityInfo().usesComposites()) {
             logAndThrow("This entity (" + md.getEntityInfo().getCfName() + ") does not use composite keys!");
         }
         Class[] cTypes = md.getEntityInfo().compositeColumnTypes();
         Object[] values = new Object[cTypes.length];
        for (int i = 0; i < cTypes.length ; ++i) {
             values[i] = voidValue(cTypes[i]);
         }
        // first value is a columnName
        // TODO: this may be incorrect in the case when first type is not a String
        values[0] = columnName;
        return compositeKey(cTypes, values);
     }
 
     /**
      * Creates the columns list for the given entity
      *
      * @param object  - entity instance
      * @param columns - list of columns metadata
      * @return List of HColumn
      */
     @SuppressWarnings("unchecked")
     protected List<HColumn<Object, Object>> createColumns(BasicEntity object, Map<String, ColumnInfo> columns) {
         List<HColumn<Object, Object>> res = new ArrayList<HColumn<Object, Object>>();
         for (Map.Entry<String, ColumnInfo> cInfo : columns.entrySet()) {
             Object value = cInfo.getValue().invokeGetter(object);
             Object name = cInfo.getValue().getFieldName();
             if (value != null) {
                 res.add(createColumn(cInfo.getValue(), name, value));
             }
         }
         return res;
     }
 
     /**
      * Creates the super columns by super column metadata and given entity
      *
      * @param scValue - entity instance
      * @param scInfo  - super column metadata
      * @return Super column
      */
     @SuppressWarnings("unchecked")
     protected HSuperColumn<String, Object, Object> createSuperColumn(BasicEntity scValue,
                                                                      SuperColumnInfo scInfo) {
         return createSuperColumn(scValue, scInfo, scInfo.getFieldName());
     }
 
     /**
      * Creates the columns list for the given super column by its metadata
      *
      * @param scValue - entity instance
      * @param scInfo  - super column metadata
      * @return List of columns
      */
     @SuppressWarnings("unchecked")
     protected List<HColumn<Object, Object>> createColumnsForSuperColumn(BasicEntity scValue,
                                                                         SuperColumnInfo scInfo) {
         List<HColumn<Object, Object>> allColumns = new ArrayList<HColumn<Object, Object>>();
         allColumns.addAll(createColumns(scValue, scInfo.getColumnMetaData().getAllColumns()));
         if (scInfo.getColumnMetaData().hasDataStorageColumn()) {
             SuperColumnInfo dsColumn = scInfo.getColumnMetaData().getEntityInfo().getDataStorageSuperColumn();
             BasicEntity subColumn = (BasicEntity) dsColumn.invokeGetter(scValue);
             if (subColumn != null) {
                 allColumns.addAll(createColumns(subColumn, dsColumn.getColumnMetaData().getAllColumns()));
             }
         }
         return allColumns;
     }
 
     /**
      * Creates super column by its value, column metadata and the super column name
      *
      * @param scValue - entity instance
      * @param scInfo  - value of the super column
      * @param name    - name of the super column
      * @return HSuperColumn
      */
     @SuppressWarnings("unchecked")
     protected HSuperColumn<String, Object, Object> createSuperColumn(BasicEntity scValue,
                                                                      SuperColumnInfo scInfo, String name) {
         return HFactory.<String, Object, Object>createSuperColumn(name,
                 createColumnsForSuperColumn(scValue, scInfo), scInfo.getKeySerializer(),
                 scInfo.getValueSerializer(), scInfo.getValueSerializer());
     }
 
     /**
      * Creates the list of the super columns
      *
      * @param object       - entity instance
      * @param superColumns - super columns metadata map
      * @return List of HSuperColumn
      */
     @SuppressWarnings("unchecked")
     protected List<HSuperColumn> createSuperColumns(T object, Map<String, SuperColumnInfo> superColumns) {
         List<HSuperColumn> res = new ArrayList<HSuperColumn>();
         for (Map.Entry<String, SuperColumnInfo> scInfo : superColumns.entrySet()) {
             BasicEntity scValue = (BasicEntity) getEntityMetadata().getSuperColumns().get(scInfo.getKey())
                     .invokeGetter(object);
             if (scValue != null) {
                 res.add(createSuperColumn(scValue, scInfo.getValue()));
             }
         }
         return res;
     }
 
     /**
      * Creates the arrays of the super columns
      *
      * @param object       - entity instance
      * @param columnArrays - super column arrays metadata map
      * @return List of HSuperColumn
      */
     @SuppressWarnings("unchecked")
     protected List<HSuperColumn> createSuperColumnArrays(T object,
                                                          Map<String, SuperColumnArrayInfo> columnArrays) {
         List<HSuperColumn> res = new ArrayList<HSuperColumn>();
         for (Map.Entry<String, SuperColumnArrayInfo> scaInfo : columnArrays.entrySet()) {
             List<BasicEntity> scaValue = (List<BasicEntity>) getEntityMetadata().getSuperColumnArrays()
                     .get(scaInfo.getKey()).invokeGetter(object);
 
             for (int i = 0; i < scaValue.size(); ++i) {
                 BasicEntity scEntity = scaValue.get(i);
                 String columnName = scaInfo.getValue().getFieldName() + "#" + i;
 
                 res.add(HFactory.<String, Object, Object>createSuperColumn(columnName,
                         createColumnsForSuperColumn(scEntity, scaInfo.getValue()), scaInfo.getValue()
                         .getKeySerializer(), scaInfo.getValue().getValueSerializer(), scaInfo
                         .getValue().getValueSerializer()));
             }
 
         }
         return res;
     }
 
     /**
      * Creates the list of HSuperColumn by the entity instance and a map of column array metadata
      *
      * @param object       - entity instance
      * @param columnArrays - column array metadata
      * @return List of HSuperColumn
      */
     @SuppressWarnings("unchecked")
     protected List<HSuperColumn> createColumnArrays(T object, Map<String, ColumnArrayInfo> columnArrays) {
         List<HSuperColumn> res = new ArrayList<HSuperColumn>();
         for (Map.Entry<String, ColumnArrayInfo> scaInfo : columnArrays.entrySet()) {
             Map scaValue = (Map) getEntityMetadata().getColumnArrays().get(scaInfo.getKey())
                     .invokeGetter(object);
             List<HColumn<Object, Object>> columns = new ArrayList<HColumn<Object, Object>>();
 
             if (scaValue != null) {
                 for (Object scaKey : scaValue.keySet()) {
                     if (scaValue.get(scaKey) != null) {
                         HColumn column = createColumn(scaKey, scaValue.get(scaKey), scaInfo.getValue()
                                 .getKeySerializer(), scaInfo.getValue().getValueSerializer());
                         columns.add(column);
                     }
                 }
             }
             res.add(HFactory.<String, Object, Object>createSuperColumn(scaInfo.getValue().getFieldName(),
                     columns, scaInfo.getValue().getKeySerializer(), scaInfo.getValue().getValueSerializer(),
                     scaInfo.getValue().getValueSerializer()));
         }
         return res;
     }
 
     /**
      * Creates the composite array of columns
      *
      * @param object          - entity instance
      * @param compositeArrays - map of composite arrays metadata
      * @return List of HColumn
      */
     @SuppressWarnings("unchecked")
     protected List<HColumn> createCompositeArraysColumns(T object,
                                                          Map<String, CompositeColumnArrayInfo> compositeArrays) {
         List<HColumn> res = new ArrayList<HColumn>();
         for (Map.Entry<String, CompositeColumnArrayInfo> scaInfo : compositeArrays.entrySet()) {
             Map<Composite, Object> scaValue = (Map) getEntityMetadata().getCompositeArrays()
                     .get(scaInfo.getKey()).invokeGetter(object);
             for (Map.Entry<Composite, Object> scaColumn : scaValue.entrySet()) {
                 res.add(createColumn(scaColumn.getKey(), scaColumn.getValue(), scaInfo.getValue()
                         .getKeySerializer(), scaInfo.getValue().getValueSerializer()));
             }
         }
         return res;
     }
 
     /**
      * Returns the deserialized value of a column
      *
      * @param cInfo  - column metadata
      * @param column - HColumn
      * @return deserialized value
      */
     protected Object processColumnValue(ColumnInfo cInfo, HColumn<Object, Object> column) {
         Object result = null;
         if (column != null && column.getValue() != null) {
             result = cInfo.getValueSerializer().fromByteBuffer((ByteBuffer) column.getValue());
         }
         return result;
     }
 
     /**
      * Sets the values of the columns for an entity instance
      *
      * @param md       - Metadata of the processing column type
      * @param instance - entity instance
      * @param columns  - list of HColumn
      */
     @SuppressWarnings("unchecked")
     protected void processColumnsList(Metadata<T> md, BasicEntity instance,
                                       List<HColumn<Object, Object>> columns) {
         for (HColumn<Object, Object> column : columns) {
 
             Object columnNameValue = md.getNameSerializer().fromByteBuffer(column.getNameBytes());
             if (md.getEntityInfo() != null && md.getEntityInfo().usesComposites()) {
                 Composite columnName = (Composite) columnNameValue;
                 String compositePrefix = stringSerializer.fromByteBuffer((ByteBuffer) columnName.get(0));
 
                 if (columnName.size() - 1 != md.getEntityInfo().compositeColumnTypes().length) {
                     logAndThrow("Cannot read composite column value for '" + compositePrefix + "'");
                 }
 
                 if (md.getAllColumns().containsKey(compositePrefix)) {
                     ColumnInfo cInfo = md.getAllColumns().get(compositePrefix);
                     cInfo.invokeSetter(instance, processColumnValue(cInfo, column));
 
                 } else if (md.hasCompositeArray(compositePrefix)) {
                     String firstPrefix = compositePrefix.split("#")[0];
                     CompositeColumnArrayInfo compInfo = md.getCompositeArrays().get(firstPrefix);
                     Object[] values = extractValuesFromComposite(md, columnName, compositePrefix);
                     CompositeKey key = compInfo.create((Class[]) ArrayUtils.addAll(new Class[]{String.class},
                             md.getEntityInfo().compositeColumnTypes()), values);
                     ((Map) compInfo.invokeGetter(instance)).put(key, processColumnValue(compInfo, column));
                 } else if (md.getDynamicColumnsStorage() != null) {
                     Object value = processColumnValue(md.getDynamicColumnsStorage(), column);
                     ((Map) md.getDynamicColumnsStorage().invokeGetter(instance)).put(compositePrefix, value);
                 }
             } else {
                 String columnName = "";
                 try {
                     columnName = (String) columnNameValue;
                 } catch (Exception e) {
                     logAndThrow("Persistence error: cannot convert column name to a string for CF '"
                             + md.getEntityInfo().getCfName() + "'! Please research!");
                 }
                 if (md.getAllColumns().containsKey(columnName)) {
                     ColumnInfo cInfo = md.getAllColumns().get(columnName);
                     cInfo.invokeSetter(instance, processColumnValue(cInfo, column));
                 }
             }
 
         }
     }
 
     private Object[] extractValuesFromComposite(Metadata<T> md, Composite columnName, String compositePrefix) {
         Object[] values = new Object[columnName.size()];
         values[0] = compositePrefix;
         for (int i = 1; i < columnName.size(); ++i) {
             values[i] = md.getEntityInfo().compositeSerializers()[i - 1]
                     .fromByteBuffer((ByteBuffer) columnName.get(i));
         }
         return values;
     }
 
     /**
      * Processes basic slice and retrieves the entity data from it
      *
      * @param columnSlice - column slice
      * @return entity instance
      */
     @SuppressWarnings("unchecked")
     protected BasicEntity processColumnSlice(ColumnSlice<Object, Object> columnSlice) {
         BasicEntity instance = null;
         Metadata<T> md = getEntityMetadata();
         try {
             if (columnSlice.getColumns().size() > 0) {
                 instance = getEntityClass().newInstance();
                 if (md.getDynamicColumnsStorage() != null) {
                     md.getDynamicColumnsStorage().invokeSetter(instance, new LinkedHashMap());
                 }
                 processColumnsList(md, instance, columnSlice.getColumns());
             }
         } catch (Exception e) {
             logAndThrow("Cannot process column slice: ", e);
         }
         return instance;
     }
 
 
     /**
      * Process the rows list and return the processed instances list
      *
      * @param rows rows to be processed
      * @return list of rows
      */
     @SuppressWarnings("unchecked")
     protected List<T> processRows(QueryResult rows) {
         List<T> resultList = new ArrayList<T>();
         for (Row row : (Rows<Object, Object, Object>)rows.get()) {
             if (row.getColumnSlice().getColumns().size() > 0) {
                 T newInstance = (T) processColumnSlice(row.getColumnSlice());
                 getEntityMetadata().getIdColumn().invokeSetter(newInstance, row.getKey());
                 resultList.add(newInstance);
             }
         }
         return resultList;
     }
 
     /**
      * Processes the super slice and retrieves the entity data from it
      *
      * @param superSlice - super slice
      * @return entity instance
      */
     @SuppressWarnings({"all", "unchecked", "suspicious method call"})
     protected BasicEntity processSuperSlice(SuperSlice<Object, Object, Object> superSlice) {
         BasicEntity instance = null;
         Metadata<T> md = getEntityMetadata();
         try {
             instance = getEntityClass().newInstance();
 
             if (md.getDynamicSuperColumnsStorage() != null) {
                 if (md.getDynamicSuperColumnsStorage().invokeGetter(instance) == null) {
                     md.getDynamicSuperColumnsStorage().invokeSetter(instance,
                             new LinkedHashMap<Object, Object>());
                 }
             }
 
             // get super column arrays
             for (Map.Entry<String, SuperColumnArrayInfo> scInfo : md.getSuperColumnArrays().entrySet()) {
                 List<BasicEntity> arraySubInstance = new ArrayList<BasicEntity>();
                 scInfo.getValue().invokeSetter(instance, arraySubInstance);
                 int count = 0;
                 HSuperColumn sColumn;
                 do {
                     String sColumnName = scInfo.getValue().getFieldName() + "#" + count;
                     sColumn = superSlice.getColumnByName(sColumnName);
                     if (sColumn != null) {
                         BasicEntity subInstance = (BasicEntity) scInfo.getValue().getColumnMetaData()
                                 .getEntityClass().newInstance();
                         Map<String, ColumnInfo> columns = scInfo.getValue().getColumnMetaData()
                                 .getAllColumns();
                         for (Map.Entry<String, ColumnInfo> cInfo : columns.entrySet()) {
                             cInfo.getValue().invokeSetter(
                                     subInstance,
                                     processColumnValue(cInfo.getValue(),
                                             sColumn.getSubColumnByName(cInfo.getValue().getFieldName())));
                         }
                         arraySubInstance.add(subInstance);
                     }
                     count += 1;
                 } while (sColumn != null);
             }
 
             // get all other super columns
             for (HSuperColumn<Object, Object, Object> superColumn : superSlice.getSuperColumns()) {
                 if (md.getSuperColumns().containsKey(superColumn.getName())) {
                     SuperColumnInfo scInfo = md.getSuperColumns().get(superColumn.getName());
                     BasicEntity subInstance = (BasicEntity) scInfo.getType().newInstance();
                     processColumnsList(scInfo.getColumnMetaData(), subInstance, superColumn.getColumns());
                     scInfo.invokeSetter(instance, subInstance);
                 } else if (md.getColumnArrays().containsKey(superColumn.getName())) {
                     ColumnArrayInfo scInfo = md.getColumnArrays().get(superColumn.getName());
                     Map subInstance = new LinkedHashMap();
                     scInfo.invokeSetter(instance, subInstance);
                     HSuperColumn sColumn = superSlice.getColumnByName(scInfo.getFieldName());
                     List<HColumn> columns = sColumn.getColumns();
                     for (HColumn column : columns) {
                         subInstance.put(column.getName(), processColumnValue(scInfo, column));
                     }
                 } else if (md.getDynamicSuperColumnsStorage() != null
                         && superColumn.getName().toString().indexOf('#') == -1) {
                     SuperColumnInfo scInfo = md.getDynamicSuperColumnsStorage();
                     BasicEntity subInstance = (BasicEntity) scInfo.getColumnMetaData().getEntityClass()
                             .newInstance();
                     processColumnsList(scInfo.getColumnMetaData(), subInstance, superColumn.getColumns());
                     ((Map) md.getDynamicSuperColumnsStorage().invokeGetter(instance)).put(
                             superColumn.getName(), subInstance);
                 }
             }
 
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         return instance;
     }
 
     /**
      * Finds an entity by columns range and row key
      *
      * @param from - column names range start
      * @param to   - colum names range end
      * @param id   - row key value
      * @return found instance of an object
      */
     @Override
     public T find(Object from, Object to, Object id) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with rowKey='"
                 + id + "' and columns range '" + from + "' - '" + to + "'");
         List<T> res = list(new Object[]{id}, from, to);
         if (res.size() == 1) {
             return res.get(0);
         }
         return null;
     }
 
     /**
      * Find an entity by row key
      *
      * @param id - row key value
      * @return found instance of an object
      */
     @Override
     public T find(Object id) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with rowKey='"
                 + id + "'");
         List<T> res = list(id);
         if (res.size() == 1) {
             return res.get(0);
         }
         return null;
     }
 
     @Override
     public T find(Object id, Object[] columnNames) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with rowKey='"
                 + id + "', select only columns: [" + StringUtils.join(columnNames, ",") + "'");
         return find(id);
     }
 
     /**
      * Save an entity using default save policy
      *
      * @param object - entity instance
      */
     @Override
     public void save(T object) {
         save(object, getEntityMetadata().getEntityInfo().getSavePolicy());
     }
 
     /**
      * Partial entity save, using only the certain fields
      *
      * @param object            - entity instance
      * @param includeFieldNames use only these fields for the columns
      */
     @Override
     public void save(T object, String... includeFieldNames) {
         save(object, ColumnFamily.SavePolicy.APPEND, includeFieldNames);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void save(T object, ColumnFamily.SavePolicy savePolicy) {
         save(object, savePolicy, null);
     }
 
     /**
      * Set save policy to APPEND if you want to perform a partial save, row will be fully replaced otherwise
      *
      * @param object            entity instance
      * @param savePolicy        defines the policy of mutation (REWRITE or APPEND)
      * @param includeFieldNames include only these fields when save
      */
     @SuppressWarnings({"unchecked", "suspicious"})
     public void save(T object, ColumnFamily.SavePolicy savePolicy, String... includeFieldNames) {
         if (!connection.isConnected()) {
             logAndThrow("Not connected to a cluster!");
         }
 
         // fill object
         Metadata md = getEntityMetadata();
         Serializer serializer = getEntityMetadata().getIdColumn().getValueSerializer();
         Mutator mutator = HFactory.createMutator(connection.getKeyspace(), serializer);
 
         Object idValue = getEntityMetadata().getIdColumn().invokeGetter(object);
         String cfName = getEntityMetadata().getEntityInfo().getCfName();
 
         logger.debug("Saving new row into '" + getEntityMetadata().getEntityInfo().getCfName()
                 + "' with rowKey='" + idValue + "'");
 
         // this is not a partial update, remove all columns before writing if necessary
         if (savePolicy == ColumnFamily.SavePolicy.REWRITE) {
             mutator.addDeletion(idValue, cfName, null, byteBufferSerializer);
         }
 
         if (idValue == null) {
             logAndThrow("ID for columnFamily " + cfName + " cannot be null!");
         }
 
         switch (md.getEntityInfo().getColumnType()) {
             case SUPER:
                 for (HSuperColumn column : createSuperColumns(object, getEntityMetadata().getSuperColumns())) {
                     mutator.addInsertion(idValue, cfName, column);
                 }
                 for (HSuperColumn column : createSuperColumnArrays(object, getEntityMetadata()
                         .getSuperColumnArrays())) {
                     mutator.addInsertion(idValue, cfName, column);
                 }
                 for (HSuperColumn column : createColumnArrays(object, getEntityMetadata().getColumnArrays())) {
                     mutator.addInsertion(idValue, cfName, column);
                 }
                 if (md.getDynamicSuperColumnsStorage() != null) {
                     for (Map.Entry<Object, BasicEntity> subInstance : ((Map<Object, BasicEntity>) md
                             .getDynamicSuperColumnsStorage().invokeGetter(object)).entrySet()) {
                         mutator.addInsertion(
                                 idValue,
                                 cfName,
                                 createSuperColumn(subInstance.getValue(), md.getDynamicSuperColumnsStorage(),
                                         subInstance.getKey().toString()));
                     }
                 }
                 break;
             default:
                 for (HColumn column : createColumns(object,
                         getEntityMetadata().getAllColumns(includeFieldNames))) {
                     mutator.addInsertion(idValue, cfName, column);
                 }
                 for (HColumn column : createCompositeArraysColumns(object, getEntityMetadata()
                         .getCompositeArrays(includeFieldNames))) {
                     mutator.addInsertion(idValue, cfName, column);
                 }
                 if (md.getDynamicColumnsStorage() != null) {
                     ColumnInfo dsInfo = md.getDynamicColumnsStorage();
                     for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) dsInfo.invokeGetter(object))
                             .entrySet()) {
                         if (entry.getValue() != null) {
                             mutator.addInsertion(idValue, cfName,
                                     createColumn(dsInfo, entry.getKey(), entry.getValue()));
                         }
                     }
                 }
                 break;
         }
         mutator.execute();
     }
 
     @Override
     public List<T> list(Object... ids) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with rowKeys='"
                 + StringUtils.join(ids, ",") + "'");
         return list(ids, null, null, maximumRowsPerQuery, maximumColumnsPerQuery);
     }
 
     @Override
     public List<T> list(Object[] ids, Object from, Object to) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with rowKeys='"
                 + StringUtils.join(ids, "', '") + "' and range '" + from + "' - '" + to + "'");
         return list(ids, from, to, maximumRowsPerQuery, maximumColumnsPerQuery);
     }
 
     @SuppressWarnings("unchecked")
     public List<T> list(Object[] ids, Object from, Object to, int rowLimit, int colLimit) {
         List<T> resultList = new ArrayList<T>();
         if (ids.length < 1) {
             return resultList;
         }
         Metadata<T> md = getEntityMetadata();
         switch (md.getEntityInfo().getColumnType()) {
             case SUPER:
                 MultigetSuperSliceQuery<Object, Object, Object, Object> multigetSuperSliceQuery = HFactory
                         .createMultigetSuperSliceQuery(connection.getKeyspace(), md.getIdColumn()
                                 .getValueSerializer(), stringSerializer, md.getNameSerializer(),
                                 byteBufferSerializer);
                 multigetSuperSliceQuery.setColumnFamily(md.getEntityInfo().getCfName()).setKeys(ids)
                         .setRange(from, to, false, colLimit);
 
                 QueryResult<SuperRows<Object, Object, Object, Object>> ssResult = multigetSuperSliceQuery
                         .execute();
                 for (SuperRow<Object, Object, Object, Object> row : ssResult.get()) {
                     if (row.getSuperSlice().getSuperColumns().size() > 0) {
                         resultList.add((T) processSuperSlice(row.getSuperSlice()));
                     }
                 }
                 break;
             default:
                 MultigetSliceQuery<Object, Object, Object> multigetSliceQuery = HFactory
                         .createMultigetSliceQuery(connection.getKeyspace(), md.getIdColumn()
                                 .getValueSerializer(), md.getNameSerializer(), byteBufferSerializer);
 
                 multigetSliceQuery.setColumnFamily(md.getEntityInfo().getCfName()).setKeys(ids)
                         .setRange(from, to, false, colLimit);
 
                 QueryResult<Rows<Object, Object, Object>> cResult = multigetSliceQuery.execute();
                 resultList = processRows((QueryResult) cResult);
                 break;
         }
         return resultList;
 
     }
 
     @Override
     public List<T> filter(Conditions conditions) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName()
                 + "' by conditions='" + conditions + "'");
         return filter(null, null, conditions, maximumColumnsPerQuery);
     }
 
     @Override
     public List<T> filter(Object from, Object to, Conditions conditions) {
         logger.debug("Searching for '" + getEntityMetadata().getEntityInfo().getCfName() + "' with range = '"
                 + from + "' - '" + to + "' by conditions='" + conditions + "'");
         return filter(from, to, conditions, maximumColumnsPerQuery);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<T> filter(Object from, Object to, Conditions conditions, int colLimit) {
         List<T> resultList = new ArrayList<T>();
         Metadata<T> md = getEntityMetadata();
         if (md.getEntityInfo().usesComposites())
             logAndThrow("Filtering using composites is not implemented!");
 
         switch (md.getEntityInfo().getColumnType()) {
             case SUPER:
                 logAndThrow("Filtering for super column families is not yet implemented!");
             default:
                 IndexedSlicesQuery indexedSlicesQuery = HFactory.createIndexedSlicesQuery(
                         connection.getKeyspace(), md.getIdColumn().getValueSerializer(),
                         md.getNameSerializer(), byteBufferSerializer);
                 for (Conditions.ConditionItem op : conditions.operations()) {
                     Object key = op.property;
                     Object value = op.value;
                     ColumnInfo cinfo = md.getAllColumns().get(key.toString());
                     // FIXME: Hector API... Have to temporary set different serializer :(
                     indexedSlicesQuery.setValueSerializer(cinfo.getValueSerializer());
                     switch (op.operation) {
                         case GT:
                             indexedSlicesQuery.addGtExpression(key, value);
                             break;
                         case LT:
                             indexedSlicesQuery.addLtExpression(key, value);
                             break;
                         default:
                             indexedSlicesQuery.addEqualsExpression(key, value);
                             break;
                     }
                 }
                 indexedSlicesQuery.setColumnFamily(md.getEntityInfo().getCfName()).setRange(from, to, false,
                         colLimit);
                 indexedSlicesQuery.setValueSerializer(byteBufferSerializer);
                 QueryResult<OrderedRows<Object, Object, Object>> cResult = indexedSlicesQuery.execute();
                 resultList = processRows(cResult);
                 break;
         }
         return resultList;
     }
 
 
     @Override
     @SuppressWarnings({"unchecked", "suspicious"})
     public void remove(Object idValue) {
         if (!connection.isConnected()) {
             logAndThrow("Not connected to a cluster!");
         }
         Metadata md = getEntityMetadata();
         Serializer serializer = getEntityMetadata().getIdColumn().getValueSerializer();
         Mutator mutator = HFactory.createMutator(connection.getKeyspace(), serializer);
 
         logger.debug("Removing a row from '" + getEntityMetadata().getEntityInfo().getCfName()
                 + "' with rowKey='" + idValue + "'");
         mutator.delete(idValue, md.getEntityInfo().getCfName(), null, byteBufferSerializer);
     }
 }
