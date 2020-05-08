 package com.ttech.cordovabuild.web;
 
 import com.ttech.cordovabuild.domain.application.Application;

 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;

import com.ttech.cordovabuild.domain.application.ApplicationService;
 import org.springframework.beans.factory.annotation.Autowired;
 
 @Path("/")
 public class RootResource {
 
     @Autowired
    ApplicationService applicationService;
 
     @GET
     @Path("/application/{id}")
     Application getApplication(@PathParam("id") Long id) {
        return applicationService.findApplication(id);
     }
 }
