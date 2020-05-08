 package br.com.igordeoliveirasa.charliemail;
 
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 /**
  *
  * @author igor.sa
  */
 public class SimpleSMTP implements ISMTP {  
   
     private String smtpHost;
     private String username;
     private String password;
     Properties props;
     
     public SimpleSMTP(String smtpHost, String username, String password) {
         this.smtpHost = smtpHost;
         this.username = username;
         this.password = password;
         
         this.props = new Properties();
         this.props.put("mail.smtp.auth", "true");
         this.props.put("mail.smtp.starttls.enable", "true");
         this.props.put("mail.smtp.host", this.smtpHost);
         this.props.put("mail.smtp.port", "587");
     }
     
     @Override
     public boolean sendTextMail(String from, String to, String cc, String bcc, String subject, String message) {
         return sendTextMail(from, new String[]{to}, new String[]{cc}, new String[]{bcc}, subject, message);
     }
     
     @Override
     public boolean sendTextMail(String from, String[] toArray, String[] ccArray, String[] bccArray, String subject, String message) {
         
         Session session = Session.getInstance(this.props,
           new javax.mail.Authenticator() {
               @Override
                 protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication(username, password);
                 }
           });
 
         try {
 
             MimeMessage mimeMessage = new MimeMessage(session);
             mimeMessage.setFrom(new InternetAddress(from));
 
             if (toArray==null||toArray.length==0) {
                 return false;
             }
             
             InternetAddress[] address = new InternetAddress[toArray.length];
             for (int i=0; i<toArray.length; i++) {
                 mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toArray[i]));
             }
 
             if (ccArray!=null&&ccArray.length>0) {
                 address = new InternetAddress[ccArray.length];
                 for (int i=0; i<ccArray.length; i++) {
                     mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(ccArray[i]));
                 }
             }
             
             if (bccArray!=null&&bccArray.length>0) {
                 address = new InternetAddress[bccArray.length];
                 for (int i=0; i<bccArray.length; i++) {
                     mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccArray[i]));
                 }
             }
 
             mimeMessage.setSubject(subject, "UTF-8");
             mimeMessage.setText(message, "UTF-8");
             
             Transport.send(mimeMessage);
         } catch (MessagingException e) {
                 //throw new RuntimeException(e);
                 return false;
         }
         return true;
     }  
 
     @Override
     public boolean sendHTMLMail(String from, String to, String cc, String bcc, String subject, String htmlMessage) {
        return sendTextMail(from, new String[]{to}, new String[]{cc}, new String[]{bcc}, subject, htmlMessage);
     }
     
     @Override
     public boolean sendHTMLMail(String from, String[] toArray, String[] ccArray, String[] bccArray, String subject, String htmlMessage) {
         
         Session session = Session.getInstance(this.props,
           new javax.mail.Authenticator() {
               @Override
                 protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication(username, password);
                 }
           });
 
         try {
 
             MimeMessage mimeMessage = new MimeMessage(session);
             mimeMessage.setFrom(new InternetAddress(from));
 
             if (toArray==null||toArray.length==0) {
                 return false;
             }
             
             InternetAddress[] address = new InternetAddress[toArray.length];
             for (int i=0; i<toArray.length; i++) {
                 mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toArray[i]));
             }
 
             if (ccArray!=null&&ccArray.length>0) {
                 address = new InternetAddress[ccArray.length];
                 for (int i=0; i<ccArray.length; i++) {
                     mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(ccArray[i]));
                 }
             }
             
             if (bccArray!=null&&bccArray.length>0) {
                 address = new InternetAddress[bccArray.length];
                 for (int i=0; i<bccArray.length; i++) {
                     mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccArray[i]));
                 }
             }
 
             mimeMessage.setSubject(subject, "UTF-8");
             mimeMessage.setContent(htmlMessage, "text/html");
             
             Transport.send(mimeMessage);
         } catch (MessagingException e) {
                 //throw new RuntimeException(e);
                 return false;
         }
         return true;
     }  
 }
