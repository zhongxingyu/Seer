 package org.sortdc.sortdc.database;
 
 import org.sortdc.sortdc.database.interfaces.DatabaseInterface;
 
 public abstract class Database implements DatabaseInterface {
 
     protected String host;
     protected int port;
     protected String db_name;
     protected String username;
     protected String password;
 
     /**
      * Sets host name
      *
      * @param host
      */
     public void setHost(String host) {
        if(host.equals("localhost")){
            host = "127.0.0.1";
        }
         this.host = host;
     }
 
     /**
      * Sets port number
      *
      * @param port
      */
     public void setPort(int port) {
         this.port = port;
     }
 
     /**
      * Sets database name
      *
      * @param db_name
      */
     public void setDbName(String db_name) {
         this.db_name = db_name;
     }
 
     /**
      * Sets username for authentication
      *
      * @param username
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * Sets password for authentication
      *
      * @param password
      */
     public void setPassword(String password) {
         this.password = password;
     }
 }
