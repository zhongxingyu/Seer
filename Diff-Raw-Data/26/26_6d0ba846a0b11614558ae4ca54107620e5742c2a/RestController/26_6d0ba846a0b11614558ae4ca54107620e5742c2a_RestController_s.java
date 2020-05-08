 /*
  * Copyright 2012 Alexander Shabanov - http://alexshabanov.com.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.alexshabanov.springrestapi.rest.controller;
 
 import com.alexshabanov.springrestapi.rest.common.IdHolder;
 import com.alexshabanov.springrestapi.service.UserService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.inject.Inject;
 
 import static com.alexshabanov.springrestapi.rest.common.RestConstants.REGISTER_USER_URI;
 
 /**
  * Exposes REST API methods for user api.
  */
 @Controller
 public final class RestController {
 
     private final UserService userService;
 
     @Inject
     public RestController(UserService userService) {
         this.userService = userService;
     }
 
     @RequestMapping(value = REGISTER_USER_URI, method = RequestMethod.POST)
     @ResponseBody
    public IdHolder registerUser(@RequestBody String name) {
        return IdHolder.as(userService.register(name));
     }
 }
