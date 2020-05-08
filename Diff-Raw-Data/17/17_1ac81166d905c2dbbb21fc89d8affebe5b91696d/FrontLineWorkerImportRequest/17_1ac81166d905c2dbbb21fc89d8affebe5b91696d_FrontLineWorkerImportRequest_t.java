 package org.motechproject.ananya.referencedata.csv.request;
 
 import org.motechproject.ananya.referencedata.flw.request.LocationRequest;
 import org.motechproject.importer.annotation.ColumnName;
 
 public class FrontLineWorkerImportRequest {
     private String msisdn;
     private String name;
     private String designation;
     private LocationRequest location = new LocationRequest();
     private String verificationStatus;
     private String id;
     private String alternateContactNumber;
 
     public FrontLineWorkerImportRequest() {
     }
 
     public FrontLineWorkerImportRequest(String id, String msisdn, String alternateContactNumber, String name, String designation, String verificationStatus, LocationRequest location) {
         this.msisdn = msisdn;
         this.name = name;
         this.designation = designation;
         this.location = location;
         this.id = id;
         this.alternateContactNumber = alternateContactNumber;
         this.verificationStatus = verificationStatus;
     }
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDesignation() {
         return designation;
     }
 
     public LocationRequest getLocation() {
         return location;
     }
 
     public String getVerificationStatus() {
         return verificationStatus;
     }
 
     public String getId() {
         return id;
     }
 
     public String getAlternateContactNumber() {
         return alternateContactNumber;
     }
 
     public void setMsisdn(String msisdn) {
         this.msisdn = msisdn;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setDesignation(String designation) {
         this.designation = designation;
     }
 
     public void setLocation(LocationRequest location) {
         this.location = location;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     @ColumnName(name = "district")
     public void setDistrict(String district) {
         location.setDistrict(district);
     }
 
     @ColumnName(name = "block")
     public void setBlock(String block) {
         location.setBlock(block);
     }
 
     @ColumnName(name = "panchayat")
     public void setPanchayat(String panchayat) {
         location.setPanchayat(panchayat);
     }
 
     @ColumnName(name = "state")
     public void setState(String state) {
         location.setState(state);
     }
 
    @ColumnName(name = "verification_status")
     public void setVerificationStatus(String verificationStatus) {
         this.verificationStatus = verificationStatus;
     }
 
    @ColumnName(name = "alternate_contact_number")
     public void setAlternateContactNumber(String alternateContactNumber) {
         this.alternateContactNumber = alternateContactNumber;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof FrontLineWorkerImportRequest)) return false;
 
         FrontLineWorkerImportRequest that = (FrontLineWorkerImportRequest) o;
 
         if (msisdn != null ? !msisdn.equals(that.msisdn) : that.msisdn != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return msisdn != null ? msisdn.hashCode() : 0;
     }
 
     public String toCSV() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                id, msisdn, alternateContactNumber, name, designation, verificationStatus, location.getState(),
                 location.getDistrict(), location.getBlock(), location.getPanchayat());
     }
 
 }
