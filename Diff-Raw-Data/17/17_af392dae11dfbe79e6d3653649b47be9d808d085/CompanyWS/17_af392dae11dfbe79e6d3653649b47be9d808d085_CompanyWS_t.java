 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.user;
 
 
 import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.db.CurrencyDAS;
 import com.sapienter.jbilling.server.util.db.CurrencyDTO;
 import com.sapienter.jbilling.server.util.db.LanguageDAS;
import javax.validation.Valid;
import javax.validation.constraints.Size;
 
 public class CompanyWS implements java.io.Serializable {
 
 
     private int id;
     private Integer currencyId;
     private Integer languageId;
    @Size(min = 0, max = 100, message = "validation.error.size,0,100")
     private String description;
    @Valid
     private ContactWS contact;
     
     public CompanyWS() {
     }
 
     public CompanyWS(int i) {
         id = i;
     }
 
     public CompanyWS(CompanyDTO companyDto) {
         this.id = companyDto.getId();
         this.currencyId= companyDto.getCurrencyId();
         this.languageId = companyDto.getLanguageId();
         this.description = companyDto.getDescription();
 
         ContactDTO contact = new EntityBL(new Integer(this.id)).getContact();
 
         if (contact != null) {
             this.contact = new ContactWS(contact.getId(),
                                          contact.getAddress1(),
                                          contact.getAddress2(),
                                          contact.getCity(),
                                          contact.getStateProvince(),
                                          contact.getPostalCode(),
                                          contact.getCountryCode(),
                                          contact.getDeleted());
         }
     }

     public CompanyDTO getDTO(){
         CompanyDTO dto = new CompanyDAS().find(new Integer(this.id));
         dto.setCurrency(new CurrencyDAS().find(this.currencyId));
         dto.setLanguage(new LanguageDAS().find(this.languageId));
         dto.setDescription(this.description);
         return dto;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public Integer getCurrencyId() {
         return currencyId;
     }
 
     public void setCurrencyId(Integer currencyId) {
         this.currencyId = currencyId;
     }
 
     public Integer getLanguageId() {
         return languageId;
     }
 
     public void setLanguageId(Integer languageId) {
         this.languageId = languageId;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public ContactWS getContact() {
         return contact;
     }
 
     public void setContact(ContactWS contact) {
         this.contact = contact;
     }
 
     public String toString() {
         return "CompanyWS [id=" + id + ", currencyId=" + currencyId
                 + ", languageId=" + languageId + ", description=" + description
                 + ", contact=" + contact + "]";
     }
 
 }
