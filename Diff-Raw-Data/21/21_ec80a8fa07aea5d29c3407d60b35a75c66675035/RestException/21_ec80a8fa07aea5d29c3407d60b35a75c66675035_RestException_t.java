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
 
 /**
  * Can be thrown when an error should be shown to the user with a specific http code
 *
 * OBS: RestExceptions are not logged. If logging is needed, it should be done before the exception is being thrown.
  */
 public class RestException extends RuntimeException {
     private String message;
     private int status;
 
     /**
      *
      * @param message Message to show the user
      * @param status One of HttpServletResponse
      */
     public RestException(String message, int status) {
         this.message = message;
         this.status = status;
     }
 
     @Override
     public String getMessage() {
         return message;
     }
 
     public int getStatus() {
         return status;
     }
 }
