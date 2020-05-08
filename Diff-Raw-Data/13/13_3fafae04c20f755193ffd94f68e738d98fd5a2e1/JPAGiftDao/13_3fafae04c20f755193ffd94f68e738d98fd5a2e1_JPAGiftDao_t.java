 package com.mpower.dao;
 
 import java.math.BigDecimal;
 import java.util.Date;
import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.stereotype.Repository;
 
 import com.mpower.domain.Gift;
 
 @Repository("giftDao")
 public class JPAGiftDao implements GiftDao {
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Gift maintainGift(Gift gift) {
         if (gift.getId() == null) {
             em.persist(gift);
             return gift;
         }
         return em.merge(gift);
     }
 
     @Override
     public Gift readGift(Long giftId) {
         return em.find(Gift.class, giftId);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> readGifts(Long personId) {
         Query query = em.createNamedQuery("READ_GIFT_BY_PERSON");
         query.setParameter("personId", personId);
         return query.getResultList();
     }

     @Override
     public double analyzeMajorDonor(Long personId, Date beginDate, Date currentDate) {
 			Query query = em.createNamedQuery("ANALYZE_FOR_MAJOR_DONOR");
 			query.setParameter("personId", personId);
 			query.setParameter("beginDate", beginDate);
 			query.setParameter("currentDate", currentDate);
 			if (query.getSingleResult() != null) {
 				return ((BigDecimal)query.getSingleResult()).doubleValue();
			}
 		return 0.00;
     }
 }
