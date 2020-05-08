package org.realityforge.jml;
 
 import java.util.Enumeration;
 import javax.jms.JMSException;
 import javax.jms.Message;
 
 public final class MessageUtil
 {
   static <T> T castToType( final Message message, final Class<T> type )
       throws Exception
   {
     if( type.isInstance( message ) )
     {
       return MessageUtil.<T>cast( message );
     }
     else
     {
       throw new IllegalArgumentException( "Message type " + message.getJMSType() +
                                           " is not of the expected type " + type.getName() +
                                           ". Message: " + message );
     }
   }
 
   @SuppressWarnings( { "unchecked" } )
   public static <T> T cast( final Object message )
   {
     return (T)message;
   }
 
   public static void copyMessageHeaders( final Message from, final Message to )
       throws JMSException
   {
     //set the developer assigned headers
     to.setJMSCorrelationID(from.getJMSCorrelationID());
     to.setJMSReplyTo( from.getJMSReplyTo() );
     to.setJMSType( from.getJMSType() );
 
     // these are not used by the JMS provider ... but we use them to keep
     // track of values and then explicitly pass them to send method.
     to.setJMSDeliveryMode( from.getJMSDeliveryMode() );
     to.setJMSPriority( from.getJMSPriority() );
     to.setJMSExpiration( from.getJMSExpiration() );
 
     // copy all the application specific properties to new message
     final Enumeration propertyNames = from.getPropertyNames();
     while( propertyNames.hasMoreElements() )
     {
       final String name = (String)propertyNames.nextElement();
       final Object value = from.getObjectProperty( name );
       to.setObjectProperty( name, value );
     }
   }
 }
