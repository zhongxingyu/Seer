 package ${package}.gwt.server.service;
 
 import org.springframework.stereotype.Service;
 
import acme.gwt.client.service.ApplicationService;
 
 @Service
 public class ApplicationServiceImpl implements ApplicationService {
     public String hello() {
         return "Hello world (from spring service) !";
     }
 }
