 package org.openmrs.module.patientregistration.controller.workflow;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.APIException;
 import org.openmrs.api.PatientIdentifierException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.controller.AbstractPatientDetailsController;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.module.patientregistration.util.UserActivityLogger;
 import org.openmrs.validator.PatientIdentifierValidator;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.openmrs.module.patientregistration.PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag;
 import static org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil.getRegistrationLocation;
 
 @Controller
 @RequestMapping(value = "/module/patientregistration/workflow/primaryCareReceptionDossierNumber.form")
 public class PrimaryCareReceptionDossierNumberController extends AbstractPatientDetailsController {
 	
 	@ModelAttribute("patient")
     public Patient getPatient(HttpSession session, 
     		@RequestParam(value= "patientIdentifier", required = false) String patientIdentifier, 
     		@RequestParam(value= "patientId", required = false) String patientId) {
 			
 		Patient patient = PatientRegistrationUtil.getPatientByAnId(patientIdentifier, patientId);
 	
 		if (patient == null) {
 			throw new APIException("Invalid patient passed to PrimaryCareReceptionDossierNumberController");			
 		}
 		
 		// Load the identifiers here to hackily prevent future lazy init exceptions
 		for (PatientIdentifier pi : patient.getIdentifiers()) {
 			pi.getIdentifier();
 		}
 				
 		return patient;
     }
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView showSelectPatient(
 			@ModelAttribute("patient") Patient patient		
 			, @RequestParam(value= "edit", required = false) String editDossier
 			, @RequestParam (value = "nextTask", required = false) String nextTask
 			, HttpSession session
 			, ModelMap model) {
 		
 		// confirm that we have an active session
     	if (!PatientRegistrationWebUtil.confirmActivePatientRegistrationSession(session)) {
 			return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
 		} 
 
     	// if there is no patient defined, redirect to the enter patient names page
 		if (patient == null) {
 			return new ModelAndView("redirect:/module/patientregistration/workflow/primaryCareReceptionTask.form");
 		}
 
 		// get the identifier we wish to display
 		model.addAttribute("preferredIdentifier", PatientRegistrationUtil.getPreferredIdentifier(patient));
 		Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);		
 		PatientIdentifier dossierIdentifier = PatientRegistrationUtil.getNumeroDossier(patient, registrationLocation);
 		
 		if((dossierIdentifier==null) ||  (StringUtils.isNotBlank(editDossier) && StringUtils.equalsIgnoreCase(editDossier, "true"))){
 			UserActivityLogger.startActivityGroup(session);
 			UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_PRIMARY_CARE_RECEPTION_DOSSIER_STARTED);
 			model.addAttribute(PatientRegistrationConstants.NUMERO_DOSSIER, dossierIdentifier);			
 			return new ModelAndView("/module/patientregistration/workflow/primaryCareReceptionDossierNumber");			
 		}		
 		String nextPage = "redirect:/module/patientregistration/workflow/patientDashboard.form?patientId="+ patient.getId();
 		if(StringUtils.isNotBlank(nextTask)){
 			nextPage = nextPage + "&nextTask=" + nextTask;
 		}
 		return new ModelAndView(nextPage);			
 		
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
     public ModelAndView processSelectPatient(
     		@ModelAttribute("patient") Patient patient, BindingResult result    		    			
     		,@RequestParam("hiddenNumeroDossier") String numeroDossier
     		,@RequestParam(value= "hiddenPrintLabel", required = false) String hiddenPrintLabel
     		,@RequestParam (value = "nextTask", required = false) String nextTask
 			,HttpSession session 
 			, ModelMap model) {
 			
 		
 		if (!PatientRegistrationWebUtil.confirmActivePatientRegistrationSession(session)) {
 			return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
 		}
 
 		Location registrationLocation = getMedicalRecordLocationRecursivelyBasedOnTag(getRegistrationLocation(session));
 
 		String nextPage = null;
 		if(StringUtils.isNotBlank(numeroDossier)){
 			PatientIdentifierType identifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_NUMERO_DOSSIER();	
 			if(identifierType!=null){
                 PatientIdentifier patientIdentifier = PatientRegistrationUtil.getNumeroDossier(patient, registrationLocation);
                 if(patientIdentifier==null){
                     patientIdentifier = new PatientIdentifier(numeroDossier, identifierType, registrationLocation);
                 }else{
                     patientIdentifier.setIdentifier(numeroDossier);
                 }
 
 				//validate the identifier
                 try{
                     PatientIdentifierValidator.validateIdentifier(numeroDossier, identifierType);
                 }catch(PatientIdentifierException e){
                     log.debug("failed to validate dossier number", e);
                    model.addAttribute("identifierError", "patientregistration.jMessages.invalidIdentifier");
                     model.addAttribute("dossierPatients", e.getMessage());
                     // reload the preferred identifier into the model map
                     model.addAttribute("preferredIdentifier", PatientRegistrationUtil.getPreferredIdentifier(patient));
                     // reload the invalid identifier back into the model map
                     model.addAttribute(PatientRegistrationConstants.NUMERO_DOSSIER, patientIdentifier);
                     return new ModelAndView("/module/patientregistration/workflow/primaryCareReceptionDossierNumber");
                 }
 
 
 				List<PatientIdentifierType> identifierTypes = new ArrayList<PatientIdentifierType>();			
 				identifierTypes.add(identifierType);
 				// check to make sure the identifier is not already in use by another patient
 				List<Patient> patientWithDossier = Context.getPatientService().getPatients(null, numeroDossier, identifierTypes, true);
 				if(patientWithDossier!=null && patientWithDossier.size()>0){
 					patientWithDossier.remove(patient);
 					if(patientWithDossier.size()>0){
 						DateFormat df = new SimpleDateFormat(PatientRegistrationConstants.DATE_FORMAT_DISPLAY, Context.getLocale());
 						StringBuilder sb = new StringBuilder();
 						for(Patient patientDossier : patientWithDossier){
 							sb.append(patientDossier.getFamilyName()).append(" ").append(patientDossier.getGivenName());
 							sb.append(", ").append(patientDossier.getGender());							
 							sb.append(", ").append(df.format(patientDossier.getBirthdate()));
 							PatientIdentifier preferredIdentifier = PatientRegistrationUtil.getPreferredIdentifier(patientDossier);
 							if(preferredIdentifier!=null){
 								sb.append(", ").append(preferredIdentifier.toString());
 							}
 							sb.append("| ");
 						}
 						
 						// redisplay page with error message saying identifier already in use
 						model.addAttribute("identifierError", "patientregistration.error.dossierInUse");
 						model.addAttribute("dossierPatients", sb.toString());					
 						// reload the preferred identifier into the model map
 						model.addAttribute("preferredIdentifier", PatientRegistrationUtil.getPreferredIdentifier(patient));
 						// reload the invalid identifier back into the model map
 						model.addAttribute(PatientRegistrationConstants.NUMERO_DOSSIER, patientIdentifier);
 						return new ModelAndView("/module/patientregistration/workflow/primaryCareReceptionDossierNumber");
 					}
 				}
 				
 				patient.addIdentifier(patientIdentifier);	
 			}
 		}
 		PatientRegistrationWebUtil.savePatient(patient);
 		
 		UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_PRIMARY_CARE_RECEPTION_DOSSIER_COMPLETED);
 		UserActivityLogger.endActivityGroup(session);
 		
 		
 		if(StringUtils.equals(hiddenPrintLabel, "no")){
 			nextPage = "redirect:/module/patientregistration/workflow/patientDashboard.form?patientId="+patient.getPatientId();
 		}else{
 			nextPage = "redirect:/module/patientregistration/workflow/printRegistrationLabel.form?patientId="+patient.getPatientId();
 		}
 		if(StringUtils.isNotBlank(nextTask)){
 			nextPage = nextPage + "&nextTask=" + nextTask;
 		}		
 		return new ModelAndView(nextPage);
 	}
 
 
 
 
 }
