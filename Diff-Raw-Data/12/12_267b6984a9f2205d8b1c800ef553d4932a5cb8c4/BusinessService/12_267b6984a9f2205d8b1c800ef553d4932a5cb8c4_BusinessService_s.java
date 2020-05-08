 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package census.business;
 
 import census.CensusStarter;
 import java.util.ResourceBundle;
 import javax.persistence.EntityManager;
 
 /**
  * This is the main class for all classes proving business services.
  * 
  * This class does not define the API to access a business service. It just gives
  * some common ground to all business services.
  * 
  * @author Danylo Vashchilenko
  */
 public abstract class BusinessService {
 
     protected StorageService storageService;
     protected SessionsService sessionService;
     protected ResourceBundle bundle;
     protected EntityManager entityManager;
     
     /**
      * Creates an instance of this class.
      */
     protected BusinessService() {
         storageService = StorageService.getInstance();
         sessionService = SessionsService.getInstance();
         bundle = ResourceBundle.getBundle("census.business.resources.Strings");
         entityManager = storageService.getEntityManager();
     }
     
     /**
      * Makes sure that the transaction is active.
      * 
      * @throws IllegalStateException if the transaction is not active
      */
     protected void assertTransactionActive() {
         if (!storageService.isTransactionActive()) {
            throw new IllegalStateException("The transaction has to be active"); //NOI18N
         }
     }
 
     /**
      * Makes sure that there is an open session.
      * 
      * @throws IllegalStateException if the session is not active
      */
     protected void assertSessionActive() {
         if (!sessionService.hasOpenSession()) {
             throw new IllegalStateException("A session has to be open."); //NOI18N
         }
     }
 }
