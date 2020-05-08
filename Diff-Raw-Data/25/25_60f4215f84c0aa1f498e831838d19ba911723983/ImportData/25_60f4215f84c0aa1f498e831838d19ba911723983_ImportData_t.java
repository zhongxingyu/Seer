 package com.computas.sublima.app.adhoc;
 
 import com.computas.sublima.query.service.DatabaseService;
 import com.computas.sublima.query.service.CachingService;
 import com.hp.hpl.jena.db.IDBConnection;
 import com.hp.hpl.jena.db.ModelRDB;
 import com.hp.hpl.jena.shared.AlreadyExistsException;
 
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 import net.spy.memcached.MemcachedClient;
 
 public class ImportData {
 
   private static Logger logger = Logger.getLogger(ImportData.class);
 
   public ImportData() {
   }
   
    /**
    * Ad hoc method to import data to the Pg Data Store.
    *
    * @param url  The URL of the RDF to import
    * @param lang The serialisation language of the file
    */
   public static void load(java.lang.String url, java.lang.String lang) {
 
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
 
     try {
       ModelRDB model = ModelRDB.createModel(connection);
       model.read(url, lang);
       modelChanged();
       logger.debug("ImportData.load() --> Created new model with data");
     }
     catch (AlreadyExistsException e) {
       ModelRDB model = ModelRDB.open(connection);
       model.read(url, lang);
       modelChanged();
       logger.debug("ImportData.load() --> Updated existing model with new data");
     }
     finally {
       try {
         connection.close();
 
       } catch (SQLException e) {
         logger.error("ImportData.load() --> SQL problem detected: " + e.getMessage());
         e.printStackTrace();
       }
     }
   }
 
   private static void modelChanged() {
       logger.debug("ImportData invalidates the cache.");
       CachingService cache = new CachingService();
       MemcachedClient memcached = cache.connect();
       cache.modelChanged(memcached);
      cache.close(memcached);
   }
 
   public static void main(String[] args) {
     load(args[0], args[1]);
 
 
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\information-model.n3", "N3");
     //System.out.println("Done loading information-model.n3");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\sublima-ns.ttl", "Turtle");
     //System.out.println("Done loading sublima-ns.ttl");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\detektor-test-data.n3", "N3");
     //System.out.println("Done loading detektor-test-data.n3");
     //load("http://www.lingvoj.org/lingvoj.rdf", "RDF/XML");
   }
 }
