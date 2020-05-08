 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: MailMessage.java 4496 2006-02-08 20:27:04Z wehrens $
  */
 package org.gridsphere.services.core.mail;
 
 /**
  * The <code>MailMessage</code> utility class represents a mail message.
  */
 public class MailMessage {
 
     private String subject = "";
     private String body = "";
     private String emailAddress = "";
     private String sender = "";
     private String recType = TO;
 
     public static String TO = "to";
     public static String BCC = "bcc";
 
     /**
      * Constructs an empty mail message
      */
     public MailMessage() {
     }
 
     /**
      * Constructs a mail message given a subject, sender, body and email address
      *
      * @param subject      the subject of the email
      * @param body         the body of the email
      * @param emailAddress the recipient to mail job info to
      * @param sender       the reply-to of the mail
      */
     public MailMessage(String subject, String body, String emailAddress, String sender) {
         this.subject = subject;
         this.body = body;
         this.emailAddress = emailAddress;
         this.sender = sender;
     }
 
     /**
      * Sets the email subject
      *
      * @param subject the email subject
      */
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     /**
      * Returns the email subject
      *
      * @return the email subject
      */
     public String getSubject() {
         return subject;
     }
 
     /**
      * Sets the email body
      *
      * @param body the email body
      */
     public void setBody(String body) {
         this.body = body;
     }
 
     /**
      * Returns the email body
      *
      * @return the email boody
      */
     public String getBody() {
         return body;
     }
 
     /**
      * Sets the email address
      *
      * @param emailAddress the email address
      */
     public void setEmailAddress(String emailAddress) {
         this.emailAddress = emailAddress;
     }
 
     /**
      * Returns the email address
      *
      * @return the email address
      */
     public String getEmailAddress() {
         return emailAddress;
     }
 
     /**
      * Sets the email sender
      *
      * @param sender the email sender
      */
     public void setSender(String sender) {
         this.sender = sender;
     }
 
     /**
      * Returns the email sender
      *
      * @return the email sender
      */
     public String getSender() {
         return sender;
     }
 
     /**
      * Return the recipient type either BCC or TO
      *
      * @return the recipient type
      */
     public String getRecipientType() {
         return recType;
     }
 
     /**
      * Sets the recipient type
      *
      * @param recType the recipient type either BCC or TO
      */
     public void setRecipientType(String recType) {
         this.recType = recType;
     }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("subject= ").append(subject);
        sb.append("emailAddress= ").append(emailAddress);
        sb.append("sender= ").append(sender);
        sb.append("body= ").append(body);
        return sb.toString();
    }
 }
