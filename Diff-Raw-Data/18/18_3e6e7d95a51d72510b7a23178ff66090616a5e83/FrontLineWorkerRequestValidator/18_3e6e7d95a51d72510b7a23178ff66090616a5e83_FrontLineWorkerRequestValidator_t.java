 package org.motechproject.ananya.referencedata.contactCenter.validator;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ananya.referencedata.contactCenter.service.FrontLineWorkerVerificationRequest;
 import org.motechproject.ananya.referencedata.flw.domain.FrontLineWorker;
 import org.motechproject.ananya.referencedata.flw.domain.VerificationStatus;
 import org.motechproject.ananya.referencedata.flw.repository.AllFrontLineWorkers;
 import org.motechproject.ananya.referencedata.flw.validators.Errors;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 @Component
 public class FrontLineWorkerRequestValidator {
     private AllFrontLineWorkers allFrontLineWorkers;
 
     @Autowired
     public FrontLineWorkerRequestValidator(AllFrontLineWorkers allFrontLineWorkers) {
         this.allFrontLineWorkers = allFrontLineWorkers;
     }
 
     public Errors validate(FrontLineWorkerVerificationRequest request) {
         Errors errors = new Errors();
         if (VerificationStatus.SUCCESS == request.getVerificationStatus())
             validateSuccessfulVerification(request, errors);
         else
             validateInvalidOtherRequest(request, errors);
 
         validateNoOtherFLWExistsWithSameMsisdnAndStatus(request, errors);
         return errors;
     }
 
     private void validateNoOtherFLWExistsWithSameMsisdnAndStatus(FrontLineWorkerVerificationRequest request, Errors errors) {
         List<FrontLineWorker> flwWithSameMsisdnAndWithSomeStatus = allFrontLineWorkers.getByMsisdnWithStatus(request.getMsisdn());
         if (flwWithSameMsisdnAndWithSomeStatus.size() == 1
                 && !flwWithSameMsisdnAndWithSomeStatus.get(0).getFlwId().equals(request.getFlwId())
                 && !request.isDummyFlwId())
            errors.add("Conflicting flw record exists. Please try again later.");
     }
 
     private void validateInvalidOtherRequest(FrontLineWorkerVerificationRequest request, Errors errors) {
         if (StringUtils.isBlank(request.getReason())) {
             errors.add("reason field has blank value");
         }
         if (request.getName() != null) {
             errors.add("name field should not be a part of the request");
         }
         if (request.getLocation() != null) {
             errors.add("location field should not be a part of the request");
         }
         if (request.getDesignation() != null) {
             errors.add("designation field should not be a part of the request");
         }
     }
 
     private void validateSuccessfulVerification(FrontLineWorkerVerificationRequest request, Errors errors) {
         if (request.getDesignation() == null) {
             errors.add("designation field has invalid/blank value");
         }
         if (StringUtils.isBlank(request.getName()) || !Pattern.matches("[a-zA-Z0-9\\s\\.]*", request.getName())) {
             errors.add("name field has invalid/blank value");
         }
         new WebRequestValidator().validateLocation(request.getLocation(), errors);
         if (request.getReason() != null) {
             errors.add("reason field should not be a part of the request");
         }
     }
 }
