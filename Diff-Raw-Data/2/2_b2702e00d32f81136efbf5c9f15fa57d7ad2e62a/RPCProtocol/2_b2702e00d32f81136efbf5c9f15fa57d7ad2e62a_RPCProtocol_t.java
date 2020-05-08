 /* 
  * 
  * PROJECT
  *     Name
  *         APS APIs
  *     
  *     Code Version
  *         1.0.0
  *     
  *     Description
  *         Provides the APIs for the application platform services.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2012-01-08: Created!
  *         
  */
 package se.natusoft.osgi.aps.api.net.rpc.service;
 
 import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
 import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
 
 /**
  * This represents an RPC protocol provider. This API is not enough in itself, it is a common base
 * for different protocols.
  */
 public interface RPCProtocol {
 
     /**
      * @return The name of the provided protocol.
      */
     String getServiceProtocolName();
     
     /**
      * @return The version of the implemented protocol.
      */
     String getServiceProtocolVersion();
 
     /**
      * @return The expected content type of a request. This should be verified by the transport if it has content type availability.
      */
     String getRequestContentType();
 
     /**
      * @return The content type of the response for when such can be provided.
      */
     String getResponseContentType();
 
     /**
      * @return A short description of the provided service. This should be in plain text.
      */
     String getRPCProtocolDescription();
 
     /**
      * Factory method to create an error object.
      *
      * @param errorType The type of the error.
      * @param message An error message.
      * @param optionalData Whatever optional data you want to pass along or null.
      *
      * @return An RPCError implementation.
      */
     RPCError createRPCError(ErrorType errorType, String message, String optionalData);
 
 }
