 package uk.ac.ox.oucs.oauth.service;
 
 import org.joda.time.DateTime;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import uk.ac.ox.oucs.oauth.advisor.LimitedPermissionsAdvisor;
 import uk.ac.ox.oucs.oauth.dao.OAuthProvider;
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 import uk.ac.ox.oucs.oauth.domain.Consumer;
 import uk.ac.ox.oucs.oauth.exception.*;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Random;
 
 /**
  * @author Colin Hebert
  */
 public class OAuthServiceImpl implements OAuthService {
     private OAuthProvider oAuthProvider;
     private boolean keepOldAccessors;
 
     public void setoAuthProvider(OAuthProvider oAuthProvider) {
         this.oAuthProvider = oAuthProvider;
     }
 
     public void setKeepOldAccessors(boolean keepOldAccessors) {
         this.keepOldAccessors = keepOldAccessors;
     }
 
     public Accessor getAccessor(String token, Accessor.Type expectedType) {
         Accessor accessor = oAuthProvider.getAccessor(token);
 
         if (accessor == null)
             throw new InvalidAccessorException("Accessor '" + token + "' doesn't exist.");
 
         if (accessor.getStatus() == Accessor.Status.VALID && !isStillValid(accessor))
             updateAccessorStatus(accessor, Accessor.Status.EXPIRED);
 
        if(accessor.getStatus() == Accessor.Status.EXPIRED)
             throw new ExpiredAccessorException("Accessor '" + token + " expired");
        else if(accessor.getStatus() == Accessor.Status.REVOKED)
             throw new RevokedAccessorException("Accessor '" + token + " revoked");
         else if (accessor.getStatus() != Accessor.Status.VALID)
             throw new InvalidAccessorException("Accessor '" + token + "' is not valid. (" + accessor.getStatus() + ")");
 
         if (accessor.getType() != expectedType)
             throw new InvalidAccessorException("Accessor with unexpected type " + accessor.getType());
 
         return accessor;
     }
 
     @Override
     public SecurityAdvisor getSecurityAdvisor(String accessorId) {
         Accessor accessor = getAccessor(accessorId, Accessor.Type.ACCESS);
         Consumer consumer = oAuthProvider.getConsumer(accessor.getConsumerId());
         return new LimitedPermissionsAdvisor(consumer.getRights());
     }
 
     @Override
     public Consumer getConsumer(String consumerKey) {
         return oAuthProvider.getConsumer(consumerKey);
     }
 
     @Override
     public Accessor createRequestAccessor(String consumerId, String secret, String callback) {
         Consumer consumer = oAuthProvider.getConsumer(consumerId);
         if (consumer == null)
             throw new InvalidConsumerException("Invalid consumer " + consumerId);
         Accessor accessor = new Accessor();
         accessor.setConsumerId(consumer.getId());
         accessor.setType(Accessor.Type.REQUEST);
         accessor.setStatus(Accessor.Status.VALID);
         accessor.setCreationDate(new DateTime().toDate());
         //A request accessor is valid for 15 minutes only
         accessor.setExpirationDate(new DateTime().plusMinutes(15).toDate());
         if (secret != null)
             //Support Variable Accessor Secret http://wiki.oauth.net/w/page/12238502/AccessorSecret
             accessor.setSecret(secret);
         else if (consumer.getAccessorSecret() != null)
             //Support Accessor Secret http://wiki.oauth.net/w/page/12238502/AccessorSecret
             accessor.setSecret(consumer.getAccessorSecret());
         else
             accessor.setSecret(consumer.getSecret());
 
         if (callback != null)
             accessor.setCallbackUrl(callback);
         else if (consumer.getCallbackURL() != null)
             accessor.setCallbackUrl(consumer.getCallbackURL());
         else
             accessor.setCallbackUrl(OUT_OF_BAND_CALLBACK);
 
         accessor.setToken(generateToken(accessor));
         oAuthProvider.createAccessor(accessor);
 
         return accessor;
     }
 
     private static String generateToken(Accessor accessor) {
         // TODO Need a better way of generating tokens in the long run.
         return generateMd5(accessor.getConsumerId() + System.nanoTime());
     }
 
     private static String generateMd5(String string) {
         try {
             MessageDigest messageDigest = MessageDigest.getInstance("MD5");
             byte[] md5Bytes = messageDigest.digest(string.getBytes("UTF-8"));
 
             StringBuilder md5String = new StringBuilder();
             for (byte md5Byte : md5Bytes) {
                 md5String.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
             }
             return md5String.toString();
         } catch (NoSuchAlgorithmException e) {
             // Unless you don't have md5 on your JVM it will work (so this exception won't happen)
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             // Unless you don't have UTF-8 on your JVM it will work (so this exception won't happen)
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public Accessor startAuthorisation(String accessorId) {
         Accessor accessor = getAccessor(accessorId, Accessor.Type.REQUEST);
         accessor.setVerifier(generateVerifier(accessor));
         accessor.setType(Accessor.Type.REQUEST_AUTHORISING);
         //The authorisation must be done in less than 15 minutes
         accessor.setExpirationDate(new DateTime().plusMinutes(15).toDate());
         accessor = oAuthProvider.updateAccessor(accessor);
         return accessor;
     }
 
     @Override
     public Accessor authoriseAccessor(String accessorId, String verifier, String userId) {
         Accessor accessor = getAccessor(accessorId, Accessor.Type.REQUEST_AUTHORISING);
         if (!accessor.getVerifier().equals(verifier))
             throw new OAuthException("Accessor verifier invalid.");
         accessor.setVerifier(generateVerifier(accessor));
         accessor.setType(Accessor.Type.REQUEST_AUTHORISED);
         accessor.setUserId(userId);
         //An authorised request accessor is valid for one month only
         accessor.setExpirationDate(new DateTime().plusMonths(1).toDate());
         accessor = oAuthProvider.updateAccessor(accessor);
         return accessor;
     }
 
     private static String generateVerifier(Accessor accessor) {
         return String.valueOf(new Random().nextInt(10000));
     }
 
     @Override
     public Accessor createAccessAccessor(String requestAccessorId) {
         Accessor requestAccessor = getAccessor(requestAccessorId, Accessor.Type.REQUEST_AUTHORISED);
 
         Consumer consumer = oAuthProvider.getConsumer(requestAccessor.getConsumerId());
         Accessor accessAccessor = new Accessor();
         accessAccessor.setConsumerId(consumer.getId());
         accessAccessor.setUserId(requestAccessor.getUserId());
         accessAccessor.setType(Accessor.Type.ACCESS);
         accessAccessor.setStatus(Accessor.Status.VALID);
         accessAccessor.setCreationDate(new DateTime().toDate());
         //An access accessor is valid based on the number of minutes given by the consumer
         if (consumer.getDefaultValidity() != null)
             accessAccessor.setExpirationDate(new DateTime().plusMinutes(consumer.getDefaultValidity()).toDate());
         accessAccessor.setToken(generateToken(accessAccessor));
 
         updateAccessorStatus(requestAccessor, Accessor.Status.EXPIRED);
         oAuthProvider.createAccessor(accessAccessor);
 
         return accessAccessor;
     }
 
     @Override
     public Collection<Accessor> getAccessAccessorForUser(String userId) {
         Collection<Accessor> accessors = new ArrayList<Accessor>(oAuthProvider.getAccessorsByUser(userId));
 
         for (Iterator<Accessor> iterator = accessors.iterator(); iterator.hasNext(); ) {
             Accessor accessor = iterator.next();
 
             if (accessor.getStatus() == Accessor.Status.VALID && !isStillValid(accessor))
                 updateAccessorStatus(accessor, Accessor.Status.EXPIRED);
 
            if (accessor.getStatus() != Accessor.Status.VALID)
                 iterator.remove();
            if (accessor.getType() != Accessor.Type.ACCESS) {
                iterator.remove();
            }
         }
         return accessors;
     }
 
     @Override
     public void revokeAccessor(String accessorId) {
         try {
             Accessor accessor = getAccessor(accessorId, Accessor.Type.ACCESS);
             updateAccessorStatus(accessor, Accessor.Status.REVOKED);
         } catch (OAuthException ignored) {
             //If the accessor is already expired/revoked, nothing to do/handle
         }
     }
 
     private static boolean isStillValid(Accessor accessor) {
         return accessor.getExpirationDate() == null || new DateTime(accessor.getExpirationDate()).isAfterNow();
     }
 
     private void updateAccessorStatus(Accessor accessor, Accessor.Status status) {
         if (keepOldAccessors || status == Accessor.Status.VALID) {
             accessor.setStatus(status);
             oAuthProvider.updateAccessor(accessor);
         } else
             oAuthProvider.removeAccessor(accessor);
     }
 }
