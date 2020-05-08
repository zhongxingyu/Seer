 package au.edu.uq.cmm.paul.status;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.aclslib.proxy.AclsAuthenticationException;
 import au.edu.uq.cmm.aclslib.proxy.AclsHelper;
 import au.edu.uq.cmm.aclslib.proxy.AclsInUseException;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.grabber.FileGrabber;
 
 /**
  * This class represents the session state of the facilities as 
  * captured by the ACLS proxy.
  * 
  * @author scrawley
  */
 public class FacilityStatusManager {
     private static final Logger LOG = LoggerFactory.getLogger(FileGrabber.class);
     // FIXME - the facility statuses need to be persisted.
     private EntityManagerFactory emf;
     private AclsHelper aclsHelper;
 
     public FacilityStatusManager(Paul services) {
         this.emf = services.getEntityManagerFactory();
         this.aclsHelper = services.getAclsHelper();
     }
 
     private Facility getFacility(EntityManager em, String facilityName) {
         TypedQuery<Facility> query = em.createQuery(
                 "from Facility f where f.facilityName = :facilityName", Facility.class);
         query.setParameter("facilityName", facilityName);
         List<Facility> res = query.getResultList();
         if (res.size() == 0) {
             return null;
         } else if (res.size() == 1) {
             return res.get(0);
         } else {
             throw new PaulException("Duplicate facility entries");
         }
     }
 
     public List<FacilitySession> sessionsForFacility(String facilityName) {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<FacilitySession> query = em.createQuery(
                     "from FacilitySession s where s.facilityName = :facilityName " +
                     "order by s.loginTime desc", FacilitySession.class);
             query.setParameter("facilityName", facilityName);
             return query.getResultList();
         } finally {
             em.close();
         }
     }
 
     public List<FacilitySession> getLatestSessions() {
         EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<String> query0 = em.createQuery(
                     "select f.facilityName from Facility f order by f.facilityName", String.class);
             List<String> facilityNames = query0.getResultList();
             List<FacilitySession> sessions = new ArrayList<FacilitySession>(facilityNames.size());
             for (String facilityName : facilityNames) {
                 FacilitySession session = latestSession(em, facilityName);
                 if (session == null) {
                     session = new FacilitySession(facilityName);
                 }
                 sessions.add(session);
             }
             em.getTransaction().rollback();
             return sessions;
         } finally {
             em.close();
         }
     }
 
     public FacilitySession getLoginDetails(String facilityName, long timestamp) {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<FacilitySession> query = em.createQuery(
                     "from FacilitySession s where s.facilityName = :facilityName " +
                     "and s.loginTime <= :timestamp " +
                    "and (s.logoutTime is null or s.logoutTime >= :timestamp) " +
                     "order by s.loginTime desc", FacilitySession.class);
             query.setParameter("facilityName", facilityName);
             query.setParameter("timestamp", new Date(timestamp));
             query.setMaxResults(1);
             List<FacilitySession> list = query.getResultList();
             if (list.size() == 0) {
                 return null;
             } else {
                 return list.get(0);
             }
         } finally {
             em.close();
         }
     }
     
     public void logoutSession(String sessionUuid) throws AclsAuthenticationException {
         EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<FacilitySession> query = em.createQuery(
                     "from FacilitySession s where s.sessionUuid = :uuid",
                     FacilitySession.class);
             query.setParameter("uuid", sessionUuid);
             FacilitySession session = query.getSingleResult();
             if (session.getLogoutTime() == null) {
                 session.setLogoutTime(new Date());
             }
             aclsHelper.logout(getFacility(em, session.getFacilityName()), session.getUserName(), 
                     session.getAccount());
             em.getTransaction().commit();
         } catch (NoResultException ex) {
             LOG.debug("session doesn't exist", ex);
         } finally {
             em.close();
         }
     }
     
     public void logoutFacility(String facilityName) throws AclsAuthenticationException {
         EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             FacilitySession session = latestSession(em, facilityName);
             if (session != null) {
                 aclsHelper.logout(getFacility(em, facilityName), 
                         session.getUserName(), session.getAccount());
                 em.getTransaction().commit();
             } else {
                 em.getTransaction().rollback();
             }
         } finally {
             em.close();
         }
     }
     
     private FacilitySession latestSession(EntityManager em, String facilityName) {
         TypedQuery<FacilitySession> query = em.createQuery(
                 "from FacilitySession s where s.facilityName = :facilityName " +
                 "order by s.loginTime desc", FacilitySession.class);
         query.setParameter("facilityName", facilityName);
         query.setMaxResults(1);
         List<FacilitySession> results = query.getResultList();
         return (results.isEmpty()) ? null : results.get(0);
     }
 
     public List<String> login(String facilityName, String userName, String password) 
     throws AclsAuthenticationException, AclsInUseException {
         Facility facility = lookupIdleFacility(facilityName);
         return aclsHelper.login(facility, userName, password);
     }
 
     public void selectAccount (String facilityName, String userName, String account) 
     throws AclsAuthenticationException, AclsInUseException {
         Facility facility = lookupIdleFacility(facilityName);
         aclsHelper.selectAccount(facility, userName, account);
     }
 
     private Facility lookupIdleFacility(String facilityName)
             throws AclsAuthenticationException, AclsInUseException {
         Facility facility;
         EntityManager em = emf.createEntityManager();
         try {
             FacilitySession session = latestSession(em, facilityName);
             if (session != null && session.getLogoutTime() == null) {
                 throw new AclsInUseException("Facility " + facilityName + " is in use");
             }
             facility = getFacility(em, facilityName);
             if (facility == null) {
                 throw new AclsAuthenticationException("Unknown facility " + facilityName);
             }
             return facility;
         } finally {
             em.close();
         }
     }
 }
