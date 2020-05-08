 package org.triple_brain.graphmanipulator.jena;
 
 import com.hp.hpl.jena.db.DBConnection;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.ModelMaker;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.sql.DataSource;
 import java.sql.SQLException;
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class JenaConnection {
 
     @Inject
     @Named("jenaDB")
     private static DataSource jenaDB;
 
     @Inject
     @Named("jenaDatabaseTypeName")
     private static String databaseTypeName;
 
     private static ModelMaker modelMaker;
     private static DBConnection dbConnection;
 
     public static ModelMaker modelMaker(){
        if(modelMaker == null){
             modelMaker = ModelFactory.createModelRDBMaker(connection());
         }
         return modelMaker;
     }
 
     public static void closeConnection() throws SQLException {
         dbConnection.close();
         modelMaker = null;
     }
 
     private static DBConnection connection(){
         try{
             dbConnection = new DBConnection(
             jenaDB.getConnection(),
             databaseTypeName
             );
             return dbConnection;
         }
         catch(Exception e){
                 e.printStackTrace();
             }
         return null;
     }
 }
