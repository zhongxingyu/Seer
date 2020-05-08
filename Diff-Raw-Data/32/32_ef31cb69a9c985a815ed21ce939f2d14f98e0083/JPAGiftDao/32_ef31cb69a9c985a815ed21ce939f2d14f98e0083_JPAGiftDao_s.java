 package com.mpower.dao;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.domain.Gift;
 import com.mpower.util.EntityUtility;
 
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
 
     @SuppressWarnings("unchecked")
     @Override
    public List<Gift> readGifts(Long personId, Map<String, String> params) {
         boolean whereUsed = true;
        StringBuffer queryString = new StringBuffer("SELECT gift FROM com.mpower.domain.Gift gift WHERE gift.person.id = :personId");
         LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<String, Object>();
         if (params != null) {
             String key;
             String value;
             for (Map.Entry<String, String> pair : params.entrySet()) {
                 key = pair.getKey();
                 value = pair.getValue();
                 if (!GenericValidator.isBlankOrNull(value)) {
                     whereUsed = EntityUtility.addWhereOrAnd(whereUsed, queryString);
                     queryString.append(" gift.");
                     queryString.append(key);
                     queryString.append(" LIKE :");
                     String paramName = key.replace(".", "_");
                     queryString.append(paramName);
                     parameterMap.put(paramName, value + "%");
                 }
             }
         }
         Query query = em.createQuery(queryString.toString());
 
        query.setParameter("personId", personId);
         for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
             query.setParameter(entry.getKey(), entry.getValue());
         }
         List giftList = query.getResultList();
         return giftList;
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
