 package web.controller;
 
 
 import facade.iGeoFacade;
 import org.springframework.beans.factory.annotation.Autowired;
 
public class AbstractController {
        
    @Autowired protected  iGeoFacade geofacade;   
     
 }
 
 
