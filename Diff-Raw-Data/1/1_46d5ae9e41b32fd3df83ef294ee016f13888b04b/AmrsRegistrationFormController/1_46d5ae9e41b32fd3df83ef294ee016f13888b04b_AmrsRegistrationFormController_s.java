 package org.openmrs.module.amrsregistration.web.controller;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Person;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.propertyeditor.ConceptEditor;
 import org.openmrs.propertyeditor.LocationEditor;
 import org.openmrs.propertyeditor.PatientIdentifierTypeEditor;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.ModelAndViewDefiningException;
 import org.springframework.web.servlet.mvc.AbstractWizardFormController;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class AmrsRegistrationFormController extends AbstractWizardFormController {
 	
     private Log log = LogFactory.getLog(super.getClass());
 	
 	public AmrsRegistrationFormController() {
 	}
 	
 	protected Object formBackingObject(HttpServletRequest paramHttpServletRequest) throws ModelAndViewDefiningException {
 		return getNewPatient();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.springframework.web.servlet.mvc.AbstractWizardFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors, int)
 	 */
 	@Override
 	protected Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors, int page) throws Exception {
 		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
 		localHashMap.put("emptyIdentifier", new PatientIdentifier());
 		localHashMap.put("emptyName", new PersonName());
 		localHashMap.put("emptyAddress", new PersonAddress());
 		return localHashMap;
 	}
 	
 	protected int getTargetPage(HttpServletRequest paramHttpServletRequest, Object paramObject, Errors paramErrors, int paramInt) {
 		
 		Patient patient = (Patient) paramObject;
 		Patient patientSearched = null;
 		
 		int targetPage = super.getTargetPage(paramHttpServletRequest, paramObject, paramErrors, paramInt);
 		int currentPage = super.getCurrentPage(paramHttpServletRequest);
 		
 		PatientService patientService = Context.getPatientService();
 		String idCard = ServletRequestUtils.getStringParameter(paramHttpServletRequest, "idCardInput", null);
 		
 		// only do search if we're coming from the start page
 		if (currentPage == 0 && idCard != null) {
 			// get the scanned id and search for patients with that id
 			List<Patient> patients = patientService.getPatients(null, idCard, null, true);
 			// This needs to be exactly match a single patient. if more then one patient are found, then the id
 			// card possibly has a null value.
 			if (patients.size() == 1) {
 				patientSearched = patients.get(0);
 				
 				// hacky way to get all the patient data. need further research on this
 				for (PatientIdentifier identifier : patientSearched.getIdentifiers()) {
 	                patient.addIdentifier(identifier);
                 }
 				copyPatient(patient, patientSearched);
 				patient.getAttributeMap();
 				
 				// patient with matching id are found, take to the review page
 				targetPage = 2;
 			}
 		}
 		
 		// Check if it is really a back request, not a refresh page
 		// This is done because in the "start page" the name of the "GO" button is "_target1" which told the controller
 		// that the next page is the "edit page" (default behavior). But above code changes the flow from "start page" to
 		// "review page", but the "_target1" is still inside the parameter.		
 		if ((currentPage == getPageCount() - 1) && (targetPage == getPageCount() - 2)) {
 			String back = ServletRequestUtils.getStringParameter(paramHttpServletRequest, "_target1", null);
 			if (!"back".equalsIgnoreCase(back)) {
 				targetPage = currentPage;
 			}
 		}
 		
 		switch (targetPage) {
 			case 0:
 				patient = null;
 				break;
 			case 1:
 				if (patient == null) {
 					patient = ((Patient) paramObject);
 				}
 				break;
 			case 2:
 				
 				String[] ids = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "identifier_");
 				String[] idTypes = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "identifierType_");
 				String[] preferredIds = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "identifier.preferred_");
 				if (ids != null || idTypes != null || preferredIds != null) {
 					int maxIds = 0;
 					if (ids != null && ids.length > maxIds)
 						maxIds = ids.length;
 					if (idTypes != null && idTypes.length > maxIds)
 						maxIds = idTypes.length;
 					if (preferredIds != null && preferredIds.length > maxIds)
 						maxIds = preferredIds.length;
 					
 					//TODO: method to remove null identifier from set
 					// the null identifier shouldn't be in the identifiers set from the first place.
 					// this block can be omitted from the code when the set doesn't contains null ids
 					if (maxIds > 0) {
 						List<PatientIdentifier> identifiers = new ArrayList<PatientIdentifier>(patient.getIdentifiers());
 						for (int j = 0; j < identifiers.size(); j++) {
 	                        PatientIdentifier patientIdentifier = identifiers.remove(j);;
 	                        if (patientIdentifier.getPatient() == null &&
 	                        		patientIdentifier.getIdentifierType() == null &&
 	                        		patientIdentifier.getIdentifier() == null)
 	                        	identifiers.add(patientIdentifier);
 						}
 						Set<PatientIdentifier> identifiersSet = new HashSet<PatientIdentifier>(identifiers);
 						
 						patient.setIdentifiers(identifiersSet);
 					}
 					
 					for (int j = 0; j < maxIds; j++) {
 	                    PatientIdentifier identifier = new PatientIdentifier();
 	                    identifier.setIdentifier(ids[j]);
 	                    identifier.setIdentifierType(patientService.getPatientIdentifierType(Integer.valueOf(idTypes[j])));
 						if (preferredIds != null && preferredIds.length > j)
 							identifier.setPreferred(new Boolean(preferredIds[j]));
 						patient.addIdentifier(identifier);
                     }
 				}
 
 				String[] givenNames = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "givenName_");
 				String[] middleNames = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "middleName_");
 				String[] familyNamePrefixes = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "familyNamePrefix_");
 				String[] familyNames = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "familyName_");
 				String[] familyName2s = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "familyName2_");
 				String[] familyNameSuffixes = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "familyNameSuffix_");
 				String[] degrees = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "degree_");
 				String[] prefixes = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "prefix_");
 				String[] preferredNames = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "personName.preferred_");
 				
 				if (givenNames != null || middleNames != null ||
 						familyNamePrefixes != null || familyNames != null ||
 						familyName2s != null || familyNameSuffixes != null ||
 						degrees != null || prefixes != null || preferredNames != null) {
 					int maxNames = 0;
 					if (givenNames != null && givenNames.length > maxNames)
 						maxNames = givenNames.length;
 					if (middleNames != null && middleNames.length > maxNames)
 						maxNames = middleNames.length;
 					if (familyNamePrefixes != null && familyNamePrefixes.length > maxNames)
 						maxNames = familyNamePrefixes.length;
 					if (familyNames != null && familyNames.length > maxNames)
 						maxNames = familyNames.length;
 					if (familyName2s != null && familyName2s.length > maxNames)
 						maxNames = familyName2s.length;
 					if (familyNameSuffixes != null && familyNameSuffixes.length > maxNames)
 						maxNames = familyNameSuffixes.length;
 					if (degrees != null && degrees.length > maxNames)
 						maxNames = degrees.length;
 					if (prefixes != null && prefixes.length > maxNames)
 						maxNames = prefixes.length;
 					if (preferredNames != null && preferredNames.length > maxNames)
 						maxNames = preferredNames.length;
 					for (int j = 0; j < maxNames; j++) {
 						PersonName name = new PersonName();
 						if (preferredNames != null && preferredNames.length > j)
 							name.setPreferred(new Boolean(preferredNames[j]));
 						name.setGivenName(givenNames[j]);
 						name.setMiddleName(middleNames[j]);
 						name.setFamilyNamePrefix(familyNamePrefixes[j]);
 						name.setFamilyName(familyNames[j]);
 						name.setFamilyName2(familyName2s[j]);
 						name.setFamilyNameSuffix(familyNameSuffixes[j]);
 						name.setDegree(prefixes[j]);
 						name.setDegree(degrees[j]);
 						patient.addName(name);
                     }
 				}
 				
 				String[] address1s = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "address1_");
 				String[] address2s = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "address2_");
 				String[] cells = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "neighborhoodCell_");
 				String[] cities = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "cityVillage_");
 				String[] townships = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "townshipDivision_");
 				String[] counties = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "countyDistrict_");
 				String[] states = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "stateProvince_");
 				String[] regions = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "region_");
 				String[] subregions = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "subregion_");
 				String[] countries = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "country_");
 				String[] postalCodes = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "postalCode_");
 				String[] preferredAddress = ServletRequestUtils.getStringParameters(paramHttpServletRequest, "personAddress.preferred_");
 				
 				if (address1s != null || address1s != null ||
 						cells != null || cities != null ||
 						townships != null || counties != null ||
 						states != null || regions != null ||
 						subregions != null || countries != null ||
 						postalCodes != null || preferredAddress != null) {
 					int maxAddress = 0;
 					if(address1s != null && address1s.length > maxAddress)
 						maxAddress = address1s.length;
 					if(address1s != null && address1s.length > maxAddress)
 						maxAddress = address1s.length;
 					if(cells != null && cells.length > maxAddress)
 						maxAddress = cells.length;
 					if(cities != null && cities.length > maxAddress)
 						maxAddress = cities.length;
 					if(townships != null && townships.length > maxAddress)
 						maxAddress = townships.length;
 					if(counties != null && counties.length > maxAddress)
 						maxAddress = counties.length;
 					if(states != null && states.length > maxAddress)
 						maxAddress = states.length;
 					if(regions != null && regions.length > maxAddress)
 						maxAddress = regions.length;
 					if(subregions != null && subregions.length > maxAddress)
 						maxAddress = subregions.length;
 					if(countries != null && countries.length > maxAddress)
 						maxAddress = countries.length;
 					if(postalCodes != null && postalCodes.length > maxAddress)
 						maxAddress = postalCodes.length;
 					if(preferredAddress != null && preferredAddress.length > maxAddress)
 						maxAddress = preferredAddress.length;
 					
 					for (int j = 0; j < maxAddress; j++) {
 						PersonAddress pa = new PersonAddress();
 						pa.setAddress1(address1s[j]);
 						pa.setAddress2(address2s[j]);
 						pa.setNeighborhoodCell(cells[j]);
 						pa.setCityVillage(cities[j]);
 						pa.setTownshipDivision(townships[j]);
 						pa.setCountyDistrict(counties[j]);
 						pa.setStateProvince(states[j]);
 						pa.setRegion(regions[j]);
 						pa.setSubregion(subregions[j]);
 						pa.setCountry(countries[j]);
 						pa.setPostalCode(postalCodes[j]);
 						pa.setCountyDistrict(counties[j]);
 						if (preferredAddress != null && preferredAddress.length > j)
 							pa.setPreferred(new Boolean(preferredAddress[j]));
 						patient.addAddress(pa);
 					}
 				}
 				
 				break;
 		}
 		
 		return targetPage;
 	}
 	
 	/**
 	 * Allows for other Objects to be used as values in input tags. Normally, only strings and lists
 	 * are expected
 	 * 
 	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
 	 *      org.springframework.web.bind.ServletRequestDataBinder)
 	 */
 	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
 		super.initBinder(request, binder);
 		
 		NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
 		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, nf, true));
 		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(Context.getDateFormat(), true, 10));
 		binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
 		binder.registerCustomEditor(Location.class, new LocationEditor());
 		binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor());
 		binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
 	}
 
     protected ModelAndView processCancel(
             HttpServletRequest paramHttpServletRequest,
             HttpServletResponse paramHttpServletResponse, Object paramObject,
             BindException paramBindException) throws Exception {
         return new ModelAndView(new RedirectView(paramHttpServletRequest
                 .getContextPath()
                 + "/module/amrsregistration/start.form"));
     }
 
     protected ModelAndView processFinish(
             HttpServletRequest paramHttpServletRequest,
             HttpServletResponse paramHttpServletResponse, Object paramObject,
             BindException paramBindException) throws Exception {
         return new ModelAndView(new RedirectView(paramHttpServletRequest
                 .getContextPath()
                 + "/module/amrsregistration/start.form"));
     }
 
     private Patient getNewPatient() {
     	Patient patient = null;
     	
         HashSet<PersonName> localHashSet1 = new HashSet<PersonName>();
         localHashSet1.add(new PersonName());
         HashSet<PersonAddress> localHashSet2 = new HashSet<PersonAddress>();
         localHashSet2.add(new PersonAddress());
         HashSet<PersonAttribute> localHashSet3 = new HashSet<PersonAttribute>();
         PersonAttribute localPersonAttribute = new PersonAttribute();
         localPersonAttribute.setAttributeType(new PersonAttributeType());
         localHashSet3.add(localPersonAttribute);
         Person localPerson = new Person();
         localPerson.setNames(localHashSet1);
         localPerson.setAddresses(localHashSet2);
         localPerson.setAttributes(localHashSet3);
         patient = new Patient(localPerson);
         patient.getIdentifiers().add(new PatientIdentifier());
         return patient;
         
     }
     
     private void copyPatient(Patient to, Patient from) {
     	to.setPersonId(from.getPersonId());
 		
     	to.setAddresses(from.getAddresses());
     	to.setNames(from.getNames());
     	to.setAttributes(from.getAttributes());
 		
     	to.setGender(from.getGender());
     	to.setBirthdate(from.getBirthdate());
     	to.setBirthdateEstimated(from.getBirthdateEstimated());
     	to.setDead(from.isDead());
     	to.setDeathDate(from.getDeathDate());
     	to.setCauseOfDeath(from.getCauseOfDeath());
 		
     	to.setPersonCreator(from.getPersonCreator());
     	to.setPersonDateCreated(from.getPersonDateCreated());
     	to.setPersonChangedBy(from.getPersonChangedBy());
     	to.setPersonDateChanged(from.getPersonDateChanged());
     	to.setPersonVoided(from.isPersonVoided());
     	to.setPersonVoidedBy(from.getPersonVoidedBy());
     	to.setPersonDateVoided(from.getPersonDateVoided());
     	to.setPersonVoidReason(from.getPersonVoidReason());
 		to.setPatientId(from.getPatientId());
 		to.setIdentifiers(from.getIdentifiers());
     }
 }
