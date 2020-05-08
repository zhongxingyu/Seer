 package com.pk.cwierkacz.model.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.joda.time.DateTime;
 
 import com.pk.cwierkacz.model.dao.TweetDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 
 public class TweetService extends AbstractService<TweetDao>
 {
 
     public TweetService( SessionFactory sessionFactory ) {
         super(sessionFactory);
     }
 
     public List<TweetDao> getTweets( ) {
         Criterion[] criteria = new Criterion[] {};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria), TweetDao.class);
         return result;
     }
 
     public List<TweetDao> getActualTweets( ) {
         Criterion[] criteria = new Criterion[] {Restrictions.eq("isDeleted", false)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria), TweetDao.class);
         return result;
     }
 
     public TweetDao getTweetById( Long id ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.eq("Id", id)};
         TweetDao result = getUniqueByCriteria(Arrays.asList(criteria), TweetDao.class);
         return result;
     }
 
     public TweetDao getByExternalId( Long id ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.eq("externalId", id)};
         TweetDao result = getUniqueByCriteria(Arrays.asList(criteria), TweetDao.class);
         return result;
     }
 
     public List<TweetDao> getByIds( List<Long> ids ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.in("Id", ids)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria),
                                                   TweetDao.class,
                                                   Order.desc("createdDate"));
         return result;
     }
 
     public TweetDao getLastActualTweetForAccount( TwitterAccountDao account ) {
         List<TweetDao> t = getActualTweetForAccount(account, null, null, 1);
         if ( t.size() >= 1 )
             return t.get(0);
         else
             return null;
     }
 
     public List<TweetDao> getActualTweetForAccount( TwitterAccountDao account,
                                                     DateTime since,
                                                     DateTime to,
                                                     Integer maxResult ) {
 
         List<TweetDao> result = getActualTweetForAccounts(Arrays.asList(account), since, to, maxResult);
 
         return result;
     }
 
     public List<TweetDao> getActualTweetForAccounts( List<TwitterAccountDao> accounts,
                                                      DateTime since,
                                                      DateTime to,
                                                      Integer maxResult ) {
 
         if ( accounts == null || accounts.size() == 0 ) {
             return new ArrayList<TweetDao>();
         }
 
         List<Criterion> criteria = new ArrayList<>();
        criteria.add(Restrictions.in("creator", accounts));
         criteria.add(Restrictions.eq("isDeleted", false));
 
         if ( since != null )
             criteria.add(Restrictions.ge("createdDate", since));
 
         if ( to != null )
             criteria.add(Restrictions.lt("createdDate", to));
 
         List<TweetDao> result = getListByCriteria(criteria, TweetDao.class, Order.desc("createdDate"));
 
         if ( maxResult != null && maxResult > 0 )
             result = result.subList(0, maxResult);
         return result;
     }
 
     public int countActualRetweets( TweetDao retweeted ) {
         Criterion[] criteria = new Criterion[] {Restrictions.eq("retweeted", retweeted),
                                                 Restrictions.eq("isDeleted", false)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria), TweetDao.class);
 
         return result.size();
     }
 
     public List<TweetDao> getActualRetweets( TweetDao retweeted ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.eq("retweeted", retweeted),
                                                 Restrictions.eq("isDeleted", false)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria),
                                                   TweetDao.class,
                                                   Order.desc("createdDate"));
 
         return result;
     }
 
     public TweetDao getLastActualReplies( TweetDao inReplyTo ) {
         List<TweetDao> t = getActualReplies(inReplyTo);
         if ( t.size() >= 1 )
             return t.get(0);
         else
             return null;
     }
 
     public int countActualReplies( TweetDao retweeted ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.eq("inReplyTo", retweeted),
                                                 Restrictions.eq("isDeleted", false)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria), TweetDao.class);
 
         return result.size();
     }
 
     public List<TweetDao> getActualReplies( TweetDao inReplyTo ) {
 
         Criterion[] criteria = new Criterion[] {Restrictions.eq("inReplyTo", inReplyTo),
                                                 Restrictions.eq("isDeleted", false)};
         List<TweetDao> result = getListByCriteria(Arrays.asList(criteria),
                                                   TweetDao.class,
                                                   Order.desc("createdDate"));
 
         return result;
     }
 
 }
