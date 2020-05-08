 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.interactuamovil.apps.contactosms.api.client.rest.contacts;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
 import com.interactuamovil.apps.contactosms.api.enums.AddedFrom;
 import com.interactuamovil.apps.contactosms.api.utils.JsonContactStatusDeserializer;
 import com.interactuamovil.apps.contactosms.api.utils.JsonObject;
 import com.interactuamovil.apps.contactosms.api.enums.ContactStatus;
 import java.io.IOException;
 import java.util.List;
 
 /**
  *
  * @author sergeiw
  */
 public class ContactJsonObject extends JsonObject {
     
     @JsonProperty(value="msisdn")
     private String msisdn;
     @JsonProperty(value="phone_number")
     private String phoneNumber;
     @JsonProperty(value="country_code")
     private String countryCode;
     @JsonProperty(value="first_name")
     private String firstName;
     @JsonProperty(value="last_name")
     private String lastName;
     @JsonProperty(value="full_name")
     private String fullName;
     @JsonProperty(value="email")
     private String email;
     @JsonProperty(value="status")
     //@JsonDeserialize(using = JsonContactStatusDeserializer.class)
     private ContactStatus status;
     @JsonProperty(value="added_from")
     private AddedFrom addedFrom;
     
     @JsonProperty(value="custom_field_1")
     private String customField1;
     @JsonProperty(value="custom_field_2")
     private String customField2;
     @JsonProperty(value="custom_field_3")
     private String customField3;
     @JsonProperty(value="custom_field_4")
     private String customField4;
     @JsonProperty(value="custom_field_5")
     private String customField5;
     
     @JsonProperty(value="tags")
     private List<String> tags;
 
     public static ContactJsonObject fromJson(String json) throws IOException {
         return JsonObject.fromJson(json, ContactJsonObject.class);        
     }    
     
     /**
      * @return the msisdn
      */
     public String getMsisdn() {
         return msisdn;
     }
 
     /**
      * @param msisdn the msisdn to set
      */
     public void setMsisdn(String msisdn) {
         this.msisdn = msisdn;
     }
 
     /**
      * @return the firstName
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * @param firstName the firstName to set
      */
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     /**
      * @return the lastName
      */
     public String getLastName() {
         return lastName;
     }
 
     /**
      * @param lastName the lastName to set
      */
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     /**
      * @return the status
      */
     public ContactStatus getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     @JsonDeserialize(using = JsonContactStatusDeserializer.class)
     public void setStatus(ContactStatus status) {
         this.status = status;
     }
 
     /**
      * @return the countryCode
      */
     public String getCountryCode() {
         return countryCode;
     }
 
     /**
      * @param countryCode the countryCode to set
      */
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     /**
      * @return the customField1
      */
     public String getCustomField1() {
         return customField1;
     }
 
     /**
      * @param customField1 the customField1 to set
      */
     public void setCustomField1(String customField1) {
         this.customField1 = customField1;
     }
 
     /**
      * @return the customField2
      */
     public String getCustomField2() {
         return customField2;
     }
 
     /**
      * @param customField2 the customField2 to set
      */
     public void setCustomField2(String customField2) {
         this.customField2 = customField2;
     }
 
     /**
      * @return the customField3
      */
     public String getCustomField3() {
         return customField3;
     }
 
     /**
      * @param customField3 the customField3 to set
      */
     public void setCustomField3(String customField3) {
         this.customField3 = customField3;
     }
 
     /**
      * @return the customField4
      */
     public String getCustomField4() {
         return customField4;
     }
 
     /**
      * @param customField4 the customField4 to set
      */
     public void setCustomField4(String customField4) {
         this.customField4 = customField4;
     }
 
     /**
      * @return the customField5
      */
     public String getCustomField5() {
         return customField5;
     }
 
     /**
      * @param customField5 the customField5 to set
      */
     public void setCustomField5(String customField5) {
         this.customField5 = customField5;
     }
 
     /**
      * @return the tags
      */
     public List<String> getTags() {
         return tags;
     }
 
     /**
      * @param tags the tags to set
      */
     public void setTags(List<String> tags) {
         this.tags = tags;
     }
 
     /**
      * @return the email
      */
     public String getEmail() {
         return email;
     }
 
     /**
      * @param email the email to set
      */
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * @return the addedFrom
      */
     public AddedFrom getAddedFrom() {
         return addedFrom;
     }
 
     /**
      * @param addedFrom the addedFrom to set
      */
     public void setAddedFrom(AddedFrom addedFrom) {
         this.addedFrom = addedFrom;
     }
 
     /**
      * @return the fullName
      */
     public String getFullName() {
         return fullName;
     }
 
     /**
      * @param fullName the fullName to set
      */
     public void setFullName(String fullName) {
         this.fullName = fullName;
     }
 
     /**
      * @return the phoneNumber
      */
     public String getPhoneNumber() {
        return msisdn.substring(countryCode.length(), msisdn.length());        
     }
 
     /**
      * @param phoneNumber the phoneNumber to set
      */
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
     
     
     
 }
