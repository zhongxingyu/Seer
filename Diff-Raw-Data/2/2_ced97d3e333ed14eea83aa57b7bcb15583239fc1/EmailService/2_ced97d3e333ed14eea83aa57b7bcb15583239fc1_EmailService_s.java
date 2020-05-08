 package com.lavida.job.service;
 
 import com.lavida.service.settings.Settings;
 import com.lavida.service.settings.SettingsService;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Resource;
 
 /**
  * Created: 13:44 11.08.13
  * The EmailSender is the service for sending e-mail messages.
  *
  * @author Ruslan
  */
 @Service
 public class EmailService {
 
     @Resource
     private JavaMailSenderImpl mailSender;
 
    @Resource(name = "customeMailMessage")
     private SimpleMailMessage simpleMailMessage;
 
     @Resource
     private SettingsService settingsService;
 
     /**
      * Sends e-mail message.
      *
      * @param from    String expression of a sender e-mail
      * @param to      String expression of a receiver e-mail
      * @param subject String expression for the subject of an e-mail
      * @param content String expression for the content of an e-mail
      * @throws org.springframework.mail.MailParseException          in case of failure when parsing the message
      * @throws org.springframework.mail.MailAuthenticationException in case of authentication failure
      * @throws org.springframework.mail.MailSendException           in case of failure when sending the message
      */
     public void sendMail(String from, String to, String subject, String content) {
         Settings settings = settingsService.getSettings();
 
         SimpleMailMessage message = new SimpleMailMessage(simpleMailMessage);
         message.setFrom(from);
         message.setTo(to);
         message.setSubject(subject);
         message.setText(content);
         mailSender.setUsername(settings.getEmail());
         mailSender.setPassword(settings.getEmailPass());
         mailSender.send(message);
     }
 }
