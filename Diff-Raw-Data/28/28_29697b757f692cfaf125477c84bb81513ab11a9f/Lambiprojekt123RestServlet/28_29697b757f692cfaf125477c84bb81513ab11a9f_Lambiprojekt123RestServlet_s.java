package com.grallandco.lambiprojekt123.service.rest.impl;
import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 @Path("/hr/")
public class CanditateResource {
 	  
 	 @GET
 	 @Produces("text/plain")  //("application/json")
 	 @Path("/canditate")
 	 public String getCanditate() {
 	        return "Hello World!";
     }
 }
