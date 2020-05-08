 /*
 * Copyright 2012-2013, CMM, University of Queensland.
 *
 * This file is part of Eccles.
 *
 * Eccles is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eccles is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Eccles. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.eccles;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Objects;
 import java.util.Random;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.message.Certification;
 
 
 public class EcclesUserDetailsManager implements UserDetailsManager {
     private static final Logger LOG = 
     		LoggerFactory.getLogger(EcclesUserDetailsManager.class);
     
     private Random random = new Random();
     private EntityManagerFactory emf;
     private EcclesFallbackMode fallbackMode;
     
 
     public EcclesUserDetailsManager(EntityManagerFactory emf, 
     		EcclesFallbackMode fallbackMode) {
         this.emf = Objects.requireNonNull(emf);
         this.fallbackMode = Objects.requireNonNull(fallbackMode);
     }
     
     @Override
     public UserDetails lookupUser(String userName, boolean fetchCollections) 
             throws UserDetailsException {
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
             throw new UserDetailsException("User '" + userName + "' not found");
         } finally {
             emClose(em);
         }
     }
     
     @Override
     public List<String> getUserNames() {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<String> query = em.createQuery(
                     "select u.userName from UserDetails u", String.class);
             return query.getResultList();
         } finally {
             emClose(em);
         }
     }
 
     @Override
     public List<UserDetails> getUsers() {
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u", UserDetails.class);
             return query.getResultList();
         } finally {
             emClose(em);
         }
     }
 
 	@Override
     public void addUser(UserDetails user) throws UserDetailsException {
 		EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
             em.persist(user);
             em.getTransaction().commit();
         } catch (PersistenceException ex) {
         	throw new UserDetailsException(
         			"User '" + user.getUserName() + "' already exists");
         } finally {
             emClose(em);
         } 
 	}
 
 	@Override
     public void removeUser(String userName) throws UserDetailsException {
 		EntityManager em = emf.createEntityManager();
         try {
             em.getTransaction().begin();
            Query query = em.createQuery(
                    "delete from UserDetails u where u.userName = :userName");
             query.setParameter("userName", userName);
            int deleted = query.executeUpdate();
            if (deleted == 0) {
            	throw new UserDetailsException("User '" + userName + "' not found");
            }
             em.getTransaction().commit();
         } finally {
             emClose(em);
         }
 	}
 
     @Override
     public void refreshUserDetails(EntityManager em, String userName, String email, 
             AclsLoginDetails loginDetails) {
         try {
             TypedQuery<UserDetails> query = em.createQuery(
                     "from UserDetails u where u.userName = :userName", 
                     UserDetails.class);
             query.setParameter("userName", userName);
             UserDetails userDetails = query.getSingleResult();
             LOG.debug("Refreshing cached user details for " + userName);
             String digest = createDigest(loginDetails.getPassword().toLowerCase(), userDetails.getSeed());
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
             String digest = createDigest(loginDetails.getPassword().toLowerCase(), seed);
             UserDetails newDetails = new UserDetails(
                     userName, email, loginDetails, seed, digest);
             em.persist(newDetails);
         }
     }
     
     @Override
     /**
      * Perform fallback authentication against cached user details.
      * The actual behavior depends on the current 'fallbackMode'
      * setting, as described by the type.  If non-null, the resulting 
      * AclsLoginDetails object will give the user's cached certification
      * for the Facility if available, defaulting to VALID if there
      * is no cached certification information for the Facility.
      */
     public AclsLoginDetails authenticate(
             String userName, String password, FacilityConfig facility) {
         LOG.debug("Called fallback authenticator (" + fallbackMode + ") for " + userName);
     	if (fallbackMode == EcclesFallbackMode.NO_FALLBACK) {
     		return null;
     	}
         try {
             UserDetails userDetails = lookupUser(userName, true);
             if (fallbackMode == EcclesFallbackMode.USER_ONLY) {
             	LOG.debug("Skipping the password check for " + userName);
             	return buildDetails(userDetails, facility);
             }
             String savedDigest = userDetails.getDigest();
             if (savedDigest == null) {
                 if (fallbackMode == EcclesFallbackMode.USER_PASSWORD_OPTIONAL) {
                     LOG.debug("Skipping the optional password check for " + userName);
                     return buildDetails(userDetails, facility);
                 } else {
                     LOG.debug("User " + userName + " has no cached password");
                     return null;
                 }
             }
         	LOG.debug("Doing the password check for " + userName);
         	// (Backwards compatibility hack.  Passwords are case-insensitive (ACLS ... sigh)
         	// but we used to treat them as case-sensitive when we cached them ...)
         	for (String p : new String[]{password.toLowerCase(), password}) {
         	    String myDigest = createDigest(p, userDetails.getSeed());
         	    LOG.debug("Comparing " + myDigest + " with " + savedDigest);
         	    if (myDigest.equals(savedDigest)) {
             		return buildDetails(userDetails, facility);
         	    }
         	}
         	return null;
         } catch (UserDetailsException ex) {
         	LOG.debug("Unknown user " + userName);
             return null;
         }
     }
 
     private AclsLoginDetails buildDetails(
     		UserDetails userDetails, FacilityConfig facility) {
         // The default certification is 'VALID'
     	String certString = userDetails.getCertifications().get(facility.getFacilityName());
     	Certification cert = (certString == null) ?
     			Certification.VALID : Certification.parse(certString);
     	// The default account is 'unknown account'
     	List<String> accounts;
     	if (userDetails.getAccounts().isEmpty()) {
     	    accounts = Collections.singletonList("unknown account");
     	} else {
     	    accounts = new ArrayList<>(userDetails.getAccounts());
     	}
     	String hrName = userDetails.getHumanReadableName();
     	if (hrName == null) {
     	    hrName = "";
     	}
     	String orgName = userDetails.getOrgName();
     	if (orgName == null) {
     	    orgName = "";
     	}
         return new AclsLoginDetails(userDetails.getUserName(), 
         		hrName, orgName, null, facility.getFacilityName(), 
                 accounts, cert, userDetails.isOnsiteAssist(), true);
 	}
 
     public static String createDigest(String password, long seed) {
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
     
     private static String toString(byte[] bytes) {
         StringBuilder sb = new StringBuilder(bytes.length * 2 + 6);
         sb.append(bytes.length).append(":");
         for (byte b : bytes) {
             sb.append("0123456789ABCDEF".charAt((b >> 4) & 0xf));
             sb.append("0123456789ABCDEF".charAt(b & 0xf));
         }
         return sb.toString();
     }
     
     private void emClose(EntityManager em) {
         if (em.getTransaction().isActive()) {
             em.getTransaction().rollback();
         }
         em.close();
     }
 }
