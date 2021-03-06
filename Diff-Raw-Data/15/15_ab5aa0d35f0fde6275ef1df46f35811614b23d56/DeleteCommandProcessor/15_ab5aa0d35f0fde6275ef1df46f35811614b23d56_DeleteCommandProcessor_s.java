 /*
  * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.hazelcast.impl.ascii.memcache;
 
 import com.hazelcast.impl.ascii.AbstractTextCommandProcessor;
 import com.hazelcast.impl.ascii.TextCommandService;
import com.hazelcast.logging.ILogger;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
import java.util.logging.Level;
 
 public class DeleteCommandProcessor extends AbstractTextCommandProcessor<DeleteCommand> {
    private final ILogger logger;
 
     public DeleteCommandProcessor(TextCommandService textCommandService) {
         super(textCommandService);
        logger = textCommandService.getNode().getLogger(this.getClass().getName());
     }
 
     public void handle(DeleteCommand command) {
        String key = null;
         try {
             key = URLDecoder.decode(command.getKey(), "UTF-8");
         } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
         }
         String mapName = "default";
         int index = key.indexOf(':');
         if (index != -1) {
             mapName = key.substring(0, index);
             key = key.substring(index + 1);
         }
         command.setResponse(DELETED);
         if (command.shouldReply()) {
             textCommandService.sendResponse(command);
         }
         textCommandService.incrementDeleteCount();
         textCommandService.delete(mapName, key);
     }
 
     public void handleRejection(DeleteCommand command) {
         handle(command);
     }
 }
