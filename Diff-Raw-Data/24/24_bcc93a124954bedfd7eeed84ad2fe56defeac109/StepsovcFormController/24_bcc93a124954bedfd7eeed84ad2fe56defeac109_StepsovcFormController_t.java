 package org.wv.stepsovc.web.controllers;
 
 import org.apache.log4j.Logger;
 import org.motechproject.casexml.service.CaseService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.wv.stepsovc.web.request.CustomForm;
 
 @Controller
 @RequestMapping("/form/**")
 public class StepsovcFormController extends CaseService<CustomForm> {
 
     private static Logger logger = Logger.getLogger(StepsovcFormController.class);
 
     public StepsovcFormController() {
        super(CustomForm.class);
     }
 
     @Override
     public void closeCase(CustomForm ccCase) {
     }
 
     @Override
     public void updateCase(CustomForm ccCase) {
     }
 
     @Override
     public void createCase(CustomForm ccCase) {
         logger.info("Inside create form");
     }
 }
