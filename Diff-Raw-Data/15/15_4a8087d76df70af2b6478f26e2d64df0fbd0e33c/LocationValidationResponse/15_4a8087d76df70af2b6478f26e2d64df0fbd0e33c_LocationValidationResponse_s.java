 package org.motechproject.ananya.referencedata.csv.response;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class LocationValidationResponse {
     private List<String> message = new ArrayList<String>();
     private boolean isValid = true;
 
     public void forBlankFieldsInLocation() {
         isValid = false;
         this.message.add("Blank district, block or panchayat");
     }
 
     public void forDuplicateLocation() {
         isValid = false;
         this.message.add("Duplicate location in CSV file");
     }
 
     public void forInvalidLocation() {
         isValid = false;
         this.message.add("Invalid location");
     }
 
     public void forInvalidStatus() {
         isValid = false;
         this.message.add("Blank or Invalid status");
     }
 
     public void forBlankAlternateLocation() {
         this.isValid = false;
        this.message.add("Location is invalid and does not have an alternate location");
     }
 
     public void forInvalidAlternateLocation() {
         this.isValid = false;
        this.message.add("Location is invalid and has an invalid alternate location");
     }
 
     public void forLocationNotExisting() {
         this.isValid = false;
         this.message.add("Location is not present in DB");
     }
 
     public void forCannotTransitionFromExistingState() {
         this.isValid = false;
         this.message.add("Location cannot be updated to the specified status");
     }
 
     public void forLocationExisting() {
         this.isValid = false;
         this.message.add("Location is already present in DB");
     }
 
     public void forNeedlessAlternateLocation() {
         this.isValid = false;
         this.message.add("Alternate location provided when not required");
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
 }
