 package no.uib.ii.deliverandum.service.mail;
 
 import java.io.File;
 
 import javax.mail.MessagingException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Service;
 
 @Service
 public class MailService {
 	
     @Autowired private JavaMailSender mailSender;
     @Value("${email.from}") private String fromEmail;
     
    public void sendMail(String to, String subject, String body, File... attachments) {
         try {
             MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage(), true, "UTF-8");
             message.setFrom(fromEmail);
             message.setTo(to);
             message.setSubject(subject);
             message.setText(body);
             for (File f : attachments) {
                 message.addAttachment(f.getName(), f);
             }
             mailSender.send(message.getMimeMessage());
         } catch (MessagingException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }

 }
