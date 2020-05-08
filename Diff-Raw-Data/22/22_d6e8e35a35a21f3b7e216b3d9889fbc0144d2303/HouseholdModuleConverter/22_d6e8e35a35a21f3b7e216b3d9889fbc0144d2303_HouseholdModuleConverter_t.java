 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.openmrs.module.amrsmobileforms;
 
import java.util.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Person;
 import org.openmrs.User;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.household.model.*;
 import org.openmrs.module.household.service.HouseholdService;
 
 /**
  * singleton providing conversion utilities for creating households with encounters and
  * adding members via the Household Module service from AMRS Mobile Form Entry data.
  */
 public class HouseholdModuleConverter {
 
 	private HouseholdService service;
 	private MobileFormEntryService mfeservice;
 	private static HouseholdModuleConverter instance = null;
 	private final Log log = LogFactory.getLog(this.getClass());
 	private Map<Integer, Concept> conceptCache;
 	private HouseholdDefinition householdDefinition;
 	private HouseholdEncounterType householdEncounterType;
 	
 	/**
 	 * provide a singleton instance of this class
 	 * 
 	 * @return 
 	 */
 	public static HouseholdModuleConverter getInstance() {
 		if (instance == null)
 			instance = new HouseholdModuleConverter();
 		return instance;
 	}
 
 	/**
 	 * cache the Household service
 	 * 
 	 * @return 
 	 */
 	private HouseholdService getService() {
 		if (service == null)
 			service = Context.getService(HouseholdService.class);
 		return service;
 	}
 	
 	/**
 	 * cache the Mobile Form Entry service
 	 * 
 	 * @return 
 	 */
 	private MobileFormEntryService getMFEService() {
 		if (mfeservice == null)
 			mfeservice = Context.getService(MobileFormEntryService.class);
 		return mfeservice;
 	}
 	
 	/**
 	 * add a household and encounter based on the household definition
 	 * 
 	 * @param household 
 	 */
 	public void addHouseholdAndEncounter(MobileFormHousehold household) {
 		
 		// look for existing MobileFormHousehold object
 		// TODO make this more intelligent? meh.
 		Household newHousehold = getService().getHouseholdGroupByIdentifier(household.getHouseholdIdentifier());
 		
 		// if it does not exist, create a new one and set a few properties
 		if (newHousehold == null) {
 			newHousehold = new Household();
 			newHousehold.setHouseholdIdentifier(household.getHouseholdIdentifier());
 			newHousehold.setCreator(household.getCreator());
 			newHousehold.setDateCreated(household.getDateCreated());
 		}
 		
 		// set changedBy information (if there is any)
 		newHousehold.setChangedBy(household.getChangedBy());
 		newHousehold.setDateChanged(household.getDateChanged());
 		
 		// set the household definition with the built-in default
 		// TODO warn if the definition is already set
 		newHousehold.setHouseholdDef(getDefaultHouseholdDefinition());
 		
 		// save the household
 		newHousehold = getService().saveHouseholdGroup(newHousehold);
 		
 		// add the index patient as a household member
 		if (household.getHousehead() != null) {
 			addMembership(household.getHousehead(), household.getHouseholdIdentifier(), true);
 		}
 		
 		// create a new household encounter
 		HouseholdEncounter encounter = new HouseholdEncounter();
 		encounter.setHouseholdGroupId(newHousehold);
 		encounter.setCreator(household.getCreator());
 		encounter.setDateCreated(household.getDateCreated());
 		
 		// get the latest survey
 		Collection<Survey> surveys = household.getSurveys();
 		Survey survey = null;
 		for (Survey s: surveys) {
 			if (s != null && (survey == null || s.getDateCreated().after(survey.getDateCreated())))
 				survey = s;
 		}
 		
 		if (survey != null) {
 			// set the provider, if possible
 			User provider = Context.getUserService().getUserByUsername(survey.getProviderId());
 			if (provider != null)
 				encounter.setProvider(provider.getPerson());
 			else
 				log.warn("nooooooooo ... provider is nulllllllll.");
 			
 			// also use survey for creator and date created
 			encounter.setCreator(survey.getCreator());
 			encounter.setDateCreated(survey.getDateCreated());
 			
 			// use the survey's "end time" as the encounter datetime
 			encounter.setHouseholdEncounterDatetime(survey.getEndTime());
 			
 		} else {
 			// fill in some defaults since no survey exists (weird)
 			log.warn("could not find a survey, using defaults for household encounter provider and datetime.");
 			encounter.setProvider(Context.getAuthenticatedUser().getPerson());
 			encounter.setHouseholdEncounterDatetime(new Date());
 		}
 		
 		// use the built-in default household encounter type
 		encounter.setHouseholdEncounterType(getDefaultHouseholdEncounterType());
 
 		// set the encounter location
 		encounter.setHouseholdLocation(buildHouseholdLocation(household));
 		
 		// create household observations for ...
 		
 		// -- number of adults
 		addNumericObservation(encounter, 
 				getCachedConcept(getGP(MobileFormEntryConstants.GP_CONCEPT_ADULTS)), 
 				household.getAdults());
 
 		// -- number of children
 		addNumericObservation(encounter, 
 				getCachedConcept(getGP(MobileFormEntryConstants.GP_CONCEPT_CHILDREN)), 
 				household.getChildren());
 
 		// -- number of eligible adults
 		addNumericObservation(encounter, 
 				getCachedConcept(getGP(MobileFormEntryConstants.GP_CONCEPT_ELIGIBLE_ADULTS)), 
 				household.getAdultsEligible());
 
 		// -- number of eligible children
 		addNumericObservation(encounter, 
 				getCachedConcept(getGP(MobileFormEntryConstants.GP_CONCEPT_ELIGIBLE_CHILDREN)), 
 				household.getChildrenEligible());
 
 		// -- survey's return visit date
 		if (survey != null) {
 			Date returnVisitDate= survey.getReturnDate();
 			if (returnVisitDate != null)
 				addDateObservation(encounter, 
 						getCachedConcept(getGP(MobileFormEntryConstants.GP_CONCEPT_RETURN_VISIT_DATE)), 
 						returnVisitDate);
 		}
 		
 		// -- all economics
 		for (Economic economic: household.getEconomics())
 			addEconomicObservation(encounter, economic);
 		
 		// save the household encounter
 		getService().saveHouseholdEncounter(encounter);
 	}
 
 	/**
 	 * creates a membership of the given patient to the household
 	 * 
 	 * @param patient
 	 * @param household 
 	 */
 	public void addMembership(Person person, String householdIdentifier, Boolean headship) {
 		MobileFormHousehold household = getMFEService().getHousehold(householdIdentifier);
 		Household hh = getService().getHouseholdGroupByIdentifier(householdIdentifier);
 		if (hh == null)
 			return;
 		
 		// check for existing relationship of this person to this household
 		List<HouseholdMembership> existing = getService().getHouseholdMembershipByGrpByPsn(person, hh);
 		if (existing != null && !existing.isEmpty()) {
 			// some membership is already in place for this person and this household
 			for (HouseholdMembership m: existing) {
 				if (m.getEndDate() == null) {
 					// there is a current relationship
 					if (m.isHouseholdMembershipHeadship() == headship) {
 						// headship is the same, so count it as already existing
 						log.info("ignoring household membership due to existing identical membership.");
 						return;
 					} else {
 						// need to discontinue this relationship so the new headship is established properly
						// TODO figure out best options for EndDate and any other data
 						m.setEndDate(new Date());
 						getService().saveHouseholdMembership(m);
 					}
 				}
 			}
 		}
 		
 		// create a new household membership
 		// TODO review startDate, creator and dateCreated
 		HouseholdMembership membership = new HouseholdMembership();
 		membership.setHouseholdMembershipGroups(hh);
 		membership.setHouseholdMembershipHeadship(headship);
 		membership.setHouseholdMembershipMember(person);
 		membership.setStartDate(household.getDateCreated());
 		membership.setCreator(household.getCreator());
 		membership.setDateCreated(household.getDateCreated());
 		getService().saveHouseholdMembership(membership);
 	}
 
 	/**
 	 * finds the household definition selected to be used for all Households
 	 * generated from this module.
 	 * 
 	 * TODO add a GP and settings page for default household definition
 	 * 
 	 * @return 
 	 */
 	private HouseholdDefinition getDefaultHouseholdDefinition() {
 		if (householdDefinition == null)
 			householdDefinition = service.getHouseholdDefinition(getGP(MobileFormEntryConstants.GP_DEFAULT_HOUSEHOLD_DEFINITION));
 		return householdDefinition;
 	}
 
 	/**
 	 * finds the household encounter type selected to be used for all household
 	 * encounters generated by this module.
 	 * 
 	 * TODO add a GP and settings page for default household encounter type
 	 * 
 	 * @return 
 	 */
 	private HouseholdEncounterType getDefaultHouseholdEncounterType() {
 		if (householdEncounterType == null)
 			householdEncounterType = service.getHouseholdEncounterType(getGP(MobileFormEntryConstants.GP_DEFAULT_HOUSEHOLD_ENCOUNTER_TYPE));
 		return householdEncounterType;
 	}
 
 	/**
 	 * adds a household observation with the specified concept and the value.
 	 * 
 	 * @param encounter
 	 * @param concept
 	 * @param value 
 	 */
 	private void addNumericObservation(HouseholdEncounter encounter, Concept concept, Number value) {
 		if (value == null)
 			return;
 		
 		HouseholdObs obs = new HouseholdObs();
 		obs.setConcept(concept);
 		obs.setValueNumeric(value.doubleValue());
 		encounter.addHouseholdObs(obs);
 	}
 
 	/**
 	 * adds a date-valued household observation to a household encounter.
 	 * 
 	 * @param encounter
 	 * @param concept
 	 * @param value 
 	 */
 	private void addDateObservation(HouseholdEncounter encounter, Concept concept, Date value) {
 		if (value == null)
 			return;
 
 		HouseholdObs obs = new HouseholdObs();
 		obs.setConcept(concept);
 		obs.setValueDatetime(value);
 		encounter.addHouseholdObs(obs);
 	}
 
 	/**
 	 * add an economic-based observation to a household encounter.
 	 * 
 	 * @param encounter
 	 * @param economic 
 	 */
 	private void addEconomicObservation(HouseholdEncounter encounter, Economic economic) {
 		if (economic == null)
 			return;
 		
 		// get concept for economic
 		EconomicConceptMap ecm = getMFEService().getEconomicConceptMapFor(economic.getValueQuestion());
 		if (ecm == null)
 			throw new APIException("No mapping for economic object " + economic.getValueQuestion());
 
 		Concept concept = ecm.getConcept();
 		if (concept == null)
 			throw new APIException("No concept for economic concept map " + ecm);
 		
 		// create the new HouseholdObs
 		HouseholdObs obs = new HouseholdObs();
 		obs.setConcept(concept);
 
 		// assume that economics will have values in the right spots and copy them over
 		obs.setValueText(economic.getValueText());
 		obs.setValueNumeric(economic.getValueNumeric());
 				
 		encounter.addHouseholdObs(obs);
 	}
 
 	/**
 	 * create a new HouseholdLocation from the household's location values; 
 	 * mapping of fields is based on suggestions from Jonah Mwogi.
 	 * 
 	 * @param household
 	 * @return 
 	 */
 	private HouseholdLocation buildHouseholdLocation(org.openmrs.module.amrsmobileforms.MobileFormHousehold household) {
 		HouseholdLocation loc = new HouseholdLocation();
 
 		loc.setCityVillage(household.getVillage());
 		loc.setCityLocation(household.getLocation());
 		loc.setCitySubLocation(household.getSublocation());
 		loc.setAddress5(household.getDistrict());
 		loc.setAddress6(household.getDivision());
 		
 		if (!StringUtils.isBlank(household.getGpsLocation())) {
 			String[] splitGps = household.getGpsLocation().split(" ");
 			if (splitGps.length == 2) {
 				loc.setLatitude(splitGps[0]);
 				loc.setLongitude(splitGps[1]);
 			} else
 				log.warn("could not set GPS coordinates of " 
 						+ household.getGpsLocation() + " for household " 
 						+ household.getHouseholdIdentifier());
 		}
 		
 		return loc;
 	}
 
 	/**
 	 * simple cache for concepts
 	 * 
 	 * @param conceptId
 	 * @return 
 	 */
 	private Concept getCachedConcept(Integer conceptId) {
 		if (conceptCache == null)
 			conceptCache = new HashMap<Integer, Concept>();
 		
 		// TODO put some error catching and logging around this
 		if (!conceptCache.containsKey(conceptId))
 			conceptCache.put(conceptId, Context.getConceptService().getConcept(conceptId));
 		
 		return conceptCache.get(conceptId);
 	}
 
 	/**
 	 * get a concept id from a global property.
 	 * 
 	 * @param globalProperty
 	 * @return 
 	 */
 	private Integer getGP(String globalProperty) {
 		String value = Context.getAdministrationService().getGlobalProperty(globalProperty);
 
 		Integer conceptId = null;
 		try {
 			conceptId = Integer.parseInt(value);
 		} catch (NumberFormatException ex) {
 			throw new APIException("could not parse concept id " + value + " for global property " + globalProperty, ex);
 		}
 		
 		return conceptId;
 	}
 
 }
