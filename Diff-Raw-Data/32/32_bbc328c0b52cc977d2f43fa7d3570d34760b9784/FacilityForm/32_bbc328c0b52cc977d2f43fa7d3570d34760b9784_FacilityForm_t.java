 package org.motechproject.ghana.national.web.form;
 
 public class FacilityForm {
     private String id;
     private String name;
     private String country;
     private String region;
     private String countyDistrict;
     private String stateProvince;
     private String phoneNumber;
     private String additionalPhoneNumber1;
     private String additionalPhoneNumber2;
     private String additionalPhoneNumber3;
 
     private String motechId;
 
     public FacilityForm(String id, String motechId, String name, String country, String region, String countyDistrict, String stateProvince, String phoneNumber, String additionalPhoneNumber1, String additionalPhoneNumber2, String additionalPhoneNumber3) {
         this.id = id;
         this.name = name;
         this.country = country;
         this.region = region;
         this.countyDistrict = countyDistrict;
         this.stateProvince = stateProvince;
         this.phoneNumber = phoneNumber;
         this.additionalPhoneNumber1 = additionalPhoneNumber1;
         this.additionalPhoneNumber2 = additionalPhoneNumber2;
         this.additionalPhoneNumber3 = additionalPhoneNumber3;
         this.motechId = motechId;
     }
 
     public FacilityForm() {
     }
 
     public String getName() {
        return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getCountry() {
        return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public String getRegion() {
        return region;
     }
 
     public void setRegion(String region) {
         this.region = region;
     }
 
     public String getCountyDistrict() {
        return countyDistrict;
     }
 
     public void setCountyDistrict(String countyDistrict) {
         this.countyDistrict = countyDistrict;
     }
 
     public String getStateProvince() {
        return stateProvince;
     }
 
     public void setStateProvince(String stateProvince) {
         this.stateProvince = stateProvince;
     }
 
     public String getPhoneNumber() {
        return phoneNumber;
     }
 
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     public String getAdditionalPhoneNumber1() {
        return additionalPhoneNumber1;
     }
 
     public void setAdditionalPhoneNumber1(String additionalPhoneNumber1) {
         this.additionalPhoneNumber1 = additionalPhoneNumber1;
     }
 
     public String getAdditionalPhoneNumber2() {
        return additionalPhoneNumber2;
     }
 
     public void setAdditionalPhoneNumber2(String additionalPhoneNumber2) {
         this.additionalPhoneNumber2 = additionalPhoneNumber2;
     }
 
     public String getAdditionalPhoneNumber3() {
        return additionalPhoneNumber3;
     }
 
     public void setAdditionalPhoneNumber3(String additionalPhoneNumber3) {
         this.additionalPhoneNumber3 = additionalPhoneNumber3;
     }
 
     public String getId() {
        return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setMotechId(String motechId) {
         this.motechId = motechId;
     }
 
     public String getMotechId() {
        return motechId;
     }
 }
