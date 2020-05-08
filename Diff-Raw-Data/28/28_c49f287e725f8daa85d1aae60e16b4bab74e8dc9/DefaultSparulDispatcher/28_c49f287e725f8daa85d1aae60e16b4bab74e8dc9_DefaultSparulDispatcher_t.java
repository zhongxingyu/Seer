 package com.computas.sublima.query.impl;
 
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.computas.sublima.query.service.SettingsService;
 import com.hp.hpl.jena.db.IDBConnection;
 import com.hp.hpl.jena.db.ModelRDB;
 import com.hp.hpl.jena.update.*;
 import com.hp.hpl.jena.query.larq.LARQ;
 import com.hp.hpl.jena.query.larq.IndexBuilderString;
 import com.hp.hpl.jena.query.larq.IndexLARQ;
 import com.hp.hpl.jena.query.larq.ARQLuceneException;
 import org.apache.cocoon.configuration.Settings;
 import org.postgresql.util.PSQLException;
 
 import java.sql.SQLException;
 
 /**
  * This component queries RDF triple stores using Sparul. It is threadsafe.
  */
 public class DefaultSparulDispatcher implements SparulDispatcher {
 
   private Settings cocoonSettings;
 
   public boolean query(String query) {
 
     //Get a GraphStore and load the graph from the Model
     GraphStore graphStore = GraphStoreFactory.create();
     graphStore.setDefaultGraph(SettingsService.getModel().getGraph());
 
     try {
       //Try to execute the updateQuery (SPARQL/Update)
       UpdateRequest updateRequest = UpdateFactory.create(query);
       
       updateRequest.exec(graphStore);
    
      LARQ.setDefaultIndex(SettingsService.getIndexBuilderString(null).getIndex());
     }
     catch (UpdateException e) {
       //model.close();
       e.printStackTrace();
       return false;
     }
     catch (ARQLuceneException e) {
       e.printStackTrace();
       return false;
     }
     catch (Exception e) {
       e.printStackTrace();
       return false;
     }
     // Return true if update success
     return true;
   }
 
   public void setCocoonSettings(Settings cocoonSettings) {
     this.cocoonSettings = cocoonSettings;
   }
 
 }
