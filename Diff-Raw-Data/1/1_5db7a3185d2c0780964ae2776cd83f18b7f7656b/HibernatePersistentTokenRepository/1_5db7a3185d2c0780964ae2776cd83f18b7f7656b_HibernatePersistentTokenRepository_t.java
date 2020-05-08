 package com.jtbdevelopment.e_eye_o.hibernate.security;
 
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
 import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 
 /**
  * Date: 4/6/13
  * Time: 12:45 AM
  */
 @Component("persistentTokenRepository")
 @Transactional(propagation = Propagation.REQUIRED)
 @SuppressWarnings("unused")
 public class HibernatePersistentTokenRepository implements PersistentTokenRepository {
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public void createNewToken(final PersistentRememberMeToken token) {
         HibernatePersistentToken hToken = new HibernatePersistentToken();
         hToken.setSeries(token.getSeries());
         hToken.setTimestamp(token.getDate());
         hToken.setUsername(token.getUsername());
         hToken.setToken(token.getTokenValue());
         sessionFactory.getCurrentSession().save(hToken);
     }
 
     @Override
     public void updateToken(final String series, final String tokenValue, final Date lastUsed) {
         HibernatePersistentToken hToken = getHibernatePersistentToken(series);
         hToken.setToken(tokenValue);
         hToken.setTimestamp(lastUsed);
         sessionFactory.getCurrentSession().update(hToken);
     }
 
     private HibernatePersistentToken getHibernatePersistentToken(final String series) {
         Query query = sessionFactory.getCurrentSession().createQuery("from PersistentToken where series = :series");
         query.setParameter("series", series);
         return (HibernatePersistentToken) query.uniqueResult();
     }
 
     @Override
     public PersistentRememberMeToken getTokenForSeries(final String seriesId) {
         HibernatePersistentToken hToken = getHibernatePersistentToken(seriesId);
         if (hToken == null) {
             return null;
         }
         return new PersistentRememberMeToken(hToken.getUsername(), hToken.getSeries(), hToken.getToken(), hToken.getTimestamp());
     }
 
    //  TODO - logging out seems to log you out on all devices
     @Override
     public void removeUserTokens(final String username) {
         Query query = sessionFactory.getCurrentSession().createQuery("from " + sessionFactory.getClassMetadata(HibernatePersistentToken.class).getEntityName() + " where username = :username");
         query.setParameter("username", username);
         for (Object o : query.list()) {
             sessionFactory.getCurrentSession().delete(o);
         }
 
     }
 }
