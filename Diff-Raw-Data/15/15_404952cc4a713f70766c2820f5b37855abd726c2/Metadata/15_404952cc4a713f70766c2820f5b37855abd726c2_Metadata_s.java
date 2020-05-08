 package me.smecsia.cassajem.meta;
 
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.ddl.ColumnType;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.smecsia.cassajem.api.BasicEntity;
 import me.smecsia.cassajem.api.CassajemException;
 import me.smecsia.cassajem.api.CassajemMetaException;
 import me.smecsia.cassajem.api.CompositeKey;
 import me.smecsia.cassajem.meta.annotations.*;
 import me.smecsia.cassajem.util.MapUtil;
 import me.smecsia.cassajem.util.TypesUtil;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.WordUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import static me.smecsia.cassajem.util.TypesUtil.*;
 
 /**
  * Metadata reader to collect entity annotations & etc
  * User: smecsia
  * Date: 27.01.12
  * Time: 13:45
  *
  * @param <T> entity class
  */
 public class Metadata<T extends BasicEntity> {
 
     private EntityInfo entityInfo = null;
     private Map<String, ColumnInfo> columns = new HashMap<String, ColumnInfo>();
     private Map<String, ColumnArrayInfo> columnArrays = new HashMap<String, ColumnArrayInfo>();
     private Map<String, SuperColumnInfo> superColumns = new HashMap<String, SuperColumnInfo>();
     private Map<String, SuperColumnArrayInfo> superColumnArrays = new HashMap<String, SuperColumnArrayInfo>();
     private Map<String, CompositeColumnArrayInfo> compositeArrays = new HashMap<String, CompositeColumnArrayInfo>();
     private ColumnInfo dynamicColumnsStorage = null;
     private SuperColumnInfo dynamicSuperColumnsStorage = null;
     private ColumnInfo idColumn = null;
     private Class<T> entityClass = null;
 
     private ComparatorType comparatorType = defaultComparator;
     private String defaultValidationClassName = TypesUtil.defaultValidationClass.getName();
     private Class keyValidationClass = defaultValidationClass;
     private String comparatorTypeAlias = "(UTF8Type)";
     private Serializer nameSerializer = stringSerializer;
 
     protected Logger logger = LoggerFactory.getLogger(getClass());
 
     public Metadata() {
     }
 
     public Metadata(Class<T> clazz) {
         readMetadata(clazz);
     }
 
     /**
      * Reads the metadata for the given class
      *
      * @param clazz - class to be read
      */
     public void readMetadata(Class<T> clazz) {
         entityClass = clazz;
         Annotation annotation = clazz.getAnnotation(ColumnFamily.class);
         if (annotation != null) {
             ColumnFamily cf = (ColumnFamily) annotation;
             entityInfo = new EntityInfo();
             entityInfo.setCfName(cf.name());
             entityInfo.setColumnType(cf.columnType());
             entityInfo.setSavePolicy(cf.savePolicy());
             entityInfo.setUseTimeForUUIDs(cf.useTimeForUUIDs());
             if (cf.compositeColumnTypes().length > 0) {
                 entityInfo.setCompositeColumnTypes(cf.compositeColumnTypes());
             }
             if (cf.columnType().equals(ColumnType.SUPER) && !cf.dataStorageSuperColumn().isEmpty()) {
                 entityInfo.setDataStorageSuperColumn(readSuperColumnInfo(clazz,
                         readField(clazz, cf.dataStorageSuperColumn())));
             }
         }
         Field[] fields = clazz.getDeclaredFields();
 
         // Read all superclasses fields
         Class superClass = clazz.getSuperclass();
         while (superClass != null) {
             fields = (Field[]) ArrayUtils.addAll(fields, superClass.getDeclaredFields());
             superClass = superClass.getSuperclass();
         }
 
         for (Field field : fields) {
 
             Column column = field.getAnnotation(Column.class);
             Id id = field.getAnnotation(Id.class);
             DynamicColumnStorage columnStorage = field.getAnnotation(DynamicColumnStorage.class);
             DynamicSuperColumnStorage superColumnStorage = field.getAnnotation(DynamicSuperColumnStorage.class);
             ColumnArray columnArray = field.getAnnotation(ColumnArray.class);
             SuperColumn superColumn = field.getAnnotation(SuperColumn.class);
             SuperColumnArray superColumnArray = field.getAnnotation(SuperColumnArray.class);
             CompositeColumnArray compositeArray = field.getAnnotation(CompositeColumnArray.class);
 
             if (entityInfo != null) {
                 if (entityInfo.getColumnType().equals(ColumnType.SUPER) &&
                         (column != null || columnStorage != null || compositeArray != null))
                     throw new CassajemException("ColumnFamily '" + entityInfo.getCfName()
                             + "' has annotations for a STANDARD column family, but it is SUPER");
                 if (entityInfo.getColumnType().equals(ColumnType.STANDARD) &&
                         (superColumn != null || superColumnArray != null || superColumnStorage != null))
                     throw new CassajemException("ColumnFamily '" + entityInfo.getCfName()
                             + "' has annotations for a SUPER column family, but it is STANDARD");
             } else if (superColumn != null || id != null || columnStorage != null || columnArray != null
                     || superColumnArray != null || compositeArray != null) {
                 throw new CassajemException("Subtype '" + clazz.getName()
                         + "' must have @ColumnFamily annotation unless you don't use any other fields except @Column!");
             }
 
             if (id != null) {
                 addIdColumn(clazz, field);
                 if (!id.keyValidationClass().equals(Object.class)) {
                     keyValidationClass = id.keyValidationClass();
                 } else {
                     keyValidationClass = validationClassByType(field.getType(), id.useTimeUUID());
                 }
                 idColumn.persist = id.persist();
             } else if (compositeArray != null) {
                 addCompositeArray(clazz, field, compositeArray);
             } else if (superColumnStorage != null) {
                 addDynamicSuperColumnsStorage(clazz, field);
             } else if (columnStorage != null) {
                 addDynamicColumnsStorage(clazz, field);
             } else if (column != null) {
                 addColumn(clazz, field, column);
             } else if (columnArray != null) {
                 addColumnArray(clazz, field, columnArray);
             } else if (superColumn != null) {
                 addSuperColumn(clazz, field, superColumn);
             } else if (superColumnArray != null) {
                 addSuperColumnArray(clazz, field, superColumnArray);
             }
         }
         chooseComparatorAndSerializer();
     }
 
     /**
      * Chooses the comparator type, validation class name and serializer
      */
     protected void chooseComparatorAndSerializer() {
         // by default comparator type is default
         comparatorType = defaultComparator;
         nameSerializer = stringSerializer;
         if (entityInfo != null && entityInfo.usesComposites()) {
             comparatorTypeAlias = toComparatorAlias(getEntityInfo().useTimeForUUIDs(),
                     (Class[]) ArrayUtils.addAll(new Class[]{String.class}, entityInfo.compositeColumnTypes()));
             comparatorType = compositeComparator;
             nameSerializer = compositeSerializer;
             defaultValidationClassName = defaultValidationClass.getName();
         } else if (!columns.isEmpty()) {
             comparatorType = comparatorTypeByType(String.class);
             //TODO: nameSerializer is always string. But it is not right.
             nameSerializer = stringSerializer;
             //            nameSerializer = (dynamicColumnsStorage != null) ? byteBufferSerializer : stringSerializer;
             //            defaultValidationClassName = (dynamicColumnsStorage != null) ? defaultValidationClass.getName() : validationClassByType(String.class).getName();
         } else if (dynamicColumnsStorage != null) {
             comparatorType = comparatorTypeByType(dynamicColumnsStorage.getType());
             nameSerializer = serializerByType(dynamicColumnsStorage.getType());
             defaultValidationClassName = validationClassByType(dynamicColumnsStorage.getType()).getName();
         }
     }
 
     /**
      * Reads the field information
      *
      * @param clazz     - class
      * @param fieldName - field name in class
      * @return field - java.reflect.Field
      */
     protected Field readField(Class<? extends BasicEntity> clazz, String fieldName) {
         Field res = null;
         try {
             res = clazz.getDeclaredField(fieldName);
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         }
         return res;
     }
 
     /**
      * Reads super column info from the class field
      *
      * @param clazz - class
      * @param field - field to be read
      * @return super column metadata
      */
     @SuppressWarnings("unchecked")
     protected SuperColumnInfo readSuperColumnInfo(Class<? extends BasicEntity> clazz, Field field) {
         SuperColumn superColumn = field.getAnnotation(SuperColumn.class);
         if (!BasicEntity.class.isAssignableFrom(field.getType())) {
             throw new CassajemMetaException("SuperColumn '" + field.getName() + "' type must be a subclass of BasicEntity");
         }
         SuperColumnInfo cInfo = new SuperColumnInfo(MetaCache.instance().forClass(
                 (Class<? extends BasicEntity>) field.getType()));
         fillColumnInfo(cInfo, clazz, field);
         String name = (superColumn.name().isEmpty()) ? field.getName() : superColumn.name();
         cInfo.setFieldName(name);
         return cInfo;
     }
 
     /**
      * Fills the default column metadata based on the field info
      *
      * @param res   - where to put metadata
      * @param clazz - class to be read
      * @param field - field to be read
      */
     protected void fillColumnInfo(ColumnInfo res, Class<? extends BasicEntity> clazz, Field field) {
         res.setField(field);
         res.setFieldName(field.getName());
         res.setType(field.getType());
         //FIXME: duplicated link to composite types in each columninfo
         if (getEntityInfo() != null) {
             res.setCompositeTypes(getEntityInfo().compositeColumnTypes());
         }
         String fieldCapName = WordUtils.capitalize(field.getName());
         // FIXME: we take the first found getter here (getSmth or isSmth): not very good style
         try {
             res.setGetter(clazz.getMethod("get" + fieldCapName));
         } catch (NoSuchMethodException getE) {
             try {
                 if (isBoolean(field.getType())) {
                     res.setGetter(clazz.getMethod("is" + fieldCapName));
                 } else {
                     throw new NoSuchMethodException("is" + fieldCapName);
                 }
             } catch (NoSuchMethodException isE) {
                 logger.warn("Cannot find getter for the field: " + field.getName() + " of entity class " + clazz.getName());
             }
         }
         try {
             res.setSetter(clazz.getMethod("set" + fieldCapName, field.getType()));
         } catch (NoSuchMethodException setE) {
             logger.warn("Cannot find setter for the field: " + field.getName() + " of entity class " + clazz.getName());
         }
         res.setKeySerializer(TypesUtil.stringSerializer);
         res.setValueSerializer(TypesUtil.serializerByType(field.getType()));
     }
 
     /**
      * Reads the Id column from the class
      *
      * @param clazz - class
      * @param field - field to be read
      */
     protected void addIdColumn(Class<? extends BasicEntity> clazz, Field field) {
         idColumn = new ColumnInfo();
         fillColumnInfo(idColumn, clazz, field);
         //        idColumn.setKeySerializer(serializerByType(field.getType()));
     }
 
     /**
      * Reads the dynamic columns storage from the class
      *
      * @param clazz - class
      * @param field - field to be read
      */
     protected void addDynamicColumnsStorage(Class<? extends BasicEntity> clazz, Field field) {
         Type[] listTypes = getFieldTypeArguments(field);
         if (!Map.class.isAssignableFrom(field.getType()) || listTypes.length != 2) {
             throw new CassajemMetaException("DynamicColumnStorage should be parametrized type implementing java.util.Map");
         }
         dynamicColumnsStorage = new ColumnInfo();
         fillColumnInfo(dynamicColumnsStorage, clazz, field);
         dynamicColumnsStorage.setType((Class<?>) listTypes[0]);
         dynamicColumnsStorage.setKeySerializer(serializerByType((Class<?>) listTypes[0]));
         dynamicColumnsStorage.setValueSerializer(serializerByType((Class<?>) listTypes[1]));
     }
 
     /**
      * Reads the dynamic super columns storage metadata
      *
      * @param clazz - class
      * @param field - field to be read
      */
     @SuppressWarnings("unchecked")
     protected void addDynamicSuperColumnsStorage(Class<? extends BasicEntity> clazz, Field field) {
         Type[] listTypes = getFieldTypeArguments(field);
         if (!Map.class.isAssignableFrom(field.getType()) || listTypes.length != 2
                 || BasicEntity.class.isAssignableFrom(listTypes[1].getClass())) {
             throw new CassajemMetaException(
                     "DynamicSuperColumnStorage should be parametrized type implementing java.util.Map with a second parameter which is a subclass of BasicEntity");
         }
         dynamicSuperColumnsStorage = new SuperColumnInfo(MetaCache.instance().forClass((Class) listTypes[1]));
         fillColumnInfo(dynamicSuperColumnsStorage, clazz, field);
         dynamicSuperColumnsStorage.setKeySerializer(serializerByType((Class<?>) listTypes[0]));
         dynamicSuperColumnsStorage.setValueSerializer(serializerByType((Class<?>) listTypes[0]));
     }
 
     /**
      * Returns the generic arguments from the field
      *
      * @param field - field to be reflect
      * @return Type arguments array
      */
     protected Type[] getFieldTypeArguments(Field field) {
         Type genericFieldType = field.getGenericType();
         if (genericFieldType instanceof ParameterizedType) {
             ParameterizedType aType = (ParameterizedType) genericFieldType;
             return aType.getActualTypeArguments();
         }
         return new Type[]{};
     }
 
     /**
      * Reads the composite array metadata
      *
      * @param clazz  - class
      * @param field  - field to be read
      * @param cArray - annotation
      */
     @SuppressWarnings("unchecked")
     protected void addCompositeArray(Class<? extends BasicEntity> clazz, Field field, CompositeColumnArray cArray) {
         Type[] listTypes = getFieldTypeArguments(field);
         if (!Map.class.isAssignableFrom(field.getType()) || listTypes.length != 2
                 || !CompositeKey.class.isAssignableFrom((Class<?>) listTypes[0])) {
             throw new CassajemMetaException(
                     "CompositeColumnArray should be parametrized type implementing java.util.Map, first parameter " +
                            "should be a type extending me.smecsia.me.smecsia.cassajem.me.smecsia.me.smecsia.cassajem.api.CompositeKey!");
         }
         Class<CompositeKey> compositeClass = (Class<CompositeKey>) listTypes[0];
         CompositeColumnArrayInfo cInfo = new CompositeColumnArrayInfo(compositeClass);
         fillColumnInfo(cInfo, clazz, field);
         cInfo.setKeySerializer(serializerByType((Class) listTypes[0]));
         cInfo.setValueSerializer(serializerByType((Class) listTypes[1]));
         String ccaName = (cArray.prefix().isEmpty()) ? field.getName() : cArray.prefix();
         cInfo.setFieldName(ccaName);
         compositeArrays.put(ccaName, cInfo);
     }
 
     /**
      * Reads the column array metadata from the class
      *
      * @param clazz  - class
      * @param field  - field to be read
      * @param cArray - annotation
      */
     @SuppressWarnings("unchecked")
     protected void addColumnArray(Class<? extends BasicEntity> clazz, Field field, ColumnArray cArray) {
         Type[] listTypes = getFieldTypeArguments(field);
         if (!Map.class.isAssignableFrom(field.getType()) || listTypes.length != 2) {
             throw new CassajemMetaException("ColumnArray should be parametrized type implementing java.util.Map");
         }
         ColumnArrayInfo caInfo = new ColumnArrayInfo();
         fillColumnInfo(caInfo, clazz, field);
         caInfo.setKeySerializer(serializerByType((Class) listTypes[0]));
         caInfo.setValueSerializer(serializerByType((Class) listTypes[1]));
         String name = (cArray.name().isEmpty()) ? field.getName() : cArray.name();
         caInfo.setFieldName(name);
         columnArrays.put(name, caInfo);
     }
 
     /**
      * Reads super column array metadata
      *
      * @param clazz   - class
      * @param field   - field to be read
      * @param scArray - annotation
      */
     @SuppressWarnings("unchecked")
     protected void addSuperColumnArray(Class<? extends BasicEntity> clazz, Field field, SuperColumnArray scArray) {
         Type[] listTypes = getFieldTypeArguments(field);
         if (!Collection.class.isAssignableFrom(field.getType()) || listTypes.length != 1
                 || BasicEntity.class.isAssignableFrom(listTypes[0].getClass())) {
             throw new CassajemMetaException(
                     "SuperColumnArray should be parametrized type implementing java.util.Collection with a parameter " +
                             "which is a subclass of BasicEntity");
         }
         SuperColumnArrayInfo cInfo = new SuperColumnArrayInfo(MetaCache.instance().forClass((Class) listTypes[0]));
         fillColumnInfo(cInfo, clazz, field);
         String name = (scArray.name().isEmpty()) ? field.getName() : scArray.name();
         cInfo.setFieldName(name);
         superColumnArrays.put(name, cInfo);
     }
 
     /**
      * Reads super column annotation
      *
      * @param clazz  - classj
      * @param field  - field to be read
      * @param column - annotation
      */
     @SuppressWarnings("unchecked")
     protected void addSuperColumn(Class<? extends BasicEntity> clazz, Field field, SuperColumn column) {
         String name = (column.name().isEmpty()) ? field.getName() : column.name();
         superColumns.put(name, readSuperColumnInfo(clazz, field));
     }
 
     /**
      * Reads column metadata
      *
      * @param clazz  - class
      * @param field  - field to be read
      * @param column - column metadata
      */
     protected void addColumn(Class<? extends BasicEntity> clazz, Field field, Column column) {
         ColumnInfo cInfo = new ColumnInfo();
         fillColumnInfo(cInfo, clazz, field);
         String fieldName = (column.name().isEmpty()) ? field.getName() : column.name();
         cInfo.setFieldName(fieldName);
         columns.put(fieldName, cInfo);
     }
 
     /**
      * Returns the entity information metadata
      *
      * @return metadata
      */
     public EntityInfo getEntityInfo() {
         return entityInfo;
     }
 
     /**
      * Indicates if this SCF has special super column for own properties
      *
      * @return true if ColumnFamily has the indicated storage super column
      */
     public boolean hasDataStorageColumn() {
         return getEntityInfo() != null && getEntityInfo().getDataStorageSuperColumn() != null;
     }
 
     /**
      * Returns true if entity contains entity info with such prefix
      *
      * @param compositePrefix - prefix (Ex: "sessions", "sessions#additionalData")
      * @return true or false
      */
     public boolean hasCompositeArray(String compositePrefix) {
         return getCompositeArrays().containsKey(compositePrefix) ||
                 (compositePrefix.contains("#") &&
                         getCompositeArrays().containsKey(compositePrefix.split("#")[0]));
     }
 
     public void setEntityInfo(EntityInfo entityInfo) {
         this.entityInfo = entityInfo;
     }
 
     public Map<String, ColumnInfo> getColumns() {
         return columns;
     }
 
     /**
      * Returns all columns metadata (including Id)
      *
      * @return all columns metadata
      */
     public Map<String, ColumnInfo> getAllColumns() {
         Map<String, ColumnInfo> result = getColumns();
         if (idColumn != null && idColumn.persist) {
             result.put(idColumn.getFieldName(), idColumn);
         }
         return result;
     }
 
     /**
      * Returns all columns metadata (including Id) but only presented in the columns list
      *
      * @param fieldNames field names that should be included into the result map
      * @return all columns metadata
      */
     public Map<String, ColumnInfo> getAllColumns(String... fieldNames) {
         return MapUtil.includeKeys(getAllColumns(), fieldNames);
     }
 
     public void setColumns(Map<String, ColumnInfo> columns) {
         this.columns = columns;
     }
 
     public Map<String, SuperColumnInfo> getSuperColumns() {
         return superColumns;
     }
 
     public void setSuperColumns(Map<String, SuperColumnInfo> superColumns) {
         this.superColumns = superColumns;
     }
 
     public Map<String, SuperColumnArrayInfo> getSuperColumnArrays() {
         return superColumnArrays;
     }
 
     public void setSuperColumnArrays(Map<String, SuperColumnArrayInfo> superColumnArrays) {
         this.superColumnArrays = superColumnArrays;
     }
 
     public ColumnInfo getIdColumn() {
         return idColumn;
     }
 
     public void setIdColumn(ColumnInfo idColumn) {
         this.idColumn = idColumn;
     }
 
     public Map<String, ColumnArrayInfo> getColumnArrays() {
         return columnArrays;
     }
 
     public void setColumnArrays(Map<String, ColumnArrayInfo> columnArrays) {
         this.columnArrays = columnArrays;
     }
 
     public ComparatorType getComparatorType() {
         return comparatorType;
     }
 
     public void setComparatorType(ComparatorType comparatorType) {
         this.comparatorType = comparatorType;
     }
 
     public Class<T> getEntityClass() {
         return entityClass;
     }
 
     public void setEntityClass(Class<T> entityClass) {
         this.entityClass = entityClass;
     }
 
     public ColumnInfo getDynamicColumnsStorage() {
         return dynamicColumnsStorage;
     }
 
     public void setDynamicColumnsStorage(ColumnInfo dynamicColumnsStorage) {
         this.dynamicColumnsStorage = dynamicColumnsStorage;
     }
 
     public SuperColumnInfo getDynamicSuperColumnsStorage() {
         return dynamicSuperColumnsStorage;
     }
 
     public void setDynamicSuperColumnsStorage(SuperColumnInfo dynamicSuperColumnsStorage) {
         this.dynamicSuperColumnsStorage = dynamicSuperColumnsStorage;
     }
 
     /**
      * Return map of the composite arrays fields metadata
      *
      * @param fieldNames names of the fields that should be included into the result
      * @return all the composite arrays fields excluding those that are not presented in fieldNames param
      */
     public Map<String, CompositeColumnArrayInfo> getCompositeArrays(String... fieldNames) {
         return MapUtil.includeKeys(getCompositeArrays(), fieldNames);
     }
 
     public Map<String, CompositeColumnArrayInfo> getCompositeArrays() {
         return compositeArrays;
     }
 
     public void setCompositeArrays(Map<String, CompositeColumnArrayInfo> compositeArrays) {
         this.compositeArrays = compositeArrays;
     }
 
     public Serializer getNameSerializer() {
         return nameSerializer;
     }
 
     public void setNameSerializer(Serializer nameSerializer) {
         this.nameSerializer = nameSerializer;
     }
 
     public String getComparatorTypeAlias() {
         return comparatorTypeAlias;
     }
 
     public void setComparatorTypeAlias(String comparatorTypeAlias) {
         this.comparatorTypeAlias = comparatorTypeAlias;
     }
 
     public Class getKeyValidationClass() {
         return keyValidationClass;
     }
 
     public void setKeyValidationClass(Class keyValidationClass) {
         this.keyValidationClass = keyValidationClass;
     }
 
     public String getDefaultValidationClassName() {
         return defaultValidationClassName;
     }
 
     public void setDefaultValidationClassName(String defaultValidationClassName) {
         this.defaultValidationClassName = defaultValidationClassName;
     }
 
 }
