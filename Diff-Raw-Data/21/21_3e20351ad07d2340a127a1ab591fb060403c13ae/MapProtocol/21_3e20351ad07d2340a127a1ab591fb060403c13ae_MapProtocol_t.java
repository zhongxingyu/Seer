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
 
 import java.util.Map;
 
 /**
  * An abstract {@link Protocol} which requires {@link Map} requests.
  *
  * @author Willi Schoenborn
  */
 public abstract class MapProtocol implements Protocol {
 
     @Override
     public final boolean supports(Object request) {
         return request instanceof Map<?, ?> && supports(Map.class.cast(request));
     }
 
     /**
      * Typed version of {@link #supports(Object)}.
      * 
      * @param request the incoming request
      * @return true if this protocol supports the specified request
      */
     public abstract boolean supports(Map<?, ?> request);
     
     @Override
     public final Object process(Object request, DetachedConnection connection) throws ProtocolException {
         return process(Map.class.cast(request), connection);
     }
 
     /**
      * Typed version of {@link #process(Object, DetachedConnection)}.
      * 
      * @param request the incoming request
      * @param connection the current connection
      * @return the produced content
      * @throws ProtocolException if processing failed
      */
     public abstract Object process(Map<?, ?> request, DetachedConnection connection) throws ProtocolException;
 
     @Override
     public final Object onError(Throwable t, Object request) {
         return onError(t, Map.class.cast(request));
     }
 
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
     public abstract Object onError(Throwable t, Map<?, ?> request);
     
 }
