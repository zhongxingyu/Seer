 /*
  * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.sisu.siesta.client.internal;
 
 import org.sonatype.sisu.siesta.common.error.ErrorXO;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 /**
  * TODO
  *
  * @since 1.4
  */
 public abstract class ErrorsV1FilterSupport
     extends ErrorResponseFilterSupport
 {
 
     protected void throwException( final ClientResponse response )
     {
         ErrorXO error = null;
         try
         {
             error = response.getEntity( ErrorXO.class );
         }
         catch ( Exception e )
         {
            // ignore
         }
         if ( error != null )
         {
             throw new UniformInterfaceException( error.getMessage() + " (" + error.getId() + ")", response );
         }
     }
 
 }
