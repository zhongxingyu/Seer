 package org.ccci.obiee.client.rowmap;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.ccci.obiee.client.rowmap.annotation.Column;
 import org.ccci.obiee.client.rowmap.annotation.ReportPath;
 import org.ccci.obiee.client.rowmap.impl.ConverterStore;
 import org.ccci.obiee.client.rowmap.impl.EnumConverter;
 
 /**
  * Encapsulates a row type with validation logic and methods to retrieve {@link ReportColumn}s.
  * 
  * @param <T> the row type
  * 
  * @author Matt Drees
  */
 public class ReportDefinition<T>
 {
 
     private final Class<T> rowType;
 
     private final Map<String, ReportColumn<T>> columns;
     
     private final ConverterStore converters;
     
     public ReportDefinition(Class<T> rowType)
     {
         validate(rowType);
         columns = buildColumns(rowType);
         this.rowType = rowType;
         this.converters = buildConverters(rowType);
     }
 
     private ConverterStore buildConverters(Class<T> rowType)
     {
         ConverterStore store = new ConverterStore();
         //TODO: search superclasses for enum fields, as well
         for (Field field : rowType.getDeclaredFields())
         {
             Class<?> type = field.getType();
             
             if (Enum.class.isAssignableFrom(type))
             {
                 addConverterForEnumType(type, store);
             }
         }
         return store;
     }
 
     /* I've been fiddling with the java type system for a while and I haven't come up with a clean way
      * to do this (i.e., a way without a couple different @SupressWarnings annotations)
      */
     private void addConverterForEnumType(Class<?> type, ConverterStore store)
     {
         @SuppressWarnings("rawtypes") //"Enum" is raw, but I don't think there's a way around this.  
         Class<? extends Enum> asEnumType = type.asSubclass(Enum.class);
         
         addEnumConverterRaw(store, asEnumType);
     }
 
     @SuppressWarnings("unchecked") //we are calling a method expecting <U extends Enum<U>>, and we know 
       // that any subclass U of Enum is a Enum<U>, but compiler doesn't know this.  Also, we have to suppress
       // this warning at the method level, since we cannot annotate method invocations.
     private void addEnumConverterRaw(ConverterStore store, @SuppressWarnings("rawtypes") Class<? extends Enum> asEnumType)
     {
         addEnumConverter(asEnumType, store);
     }
     
     private <U extends Enum<U>> void addEnumConverter(Class<U> asEnumType, ConverterStore store)
     {
         store.addConverter(asEnumType, new EnumConverter<U>(asEnumType));
     }
 
 
     private void validate(Class<T> rowType)
     {
         if (!rowType.isAnnotationPresent(ReportPath.class))
         {
             throw new IllegalArgumentException(
                 rowType.getName() + " is not a valid OBIEE report row; it is not annotated @" + ReportPath.class.getSimpleName());
         }
     }
 
     private Map<String, ReportColumn<T>> buildColumns(Class<T> rowType)
     {
         Map<String, ReportColumn<T>> columns = new HashMap<String, ReportColumn<T>>();
         for (Field field : rowType.getDeclaredFields())
         {
             if (field.isAnnotationPresent(Column.class))
             {
                 ReportColumn<T> column = new ReportColumn<T>(field, rowType);
                 columns.put(column.getName(), column);
             }
         }
         return columns;
     }
 
     public String getName()
     {
         return rowType.getSimpleName();
     }
 
     /**
      * @param columnName
      * @return the corresponding {@link ReportColumn}
      * @throws IllegalArgumentException if there is no column with the given name
      */
     public ReportColumn<T> getColumn(String columnName)
     {
         ReportColumn<T> column = columns.get(columnName);
         if (column == null)
         {
            throw new IllegalArgumentException(getName() + " has no column named " + columnName);
         }
         return column;
     }
 
     public Set<ReportColumn<T>> getColumns()
     {
         return new HashSet<ReportColumn<T>>(columns.values());
     }
 
     public Class<T> getRowType()
     {
         return rowType;
     }
 
 
     public ConverterStore getConverterStore()
     {
         return converters;
     }
     
 }
