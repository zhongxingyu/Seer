 package iReport;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 
 public class MYSQL {
     
     private String host;
     private int port;
     private String user;
     private String password;
     private String database;
     
     private Connection conn;
	private static MYSQL sql;
     
     
    public MYSQL() throws Exception {
        File file = new File("plugins/iReport/", "database.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        
        String db = "database.";
        cfg.addDefault(db + "host", "localhost");
        cfg.addDefault(db + "port", 3306);
        cfg.addDefault(db + "user", "user");
        cfg.addDefault(db + "password", "password");
        cfg.addDefault(db + "database", "database");
        cfg.options().copyDefaults(true);
        try {
         cfg.save(file);
     } catch (IOException e) {
         e.printStackTrace();
     }
        
        this.host = cfg.getString(db + "host");
        this.port = cfg.getInt(db + "port");
        this.user = cfg.getString(db + "user");
        this.password = cfg.getString(db + "passsword");
        this.database = cfg.getString(db + "database");
        
        this.oppenConnection();
    }
    
   public static MYSQL getMYSQL() {
       if (sql == null) {
           try {
               sql = new MYSQL();
           } catch (Exception e) {
        	   System.err.println("fail to cornedt to MYSQL");
           }
       }
       return sql;
   }

    public Connection oppenConnection() throws Exception {
       Class.forName("com.mqsql.jdbc.Driver");
         Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.user, this.password);
         this.conn = conn;
         return conn;
    }
     
 
    
    
    public Connection getConnection() {
        return this.conn;
    }
    
    public boolean hasConnection() {
        try {
         return this.conn != null || this.conn.isValid(1);
     } catch (SQLException e) {
         e.printStackTrace();
         return false;
     }
    }
    
    public void queryUpdate(String query) {
        Connection conn = this.conn;
        PreparedStatement st = null;
       try {
         st = conn.prepareStatement(query);
         st.executeUpdate();
     } catch (SQLException e) {
         System.err.println("Failed to send update '" + query + "'.");
     } finally {
         this.closeRessources(null, st);
     }
    }
    
    public void closeRessources(ResultSet rs, PreparedStatement st) {
        if (rs != null) {
            try {
             rs.close();
         } catch (SQLException e) {
         }
        }
        if (st != null) {
            try {
             st.close();
         } catch (SQLException e) {
         }
        }
    }
 
 
 
 public void closeConnection() {
     try {
         this.conn.close();
     } catch (SQLException e) {
         e.printStackTrace();
     } finally {
        this.conn = null;
     
    }
   }
 }
