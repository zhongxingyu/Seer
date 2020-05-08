 package com.computas.sublima.app.adhoc;
 
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.CachingService;
 import com.computas.sublima.query.service.SettingsService;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import net.spy.memcached.MemcachedClient;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayOutputStream;
 
 public class ImportData {
 
   private static Logger logger = Logger.getLogger(ImportData.class);
   private DefaultSparulDispatcher sparul = new DefaultSparulDispatcher();
 
   public ImportData() {
   }
 
   /**
    * Ad hoc method to import data to the Pg Data Store.
    *
    * @param url  The URL of the RDF to import
    * @param lang The serialisation language of the file
    */
   public void load(java.lang.String url, java.lang.String lang) {
 
     try {
       Model model = ModelFactory.createDefaultModel();
       model.read(url, lang);
 
       ByteArrayOutputStream out = new ByteArrayOutputStream();
      model.write(out, lang);
      System.out.println(out.toString());
 
       StringBuilder insert = new StringBuilder();
 
       //insert.append("CLEAR <http://msone.computas.no/graphs/ontology/mediasone>\n");
       insert.append("INSERT INTO <"+ SettingsService.getProperty("sublima.basegraph") +"> {\n");
       insert.append(out.toString());
       insert.append("\n}");
 
       sparul.query(insert.toString());
 
       out.close();
       model.close();
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   private void modelChanged() {
     logger.debug("ImportData invalidates the cache.");
     CachingService cache = new CachingService();
     MemcachedClient memcached = cache.connect();
     cache.modelChanged(memcached);
     cache.close(memcached);
   }
 
   public static void main(String[] args) {
     ImportData id = new ImportData();
     id.load(args[0], args[1]);
 
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\information-model.n3", "N3");
     //System.out.println("Done loading information-model.n3");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\sublima-ns.ttl", "Turtle");
     //System.out.println("Done loading sublima-ns.ttl");
     //load("file:\\Prosjekter\\SUBLIMA\\Kode\\Sublima\\blocks\\sublima-app\\src\\main\\resources\\rdf-data\\detektor-test-data.n3", "N3");
     //System.out.println("Done loading detektor-test-data.n3");
     //load("http://www.lingvoj.org/lingvoj.rdf", "RDF/XML");
   }
 }
