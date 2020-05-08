 /**
  * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.micromailer.imp;
 
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 
 import org.sonatype.micromailer.EmailerConfiguration;
 import org.sonatype.micromailer.MailCompositionAttachmentException;
 import org.sonatype.micromailer.MailCompositionMessagingException;
 import org.sonatype.micromailer.MailRequest;
 import org.sonatype.micromailer.MailSender;
 import org.sonatype.micromailer.MailType;
 
 /**
  * The JavaMail sender.
  * 
  * @author cstamas
  */
 @Singleton
 @Named
 public class DefaultMailSender
     implements MailSender
 {
     public void sendMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
         throws MailCompositionAttachmentException, MailCompositionMessagingException
     {
         try
         {
             Session session = configuration.getSession();
 
             MimeMessage message = request.getMimeMessage();
 
             message.saveChanges();
 
             // send it
             Transport t = session.getTransport();
 
             try
             {
                 t.connect();
 
                 t.sendMessage( message, message.getAllRecipients() );
             }
             finally
             {
                 t.close();
             }
         }
         catch ( MessagingException ex )
         {
            throw new MailCompositionMessagingException( "MessagingException occured!", ex );
         }
     }
 }
