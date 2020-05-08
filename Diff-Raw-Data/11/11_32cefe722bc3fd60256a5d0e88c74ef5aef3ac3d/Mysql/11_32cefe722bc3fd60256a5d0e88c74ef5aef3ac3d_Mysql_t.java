 package net.battlenexus.bukkit.economy.sql.drivers;
 
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
 
 import net.battlenexus.bukkit.economy.sql.SqlClass;
 
 public class Mysql extends SqlClass {
 
     @Override
     public boolean connect(String host, String port, String database, String username, String password) {
         try {
             DriverManager.registerDriver(new com.mysql.jdbc.Driver());
             conn = DriverManager.getConnection("jdbc:mysql://" + host + ":"
                     + port + "/" + database, username, password);
             dbhost = host;
             dbport = port;
             dbname = database;
             dbuser = username;
             dbpass = password;
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
     
     public boolean reconnect() {
         return connect(dbhost, dbport, dbname, dbuser, dbpass);
     }
 
     @Override
     public void disconnect() {
         try {
             conn.close();
         } catch (SQLException e) {
             //Lets just assume it's already disconnected 
         }
     }
 
     @Override
     public ResultSet executeRawPreparedQuery(String sql, String[] parameters) {
         PreparedStatement preparedStatement;
         ResultSet results = null;
         int r = 0;
         do{ 
             try {
                 preparedStatement = conn.prepareStatement(sql);
                 int i = 1;
                 for (String parameter : parameters) {
                     preparedStatement.setString(i, parameter);
                     i++;
                 }
                 results = preparedStatement.executeQuery();
                 break;
             } catch (CommunicationsException e) {
                 if(r < retries){
                     reconnect();
                 }else{
                     System.out.print("ERROR");
                     break;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                break;
             }
         }while (true);
         
         return results;
     }
 
     @Override
     public int executeRawPreparedUpdate(String sql, String[] parameters) {
         PreparedStatement preparedStatement;
         int results = 0;
         int r = 0;
         do{
             try {
                 preparedStatement = conn.prepareStatement(sql);
                 int i = 1;
                 for (String parameter : parameters) {
                     preparedStatement.setString(i, parameter);
                     i++;
                 }
                 results = preparedStatement.executeUpdate();
                 break;
             } catch (CommunicationsException e) {
                 if(r < retries){
                     reconnect();
                 }else{
                     System.out.print("ERROR");
                     break;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                break;
             }
         }while(true);
         return results;
     }
 
     @Override
     public ResultSet executeRawQuery(String sql) {
         PreparedStatement preparedStatement;
         ResultSet results = null;
         int r = 0;
         do{
             try {
                 preparedStatement = conn.prepareStatement(sql);
                 results = preparedStatement.executeQuery();
                 break;
             } catch (CommunicationsException e) {
                 if(r < retries){
                     reconnect();
                 }else{
                     System.out.print("ERROR");
                     break;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                break;
             }
         }while(true);
         return results;
     }
 
     @Override
     public int executeRawUpdate(String sql) {
         PreparedStatement preparedStatement;
         int results = 0;
         int r = 0;
         do {
             try {
                 preparedStatement = conn.prepareStatement(sql);
                 results = preparedStatement.executeUpdate();
                break;
             } catch (CommunicationsException e) {
                 if(r < retries){
                     reconnect();
                 }else{
                     System.out.print("ERROR");
                     break;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                break;
             }
             
         } while(true);
         return results;
     }
 }
