 package com.mpower.dao.jpa;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.FlushModeType;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.dao.PaymentSourceDao;
 import com.mpower.domain.PaymentSource;
 
 @Repository("paymentSourceDao")
 public class JPAPaymentSourceDao implements PaymentSourceDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public PaymentSource maintainPaymentSource(PaymentSource paymentSource) {
         if (paymentSource.getId() == null) {
             em.persist(paymentSource);
             return paymentSource;
         } else {
             return em.merge(paymentSource);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public PaymentSource findPaymentSourceProfile(Long personId, String profile) {
         if (logger.isDebugEnabled()) {
             logger.debug("findPaymentSourceProfile: personId = " + personId + " profile = " + profile);
         }
         Query query = em.createNamedQuery("READ_PAYMENT_SOURCE_BY_PERSON_ID_PROFILE");
         query.setFlushMode(FlushModeType.COMMIT);
         query.setParameter("personId", personId);
         query.setParameter("profile", profile);
         List<PaymentSource> l = query.getResultList();
         PaymentSource source = null;
         if (l != null && !l.isEmpty()) {
             source = l.get(0);
         }
         return source;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<PaymentSource> readActivePaymentSources(Long personId) {
         Query query = em.createNamedQuery("READ_ACTIVE_PAYMENT_SOURCES_BY_PERSON_ID");
         query.setParameter("personId", personId);
         List<PaymentSource> paymentSourceList = query.getResultList();
         return paymentSourceList;
     }
 
     @Override
     public PaymentSource readPaymentSource(Long paymentSourceId) {
         return em.find(PaymentSource.class, paymentSourceId);
     }
 }
