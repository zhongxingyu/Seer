 /* ********************************************************************
 Licensed to Jasig under one or more contributor license
 agreements. See the NOTICE file distributed with this work
 for additional information regarding copyright ownership.
 Jasig licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file
 except in compliance with the License. You may obtain a
 copy of the License at:
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on
 an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied. See the License for the
 specific language governing permissions and limitations
 under the License.
  */
 package org.bedework.eventreg.bus;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 /**
  * @author douglm
  *
  */
 public class Mailer {
   /**
    * @param recipients
    * @param subject
    * @param message
    * @param from
    * @param id
    * @throws MessagingException
    */
   public static void postMail( final String recipients[ ],
                                final String subject,
                                final String message ,
                                final String from,
                                final String id) throws MessagingException {
     boolean debug = false;
 
     //Set the host smtp address
     Properties props = new Properties();
    props.put("mail.smtp.host", "mail.rpi.edu");
 
     // create some properties and get the default Session
     Session session = Session.getDefaultInstance(props, null);
     session.setDebug(debug);
 
     // create a message
     Message msg = new MimeMessage(session);
 
     // set the from and to address
     InternetAddress addressFrom = new InternetAddress(from);
     msg.setFrom(addressFrom);
 
     InternetAddress[] addressTo = new InternetAddress[recipients.length];
     for (int i = 0; i < recipients.length; i++)
     {
       addressTo[i] = new InternetAddress(recipients[i]);
     }
     msg.setRecipients(Message.RecipientType.TO, addressTo);
 
 
     // Optional : You can also set your custom headers in the Email if you Want
     msg.addHeader("X-EMPAC-REG", id);
 
     // Setting the Subject and Content Type
     msg.setSubject(subject);
     msg.setContent(message, "text/plain");
     Transport.send(msg);
   }
 }
 
