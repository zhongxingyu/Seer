 package fr.capwebct.capdemat.plugins.externalservices.edemande.adapters;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import fr.capwebct.capdemat.plugins.externalservices.edemande.service.EdemandeService;
 import fr.cg95.cvq.xml.common.AddressType;
 import fr.cg95.cvq.xml.common.FrenchRIBType;
 import fr.cg95.cvq.xml.common.IndividualType;
 import fr.cg95.cvq.xml.common.SexType;
 import fr.cg95.cvq.xml.common.TitleType;
 import fr.cg95.cvq.xml.common.TitleType.Enum;
 import fr.cg95.cvq.xml.request.social.impl.BAFAGrantRequestDocumentImpl.BAFAGrantRequestImpl;
 
 public class BAFAGrantEdemandeRequest implements EdemandeRequest {
 
     private BAFAGrantRequestImpl request;
 
     public BAFAGrantEdemandeRequest(BAFAGrantRequestImpl request) {
         this.request = request;
     }
 
     private IndividualType getSubject() {
         if (request.getSubject().isSetAdult()) return request.getSubject().getAdult();
         return request.getSubject().getChild();
     }
 
     @Override
     public Calendar getAccountHolderBirthDate() {
         return request.getAccountHolderBirthDate();
     }
 
     @Override
     public String getAccountHolderEdemandeId() {
         return request.getAccountHolderEdemandeId();
     }
 
     @Override
     public String getAccountHolderFirstName() {
         return request.getAccountHolderFirstName();
     }
 
     @Override
     public String getAccountHolderLastName() {
         return request.getAccountHolderLastName();
     }
 
     @Override
     public Enum getAccountHolderTitle() {
         return request.getAccountHolderTitle();
     }
 
     @Override
     public Calendar getCreationDate() {
         return request.getCreationDate();
     }
 
     @Override
     public String getEdemandeId() {
         return request.getEdemandeId();
     }
 
     @Override
     public FrenchRIBType getFrenchRIB() {
         return request.getFrenchRIB();
     }
 
     @Override
     public Long getHomeFolderId() {
         return request.getHomeFolder().getId();
     }
 
     @Override
     public Long getId() {
         return request.getId();
     }
 
     @Override
     public AddressType getSubjectAddress() {
         return request.getSubjectAddress();
     }
 
     @Override
     public String getSubjectBirthCity() {
         return request.getSubjectBirthCity();
     }
 
     @Override
     public Calendar getSubjectBirthDate() {
         return request.getSubjectBirthDate();
     }
 
     @Override
     public String getSubjectEdemandeId() {
         return getSubject().getExternalId();
     }
 
     @Override
     public String getSubjectEmail() {
         return request.getSubjectEmail();
     }
 
     @Override
     public String getSubjectFirstName() {
         return getSubject().getFirstName();
     }
 
     @Override
     public String getSubjectLastName() {
         return getSubject().getLastName();
     }
 
     @Override
     public String getSubjectPhone() {
         return request.getSubjectPhone();
     }
 
     @Override
     public Enum getSubjectTitle() {
         if (request.getSubject().getAdult() != null) {
             return request.getSubject().getAdult().getTitle();
         }
         switch (request.getSubject().getChild().getSex().intValue()) {
             case SexType.INT_FEMALE :
                 return TitleType.MISS;
             case SexType.INT_MALE :
                 return TitleType.MISTER;
             default :
                 return TitleType.UNKNOWN;
         }
     }
 
     @Override
     public boolean isSubjectAccountHolder() {
         return request.getIsSubjectAccountHolder();
     }
 
     @Override
     public void setAccountHolderEdemandeId(String id) {
         request.setAccountHolderEdemandeId(id);
     }
 
     @Override
     public void setSubjectEdemandeId(String id) {
         getSubject().setExternalId(id);
     }
 
     @Override
     public Long getSubjectId() {
         return getSubject().getId();
     }
 
     @Override
     public void setEdemandeId(String id) {
         request.setEdemandeId(id);
     }
 
     @Override
     public Map<String, Object> getSpecificFields(EdemandeService service) {
         Map<String, Object> result = new HashMap<String, Object>();
         result.put("internshipStartDate", service.formatDate(request.getInternshipStartDate()));
         result.put("internshipEndDate", service.formatDate(request.getInternshipEndDate()));
         result.put("internshipInstituteName", request.getInternshipInstituteName());
        // FIXME hack to avoid template compilation exception
        result.put("taxHouseholdCityCode", "");
         return result;
     }
 
     @Override
     public Config getConfig() {
         return Config.BGR;
     }
 }
