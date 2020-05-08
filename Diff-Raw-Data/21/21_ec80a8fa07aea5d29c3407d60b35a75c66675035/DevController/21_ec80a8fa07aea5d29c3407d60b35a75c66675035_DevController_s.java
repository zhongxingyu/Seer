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
 
 package no.dusken.momus.controller;
 
 import no.dusken.momus.authentication.AuthUserDetails;
 import no.dusken.momus.authentication.Token;
 import no.dusken.momus.authentication.UserAuthorities;
 import no.dusken.momus.authentication.UserLoginService;
 import no.dusken.momus.smmdb.Syncer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Dev only, not accessible when live
  * Utility methods etc. goes here.
  */
 @Controller
 @RequestMapping("/dev")
 public class DevController {
 
     private Logger logger = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private UserAuthorities userAuthorities;
 
     @Autowired
     private UserLoginService userLoginService;
 
     @Autowired
     private Syncer syncer;
 
     /**
      * Bypass login and logs you in as the user with the provided id
      */
     @RequestMapping("/login/{id}")
     public @ResponseBody void login(@PathVariable("id") Long id) {
         AuthUserDetails user = userAuthorities.getAuthoritiesForUser(id);
         Token token = new Token(null, user);
         SecurityContextHolder.getContext().setAuthentication(token);
     }
 
     /**
      * Syncs stuff from SmmDB
      */
     @RequestMapping("/sync")
     public @ResponseBody String sync() {
         syncer.sync();
         return "sync ok";
     }
 
     @RequestMapping("/test")
     @PreAuthorize("hasRole('momus:teeest')")
     public @ResponseBody String test() {
         return "ok";
     }
 
     @RequestMapping("/editor")
     @PreAuthorize("hasRole('momus:editor')")
     public @ResponseBody String editor() {
         return "editor ok";
     }
 }
