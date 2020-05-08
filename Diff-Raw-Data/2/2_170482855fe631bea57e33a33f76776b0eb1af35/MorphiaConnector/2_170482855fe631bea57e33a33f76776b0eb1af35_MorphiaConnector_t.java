 /**
  * Mule Morphia Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.morphia;
 
 import com.google.code.morphia.AuthenticationException;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Key;
 import com.google.code.morphia.Morphia;
 import com.google.code.morphia.logging.MorphiaLoggerFactory;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.MongoOptions;
 import com.mongodb.ServerAddress;
 import com.mongodb.WriteResult;
 import com.mongodb.util.JSON;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.mule.api.ConnectionException;
 import org.mule.api.ConnectionExceptionCode;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Connect;
 import org.mule.api.annotations.ConnectionIdentifier;
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Disconnect;
 import org.mule.api.annotations.Mime;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.ValidateConnection;
 import org.mule.api.annotations.display.Password;
 import org.mule.api.annotations.param.ConnectionKey;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.transformer.types.MimeTypes;
 
 import javax.annotation.PostConstruct;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Morphia is a lightweight type-safe library for mapping Java objects to/from MongoDB:
  * <p/>
  * <ul>
  * <li>Easy to use, and very lightweight; reflection is used once per type and cached for good performance.</li>
  * <li>Datastore and DAO<T,V> access abstractions, or roll your own...</li>
  * <li>Type-safe, and Fluent Query support with (runtime) validation</li>
  * <li>Annotations based mapping behavior; there are no XML files.</li>
  * <li>Extensions: Validation (jsr303), and SLF4J Logging</li>
  * </ul>
  *
  * @author MuleSoft, Inc.
  */
 @Connector(name = "morphia", schemaVersion = "1.0")
 public class MorphiaConnector {
 
     /**
      * List of class names with mappings
      */
     @Configurable
     @Optional
     private List<String> classes;
 
     /**
      * List of packages with mappings
      */
     @Configurable
     @Optional
     private List<String> packages;
 
     /**
      * Specifies whether to ignore classes in the package that cannot be mapped
      */
     @Configurable
     @Optional
     @Default("true")
     private boolean ignoreInvalidClasses;
 
     /**
      * Ensure indexes on the background
      */
     @Configurable
     @Optional
     @Default("true")
     private boolean ensureIndexesOnBackground;
 
 
     /**
      * The number of connections allowed per host (the pool size, per host)
      */
     @Configurable
     @Optional
     public Integer connectionsPerHost;
 
     /**
      * Multiplier for connectionsPerHost for # of threads that can block
      */
     @Configurable
     @Optional
     public Integer threadsAllowedToBlockForConnectionMultiplier;
 
     /**
      * The max wait time for a blocking thread for a connection from the pool in ms.
      */
     @Configurable
     @Optional
     public Integer maxWaitTime;
 
     /**
      * The connection timeout in milliseconds; this is for establishing the socket connections (open). 0 is default and infinite.
      */
     @Configurable
     @Optional
     private Integer connectTimeout;
 
     /**
      * The socket timeout. 0 is default and infinite.
      */
     @Configurable
     @Optional
     private Integer socketTimeout;
 
     /**
      * This controls whether the system retries automatically on connection errors.
      */
     @Configurable
     @Optional
     private Boolean autoConnectRetry;
 
     /**
      * Specifies if the driver is allowed to read from secondaries or slaves.
      */
     @Configurable
     @Optional
     private Boolean slaveOk;
 
     /**
      * If the driver sends a getLastError command after every update to ensure it succeeded.
      */
     @Configurable
     @Optional
     public Boolean safe;
 
     /**
      * If set, the w value of WriteConcern for the connection is set to this.
      */
     @Configurable
     @Optional
     public Integer w;
 
     /**
      * If set, the wtimeout value of WriteConcern for the connection is set to this.
      */
     @Configurable
     @Optional
     public Integer wtimeout;
 
     /**
      * Sets the fsync value of WriteConcern for the connection.
      */
     @Configurable
     @Optional
     public Boolean fsync;
 
 
     /**
      * Morphia datastore
      */
     private Datastore datastore;
 
     /**
      * Morphia interface
      */
     private Morphia morphia;
 
     static {
         MorphiaLoggerFactory.registerLogger(SFL4JLogrFactory.class);
     }
 
     /**
      * Construct a new instance of Morphia
      */
     @PostConstruct
     public void init() throws ClassNotFoundException {
         this.morphia = new Morphia();
 
         if (classes != null) {
             for (String className : classes) {
                 this.morphia.map(Class.forName(className));
             }
         }
 
         if (packages != null) {
             for (String packageName : packages) {
                 this.morphia.mapPackage(packageName, ignoreInvalidClasses);
             }
         }
     }
 
     /**
      * Method invoked when a new datastore needs to be accessed
      *
      * @param username the username to use in case authentication is required
      * @param password the password to use in case authentication is required, null
      *                 if no authentication is desired
      * @param host     The host of the Mongo server
      * @param port     The port of the Mongo server
      * @param database The database name of the Mongo server
      * @throws org.mule.api.ConnectionException
      *          if there is a connection exception
      */
     @Connect
     public void connect(String username, @Password String password,
                         @Optional @Default("localhost") String host,
                         @Optional @Default("27017") int port,
                         @ConnectionKey String database) throws ConnectionException {
         try {
             MongoOptions options = new MongoOptions();
 
             if (connectionsPerHost != null) options.connectionsPerHost = connectionsPerHost;
             if (threadsAllowedToBlockForConnectionMultiplier != null)
                 options.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
             if (maxWaitTime != null) options.maxWaitTime = maxWaitTime;
             if (connectTimeout != null) options.connectTimeout = connectTimeout;
             if (socketTimeout != null) options.socketTimeout = socketTimeout;
             if (autoConnectRetry != null) options.autoConnectRetry = autoConnectRetry;
             if (slaveOk != null) options.slaveOk = slaveOk;
             if (safe != null) options.safe = safe;
             if (w != null) options.w = w;
             if (wtimeout != null) options.wtimeout = wtimeout;
             if (fsync != null) options.fsync = fsync;
 
             this.datastore = this.morphia.createDatastore(new Mongo(new ServerAddress(host, port), options), database, username, password.toCharArray());
             this.datastore.ensureIndexes(ensureIndexesOnBackground);
             this.datastore.ensureCaps();
         } catch (AuthenticationException ae) {
             throw new ConnectionException(ConnectionExceptionCode.INCORRECT_CREDENTIALS, null, ae.getMessage(), ae);
         } catch (UnknownHostException e) {
             throw new ConnectionException(ConnectionExceptionCode.UNKNOWN_HOST, null, e.getMessage(), e);
         }
     }
 
     /**
      * Disconnect
      */
     @Disconnect
     public void disconnect() {
         this.datastore = null;
     }
 
     /**
      * Are we connected
      */
     @ValidateConnection
     public boolean isConnected() {
         return this.datastore != null;
     }
 
     /**
      * Are we connected
      */
     @ConnectionIdentifier
     public String connectionId() {
         return "Morphia [host=" + this.datastore.getMongo().getConnector().getAddress().getHost() + "] [port=" + this.datastore.getMongo().getConnector().getAddress().getPort() + "] [database=" + this.datastore.getDB().getName() + "]";
     }
 
     /**
      * Add a new user to the database
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:add-user}
      *
      * @param newUsername    Username to be created
      * @param newPassword    Password that will be used for authentication
      * @param targetDatabase Database at which this user will be created. It defaults to the current one if not specified.
      */
     @Processor
     public void addUser(String newUsername, String newPassword, @Optional String targetDatabase) {
         WriteResult writeResult = null;
        if (targetDatabase == null) {
             writeResult = this.datastore.getDB().addUser(newUsername, newPassword.toCharArray());
         } else {
             writeResult = this.datastore.getMongo().getDB(targetDatabase).addUser(newUsername, newPassword.toCharArray());
         }
         if (!writeResult.getLastError().ok()) {
             throw new MongoException(writeResult.getLastError().getErrorMessage());
         }
     }
 
 
     /**
      * Drop the current database
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:drop-database}
      */
     @Processor
     public void dropDatabase() {
         this.datastore.getDB().dropDatabase();
     }
 
     /**
      * Saves the entity (Object) and updates the @Id field
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:save}
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:save-with-ref}
      *
      * @param object       Object to be saved
      * @param writeConcern Sets the write concern for this database. It Will be used for writes to any collection in
      *                     this database. See the documentation for {@link WriteConcern} for more information.
      * @return An instance of {@link com.google.code.morphia.Key}
      */
     @Processor
     public Object save(@Optional @Default("#[payload]") Object object, @Optional @Default("NORMAL") WriteConcern writeConcern) {
         return this.datastore.save(object, writeConcern.getMongoWriteConcern());
     }
 
     /**
      * Does the object exists?
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:exists}
      *
      * @param filters   Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @param className Class name of the object to retrieve
      * @return True if it exists, false otherwise
      * @throws ClassNotFoundException If the class cannot be found
      */
     @Processor
     public boolean exists(String className, Map<String, Object> filters) throws ClassNotFoundException {
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setFilters(filters);
         return this.datastore.getCount(queryBuilder.getQuery()) > 0;
     }
 
     /**
      * Does the object exists?
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:exists-by-id}
      *
      * @param id        Id of the object to retrieve
      * @param className Class name of the object to retrieve
      * @return True if it exists, false otherwise
      * @throws ClassNotFoundException If the class cannot be found
      */
     @Processor
     public boolean existsById(String className, Object id) throws ClassNotFoundException {
         return this.datastore.exists(new Key(Class.forName(className), id)) != null;
     }
 
 
     /**
      * Retrieve an entity (Object) using the specified id
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:retrieve}
      *
      * @param id        Id of the object to retrieve
      * @param className Class name of the object to retrieve
      * @return The retrieve object or null if cannot be found
      * @throws ClassNotFoundException If the class cannot be found
      */
     @Processor
     public Object retrieve(String className, Object id) throws ClassNotFoundException {
         return this.datastore.get(Class.forName(className), id);
     }
 
     /**
      * Count total objects of the specified class
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:count-by-class}
      *
      * @param className Class of the object to count
      * @return The count
      * @throws ClassNotFoundException If the class cannot be found
      */
     @Processor
     public long countByClass(String className) throws ClassNotFoundException {
         return this.datastore.getCount(Class.forName(className));
     }
 
     /**
      * Count total objects using the embedded querying criteria
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:count}
      *
      * @param className The name of the object class to count
      * @param filters   Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @return The count
      * @throws Exception if there is an exception
      */
     @Processor
     public Object count(String className, Map<String, Object> filters) throws Exception {
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setFilters(filters);
         return this.datastore.getCount(queryBuilder.getQuery());
     }
 
     /**
      * Find all instances by type using the specified query criteria
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:find}
      *
      * @param className            Type class name
      * @param filters              Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @param offset               Starts the query results at a particular zero-based offset.
      * @param limit                Limit the fetched result set to a certain number of values.
      * @param order                Sorts based on a property (defines return order).
      * @param fields               Limit the returned fields to the ones specified.
      * @param disableCursorTimeout Disables cursor timeout on server
      * @param disableSnapshotMode  Disable snapshotted mode (default mode). This will be faster but changes made
      *                             during the cursor may cause duplicates.
      * @param disableValidation    Turns off validation
      * @return An object collection
      * @throws Exception if there is an exception
      */
     @Processor
     public Object find(String className,
                        @Optional Map<String, Object> filters,
                        @Optional Integer offset,
                        @Optional Integer limit,
                        @Optional String order,
                        @Optional List<String> fields,
                        @Optional Boolean disableCursorTimeout,
                        @Optional Boolean disableSnapshotMode,
                        @Optional Boolean disableValidation) throws Exception {
 
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setDisableCursorTimeout(disableCursorTimeout)
                 .setDisableValidation(disableValidation)
                 .setDisableSnapshotMode(disableSnapshotMode)
                 .setFilters(filters)
                 .setOffset(offset)
                 .setLimit(limit)
                 .setOrder(order)
                 .setFields(fields);
 
         return queryBuilder.getQuery().asList();
     }
 
 
     /**
      * Find a single instance of type using the specified query criteria
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:find-single}
      *
      * @param className            Type class name
      * @param filters              Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @param fields               Limit the returned fields to the ones specified.
      * @param disableCursorTimeout Disables cursor timeout on server
      * @param disableSnapshotMode  Disable snapshotted mode (default mode). This will be faster but changes made
      *                             during the cursor may cause duplicates.
      * @param disableValidation    Turns off validation
      * @return An object collection
      * @throws Exception if there is an exception
      */
     @Processor
     public Object findSingle(String className,
                              @Optional Map<String, Object> filters,
                              @Optional List<String> fields,
                              @Optional Boolean disableCursorTimeout,
                              @Optional Boolean disableSnapshotMode,
                              @Optional Boolean disableValidation) throws Exception {
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setDisableCursorTimeout(disableCursorTimeout)
                 .setDisableValidation(disableValidation)
                 .setDisableSnapshotMode(disableSnapshotMode)
                 .setFilters(filters)
                 .setFields(fields);
 
         return queryBuilder.getQuery().get();
     }
 
     /**
      * Find all object ids of type using the specified query criteria
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:find-ids}
      *
      * @param className            Type class name
      * @param filters              Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @param offset               Starts the query results at a particular zero-based offset.
      * @param limit                Limit the fetched result set to a certain number of values.
      * @param order                Sorts based on a property (defines return order).
      * @param disableCursorTimeout Disables cursor timeout on server
      * @param disableSnapshotMode  Disable snapshotted mode (default mode). This will be faster but changes made
      *                             during the cursor may cause duplicates.
      * @param disableValidation    Turns off validation
      * @return A collection of {@link Key}s
      * @throws Exception if there is an exception
      */
     @Processor
     public Object findIds(String className,
                           @Optional Map<String, Object> filters,
                           @Optional Integer offset,
                           @Optional Integer limit,
                           @Optional String order,
                           @Optional Boolean disableCursorTimeout,
                           @Optional Boolean disableSnapshotMode,
                           @Optional Boolean disableValidation) throws Exception {
 
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setDisableCursorTimeout(disableCursorTimeout)
                 .setDisableValidation(disableValidation)
                 .setDisableSnapshotMode(disableSnapshotMode)
                 .setFilters(filters)
                 .setOffset(offset)
                 .setLimit(limit)
                 .setOrder(order);
 
         List<?> result = queryBuilder.getQuery().asKeyList();
         if (result.size() == 1) {
             return result.get(0);
         } else {
             return result;
         }
     }
 
     /**
      * Deletes the given entity (by id)
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:delete-by-id}
      *
      * @param className Class of the object to delete
      * @param id        Id of the object to delete
      * @return An instance of @{link WriteResult}
      * @throws ClassNotFoundException If the class cannot be found
      */
     @Processor
     public WriteResult deleteById(String className, Object id) throws ClassNotFoundException {
         return this.datastore.delete(Class.forName(className), id);
     }
 
     /**
      * Deletes the given entity (by @Id)
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:delete}
      *
      * @param object Object to delete
      * @return An instance of @{link WriteResult}
      * @throws Exception if there is an exception
      */
     @Processor
     public WriteResult delete(@Optional @Default("#[payload]") Object object) throws Exception {
         return this.datastore.delete(object);
     }
 
     /**
      * Deletes the given entities based on the query (first item only).
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:find-and-delete}
      *
      * @param className            Type class name
      * @param filters              Filter criteria. Criteria is a composite of the field name and the operator ("field >", or "field in"). All criteria are implicitly combined with a logical "and".
      * @param offset               Starts the query results at a particular zero-based offset.
      * @param limit                Limit the fetched result set to a certain number of values.
      * @param order                Sorts based on a property (defines return order).
      * @param fields               Limit the returned fields to the ones specified.
      * @param disableCursorTimeout Disables cursor timeout on server
      * @param disableSnapshotMode  Disable snapshot mode (default mode). This will be faster but changes made
      *                             during the cursor may cause duplicates.
      * @param disableValidation    Turns off validation
      * @return the deleted Entity
      * @throws Exception if there is an exception
      */
     @Processor
     public Object findAndDelete(String className,
                                 @Optional Map<String, Object> filters,
                                 @Optional Integer offset,
                                 @Optional Integer limit,
                                 @Optional String order,
                                 @Optional List<String> fields,
                                 @Optional Boolean disableCursorTimeout,
                                 @Optional Boolean disableSnapshotMode,
                                 @Optional Boolean disableValidation) throws Exception {
 
         QueryBuilder queryBuilder = QueryBuilder.newBuilder(datastore, className)
                 .setDisableCursorTimeout(disableCursorTimeout)
                 .setDisableValidation(disableValidation)
                 .setDisableSnapshotMode(disableSnapshotMode)
                 .setFilters(filters)
                 .setOffset(offset)
                 .setLimit(limit)
                 .setOrder(order)
                 .setFields(fields);
 
         return this.datastore.findAndDelete(queryBuilder.getQuery());
     }
 
 
     /**
      * Transform a Morphia object into JSON
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:object-to-json}
      *
      * @param object Object to transform
      * @return A string containing JSON
      * @throws IOException if there is an exception
      */
     @Processor
     @Mime(MimeTypes.JSON)
     public String objectToJson(@Optional @Default("#[payload]") Object object) throws IOException {
         if (isListClass(object.getClass())) {
             if (((List) object).size() == 0) {
                 throw new IllegalArgumentException("The list is empty");
             }
             if (this.morphia.isMapped(((List) object).get(0).getClass())) {
                 List originalList = (List) object;
                 LinkedList<DBObject> dbObjectList = new LinkedList<DBObject>();
                 for (Object innerObject : originalList) {
                     dbObjectList.addLast(this.morphia.toDBObject(innerObject));
                 }
                 return JSON.serialize(dbObjectList);
             } else if (((List) object).get(0) instanceof Key) {
                 List originalList = (List) object;
                 JsonFactory jsonFactory = new JsonFactory();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream);
                 jsonGenerator.writeStartArray();
                 for (Object innerObject : originalList) {
                     Key keyObject = (Key) innerObject;
                     ObjectId id = (ObjectId) keyObject.getId();
                     jsonGenerator.writeString(id.toStringMongod());
                 }
                 jsonGenerator.writeEndArray();
                 jsonGenerator.flush();
                 return new String(outputStream.toByteArray());
             } else {
                 throw new IllegalArgumentException(((List) object).get(0).getClass().getName() + " is not a Morphia-mapped type");
             }
         } else if (this.morphia.isMapped(object.getClass())) {
             return JSON.serialize(this.morphia.toDBObject(object));
         } else if (object instanceof Key) {
             JsonFactory jsonFactory = new JsonFactory();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream);
             Key keyObject = (Key) object;
             ObjectId id = (ObjectId) keyObject.getId();
             jsonGenerator.writeString(id.toStringMongod());
             jsonGenerator.flush();
             return new String(outputStream.toByteArray());
         } else {
             throw new IllegalArgumentException(object.getClass().getName() + " is not a Morphia-mapped type");
         }
     }
 
     /**
      * Transform a JSON object into Morphia object
      * <p/>
      * {@sample.xml ../../../doc/mule-module-morphia.xml.sample morphia:json-to-object}
      *
      * @param className Name of the class representing the type
      * @param json      String containing JSON
      * @return The parsed object
      * @throws ClassNotFoundException if class not found
      */
     @Processor
     public Object jsonToObject(String className, @Optional @Default("#[payload]") String json) throws ClassNotFoundException {
         Object object = JSON.parse(json);
         if (object == null || !(object instanceof DBObject)) {
             throw new IllegalArgumentException("Unable to convert JSON string into an object");
         }
         return morphia.fromDBObject(Class.forName(className), (DBObject) object);
     }
 
     /**
      * Checks whether the specified class parameter is an instance of {@link List }
      *
      * @param clazz <code>Class</code> to check.
      * @return
      */
     private boolean isListClass(Class clazz) {
         List<Class> classes = new ArrayList<Class>();
         computeClassHierarchy(clazz, classes);
         return classes.contains(List.class);
     }
 
     /**
      * Get all superclasses and interfaces recursively.
      *
      * @param classes List of classes to which to add all found super classes and interfaces.
      * @param clazz   The class to start the search with.
      */
     private void computeClassHierarchy(Class clazz, List classes) {
         for (Class current = clazz; (current != null); current = current.getSuperclass()) {
             if (classes.contains(current)) {
                 return;
             }
             classes.add(current);
             for (Class currentInterface : current.getInterfaces()) {
                 computeClassHierarchy(currentInterface, classes);
             }
         }
     }
 
     private String removeQuotes(String text) {
         String result = text.trim();
         if (result.startsWith("'") || result.startsWith("\"")) {
             result = result.substring(1, result.length() - 1);
         }
         return result;
     }
 
     public List<String> getClasses() {
         return classes;
     }
 
     public void setClasses(List<String> classes) {
         this.classes = classes;
     }
 
     public List<String> getPackages() {
         return packages;
     }
 
     public void setPackages(List<String> packages) {
         this.packages = packages;
     }
 
     public boolean isIgnoreInvalidClasses() {
         return ignoreInvalidClasses;
     }
 
     public void setIgnoreInvalidClasses(boolean ignoreInvalidClasses) {
         this.ignoreInvalidClasses = ignoreInvalidClasses;
     }
 
     public boolean isEnsureIndexesOnBackground() {
         return ensureIndexesOnBackground;
     }
 
     public void setEnsureIndexesOnBackground(boolean ensureIndexesOnBackground) {
         this.ensureIndexesOnBackground = ensureIndexesOnBackground;
     }
 
     public Integer getConnectionsPerHost() {
         return connectionsPerHost;
     }
 
     public void setConnectionsPerHost(Integer connectionsPerHost) {
         this.connectionsPerHost = connectionsPerHost;
     }
 
     public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
         return threadsAllowedToBlockForConnectionMultiplier;
     }
 
     public void setThreadsAllowedToBlockForConnectionMultiplier(Integer threadsAllowedToBlockForConnectionMultiplier) {
         this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
     }
 
     public Integer getMaxWaitTime() {
         return maxWaitTime;
     }
 
     public void setMaxWaitTime(Integer maxWaitTime) {
         this.maxWaitTime = maxWaitTime;
     }
 
     public Integer getConnectTimeout() {
         return connectTimeout;
     }
 
     public void setConnectTimeout(Integer connectTimeout) {
         this.connectTimeout = connectTimeout;
     }
 
     public Integer getSocketTimeout() {
         return socketTimeout;
     }
 
     public void setSocketTimeout(Integer socketTimeout) {
         this.socketTimeout = socketTimeout;
     }
 
     public Boolean getAutoConnectRetry() {
         return autoConnectRetry;
     }
 
     public void setAutoConnectRetry(Boolean autoConnectRetry) {
         this.autoConnectRetry = autoConnectRetry;
     }
 
     public Boolean getSlaveOk() {
         return slaveOk;
     }
 
     public void setSlaveOk(Boolean slaveOk) {
         this.slaveOk = slaveOk;
     }
 
     public Boolean getSafe() {
         return safe;
     }
 
     public void setSafe(Boolean safe) {
         this.safe = safe;
     }
 
     public Integer getW() {
         return w;
     }
 
     public void setW(Integer w) {
         this.w = w;
     }
 
     public Integer getWtimeout() {
         return wtimeout;
     }
 
     public void setWtimeout(Integer wtimeout) {
         this.wtimeout = wtimeout;
     }
 
     public Boolean getFsync() {
         return fsync;
     }
 
     public void setFsync(Boolean fsync) {
         this.fsync = fsync;
     }
 }
