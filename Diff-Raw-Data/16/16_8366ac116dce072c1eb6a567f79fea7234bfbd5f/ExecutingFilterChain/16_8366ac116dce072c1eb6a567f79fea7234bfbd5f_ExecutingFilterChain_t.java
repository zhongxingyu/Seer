 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.ipc.execvm;
 
import com.google.common.collect.Maps;
import de.cosmocode.palava.ipc.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import java.util.Map;
 
 /**
  * A {@link IpcCallFilterChain} implementation that executes the given command.
  *
  * @author Willi Schoenborn
  */
 enum ExecutingFilterChain implements IpcCallFilterChain {
     
     INSTANCE;
     
     private static final Logger LOG = LoggerFactory.getLogger(ExecutingFilterChain.class);
     
     @Override
     public Map<String, Object> filter(IpcCall call, IpcCommand command) throws IpcCommandExecutionException {
         final Map<String, Object> result = Maps.newLinkedHashMap();
         LOG.debug("Executing {}", command);
         
         try {
             command.execute(call, result);
         } catch (IpcCommandExecutionException e) {
             if (e.getCause() == null || IpcCommands.mayThrow(command.getClass(), e.getCause())) {
                 LOG.debug("An expected exception was thrown while executing " + command, e);
             } else {
                 LOG.error("An undeclared exception was thrown while executing " + command, e);
             }
             throw e;
             /* CHECKSTYLE:OFF */
         } catch (RuntimeException e) {
             /* CHECKSTYLE:ON */
            if (IpcCommands.mayThrow(command.getClass(), e)) {
                 LOG.debug("An expected exception was thrown while executing " + command, e);
             } else {
                 LOG.error("An unexpected exception was thrown while executing " + command, e);
             }
             throw e;
         }
         
         return result;
     }
     
 }
