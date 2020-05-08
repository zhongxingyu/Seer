 package org.openmrs.module.patientregistration.service;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.Person;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 import org.openmrs.layout.web.address.AddressSupport;
 import org.openmrs.module.patientregistration.PatientRegistrationConstants;
 import org.openmrs.module.patientregistration.PatientRegistrationGlobalProperties;
 import org.openmrs.module.patientregistration.PatientRegistrationSearch;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.openmrs.module.patientregistration.UserActivity;
 import org.openmrs.module.patientregistration.service.db.PatientRegistrationDAO;
 import org.openmrs.module.patientregistration.util.DuplicatePatient;
 import org.openmrs.util.OpenmrsConstants;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.transaction.annotation.Transactional;
 
 public class PatientRegistrationServiceImpl implements PatientRegistrationService {
 
 	protected final Log log = LogFactory.getLog(getClass());
 
 	//***** PROPERTIES *****
 	private PatientRegistrationDAO dao;
 	
 	//***** GETTERS AND SETTERS ****
 	
 	public PatientRegistrationDAO getDao() {
 		return dao;
 	}
 
 	public void setDao(PatientRegistrationDAO dao) {
 		this.dao = dao;
 	}
 	
 	//***** SERVICE METHODS ***********
 	@Transactional
 	public Encounter registerPatient(Patient patient, Person provider, EncounterType encounterType, Location location, Date registrationDate) {
 		
 		if (patient == null) {
 			throw new APIException("No patient specified");
 		}
 		if (provider == null) {
 			throw new APIException("No provider specified");
 		}
 		if (encounterType == null) {
 			throw new APIException("No encounter type specified");
 		}
 		if (location == null) {
 			throw new APIException("No location specified");
 		}
 		if (registrationDate == null) {
 			throw new APIException("No registration date specified");
 		}
 		
 		Encounter registration = getEncounterByDateAndType(patient, encounterType, location, registrationDate);
 		if(registration==null){
 			registration = new Encounter();
 			registration.setPatient(patient);
 			registration.setProvider(provider);
 			registration.setEncounterType(encounterType);
 			registration.setLocation(location);
 			registration.setEncounterDatetime(registrationDate);
 			
 			Context.getEncounterService().saveEncounter(registration);	
 		}
 		else {
 			log.info("patient " + patient.getId() + " already registered on " + registrationDate + " at " + location.getName());
 		}
 		
 		return registration;
     }
 
	//@Transactional
 	public Encounter registerPatient(Patient patient, Person provider, EncounterType encounterType, Location location) {
 		// delegate to main register patient, using the current date as the registration date
 	   return registerPatient(patient, provider, encounterType, location, new Date());
     }
 
 	@Transactional(readOnly=true)
 	public List<Patient> exactSearch(PersonName personName) {
 		// return null if passed null
 		if (personName == null) {
 			return null;
 		}
 	
 		return getPatientRegistrationSearch().exactSearch(personName);
     }
 	
 	@Transactional(readOnly=true)
 	public List<Patient> search(PersonName personName) {
 		// return null if passed null
 		if (personName == null) {
 			return null;
 		}
 	
 		return getPatientRegistrationSearch().search(personName);
     }
 	
 	@Transactional(readOnly=true)
 	public List<Patient> search(String personFirstName) {
 		// return null if blank value was passed
 		if(!StringUtils.isNotBlank(personFirstName)){
 			return null;
 		}
 	
 		return getPatientRegistrationSearch().search(personFirstName);
     }
 	@Transactional(readOnly=true)
 	public Set<String> searchNames(String name, String nameField) {
 		return dao.searchNames(name, nameField);
 	}
 	@Transactional(readOnly=true)
 	public Map<String,Integer> searchNamesByOccurence(String name, String nameField) {
 		return dao.searchNamesByOccurence(name, nameField);
 	}
 	@Transactional(readOnly=true)
 	public List<Integer> getPhoneticsPersonId(String firstName, String lastName) {
 		return dao.getPhoneticsPersonId(firstName, lastName);
 	}
 	@Transactional(readOnly=true)
 	public List<Patient> getPatientsByNameId(List<Integer> nameIds) {
 		return dao.getPatientsByNameId(nameIds);
 	}
 	
 	@Transactional(readOnly=true)
 	public List<String> getDistinctObs(Integer conceptId){
 		return dao.getDistinctObs(conceptId);
 	}
 	
 	@Transactional(readOnly=true)
 	public Set<Integer> getDistinctDuplicateObs(Integer conceptId){
 		return dao.getDistinctDuplicateObs(conceptId);
 	}
 	
 	@Transactional(readOnly=true)
 	public List<Patient> search(Patient patient) {
 		// return null if passed null
 		if (patient == null) {
 			return null;
 		}
 		
 		return getPatientRegistrationSearch().search(patient);
     }
 
 	@Transactional(readOnly=true)
 	public boolean printRegistrationLabel(Patient patient, Location location) {
 		return printRegistrationLabel(patient, location, 1);
 	}
 	
 	@Transactional(readOnly=true)
 	public boolean printRegistrationLabel(Patient patient, Location location, Integer count) {
 	
 		try {
 			// handle null case
 			if (patient == null) {
 				throw new APIException("No patient passed to printRegistrationLabel method");
 			}
 			
 			// make sure we have a ip address and port specified
 			String ipAddress = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_LABEL_PRINTER_IP_ADDRESS();
 			
 			if (ipAddress == null) {
 				throw new APIException("No ip address specified for label printer in global property");
 			}
 			
 			Integer port = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_LABEL_PRINTER_PORT();
 			
 			if (port == null) {
 				throw new APIException("No port specified for label printer in global property");
 			}
 			
 			DateFormat df = new SimpleDateFormat(PatientRegistrationConstants.DATE_FORMAT_DISPLAY, Context.getLocale());
 			
 			// TODO: potentially pull this formatting code into a configurable template?
 			// build the command to send to the printer -- written in ZPL
 			StringBuilder data = new StringBuilder();
 			data.append("^XA"); 
 			data.append("^CI28");   // specify Unicode encoding		
 			
 			/* LEFT COLUMN */
 			
 			/* Name (Only print first and last name */			
 			data.append("^FO140,40^AVN^FD" + (patient.getPersonName().getGivenName() != null ? patient.getPersonName().getGivenName() : "") + " " 
 					+ (patient.getPersonName().getFamilyName() != null ? patient.getPersonName().getFamilyName() : "") + "^FS");
 			
 			/* Address (using address template) */
 			if (patient.getPersonAddress() != null) {
 				
 				int verticalPosition = 140;
 				
 				// print out the address using the layout format		
 				// first iterate through all the lines in the format
 				if (AddressSupport.getInstance().getDefaultLayoutTemplate() != null && AddressSupport.getInstance().getDefaultLayoutTemplate().getLines() != null) {
 					
 					List<List<Map<String,String>>> lines = AddressSupport.getInstance().getDefaultLayoutTemplate().getLines();
 					ListIterator<List<Map<String,String>>> iter = lines.listIterator();
 					
 					while(iter.hasNext()){
 						List<Map<String,String>> line = iter.next();				
 						// now iterate through all the tokens in the line and build the string to print
 						StringBuffer output = new StringBuffer();					
 						for (Map<String,String> token : line) {
 							// find all the tokens on this line, and then add them to that output line 
 							if(token.get("isToken").equals(AddressSupport.getInstance().getDefaultLayoutTemplate().getLayoutToken())) {
 								
 								String property = PatientRegistrationUtil.getPersonAddressProperty(patient.getPersonAddress(), token.get("codeName"));
 								
 								if (!StringUtils.isBlank(property)) {
 									output.append(property + ", ");
 								}
 							}
 						}
 						
 						if (output.length() > 2) { 
 							// drop the trailing comma and space from the last token on the line
 							output.replace(output.length() - 2, output.length(), "");
 						}
 						
 						if (!StringUtils.isBlank(output.toString())) {
 							data.append("^FO140," + verticalPosition + "^ATN^FD" + output.toString() + "^FS"); 
 							verticalPosition = verticalPosition + 50;
 						}				
 					}
 				}
 				else {
 					log.error("Address template not properly configured");
 				}
 			}
 			
 			
 			/* Birthdate */
 			data.append("^FO140,350^ATN^FD" + df.format(patient.getBirthdate()) + " " + (patient.getBirthdateEstimated() ? "(*)" : " ") + "^FS"); 
 			data.append("^FO140,400^ATN^FD" + Context.getMessageSourceService().getMessage("patientregistration.gender." + patient.getGender()) + "^FS"); 	
 			
 	
 			/* RIGHT COLUMN */
 			
 			/* Print the numero dossier for the current location */
 			PatientIdentifier numeroDossier = PatientRegistrationUtil.getNumeroDossier(patient, location);
 			
 			if (numeroDossier != null) {
 				data.append("^FO870,50^FB350,1,0,R,0^AVN^FD" + numeroDossier.getIdentifier() + "^FS"); 
 				data.append("^FO870,125^FB350,1,0,R,0^ATN^FD" + location.getName() + " " + Context.getMessageSourceService().getMessage("patientregistration.dossier") + "^FS"); 
 				data.append("^FO870,175^FB350,1,0,R,0^ATN^FD" + Context.getMessageSourceService().getMessage("patientregistration.issued") + " " + df.format(new Date()) + "^FS"); 
 			}
 			
 			/* Print the bar code, based on the primary identifier */
 			PatientIdentifier primaryIdentifier = PatientRegistrationUtil.getPrimaryIdentifier(patient);
 			
 			if (primaryIdentifier != null) {
 				data.append("^FO790,250^ATN^BY4^BCN,150^FD" + primaryIdentifier.getIdentifier() + "^FS");    // print barcode & identifier
 			}
 				
 			/* Quanity and print command */
 			data.append("^PQ" + count);
 			data.append("^XZ");
 			
 			Socket socket = null;
 			// Create a socket with a timeout
 			try {
 			    InetAddress addr = InetAddress.getByName(ipAddress);		    
 			    SocketAddress sockaddr = new InetSocketAddress(addr, port);
 			    // Create an unbound socket
 			    socket = new Socket();
 	
 			    // This method will block no more than timeoutMs.
 			    // If the timeout occurs, SocketTimeoutException is thrown.
 			    int timeoutMs = 500;   // 500ms
 			    socket.connect(sockaddr, timeoutMs);
 			    // now send the whole command string to the printer
 			    IOUtils.write(data.toString(), socket.getOutputStream(), "UTF-8");
 			    return true;
 			}
 			finally {
 				try {
 					socket.close();
 				} catch (IOException e) {
 					log.error("failed to close the socket to the label printer" + e);
 				}
 			}
 		}
 		catch (Exception e) {
 			log.error("Unable to print registration label: " + e);
 			return false;
 		}
 	}
 	
 	@Transactional(readOnly=true)
 	public boolean printIDCardLabel(Patient patient) {
 		
 		try {
 			// handle null case
 			if (patient == null) {
 				throw new APIException("No patient passed to printIDCardLabel method");
 			}
 			
 			// make sure we have a ip address and port specified
 			String ipAddress = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_LABEL_PRINTER_IP_ADDRESS();
 			
 			if (ipAddress == null) {
 				throw new APIException("No ip address specified for label printer in global property");
 			}
 			
 			Integer port = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_LABEL_PRINTER_PORT();
 			
 			if (port == null) {
 				throw new APIException("No port specified for label printer in global property");
 			}
 					
 			// TODO: potentially pull this formatting code into a configurable template?
 			// build the command to send to the printer -- written in ZPL
 			StringBuilder data = new StringBuilder();
 			data.append("^XA"); 
 			data.append("^CI28");   // specify Unicode encoding		
 			
 			
 			List<PatientIdentifier> patientIdentifiers = PatientRegistrationUtil.getAllNumeroDossiers(patient);
 		
 			/* Print all number dossiers in two columns*/
 			if (patientIdentifiers != null && patientIdentifiers.size() > 0) {	
 				int verticalPosition = 30;
 				int horizontalPosition = 140;
 				int count = 0;
 				
 				for (PatientIdentifier identifier : patientIdentifiers) {
 					data.append("^FO" + horizontalPosition + "," + verticalPosition + "^AVN^FD" + identifier.getIdentifier() + "^FS"); 
 					data.append("^FO" + horizontalPosition + "," + (verticalPosition + 75) + "^ATN^FD" + identifier.getLocation().getName() + " " + Context.getMessageSourceService().getMessage("patientregistration.dossier") + "^FS"); 
 	
 					verticalPosition = verticalPosition + 130;
 					count++;
 					
 					// switch to second column if needed
 					if (verticalPosition == 420) {
 						verticalPosition = 30;
 						horizontalPosition = 550;
 					}
 					
 					// we can't fit more than 6 dossier numbers on a label--this is a real edge case
 					if (count > 5) {
 						break;
 					}
 				}
 			}
 			
 			/* Draw the "tear line" */
 			data.append("^FO1025,10^GB0,590,10^FS");
 			
 			/* Print command */
 			data.append("^XZ");
 			
 			
 			Socket socket = null;
 			// Create a socket with a timeout
 			try {
 			    InetAddress addr = InetAddress.getByName(ipAddress);		    
 			    SocketAddress sockaddr = new InetSocketAddress(addr, port);
 			    // Create an unbound socket
 			    socket = new Socket();
 	
 			    // This method will block no more than timeoutMs.
 			    // If the timeout occurs, SocketTimeoutException is thrown.
 			    int timeoutMs = 500;   // 500ms
 			    socket.connect(sockaddr, timeoutMs);
 			    IOUtils.write(data.toString(), socket.getOutputStream(), "UTF-8");
 			    return true;
 			}
 			finally{
 				try {
 					socket.close();
 				} catch (IOException e) {
 					log.error("failed to close the socket to the label printer" + e);
 				}
 			}
 		}
 		catch (Exception e) {
 			log.error("Unable to print id card label: " + e);
 			return false;
 		}
 	}
 	
 	@Transactional(readOnly=true)
 	public boolean printIDCard(Patient patient, Location location) {
 	
 		try {
 			// handle null case
 			if (patient == null) {
 				throw new APIException("No patient passed to printIDLabel method");
 			}
 			
 			// make sure we have a ip address and port specified
 			String ipAddress = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_PRINTER_IP_ADDRESS();
 			
 			if (ipAddress == null) {
 				throw new APIException("No ip address specified for id card printer in global property");
 			}
 			
 			Integer port = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_PRINTER_PORT();
 			
 			if (port == null) {
 				throw new APIException("No port specified for id card printer in global property");
 			}
 			
 			DateFormat df = new SimpleDateFormat(PatientRegistrationConstants.DATE_FORMAT_DISPLAY, Context.getLocale());
 			
 			// TODO: potentially pull this formatting code into a configurable template?
 			// build the command to send to the printer -- written in EPCL
 			String ESC = "\u001B";
 			
 			StringBuilder data = new StringBuilder();
 			
 			data.append(ESC + "+RIB\n");   // specify monochrome ribbon type
 			data.append(ESC + "+C 4\n");   // specify thermal intensity
 			data.append(ESC + "F\n");	   // clear monochrome buffer
 		
 			data.append(ESC + "B 75 550 0 0 0 3 100 0 "+ PatientRegistrationUtil.getPreferredIdentifier(patient) + "\n");    // layout bar code and patient identifier
 			data.append(ESC + "T 75 600 0 1 0 45 1 "+ PatientRegistrationUtil.getPreferredIdentifier(patient) + "\n");   
 			
 			data.append(ESC + "T 75 80 0 1 0 75 1 "+ (patient.getPersonName().getFamilyName() != null ? patient.getPersonName().getFamilyName() : "") + " " 
 								+ (patient.getPersonName().getFamilyName2() != null ? patient.getPersonName().getFamilyName2() : "") 
 								+ (patient.getPersonName().getGivenName() != null ? patient.getPersonName().getGivenName() : "") + " "
 								+ (patient.getPersonName().getMiddleName() != null ? patient.getPersonName().getMiddleName() : "") 
 								+ "\n");
 			
 			data.append(ESC + "T 75 350 0 0 0 25 1 " + Context.getMessageSourceService().getMessage("patientregistration.gender") + "\n");
 			data.append(ESC + "T 75 400 0 1 0 50 1 " + Context.getMessageSourceService().getMessage("patientregistration.gender." + patient.getGender()) + "\n");
 			
 			data.append(ESC + "T 250 350 0 0 0 25 1 " + Context.getMessageSourceService().getMessage("patientregistration.person.birthdate") + 
 						(patient.getBirthdateEstimated() ? " (" + Context.getMessageSourceService().getMessage("patientregistration.person.birthdate.estimated") + ")" : " ") + "\n");
 			data.append(ESC + "T 250 400 0 1 0 50 1 " + df.format(patient.getBirthdate()) + "\n");
 							
 			// layout out the address using the address template, if one exists
 			int verticalPosition = 150;
 			if (patient.getPersonAddress() != null) {
 				
 				// print out the address using the layout format		
 				// first iterate through all the lines in the format
 				if (AddressSupport.getInstance().getDefaultLayoutTemplate() != null && AddressSupport.getInstance().getDefaultLayoutTemplate().getLines() != null) {
 					
 					List<List<Map<String,String>>> lines = AddressSupport.getInstance().getDefaultLayoutTemplate().getLines();
 					ListIterator<List<Map<String,String>>> iter = lines.listIterator();
 				
 					while(iter.hasNext()){
 						List<Map<String,String>> line = iter.next();				
 						// now iterate through all the tokens in the line and build the string to print
 						StringBuffer output = new StringBuffer();					
 						for (Map<String,String> token : line) {
 							// find all the tokens on this line, and then add them to that output line 
 							if(token.get("isToken").equals(AddressSupport.getInstance().getDefaultLayoutTemplate().getLayoutToken())) {
 								
 								String property = PatientRegistrationUtil.getPersonAddressProperty(patient.getPersonAddress(), token.get("codeName"));
 								
 								if (!StringUtils.isBlank(property)) {
 									output.append(property + ", ");
 								}
 							}
 						}
 						
 						if (output.length() > 2) { 
 							// drop the trailing comma and space from the last token on the line
 							output.replace(output.length() - 2, output.length(), "");
 						}
 						
 						if (!StringUtils.isBlank(output.toString())) {
 							data.append(ESC + "T 75 " + verticalPosition + " 0 1 0 50 1 " + output.toString() + "\n");
 							verticalPosition = verticalPosition + 50;
 						}				
 					}
 				}
 				else {
 					log.error("Address template not properly configured");
 				}
 		
 			}
 			
 			// now print the patient attribute type that has specified in the idCardPersonAttributeType global property
 			PersonAttributeType type = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_PERSON_ATTRIBUTE_TYPE();
 			if (type != null) {
 				PersonAttribute attr = patient.getAttribute(type);
 				if (attr != null && attr.getValue() != null) {
 					// see if there is a message code for this type name (by creating a camel case version of the name), otherwise just use the type name directly
 					String typeName = Context.getMessageSourceService().getMessage("patientregistration." + PatientRegistrationUtil.toCamelCase(type.getName()), null, type.getName(), Context.getLocale());
 					data.append(ESC + "T 600 350 0 0 0 25 1 " + (StringUtils.isBlank(typeName) ? type.getName() : typeName) + "\n");
 					verticalPosition = verticalPosition + 50;
 					data.append(ESC + "T 600 400 0 1 0 50 1 " + attr.getValue() + "\n");	
 				}
 			}
 			
 			// custom card label, if specified
 			if (PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_LABEL_TEXT() != null) {
 				data.append(ESC + "T 420 510 0 1 0 45 1 " + PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_ID_CARD_LABEL_TEXT() + "\n");
 			}
 			
 			// date issued and location issued are aligned to bottom of the card
 			data.append(ESC + "T 420 550 0 0 0 25 1 " + Context.getMessageSourceService().getMessage("patientregistration.dateIDIssued") + "\n");
 			data.append(ESC + "T 420 600 0 1 0 50 1 " + df.format(new Date()) + "\n");    // date issued is today
 	
 			data.append(ESC + "T 720 550 0 0 0 25 1 " + Context.getMessageSourceService().getMessage("patientregistration.locationIssued") + "\n");
 			data.append(ESC + "T 720 600 0 1 0 50 1 " + (location != null ? location.getDisplayString() : "") + "\n");
 			
 			data.append(ESC + "L 20 420 955 5 1\n");   //divider line
 			
 			data.append(ESC + "I\n");		// trigger the actual print job
 			
 			// TOOD: remove this line once we figure out how to make the print stops making noise
 			//data.append(ESC + "R\n");       // reset the printer (hacky workaround to make the printer stop making noise)
 			
 			Socket socket = null;
 			// Create a socket with a timeout
 			try {
 			    InetAddress addr = InetAddress.getByName(ipAddress);		    
 			    SocketAddress sockaddr = new InetSocketAddress(addr, port);
 			    // Create an unbound socket
 			    socket = new Socket();
 	
 			    // This method will block no more than timeoutMs.
 			    // If the timeout occurs, SocketTimeoutException is thrown.
 			    int timeoutMs = 500;   // 500ms
 			    socket.connect(sockaddr, timeoutMs);
 			    IOUtils.write(data.toString().getBytes("Windows-1252"), socket.getOutputStream());    // the id card printer doesn't speak unicode;
 			    return true;
 			}
 			finally{
 				try {
 					socket.close();
 				} catch (IOException e) {
 					log.error("failed to close the socket to the ID card printer", e);
 				}
 			}
 		}
 		catch (Exception e) {
 			log.error("Unable to print id card", e);
 			return false;
 		}
 	}
 	
     /**
      * @see PatientRegistrationService#getNumberOfRegistrationEncounters(List<EncounterType>, Date, Date)
      */
 	// TODO: do we want to move this to the DAO?
 	@Transactional(readOnly=true)
 	public Map<EncounterType, Integer> getNumberOfRegistrationEncounters(List<EncounterType> encounterTypes, Location location, Date fromDate, Date toDate) {
 		
 		Map<EncounterType, Integer> m = new HashMap<EncounterType, Integer>();
 		
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		
 		// Get all of the Encounter Types by ID
 		Map<String, EncounterType> encounterTypeMap = new HashMap<String, EncounterType>();
 		if (encounterTypes == null) {
 			encounterTypes = Context.getEncounterService().getAllEncounterTypes();
 		}
 		for (EncounterType type : encounterTypes) {
 			encounterTypeMap.put(type.getEncounterTypeId().toString(), type);
 		}
 		
 		// Get the number of registrations grouped by encounter type
 		StringBuilder query = new StringBuilder();
 		query.append("select e.encounter_type, count(*) from encounter e, patient p ");
 		query.append("where e.patient_id = p.patient_id ");
 		query.append("and e.voided = 0 and p.voided = 0 ");
 		query.append("and e.encounter_type in (" + OpenmrsUtil.join(encounterTypeMap.keySet(), ",") + ") ");
 		if (fromDate != null) {
 			query.append("and date(e.encounter_datetime) >= " + df.format(fromDate) + " ");
 		}
 		if (toDate != null) {
 			query.append("and date(e.encounter_datetime) <= " + df.format(toDate) + " ");
 		}
 		if (location != null) {
 			query.append("and e.location_id = " + location.getId() + " ");
 		}
 		query.append("group by e.encounter_type");
 		
 		List<List<Object>> queryResults = Context.getAdministrationService().executeSQL(query.toString(), true);
 		for (List<Object> l : queryResults) {
 			Object encounterTypeId = l.get(0);
 			if (encounterTypeId != null) {			
 				EncounterType type = encounterTypeMap.get(encounterTypeId.toString());
 				Integer count = Integer.valueOf(l.get(1).toString());
 				m.put(type, count);
 			}
 		}
 
 		return m;
 	}
 	
     /**
      * @see PatientRegistrationService#getNumberOfEncountersByDate(EncounterType, Location)
      */
 	// TODO: do we want to move this to the DAO?
 	@Transactional(readOnly=true)
 	public Map<Date, Integer> getNumberOfEncountersByDate(EncounterType encounterType, Location location) {
 		
 		Map<Date, Integer> m = new TreeMap<Date, Integer>();
 		try{
 			Context.addProxyPrivilege(OpenmrsConstants.PRIV_SQL_LEVEL_ACCESS);
 			// Get the number of registrations grouped by encounter type
 			StringBuilder query = new StringBuilder();
 			query.append("select date(e.encounter_datetime) as encounter_date, count(*) from encounter e, patient p ");
 			query.append("where e.patient_id = p.patient_id ");
 			query.append("and e.voided = 0 and p.voided = 0 ");
 			if (encounterType != null) {
 				query.append("and e.encounter_type = " + encounterType.getEncounterTypeId() + " ");
 			}
 			if (location != null) {
 				query.append("and e.location_id = " + location.getLocationId() + " ");
 			}
 			query.append("group by date(e.encounter_datetime)");
 			
 			List<List<Object>> queryResults = Context.getAdministrationService().executeSQL(query.toString(), true);
 			for (List<Object> l : queryResults) {
 				m.put((Date)l.get(0), Integer.valueOf(l.get(1).toString()));
 			}
 		}finally {
 			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_SQL_LEVEL_ACCESS);
 		}
 		return m;
 	}
 
     /**
      * @see PatientRegistrationService#getNumberOfPatientsByAddress(Map<String, String>, String, EncounterType, Location)
      */
 	@Transactional(readOnly=true)
 	public Map<String, Integer> getNumberOfPatientsByAddress(Map<String, String> filterCriteria, String addressField, EncounterType encounterType, Location location) {
 		return dao.getNumberOfRegistrationsByAddress(filterCriteria, addressField, encounterType, location, true);
 	}
 
 	@Transactional(readOnly=true)
 	public List<DuplicatePatient> getDuplicatePatients(Patient patient){
 		return dao.getDuplicatePatients(patient);
 	}
     /**
      * @see PatientRegistrationService#getNumberOfEncountersByAddress(Map<String, String>, String, EncounterType, Location)
      */
 	@Transactional(readOnly=true)
 	public Map<String, Integer> getNumberOfEncountersByAddress(Map<String, String> filterCriteria, String addressField, EncounterType encounterType, Location location) {
 		return dao.getNumberOfRegistrationsByAddress(filterCriteria, addressField, encounterType, location, false);
 	}
 	
 	
 	/** 
 	 * Private utility methods
 	 */
 	
 	/**
 	 * Test if a patient is already registered at the given location on the given date
 	 * 
 	 * @param patient the patient
 	 * @param encounterType the encounter type of the registration
 	 * @param location the location of the registration
 	 * @param registrationDate the date of registration
 	 * @return true/false if the patient is already registered at the given location on the given date
 	 */
 	private Boolean alreadyRegistered(Patient patient, EncounterType encounterType, Location location, Date registrationDate) {
 		
 		Encounter encounter = getEncounterByDateAndType(patient, encounterType, location, registrationDate);
 		return (encounter != null ? true : false);
 	}
 	
 	private Encounter getEncounterByDateAndType(Patient patient, EncounterType encounterType, Location location, Date registrationDate) {
 		
 		// clear the time component to get the start time to search (first millisecond of current day)
 		Date startTime = PatientRegistrationUtil.clearTimeComponent(registrationDate);
 		
 		// create the end time to search (last millisecond of the current day)
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(startTime);
 		cal.add(Calendar.DAY_OF_MONTH, +1);
 		cal.add(Calendar.MILLISECOND, -1);
 		Date endTime = cal.getTime();
 		
 		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, location, startTime, endTime, null, Arrays.asList(encounterType), null, false);
 		if(encounters!=null && encounters.size()>0){
 			return encounters.get(0);
 		}else{
 			return null;
 		}
 		
 	}
 	
 	public List<Obs> getPatientObs(Patient patient, EncounterType encounterType,  List<Encounter> encounters, List<Concept> questions, Location location, Date registrationDate) {
 		
 		// clear the time component to get the start time to search (first millisecond of current day)
 		Date startTime = PatientRegistrationUtil.clearTimeComponent(registrationDate);
 		
 		// create the end time to search (last millisecond of the current day)
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(startTime);
 		cal.add(Calendar.DAY_OF_MONTH, +1);
 		cal.add(Calendar.MILLISECOND, -1);
 		Date endTime = cal.getTime();
 		
 		List<Obs> obs= Context.getObsService().getObservations(Collections.singletonList((Person)patient)
 				, encounters, questions
 				, null, null, Collections.singletonList((Location)location)
 				, null, null, null, startTime, endTime, false);
 		
 		return (obs != null && obs.size() > 0 ? obs : null);
 	}
 	
 	/**
 	 * Fetches and loads the Patient Search Class specified in GLOBAL_PROPERTY_SEARCH_CLASS
 	 * If no search class defined in a global property, use the default search class
 	 */
     private PatientRegistrationSearch getPatientRegistrationSearch() {
 		String patientSearchClass = PatientRegistrationGlobalProperties.GLOBAL_PROPERTY_SEARCH_CLASS();
 		
 		// if the global prop doesn't exist, use the hard-coded default class
 		if (StringUtils.isBlank(patientSearchClass)) {
 			patientSearchClass = PatientRegistrationConstants.DEFAULT_SEARCH_CLASS;
 		}
 		 
 		// now try to load the specified class
 		@SuppressWarnings("rawtypes")
         Class patientSearch;
 		try {
 	        patientSearch= Context.loadClass(patientSearchClass);
         }
         catch (ClassNotFoundException e) {
 	        throw new APIException("Unable to load search class " + patientSearchClass);
         }
 		
         try {
         	return (PatientRegistrationSearch) patientSearch.newInstance();
         }
         catch(Exception e) {
         	throw new APIException("Unable to instantiate search class " + patientSearchClass);
         }	
 	}
 
     /**
      * @see PatientRegistrationService#saveUserActivity(UserActivity)
      */
     @Transactional
 	public UserActivity saveUserActivity(UserActivity userActivity) {
 		return dao.saveUserActivity(userActivity);
 	}
 }
