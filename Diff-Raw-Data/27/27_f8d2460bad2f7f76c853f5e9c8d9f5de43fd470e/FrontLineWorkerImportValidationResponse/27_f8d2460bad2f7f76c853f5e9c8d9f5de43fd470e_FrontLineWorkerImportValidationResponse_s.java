 package org.motechproject.ananya.referencedata.csv.response;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FrontLineWorkerImportValidationResponse {
     private List<String> message = new ArrayList<String>();
     private boolean isValid = true;
 
     public void forInvalidMsisdn() {
         isValid = false;
         addMessage("Invalid msisdn");
     }
 
     public void forDuplicateRecordWithStatus() {
         isValid = false;
         addMessage("Flw with same Msisdn having non blank verification status already present");
     }
 
     public void forNonMatchingMsisdn() {
         isValid = false;
         addMessage("Msisdn is not matching with the record in DB for the given FLW ID");
     }
 
 
     public void forMissingFLW() {
         isValid = false;
         addMessage("FLW with given id not found in DB");
     }
 
     public void forInvalidAlternateContactNumber() {
         isValid = false;
         addMessage("Invalid alternate contact number");
     }
 
     public void forInvalidId() {
         isValid = false;
         addMessage("Invalid id");
     }
 
     public void forInvalidVerificationStatus() {
         isValid = false;
         addMessage("Invalid verification status");
     }
 
     public void forUpdatingVerificationStatusToBlank() {
         isValid = false;
         addMessage("Cannot update non blank verification status to blank");
     }
 
     public void forInvalidLocation() {
         isValid = false;
         addMessage("Invalid location");
     }
 
     public void forInvalidName() {
         isValid = false;
         addMessage("Invalid name");
     }
 
     public void forMissingVerificationStatus() {
         isValid = false;
         addMessage("Verification Status cannot be blank when FLW ID is given");
     }
 
     public void forInvalidDuplicatesInCSV() {
         isValid = false;
         addMessage("Duplicate with verification status found in CSV");
     }
 
     public boolean isValid() {
         return isValid;
     }
 
     public List<String> getMessage() {
         return message;
     }
 
     @Override
     public String toString() {
         return message.toString();
     }
 
     public boolean isInValid() {
         return !isValid;
     }
 
     private void addMessage(String message) {
         if(!this.message.contains(message))
             this.message.add(message);
     }
 
     public void forInvalidDesignation() {
         isValid = false;
         addMessage("Designation field has invalid value");
     }
 
     public void forBlankDesignation() {
         isValid = false;
         addMessage("Designation field is missing");
     }
 }
