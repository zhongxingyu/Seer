 package com.madalla.service.email;
 
 import java.io.Serializable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 public class SimpleEmailSender implements IEmailSender, Serializable {
 	private static final long serialVersionUID = 1628736470390933607L;
 	private static final Log log = LogFactory.getLog(SimpleEmailSender.class);
     private SimpleEmail email;
     private String emailHost;
     private String emailFromName;
     private String emailFromEmail;
     private String emailToName;
     private String emailToEmail;
     private String mailAuthName;
     private String mailAuthPassword;
     
     
     public SimpleEmailSender(){
         email = new SimpleEmail();
     }
     
     public boolean sendEmail(String subject, String body){
         return sendEmailUsingCommonsMail(subject, body);
     }
     
     public boolean sendEmail(){
         return sendEmail("com.emalan.service.email.SimpleEmailSender - no subject", 
                 "com.emalan.service.email.SimpleEmailSender - no body");
     }
     
     private void init() throws EmailException {
         email.setHostName(emailHost);
         email.setAuthentication(mailAuthName, mailAuthPassword);
         email.addTo(emailToEmail, emailToName);
         email.setFrom(emailFromEmail, emailFromName);
         email.setDebug(true);
     }
     
     private boolean sendEmailUsingCommonsMail(String subject, String body){
         try {
             init();
             email.setSubject(subject);
             email.setMsg(body);
             log.debug("Sending email."+this);
             email.send();
         } catch (EmailException e) {
             log.error("Exception while sending email from emalancom.",e);
             log.warn("Email not sent:" + this);
             return false;
         }
         return true;
     }
     
     public void setEmailFromEmail(String emailFromEmail) {
         this.emailFromEmail = emailFromEmail;
     }
 
     public void setEmailFromName(String emailFromName) {
         this.emailFromName = emailFromName;
     }
 
     public void setEmailHost(String emailHost) {
         this.emailHost = emailHost;
     }
 
     public void setEmailToEmail(String emailToEmail) {
         this.emailToEmail = emailToEmail;
     }
 
     public void setEmailToName(String emailToName) {
         this.emailToName = emailToName;
     }

     public String toString() {
        StringBuffer sb = new StringBuffer("SimpleEmailSender").append(System.getProperty("line.separator"));
        sb.append("emailhost: "+emailHost).append(System.getProperty("line.separator"));
        sb.append("emailToEmail: "+emailToEmail).append(System.getProperty("line.separator"));
        sb.append("emailToName: "+emailToName).append(System.getProperty("line.separator"));
        sb.append("emailFromEmail: "+emailFromEmail).append(System.getProperty("line.separator"));
        sb.append("emailFromName: "+emailFromName).append(System.getProperty("line.separator"));
        sb.append("subject: "+email.getSubject());
        
        return sb.toString();
     }
 
     public void setMailAuthName(String mailAuthName) {
         this.mailAuthName = mailAuthName;
     }
 
     public void setMailAuthPassword(String mailAuthPassword) {
         this.mailAuthPassword = mailAuthPassword;
     }
 
 }
