 /**
  * @author dgeorge
  * 
 * $Id: TherapyManagerImpl.java,v 1.28 2008-02-11 18:00:08 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.27  2007/10/31 19:11:04  pandyas
 * Fixed #8188 	Rename UnctrlVocab items to text entries
 *
  * Revision 1.26  2007/09/12 19:36:03  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.25  2007/05/16 12:29:10  pandyas
  * Added developmental stage evs tree to Therapy when species is Zebrafsih
  *
  * Revision 1.24  2006/08/17 18:17:35  pandyas
  * Defect# 410: Externalize properties files - Code changes to get properties
  *
  * Revision 1.23  2006/05/10 14:14:33  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.22  2006/04/19 17:38:26  pandyas
  * Removed TODO text
  *
  * Revision 1.21  2006/04/18 17:54:44  pandyas
  * Fixed Therapy multi-select collections and cleaned up e-mail code
  *
  * Revision 1.20  2006/04/17 19:11:06  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.19  2005/12/29 18:28:05  pandyas
  * Clean up - removed code for TumorResponse
  *
  * Revision 1.18  2005/11/28 13:46:53  georgeda
  * Defect #207, handle nulls for pages w/ uncontrolled vocab
  *
  * Revision 1.17  2005/11/18 22:50:02  georgeda
  * Defect #184.  Cleanup editing of old models
  *
  * Revision 1.16  2005/11/16 15:31:05  georgeda
  * Defect #41. Clean up of email functionality
  *
  * Revision 1.15  2005/11/14 14:19:22  georgeda
  * Cleanup
  *
  * Revision 1.14  2005/11/09 00:17:16  georgeda
  * Fixed delete w/ constraints
  *
  * Revision 1.13  2005/11/02 19:02:55  pandyas
  * Added e-mail functionality
  *
  * Revision 1.12  2005/10/26 14:10:48  georgeda
  * Added other administrative route to therapy
  *
  * Revision 1.11  2005/10/25 19:42:15  georgeda
  * Finished Therapy page
  *
  * Revision 1.10  2005/10/19 19:26:35  pandyas
  * added admin route to growth factor
  *
  * Revision 1.9  2005/10/18 21:59:34  pandyas
  * fixed other field
  *
  * Revision 1.8  2005/10/11 16:45:47  pandyas
  * fixed tumor response dose unit retrieval that was not working
  *
  * Revision 1.7  2005/10/06 19:30:22  pandyas
  * modified for Therapy screen
  *
  * Revision 1.6  2005/09/28 21:20:01  georgeda
  * Finished up converting to new manager
  *
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.TherapyManager;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.*;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Implementation of the TherapyManager interface. Creates/saves/updates the
  * different types of therapies based on the specific interface passed in
  */
 public class TherapyManagerImpl extends BaseManager implements TherapyManager {
 	/**
 	 * Get a specific Therapy by id
 	 * 
 	 * @param id
 	 *            the unique id for a therapy
 	 * 
 	 * @return the matching Therapy object, or null if not found.
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public Therapy get(String id) throws Exception {
 		log.debug("In TherapyManagerImpl.get");
 		return (Therapy) super.get(id, Therapy.class);
 	}
 
 	/**
 	 * Save Therapy
 	 * 
 	 * @param therapy
 	 *            the therapy to save
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void save(Therapy therapy) throws Exception {
 		log.debug("In TherapyManagerImpl.save");
 		super.save(therapy);
 	}
 
 	/**
 	 * Remove a specific Therapy by id
 	 * 
 	 * @param id
 	 *            the unique id for a therapy
 	 * 
 	 * @exception Exception
 	 *                when anything goes wrong.
 	 */
 	public void remove(String id, AnimalModel inAnimalModel) throws Exception {
 		log.debug("In TherapyManagerImpl.remove");
 
 		Therapy theTherapy = get(id);
 
 		inAnimalModel.getTherapyCollection().remove(theTherapy);
 		super.save(inAnimalModel);
 	}
 
 	private void populateAgeGender(TherapyData inTherapyData, Therapy theTherapy) {
 
 		log.debug("In TherapyManagerImpl.populateAgeGender");
 
 		// Set the treatment
 		Treatment theTreatment = theTherapy.getTreatment();
 		if (theTreatment == null) {
 			theTreatment = new Treatment();
 			theTherapy.setTreatment(theTreatment);
 		}
 
 		// Set the gender
 		SexDistribution sexDistribution = SexDistributionManagerSingleton
 				.instance().getByType(inTherapyData.getType());
 
 		// save the treatment
 		theTreatment.setSexDistribution(sexDistribution);
 
 		theTreatment.setAgeAtTreatment(inTherapyData.getAgeAtTreatment());
 		theTreatment.setAgeAtTreatmentUnit(inTherapyData
 				.getAgeAtTreatmentUnit());
 	}
 
 	private void populateDose(TherapyData inTherapyData, Therapy theTherapy) {
 		log.debug("Entering populateDose");
 
 		// Set the treatment
 		Treatment theTreatment = theTherapy.getTreatment();
 		if (theTreatment == null) {
 			theTreatment = new Treatment();
 			theTherapy.setTreatment(theTreatment);
 		}
 
 		theTreatment.setDosage(inTherapyData.getDosage());
 		theTreatment.setDosageUnit(inTherapyData.getDosageUnit());
 	}
 	
 	private void populateDevelopmentalStage(TherapyData inTherapyData, Therapy theTherapy) 
 							throws Exception {
 		log.debug("Entering populateDevelopmentalStage");
 
 		if (inTherapyData.getDevelopmentalStageCode() != null && inTherapyData.getDevelopmentalStageCode().length() > 0) {
 			log.debug("inTherapyData.getDevelopmentalStageCode(): "
 					+ inTherapyData.getDevelopmentalStageCode());
 			// Get or create DevelopmentalStage object each time
 			log.debug("getOrCreate method used");
 			DevelopmentalStage theDevelopmentalStage = DevelopmentalStageManagerSingleton.instance().getOrCreate(
 					inTherapyData.getDevelopmentalStageCode(),
 					inTherapyData.getDevelopmentalStageName());
 			theTherapy.setDevelopmentalStage(theDevelopmentalStage);
 		} else {
 			// Null out - not required field
 			theTherapy.setDevelopmentalStage(null);
 		}
 	}	
 
 	private void populateAdministration(AnimalModel inAnimalModel,
 			TherapyData inTherapyData, Therapy theTherapy) {
 
 		log.debug("In CarcinogenExposureManagerImpl.populateAdministration");
 
 		if (inTherapyData.getAdministrativeRoute() != null
 				&& inTherapyData.getAdministrativeRoute().length() > 0) {
 			// Set the treatment
 			Treatment theTreatment = theTherapy.getTreatment();
 			if (theTreatment == null) {
 				theTreatment = new Treatment();
 				theTherapy.setTreatment(theTreatment);
 			}
 		}
 
 		/* Set other adminstrative route or selected adminstrative route */
 		// anytime admin route is other
 		if (inTherapyData.getAdministrativeRoute().equals(
 				Constants.Dropdowns.OTHER_OPTION)) {
 			log.debug("admin route equals other");
 			// Do not save 'Other' in the database
 			theTherapy.getTreatment().setAdminRouteAlternEntry(
 					inTherapyData.getOtherAdministrativeRoute());
 
 			log
 					.trace("Sending Notification eMail - new Administrative Route added");
 			sendEmail(inAnimalModel, inTherapyData
 					.getOtherAdministrativeRoute(), " AdministrativeRoute");
 
 		} else if (inTherapyData.getAdministrativeRoute() != null) {
 			theTherapy.getTreatment().setAdministrativeRoute(
 					inTherapyData.getAdministrativeRoute());
 		}
 
 		Agent theAgent = theTherapy.getAgent();
 		if (theAgent == null) {
 			theAgent = new Agent();
 			theTherapy.setAgent(theAgent);
 		}
 	}
 
 	/**
 	 * Create a therapy object with the correct data filled in.
 	 * 
 	 * @param inTherapyData
 	 *            the interface to create the therapy object from
 	 * 
 	 * @returns a therapy
 	 */
 	public Therapy create(AnimalModel inAnimaModel, TherapyData inTherapyData)
 			throws Exception {
 
 		log.debug("In TherapyManagerImpl.create Entering");
 
 		Therapy theTherapy = new Therapy();
 
 		populateAgeGender(inTherapyData, theTherapy);
 		populateDose(inTherapyData, theTherapy);
 		populateAdministration(inAnimaModel, inTherapyData, theTherapy);
 		populateDevelopmentalStage(inTherapyData, theTherapy);		
 		populateTherapy(inTherapyData, theTherapy);
 
 		log.debug("In TherapyManagerImpl.create Exiting");
 		return theTherapy;
 	}
 
 	public void update(AnimalModel inAnimalModel, TherapyData inTherapyData,
 			Therapy inTherapy) throws Exception {
 
 		log.debug("In TherapyManagerImpl.update");
 
 		populateAgeGender(inTherapyData, inTherapy);
 		populateDose(inTherapyData, inTherapy);
 		populateAdministration(inAnimalModel, inTherapyData, inTherapy);
 		populateDevelopmentalStage(inTherapyData, inTherapy);		
 		populateTherapy(inTherapyData, inTherapy);
 
 		save(inTherapy);
 
 	}
 
 	private void populateTherapy(TherapyData inTherapyData, Therapy theTherapy)
 			throws Exception {
 
 		log.debug("Entering populateTherapy Entering");
 
 		/* populateName method without otherName */
 
 		// Set the treatment
 		Treatment theTreatment = theTherapy.getTreatment();
 		if (theTreatment == null) {
 			theTreatment = new Treatment();
 			theTherapy.setTreatment(theTreatment);
 		}
 		log.debug("populateTherapy Set the treatment");
 
 		// Set the agent name
 		Agent theAgent = theTherapy.getAgent();
 		if (theAgent == null) {
 			theAgent = new Agent();
 			theTherapy.setAgent(theAgent);
 		}
 		theAgent.setName(inTherapyData.getName());
 		log.debug("populateTherapy setName");
 
 		// Set NSC and CAS
 		String theNscNumber = inTherapyData.getNscNumber().trim();
 		if (theNscNumber != null && theNscNumber.length() > 0) {
 			try {
 				theAgent.setNscNumber(Long.valueOf(theNscNumber));
 			} catch (NumberFormatException e) {
 				log.error("Bad NSC number: " + theNscNumber);
 			}
		} else {
			theAgent.setNscNumber(null);
 		}
 		log.debug("populateTherapy setNscNumber");
 
 		String theCasNumber = inTherapyData.getCasNumber().trim();
 		if (theCasNumber != null && theCasNumber.length() > 0) {
 			theAgent.setCasNumber(theCasNumber);
		} else {
			theAgent.setCasNumber(null);
 		}
 		log.debug("populateTherapy setCasNumber");
 
 		// Therapy object attributes
 		theTherapy.setToxicityGrade(inTherapyData.getToxicityGrade());
 		theTherapy.setBiomarker(inTherapyData.getBiomarker());
 		theTherapy.setExperiment(inTherapyData.getExperiment());
 		theTherapy.setResults(inTherapyData.getResults());
 		theTherapy.setTumorResponse(inTherapyData.getTumorResponse());
 		theTherapy.setComments(inTherapyData.getComments());
 		log.debug("populateTherapy set Therapy object attributes");
 
 		// Get the ChemicalClass
 		String[] theChemicalClasses = inTherapyData
 				.getSelectedChemicalClasses();
 		log.debug("theChemicalClasses.length: " + theChemicalClasses.length);
 		Set<ChemicalClass> theCurrentChemicalClassSet = theTherapy.getAgent()
 				.getChemicalClassCollection();
 		theCurrentChemicalClassSet.clear();
 
 		if (theChemicalClasses != null) {
 			for (int i = 0; i < theChemicalClasses.length; i++) {
 				ChemicalClass theChemicalClass = ChemicalClassManagerSingleton
 						.instance().getByName(theChemicalClasses[i]);
 				if (theChemicalClass == null) {
 					log.error("Unknown chemical class name: "
 							+ theChemicalClasses[i]);
 				} else {
 					theCurrentChemicalClassSet.add(theChemicalClass);
 				}
 			}
 		}
 
 		// Get the biological process
 		String[] theProcesses = inTherapyData.getSelectedProcesses();
 		Set<BiologicalProcess> theCurrentProcessSet = theTherapy.getAgent()
 				.getBiologicalProcessCollection();
 		theCurrentProcessSet.clear();
 
 		if (theProcesses != null) {
 			for (int i = 0; i < theProcesses.length; i++) {
 				BiologicalProcess theBiologicalProcess = BiologicalProcessManagerSingleton
 						.instance().getByName(theProcesses[i]);
 				if (theBiologicalProcess == null) {
 					log.error("Unknown biological process name: "
 							+ theProcesses[i]);
 				} else {
 					theCurrentProcessSet.add(theBiologicalProcess);
 				}
 			}
 		}
 
 		// Get the agent target
 		String[] theTargets = inTherapyData.getSelectedTargets();
 		Set<AgentTarget> theCurrentAgentSet = theTherapy.getAgent()
 				.getAgentTargetCollection();
 		theCurrentAgentSet.clear();
 
 		if (theTargets != null) {
 			for (int i = 0; i < theTargets.length; i++) {
 				AgentTarget theAgentTarget = AgentTargetManagerSingleton
 						.instance().getByName(theTargets[i]);
 				if (theAgentTarget == null) {
 					log.error("Unknown agent target name: " + theTargets[i]);
 				} else {
 					theCurrentAgentSet.add(theAgentTarget);
 				}
 			}
 		}
 
 		log.debug("Exiting populateTherapy Exiting");
 	}
 
 	private void sendEmail(AnimalModel inAnimalModel,
 			String theUncontrolledVocab, String inType) {
 		// Get the e-mail resource
 		Properties camodProperties = new Properties();
 		String camodPropertiesFileName = null;
 
 		camodPropertiesFileName = System
 				.getProperty("gov.nih.nci.camod.camodProperties");
 
 		try {
 
 			FileInputStream in = new FileInputStream(camodPropertiesFileName);
 			camodProperties.load(in);
 
 		} catch (FileNotFoundException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error("Caught exception finding file for properties: ", e);
 			e.printStackTrace();
 		}
 
 		// Iterate through all the reciepts in the config file
 		String recipients = UserManagerSingleton.instance()
 				.getEmailForCoordinator();
 		StringTokenizer st = new StringTokenizer(recipients, ",");
 		String inRecipients[] = new String[st.countTokens()];
 		for (int i = 0; i < inRecipients.length; i++) {
 			inRecipients[i] = st.nextToken();
 		}
 
 		String inSubject = camodProperties
 				.getProperty("model.new_unctrl_vocab_subject");
 		String inFrom = inAnimalModel.getSubmitter().getEmailAddress();
 
 		// gather message keys and variable values to build the e-mail
 		// content with
 		String[] messageKeys = { Constants.Admin.NONCONTROLLED_VOCABULARY };
 		Map<String, Object> values = new TreeMap<String, Object>();
 		values.put("type", inType);
 		values.put("value", theUncontrolledVocab);
 		values.put("submitter", inAnimalModel.getSubmitter());
 		values.put("model", inAnimalModel.getModelDescriptor());
 		values.put("modelstate", inAnimalModel.getState());
 
 		// Send the email
 		try {
 			MailUtil.sendMail(inRecipients, inSubject, "", inFrom, messageKeys,
 					values);
 		} catch (Exception e) {
 			log.error("Caught exception sending mail: ", e);
 			e.printStackTrace();
 		}
 	}
 }
