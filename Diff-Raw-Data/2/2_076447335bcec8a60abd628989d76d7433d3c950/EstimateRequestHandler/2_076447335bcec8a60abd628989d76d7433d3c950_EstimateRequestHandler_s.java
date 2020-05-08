 /*
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  * Copyright @2011 the original author or authors.
  */
 package com.agile_coder.poker.server.rest;
 
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.httpclient.HttpStatus;
 
 import com.agile_coder.poker.server.SubmitException;
 import com.agile_coder.poker.server.model.Estimate;
 import com.agile_coder.poker.server.model.Session;
 
 @Path("/{name}/{estimate}")
 public class EstimateRequestHandler {
 
     @PUT
     public Response addEstimate(
             @PathParam("name") String name,
             @PathParam("estimate") String estimate) {
         Session session = Session.getInstance();
         try {
             Estimate estimateVal;
             if (isInteger(estimate)) {
                 estimateVal = Estimate.fromInt(Integer.valueOf(estimate));
             } else {
                 estimateVal = Estimate.valueOf(estimate.toUpperCase());
             }
             session.addEstimate(name, estimateVal);
         } catch (SubmitException e) {
             return Response.serverError().status(HttpStatus.SC_CONFLICT).build();
         }
         return Response.noContent().build();
     }
 
     private boolean isInteger(String vote) {
         try {
             Integer.parseInt(vote);
         } catch (NumberFormatException e) {
             return false;
         }
         return true;
     }
 
 }
