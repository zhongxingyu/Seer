 package org.h2o.db;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2o.autonomic.decision.ranker.metric.ActionRequest;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.SystemTableProxy;
 import org.h2o.db.manager.TableManagerProxy;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.query.TableProxy;
 import org.h2o.db.query.asynchronous.CommitResult;
 import org.h2o.db.query.locking.LockRequest;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.replication.ReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import uk.ac.standrews.cs.nds.rpc.DeserializationException;
 import uk.ac.standrews.cs.nds.rpc.JSONValue;
 import uk.ac.standrews.cs.nds.rpc.Marshaller;
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.stachord.impl.ChordRemoteMarshaller;
 import uk.ac.standrews.cs.stachord.interfaces.IChordRemoteReference;
 
 public class H2OMarshaller extends Marshaller {
 
     private static final String EXPECTED_UPDATE_ID = "expectedUpdateID";
     private static final String COMMIT = "commit";
     private static final String UPDATE_ID = "updateID";
     private static final String REQUESTING_DATABASE = "requestingDatabase";
     private static final String TABLE_MANAGER2 = "tableManager";
     private static final String LOCK_REQUESTED = "lockRequested";
     private static final String LOCK_GRANTED = "lockGranted";
     private static final String TABLE_MANAGER_URL = "tableManagerURL";
     private static final String TABLE_MANAGER = TABLE_MANAGER2;
     private static final String TABLE_INFO = "tableInfo";
     private static final String DISK = "disk";
     private static final String NETWORK = "network";
     private static final String MEMORY = "memory";
     private static final String CPU = "cpu";
     private static final String IMMEDIATE_DISK_SPACE = "immediateDiskSpace";
     private static final String EXPECTED_TIME_TO_COMPLETION = "expectedTimeToCompletion";
     private static final String ACTIVE = "active";
     private static final String DATABASE_INSTANCE = "databaseInstance";
     private static final String PRIMARY_LOCATION = "primaryLocation";
     private static final String ACTIVE_REPLICAS = "activeReplicas";
     private static final String ALL_REPLICAS = "allReplicas";
     private static final String SESSION_ID = "sessionID";
     private static final String DATABASE_MAKING_REQUEST = "databaseMakingRequest";
     private static final String TABLE_TYPE = "tableType";
     private static final String TABLE_SET = "tableSet";
     private static final String MODIFICATION_ID = "modificationID";
     private static final String SCHEMA_NAME = "schemaName";
     private static final String TABLE_NAME = "tableName";
     private static final String DATABASE_URL = "databaseURL";
     private static final String DATABASE_ID = "databaseID";
     private static final String DATABASE_LOCATION = "dbLocation";
     private static final String KEYS = "keys";
     private static final String VALUES = "values";
     private static final String WRAPPER = "wrapper";
 
     private final ChordRemoteMarshaller chord_marshaller;
 
     public H2OMarshaller() {
 
         chord_marshaller = new ChordRemoteMarshaller();
     }
 
     /////////////////////
 
     public JSONValue serializeChordRemoteReference(final IChordRemoteReference source) {
 
         return chord_marshaller.serializeChordRemoteReference(source);
     }
 
     public IChordRemoteReference deserializeChordRemoteReference(final JSONObject object) throws DeserializationException {
 
         return chord_marshaller.deserializeChordRemoteReference(object);
     }
 
     /////////////////////
 
     public JSONValue serializeITableManagerRemote(final ITableManagerRemote source) { // yes is a reference type
 
         try {
             return serializeInetSocketAddress(source.getAddress());
         }
         catch (final RPCException e) {
             ErrorHandling.exceptionError(e, "Unexpected RPCException.");
             return JSONValue.NULL;
         }
     }
 
     public ITableManagerRemote deserializeITableManagerRemote(final String address_string) throws DeserializationException {
 
         final InetSocketAddress address = deserializeInetSocketAddress(address_string);
 
         return TableManagerProxy.getProxy(address);
     }
 
     /////////////////////
 
     public JSONValue serializeISystemTableMigratable(final ISystemTableMigratable source) { // yes is a reference type
 
         try {
             return serializeInetSocketAddress(source.getAddress());
         }
         catch (final RPCException e) {
             ErrorHandling.exceptionError(e, "Unexpected RPCException.");
             return JSONValue.NULL;
         }
     }
 
     public ISystemTableMigratable deserializeISystemTableMigratable(final String address_string) throws DeserializationException {
 
         final InetSocketAddress address = deserializeInetSocketAddress(address_string);
 
         return SystemTableProxy.getProxy(address);
     }
 
     /////////////////////
 
     public JSONValue serializeIDatabaseInstanceRemote(final IDatabaseInstanceRemote source) {
 
         try {
             return serializeInetSocketAddress(source.getAddress());
         }
         catch (final RPCException e) {
             ErrorHandling.exceptionError(e, "Unexpected RPCException.");
             return JSONValue.NULL;
         }
     }
 
     public IDatabaseInstanceRemote deserializeIDatabaseInstanceRemote(final String address_string) throws DeserializationException {
 
         final InetSocketAddress address = deserializeInetSocketAddress(address_string);
 
         return DatabaseInstanceProxy.getProxy(address);
     }
 
     /////////////////////
 
     public JSONValue serializeDatabaseID(final DatabaseID source) {
 
         if (source == null) { return JSONValue.NULL; }
         final JSONObject object = new JSONObject();
         try {
             object.put(DATABASE_ID, source.getID());
             object.put(DATABASE_URL, source.getURLwithRMIPort());
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing DatabaseID: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public DatabaseID deserializeDatabaseID(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final String database_id = object.getString(DATABASE_ID);
             final String database_url = object.getString(DATABASE_URL);
             final DatabaseURL url = DatabaseURL.parseURL(database_url);
 
             return new DatabaseID(database_id, url);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeTableInfo(final TableInfo source) {
 
         final String tableName = source.getTableName();
         final String schemaName = source.getSchemaName();
         final JSONValue modificationID = new JSONValue(source.getModificationID());
         final JSONValue tableSet = new JSONValue(source.getTableSet());
         final String tableType = source.getTableType();
         final JSONValue databaseID = serializeDatabaseID(source.getDatabaseID());
 
         final JSONObject object = new JSONObject();
         try {
             object.put(TABLE_NAME, tableName);
             object.put(SCHEMA_NAME, schemaName);
             object.put(MODIFICATION_ID, modificationID);
             object.put(TABLE_SET, tableSet);
 
             if (source.getTableType() != null) {
                 object.put(TABLE_TYPE, tableType);
             }
             else {
                 object.put(TABLE_TYPE, JSONObject.NULL);
             }
 
             if (source.getDatabaseID() != null) {
                 object.put(DATABASE_LOCATION, databaseID.getValue());
             }
             else {
                 object.put(DATABASE_LOCATION, JSONObject.NULL);
             }
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing TableInfo: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public TableInfo deserializeTableInfo(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final String tableName = object.getString(TABLE_NAME);
             final String schemaName = object.getString(SCHEMA_NAME);
             final long modificationID = object.getLong(MODIFICATION_ID);
             final int tableSet = object.getInt(TABLE_SET);
 
             String tableType = null;
             if (!object.isNull(TABLE_TYPE)) {
                 tableType = object.getString(TABLE_TYPE);
             }
 
             DatabaseID dbLocation = null;
             if (!object.isNull(DATABASE_LOCATION)) {
                 dbLocation = deserializeDatabaseID(object.getJSONObject(DATABASE_LOCATION));
             }
             return new TableInfo(tableName, schemaName, modificationID, tableSet, tableType, dbLocation);
         }
         catch (final Exception e) {
             ErrorHandling.exceptionError(e, "Couldn't deserialize TableInfo");
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeLockType(final LockType source) {
 
         return new JSONValue(source.toString());
     }
 
     public LockType deserializeLockType(final String s) throws DeserializationException {
 
         try {
             return LockType.valueOf(LockType.class, s);
 
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeLockRequest(final LockRequest source) {
 
         final DatabaseInstanceWrapper databaseMakingRequest = source.getRequestLocation();
         final int sessionID = source.getSessionID();
 
         final JSONObject object = new JSONObject();
         try {
             object.put(DATABASE_MAKING_REQUEST, serializeDatabaseInstanceWrapper(databaseMakingRequest));
             object.put(SESSION_ID, sessionID);
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockRequest: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public LockRequest deserializeLockRequest(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final DatabaseInstanceWrapper databaseMakingRequest = deserializeDatabaseInstanceWrapper(object.getJSONObject(DATABASE_MAKING_REQUEST));
             final int sessionID = object.getInt(SESSION_ID);
             // ...
 
             return new LockRequest(databaseMakingRequest, sessionID);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeReplicaManager(final ReplicaManager source) {
 
         final JSONValue allReplicas = serializeMapDatabaseInstanceWrapperInteger(source.getActiveReplicas());
         final JSONValue activeReplicas = serializeMapDatabaseInstanceWrapperInteger(source.getAllReplicas());
         final JSONValue primaryLocation = serializeDatabaseInstanceWrapper(source.getPrimaryLocation());
 
         final JSONObject object = new JSONObject();
         try {
             object.put(ALL_REPLICAS, allReplicas);
             object.put(ACTIVE_REPLICAS, activeReplicas);
             object.put(PRIMARY_LOCATION, primaryLocation);
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing ReplicaManager: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public ReplicaManager deserializeReplicaManager(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final Map<DatabaseInstanceWrapper, Integer> allReplicas = deserializeMapDatabaseInstanceWrapperInteger(object.getJSONObject(ALL_REPLICAS));
             final Map<DatabaseInstanceWrapper, Integer> activeReplicas = deserializeMapDatabaseInstanceWrapperInteger(object.getJSONObject(ACTIVE_REPLICAS));
             final DatabaseInstanceWrapper primaryLocation = deserializeDatabaseInstanceWrapper(object.getJSONObject(PRIMARY_LOCATION));
 
             return new ReplicaManager(allReplicas, activeReplicas, primaryLocation);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeDatabaseInstanceWrapper(final DatabaseInstanceWrapper source) {
 
         final JSONValue databaseURL = serializeDatabaseID(source.getURL());
         final JSONValue databaseInstance = serializeIDatabaseInstanceRemote(source.getDatabaseInstance());
 
         final JSONObject object = new JSONObject();
         try {
             object.put(DATABASE_URL, databaseURL.getValue());
             object.put(DATABASE_INSTANCE, databaseInstance.getValue());
             object.put(ACTIVE, source.getActive());
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing DatabaseInstanceWrapper: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public DatabaseInstanceWrapper deserializeDatabaseInstanceWrapper(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final DatabaseID databaseURL = deserializeDatabaseID(object.getJSONObject(DATABASE_URL));
             final IDatabaseInstanceRemote databaseInstance = deserializeIDatabaseInstanceRemote(object.getString(DATABASE_INSTANCE));
             final boolean active = object.getBoolean(ACTIVE);
 
             return new DatabaseInstanceWrapper(databaseURL, databaseInstance, active);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeActionRequest(final ActionRequest source) {
 
         final JSONObject object = new JSONObject();
         try {
             object.put(EXPECTED_TIME_TO_COMPLETION, new JSONValue(source.expectedTimeToCompletion));
             object.put(IMMEDIATE_DISK_SPACE, new JSONValue(source.immediateDiskSpace));
             object.put(CPU, new JSONValue(source.cpu));
             object.put(MEMORY, new JSONValue(source.memory));
             object.put(NETWORK, new JSONValue(source.network));
             object.put(DISK, new JSONValue(source.disk));
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public ActionRequest deserializeActionRequest(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final long expectedTimeToCompletion = object.getLong(EXPECTED_TIME_TO_COMPLETION);
             final long immediateDiskSpace = object.getLong(IMMEDIATE_DISK_SPACE);
             final double cpu = object.getDouble(IMMEDIATE_DISK_SPACE);
             final double memory = object.getDouble(MEMORY);
             final double network = object.getDouble(NETWORK);
             final double disk = object.getDouble(DISK);
 
             return new ActionRequest(expectedTimeToCompletion, immediateDiskSpace, cpu, memory, network, disk);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeTableManagerWrapper(final TableManagerWrapper source) {
 
         if (source == null) { return JSONValue.NULL; }
 
         final JSONValue tableInfo = serializeTableInfo(source.getTableInfo());
         final JSONValue tableManager = serializeITableManagerRemote(source.getTableManager());
         final JSONValue tableManagerURL = serializeDatabaseID(source.getURL());
 
         final JSONObject object = new JSONObject();
         try {
             object.put(TABLE_INFO, tableInfo);
             object.put(TABLE_MANAGER, tableManager);
 
             if (source.getURL() == null) {
                 object.put(TABLE_MANAGER_URL, JSONObject.NULL);
             }
             else {
                 object.put(TABLE_MANAGER_URL, tableManagerURL);
             }
         }
         catch (final JSONException e) {
            Diagnostic.trace(DiagnosticLevel.RUN, "error serializing TableManagerWrapper: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public TableManagerWrapper deserializeTableManagerWrapper(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final ITableManagerRemote tableManager = deserializeITableManagerRemote(object.getString(TABLE_MANAGER));
 
             DatabaseID tableManagerURL = null;
 
             if (!object.isNull(TABLE_MANAGER_URL)) {
                 tableManagerURL = deserializeDatabaseID(object.getJSONObject(TABLE_MANAGER_URL));
             }
            final TableInfo tableInfo = deserializeTableInfo(object.getJSONObject(TABLE_INFO));
 
             return new TableManagerWrapper(tableInfo, tableManager, tableManagerURL);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeTableProxy(final TableProxy source) {
 
         final JSONValue lockGranted = serializeLockType(source.getLockGranted());
         final JSONValue lockRequested = serializeLockType(source.getLockRequested());
         final JSONValue tableName = serializeTableInfo(source.getTableName());
         final JSONValue allReplicas = serializeMapDatabaseInstanceWrapperInteger(source.getAllReplicas());
         final JSONValue tableManager = serializeITableManagerRemote(source.getTableManager());
         final JSONValue requestingDatabase = serializeLockRequest(source.getRequestingDatabase());
         final JSONValue updateID = new JSONValue(source.getUpdateID());
 
         final JSONObject object = new JSONObject();
         try {
             object.put(LOCK_GRANTED, lockGranted);
             object.put(TABLE_NAME, tableName);
             object.put(ALL_REPLICAS, allReplicas);
             object.put(TABLE_MANAGER, tableManager);
             object.put(REQUESTING_DATABASE, requestingDatabase);
             object.put(UPDATE_ID, updateID);
             object.put(LOCK_REQUESTED, lockRequested);
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing TableProxy: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public TableProxy deserializeTableProxy(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
 
             final LockType lockGranted = deserializeLockType(object.getString(LOCK_GRANTED));
             final TableInfo tableName = deserializeTableInfo(object.getJSONObject(TABLE_NAME));
             final Map<DatabaseInstanceWrapper, Integer> allReplicas = deserializeMapDatabaseInstanceWrapperInteger(object.getJSONObject(ALL_REPLICAS));
             final ITableManagerRemote tableManager = deserializeITableManagerRemote(object.getString(TABLE_MANAGER));
             final LockRequest requestingDatabase = deserializeLockRequest(object.getJSONObject(REQUESTING_DATABASE));
             final int updateID = object.getInt(UPDATE_ID);
             final LockType lockRequested = deserializeLockType(object.getString(LOCK_REQUESTED));
 
             return new TableProxy(lockGranted, tableName, allReplicas, tableManager, requestingDatabase, updateID, lockRequested);
 
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeCollectionDatabaseInstanceWrapper(final Collection<DatabaseInstanceWrapper> source) {
 
         final JSONArray object = new JSONArray();
         for (final DatabaseInstanceWrapper instance : source) {
             object.put(serializeDatabaseInstanceWrapper(instance).getValue());
         }
         return new JSONValue(object);
     }
 
     public Set<DatabaseInstanceWrapper> deserializeSetDatabaseInstanceWrapper(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<DatabaseInstanceWrapper> result = new HashSet<DatabaseInstanceWrapper>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeDatabaseInstanceWrapper(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     public Collection<DatabaseInstanceWrapper> deserializeCollectionDatabaseInstanceWrapper(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Collection<DatabaseInstanceWrapper> result = new ArrayList<DatabaseInstanceWrapper>(array.length());
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeDatabaseInstanceWrapper(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeMapDatabaseIDDatabaseInstanceWrapper(final Map<DatabaseID, DatabaseInstanceWrapper> source) {
 
         final JSONObject object = new JSONObject();
         try {
 
             final JSONArray key_array = serializeSetDatabaseID(source.keySet());
             final JSONArray value_arrray = serializeCollectionDatabaseInstanceWrapper(source.values()).getJSONArray();
 
             object.put(KEYS, key_array);
             object.put(VALUES, value_arrray);
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public Map<DatabaseID, DatabaseInstanceWrapper> deserializeMapDatabaseIDDatabaseInstanceWrapper(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final Set<DatabaseID> keys = deserializeSetDatabaseID(object.getJSONArray(KEYS));
             final Collection<DatabaseInstanceWrapper> values = deserializeCollectionDatabaseInstanceWrapper(object.getJSONArray(VALUES));
 
             final HashMap<DatabaseID, DatabaseInstanceWrapper> result = new HashMap<DatabaseID, DatabaseInstanceWrapper>();
 
             final Iterator<DatabaseID> it1 = keys.iterator();
             final Iterator<DatabaseInstanceWrapper> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final DatabaseID arg0 = it1.next();
                 final DatabaseInstanceWrapper arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeSetDatabaseID(final Set<DatabaseID> source) {
 
         final JSONArray object = new JSONArray();
         for (final DatabaseID instance : source) {
             object.put(serializeDatabaseID(instance).getValue());
         }
         return object;
     }
 
     private Set<DatabaseID> deserializeSetDatabaseID(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<DatabaseID> result = new HashSet<DatabaseID>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeDatabaseID(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeMapTableInfoTableManagerWrapper(final Map<TableInfo, TableManagerWrapper> source) {
 
         final JSONObject object = new JSONObject();
         try {
 
             final JSONArray key_array = serializeSetTableInfo(source.keySet());
             final JSONArray value_arrray = serializeCollectionTableManagerWrapper(source.values());
 
             object.put(KEYS, key_array);
             object.put(VALUES, value_arrray);
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     /////////////////////
 
     public Map<TableInfo, TableManagerWrapper> deserializeMapTableInfoTableManagerWrapper(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final Set<TableInfo> keys = deserializeSetTableInfo(object.getJSONArray(KEYS));
             final Collection<TableManagerWrapper> values = deserializeCollectionTableManagerWrapper(object.getJSONArray(VALUES));
 
             final HashMap<TableInfo, TableManagerWrapper> result = new HashMap<TableInfo, TableManagerWrapper>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<TableManagerWrapper> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final TableManagerWrapper arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeCollectionTableManagerWrapper(final Collection<TableManagerWrapper> source) {
 
         final JSONArray object = new JSONArray();
         for (final TableManagerWrapper instance : source) {
             object.put(serializeTableManagerWrapper(instance).getValue());
         }
         return object;
     }
 
     public Set<TableManagerWrapper> deserializeSetTableManagerWrapper(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<TableManagerWrapper> result = new HashSet<TableManagerWrapper>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeTableManagerWrapper(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeSetTableInfo(final Set<TableInfo> source) {
 
         final JSONArray object = new JSONArray();
         for (final TableInfo instance : source) {
             object.put(serializeTableInfo(instance).getValue());
         }
         return object;
     }
 
     private Set<TableInfo> deserializeSetTableInfo(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<TableInfo> result = new HashSet<TableInfo>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeTableInfo(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeMapTableInfoSetDatabaseID(final Map<TableInfo, Set<DatabaseID>> source) {
 
         final JSONObject object = new JSONObject();
         try {
 
             final JSONArray key_array = serializeSetTableInfo(source.keySet());
             final JSONArray value_arrray = serializeCollectionSetDatabaseID(source.values());
 
             object.put(KEYS, key_array);
             object.put(VALUES, value_arrray);
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public Map<TableInfo, Set<DatabaseID>> deserializeMapTableInfoSetDatabaseID(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final Set<TableInfo> keys = deserializeSetTableInfo(object.getJSONArray(KEYS));
             final Collection<Set<DatabaseID>> values = deserializeCollectionSetDatabaseID(object.getJSONArray(VALUES));
 
             final HashMap<TableInfo, Set<DatabaseID>> result = new HashMap<TableInfo, Set<DatabaseID>>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<Set<DatabaseID>> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final Set<DatabaseID> arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeCollectionSetDatabaseID(final Collection<Set<DatabaseID>> source) {
 
         final JSONArray object = new JSONArray();
         for (final Set<DatabaseID> instance : source) {
             object.put(serializeSetDatabaseID(instance)); //<<<<<<<<<<<<<<<<
         }
         return object;
     }
 
     private Set<Set<DatabaseID>> deserializeSetSetDatabaseID(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final HashSet<Set<DatabaseID>> result = new HashSet<Set<DatabaseID>>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeSetDatabaseID(array.getJSONArray(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     private Collection<Set<DatabaseID>> deserializeCollectionSetDatabaseID(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Collection<Set<DatabaseID>> result = new ArrayList<Set<DatabaseID>>(array.length());
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeSetDatabaseID(array.getJSONArray(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeSetTableManagerWrapper(final Set<TableManagerWrapper> source) {
 
         final JSONArray object = new JSONArray();
         for (final TableManagerWrapper instance : source) {
             object.put(serializeTableManagerWrapper(instance).getValue());
         }
         return new JSONValue(object);
     }
 
     public Collection<TableManagerWrapper> deserializeCollectionTableManagerWrapper(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Collection<TableManagerWrapper> result = new ArrayList<TableManagerWrapper>(array.length());
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeTableManagerWrapper(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeMapTableInfoDatabaseID(final Map<TableInfo, DatabaseID> source) {
 
         final JSONObject object = new JSONObject();
         try {
 
             final JSONArray key_array = serializeSetTableInfo(source.keySet());
             final JSONArray value_arrray = serializeCollectionDatabaseID(source.values());
 
             object.put(KEYS, key_array);
             object.put(VALUES, value_arrray);
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public Map<TableInfo, DatabaseID> deserializeMapTableInfoDatabaseID(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final Set<TableInfo> keys = deserializeSetTableInfo(object.getJSONArray(KEYS));
             final Collection<DatabaseID> values = deserializeCollectionDatabaseID(object.getJSONArray(VALUES));
 
             final HashMap<TableInfo, DatabaseID> result = new HashMap<TableInfo, DatabaseID>();
 
             final Iterator<TableInfo> it1 = keys.iterator();
             final Iterator<DatabaseID> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final TableInfo arg0 = it1.next();
                 final DatabaseID arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeCollectionDatabaseID(final Collection<DatabaseID> source) {
 
         final JSONArray object = new JSONArray();
         for (final DatabaseID instance : source) {
             object.put(serializeDatabaseID(instance));
         }
         return object;
     }
 
     private Collection<DatabaseID> deserializeCollectionDatabaseID(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Collection<DatabaseID> result = new ArrayList<DatabaseID>(array.length());
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeDatabaseID(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONValue serializeCommitResult(final CommitResult source) {
 
         final JSONObject object = new JSONObject();
         try {
             object.put(COMMIT, new JSONValue(source.isCommit()));
             object.put(WRAPPER, serializeDatabaseInstanceWrapper(source.getDatabaseInstanceWrapper()));
             object.put(UPDATE_ID, new JSONValue(source.getUpdateID()));
             object.put(EXPECTED_UPDATE_ID, new JSONValue(source.getExpectedUpdateID()));
             object.put(TABLE_NAME, serializeTableInfo(source.getTable()));
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     private CommitResult deserializeCommitResult(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final boolean commit = object.getBoolean(COMMIT);
             final DatabaseInstanceWrapper wrapper = deserializeDatabaseInstanceWrapper(object.getJSONObject(WRAPPER));
             final int updateID = object.getInt(UPDATE_ID);
             final int expectedUpdateID = object.getInt(EXPECTED_UPDATE_ID);
             final TableInfo tableName = deserializeTableInfo(object.getJSONObject(TABLE_NAME));
 
             return new CommitResult(commit, wrapper, updateID, expectedUpdateID, tableName);
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeCollectionCommitResult(final Collection<CommitResult> source) {
 
         final JSONArray object = new JSONArray();
         for (final CommitResult instance : source) {
             object.put(serializeCommitResult(instance).getValue());
         }
         return new JSONValue(object);
     }
 
     public Set<CommitResult> deserializeCollectionCommitResult(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<CommitResult> result = new HashSet<CommitResult>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeCommitResult(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeSetString(final Set<String> source) {
 
         final JSONArray object = new JSONArray();
         for (final String s : source) {
             object.put(s);
         }
         return new JSONValue(object);
     }
 
     public Set<String> deserializeSetString(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Set<String> result = new HashSet<String>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(array.getString(i));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeQueueDatabaseInstanceWrapper(final Queue<DatabaseInstanceWrapper> source) {
 
         final JSONArray object = new JSONArray();
         for (final DatabaseInstanceWrapper instance : source) {
             object.put(serializeDatabaseInstanceWrapper(instance).getValue());
         }
         return new JSONValue(object);
     }
 
     public Queue<DatabaseInstanceWrapper> deserializeQueueDatabaseInstanceWrapper(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Queue<DatabaseInstanceWrapper> result = new PriorityQueue<DatabaseInstanceWrapper>();
             for (int i = 0; i < array.length(); i++) {
                 result.add(deserializeDatabaseInstanceWrapper(array.getJSONObject(i)));
             }
 
             return result;
         }
         catch (final Exception e) {
             ErrorHandling.exceptionErrorNoEvent(e, "Failure on database instance wrapper queue deserialization.");
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     public JSONValue serializeMapDatabaseInstanceWrapperInteger(final Map<DatabaseInstanceWrapper, Integer> source) {
 
         final JSONObject object = new JSONObject();
         try {
 
             final JSONArray key_array = serializeCollectionDatabaseInstanceWrapper(source.keySet()).getJSONArray();
             final JSONArray value_arrray = serializeCollectionInteger(source.values());
 
             object.put(KEYS, key_array);
             object.put(VALUES, value_arrray);
 
         }
         catch (final JSONException e) {
             Diagnostic.trace(DiagnosticLevel.RUN, "error serializing LockType: " + e.getMessage());
         }
         return new JSONValue(object);
     }
 
     public Map<DatabaseInstanceWrapper, Integer> deserializeMapDatabaseInstanceWrapperInteger(final JSONObject object) throws DeserializationException {
 
         if (object == null) { return null; }
 
         try {
             final Set<DatabaseInstanceWrapper> keys = deserializeSetDatabaseInstanceWrapper(object.getJSONArray(KEYS));
             final Collection<Integer> values = deserializeCollectionInteger(object.getJSONArray(VALUES));
 
             final HashMap<DatabaseInstanceWrapper, Integer> result = new HashMap<DatabaseInstanceWrapper, Integer>();
 
             final Iterator<DatabaseInstanceWrapper> it1 = keys.iterator();
             final Iterator<Integer> it2 = values.iterator();
             while (it1.hasNext()) {
                 if (!it2.hasNext()) { throw new DeserializationException("unbalanced elements in Map keys and values"); }
                 final DatabaseInstanceWrapper arg0 = it1.next();
                 final Integer arg1 = it2.next();
                 result.put(arg0, arg1);
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
     /////////////////////
 
     private JSONArray serializeCollectionInteger(final Collection<Integer> source) {
 
         final JSONArray object = new JSONArray();
         for (final Integer instance : source) {
             object.put(instance);
         }
         return object;
     }
 
     private Collection<Integer> deserializeCollectionInteger(final JSONArray array) throws DeserializationException {
 
         if (array == null) { return null; }
 
         try {
 
             final Collection<Integer> result = new ArrayList<Integer>(array.length());
             for (int i = 0; i < array.length(); i++) {
                 result.add(array.getInt(i));
             }
 
             return result;
         }
         catch (final Exception e) {
             throw new DeserializationException(e);
         }
     }
 
 }
