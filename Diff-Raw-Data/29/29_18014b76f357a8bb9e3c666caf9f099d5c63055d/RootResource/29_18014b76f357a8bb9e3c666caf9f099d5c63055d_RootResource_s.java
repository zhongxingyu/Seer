 package com.ttech.cordovabuild.web;
 
 import com.ttech.cordovabuild.domain.application.Application;
import com.ttech.cordovabuild.domain.application.ApplicationRepository;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import org.springframework.beans.factory.annotation.Autowired;
 
 @Path("/")
 public class RootResource {
 
     @Autowired
    ApplicationRepository applicationRepository;
 
     @GET
     @Path("/application/{id}")
     Application getApplication(@PathParam("id") Long id) {
        return applicationRepository.findById(id);
     }
 }
