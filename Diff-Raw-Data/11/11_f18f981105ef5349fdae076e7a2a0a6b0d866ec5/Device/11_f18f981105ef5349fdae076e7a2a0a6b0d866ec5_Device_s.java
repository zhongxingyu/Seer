 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.kissy.gcm.rest.server.model;
 
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.index.Indexed;
 
 import java.util.Date;
 
 /**
  * Device info.
  *
  * @author Guillaume Le Biller (<i>lebiller@ekino.com</i>)
  * @version $Id$
  */
 public class Device {
     @Id
     private String id;
    @Indexed
     private String application;
     private String registration;
     private Date date;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getApplication() {
         return application;
     }
 
     public void setApplication(String application) {
         this.application = application;
     }
 
     public String getRegistration() {
         return registration;
     }
 
     public void setRegistration(String registration) {
         this.registration = registration;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 }
