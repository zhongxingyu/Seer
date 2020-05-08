 package org.openmrs.module.patientregistration.controller.ajax;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonName;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.utils.FeatureToggleProperties;
 import org.openmrs.module.importpatientfromws.RemotePatient;
 import org.openmrs.module.importpatientfromws.api.ImportPatientFromWebService;
 import org.openmrs.module.paperrecord.PaperRecordService;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.service.PatientRegistrationService;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.module.patientregistration.util.SortableValueMap;
 import org.openmrs.module.patientregistration.util.UserActivityLogger;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.validator.PatientIdentifierValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 
 
 @Controller
 public class PatientSearchAjaxController {
 
     protected final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     @Qualifier("paperRecordService")
     private PaperRecordService paperRecordService;
 
 	@RequestMapping("/module/patientregistration/ajax/patientIdentifierSearch.form")
 	public void patientIdentifierSearch(@RequestParam("patientIdentifier") String patientIdentifier,
 			ModelMap model, 
 			HttpServletRequest request, 
 			HttpServletResponse response,
             @SpringBean("featureToggles") FeatureToggleProperties featureToggleProperties,
 			@RequestParam(value = "resultsCounter", required = false) Integer resultsCounter  ) 
 	throws Exception {
 	
 		List<Patient> patientList = null;
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         PrintWriter out = response.getWriter();
         out.print("[");
 
 		if(StringUtils.isNotBlank(patientIdentifier)){			
 			List<PatientIdentifierType> identifierTypes = new ArrayList<PatientIdentifierType>();			
 			PatientIdentifierType preferredIdentifierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE();	
 			if(preferredIdentifierType!=null){
                 PatientIdentifierValidator.validateIdentifier(patientIdentifier, preferredIdentifierType);
 				identifierTypes.add(preferredIdentifierType);
 				patientList = Context.getPatientService().getPatients(null, patientIdentifier, identifierTypes, true);
 			}
 
             if(CollectionUtils.isEmpty(patientList)){
                 boolean import_mpi_patients = featureToggleProperties.isFeatureEnabled("import_mpi_patients");
                log.error("feature toggle: import_mpi_patients=" + import_mpi_patients);
                 if(import_mpi_patients){
                     try{
                         List<RemotePatient> remotePatients = Context.getService(ImportPatientFromWebService.class).searchRemoteServer(PatientRegistrationConstants.MPI_REMOTE_SERVER, patientIdentifier, PatientRegistrationConstants.MPI_CONNECT_TIMEOUT);
                         if(remotePatients!=null && remotePatients.size()>0){
                             PatientRegistrationWebUtil.saveToCache(remotePatients, request.getSession());
                             for(RemotePatient remotePatient : remotePatients){
                                 String jsonPatient = PatientRegistrationUtil.convertRemotePatientToJson(remotePatient);
                                 if(StringUtils.isNotBlank(jsonPatient)){
                                     out.print(jsonPatient);
                                     out.print("]");
                                     //we got an exact match on the remote MPI server
                                     return;
                                 }
                             }
                         }
                     }catch(Exception e){
                         log.error("error finding remote MPI patients", e);
                     }
                 }
                 PatientIdentifierType dossierType = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_NUMERO_DOSSIER();
                 if(dossierType!=null){
                     List<Patient> patientWithDossier = null;
                     identifierTypes = new ArrayList<PatientIdentifierType>();
                     identifierTypes.add(dossierType);
                     patientWithDossier = Context.getPatientService().getPatients(null, patientIdentifier, identifierTypes, true);
                     if(patientWithDossier!=null && patientWithDossier.size()>0){
                         patientWithDossier = Context.getPatientService().getPatients(null, patientIdentifier, identifierTypes, false);
                     }
                     if(patientWithDossier!=null){
                         if(patientList==null){
                             patientList=patientWithDossier;
                         }else{
                             patientList.addAll(patientWithDossier);
                         }
                     }
                 }
             }
 			
 		}
 
     	int counter=1;
     	// start the JSON
     	if(patientList!=null){
 	    	Iterator<Patient> i = patientList.iterator();    	
 	    	while(i.hasNext()) {	    		
 	    		out.print(PatientRegistrationUtil.convertPatientToJSON(i.next()));	  
 	    		if(resultsCounter!=null && resultsCounter.intValue()<counter){
 	    			break;
 	    		}
 	    		counter++;
 	    		if (i.hasNext()) {
 	    			out.print(",");
 	    		}
 	    	}
     	}
     	// close the JSON
 		out.print("]");
 		
 	}
 
     @RequestMapping("/module/patientregistration/ajax/patientSearch.form")
 	public void patientSearch(@ModelAttribute("patientName") PersonName patientName, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 	
 		long start = System.currentTimeMillis();
 		log.debug("patientSearch start: " + start);
 		List<Patient> patientList = Context.getService(PatientRegistrationService.class).exactSearch(patientName);
 		List<Patient> filteredPatientList = Context.getService(PatientRegistrationService.class).removeUnknownPatients(patientList);
 		PatientRegistrationUtil.convertPatientListToJson(filteredPatientList, response);
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/patientSoundexSearch.form")
 	public void patientSoundexSearch(@ModelAttribute("patientName") PersonName patientName, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 			
 		List<Patient> patientList = Context.getService(PatientRegistrationService.class).exactSearch(patientName);	
 		List<Patient> filteredPatientList = Context.getService(PatientRegistrationService.class).removeUnknownPatients(patientList);
 		Set<Integer> patientIds = null;
 		if(filteredPatientList!=null && filteredPatientList.size()>0){
 			patientIds =  new HashSet<Integer>();
 			for(Patient patient : filteredPatientList){
 				patientIds.add(patient.getId());
 			}
 		}
 		AdministrationService as = Context.getAdministrationService();
     	String processorName =  as.getGlobalProperty("namephonetics.givenNameStringEncoder");
     	
 		String encodedFirstName = patientName.getGivenName();
 		if(StringUtils.isNotBlank(encodedFirstName)){
 			encodedFirstName = PatientRegistrationUtil.encodeName(encodedFirstName, processorName);
 			if(log.isDebugEnabled()){
 				log.debug(String.format("firstName=%s; encodedFirstName=%s", patientName.getGivenName(), encodedFirstName ));
 			}
 		}
 		String encodedLastName = patientName.getFamilyName();
 		if(StringUtils.isNotBlank(encodedLastName)){
 			encodedLastName = PatientRegistrationUtil.encodeName(encodedLastName, processorName);
 			if(log.isDebugEnabled()){
 				log.debug(String.format("lastName=%s; encodedLastName=%s", patientName.getFamilyName(), encodedLastName ));
 			}
 		}
 		
 		List<Integer> personNameId =  Context.getService(PatientRegistrationService.class).getPhoneticsPersonId(encodedFirstName, encodedLastName);
         List<Integer> lastNameId=Context.getService(PatientRegistrationService.class).getPhoneticsPersonId(encodedLastName, encodedFirstName);
         if(lastNameId!=null && lastNameId.size()>0){
            for(Integer id : lastNameId){
                if(!personNameId.contains(id)){
                    personNameId.add(id);
                }
            }
         }
 		List<Integer> unknownPersonId = Context.getService(PatientRegistrationService.class).getUnknownPersonId();
 		List<Patient> patientPhonetics = null;
 		if(personNameId!=null && personNameId.size()>0){	
 			if(log.isDebugEnabled()){
 				log.debug(String.format("we found %d soundex matches", personNameId.size()));
 			}
 			if(unknownPersonId!=null && unknownPersonId.size()>0){
 				if(log.isDebugEnabled()){
 					log.debug(String.format("we found %d unknown patients", unknownPersonId.size()));
 				}
 				personNameId.removeAll(unknownPersonId);
 				if(log.isDebugEnabled()){
 					log.debug(String.format("number of patients left after the unknown patients have been removed: %d", personNameId.size()));
 				}
 			}
 			patientPhonetics = Context.getService(PatientRegistrationService.class).getPatientsByNameId(personNameId);				
 		}
 		if(patientPhonetics!=null && patientPhonetics.size()>0){
 			if(filteredPatientList!=null && filteredPatientList.size()>0){
 				for(Patient patient: patientPhonetics){
 					if(!patientIds.contains(patient.getId())){
 						filteredPatientList.add(patient);
 					}
 				}
 			}else{
 				filteredPatientList = patientPhonetics;
 			}
 		}
 		
 		PatientRegistrationUtil.convertPatientListToJson(filteredPatientList, response);
 		
 	}
 	
 
 	
 	
 	@RequestMapping("/module/patientregistration/ajax/identifierExactLookup.form")
 	public void identifierExactLookup(@RequestParam("lookupInfo") String searchString
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 	
 		
 		List<Patient> patientList = null;
 		if(StringUtils.isNotBlank(searchString)){
 			patientList = Context.getPatientService().getPatients(null, searchString, null, true);						
 		}
 		PatientRegistrationUtil.convertPatientListToJson(patientList, response);
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/identifierPartialLookup.form")
 	public void identifierPartialLookup(@RequestParam("lookupInfo") String searchString
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 	
 		
 		List<Patient> patientList = null;
 		if(StringUtils.isNotBlank(searchString)){
 			patientList = Context.getPatientService().getPatients(null, searchString, null, false);						
 		}
 		PatientRegistrationUtil.convertPatientListToJson(patientList, response);
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/removeFalseDuplicates.form")
 	public void removeFalseDuplicate(
 			@RequestParam(value = "patientId", required = true) Integer patientId
 			, @RequestParam(value = "duplicateId", required = true) Integer duplicateId
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 	
 				
 		PatientRegistrationWebUtil.addPocFalseDuplicatePatient(patientId, duplicateId, request.getSession());
 		PatientRegistrationWebUtil.addPocFalseDuplicatePatient(duplicateId, patientId, request.getSession());
 	
 		response.setContentType("text/html");
     	response.setCharacterEncoding("UTF-8");
     	PrintWriter out=null;
 		try {
 			out = response.getWriter();
 			out.write("ok");
 		} catch (IOException e) {
 			log.error("failed to get the servlet response writer", e);
 			throw new Exception(e);
 		}
 	}
 	@RequestMapping("/module/patientregistration/ajax/addPatientDuplicate.form")
 	public void addPatientDuplicate(
 			@RequestParam(value = "patientId", required = true) Integer patientId
 			, @RequestParam(value = "duplicateId", required = true) Integer duplicateId
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 		
 		PatientRegistrationWebUtil.addPocDuplicatePatient(patientId, duplicateId, request.getSession());
 		PatientRegistrationWebUtil.addPocDuplicatePatient(duplicateId, patientId, request.getSession());
 		
 		response.setContentType("text/html");
     	response.setCharacterEncoding("UTF-8");
     	PrintWriter out=null;
 		try {
 			out = response.getWriter();
 			out.write("ok");
 		} catch (IOException e) {
 			log.error("failed to get the servlet response writer", e);
 			throw new Exception(e);
 		}
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/patientNameLookup.form")
 	public void patientNameLookup(@RequestParam("lookupInfo") String searchString
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 	
 		
 		List<Patient> patientList = null;
 		if(StringUtils.isNotBlank(searchString)){
 			String names[]= StringUtils.split(searchString);
 			PersonName personName = new PersonName();
 			if(names!=null && names.length>0){
 				personName.setGivenName(names[0]);
 				if(names.length>1){
 					personName.setFamilyName(names[1]);
 				}
 				patientList = Context.getService(PatientRegistrationService.class).search(personName);
 			}										
 		}
 		PatientRegistrationUtil.convertPatientListToJson(patientList, response);
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/patientLookup.form")
 	public void patientLookup(@RequestParam("lookupInfo") String searchString
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) throws Exception {
 	
 		String message = "Lookup patient <" + searchString + ">";
 		UserActivityLogger.logActivity(request.getSession(), PatientRegistrationConstants.ACTIVITY_PATIENT_LOOKUP_STARTED, message);
 		List<Patient> patientList = null;
 		if(StringUtils.isNotBlank(searchString)){
 			List<Patient> patientIds = Context.getPatientService().getPatients(null, searchString, null, false);
 			if(patientIds!=null && patientIds.size()>0){
 				patientList= patientIds; //(List<Patient>) ListUtils.sum(patientList, patientIds);
 			}else{		
 				/*
 				patientIds = Context.getPatientService().getPatients(null, searchString, null, false); 
 				if(patientIds!=null && patientIds.size()>0){
 					patientList= patientIds; //(List<Patient>) ListUtils.sum(patientList, patientIds);
 				}else{
 				*/	
 					String names[]= StringUtils.split(searchString);
 					PersonName personName = new PersonName();
 					if(names!=null && names.length>0){
 						personName.setGivenName(names[0]);
 						if(names.length>1){
 							personName.setFamilyName(names[1]);
 						}
 						patientList = Context.getService(PatientRegistrationService.class).search(personName);
 					}
 				//}
 			}
 			
 		}
 		
 		PatientRegistrationUtil.convertPatientListToJson(patientList, response);
 		message = "Found " + (patientList == null ? 0 : patientList.size()) + " patients";
 		UserActivityLogger.logActivity(request.getSession(), PatientRegistrationConstants.ACTIVITY_PATIENT_LOOKUP_COMPLETED, message);
 	}
 	
 	@RequestMapping("/module/patientregistration/ajax/patientNameSearch.form")
 	public void patientFirstNameSearch(@RequestParam("patientInputName") String patientName, ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 			
 		Set<String> patientNames= Context.getService(PatientRegistrationService.class).searchNames(patientName, "givenName");
 		
 		Set<String> familyNames= Context.getService(PatientRegistrationService.class).searchNames(patientName, "familyName");
 		
 		if(patientNames!=null){
 			if(familyNames!=null){
 				patientNames.addAll(familyNames);
 			}
 		}else if(familyNames!=null){
 			patientNames=familyNames;
 		}
 		
 		response.setContentType("application/json");
     	response.setCharacterEncoding("UTF-8");
     	PrintWriter out = response.getWriter();
 
     	// start the JSON
     	out.print("[");
     	
     	Iterator<String> i = patientNames.iterator();
     	
     	while(i.hasNext()) {    		
     		out.print(PatientRegistrationUtil.convertPatientNameToJSON(i.next()));
     	
     		if (i.hasNext()) {
     			out.print(",");
     		}
     	}
     	
     	// close the JSON
 		out.print("]");
 	}
 	@RequestMapping("/module/patientregistration/ajax/patientNameOccurrencesSearch.form")
 	public void patientNameOccurenceSearch(
 			  @RequestParam("searchFieldName") String patientName
 			, @RequestParam(value = "resultsCounter", required = false) Integer resultsCounter  
 			, ModelMap model
 			, HttpServletRequest request
 			, HttpServletResponse response) 
 	throws Exception {
 		
 		Map<String, Integer> patientFirstNames = Context.getService(PatientRegistrationService.class).searchNamesByOccurence(patientName, "givenName");		
 		Map<String, Integer> patientLastNames = Context.getService(PatientRegistrationService.class).searchNamesByOccurence(patientName, "familyName");
 		
 		SortableValueMap<String, Integer> nameOccurrences = null;
 				
 		if(patientFirstNames!=null && patientFirstNames.size()>0){
 			nameOccurrences = new SortableValueMap<String, Integer>(patientFirstNames);
 		}	
 		if(patientLastNames!=null && patientLastNames.size()>0){
 			if(nameOccurrences!=null){
 				for(String keyName : patientLastNames.keySet()){
 					Integer firstCounter = nameOccurrences.get(keyName);
 					Integer lastNameCounter = patientLastNames.get(keyName);
 					if(firstCounter!=null){
 						nameOccurrences.put(keyName, firstCounter + lastNameCounter);
 					}else{
 						nameOccurrences.put(keyName, lastNameCounter);
 					}
 				}
 			}else{
 				nameOccurrences = new SortableValueMap<String, Integer>(patientLastNames);
 			}
 		}
 		
 		if(nameOccurrences!=null){
 			nameOccurrences.sortByValue();
 		}
 		
 		response.setContentType("application/json");
     	response.setCharacterEncoding("UTF-8");
     	PrintWriter out = response.getWriter();
 
     	// start the JSON
     	out.print("[");
     	if(nameOccurrences!=null && nameOccurrences.size()>0){
     		boolean firstRecord = true;
     		int nameCounter=1;
 	    	for(String name : nameOccurrences.keySet()){
 	    		if(resultsCounter!=null && resultsCounter.intValue()<nameCounter){
 	    			break;
 	    		}
 	    		Integer counter = nameOccurrences.get(name);
 	    		if(!firstRecord){
 	    			out.print(",");
 	    		}else{
 	    			firstRecord=false;
 	    		}
 	    		out.print(PatientRegistrationUtil.convertPatientNameOccurrencesToJSON(name, counter));
 	    		nameCounter++;
 	    	}
     	}
     	
     	// close the JSON
 		out.print("]");
 	}
 }
