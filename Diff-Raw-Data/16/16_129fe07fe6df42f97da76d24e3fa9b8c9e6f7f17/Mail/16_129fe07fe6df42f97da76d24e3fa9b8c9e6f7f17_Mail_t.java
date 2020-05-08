 package com.mymed.utils;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 public class Mail {
 
   private final Properties props;
   private final String from, to, object, content;
 
   public Mail(final String from, final String to, final String object, final String content) {
     props = new Properties();
     props.put("mail.smtp.host", "smtp.gmail.com");
     props.put("mail.smtp.socketFactory.port", "465");
     props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
     props.put("mail.smtp.auth", "true");
     props.put("mail.smtp.port", "465");
 
     this.from = from;
     this.to = to;
     this.object = object;
     this.content = content;
 
     sendMail();
   }
 
   private void sendMail() {
     final Session session = Session.getInstance(props, new javax.mail.Authenticator() {
       @Override
       protected PasswordAuthentication getPasswordAuthentication() {
         return new PasswordAuthentication("mymed.subscribe", "myalcotra");
       }
     });
 
     try {
 
       final Message message = new MimeMessage(session);
       message.setFrom(new InternetAddress(from));
       message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
       message.setSubject(object);
       message.setText(content);
 
       Transport.send(message);
 
      MLogger.getLogger().info("Email sent!");
 
     } catch (final MessagingException e) {
       throw new RuntimeException(e);
     }
   }
 }
