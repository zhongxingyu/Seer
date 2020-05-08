 package org.openmrs.module.patientregistration.service;
 
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.PersonName;
 import org.openmrs.module.emrapi.printer.UnableToPrintViaSocketException;
 import org.openmrs.module.paperrecord.UnableToPrintLabelException;
 import org.openmrs.module.patientregistration.UserActivity;
 import org.openmrs.module.patientregistration.util.DuplicatePatient;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The Interface PatientRegistrationService has the service methods for PatientRegistration module.
  */
 public interface PatientRegistrationService {
 	
 	/**
 	 * Registers a patient on the date specified
 	 * Currently, "registering" a patient means creating a patient registration encounter on the specified date
 	 * Will not create a registration encounter if there is already an encounter for that patient of the same type and location on the same day
 	 * TODO: In the 1.8 version of the module, we may want to create a Visit instead of an Encounter
 	 * 
 	 * @param patient the patient to register
 	 * @param registrationDate the date of registration
 	 */
 	public Encounter registerPatient(Patient patient, Person provider, EncounterType encounterType, Location location, Date registrationDate);
 	
 	/**
 	 *  Retrieves a list of observations for a given patient
 	 * @param patient
 	 * @param encounterType
 	 * @param questions
 	 * @param location
 	 * @param registrationDate
 	 * @return
 	 */
 	public List<Obs> getPatientObs(Patient patient, EncounterType encounterType, List<Encounter> encounters, List<Concept> questions, Location location, Date registrationDate);
 	
 	/**
 	 * Retrieves a list of distinct free text observations for a given Concept ID
 	 * @param conceptId an Integer indicating the Concept ID
 	 * @return a List<String>
 	 */
 	public List<String> getDistinctObs(Integer conceptId);
 	
 	public Set<Integer> getDistinctDuplicateObs(Integer conceptId);
 	
 	public List<DuplicatePatient> getDuplicatePatients(Patient patient);
 	
 	/**
 	 * Registers a patient on the current date
 	 * Currently, "registering" a patient means creating a patient registration encounter on the specified date
 	 * Will not create a registration encounter if there is already an encounter for that patient of the same type and location on the same day
 	 * In the 1.8 version of the module, we may want to create a Visit instead of an Encounter
 	 * 
 	 * @param patient the patient to register
 	 */
 	public Encounter registerPatient(Patient patient, Person provider, EncounterType encounterType, Location location);
 
 	/**
 	 * Searches for patients that match the given person name; Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param personName the name to search for
 	 */
 	public List<Patient> exactSearch(PersonName personName);
 	/**
 	 * Searches for patients that match the given person name; Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param personName the name to search for
 	 */
 	public List<Patient> search(PersonName personName);
 	
 	/**
 	 * Searches for patients that match the given first person name; Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param personName the name to search for
 	 */
 	public List<Patient> search(String personFirstName);
 	
 	/**
 	 * Searches for patients that match the given first person name; Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param personName the name to search for
 	 */
 	public Set<String> searchNames(String name, String nameField);
 	
 	/**
 	 * 
 	 * Removes from the list the patients the UNKNOWN(John Doe) patients 
 	 * 
 	 * @param patientList a List<Patient> representing the unfiltered list
 	 * @return a List<Patient> representing the filtered list
 	 */
 	public List<Patient> removeUnknownPatients(List<Patient> patientList);
 	
 	/**
 	 * Searches for PersonName that phonetically match the given first name and last name
 	 * @param firstName a String indicating the first name phonetic encoding
 	 * @param lastName a String indicating the last name phonetic encoding
 	 * @return a Set of Integer representing the PersonName IDs of the potential matches
 	 */
 	public List<Integer> getPhoneticsPersonId(String firstName, String lastName);
 	
 	/**
 	 * 
 	 * Searches for the ids of the unknown(John Doe) persons 
 	 * 
 	 * @return a List of Integer representing the PersonName IDs of the unknown persons
 	 */
 	public List<Integer> getUnknownPersonId();
 	
 	/**
 	 * Searches for patients that match the given person name; The results list is order by the occurrence of the name. 
 	 * Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param name the name to search for
 	 * @param nameField the table field against which the search is executed
 	 */
 	public Map<String, Integer> searchNamesByOccurence(String name, String nameField);
 	
 	public List<Patient> getPatientsByNameId(List<Integer> nameIds);
 	
 	/**
 	 * Searches for patients that match the given reference patient; Uses the search class referenced in patientregistration.search global property, 
 	 * otherwise uses the default patient search (DefaultPatientRegistrationSearch)
 	 * 
 	 * @param patient the reference patient to use as the basis of the search
 	 */
 	public List<Patient> search(Patient patient);
 	
 	/**
 	 * Prints a registration label for this patient to the default networked label printer
 	 * 
 	 * @param patient the patient we are printing a registration label for
 	 */
 	public void printRegistrationLabel(Patient patient, Location location)
             throws UnableToPrintLabelException;
 	
 	/**
 	 * Prints a one or more registration labels for this patient to the default networked label printer
 	 * 
 	 * @param patient the patient we are printing a registration label for
 	 * @param count the number of copies of the label to print
 	 */
 	public void  printRegistrationLabel(Patient patient, Location location, Integer count)
             throws  UnableToPrintLabelException;
 	
 	/**
 	 * Prints an ID card label for this patient to the default networked label printer
 	 */
 	public void printIDCardLabel(Patient patient, Location location)
         throws UnableToPrintLabelException;
 	
 	/**
 	 * Prints a ID card for this patient to the default networked id card printer
 	 * 
 	 * @param patient the patient we are printing the ID card for
 	 */
 	public void printIDCard(Patient patient, Location location)
        throws UnableToPrintViaSocketException, UnableToPrintLabelException;
 	
 	/**
 	 * @return the number of registration encounters for each passed encounter type during the passed date range
 	 */
 	public Map<EncounterType, Integer> getNumberOfRegistrationEncounters(List<EncounterType> encounterTypes, Location location, Date fromDate, Date toDate);
 
 	/**
 	 * @return the number of registration encounters for each day for the given encounterType and location
 	 */
 	public Map<Date, Integer> getNumberOfEncountersByDate(EncounterType encounterType, Location location);
 	
 	/**
 	 * @return the number of distinct patients ever registered with a given encounter type and location, by address field
 	 */
 	public Map<String, Integer> getNumberOfPatientsByAddress(Map<String, String> filterCriteria, String addressField, EncounterType encounterType, Location location);
 
 	/**
 	 * @return the number of distinct encounters ever registered with a given encounter type and location, by address field
 	 */
 	public Map<String, Integer> getNumberOfEncountersByAddress(Map<String, String> filterCriteria, String addressField, EncounterType encounterType, Location location);
 
 	/**
 	 * Saves an UserActivity to the database
 	 * @should save the passed UserActivity to the database
 	 */
 	public UserActivity saveUserActivity(UserActivity userActivity);
 }
