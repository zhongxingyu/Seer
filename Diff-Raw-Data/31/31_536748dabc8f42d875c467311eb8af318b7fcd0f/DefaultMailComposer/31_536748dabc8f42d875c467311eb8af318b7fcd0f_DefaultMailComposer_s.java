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
 
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.DataHandler;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.mail.Message.RecipientType;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.sonatype.micromailer.Address;
 import org.sonatype.micromailer.EmailerConfiguration;
 import org.sonatype.micromailer.MailComposer;
 import org.sonatype.micromailer.MailCompositionAttachmentException;
 import org.sonatype.micromailer.MailCompositionMessagingException;
 import org.sonatype.micromailer.MailCompositionTemplateException;
 import org.sonatype.micromailer.MailPart;
 import org.sonatype.micromailer.MailRequest;
 import org.sonatype.micromailer.MailType;
 import org.sonatype.sisu.velocity.Velocity;
 
 /**
  * The Velocity powered mail composer.
  * 
  * @author cstamas
  */
 @Singleton
 @Named
 public class DefaultMailComposer
     implements MailComposer
 {
     public static final String MESSAGE_FROM = "From";
 
     public static final String MESSAGE_TO = "To";
 
     public static final String MESSAGE_BCC = "Bcc";
 
     public static final String MESSAGE_SENDER = "Sender";
 
     public static final String X_MESSAGE_ID_HEADER = "X-EMailer-Mail-Request-ID";
 
     @Inject
     private Velocity velocityComponent;
 
     protected Map<String, Object> initialVelocityContext;
 
     public Map<String, Object> getInitialVelocityContext()
     {
         return initialVelocityContext;
     }
 
     public void setInitialVelocityContext( Map<String, Object> initialVelocityContext )
     {
         this.initialVelocityContext = initialVelocityContext;
     }
 
     // ====================
     // mail composer iface
 
     public void composeMail( EmailerConfiguration configuration, MailRequest request, MailType mailType )
         throws MailCompositionTemplateException, MailCompositionAttachmentException, MailCompositionMessagingException
     {
         // expand subject if needed
 
         if ( request.getExpandedSubject() == null )
         {
             request.setExpandedSubject( expandTemplateFromString( mailType.getSubjectTemplate(), request
                 .getBodyContext() ) );
         }
 
         // expand body if needed
 
         if ( request.getExpandedBody() == null )
         {
             request.setExpandedBody( expandTemplateFromString( mailType.getBodyTemplate(), request.getBodyContext() ) );
         }
 
         // compose the mime email
 
         try
         {
             Session session = configuration.getSession();
 
             MimeMessage message = new MimeMessage( session );
 
             MimeMultipart root = new MimeMultipart( "related" );
 
             message.setContent( root );
 
             if ( request.getCustomHeaders().size() > 0 )
             {
                 for ( String key : request.getCustomHeaders().keySet() )
                 {
                     message.addHeader( key, request.getCustomHeaders().get( key ) );
                 }
             }
 
             if ( request.getRequestId() != null )
             {
                 message.addHeader( X_MESSAGE_ID_HEADER, request.getRequestId() );
             }
 
             if ( request.getSender() != null )
             {
                 message.setSender( request.getSender().getInternetAddress( request.getEncoding() ) );
             }
 
             if ( request.getFrom() != null )
             {
                 message.setFrom( request.getFrom().getInternetAddress( request.getEncoding() ) );
             }
 
             if ( request.getReplyTo() != null )
             {
                 message.setReplyTo( new InternetAddress[] { request.getReplyTo()
                     .getInternetAddress( request.getEncoding() ) } );
             }
 
             if ( request.getSentDate() != null )
             {
                 message.setSentDate( request.getSentDate() );
             }
             else
             {
                 message.setSentDate( new Date() );
             }
 
             setRecipientsFromList( request.getEncoding(), message, RecipientType.TO, request.getToAddresses() );
 
             setRecipientsFromList( request.getEncoding(), message, RecipientType.BCC, request.getBccAddresses() );
 
             setRecipientsFromList( request.getEncoding(), message, RecipientType.CC, request.getCcAddresses() );
 
             // add content and any inline resource we have
 
             message.setSubject( request.getExpandedSubject(), request.getEncoding() );
 
             MimeBodyPart body = new MimeBodyPart();
 
             root.addBodyPart( body );
 
             if ( mailType.isBodyIsHtml() )
             {
                 body.setContent( request.getExpandedBody(), "text/html;charset=" + request.getEncoding() );
             }
             else
             {
                 body.setText( request.getExpandedBody(), request.getEncoding() );
             }
 
             for ( String key : mailType.getInlineResources().keySet() )
             {
                 MimeBodyPart mimeBodyPart = new MimeBodyPart();
 
                 mimeBodyPart.setDisposition( MimeBodyPart.INLINE );
 
                 mimeBodyPart.setContentID( key );
 
                 mimeBodyPart.setDataHandler( new DataHandler( mailType.getInlineResources().get( key ) ) );
 
                 root.addBodyPart( mimeBodyPart );
             }
 
             // add attachments if any
 
             for ( String key : request.getAttachmentMap().keySet() )
             {
                 MimeBodyPart mimeBodyPart = new MimeBodyPart();
 
                 mimeBodyPart.setDisposition( MimeBodyPart.ATTACHMENT );
 
                 mimeBodyPart.setFileName( key );
 
                 mimeBodyPart.setDataHandler( new DataHandler( request.getAttachmentMap().get( key ) ) );
 
                 root.addBodyPart( mimeBodyPart );
             }
 
             for ( MailPart part : request.getParts() )
             {
                 MimeBodyPart mimeBodyPart = new MimeBodyPart();
 
                 mimeBodyPart.setDisposition( part.getDisposition() );
                 if ( part.getFilename() != null )
                 {
                     mimeBodyPart.setFileName( part.getFilename() );
                 }
 
                 if ( part.getContentId() != null )
                 {
                     mimeBodyPart.setContentID( part.getContentId() );
                 }
                 if ( part.getContentLocation() != null )
                 {
                     mimeBodyPart.setHeader( "Content-Location", part.getContentLocation() );
                 }
 
                 for ( Map.Entry<String, String> entry : part.getHeaders().entrySet() )
                 {
                     if ( entry.getValue() == null )
                     {
                         mimeBodyPart.removeHeader( entry.getKey() );
                     }
                     else
                     {
                         mimeBodyPart.setHeader( entry.getKey(), entry.getValue() );
                     }
                 }
 
                 mimeBodyPart.setDataHandler( part.getContent() );
 
                 root.addBodyPart( mimeBodyPart );
             }
 
             // validate some of it
 
             if ( message.getHeader( MESSAGE_FROM, null ) == null )
             {
                 // RFC822: From is MANDATORY
                 // http://www.ietf.org/rfc/rfc822.txt
                 throw new MailCompositionMessagingException( "E-Mail 'From' field is mandatory!" );
             }
             if ( message.getHeader( MESSAGE_TO, null ) == null && message.getHeader( MESSAGE_BCC, null ) == null )
             {
                 // RFC822: Bcc OR To is MANDATORY
                 // http://www.ietf.org/rfc/rfc822.txt
                 throw new MailCompositionMessagingException( "One of the 'To' or 'Bcc' header is mandatory!" );
             }
 
             // make it done
             message.saveChanges();
 
             // set the composed mime message to request
             request.setMimeMessage( message );
         }
         catch ( UnsupportedEncodingException ex )
         {
            throw new MailCompositionMessagingException( "Unsupported encoding occured!", ex );
         }
         catch ( MessagingException ex )
         {
            throw new MailCompositionMessagingException( "MessagingException occured!", ex );
         }
     }
 
     protected void setRecipientsFromList( String encoding, MimeMessage message, RecipientType type,
                                           List<Address> addresses )
         throws MessagingException, UnsupportedEncodingException
     {
         if ( addresses == null || addresses.size() == 0 )
         {
             return;
         }
 
         InternetAddress[] adrs = new InternetAddress[addresses.size()];
 
         for ( int i = 0; i < addresses.size(); i++ )
         {
             adrs[i] = addresses.get( i ).getInternetAddress( encoding );
         }
 
         message.setRecipients( type, adrs );
     }
 
     protected String expandTemplateFromString( String template, Map<String, Object> model )
         throws MailCompositionTemplateException
     {
         try
         {
             StringWriter sw = new StringWriter();
 
             VelocityContext context = new VelocityContext( getInitialVelocityContext(), new VelocityContext( model ) );
 
             velocityComponent.getEngine().evaluate( context, sw, "SUBJECT", template );
 
             return sw.toString();
         }
         catch ( Exception ex )
         {
             throw new MailCompositionTemplateException( "Velocity throw exception during template merge.", ex );
         }
     }
 
     protected String expandTemplateFromResource( String templateResourceName, Map<String, Object> model )
         throws MailCompositionTemplateException
     {
         try
         {
             StringWriter sw = new StringWriter();
 
             Template template = velocityComponent.getEngine().getTemplate( templateResourceName, "UTF-8" );
 
             VelocityContext context = new VelocityContext( getInitialVelocityContext(), new VelocityContext( model ) );
 
             template.merge( context, sw );
 
             return sw.toString();
         }
         catch ( Exception ex )
         {
             throw new MailCompositionTemplateException( "Velocity throw exception during template merge.", ex );
         }
     }
 
 }
