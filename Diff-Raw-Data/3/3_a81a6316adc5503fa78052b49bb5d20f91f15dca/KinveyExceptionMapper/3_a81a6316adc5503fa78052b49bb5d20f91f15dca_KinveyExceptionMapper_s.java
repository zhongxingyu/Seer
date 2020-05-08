 /*
  * Copyright (c) 2013 Kinvey Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.kinvey.business_logic.providers;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import com.kinvey.business_logic.CommandResponse;
 import com.kinvey.business_logic.KinveyError;
 import com.kinvey.business_logic.KinveyResponse;
 
 /**
  * @author mjsalinger
  * @since 2.0
  */
 @Provider
 public class KinveyExceptionMapper implements ExceptionMapper<Exception> {
     public Response toResponse(Exception exception){
 
 
         KinveyResponse kinveyRes = new KinveyResponse(400, false);
         kinveyRes.setError(new KinveyError.Builder().setException(exception).build());
 
        CommandResponse commandRes = new CommandResponse();
        commandRes.setResponse(kinveyRes);
 
         return Response.status(Response.Status.BAD_REQUEST)
                 .type(MediaType.APPLICATION_JSON)
                 .entity(commandRes)
                 .build();
 
     }
 }
