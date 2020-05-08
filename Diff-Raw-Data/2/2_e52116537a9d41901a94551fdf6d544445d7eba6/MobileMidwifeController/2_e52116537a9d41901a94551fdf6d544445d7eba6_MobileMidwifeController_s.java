 package org.motechproject.ghana.national.web;
 
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.ghana.national.tools.Messages;
 import org.motechproject.ghana.national.validator.MobileMidwifeFormValidator;
 import org.motechproject.ghana.national.web.form.MobileMidwifeEnrollmentForm;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 
 @Controller
 @RequestMapping(value = "/admin/enroll/mobile-midwife")
 public class MobileMidwifeController {
 
     public static final String MOBILE_MIDWIFE_URL = "enroll/mobile-midwife/new";
    public static final String SUCCESS_MESSAGE = "changes_saved_successfully";
     private MobileMidwifeFormValidator mobileMidwifeFormValidator;
     private MobileMidwifeService mobileMidwifeService;
     private Messages messages;
 
     public MobileMidwifeController() {
     }
 
     @Autowired
     public MobileMidwifeController(MobileMidwifeFormValidator mobileMidwifeFormValidator, MobileMidwifeService mobileMidwifeService, Messages messages) {
         this.mobileMidwifeFormValidator = mobileMidwifeFormValidator;
         this.mobileMidwifeService = mobileMidwifeService;
         this.messages = messages;
     }
 
     @RequestMapping(value = "form", method = RequestMethod.GET)
     public String form(@RequestParam String motechPatientId, ModelMap modelMap){
 
         MobileMidwifeEnrollment midwifeEnrollment = mobileMidwifeService.findBy(motechPatientId);
         MobileMidwifeEnrollmentForm enrollmentForm = midwifeEnrollment != null ? new MobileMidwifeEnrollmentForm(midwifeEnrollment)
                 : new MobileMidwifeEnrollmentForm().setPatientMotechId(motechPatientId);
         addFormInfo(modelMap, enrollmentForm);
         return MOBILE_MIDWIFE_URL;
     }
 
     @ApiSession
     @RequestMapping(value = "save", method = RequestMethod.POST)
     public String save(MobileMidwifeEnrollmentForm form, BindingResult bindingResult, ModelMap modelMap) {
         List<FormError> formErrors = mobileMidwifeFormValidator.validateFacilityPatientAndStaff(form.getPatientMotechId(), form.getFacilityMotechId(), form.getStaffMotechId());
         if (isNotEmpty(formErrors)) {
             addFormInfo(modelMap, form).
                     addAttribute("formErrors", formErrors);
         } else {
             MobileMidwifeEnrollment midwifeEnrollment = mobileMidwifeService.findBy(form.getPatientMotechId());
             midwifeEnrollment = form.createEnrollment(midwifeEnrollment != null ? midwifeEnrollment : new MobileMidwifeEnrollment());
             mobileMidwifeService.saveOrUpdate(midwifeEnrollment);
             addFormInfo(modelMap, new MobileMidwifeEnrollmentForm(midwifeEnrollment))
                     .addAttribute("successMessage", messages.message(SUCCESS_MESSAGE));
         }
         return MOBILE_MIDWIFE_URL;
     }
 
     private ModelMap addFormInfo(ModelMap modelMap, MobileMidwifeEnrollmentForm enrollmentForm) {
         return modelMap.addAttribute("mobileMidwifeEnrollmentForm", enrollmentForm)
                 .addAttribute("serviceTypes", ServiceType.values())
                 .addAttribute("phoneOwnerships", PhoneOwnership.values())
                 .addAttribute("reasonsToJoin", ReasonToJoin.values())
                 .addAttribute("learnedFrom", LearnedFrom.values())
                 .addAttribute("languages", Language.values())
                 .addAttribute("mediums", Medium.values())
                 .addAttribute("dayOfWeeks", collectDayOfWeekOptions())
                 .addAttribute("messageStartWeeks", MessageStartWeek.messageStartWeeks());
     }
 
     private Map<String, String> collectDayOfWeekOptions() {
         Map<String, String> options = new LinkedHashMap<String, String>();
         for(DayOfWeek option : DayOfWeek.values()){
             options.put(option.name(), option.name());
         }
         return options;
     }
 }
