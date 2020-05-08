 package com.aciertoteam.mail.services.impl;
 
 import com.aciertoteam.mail.dto.NotificationDTO;
 import com.aciertoteam.mail.entity.DecisionRequest;
 import com.aciertoteam.mail.entity.EmailVerification;
 import com.aciertoteam.mail.entity.Notification;
 import com.aciertoteam.mail.enums.NotificationStatus;
 import com.aciertoteam.mail.enums.RequestStatus;
 import com.aciertoteam.mail.repositories.EmailVerificationRepository;
 import com.aciertoteam.mail.repositories.MailTemplateRepository;
 import com.aciertoteam.mail.repositories.NotificationRepository;
 import com.aciertoteam.mail.services.MailConfigurationService;
 import com.aciertoteam.mail.services.NotificationService;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Service
 @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
 public class DefaultNotificationService implements NotificationService {
 
     @Autowired
     private NotificationRepository notificationRepository;
 
     @Autowired
     private EmailVerificationRepository emailVerificationRepository;
 
     @Autowired
     private MailTemplateRepository mailTemplateRepository;
 
     @Autowired
     private MailConfigurationService configurationService;
 
     @Override
     public void notifyUserAboutRegisteredDecisionRequest(DecisionRequest decisionRequest) {
         Notification notification = createRegistrationRequestNotification(decisionRequest);
         notificationRepository.saveOrUpdate(notification);
     }
 
     @Override
     public void notifyUserAboutTakenDecisionByRequest(DecisionRequest decisionRequest) {
         Notification notification = createResultOfRequestNotification(decisionRequest);
         notificationRepository.saveOrUpdate(notification);
     }
 
     @Override
     public EmailVerification createVerificationToken(String email, String token) {
         return emailVerificationRepository.createVerificationToken(email, token);
     }
 
     @Override
     public Collection<NotificationDTO> findNotifications(NotificationStatus status) {
         return Notification.createDTOList(notificationRepository.findCollectionByField("status", status));
     }
 
     @Override
     public Collection<NotificationDTO> findNotifications(List<NotificationStatus> statuses) {
         return Notification.createDTOList(notificationRepository.findNotificationByStatuses(statuses));
     }
 
     public Map<String, Long> loadNotifications(Collection<Notification> notifications) {
         notificationRepository.saveAll(notifications);
         return createNotificationMap(notifications);
     }
 
     private Notification createRegistrationRequestNotification(DecisionRequest decisionRequest) {
         Notification notification = new Notification(NotificationStatus.CREATED);
         notification.setLocale(decisionRequest.getLocale());
         notification.setTo(decisionRequest.getRecipient());
         notification.setMailTemplate(mailTemplateRepository.findByTemplateName("registered_decision_request"));
         notification.setAttachments(getRequestNotificationImages());
         addRegistrationProperties(notification, decisionRequest);
         return notification;
     }
 
     private Notification createResultOfRequestNotification(DecisionRequest decisionRequest) {
         boolean isAccepted = decisionRequest.getRequestStatus().isAccepted();
 
         Notification notification = new Notification(NotificationStatus.CREATED);
         notification.setLocale(decisionRequest.getLocale());
         notification.setTo(decisionRequest.getRecipient());
         notification.setMailTemplate(mailTemplateRepository.findByTemplateName(
                 String.format("%s_decision_request", isAccepted ? "success" : "rejected")));
         notification.setAttachments(getResultNotificationImages(decisionRequest));
         addProperties(notification, decisionRequest);
 
         return notification;
     }
 
     private void addRegistrationProperties(Notification notification, DecisionRequest decisionRequest) {
         HashMap<String, Object> properties = new HashMap<String, Object>();
         properties.put("requestSid", decisionRequest.getSid());
         properties.put("user", "user");
         notification.setProperties(properties);
     }
 
     private void addProperties(Notification notification, DecisionRequest decisionRequest) {
         HashMap<String, Object> properties = new HashMap<String, Object>();
         properties.put("requestSid", decisionRequest.getSid());
         properties.put("cause", decisionRequest.getRejectCause());
         properties.put("user", "user");
         notification.setProperties(properties);
     }
 
     private HashMap<String, Object> getCommonNotificationImages() {
         HashMap<String, Object> attachments = new HashMap<String, Object>();
        attachments.put("cid:email_logo", "/com/aciertoteam/images/logo.gif");
         attachments.put("cid:logo_big", "/com/aciertoteam/images/logo_big.png");
         return attachments;
     }
 
     private HashMap<String, Object> getRequestNotificationImages() {
         HashMap<String, Object> attachments = getCommonNotificationImages();
         attachments.put("cid:new_request", "/com/aciertoteam/images/new_request.png");
         return attachments;
     }
 
     private HashMap<String, Object> getResultNotificationImages(DecisionRequest decisionRequest) {
         HashMap<String, Object> attachments = getCommonNotificationImages();
         if (decisionRequest.getRequestStatus().isFailed()) {
             attachments.put("cid:rejected_decision_request", "/com/aciertoteam/images/rejected_decision_request.png");
         } else {
             attachments.put("cid:accepted_decision_request", "/com/aciertoteam/images/accepted_decision_request.png");
         }
         return attachments;
     }
 
     private Map<String, Long> createNotificationMap(Collection<Notification> notifications) {
         Map<String, Long> notificationMap = new HashMap<String, Long>();
         for (Notification notification : notifications) {
             notificationMap.put(getEmail(notification), notification.getId());
         }
         return notificationMap;
     }
 
     private String getEmail(Notification notification) {
         String email = notification.getFrom();
         if (StringUtils.isNotBlank(email)) {
             return configurationService.getConfig("mail.user").getValue();
         }
         return email;
     }
 
     public void markAsSent(long notificationId, boolean successfully) {
         Notification notification = notificationRepository.get(notificationId);
         notification.setStatus(successfully ? NotificationStatus.SUCCESSFULLY_SENT : NotificationStatus.FAILED_SENT);
         updateEmailVerificationStatus(notification);
     }
 
     @Override
     public void markAsPending(Collection<NotificationDTO> notifications) {
         for (NotificationDTO n : notifications) {
             if (n.isCreated()) {
                 Notification notification = notificationRepository.get(n.getId());
                 notification.setStatus(NotificationStatus.PENDING);
                 notificationRepository.saveOrUpdate(notification);
             }
         }
     }
 
     private void updateEmailVerificationStatus(Notification notification) {
         EmailVerification emailVerification = emailVerificationRepository.findByNotificationId(notification.getId());
         if (emailVerification != null) {
             emailVerification.setRequestStatus(RequestStatus.PENDING);
             emailVerificationRepository.saveOrUpdate(emailVerification);
         }
     }
 }
