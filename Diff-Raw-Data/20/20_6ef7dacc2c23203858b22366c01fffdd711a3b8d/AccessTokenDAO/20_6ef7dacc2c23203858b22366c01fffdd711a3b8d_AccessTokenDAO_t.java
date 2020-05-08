 /**
  * 
  */
 package pl.psnc.dl.wf4ever.db.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 
 import pl.psnc.dl.wf4ever.db.AccessToken;
 import pl.psnc.dl.wf4ever.db.OAuthClient;
 import pl.psnc.dl.wf4ever.db.UserProfile;
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 
 /**
  * RODL user model.
  * 
  * @author piotrhol
  * 
  */
 public final class AccessTokenDAO extends AbstractDAO<AccessToken> {
 
     /** id. */
     private static final long serialVersionUID = -4468344863067565271L;
 
 
     /**
      * Find the access token by its value.
      * 
      * @param value
      *            access token value
      * @return access token active record
      */
     public AccessToken findByValue(String value) {
         return findByPrimaryKey(AccessToken.class, value);
     }
 
 
     /**
      * Find an access token by its client id, user id or both.
      * 
      * @param client
      *            client
     * @param userProfile
      *            owner
      * @return access token active record
      */
     @SuppressWarnings("unchecked")
    public List<AccessToken> findByClientOrUser(OAuthClient client, UserProfile userProfile) {
         Criteria criteria = HibernateUtil.getSessionFactory().getCurrentSession().createCriteria(AccessToken.class);
         if (client != null) {
             criteria.add(Restrictions.eq("client.clientId", client.getClientId()));
         }
        if (userProfile != null) {
            criteria.add(Restrictions.eq("user.login", userProfile.getLogin()));
         }
         return criteria.list();
     }
 
 }
