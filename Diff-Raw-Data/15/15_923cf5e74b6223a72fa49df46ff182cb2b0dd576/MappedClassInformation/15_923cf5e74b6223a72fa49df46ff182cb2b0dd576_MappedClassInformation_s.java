 package org.narwhal.util;
 
 import org.narwhal.annotation.Column;
 import org.narwhal.annotation.Table;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
 
 /**
  * The <code>MappedClassInformation</code> class keeps all the information about particular class.
  * Instance of this class holds data about set methods, constructors and columns name of the database
  * tables that mapped to the fields of class.
  * */
 public class MappedClassInformation<T> {
 
     private Constructor<T> constructor;
     private Method[] setMethods;
     private Method[] getMethods;
     private String[] columns;
     private Method primaryKeyGetMethod;
     private Map<QueryType, String> queries;
     private String primaryColumnName;
 
 
     /**
      * Initializes a new instance of the MappedClassInformation class.
      * Instance is specified by the value of the Class<T>.
      * This constructor tries to retrieve all the necessary information about the class.
      *
      * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
      * @throws NoSuchMethodException If there is no appropriate method to invoke
      * */
     public MappedClassInformation(Class<T> mappedClass) throws NoSuchMethodException {
         Field[] fields = mappedClass.getDeclaredFields();
         primaryKeyGetMethod = getPrimaryKeyMethod(mappedClass, fields);
         primaryColumnName   = getPrimaryKeyColumnName(mappedClass, fields);
         constructor = mappedClass.getConstructor();
         setMethods  = getSetMethods(mappedClass, fields);
         getMethods  = getGetMethods(mappedClass, fields);
         columns     = getColumnsName(fields);
         queries     = createQueries(mappedClass);
     }
 
     /**
      * Returns list of the set methods for corresponding fields of the class.
      *
      * @return List of set methods.
      * */
     public Method[] getSetMethods() {
         return setMethods;
     }
 
     /**
      * Returns list of the get methods for corresponding fields of the class.
      *
      * @return List of get methods.
      * */
     public Method[] getGetMethods() {
         return getMethods;
     }
 
     /**
      * Returns getter method for class field that annotated by Column annotation with primaryKey = true.
      *
      * @return Getter method for field of the class that maps to the primary key.
      * */
     public Method getPrimaryKeyGetMethod() {
         return primaryKeyGetMethod;
     }
 
     /**
      * Returns string representation of the SQL query by the QueryType object.
      *
      * @return String representation of the SQL query.
      * */
     public String getQuery(QueryType queryType) {
         return queries.get(queryType);
     }
 
     /**
      * Returns list of the columns that have been retrieved from the annotated fields.
      *
      * @return Columns of the database table.
      * */
    public List<String> getColumns() {
         return columns;
     }
 
     /**
      * Returns default constructor of the class.
      *
      * @return Default constructor of the class.
      * */
     public Constructor<T> getConstructor() {
         return constructor;
     }
 
     /**
      * Retrieves columns name of the database table from the annotated fields.
      *
      * @param fields Fields of class that have been annotated by {@literal @}Column annotation.
      * @return Columns of the database table.
      * */
     private String[] getColumnsName(Field[] fields) {
         String[] columns = new String[fields.length];
 
         for (int i = 0; i < fields.length; ++i) {
             if (fields[i].isAnnotationPresent(Column.class) &&
                 !fields[i].getAnnotation(Column.class).value().isEmpty()) {
                 columns[i] = fields[i].getAnnotation(Column.class).value();
             } else {
                 columns[i] = fields[i].getName();
             }
         }
 
         return columns;
     }
 
     /**
      * Retrieves primary key name of the database table from the annotated fields.
      *
      * @param mappedClass A class, which is used for creating appropriate exception message.
      * @param fields Fields of class that have been annotated by {@literal @}Column annotation.
      * @throws IllegalArgumentException if field of the class wasn't annotated by the {@literal @}Column annotation
      *         with primaryKey = true.
      * */
     private <T> String getPrimaryKeyColumnName(Class<T> mappedClass, Field[] fields) {
         for (Field field : fields) {
             if (field.isAnnotationPresent(Column.class) &&
                 field.getAnnotation(Column.class).primaryKey()) {
                 String columnName = field.getAnnotation(Column.class).value();
 
                 if (columnName.isEmpty()) {
                     return field.getName();
                 } else {
                     return columnName;
                 }
             }
         }
 
         throw new IllegalArgumentException("Class " + mappedClass +
                 " doesn't have a field that was annotated by " + Column.class + " annotation with primaryKey = true");
     }
 
     /**
      * Retrieves string representation of the table name that maps to the particular entity.
      *
      * @param mappedClass A class that is used for checking whether
      *                    class was annotated by a particular annotation or not.
      * @return string representation of the table name.
      * @throws IllegalArgumentException If class wasn't annotated by the Table annotation.
      * */
     private <T> String getTableName(Class<T> mappedClass) {
         if (mappedClass.isAnnotationPresent(Table.class)) {
             return mappedClass.getAnnotation(Table.class).value();
         }
 
         throw new IllegalArgumentException("Class " + mappedClass.toString() +
                 " wasn't annotated by " + Table.class + " annotation");
     }
 
     /**
      * Constructs string representation method by using field name and a prefix (get, set).
      *
      * @param prefix Prefix that uses to persist whether getter or setter.
      * @return String representation of the class method.
      * */
     private String getMethodName(Field field, String prefix) {
         char[] fieldNameArray = field.getName().toCharArray();
         fieldNameArray[0] = Character.toUpperCase(fieldNameArray[0]);
 
         return prefix + new String(fieldNameArray);
     }
 
     /**
      * Creates and returns name of the set method from string representation of the field.
      *
      * @return String representation of the set method.
      * */
     private String getSetMethodName(Field field) {
         return getMethodName(field, "set");
     }
 
     /**
      * Creates and returns name of the get method from string representation of the field.
      *
      * @return String representation of the set method.
      * */
     private String getGetMethodName(Field field) {
         Class type = field.getType();
 
         if (type.equals(boolean.class) || type.equals(Boolean.class)) {
             return getMethodName(field, "is");
         } else {
             return getMethodName(field, "get");
         }
     }
 
     /**
      * Returns all set methods from the class. This method uses list of fields
      * which is used for retrieving set methods from the class.
      *
      * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
      * @param fields Fields of the class.
      * @return Set methods of the class.
      * @throws NoSuchMethodException If there is no appropriate method to invoke.
      * */
     private <T> Method[] getSetMethods(Class<T> mappedClass, Field[] fields) throws NoSuchMethodException {
         Method[] methods = new Method[fields.length];
 
         for (int i = 0; i < fields.length; ++i) {
             String methodName = getSetMethodName(fields[i]);
             methods[i] = mappedClass.getMethod(methodName, fields[i].getType());
         }
 
         return methods;
     }
 
     /**
      * Returns all get methods from the class. This method uses list of fields
      * which is used for retrieving set methods from the class.
      *
      * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
      * @param fields Fields of the class.
      * @return Set methods of the class.
      * @throws NoSuchMethodException If there is no appropriate method to invoke.
      * */
     private <T> Method[] getGetMethods(Class<T> mappedClass, Field[] fields) throws NoSuchMethodException {
         Method[] methods = new Method[fields.length];
 
         for (int i = 0; i < fields.length; ++i) {
             String methodName = getGetMethodName(fields[i]);
             methods[i] = mappedClass.getMethod(methodName);
         }
 
         return methods;
     }
 
     /**
      * Retrieves getter method for the class' field that was annotated by the
      * {@literal @}Column annotation with primaryKey = true.
      *
      * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
      * @param fields Fields of the class.
      * @return Getter method for the field that maps to the primary key.
      * @throws NoSuchMethodException If there is no appropriate method to invoke.
      * */
     private <T> Method getPrimaryKeyMethod(Class<T> mappedClass, Field[] fields) throws NoSuchMethodException {
         for (Field field : fields) {
             if (field.isAnnotationPresent(Column.class) &&
                 field.getAnnotation(Column.class).primaryKey()) {
                 String methodName = getGetMethodName(field);
                 return mappedClass.getMethod(methodName);
             }
         }
 
         throw new IllegalArgumentException("Class " + mappedClass +
                 " doesn't have a field that was annotated by " + Column.class + " annotation with primaryKey = true");
     }
 
     /**
      * Return maps of the QueryType and string representation of the SQL query pairs.
      *
      * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
      * @return Map of QueryType and SQL query pairs.
      * */
     private <T> Map<QueryType, String> createQueries(Class<T> mappedClass) {
         String tableName = getTableName(mappedClass);
         Map<QueryType, String> queries = new HashMap<>();
 
         queries.put(QueryType.CREATE, makeInsertQuery(tableName));
         queries.put(QueryType.READ,   makeSelectQuery(tableName));
         queries.put(QueryType.UPDATE, makeUpdateQuery(tableName));
         queries.put(QueryType.DELETE, makeDeleteQuery(tableName));
 
         return queries;
     }
 
     /**
      * Makes prepared INSERT SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the INSERT SQL statement.
      * */
     private String makeInsertQuery(String tableName) {
         StringBuilder builder = new StringBuilder("INSERT INTO ");
         builder.append(tableName);
         builder.append(" VALUES (");
 
        for (int i = 0; i < columns.size(); ++i) {
             if (i > 0) {
                 builder.append(',');
             }
 
             builder.append('?');
         }
         builder.append(")");
 
         return builder.toString();
     }
 
     /**
      * Makes prepared SELECT SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the SELECT SQL statement.
      * */
     private String makeSelectQuery(String tableName) {
         StringBuilder builder = new StringBuilder("SELECT * FROM ");
         builder.append(tableName);
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 
     /**
      * Makes prepared DELETE SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the DELETE SQL statement.
      * */
     private String makeDeleteQuery(String tableName) {
         StringBuilder builder = new StringBuilder("DELETE FROM ");
         builder.append(tableName);
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 
     /**
      * Makes prepared UPDATE SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the UPDATE SQL statement.
      * */
     private String makeUpdateQuery(String tableName) {
         StringBuilder builder = new StringBuilder("UPDATE ");
         builder.append(tableName);
         builder.append(" SET ");
 
        for (int i = 0; i < columns.size(); ++i) {
             if (i > 0) {
                 builder.append(',');
             }
 
            builder.append(columns.get(i));
             builder.append(" = ?");
         }
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 }
