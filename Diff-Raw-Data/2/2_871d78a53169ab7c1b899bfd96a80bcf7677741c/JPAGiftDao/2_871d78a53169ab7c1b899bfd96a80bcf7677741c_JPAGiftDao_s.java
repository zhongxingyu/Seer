 package com.mpower.dao;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.dao.util.QueryUtil;
 import com.mpower.domain.Gift;
 import com.mpower.util.EntityUtility;
 
 @Repository("giftDao")
 public class JPAGiftDao implements GiftDao {
 	
     /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
 
 
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
     public List<Gift> readGifts(String siteName, Map<String, Object> params) {
         boolean whereUsed = true;
         StringBuilder queryString = new StringBuilder("SELECT gift FROM com.mpower.domain.Gift gift WHERE gift.person.site.name = :siteName");
         Map<String, Object> addressParams = new HashMap<String, Object>();
         Map<String, String> customParams = new HashMap<String, String>();
         LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<String, Object>();
         if (params != null) {
             String key;
             Object value;
             for (Map.Entry<String, Object> pair : params.entrySet()) {
                 key = pair.getKey();
                 value = pair.getValue();
                 boolean isString = true;
                 if (value instanceof String) {
                     if (GenericValidator.isBlankOrNull((String) value)) {
                         continue;
                     }
                 } else {
                     if (value == null) {
                         continue;
                     }
                     isString = false;
                 }
                 if (key.startsWith("person.addressMap[")) {
                     addressParams.put(key.substring(key.lastIndexOf('.') + 1), value);
                 } else if (key.startsWith("customFieldMap[")) {
                     customParams.put(key.substring(key.indexOf('[') + 1, key.indexOf(']')), (String) value);
                 } else {
                     whereUsed = EntityUtility.addWhereOrAnd(whereUsed, queryString);
                     queryString.append(" gift.");
                     queryString.append(key);
                     String paramName = key.replace(".", "_");
                     if (isString) {
                         queryString.append(" LIKE :");
                         queryString.append(paramName);
                         parameterMap.put(paramName, "%" + value + "%");
                     } else {
                         queryString.append(" = :");
                         queryString.append(paramName);
                         parameterMap.put(paramName, value);
                     }
                 }
             }
         }
         queryString.append(getAddressString(addressParams, parameterMap));
         queryString.append(QueryUtil.getCustomString(customParams, parameterMap));
 
         Query query = em.createQuery(queryString.toString());
         query.setParameter("siteName", siteName);
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
             return ((BigDecimal) query.getSingleResult()).doubleValue();
         }
         return 0.00;
     }
 
     private StringBuilder getAddressString(Map<String, Object> addressParams, LinkedHashMap<String, Object> parameterMap) {
         StringBuilder addressString = new StringBuilder();
         if (addressParams != null && !addressParams.isEmpty()) {
             addressString.append(" AND EXISTS ( SELECT personAddress FROM com.mpower.domain.PersonAddress personAddress WHERE personAddress.person.id = gift.person.id ");
             for (Map.Entry<String, Object> pair : addressParams.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();
                 addressString.append("AND personAddress.address.");
                 addressString.append(key);
                 addressString.append(" LIKE :");
                 String paramName = key.replace(".", "_");
                 addressString.append(paramName);
                 if (value instanceof String) {
                     parameterMap.put(paramName, "%" + value + "%");
                 } else {
                     parameterMap.put(paramName, value);
                 }
             }
             addressString.append(")");
         }
         return addressString;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> readGiftsByPersonId(Long personId) {
         Query query = em.createNamedQuery("READ_GIFTS_BY_PERSON_ID");
         query.setParameter("personId", personId);
         List<Gift> giftList = query.getResultList();
         return giftList;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> readAllGifts() {
         Query query = em.createNamedQuery("READ_ALL_GIFTS");
         List<Gift> lg = query.getResultList();
         return lg;
     }
 
 }
