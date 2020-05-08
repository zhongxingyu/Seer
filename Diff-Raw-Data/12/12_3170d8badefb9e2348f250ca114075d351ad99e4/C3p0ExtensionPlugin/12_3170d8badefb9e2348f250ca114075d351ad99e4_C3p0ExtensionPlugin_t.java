 package me.jayfella.c3p0extension;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public final class C3p0ExtensionPlugin extends JavaPlugin
 {
     private final Map<DatabaseConnection, ComboPooledDataSource> dataSources = new HashMap<>();
     
     public C3p0ExtensionPlugin()
     {
         Properties p = new Properties(System.getProperties());
         p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
         p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");
         System.setProperties(p);
     }
     
     @Override public void onEnable() 
     { 
         
     }
     
     @Override
     public void onDisable()
     {
         // Close all connections for the sake of being tidy.
         
         Iterator it = dataSources.entrySet().iterator();
     
         while (it.hasNext()) 
         {
             Map.Entry pair = (Map.Entry)it.next();
             ComboPooledDataSource source = (ComboPooledDataSource)pair.getValue();
             
             source.close();
             it.remove();
         }
     }
     
     
     
     public boolean createDataSource(DatabaseConnection databaseConnection)
     {
         //ensure a datasource doesnt already exist for this database
         if (dataSources.get(databaseConnection) != null)
         {
             return true;
         }
         
         StringBuilder connectionString = new StringBuilder()
                 .append("jdbc:")
                .append(databaseConnection.getDatabaseType().getPrefix())
                 .append(databaseConnection.getAddress());
         
         if (databaseConnection.getDatabaseType().requiresPort())
         {
             connectionString    
                 .append(":")
                 .append(databaseConnection.getPort())
                 .append("/")
                .append(databaseConnection.getDatabaseName());
        }
        else
        {
            connectionString
                .append(databaseConnection.getDatabaseName());
         }
         
         ComboPooledDataSource comboPool;
 
         try
         {
             comboPool = new ComboPooledDataSource();
 
             comboPool.setAcquireIncrement(1);
             comboPool.setBreakAfterAcquireFailure(true);
 
             comboPool.setIdleConnectionTestPeriod(300);
 
             comboPool.setDriverClass(databaseConnection.getDatabaseType().getDriver());
             comboPool.setJdbcUrl(connectionString.toString());
             comboPool.setUser(databaseConnection.getUsername());
             comboPool.setPassword(databaseConnection.getPassword());
 
             comboPool.setInitialPoolSize(databaseConnection.getInitialPoolSize());
             comboPool.setMinPoolSize(databaseConnection.getMaxPoolSize());
             comboPool.setAcquireIncrement(databaseConnection.getacquireIncrement());
             comboPool.setMaxPoolSize(databaseConnection.getMaxPoolSize());
         }
         catch (Exception ex)
 		{
 			this.getLogger().log(Level.WARNING, ex.getMessage(), ex);
             return false;
 		}
         
         // check the connection to ensure its valid.
         if (!checkConnection(comboPool))
         {
             return false;
         }
         
         this.dataSources.put(databaseConnection, comboPool);
         
         return true;
     }
 
     public ComboPooledDataSource getDataSource(DatabaseConnection databaseConnection)
     {
         return dataSources.get(databaseConnection);
     }
     
     public Connection getConnection(DatabaseConnection databaseConnection) throws SQLException
     {
         return this.getDataSource(databaseConnection).getConnection();
     }
     
     private boolean checkConnection(ComboPooledDataSource comboPool)
     {
         this.getLogger().log(Level.INFO, "Attempting connection to database: {0}", comboPool.getJdbcUrl());
 
         Connection con = null;
         Statement st = null;
         ResultSet rs = null;
         
         try
         {
             con = comboPool.getConnection();
             st = con.createStatement();
             rs = st.executeQuery("SELECT VERSION()");
 
             if (rs.next())
             {
                 this.getLogger().info("Database connection successful.");
                 return true;
             }
             else
             {
                 this.getLogger().info("Database connection FAILED.");
                 return false;
             }
         }
         catch (SQLException ex)
         {
             this.getLogger().info("Database connection FAILED.");
             this.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
             return false;
         }
         finally
         {
             try
             {
                 if (rs != null) rs.close();
                 if (st != null) st.close();
                 if (con != null) con.close();
             }
             catch (SQLException ex)
             {
                 this.getLogger().log(Level.WARNING, ex.getMessage(), ex);
                 return false;
             }
         }
         
     }
  
 }
