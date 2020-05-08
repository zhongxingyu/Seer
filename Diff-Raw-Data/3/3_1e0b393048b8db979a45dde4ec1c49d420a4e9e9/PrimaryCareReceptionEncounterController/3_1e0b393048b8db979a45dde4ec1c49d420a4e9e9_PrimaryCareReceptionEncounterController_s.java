 /**
  * 
  */
 package org.openmrs.module.patientregistration.controller.workflow;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Concept;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.api.APIException;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.controller.AbstractPatientDetailsController;
 import org.openmrs.module.patientregistration.task.EncounterTaskItemQuestion;
 import org.openmrs.module.patientregistration.util.POCObservation;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.module.patientregistration.util.TaskProgress;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * @author cospih
  *
  */
 @Controller
 @RequestMapping(value = "/module/patientregistration/workflow/primaryCareReceptionEncounter.form")
 public class PrimaryCareReceptionEncounterController extends AbstractPatientDetailsController {
 	
 	@ModelAttribute("patient")
     public Patient getPatient(HttpSession session, 
     		@RequestParam(value= "patientIdentifier", required = false) String patientIdentifier, 
     		@RequestParam(value= "patientId", required = false) String patientId) {
 			
 		Patient patient = PatientRegistrationUtil.getPatientByAnId(patientIdentifier, patientId);
 	
 		if (patient == null) {
 			throw new APIException("Invalid patient passed to PrimaryCareReceptionDossierNumberController");			
 		}
 				
 		for (PatientIdentifier pi : patient.getIdentifiers()) {
 			pi.getIdentifier();
 		}
 				
 		return patient;
     }
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView showSelectPatient(
 			@ModelAttribute("patient") Patient patient		
 			, @RequestParam(value= "encounterId", required = false) String encounterId
 			, @RequestParam(value= "createNew", required = false) String createNew
 			, @RequestParam(value= "nextTask", required = false) String nextTask
 			, HttpSession session
 			, ModelMap model) {
 		
 		
 		// confirm that we have an active session
     	if (!PatientRegistrationWebUtil.confirmActivePatientRegistrationSession(session)) {
 			return new ModelAndView(PatientRegistrationConstants.WORKFLOW_FIRST_PAGE);
 		}
         model.addAttribute("registration_task", "primaryCareReception");
 
         // if there is no patient defined, redirect to the primaryCareVisit task first page
         if (patient == null) {
             return new ModelAndView("redirect:/module/patientregistration/workflow/primaryCareReceptionTask.form");
         }
 
         Concept paymentAmountConcept = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_PAYMENT_AMOUNT_CONCEPT();
 
         model.addAttribute("preferredIdentifier", PatientRegistrationUtil.getPreferredIdentifier(patient));
 
         EncounterTaskItemQuestion visitReason = new EncounterTaskItemQuestion();
         visitReason.setConcept(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_VISIT_REASON_CONCEPT());
         String label = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_VISIT_REASON_CONCEPT_LOCALIZED_LABEL(Context.getLocale());
         if (label != null) {
             visitReason.setLabel(label);
         }
         visitReason.setType(EncounterTaskItemQuestion.Type.SELECT);
         visitReason.initializeAnswersFromConceptAnswers();
 
         model.addAttribute("visitReason", visitReason);
 
         LinkedHashMap<String, String> paymentAmounts = new LinkedHashMap<String, String>();
         paymentAmounts.put("50 Gourdes", "50");
         paymentAmounts.put("100 Gourdes", "100");
         paymentAmounts.put("Exempt", "0");
 
         EncounterTaskItemQuestion paymentAmount = new EncounterTaskItemQuestion();
         paymentAmount.setConcept(paymentAmountConcept);
         label = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_PAYMENT_AMOUNT_CONCEPT_LOCALIZED_LABEL(Context.getLocale());
         if (label != null) {
             paymentAmount.setLabel(label);
         }
         paymentAmount.setType(EncounterTaskItemQuestion.Type.SELECT);
         paymentAmount.setAnswers(paymentAmounts);
 
 		if(paymentAmount!=null){
 			model.addAttribute("paymentAmount", paymentAmount);
 		}
 		
 		EncounterTaskItemQuestion receipt = new EncounterTaskItemQuestion();
 		receipt.setConcept(PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_RECEIPT_NUMBER_CONCEPT());
 		label = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_RECEIPT_NUMBER_CONCEPT_LOCALIZED_LABEL(Context.getLocale());
 		if (label != null) {
 			receipt.setLabel(label);
 		}
 		receipt.setType(EncounterTaskItemQuestion.Type.TEXT);
 		if(receipt!=null){
 			model.addAttribute("receipt", receipt);
 		}
 		
 		Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);	
 		EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_VISIT_ENCOUNTER_TYPE();	
 		Date encounterDate = new Date();
 		List<Obs> obs = null;
 		if(StringUtils.isNotBlank(encounterId)){
 			Integer editEncounterId = Integer.parseInt(encounterId);
 			if(editEncounterId!=null){
 				try{
 					Encounter editEncounter = Context.getEncounterService().getEncounter(editEncounterId);
 					if(editEncounter!=null){
 						encounterDate = editEncounter.getEncounterDatetime();
 						obs = PatientRegistrationWebUtil.getPatientPayment(patient, encounterType, editEncounter, registrationLocation, encounterDate);
 					}
 				}catch(Exception e){
 					log.error("failed to retrieve encounter.", e);
 				}
 			}
 		}
 		
 		if(obs==null || (obs!=null && obs.size()<1)){
 			obs = PatientRegistrationWebUtil.getPatientPayment(patient, encounterType, null, registrationLocation, encounterDate);
 		}
 		if(obs!=null && obs.size()>0){
 			List<POCObservation> todayObs = new ArrayList<POCObservation>();
 			for(Obs ob : obs){
 				POCObservation pocObs = new POCObservation();
 				pocObs.setObsId(ob.getId());
 
                 if (ob.getConcept().getDatatype().isCoded()) {
                     Concept codedObs= ob.getValueCoded();
                     pocObs.setType(POCObservation.CODED);
                     pocObs.setId(codedObs.getId());
                     pocObs.setLabel(codedObs.getDisplayString());
                 }
                 else if (ob.getConcept().getDatatype().isText()) {
                     pocObs.setType(POCObservation.NONCODED);
                     pocObs.setId(new Integer(0));
                     pocObs.setLabel(ob.getValueText());
                 }
                 else if (ob.getConcept().getDatatype().isNumeric()) {
                    if (((ConceptNumeric) ob.getConcept()).isPrecise()) {
                        throw new IllegalArgumentException("Precise (i.e. non-integer) numeric concepts not supported");
                    }
                     pocObs.setType(POCObservation.NUMERIC);
                     pocObs.setId(ob.getValueNumeric().intValue());
                     if (ob.getConcept().equals(paymentAmountConcept)) {
                         pocObs.setLabel(getLabelFromMap(paymentAmounts, pocObs.getId().toString()));
                     }
                 }
 
 				pocObs.setConceptId(ob.getConcept().getConceptId());
 				pocObs.setConceptName(ob.getConcept().getDisplayString());
 				todayObs.add(pocObs);
 			}
 			model.addAttribute("todayObs", todayObs);
 		}
 		if(StringUtils.equals(createNew, "true")){
 			model.addAttribute("createNew", true);
 		}
 		String currentTask =PatientRegistrationWebUtil.getRegistrationTask(session);
 		if(StringUtils.isNotBlank(nextTask)){
 			model.addAttribute("nextTask", nextTask);
 		}else if(StringUtils.equalsIgnoreCase(currentTask, "retrospectiveEntry")){
 			model.addAttribute("nextTask", "primaryCareVisitEncounter.form");
 		}
 		model.addAttribute("encounterDate", PatientRegistrationUtil.clearTimeComponent(encounterDate));
 		return new ModelAndView("/module/patientregistration/workflow/primaryCareReceptionEncounter");	
 																	  
 	}
 
     private String getLabelFromMap(Map<String,String> labelToValueMap, String value) {
         for (Map.Entry<String, String> entry : labelToValueMap.entrySet()) {
             if (entry.getValue().equals(value)) {
                 return entry.getKey();
             }
         }
         throw new IllegalArgumentException("Cannot find " + value + " in " + labelToValueMap);
     }
 
     @RequestMapping(method = RequestMethod.POST)
 	public ModelAndView processPayment(
 			@ModelAttribute("patient") Patient patient		
 			,@RequestParam("listOfObs") String obsList	
 			,@RequestParam("hiddenEncounterYear") String encounterYear	
 			,@RequestParam("hiddenEncounterMonth") String encounterMonth	
 			,@RequestParam("hiddenEncounterDay") String encounterDay
 			,@RequestParam(value="hiddenNextTask", required = false) String nextTask
 			, HttpSession session			
 			, ModelMap model) {
 		
 		if(StringUtils.isNotBlank(obsList)){
 			List<Obs> observations = PatientRegistrationUtil.parseObsList(obsList);
 			
 			if(observations!=null && observations.size()>0){				
 				
 				//void existing observations
 				Location registrationLocation = PatientRegistrationWebUtil.getRegistrationLocation(session);					
 								
 				EncounterType encounterType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_CARE_RECEPTION_ENCOUNTER_TYPE();						
 				Calendar encounterDate = Calendar.getInstance();
 				
 				// only process if we have values for all three fields
 				if (StringUtils.isNotBlank(encounterYear) && StringUtils.isNotBlank(encounterMonth) && StringUtils.isNotBlank(encounterDay)) {					
 					Integer year;
 					Integer month;
 					Integer day;
 						
 					try {
 						year = Integer.valueOf(encounterYear);
 						month = Integer.valueOf(encounterMonth);
 						day = Integer.valueOf(encounterDay);
 					}
 					catch (Exception e) {
 						throw new APIException("Unable to parse encounter date", e);
 					}										
 					
 					// if everything is good, create the new encounter date and update it on the encounter we are creating					
 					encounterDate.set(Calendar.YEAR, year);
 					encounterDate.set(Calendar.MONTH, month - 1);  // IMPORTANT that we subtract one from the month here
 					encounterDate.set(Calendar.DAY_OF_MONTH, day);				
 				}
 
 				List<Obs> currentObs = PatientRegistrationWebUtil.getPatientPayment(patient, encounterType, null, registrationLocation, encounterDate.getTime());
 				if(currentObs!=null && currentObs.size()>0){
 					Set<Encounter> voidEncounters = new HashSet<Encounter>();
 					for(Obs voidOb : currentObs){
 						try{
 							Context.getObsService().voidObs(voidOb, "overwrite payment types");
 							voidEncounters.add(voidOb.getEncounter());
 						}catch(Exception e){
 							log.error("failed to void ob", e);
 						}
 					}
 					if(voidEncounters.size()>0){
 						for(Encounter voidEncounter : voidEncounters){
 							Context.getEncounterService().voidEncounter(voidEncounter, "voided reception encounter");
 						}
 					}
 				}
 				
 				Encounter encounter = new Encounter();				
 				encounter.setEncounterDatetime(encounterDate.getTime());
 				encounter.setEncounterType(encounterType);
 				encounter.setProvider(Context.getAuthenticatedUser().getPerson());
 				encounter.setLocation(PatientRegistrationWebUtil.getRegistrationLocation(session));
 				encounter.setPatient(patient);
 				for(Obs obs : observations){
 					obs.getValueCoded();						
 					encounter.addObs(obs);
 				}
 				Encounter e = Context.getService(EncounterService.class).saveEncounter(encounter);	
 				TaskProgress taskProgress = PatientRegistrationWebUtil.getTaskProgress(session);
 				if(taskProgress!=null){
 					taskProgress.setPatientId(patient.getId());
 					taskProgress.setProgressBarImage(PatientRegistrationConstants.RETROSPECTIVE_PROGRESS_3_IMG);			
 					Map<String, Integer> completedTasks = taskProgress.getCompletedTasks();
 					if(completedTasks == null){
 						completedTasks = new HashMap<String, Integer>();
 					}					
 					completedTasks.put("receptionTask", new Integer(1));
 					taskProgress.setCompletedTasks(completedTasks);
 					PatientRegistrationWebUtil.setTaskProgress(session, taskProgress);
 				}
 				if(StringUtils.isNotBlank(nextTask)){
 					return new ModelAndView("redirect:/module/patientregistration/workflow/" + nextTask + "?patientId=" + patient.getPatientId(), model);
 				}else{
 					return new ModelAndView("redirect:/module/patientregistration/workflow/patientDashboard.form?patientId=" + patient.getPatientId(), model);
 				}
 			}
 		
 		}
 		return new ModelAndView("redirect:/module/patientregistration/workflow/primaryCareReceptionTask.form");	
 	}
 	
 	
 }
