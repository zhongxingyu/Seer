 package org.narwhal.core;
 
 import org.narwhal.annotation.Column;
 import org.narwhal.annotation.Table;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.*;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * <p>
  * The <code>DatabaseConnection</code> represents connection to the relational database.
  * This class includes methods for retrieve particular information from the relational databases.
  * It automatically maps retrieved result set to the particular entity (java class).
  * DatabaseConnection class also manages all resources like database connection,
  * prepared statements, result sets etc.
  * It also provides basic logging using slf4j library for this case.
  * </p>
  *
  * <p>
  *     For using this class properly, all mapped classes have to annotate all fields
  *     that map to the database columns.
  *
  *     Here's an example how to use the annotation:
  *
  *     <p><code>
  *         public class Person {
  *             {@literal @}Column("person_id")
  *             private int id;
  *             {@literal @}Column("name)
  *             private String name;
  *
  *             // get and set methods.
  *         }
  *     </code></p>
  *
  *     The methods of the <code>DatabaseConnection</code> class use annotations and annotated fields
  *     to retrieve necessary information from database and to invoke set methods through reflection api.
  * </p>
 
  * <p>Here are some examples how DatabaseConnection can be used:</p>
 
  * <p><code>
  * DatabaseConnection connection = new DatabaseConnection(new DatabaseInformation(driver, url, username, password));
  *
  * connection.executeUpdate("UPDATE person SET name = ? WHERE id = ?", name, id);
  *
  * Person person = connection.executeQuery("SELECT * FROM person WHERE id = ?", Person.class, id);
  * </code>
  * </p>
  *
  * @author Miron Aseev
  * @see DatabaseInformation
  */
 public class DatabaseConnection {
 
     private static enum QueryType {
         CREATE,
         READ,
         UPDATE,
         DELETE
     }
 
     /**
      * The <code>MappedClassInformation</code> class keeps all the information about particular class.
      * Instance of this class holds data about set methods, constructors and columns name of the database
      * tables that mapped to the fields of class.
      * */
     private static class MappedClassInformation<T> {
 
         private Constructor<T> constructor;
         private List<Method> setMethods;
         private List<Method> getMethods;
         private List<String> columns;
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
             List<Field> annotatedFields = getAnnotatedFields(mappedClass, Column.class);
             constructor = mappedClass.getConstructor();
             setMethods = getSetMethods(mappedClass, annotatedFields);
             getMethods = getGetMethods(mappedClass, annotatedFields);
             primaryKeyGetMethod = getPrimaryKeyMethod(mappedClass, annotatedFields);
             columns = getColumnsName(annotatedFields);
             primaryColumnName = getPrimaryKeyColumnName(mappedClass, annotatedFields);
             queries = createQueries(mappedClass);
         }
 
         /**
          * Returns list of the set methods for corresponding fields of the class.
          *
          * @return List of set methods.
          * */
         public List<Method> getSetMethods() {
             return setMethods;
         }
 
         public List<Method> getGetMethods() {
             return getMethods;
         }
 
         public Method getPrimaryKeyGetMethod() {
             return primaryKeyGetMethod;
         }
 
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
          * @param annotatedFields Fields of class that have been annotated by {@literal @}Column annotation.
          * @return Columns of the database table.
          * */
         private List<String> getColumnsName(List<Field> annotatedFields) {
             List<String> columns = new ArrayList<String>();
 
             for (Field field : annotatedFields) {
                 columns.add(field.getAnnotation(Column.class).value());
             }
 
             return columns;
         }
 
         private <T> String getPrimaryKeyColumnName(Class<T> mappedClass, List<Field> annotatedFields) {
             for (Field field : annotatedFields) {
                 if (field.getAnnotation(Column.class).primaryKey()) {
                     return field.getAnnotation(Column.class).value();
                 }
             }
 
             throw new IllegalArgumentException("Class " + mappedClass +
                     " doesn't have a field that was annotated by " + Column.class + " annotation with primaryKey = true");
         }
 
         private <T> String getTableName(Class<T> mappedClass) {
             if (mappedClass.isAnnotationPresent(Table.class)) {
                 return mappedClass.getAnnotation(Table.class).value();
             }
 
             throw new IllegalArgumentException("Class " + mappedClass.toString() +
                     " wasn't annotated by " + Table.class + " annotation");
         }
 
         /**
          * Retrieves all fields of the class that have been annotated by a particular annotation.
          *
          * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
          * @param annotation Annotation which is used as a condition to filter annotated fields of the class.
          * @return Fields that have been annotated by a particular annotation.
          * */
         private <T, V extends Annotation> List<Field> getAnnotatedFields(Class<T> mappedClass, Class<V> annotation) {
             Field[] fields = mappedClass.getDeclaredFields();
             List<Field> annotatedFields = new ArrayList<Field>();
 
             for (Field field : fields) {
                 if (field.isAnnotationPresent(annotation)) {
                     annotatedFields.add(field);
                 }
             }
 
             return annotatedFields;
         }
 
         private String getMethodName(String fieldName, String prefix) {
             char[] fieldNameArray = fieldName.toCharArray();
             fieldNameArray[0] = Character.toUpperCase(fieldNameArray[0]);
 
             return prefix + new String(fieldNameArray);
         }
 
         /**
          * Creates and returns name of the set method from string representation of the field.
          *
          * @param fieldName String representation of class field.
          * @return String representation of the set method.
          * */
         private String getSetMethodName(String fieldName) {
             return getMethodName(fieldName, "set");
         }
 
         private String getGetMethodName(String fieldName) {
             return getMethodName(fieldName, "get");
         }
 
         /**
          * Returns all set methods from the class. This method uses list of fields
          * which is used for retrieving set methods from the class.
          *
          * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
          * @param fields Fields of the class that.
          * @return Set methods of the class.
          * @throws NoSuchMethodException If there is no appropriate method to invoke.
          * */
         private <T> List<Method> getSetMethods(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException {
             List<Method> methods = new ArrayList<Method>();
 
             for (Field field : fields) {
                 String methodName = getSetMethodName(field.getName());
                 methods.add(mappedClass.getMethod(methodName, field.getType()));
             }
 
             return methods;
         }
 
         private <T> List<Method> getGetMethods(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException {
             List<Method> methods = new ArrayList<Method>();
 
             for (Field field : fields) {
                 if (!field.getAnnotation(Column.class).primaryKey()) {
                     String methodName = getGetMethodName(field.getName());
                     methods.add(mappedClass.getMethod(methodName, field.getType()));
                 }
             }
 
             return methods;
         }
 
         private <T> Method getPrimaryKeyMethod(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException{
             for (Field field : fields) {
                 if (field.getAnnotation(Column.class).primaryKey()) {
                     String methodName = getGetMethodName(field.getName());
                     return mappedClass.getMethod(methodName, field.getType());
                 }
             }
 
             throw new IllegalArgumentException("Class " + mappedClass +
                     " doesn't have a field that was annotated by " + Column.class + " annotation with primaryKey = true");
         }
 
         private <T> Map<QueryType, String> createQueries(Class<T> mappedClass) {
             String tableName = getTableName(mappedClass);
             Map<QueryType, String> queries = new HashMap<QueryType, String>();
 
             queries.put(QueryType.CREATE, makeInsertQuery(tableName));
             queries.put(QueryType.READ,   makeSelectQuery(tableName));
             queries.put(QueryType.UPDATE, makeUpdateQuery(tableName));
             queries.put(QueryType.DELETE, makeDeleteQuery(tableName));
 
             return queries;
         }
 
         private String makeInsertQuery(String tableName) {
             StringBuilder builder = new StringBuilder("INSERT INTO ");
             builder.append(tableName);
             builder.append(" VALUES (");
 
             for (int i = 0; i < columns.size(); ++i) {
                 if (i > 0 && i < columns.size() - 1) {
                     builder.append(',');
                 }
 
                 builder.append('?');
             }
             builder.append(")");
 
             return builder.toString();
         }
 
         private String makeSelectQuery(String tableName) {
             StringBuilder builder = new StringBuilder("SELECT * FROM ");
             builder.append(tableName);
             builder.append(" WHERE ");
             builder.append(primaryColumnName);
             builder.append(" = ?");
 
             return builder.toString();
         }
 
         private String makeDeleteQuery(String tableName) {
             StringBuilder builder = new StringBuilder("DELETE FROM ");
             builder.append(tableName);
             builder.append(" WHERE ");
             builder.append(primaryColumnName);
             builder.append(" = ?");
 
             return builder.toString();
         }
 
         private String makeUpdateQuery(String tableName) {
             StringBuilder builder = new StringBuilder("UPDATE ");
             builder.append(tableName);
 
             for (int i = 0; i < columns.size(); ++i) {
                 if (i > 0 && i < columns.size() - 1) {
                     builder.append(',');
                 }
 
                 builder.append("SET ");
                 builder.append(columns.get(i));
                 builder.append(" = ? ");
             }
             builder.append("WHERE ");
             builder.append(primaryColumnName);
             builder.append(" = ?");
 
             return builder.toString();
         }
     }
 
     /**
      * The <code>Cache</code> class is an implementation of the thread-safe cache that
      * keeps pair of mapped class and mapped class information correspondingly.
      * Actually, this cache is an implementation of the thread safe hash map.
      * */
     private static class Cache {
 
         private ReadWriteLock lock;
         private Lock readLock;
         private Lock writeLock;
         private Map<Class, MappedClassInformation> entityCache;
 
         /**
          * Initializes a new instance of the Cache class.
          * */
         public Cache() {
             lock = new ReentrantReadWriteLock();
             readLock = lock.readLock();
             writeLock = lock.writeLock();
             entityCache = new HashMap<Class, MappedClassInformation>();
         }
 
         /**
          * Tests whether cache contains a particular key or not.
          *
          * @param key Key whose presence in this map is to be tested
          * @return True if cache contains the key. False otherwise.
          * */
         public boolean containsKey(Class key) {
             readLock.lock();
             try {
                 return entityCache.containsKey(key);
             } finally {
                 readLock.unlock();
             }
         }
 
         /**
          * Associates the specified value with the specified key in the map If the map already
          * contained the key in the map then the associated is replaced by new value.
          *
          * @param mappedClass The value of Class that uses as a key in the map.
          * @param classInformation The instance of MappedClassInformation that uses as value in the map.
          * @return The value of MappedClassInformation that was associated with a particular key.
          * */
         public MappedClassInformation put(Class mappedClass, MappedClassInformation classInformation) {
             writeLock.lock();
             try {
                 return entityCache.put(mappedClass, classInformation);
             } finally {
                 writeLock.unlock();
             }
         }
 
         /**
          * Returns the value to which the specified key is mapped.
          *
          * @param key Key whose presence in this map is to be tested
          * @return Instance of the MappedClassInformation if the key is presence in the map. Null otherwise.
          * */
         public MappedClassInformation get(Class key) {
             readLock.lock();
             try {
                 return entityCache.get(key);
             } finally {
                 readLock.unlock();
             }
         }
     }
 
     private static Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
     private static Cache cache = new Cache();
     private Connection connection;
 
 
     /**
      * Initializes a new instance of the DatabaseConnection class and trying to connect to the database.
      * Instance is specified by the value of DatabaseInformation class that keeps all the information needed to connect.
      *
      * @param databaseInformation instance of {@code DatabaseInformation} class that includes
      *                            all the information for making connection to the database.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If any problem with register JDBC driver occurs.
      * */
     public DatabaseConnection(DatabaseInformation databaseInformation) throws ClassNotFoundException, SQLException {
         connection = getConnection(databaseInformation);
     }
 
     public void beginTransaction() throws SQLException {
         connection.setAutoCommit(false);
     }
 
     public void commit() throws SQLException {
         connection.commit();
        connection.setAutoCommit(true);
     }
 
     public void rollback() throws SQLException {
         connection.rollback();
     }
 
     public int create(Object object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, SQLException {
         List<Object> parameters = new ArrayList<Object>();
         parameters.add(getPrimaryKeyMethodValue(object));
         parameters.add(Arrays.asList(getParameters(object)));
 
         MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
         String query = classInformation.getQuery(QueryType.CREATE);
 
         return executeUpdate(query, parameters.toArray());
     }
 
     public <T> T read(Class<T> mappedClass, Object primaryKey) throws NoSuchMethodException, SQLException, InstantiationException, IllegalAccessException, InvocationTargetException {
         MappedClassInformation classInformation = getMappedClassInformation(mappedClass);
         String query = classInformation.getQuery(QueryType.READ);
 
         return executeQuery(query, mappedClass, primaryKey);
     }
 
     public int udpate(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException {
         List<Object> parameters = new ArrayList<Object>();
         parameters.add(Arrays.asList(getParameters(object)));
         parameters.add(getPrimaryKeyMethodValue(object));
         MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
         String query = classInformation.getQuery(QueryType.UPDATE);
 
         return executeUpdate(query, parameters.toArray());
     }
 
     public int delete(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException {
         MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
         String query = classInformation.getQuery(QueryType.DELETE);
         Object primaryKey = getPrimaryKeyMethodValue(object);
 
         return executeUpdate(query, primaryKey);
     }
 
     /**
      * Builds and executes update SQL query. This method returns the number of rows that have been affected.
      *
      * Here is an example of usage:
      * <p>
      *     <code>
      *         connection.executeUpdate("DELETE FROM person WHERE id = ? AND name = ?", id, name);
      *     </code>
      * </p>
      *
      * As you could see in the example above, this method takes the string representation of SQL update query
      * and the arbitrary number of parameters.
      * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
      * and <code>name</code> variable is assigned to "John".
      *
      * Then you'll get the following SQL query:
      *
      * <p>
      *     <code>
      *         "DELETE FROM person WHERE id = 1 AND name = 'John'"
      *     </code>
      * </p>
      *
      * @param query SQL update query (UPDATE, DELETE, INSERT) that can include wildcard symbol - "?".
      * @param parameters Arbitrary number of parameters that will be used to substitute wildcard
      *                   symbols in the SQL query parameter.
      * @return Number of rows that have been affected.
      * @throws SQLException If any database access error happened
      * */
     public int executeUpdate(String query, Object... parameters) throws SQLException {
         PreparedStatement preparedStatement = null;
         int result = 0;
 
         try {
             try {
                 preparedStatement = createPreparedStatement(query, parameters);
                 result = preparedStatement.executeUpdate();
             } finally {
                 close(preparedStatement);
             }
         } catch (SQLException ex) {
             logger.error("Database access error has occurred", ex);
             close();
         }
 
         return result;
     }
 
     /**
      * Executes prepared SQL query.
      * This method returns the corresponding object, whose type was pointed as a second parameter.
      *
      * Here is an example of usage:
      * <p>
      *     <code>
      *         Person person = connection.executeQuery("SELECT * FROM person WHERE id = ?", Person.class, id);
      *     </code>
      * </p>
      *
      * As you could see in the example above, this method takes the string representation of SQL update query
      * and the arbitrary number of parameters. Its also takes a type of class that will be used for building result object.
      * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
      *
      * Then you'll get the following SQL query:
      *
      * <p>
      *     <code>
      *         SELECT * FROM person WHERE id = 1"
      *     </code>
      * </p>
      *
      * @param query A SQL select query that can have the wildcard symbols.
      * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
      * @param parameters Arbitrary number of parameters that will be used to substitute wildcard
      *                   symbols in the SQL query parameter.
      * @return Mapped object that was created by based on the data from the result set.
      * @throws SQLException If any database access problems happened.
      * @throws InvocationTargetException If there is any problem with invocation.
      * @throws NoSuchMethodException If there is no appropriate method to invoke
      * @throws InstantiationException If there is any problem with creating object.
      * @throws IllegalAccessException If there is some problem with object fields accessibility.
      * */
     public <T> T executeQuery(String query, Class<T> mappedClass, Object... parameters) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
         PreparedStatement preparedStatement = null;
         ResultSet resultSet = null;
         T result = null;
 
         try {
             try {
                 preparedStatement = createPreparedStatement(query, parameters);
                 resultSet = preparedStatement.executeQuery();
 
                 if (resultSet.next()) {
                     result = createEntity(resultSet, mappedClass);
                 }
             } finally {
                 close(resultSet);
                 close(preparedStatement);
             }
         } catch (SQLException ex) {
             logger.error("Database access error has occurred", ex);
             close();
         }
 
         return result;
     }
 
     /**
      * Executes prepared SQL query.
      * This method returns the collection of the corresponding objects,
      * whose type was pointed as a second parameter.
      *
      * Here is example of usage:
      * <p>
      * <code>
      * List<Person> persons = connection.executeQueryForCollection("SELECT * FROM person WHERE id = ?", Person.class, id);
      * </code>
      * </p>
      *
      * As you could see in the example above, this method takes the string representation of SQL update query
      * and the arbitrary number of parameters. Its also takes a type of class that will be used for building result object.
      * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
      *
      * Then you'll get the following SQL query:
      *
      * <p>
      *     <code>
      *         SELECT * FROM person WHERE id = 1"
      *     </code>
      * </p>
      *
      * @param query A SQL select query that can have the wildcard symbols.
      * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
      * @param parameters An Arbitrary number of parameters that will be used to substitute wildcard
      *                   symbols in the SQL query parameter.
      * @return A List of the entity objects. Objects have type that was pointed as a second parameter.
      * @throws SQLException If any database access problems happened.
      * @throws InvocationTargetException If there is any problem with invocation.
      * @throws NoSuchMethodException If there is no appropriate method to invoke
      * @throws InstantiationException If there is any problem with creating object.
      * @throws IllegalAccessException If there is some problem with accessibility.
      * */
     public <T> List<T> executeQueryForCollection(String query, Class<T> mappedClass, Object... parameters) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
         PreparedStatement preparedStatement = null;
         ResultSet resultSet = null;
         List<T> collection = new ArrayList<T>();
 
         try {
             try {
                 preparedStatement = createPreparedStatement(query, parameters);
                 resultSet = preparedStatement.executeQuery();
 
                 while (resultSet.next()) {
                     collection.add(createEntity(resultSet, mappedClass));
                 }
             } finally {
                 close(resultSet);
                 close(preparedStatement);
             }
         } catch (SQLException ex) {
             logger.error("Database access error has occurred", ex);
             close();
         }
 
         return collection;
     }
 
     /**
      * Makes connection to the database.
      *
      * @param databaseInformation Instance of the DatabaseInformation class that keeps all the information
      *                            about database connection like database driver's name, url, username and password.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If any problem with register JDBC driver occurs.
      * */
      public void connect(DatabaseInformation databaseInformation) throws SQLException, ClassNotFoundException {
         connection = getConnection(databaseInformation);
      }
 
     /**
      * Closes database connection.
      *
      * @throws SQLException If any database access problems happened.
      * */
     public void close() throws SQLException {
         connection.close();
         logger.info("Database connection has been closed");
     }
 
     /**
      * Tests whether database connection is closed or not.
      *
      * @return true if database connection is closed. false otherwise
      * @throws SQLException If any database access problems happened.
      * */
     public boolean isClosed() throws SQLException {
         return connection.isClosed();
     }
 
     /**
      * Returns raw connection object.
      *
      * @return Connection object corresponding to the DatabaseConnection object.
      * */
     public Connection getRawConnection() {
         return connection;
     }
 
     @SuppressWarnings("unchecked")
     private Object[] getParameters(Object object) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
         List<Object> parameters = new ArrayList<Object>();
         MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
         List<Method> getMethods = classInformation.getGetMethods();
 
         for (Method method : getMethods) {
             parameters.add(method.invoke(object));
         }
 
         return parameters.toArray();
     }
 
     /**
      * Builds prepared statement. This method takes the string representation of SQL query and the arbitrary
      * number of wildcard parameters.
      * This method substitutes every wildcard symbol in the SQL query on the corresponding wildcard
      * parameter that was placed like the second and subsequent argument after SQL query.
      *
      * Here's how it works:
      * In the example below, there are two wildcard symbols and two wildcard parameters.
      *
      * <code>
      *     createPreparedStatement("SELECT * FROM person WHERE id = ? AND name = ?", 1, "John");
      * </code>
      *
      * The query above will be converted to the following representation:
      * <code>
      *     "SELECT * FROM person WHERE id = 1 AND name = 'John'"
      * </code>
      *
      * @param query SQL query that can keep wildcard symbols.
      * @param parameters Arbitrary number of parameters that will be used
      *                   to substitute the wildcard symbols in the SQL query.
      * @return object of the PreparedStatement class.
      * @throws SQLException If any database access problems happened.
      * */
     private PreparedStatement createPreparedStatement(String query, Object... parameters) throws SQLException {
         PreparedStatement preparedStatement = connection.prepareStatement(query);
 
         for (int parameterIndex = 0; parameterIndex < parameters.length; ++parameterIndex) {
             preparedStatement.setObject(parameterIndex + 1, parameters[parameterIndex]);
         }
 
         return preparedStatement;
     }
 
     /**
      * Closes result set object
      *
      * @throws SQLException If any database access problems happened.
      * */
     private void close(ResultSet resultSet) throws SQLException {
         if (resultSet != null) {
             resultSet.close();
         }
     }
 
     /**
      * Closes prepared statement object
      *
      * @throws SQLException If any database access problems happened.
      * */
     private void close(Statement statement) throws SQLException {
         if (statement != null) {
             statement.close();
         }
     }
 
     @SuppressWarnings("unchecked")
     private MappedClassInformation getMappedClassInformation(Class mappedClass) throws NoSuchMethodException {
         if (cache.containsKey(mappedClass)) {
            return cache.get(mappedClass);
         }
 
         MappedClassInformation classInformation = new MappedClassInformation(mappedClass);
         cache.put(mappedClass, classInformation);
 
         return classInformation;
     }
 
     private Object getPrimaryKeyMethodValue(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
 
         return classInformation.getPrimaryKeyGetMethod().invoke(object);
     }
 
     /**
      * Registers JDBC driver and trying to connect to the database.
      *
      * @param databaseInformation Instance of the DatabaseInformation class that keeps all the information
      *                            about database connection like database driver's name, url, username and password.
      * @return A new Connection object associated with particular database.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If any problem with register JDBC driver occurs.
      * */
     private Connection getConnection(DatabaseInformation databaseInformation) throws ClassNotFoundException, SQLException {
         String url = databaseInformation.getUrl();
         String username = databaseInformation.getUsername();
         String password = databaseInformation.getPassword();
 
         Class.forName(databaseInformation.getDriver());
         connection = DriverManager.getConnection(url, username, password);
 
         logger.info("Database connection has been opened");
 
         return connection;
     }
 
     /**
      * Gets the data from the result set and tries to build an entity object.
      * The entity class must have a constructor with corresponding number
      * of parameters that have an appropriate types according to data from the result set.
      *
      * @param resultSet Result set that was retrieved from the database.
      * @param mappedClass Class, whose annotated fields will be used for creating corresponding entity.
      * @return Instance of the class that has been pointed as a second parameter.
      * @throws SQLException If any database access problems happened.
      * @throws InvocationTargetException If there is any problem with invocation.
      * @throws NoSuchMethodException If there is no appropriate method to invoke
      * @throws InstantiationException If there is any problem with creating object.
      * @throws IllegalAccessException If there is some problem with accessibility.
      * */
      @SuppressWarnings("unchecked")
      private <T> T createEntity(ResultSet resultSet, Class<T> mappedClass) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
         return (T) createEntitySupporter(resultSet, getMappedClassInformation(mappedClass));
      }
 
     /**
      * Gets the information from the result set that has been pointed
      * as a first argument and trying to build instance of the particular class.
      * Information about the class holds in the MappedClassInformation instance
      * that has been pointed as a second argument.
      * This method invokes all set methods mapped class for setting fields
      * information that has been retrieved from result set.
      *
      * @param resultSet Result set that was retrieved from the database.
      * @param classInformation Instance of MappedClassInformation that holds all information about mapped class.
      * @return Instance of the class that has been pointed as a second parameter.
      * @throws IllegalAccessException If there is some problem with accessibility.
      * @throws InvocationTargetException If there is any problem with invocation.
      * @throws InstantiationException If there is any problem with creating object.
      * @throws SQLException If any database access problems happened.
      * */
      private <T> T createEntitySupporter(ResultSet resultSet, MappedClassInformation<T> classInformation) throws IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
         List<Method> setMethods = classInformation.getSetMethods();
         List<String> columns = classInformation.getColumns();
         T result = classInformation.getConstructor().newInstance();
 
         for (int i = 0; i < columns.size(); ++i) {
             Object data = resultSet.getObject(columns.get(i));
             setMethods.get(i).invoke(result, data);
         }
 
         return result;
     }
 }
