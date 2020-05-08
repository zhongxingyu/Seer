 /*
  * Copyright 2013 - Elian ORIOU
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
 
 package fr.webmarket.backend.model;
 
 /**
  * User: walien
  * Date: 18/04/13
  * Time: 23:45
  */
 public class Session {
 
     private User user;
     private String id;
 
     public Session() {
        this(null, null);
     }
 
     public Session(User user, String sessionID) {
         this.user = user;
         this.id = sessionID;
     }
 
     public User getUser() {
         return user;
     }
 
     public Session setUser(User user) {
         this.user = user;
         return this;
     }
 
     public String getId() {
         return id;
     }
 
     public Session setId(String id) {
         this.id = id;
         return this;
     }
 
     @Override
     public String toString() {
         return "Session{" +
                 "user=" + user +
                 ", id='" + id + '\'' +
                 '}';
     }
 }
