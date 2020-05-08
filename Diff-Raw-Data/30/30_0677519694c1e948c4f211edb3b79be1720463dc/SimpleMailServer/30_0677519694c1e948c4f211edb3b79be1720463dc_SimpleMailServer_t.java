 /***********************************************************************
  * Copyright (c) 2006 The Apache Software Foundation.                  *
  * All rights reserved.                                                *
  * ------------------------------------------------------------------- *
  * Licensed under the Apache License, Version 2.0 (the "License"); you *
  * may not use this file except in compliance with the License. You    *
  * may obtain a copy of the License at:                                *
  *                                                                     *
  *     http://www.apache.org/licenses/LICENSE-2.0                      *
  *                                                                     *
  * Unless required by applicable law or agreed to in writing, software *
  * distributed under the License is distributed on an "AS IS" BASIS,   *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
  * implied.  See the License for the specific language governing       *
  * permissions and limitations under the License.                      *
  ***********************************************************************/
 
 
 package org.apache.james.postage.smtpserver;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.james.postage.result.MailProcessingRecord;
 import org.apache.james.postage.result.PostageRunnerResult;
 import org.apache.james.postage.mail.HeaderConstants;
 import org.apache.james.services.MailRepository;
 import org.apache.james.services.MailServer;
 import org.apache.mailet.Mail;
 import org.apache.mailet.MailAddress;
 
 import javax.mail.Address;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashSet;
 
 
 /**
  * a quite simple (only receiving) SMTP server which reads mails and tries to match them with sent test mails.
  * reuses James' own SMTP server components  
  */
 public class SimpleMailServer implements MailServer {
 
     private static Log log = LogFactory.getLog(SimpleMailServer.class);
 
     private int m_counter = 0;
     private PostageRunnerResult m_results;
 
     public void sendMail(MailAddress sender, Collection recipients, MimeMessage msg) throws MessagingException {
        //log.info("start processing incoming mail having id = " + msg.getMessageID());
         MailProcessingRecord mailProcessingRecord = new MailProcessingRecord();
         mailProcessingRecord.setReceivingQueue("smtpOutbound");
         mailProcessingRecord.setTimeFetchStart(System.currentTimeMillis());
         mailProcessingRecord.setByteReceivedTotal(msg.getSize());
 
         try {
             String[] id = msg.getHeader(HeaderConstants.MAIL_ID_HEADER);
             if (id == null || id.length == 0 ) {
                 log.info("skipping non-postage mail. message is lost. subject was: " + msg.getSubject());
                 return;
             }
             mailProcessingRecord.setMailId(id[0]);
 
             String[] subjectHeader = msg.getHeader("Subject");
             if (subjectHeader != null && subjectHeader.length > 0) {
                 mailProcessingRecord.setSubject(subjectHeader[0]);
             }
 
             // TODO mailProcessingRecord.setByteReceivedText();
             // TODO mailProcessingRecord.setByteReceivedBinary();
             
             mailProcessingRecord.setTimeFetchEnd(System.currentTimeMillis());
        } catch(MessagingException e) {
            log.error("error processing incoming mail: " + e.getMessage());
            throw e; // rethrow after logging 
         } finally{
             boolean matched = m_results.matchMailRecord(mailProcessingRecord);
             if (!matched) {
                 if (mailProcessingRecord.getMailId() == null) mailProcessingRecord.setMailId(MailProcessingRecord.getNextId());
                 m_results.addNewMailRecord(mailProcessingRecord);
             }
         }
     }
 
     public void sendMail(MailAddress sender, Collection recipients, InputStream msg) throws MessagingException {
         //Object[] mailObjects = new Object[]{sender, recipients, msg};
         throw new IllegalStateException("not supported");
     }
 
     public void sendMail(Mail mail) throws MessagingException {
         sendMail(mail.getSender(), mail.getRecipients(), mail.getMessage());
     }
 
     public void sendMail(MimeMessage message) throws MessagingException {
         // taken from class org.apache.james.James 
         MailAddress sender = new MailAddress((InternetAddress)message.getFrom()[0]);
         Collection recipients = new HashSet();
         Address addresses[] = message.getAllRecipients();
         if (addresses != null) {
             for (int i = 0; i < addresses.length; i++) {
                 // Javamail treats the "newsgroups:" header field as a
                 // recipient, so we want to filter those out.
                 if ( addresses[i] instanceof InternetAddress ) {
                     recipients.add(new MailAddress((InternetAddress)addresses[i]));
                 }
             }
         }
         sendMail(sender, recipients, message);
     }
 
     public MailRepository getUserInbox(String userName) {
         throw new IllegalStateException("not implemented");
     }
 
     public synchronized String getId() {
         m_counter++;
         return "SimpleMailServer-ID-" + m_counter;
     }
 
     public boolean addUser(String userName, String password) {
         throw new IllegalStateException("not implemented");
     }
 
     public boolean isLocalServer(String serverName) {
         return true;
     }
 
     public void setResults(PostageRunnerResult results) {
         m_results = results;
     }
 }
 
