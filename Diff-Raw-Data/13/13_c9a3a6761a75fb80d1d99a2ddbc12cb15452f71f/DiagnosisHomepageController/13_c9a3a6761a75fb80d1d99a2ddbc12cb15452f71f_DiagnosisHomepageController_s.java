 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.diagnosiscapturerwanda;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptNameTag;
 import org.openmrs.Location;
 import org.openmrs.User;
 import org.openmrs.api.ConceptNameType;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.diagnosiscapturerwanda.queue.DiagnosisCaptureQueueService;
 import org.openmrs.module.diagnosiscapturerwanda.queue.QueueObj;
 import org.openmrs.module.diagnosiscapturerwanda.queue.jsonconverter.QueueSimpleObjectConverter;
 import org.openmrs.module.diagnosiscapturerwanda.util.DiagnosisUtil;
 import org.openmrs.util.OpenmrsConstants;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class DiagnosisHomepageController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 
 	//TODO:
 	@RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.GET)
     public void processHomePageGet(HttpSession session, ModelMap map){
 		map.put("locations", Context.getLocationService().getAllLocations(false));
 		map.put("user", Context.getAuthenticatedUser());
 		
 		//add user location to model, so default can be set in UI.
 		String locStr = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
         Location userLocation = null;
         try { 
             userLocation = Context.getLocationService().getLocation(Integer.valueOf(locStr));
         } catch (Exception ex){
             //pass
         }
         if (userLocation == null){
         	map.put("userLocation", null);
         } else {
         	map.put("userLocation", userLocation);
         }        
     }
 
     
     @RequestMapping(value="/module/diagnosiscapturerwanda/diagnosisHomepage",method=RequestMethod.POST)
     public void processHomePageSubmit(ModelMap model, HttpSession session, HttpServletRequest request){
     	String locationStr = request.getParameter("location");
     		
          if (locationStr != null && !locationStr.equals("")){
              
              Location location = Context.getLocationService().getLocation(Integer.valueOf(locationStr));
              if (location == null)
                  throw new NullPointerException();
              
              //note:  these allow you to switch seamlessly to the rwandaprimarycare module homepage.  the actual strings used by the two modules are the same.
              session.setAttribute(MetadataDictionary.SESSION_ATTRIBUTE_PRIMARY_CARE_WORKSTATION_LOCATION, location);
              session.setAttribute(MetadataDictionary.SESSION_ATTRIBUTE_DIAGNOSIS_WORKSTATION_LOCATION, location);
              Context.setVolatileUserData(MetadataDictionary.VOLATILE_USER_DATA_LOGIN_LOCATION, location);
              
              User user = Context.getAuthenticatedUser();
              model.addAttribute("user", user);
              
              if (Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION) == null || !Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION).equals(locationStr)){
                  user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, location.getLocationId().toString());
                  Context.getUserService().saveUser(user, null);
              }
          }    
     }
     
     /**
      * this loads the patientQueue, called by ajax
      * @param session
      * @param model
      * @return
      */
     @RequestMapping(value="/module/diagnosiscapturerwanda/getJSONQueue",method=RequestMethod.GET)
     public String getJSONQueue(HttpSession session, ModelMap model){
     	Map<Integer, QueueObj> map = Context.getService(DiagnosisCaptureQueueService.class).getServiceQueueMap();
     	model.put("json", DiagnosisUtil.convertToJSON((new QueueSimpleObjectConverter()).convert(map)));
     	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
     }	
     
     /**
      * this removes someone from the patient queue, called by ajax
      * @param patientId
      * @param encUuid
      * @param action
      * @param session
      * @param model
      * @return
      */
     @RequestMapping(value="/module/diagnosiscapturerwanda/processQueueItem",method=RequestMethod.GET)
     public String processQueueSelection( @RequestParam("patientId") int patientId,  
     			@RequestParam("encounterUuid") String encUuid,  
     			@RequestParam("action") String action,
     			HttpSession session, 
     			ModelMap model){
     	String json = "{\"result\": \"FAILED\",\"reason\":\" action can only be process, select or remove\"}";
     	try {
 	    	if (action.equals("process")) {
 	    		//this is now handled by the DiagnosisPatientDashboardController, so that direct jumps from registration to diagnosis app dashboard removes patients from queue...
 	    		//Context.getService(DiagnosisCaptureQueueService.class).selectQueueObjectByEncounterUuid(encUuid);
 	    		json = "{\"result\": \"SUCCESS\"}";
 	    	} else if (action.equals("skip")) {
 	    		Context.getService(DiagnosisCaptureQueueService.class).skipQueueObjectByEncounterUuid(encUuid);
 	    		json = "{\"result\": \"SUCCESS\"}";
 	    	} else if (action.equals("remove")) {
 	    		Context.getService(DiagnosisCaptureQueueService.class).removeFromQueue(encUuid);
 	    		json = "{\"result\": \"SUCCESS\"}";
 	    	}
     	} catch (Exception ex){
     		json = "{\"result\": \"FAILED\",\"reason\":\"" + ex.getLocalizedMessage() + "\"}";
     	}
     	model.put("json", json);
     	return "/module/diagnosiscapturerwanda/jsonAjaxResponse";
     }
     
     
     @RequestMapping(value="/module/diagnosiscapturerwanda/cleanupRwanda",method=RequestMethod.GET)
     public String cleanupRwanda( HttpSession session, 
     			ModelMap model){
     	
     	List<Concept> cList = Context.getConceptService().getAllConcepts();
 
     	for (ConceptNameTag cnt : Context.getConceptService().getAllConceptNameTags()){
     		if (cnt.getTag().equals("short_"))
     			cnt.setDescription("short general");
     		if (cnt.getTag().equals("short_en"))
     			cnt.setDescription("short english");
     		if (cnt.getTag().equals("short_fr"))
     			cnt.setDescription("short french");
     		Context.getConceptService().saveConceptNameTag(cnt);	
     	}
     	Context.getConceptService().getAllConceptNameTags();
     	
     	
     	
     	Concept x = Context.getConceptService().getConceptByUuid("de8e7a2a-32f6-41d5-aa34-65a1b2a51b40");
     	for (ConceptName cn : x.getNames(true)){
     		cn.setVoided(false);
     		cn.setVoidedBy(null);
     		cn.setVoidReason(null);
     		cn.setDateVoided(null);
     		cn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
     	}
     	Context.getConceptService().saveConcept(x);
     	
     	
     	
     	
     	Concept cTmp = Context.getConceptService().getConceptByUuid("3cd9cede-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iterTmp = cTmp.getNames(true).iterator();
 		while (iterTmp.hasNext()) {
 			ConceptName cn = iterTmp.next();
 			if (cn.getUuid().equals("3e18abda-26fe-102b-80cb-0017a47871b2")){
 				iterTmp.remove();
 				cTmp.removeName(cn);
 				Context.getConceptService().updateConceptIndex(cTmp);
 				Context.getConceptService().saveConcept(cTmp);
 				break;
 			}	
 		}
 		
 		Concept c1 = Context.getConceptService().getConceptByUuid("807721e6-e06b-47b3-af7e-3d933a2e90db");
     	Iterator<ConceptName> iter1 = c1.getNames(true).iterator();
 		while (iter1.hasNext()) {
 			ConceptName cn = iter1.next();
 			if (cn.getUuid().equals("f78303c0-d5db-102d-ad2a-000c29c2a5d7")){
 				iter1.remove();
 				c1.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c1);
 				Context.getConceptService().saveConcept(c1);
 				break;
 			}	
 		}
 		
 		Concept c2 = Context.getConceptService().getConceptByUuid("3cd507be-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter2 = c2.getNames(true).iterator();
 		while (iter2.hasNext()) {
 			ConceptName cn = iter2.next();
 			if (cn.getUuid().equals("94b66ee8-07d4-102c-b5fa-0017a47871b2")){
 				iter2.remove();
 				c2.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c2);
 				Context.getConceptService().saveConcept(c2);
 				break;
 			}	
 		}
 		
 		Concept c3 = Context.getConceptService().getConceptByUuid("3cd9cbf0-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter3 = c3.getNames(true).iterator();
 		while (iter3.hasNext()) {
 			ConceptName cn = iter3.next();
 			if (cn.getUuid().equals("3e18a5fe-26fe-102b-80cb-0017a47871b2")){
 				iter3.remove();
 				c3.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c3);
 				Context.getConceptService().saveConcept(c3);
 				break;
 			}	
 		}
 		
 		Concept c4 = Context.getConceptService().getConceptByUuid("3ce17fda-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter4 = c4.getNames(true).iterator();
 		while (iter4.hasNext()) {
 			ConceptName cn = iter4.next();
 			if (cn.getUuid().equals("3e225554-26fe-102b-80cb-0017a47871b2")){
 				iter4.remove();
 				c4.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c4);
 				Context.getConceptService().saveConcept(c4);
 				break;
 			}	
 		}
 
 		Concept c5 = Context.getConceptService().getConceptByUuid("3ce47cf8-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter5 = c5.getNames(true).iterator();
 		while (iter5.hasNext()) {
 			ConceptName cn = iter5.next();
 			if (cn.getUuid().equals("0b90a7de-15f5-102d-96e4-000c29c2a5d7")){
 				iter5.remove();
 				c5.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c5);
 				Context.getConceptService().saveConcept(c5);
 				break;
 			}	
 		}
 		
 		Concept c6 = Context.getConceptService().getConceptByUuid("3cde34a6-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter6 = c6.getNames(true).iterator();
 		while (iter6.hasNext()) {
 			ConceptName cn = iter6.next();
 			if (cn.getUuid().equals("3e1e6494-26fe-102b-80cb-0017a47871b2")){
 				iter6.remove();
 				c6.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c6);
 				Context.getConceptService().saveConcept(c6);
 				break;
 			}	
 		}
 		
 		Concept c7 = Context.getConceptService().getConceptByUuid("3cd9bff2-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter7 = c7.getNames(true).iterator();
 		while (iter7.hasNext()) {
 			ConceptName cn = iter7.next();
 			if (cn.getUuid().equals("f6200140-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("tuberculose mycobactéries pulmonaire");
 				Context.getConceptService().updateConceptIndex(c7);
 				Context.getConceptService().saveConcept(c7);
 				break;
 			}	
 		}
 		
 		Concept c8 = Context.getConceptService().getConceptByUuid("3cdc0a8c-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter8 = c8.getNames(true).iterator();
 		while (iter8.hasNext()) {
 			ConceptName cn = iter8.next();
 			if (cn.getUuid().equals("f623549e-d5db-102d-ad2a-000c29c2a5d7")){
 				iter8.remove();
 				c8.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c8);
 				Context.getConceptService().saveConcept(c8);
 				break;
 			}	
 		}
 		
 		Concept c10 = Context.getConceptService().getConceptByUuid("3cde899c-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter10 = c10.getNames(true).iterator();
 		while (iter10.hasNext()) {
 			ConceptName cn = iter10.next();
 			if (cn.getUuid().equals("f6471532-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("source d'eau");
 				Context.getConceptService().updateConceptIndex(c10);
 				Context.getConceptService().saveConcept(c10);
 				break;
 			}	
 		}
 		
 		
 		Concept c9 = Context.getConceptService().getConceptByUuid("3cdd0f04-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter9 = c9.getNames(true).iterator();
 		while (iter9.hasNext()) {
 			ConceptName cn = iter9.next();
 			if (cn.getUuid().equals("f63a2750-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("INFECTION OPPORTUNISTE OU COMORBIDITÉ ACTUELLES, CONFIRMÉES OU PRÉSUMÉES, NON-CODÉ");
 				Context.getConceptService().updateConceptIndex(c9);
 				Context.getConceptService().saveConcept(c9);
 				break;
 			}	
 		}
 		
 		Concept c99 = Context.getConceptService().getConceptByUuid("3cd6b85c-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter99 = c99.getNames(true).iterator();
 		while (iter99.hasNext()) {
 			ConceptName cn = iter99.next();
 			if (cn.getUuid().equals("f5c43f90-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("AMPLIFICATION EN CHAÎNE POLYMÉRASE DE CHLAMYDIA TRACHOMATIS, QUALITATIF");
 				Context.getConceptService().updateConceptIndex(c99);
 				Context.getConceptService().saveConcept(c99);
 				break;
 			}	
 		}
 		
 		
 		Concept c11 = Context.getConceptService().getConceptByUuid("3ce43b3a-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter11 = c11.getNames(true).iterator();
 		while (iter11.hasNext()) {
 			ConceptName cn = iter11.next();
 			if (cn.getUuid().equals("f6a84a50-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("affaissement du poumon");
 				Context.getConceptService().updateConceptIndex(c11);
 				Context.getConceptService().saveConcept(c11);
 				break;
 			}	
 		}
 		
 		
 		Concept c12 = Context.getConceptService().getConceptByUuid("da36e0f6-81b3-4e82-9df9-43cff48a0e03");
     	Iterator<ConceptName> iter12 = c12.getNames(true).iterator();
 		while (iter12.hasNext()) {
 			ConceptName cn = iter12.next();
 			if (cn.getUuid().equals("0b8feaa6-15f5-102d-96e4-000c29c2a5d7")){
 				iter12.remove();
 				c12.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c12);
 				Context.getConceptService().saveConcept(c12);
 			}	
 		}
 		
 		
 		Concept c13 = Context.getConceptService().getConceptByUuid("3cdc01a4-26fe-102b-80cb-0017a47871b2");
     	Iterator<ConceptName> iter13 = c13.getNames(true).iterator();
 		while (iter13.hasNext()) {
 			ConceptName cn = iter13.next();
 			if (cn.getUuid().equals("3e1b8b84-26fe-102b-80cb-0017a47871b2")){
 				iter13.remove();
 				c13.removeName(cn);
 				Context.getConceptService().updateConceptIndex(c13);
 				Context.getConceptService().saveConcept(c13);
 			}	
 		}
 		
 		Concept c14 = Context.getConceptService().getConceptByUuid("edd2cae1-99e7-4219-b5f9-fa512a69fed2");
     	Iterator<ConceptName> iter14 = c14.getNames(true).iterator();
 		while (iter14.hasNext()) {
 			ConceptName cn = iter14.next();
 			if (cn.getUuid().equals("f733a942-d5db-102d-ad2a-000c29c2a5d7")){
 				cn.setName("RS. DE L'EXAMEN NEUROLOGIQUE");
 				Context.getConceptService().updateConceptIndex(c14);
 				Context.getConceptService().saveConcept(c14);
 				break;
 			}	
 		}
 		
     	//remove empty names
     	for (Concept c : cList){
     		boolean needsUpdate = false;
     		
     		Iterator<ConceptDescription> iterCD = c.getDescriptions().iterator();
     		while (iterCD.hasNext()) {
     			ConceptDescription cd = iterCD.next();
     			if (cd.getDescription() == null  || cd.getDescription().trim().equals("")){
     				iterCD.remove();
     				c.removeDescription(cd);
     				needsUpdate = true;
     				System.out.println("Removing empty concept description on " + c.getId());
     			}
     		}
     		
     		//cleaning out empty concept names
     		Iterator<ConceptName> iter = c.getNames(true).iterator();
     		while (iter.hasNext()) {
     			ConceptName cn = iter.next();
     			if(cn.getName() == null || cn.getName().trim().equals("")){
     				iter.remove();
     				c.removeName(cn);
     				System.out.println("Removing empty concept name");
     				needsUpdate = true;
     			}
     			else if (cn.getUuid().equals("f63a2750-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("INFECTION OPPORTUNISTE OU COMORBIDITÉ ACTUELLES, CONFIRMÉES OU PRÉSUMÉES, NON-CODÉ");
     				needsUpdate = true;
     			}
     			else if (cn.getUuid().equals("3e0b767c-26fe-102b-80cb-0017a47871b2")){
     				cn.setName("SEXUALLY TRANSMITTED INFECTION DIAGNOSIS");
     				needsUpdate = true;
     			}
     			else if (cn.getUuid().equals("f7278a40-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("diabète sucré");
     				needsUpdate = true;
     			} else if (cn.getUuid().equals("f6d53c2c-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("de transfert de sortie");
     				needsUpdate = true;
     			} else if (cn.getUuid().equals("f6200140-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("tuberculose mycobactéries pulmonaire");
     				needsUpdate = true;   
     			}	else if (cn.getUuid().equals("f67bd736-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("quelquefois");
     				needsUpdate = true;   
     			}	else if (cn.getUuid().equals("f6a84a50-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("affaissement du poumon");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f7695aa6-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("PARTENAIRES SEXUELS MULTIPLES PARTENAIRE");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f777474c-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("DE LA MENOPAUSE");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f6937f26-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("EXAMEN PULMONAIRE CONSTRUCT");
     				needsUpdate = true;
     			}   else if (cn.getUuid().equals("f6f2c0c6-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("EXAMEN PULMONAIRE");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("3e0b77e4-26fe-102b-80cb-0017a47871b2")){
     				iter.remove();
     				c.removeName(cn);
     				needsUpdate = true;
     			}   else if (cn.getUuid().equals("94adc2e8-07d4-102c-b5fa-0017a47871b2")){
     				iter.remove();
     				c.removeName(cn);
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f598d8c8-d5db-102d-ad2a-000c29c2a5d7")){
     				iter.remove();
     				c.removeName(cn);
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("3e142ba0-26fe-102b-80cb-0017a47871b2")){
     				cn.setName("pour bicyclette");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f67bf0ae-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("aucun moment");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f61fb0d2-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("TUBERCULOSE EXTRA PULMONAIRE");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f67ba1c6-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("très bon");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("dddf1d1a-3a6d-4705-80a6-e2921874a2d4")){
     				iter.remove();
     				c.removeName(cn);
     				needsUpdate = true; 
     			}	else if (cn.getUuid().equals("f6ec9124-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("POST-OPÉRATOIRE");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("3e1aa124-26fe-102b-80cb-0017a47871b2")){
     				iter.remove();
     				c.removeName(cn);
     				needsUpdate = true; 
     			}	else if (cn.getUuid().equals("3e1616ae-26fe-102b-80cb-0017a47871b2")){
     				cn.setName("IS THIS PERSON AN ORPHAN");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("3e177df0-26fe-102b-80cb-0017a47871b2")){
     				cn.setName("TO WHERE WAS THE PATIENT HOSPITALIZED");
     				needsUpdate = true;
     			}	else if (cn.getUuid().equals("f560a62e-d5db-102d-ad2a-000c29c2a5d7")){
     				cn.setName("RADIOGRAPHIE-PULMONAIRE");
     				needsUpdate = true;
     			}	
     		}
     		
     		//duplicate names in locale:
     		ConceptName fullySpecifiedFr = null;
     		ConceptName fullySpecifiedEn = null;
     		List<String> unmarkedNames = new ArrayList<String>();
     		
     		for (ConceptName cn : c.getNames()){
     			if (OpenmrsUtil.nullSafeEquals(cn.getConceptNameType(), ConceptNameType.FULLY_SPECIFIED) && cn.getLocale().equals(Locale.ENGLISH)){
     				fullySpecifiedEn = cn;
     				//System.out.println("Setting fully specified name for english to " + cn.getName());
     			}	
     			if (OpenmrsUtil.nullSafeEquals(cn.getConceptNameType(), ConceptNameType.FULLY_SPECIFIED) && cn.getLocale().equals(Locale.FRENCH)){
     				fullySpecifiedFr = cn;
     				//System.out.println("Setting fully specified name for french  to " + cn.getName());
     			}	
     		}
     		Iterator<ConceptName> iterx = c.getNames().iterator();
     		while (iterx.hasNext()) {
     			ConceptName cn = iterx.next();
     			if (
     					((cn.getLocale().equals(Locale.ENGLISH) && !OpenmrsUtil.nullSafeEquals(cn.getConceptNameType(), ConceptNameType.FULLY_SPECIFIED) && fullySpecifiedEn != null && cn.getName().trim().toLowerCase().equals(fullySpecifiedEn.getName().trim().toLowerCase())))
     					||
     					((cn.getLocale().equals(Locale.FRENCH) && !OpenmrsUtil.nullSafeEquals(cn.getConceptNameType(), ConceptNameType.FULLY_SPECIFIED) && fullySpecifiedFr != null && cn.getName().trim().toLowerCase().equals(fullySpecifiedFr.getName().trim().toLowerCase())))
     					||
     					((unmarkedNames.contains(cn.getName().toUpperCase()) && cn.getConceptNameType() == null))
     			
     			){
     				iterx.remove();
     				c.removeName(cn);
     				System.out.println("Removing duplicate name in locale for concept " + c.getId());
     				needsUpdate = true;
     			} else if (cn.getConceptNameType() == null){
     				unmarkedNames.add(cn.getName().toUpperCase());
     			}
     		}
     		
     		
     		
     		
     		//activating voided fully specified names when there are no non-voided fully specified names
     		boolean foundFullySpecified = false;
     		for (ConceptName cn : c.getNames()){
     			if (cn.isFullySpecifiedName()){
     				foundFullySpecified = true;
     				break;
     			}
     		}
     		if (!foundFullySpecified){
     			for (ConceptName cn : c.getNames(true)){
         			if (cn.isFullySpecifiedName() && cn.isVoided()){
         				cn.setVoided(false);
         				cn.setVoidedBy(null);
         				cn.setVoidReason(null);
         				cn.setDateVoided(null);
         				System.out.println("Updating " + c.getId() + " to have at least one fully specified name");
         				needsUpdate = true;
         				break;
         			}
         		}
     		}
     		
     		//removing duplicate concept maps
     		Set<ConceptMap> newList = new HashSet<ConceptMap>();
     		Iterator<ConceptMap> iterCM = c.getConceptMappings().iterator();
     		while (iterCM.hasNext()) {
     			ConceptMap cm = iterCM.next();
     			if(containsDuplicateMapping(newList, cm)){
     				iterCM.remove();
     				c.removeConceptMapping(cm);
     				needsUpdate = true;
     				System.out.println("Removing duplicate concept map for " + c.getId());
     			} else {
     				newList.add(cm);
     			}
     		}
     		
 
     		if (needsUpdate){
     			if ((c.getNames() == null || c.getNames().size() == 0) && c.isRetired()) {//if there are no names
     				try {
     					Context.getConceptService().purgeConcept(c);
     				} catch (Exception ex){
     					ConceptName cn = new ConceptName();
     					cn.setConcept(c);
     					cn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
     					cn.setCreator(Context.getAuthenticatedUser());
     					cn.setDateCreated(new Date());
     					cn.setLocale(Locale.ENGLISH);
     					cn.setLocalePreferred(true);
     					cn.setName("Placeholder name for piece of shit 1.9 concept validator for a dead concept" + c.getId());
     					cn.setVoided(false);
     					c.addName(cn);
     					for (ConceptName cnTmp : c.getNames(true)){
     						cnTmp.getTags();
     					}
     					System.out.println("Saving Concept with new ConceptName for " + c.getId());
     					Context.getConceptService().updateConceptIndex(c);
     					c = Context.getConceptService().saveConcept(c);
     				}
     			} else {
     				System.out.println("Saving Concept " + c.getId());
     				Context.getConceptService().updateConceptIndex(c);
     				c = Context.getConceptService().saveConcept(c);
     			}	
     		}	
     	}
 
     	return "redirect:/module/diagnosiscapturerwanda/diagnosisHomepage.list";
     }
     
     private boolean containsDuplicateMapping(Set<ConceptMap> cmList, ConceptMap cm){
     	
     	for (ConceptMap cmTmp : cmList){
     		if (cm.getConceptReferenceTerm().getCode().equals(cmTmp.getConceptReferenceTerm().getCode()) 
     				&& cm.getConceptReferenceTerm().getConceptSource().getName().equals(cmTmp.getConceptReferenceTerm().getConceptSource().getName())){
     			return true;
     		}
     	}
     	
     	return false;
     }
     
     
     
 }
