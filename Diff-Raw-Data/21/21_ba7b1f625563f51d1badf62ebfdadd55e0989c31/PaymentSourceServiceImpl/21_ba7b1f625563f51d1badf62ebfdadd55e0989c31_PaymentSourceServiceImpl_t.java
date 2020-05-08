 package com.mpower.service.impl;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.mpower.dao.PaymentSourceDao;
 import com.mpower.domain.PaymentSource;
import com.mpower.service.AddressService;
 import com.mpower.service.AuditService;
 import com.mpower.service.InactivateService;
 import com.mpower.service.PaymentSourceService;
import com.mpower.service.PhoneService;
 
 @Service("paymentSourceService")
 public class PaymentSourceServiceImpl implements PaymentSourceService, InactivateService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "auditService")
     private AuditService auditService;
 
    @Resource(name = "addressService")
    private AddressService addressService;

    @Resource(name = "phoneService")
    private PhoneService phoneService;

     @Resource(name = "paymentSourceDao")
     private PaymentSourceDao paymentSourceDao;
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public PaymentSource maintainPaymentSource(PaymentSource paymentSource) {
    	
        if (paymentSource.getAddress().getId() == null) {
        	paymentSource.setAddress(addressService.saveAddress(paymentSource.getAddress()));
        }
        if (paymentSource.getPhone().getId() == null) {
        	paymentSource.setPhone(phoneService.savePhone(paymentSource.getPhone()));
        }

         paymentSource = paymentSourceDao.maintainPaymentSource(paymentSource);
        
         if (paymentSource.isInactive()) {
             auditService.auditObjectInactive(paymentSource);
         }
         else {
             auditService.auditObject(paymentSource);
         }
         return paymentSource;
     }
 
     public List<PaymentSource> readPaymentSources(Long personId) {
         return paymentSourceDao.readActivePaymentSources(personId);
     }
 
     public void setAuditService(AuditService auditService) {
         this.auditService = auditService;
     }
 
     @Override
     public PaymentSource readPaymentSource(Long paymentSourceId) {
         return paymentSourceDao.readPaymentSource(paymentSourceId);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void inactivate(Long id) {
         if (logger.isDebugEnabled()) {
             logger.debug("inactivate: id = " + id);
         }
         PaymentSource ps = this.readPaymentSource(id);
         ps.setInactive(true);
         this.maintainPaymentSource(ps);
     }
 
     @Override
     public PaymentSource findPaymentSourceProfile(Long personId, String profile) {
         if (logger.isDebugEnabled()) {
             logger.debug("findPaymentSourceProfile: personId = " + personId + " profile = " + profile);
         }
         return paymentSourceDao.findPaymentSourceProfile(personId, profile);
     }
 }
