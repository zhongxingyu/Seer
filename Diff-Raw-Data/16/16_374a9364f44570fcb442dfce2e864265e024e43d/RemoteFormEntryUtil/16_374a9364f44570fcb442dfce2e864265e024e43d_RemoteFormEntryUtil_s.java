 package org.openmrs.module.remoteformentry;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
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
 import org.openmrs.Relationship;
 import org.openmrs.RelationshipType;
 import org.openmrs.User;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.UserService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.remoteformentry.RemoteFormEntryConstants.RP_SERVER_TYPES;
 import org.openmrs.util.OpenmrsUtil;
 import org.openmrs.validator.PatientIdentifierValidator;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Remote form entry utility methods
  */
 public class RemoteFormEntryUtil {
 	
 	private static Log log = LogFactory.getLog("org.openmrs.module.RemoteFormEntryUtil");
 	
 	/**
 	 * Cached directory where pending queue items are stored
 	 * 
 	 * @see #getPendingQueueDir()
 	 */
 	private static File pendingDir = null;
 	
 	/**
 	 * Cached directory where acks for import pending queue items are stored
 	 * 
 	 * @see #getAckDir()
 	 */
 	private static File ackDir = null;
 	
 	/**
 	 * Cached directory where the generated data for returning to the remote site is stored
 	 * 
 	 * @see #getGeneratedReturnDataFile()
 	 */
 	private static File generatedReturnDataFile = null;
 	
 	/**
 	 * Cached directory for what type of server this is (central vs remote)
 	 * 
 	 * @see #getServerType()
 	 */
 	private static RemoteFormEntryConstants.RP_SERVER_TYPES serverType = null;
 	
 	/**
 	 * Name of the sql file in the zip file going back to the remote site. This should not end with
 	 * .sql
 	 */
 	public static final String GENERATED_DATA_FILENAME = "generatedReturnData";
 	
 	/**
 	 * Gets the directory where the user specified their queues were being stored
 	 * 
 	 * @return directory in which to store queued items
 	 */
 	public static File getPendingQueueDir() {
 		log.debug("Before checking pending dir");
 		
 		if (pendingDir == null) {
 			AdministrationService as = Context.getAdministrationService();
 			String folder = as.getGlobalProperty(RemoteFormEntryConstants.GP_PENDING_QUEUE_DIR,
 			    RemoteFormEntryConstants.GP_PENDING_QUEUE_DIR_DEFAULT);
 			
 			pendingDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
 			
 			if (log.isDebugEnabled())
 				log
 				        .debug("Loaded formentry pending queue directory from global properties: "
 				                + pendingDir.getAbsolutePath());
 		}
 		
 		return pendingDir;
 	}
 	
 	/**
 	 * Determine if this server should act a central server It is possible that both this and
 	 * isRemoteServer could return true. It all depends on the
 	 * RemoteFormEntryConstants.RP_SERVER_TYPE_DEFAULT value
 	 * 
 	 * @return boolean true/false whether this server represents a central server.
 	 * @see #isRemoteServer()
 	 */
 	public static boolean isCentralServer() {
 		if (serverType == null)
 			setServerType();
 		
 		if (serverType == null || serverType.equals(RP_SERVER_TYPES.central))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Determine if this server should act like a remote server It is possible that both this and
 	 * isCentralServer could return true. It all depends on the
 	 * RemoteFormEntryConstants.RP_SERVER_TYPE_DEFAULT value
 	 * 
 	 * @return boolean true/false whether this server represents a remote server.
 	 * @see #isCentralServer()
 	 */
 	public static boolean isRemoteServer() {
 		if (serverType == null)
 			setServerType();
 		
 		if (serverType == null || serverType.equals(RP_SERVER_TYPES.remote))
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Determine the serverType of this server from the runtime properties
 	 */
 	private static void setServerType() {
 		Properties runtimeProps = Context.getRuntimeProperties();
 		String type = (String) runtimeProps.get(RemoteFormEntryConstants.RP_SERVER_TYPE);
 		
 		// set to default value if no runtime property is set
 		if (type == null)
 			type = RemoteFormEntryConstants.RP_SERVER_TYPE_DEFAULT;
 		
 		if (type != null)
 			serverType = RemoteFormEntryConstants.RP_SERVER_TYPES.valueOf(type);
 		
 	}
 	
 	/**
 	 * Joins the given string array into a string with items separated with the given token
 	 * 
 	 * @param strings
 	 * @param sep
 	 * @return string representation of the given array
 	 */
 	public static String join(Object[] strings, String sep) {
 		StringBuffer sb = new StringBuffer();
 		for (int x = 0; x < strings.length - 1; x++) {
 			sb.append(strings[x]);
 			sb.append(sep);
 		}
 		sb.append(strings[strings.length - 1]);
 		
 		return (sb.toString());
 	}
 	
 	/**
 	 * Return the directory that contains the ack files. An ack file contains just a list of
 	 * filenames of queue items received
 	 * 
 	 * @param location Location for which to get the ack files
 	 * @return File directory that contains other directories/files that are the ack files
 	 */
 	public static File getAckDir(Location location) {
 		
 		if (ackDir == null) {
 			AdministrationService as = Context.getAdministrationService();
 			String folder = as.getGlobalProperty(RemoteFormEntryConstants.GP_ACK_DIR,
 			    RemoteFormEntryConstants.GP_ACK_DIR_DEFAULT);
 			ackDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
 			if (log.isDebugEnabled())
 				log.debug("Loaded ack directory from global properties: " + ackDir.getAbsolutePath());
 		}
 		
 		String locationId = "0";
 		
 		if (location != null)
 			locationId = location.getLocationId().toString();
 		
 		File file = new File(ackDir, locationId);
 		if (!file.exists())
 			file.mkdir();
 		
 		return file;
 	}
 	
 	/**
 	 * Return the file name and location for the generated file
 	 * 
 	 * @return File with location of the generated data to return to the remote location
 	 */
 	public static File getGeneratedReturnDataFile() {
 		
 		if (generatedReturnDataFile == null) {
 			AdministrationService as = Context.getAdministrationService();
 			String folder = as.getGlobalProperty(RemoteFormEntryConstants.GP_GENERATED_DATA_DIR,
 			    RemoteFormEntryConstants.GP_GENERATED_DATA_DIR_DEFAULT);
 			File generatedReturnDataDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
 			
 			generatedReturnDataFile = new File(generatedReturnDataDir, GENERATED_DATA_FILENAME);
 			
 			if (log.isDebugEnabled())
 				log.debug("Loaded generatedReturnDataDir directory from global properties: "
 				        + generatedReturnDataFile.getAbsolutePath());
 		}
 		
 		return generatedReturnDataFile;
 	}
 	
 	/**
 	 * Return the folder name (path) for the generated file for the given location
 	 * 
 	 * @return File with location of the generated data to return to the remote location for
 	 *         location specific data
 	 */
 	public static File getGeneratedReturnDataFolderForLocation(Location location) {
 		
 		File generatedReturnDataDir = getGeneratedReturnDataDir();
 		
 		return new File(generatedReturnDataDir, "generatedDataFor" + "-" + location.getLocationId());
 	}
 	
 	/**
 	 * Get the directory that generated return data will be stored
 	 * 
 	 * @return File directory of the generated data to return to the remote location
 	 */
 	private static File getGeneratedReturnDataDir() {
 		
 		AdministrationService as = Context.getAdministrationService();
 		String folder = as.getGlobalProperty(RemoteFormEntryConstants.GP_GENERATED_DATA_DIR,
 		    RemoteFormEntryConstants.GP_GENERATED_DATA_DIR_DEFAULT);
 		File generatedReturnDataDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
 		
 		return generatedReturnDataDir;
 	}
 	
 	/**
 	 * Generate the string for a download specifying the location and the current timestamp
 	 * 
 	 * @param location Location this file will be for
 	 * @return string to put in the filename of the download to recognize it
 	 */
 	public static String getDownloadSuffix(Location location) {
 		String name = location.getName();
 		name = name.replace(" ", "_");
 		if (name.length() > 30)
 			name = name.substring(0, 30) + "...";
 		
 		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 		String ret = location.getLocationId() + "-" + name + "-" + dateFormat.format(new Date());
 		
 		return ret;
 	}
 	
 	/**
 	 * Add the given patientId to the given document under /patient/patientId. This is used to
 	 * update the identifier for cases where the remote site has created a patient or has a wrong
 	 * patientId for the patient
 	 * 
 	 * @param patientId integer patient id to add to the document
 	 * @param formData String data from the form
 	 * @throws XPathExpressionException
 	 */
 	public static String replacePatientIdInDocument(Integer patientId, String formData) {
 		int element = formData.indexOf("<patient.patient_id");
 		int left = formData.indexOf(">", element);
 		int right = formData.indexOf("</patient.patient_id", left);
 		
 		StringBuilder builder = new StringBuilder();
 		builder.append(formData.substring(0, left + 1));
 		builder.append(patientId);
 		builder.append(formData.substring(right));
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * Get the patient identifiers from the given form data
 	 * 
 	 * @param doc document that is the form data
 	 * @param xp xpath transform
 	 * @return new list of PatientIdentifiers
 	 * @throws XPathExpressionException
 	 */
 	public static List<PatientIdentifier> getPatientIdentifiers(Document doc, XPath xp, User enterer)
 	                                                                                             throws XPathExpressionException, Exception {
 		
 		List<PatientIdentifier> patientIdentifiers = new ArrayList<PatientIdentifier>();
 		
 		// get the list of patient identifier nodes
 		NodeList nodeList = (NodeList) xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PATIENT_IDENTIFIER, doc, XPathConstants.NODESET);
 		
 		// loop over the nodes
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node currentNode = nodeList.item(i);
 			
 			PatientIdentifier pi = new PatientIdentifier();
 			
 			pi.setIdentifier(xp.evaluate(RemoteFormEntryConstants.PATIENT_IDENTIFIER_IDENTIFIER, currentNode));
 			
 			try {
 				String typeId = xp.evaluate(RemoteFormEntryConstants.PATIENT_IDENTIFIER_TYPE, currentNode);
 				if (typeId != null) {
 					PatientIdentifierType pit = Context.getPatientService()
 					        .getPatientIdentifierType(Integer.valueOf(typeId));
 					pi.setIdentifierType(pit);
 				}
 			}
 			catch (Exception e) {
 				log.debug("Unable to get identifier type", e);
 			}
 			
 			try {
 				String locationId = xp.evaluate(RemoteFormEntryConstants.PATIENT_IDENTIFIER_LOCATION, currentNode);
 				if (locationId != null)
 					pi.setLocation(new Location(Integer.valueOf(locationId)));
 			}
 			catch (Exception e) {
 				log.debug("Unable to get location id", e);
 			}
 			
 			// get the voided status
 			String voidStatus = xp.evaluate(RemoteFormEntryConstants.PATIENT_IDENTIFIER_VOIDED, currentNode);
 			if (!pi.isVoided() && voidStatus != null && voidStatus.equals("true")) {
 				pi.setVoided(true);
 				pi.setVoidReason("Voided at remote");
 				pi.setVoidedBy(enterer);
 				pi.setDateVoided(new Date());
 			}
 			
 			// get the preferred status
 			String preferredStatus = xp.evaluate(RemoteFormEntryConstants.PATIENT_IDENTIFIER_PREFERRED, currentNode);
 			pi.setPreferred(preferredStatus != null && preferredStatus.equals("true"));
 			
 			pi.setCreator(enterer);
 			pi.setDateCreated(new Date());
 			
 			// if this identifier type has a validator and it is invalid
 			boolean validCheckDigit;
 			if (!pi.getIdentifierType().hasValidator()) {
 				validCheckDigit = true; // there is no check digit
 			}
 			else {
 				try {
					PatientIdentifierValidator.validateIdentifier(pi);
 					validCheckDigit = true;
 				}
 				catch (Exception e) {
 					validCheckDigit = false;
					log.debug("Error while validating the checkdigit", e);
 				}
 			}
 			
 			if (!validCheckDigit) {
 				// see if the admin has given an identifierType to convert this to
 				String invalidTypeId = Context.getAdministrationService().getGlobalProperty(RemoteFormEntryConstants.GP_INVALID_IDENTIFIER_TYPE, "");
 				
 				// if the admin defined the global property, reset the type to the INVALID type 
 				if (!invalidTypeId.equals("")) {
 					PatientIdentifierType pit = Context.getPatientService()
 					        .getPatientIdentifierType(Integer.valueOf(invalidTypeId));
 					pi.setIdentifierType(pit);
 				}
 				else {
 					log
 				        .warn("Invalid identifier found: "
 				                + pi.getIdentifier()
 				                + ".  Saving of this patient will fail. You can set the "
 				                + RemoteFormEntryConstants.GP_INVALID_IDENTIFIER_TYPE
 				                + " global property so that patients can at least be processed"
 				                + " (but end up with the assigned invalid identifier type");
 				}
 			}
 			
 			patientIdentifiers.add(pi);
 		}
 		
 		return patientIdentifiers;
 	}
 	
 	/**
 	 * Get a list of person names object out of the given document
 	 * 
 	 * @param doc document that is the form data
 	 * @param xp xpath transform
 	 * @return new list of PersonName s
 	 * @throws XPathExpressionException
 	 */
 	public static List<PersonName> getPersonNames(Document doc, XPath xp, User enterer) throws XPathExpressionException {
 		
 		List<PersonName> personNames = new Vector<PersonName>();
 		
 		NodeList nodeList = (NodeList) xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_NAME, doc, XPathConstants.NODESET);
 		
 		// loop over the nodes
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node currentNode = nodeList.item(i);
 			
 			PersonName personName = new PersonName();
 			
 			personName.setGivenName(xp.evaluate(RemoteFormEntryConstants.PERSON_NAME_GIVEN, currentNode));
 			personName.setMiddleName(xp.evaluate(RemoteFormEntryConstants.PERSON_NAME_MIDDLE, currentNode));
 			personName.setFamilyName(xp.evaluate(RemoteFormEntryConstants.PERSON_NAME_FAMILY, currentNode));
 			
 			// get the voided status
 			String voidStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_NAME_VOIDED, currentNode);
 			if (!personName.isVoided() && voidStatus != null && voidStatus.equals("true")) {
 				personName.setVoided(true);
 				personName.setVoidedBy(enterer);
 				personName.setDateVoided(new Date());
 				personName.setVoidReason("Voided at remote site");
 			}
 			
 			// get the preferred status
 			String preferredStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_NAME_PREFERRED, currentNode);
 			personName.setPreferred(preferredStatus != null && preferredStatus.equals("true"));
 			
 			personName.setCreator(enterer);
 			personName.setDateCreated(new Date());
 			
 			personNames.add(personName);
 		}
 		
 		return personNames;
 	}
 	
 	/**
 	 * Create a list of PersonAddresses object from the given form data
 	 * 
 	 * @param doc document that represents the form data
 	 * @param xp xpath translator
 	 * @param enterer user who entered this form
 	 * @return new list of PersonAddress's
 	 * @throws XPathExpressionException
 	 */
 	public static List<PersonAddress> getPersonAddresses(Document doc, XPath xp, User enterer)
 	                                                                                          throws XPathExpressionException {
 		
 		List<PersonAddress> personAddresses = new Vector<PersonAddress>();
 		
 		NodeList nodeList = (NodeList) xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_ADDRESS, doc, XPathConstants.NODESET);
 		
 		// loop over the nodes
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node currentNode = nodeList.item(i);
 			
 			PersonAddress personAddress = new PersonAddress();
 			personAddress.setAddress1(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_ADDRESS1, currentNode));
 			personAddress.setAddress2(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_ADDRESS2, currentNode));
 			personAddress.setCityVillage(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_CITY_VILLAGE, currentNode));
 			personAddress.setCountry(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_COUNTRY, currentNode));
 			personAddress.setCountyDistrict(xp
 			        .evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_COUNTY_DISTRICT, currentNode));
 			personAddress.setLatitude(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_LATITUDE, currentNode));
 			personAddress.setLongitude(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_LONGITUDE, currentNode));
 			personAddress.setNeighborhoodCell(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_NEIGHBORHOOD_CELL,
 			    currentNode));
 			personAddress.setPostalCode(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_POSTAL_CODE, currentNode));
 			personAddress.setStateProvince(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_STATE_PROVINCE, currentNode));
 			personAddress.setRegion(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_REGION, currentNode));
 			personAddress.setSubregion(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_SUBREGION, currentNode));
 			personAddress.setTownshipDivision(xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_TOWNSHIP_DIVISION,
 			    currentNode));
 			
 			// get the voided status
 			String voidStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_VOIDED, currentNode);
 			if (!personAddress.isVoided() && voidStatus != null && voidStatus.equals("true")) {
 				personAddress.setVoided(true);
 				personAddress.setVoidReason("Voided at remote");
 				personAddress.setVoidedBy(enterer);
 				personAddress.setDateVoided(new Date());
 			}
 			
 			// get the preferred status
 			String preferredStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_ADDRESS_PREFERRED, currentNode);
 			personAddress.setPreferred(preferredStatus != null && preferredStatus.equals("true"));
 			
 			personAddress.setCreator(enterer);
 			personAddress.setDateCreated(new Date());
 			
 			personAddresses.add(personAddress);
 		}
 		
 		return personAddresses;
 	}
 	
 	/**
 	 * Get all of the person attributes from the given document and return them
 	 * 
 	 * @param doc form that is this document
 	 * @param xp xpath translator
 	 * @param enterer user that entered this
 	 * @return list of person attributes
 	 * @throws XPathExpressionException
 	 * @throws NumberFormatException
 	 */
 	public static List<PersonAttribute> getPersonAttributes(Document doc, XPath xp, User enterer)
 	                                                                                             throws NumberFormatException,
 	                                                                                             XPathExpressionException {
 		
 		// list to return
 		List<PersonAttribute> personAttributes = new ArrayList<PersonAttribute>();
 		
 		// get all person attribute nodes
 		NodeList nodeList = (NodeList) xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_ATTRIBUTE, doc, XPathConstants.NODESET);
 		
 		// loop over the nodes
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node currentNode = nodeList.item(i);
 			
 			PersonAttribute personAttribute = new PersonAttribute();
 			
 			// get the type id
 			String typeId = xp.evaluate(RemoteFormEntryConstants.PERSON_ATTRIBUTE_TYPE, currentNode);
 			if (typeId.length() > 0) {
 				PersonAttributeType pat = Context.getPersonService().getPersonAttributeType(Integer.valueOf(typeId));
 				personAttribute.setAttributeType(pat);
 			} else
 				log.error("Uh oh. Going to have trouble creating an attribute with no attribute type id. node: "
 				        + currentNode.getTextContent());
 			
 			// get the value
 			personAttribute.setValue(xp.evaluate(RemoteFormEntryConstants.PERSON_ATTRIBUTE_VALUE, currentNode));
 			
 			// get the voided status
 			String voidStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_ATTRIBUTE_VOIDED, currentNode);
 			if (!personAttribute.isVoided() && voidStatus != null && voidStatus.equals("true")) {
 				personAttribute.setVoided(true);
 				personAttribute.setVoidReason("Voided at remote");
 				personAttribute.setVoidedBy(enterer);
 				personAttribute.setDateVoided(new Date());
 			}
 			
 			personAttribute.setCreator(enterer);
 			personAttribute.setDateCreated(new Date());
 			
 			personAttributes.add(personAttribute);
 		}
 		
 		return personAttributes;
 	}
 	
 	/**
 	 * Add demographics/properties (like tribe, etc) to the given patient object
 	 * 
 	 * @param patient Patient object to modify
 	 * @param doc Document representation of the form
 	 * @param xp xpath translator
 	 * @param enterer User that entered the form
 	 * @throws XPathExpressionException
 	 */
 	public static void setPatientProperties(Patient patient, Document doc, XPath xp, User enterer)
 	                                                                                              throws XPathExpressionException {
 		
 		if (patient.getCreator() == null) {
 			patient.setCreator(enterer);
 			patient.setDateCreated(new Date());
 		}
 	}
 	
 	/**
 	 * Add demographics/properties (like birthdate, death status, etc) to the given person object
 	 * 
 	 * @param person Person object to modify
 	 * @param doc Document representation of the form
 	 * @param xp xpath translator
 	 * @param enterer User that entered the form
 	 * @throws XPathExpressionException
 	 */
 	public static void setPersonProperties(Person person, Document doc, XPath xp, User enterer)
 	                                                                                           throws XPathExpressionException {
 		DateFormat hl7DateFormat = new SimpleDateFormat("yyyyMMdd");
 		
 		String birthdateString = xp.evaluate(
 		    RemoteFormEntryConstants.nodePrefix + RemoteFormEntryConstants.PERSON_BIRTHDATE, doc);
 		try {
 			person.setBirthdate(hl7DateFormat.parse(birthdateString));
 		}
 		catch (ParseException e) {
 			log.error("Error getting birthdate from string for person: " + person, e);
 		}
 		
 		String birthdateEstimated = xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_BIRTHDATE_ESTIMATED, doc);
 		person.setBirthdateEstimated(birthdateEstimated.length() > 0);
 		
 		String deathStatus = xp.evaluate(RemoteFormEntryConstants.nodePrefix + RemoteFormEntryConstants.PERSON_DEAD, doc);
 		person.setDead(deathStatus != null && deathStatus.equals("true"));
 		
 		String deathDate = xp
 		        .evaluate(RemoteFormEntryConstants.nodePrefix + RemoteFormEntryConstants.PERSON_DEATH_DATE, doc);
 		try {
 			if (deathDate != null && deathDate.length() > 0)
 				person.setDeathDate(hl7DateFormat.parse(deathDate));
 		}
 		catch (ParseException e) {
 			log.error("Error getting death date from string for person: " + person, e);
 		}
 		
 		String deathCause = xp.evaluate("substring-before(" + RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_DEATH_REASON + ", '^')", doc);
 		if (deathCause != null && deathCause.length() > 0)
 			person.setCauseOfDeath(new Concept(Integer.valueOf(deathCause)));
 		
 		String gender = xp.evaluate(RemoteFormEntryConstants.nodePrefix + RemoteFormEntryConstants.PERSON_GENDER, doc);
 		setGender(person, gender);
 		
 		if (person.getPersonCreator() == null) {
 			person.setPersonCreator(enterer);
 			person.setPersonDateCreated(new Date());
 		}
 		
 	}
 	
 	/**
 	 * Convenience method to determine the gender from a string
 	 * 
 	 * @param person
 	 * @param gender
 	 */
 	private static void setGender(Person person, String gender) {
 		if ("F".equals(gender))
 			person.setGender("F");
 		else if ("M".equals(gender))
 			person.setGender("M");
 		else {
 			person.setGender("M");
 			log.error("Unable to determine gender for person_id " + person.getPersonId());
 		}
     }
 
 	/**
 	 * Create the relationship mappings for this patient
 	 * 
 	 * @param createdPatient patient (and patient_id) to map the relationships to
 	 * @param doc Document that represents the form
 	 * @param xp xpath translator
 	 * @param enterer User that entered this form
 	 */
 	public static List<Relationship> getRelationships(Patient createdPatient, Document doc, XPath xp, User enterer)
 	                                                                                                    throws XPathExpressionException {
 		
 		DateFormat hl7DateFormat = new SimpleDateFormat("yyyyMMdd");
 		
 		// discover and loop over the patient's relationships
 		
 		// list to return
 		List<Relationship> relationships = new ArrayList<Relationship>();
 		
 		// get all person attribute nodes
 		NodeList nodeList = (NodeList) xp.evaluate(RemoteFormEntryConstants.nodePrefix
 		        + RemoteFormEntryConstants.PERSON_RELATIONSHIP, doc, XPathConstants.NODESET);
 		
 		// loop over the nodes
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node currentNode = nodeList.item(i);
 			
 			Relationship relationship = new Relationship();
 			
 			// get the type id
 			String typeId = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_TYPE, currentNode);
 			if (typeId.length() > 0) {
 				RelationshipType pat = Context.getPersonService().getRelationshipType(Integer.valueOf(typeId));
 				relationship.setRelationshipType(pat);
 			} else
 				log.error("Uh oh. Going to have trouble creating a relationships with no relationship type id. node: "
 				        + currentNode.getTextContent());
 			
 			// get the other person from our db
 			String otherPersonsUuid = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_UUID, currentNode);
 			Patient patient = Context.getPatientService().getPatientByUuid(otherPersonsUuid);
 			if (patient == null) {
 				// the person was created on the remote server, create the person stub from the information 
 				// passed to us
 				patient = new Patient();
 				
 				// set the identifier (if is one) on the new person/patient
 				String identifierStr = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_IDENTIFIER, currentNode);
 				String identifierTypeId = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_IDENTIFIER_TYPE, currentNode);
 				String locationId = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_IDENTIFIER_LOC, currentNode);
 				if (identifierStr != null) {
 					PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierType(Integer.valueOf(identifierTypeId));
 					Location loc = Context.getLocationService().getLocation(locationId);
 					PatientIdentifier identifier = new PatientIdentifier(identifierStr, pit, loc);
 					patient.addIdentifier(identifier);
 				}
 				
 				// get the birthdate
 				String birthdateString = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_PERSON_BIRTHDATE, currentNode);
 				try {
 					patient.setBirthdate(hl7DateFormat.parse(birthdateString));
 				}
 				catch (ParseException e) {
 					log.error("Error getting birthdate from string for relationship person uuid: " + otherPersonsUuid, e);
 				}
 				
 				// get the gender
 				String gender = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_PERSON_GENDER, currentNode);
 				setGender(patient, gender);
 				
 				// set the person's name
 				PersonName pn = new PersonName();
 				pn.setGivenName(xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_PERSON_GIVENNAME, currentNode));
 				pn.setMiddleName(xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_PERSON_MIDDLENAME, currentNode));
 				pn.setFamilyName(xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_PERSON_FAMILYNAME, currentNode));
 				patient.addName(pn);
 				
 				// now save the patient to the db so we have a primary key
 				Context.getPatientService().savePatient(patient);
 			}
 			
 			String personAorB = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_A_OR_B, currentNode);
 			personAorB = personAorB.trim(); // take out whitespace
 			if ("B".equals(personAorB)) { // the person defined here is person B.  then personA is the parent patient object for the current form  
 				relationship.setPersonA(createdPatient);
 				relationship.setPersonB(patient);
 			}
 			else { // "this" is person B is the parent person for this whole message
 				relationship.setPersonA(patient);
 				relationship.setPersonB(createdPatient);
 			}
 			
 			// get the voided status
 			String voidStatus = xp.evaluate(RemoteFormEntryConstants.PERSON_RELATIONSHIP_VOIDED, currentNode);
 			if (!relationship.isVoided() && voidStatus != null && voidStatus.equals("true")) {
 				relationship.setVoided(true);
 				relationship.setVoidReason("Voided at remote");
 				relationship.setVoidedBy(enterer);
 				relationship.setDateVoided(new Date());
 			}
 			
 			relationship.setCreator(enterer);
 			relationship.setDateCreated(new Date());
 			
 			relationships.add(relationship);
 		}
 		
 		return relationships;
 		
 	}
 	
 	/**
 	 * Create the program/workflow/states that this patient has been added to
 	 * 
 	 * @param createdPatient Patient that should be enrolled in the states
 	 * @param doc Document that represents this form
 	 * @param xp xpath translator
 	 * @param enterer user that entered the form
 	 */
 	public static void createProgramWorkflowEnrollment(Patient createdPatient, Document doc, XPath xp, User enterer)
 	                                                                                                                throws XPathExpressionException {
 		// TODO discover and loop over the patient's states
 		
 	}
 	
 	/**
 	 * Look up the DA that entered this form
 	 * 
 	 * @param doc Document that represents this form
 	 * @param xp xpath translator
 	 * @return User that entered this form
 	 */
 	public static User getEnterer(Document doc, XPath xp) throws XPathExpressionException {
 		UserService userService = Context.getUserService();
 		
 		Integer entererId = Integer.valueOf(xp.evaluate("substring-before(/form/header/enterer, '^')", doc));
 		User enterer = userService.getUser(entererId);
 		
 		return enterer;
 	}
 	
 	/**
 	 * @return
 	 */
 	public static Map<Location, List<Location>> getRemoteLocations() {
 		LocationService locationService = Context.getLocationService();
 		
 		String locationGP = Context.getAdministrationService().getGlobalProperty(
 		    RemoteFormEntryConstants.GP_REMOTE_LOCATIONS, "");
 		
 		Map<Location, List<Location>> locations = new HashMap<Location, List<Location>>();
 		
 		// if no global property is defined, assume all locations
 		if (locationGP.length() < 1) {
 			for (Location loc : locationService.getAllLocations(false)) {
 				List<Location> locationList = new Vector<Location>();
 				locationList.add(loc);
 				locations.put(loc, locationList);
 			}
 		}
 		// if they defined the global property, build the list dynamically
 		else {
 			String[] groupedLocations = locationGP.split(",");
 			
 			// loop over the strings like "1", "2 4 5", "13", "3 6"
 			for (String locationIds : groupedLocations) {
 				String[] singleLocations = locationIds.split(" ");
 				List<Location> locationList = new Vector<Location>();
 				for (String locationId : singleLocations) {
 					// skip over any empty or blank spaces
 					locationId = locationId.trim();
 					if (locationId.length() > 0)
 						locationList.add(locationService.getLocation(Integer.valueOf(locationId)));
 				}
 				
 				// skip over any double commas in the global property
 				if (locationList.size() > 0) {
 					// the first location in the list is the key 
 					locations.put(locationList.get(0), locationList);
 				}
 			}
 		}
 		
 		return locations;
 		
 	}
 	
 }
