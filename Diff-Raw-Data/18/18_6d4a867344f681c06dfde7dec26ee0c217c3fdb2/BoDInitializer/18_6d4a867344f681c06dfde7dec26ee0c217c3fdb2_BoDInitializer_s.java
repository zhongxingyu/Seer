 package nl.surfnet.bod.util;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.hibernate.search.jpa.FullTextEntityManager;
 import org.hibernate.search.jpa.Search;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Initializes the BoD application, will be executed by spring after loading and
  * decrypting of properties
  */
 public class BoDInitializer {
   private final static Logger logger = LoggerFactory.getLogger(BoDInitializer.class);
 
   @PersistenceContext
   EntityManager entityManager;
 
   public void init() {
     logger.info("Initializing BoD");
     indexDatabaseContent();
    //IBM humor...
     logger.info("Ready for eBusiness");
   }
 
   public void indexDatabaseContent() {
     FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
     try {
      fullTextEntityManager.createIndexer().startAndWait();
     }
     catch (InterruptedException e) {
       logger.error("Error indexing current database content", e);
     }
 
     logger.info("Succesfully indexed database content for full text search");
   }
 
 }
