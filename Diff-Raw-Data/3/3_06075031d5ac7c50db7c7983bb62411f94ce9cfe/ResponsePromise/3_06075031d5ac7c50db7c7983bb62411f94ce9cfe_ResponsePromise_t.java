 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.paxxis.cornerstone.common;
 
 import com.paxxis.cornerstone.base.ResponseMessage;
 
 
 
 /**
  * A response promise which is an abstraction over a DataLatch that enforces type safety
  * and provides the ability for clients of services to set reasonable timeouts. Response promises
  * are failfast by default meaning if for any reason a valid response cannot be returned then a
  * runtime exception is thrown.
  */
 public class ResponsePromise<RESP extends ResponseMessage<?>> extends DataLatch<RESP> {
 
     private long timeout;
     private boolean failfast;
     private RuntimeException exception;
     
     public ResponsePromise() {
         this(10000, true);
     }
     
     public ResponsePromise(long timeout) {
         this(timeout, true);
     }
     
     public ResponsePromise(long timeout, boolean failfast) {
         this.timeout = timeout;
         this.failfast = failfast;
     }
     
     public RESP getResponse(long timeout, boolean failfast) {
         RESP response = (RESP) waitForObject(timeout, failfast);
        //our response maybe null if failfast is false
        if (response != null && response.isError()) {
             this.exception = new ResponseException(response);
         }
         if (failfast && this.exception != null) {
             throw this.exception;
         }
         return response;
     }
     
     public RESP getResponse(long timeout) {
         return getResponse(timeout, this.failfast);
     }
     
     public RESP getResponse(boolean failfast) {
         return getResponse(this.timeout, failfast);
     }
     
     public RESP getResponse() {
         return getResponse(this.timeout, this.failfast);
     }
 
     public boolean hasResponse() {
         return hasObject();
     }
     
     @Override
     public synchronized RuntimeException getException() {
         if (this.exception != null) {
             return this.exception;
         }
         return super.getException();
     }
     /**
      * Enforce the timeout value provided at construct time
      */
     @Override
     public RESP waitForObject() {
         return getResponse(this.timeout, this.failfast);
     }
     
     /**
      * Enforce the failfast value provided at construct time
      */
     @Override
     public RESP waitForObject(long timeout) {
         return getResponse(timeout, this.failfast);
     }
 
     /**
      * Enforce the failfast value provided at construct time
      */
     @Override
     public RESP waitForObject(boolean failfast) {
         return getResponse(this.timeout, failfast);
     }
 
 }
 
