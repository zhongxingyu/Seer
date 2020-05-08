 /**
  * @author dgeorge
  * 
 * $Id: AnimalModelManagerImpl.java,v 1.26 2005-10-06 20:41:51 schroedn Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.25  2005/10/06 19:33:10  pandyas
  * modified for Therapy screen
  *
  * Revision 1.24  2005/10/06 13:36:09  georgeda
  * Changed ModelCharacteristics interface to be consistent w/ the rest of the interfaces
  *
  * Revision 1.23  2005/10/05 15:17:48  schroedn
  * SpontaneousMutation create and edit now working
  *
  * Revision 1.22  2005/10/04 20:12:52  schroedn
  * Added Spontaneous Mutation, InducedMutation, Histopathology, TargetedModification and GenomicSegment
  *
  * Revision 1.21  2005/10/03 13:51:36  georgeda
  * Search changes
  *
  * Revision 1.20  2005/09/30 18:59:06  pandyas
  * modified for cell line
  *
  * Revision 1.19  2005/09/28 21:20:02  georgeda
  * Finished up converting to new manager
  *
  * Revision 1.18  2005/09/28 15:12:29  schroedn
  * Added GeneDelivery and Xenograft/Transplant, businass logic in Managers
  *
  * Revision 1.17  2005/09/28 14:14:00  schroedn
  * Added saveXenograft and saveGeneDelivery
  *
  * Revision 1.16  2005/09/28 12:46:12  georgeda
  * Cleanup of animal manager
  *
  * Revision 1.15  2005/09/27 19:17:16  georgeda
  * Refactor of CI managers
  *
  * Revision 1.14  2005/09/27 16:44:49  georgeda
  * Added ChemicalDrug handling
  * Revision 1.13  2005/09/26 14:04:36  georgeda
  * Cleanup for cascade fix and common manager code
  *
  * Revision 1.12  2005/09/23 14:55:19  georgeda
  * Made SexDistribution a reference table
  *
  * Revision 1.11  2005/09/22 18:55:53  georgeda
  * Get coordinator from user in properties file
  *
  * Revision 1.10  2005/09/19 18:13:51  georgeda
  * Changed boolean to Boolean
  *
  * Revision 1.9  2005/09/19 12:55:24  georgeda
  * Handle empty sex distribution table
  *
  * Revision 1.8  2005/09/16 15:52:57  georgeda
  * Changes due to manager re-write
  *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Availability;
 import gov.nih.nci.camod.domain.CellLine;
 import gov.nih.nci.camod.domain.ContactInfo;
 import gov.nih.nci.camod.domain.GeneDelivery;
 import gov.nih.nci.camod.domain.GenomicSegment;
 import gov.nih.nci.camod.domain.InducedMutation;
 import gov.nih.nci.camod.domain.Log;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.domain.Phenotype;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.domain.SpontaneousMutation;
 import gov.nih.nci.camod.domain.TargetedModification;
 import gov.nih.nci.camod.domain.Taxon;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.domain.Xenograft;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.CellLineData;
 import gov.nih.nci.camod.webapp.form.ChemicalDrugData;
 import gov.nih.nci.camod.webapp.form.EnvironmentalFactorData;
 import gov.nih.nci.camod.webapp.form.GeneDeliveryData;
 import gov.nih.nci.camod.webapp.form.GenomicSegmentData;
 import gov.nih.nci.camod.webapp.form.GrowthFactorData;
 import gov.nih.nci.camod.webapp.form.HormoneData;
 import gov.nih.nci.camod.webapp.form.InducedMutationData;
import gov.nih.nci.camod.webapp.form.ModelCharacteristics;
 import gov.nih.nci.camod.webapp.form.NutritionalFactorData;
 import gov.nih.nci.camod.webapp.form.RadiationData;
 import gov.nih.nci.camod.webapp.form.SearchData;
 import gov.nih.nci.camod.webapp.form.SpontaneousMutationData;
 import gov.nih.nci.camod.webapp.form.SurgeryData;
 import gov.nih.nci.camod.webapp.form.TargetedModificationData;
 import gov.nih.nci.camod.webapp.form.ViralTreatmentData;
 import gov.nih.nci.camod.webapp.form.XenograftData;
 import gov.nih.nci.common.persistence.Persist;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.HibernateUtil;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 /**
  * Manages fetching/saving/updating of animal models
  */
 public class AnimalModelManagerImpl extends BaseManager implements AnimalModelManager {
 
     /**
      * Get all of the animal models in the DB
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public List getAll() throws Exception {
         log.trace("In AnimalModelManagerImpl.getAll");
         return super.getAll(AnimalModel.class);
     }
 
     /**
      * Get all of the animal models submitted by a username
      * 
      * @param inUsername
      *            the username the models are submitted by
      * 
      * @return the list of animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public List getAllByUser(String inUsername) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.getAllByUser");
 
         // The list of AnimalModels to be returned
         List theAnimalModels = null;
 
         // The following two objects are needed for eQBE.
         AnimalModel theAnimalModel = new AnimalModel();
         Person theSubmitter = new Person();
         theSubmitter.setUsername(inUsername);
         theAnimalModel.setSubmitter(theSubmitter);
 
         try {
             theAnimalModels = Search.query(theAnimalModel);
         } catch (Exception e) {
             log.error("Exception occurred in getAll", e);
             throw e;
         }
 
         log.trace("Exiting AnimalModelManagerImpl.getAllByUser");
 
         return theAnimalModels;
     }
 
     /**
      * Get all of the animal models of a specific state
      * 
      * @param inState
      *            the state to query for
      * 
      * @return the list of animal models
      * 
      * @exception Exception
      *                if an error occurred
      */
     public List getAllByState(String inState) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.getAllByState");
 
         // The list of AnimalModels to be returned
         List theAnimalModels = new ArrayList();
 
         // The following two objects are needed for eQBE.
         AnimalModel theAnimalModel = new AnimalModel();
         theAnimalModel.setState(inState);
 
         try {
             theAnimalModels = Search.query(theAnimalModel);
         } catch (Exception e) {
             log.error("Exception occurred in getAllByState", e);
             throw e;
         }
 
         log.trace("Exiting AnimalModelManagerImpl.getAllByState");
 
         return theAnimalModels;
     }
 
     /**
      * Get a specific animal model
      * 
      * @param id
      *            The unique id for the model
      * 
      * @return the animal model if found, null otherwise
      * @throws Exception
      * 
      * @exception Exception
      *                if an error occurred
      */
     public AnimalModel get(String id) throws Exception {
         log.trace("In AnimalModelManagerImpl.get");
         return (AnimalModel) super.get(id, AnimalModel.class);
     }
 
     /**
      * Save an animal model
      * 
      * @param id
      *            The unique id for the model
      * 
      * @exception Exception
      *                if an error occurred
      */
     public void save(AnimalModel inAnimalModel) throws Exception {
         log.trace("In AnimalModelManagerImpl.save");
         super.save(inAnimalModel);
     }
 
     /**
      * Update an animal model and create an associated log entry
      * 
      * @param id
      *            The unique id for the model
      * @throws Exception
      * 
      * @exception Exception
      *                if an error occurred
      */
     public void updateAndAddLog(AnimalModel inAnimalModel, Log inLog) throws Exception {
 
         log.trace("Entering updateAndAddLog");
 
         try {
 
             // Make sure they get saved together
             HibernateUtil.beginTransaction();
             Persist.save(inAnimalModel);
             Persist.save(inLog);
             HibernateUtil.commitTransaction();
 
         } catch (PersistenceException pe) {
             HibernateUtil.rollbackTransaction();
             log.error("PersistenceException in save", pe);
             throw pe;
         } catch (Exception e) {
             HibernateUtil.rollbackTransaction();
             log.error("Exception in save", e);
             throw e;
         }
 
         log.trace("Exiting updateAndAddLog");
     }
 
     /**
      * Create a new/unsaved animal model
      * 
      * @param inModelCharacteristicsData
      *            The values for the model and associated objects
      * 
      * @param inUsername
      *            The submitter
      * 
      * @return the created and unsaved AnimalModel
      * @throws Exception
      */
     public AnimalModel create(ModelCharacteristicsData inModelCharacteristicsData, String inUsername) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.create");
 
         AnimalModel theAnimalModel = new AnimalModel();
 
         log.trace("Exiting AnimalModelManagerImpl.create");
         return populateAnimalModel(inModelCharacteristicsData, inUsername, theAnimalModel);
     }
 
     /**
      * Update the animal model w/ the new characteristics and save
      * 
      * @param inModelCharacteristicsData
      *            The new values for the model and associated objects
      * 
      * @param inAnimalModel
      *            The animal model to update
      */
     public void update(ModelCharacteristicsData inModelCharacteristicsData, AnimalModel inAnimalModel) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.update");
         log.debug("Updating animal model: " + inAnimalModel.getId());
 
         // Populate w/ the new values and save
         inAnimalModel = populateAnimalModel(inModelCharacteristicsData, null, inAnimalModel);
         save(inAnimalModel);
 
         log.trace("Exiting AnimalModelManagerImpl.update");
     }
 
     /**
      * Remove the animal model from the system. Should remove all associated
      * data as well
      * 
      * @param id
      *            The unique id of the animal model to delete
      * 
      * @throws Exception
      *             An error occurred when attempting to delete the model
      */
     public void remove(String id) throws Exception {
         log.trace("In AnimalModelManagerImpl.remove");
         super.remove(id, AnimalModel.class);
     }
 
     /**
      * Search for animal models based on: - modelName - piName - siteOfTumor -
      * speciesName
      * 
      * Note: This method is currently a dummy search method and simply returns
      * all the animal model objects in the database. Searching using eQBE needs
      * to be done.
      * 
      * @throws Exception
      */
     public List search(SearchData inSearchData) throws Exception {
 
         log.trace("In search");
         return QueryManagerSingleton.instance().searchForAnimalModels(inSearchData);
     }
 
     // Populate the model based on the model characteristics form passed in. It
     // will update associated
     // object if they exist, create them if they don't.
     private AnimalModel populateAnimalModel(ModelCharacteristicsData inModelCharacteristics, String inUsername,
             AnimalModel inAnimalModel) throws Exception {
 
         log.trace("Entering populateAnimalModel");
 
         // Handle the person information
         if (inUsername != null) {
             Person thePerson = PersonManagerSingleton.instance().getByUsername(inUsername);
             if (thePerson == null) {
 
                 // Create a new person
                 thePerson = new Person();
                 thePerson.setUsername(inUsername);
                 thePerson.setIsPrincipalInvestigator(new Boolean(true));
 
                 // Add the contact information
                 ContactInfo theContactInfo = new ContactInfo();
                 theContactInfo.setEmail(inModelCharacteristics.getEmail());
                 thePerson.addContactInfo(theContactInfo);
             }
 
             System.out.println("The person: " + thePerson.getUsername());
             inAnimalModel.setSubmitter(thePerson);
 
             // Change this to match the real PI
             inAnimalModel.setPrincipalInvestigator(thePerson);
         }
 
         // Set the animal model information
         boolean isToolMouse = inModelCharacteristics.getIsToolMouse().equals("yes") ? true : false;
         inAnimalModel.setIsToolMouse(new Boolean(isToolMouse));
         inAnimalModel.setUrl(inModelCharacteristics.getUrl());
         inAnimalModel.setModelDescriptor(inModelCharacteristics.getModelDescriptor());
         inAnimalModel.setExperimentDesign(inModelCharacteristics.getExperimentDesign());
 
         // Create/reuse the taxon
         //Taxon theTaxon = inAnimalModel.getSpecies();
         //if (theTaxon == null) {
         //    theTaxon = new Taxon();
         // }
         //theTaxon.setScientificName(inModelCharacteristics.getScientificName());
         //theTaxon.setEthnicityStrain(inModelCharacteristics.getEthinicityStrain());
         
         // Find the matching taxon in the db and reuse it
         Taxon theTaxon = new Taxon();
         List taxonList = (List) TaxonManagerSingleton.instance().getAll(  );
         
         for( int i=0; i < taxonList.size(); i++ ) {
         	theTaxon = (Taxon) taxonList.get(i);
         	if ( theTaxon.getEthnicityStrain() != null ) {
 	        	if ( theTaxon.getEthnicityStrain().equals( inModelCharacteristics.getEthinicityStrain() ))
 	        		break;
         	}
         }
      
         // Problem when editing and this doesn't change, the admin will get two
         // emails about the same STRAIN
         if (inModelCharacteristics.getEthnicityStrainUnctrlVocab() != null) {
             if (!inModelCharacteristics.getEthnicityStrainUnctrlVocab().equals("")) {
 
                 log.trace("Sending Notification eMail - new EthinicityStrain added");
 
                 ResourceBundle theBundle = ResourceBundle.getBundle("camod");
 
                 // Iterate through all the reciepts in the config file
                 String recipients = theBundle.getString(Constants.EmailMessage.RECIPIENTS);
                 StringTokenizer st = new StringTokenizer(recipients, ",");
                 String inRecipients[] = new String[st.countTokens()];
 
                 for (int i = 0; i < inRecipients.length; i++)
                     inRecipients[i] = st.nextToken();
 
                 String inSubject = theBundle.getString(Constants.EmailMessage.SUBJECT);
                 String inMessage = theBundle.getString(Constants.EmailMessage.MESSAGE) + " Strain added ( "
                         + inModelCharacteristics.getEthnicityStrainUnctrlVocab() + " ) and is awaiting your approval.";
                 String inFrom = theBundle.getString(Constants.EmailMessage.FROM);
                 // String inSender =
                 // theBundle.getString(Constants.EmailMessage.SENDER);
 
                 // Send the email
                 try {
                     MailUtil.sendMail(inRecipients, inSubject, inMessage, inFrom);
                 } catch (Exception e) {
                     System.out.println("Caught exception" + e);
                     e.printStackTrace();
                 }
 
                 // 2. Set flag, this Strain will need to be approved before
                 // being added the list
                 theTaxon.setEthnicityStrainUnctrlVocab(inModelCharacteristics.getEthnicityStrainUnctrlVocab());
             }
         }
 
         Phenotype thePhenotype = inAnimalModel.getPhenotype();
         if (thePhenotype == null) {
             thePhenotype = new Phenotype();
         }
 
         // Get/create the sex distribution
         SexDistribution theSexDistribution = SexDistributionManagerSingleton.instance().getByType(
                 inModelCharacteristics.getType());
         if (theSexDistribution == null) {
             theSexDistribution = new SexDistribution();
             theSexDistribution.setType(inModelCharacteristics.getType());
         }
 
         // Create the phenotype
         thePhenotype.setDescription(inModelCharacteristics.getDescription());
         thePhenotype.setBreedingNotes(inModelCharacteristics.getBreedingNotes());
         thePhenotype.setSexDistribution(theSexDistribution);
 
         // Get the availability
         Availability theAvailability = inAnimalModel.getAvailability();
 
         // When the model was created
         if (theAvailability == null) {
             theAvailability = new Availability();
         }
         theAvailability.setEnteredDate(new Date());
 
         if (inModelCharacteristics.getReleaseDate().equals("immediately")) {
             theAvailability.setReleaseDate(new Date());
         } else {
             // TODO: add popup calender to submitNewModel.jsp and convert the
             // string to Date object here
             // modelChar.getCalendarReleaseDate();
             theAvailability.setReleaseDate(new Date());
         }
 
         // By default a new model's state is set to incomplete if it's not set
         if (inAnimalModel.getState() == null) {
             inAnimalModel.setState("Incomplete");
         }
 
         // Associated the created objects
         inAnimalModel.setAvailability(theAvailability);
         inAnimalModel.setPhenotype(thePhenotype);
         inAnimalModel.setSpecies(theTaxon);
 
         log.trace("Exiting populateAnimalModel");
 
         return inAnimalModel;
     }
 
     public void addXenograft(AnimalModel inAnimalModel, XenograftData inXenograftData) throws Exception {
 
         log.trace("Entering saveXenograft");
 
         Xenograft theXenograft = XenograftManagerSingleton.instance().create(inXenograftData, inAnimalModel);
 
         inAnimalModel.addXenograft(theXenograft);
         save(inAnimalModel);
 
         log.trace("Exiting saveXenograft");
     }
 
     /**
      * Add a gene delivery
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inGeneDeliveryData
      *            the gene delivery
      * @throws Exception
      */
     public void addGeneDelivery(AnimalModel inAnimalModel, GeneDeliveryData inGeneDeliveryData) throws Exception {
 
         log.trace("Entering addGeneDelivery");
 
         GeneDelivery theGeneDelivery = GeneDeliveryManagerSingleton.instance().create(inGeneDeliveryData);
 
         inAnimalModel.addGeneDelivery(theGeneDelivery);
         save(inAnimalModel);
 
         log.trace("Exiting addGeneDelivery");
     }
 
     /**
      * Add a chemical/drug therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inChemicalDrugData
      *            the new chemical drug data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, ChemicalDrugData inChemicalDrugData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inChemicalDrugData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inEnvironmentalFactorData
      *            the ef data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, EnvironmentalFactorData inEnvironmentalFactorData)
             throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inEnvironmentalFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inRadiationData
      *            the new radiation data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, RadiationData inRadiationData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inRadiationData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an environmental factor therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inViralTreatmentData
      *            the new viral treatment data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, ViralTreatmentData inViralTreatmentData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inViralTreatmentData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a growth factor
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inGrowthFactorData
      *            the new growth factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, GrowthFactorData inGrowthFactorData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inGrowthFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a hormone
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inHormoneData
      *            the new growth factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, HormoneData inHormoneData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inHormoneData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a nutritional factor
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inNutritionalFactorData
      *            the new nutrional factor data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, NutritionalFactorData inNutritionalFactorData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inNutritionalFactorData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a surgery/other
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inSurgeryData
      *            the new surgery data
      * @throws Exception
      */
     public void addTherapy(AnimalModel inAnimalModel, SurgeryData inSurgeryData) throws Exception {
 
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inSurgeryData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add a cell line
      * 
      * @param inAnimalModel
      *            the animal model that has the cell line
      * @param inSurgeryData
      *            the new cell line data
      * @throws Exception
      */
     public void addCellLine(AnimalModel inAnimalModel, CellLineData inCellLineData) throws Exception {
 
         log.trace("Entering saveCellLine");
 
         CellLine theCellLine = CellLineManagerSingleton.instance().create(inCellLineData, inAnimalModel);
 
         inAnimalModel.addCellLine(theCellLine);
         save(inAnimalModel);
 
         log.trace("Exiting saveCellLine");
     }     
     
     /**
      * Add a SpontaneousMutation
      */   
     public void addGeneticDescription(AnimalModel inAnimalModel, SpontaneousMutationData inSpontaneousMutationData) throws Exception {
 
         log.trace( "Entering addGeneticDescription (spontaneousMutation)" );
 
         SpontaneousMutation theSpontaneousMutation = SpontaneousMutationManagerSingleton.instance().create( inSpontaneousMutationData );
         System.out.println(theSpontaneousMutation.getName() );
         
         inAnimalModel.addSpontaneousMutation( theSpontaneousMutation );
         save( inAnimalModel );
 
         log.trace("Exiting addGeneticDescription (spontaneousMutation)");
     }         
     
     /**
      * Add a InducedMutation 
      */   
     public void addGeneticDescription(AnimalModel inAnimalModel, InducedMutationData inInducedMutationData) throws Exception {
 
         log.trace( "Entering addGeneticDescription (inducedMutation)" );
 
         InducedMutation theInducedMutation = InducedMutationManagerSingleton.instance().create( inInducedMutationData );        
         inAnimalModel.addEngineeredGene( theInducedMutation );
         save( inAnimalModel );
 
         log.trace("Exiting addGeneticDescription (inducedMutation)");
     }   
     
     /**
      * Add a TargetedModification 
      */   
     public void addGeneticDescription(AnimalModel inAnimalModel, TargetedModificationData inTargetedModificationData) throws Exception {
 
         log.trace( "Entering addGeneticDescription (TargetedModification)" );
 
         TargetedModification theTargetedModification = TargetedModificationManagerSingleton.instance().create( inTargetedModificationData );
         //System.out.println(theGene.getName() );
 
         inAnimalModel.addEngineeredGene( theTargetedModification );
         save( inAnimalModel );
 
         log.trace("Exiting addGeneticDescription (TargetedModification)");
     }
     
     public void addGeneticDescription(AnimalModel inAnimalModel, GenomicSegmentData inGenomicSegmentData) throws Exception {
     	
         log.trace( "Entering addGeneticDescription (GenomicSegment)" );
 
         GenomicSegment theGenomicSegment = GenomicSegmentManagerSingleton.instance().create( inGenomicSegmentData );
         System.out.println(theGenomicSegment.getName() );
         
         //inAnimalModel.addEngineeredGene( theGenomicSegment );
         //save( inAnimalModel );
 
         log.trace("Exiting addGeneticDescription (GenomicSegment)");
     }
     
     /**
      * Add a therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the therapy
      * @param inChemicalDrugData
      *            the new therapy data
      * @throws Exception
      */
      
     public void addTherapy(AnimalModel inAnimalModel, TherapyData inTherapyData) throws Exception {
     	
     	System.out.println( "<AnimalModelManagerImpl addTherapy>");
     	   
         log.trace("Entering AnimalModelManagerImpl.addTherapy");
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inTherapyData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
         log.trace("Exiting AnimalModelManagerImpl.addTherapy");
     }     
 }
