 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package recipe.db;
 import java.sql.*;
 import java.io.File;
 
 /**
  *
  * @author Michael
  */
 public class SqliteDB {
     Connection con;
     
     public SqliteDB(String path) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC"); 
       String fullpath = (new File(path)).getAbsolutePath();
       path = "jdbc:sqlite:"+fullpath;
        System.out.println(fullpath);
        con = DriverManager.getConnection(fullpath);
     }
     
     protected ResultSet executeQuery(String query) throws SQLException {
         Statement s = con.createStatement();
         return s.executeQuery(query);
     }
     
     public void disconnect() throws SQLException {
         if (con!=null)
             con.close();    
     }
 }
