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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.binary.Base64;
 import org.syphr.mythtv.proto.CommandException;
 import org.syphr.mythtv.proto.ProtocolException;
 import org.syphr.mythtv.proto.ProtocolException.Direction;
 import org.syphr.mythtv.proto.SocketManager;
 import org.syphr.mythtv.proto.data.PixMap;
 import org.syphr.mythtv.proto.data.ProgramInfo;
 
 /* default */class Command63QueryPixMapGetIfModified extends AbstractCommand<PixMap>
 {
    private static final Pattern BAD_RESPONSE_PATTERN = Pattern.compile("\\d: (.*)");
 
     private final Date timestamp;
     private final int maxFileSize;
     private final ProgramInfo program;
 
     public Command63QueryPixMapGetIfModified(Date timestamp,
                                              int maxFileSize,
                                              ProgramInfo program)
     {
         this.timestamp = timestamp;
         this.maxFileSize = maxFileSize;
         this.program = program;
     }
 
     @Override
     protected String getMessage() throws ProtocolException
     {
         List<String> messageList = new ArrayList<String>();
         messageList.add("QUERY_PIXMAP_GET_IF_MODIFIED");
         messageList.add(timestamp == null
                 ? "-1"
                 : String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timestamp.getTime())));
         messageList.add(String.valueOf(maxFileSize));
         messageList.addAll(Protocol63Utils.extractProgramInfo(program));
 
         return Protocol63Utils.combineArguments(messageList);
     }
 
     @Override
     public PixMap send(SocketManager socketManager) throws IOException,
                                                    CommandException
     {
         String response = socketManager.sendAndWait(getMessage());
        List<String> args = Protocol63Utils.splitArguments(response);
 
        if (args.size() == 0)
         {
            throw new ProtocolException(response, Direction.RECEIVE);
        }

        if ("ERROR".equals(args.get(0)) || "WARNING".equals(args.get(0)))
        {
            if (args.size() != 2)
            {
                throw new ProtocolException(response, Direction.RECEIVE);
            }

            Matcher matcher = BAD_RESPONSE_PATTERN.matcher(args.get(1));
            if (matcher.find())
            {
                throw new CommandException(matcher.group(1));
            }

            throw new ProtocolException(response, Direction.RECEIVE);
         }
 
         try
         {
             switch (args.size())
             {
                 case 1:
                     return new PixMap(new Date(TimeUnit.SECONDS.toMillis(Long.parseLong(args.get(0)))));
 
                 case 4:
                     Date lastModified = new Date(TimeUnit.SECONDS.toMillis(Long.parseLong(args.get(0))));
                     int size = Integer.parseInt(args.get(1));
                     String checksum = args.get(2);
                     byte[] data = new Base64().decode(args.get(3));
 
                     return new PixMap(lastModified, size, checksum, data);
 
                 default:
                     throw new ProtocolException(response, Direction.RECEIVE);
             }
         }
         catch (NumberFormatException e)
         {
             throw new ProtocolException(response, Direction.RECEIVE, e);
         }
     }
 }
