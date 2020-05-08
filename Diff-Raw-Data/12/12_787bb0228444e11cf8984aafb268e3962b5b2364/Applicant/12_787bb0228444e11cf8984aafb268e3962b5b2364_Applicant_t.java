 package com.jclarity.had_one_dismissal.domain;
 
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.web.client.RestTemplate;
 
 import com.jclarity.had_one_dismissal.auth.RestfulAuthProvider;
 import com.jclarity.had_one_dismissal.auth.domain.WebUser;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 public class Applicant {
 
     @NotNull
     @Size(min = 2)
     private String forename;
 
     @NotNull
     @Size(min = 2)
     private String surname;
 
     private int yearsExperience;
 
     @Transient
     private transient int userId = -1;
 
     @Autowired
     private transient RestTemplate restTemplate;
 
     @Autowired
     private transient MailSender email;
    
    // TODO: re-add once I've worked out how to avoid spring shitting itself.
    //@RooUploadedFile(contentType = "application/pdf")
    //@Lob
    //private byte[] cv;
 
     public void setForename(String name) {
         forename = name;
         updateId();
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
         updateId();
     }
 
     private void updateId() {
         if (forename != null && surname != null) {
             WebUser user = RestfulAuthProvider.getUser(forename + "." + surname, restTemplate);
 
             if (user != null) {
                 this.userId = user.getId();
             }
         }
     }
 
     public void sendMessage(String mailFrom, String subject, String mailTo, String message) {
         SimpleMailMessage mailMessage = new SimpleMailMessage();
         mailMessage.setFrom(mailFrom);
         mailMessage.setSubject(subject);
         mailMessage.setTo(mailTo);
         mailMessage.setText(message);
         email.send(mailMessage);
     }
 }
