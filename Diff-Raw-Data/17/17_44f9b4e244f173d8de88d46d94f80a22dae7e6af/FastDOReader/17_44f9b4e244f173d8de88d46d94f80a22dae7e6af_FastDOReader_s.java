 package fedora.server.storage;
 
 import java.io.File;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Vector;
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DisseminationBindingInfo;
 import fedora.server.storage.types.DSBindingMapAugmented;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodParmDef;
 
 /**
  * <p>Title: FastDOReader.java</p>
  * <p>Description: Digital Object Reader that accesses objects located in the
  * "Fast" storage area. To enhance performance of disseminations, there are
  * two distinct storage areas for digital objects:
  * <ol>
  * <li>
  * "Fast" storage area - The storage area containing a subset of digital
  * objects that is optimized for performance. Both the composition of the
  * subset of objects and storage area are implementation specific. For Phase 1,
  * this object subset consists of a partial replication of the most current
  * version of each object and is used as the primary source for resolving
  * dissemination requests. The replication is partial since only information
  * required to disseminate the object is replicated in the Fast storage area.
  * For Phase 1, the Fast storage area is implemented as a relational database
  * that is accessed via JDBC. <i>Note that an appropriate definitve reader
  * should always be used to obtain the most complete information about a
  * specific object. A fast reader is used primarily for dissemination
  * requests.</i>.
  * </li>
  * <li>
  * Definitive storage area - The storage area containing complete information on
  * all digital objects in the repository. This storage area is used as the
  * authoritative source for reading complete information about a digital object.
  * This storage area is used as a secondary source for resolving dissemination
  * requests when the specified object does not exist in the Fast storage area.
  * </li>
  * </ol>
  * <p>This reader is designed to read objects from the "Fast" storage area that
  * is implemented as a relational database. If the object cannot be found in
  * the relational database, this reader will attempt to read the object
  * from the Definitive storage area using the appropriate definitive reader.
  * When the object exists in both storage areas, preference is given to the
  * Fast storage area since this reader is designed to read primarily from the
  * Fast Storage area. <code>DefinitiveDOReader</code>,
  * <code>DefinitveBMechReader</code>, or <code>DefinitiveBDefReader</code>
  * should always be used to read the authoritative version of an object.</p>
  * <i><b>Note that versioning is not implemented in Phase 1. Methods in
  * <code>FastDOReader</code> that contain arguments related to versioning date
  * such as <code>versDateTime</code> or <code>asOfDate</code> will be ignored
  * in Phase 1.</p>
  * <p></p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class FastDOReader implements DisseminatingDOReader
 {
   /** Debug toggle for testing */
   private static boolean debug;
 
   /** Database ConnectionPool instance */
   private static ConnectionPool connectionPool = null;
 
   /** Fedora server instance */
   private static Server s_server = null;
 
   /** Current Fedora server DOManager instance */
   private static DOManager m_manager = null;
 
   /** Signals object found in fast storage area */
   private boolean isFoundInFastStore = false;
 
   /** signals object found in definitive storage area */
   private boolean isFoundInDefinitiveStore = false;
 
   /** Label of the digital object. */
   private String doLabel = null;
 
   /** Persistent identifier of the digital object. */
   private String PID = null;
 
   /** Instance of DOReader... used to get definitive readers. */
   private DOReader doReader = null;
 
   /** Context for cached objects. */
   private Context s_context = null;
 
   /** Context for uncached objects. */
   private static Context m_context = null;
 
   /** Make sure we have a server instance. */
   static
   {
     try
     {
       s_server =
           Server.getInstance(new File(System.getProperty("fedora.home")));
       Boolean B1 = new Boolean(s_server.getParameter("debug"));
       debug = B1.booleanValue();
       m_manager = (DOManager) s_server.getModule(
           "fedora.server.storage.DOManager");
       HashMap h = new HashMap();
       h.put("application", "apia");
       h.put("useCachedObject", "false");
       h.put("userId", "fedoraAdmin");
       m_context = new ReadOnlyContext(h);
     } catch (InitializationException ie)
     {
       System.err.println(ie.getMessage());
     }
   }
 
   /**
    * <p>Constructs a new <code>FastDOReader</code> for the specified digital
    * object. It initializes the database connection for JDBC access to the
    * relational database and verifies existence of the specified object. If
    * the object is found, this constructor initializes the class variables for
    * <code>PID</code>, <code>doLabel</code>, and <code>s_context</code>.
    *
    * @param context The context of this request.
    * @param objectPID The persistent identifier of the digital object.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public FastDOReader(Context context, String objectPID) throws ServerException
   {
     try
     {
       // Get database connection pool
       ConnectionPoolManager poolManager = (ConnectionPoolManager)
           s_server.getModule("fedora.server.storage.ConnectionPoolManager");
       connectionPool = poolManager.getPool();
 
       // Attempt to find object in either Fast or Definitive store
       this.doLabel = locatePID(objectPID);
       this.PID = objectPID;
       this.s_context = context;
       s_server.logFinest("instantiated FastDOReader: context:" + s_context);
     } catch (ServerException se)
     {
       throw se;
     } catch (Throwable th)
     {
       s_server.logWarning("Unable to construct FastDOReader");
       throw new GeneralException("Fast reader returned error: ("
                                  + th.getClass().getName() + ") - "
                                  + th.getMessage());
     }
   }
 
   /**
    * <p>Exports the object. Since the XML representation of an object is
    * not stored in the Fast storage area, this method always queries the
    * Definitive storage area using <code>DefinitiveDOReader</code>.</p>
    *
    * @return A stream of bytes consisting of the XML-encoded representation
    *         of the digital object.
    * @throws StreamIOException If there is a problem in getting the XML input
    *         stream.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public InputStream ExportObject() throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return(doReader.ExportObject());
     } catch (Throwable th)
     {
       throw new GeneralException("Definitive reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     }
 
   }
 
   /**
    * <p>Gets a list of Behavior Definition object PIDs associated with the
    * specified digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array containing a list of Behavior Definition object PIDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] GetBehaviorDefs(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] behaviorDefs = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "BehaviorDefinition.BDEF_PID "
           + "FROM "
           + "BehaviorDefinition,"
           + "Disseminator,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetBehaviorDefsQuery: " + query);
       String results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           for (int i=1; i<=cols; i++)
           {
             results = new String(rs.getString(i));
           }
           queryResults.add(results);
         }
         behaviorDefs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           behaviorDefs[rowCount] = (String)e.nextElement();
           rowCount++;
         }
 
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return behaviorDefs;
   }
 
   /**
    * <p>Gets method parameters associated with the specified method name.</p>
    *
    * @param bDefPID The persistent identifer of Behavior Definition object.
    * @param methodName The name of the method.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method parameter definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public MethodParmDef[] GetBMechMethodParms(String bDefPID, String methodName,
       Date versDateTime) throws GeneralException
   {
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "PARM_Name,"
           + "PARM_Default_Value,"
           + "PARM_Domain_Values,"
           + "PARM_Required_Flag,"
           + "PARM_Label,"
           + "PARM_Type "
           + " FROM "
           + "DigitalObject,"
           + "BehaviorDefinition,"
           + "BehaviorMechanism,"
           + "MechanismImpl,"
           + "Method,"
           + "Parameter "
           + " WHERE "
           + "BehaviorMechanism.BDEF_DBID=Parameter.BDEF_DBID AND "
           + "Method.BDEF_DBID=Parameter.BDEF_DBID AND "
           + "Method.METH_DBID=Parameter.METH_DBID AND "
           + "BehaviorMechanism.BDEF_DBID=Method.BDEF_DBID AND "
           + "MechanismImpl.METH_DBID=Method.METH_DBID AND "
           + "BehaviorMechanism.BDEF_DBID=BehaviorDefinition.BDEF_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\' AND "
           + "BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND "
           + "Method.METH_Name='"  + methodName + "' ";
 
       if(debug) s_server.logFinest("GetBMechMethodParmQuery=" + query);
       try
       {
         connection = connectionPool.getConnection();
         if(debug) s_server.logFinest("connectionPool = " + connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
 
         // Note: a row is returned for each method parameter
         while (rs.next())
         {
           methodParm = new MethodParmDef();
           String[] results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodParm.parmName = results[0];
           methodParm.parmDefaultValue = results[1];
           methodParm.parmDomainValues = results[2].split(",");
           Boolean B = new Boolean(results[3]);
           methodParm.parmRequired = B.booleanValue();
           methodParm.parmLabel = results[4];
           methodParm.parmType = results[5];
             s_server.logFinest("methodParms: " + methodParm.parmName
                 + "label: " + methodParm.parmLabel
                 + "default: " + methodParm.parmDefaultValue
                 + "required: " + methodParm.parmRequired
                 + "type: " + methodParm.parmType);
             for (int j=0; j<methodParm.parmDomainValues.length; j++)
             {
               s_server.logFinest("domain: " + methodParm.parmDomainValues[j]);
             }
           queryResults.addElement(methodParm);
         }
         methodParms = new MethodParmDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodParms[rowCount] = (MethodParmDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodParms = doReader.GetBMechMethodParms(bDefPID, methodName,
             versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return methodParms;
   }
 
   /**
    * <p>Gets default method parameters associated with the specified
    * method name. Default method parameters are defined by the Behavior
    * Mechanism object as mechanism default parameters and cannot be altered
    * by the user.</p>
    *
    * @param bDefPID The persistent identifer of Behavior Definition object.
    * @param methodName The name of the method.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method parameter definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public MethodParmDef[] GetBMechDefaultMethodParms(String bDefPID,
       String methodName, Date versDateTime) throws GeneralException
   {
     MethodParmDef[] methodParms = null;
     MethodParmDef methodParm = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "DEFPARM_Name,"
           + "DEFPARM_Default_Value,"
           + "DEFPARM_Domain_Values,"
           + "DEFPARM_Required_Flag,"
           + "DEFPARM_Label,"
           + "DEFPARM_Type "
           + " FROM "
           + "DigitalObject,"
           + "BehaviorDefinition,"
           + "BehaviorMechanism,"
           + "MechanismImpl,"
           + "Method,"
           + "MechDefaultParameter "
           + " WHERE "
           + "BehaviorMechanism.BMECH_DBID=MechDefaultParameter.BMECH_DBID AND "
           //+ "Method.BDEF_DBID=MechDefaultParameter.BDEF_DBID AND "
           + "Method.METH_DBID=MechDefaultParameter.METH_DBID AND "
           + "BehaviorMechanism.BDEF_DBID=Method.BDEF_DBID AND "
           + "MechanismImpl.METH_DBID=Method.METH_DBID AND "
           + "BehaviorMechanism.BDEF_DBID=BehaviorDefinition.BDEF_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\' AND "
           + "BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND "
           + "Method.METH_Name='"  + methodName + "' ";
 
       if(debug) s_server.logFinest("GetBMechDefaultMethodParmQuery=" + query);
       try
       {
         connection = connectionPool.getConnection();
         if(debug) s_server.logFinest("connectionPool = " + connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
 
         // Note: a row is returned for each method parameter
         while (rs.next())
         {
           methodParm = new MethodParmDef();
           String[] results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodParm.parmName = results[0];
           methodParm.parmDefaultValue = results[1];
           methodParm.parmDomainValues = results[2].split(",");
           Boolean B = new Boolean(results[3]);
           methodParm.parmRequired = B.booleanValue();
           methodParm.parmLabel = results[4];
           methodParm.parmType = results[5];
             s_server.logFinest("FastDOReader:methodParms: " + methodParm.parmName
                 + "\nlabel: " + methodParm.parmLabel
                 + "\ndefault: " + methodParm.parmDefaultValue
                 + "\nrequired: " + methodParm.parmRequired
                 + "\ntype: " + methodParm.parmType);
             for (int j=0; j<methodParm.parmDomainValues.length; j++)
             {
               s_server.logFinest("FastDOReader:domainValues: "
                   + methodParm.parmDomainValues[j]);
             }
           queryResults.addElement(methodParm);
         }
         methodParms = new MethodParmDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodParms[rowCount] = (MethodParmDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodParms = doReader.GetBMechDefaultMethodParms(bDefPID, methodName,
             versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return methodParms;
   }
 
   /**
    * <p>Gets all method defintiions associated with the specified Behavior
    * Mechanism. Note the PID of the associated Behavior Mechanism object is
    * determined via reflection based on the specified PID of the digital object
    * and the PID of its Behavior Definition object. This method retrieves the
    * list of available methods based on the assocaited Behavior Mechanism
    * object and NOT the Behavior Definition object. This is done to insure
    * that only methods that have been implemented in the mechanism are returned.
    * This distinction is only important when versioning is enabled
    * in a later release. When versioning is enabled, it is possible
    * that a versioned Behavior Definition may have methods that have not
    * yet been implemented by all of its associated Behavior Mechanisms.
    * In such a case, only those methods implemented in the mechanism
    * will be returned.</p>
    *
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param versDateTime The versioning datetime stamp.
    * @return An array of method definitions.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public MethodDef[] GetBMechMethods(String bDefPID, Date versDateTime)
       throws GeneralException
   {
     MethodDef[] methodDefs = null;
     MethodDef methodDef = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "Method.METH_Name,"
           + "Method.METH_Label,"
           + "MechanismImpl.MECHImpl_Address_Location,"
           + "MechanismImpl.MECHImpl_Operation_Location "
           + "FROM "
           + "BehaviorDefinition,"
           + "Disseminator,"
           + "Method,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc,"
           + "BehaviorMechanism,"
           + "MechanismImpl "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND "
           + "Method.METH_DBID = MechanismImpl.METH_DBID AND "
           + "Method.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND "
           + "BehaviorDefinition.BDEF_PID = \'" + bDefPID + "\' AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetBMechMethodsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           methodDef = new MethodDef();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           methodDef.methodName = results[0];
           methodDef.methodLabel = results[1];
           try
           {
             methodDef.methodParms = this.GetBMechMethodParms(bDefPID,
                 methodDef.methodName, versDateTime);
           } catch (Throwable th)
           {
             // Failed to get method paramters
             throw new GeneralException("Fast reader returned error. The "
                                        + "underlying error was a "
                                        + th.getClass().getName()
                                        + "The message was \""
                                        + th.getMessage() + "\"");
           }
           queryResults.add(methodDef);
         }
         methodDefs = new MethodDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           methodDefs[rowCount] = (MethodDef)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         methodDefs = doReader.GetBMechMethods(bDefPID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return methodDefs;
   }
 
   /**
    * <p>Gets WSDL containing method definitions. Since the XML representation
    * of digital objects is not stored in the Fast storage area, this method
    * uses a <code>DOReader</code> to query the Definitive
    * storage area.</p>
    *
    * @param bDefPID The persistent identifier of Behavior Definition object.
    * @param versDateTime The versioning datetime stamp.
    * @return A stream of bytes containing XML-encoded representation of
    *         method definitions from WSDL in assocaited Behavior Mechanism
    *         object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public InputStream GetBMechMethodsWSDL(String bDefPID, Date versDateTime)
       throws GeneralException, ServerException
   {
     try
     {
       if (doReader == null)
       {
         doReader = m_manager.getReader(m_context, PID);
       }
       return doReader.GetBMechMethodsWSDL(bDefPID, versDateTime);
     } catch (ServerException se)
     {
       throw se;
 
     } catch (Throwable th)
     {
       throw new GeneralException("Definitive reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     }
   }
 
   /**
    * <p>Gets a datastream specified by the datastream ID.</p>
    *
    * @param datastreamID The identifier of the requested datastream.
    * @param versDateTime The versioning datetime stamp.
    * @return The specified datastream.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Datastream GetDatastream(String datastreamID, Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreams = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "DataStreamBinding.DSBinding_DS_Label,"
           + "DataStreamBinding.DSBinding_DS_MIME,"
           + "DataStreamBinding.DSBinding_DS_Location "
           + "FROM "
           + "DigitalObject,"
           + "DataStreamBinding "
           + "WHERE "
           + "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "
           + "DataStreamBinding.DSBinding_DS_ID=\'" + datastreamID +"\' AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetDatastreamQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DSLabel = results[0];
           datastream.DSMIME = results[1];
           datastream.DSLocation = results[2];
           queryResults.addElement(datastream);
         }
 
         datastreams = new Datastream[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastream = (Datastream)e.nextElement();
         }
       } catch (Throwable th)
       {
         // Problem with the relational database or query
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastream = doReader.GetDatastream(datastreamID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
       }
     }
     return datastream;
   }
 
   /**
    * <p>Gets all the datastreams of a digital object.</p>
    *
    * @param versDateTime The versioning datetime stamp.
    * @return An array of datastreams.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Datastream[] GetDatastreams(Date versDateTime)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Datastream[] datastreamArray = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "DataStreamBinding.DSBinding_DS_Label,"
           + "DataStreamBinding.DSBinding_DS_MIME,"
           + "DataStreamBinding.DSBinding_DS_Location "
           + "FROM "
           + "DigitalObject,"
           + "DataStreamBinding "
           + "WHERE "
           + "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetDatastreamsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DSLabel = results[0];
           datastream.DSMIME = results[1];
           datastream.DSLocation = results[2];
           queryResults.addElement(datastream);
         }
         datastreamArray = new Datastream[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastreamArray[rowCount] = (Datastream)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in the Definitve storage area; query
       // Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastreamArray = doReader.GetDatastreams(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
       }
     }
     return datastreamArray;
   }
 
   /**
    * <p>Gets a dissemination result.</p>
    *
    * @param PID The persistent identifier for the digital object.
    * @param bDefPID The persistent identifier for the Behavior Definition
    *        object.
    * @param methodName The name of the method to be executed.
    * @param versDateTime The versioning datetime stamp.
    * @return A MIME-typed stream containing the dissemination result.
    * @throws ObjectNotFoundException If object cannot be found.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public DisseminationBindingInfo[] getDissemination(String PID,
       String bDefPID, String methodName, Date versDateTime)
       throws GeneralException
   {
     DisseminationBindingInfo dissBindInfo = null;
     DisseminationBindingInfo[] dissBindInfoArray = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String query =
           "SELECT DISTINCT "
           + "DigitalObject.DO_PID,"
           + "BehaviorDefinition.BDEF_PID,"
           + "Method.METH_Name,"
           + "MechanismImpl.MECHImpl_Address_Location,"
           + "MechanismImpl.MECHImpl_Operation_Location,"
           + "MechanismImpl.MECHImpl_Protocol_Type,"
           + "DataStreamBinding.DSBinding_DS_Location, "
           + "DataStreamBinding.DSBinding_DS_Control_Group_Type, "
           + "DataStreamBinding.DSBinding_DS_ID, "
           + "DataStreamBinding.DSBinding_DS_Current_Version_ID, "
           + "DataStreamBindingSpec.DSBindingSpec_Name "
           + " FROM "
           + "DigitalObject,"
           + "BehaviorDefinition,"
           + "BehaviorMechanism,"
           + "DataStreamBinding,"
           + "Disseminator,"
           + "DigitalObjectDissAssoc,"
           + "MechanismImpl,"
           + "Method,"
           + "DataStreamBindingSpec "
           + " WHERE "
           + "DigitalObject.DO_DBID=DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID=Disseminator.DISS_DBID AND "
           + "Disseminator.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND "
           + "Disseminator.BMECH_DBID = BehaviorMechanism.BMECH_DBID AND "
           + "DataStreamBinding.DO_DBID = DigitalObject.DO_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND "
           + "MechanismImpl.DSBindingKey_DBID = "
           + "DataStreamBinding.DSBindingKey_DBID AND "
           + "DataStreamBindingSpec.DSBindingKey_DBID = "
           + "MechanismImpl.DSBindingKey_DBID AND "
           + "MechanismImpl.METH_DBID = Method.METH_DBID AND "
           + "DigitalObject.DO_PID='" + PID + "' AND "
           + " BehaviorDefinition.BDEF_PID=\'" + bDefPID + "\' AND "
           + " Method.METH_Name=\'"  + methodName + "\' "
           + " ORDER BY DataStreamBindingSpec.DSBindingSpec_Name";
 
       if(debug) s_server.logFinest("GetDisseminationQuery=" + query);
 
       try
       {
         // execute database query and retrieve results
         connection = connectionPool.getConnection();
         if(debug) s_server.logFinest("DisseminationConnectionPool: "+
                                      connectionPool);
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         String[] results = null;
         int cols = rsMeta.getColumnCount();
         // Note: When more than one datastream matches the DSBindingKey
         // or there are multiple DSBindingKeys associated with the method
         // in the dissemination query, multiple rows are returned.
         while (rs.next())
         {
           results = new String[cols];
           dissBindInfo = new DisseminationBindingInfo();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           dissBindInfo.AddressLocation = results[3];
           dissBindInfo.OperationLocation = results[4];
           dissBindInfo.ProtocolType = results[5];
           dissBindInfo.dsLocation = results[6];
           dissBindInfo.dsControlGroupType = results[7];
           dissBindInfo.dsID = results[8];
           dissBindInfo.dsVersionID = results[9];
           dissBindInfo.DSBindKey = results[10];
           try
           {
             dissBindInfo.methodParms = this.GetBMechMethodParms(results[1],
                 results[2], versDateTime);
           } catch (GeneralException ge)
           {
             dissBindInfo.methodParms = null;
           }
           // Add each row of returned data
           queryResults.addElement(dissBindInfo);
         }
         dissBindInfoArray = new DisseminationBindingInfo[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           dissBindInfoArray[rowCount] = (DisseminationBindingInfo)
                              e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
 
         // FIXME!! - code to perform disseminations directly from the
         // XML objects NOT implemented in Phase 1.
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return dissBindInfoArray;
   }
 
   /**
    * <p>Gets a disseminator with the specified ID.</p>
    *
    * @param disseminatorID the identifier of the requested disseminator
    * @param versDateTime versioning datetime stamp
    * @return Disseminator
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Disseminator GetDisseminator(String disseminatorID, Date versDateTime)
       throws GeneralException
   {
     Disseminator disseminator = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "Disseminator.DISS_ID,"
           + "BehaviorDefinition.BDEF_PID,"
           + "BehaviorMechanism.BMECH_PID,"
           + "DataStreamBindingMap.DSBindingMap_ID "
           + "FROM "
           + "BehaviorDefinition,"
           + "Disseminator,"
           + "DataStreamBindingMap,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc,"
           + "BehaviorMechanism "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "
           + "DataStreamBindingMap.BMECH_DBID=BehaviorMechanism.BMECH_DBID AND "
           + "Disseminator.DISS_ID=\'" + disseminatorID + "\' AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetDisseminatorQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           disseminator.bDefID = results[1];
           disseminator.bMechID = results[2];
           disseminator.dsBindMapID = results[3];
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminator = doReader.GetDisseminator(disseminatorID, versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return disseminator;
   }
 
   /**
    * <p>Gets all disseminators of the specified object.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return Disseminator[] array of disseminators
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public Disseminator[] GetDisseminators(Date versDateTime)
       throws GeneralException
   {
     Disseminator[] disseminatorArray = null;
     Disseminator disseminator = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in the Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "Disseminator.DISS_ID,"
           + "BehaviorDefinition.BDEF_PID,"
           + "BehaviorMechanism.BMECH_PID,"
           + "DataStreamBindingMap.DSBindingMap_ID "
           + "FROM "
           + "BehaviorDefinition,"
           + "Disseminator,"
           + "DataStreamBindingMap,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc,"
           + "BehaviorMechanism "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "
           + "DataStreamBindingMap.BMECH_DBID=BehaviorMechanism.BMECH_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("GetDisseminatorsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           disseminator.bDefID = results[1];
           disseminator.bMechID = results[2];
           disseminator.dsBindMapID = results[3];
           queryResults.addElement(disseminator);
         }
         disseminatorArray = new Disseminator[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           disseminatorArray[rowCount] = (Disseminator)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminatorArray = doReader.GetDisseminators(versDateTime);
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return disseminatorArray;
   }
 
   /**
    * <p>Gets datastream binding map.</p>
    *
    * @param versDateTime versioning datetime stamp
    * @return DSBindingMapAugmented[] array of datastream binding maps
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
       throws GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return doReader.GetDSBindingMaps(versDateTime);
     } catch (Throwable th)
     {
       throw new GeneralException("Definitive reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     }
   }
 
   /**
    * <p>Gets the label of the requested object.</p>
    *
    * @return String contining the object label
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectLabel() throws GeneralException
   {
     if (debug) s_server.logFinest("GetObjectLabel = " + doLabel);
     return doLabel;
   }
 
   /**
    * <p>Gets all methods associated with the specified digital object. If the
    * object is found, an array of <code>ObjectMethodsDef</code> is returned.
    * If the object cannot be found in the relational database, the method
    * attempts to find the object in the Definitive storage area. If the object
    * cannot be found, <code>ObjectNotFoundException</code> is thrown.</p>
    *
    * @param PID persistent identifier for the digital object
    * @param versDateTime The versioning datetime stamp.
    * @return ObjectMethodsDef containing all object methods
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public ObjectMethodsDef[] getObjectMethods(String PID, Date versDateTime)
       throws GeneralException
   {
     ObjectMethodsDef[] objectMethodsDefArray = null;
     ObjectMethodsDef objectMethodsDef = null;
     Vector queryResults = new Vector();
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
 
     if (isFoundInFastStore && versDateTime == null)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "DigitalObject.DO_PID,"
           + "BehaviorDefinition.BDEF_PID,"
           + "Method.METH_Name "
           + "FROM "
           + "BehaviorDefinition,"
           + "Disseminator,"
           + "Method,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc,"
           + "BehaviorMechanism,"
           + "MechanismImpl "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "
           + "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND "
           + "BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND "
           + "Method.METH_DBID = MechanismImpl.METH_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("getObjectMethodsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           objectMethodsDef = new ObjectMethodsDef();
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           objectMethodsDef.PID = results[0];
           objectMethodsDef.bDefPID = results[1];
           objectMethodsDef.methodName = results[2];
           MethodParmDef[] methodParms = GetBMechMethodParms(results[1],
               results[2], versDateTime);
           objectMethodsDef.methodParmDefs = methodParms;
           queryResults.add(objectMethodsDef);
         }
         objectMethodsDefArray = new ObjectMethodsDef[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           objectMethodsDefArray[rowCount] = (ObjectMethodsDef)e.nextElement();
           rowCount++;
         }
      } catch (Throwable th)
      {
        throw new GeneralException("Fast reader returned error. The "
                                   + "underlying error was a "
                                   + th.getClass().getName() + "The message "
                                   + "was \"" + th.getMessage() + "\"");
      } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore || versDateTime != null)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitve storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         String[] behaviorDefs = doReader.GetBehaviorDefs(versDateTime);
         Vector results = new Vector();
         for (int i=0; i<behaviorDefs.length; i++)
         {
           MethodDef[] methodDefs = doReader.GetBMechMethods(behaviorDefs[i],
                                    versDateTime);
           for (int j=0; j<methodDefs.length; j++)
           {
             objectMethodsDef = new ObjectMethodsDef();
             objectMethodsDef.PID = PID;
             objectMethodsDef.bDefPID = behaviorDefs[i];
             objectMethodsDef.methodName = methodDefs[j].methodName;
             objectMethodsDef.methodParmDefs = doReader.GetBMechMethodParms(behaviorDefs[i], methodDefs[i].methodName, versDateTime);
             objectMethodsDef.asOfDate = versDateTime;
             results.addElement(objectMethodsDef);
           }
         }
         int rowCount = 0;
         objectMethodsDefArray = new ObjectMethodsDef[results.size()];
         for (Enumeration e = results.elements(); e.hasMoreElements();)
         {
           objectMethodsDefArray[rowCount] = (ObjectMethodsDef)e.nextElement();
           rowCount++;
         }
         return objectMethodsDefArray;
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return objectMethodsDefArray;
   }
 
   /**
    * <p>Gets the persistent identifier or PID of the digital object.</p>
    *
    * @return String containing the persistent identifier
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectPID() throws GeneralException
   {
     if (debug) s_server.logFinest("GetObjectPID = " + PID);
     return this.PID;
   }
 
   /**
    * <p>Gets the state on a digital object</p>
    *
    * @return String state of the object
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String GetObjectState() throws GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader =  m_manager.getReader(m_context, PID);
       }
       return doReader.GetObjectState();
     } catch (Throwable th)
     {
       throw new GeneralException("Definitive reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     }
   }
 
   /**
    * <p>Gets the XML representation of the object. Since the XML representation
    * of an object is not stored in the Fast storage area, this method always
    * queries the Definitive storage area using <code>DefinitveDOReader</code>.
    * </p>
    *
    * @return String containing the XML representation of the object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws StreamIOException If there was a failure in accessing the object
    *         for any IO reason during retrieval of the object from low-level
    *         storage. Extends ServerException.
    */
   public InputStream GetObjectXML()
       throws StreamIOException, GeneralException
   {
     try
     {
       if (doReader == null)
       {
         doReader = m_manager.getReader(m_context, PID);
       }
       return(doReader.GetObjectXML());
     } catch (Throwable th)
     {
       throw new GeneralException("Definitive reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     }
   }
 
   /**
    * <p>Lists the datastream IDs of the requested object having the
    * specified <code>state</code>. Note that the Fast storage area does NOT
    * contain state information so state is ignored when querying the Fast
    * storage area. <code>DefinitiveDOReader</code> should be used instead
    * to list datastream IDs with a given state.</p>
    *
    * @param state State of the datastreams.
    * @return An array containing the datastream IDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] ListDatastreamIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     String[] datastreamIDs = null;
     Datastream datastream = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database.
       String  query =
           "SELECT DISTINCT "
           + "DataStreamBinding.DSBinding_DS_ID "
           + "FROM "
           + "DigitalObject,"
           + "DataStreamBinding "
           + "WHERE "
           + "DigitalObject.DO_DBID = DataStreamBinding.DO_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("ListDatastreamIDsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           datastream = new Datastream();
           datastream.DatastreamID = results[0];
           queryResults.addElement(datastream);
         }
         datastreamIDs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           datastreamIDs[rowCount] = (String)e.nextElement();
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         datastreamIDs = doReader.ListDatastreamIDs("");
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return datastreamIDs;
   }
 
   /**
    * Gets a list of disseminator IDs. Note that the Fast storage area does
    * not contain state information and state is ignored.
    * <code>DefinitiveDOReader</code> should be used to list disseminator IDs
    * when state is specified.
    *
    * @param state State of the disseminators.
    * @return An array listing disseminator IDs.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    */
   public String[] ListDisseminatorIDs(String state)
       throws GeneralException
   {
     Vector queryResults = new Vector();
     Disseminator disseminator = null;
     String[] disseminatorIDs = null;
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     if (isFoundInFastStore)
     {
       // Requested object exists in Fast storage area and is NOT versioned;
       // query relational database
       String  query =
           "SELECT DISTINCT "
           + "Disseminator.DISS_ID "
           + "FROM "
           + "Disseminator,"
           + "DigitalObject,"
           + "DigitalObjectDissAssoc "
           + "WHERE "
           + "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "
           + "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "
           + "DigitalObject.DO_PID=\'" + PID + "\';";
 
       if (debug) s_server.logFinest("ListDisseminatorIDsQuery: " + query);
       String[] results = null;
       try
       {
         connection = connectionPool.getConnection();
         statement = connection.createStatement();
         rs = statement.executeQuery(query);
         ResultSetMetaData rsMeta = rs.getMetaData();
         int cols = rsMeta.getColumnCount();
         while (rs.next())
         {
           results = new String[cols];
           for (int i=1; i<=cols; i++)
           {
             results[i-1] = rs.getString(i);
           }
           disseminator = new Disseminator();
           disseminator.dissID = results[0];
           queryResults.addElement(disseminator);
         }
         disseminatorIDs = new String[queryResults.size()];
         int rowCount = 0;
         for (Enumeration e = queryResults.elements(); e.hasMoreElements();)
         {
           disseminator = (Disseminator)e.nextElement();
           disseminatorIDs[rowCount] = disseminator.dissID;
           rowCount++;
         }
       } catch (Throwable th)
       {
         throw new GeneralException("Fast reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     } else if (isFoundInDefinitiveStore)
     {
       // Requested object exists in Definitive storage area or is versioned;
       // query Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         disseminatorIDs = doReader.ListDisseminatorIDs("A");
       } catch (Throwable th)
       {
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     }
     return disseminatorIDs;
   }
 
   /**
    * <p>Locates the specified digital object using its persitent identifier.
    * This method will first attempt to locate the object in the Fast storage
    * area. If the the object cannot be located there, it will then try to find
    * it in the Definitive strorage area. If the object is found, the object's
    * label is returned. Otherwise, it throws
    * <code>ObjectNotFoundException</code>.</p>
    *
    * @param PID persistent identifier of the digital object.
    * @return String containing label of the specified digital object.
    * @throws GeneralException If there was any misc exception that we want to
    *         catch and re-throw as a Fedora exception. Extends ServerException.
    * @throws ServerException If any type of error occurred fulfilling the
    *         request.
    */
   public String locatePID(String PID) throws GeneralException, ServerException
   {
     Connection connection = null;
     Statement statement = null;
     ResultSet rs = null;
     String  query =
         "SELECT "
         + "DigitalObject.DO_Label "
         + "FROM "
         + "DigitalObject "
         + "WHERE "
         + "DigitalObject.DO_PID=\'" + PID + "\';";
     if (debug) s_server.logFinest("LocatPIDQuery: " + query);
 
     try
     {
       connection = connectionPool.getConnection();
       if(debug) s_server.logFinest("LocatePIDConnectionPool: "
                                    + connectionPool);
       statement = connection.createStatement();
       rs = statement.executeQuery(query);
       while (rs.next())
       {
         doLabel = rs.getString(1);
       }
     } catch (Throwable th)
     {
       throw new GeneralException("Fast reader returned error. The "
                                  + "underlying error was a "
                                  + th.getClass().getName() + "The message "
                                  + "was \"" + th.getMessage() + "\"");
     } finally
       {
         if (connection != null)
         {
           try
           {
             rs.close();
             statement.close();
             connectionPool.free(connection);
             connection.close();
           } catch (SQLException sqle)
           {
             throw new GeneralException("Unexpected error from SQL "
                 + "database. The error was: " + sqle.getMessage());
           }
         }
       }
     if (doLabel == null || doLabel.equalsIgnoreCase(""))
     {
       // Empty result means that the digital object could not be found in the
       // relational database. This could be due to incorrectly specified
       // parameter for PID OR the object is not in the relational database.
       // If not in the relational database, attempt to find the object in the
       // Definitive storage area.
       try
       {
         if (doReader == null)
         {
           doReader = m_manager.getReader(m_context, PID);
         }
         doLabel = doReader.GetObjectLabel();
         isFoundInDefinitiveStore = true;
         s_server.logFinest("OBJECT FOUND IN DEFINITIVE STORE: " + PID);
       } catch (ServerException se)
       {
         throw se;
       } catch (Throwable th)
       {
         s_server.logWarning("OBJECT NOT FOUND IN DEFINITIVE STORE: " + PID);
         throw new GeneralException("Definitive reader returned error. The "
                                    + "underlying error was a "
                                    + th.getClass().getName() + "The message "
                                    + "was \"" + th.getMessage() + "\"");
       }
     } else
     {
       isFoundInFastStore = true;
       s_server.logFinest("OBJECT FOUND IN FAST STORE: " + PID);
     }
     return doLabel;
   }
 }
