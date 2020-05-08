 package org.codehaus.mojo.mail;
 
 /* 
  * Copyright 2011 Markus W Mahlberg 
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import java.io.File;
 import java.util.List;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * Goal which sends a mail to recipients 
  *
  * @goal mail 
  * @phase deploy
  */
 public class MailMojo extends AbstractMojo {
 
     // Sadly, one can't extend enums, so we have to wrap it ourselves
 
     private enum RecipientType {
         TO("To", Message.RecipientType.TO), 
         CC("CC", Message.RecipientType.CC), 
         BCC("BCC", Message.RecipientType.BCC);
 
         private final String description_;
         private final Message.RecipientType rectype_;
 
         private RecipientType(String desc, Message.RecipientType rt) {
             this.description_ = desc;
             this.rectype_ = rt;
         }
 
         String getDescription() {
             return this.description_;
         }
 
         Message.RecipientType getType() {
             return this.rectype_;
         }
     }
 
     /**
      * Sender
      * 
      * @parameter default-value="maven2@localhost"
      * @required
      */
     private String from;
 
     /**
     * To-Adresses
      * 
      * @parameter
      * @required
      */
     private List<String> recipients;
 
     /**
     * CC-Adresses
      * 
      * @parameter
      */
     private List<String> ccRecipients;
 
     /**
     * BCC-Adresses
      * 
      * @parameter
      */
     private List<String> bccRecipients;
 
     /**
      * Subject
      * 
      * @parameter expression="${mail.subject}"
      *            default-value="${project.name} deployed"
      */
     private String subject;
 
     /**
      * Body
      * 
      * @parameter expression="${mail.body}"
      *            default-value="${project.name} was successfully deployed"
      * @required
      */
     private String body;
 
     /**
     * Attachements
      * 
      * @parameter
      */
     private List<File> attachments;
 
     /**
      * SMTP Host
      * 
      * @parameter expression="${mail.smtp.host}" default-value="localhost"
      * @required
      */
     private String smtphost;
 
     /**
      * SMTP Port The port which will be used to connect to the SMTP server
      * 
      * @parameter expression="${mail.smtp.port}" default-value="25"
      */
     private Integer smtpport;
 
     /**
      * SMTP User
      * 
      * @parameter expression="${mail.smtp.user}"
      */
     private String smtpuser;
 
     /**
      * SMTP Password
      * 
      * @parameter expression="${mail.smtp.user}"
      */
     private String smtppassword;
 
     private MimeMessage message;
 
     private void addRecipents(List<String> recipients, RecipientType rt)
             throws MojoExecutionException {
 
         if (recipients == null) {
             return;
         }
 
         for (String recipient : recipients) {
             getLog().info(rt.getDescription() + ": " + recipients);
             try {
                 message.addRecipient(rt.getType(), new InternetAddress(recipient, true));
                 getLog().debug("Added " + rt.getType());
             } catch (AddressException e) {
                 throw new MojoExecutionException(
                         "Reason was an AddressException", e);
             } catch (MessagingException e) {
                 throw new MojoExecutionException(
                         "Reason was a MessagingException", e);
             } catch (Exception e) {
                 throw new MojoExecutionException("Unknown Reason", e);
             }
             
         }
     }
 
     private void addAttachements(MimeMultipart parent)
             throws MojoExecutionException {
         
         for (File attachment : attachments) {
 
             /*
              * Traversing directories is not yet implemented I strongly doubt
              * that it should be, since this is most likely going to produce
              * quite some mailtraffic
              */
             if (attachment.isDirectory()) {
                 throw new MojoExecutionException(
                         "The " + attachment.getAbsolutePath() + " is a directory which is not supported (future?)");
             }
 
             getLog().info("\t...\"" + attachment.getPath() + "\"");
 
             try {
                 parent.addBodyPart(addBodyPart(attachment));
             } catch (MessagingException e) {
                 getLog().error(
                         "Could not attach \"" + attachment.getName()
                                 + "\" to message");
                 throw new MojoExecutionException("Cought MessagingException", e);
             }
         }
     }
 
     private BodyPart addBodyPart(File attachment) throws MojoExecutionException {
         DataSource src = new FileDataSource(attachment);
         BodyPart msgPart = new MimeBodyPart();
         try {
             msgPart.setDataHandler(new DataHandler(src));
             msgPart.setFileName(attachment.getName());
 
             // Don't know whether using FileDataSource.getContentType() is
             // really a good idea,
             // but so far it works.
             msgPart.setHeader("Content-Type", src.getContentType());
             msgPart.setHeader("Content-ID", attachment.getName());
             msgPart.setDisposition(Part.ATTACHMENT);
         } catch (MessagingException e) {
             getLog().error(
                     "Could not create attachment from file \""
                             + attachment.getName() + "\"");
             throw new MojoExecutionException("Cought MessagingException", e);
         }
         return msgPart;
     }
 
     public void execute() throws MojoExecutionException {
 
         Properties props = new Properties();
         getLog().debug("Sending Mail via: " + smtphost);
         props.put("mail.smtp.host", smtphost);
         props.put("mail.smtp.port", smtpport);
         props.put("mail.smtp.starttls.enable", "true");
 
         Session s = Session.getInstance(props, null);
         message = new MimeMessage(s);
 
         try {
             message.setFrom(new InternetAddress(from));
         } catch (AddressException e) {
             throw new MojoExecutionException("Cought AddressException!", e);
         } catch (MessagingException e) {
             throw new MojoExecutionException("Cought MessagingException!", e);
         } catch (Exception e) {
             getLog().error("Could not set " + from + " as FromAddress");
             throw new MojoExecutionException(
                     "Something is SERIOUSLY going wrong: ", e);
         }
 
         getLog().info("recipients=" + recipients);
 
         getLog().info("Preparing mail from " + from + " via " + smtphost);
 
         addRecipents(recipients, RecipientType.TO);
 
         getLog().info("ccRecipients:" + ccRecipients);
         if (ccRecipients != null) {
             addRecipents(ccRecipients, RecipientType.CC);
         }
 
         if (bccRecipients != null) {
             addRecipents(bccRecipients, RecipientType.BCC);
         }
 
         BodyPart msgBodyPart = new MimeBodyPart();
         try {
             msgBodyPart.setContent(body, "text/plain; charset=utf-8");
             msgBodyPart.setDisposition(Part.INLINE);
         } catch (MessagingException e) {
             getLog().error("Could not set body to \"" + body + "\"");
             throw new MojoExecutionException("Cought MessagingException: ", e);
         }
 
         MimeMultipart multipart = new MimeMultipart();
 
         try {
             multipart.addBodyPart(msgBodyPart);
         } catch (MessagingException e) {
             getLog().error("Could not attach body to MultipartMessage");
             throw new MojoExecutionException("Cought MessagingException: ", e);
         }
         if (attachments != null) {
             getLog().info("Attaching...");
             this.addAttachements(multipart);
         } else {
             getLog().info("No attachments");
         }
 
         try {
             message.setSubject(subject, "UTF-8");
             message.setHeader("Content-Transfer-Encoding", "8bit");
             message.setContent(multipart);
             if (smtpuser != null && smtppassword != null) {
                 Transport transport = s.getTransport("smtp");
                 transport.connect(smtpuser, smtppassword);
             }
             Transport.send(message);
             getLog().info("Mail sucessfully sent");
         } catch (MessagingException e) {
             getLog().info("Cought MessagingException: " + e.toString());
             throw new MojoExecutionException(e.toString());
         }
     }
 }
