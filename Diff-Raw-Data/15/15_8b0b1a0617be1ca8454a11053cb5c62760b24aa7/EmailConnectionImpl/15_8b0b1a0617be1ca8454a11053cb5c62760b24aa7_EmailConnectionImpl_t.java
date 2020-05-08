 package com.meltmedia.cadmium.email;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EmailConnectionImpl
   implements EmailConnection
 {
   /** The log for the email connection class. */
   private static final Logger log = LoggerFactory.getLogger(EmailConnectionImpl.class);
 
   /** The java mail session to which this connection is attached. */
   protected Session session = null;
 
   /** The java mail transport used by this connection to send attachments. */
   protected Transport transport = null;
 
   /** The email filter to use when creating messages. */
   protected MessageTransformer transformer;
 
   public EmailConnectionImpl( Session session, MessageTransformer transformer )
     throws EmailException
   {
     // validate that the session is not null.
     if( session == null ) {
       throw new IllegalArgumentException("A connection cannot be created with a null session.");
     }
 
     // set the session.
     this.session = session;
 
     // get the transport.
     try {
       this.transport = session.getTransport();
     }
     catch( Exception e ) {
       log.error("Could not create transport.", e);
       throw new EmailException("Could not create transport.", e);
     }
 
     // set the transformer.
     this.transformer = transformer!=null?transformer:new IdentityMessageTransformer();
   }
 
   /**
    * Opens a connection on the underlying transport.
    */
   public void connect()
     throws EmailException
   {
     try {
       transport.connect();
     }
     catch( Exception e ) {
       throw new EmailException("Could not connect to underlying transport.", e);
     }
   }
 
   public void send( Email email )
     throws EmailException
   {
     MimeMessage message = null;
     // simplify all of the email address headers down.
     email.simplify();
 
     // build the email.
     try {
       message = email.newMessage(session);
     }
     catch( MessagingException me ) {
       throw new EmailException("Could not create a JavarMail message for an email.", me);
     }
 
     // transformer the message.
     try {
       message = transformer.transform(message);
       if( message == null ) {
         log.warn("Message transformer canceled sending of message.");
       }
     }
     catch( MessagingException me ) {
       throw new EmailException("Could not translate the message.", me);
     }
 
     // send the email.
     if( message != null ) {
       try {
         log.debug("Sending email.");
         Transport.send(message);
       }
       catch( Exception e ) {
      	e.printStackTrace();
         throw new EmailException("Could not send message.", e);
       }
     }
   }
 
   public void close()
     throws EmailException
   {
     close(transport);
   }
 
   public boolean isConnected()
   {
     boolean connected = false;
     if( transport != null ) {
       connected = transport.isConnected();
     }
     return connected;
   }
 
   public void finalize()
   {
     try {
       if( !isConnected() ) {
         this.close();
       }
     }
     catch( Exception e ) {
       log.warn("Could not close connection in finalize block.", e);
     }
   }
 
   public static void close( Transport transport )
   {
     try {
       if( transport != null ) {
         transport.close();
       }
     }
     catch( Exception e ) {
       log.warn("Exception thrown while closing java mail transport.", e);
     }
   }
 }
