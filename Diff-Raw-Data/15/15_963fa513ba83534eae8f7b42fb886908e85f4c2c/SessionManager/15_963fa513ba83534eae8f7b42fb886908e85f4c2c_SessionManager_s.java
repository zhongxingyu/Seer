 package gov.usgs.cida.coastalhazards.jpa;
 
import gov.usgs.cida.coastalhazards.jpa.JPAHelper;
 import gov.usgs.cida.coastalhazards.model.Session;
 import gov.usgs.cida.coastalhazards.session.io.SessionIO;
 import gov.usgs.cida.coastalhazards.session.io.SessionIOException;
 import java.security.NoSuchAlgorithmException;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class SessionManager implements SessionIO {
     
     @PersistenceContext
     private EntityManager em;
     private TypedQuery<Session> query;
             
     public SessionManager() {
         em = JPAHelper.getEntityManagerFactory().createEntityManager();
         //query = em.createQuery("select s from Session s where s.id = ?", Session.class);
     }
 
     @Override
     public String load(String sessionID) throws SessionIOException {
         Session session = em.find(Session.class, sessionID);
        String jsonSession = session.toJSON();
         return jsonSession;
     }
 
     @Override
     public String save(String session) throws SessionIOException {
         String id = "ERR";
         try {
             em.getTransaction().begin();
             Session sessionObj = Session.fromJSON(session);
             em.persist(sessionObj);
             id = sessionObj.getId();
             em.getTransaction().commit();
         } catch (NoSuchAlgorithmException ex) {
             em.getTransaction().rollback();
         }
         return id;
     }
 
 }
