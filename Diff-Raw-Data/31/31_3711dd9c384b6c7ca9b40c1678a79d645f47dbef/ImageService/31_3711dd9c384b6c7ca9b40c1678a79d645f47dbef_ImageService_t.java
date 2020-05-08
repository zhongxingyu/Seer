 package org.kurron.hello;
 
 import org.springframework.integration.Message;
 import org.springframework.integration.annotation.Header;
 import org.springframework.integration.annotation.ServiceActivator;
 import org.springframework.integration.support.MessageBuilder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ron
  * Date: 3/26/13
  * Time: 8:24 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ImageService
 {
     @ServiceActivator
     public Message<byte[]> processImage( final byte[] payload, @Header( "logan" ) final String header )
     {
         System.err.println( "logan = " + header );
        if ( 0 == payload.length % 2 )
        {
            throw new IllegalArgumentException( "Forced to fail." );
        }
         return MessageBuilder.withPayload( payload ).setHeader( "logan", header ).build();
     }
 }
