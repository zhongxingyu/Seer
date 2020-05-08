 /*
  * Copyright 2011-2012 Gregory P. Moyer
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
 package org.syphr.mythtv.protocol.impl;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.syphr.mythtv.util.exception.ProtocolException;
 import org.syphr.mythtv.util.exception.ProtocolException.Direction;
 import org.syphr.mythtv.util.socket.SocketManager;
 import org.syphr.mythtv.util.translate.Translator;
 
 /* default */class Command72QueryActiveBackends extends AbstractProtocolCommand<List<String>>
 {
     public Command72QueryActiveBackends(Translator translator, Parser parser)
     {
         super(translator, parser);
     }
 
     @Override
     protected String getMessage() throws ProtocolException
     {
         return "QUERY_ACTIVE_BACKENDS";
     }
 
     @Override
     public List<String> send(SocketManager socketManager) throws IOException
     {
         String response = socketManager.sendAndWait(getMessage());
         List<String> args = getParser().splitArguments(response);
 
         if (args.isEmpty())
         {
             throw new ProtocolException(response, Direction.RECEIVE);
         }
 
         try
         {
             int size = Integer.parseInt(args.get(0));
            if (args.size() != size - 1)
             {
                 throw new ProtocolException(response, Direction.RECEIVE);
             }
 
             return args.subList(1, args.size());
         }
         catch (NumberFormatException e)
         {
             throw new ProtocolException(response, Direction.RECEIVE);
         }
     }
 }
