 package org.motechproject.whp.reports.webservice.controller;
 
 import org.motechproject.validation.validator.BeanValidator;
 import org.motechproject.whp.reports.builder.ContainerRegistrationCallLogMapper;
 import org.motechproject.whp.reports.contract.ContainerRegistrationCallDetailsLogRequest;
 import org.motechproject.whp.reports.contract.ContainerVerificationLogRequest;
 import org.motechproject.whp.reports.contract.ProviderVerificationLogRequest;
 import org.motechproject.whp.reports.service.ContainerRegistrationCallLogService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 @Controller
 @RequestMapping("/containerRegistrationCallLog")
 public class ContainerRegistrationCallLogController extends BaseController {
 
     private ContainerRegistrationCallLogService containerRegistrationCallLogService;
     private ContainerRegistrationCallLogMapper containerRegistrationCallLogMapper;
     private BeanValidator beanValidator;
 
     @Autowired
     public ContainerRegistrationCallLogController(ContainerRegistrationCallLogService containerRegistrationCallLogService, ContainerRegistrationCallLogMapper containerRegistrationCallLogMapper, BeanValidator beanValidator) {
         this.containerRegistrationCallLogService = containerRegistrationCallLogService;
         this.containerRegistrationCallLogMapper = containerRegistrationCallLogMapper;
         this.beanValidator = beanValidator;
     }
 
     @RequestMapping(value = "updateCallDetails", method = RequestMethod.POST)
     @ResponseBody
     public List<ObjectError> create(@RequestBody ContainerRegistrationCallDetailsLogRequest request, HttpServletResponse response) {
         BeanPropertyBindingResult result = new BeanPropertyBindingResult(request, request.getClass().getSimpleName());
         beanValidator.validate(request, "", result);
 
         if(result.hasErrors()){
             response.setStatus(500);
             return result.getAllErrors();
         }
 
         containerRegistrationCallLogService.save(containerRegistrationCallLogMapper.mapFromCallDetails(request));
         return null;
     }
 
     @RequestMapping(value = "providerVerification", method = RequestMethod.POST)
     @ResponseBody
     public void create(@RequestBody ProviderVerificationLogRequest request) {
         containerRegistrationCallLogService.save(containerRegistrationCallLogMapper.mapFromProviderVerificationDetails(request));
     }
 
     @RequestMapping(value = "containerVerification", method = RequestMethod.POST)
     @ResponseBody
     public void create(@RequestBody ContainerVerificationLogRequest request) {
         containerRegistrationCallLogService.save(containerRegistrationCallLogMapper.mapFromContainerVerificationDetails(request));
     }
 }
