 package com.exadel.borsch.notification;
 
 import com.exadel.borsch.dao.User;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PropertiesLoaderUtils;
 import org.springframework.stereotype.Service;
 
 /**
  * @author zubr
  */
 @Service
 public class EmailNotification extends Notification {
     protected static final Logger LOGGER = Logger.getLogger(EmailNotification.class.getName());
     private static final String MAILER_PROPERTY_FILE = "/mailer.properties";
 
     private Properties properties;
 
     @Value("${mailer.from}")
     private String from;
 
     @Value("${mailer.subject}")
     private String subject;
 
     public EmailNotification(String message) {
         super(message);
         // Let the Spring load property file for us
         try {
             Resource resource = new ClassPathResource(MAILER_PROPERTY_FILE);
             properties = PropertiesLoaderUtils.loadProperties(resource);
         } catch (IOException ex) {
             LOGGER.log(Level.SEVERE, "Cannot load mailer properties", ex);
         }
     }
 
     public Properties getProperties() {
         return properties;
     }
 
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
     public String getFrom() {
         return from;
     }
 
     public void setFrom(String from) {
         this.from = from;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     @Override
     public void submit(User target) {
         try {
             Session session = Session.getDefaultInstance(properties);
             MimeMessage message = new MimeMessage(session);
             message.setFrom(new InternetAddress(from));
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(target.getEmail()));
             message.setSubject(subject);
             message.setText(getMessage());
             Transport.send(message);
         } catch (AddressException ex) {
             LOGGER.log(Level.SEVERE, "User specified bad email address", ex);
         } catch (MessagingException ex) {
             LOGGER.log(Level.SEVERE, "Some java mailer exception", ex);
         }
     }
 }
