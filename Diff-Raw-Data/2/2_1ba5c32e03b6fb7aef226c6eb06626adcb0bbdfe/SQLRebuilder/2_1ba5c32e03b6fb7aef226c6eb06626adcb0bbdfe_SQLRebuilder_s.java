 package fedora.server.utilities.rebuild;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.config.ServerConfiguration;
 import fedora.server.errors.ConnectionPoolNotFoundException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.LowlevelStorageException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.management.PIDGenerator;
 import fedora.server.search.FieldSearch;
 import fedora.server.storage.BDefReader;
 import fedora.server.storage.BMechReader;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.ConnectionPoolManager;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.DOWriter;
 import fedora.server.storage.lowlevel.FileSystemLowlevelStorage;
 import fedora.server.storage.replication.DOReplicator;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.utilities.SQLUtility;
 
 /**
  * A Rebuilder for the SQL database.
  * 
  * @@version $Id$
  */
 public class SQLRebuilder implements Rebuilder {
 
     private File m_serverDir;
     private ServerConfiguration m_serverConfig;
     private static Server s_server;
     private ConnectionPool m_connectionPool;
     private Connection m_connection;
     private Context m_context;
     
     private String m_echoString;
 
     /**
      * Get a short phrase describing what the user can do with this rebuilder.
      */
     public String getAction() {
         return "Rebuild SQL database.";
     }
 
     /**
      * Returns true is the server _must_ be shut down for this 
      * rebuilder to safely operate.
      */
     public boolean shouldStopServer()
     {
         return(true);
     }
  
     /**
      * Initialize the rebuilder, given the server configuration.
      *
      * @@returns a map of option names to plaintext descriptions.
      */
     public Map init(File serverDir,
                     ServerConfiguration serverConfig) {
         m_serverDir = serverDir;
         m_serverConfig = serverConfig;
         Map m = new HashMap();
         return m;
     }
 
     /**
      * Validate the provided options and perform any necessary startup tasks.
      */
     public void start(Map options) throws NumberFormatException 
     {
         long startupDelay = 0;
 
         // do startup tasks
 
         try {
             s_server=RebuildServer.getRebuildInstance(new File(System.getProperty("fedora.home")));
             // now get the connectionpool
             ConnectionPoolManager cpm=(ConnectionPoolManager) s_server.
                     getModule("fedora.server.storage.ConnectionPoolManager");
             if (cpm==null) 
             {
                 throw new ModuleInitializationException(
                         "ConnectionPoolManager not loaded.", "ConnectionPoolManager");
             }
             m_connectionPool = cpm.getPool();
             /*
             HashMap h = new HashMap();
             h.put("application", "rebuild");
             h.put("useCachedObject", "false");
             h.put("userId", "fedoraAdmin");
             */
             m_context = ReadOnlyContext.getContext("utility", "fedoraAdmin", "", null, ReadOnlyContext.DO_OP); 
             String registryClassTemp = s_server.getParameter("registry");
             String reason = "registry";
 
             blankExistingTables( );
 
             try
             {
                 FileSystemLowlevelStorage.getObjectStore().rebuild();
                 FileSystemLowlevelStorage.getDatastreamStore().rebuild();
             } 
             catch (LowlevelStorageException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
         } 
         catch (InitializationException ie)
         {
             System.err.println(ie.getMessage());
             System.err.flush();
         }
         catch (ConnectionPoolNotFoundException ie)
         {
             System.err.println(ie.getMessage());
             System.err.flush();
         }        
         catch (Exception e)
         {
             System.err.println(e.getMessage());
             System.err.flush();
         }              
     }
     
         
     public static List getExistingTables( Connection conn )
             throws SQLException 
     {
 
         ArrayList existing=new ArrayList();
         DatabaseMetaData dbMeta=conn.getMetaData();
         ResultSet r = null;
         // Get a list of tables that don't exist, if any
         try
         {
             r = dbMeta.getTables(null, null, "%", null);
             HashSet existingTableSet=new HashSet();
             while (r.next()) 
             {
                existing.add(r.getString("TABLE_NAME").toLowerCase());
             }
             r.close();
             r = null;
         } 
         catch (SQLException sqle)
         {
             throw new SQLException(sqle.getMessage());
         } 
         finally
         {
             try {
                 if (r != null) r.close();
             } 
             catch (SQLException sqle2) 
             {
                 throw sqle2;
             } 
             finally 
             {
                 r=null;
             }
         }
         return existing;
     }
 
     
     
     public void blankExistingTables( )
     {
         Connection connection = null;
         try {
             connection = m_connectionPool.getConnection();
             List existingTables = getExistingTables(connection);
             for (int i = 0; i < existingTables.size(); i++)
             {
                 String tableName = existingTables.get(i).toString();
                 if (!tableName.startsWith("ri"))
                 {
                     try {
                         executeSql(connection, "DELETE FROM " + tableName + " WHERE 1");
                     }
                     catch (LowlevelStorageException lle)
                     {
                         System.err.println(lle.getMessage());
                         System.err.flush();
                     }
                 }
             }
         }
         catch (SQLException e)
         {
             e.printStackTrace();
         }
         finally 
         {
             try {
                 if (connection != null) m_connectionPool.free(connection);
             } 
             catch (Exception e2) 
             { // purposely general to include uninstantiated statement, connection
                 e2.printStackTrace();
             } 
         }
 
 
     }
     
     public void executeSql(Connection connection, String sql ) 
            throws LowlevelStorageException
     {
         Statement statement = null;
         try {
             statement = connection.createStatement();
             if (statement.execute(sql)) 
             {
                 throw new LowlevelStorageException(true, "sql returned query results for a nonquery");
             }
             int updateCount = statement.getUpdateCount();
         } 
         catch (SQLException e1) 
         {
             throw new LowlevelStorageException(true, "sql failurex (exec)", e1);
         } 
         finally 
         {
             try {
                 if (statement != null) statement.close();
             } 
             catch (Exception e2) 
             { // purposely general to include uninstantiated statement, connection
                 throw new LowlevelStorageException(true,"sql failure closing statement, connection, pool (exec)", e2);
             } 
             finally 
             {
                 statement=null;
             }
         }
     }
 
     /**
      * Add the data of interest for the given object.
      */
     public void addObject(DigitalObject obj) 
     {
         // CURRENT TIME:
         // Get the current time to use for created dates on object
         // and object components (if they are not already there).
         Date nowUTC=new Date();
 
         DOReplicator replicator=(DOReplicator) s_server.getModule("fedora.server.storage.replication.DOReplicator");
         DOManager manager=(DOManager) s_server.getModule("fedora.server.storage.DOManager");
         FieldSearch fieldSearch=(FieldSearch) s_server.getModule("fedora.server.search.FieldSearch");
         PIDGenerator pidGenerator=(PIDGenerator) s_server.getModule("fedora.server.management.PIDGenerator");
        
         // SET OBJECT PROPERTIES:
         s_server.logFinest("Rebuild: Setting object/component states and create dates if unset...");
         // set object state to "A" (Active) if not already set
         if (obj.getState()==null || obj.getState().equals("")) {
             obj.setState("A");
         }
         // set object create date to UTC if not already set
         if (obj.getCreateDate()==null || obj.getCreateDate().equals("")) {
             obj.setCreateDate(nowUTC);
         }
         // set object last modified date to UTC
         obj.setLastModDate(nowUTC);
         
         // SET OBJECT PROPERTIES:
         s_server.logFinest("Rebuild: Setting object/component states and create dates if unset...");
         // set object state to "A" (Active) if not already set
         if (obj.getState()==null || obj.getState().equals("")) {
             obj.setState("A");
         }
         // set object create date to UTC if not already set
         if (obj.getCreateDate()==null || obj.getCreateDate().equals("")) {
             obj.setCreateDate(nowUTC);
         }
         // set object last modified date to UTC
         obj.setLastModDate(nowUTC);
         
         // SET DATASTREAM PROPERTIES...
         Iterator dsIter=obj.datastreamIdIterator();
         while (dsIter.hasNext()) {
             List dsList=(List) obj.datastreams((String) dsIter.next());
             for (int i=0; i<dsList.size(); i++) {
                 Datastream ds=(Datastream) dsList.get(i);
                 // Set create date to UTC if not already set
                 if (ds.DSCreateDT==null || ds.DSCreateDT.equals("")) {
                     ds.DSCreateDT=nowUTC;
                 }
                 // Set state to "A" (Active) if not already set
                 if (ds.DSState==null || ds.DSState.equals("")) {
                     ds.DSState="A";
                 }
             }
         }
         // SET DISSEMINATOR PROPERTIES...
         Iterator dissIter=obj.disseminatorIdIterator();
         while (dissIter.hasNext()) {
             List dissList=(List) obj.disseminators((String) dissIter.next());
             for (int i=0; i<dissList.size(); i++) {
                 Disseminator diss=(Disseminator) dissList.get(i);
                 // Set create date to UTC if not already set
                 if (diss.dissCreateDT==null || diss.dissCreateDT.equals("")) {
                     diss.dissCreateDT=nowUTC;
                 }
                 // Set state to "A" (Active) if not already set
                 if (diss.dissState==null || diss.dissState.equals("")) {
                     diss.dissState="A";
                 }
             }
         }
         
         // GET DIGITAL OBJECT WRITER:
         // get an object writer configured with the DEFAULT export format
         s_server.logFinest("INGEST: Instantiating a SimpleDOWriter...");
         try {
             DOWriter w = manager.getWriter(Server.USE_DEFINITIVE_STORE, m_context, obj.getPid());
         }
         catch (ServerException se)        
         {
         }
 
         // PID GENERATION:
         // have the system generate a PID if one was not provided
         s_server.logFinest("INGEST: Stream contained PID with retainable namespace-id... will use PID from stream.");
         try {
             pidGenerator.neverGeneratePID(obj.getPid());
         } 
         catch (IOException e) 
         {
           //  throw new GeneralException("Error calling pidGenerator.neverGeneratePID(): " + e.getMessage());
         }
  
         // REGISTRY:
         // at this point the object is valid, so make a record 
         // of it in the digital object registry
         try {
             registerObject(obj.getPid(), obj.getFedoraObjectType(), 
             "fedoraAdmin", obj.getLabel(), obj.getContentModelId(), 
             obj.getCreateDate(), obj.getLastModDate());
         }
         catch (StorageDeviceException e)
         {}
                 
         
         // REPLICATE:
         // add to replication jobs table and do replication to db
         s_server.logFinest("COMMIT: Adding replication job...");
         try {
             addReplicationJob(obj.getPid(), false);
         }
         catch (StorageDeviceException e)
         {
         }
         
         try {
             if (obj.getFedoraObjectType()==DigitalObject.FEDORA_BDEF_OBJECT) 
             {
                 s_server.logInfo("COMMIT: Attempting replication as bdef object: " + obj.getPid());
                 BDefReader reader = manager.getBDefReader(Server.USE_DEFINITIVE_STORE, m_context, obj.getPid());
                 replicator.replicate(reader);
                 s_server.logInfo("COMMIT: Updating FieldSearch indexes...");
                 fieldSearch.update(reader);
             } 
             else if (obj.getFedoraObjectType()==DigitalObject.FEDORA_BMECH_OBJECT) 
             {
                 s_server.logInfo("COMMIT: Attempting replication as bmech object: " + obj.getPid());
                 BMechReader reader = manager.getBMechReader(Server.USE_DEFINITIVE_STORE, m_context, obj.getPid());
                 replicator.replicate(reader);
                 s_server.logInfo("COMMIT: Updating FieldSearch indexes...");
                 fieldSearch.update(reader);
             } 
             else 
             {
                 s_server.logInfo("COMMIT: Attempting replication as normal object: " + obj.getPid());
                 DOReader reader = manager.getReader(Server.USE_DEFINITIVE_STORE, m_context, obj.getPid());
                 replicator.replicate(reader);
                 s_server.logInfo("COMMIT: Updating FieldSearch indexes...");
                 fieldSearch.update(reader);
             }
             // FIXME: also remove from temp storage if this is successful
             removeReplicationJob(obj.getPid());
         } 
         catch (ServerException se) 
         {
           System.out.println("Error while replicating: " + se.getClass().getName() + ": " + se.getMessage());
           se.printStackTrace();
     //        throw se;
         } 
         catch (Throwable th) 
         {
           System.out.println("Error while replicating: " + th.getClass().getName() + ": " + th.getMessage());
           th.printStackTrace();
     //        throw new GeneralException("Replicator returned error: (" + th.getClass().getName() + ") - " + th.getMessage());
         }
         System.out.println(m_echoString + ": " + obj.getPid());
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
                   throws StorageDeviceException 
     {
         Connection conn=null;
         Statement s=null;
         try {
             conn=m_connectionPool.getConnection();
             s=conn.createStatement();
             s.executeUpdate("DELETE FROM doRepJob "+ "WHERE doPID = '" + pid + "'");
         } 
         catch (SQLException sqle) 
         {
             throw new StorageDeviceException("Error removing entry from replication jobs table: " + sqle.getMessage());
         } 
         finally 
         {
         
             try {
                 if (s!=null) s.close();
                 if (conn!=null) m_connectionPool.free(conn);
             } 
             catch (SQLException sqle) 
             {
                 throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
             } 
             finally 
             {
                 s=null;
             }
         }
     }
 
 
     /**
      * Adds a new object.
      */
     private void registerObject(String pid, int fedoraObjectType, String userId,
             String label, String contentModelId, Date createDate, Date lastModDate)
             throws StorageDeviceException 
     {
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
         Statement s1=null;
         String foType="O";
         if (fedoraObjectType==DigitalObject.FEDORA_BDEF_OBJECT) {
             foType="D";
         }
         if (fedoraObjectType==DigitalObject.FEDORA_BMECH_OBJECT) {
             foType="M";
         }
         try 
         {
             String query="INSERT INTO doRegistry (doPID, foType, "
                                                    + "ownerId, label, "
                                                    + "contentModelID) "
                        + "VALUES ('" + pid + "', '" + foType +"', '"
                                      + userId +"', '" + SQLUtility.aposEscape(theLabel) + "', '"
                                      + theContentModelId + "')";
             conn=m_connectionPool.getConnection();
             s1=conn.createStatement();
             s1.executeUpdate(query);
         }
         catch (SQLException sqle) 
         {
             throw new StorageDeviceException("Unexpected error from SQL database while registering object: " + sqle.getMessage());
         } 
         finally 
         {
             try {
                 if (s1!=null) s1.close();
              } 
             catch (Exception sqle) 
             {
                 throw new StorageDeviceException("Unexpected error from SQL database while registering object: " + sqle.getMessage());
             } 
             finally 
             {
                 s1=null;
             }
         }
            
         Statement s2 = null;
         ResultSet results = null;
         try {
             // REGISTRY:
             // update systemVersion in doRegistry (add one)
             s_server.logFinest("COMMIT: Updating registry...");
     //                conn=m_connectionPool.getConnection();
             String query="SELECT systemVersion "
                            + "FROM doRegistry "
                            + "WHERE doPID='" + pid + "'";
             s2 = conn.createStatement();
             results = s2.executeQuery(query);
             if (!results.next()) 
             {
                 throw new ObjectNotFoundException("Error creating replication job: The requested object doesn't exist in the registry.");
             }
             int systemVersion=results.getInt("systemVersion");
             systemVersion++;
             Date now = new Date();
             s2.executeUpdate("UPDATE doRegistry SET systemVersion="
                     + systemVersion + " "
                     + "WHERE doPID='" + pid + "'");
         } 
         catch (SQLException sqle) 
         {
             throw new StorageDeviceException("Error creating replication job: " + sqle.getMessage());
         } 
         catch (ObjectNotFoundException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
         finally 
         {
             try
             {
               if (results!=null) results.close();
               if (s2!= null) s2.close();
               if (conn!=null) m_connectionPool.free(conn);
             } 
             catch (SQLException sqle)
             {
                 throw new StorageDeviceException("Unexpected error from SQL database: " + sqle.getMessage());
             } 
             finally 
             {
                 results=null;
                 s2=null;
             }
         }
     }
 
     /**
      * Free up any system resources associated with rebuilding.
      */
     public void finish() 
     {
         try {
             s_server.shutdown(null);
         } catch (Throwable th) {
             System.out.println("Error shutting down RebuildServer:");
             th.printStackTrace();
         }
     }
 
 }
