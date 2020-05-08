 package fedora.server.storage;
 
 /**
  * <p>Title: ConnectionPoolManagerImpl.java</p>
  * <p>Description: Implements ConnectionPoolManager to facilitate obtaining
  * ConnectionPools. This class initializes the ConnectionPools specified
  * by parameters in the Fedora <code>fedora.fcfg</code> configuration file.
  * The Fedora server must be instantiated in order for this class to
  * function properly.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 // java imports
 import java.io.File;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.regex.PatternSyntaxException;
 import java.sql.SQLException;
 
 // Fedora imports
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.InitializationException;
 import fedora.server.Module;
 import fedora.server.Server;
 
 public class ConnectionPoolManagerImpl extends Module
     implements ConnectionPoolManager
 {
 
   private Hashtable h_ConnectionPools = null;
   private String jdbcDriverClass = null;
   private String dbUsername = null;
   private String dbPassword = null;
   private String jdbcURL = null;
   private int initConnections = 0;
   private int maxConnections = 0;
   private static Server s_server;
 
   static
   {
     try
     {
       s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
     } catch (InitializationException ie)
     {
       System.err.println(ie.getMessage());
       System.err.flush();
     }
   }
 
   /**
    * <p>Creates a new ConnectionPoolManager</p>
    *
    * @param moduleParameters name/value pair map of module parameters
    * @param server Server instance
    * @param role String containing the module role name
    * @throws ModuleInitializationException
    */
   public ConnectionPoolManagerImpl(Map moduleParameters,
                                    Server server, String role)
       throws ModuleInitializationException
   {
     super(moduleParameters, server, role);
   }
 
   /**
    * Initializes the Module based on configuration parameters.
    *
    * @throws ModuleInitializationException If initialization values are
    *         invalid or initialization fails for some other reason.
    */
   public void initModule() throws ModuleInitializationException
   {
 
     try
     {
       // Get list of connection pool names from fedora.fcfg config file.
       String poolList =
           s_server.getModule("fedora.server.storage.ConnectionPoolManager").
           getParameter("poolNames");
       String[] poolNames = poolList.split(",");
       // Initialize each pool found
       for (int i=0; i<poolNames.length; i++)
       {
         jdbcDriverClass = s_server.getDatastoreConfig(poolNames[i]).
                  getParameter("jdbcDriverClass");
         dbUsername = s_server.getDatastoreConfig(poolNames[i]).
                    getParameter("dbUsername");
         dbPassword = s_server.getDatastoreConfig(poolNames[i]).
                    getParameter("dbPassword");
         jdbcURL = s_server.getDatastoreConfig(poolNames[i]).
                   getParameter("jdbcURL");
         Integer i1 = new Integer(s_server.getDatastoreConfig(poolNames[i]).
                                  getParameter("minPoolSize"));
         int initConnections = i1.intValue();
         Integer i2 = new Integer(s_server.getDatastoreConfig(poolNames[i]).
                                  getParameter("maxPoolSize"));
         int maxConnections = i2.intValue();
         System.out.println("class: "+jdbcDriverClass+dbUsername+dbPassword+
                            jdbcURL+initConnections+maxConnections);
         System.out.flush();
         ConnectionPool connectionPool  = null;
         // Create connection pool
         connectionPool = new ConnectionPool(jdbcDriverClass, jdbcURL,
             dbUsername, dbPassword, initConnections, maxConnections, true);
         // Add ConnectionPool to hashtable
         h_ConnectionPools.put(poolNames[i],connectionPool);
       }
     } catch (SQLException sqe)
     {
       throw new ModuleInitializationException(
          sqe.getMessage(),"fedora.server.storage.ConnectionPoolManagerIml");
     } catch (PatternSyntaxException pse)
     {
       throw new ModuleInitializationException(
          pse.getMessage(),"fedora.server.storage.ConnectionPoolManagerIml");
     } catch (NullPointerException npe)
     {
       throw new ModuleInitializationException(
          npe.getMessage(),"fedora.server.storage.ConnectionPoolManagerIml");
     } catch (Exception e)
     {
       throw new ModuleInitializationException(
          e.getMessage(),"fedora.server.storage.ConnectionPoolManagerIml");
     }
   }
 
   /**
    * <p>Gets a named connection pool.</p>
    *
    * @param poolName name of the ConnectionPool
    * @return ConnectionPool the named ConnectionPool
    */
   public ConnectionPool getPool(String poolName)
       throws ModuleInitializationException
   {
     ConnectionPool connectionPool = null;
     try
     {
       if (h_ConnectionPools.containsKey(poolName))
       {
         // Pool exists
         connectionPool = (ConnectionPool)h_ConnectionPools.get(poolName);
       } else
       {
         // Error pool was never initialized or name could not be found
         String message = "CONNECTION POOL NOT INITIALIZED: "+poolName;
         throw new ModuleInitializationException(message,
             "fedora.server.storage.ConnectionPoolManagerImpl");
       }
     } catch (Exception e)
     {
       throw new ModuleInitializationException(
           e.getMessage(),"fedora.server.storage.ConnectionPoolManagerImpl");
     }
     return connectionPool;
   }
 }
