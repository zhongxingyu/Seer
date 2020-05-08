 package org.motechproject.ananya.referencedata.csv.validator;
 
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ananya.referencedata.csv.request.FrontLineWorkerImportRequest;
 import org.motechproject.ananya.referencedata.csv.response.FrontLineWorkerImportValidationResponse;
 import org.motechproject.ananya.referencedata.flw.domain.Designation;
 import org.motechproject.ananya.referencedata.flw.domain.FrontLineWorker;
 import org.motechproject.ananya.referencedata.flw.domain.Location;
 import org.motechproject.ananya.referencedata.flw.domain.VerificationStatus;
 import org.motechproject.ananya.referencedata.flw.repository.AllFrontLineWorkers;
 import org.motechproject.ananya.referencedata.flw.utils.ValidationUtils;
 import org.motechproject.ananya.referencedata.flw.validators.Errors;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import static org.apache.commons.collections.CollectionUtils.find;
 import static org.apache.commons.collections.CollectionUtils.select;
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 import static org.motechproject.ananya.referencedata.flw.utils.PhoneNumber.*;
 
 @Component
 public class FrontLineWorkerImportRequestValidator {
     private AllFrontLineWorkers allFrontLineWorkers;
 
     @Autowired
     public FrontLineWorkerImportRequestValidator(AllFrontLineWorkers allFrontLineWorkers) {
         this.allFrontLineWorkers = allFrontLineWorkers;
     }
 
     public FrontLineWorkerImportValidationResponse validate(List<FrontLineWorkerImportRequest> requests, FrontLineWorkerImportRequest request, Location location) {
         FrontLineWorkerImportValidationResponse response = new FrontLineWorkerImportValidationResponse();
         validateDuplicates(requests, request, response);
         validateMsisdn(request, response);
         validateLocation(location, response);
         validateName(request, response);
         validateId(request, response);
         validateAlternateContactNumber(request, response);
         validateVerificationStatus(request, response);
         validateDesignation(request, response);
         return response;
     }
 
     void validateDesignation(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         String designation = request.getDesignation();
         if (StringUtils.isEmpty(designation)) {
             response.forBlankDesignation();
             return;
         }
         if(!Designation.isValid(designation)) {
             response.forInvalidDesignation();
         }
     }
 
 
     void validateVerificationStatus(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         String verificationStatus = request.getVerificationStatus();
         if (isBlank(verificationStatus)) {
             nonBlankVerificationStatusCannotBeBlanked(response, request.getMsisdn(), request.getId());
         } else {
             if (!VerificationStatus.isValid(verificationStatus)) {
                 response.forInvalidVerificationStatus();
             }
         }
     }
 
     void validateAlternateContactNumber(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         if (!isValidWithBlanksAllowed(request.getAlternateContactNumber())) {
             response.forInvalidAlternateContactNumber();
         }
     }
 
     void validateId(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         String id = request.getId();
         if (isBlank(id) || StringUtils.equals(FrontLineWorker.DEFAULT_UUID_STRING, id))
             return;
         if (!Pattern.matches(FrontLineWorker.FLW_ID_FORMAT, id)) {
             response.forInvalidId();
             return;
         }
         if (isBlank(request.getVerificationStatus())) {
             response.forMissingVerificationStatus();
             return;
         }
         validateIdFlwFromDB(request, response);
     }
 
     void validateMsisdn(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         if(isBlank(request.getMsisdn())){
             response.forMissingMsisdn();
             return;
         }
 
         if (!isValid(request.getMsisdn())) {
             response.forInvalidMsisdn();
         }
     }
 
     void validateLocation(Location location, FrontLineWorkerImportValidationResponse response) {
         if (location == null) {
             response.forInvalidLocation();
         }
     }
 
     void validateName(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         String name = request.getName();
         if (ValidationUtils.isInvalidNameWithBlankAllowed(name)) {
             response.forInvalidName();
         }
     }
 
     private void validateIdFlwFromDB(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         String id = request.getId();
 
         FrontLineWorker byFlwId = allFrontLineWorkers.getByFlwId(UUID.fromString(id));
         if (byFlwId == null) {
             response.forMissingFLW();
             return;
         }
 
         Long requestMsisdn = null;
         try {
             requestMsisdn = formatPhoneNumber(request.getMsisdn());
         } catch (NumberFormatException e) {
             response.forInvalidMsisdn();
             return;
         }
 
         if (!requestMsisdn.equals(byFlwId.getMsisdn())) {
             response.forNonMatchingMsisdn();
             return;
         }
 
         cannotUpdateIfADuplicateWithNonBlankVerificationStatusExist(request, response, byFlwId, requestMsisdn);
     }
 
     private void cannotUpdateIfADuplicateWithNonBlankVerificationStatusExist(FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response, FrontLineWorker byFlwId, Long requestMsisdn) {
         if (isBlank(byFlwId.getVerificationStatus()) && isNotBlank(request.getVerificationStatus())) {
             List<FrontLineWorker> byMsisdnWithStatus = allFrontLineWorkers.getByMsisdnWithStatus(requestMsisdn);
             if ((byMsisdnWithStatus.size() > 0))
                 response.forDuplicateRecordWithStatus();
         }
     }
 
     private void nonBlankVerificationStatusCannotBeBlanked(FrontLineWorkerImportValidationResponse response, String msisdn, String requestFlwId) {
         try {
             List<FrontLineWorker> flwsWithMsisdnAndStatusInDb = allFrontLineWorkers.getByMsisdnWithStatus(formatPhoneNumber(msisdn));
             if (flwsWithMsisdnAndStatusInDb.size() != 0) {
                 if (updateByFlwId(requestFlwId) && idsAreDifferent(requestFlwId, flwsWithMsisdnAndStatusInDb.get(0)))
                     return;
                 response.forUpdatingVerificationStatusToBlank();
             }
         } catch (NumberFormatException e) {
             response.forInvalidMsisdn();
         }
     }
 
     private boolean updateByFlwId(String flwId) {
         return Pattern.matches(FrontLineWorker.FLW_ID_FORMAT, flwId) && !FrontLineWorker.DEFAULT_UUID_STRING.equals(flwId);
     }
 
     private boolean idsAreDifferent(String flwIdInRequest, FrontLineWorker flwWithMsisdnAndStatusInDb) {
         String idForVerifiedFlwInDB = flwWithMsisdnAndStatusInDb.getFlwId().toString();
         return !idForVerifiedFlwInDB.equals(flwIdInRequest);
     }
 
     private void validateDuplicates(List<FrontLineWorkerImportRequest> requests, FrontLineWorkerImportRequest request, FrontLineWorkerImportValidationResponse response) {
         Collection flwsByMsisdn = select(requests, new FLWPredicate(request.getMsisdn()));
         if (flwsByMsisdn.size() > 1) {
             if (find(flwsByMsisdn, flwWithVerificationStatus()) != null)
                 response.forInvalidDuplicatesInCSV();
         }
     }
 
     private Predicate flwWithVerificationStatus() {
         return new Predicate() {
             @Override
             public boolean evaluate(Object o) {
                 return isNotBlank(((FrontLineWorkerImportRequest) o).getVerificationStatus());
             }
         };
     }
 
     class FLWPredicate implements Predicate {
         private String msisdn;
 
         public FLWPredicate(String msisdn) {
             super();
             this.msisdn = msisdn;
         }
 
         @Override
         public boolean evaluate(Object o) {
             return msisdn.equals(((FrontLineWorkerImportRequest) o).getMsisdn());
         }
     }
 }
