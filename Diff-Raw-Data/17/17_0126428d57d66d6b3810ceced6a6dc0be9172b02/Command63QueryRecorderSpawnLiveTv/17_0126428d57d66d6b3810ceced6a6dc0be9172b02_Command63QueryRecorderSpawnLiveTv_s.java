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
 
 import org.syphr.mythtv.proto.ProtocolException;
 import org.syphr.mythtv.proto.SocketManager;
 import org.syphr.mythtv.proto.data.Channel;
 
 /* default */class Command63QueryRecorderSpawnLiveTv implements Command<Boolean>
 {
     private final String message;
 
     public Command63QueryRecorderSpawnLiveTv(int recorder, String chainId, boolean pip, Channel startChannel)
     {
         message = Protocol63Utils.getProtocolValue("QUERY_RECORDER " + recorder,
                                                    "SPAWN_LIVETV",
                                                    chainId,
                                                    pip ? "1" : "0",
                                                    startChannel.getNumber());
     }
 
     @Override
     public Boolean send(SocketManager socketManager) throws IOException
     {
         String response = socketManager.sendAndWait(message);
 
         if ("bad".equals(response))
         {
             return false;
         }
 
        if ("OK".equals(response))
         {
             return true;
         }
 
         throw new ProtocolException(response);
     }
 }
