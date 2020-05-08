 /*
  *    Copyright 2013 Weald Technology Trading Limited
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.wealdtech.jersey.providers;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.wealdtech.errors.ErrorInfo;
 import com.wealdtech.errors.ErrorInfoMap;
 import com.wealdtech.jackson.ObjectMapperFactory;
 import com.wealdtech.jersey.exceptions.HttpException;
 
 /**
  * Convert exceptions in to a suitable JSON response for clients.
  * <p/>As part of this process
  */
 @Provider
 public class WealdExceptionMapper implements ExceptionMapper<HttpException>
 {
   private static final Logger LOGGER = LoggerFactory.getLogger(WealdExceptionMapper.class);
 
   private static transient final ObjectMapper mapper = ObjectMapperFactory.getDefaultMapper();
 
   @Override
   public Response toResponse(final HttpException exception)
   {
     ResponseBuilder builder = Response.status(exception.getStatus())
                                       .entity(statusToJSON(exception.getMessage()))
                                       .type(MediaType.APPLICATION_JSON);
 
     if (exception.getRetryAfter().isPresent())
     {
       builder.header("Retry-After", exception.getRetryAfter().get());
     }
 
     return builder.build();
   }
 
   private String statusToJSON(final String message)
   {
     // See if our message is really an error code
     ErrorInfo status = ErrorInfoMap.get(message);
     if (status == null)
     {
      status =new ErrorInfo(message, null, null, null);
     }
     try
     {
       return mapper.writeValueAsString(status);
     }
     catch (JsonProcessingException e)
     {
       LOGGER.error("Failed to generate JSON for status \"{}\"", status);
       return "{\"message\":\"" + message + "\"}";
     }
   }
 }
