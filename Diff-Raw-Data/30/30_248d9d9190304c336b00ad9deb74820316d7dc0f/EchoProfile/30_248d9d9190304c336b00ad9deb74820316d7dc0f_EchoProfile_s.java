 /*
 * EchoProfile.java    $Revision: 1.2 $ $Date: 2001/04/02 21:39:00 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.profile.echo;
 
 
 import java.io.IOException;
 
 import org.beepcore.beep.core.*;
 import org.beepcore.beep.profile.*;
 import org.beepcore.beep.util.*;
 
 
 /**
  * This is the Echo profile implementation
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/04/02 21:39:00 $
  */
 public class EchoProfile
     extends ProfileImpl implements StartChannelListener, MessageListener
 {
 
     public static final String ECHO_URI =
         "http://xml.resource.org/profiles/NULL/ECHO";
 
     public StartChannelListener getStartChannelListener()
     {
         return this;
     }
 
     public String getURI()
     {
         return ECHO_URI;
     }
 
     /**
      * Method startChannel
      *
      *
      * @param channel
      * @param encoding
      * @param data
      *
      * @throws StartChannelException
      *
      */
     public void startChannel(Channel channel, String encoding, String data)
             throws StartChannelException
     {
         Log.logEntry(Log.SEV_DEBUG, "EchoCCL StartChannel Callback");
         channel.setDataListener(this);
     }
 
     /**
      * Method closeChannel
      *
      *
      * @param channel
      *
      * @throws CloseChannelException
      *
      */
     public void closeChannel(Channel channel) throws CloseChannelException
     {
         Log.logEntry(Log.SEV_DEBUG, "EchoCCL CloseChannel Callback");
         channel.setDataListener(null);
     }
 
     public void receiveMSG(Message message) throws BEEPError
     {
         Log.logEntry(Log.SEV_DEBUG, "Received MSG Callback [ECHO]");
 
        // Generate a response
        byte buff[] = null;
        DataStream ds = message.getDataStream();

        try {
            int length = ds.getInputStream().available();

            buff = new byte[length];

            int count = 0;

            while (count < length) {
                count += ds.getInputStream().read(buff, 0, length);
            }

        } catch (IOException e) {
            throw new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                "Error while reading message in Echo Profile");
        }

        Channel ch = message.getChannel();
         try {
            ch.sendRPY(ds);
         } catch (BEEPException e) {
             throw new BEEPError(BEEPError.CODE_REQUESTED_ACTION_ABORTED,
                                 "Error sending RPY");
         }
     }
 }
