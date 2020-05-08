 package org.openmrs.module.mastercard;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PatientProgram;
 import org.openmrs.PatientState;
 import org.openmrs.PersonAddress;
 import org.openmrs.PersonName;
 import org.openmrs.ProgramWorkflow;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.ProgramWorkflowService;
 import org.openmrs.api.context.Context;
 import org.openmrs.mastercard.exceptions.WrongFormatException;
 import org.openmrs.module.mastercard.entities.ArvMastercardBean;
 import org.openmrs.module.mastercard.entities.EncounterData;
 import org.openmrs.module.mastercard.entities.HeaderData;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ArtImporter {
 	
 	static Logger logger = Logger.getLogger(ArtImporter.class);
 	
 	private Helper h = new Helper();
 	
 	public ArtImporter() {
 	}
 	
 	public static void main(String[] args) throws Exception {
 		// parameters
 		String importFile = args[0];
 		String openmrsRuntimeProperties = args[1];
 		String openmrsUser = args[2];
 		String openmrsPw = args[3];
 		
 		// properties
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(openmrsRuntimeProperties));
 		String connectionUser = prop.getProperty("connectionImport.username");
 		String conncetionPw = prop.getProperty("connectionImport.password");
 		String conncetionUrl = prop.getProperty("connectionImport.url");
 		
 		// connection init
 		Context.startup(conncetionUrl, connectionUser, conncetionPw, prop);
 		Context.openSession();
 		Context.authenticate(openmrsUser, openmrsPw);
 		
 		new ArtImporter().run();
 	}
 	
 	public void run() throws Exception {
 		try {
 			executeImport();
 		}
 		catch (WrongFormatException e) {
 			logger.error("Error generated", e);
 			throw new Exception(e);
 		}
 	}
 	
 	public void executeImport() throws Exception, WrongFormatException {
 		EncounterService es = Context.getEncounterService();
 		PatientService ps = Context.getPatientService();
 		
 		logger.debug("Start importing");
 		
 		File dir = new File("export");
 		
 		FilenameFilter filter = new FilenameFilter() {
 			
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".csv");
 			}
 		};
 		
 		File[] children = dir.listFiles(filter);
 		if (children == null) {
 			// Either dir does not exist or is not a directory
 		} else {
 			for (int i = 0; i < children.length; i++) {
 				// Get filename of file or directory
 				File file = children[i];
 				logger.info("Found files in export: " + file);
 				//getMastercardFromFile(file);
 				importPatientsData(es, ps, null, file);
 			}
 		}
 		
 		logger.info("Import finished");
 	}
 	
 	private void importPatientsData(EncounterService es, PatientService ps, Location nno, File file)
 	                                                                                                throws FileNotFoundException,
 	                                                                                                IOException,
 	                                                                                                WrongFormatException {
 		ArvMastercardBean mastercard = getMastercardFromFile(file);
 		writeMastercardToDatabase(es, ps, nno, mastercard);
 	}
 	
 	private ArvMastercardBean getMastercardFromFile(File file) throws IOException, WrongFormatException {
 		ArvMastercardBean masterCardBean = null;
 		try {
 			String[] headerStringArray = new String[10];
 			String[] encounterStringArray = null;
 			
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			String str;
 			HashMap<Integer, String> encounterStringMap = new HashMap<Integer, String>();
 			
 			int i = 0;
 			while ((str = in.readLine()) != null) {
 				logger.info(i + " " + str);
 				if (i < 10) {
 					logger.info("Adding headerStringArray[" + i + "] " + str);
 					headerStringArray[i] = str;
 				}
 				if (i > 10) {
 					logger.info("Adding encounterStringMap[" + (i - 11) + "] " + str);
 					encounterStringMap.put(new Integer(i - 11), str);
 				}
 				i++;
 			}
 			in.close();
 			
 			List<Integer> sortedHashKeys = new ArrayList<Integer>();
 			sortedHashKeys.addAll(encounterStringMap.keySet());
 			
 			Collections.sort(sortedHashKeys);
 			
 			i = 0;
 			encounterStringArray = new String[encounterStringMap.size()];
 			Iterator<Integer> iter = sortedHashKeys.iterator();
 			while (iter.hasNext()) {
 				encounterStringArray[i] = encounterStringMap.get(iter.next());
 				logger.info("files to encounterStringArray[" + i + "]" + encounterStringArray[i]);
 				i++;
 			}
 			
 			HeaderData headerBean = parseArrayForHeaderData(headerStringArray);
 			EncounterData[] encounterBeanArray = parseArrayForEncounterData(encounterStringArray);
 			
 			masterCardBean = new ArvMastercardBean();
 			masterCardBean.setHeaderData(headerBean);
 			masterCardBean.setEncounterData(encounterBeanArray);
 		}
 		catch (IOException e) {
 			logger.error(e.fillInStackTrace());
 		}
 		
 		return masterCardBean;
 	}
 	
 	private void writeMastercardToDatabase(EncounterService es, PatientService ps, Location nno, ArvMastercardBean mastercard)
 	                                                                                                                          throws FileNotFoundException,
 	                                                                                                                          IOException {
 		Patient p = new Patient();
 		p.setBirthdate(mastercard.getHeaderData().getObservations().getDateOfBirth());
 		PersonName pName = new PersonName();
 		pName.setFamilyName(mastercard.getHeaderData().getObservations().getPatientFamilyName());
 		pName.setGivenName(mastercard.getHeaderData().getObservations().getPatientGivenName());
 		p.addName(pName);
 		p.setDateChanged(new Date(System.currentTimeMillis()));
 		p.setGender(mastercard.getHeaderData().getObservations().getSex());
 		
 		ps.createPatient(p);
 		//TODO mild finish method
 		
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param encounterStringArray
 	 * @return
 	 * @throws WrongFormatException
 	 */
 	protected EncounterData[] parseArrayForEncounterData(String[] encounterStringArray) throws WrongFormatException {
 		
 		EncounterData[] encounterDataArray = new EncounterData[encounterStringArray.length - 1];
 		
 		for (int i = 0; i < encounterStringArray.length; i++) {
 			
 			if (!encounterStringArray[0]
 			        .equals("Visit loc;Vist Date;Hgt;Wt;Outcome Enrollment;Adverse Outcome;Outcome date;Regimen;Side Effects;TB status;current Pill count;Doses missed;ARVs given #;To;CPT #;Comment;Next appointment;Unknown Obs;"))
 				throw new WrongFormatException("Header Line 0 expected to be Encounter Data Header /'/'");
 			
 			if (i > 0) {
 				EncounterData encounterData = (EncounterData) new EncounterData(encounterStringArray[i]);
 				encounterDataArray[i - 1] = encounterData;
 			}
 		}
 		return encounterDataArray;
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param headerStringArray
 	 * @return
 	 * @throws WrongFormatException
 	 */
 	protected HeaderData parseArrayForHeaderData(String[] headerStringArray) throws WrongFormatException {
 		
 		//To keep signatures symetric and allow abstraction we assemble the different 
 		// mastercard-headerlines to a single string and disassemble those in HeaderData.demarshalData()
 		String assembledHelperString = new String();
 		assembledHelperString = assembledHelperString + headerStringArray[0];
 		
 		for (int i = 1; i < headerStringArray.length; i++) {
 			assembledHelperString = assembledHelperString + "#;;#";
 			assembledHelperString = assembledHelperString + headerStringArray[i];
 		}
 		
 		//That is what we want to call with a single string parameter, see HeaderData.demarshalData()
 		HeaderData headerData = (HeaderData) new HeaderData(assembledHelperString);
 		return headerData;
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param es
 	 * @param nno
 	 * @param artInitials
 	 * @param artFollowups
 	 * @param p
 	 * @return
 	 */
 	private ArvMastercardBean collectMastercardData(EncounterService es, Location nno, List<EncounterType> artInitials,
 	                                                List<EncounterType> artFollowups, Patient p) {
 		ArvMastercardBean mastercard = null;
 		
 		mastercard = new ArvMastercardBean();
 		
 		{//nnoArv to mastercard as identifier!
 			mastercard.setIdentifier(checkForPIIdentifier(p, p.getPatientId() + ""));
 			
 			logger.info("Filing mastercard for Patient with id: " + p.getId());
 			List<Encounter> el = es.getEncounters(p, nno, null, null, null, artInitials, null, false);
 			
 			HeaderData headerData = null;
 			if (el.size() == 0) {
 				// missing initial
 				logger.warn("  Missing initial, trying to use another encounter");
 				List<Encounter> anyEncounter = es.getEncounters(p, nno, null, null, null, null, null, false);
 				
 				if (anyEncounter.size() > 0) {
 					logger.info("    Going to write exportInitial to BufferedWriter");
 					headerData = new HeaderData(anyEncounter.get(0));
 				}
 			} else {
 				// take last initial (assuming list is ordered by date of entry)
 				logger.info("  Last initial encounter");
 				headerData = new HeaderData(el.get(el.size() - 1));
 			}
 			mastercard.setHeaderData(headerData);
 			
 			logger.info("  Getting encounters for Patient " + p.getId());
 			el = es.getEncounters(p, nno, null, null, null, artFollowups, null, false);
 			
 			logger.info("  Initializing Arrayof encounters. # of encounters: " + el.size());
 			EncounterData[] encounterDataArray = new EncounterData[el.size()];
 			int i = 0;
 			for (Encounter e : el) {
 				encounterDataArray[i] = new EncounterData(e);
 				i++;
 			}
 			mastercard.setEncounterData(encounterDataArray);
 		}
 		return mastercard;
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param p
 	 * @param nnoArv
 	 * @return
 	 */
 	private String checkForPIIdentifier(Patient p, String nnoArv) {
 		for (PatientIdentifier pi : p.getPatientIdentifiers(Context.getPatientService().getPatientIdentifierType(
 		    "ARV Number"))) {
 			if (pi.getIdentifier().startsWith("NNO ")) {
 				nnoArv = pi.getIdentifier();
 				logger.info("    itterating pi-identifiert: " + nnoArv);
 			}
 		}
 		return nnoArv;
 	}
 	
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param ps
 	 * @param identifierTypes
 	 * @return
 	 */
 	private List<Patient> getPatientArrayFromDB(PatientService ps, List<PatientIdentifierType> identifierTypes) {
 		List<Patient> patients = new ArrayList<Patient>();
 		for (int i = 1; i < 5; i++) {
 			patients.addAll(ps.getPatients(null, "NNO " + i, identifierTypes, true));
 			//patients.addAll(ps.getAllPatients(false));
 			logger.info("added patients - Iteration: " + i);
 		}
 		// as substitute for aboves loop
 		//patients.addAll(ps.getAllPatients(false));
 		logger.info("added patients: " + patients.size());
 		// patients.addAll(ps.getPatients(null, "NNO 1", identifierTypes,
 		// true));
 		// doesn't work ???
 		// List<Patient> patients = ps.getPatients(null, "NNO%",
 		// Arrays.asList(ps.getPatientIdentifierTypeByName("ARV Number"))),
 		// false);
 		return patients;
 	}
 	
 	private SessionFactory sessionFactory() {
 		return null;
 		//		return ((HibernatePihMalawiQueryDao) Context.getRegisteredComponents(
 		//			HibernatePihMalawiQueryDao.class).get(0)).getSessionFactory();
 	}
 	
 }
