 package gov.usgs.cida.coastalhazards.jpa;
 
 import gov.usgs.cida.coastalhazards.model.Thumbnail;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import org.apache.commons.codec.binary.Base64;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ThumbnailManager {
 
     public InputStream load(String itemId) {
         EntityManager em = JPAHelper.getEntityManagerFactory().createEntityManager();
         InputStream inputStream = null;
         try {
             Thumbnail thumb = em.find(Thumbnail.class, itemId);
            inputStream = new ByteArrayInputStream(Base64.decodeBase64(thumb.getImage()));
         } finally {
             JPAHelper.close(em);
         }
         return inputStream;
     }
     
     public synchronized String save(Thumbnail thumbnail) {
         EntityManager em = JPAHelper.getEntityManagerFactory().createEntityManager();
         EntityTransaction transaction = em.getTransaction();
         String result = "{\"success\": false}";
 		try {
 			transaction.begin();
 			em.merge(thumbnail);
 			transaction.commit();
             result = "{\"success\": true}";
 		} catch (Exception ex) {
             if (transaction.isActive()) {
                 transaction.rollback();
             }
 		} finally {
             JPAHelper.close(em);
         }
 		return result;
     }
     
 }
