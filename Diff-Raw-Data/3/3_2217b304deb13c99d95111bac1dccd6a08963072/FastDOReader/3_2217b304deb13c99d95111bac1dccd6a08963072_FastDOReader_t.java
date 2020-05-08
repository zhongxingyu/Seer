 package fedora.server.storage;
 
 /**
  * <p>Title: FastDOReader.java</p>
  * <p>Description: Digital Object Reader. Reads objects in SQL database</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // java imports
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Vector;
 
 // fedora imports
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.storage.types.DSBindingMapAugmented;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.errors.ObjectNotFoundException;
 import fedora.server.errors.MethodParmNotFoundException;
 import fedora.server.utilities.DateUtility;
 
 public class FastDOReader implements DisseminatingDOReader
 {
 
 private static ConnectionPool connectionPool = null;
 private static boolean debug = true;
 //FIXME!! need to decide where to locate the db.properties file
 private static final String dbPropsFile = "db.properties";
 
   public FastDOReader()
   {
   }
 
   public FastDOReader(String PID, String bDefPID, String methodName,
                       Date versDateTime) throws ObjectNotFoundException
   {
     //initialize database connection
     initDB();
   }
 
   /**
    * A method that resolves a dissemination request by attempting to
    * locate the necessary information in the SQL database. If not found
    * there, it will then attempt to locate the information in the
    * Definitive XML storage area.
    *
    * @param PID Persistent idenitfier for the digital object
    * @param bDefPID Persistent identifier for the Behavior Definition object
    * @param methodName Name of the method to be executed
    * @param versDateTime Versioning datetime stamp
    * @return MIMETypedStream containing the dissemination result
    * @throws ObjectNotFoundException
    */
   public Vector getDissemination(String PID, String bDefPID, String methodName,
                                  Date versDateTime)
       throws ObjectNotFoundException
   {
     Vector queryResults = new Vector();
     String query = "SELECT DISTINCT "+
         "DigitalObject.DO_PID,"+
         "Disseminator.DISS_ID,"+
         "Method.METH_Name,"+
         "MechanismImpl.MECHImpl_Address_Location,"+
         "MechanismImpl.MECHImpl_Operation_Location,"+
         "MechanismImpl.MECHImpl_Protocol_Type,"+
         "DataStreamBinding.DSBinding_DS_Location, "+
         "DataStreamBindingSpec.DSBindingSpec_Name "+
         " FROM "+
         "DigitalObject,"+
         "BehaviorDefinition,"+
         "BehaviorMechanism,"+
         "DataStreamBinding,"+
         "Disseminator,"+
         "DigitalObjectDissAssoc,"+
         "MechanismImpl,"+
         "Method,"+
         "DataStreamBindingSpec "+
  	" WHERE "+
         "DigitalObject.DO_DBID=DigitalObjectDissAssoc.DO_DBID AND "+
 	"DigitalObjectDissAssoc.DISS_DBID=Disseminator.DISS_DBID AND " +
 	"Disseminator.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND " +
 	"Disseminator.BMECH_DBID = BehaviorMechanism.BMECH_DBID AND " +
 	"DataStreamBinding.DO_DBID = DigitalObject.DO_DBID AND " +
 	"BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND " +
 	"MechanismImpl.DSBindingKey_DBID = DataStreamBinding.DSBindingKey_DBID AND " +
         "DataStreamBindingSpec.DSBindingKey_DBID = MechanismImpl.DSBindingKey_DBID AND "+
 	"MechanismImpl.METH_DBID = Method.METH_DBID AND " +
 	"DigitalObject.DO_PID='" + PID + "' AND " +
 	" BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND " +
 	" Method.METH_Name='"  + methodName + "' "+
         " ORDER BY DataStreamBindingSpec.DSBindingSpec_Name";
     if(debug) System.out.println("DissemQuery="+query+"\n");
     try
     {
       // execute database query and retrieve results
       Connection connection = connectionPool.getConnection();
       if(debug) System.out.println("connectionPool = "+connectionPool);
       Statement statement = connection.createStatement();
       ResultSet rs = statement.executeQuery(query);
       ResultSetMetaData rsMeta = rs.getMetaData();
       int cols = rsMeta.getColumnCount();
       // Note: When more than one datastream matches the DSBindingKey
       // or there are multiple DSBindingKeys associated with the method
       // in the dissemination query, multiple rows are returned.
       while (rs.next())
       {
         String[] results = new String[cols];
         for (int i=1; i<=cols; i++)
         {
           results[i-1] = rs.getString(i);
         }
         // Add each row of returned data
         queryResults.addElement(results);
       }
       connectionPool.free(connection);
       connection.close();
       statement.close();
     } catch (SQLException sqle)
     {
       // Problem with the SQL database or query
       ObjectNotFoundException onfe = new ObjectNotFoundException("");
       onfe.initCause(sqle);
       throw onfe;
 
     }
 
     if (queryResults.isEmpty())
     {
       // Empty result means that object could not be found in the
       // SQL database. This could be due to incorrectly specified
       // parameters for PID, bDefPID, methodName, or asOfDate OR the
       // object is not in the SQL database. If not in the SQL
       // database, attempt to find the object in the Definitive XML
       // storage.
       try
       {
         // Try to find object in the Definitive storage.
         DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
         // FIXME - code to perform disseminations directly from the
         // XML objects NOT implemented in this release.
         return queryResults;
         // FIXME - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any Exception.
       } catch (Exception e)
       {
         // If object cannot be found in the Definitive storage, then
         // the dissemination request contains errors or the object does
         // NOT exist in the repository. In either case, this is a
         // nonfatal error that is passed back up the line.
         String message = "OBJECT NOT FOUND --\nPID: "+PID+"\n bDefPID: "+bDefPID+
                          "\n methodName: "+methodName+"\n asOfDate: "+
                          DateUtility.convertDateToString(versDateTime)+"\n";
         throw new ObjectNotFoundException(message);
       }
     } else
     {
       // Dissemination was successful; return results.
       return queryResults;
     }
   }
 
   /**
    * A method that gets a list methods and any associated method parameters
    * by attempting to read from the SQL database. If the information is not
    * found there, it will attempt to find the information in the Definitive
    * XML storage area.
    *
    * @param bDefPID Persistent identifier for the Behavior Mechanism object
    * @param methodName Name of the method
    * @param versDateTime Versioning datetime stamp
    * @return Vector containing rows from the SQL query
    * @throws MethodParmNotFoundException
    */
   public Vector getMethodParms(String bDefPID, String methodName,
                                Date versDateTime)
       throws MethodParmNotFoundException
   {
     Vector queryResults = new Vector();
 
     // Note that the query retrieves the list of available methods
     // based on Behavior Mechanism object and NOT the Behavior
     // Definition object. This is done to insure that only methods
     // that have been implemented in the mechanism are returned.
     // This distinction is only important when versioning is enabled
     // in a later release. When versioning is enabled, it is possible
     // that a given Behavior Definition may have methods that have not
     // yet been implemented by all of its associated Behavior Mechanisms.
     // In such a case, only those methods implemented in the mechanism
     // will be returned.
     String query = "SELECT "+
             "PARM_Name,"+
             "PARM_Default_Value,"+
             "PARM_Required_Flag,"+
             "PARM_Label "+
             " FROM "+
             "BehaviorDefinition,"+
             "BehaviorMechanism,"+
             "MechanismImpl,"+
             "Method,"+
             "Parameter "+
             " WHERE "+
             "BehaviorMechanism.BDEF_DBID=Parameter.BDEF_DBID AND "+
             "Method.BDEF_DBID=Parameter.BDEF_DBID AND "+
             "Method.METH_DBID=Parameter.METH_DBID AND "+
             "BehaviorMechanism.BDEF_DBID=Method.BDEF_DBID AND "+
             "MechanismImpl.METH_DBID=Method.METH_DBID AND " +
             "BehaviorMechanism.BDEF_DBID=BehaviorDefinition.BDEF_DBID AND "+
             "BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND "+
             "Method.METH_Name='"  + methodName + "' ";
 
     if(debug) System.out.println("MethodParmQuery="+query+"\n");
     try
     {
       Connection connection = connectionPool.getConnection();
       if(debug) System.out.println("connectionPool = "+connectionPool);
       Statement statement = connection.createStatement();
       ResultSet rs = statement.executeQuery(query);
       ResultSetMetaData rsMeta = rs.getMetaData();
       int cols = rsMeta.getColumnCount();
       // Note: a row is returned for each method parameter
       while (rs.next())
       {
         String[] results = new String[cols];
         for (int i=1; i<=cols; i++)
         {
           results[i-1] = rs.getString(i);
         }
         // Add each row of results to vector
         queryResults.addElement(results);
       }
       connectionPool.free(connection);
       connection.close();
       statement.close();
     } catch (SQLException sqle)
     {
       // Problem with the SQL database or query
       MethodParmNotFoundException mpnfe = new MethodParmNotFoundException("");
       mpnfe.initCause(sqle);
       throw mpnfe;
     }
 
     if (queryResults.isEmpty())
     {
       // Empty result means that method(Behavior Mechanism object) could
       // not be found in the SQL database. This could be due to incorrectly
       // specified parameters for bDefPID and/or method OR the method is not
       // not in the SQL database. If not in the SQL database, attempt
       // to find the object in the Definitive storage.
       try
       {
         // Try to find method parameters in the Definitive storage.
         // FIXME - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the sample
         // objects in DefinitiveBMechReader
         DefinitiveBMechReader bmechReader = new DefinitiveBMechReader(bDefPID);
         // FIXME - code to get method parameters directly from the
         // XML objects NOT implemented yet.
         return queryResults;
         // FIXME - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // If method cannot be found in the Definitive storage, then
         // the method parameter request contains errors or the method
         // does NOT exist in the repository. In either case, this is a
         // nonfatal error that is passed back up the line.
         String message = "METHOD PARM NOT FOUND --\n bDefPID: "+bDefPID+
                          "\n methodName: "+methodName+"\n asOfDate: "+
                          DateUtility.convertDateToString(versDateTime)+"\n";
         throw new MethodParmNotFoundException(message);
       }
     } else
     {
       // Request for method parameters successful; return results.
       return queryResults;
     }
   }
 
   /**
    * A method that retrieves information from the SQL database about a digital
    * object that includes: Disseminators, Behavior Definitions, Behavior
    * Mechanisms, and their methods. If the object is not found in the SQL
    * database, the method attempts to find the object in the Definitive XML
    * storage area.
    *
    * @param PID Persistent identifier for the digital object
    * @return Vector containing rows from SQL query
    * @throws ObjectNotFoundException
    */
   public Vector getObject(String PID, Date versDateTime) throws ObjectNotFoundException
   {
     Vector queryResults = new Vector();
     String  query =
         "SELECT DISTINCT "+
         "DigitalObject.DO_PID,"+
         "Disseminator.DISS_ID,"+
         "BehaviorDefinition.BDEF_PID,"+
         "BehaviorMechanism.BMECH_PID,"+
         "Method.METH_Name "+
         "FROM "+
         "BehaviorDefinition,"+
         "Disseminator,"+
         "Method,"+
         "DigitalObject,"+
         "DigitalObjectDissAssoc,"+
         "BehaviorMechanism,"+
         "MechanismImpl "+
         "WHERE "+
         "DigitalObject.DO_DBID = DigitalObjectDissAssoc.DO_DBID AND "+
         "DigitalObjectDissAssoc.DISS_DBID = Disseminator.DISS_DBID AND "+
         "BehaviorDefinition.BDEF_DBID = Disseminator.BDEF_DBID AND "+
         "BehaviorMechanism.BMECH_DBID = Disseminator.BMECH_DBID AND "+
         "BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND "+
         "BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND "+
         "Method.METH_DBID = MechanismImpl.METH_DBID AND "+
         "DigitalObject.DO_PID=\'" + PID + "\';";
 
     if (debug) System.out.println("ObjectQuery: "+query);
     ResultSet rs = null;
     String[] results = null;
     try
     {
       Connection connection = connectionPool.getConnection();
       Statement statement = connection.createStatement();
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
         queryResults.add(results);
       }
      connectionPool.free(connection);
      connection.close();
      statement.close();
     } catch (SQLException sqle)
     {
       // Problem with the SQL database or query
       ObjectNotFoundException onfe = new ObjectNotFoundException("");
       onfe.initCause(sqle);
       throw onfe;
     }
     if (queryResults.isEmpty())
     {
       // Empty result means that the digital object could not be found in the
       // SQL database. This could be due to incorrectly specified parameters
       // for PID OR the method is not in the SQL database. If not in the SQL
       // database, attempt to find the object in the Definitive XML storage
       // area.
       try
       {
         // Try to find object in the Definitive XML storage area.
         // FIXME - until xml storage code is implemented, the call below
         // will throw a FileNotFound exception unless the object is one of the sample
         // objects in DefinitiveBMechReader
         DefinitiveDOReader doReader = new DefinitiveDOReader(PID);
         // FIXME - need to add code to get object info from XML objects
         return queryResults;
         // FIXME - need to catch appropriate Exception thrown by
         // DefinitiveDOReader if the PID cannot be found. For now,
         // just catch any exception.
       } catch (Exception e)
       {
         // If object cannot be found in the Definitive XML storage area,
         // then the object request contains errors or the object does NOT
         // exist in the repository. In either case, this is a nonfatal
         // error that is passed back up the line.
         String message = "OBJECT NOT FOUND --\n PID: "+PID+"\n asOfDate: "+
                          DateUtility.convertDateToString(versDateTime)+"\n";
         throw new ObjectNotFoundException(message);
       }
     } else
     {
       // Request for object successful; return results.
       return queryResults;
     }
   }
 
   public static void initDB() throws ObjectNotFoundException
   {
     try
     {
       // read database properties file and init connection pool
       FileInputStream fis = new FileInputStream(dbPropsFile);
       Properties dbProps = new Properties();
       dbProps.load(fis);
       String driver = dbProps.getProperty("drivers");
       String username = dbProps.getProperty("username");
       String password = dbProps.getProperty("password");
       String url = dbProps.getProperty("url");
       Integer i1 = new Integer(dbProps.getProperty("initConnections"));
       int initConnections = i1.intValue();
       Integer i2 = new Integer(dbProps.getProperty("maxConnections"));
       int maxConnections = i2.intValue();
       if(debug) System.out.println("\nurl = "+url);
 
       // initialize connection pool
       //ConnectionPool connectionPool = null;
       connectionPool = new ConnectionPool(driver, url, username, password,
           initConnections, maxConnections, true);
     } catch (SQLException sqle)
     {
       // Problem with connection pool and/or database
       System.out.println("Unable to create connection pool: "+sqle);
       ConnectionPool connectionPool = null;
       connectionPool = null;
       ObjectNotFoundException onfe = new ObjectNotFoundException("");
       onfe.initCause(sqle);
       throw onfe;
     } catch (FileNotFoundException fnfe)
     {
       System.out.println("Unable to read the properties file: " +
           dbPropsFile);
       ObjectNotFoundException onfe = new ObjectNotFoundException("");
       onfe.initCause(fnfe);
       throw onfe;
     } catch (IOException ioe)
     {
       System.out.println(ioe);
       ObjectNotFoundException onfe = new ObjectNotFoundException("");
       onfe.initCause(ioe);
       throw onfe;
     }
 
   }
 
   // Methods required by DOReader
   public String GetObjectXML()
   {
     return null;
   }
 
   public String ExportObject()
   {
     return null;
   }
 
   public String GetObjectPID()
   {
     return null;
   }
 
   public String GetObjectLabel()
   {
     return null;
   }
 
   public String[] ListDatastreamIDs(String state)
   {
     return null;
   }
 
   public Datastream[] GetDatastreams(Date versDateTime)
   {
     return null;
   }
 
   public Datastream GetDatastream(String datastreamID, Date versDateTime)
   {
     return null;
   }
 
   public Disseminator[] GetDisseminators(Date versDateTime)
   {
     return null;
   }
 
   public String[] ListDisseminatorIDs(String state)
   {
     return null;
   }
 
   public Disseminator GetDisseminator(String disseminatorID, Date versDateTime)
   {
     return null;
   }
 
   public String[] GetBehaviorDefs(Date versDateTime)
   {
     return null;
   }
 
   public MethodDef[] GetBMechMethods(String bDefPID, Date versDateTime)
   {
     return null;
   }
 
   public InputStream GetBMechMethodsWSDL(String bDefPID, Date versDateTime)
   {
     return null;
   }
 
   public DSBindingMapAugmented[] GetDSBindingMaps(Date versDateTime)
   {
     return null;
   }
 
   public static void main(String[] args)
   {
     // Test dissemination query against SQL database
     String PID = "1007.lib.dl.test/text_ead/viu00003";
     String bDefPID = "web_ead";
     String methodName = "get_web_default";
     Date versDateTime = DateUtility.convertStringToDate("2002-08-21T12:30:58");
     FastDOReader fdor = null;
     Vector results = new Vector();
     System.out.println("\nBEGIN -- TEST RESULTS FOR DISSEMINATION:");
     try
     {
       fdor = new FastDOReader(PID, bDefPID, methodName, versDateTime);
       results = fdor.getDissemination(PID, bDefPID, methodName, versDateTime);
       Enumeration e = results.elements();
       while(e.hasMoreElements())
       {
         String[] list = (String[])e.nextElement();
         for(int i=0; i<list.length; i++)
         {
           System.out.println("dissemResults["+i+"] = "+list[i]+"\n");
         }
       }
     } catch(ObjectNotFoundException onfe)
     {
       System.out.println(onfe.getMessage());
       //onfe.printStackTrace();
     }
     System.out.println("END -- TEST RESULTS FOR DISSEMINATION\n");
 
     // Test reading method paramters from SQL database
     Vector methodParms = null;
     System.out.println("\nBEGIN -- TEST RESULTS FOR READING METHOD PARMS:");
     try
     {
       methodParms = fdor.getMethodParms(bDefPID,methodName,
                                         versDateTime);
       Enumeration e2 = methodParms.elements();
       while(e2.hasMoreElements())
       {
         String[] methodParm = (String[])e2.nextElement();
         for(int i=0; i<methodParm.length; i++)
         {
           System.out.println("methodParm["+i+"] = "+methodParm[i]+"\n");
         }
       }
     } catch(MethodParmNotFoundException mpnfe)
     {
       System.out.println("Method has no parameters");
     }
     System.out.println("END -- TEST RESULTS FOR READING METHOD PARAMETERS\n");
 
     System.out.println("TEST GET OBJECT");
     try{
     Vector rs = fdor.getObject(PID, versDateTime);
     System.out.println("size: "+rs.size());
     Enumeration e = rs.elements();
     while (e.hasMoreElements())
     {
       String[] res = (String[])e.nextElement();
       for (int i=0; i<res.length; i++)
       {
         System.out.println("res["+i+"] = "+res[i]);
       }
     }
     } catch(Exception e)
     {
       System.out.println("Error"+e.getMessage());
     }
   }
 }
