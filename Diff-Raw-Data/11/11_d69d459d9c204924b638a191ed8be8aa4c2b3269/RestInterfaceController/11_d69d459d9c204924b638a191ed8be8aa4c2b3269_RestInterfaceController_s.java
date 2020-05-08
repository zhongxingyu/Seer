 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package acs.fluffy.controller;
 
 import acs.fluffy.form.MeasurementForm;
 import acs.fluffy.restinterface.authentication.AuthenticationService;
 import acs.fluffy.restinterface.domain.LocalizationResponse;
 import acs.fluffy.restinterface.domain.MeasurementContainer;
 import acs.fluffy.service.LocalizationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author tonykova
  */
 @Controller
 @RequestMapping("rest")
 public class RestInterfaceController {
     @Autowired
     AuthenticationService auth;
     
     @Autowired
     LocalizationService localizationService;
     
    @RequestMapping(value = "/localization")
     @ResponseBody
    public LocalizationResponse localizeByDefaultMethod(@ModelAttribute MeasurementContainer measurementContainer) {
         System.out.println("in method");
         if(!auth.authenticate(measurementContainer.getUsername(), measurementContainer.getPassword())) {
             System.out.println("in if");
             LocalizationResponse response = new LocalizationResponse();
             response.setPlaceName("");
             response.setAuthenticationSuccessful(false);
             return response;
         }
         System.out.println("not in if");
         return localizationService.localize(measurementContainer);
     }
     
 }
