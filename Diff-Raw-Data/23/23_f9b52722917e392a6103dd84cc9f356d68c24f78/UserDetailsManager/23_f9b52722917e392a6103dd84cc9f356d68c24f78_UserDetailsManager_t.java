 package au.edu.uq.cmm.eccles;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.message.Certification;
 
 
 public class UserDetailsManager {
     private static final Logger LOG = LoggerFactory.getLogger(UserDetailsManager.class);
     
     private Random random = new Random();
     private EntityManagerFactory emf;
     private Certification defaultCertification = Certification.VALID;
     
 
     public UserDetailsManager(EntityManagerFactory emf) {
         this.emf = emf;
     }
     
     public UserDetails lookupUser(String userName, boolean fetchCollections) 
             throws UnknownUserException {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u where u.userName = :userName", 
                     UserDetails.class);
             query.setParameter("userName", userName);
             UserDetails userDetails = query.getSingleResult();
             if (fetchCollections) {
                 userDetails.getAccounts().size();
                 userDetails.getCertifications().size();
             }
             return userDetails;
         } catch (NoResultException ex) {
             throw new UnknownUserException(userName);
         } finally {
             em.close();
         }
     }
     
     public List<String> getUserNames() {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<String> query = em.createQuery(
                     "select u.userName from UserDetails u", String.class);
             return query.getResultList();
         } finally {
             em.close();
         }
     }
 
     public List<UserDetails> getUsers() {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u", UserDetails.class);
             return query.getResultList();
         } finally {
             em.close();
         }
     }
 
    public void refreshUserDetails(EntityManager em, String userName, String email, 
             AclsLoginDetails loginDetails) {
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u where u.userName = :userName", 
                     UserDetails.class);
             query.setParameter("userName", userName);
             UserDetails userDetails = query.getSingleResult();
             LOG.debug("Refreshing cached user details for " + userName);
             String digest = createDigest(loginDetails.getPassword(), userDetails.getSeed());
             userDetails.setAccounts(new HashSet<String>(loginDetails.getAccounts()));
             userDetails.setDigest(digest);
             userDetails.setOrgName(loginDetails.getOrgName());
             userDetails.setHumanReadableName(loginDetails.getHumanReadableName());
             userDetails.setOnsiteAssist(loginDetails.isOnsiteAssist());
             userDetails.getCertifications().put(
                     loginDetails.getFacilityName(), 
                     loginDetails.getCertification().toString());
         } catch (NoResultException ex) {
             LOG.debug("Caching user details for " + userName);
             long seed = random.nextLong();
             String digest = createDigest(loginDetails.getPassword(), seed);
             UserDetails newDetails = new UserDetails(
                     userName, email, loginDetails, seed, digest);
             em.persist(newDetails);
         }
     }
     
     public AclsLoginDetails authenticateAgainstCachedCredentials(
             String userName, String password, FacilityConfig facility) {
         LOG.debug("Trying to authenticate using cached user details for " + userName);
         try {
             UserDetails userDetails = lookupUser(userName, true);
             String myDigest = createDigest(password, userDetails.getSeed());
             String savedDigest = userDetails.getDigest();
             LOG.debug("Comparing digests - " + savedDigest + " vs " + myDigest);
             if (myDigest.equals(savedDigest)) {
                 Certification cert = Certification.parse(
                         userDetails.getCertifications().get(facility.getFacilityName()));
                 if (cert == null) {
                     cert = defaultCertification;
                 }
                 return new AclsLoginDetails(userName, userDetails.getHumanReadableName(),
                         userDetails.getOrgName(), password,  facility.getFacilityName(), 
                         new ArrayList<String>(userDetails.getAccounts()),
                         cert, userDetails.isOnsiteAssist(), true);
             }
             return null;
         } catch (UnknownUserException ex) {
             return null;
         }
     }
 
     private String createDigest(String password, long seed) {
         LOG.debug("Creating digest for password using seed " + seed);
         try {
             MessageDigest digester = MessageDigest.getInstance("MD5");
             for (int i = 0; i < 8; i++) {
                 byte b = (byte) ((seed >> (i * 8)) & 0xff);
                 digester.update(b);
             }
             digester.update(password.getBytes("UTF-8"));
             String res = toString(digester.digest());
             LOG.debug("Created digest - " + res);
             return res;
         } catch (NoSuchAlgorithmException ex) {
             throw new AssertionError("Don't understand MD5?", ex);
         } catch (UnsupportedEncodingException ex) {
             throw new AssertionError("Don't understand UTF-8?", ex);
         }
     }
     
     private String toString(byte[] bytes) {
         StringBuilder sb = new StringBuilder(bytes.length * 2 + 6);
         sb.append(bytes.length).append(":");
         for (byte b : bytes) {
             sb.append("0123456789ABCDEF".charAt((b >> 4) & 0xf));
             sb.append("0123456789ABCDEF".charAt(b & 0xf));
         }
         return sb.toString();
     }
 }
