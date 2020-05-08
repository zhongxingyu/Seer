 /*
  * Copyright 2013 Studentmediene i Trondheim AS
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package no.dusken.momus.exceptions;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * All exceptions while processing a request will be sent to this handler.
  * Will make a proper http response out of it, can log it here etc.
  */
 public class ExceptionHandler implements HandlerExceptionResolver {
 
     Logger logger = LoggerFactory.getLogger(getClass());
 
     @Override
     public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, Exception e) {
 
         if (e instanceof RestException) { // If it's our exception, we know how to handle it and has set a status
             response.setStatus(((RestException) e).getStatus());
         } else if(e instanceof AccessDeniedException) { // let Spring handle it by throwing it again
             throw (AccessDeniedException) e;
         } else if (e instanceof AuthenticationException) { // let Spring handle it, is a failed login
             throw (AuthenticationException) e;
         } else { // Something else, log it and set status to internal server error
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             logException(e);
         }
 
         ModelAndView mav = new ModelAndView(new MappingJackson2JsonView());
        mav.addObject("error", e.getMessage());
 
         return mav;
     }
 
     private void logException(Exception e) {
         logger.warn("Exceptionhandler caught:", e);
     }
 }
