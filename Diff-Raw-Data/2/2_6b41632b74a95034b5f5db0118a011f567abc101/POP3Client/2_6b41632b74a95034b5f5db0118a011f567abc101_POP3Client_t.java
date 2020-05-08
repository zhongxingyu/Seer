 /****************************************************************
  * Licensed to the Apache Software Foundation (ASF) under one   *
  * or more contributor license agreements.  See the NOTICE file *
  * distributed with this work for additional information        *
  * regarding copyright ownership.  The ASF licenses this file   *
  * to you under the Apache License, Version 2.0 (the            *
  * "License"); you may not use this file except in compliance   *
  * with the License.  You may obtain a copy of the License at   *
  *                                                              *
  *   http://www.apache.org/licenses/LICENSE-2.0                 *
  *                                                              *
  * Unless required by applicable law or agreed to in writing,   *
  * software distributed under the License is distributed on an  *
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
  * KIND, either express or implied.  See the License for the    *
  * specific language governing permissions and limitations      *
  * under the License.                                           *
  ****************************************************************/
 
 
 package org.apache.james.postage.client;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.net.pop3.POP3MessageInfo;
 import org.apache.james.fetchmail.ReaderInputStream;
 import org.apache.james.postage.PostageException;
 import org.apache.james.postage.SamplingException;
 import org.apache.james.postage.StartupException;
 import org.apache.james.postage.execution.Sampler;
 import org.apache.james.postage.mail.HeaderConstants;
 import org.apache.james.postage.result.MailProcessingRecord;
 import org.apache.james.postage.result.PostageRunnerResult;
 import org.apache.james.postage.user.UserList;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.Iterator;
 import java.util.List;
 
 public class POP3Client implements Sampler {
 
     private static Log log = LogFactory.getLog(POP3Client.class);
 
     private String m_host;
     private int m_port;
     private UserList m_internalUsers;
     private PostageRunnerResult m_results;
 
     public POP3Client(String host, int port, UserList internalUsers, PostageRunnerResult results) {
         m_host = host;
         m_port = port;
         m_internalUsers = internalUsers;
         m_results = results;
     }
 
     public boolean checkAvailability() throws StartupException {
         try {
             org.apache.commons.net.pop3.POP3Client pop3Client = openConnection(m_internalUsers.getRandomUsername());
             closeSession(pop3Client);
         } catch (PostageException e) {
             throw new StartupException("error checking availability");
         }
         return true;
     }
 
     private void closeSession(org.apache.commons.net.pop3.POP3Client pop3Client) throws PostageException {
         try {
             pop3Client.sendCommand("QUIT");
             pop3Client.disconnect();
         } catch (IOException e) {
             throw new PostageException("error closing pop3 session", e);
         }
     }
 
     private org.apache.commons.net.pop3.POP3Client openConnection(String username) throws PostageException {
         org.apache.commons.net.pop3.POP3Client pop3Client = new org.apache.commons.net.pop3.POP3Client();
         try {
             pop3Client.connect(m_host, m_port);
             pop3Client.login(username, m_internalUsers.getPassword());
         } catch (IOException e) {
             throw new PostageException("POP3 service not available", e);
         }
         return pop3Client;
     }
 
     public void doSample() throws SamplingException {
         String username = m_internalUsers.getRandomUsername();
 
         try {
             findAllMatchingTestMail(username);
         } catch (SamplingException e) {
             log.warn("error sampling mail for user " + username);
             throw e;
         }
     }
 
     public void doMatchMailForAllUsers() {
         Iterator usernames = m_internalUsers.getUsernames();
         while (usernames.hasNext()) {
             String username = (String)usernames.next();
             try {
                 findAllMatchingTestMail(username);
             } catch (SamplingException e) {
                 log.warn("error reading mail for user " + username);
             }
         }
     }
 
     private void findAllMatchingTestMail(String username) throws SamplingException {
         try {
             org.apache.commons.net.pop3.POP3Client pop3Client = openConnection(username);
 
 
 
             // retrieve all messages
             POP3MessageInfo[] entries = null;
             try {
                 entries = pop3Client.listMessages();
             } catch (Exception e) {
                 String errorMessage = "failed to read pop3 account mail list for " + username;
                 m_results.addError(500, errorMessage);
                 log.info(errorMessage);
                 return;
             }
 
            for (int i = 0; entries != null && i < entries.length; i++) {
 // TODO do we need to check the state?                assertEquals(1, pop3Client.getState());
                 POP3MessageInfo entry = entries[i];
                 int size = entry.size;
 
                 MailProcessingRecord mailProcessingRecord = new MailProcessingRecord();
                 mailProcessingRecord.setReceivingQueue("pop3");
                 mailProcessingRecord.setTimeFetchStart(System.currentTimeMillis());
 
                 String mailText;
                 BufferedReader mailReader = null;
                 Reader reader = null;
                 try {
                     reader = pop3Client.retrieveMessage(entry.number);
                     mailReader = new BufferedReader(reader);
 
                     analyzeAndMatch(mailReader, mailProcessingRecord, pop3Client, i);
                 } catch (Exception e) {
                     log.info("failed to read pop3 mail #" + i + " for " + username);
                     continue; // mail processing record is discarded
                 } finally {
                     if (reader != null) {
                         try {
                             reader.close();
                         } catch (IOException e) {
                             log.warn("error closing mail reader");
                         }
                     }
                 }
 
                 continue;
             }
 
             closeSession(pop3Client);
         } catch (PostageException e) {
             throw new SamplingException("sample failed", e);
         }
     }
 
     private void analyzeAndMatch(Reader mailReader, MailProcessingRecord mailProcessingRecord, org.apache.commons.net.pop3.POP3Client pop3Client, int i) {
 
         InputStream in = new ReaderInputStream(mailReader);
         MimeMessage message;
         String id = null;
         try {
             message = new MimeMessage(null, in);
             in.close();
 
             String[] idHeaders = message.getHeader(HeaderConstants.MAIL_ID_HEADER);
             if (idHeaders != null && idHeaders.length > 0) {
                 id = idHeaders[0]; // there should be exactly one.
             }
             if (id == null) {
                 log.info("skipping non-postage mail. remains on server. subject was: " + message.getSubject());
                 return;
             }
         } catch (IOException e) {
             log.info("failed to close mail reader.");
             return;
         } catch (MessagingException e) {
             log.info("failed to process mail. remains on server");
             return;
         }
 
         try {
             mailProcessingRecord.setByteReceivedTotal(message.getSize());
 
             mailProcessingRecord.setMailId(id);
             mailProcessingRecord.setSubject(message.getSubject());
 
             MimeMultipart mimeMultipart = new MimeMultipart(message.getDataHandler().getDataSource());
 
             // this assumes, there is zero or one part each, either of type plaintext or binary
             mailProcessingRecord.setByteReceivedText(getPartSize(mimeMultipart, "text/plain"));
             mailProcessingRecord.setByteReceivedBinary(getPartSize(mimeMultipart, "application/octet-stream"));
 
             mailProcessingRecord.setTimeFetchEnd(System.currentTimeMillis());
 
             try {
                 pop3Client.deleteMessage(i + 1); // don't retrieve again next time
             } catch (Exception e) {
                 log.info("failed to delete mail.");
                 return;
             }
         } catch (MessagingException e) {
             log.info("failed to process mail. remains on server");
             return;
         } finally {
             matchMails(mailProcessingRecord);
         }
     }
 
     private void matchMails(MailProcessingRecord mailProcessingRecord) {
         boolean matched = m_results.matchMailRecord(mailProcessingRecord);
         if (!matched) {
             String oldMailId = mailProcessingRecord.getMailId();
             String newMailId = MailProcessingRecord.getNextId();
             mailProcessingRecord.setMailId(newMailId);
             log.info("changed mail id from " + oldMailId + " to " + newMailId);
             m_results.addNewMailRecord(mailProcessingRecord);
         }
     }
 
     private int getPartSize(MimeMultipart parts, String mimeType) {
         if (parts != null) {
             try {
                 for (int i = 0; i < parts.getCount(); i++) {
                     BodyPart bodyPart = parts.getBodyPart(i);
                     if (mimeType.equals(bodyPart.getContentType())) {
                         return bodyPart.getSize();
                     }
                 }
             } catch (MessagingException e) {
                 log.info("failed to process body parts.", e);
             }
         }
         return 0;
     }
 }
 
