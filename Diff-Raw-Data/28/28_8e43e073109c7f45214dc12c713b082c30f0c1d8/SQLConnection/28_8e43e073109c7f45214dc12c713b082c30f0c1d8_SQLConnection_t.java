 package org.triple_brain.module.repository_sql;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class SQLConnection {
 
     static Connection connection;
 
     @Inject
     @Named("nonRdfDb")
     private static DataSource dataSource;
 
     public static PreparedStatement preparedStatement(String query){
         try{
             return staleConnectionProofGetter().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
         }catch (SQLException ex){
             throw new SQLConnectionException(ex);
         }
     }
 
     public static void closeConnection() throws SQLException{
         staleConnectionProofGetter().close();
     }
 
     public static void clearDatabases(){
         try{
             String query = "DROP TABLE IF EXISTS member;";
             preparedStatement(query).executeUpdate();
         }catch(SQLException e){
             throw new RuntimeException(e);
         }
     }
 
     public static void createTables(){
         String query = "CREATE TABLE member (\n" +
                "    id           BIGINT    PRIMARY KEY AUTO_INCREMENT,\n" +
                 "    creationTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                 "    updateTime   TIMESTAMP NOT NULL,\n" +
                 "\n" +
                 "    uuid   VARCHAR(36)   UNIQUE NOT NULL,\n" +
                 "    username  VARCHAR(50)   UNIQUE NOT NULL,\n" +
                 "    email  VARCHAR(255)   UNIQUE NOT NULL,\n" +
                 "    locales VARCHAR(255)  NOT NULL,\n" +
                 "\n" +
                 "    salt                 VARCHAR(36),\n" +
                 "    passwordHash         VARCHAR(100)\n" +
                 ");";
         try{
             preparedStatement(query).executeUpdate();
         }catch(SQLException e){
             throw new RuntimeException(e);
         }
     }
 
 
     private static Connection staleConnectionProofGetter(){
         try{
             if(connection == null || connection.isClosed()){
                 connection = createConnection();
             }
         }catch(SQLException ex){
             connection = createConnection();
             ex.printStackTrace();
         }
         return connection;
     }
     
     private static Connection createConnection(){
         try{
             return dataSource.getConnection();
         }catch(SQLException ex){
             throw new SQLConnectionException(ex);
         }
     }
 }
