 package org.uiautomation;
 
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 public class Database {
 
   public static Connection getConnection() throws URISyntaxException, SQLException {
 
    String s = System.getenv("DATABASE_URL");
    if (s == null) {
      s = "postgres://postgres:password@localhost:5432/agonj";
    }
    URI dbUri = new URI(s);
 
     String username = dbUri.getUserInfo().split(":")[0];
     String password = dbUri.getUserInfo().split(":")[1];
     String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();
 
     return DriverManager.getConnection(dbUrl, username, password);
   }
 }
