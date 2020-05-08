 package fedora.server.storage;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 
 import fedora.server.Context;
 import fedora.server.Module;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.DatastreamNotFoundException;
 import fedora.server.errors.ConnectionPoolNotFoundException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InvalidContextException;
 import fedora.server.errors.LowlevelStorageException;
 import fedora.server.errors.MalformedPidException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ObjectDependencyException;
 import fedora.server.errors.ObjectExistsException;
 import fedora.server.errors.ObjectLockedException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.ObjectNotInLowlevelStorageException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.management.PIDGenerator;
 import fedora.server.search.Condition;
 import fedora.server.search.FieldSearch;
 import fedora.server.search.FieldSearchResult;
 import fedora.server.search.FieldSearchQuery;
 import fedora.server.storage.lowlevel.FileSystemLowlevelStorage;
 import fedora.server.storage.lowlevel.ILowlevelStorage;
 import fedora.server.storage.replication.DOReplicator;
 import fedora.server.storage.translation.DOTranslator;
 import fedora.server.storage.types.AuditRecord;
 import fedora.server.storage.types.BasicDigitalObject;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.SQLUtility;
 import fedora.server.utilities.TableCreatingConnection;
 import fedora.server.utilities.TableSpec;
 import fedora.server.validation.DOValidator;
 
 /**
  * Provides access to digital object readers and writers.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class DefaultDOManager
         extends Module implements DOManager {
 
     private String m_pidNamespace;
     private String m_storagePool;
     private String m_storageFormat;
     private String m_storageCharacterEncoding;
     private PIDGenerator m_pidGenerator;
     private DOTranslator m_translator;
     private ILowlevelStorage m_permanentStore;
     private ILowlevelStorage m_tempStore;
     private DOReplicator m_replicator;
     private DOValidator m_validator;
     private FieldSearch m_fieldSearch;
     private ExternalContentManager m_contentManager;
 
     private ConnectionPool m_connectionPool;
     private Connection m_connection;
     private SimpleDateFormat m_formatter=new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
 
     public static String DEFAULT_STATE="L";
 
     /**
      * Creates a new DefaultDOManager.
      */
     public DefaultDOManager(Map moduleParameters, Server server, String role)
             throws ModuleInitializationException {
         super(moduleParameters, server, role);
     }
 
     /**
      * Gets initial param values.
      */
     public void initModule()
             throws ModuleInitializationException {
         // pidNamespace (required, 1-17 chars, a-z, A-Z, 0-9 '-')
         m_pidNamespace=getParameter("pidNamespace");
         if (m_pidNamespace==null) {
             throw new ModuleInitializationException(
                     "pidNamespace parameter must be specified.", getRole());
         }
         if ( (m_pidNamespace.length() > 17) || (m_pidNamespace.length() < 1) ) {
             throw new ModuleInitializationException(
                     "pidNamespace parameter must be 1-17 chars long", getRole());
         }
         StringBuffer badChars=new StringBuffer();
         for (int i=0; i<m_pidNamespace.length(); i++) {
             char c=m_pidNamespace.charAt(i);
             boolean invalid=true;
             if (c>='0' && c<='9') {
                 invalid=false;
             } else if (c>='a' && c<='z') {
                 invalid=false;
             } else if (c>='A' && c<='Z') {
                 invalid=false;
             } else if (c=='-') {
                 invalid=false;
             }
             if (invalid) {
                 badChars.append(c);
             }
         }
         if (badChars.toString().length()>0) {
             throw new ModuleInitializationException("pidNamespace contains "
                     + "invalid character(s) '" + badChars.toString() + "'", getRole());
         }
         // storagePool (optional, default=ConnectionPoolManager's default pool)
         m_storagePool=getParameter("storagePool");
         if (m_storagePool==null) {
             getServer().logConfig("Parameter storagePool "
                 + "not given, will defer to ConnectionPoolManager's "
                 + "default pool.");
         }
         // storageFormat (required)
         m_storageFormat=getParameter("storageFormat");
         if (m_storageFormat==null) {
             throw new ModuleInitializationException("Parameter storageFormat "
                 + "not given, but it's required.", getRole());
         }
         // storageCharacterEncoding (optional, default=UTF-8)
         m_storageCharacterEncoding=getParameter("storageCharacterEncoding");
         if (m_storageCharacterEncoding==null) {
             getServer().logConfig("Parameter storage_character_encoding "
                 + "not given, using UTF-8");
             m_storageCharacterEncoding="UTF-8";
         }
 
     }
 
     public void postInitModule()
             throws ModuleInitializationException {
         // get ref to contentmanager module
         m_contentManager = (ExternalContentManager)
           getServer().getModule("fedora.server.storage.ExternalContentManager");
         if (m_contentManager==null) {
             throw new ModuleInitializationException(
                     "ExternalContentManager not loaded.", getRole());
         }
         // get ref to fieldsearch module
         m_fieldSearch=(FieldSearch) getServer().
                 getModule("fedora.server.search.FieldSearch");
         // get ref to pidgenerator
         m_pidGenerator=(PIDGenerator) getServer().
                 getModule("fedora.server.management.PIDGenerator");
         // get the permanent and temporary storage handles
         // m_permanentStore=FileSystemLowlevelStorage.getObjectStore();
         // m_tempStore=FileSystemLowlevelStorage.getTempStore();
         // moved above to getPerm and getTemp (lazy instantiation) because of
         // multi-instance problem due to s_server.getInstance occurring while another is running
 
         // get ref to translator and derive storageFormat default if not given
         m_translator=(DOTranslator) getServer().
                 getModule("fedora.server.storage.translation.DOTranslator");
         // get ref to replicator
         m_replicator=(DOReplicator) getServer().
                 getModule("fedora.server.storage.replication.DOReplicator");
         // get ref to digital object validator
         m_validator=(DOValidator) getServer().
                 getModule("fedora.server.validation.DOValidator");
         if (m_validator==null) {
             throw new ModuleInitializationException(
                     "DOValidator not loaded.", getRole());
         }
         // now get the connectionpool
         ConnectionPoolManager cpm=(ConnectionPoolManager) getServer().
                 getModule("fedora.server.storage.ConnectionPoolManager");
         if (cpm==null) {
             throw new ModuleInitializationException(
                     "ConnectionPoolManager not loaded.", getRole());
         }
         try {
             if (m_storagePool==null) {
                 m_connectionPool=cpm.getPool();
             } else {
                 m_connectionPool=cpm.getPool(m_storagePool);
             }
         } catch (ConnectionPoolNotFoundException cpnfe) {
             throw new ModuleInitializationException("Couldn't get required "
                     + "connection pool...wasn't found", getRole());
         }
         try {
             String dbSpec="fedora/server/storage/resources/DefaultDOManager.dbspec";
             InputStream specIn=this.getClass().getClassLoader().
                     getResourceAsStream(dbSpec);
             if (specIn==null) {
                 throw new IOException("Cannot find required "
                     + "resource: " + dbSpec);
             }
             SQLUtility.createNonExistingTables(m_connectionPool, specIn, this);
         } catch (Exception e) {
             throw new ModuleInitializationException("Error while attempting to "
                     + "check for and create non-existing table(s): "
                     + e.getClass().getName() + ": " + e.getMessage(), getRole());
         }
 
     }
 
     public void releaseWriter(DOWriter writer) {
         writer.invalidate();
         // remove pid from tracked list...m_writers.remove(writer);
     }
 
     public ILowlevelStorage getObjectStore() {
         return FileSystemLowlevelStorage.getObjectStore();
     }
 
     public ILowlevelStorage getDatastreamStore() {
         return FileSystemLowlevelStorage.getDatastreamStore();
     }
 
     public ILowlevelStorage getTempStore() {
         return FileSystemLowlevelStorage.getTempStore();
     }
 
     public ConnectionPool getConnectionPool() {
         return m_connectionPool;
     }
 
     public DOValidator getDOValidator() {
         return m_validator;
     }
 
     public DOReplicator getReplicator() {
         return m_replicator;
     }
 
     public String[] getRequiredModuleRoles() {
         return new String[] {
                 "fedora.server.management.PIDGenerator",
                 "fedora.server.search.FieldSearch",
                 "fedora.server.storage.ConnectionPoolManager",
                 "fedora.server.storage.ExternalContentManager",
                 "fedora.server.storage.translation.DOTranslator",
                 "fedora.server.storage.replication.DOReplicator",
                 "fedora.server.validation.DOValidator" };
     }
 
     public String getStorageFormat() {
         return m_storageFormat;
     }
 
     public String getStorageCharacterEncoding() {
         return m_storageCharacterEncoding;
     }
 
     public DOTranslator getTranslator() {
         return m_translator;
     }
 
     /**
      * Tells whether the context indicates that cached objects are required.
      */
     private static boolean cachedObjectRequired(Context context) {
         String c=context.get("useCachedObject");
         if (c!=null && c.equalsIgnoreCase("true")) {
             return true;
         } else {
             return false;
         }
     }
 
     public DOReader getReader(Context context, String pid)
             throws ServerException {
         if (cachedObjectRequired(context)) {
             return new FastDOReader(context, pid);
         } else {
             return new SimpleDOReader(context, this, m_translator,
                     m_storageFormat, m_storageFormat, m_storageFormat,
                     m_storageCharacterEncoding,
                     getObjectStore().retrieve(pid), this);
         }
     }
 
     public BMechReader getBMechReader(Context context, String pid)
             throws ServerException {
         if (cachedObjectRequired(context)) {
             return new FastBmechReader(context, pid);
             //throw new InvalidContextException("A BMechReader is unavailable in a cached context.");
         } else {
             return new SimpleBMechReader(context, this, m_translator,
                     m_storageFormat, m_storageFormat, m_storageFormat,
                     m_storageCharacterEncoding,
                     getObjectStore().retrieve(pid), this);
         }
     }
 
     public BDefReader getBDefReader(Context context, String pid)
             throws ServerException {
         if (cachedObjectRequired(context)) {
             return new FastBdefReader(context, pid);
             //throw new InvalidContextException("A BDefReader is unavailable in a cached context.");
         } else {
             return new SimpleBDefReader(context, this, m_translator,
                     m_storageFormat, m_storageFormat, m_storageFormat,
                     m_storageCharacterEncoding,
                     getObjectStore().retrieve(pid), this);
         }
     }
 
     /**
      * This could be in response to update *or* delete
      * makes a new audit record in the object,
      * saves object to definitive store, and replicates.
      *
      * In the case where it is not a deletion, the lock is released, too.
      * This happens as the result of a writer.commit() call.
      */
     public void doCommit(Context context, DigitalObject obj, String logMessage, boolean remove)
             throws ServerException {
         // make audit record
         AuditRecord a=new AuditRecord();
         a.id="REC1024";  // FIXME: id should be auto-gen'd somehow
         a.processType="API-M";
         a.action="Don't know";
         a.responsibility=getUserId(context);
         a.date=new Date();
         a.justification=logMessage;
         //obj.getAuditRecords().add(a);
 
         if (remove) {
             // Before removing an object, verify that there are no other objects
             // in the repository that depend on the object being deleted.
             FieldSearchResult result = findObjects(context,
                 new String[] {"pid"}, 10,
                 new FieldSearchQuery(Condition.getConditions("bDef~"+obj.getPid())));
             if (result.objectFieldsList().size() > 0)
             {
                 throw new ObjectDependencyException("The digital object \""
                     + obj.getPid() + "\" is used by one or more other objects "
                     + "in the repository. All related objects must be removed "
                     + "before this object may be deleted. Use the search "
                     + "interface with the query \"bDef~" + obj.getPid()
                     + "\" to obtain a list of dependent objects.");
             }
             result = findObjects(context,
                 new String[] {"pid"}, 10,
                 new FieldSearchQuery(Condition.getConditions("bMech~"+obj.getPid())));
             if (result.objectFieldsList().size() > 0)
             {
               throw new ObjectDependencyException("The digital object \""
                   + obj.getPid() + "\" is used by one or more other objects "
                   + "in the repository. All related objects must be removed "
                   + "before this object may be deleted. Use the search "
                   + "interface with the query \"bMech~" + obj.getPid()
                   + "\" to obtain a list of dependent objects.");
             }
             // remove any managed content datastreams associated with object
             // from permanent store.
             Iterator dsIDIter = obj.datastreamIdIterator();
             while (dsIDIter.hasNext())
             {
               String dsID=(String) dsIDIter.next();
               String controlGroupType =
                   ((Datastream) obj.datastreams(dsID).get(0)).DSControlGrp;
               if ( controlGroupType.equalsIgnoreCase("M"))
               {
                 List allVersions = obj.datastreams(dsID);
                 Iterator dsIter = allVersions.iterator();
 
                 // iterate over all versions of this dsID
                 while (dsIter.hasNext())
                 {
                   Datastream dmc =
                       (Datastream) dsIter.next();
                   String id = obj.getPid() + "+" + dmc.DatastreamID + "+"
                       + dmc.DSVersionID;
                   logInfo("Deleting ManagedContent datastream. " + "id: " + id);
                   try {
                     getDatastreamStore().remove(id);
                   } catch (LowlevelStorageException llse) {
                     logWarning("While attempting removal of managed content datastream: " + llse.getClass().getName() + ": " + llse.getMessage());
                   }
                 }
               }
             }
 
             // remove from temp *and* definitive store
             try {
                 getTempStore().remove(obj.getPid());
             } catch (ObjectNotInLowlevelStorageException onilse) {
                 // ignore... it might not be in temp storage so this is ok.
                 // FIXME: could check modification state with reg table to
                 // deal with this better, but for now this works
             }
             getObjectStore().remove(obj.getPid());
             // Remove it from the registry
             unregisterObject(obj.getPid());
             // Set entry for this object to "D" in the replication jobs table
             addReplicationJob(obj.getPid(), true);
             // tell replicator to do deletion
             m_replicator.delete(obj.getPid());
             removeReplicationJob(obj.getPid());
             logInfo("Deleting from FieldSearch indexes...");
             m_fieldSearch.delete(obj.getPid());
         } else {
             // copy and store any datastreams of type Managed Content
             Iterator dsIDIter = obj.datastreamIdIterator();
             while (dsIDIter.hasNext())
             {
               String dsID=(String) dsIDIter.next();
               Datastream dStream=(Datastream) obj.datastreams(dsID).get(0);
               String controlGroupType = dStream.DSControlGrp;
               if ( controlGroupType.equalsIgnoreCase("M") &&
                    (dStream.DSLocation.indexOf("//")!=-1) )
                    // if it's managed, and a url, means we need to grab content
               {
                 List allVersions = obj.datastreams(dsID);
                 Iterator dsIter = allVersions.iterator();
 
                 // iterate over all versions of this dsID
                 while (dsIter.hasNext())
                 {
                   Datastream dmc =
                       (Datastream) dsIter.next();
                   MIMETypedStream mimeTypedStream = m_contentManager.
                       getExternalContent(dmc.DSLocation.toString());
                   logInfo("Retrieving ManagedContent datastream from remote "
                       + "location: " + dmc.DSLocation);
                   // RLW: change required by conversion fom byte[] to InputStream
                   //ByteArrayInputStream bais =
                   //    new ByteArrayInputStream(mimeTypedStream.stream);
                   // RLW: change required by conversion fom byte[] to InputStream
                   String id = obj.getPid() + "+" + dmc.DatastreamID + "+"
                             + dmc.DSVersionID;
                   // RLW: change required by conversion fom byte[] to InputStream
                   if (obj.getState().equals("I")) {
                       getDatastreamStore().add(id, mimeTypedStream.getStream());
                   } else {
                       // object already existed...so call replace instead
                       getDatastreamStore().replace(id, mimeTypedStream.getStream());
                   }
                   //getDatastreamStore().add(id, bais);
                   // RLW: change required by conversion fom byte[] to InputStream
 
                   // Make new audit record.
 
                   /*
                   // SDP: commented out since audit record id is not yet
                   // auto-incremented and we get XML validation error when
                   // there are multiple managed content datastreams (we get
                   // duplicate ID elements in the XML)
                   a = new AuditRecord();
                   int numAuditRecs = obj.getAuditRecords().size() + 1;
                   a.id = "REC-" + numAuditRecs;
                   a.processType = "API-M";
                   a.action = "Added a ManagedContent datastream for the first "
                       + "time. Copied remote content stored at \""
                       + dmc.DSLocation + "\" and stored it in the Fedora "
                       + "permanentStore under the id: " + id;
                   a.responsibility = getUserId(context);
                   a.date = new Date();
                   a.justification = logMessage;
                   obj.getAuditRecords().add(a);
                   */
 
                   // Reset dsLocation in object to new internal location.
                   dmc.DSLocation = id;
                   logInfo("Replacing ManagedContent datastream with "
                       + "internal id: " + id);
                   //bais = null;
                 }
               }
             }
 
             // save to definitive store, validating beforehand
             // update the system version (add one) and reflect that the object is no longer locked
 
             // Validation:
             // Perform FINAL validation before saving the object to persistent storage.
             // For now, we'll request all levels of validation (level=0), but we can
             // consider whether there is too much redundancy in requesting full validation
             // at time of ingest, then again, here, at time of storage.
             // We'll just be conservative for now and call all levels both times.
             // First, serialize the digital object into an Inputstream to be passed to validator.
 
             // set object status to "A" if "I".  other status changes should occur elsewhere!
             if (obj.getState().equals("I")) {
                 obj.setState("A");
             }
             // set datastream statuses to "A" if "I".  other status changes should occur elsewhere!
             Iterator dsIter=obj.datastreamIdIterator();
             while (dsIter.hasNext()) {
                 List dsList=(List) obj.datastreams((String) dsIter.next());
                 for (int i=0; i<dsList.size(); i++) {
                     Datastream ds=(Datastream) dsList.get(i);
                     if (ds.DSState.equals("I")) {
                         ds.DSState="A";
                     }
                 }
             }
             // same thing, but with disseminators
             Iterator dissIter=obj.disseminatorIdIterator();
             while (dissIter.hasNext()) {
                 List dissList=(List) obj.disseminators((String) dissIter.next());
                 for (int i=0; i<dissList.size(); i++) {
                     Disseminator diss=(Disseminator) dissList.get(i);
                     if (diss.dissState.equals("I")) {
                         diss.dissState="A";
                     }
                 }
             }
             // set last mod date, in UTC
             obj.setLastModDate(DateUtility.convertLocalDateToUTCDate(new Date()));
             // Set useSerializer to false to disable the serializer (for debugging/testing).
             boolean useSerializer=true;
             if (useSerializer) {
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 m_translator.serialize(obj, out, m_storageFormat, m_storageCharacterEncoding);
                 ByteArrayInputStream inV = new ByteArrayInputStream(out.toByteArray());
                 m_validator.validate(inV, 0, "store");
                 // if ok, write change to perm store here...right before db stuff
                 getObjectStore().add(obj.getPid(), new ByteArrayInputStream(out.toByteArray()));
             } else {
                 m_validator.validate(getTempStore().retrieve(obj.getPid()), 0, "store");
             }
             // update lockinguser (set to NULL) and systemVersion (add one)
             Connection conn=null;
             Statement s = null;
             ResultSet results=null;
             try {
                 conn=m_connectionPool.getConnection();
                 String query="SELECT systemVersion "
                            + "FROM doRegistry "
                            + "WHERE doPID='" + obj.getPid() + "'";
                 s=conn.createStatement();
                 results=s.executeQuery(query);
                 if (!results.next()) {
                     throw new ObjectNotFoundException("Error creating replication job: The requested object doesn't exist in the registry.");
                 }
                 int systemVersion=results.getInt("systemVersion");
                 systemVersion++;
                 Date now=new Date();
                 String formattedLastModDate=m_formatter.format(now);
                 s.executeUpdate("UPDATE doRegistry SET systemVersion="
                         + systemVersion + ", lockingUser=NULL, createDate=createDate, lastModifiedDate='" + formattedLastModDate + "' "
                         + "WHERE doPID='" + obj.getPid() + "'");
             } catch (SQLException sqle) {
                 throw new StorageDeviceException("Error creating replication job: " + sqle.getMessage());
             } finally {
                 try
                 {
                   if (results!=null) results.close();
                   if (s!= null) s.close();
                   m_connectionPool.free(conn);
                 } catch (SQLException sqle)
                 {
                   throw new StorageDeviceException("Error creating replication job: " + sqle.getMessage());
                 }
             }
             // add to replication jobs table
             addReplicationJob(obj.getPid(), false);
             // replicate
             try {
                 if (obj.getFedoraObjectType()==DigitalObject.FEDORA_BDEF_OBJECT) {
                     logInfo("Attempting replication as bdef object: " + obj.getPid());
                     BDefReader reader=getBDefReader(context, obj.getPid());
                     logInfo("Got a BDefReader...");
                     m_replicator.replicate(reader);
                     logInfo("Updating FieldSearch indexes...");
                     m_fieldSearch.update(reader);
                 } else if (obj.getFedoraObjectType()==DigitalObject.FEDORA_BMECH_OBJECT) {
                     logInfo("Attempting replication as bmech object: " + obj.getPid());
                     BMechReader reader=getBMechReader(context, obj.getPid());
                     logInfo("Got a BMechReader...");
                     m_replicator.replicate(reader);
                     logInfo("Updating FieldSearch indexes...");
                     m_fieldSearch.update(reader);
                 } else {
                     logInfo("Attempting replication as normal object: " + obj.getPid());
                     DOReader reader=getReader(context, obj.getPid());
                     logInfo("Got a DOReader...");
                     m_replicator.replicate(reader);
                     logInfo("Updating FieldSearch indexes...");
                     m_fieldSearch.update(reader);
                 }
                 // FIXME: also remove from temp storage if this is successful
                 removeReplicationJob(obj.getPid());
             } catch (ServerException se) {
               System.out.println("Error while replicating: " + se.getClass().getName() + ": " + se.getMessage());
               se.printStackTrace();
                 throw se;
             } catch (Throwable th) {
               System.out.println("Error while replicating: " + th.getClass().getName() + ": " + th.getMessage());
               th.printStackTrace();
                 throw new GeneralException("Replicator returned error: (" + th.getClass().getName() + ") - " + th.getMessage());
             }
         }
     }
 
     /**
      * Add an entry to the replication jobs table.
      */
     private void addReplicationJob(String pid, boolean deleted)
             throws StorageDeviceException {
         Connection conn=null;
         String[] columns=new String[] {"doPID", "action"};
         String action="M";
         if (deleted) {
             action="D";
         }
         String[] values=new String[] {pid, action};
         try {
             conn=m_connectionPool.getConnection();
             SQLUtility.replaceInto(conn, "doRepJob", columns,
                     values, "doPID");
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error creating replication job: " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 m_connectionPool.free(conn);
             }
         }
     }
 
     private void removeReplicationJob(String pid)
             throws StorageDeviceException {
         Connection conn=null;
         try {
             conn=m_connectionPool.getConnection();
             Statement s=conn.createStatement();
             s.executeUpdate("DELETE FROM doRepJob "
                     + "WHERE doPID = '" + pid + "'");
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error removing entry from replication jobs table: " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 m_connectionPool.free(conn);
             }
         }
     }
 
     /**
      * Requests a lock on the object.
      *
      * If the lock is already owned by the user in the context, that's ok.
      *
      * @param context The current context.
      * @param pid The pid of the object.
      * @throws ObjectNotFoundException If the object does not exist.
      * @throws ObjectLockedException If the object is already locked by someone else.
      */
     protected void obtainLock(Context context, String pid)
             throws ObjectLockedException, ObjectNotFoundException,
             StorageDeviceException, InvalidContextException {
         Connection conn=null;
         Statement s = null;
         ResultSet results=null;
         try {
             String query="SELECT lockingUser "
                        + "FROM doRegistry "
                        + "WHERE doPID='" + pid + "'";
             conn=m_connectionPool.getConnection();
             s=conn.createStatement();
             results=s.executeQuery(query);
             if (!results.next()) {
                 throw new ObjectNotFoundException("The requested object doesn't exist.");
             }
             String lockingUser=results.getString("lockingUser");
             if (lockingUser==null) {
                 // get the lock
                 lockingUser=getUserId(context);
                 s.executeUpdate("UPDATE doRegistry SET lockingUser='"
                     + lockingUser + "', createDate=createDate, lastModifiedDate=lastModifiedDate WHERE doPID='" + pid + "'");
             }
             if (!lockingUser.equals(getUserId(context))) {
                 throw new ObjectLockedException("The object is locked by " + lockingUser);
             }
             // if we got here, the lock is already owned by current user, ok
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
         } finally {
           try
           {
             if (results!=null) results.close();
             if (s!= null) s.close();
             m_connectionPool.free(conn);
           } catch (SQLException sqle)
           {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
                 }
         }
     }
 
     /**
      * Gets a writer on an an existing object.
      *
      * If the object is locked, it must be by the current user.
      * If the object is not locked, a lock is obtained automatically.
      *
      * The object must be locked by the user identified in the context.
      */
     public DOWriter getWriter(Context context, String pid)
             throws ServerException, ObjectLockedException {
         if (cachedObjectRequired(context)) {
             throw new InvalidContextException("A DOWriter is unavailable in a cached context.");
         } else {
             // ensure we've got a lock
             obtainLock(context, pid);
 
             // TODO: make sure there's no session lock on a writer for the pid
 
             BasicDigitalObject obj=new BasicDigitalObject();
             m_translator.deserialize(getObjectStore().retrieve(pid), obj,
                     m_storageFormat, m_storageCharacterEncoding);
             DOWriter w=new SimpleDOWriter(context, this, m_translator,
                     m_storageFormat, m_storageFormat,
                     m_storageCharacterEncoding, obj, this);
             // add to internal list...somehow..think...
             System.gc();
             return w;
         }
     }
 
     /**
      * Gets a writer on a new, imported object.
      *
      * A new object is created in the system, locked by the current user.
      * The incoming stream must represent a valid object.
      *
      * If newPid is false, the PID from the stream will be used
      * If newPid is true, the PID generator will create a new PID, unless
      * the PID in the stream has a namespace-id part of "test"... in which
      * cast the PID from the stream will be used
      */
     public DOWriter newWriter(Context context, InputStream in, String format, String encoding, boolean newPid)
             throws ServerException {
         getServer().logFinest("Entered DefaultDOManager.newWriter(Context, InputStream, String, String, boolean)");
         String permPid=null;
         boolean wroteTempIngest=false;
         boolean inPermanentStore=false;
         boolean inTempStore=false;
         if (cachedObjectRequired(context)) {
             throw new InvalidContextException("A DOWriter is unavailable in a cached context.");
         } else {
             try {
                 // write it to temp, as "temp-ingest"
                 // FIXME: temp-ingest stuff is temporary, and not threadsafe
                 getTempStore().add("temp-ingest", in);
                 wroteTempIngest=true;
                 InputStream in2=getTempStore().retrieve("temp-ingest");
 
                 // perform initial validation of the ingest submission format
                 InputStream inV=getTempStore().retrieve("temp-ingest");
                 m_validator.validate(inV, 0, "ingest");
 
                 // deserialize it first
                 BasicDigitalObject obj=new BasicDigitalObject();
                 m_translator.deserialize(in2, obj, format, encoding);
                 // then, before doing anything, change the object status to I,
                 // and all datastreams and disseminators' statuses to I.
                 // objects,
                 obj.setState("I");
                 // datastreams,
                 Iterator dsIter=obj.datastreamIdIterator();
                 while (dsIter.hasNext()) {
                     List dsList=(List) obj.datastreams((String) dsIter.next());
                     for (int i=0; i<dsList.size(); i++) {
                         Datastream ds=(Datastream) dsList.get(i);
                         ds.DSState="I";
                     }
                 }
                 // ...finally, disseminators
                 Iterator dissIter=obj.disseminatorIdIterator();
                 while (dissIter.hasNext()) {
                     List dissList=(List) obj.disseminators((String) dissIter.next());
                     for (int i=0; i<dissList.size(); i++) {
                         Disseminator diss=(Disseminator) dissList.get(i);
                         diss.dissState="I";
                     }
                 }
                 // do we need to generate a pid?
                 if (obj.getPid()!=null && (obj.getPid().startsWith("test:") || obj.getPid().startsWith("demo:"))) {
                     getServer().logFinest("Stream contained PID with 'test' namespace-id... will use PID from stream.");
                 } else {
                     if (newPid) {
                         getServer().logFinest("Ingesting client wants a new PID.");
                         // yes... so do that, then set it in the obj.
                         String p=null;
                         try {
                             p=m_pidGenerator.generatePID(m_pidNamespace);
                         } catch (Exception e) {
                             throw new GeneralException("Error generating PID, PIDGenerator returned unexpected error: ("
                                     + e.getClass().getName() + ") - " + e.getMessage());
                         }
                         getServer().logFiner("Generated PID: " + p);
                         obj.setPid(p);
                     } else {
                         getServer().logFinest("Ingesting client wants to use existing PID.");
                     }
                 }
                 // now check the pid.. 1) it must be a valid pid and 2) it can't already exist
 
 
                 // FIXME: need to take out urn: assumption from following func and re-calc length limits.
                 // assertValidPid(obj.getPid());
 
                 // make sure the pid isn't already used by a registered object
                 if (objectExists(obj.getPid())) {
                     throw new ObjectExistsException("The PID '" + obj.getPid() + "' already exists in the registry... the object can't be re-created.");
                 }
 
                 // FIXME: I don't think sending to perm store is needed in the normal
                 // case (i.e. where the serializer is used throughout), but it doesn't
                 // hurt here for now... if it's decided that this isn't necessary,
                 // and this is removed, be sure to change the .replace(...) call
                 // to .add(...) in doCommit()
                 //InputStream in3=getTempStore().retrieve("temp-ingest");
                 //getObjectStore().add(obj.getPid(), in3);
 
                 permPid=obj.getPid();
                 inPermanentStore=true; // signifies successful perm store addition
                 InputStream in4=getTempStore().retrieve("temp-ingest");
 
                 // now add it to the working area with the *known* pid
                 getTempStore().add(obj.getPid(), in4);
                 inTempStore=true; // signifies successful perm store addition
 
                 // then get the writer
                 DOWriter w=new SimpleDOWriter(context, this, m_translator,
                         m_storageFormat, m_storageFormat,
                         m_storageCharacterEncoding, obj, this);
 
                 // ...set the create and last modified dates as the current
                 // server date/time... in UTC (considering the local timezone
                 // and whether it's in daylight savings)
                 Date nowUTC=DateUtility.convertLocalDateToUTCDate(new Date());
                 obj.setCreateDate(nowUTC);
                 obj.setLastModDate(nowUTC);
 
                 // add to internal list...somehow..think...
 
                 // at this point all is good...
                 // so make a record of it in the registry
                 registerObject(obj.getPid(), obj.getFedoraObjectType(), getUserId(context), obj.getLabel(), obj.getContentModelId(), obj.getCreateDate(), obj.getLastModDate());
                 return w;
             } catch (ServerException se) {
                 // remove from temp store if anything failed
                 if (permPid!=null) {
                     if (inTempStore) {
                         getTempStore().remove(permPid);
                     }
                 }
                 throw se; // re-throw it so the client knows what's up
             } finally {
                 if (wroteTempIngest) {
                     // remove this in any case
                     getTempStore().remove("temp-ingest");
                 }
                 System.gc();
             }
         }
     }
 
     /**
      * Gets a writer on a new, empty object.
      */
     public DOWriter newWriter(Context context)
             throws ServerException {
         getServer().logFinest("Entered DefaultDOManager.newWriter(Context)");
         if (cachedObjectRequired(context)) {
             throw new InvalidContextException("A DOWriter is unavailable in a cached context.");
         } else {
             BasicDigitalObject obj=new BasicDigitalObject();
             getServer().logFinest("Creating object, need a new PID.");
             String p=null;
             try {
                 p=m_pidGenerator.generatePID(m_pidNamespace);
             } catch (Exception e) {
                 throw new GeneralException("Error generating PID, PIDGenerator returned unexpected error: ("
                         + e.getClass().getName() + ") - " + e.getMessage());
             }
             getServer().logFiner("Generated PID: " + p);
             obj.setPid(p);
 // FIXME: uncomment the following after lv0 test
 //          assertValidPid(obj.getPid());
             if (objectExists(obj.getPid())) {
                 throw new ObjectExistsException("The PID '" + obj.getPid() + "' already exists in the registry... the object can't be re-created.");
             }
             // make a record of it in the registry
             // FIXME: this method is incomplete...
             //registerObject(obj.getPid(), obj.getFedoraObjectType(), getUserId(context));
 
             // serialize to disk, then validate.. if that's ok, go on.. else unregister it!
         }
         return null;
     }
 
     /**
      * Gets the userId property from the context... if it's not
      * populated, throws an InvalidContextException.
      */
     private String getUserId(Context context)
             throws InvalidContextException {
         String ret=context.get("userId");
         if (ret==null) {
             throw new InvalidContextException("The context identifies no userId, but a user must be identified for this operation.");
         }
         return ret;
     }
 
     /**
      * FIXME: This is no longer valid given the decision not to start pids with "urn:"
      *
      * Throws an exception if the PID is invalid.
      * <pre>
      * Basically:
      * ----------
      * The implementation's limit for the namespace
      * id is 17 characters.
      *
      * The limit for object id is 10 characters,
      * representing any decimal # between zero and
      * 2147483647 (2.14 billion)
      *
      * This does not necessarily mean a particular
      * installation can handle 2.14 billion objects.
      * The max number of objects is practically
      * limited by:
      *   - disk storage limits
      *   - OS filesystem impl. limits
      *   - database used (max rows in a table, etc.)
      *
      * How prantical length limits were derived:
      * -----------------------------------------
      * The type for dbid's on objects in the db is int.
      *
      * MySQL and McKoi both impose a max of 2.14Billion (10
      * decimal digits) on INT. (for oracle it's higher, but
      * unknown).  Some dbs have a higher-prcision int type
      * (like bigint), but it's likely a limit in number of
      * rows would be reached before the int type is
      * exhausted.
      *
      * So for PIDs, which use URN syntax, the NSS part (in
      * our case, a decimal number [see spec section
      * 8.3.1(3)]) can be *practically* be between 1 and 10
      * (decimal) digits.
      *
      * Additionally, where PIDs are stored in the db, we
      * impose a max length of 32 chars.
      *
      * Given the urn-syntax-imposed 5 chars ('urn:' and ':'),
      * the storage system's int-type limit of 10 chars for
      * row ids, and the storage system's imposed limit of 32
      * chars for the total pid, this leaves 17 characters for
      * the namespace id.
      *
      * urn:17maxChars-------:10maxChars
      * ^                              ^
      * |-------- 32 chars max --------|
      * </pre>
      */
     private void assertValidPid(String pid)
             throws MalformedPidException {
         if (pid.length()>32) {
             throw new MalformedPidException("Pid is too long.  Max total length is 32 chars.");
         }
         String[] parts=pid.split(":");
         if (parts.length!=3) {
             throw new MalformedPidException("Pid must have two ':' characters, as in urn:nsid:1234");
         }
         if (!parts[0].equalsIgnoreCase("urn")) {
             throw new MalformedPidException("Pids must use the urn scheme, as in urn:nsid:1234");
         }
         if (parts[1].length()>17) {
             throw new MalformedPidException("Namespace id part of pid must be less than 18 chars.");
         }
         if (parts[1].length()==0) {
             throw new MalformedPidException("Namespace id part of pid must be at least 1 char.");
         }
         // check for valid chars in namespace id part
         StringBuffer badChars=new StringBuffer();
         for (int i=0; i<parts[1].length(); i++) {
             char c=parts[1].charAt(i);
             boolean invalid=true;
             if (c>='0' && c<='9') {
                 invalid=false;
             } else if (c>='a' && c<='z') {
                 invalid=false;
             } else if (c>='A' && c<='Z') {
                 invalid=false;
             } else if (c=='-') {
                 invalid=false;
             }
             if (invalid) {
                 badChars.append(c);
             }
         }
         if (badChars.toString().length()>0) {
             throw new MalformedPidException("Pid namespace id part contains "
                     + "invalid character(s) '" + badChars.toString() + "'");
         }
         if (parts[2].length()>10) {
             throw new MalformedPidException("Pid object id part must be "
                     + "less than 11 chars.");
         }
         if (parts[2].length()==0) {
             throw new MalformedPidException("Pid object id part must be "
                     + "at least 1 char.");
         }
         try {
             long lng=Long.parseLong(parts[2]);
             if (lng>2147483647) {
                 throw new NumberFormatException("");
             }
             if (lng<0) {
                 throw new NumberFormatException("");
             }
         } catch (NumberFormatException nfe) {
             throw new MalformedPidException("Pid object id part must be "
                     + "an integer between 0 and 2.147483647 billion.");
         }
     }
 
     /**
      * Checks the object registry for the given object.
      */
     public boolean objectExists(String pid)
             throws StorageDeviceException {
         Connection conn=null;
         Statement s = null;
         ResultSet results=null;
         try {
             String query="SELECT doPID "
                        + "FROM doRegistry "
                        + "WHERE doPID='" + pid + "'";
             conn=m_connectionPool.getConnection();
             s=conn.createStatement();
             results=s.executeQuery(query);
             return results.next(); // 'true' if match found, else 'false'
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
         } finally {
           try
           {
             if (results!=null) results.close();
             if (s!= null) s.close();
             m_connectionPool.free(conn);
           } catch (SQLException sqle)
           {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
           }
         }
     }
 
     public String getLockingUser(String pid)
             throws StorageDeviceException, ObjectNotFoundException {
         Connection conn=null;
         Statement s = null;
         ResultSet results=null;
         try {
             String query="SELECT lockingUser "
                        + "FROM doRegistry "
                        + "WHERE doPID='" + pid + "'";
             conn=m_connectionPool.getConnection();
             s=conn.createStatement();
             results=s.executeQuery(query);
             if (results.next()) {
                 return results.getString(1);
             } else {
                 throw new ObjectNotFoundException("Object " + pid + " not found in object registry.");
             }
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
         } finally {
           try
           {
             if (results!=null) results.close();
             if (s!= null) s.close();
             m_connectionPool.free(conn);
           } catch (SQLException sqle)
           {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
           }
         }
     }
 
     /**
      * Adds a new, locked object.
      */
     private void registerObject(String pid, int fedoraObjectType, String userId,
             String label, String contentModelId, Date createDate, Date lastModDate)
             throws StorageDeviceException {
         // label or contentModelId may be null...set to blank if so
         String theLabel=label;
         if (theLabel==null) {
             theLabel="";
         }
         String theContentModelId=contentModelId;
         if (theContentModelId==null) {
             theContentModelId="";
         }
         Connection conn=null;
         Statement st=null;
         String foType="O";
         if (fedoraObjectType==DigitalObject.FEDORA_BDEF_OBJECT) {
             foType="D";
         }
         if (fedoraObjectType==DigitalObject.FEDORA_BMECH_OBJECT) {
             foType="M";
         }
         try {
             String formattedCreateDate=m_formatter.format(createDate);
             String formattedLastModDate=m_formatter.format(lastModDate);
             String query="INSERT INTO doRegistry (doPID, foType, "
                                                    + "lockingUser, label, "
                                                    + "contentModelID, createDate, "
                                                    + "lastModifiedDate) "
                        + "VALUES ('" + pid + "', '" + foType +"', '"
                                     + userId +"', '" + SQLUtility.aposEscape(theLabel) + "', '"
                                      + theContentModelId + "', '"
                                      + formattedCreateDate + "', '"
                                      + formattedLastModDate + "')";
             conn=m_connectionPool.getConnection();
             st=conn.createStatement();
             st.executeUpdate(query);
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database while registering object: " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 if (st!=null) {
                     try {
                         st.close();
                     } catch (Exception e) { }
                 }
                 m_connectionPool.free(conn);
             }
         }
     }
 
     /**
      * Removes an object from the object registry.
      */
     private void unregisterObject(String pid)
             throws StorageDeviceException {
         Connection conn=null;
         Statement st=null;
         try {
             conn=m_connectionPool.getConnection();
             st=conn.createStatement();
             st.executeUpdate("DELETE FROM doRegistry WHERE doPID='" + pid + "'");
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database while unregistering object: " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 if (st!=null) {
                     try {
                         st.close();
                     } catch (Exception e) { }
                 }
                 m_connectionPool.free(conn);
             }
         }
     }
 
     public String[] listObjectPIDs(Context context)
             throws StorageDeviceException {
         return getPIDs("WHERE systemVersion > 0");
     }
 
     public String[] listObjectPIDs(Context context, String pidPattern,
             String foType, String lockedByPattern, String state,
             String labelPattern, String contentModelIdPattern,
             Calendar createDateMin, Calendar createDateMax,
             Calendar lastModDateMin, Calendar lastModDateMax)
             throws StorageDeviceException {
         StringBuffer whereClause=new StringBuffer();
         boolean needEscape=false;
         whereClause.append("WHERE systemVersion > 0");
         if (pidPattern!=null) {
             String part=toSql("doPID", pidPattern);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (foType!=null) {
             String part=toSql("foType", foType);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (lockedByPattern!=null) {
             String part=toSql("lockingUser", lockedByPattern);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (state!=null) {
             String part=toSql("objectState", state);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (labelPattern!=null) {
             String part=toSql("label", labelPattern);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (contentModelIdPattern!=null) {
             String part=toSql("contentModelID", contentModelIdPattern);
             if (part.charAt(0)==' ') {
                 needEscape=true;
             } else {
                 whereClause.append(' ');
             }
             whereClause.append("AND ");
             whereClause.append(part);
         }
         if (createDateMin!=null) {
             whereClause.append(" AND createDate >= '");
             whereClause.append(m_formatter.format(createDateMin));
             whereClause.append('\'');
         }
         if (createDateMax!=null) {
             whereClause.append(" AND createDate <= '");
             whereClause.append(m_formatter.format(createDateMax));
             whereClause.append('\'');
         }
         if (lastModDateMin!=null) {
             whereClause.append(" AND lastModifiedDate >= '");
             whereClause.append(m_formatter.format(lastModDateMin));
             whereClause.append('\'');
         }
         if (lastModDateMax!=null) {
             whereClause.append(" AND lastModifiedDate <= '");
             whereClause.append(m_formatter.format(lastModDateMax));
             whereClause.append('\'');
         }
         if (needEscape) {
             whereClause.append(" {escape '/'}");
         }
         return getPIDs(whereClause.toString());
     }
 
     // translates simple wildcard string to sql-appropriate.
     // the first character is a " " if it needs an escape
     public static String toSql(String name, String in) {
         if (in.indexOf("\\")!=-1) {
             // has one or more escapes, un-escape and translate
             StringBuffer out=new StringBuffer();
             out.append("\'");
             boolean needLike=false;
             boolean needEscape=false;
             boolean lastWasEscape=false;
             for (int i=0; i<in.length(); i++) {
                 char c=in.charAt(i);
                 if ( (!lastWasEscape) && (c=='\\') ) {
                     lastWasEscape=true;
                 } else {
                     char nextChar='!';
                     boolean useNextChar=false;
                     if (!lastWasEscape) {
                         if (c=='?') {
                             out.append('_');
                             needLike=true;
                         } else if (c=='*') {
                             out.append('%');
                             needLike=true;
                         } else {
                             nextChar=c;
                             useNextChar=true;
                         }
                     } else {
                         nextChar=c;
                         useNextChar=true;
                     }
                     if (useNextChar) {
                         if (nextChar=='\"') {
                             out.append("\\\"");
                             needEscape=true;
                         } else if (nextChar=='\'') {
                             out.append("\\\'");
                             needEscape=true;
                         } else if (nextChar=='%') {
                             out.append("\\%");
                             needEscape=true;
                         } else if (nextChar=='_') {
                             out.append("\\_");
                             needEscape=true;
                         } else {
                             out.append(nextChar);
                         }
                     }
                     lastWasEscape=false;
                 }
             }
             out.append("\'");
             if (needLike) {
                 out.insert(0, " LIKE ");
             } else {
                 out.insert(0, " = ");
             }
             out.insert(0, name);
             if (needEscape) {
                 out.insert(0, ' ');
             }
             return out.toString();
         } else {
             // no escapes, just translate if needed
             StringBuffer out=new StringBuffer();
             out.append("\'");
             boolean needLike=false;
             boolean needEscape=false;
             for (int i=0; i<in.length(); i++) {
                 char c=in.charAt(i);
                 if (c=='?') {
                     out.append('_');
                     needLike=true;
                 } else if (c=='*') {
                     out.append('%');
                     needLike=true;
                 } else if (c=='\"') {
                     out.append("\\\"");
                     needEscape=true;
                 } else if (c=='\'') {
                     out.append("\\\'");
                     needEscape=true;
                 } else if (c=='%') {
                     out.append("\\%");
                     needEscape=true;
                 } else if (c=='_') {
                     out.append("\\_");
                     needEscape=true;
                 } else {
                     out.append(c);
                 }
             }
             out.append("\'");
             if (needLike) {
                 out.insert(0, " LIKE ");
             } else {
                 out.insert(0, " = ");
             }
             out.insert(0, name);
             if (needEscape) {
                 out.insert(0, ' ');
             }
             return out.toString();
         }
     }
 
     /** whereClause is a WHERE clause, starting with "where" */
     private String[] getPIDs(String whereClause)
             throws StorageDeviceException {
         ArrayList pidList=new ArrayList();
         Connection conn=null;
         Statement s = null;
         ResultSet results=null;
         try {
             conn=m_connectionPool.getConnection();
             s=conn.createStatement();
             StringBuffer query=new StringBuffer();
             query.append("SELECT doPID FROM doRegistry ");
             query.append(whereClause);
             logFinest("Executing db query: " + query.toString());
             results=s.executeQuery(query.toString());
             while (results.next()) {
                 pidList.add(results.getString("doPID"));
             }
             String[] ret=new String[pidList.size()];
             Iterator pidIter=pidList.iterator();
             int i=0;
             while (pidIter.hasNext()) {
                 ret[i++]=(String) pidIter.next();
             }
             return ret;
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
 
         } finally {
           try
           {
             if (results!=null) results.close();
             if (s!= null) s.close();
             m_connectionPool.free(conn);
           } catch (SQLException sqle)
           {
             throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
           }
         }
     }
 
     public FieldSearchResult findObjects(Context context,
             String[] resultFields, int maxResults, FieldSearchQuery query)
             throws ServerException {
         return m_fieldSearch.findObjects(resultFields, maxResults, query);
     }
 
     public FieldSearchResult resumeFindObjects(Context context,
             String sessionToken)
             throws ServerException {
         return m_fieldSearch.resumeFindObjects(sessionToken);
     }
 
 }
