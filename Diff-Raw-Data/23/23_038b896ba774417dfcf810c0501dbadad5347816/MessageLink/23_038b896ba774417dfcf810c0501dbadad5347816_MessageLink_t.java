package jamex.link;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.Topic;
 
 public final class MessageLink
 {
   private String name;
   private String sourceName;
   private String subscriptionName;
   private String selector;
   private boolean isSourceTopic;
   private MessageVerifier inputVerifier;
 
   private String destinationName;
   private boolean isDestinationTopic;
   private MessageVerifier outputVerifier;
 
   private String dmqName;
   private MessageTransformer transformer;
 
   private boolean isFrozen;
 
   private Session session;
   private MessageConsumer inConsumer;
   private MessageProducer outProducer;
   private MessageProducer dmqProducer;
 
   public void setName( final String name )
   {
     ensureEditable();
     this.name = name;
   }
 
   public void setInputQueue( final String name, final String selector )
   {
     ensureEditable();
     this.sourceName = name;
     this.isSourceTopic = false;
     this.subscriptionName = null;
     this.selector = selector;
   }
 
   public void setInputTopic( final String name, final String subscription, final String selector )
   {
     ensureEditable();
     if( null == name ) throw new NullPointerException( "name" );
     this.sourceName = name;
     this.isSourceTopic = true;
     this.subscriptionName = subscription;
     this.selector = selector;
   }
 
   public void setOutputQueue( final String name )
   {
     ensureEditable();
     if( null == name ) throw new NullPointerException( "name" );
     this.destinationName = name;
     this.isDestinationTopic = false;
   }
 
   public void setOutputTopic( final String name )
   {
     ensureEditable();
     this.destinationName = name;
     this.isDestinationTopic = true;
   }
 
   public void setDmqName( final String dmqName )
   {
     ensureEditable();
     this.dmqName = dmqName;
   }
 
   public void setInputVerifier( final MessageVerifier inputVerifier )
   {
     ensureEditable();
     this.inputVerifier = inputVerifier;
   }
 
   public void setOutputVerifier( final MessageVerifier outputVerifier )
   {
     ensureEditable();
     this.outputVerifier = outputVerifier;
   }
 
   public void setTransformer( final MessageTransformer transformer )
   {
     ensureEditable();
     this.transformer = transformer;
   }
 
   public void start( final Session session )
       throws Exception
   {
     if( null == session ) throw invalid( "session must not be null" );
     isFrozen = true;
     try
     {
       ensureValidConfig();
 
       this.session = session;
 
       final Destination inChannel =
           isSourceTopic ? session.createTopic( sourceName ) : session.createQueue( sourceName );
       final Destination outChannel =
           isDestinationTopic ? session.createTopic( destinationName ) : session.createQueue( destinationName );
       final Destination dmq = ( null != dmqName ) ? session.createQueue( dmqName ) : null;
 
       outProducer = session.createProducer( outChannel );
       dmqProducer = ( null != dmq ) ? session.createProducer( dmq ) : null;
 
       if( null != subscriptionName )
       {
         inConsumer = session.createDurableSubscriber( (Topic)inChannel, subscriptionName, selector, true );
       }
       else
       {
         inConsumer = session.createConsumer( inChannel );
       }
       inConsumer.setMessageListener( new LinkMessageListener() );
     }
     catch( final JMSException e )
     {
       isFrozen = false;
       log( "Error starting MessageLink", e );
       stop();
       throw e;
     }
   }
 
   public void stop()
       throws Exception
   {
     try
     {
       if( null != inConsumer ) inConsumer.close();
     }
     catch( final JMSException e )
     {
       log( "Closing consumer", e );
     }
     inConsumer = null;
 
     try
     {
       if( null != outProducer ) outProducer.close();
     }
     catch( final JMSException e )
     {
       log( "Closing producer", e );
     }
     outProducer = null;
 
     try
     {
       if( null != dmqProducer ) dmqProducer.close();
     }
     catch( final JMSException e )
     {
       log( "Closing producer for dmq", e );
     }
     dmqProducer = null;
 
     try
     {
       if( null != session ) session.close();
     }
     catch( final JMSException e )
     {
       log( "Closing session", e );
     }
     session = null;
 
     isFrozen = false;
   }
 
   private void doMessage( final Message message )
   {
     try
     {
       if( null != inputVerifier ) inputVerifier.verifyMessage( message );
     }
     catch( final Exception e )
     {
       handleFailure( message, "Incoming message failed precondition check. Error: " + e, e );
       return;
     }
 
     final Message output;
     try
     {
       if( null != transformer ) output = transformer.transformMessage( session, message );
       else output = message;
     }
     catch( final Exception e )
     {
       handleFailure( message, "Incoming message failed during message transformation step. Error: " + e, e );
       return;
     }
     if( null != output )
     {
       send( message, output );
     }
   }
 
   private void handleFailure( final Message inMessage,
                               final String reason,
                               final Throwable t )
   {
     log( reason, t );
     if( null == dmqProducer )
     {
       final String message = "Unable to handle message and no DMQ to send message to. Message: " + inMessage;
       throw new IllegalStateException( message );
     }
     final Message message = inMessage;
     try
     {
       message.setStringProperty( "JMLMessageLink", name );
       message.setStringProperty( "JMLFailureReason", reason );
       message.setStringProperty( "JMLInChannelName", sourceName );
       message.setStringProperty( "JMLInChannelType", isSourceTopic ? "Topic" : "Queue" );
       if( null != subscriptionName )
       {
         message.setStringProperty( "JMLInSubscriptionName", subscriptionName );
       }
       message.setStringProperty( "JMLOutChannelName", destinationName );
       message.setStringProperty( "JMLOutChannelType", isDestinationTopic ? "Topic" : "Queue" );
       dmqProducer.send( message,
                         message.getJMSDeliveryMode(),
                         message.getJMSPriority(),
                         message.getJMSExpiration() );
     }
     catch( final Exception e )
     {
       log( "Failed to send message to DMQ. Error: " + e, e );
       throw new IllegalStateException( "Failed to send message to DMQ. Message: " + inMessage, t );
 
     }
   }
 
   private void send( final Message inMessage, final Message outMessage )
   {
     try
     {
       if( null != outputVerifier ) outputVerifier.verifyMessage( outMessage );
     }
     catch( final Exception e )
     {
       handleFailure( inMessage, "Generated message failed send precondition check. Error: " + e, e );
     }
     try
     {
       outProducer.send( outMessage,
                         outMessage.getJMSDeliveryMode(),
                         outMessage.getJMSPriority(),
                         outMessage.getJMSExpiration() );
     }
     catch( final Exception e )
     {
       handleFailure( inMessage, "Failed to send generated message to destination. Error: " + e, e );
     }
   }
 
   private void ensureValidConfig()
       throws Exception
   {
     if( null == sourceName ) throw invalid( "sourceName not specified" );
     else if( null == destinationName ) throw invalid( "sourceName not specified" );
     else if( null != subscriptionName && !isSourceTopic )
     {
       throw invalid( "subscriptionName should only be specified for topics" );
     }
   }
 
   private Exception invalid( final String message )
   {
     return new IllegalStateException( "MessageLink (" + name + ") invalid. Reason: " + message );
   }
 
   private void log( final String message, final Throwable t )
   {
     synchronized( System.out )
     {
       System.out.println( "MessageLink (" + name + ") Error: " + message );
       if( null != t ) t.printStackTrace( System.out );
     }
   }
 
   private void ensureEditable()
   {
     if( isFrozen )
     {
       final String message = "Attempting to edit active MessageLink";
       log( message, null );
       throw new IllegalStateException( "MessageLink (" + name + "). " + message );
     }
   }
 
   private class LinkMessageListener implements MessageListener
   {
     public void onMessage( final Message message )
     {
       doMessage( message );
     }
   }
 }
