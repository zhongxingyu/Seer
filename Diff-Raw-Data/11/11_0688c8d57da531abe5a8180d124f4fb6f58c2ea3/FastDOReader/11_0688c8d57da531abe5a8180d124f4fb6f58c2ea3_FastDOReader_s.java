 package fedora.server.storage;
 
 /**
 * <p>Title: </p>
  * <p>Description: Digital Object Reader. Reads objects in SQL database</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // java imports
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Vector;
 
 // fedora imports
 import fedora.server.access.MIMETypedStream;
 
 
 public class FastDOReader extends DefinitiveDOReader implements DisseminatingDOReader
 {
 
 private static ConnectionPool connectionPool = null;
 //FIXME!! need to decide where to locate the db.properties file
 private static final String dbPropsFile = "db.properties";
 
   public FastDOReader()
   {
   }
 
   public FastDOReader(String PID, String bDefPID, String method)
   {
     //initialize database connection
     initDB();
   }
 
   public Vector getDissemination(String PID, String bDefPID, String method)
   {
     Vector queryResults = new Vector();
     String query = "SELECT DISTINCT "+
         "DigitalObject.DO_PID,"+
         "Disseminator.DISS_Label,"+
         "Method.METH_Name,"+
         "MechanismImpl.MECHImpl_Address_Location,"+
         "MechanismImpl.MECHImpl_Operation_Location,"+
         "MechanismImpl.MECHImpl_Protocol_Type,"+
         "MechanismImpl.MECHImpl_Return_Type,"+
         "DigitalObjectBindingMap.DOBINDINGMap_DS_Location, "+
         "DatastreamBindingKey.DSBindingKey_Name "+
         " FROM "+
         "DigitalObject,"+
         "BehaviorDefinition,"+
         "BehaviorMechanism,"+
         "DigitalObjectBindingMap,"+
         "Disseminator,"+
         "DigitalObjectDissAssoc,"+
         "MechanismImpl,"+
         "Method,"+
         "DatastreamBindingKey "+
  	" WHERE "+
         "DigitalObject.DO_DBID=DigitalObjectDissAssoc.DO_DBID AND "+
 	"DigitalObjectDissAssoc.DISS_DBID=Disseminator.DISS_DBID AND " +
 	"Disseminator.BDEF_DBID = BehaviorDefinition.BDEF_DBID AND " +
 	"Disseminator.BMECH_DBID = BehaviorMechanism.BMECH_DBID AND " +
 	"DigitalObjectBindingMap.DO_DBID = DigitalObject.DO_DBID AND " +
 	"BehaviorMechanism.BMECH_DBID = MechanismImpl.BMECH_DBID AND " +
 	"BehaviorDefinition.BDEF_DBID = MechanismImpl.BDEF_DBID AND " +
 	"MechanismImpl.DSBindingKey_DBID = DigitalObjectBindingMap.DSBindingKey_DBID AND " +
         "DatastreamBindingKey.DSBindingKey_DBID = MechanismImpl.DSBindingKey_DBID AND "+
 	"MechanismImpl.METH_DBID = Method.METH_DBID AND " +
 	"DigitalObject.DO_PID='" + PID + "' AND " +
 	" BehaviorDefinition.BDEF_PID='" + bDefPID + "' AND " +
 	" Method.METH_Name='"  + method + "' ";
     if(debug) System.out.println("query="+query+"\n");
     try
     {
       // execute database query and retrieve results
       Connection connection = connectionPool.getConnection();
       if(debug) System.out.println("connectionPool = "+connectionPool);
       Statement statement = connection.createStatement();
       ResultSet rs = statement.executeQuery(query);
       ResultSetMetaData rsMeta = rs.getMetaData();
       int cols = rsMeta.getColumnCount();
      String[] results = new String[cols];
       // Note: When more than one datastream matches the DSBindingKey
       // in the dissemination query, multiple rows are returned.
       while (rs.next())
       {
         for (int i=1; i<=cols; i++)
         {
           results[i-1] = rs.getString(i);
         }
         queryResults.addElement(results);
       }
       connectionPool.free(connection);
       connection.close();
       statement.close();
     } catch (SQLException sqle)
     {
       System.out.println(sqle);
     }
     return queryResults;
   }
 
   public Vector getMethodParms(String bDefPID, String method)
   {
     Vector methodParms = new Vector();
     String query = "SELECT "+
         "PARM_Name,"+
         "PARM_Default_Value,"+
         "PARM_Required_Flag,"+
         "PARM_Label "+
         " FROM "+
         "Method,"+
         "Parameter "+
         " WHERE "+
         "Parameter.METH_DBID=Method.METH_DBID AND "+
         "Method.METH_Name='"  + method + "' ";
     if(debug) System.out.println("query="+query+"\n");
     try
     {
       Connection connection = connectionPool.getConnection();
       if(debug) System.out.println("connectionPool = "+connectionPool);
       Statement statement = connection.createStatement();
       ResultSet rs = statement.executeQuery(query);
       ResultSetMetaData rsMeta = rs.getMetaData();
       int cols = rsMeta.getColumnCount();
       String[] results = new String[cols];
       // Note: a row is returned for each method parameter
       while (rs.next())
       {
         for (int i=1; i<=cols; i++)
         {
           results[i-1] = rs.getString(i);
         }
         methodParms.addElement(results);
       }
       connectionPool.free(connection);
       connection.close();
       statement.close();
     } catch (SQLException sqle)
     {
       System.out.println(sqle);
     }
     return methodParms;
   }
 
   public MIMETypedStream getHttpContent(String urlString)
   {
     MIMETypedStream httpContent = null;
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     try
     {
       URL url = new URL(urlString);
       HttpURLConnection connection = (HttpURLConnection)url.openConnection();
       String contentType = connection.getContentType();
       if(debug) System.out.println("MIMEType = "+contentType);
       InputStream is = connection.getInputStream();
       int byteStream = 0;
       while((byteStream = is.read()) >=0 )
       {
         baos.write(byteStream);
       }
       httpContent = new MIMETypedStream(contentType, baos.toByteArray());
     } catch (MalformedURLException murle)
     {
       System.out.println(murle);
     } catch (IOException ioe)
     {
       System.out.println(ioe);
     }
 
     return httpContent;
   }
 
   public static void initDB()
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
       if(debug) System.out.println("/nurl = "+url);
 
       // initialize connection pool
       connectionPool = new ConnectionPool(driver, url, username, password,
           initConnections, maxConnections, true);
     } catch (SQLException sqle)
     {
       System.out.println("Unable to create connection pool: "+sqle);
       connectionPool = null;
     } catch (FileNotFoundException fnfe)
     {
       System.out.println("Unable to read the properties file: " +
           dbPropsFile);
     } catch (IOException ioe)
     {
       System.out.println(ioe);
     }
 
   }
 
   public static void main(String[] args)
   {
     // Test dissemination query against SQL database
     FastDOReader fdor = new FastDOReader("1007.lib.dl.test/text_ead/viu00003","web_ead","get_web_default");
     Vector results = null;
     results = fdor.getDissemination("1007.lib.dl.test/text_ead/viu00003","web_ead","get_web_default");
     Enumeration e = results.elements();
    String[] list  = null;
     while(e.hasMoreElements())
     {
      list = (String[])e.nextElement();
       for(int i=0; i<list.length; i++)
       {
         System.out.println("dissemResults["+i+"] = "+list[i]+"\n");
       }
     }
 
     // Test reading method paramters from SQL database
     Vector methodParms = null;
     methodParms = fdor.getMethodParms("web_ead","get_web_default");
     Enumeration e2 = methodParms.elements();
     String[] methodParm  = null;
     while(e2.hasMoreElements())
     {
       methodParm = (String[])e2.nextElement();
       for(int i=0; i<methodParm.length; i++)
       {
         System.out.println("methodParm["+i+"] = "+methodParm[i]+"\n");
       }
     }
 
     // Test reading Http content from the internet
     MIMETypedStream ms = fdor.getHttpContent("http://icarus.lib.virginia.edu/~rlw/test.html");
     try
     {
       System.out.write(ms.stream);
     } catch (IOException ioe)
     {
       System.out.println(ioe);
     }
 
   }
 }
