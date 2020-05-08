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
 package org.syphr.mythtv.protocol.events.impl;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.syphr.mythtv.commons.exception.ProtocolException;
 import org.syphr.mythtv.commons.exception.ProtocolException.Direction;
 import org.syphr.mythtv.commons.translate.Translator;
 import org.syphr.mythtv.protocol.events.BackendEventListener63;
 import org.syphr.mythtv.protocol.events.EventProtocol;
 import org.syphr.mythtv.protocol.events.SystemEvent;
 import org.syphr.mythtv.protocol.events.SystemEventData;
 import org.syphr.mythtv.protocol.impl.Parser;
 
 public class EventProtocol69 extends AbstractEventProtocol<BackendEventListener63>
 {
     public EventProtocol69(Translator translator, Parser parser)
     {
         super(translator, parser, BackendEventListener63.class);
     }
 
     @Override
     protected EventProtocol createFallbackProtocol()
     {
         return new EventProtocol68(getTranslator(), getParser());
     }
 
     @Override
     protected EventSender<BackendEventListener63> createSender(List<String> fragments) throws ProtocolException,
                                                                                       UnknownEventException
     {
         BackendMessage63 message = new BackendMessage63(fragments);
 
         try
         {
             String command = message.getCommand();
 
             if ("SYSTEM_EVENT".equals(command))
             {
                 final List<String> args = message.getArgs();
                 final SystemEvent event = SystemEvent.valueOf(args.get(0));
                 final Map<SystemEventData, String> dataMap = new HashMap<SystemEventData, String>();
 
                 for (int i = 1; i < args.size(); i += 2)
                 {
                     String dataType = args.get(i);
                     String dataValue = args.get(i + 1);
 
                     if ("CREATED".equals(dataType))
                     {
                         dataMap.put(SystemEventData.CREATED, dataValue);
                     }
                    else if ("DESTORYED".equals(dataType))
                     {
                         dataMap.put(SystemEventData.DESTROYED, dataValue);
                     }
                 }
 
                 if (!dataMap.isEmpty())
                 {
                     return new EventSender<BackendEventListener63>()
                     {
                         @Override
                         public void sendEvent(BackendEventListener63 l)
                         {
                             l.systemEvent(event, dataMap);
                         }
                     };
                 }
             }
         }
         catch (Exception e)
         {
             throw new ProtocolException(message.toString(), Direction.RECEIVE, e);
         }
 
         throw new UnknownEventException(message.toString());
     }
 }
