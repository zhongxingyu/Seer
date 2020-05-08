 /**
  * Copyright (c) 2011, yMock.com
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met: 1) Redistributions of source code must retain the above
  * copyright notice, this list of conditions and the following
  * disclaimer. 2) Redistributions in binary form must reproduce the above
  * copyright notice, this list of conditions and the following
  * disclaimer in the documentation and/or other materials provided
  * with the distribution. 3) Neither the name of the yMock.com nor
  * the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.ymock.server;
 
 // commons from com.ymock:ymock-commons
 import com.ymock.commons.YMockException;
 
 // for JAX-RS
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 /**
  * RESTful Mock.
  *
  * @author Yegor Bugayenko (yegor@ymock.com)
  * @version $Id$
  */
 @Path("/mock")
 public final class RestfulMock {
 
     /**
      * Make a request and return response.
     * @param input Incoming HTTP stream
      * @return The response
      */
     @POST
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.TEXT_PLAIN)
    public javax.ws.rs.core.Response call(final String input) {
         String response;
         javax.ws.rs.core.Response.Status status;
         try {
             response = RestfulServer.INSTANCE.call(input).process(input);
             status = javax.ws.rs.core.Response.Status.OK;
         } catch (YMockException ex) {
             response = ex.getMessage();
             status = javax.ws.rs.core.Response.Status.BAD_REQUEST;
         }
         return javax.ws.rs.core.Response
             .status(status)
             .type(MediaType.TEXT_PLAIN)
             .entity(response)
             .build();
     }
 
 }
