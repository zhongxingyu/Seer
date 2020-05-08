 package org.motechproject.care.service;
 
 import org.motechproject.care.request.CareCase;
 import org.motechproject.care.request.CaseType;
 import org.motechproject.casexml.service.CaseService;
 import org.motechproject.casexml.service.exception.CaseException;
 import org.motechproject.util.StringUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping("/care/**")
 public class CareCaseService extends CaseService<CareCase>{
 
     private MotherService motherService;
     private ChildService childService;
 
     @Autowired
     public CareCaseService(MotherService motherService, ChildService childService) {
         super(CareCase.class);
         this.motherService = motherService;
         this.childService = childService;
     }
 
     @Override
     public void createCase(CareCase careCase) throws CaseException{
         validateCreateCase(careCase);
        if(careCase.getCase_type().equals(CaseType.Mother.getType()))
             motherService.process(careCase);
        else
             childService.process(careCase);
     }
 
     @Override
     public void updateCase(CareCase careCase)  throws CaseException{
     }
 
     @Override
     public void closeCase(CareCase careCase) throws CaseException{
         validateCloseCase(careCase);
         boolean wasMotherClosed = motherService.closeCase(careCase.getCase_id());
         if(!wasMotherClosed)
             childService.closeCase(careCase.getCase_id());
     }
 
     private void validateCreateCase(CareCase careCase) throws CaseException {
         validateMandatory(careCase.getCase_id(), "Case Id");
         validateMandatory(careCase.getOwner_id(), "Owner Id");
     }
 
     private void validateCloseCase(CareCase careCase) throws CaseException {
         validateMandatory(careCase.getCase_id(), "Case Id");
     }
 
     private void validateMandatory(String fieldValue, String fieldName) throws CaseException {
         if(StringUtil.isNullOrEmpty(fieldValue)) {
             throw new CaseException(fieldName + " is a mandatory field.", HttpStatus.BAD_REQUEST);
         }
     }
 }
