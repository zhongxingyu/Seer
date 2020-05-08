 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
 import org.motechproject.ghana.national.exception.FacilityNotFoundException;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.ghana.national.web.helper.FacilityHelper;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 @Controller
 @RequestMapping(value = "/admin/facilities")
 public class FacilityController {
     public static final String FACILITY_FORM = "facilityForm";
    public static final String SUCCESS_VIEW = "facilities/success";
     public static final String NEW_FACILITY_VIEW = "facilities/new";
     public static final String SEARCH_FACILITY_VIEW = "facilities/search";
     public static final String EDIT_FACILITY_VIEW = "facilities/edit";
 
     public static final String FACILITY_ID = "Id";
 
     private FacilityService facilityService;
     private MessageSource messageSource;
     private FacilityHelper facilityHelper;
     private static final String NEW_FACILITY_REDIRECT_URL = "/admin/facilities/new";
 
     public FacilityController() {
     }
 
     @Autowired
     public FacilityController(FacilityService facilityService, MessageSource messageSource, FacilityHelper facilityHelper) {
         this.facilityService = facilityService;
         this.messageSource = messageSource;
         this.facilityHelper = facilityHelper;
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newFacility(ModelMap modelMap) {
         modelMap.put(FACILITY_FORM, new FacilityForm());
         modelMap.mergeAttributes(facilityHelper.locationMap());
         return NEW_FACILITY_VIEW;
     }
 
     @ApiSession
     @RequestMapping(value = "create", method = RequestMethod.POST)
     public String create(@Valid FacilityForm createFacilityForm, BindingResult bindingResult, ModelMap modelMap) {
         String facilityId;
         try {
             facilityId = facilityService.create(createFacilityForm.getName(), createFacilityForm.getCountry(), createFacilityForm.getRegion(),
                     createFacilityForm.getCountyDistrict(), createFacilityForm.getStateProvince(), createFacilityForm.getPhoneNumber(),
                     createFacilityForm.getAdditionalPhoneNumber1(), createFacilityForm.getAdditionalPhoneNumber2(),
                     createFacilityForm.getAdditionalPhoneNumber3());
         } catch (FacilityAlreadyFoundException e) {
             facilityHelper.handleExistingFacilityError(bindingResult, modelMap, messageSource.getMessage("facility_already_exists", null, Locale.getDefault()), FACILITY_FORM);
             modelMap.mergeAttributes(facilityHelper.locationMap());
             return NEW_FACILITY_VIEW;
         }
         return facilityHelper.getFacilityForId(modelMap, facilityService.getFacility(facilityId));
     }
 
     @ApiSession
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String search(ModelMap modelMap) {
         modelMap.put(FACILITY_FORM, new FacilityForm());
         modelMap.mergeAttributes(facilityHelper.locationMap());
         return SEARCH_FACILITY_VIEW;
     }
 
     @ApiSession
     @RequestMapping(value = "searchFacilities", method = RequestMethod.POST)
     public String search(@Valid final FacilityForm searchFacilityForm, ModelMap modelMap) {
         final List<Facility> searchResults = facilityService.searchFacilities(searchFacilityForm.getFacilityId(),
                 searchFacilityForm.getName(), searchFacilityForm.getCountry(), searchFacilityForm.getRegion(),
                 searchFacilityForm.getCountyDistrict(), searchFacilityForm.getStateProvince());
         List<FacilityForm> requestedFacilities = new ArrayList<FacilityForm>();
         for (Facility facility : searchResults) {
             requestedFacilities.add(new FacilityForm(facility.mrsFacilityId(), facility.motechId(), facility.name(), facility.country(),
                     facility.region(), facility.district(), facility.province(), facility.phoneNumber(), facility.additionalPhoneNumber1(),
                     facility.additionalPhoneNumber2(), facility.additionalPhoneNumber3()));
         }
         modelMap.put("requestedFacilities", requestedFacilities);
         modelMap.mergeAttributes(facilityHelper.locationMap());
         return SEARCH_FACILITY_VIEW;
     }
 
     @ApiSession
     @RequestMapping(value = "edit", method = RequestMethod.GET)
     public String edit(ModelMap modelMap, HttpServletRequest httpServletRequest) {
         String facilityId = httpServletRequest.getParameter(FACILITY_ID);
         return facilityHelper.getFacilityForId(modelMap, facilityService.getFacility(facilityId));
     }
 
     @ApiSession
     @RequestMapping(value = "update", method = RequestMethod.POST)
     public String update(@Valid FacilityForm updateFacilityForm, BindingResult bindingResult, ModelMap modelMap) {
         try {
             facilityService.update(updateFacilityForm.getId(), updateFacilityForm.getFacilityId(),
                     updateFacilityForm.getName(), updateFacilityForm.getCountry(), updateFacilityForm.getRegion(),
                     updateFacilityForm.getCountyDistrict(), updateFacilityForm.getStateProvince(), updateFacilityForm.getPhoneNumber(),
                     updateFacilityForm.getAdditionalPhoneNumber1(), updateFacilityForm.getAdditionalPhoneNumber2(),
                     updateFacilityForm.getAdditionalPhoneNumber3());
            modelMap.put("successMessage", "Staff edited successfully.");
         } catch (FacilityNotFoundException e) {
             return "redirect:" + NEW_FACILITY_REDIRECT_URL;
         }
         return EDIT_FACILITY_VIEW;
     }
 }
