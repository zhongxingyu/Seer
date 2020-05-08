 package com.google.wave.extensions.emaily.email;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 
 import com.google.inject.Singleton;
 
 @Singleton
 public class EmailSender {
   /**
    * Send simple text email.
    * 
    * @param from The recipient email address. Currently it is ignored and
    *   used only as a string representation of the email address.
    * @param recipients List of recipients.
    * @param subject The subject of the email.
    * @param body The body of the email.
    */
   public void simpleSendTextEmail(String from, List<String> recipients,
       String subject, String body) {
     // Initialize JavaMail
     Properties props = new Properties();
     Session session = Session.getDefaultInstance(props, null);
 
     // Build message
     Message msg = new MimeMessage(session);
     try {
       // TODO(dlux): Find out why I cannot send from emaily-wave@appspot.com
       // As a temporary solution, I created a gmail acc for the robot which
       // I can send emails from.
       msg.setFrom(new InternetAddress("emaily.wave.robot@gmail.com", from));
       for (String recipient : recipients) {
         msg.addRecipient(RecipientType.TO, new InternetAddress(recipient));
       }
       msg.setSubject(subject);
       msg.setText(body);
       Transport.send(msg);
     } catch (MessagingException e) {
       throw new RuntimeException("Cannot send email", e);
     } catch (UnsupportedEncodingException e) {
       throw new RuntimeException("Invalid recipients in email", e);
     }
   }
 }
