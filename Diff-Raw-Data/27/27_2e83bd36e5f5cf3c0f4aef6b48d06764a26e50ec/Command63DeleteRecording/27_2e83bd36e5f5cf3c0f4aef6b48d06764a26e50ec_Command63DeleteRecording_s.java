 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.proto.impl;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.syphr.mythtv.proto.ProtocolException;
 import org.syphr.mythtv.proto.SocketManager;
 import org.syphr.mythtv.proto.data.Channel;
 
 /* default */class Command63DeleteRecording implements Command<Boolean>
 {
     private final String message;
 
     public Command63DeleteRecording(Channel channel,
                                    Date startTime,
                                     boolean force,
                                     boolean forget)
     {
         StringBuilder builder = new StringBuilder();
         builder.append("DELETE_RECORDING ");
         builder.append(channel.getId());
         builder.append(' ');
        builder.append(Protocol63Utils.getConcatDateFormat().format(startTime));
         builder.append(' ');
         builder.append(force ? "FORCE" : "NO_FORCE");
         builder.append(' ');
         builder.append(forget ? "FORGET" : "NO_FORGET");
 
         message = builder.toString();
     }
 
     @Override
     public Boolean send(SocketManager socketManager) throws IOException
     {
         String response = socketManager.sendAndWait(message);
 
         try
         {
             int result = Integer.parseInt(response);
             if (result == -2)
             {
                 return false;
             }
 
             if (result > -2)
             {
                 return true;
             }
         }
         catch (NumberFormatException e)
         {
             throw new ProtocolException(response, e);
         }
 
         throw new ProtocolException(response);
     }
 }
