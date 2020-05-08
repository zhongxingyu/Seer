 package au.edu.uq.cmm.eccles;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
 import au.edu.uq.cmm.aclslib.authenticator.Authenticator;
 import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.config.FacilityMapper;
 import au.edu.uq.cmm.aclslib.message.AclsException;
 import au.edu.uq.cmm.aclslib.proxy.AclsFacilityEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsFacilityEventListener;
 import au.edu.uq.cmm.aclslib.proxy.AclsLoginEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsLogoutEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsPasswordAcceptedEvent;
 import au.edu.uq.cmm.aclslib.proxy.AclsProxy;
 
 public class Eccles implements AclsFacilityEventListener, Authenticator {
 
     private static final Logger LOG = LoggerFactory.getLogger(Eccles.class);
     private AclsProxy proxy;
     private EntityManagerFactory emf;
     private SessionDetailMapper userDetailsMapper;
     private UserDetailsManager userDetailsManager;
     
     /**
      * @param args
      * @throws InterruptedException 
      */
     public static void main(String[] args) throws InterruptedException {
         Properties properties = null;
         if (args.length > 0) {
             properties = new Properties();
             try {
                 properties.load(new FileInputStream(args[0]));
             } catch (IOException ex) {
                 System.err.println(ex.getMessage());
                 System.exit(1);
             }
         }
         LOG.info("Hallo ... I'm the famous Eccles!");
         try {
             new Eccles().run(properties);
         } catch (Throwable ex) {
             LOG.error("Some rotten swine has deaded me!", ex);
         }
         // (Wrong character, but who cares :-) )
         LOG.info("Exits stage left on council dust cart. Pieew!!");
     }
 
     private void run(Properties properties) throws InterruptedException {
         emf = properties == null ?
                 Persistence.createEntityManagerFactory("au.edu.uq.cmm.paul") :
                 Persistence.createEntityManagerFactory("au.edu.uq.cmm.paul", properties);
         ACLSProxyConfiguration config = EcclesProxyConfiguration.load(emf);
         FacilityMapper mapper = new EcclesFacilityMapper(config, emf);
         userDetailsMapper = new DefaultSessionDetailsMapper();
         userDetailsManager = new UserDetailsManager(emf);
         proxy = new AclsProxy(config, 0, mapper, this);
         proxy.addListener(this);
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             @Override public void run() {
                 LOG.info("Ok.  I'll be off then");
                 proxy.startShutdown();
                 try {
                     proxy.awaitShutdown();
                 } catch (InterruptedException ex) {
                     LOG.error("Shutdown interrupted ...", ex);
                 }
                 LOG.info("Bye");
             }
         }));
         proxy.startup();
         
         Object lock = new Object();
         synchronized (lock) {
             try {
                 lock.wait();
             } catch (InterruptedException ex) {
                 LOG.error("Interrupted ...", ex);
             }
         }
     }
 
     public void eventOccurred(AclsFacilityEvent event) {
         LOG.debug("Processing event " + event);
         EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             String facilityName = event.getFacilityName();
             if (event instanceof AclsLoginEvent) {
                processLoginEvent((AclsLoginEvent) event, em, facilityName);
             } else if (event instanceof AclsLogoutEvent) {
                processLogoutEvent((AclsLogoutEvent) event, em, facilityName);
             } else {
                 processPasswordAcceptedEvent((AclsPasswordAcceptedEvent) event, em, facilityName);
             }
             em.getTransaction().commit();
         } catch (InvalidSessionException ex) {
             LOG.error("Bad session information - ignoring facility event", ex);
         } finally {
             em.close();
         }
         LOG.debug("Finished processing event " + event);
     }
 
     @Override
     public AclsLoginDetails authenticate(
             String userName, String password, FacilityConfig facility)
             throws AclsException {
         return userDetailsManager.authenticateAgainstCachedCredentials(userName, password, facility);
     }
 
     private void processPasswordAcceptedEvent(AclsPasswordAcceptedEvent event,
             EntityManager em, String facilityName) throws InvalidSessionException {
         String userName = userDetailsMapper.mapToUserName(event.getUserName());
         String email = userDetailsMapper.mapToEmailAddress(event.getUserName());
         if (!event.getLoginDetails().isCached()) {
             userDetailsManager.refreshUserDetails(em, userName, email, event.getLoginDetails());
         }
     }
 
    private void processLogoutEvent(AclsLogoutEvent event, EntityManager em,
             String facilityName) throws InvalidSessionException {
         FacilitySession session;
         String userName = userDetailsMapper.mapToUserName(event.getUserName());
         String account = userDetailsMapper.mapToAccount(event.getAccount());
         TypedQuery<FacilitySession> query = em.createQuery(
                 "from FacilitySession f where f.facilityName = :facilityName " +
                         "order by f.loginTime desc", 
                 FacilitySession.class);
         query.setParameter("facilityName", facilityName);
         query.setMaxResults(1);
         List<FacilitySession> sessions = query.getResultList();
         if (sessions.isEmpty()) {
             throw new InvalidSessionException(
                     "No sessions for facility " + facilityName);
         }
         session = sessions.get(0);
         if (session.getLogoutTime() != null) {
             throw new InvalidSessionException(
                     "No current session for facility " + facilityName);
         } else if (!session.getUserName().equals(userName) ||
                 !session.getAccount().equals(account)) {
             throw new InvalidSessionException(
                     "Inconsistent session user or account name for facility " + 
                             facilityName);
         }
         session.setLogoutTime(new Date());
     }
 
    private void processLoginEvent(AclsLoginEvent event, EntityManager em,
             String facilityName) throws InvalidSessionException {
         FacilitySession session;
         String userName = userDetailsMapper.mapToUserName(event.getUserName());
         String account = userDetailsMapper.mapToAccount(event.getAccount());
         String email = userDetailsMapper.mapToEmailAddress(event.getUserName());
         session = new FacilitySession(
                 userName, account, facilityName, email, new Date());
         em.persist(session);
     }
 }
