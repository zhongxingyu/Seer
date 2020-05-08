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
 
 package de.cosmocode.palava.ipc.protocol;
 
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutor;
 
 /**
 * A protocol mediate between arbitrary objects and
  * {@link IpcCommand}s. It's in the responsibilities of a protocol
  * to read and translate incoming requests and push
  * execution instructions to the {@link IpcCommandExecutor}.
  *
  * @author Willi Schoenborn
  */
 public interface Protocol {
     
     /**
      * A constant response object which indicates that no response should be sent
      * to the caller when returned by {@link #process(Object, DetachedConnection)}.
      */
     Object NO_RESPONSE = new Object();
     
     /**
      * Checks whether this protocol supports the specified request.
      * 
      * @param request the incoming request
     * @return true if this protocol supports the request, false otherwise
      */
     boolean supports(Object request);
     
     /**
      * Processes the incoming request and returns the response.
      * 
      * @param request the incoming request
      * @param connection the current connection
      * @return the produced response, in case {@link Protocol#NO_RESPONSE} is returned
      *         the invoker must not send a response to the caller
      * @throws ProtocolException if processing failed        
      */
     Object process(Object request, DetachedConnection connection) throws ProtocolException;
     
     /**
      * Callback for the invoker which may be called when the request
     * couldn't be read because of an exception. There is no guarantee
      * this method is called when more than one protocol is installed
      * because the invoker may not know which protocol was addressed.
      * 
     * @param t the occurred exception
      * @param request the incoming request, may be null when parsing failed
      * @return the error response for the caller
      */
     Object onError(Throwable t, Object request);
     
 }
