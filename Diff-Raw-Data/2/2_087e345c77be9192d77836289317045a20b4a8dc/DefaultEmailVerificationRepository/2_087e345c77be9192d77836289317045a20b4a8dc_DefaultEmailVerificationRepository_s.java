 package com.aciertoteam.mail.repositories.impl;
 
 import java.util.List;
 import com.aciertoteam.common.repository.impl.DefaultAbstractRepository;
 import com.aciertoteam.mail.entity.EmailVerification;
 import com.aciertoteam.mail.enums.RequestStatus;
 import com.aciertoteam.mail.repositories.EmailVerificationRepository;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Repository(value = "emailVerificationRepository")
 @Transactional
 public class DefaultEmailVerificationRepository extends DefaultAbstractRepository<EmailVerification> implements
         EmailVerificationRepository {
 
     private static final Logger LOGGER = Logger.getLogger(DefaultEmailVerificationRepository.class);
 
     @Override
     public EmailVerification createVerificationToken(String email, String token) {
         LOGGER.info("Creating of verification token with next data: email = " + email + ", token = " + token);
 
         EmailVerification emailVerification = new EmailVerification();
 
         emailVerification.setEmail(email);
         emailVerification.setToken(token);
         emailVerification.closeEndPeriod(new DateTime().plusDays(1).toDate());
         emailVerification.setRequestStatus(RequestStatus.REQUESTED);
 
         saveOrUpdate(emailVerification);
 
         return emailVerification;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public EmailVerification getByEmail(String email) {
        List<EmailVerification> emailVerificationList = getSession()
                 .createQuery("from EmailVerification where email = :email order by timestamp desc")
                 .setParameter("email", email).list();
 
         return emailVerificationList.isEmpty() ? null : emailVerificationList.get(0);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public EmailVerification findByNotificationId(Long notificationId) {
         List<EmailVerification> emailVerificationList = getSession()
                 .createQuery("from EmailVerification where notification.id = :notificationId order by timestamp desc")
                 .setParameter("notificationId", notificationId).list();
 
         return emailVerificationList.isEmpty() ? null : emailVerificationList.get(0);
     }
 }
