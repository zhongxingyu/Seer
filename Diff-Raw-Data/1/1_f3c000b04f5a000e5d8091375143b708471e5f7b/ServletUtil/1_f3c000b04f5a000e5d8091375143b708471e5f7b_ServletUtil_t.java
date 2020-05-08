 /*
  * Copyright 2012 Janrain, Inc.
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
 
 package com.janrain.servlet;
 
 import com.janrain.backplane2.server.InvalidRequestException;
 
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author Tom Raney
  */
 public class ServletUtil {
 
     /**
      * Check to determine if our connection is secure at the load balancer
      * @param request
      * @return
      */
 
     public static boolean isSecure(HttpServletRequest request) {
         boolean isSecure = request.isSecure();
 
         if (!isSecure) {
             String entryProtocol = request.getHeader("x-forwarded-proto");
             if (entryProtocol != null && entryProtocol.equals("https")) {
                 isSecure = true;
             } else if (entryProtocol == null && request.getServerName().contains("localhost")) {
                 // allow testing on localhost to be considered secure
                 isSecure = true;
             }
         }
 
         return isSecure;
     }
 
     /**
      * @throws InvalidRequestException if the request is not secure, as indicated by either
      * (1) x-forwarded-proto header or (2) request URL scheme
      */
     public static void checkSecure(HttpServletRequest request) throws InvalidRequestException {
         if ( ! isSecure(request) ) {
             throw new InvalidRequestException("Connection must be made over https", HttpServletResponse.SC_FORBIDDEN);
         }
     }
 
 }
