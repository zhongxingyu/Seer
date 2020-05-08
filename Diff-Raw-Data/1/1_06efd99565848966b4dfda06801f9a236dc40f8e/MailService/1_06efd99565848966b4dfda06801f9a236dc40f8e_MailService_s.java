 package ru.kpfu.quantum.service.mailing;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import ru.kpfu.quantum.spring.entities.PendingMail;
 import ru.kpfu.quantum.spring.repository.PendingMailRepository;
 
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author sala
  */
 public class MailService {
     private static final Log log = LogFactory.getLog(MailService.class);
 
     private String host;
     private String sender;
     private String username;
     private String password;
 
     private String domain;
 
     private PendingMailRepository pendingMailRepository;
 
     private MailManager mailManager;
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public void setSender(String sender) {
         this.sender = sender;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setPendingMailRepository(PendingMailRepository pendingMailRepository) {
         this.pendingMailRepository = pendingMailRepository;
     }
 
     public void setMailManager(MailManager mailManager) {
         this.mailManager = mailManager;
     }
 
     public void setDomain(String domain) {
         this.domain = domain;
     }
 
     public void init() {
         log.info("MailService initialized");
         log.info("Host: " + host);
         log.info("Sender: " + sender);
         log.info("Username: " + username);
         log.info("Password: " + password);
     }
 
     public void sendMail() {
         log.info("sendMail() called");
         final Page<PendingMail> mailToSend = pendingMailRepository.findBySent(false, new PageRequest(0, 10));
         log.info(String.format("Found %d pending mails", mailToSend.getContent().size()));
 
         for(PendingMail pendingMail : mailToSend) {
             sendMail(pendingMail);
         }
     }
 
     private void sendMail(PendingMail pendingMail) {
         final String to = pendingMail.getReceiver();
         final String from = sender;
         final String host = this.host;
         //
         Properties properties = new Properties();
         // Setup mail server
         properties.setProperty("mail.transport.protocol", "smtp");
         properties.setProperty("mail.smtp.host", host);
         properties.setProperty("mail.smtp.auth", "true");
 
         Authenticator auth = new SMTPAuthenticator();
         Session session = Session.getInstance(properties, auth);
 
         try{
             // Create a default MimeMessage object.
             MimeMessage message = new MimeMessage(session);
 
             // Set From: header field of the header.
             message.setFrom(new InternetAddress(from));
 
             // Set To: header field of the header.
             message.addRecipient(Message.RecipientType.TO,
                     new InternetAddress(to));
 
             // Set Subject: header field
             message.setSubject(pendingMail.getSubject());
 
 
 
             // Now set the actual message
             message.setContent(pendingMail.getMessage(), "text/html; charset=utf-8");
 
             // Send message
             Transport.send(message);
             pendingMail.setSent(true);
             pendingMailRepository.save(pendingMail);
             log.info("Email was sent successfully");
         } catch (MessagingException mex) {
             log.error("An exception has occurred while sending mail", mex);
         }
 
 
     }
 
     public void sendInvite(String email, String code) {
         Map<String, Object> params = new HashMap<>();
         params.put("code", code);
         params.put("email", email);
         params.put("registerUrl", domain+"/registration");
         final String mail = mailManager.getMail("/invite.ftl", params);
         PendingMail pendingMail = new PendingMail(email, "Приглашение к регистрации", mail);
         pendingMailRepository.save(pendingMail);
     }
 
     public void sendPasswordRemind(String email, String remindCode) {
         Map<String, Object> params = new HashMap<>();
         params.put("remindCode", remindCode);
         params.put("domain", domain);
         final String mail = mailManager.getMail("/remind.ftl", params);
         PendingMail pendingMail = new PendingMail(email, "Восстановление пароля", mail);
         pendingMailRepository.save(pendingMail);
     }
 
     private class SMTPAuthenticator extends Authenticator {
         @Override
         public PasswordAuthentication getPasswordAuthentication() {
             String username = MailService.this.username;
             String password = MailService.this.password;
             return new PasswordAuthentication(username, password);
         }
     }
 
     public void sendHelloWorld(String receiver) {
         Map<String, Object> params = new HashMap<>();
         params.put("receiver", receiver);
         final String mail  = mailManager.getMail("/helloWorld.ftl", params);
         PendingMail pendingMail = new PendingMail(receiver, "Hello!", mail);
         pendingMailRepository.save(pendingMail);
     }
 
     public void sendRegistrationSuccessful(String email, String firstName, String lastName) {
         Map<String, Object> params = new HashMap<>();
         params.put("firstName", firstName);
         params.put("lastName", lastName);
         params.put("domain", domain);
         final String mail = mailManager.getMail("/registrationSuccess.ftl", params);
         PendingMail pendingMail = new PendingMail(email, "Регистрация завершена", mail);
         pendingMailRepository.save(pendingMail);
     }
 
 }
