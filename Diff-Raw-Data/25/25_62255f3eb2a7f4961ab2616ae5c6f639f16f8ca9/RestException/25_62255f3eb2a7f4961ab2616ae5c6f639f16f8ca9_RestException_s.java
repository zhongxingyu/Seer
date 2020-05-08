 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fusesource.camel.component.salesforce.api;
 
 import org.apache.camel.CamelException;
 import org.fusesource.camel.component.salesforce.api.dto.RestError;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class RestException extends CamelException {
 
     private List<RestError> errors;

     private int statusCode;
 
     public RestException(List<RestError> errors, int statusCode) {
         this(toErrorMessage(errors, statusCode), statusCode);
 
         this.errors = errors;
     }
 
     public RestException(String message, int statusCode) {
         super(message);
 
         this.statusCode = statusCode;
     }
 
     public RestException(String message, Throwable cause) {
         super(message, cause);
     }
 
     public RestException(Throwable cause) {
         super(cause);
     }
 
     public List<RestError> getErrors() {
         return Collections.unmodifiableList(errors);
     }
 
     public void setErrors(List<RestError> errors) {
         if (this.errors != null) {
             this.errors.clear();
         } else {
             this.errors = new ArrayList<RestError>();
         }
         this.errors.addAll(errors);
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(int statusCode) {
         this.statusCode = statusCode;
     }
 
     @Override
     public String toString() {
        return toErrorMessage(this.errors, this.statusCode);
     }
 
     private static String toErrorMessage(List<RestError> errors, int statusCode) {
        StringBuilder builder = new StringBuilder("{ errors: [");
         if (errors != null) {
             for (RestError error : errors) {
                 builder.append(error.toString());
             }
         }
        builder.append(" ] statusCode: ");
         builder.append(statusCode);
         builder.append("}");
 
         return builder.toString();
     }
 
 }
