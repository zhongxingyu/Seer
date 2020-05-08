 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.service.CareService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.validator.RegisterANCFormValidator;
 import org.motechproject.ghana.national.vo.ANCVO;
 import org.motechproject.ghana.national.web.form.ANCEnrollmentForm;
 import org.motechproject.ghana.national.web.helper.ANCFormMapper;
 import org.motechproject.ghana.national.web.helper.FacilityHelper;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.validation.Valid;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Controller
 @RequestMapping("admin/anc")
 public class ANCController {
 
     public static final String ENROLL_ANC_URL = "anc/new";
     @Autowired
     private FacilityHelper facilityHelper;
     @Autowired
     private CareService careService;
     @Autowired
     RegisterANCFormValidator registerANCFormValidator;
     @Autowired
     PatientService patientService;
     @Autowired
     ANCFormMapper ancFormMapper;
 
     public static final String PATIENT_NOT_FOUND = "Patient Not Found";
 
     public ANCController() {
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
     }
 
     @ApiSession
     @RequestMapping(value = "new", method = RequestMethod.GET)
     public String newANC(@RequestParam("motechPatientId") String motechPatientId, ModelMap modelMap) {
         List<FormError> formErrors = registerANCFormValidator.validatePatient(motechPatientId);
         ANCEnrollmentForm enrollmentForm = new ANCEnrollmentForm(motechPatientId);
         if (formErrors.isEmpty()) {
             MRSEncounter mrsEncounter = careService.getEncounter(motechPatientId, Constants.ENCOUNTER_ANCREGVISIT);
             enrollmentForm = (mrsEncounter == null) ? enrollmentForm : ancFormMapper.convertMRSEncounterToView(mrsEncounter);
             MRSEncounter pregnancyInfoEncounter = careService.getEncounter(motechPatientId, Constants.ENCOUNTER_PREGREGVISIT);
             ancFormMapper.populatePregnancyInfo(pregnancyInfoEncounter, enrollmentForm);
         } else {
             modelMap.addAttribute("validationErrors", formErrors);
         }
         modelMap.put("ancEnrollmentForm", enrollmentForm);
         addCareHistoryValues(modelMap);
         return ENROLL_ANC_URL;
     }
 
     @ApiSession
     @RequestMapping(value = "save", method = RequestMethod.POST)
     public String save(@Valid ANCEnrollmentForm ancEnrollmentForm, ModelMap modelMap) {
         List<FormError> formErrors = registerANCFormValidator.validatePatientAndStaff(ancEnrollmentForm.getMotechPatientId(),
                  ancEnrollmentForm.getStaffId());
 
         if (formErrors.isEmpty()) {
             careService.enroll(createANCVO(ancEnrollmentForm));
             modelMap.addAttribute("success", "Updated successfully.");
         } else {
             modelMap.addAttribute("validationErrors", formErrors);
         }
 
         modelMap.put("ancEnrollmentForm", ancEnrollmentForm);
         addCareHistoryValues(modelMap);
         return ENROLL_ANC_URL;
     }
 
     private ANCVO createANCVO(ANCEnrollmentForm ancForm) {
        ancForm.setRegistrationDate((RegistrationToday.TODAY.equals(ancForm.getRegistrationToday())) ? DateUtil.now().toDate() : ancForm.getRegistrationDate());
         return new ANCVO(ancForm.getStaffId(), ancForm.getFacilityForm().getFacilityId(),
                 ancForm.getMotechPatientId(), ancForm.getRegistrationDate(), ancForm.getRegistrationToday(),
                 ancForm.getSerialNumber(), ancForm.getEstimatedDateOfDelivery(), ancForm.getHeight(),
                 ancForm.getGravida(), ancForm.getParity(), ancForm.getAddHistory(), ancForm.getDeliveryDateConfirmed(),
                 ancForm.getCareHistory(), ancForm.getLastIPT(), ancForm.getLastTT(),
                 ancForm.getLastIPTDate(), ancForm.getLastTTDate(), ancForm.getAddHistory());
     }
 
     private void addCareHistoryValues(ModelMap modelMap) {
         modelMap.put("careHistories", Arrays.asList("TT", "IPT"));
         HashMap<Integer,String> lastIPTValues = new LinkedHashMap<Integer, String>();
         lastIPTValues.put(1,"IPT 1");
         lastIPTValues.put(2,"IPT 2");
         lastIPTValues.put(3,"IPT 3");
 
         HashMap<Integer,String> lastTTValues = new LinkedHashMap<Integer, String>();
         lastTTValues.put(1,"TT 1");
         lastTTValues.put(2,"TT 2");
         lastTTValues.put(3,"TT 3");
         lastTTValues.put(4,"TT 4");
         lastTTValues.put(5,"TT 5");
 
         modelMap.put("lastIPT", lastIPTValues);
         modelMap.put("lastTT", lastTTValues);
 
         modelMap.mergeAttributes(facilityHelper.locationMap());
     }
 }
