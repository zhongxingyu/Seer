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
@Controller("rest")
 public class RestInterfaceController {
     @Autowired
     AuthenticationService auth;
     
     @Autowired
     LocalizationService localizationService;
     
    @RequestMapping(method = RequestMethod.POST, value = "/localization/")
     @ResponseBody
     public LocalizationResponse localizeByDefaultMethod(@ModelAttribute MeasurementContainer measurementContainer) {
         if(!auth.authenticate(measurementContainer.getUsername(), measurementContainer.getPassword())) {
             LocalizationResponse response = new LocalizationResponse();
             response.setPlaceName("");
             response.setAuthenticationSuccessful(false);
             return response;
         }
         return localizationService.localize(measurementContainer);
     }
     
 }
