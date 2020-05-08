 package org.motechproject.ghana.national.web;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.service.CareService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.validator.FormValidator;
 import org.motechproject.ghana.national.validator.RegisterCWCFormValidator;
 import org.motechproject.ghana.national.vo.CwcVO;
 import org.motechproject.ghana.national.web.form.CWCEnrollmentForm;
 import org.motechproject.ghana.national.web.helper.CwcFormMapper;
 import org.motechproject.ghana.national.web.helper.FacilityHelper;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.openmrs.advice.ApiSession;
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
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 @RequestMapping("admin/cwc")
 public class CWCController {
 
 
     @Autowired
     PatientService patientService;
 
     @Autowired
     CareService careService;
 
     @Autowired
     FacilityHelper facilityHelper;
 
     @Autowired
     CwcFormMapper cwcFormMapper;
 
     @Autowired
     RegisterCWCFormValidator registerCWCFormValidator;
 
     @Autowired
     FormValidator formValidator;
 
     private String errors = "errors";
     public static final String ENROLLMENT_CWC_FORM = "cwcEnrollmentForm";
     public static final String ENROLL_CWC_URL = "cwc/new";
     public static final String PATIENT_NOT_FOUND = "Patient Not Found";
     public static final String PATIENT_IS_NOT_A_CHILD = "Patient is Not A Child";
     public static final String STAFF_ID_NOT_FOUND = "Staff Not Found";
     public static final String REGISTRATION_OPTIONS = "registrationOptions";
     static final String CWCREGVISIT = "CWCREGVISIT";
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
     }
 
     @ApiSession
     @RequestMapping(value = "/new", method = RequestMethod.GET)
     public String create(@RequestParam String motechPatientId, ModelMap modelMap) {
         Patient patient = patientService.getPatientByMotechId(motechPatientId);
         MRSEncounter encounter = careService.getEncounter(motechPatientId,CWCREGVISIT);
         CWCEnrollmentForm cwcEnrollmentForm;
         if(encounter != null) {
             cwcEnrollmentForm = cwcFormMapper.mapEncounterToView(encounter);
         } else {
             cwcEnrollmentForm = new CWCEnrollmentForm();
             cwcEnrollmentForm.setPatientMotechId(motechPatientId);
         }
         modelMap.addAttribute(ENROLLMENT_CWC_FORM, cwcEnrollmentForm);
         modelMap.mergeAttributes(cwcFormMapper.setViewAttributes());
         modelMap.mergeAttributes(facilityHelper.locationMap());
 
         if (patient == null) {
             modelMap.addAttribute(errors, Arrays.asList(PATIENT_NOT_FOUND));
             return ENROLL_CWC_URL;
         }
 
         if (patientService.getAgeOfPatientByMotechId(motechPatientId) >= 5) {
             modelMap.addAttribute(errors, Arrays.asList(PATIENT_IS_NOT_A_CHILD));
             return ENROLL_CWC_URL;
         }
         return ENROLL_CWC_URL;
     }
 
     @ApiSession
     @RequestMapping(value = "/save", method = RequestMethod.POST)
     public String save(@Valid CWCEnrollmentForm cwcEnrollmentForm, ModelMap modelMap) {
         modelMap.addAttribute(ENROLLMENT_CWC_FORM, cwcEnrollmentForm);
         modelMap.mergeAttributes(cwcFormMapper.setViewAttributes());
         modelMap.mergeAttributes(facilityHelper.locationMap());
 
         List<FormError> formErrors = registerCWCFormValidator.validatePatient(cwcEnrollmentForm.getPatientMotechId());
         formErrors.addAll(formValidator.validateIfStaffExists(cwcEnrollmentForm.getStaffId()));
 
         List<String> validationErrors = new ArrayList<String>();
         for (FormError formError : formErrors) {
             if(formError.getParameter().equals(Constants.CHILD_AGE_PARAMETER))  {
                 validationErrors.add(PATIENT_IS_NOT_A_CHILD);
             }
             if(formError.getParameter().equals(FormValidator.STAFF_ID)) {
                 validationErrors.add(STAFF_ID_NOT_FOUND);
             }
             if(formError.getParameter().equals(Constants.MOTECH_ID_ATTRIBUTE_NAME)) {
                 validationErrors.add("Patient " + StringUtils.capitalize(formError.getError()));
             }
         }
         
         if(!validationErrors.isEmpty()) {
             modelMap.addAttribute(errors, validationErrors);
             return ENROLL_CWC_URL;
         }
 
        Date registrationDate = cwcEnrollmentForm.getRegistrationDate();
         if (cwcEnrollmentForm.getRegistrationToday().equals(RegistrationToday.TODAY)) {
            registrationDate = new Date();
         }
         careService.enroll(new CwcVO(
                 cwcEnrollmentForm.getStaffId(),
                 cwcEnrollmentForm.getFacilityForm().getFacilityId(),
                registrationDate,
                 cwcEnrollmentForm.getPatientMotechId(),
                 cwcEnrollmentForm.getCareHistory(),
                 cwcEnrollmentForm.getBcgDate(),
                 cwcEnrollmentForm.getVitADate(),
                 cwcEnrollmentForm.getMeaslesDate(),
                 cwcEnrollmentForm.getYfDate(),
                 cwcEnrollmentForm.getLastPentaDate(),
                 cwcEnrollmentForm.getLastPenta(),
                 cwcEnrollmentForm.getLastOPVDate(),
                 cwcEnrollmentForm.getLastOPV(),
                 cwcEnrollmentForm.getLastIPTiDate(),
                 cwcEnrollmentForm.getLastIPTi(),
                 cwcEnrollmentForm.getSerialNumber(),
                 cwcEnrollmentForm.getAddHistory()));
         modelMap.addAttribute("success", "Client registered for CWC successfully.");
         return ENROLL_CWC_URL;
     }
 }
