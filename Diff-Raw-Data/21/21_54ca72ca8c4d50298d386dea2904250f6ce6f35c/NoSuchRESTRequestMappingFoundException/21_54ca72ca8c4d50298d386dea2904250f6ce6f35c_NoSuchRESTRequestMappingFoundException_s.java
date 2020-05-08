 package org.faziz.assignment.web;
 
 /**
  *
  * @author faziz
  */
class NoSuchRESTRequestMappingFoundException extends RuntimeException {
 
     public NoSuchRESTRequestMappingFoundException(String actionRequest) {
         super(actionRequest);
     }
     
 }
