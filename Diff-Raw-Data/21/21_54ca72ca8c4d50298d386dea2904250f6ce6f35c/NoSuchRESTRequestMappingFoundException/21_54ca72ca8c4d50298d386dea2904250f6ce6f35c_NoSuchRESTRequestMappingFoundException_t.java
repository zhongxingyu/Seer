 package org.faziz.assignment.web;
 
 /**
  *
  * @author faziz
  */
public class NoSuchRESTRequestMappingFoundException extends RuntimeException {
 
     public NoSuchRESTRequestMappingFoundException(String actionRequest) {
         super(actionRequest);
     }
     
 }
