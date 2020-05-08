 package au.edu.uq.cmm.paul.status;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 
 import org.apache.log4j.Logger;
 
 import au.edu.uq.cmm.aclslib.proxy.AclsFacilityEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsFacilityEventListener;
 import au.edu.uq.cmm.aclslib.proxy.AclsLoginEvent;
import au.edu.uq.cmm.aclslib.proxy.AclsLoginException;
 import au.edu.uq.cmm.aclslib.proxy.AclsLogoutEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsProxy;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.grabber.FileGrabber;
 
 /**
  * This class represents the session state of the facilities as 
  * captured by the ACLS proxy.
  * 
  * @author scrawley
  */
 public class FacilityStatusManager implements AclsFacilityEventListener {
     private static final Logger LOG = Logger.getLogger(FileGrabber.class);
     // FIXME - the facility statuses need to be persisted.
     private AclsProxy proxy;
     private EntityManagerFactory emf;
     private SessionDetailMapper aclsAccountMapper;
 
     public FacilityStatusManager(Paul services) {
         this.proxy = services.getProxy();
         this.proxy.addListener(this);
         this.emf = services.getEntityManagerFactory();
         this.aclsAccountMapper = services.getSessionDetailMapper();
         if (this.aclsAccountMapper == null) {
             this.aclsAccountMapper = new DefaultSessionDetailsMapper();
         }
     }
 
     public void eventOccurred(AclsFacilityEvent event) {
         EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             String facilityName = event.getFacilityName();
             Facility facility = getFacility(em, facilityName);
             if (facility == null) {
                 LOG.error("No facility found for facility id " + facilityName);
                 return;
             }
             String userName = aclsAccountMapper.mapToUserName(
                     event.getUserName(), event.getAccount());
             String accountName = aclsAccountMapper.mapToAccountName(
                     event.getUserName(), event.getAccount());
             String emailAddress = aclsAccountMapper.mapToEmailAddress(
                     event.getUserName(), event.getAccount());
             if (event instanceof AclsLoginEvent) {
                 FacilitySession details = new FacilitySession(
                         userName, accountName, facility, emailAddress, new Date());
                 facility.addSession(details);
             } else if (event instanceof AclsLogoutEvent) {
                 FacilitySession details = facility.getCurrentSession();
                 if (details == null) {
                     throw new InvalidSessionException(
                             "No current session for facility " + facility.getFacilityName());
                 } else if (!details.getUserName().equals(userName) ||
                         !details.getAccount().equals(accountName)) {
                     throw new InvalidSessionException(
                             "Inconsistent session user or account name for facility " + 
                             facility.getFacilityName());
                 }
                 details.setLogoutTime(new Date());
             }
             em.persist(facility);
             em.getTransaction().commit();
         } catch (InvalidSessionException ex) {
             LOG.error("Bad session information - ignoring login/logout event", ex);
         } finally {
             em.close();
         }
     }
 
     private Facility getFacility(EntityManager entityManager, String facilityName) {
         TypedQuery<Facility> query = entityManager.createQuery(
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
 
     public FacilitySession getLoginDetails(String facilityId, long timestamp) {
         EntityManager em = emf.createEntityManager();
         try {
             Facility facility = getFacility(em, facilityId);
             if (facility == null) {
                 LOG.error("No Facility record for facility " + facilityId);
                 return null;
             }
             return facility.getLoginDetails(timestamp);
         } finally {
             em.close();
         }
     }
     
     public void endSession(String sessionUuid) {
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
             em.getTransaction().commit();
         } catch (NoResultException ex) {
             LOG.debug("session doesn't exist", ex);
         } finally {
             em.close();
         }
     }
 
     public Collection<Facility> getSnapshot() {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<Facility> query = em.createQuery(
                     "from Facility", Facility.class);
             Collection<Facility> res = query.getResultList();
             LOG.debug("Snapshot contains " + res.size() + " entries");
             return res;
         } finally {
             em.close();
         }
     }
 
     public List<String> login(String facilityName, String userName, String password) 
     throws AclsLoginException {
         Facility facility = lookupIdleFacility(facilityName);
         return proxy.login(facility, userName, password);
     }
 
     public void selectAccount (String facilityName, String userName, String account) 
     throws AclsLoginException {
         Facility facility = lookupIdleFacility(facilityName);
         proxy.selectAccount(facility, userName, account);
     }
 
     private Facility lookupIdleFacility(String facilityName)
     throws AclsLoginException {
         Facility facility;
         EntityManager em = emf.createEntityManager();
         try {
             facility = getFacility(em, facilityName);
         } finally {
             em.close();
         }
         if (facility == null) {
             throw new AclsLoginException ("Unknown facility " + facilityName);
         }
         if (facility.isInUse()) {
             throw new AclsLoginException ("Facility " + facilityName + " is in use");
         }
         return facility;
     }
 }
