 package smartpool.service;
 
 import org.springframework.stereotype.Service;
 import smartpool.domain.Mail;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 import java.util.List;
 import java.util.Properties;
 
 @Service
 public class MailService {
 
     private Mail mail;
 
    private MailService() {
         this.mail = new Mail(new MimeMessage(getSession()));
     }
 
     public MailService(Mail mail) {
         this.mail = mail;
     }
 
     public void sendMailToList(List<String> emailList, String subject, String messageBody) {
         for(String emailTo : emailList) {
             sendMailTo(emailTo, subject, messageBody);
         }
     }
 
     public void sendMailTo(String emailTo, String subject, String messageBody) {
         try {
             Transport.send(mail.compose(emailTo, subject, messageBody));
         } catch (MessagingException e) {
             e.printStackTrace();
         }
     }
 
     private Session getSession() {
         Properties props = new Properties();
         props.put("mail.smtp.auth", "false");
         props.put("mail.smtp.starttls.enable", "false");
         props.put("mail.smtp.host", "mother.thoughtworks.com");
 
         return Session.getDefaultInstance(props);
     }
 }
