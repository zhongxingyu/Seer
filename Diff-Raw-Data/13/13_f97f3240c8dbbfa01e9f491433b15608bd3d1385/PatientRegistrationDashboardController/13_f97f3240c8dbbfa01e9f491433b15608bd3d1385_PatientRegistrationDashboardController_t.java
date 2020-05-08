 package org.openmrs.module.patientregistration.controller.workflow;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.context.Context;
 import org.openmrs.messagesource.MessageSourceService;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.importpatientfromws.RemotePatient;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.controller.AbstractPatientDetailsController;
 import org.openmrs.module.patientregistration.service.PatientRegistrationService;
 import org.openmrs.module.patientregistration.util.IDCardInfo;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.module.patientregistration.util.PrintErrorType;
 import org.openmrs.module.patientregistration.util.TaskProgress;
 import org.openmrs.module.patientregistration.util.UserActivityLogger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import static org.openmrs.module.patientregistration.PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag;
 import static org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil.getRegistrationLocation;
 
 
 @Controller
 @RequestMapping("/module/patientregistration/workflow/patientDashboard.form") 
 public class PatientRegistrationDashboardController extends AbstractPatientDetailsController{
 
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView showPatientInfo(HttpSession session, ModelMap model,
             @ModelAttribute("printErrorsType") List<PrintErrorType> printErrorsType,
 			@RequestParam(value= "patientIdentifier", required = false) String patientIdentifier,
 			@RequestParam(value= "patientId", required = false) String patientId,
             @RequestParam(value= "remoteUuid", required = false) String remoteUuid,
 			@RequestParam(value= "createEncounter", required = false) String encounterName,
 			@RequestParam(value= "createId", required = false) String createId,
 			@RequestParam(value= "scanIdCard", required = false) String scanIdCard,
 			@RequestParam(value= "cardPrinted", required = false) String cardPrinted,
 			@RequestParam(value= "nextTask", required = false) String nextTask
 			) {
 
         String printErrors = getPrintErrors(printErrorsType);
         model.addAttribute("errorMessages", printErrors);
 
         if (!PatientRegistrationWebUtil.confirmActivePatientRegistrationSession(session)) {
 			return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
 		}
         String task = PatientRegistrationWebUtil.getRegistrationTask(session);
        if (StringUtils.isNotBlank(task)) {
            model.addAttribute("task", task);
        }
 		Patient patient = null;
 		if(StringUtils.isNotBlank(patientIdentifier)){
 			List<Patient> patients = Context.getPatientService().getPatients(null, patientIdentifier, null, true);
 			if(patients!=null && patients.size()>0){
 				patient = patients.get(0);
 			}
 		}
         PatientIdentifierType zlIdentifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE();
         if(StringUtils.isNotBlank(patientId)){
 			try{
 				patient = Context.getPatientService().getPatient(new Integer(patientId));
 			}catch(Exception e){
 				log.error("patient not found", e);
 			}
 		}else if(StringUtils.isNotBlank(remoteUuid)){
             RemotePatient remotePatient = PatientRegistrationWebUtil.getFromCache(remoteUuid, session);
             if((remotePatient!=null) &&
                     (remotePatient.getPatient()!=null)){
                 patient = remotePatient.getPatient();
                 if(patient!=null){
                     //import the patient
                     try{
                         if(zlIdentifierType!=null && patient!=null){
                             PatientIdentifier zlPatientIdentifier = patient.getPatientIdentifier(zlIdentifierType);
                             if(zlPatientIdentifier!=null){
                                 zlPatientIdentifier.setPreferred(true);
                             }
                         }
                         patient = Context.getPatientService().savePatient(patient);
                         UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_PATIENT_IMPORTED);
                         PatientRegistrationWebUtil.removeFromCache(remoteUuid, session);
                     }catch(Exception e){
                         log.error("failed to import patient", e);
                        model.addAttribute("errorMessages", "[ \"failed to import remote patient:"
                                + patient.getFamilyName()
                                + ", " + patient.getGivenName()
                                + "; " + e + "\" ]");
                         return new ModelAndView("/module/patientregistration/workflow/patientDashboard");
                     }
                 }
             }
         }
 		Location medicalRecordLocation = getMedicalRecordLocationRecursivelyBasedOnTag(getRegistrationLocation(session));
 		Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);
 		PatientIdentifier patientPreferredIdentifier = null;
 		if (patient != null) {
 			model.addAttribute("patient", patient);
 			if(zlIdentifierType!=null){
 				patientPreferredIdentifier = PatientRegistrationUtil.getPreferredIdentifier(patient);				
 				if(patientPreferredIdentifier==null ||
 					(patientPreferredIdentifier!=null && (patientPreferredIdentifier.getIdentifierType().getId().compareTo(zlIdentifierType.getId())!=0))){
 				 
 					 patientPreferredIdentifier = new PatientIdentifier(null, zlIdentifierType, medicalRecordLocation);
 					 if(StringUtils.equalsIgnoreCase(createId, "true")){	
 						 patientPreferredIdentifier.setIdentifier(PatientRegistrationUtil.assignIdentifier(zlIdentifierType)) ;
 						 patientPreferredIdentifier.setPreferred(true);
 						 patient.addIdentifier(patientPreferredIdentifier);	
 						 String message = "Identifier: " + patientPreferredIdentifier.getIdentifier();
 						 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_REGISTRATION_NEW_ZL_ID, message);
 						 try{
 							 Context.getPatientService().savePatient(patient);							 							
 						 }
 						 catch(Exception e){
 							 log.error("failed to save patient", e);
 						 }
 					 }
 				}
 				model.addAttribute("preferredIdentifier", patientPreferredIdentifier);
 				
 			}
 
             PatientIdentifierType identifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_NUMERO_DOSSIER();
 			if(identifierType!=null){				
 				PatientIdentifier dossierIdentifier = PatientRegistrationUtil.getNumeroDossier(patient, medicalRecordLocation);
 				if(dossierIdentifier==null){
 					dossierIdentifier = new PatientIdentifier("",identifierType, medicalRecordLocation);
 				}
 				model.addAttribute(PatientRegistrationConstants.NUMERO_DOSSIER, dossierIdentifier);
 			}
             identifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_EXTERNAL_NUMERO_DOSSIER();
             if(identifierType!=null){
                 PatientIdentifier dossierIdentifier = patient.getPatientIdentifier(identifierType);
                 if(dossierIdentifier==null){
                     dossierIdentifier = new PatientIdentifier("",identifierType, medicalRecordLocation);
                 }
                 model.addAttribute(PatientRegistrationConstants.EXTERNAL_NUMERO_DOSSIER, dossierIdentifier);
             }
 
 			Encounter registrationEncounter = null;
             EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
 
             if (encounterType == null) {
                 log.error( "encounterName=" + encounterName + " does not exist");
             }
             else {
                 registrationEncounter = Context.getService(PatientRegistrationService.class).getLastEncounterByType(patient, encounterType, null);
                 model.addAttribute("registrationEncounter", registrationEncounter);
                 TaskProgress taskProgress = PatientRegistrationWebUtil.getTaskProgress(session);
                 if(taskProgress!=null){
                     taskProgress.setPatientId(patient.getId());
                     taskProgress.setProgressBarImage(PatientRegistrationConstants.RETROSPECTIVE_PROGRESS_2_IMG);
                     Map<String, Integer> completedTasks = new HashMap<String, Integer>();
                     completedTasks.put("registrationTask", new Integer(1));
                     taskProgress.setCompletedTasks(completedTasks);
                     PatientRegistrationWebUtil.setTaskProgress(session, taskProgress);
                     model.addAttribute("taskProgress", taskProgress);
                 }
 
             }
 
 			Boolean cardPrintedStatus = null;
 			if(StringUtils.equalsIgnoreCase(cardPrinted, "true")){
 				cardPrintedStatus = new Boolean(true);				
 			}else if (StringUtils.equalsIgnoreCase(cardPrinted, "false")){
 				cardPrintedStatus = new Boolean(false);				
 			}	
 			IDCardInfo cardInfo = null;
 			if(patientPreferredIdentifier!=null){
 				cardInfo = PatientRegistrationWebUtil.updatePrintingCardStatus(patient, encounterType, registrationEncounter, medicalRecordLocation, cardPrintedStatus, null);
 			}
 			model.addAttribute("cardInfo", cardInfo);
 			
 			List<Encounter> encounters = new Vector<Encounter>();
 			List<Encounter> encs = Context.getEncounterService().getEncountersByPatient(patient);
 			if (encs != null && encs.size() > 0)
 				encounters.addAll(encs);
 			model.addAttribute("encounters", encounters);
 			model.addAttribute("editURLs", PatientRegistrationWebUtil.getEncounterEditURLs());
 			model.addAttribute("encounterLocale", PatientRegistrationWebUtil.getEncounterTypeLocale());
 			if(StringUtils.equalsIgnoreCase(scanIdCard, "true")){
 				model.addAttribute("scanIdCard", "true");
 			}
 		}
 
 		if(StringUtils.isNotBlank(nextTask)){
             if(StringUtils.equals(task, PatientRegistrationConstants.EMERGENCY_DEPARTMENT_TASK)){
                 return new ModelAndView("redirect:/module/patientregistration/workflow/" + nextTask + "?patientId=" + patient.getPatientId(), model);
             }
             model.addAttribute("nextTask", nextTask);
 		}
 		return new ModelAndView("/module/patientregistration/workflow/patientDashboard");
 	}
 
     private String getPrintErrors(List<PrintErrorType> printErrorTypes) {
 
         String errorMessages = "[ ";
 
         MessageSourceService messageSourceService = Context.getMessageSourceService();
 
         for (int i = 0 ; i < printErrorTypes.size() ; i++) {
             errorMessages += "\"" + messageSourceService.getMessage(printErrorTypes.get(i).getMessage()) + "\"";
             if (i != printErrorTypes.size() - 1){
                 errorMessages += ", ";
             }
         }
 
         errorMessages += "]";
 
         return errorMessages;
     }
 
     @RequestMapping(params= "printIDCard", method = RequestMethod.POST)
 	public ModelAndView printIDCard(@ModelAttribute("patient") Patient patient, BindingResult result, HttpSession session , ModelMap model){
         return  handleCardPrinting(patient, result, session, model);
 	}
     @RequestMapping(params= "reprintIDCard", method = RequestMethod.POST)
     public ModelAndView reprintIDCard(@ModelAttribute("patient") Patient patient, BindingResult result, HttpSession session , ModelMap model){
        return  handleCardPrinting(patient, result, session, model);
     }
 
     public ModelAndView handleCardPrinting(@ModelAttribute("patient") Patient patient, BindingResult result, HttpSession session , ModelMap model){
         if(patient!=null){
             // fetch the patient
             patient = Context.getPatientService().getPatient((Integer) patient.getId());
             Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);
             if(registrationLocation==null){
                 return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
             }
             EncounterType registrationEncounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PATIENT_REGISTRATION_ENCOUNTER_TYPE();
             Encounter registrationEncounter = Context.getService(PatientRegistrationService.class).registerPatient(
                     patient
                     , Context.getAuthenticatedUser().getPerson()
                     , registrationEncounterType
                     , registrationLocation);
             boolean cardPrintedStatus = false;
 
             try {
                 Context.getService(PatientRegistrationService.class).printIDCard(patient, new EmrContext(session).getSessionLocation());
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_SUCCESSFUL);
                 cardPrintedStatus = true;
             }
             catch (Exception e) {
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_ID_CARD_PRINTING_FAILED);
                 // TODO: Decide what else to do if this fails
             }
             IDCardInfo cardInfo = PatientRegistrationWebUtil.updatePrintingCardStatus(patient, registrationEncounterType, registrationEncounter, registrationLocation, new Boolean(cardPrintedStatus), null);
             model.clear();
             model.addAttribute("cardInfo", cardInfo);
             return new ModelAndView("redirect:/module/patientregistration/workflow/patientDashboard.form?scanIdCard=true&patientId="+ patient.getId());
         }else{
             return new ModelAndView("redirect:/module/patientregistration/workflow/patientRegistrationTask.form");
         }
     }
 	
 	@RequestMapping(params= "printDossierLabel", method = RequestMethod.POST)
 	public ModelAndView printDossierLabel(@ModelAttribute("patient") Patient patient,
                                           BindingResult result,
                                           HttpSession session,
                                           ModelMap model){
 		if (patient!=null) {
 			patient = Context.getPatientService().getPatient(new Integer(patient.getId()));
             Location location = PatientRegistrationWebUtil.getRegistrationLocation(session);
             List<PrintErrorType> printErrorTypes = PatientRegistrationWebUtil.printLabels(patient, session, location, 1);
             if(printErrorTypes!=null && printErrorTypes.size()>0){
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_DOSSIER_LABEL_PRINTING_FAILED);
             }else{
                 UserActivityLogger.logActivity(session, PatientRegistrationConstants.ACTIVITY_DOSSIER_LABEL_PRINTING_SUCCESSFUL);
             }
             String printErrorsQuery = PatientRegistrationWebUtil.createPrintErrorsQuery(printErrorTypes);
             model.clear();
 			return new ModelAndView("redirect:/module/patientregistration/workflow/patientDashboard.form?patientId="+ patient.getId()+ printErrorsQuery, model);
 		}
 		else{
 			return new ModelAndView("redirect:/module/patientregistration/workflow/patientRegistrationTask.form");
 		}
 		
 	}
 	
 }
