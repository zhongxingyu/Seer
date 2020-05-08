 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2011, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 17th January 2011
  */
 package au.edu.uts.eng.remotelabs.schedserver.messenger.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.messenger.Message;
 
 /**
  * Messenger sender that sends messages using email.
  * 
  * TODO Implement SMTP mail server authentication
  * TODO Implement Attachment sending
  */
 public class SMTPSender
 {
     /** The from address of system emails. */
     private Session session;
     
     /** Administrator email address. */
     private List<String> adminEmails;
     
     /** Logger. */
     private Logger logger;
     
     public SMTPSender()
     {
         this.logger = LoggerActivator.getLogger();
         
         this.adminEmails = new ArrayList<String>();
     }
     
     public void init(Config config)
     {
         /* Load administrator email. */
         String tmp = config.getProperty("Admin_Email");
         String emails[] = tmp.split(";");
         for (String e : emails)
         {
             e = e.trim();
             this.logger.info("Loaded administrator email '" + e + "' to send notifications to.");
             this.adminEmails.add(e);
         }
         
         /* Load mail session properties. */
         Properties props = new Properties();
         if ((tmp = config.getProperty("From_Address")) != null)
         {
             tmp = tmp.trim();
            props.put("mail.from", tmp);
             this.logger.info("Loaded system generated email address as '" + tmp + "'.");
         }
         
         if ((tmp = config.getProperty("SMTP_Host")) != null)
         {
             tmp = tmp.trim();
             props.put("mail.smtp.host", tmp);
             this.logger.info("Loaded SMTP mail server address as '" + tmp + "'.");
         }
         
         if ((tmp = config.getProperty("SMTP_Port")) != null)
         {
             tmp = tmp.trim();
             props.put("mail.smtp.port", tmp);
             this.logger.info("Loaded SMTP mail server port as '" + tmp + "'.");
         }
         
         this.session = Session.getDefaultInstance(props);
     }
     
     /**
      * Sends the message.
      * 
      * @param message message to send
      */
     public void send(Message message)
     {
         List<String> addresses =  new ArrayList<String>(message.getRecipients().size());
         for (User user : message.getRecipients())
         {
             if (user.getEmail() == null)
             {
                 this.logger.debug("User " + user.qName() + " not getting message with subject '" + message.getSubject() + 
                         " sent to them because they have no stored email address.");
             }
             else
             {
                 addresses.add(user.getEmail());
             }
         }
         
         if (addresses.size() > 0) this.smtpSend(addresses, message);
         
     }
     
     /** 
      * Sends message to configured administrator.
      * 
      * @param message message to send
      */
     public void sendAdmin(Message message)
     {
         this.smtpSend(this.adminEmails, message);
     }
     
     /**
      * Sends the message to the addresses.
      * 
      * @param addresses recipients
      * @param message message to send
      */
     private void smtpSend(List<String> addresses, Message message)
     {
         if (this.session == null) return;
         
         try 
         {
             MimeMessage mime = new MimeMessage(this.session);
             mime.setFrom();
             
             for (String a : addresses)
             {
                 mime.addRecipients(javax.mail.Message.RecipientType.TO, a);
             }
             
             mime.setSubject(message.getSubject());
             mime.setSentDate(new Date());
             mime.setText(message.getBody());
             Transport.send(mime);
             
             this.logger.debug("Send email with subject '" + message.getSubject() + "' to " + addresses + ".");
         } 
         catch (MessagingException ex) 
         {
             this.logger.error("Failed to send email with subject '" + message.getSubject() + "' to " + addresses + 
                     "because exception: " + ex.getClass().getSimpleName() + ", with message: " + ex.getMessage() + '.'); 
         }
     }
 }
