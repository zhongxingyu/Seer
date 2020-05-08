 package org.atomium;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.CaseFormat.LOWER_CAMEL;
 import static com.google.common.base.CaseFormat.UPPER_CAMEL;
 import static com.google.common.base.Preconditions.checkState;
 import static com.google.common.base.Throwables.propagate;
 
 /**
  * @author Blackrush
  */
 public class Metadata<T> {
     private final MetadataRegistry registry;
     private final Class<T> target;
     private final Map<String, ColumnMetadata<T>> columns = Maps.newHashMap();
 
     private boolean loaded;
     private String tableName;
     private ColumnMetadata<T> primaryKey;
 
     public Metadata(MetadataRegistry registry, Class<T> target) {
         this.registry = registry;
         this.target = target;
     }
 
     public MetadataRegistry getRegistry() {
         return registry;
     }
 
     public Class<T> getTarget() {
         return target;
     }
 
     public BeanInfo getBeanInfo() {
         try {
             return Introspector.getBeanInfo(target);
         } catch (IntrospectionException e) {
             throw propagate(e);
         }
     }
 
     public void load() {
         checkState(!loaded, "this metadata is already loaded");
 
         loadMetadata();
         loadColumns();
 
         checkState(primaryKey != null, "a metadata must have a primary key");
 
         loaded = true;
     }
 
     private void loadMetadata() {
         Table table = target.getAnnotation(Table.class);
         if (table != null && !table.value().equals(Table.DEFAULT)) {
             tableName = table.value();
         } else {
             tableName = UPPER_CAMEL.to(LOWER_CAMEL, target.getSimpleName());
         }
     }
 
     private void loadColumns() {
         for (PropertyDescriptor prop : getBeanInfo().getPropertyDescriptors()) {
             Column annotation = prop.getReadMethod().getAnnotation(Column.class);
             if (annotation == null) continue;
 
             String name;
             if (!annotation.value().equals(Column.DEFAULT)) {
                 name = annotation.value();
             } else {
                 name = prop.getName();
             }
 
             PropertyColumnMetadata<T> column = new PropertyColumnMetadata<>(this, name, prop.getReadMethod(), prop.getWriteMethod());
             column.load();
 
             if (column.isPrimaryKey()) {
                 checkState(primaryKey == null, "%s has more than one primary key", target.getName());
                 primaryKey = column;
             }
             columns.put(name, column);
         }
 
         for (Field field : getTarget().getDeclaredFields()) {
             Column annotation = field.getAnnotation(Column.class);
             if (annotation == null) continue;
 
             String name;
             if (!annotation.value().equals(Column.DEFAULT)) {
                 name = annotation.value();
             } else {
                 name = field.getName();
             }
 
             FieldColumnMetadata<T> column = new FieldColumnMetadata<>(this, name, field);
             column.load();
 
             if (column.isPrimaryKey()) {
                 checkState(primaryKey == null, "%s has more than one primary key", target.getName());
                 primaryKey = column;
             }
             columns.put(name, column);
         }
     }
 
     /**
      * a set of all columns
      * @return an immutable colletion of all columns
      */
     public Set<ColumnMetadata<T>> getColumns() {
         return ImmutableSet.copyOf(columns.values());
     }
 
     /**
      * get the column by its name
      * @param name column's name
      * @return found column or null
      */
     public ColumnMetadata<T> getColumn(String name) {
         return columns.get(name);
     }
 
     /**
      * get the table's name
      * @return table's name
      */
     public String getTableName() {
         return tableName;
     }
 
     public NamedValues map(T instance) {
         NamedValues values = NamedValues.of();
 
         for (ColumnMetadata<T> column : columns.values()) {
             ConverterInterface converter = column.getConverter();
 
             if (converter == null || !converter.export(column, instance, values)) {
                 values.set(column.getName(), column.get(instance));
             }
         }
 
         return values;
     }
 
     public T map(NamedValues values) {
         T instance = createEmpty();
 
         for (ColumnMetadata<T> column : columns.values()) {
             ConverterInterface converter = column.getConverter();
 
             if (converter == null || !converter.extract(column, instance, values)) {
                 column.set(instance, values.get(column.getName()));
             }
         }
 
         return instance;
     }
 
     public ColumnMetadata<T> getPrimaryKey() {
         return primaryKey;
     }
 
     public T createEmpty() {
         try {
             return target.newInstance();
         } catch (InstantiationException | IllegalAccessException e) {
             throw propagate(e);
         }
     }
 
     /**
      * this methods will return {@code true} if the given entity is not persisted in the database (ie not created)
      * @param instance the entity
      * @return a boolean
      * TODO improve the way to know if an entity is persisted or not
      */
     public boolean isPersisted(T instance) {
         Object pkey = primaryKey.get(instance);
        return pkey != null && !pkey.equals(0);
     }
 }
