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
 
 import java.util.List;
 
 import org.syphr.mythtv.proto.CommandException;
 import org.syphr.mythtv.proto.ProtocolException;
 import org.syphr.mythtv.proto.ProtocolException.Direction;
 
 /* default */class Command63QueryRecorderGetFramesWritten extends AbstractCommand63QueryRecorder<Long>
 {
     public Command63QueryRecorderGetFramesWritten(int recorderId)
     {
         super(recorderId);
     }
 
     @Override
     protected String getSubCommand() throws ProtocolException
     {
         return "GET_FRAMES_WRITTEN";
     }
 
     @Override
     public Long parseResponse(String response) throws ProtocolException, CommandException
     {
        if ("-1".equals(response))
        {
            throw new CommandException("Unable to determine number of frames written");
        }

         List<String> args = Protocol63Utils.splitArguments(response);
 
         if (args.size() != 2)
         {
             throw new ProtocolException(response, Direction.RECEIVE);
         }
 
        return ProtocolUtils.combineInts(Integer.parseInt(args.get(0)),
                                         Integer.parseInt(args.get(1)));
     }
 }
