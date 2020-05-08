 /**
  * Copyright (C) 2013 Alexander Szczuczko
  *
  * This file may be modified and distributed under the terms
  * of the MIT license. See the LICENSE file for details.
  */
 package ca.szc.keratin.core.event.message.recieve;
 
 import java.util.Arrays;
 import java.util.List;
 
 import net.engio.mbassy.bus.MBassador;
 import ca.szc.keratin.core.event.IrcEvent;
 import ca.szc.keratin.core.event.message.MessageRecieve;
 import ca.szc.keratin.core.net.message.IrcMessage;
 
 public class ReceiveMode
     extends MessageRecieve
 {
     public static final String COMMAND = "MODE";
 
    // private final String sender;
 
     private final String target;
 
     private final String flags;
 
     private final List<String> flagParams;
 
     public ReceiveMode( MBassador<IrcEvent> bus, IrcMessage message )
     {
         super( bus, message );
 
         String[] params = message.getParams();
 
        // sender = message.getPrefix().substring( 0, message.getPrefix().indexOf( '!' ) );
         target = params[0];
         flags = params[1];

         flagParams = params.length > 2 ? Arrays.asList( Arrays.copyOfRange( params, 2, params.length ) ) : null;

        String firstFlagParam = flagParams.get( 0 );
        if ( firstFlagParam.startsWith( ":" ) )
            flagParams.set( 0, firstFlagParam.substring( 1 ) );
     }
 
     // public ReceiveMode( MBassador<IrcEvent> bus, String prefix, String name, String mode )
     // throws InvalidMessagePrefixException, InvalidMessageCommandException, InvalidMessageParamException
     // {
     // super( bus, new IrcMessage( prefix, COMMAND, name, mode ));
     // }
 
    // public String getSender()
    // {
    // return sender;
    // }
 
     public String getTarget()
     {
         return target;
     }
 
     public String getFlags()
     {
         return flags;
     }
 
     public List<String> getFlagParams()
     {
         return flagParams;
     }
 }
