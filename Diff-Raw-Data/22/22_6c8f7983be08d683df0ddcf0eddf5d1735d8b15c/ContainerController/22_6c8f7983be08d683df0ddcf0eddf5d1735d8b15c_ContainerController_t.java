 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.ocsys.web.controller;
 
 import com.posabro.ocsys.domain.Container;
 import com.posabro.ocsys.services.ContainerService;
 import com.posabro.web.controller.ValidationController;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author Carlos Juarez
  */
 @Controller
@RequestMapping("/containerController/*")
 public class ContainerController extends ValidationController{
     
     final org.slf4j.Logger logger = LoggerFactory.getLogger(ContainerController.class);
     
     @Autowired
     private ContainerService containerService;
     
     @RequestMapping(value="findContainer", method = RequestMethod.POST)
    public @ResponseBody Container findContainer(@RequestBody String containerId){
        logger.debug("finding container : " + containerId);
         Container foundContainer = containerService.findContainer(containerId);
         return foundContainer;
     }
     
 }
