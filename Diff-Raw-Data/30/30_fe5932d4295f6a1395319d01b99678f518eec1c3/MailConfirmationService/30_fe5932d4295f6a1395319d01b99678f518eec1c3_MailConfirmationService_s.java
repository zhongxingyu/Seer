 package org.homebudget.services;
 
 import javax.annotation.Resource;
 import org.apache.log4j.Logger;
 import org.homebudget.model.UserDetails;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 
 import org.springframework.stereotype.Service;
 
 @Service
 public class MailConfirmationService {
 
     private static final Logger logger = Logger.getLogger(MailConfirmationService.class);
     
     @Resource
     private MailSender mailSender;
     
    private SimpleMailMessage message;
 
     public MailConfirmationService() {
        this.message = new SimpleMailMessage();
     }
 
     public void sendConfirmation(UserDetails userDetails) {
        message.setTo(userDetails.getEmail());
        message.setSubject("Homebudget registration. The last step");
        message.setText(
                 "Dear "
                 + userDetails.getUserName() + " "
                 + userDetails.getUserSurname()
                 + ",\n Thank you for your registration at homebudget service.\n"
                 + "In order to use all our features we need to confirm your E-Mail.\n"
                 + "Please click the link below to complete your registration.\n"
                 + "<<Very pretty link>>");
         try {
            mailSender.send(message);
             logger.info("Confirmation E-Mail was successfully sent to :" + userDetails.getEmail());
 
         } catch (MailException ex) {
             logger.error("Message sending to email:" + userDetails.getEmail() + " is failed!\n");
             ex.printStackTrace();
         }
     }
 
     public MailSender getMailSender() {
         return mailSender;
     }
 
     public void setMailSender(MailSender mailSender) {
         this.mailSender = mailSender;
     }
 
    public SimpleMailMessage getMessage() {
        return message;
    }
 
    public void setMessage(SimpleMailMessage message) {
        this.message = message;
    }
 }
