 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ehis;
 
 /**
  *
  * @author SahitiRaviChand
  */
 
 import java.sql.*;
 import javax.swing.*;
 public class Javaconnect {
     
     Connection conn = null;
     
     public static Connection ConnectorDb(){
         try{
             Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir")+ "\\Ehis.sqlite");
             return conn;
             
         }
         catch(Exception e){
             JOptionPane.showMessageDialog(null, e);
             return null;
         }
     }
 }
