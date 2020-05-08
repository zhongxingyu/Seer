 /*
  * beetlejuice
  * beetlejuice-email
  * Copyright (C) 2011-2012 art of coding UG, http://www.art-of-coding.eu/
  *
  * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
  * All rights reserved. Use is subject to license terms.
  *
  * rbe, 24.11.12 12:42
  */
 
 package eu.artofcoding.beetlejuice.email;
 
 import eu.artofcoding.beetlejuice.api.BeetlejuiceConstant;
 import eu.artofcoding.beetlejuice.entity.Attachment;
 import eu.artofcoding.beetlejuice.entity.Email;
 import eu.artofcoding.beetlejuice.entity.MimeType;
 
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import java.sql.Timestamp;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import static eu.artofcoding.beetlejuice.api.MimeTypeConstants.S_UTF8;
 
 public class PostmanImpl implements Postman {
 
     private static final Logger logger = Logger.getLogger(PostmanImpl.class.getName());
 
     private transient Session session;
 
    /**
     * Support Postman#addPart.
     */
    private transient Multipart multipart;

     private String server;
 
     private int port;
 
     private MailAuth mailAuth;
 
     public PostmanImpl(Session session, MailAuth mailAuth) {
         this.session = session;
         this.mailAuth = mailAuth;
     }
 
     //<editor-fold desc="Server, port, ...">
 
     public String getServer() {
         return server;
     }
 
     public int getPort() {
         return port;
     }
 
     //</editor-fold>
 
     //<editor-fold desc="Authentication">
 
     public void setUsername(String username) {
         mailAuth.setUsername(username);
     }
 
     public void setPassword(String password) {
         mailAuth.setPassword(password);
     }
 
     public void setMailAuth(MailAuth mailAuth) {
         this.mailAuth = mailAuth;
     }
 
     //</editor-fold>
 
     @Override
     public Session getSession() {
         return session;
     }
 
     @Override
     public void setSession(Session session) {
         this.session = session;
     }
 
     //<editor-fold desc="Message">
 
    private Multipart getMultipart() {
        if (null == multipart) {
            multipart = new MimeMultipart();
        }
        return multipart;
    }

     @Override
     public void addPart(Object content, MimeType mimeType, String filename) throws MessagingException {
         MimeBodyPart mimeBodyPart = new MimeBodyPart();
         // Check filename
         if (null != filename) {
             mimeBodyPart.setFileName(filename);
         }
         // Add content
         if (mimeType == MimeType.HTML) {
             mimeBodyPart.setContent(content, String.format("%s; charset=%s", mimeType.getMimeType(), S_UTF8));
        } else if (null != mimeType) /*if (mimeType == MimeType.PLAIN)*/ {
             mimeBodyPart.setContent(content, mimeType.getMimeType());
         } else {
             mimeBodyPart.setContent(content, MimeType.UNKNOWN.getMimeType());
         }
         // Add part to message
        getMultipart().addBodyPart(mimeBodyPart);
     }
 
     @Override
     public void setBody(Object content, MimeType mimeType) throws MessagingException {
         addPart(content, mimeType, null);
     }
 
     @Override
     public MimeMessage createMimeMessage(Email email) throws MessagingException {
         // Check state
         if (null == session) {
             throw new PostmanRuntimeException("No session");
         }
         // Create message
         MimeMessage m = new MimeMessage(session);
         // From
         Address from = new InternetAddress(email.getFromAddress());
         m.setFrom(from);
         // To
         for (String toAddress : email.getToAddress().split(BeetlejuiceConstant.COMMA)) {
             m.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
         }
         // Subject
         m.setSubject(email.getSubject());
         m.setSentDate(new java.util.Date());
         // Set content and MIME type
         addPart(email.getBody(), email.getMimeType(), null);
         // Add attachments
         if (null != email.getAttachments()) {
             for (Attachment attachment : email.getAttachments()) {
                 addPart(attachment.getContent(), attachment.getMimeType(), attachment.getFilename());
             }
         }
         // Set content of messages
         m.setContent(multipart);
         // Save changes
         m.saveChanges();
         return m;
     }
 
     @Override
     public MimeMessage createMimeMessage(String fromAddress, String[] toAddress, String subject, Object content, MimeType mimeType) throws MessagingException {
         // Check state
         if (null == session) {
             throw new PostmanRuntimeException("No session");
         }
         // Create message
         MimeMessage m = new MimeMessage(session);
         // From
         Address from = new InternetAddress(fromAddress);
         m.setFrom(from);
         // To
         for (String to : toAddress) {
             m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         }
         // Subject
         m.setSubject(subject);
         m.setSentDate(new java.util.Date());
         // Set content and MIME type
         addPart(content, mimeType, null);
         // Save changes
         m.saveChanges();
         return m;
     }
 
     @Override
     public MimeMessage createMimeMessage(String fromAddress, String toAddress, String subject, Object content, MimeType mimeType) throws MessagingException {
         return createMimeMessage(fromAddress, toAddress.split(BeetlejuiceConstant.COMMA), subject, content, mimeType);
     }
 
     //</editor-fold>
 
     //<editor-fold desc="Send email">
 
     @Override
     public void sendMail(Email email) throws MessagingException {
         // Check state
         if (null == session) {
             throw new PostmanRuntimeException("No session");
         }
         // Create message
         MimeMessage m = createMimeMessage(email);
         // Set header
         m.setHeader("X-Mailer", "beetlejuice Postman");
         // Send mail
         Transport.send(m);
         // Set successfully-sent-flag
         email.setSentSuccessfully(true);
         email.setSentDate(new Timestamp(m.getSentDate().getTime()));
        // Reset state
        multipart = null;
     }
 
     @Override
     public void sendMail(String fromAddress, Set<String> recipient, String subject, String body, MimeType contentType) throws MessagingException {
         // Create Email entity instance and populate it with data
         Email email = new Email();
         email.setFromAddress(fromAddress);
         StringBuilder toAddressBuilder = new StringBuilder();
         for (Iterator<String> iterator = recipient.iterator(); iterator.hasNext(); ) {
             String to = iterator.next();
             toAddressBuilder.append(to);
             if (iterator.hasNext()) {
                 toAddressBuilder.append(BeetlejuiceConstant.COMMA);
             }
         }
         email.setToAddress(toAddressBuilder.toString());
         email.setSubject(subject);
         email.setBody(body);
         email.setMimeType(contentType);
         // Set object in message and sendMail it
         sendMail(email);
     }
 
     @Override
     public void sendPlainTextMail(String fromAddress, Set<String> recipient, String subject, String body) throws MessagingException {
         sendMail(fromAddress, recipient, subject, body, MimeType.PLAIN);
     }
 
     @Override
     public void sendHtmlMail(String fromAddress, Set<String> recipient, String subject, String body) throws MessagingException {
         sendMail(fromAddress, recipient, subject, body, /*"text/html; charset=UTF-8"*/MimeType.HTML);
     }
 
     @Override
     public void sendExceptionMail(String fromAddress, Set<String> recipient, String subject, String text, Throwable throwable) throws MessagingException {
         // Exception
         StringBuilder builder = new StringBuilder();
         if (null != throwable) {
             builder.append(throwable.getMessage()).append("\n");
             for (StackTraceElement elt : throwable.getStackTrace()) {
                 builder.append("    at ").append(elt.getClassName()).append(".").append(elt.getMethodName());
                 builder.append("(").append(elt.getFileName()).append(":").append(elt.getLineNumber()).append("): ");
                 builder.append("\n");
             }
         }
         String body = String.format("%s%n%nException:%n%s", text, builder.toString());
         sendMail(fromAddress, recipient, subject, body, MimeType.HTML);
     }

     //</editor-fold>
 
 }
