 package org.motechproject.ghana.national.functional.Generator;
 
 import org.motechproject.functional.util.DataGenerator;
 import org.motechproject.ghana.national.web.FacilityController;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 
 @Component
 public class FacilityGenerator {
 
     @Autowired
     FacilityController facilityController;
     DataGenerator dataGenerator;
 
     FacilityForm createdFacility;
 
     public FacilityGenerator() {
     }
 
    @Autowired
     public FacilityGenerator(FacilityController facilityController) {
         this.facilityController = facilityController;
     }
 
     @LoginAsAdmin
     @ApiSession
     public String createFacilityAndReturnFacilityId() {
         FacilityForm facilityForm = createFacilityForm();
         ModelMap modelMap = new ModelMap();
         facilityController.create(facilityForm, null, modelMap);
         createdFacility = (FacilityForm) modelMap.get(FacilityController.FACILITY_FORM);
         return createdFacility.getId();
     }
 
     public String getFacilityMotechId(){
         return createdFacility.getFacilityId();
     }
 
     private FacilityForm createFacilityForm() {
         FacilityForm facilityForm = new FacilityForm();
         dataGenerator = new DataGenerator();
         String randomString = dataGenerator.randomString(4);
         String name = "Dummy Facility" + randomString;
         String country = "Country";
         String region = "Western Region";
         String province = "Test Province";
         String district = "Test District";
         String phoneNumber = dataGenerator.randomPhoneNumber();
         facilityForm.setId(randomString);
         facilityForm.setName(name);
         facilityForm.setCountry(country);
         facilityForm.setRegion(region);
         facilityForm.setCountyDistrict(district);
         facilityForm.setStateProvince(province);
         facilityForm.setPhoneNumber(phoneNumber);
         return facilityForm;
     }
 }
