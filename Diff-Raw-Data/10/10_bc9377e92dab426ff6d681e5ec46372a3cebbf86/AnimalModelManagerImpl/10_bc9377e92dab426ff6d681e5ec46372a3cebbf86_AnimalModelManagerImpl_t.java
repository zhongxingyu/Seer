 /**
  * @author dgeorge
  * 
 * $Id: AnimalModelManagerImpl.java,v 1.96 2008-08-14 17:41:54 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.95  2008/08/14 17:18:21  pandyas
 * remove debug lines
 *
  * Revision 1.94  2008/08/14 16:32:05  pandyas
  * remove debug lines
  *
  * Revision 1.93  2008/08/14 06:25:35  schroedn
  * Added Microarray
  *
  * Revision 1.92  2008/07/11 17:44:42  schroedn
  * Bug 5200
  * Status of duplicate model
  *
  * Revision 1.91  2008/05/21 19:03:56  pandyas
  * Modified advanced search to prevent SQL injection
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.90  2008/01/31 22:22:27  pandyas
  * remove log printouts now that bug is resolved
  *
  * Revision 1.89  2008/01/28 18:44:55  pandyas
  * Modified to debug instability in base manager and animal model manager get method
  *
  * Revision 1.88  2008/01/18 21:26:13  pandyas
  * enabled debug statements for dev testing
  *
  * Revision 1.87  2008/01/16 18:30:22  pandyas
  * Renamed value to Transplant for #8290
  *
  * Revision 1.86  2007/10/31 18:56:38  pandyas
  * Fixed #8290 	Rename graft object into transplant object
  * Fixed #8355 	Add comments field to every submission page
  * Fixed #8188 	Rename UnctrlVocab items to text entries
  *
  * Revision 1.85  2007/09/12 19:36:03  pandyas
  * modified debug statements for build to stage tier
  *
  * Revision 1.84  2007/07/31 12:02:28  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.83  2007/04/30 20:09:13  pandyas
  * Implemented species specific vocabulary trees from EVSTree
  * Added code to sendMail for other disease for Zebrafish
  *
  * Revision 1.82  2007/03/26 12:01:11  pandyas
  * caMOd 2.3 enhancements for Zebrafish support
  *
  * Revision 1.81  2007/02/23 21:26:07  pandyas
  * Fixed Genotype and Nomenclature - split objects and cleaned up database
  *
  * Revision 1.80  2007/02/21 18:53:58  pandyas
  * Still working on nomenclature bug - two entries are in Db, new one displays (better but not right yet)
  *
  * Revision 1.79  2007/02/21 13:11:15  pandyas
  * Fixed Nomenclature save - clear existing Nomenclature entry
  *
  * Revision 1.78  2007/02/21 00:56:26  pandyas
  * Fixed Nomenclature save
  *
  * Revision 1.77  2007/02/15 18:59:17  pandyas
  * Still working on nomenclature bug
  *
  * Revision 1.76  2007/02/01 19:05:50  pandyas
  * Fixed Genotype bug - working on saving Nomenclature
  *
  * Revision 1.75  2006/11/09 17:36:28  pandyas
  * Commented out debug code
  *
  * Revision 1.74  2006/10/17 16:13:47  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.73  2006/08/30 16:46:57  pandyas
  * needed to reverse back to original changes before toolMouse attribute name for dev server build
  *
  * Revision 1.72  2006/08/17 18:34:25  pandyas
  * Defect# 410: Externalize properties files - Code Changes to send mail method
  *
  * Revision 1.71  2006/05/23 17:00:31  pandyas
  * fixed comments - cut and paste from other section
  *
  * Revision 1.70  2006/05/03 20:04:21  pandyas
  * Modified to add Morpholino object data to application
  *
  * Revision 1.69  2006/04/27 15:03:54  pandyas
  * Removed unused import statement
  *
  * Revision 1.68  2006/04/20 19:18:53  pandyas
  * Moved save Assoc Met from AnimalModel to the Histopathology
  *
  * Revision 1.67  2006/04/20 18:11:31  pandyas
  * Cleaned up Species or Strain save of Other in DB
  *
  * Revision 1.66  2006/04/19 17:39:57  pandyas
  * Cleaned up e-mail - removed save of 'Other' to DB
  *
  * Revision 1.65  2006/04/18 16:19:32  pandyas
  * modified debug command from debug to info to display messages
  *
  * Revision 1.64  2006/04/17 19:11:06  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.63  2006/01/18 14:24:23  georgeda
  * TT# 376 - Updated to use new Java 1.5 features
  *
  * Revision 1.62  2005/12/21 15:40:29  georgeda
  * Defect #288 - error when submitting an other strain
  *
  * Revision 1.61  2005/12/01 13:43:36  georgeda
  * Defect #226, reuse Taxon objects and do not delete them from Database
  *
  * Revision 1.60  2005/11/16 15:31:05  georgeda
  * Defect #41. Clean up of email functionality
  *
  * Revision 1.59  2005/11/14 14:17:57  georgeda
  * Cleanup
  *
  * Revision 1.58  2005/11/08 16:48:24  georgeda
  * Changes for images
  *
  * Revision 1.57  2005/11/07 19:15:17  pandyas
  * modified for clinical marker screen
  *
  * Revision 1.56  2005/11/04 14:44:25  georgeda
  * Cleaned up histopathology/assoc metastasis
  *
  * Revision 1.55  2005/11/03 18:57:13  pandyas
  * Modified for histopathology screens
  *
  * Revision 1.54  2005/11/03 18:11:04  georgeda
  * Fix old vocab problem
  *
  * Revision 1.53  2005/11/03 17:22:35  schroedn
  * Changed how Associated Expression for Genetic Description were coded, cleaned up
  *
  * Revision 1.52  2005/11/03 17:01:30  georgeda
  * Change taxon creation strat.
  *
  * Revision 1.51  2005/11/02 21:46:09  georgeda
  * Fixed creation of sex distribution
  *
  * Revision 1.50  2005/11/02 20:56:04  schroedn
  * Added Staining to Image submission
  *
  * Revision 1.49  2005/11/02 19:02:55  pandyas
  * Added e-mail functionality
  *
  * Revision 1.48  2005/10/27 17:15:22  schroedn
  * Updated addAssociatedExpression for all Genetic Descriptions
  *
  * Revision 1.45  2005/10/26 20:42:52  schroedn
  * merged changes, Added AssocExpression to EngineeredTransgene submission page
  *
  * Revision 1.44  2005/10/26 20:16:57  pandyas
  * implemented model availability
  *
  * Revision 1.43  2005/10/24 21:00:17  schroedn
  * addImage added
  *
  * Revision 1.42  2005/10/24 18:05:36  georgeda
  * Show the modified date in the returned models
  *
  * Revision 1.41  2005/10/24 17:10:39  georgeda
  * First pass at duplicate
  *
  * Revision 1.40  2005/10/24 13:28:06  georgeda
  * Cleanup changes
  *
  * Revision 1.39  2005/10/21 19:38:37  schroedn
  * Added caImage ftp capabilities for EngineeredTransgene, GenomicSegment and TargetedModification
  *
  * Revision 1.38  2005/10/21 16:07:26  pandyas
  * implementation of animal availability
  *
  * Revision 1.37  2005/10/20 20:39:50  stewardd
  * modified to use constant value instead of a hard coded string in messageKeys
  *
  * Revision 1.36  2005/10/20 18:55:38  stewardd
  * Employs new EmailUtil API supporting e-mail content built from ResourceBundle-stored templates with support for variables (via Velocity API)
  *
  * Revision 1.35  2005/10/19 19:26:35  pandyas
  * added admin route to growth factor
  *
  * Revision 1.34  2005/10/18 16:23:31  georgeda
  * Changed getModelsByUser to return models where the PI is the user as well
  *
  * Revision 1.33  2005/10/13 20:47:25  georgeda
  * Correctly handle the PI
  *
  * Revision 1.32  2005/10/12 15:55:16  georgeda
  * Do not reuse taxon since it has an uncontolled vocab
  *
  * Revision 1.31  2005/10/11 20:52:51  schroedn
  * EngineeredTransgene and GenomicSegment edit/save works, not image
  *
  * EngineeredTransgene - 'Other' Species not working
  *
  * Revision 1.30  2005/10/10 20:05:19  pandyas
  * removed animalmodel reference in populate method
  *
  * Revision 1.29  2005/10/10 14:08:02  georgeda
  * Performance improvement
  *
  * Revision 1.28  2005/10/07 16:27:54  georgeda
  * Implemented paganation
  *
  * Revision 1.27  2005/10/06 20:43:45  schroedn
  * Fixed missing reference
  *
  * Revision 1.26  2005/10/06 20:41:51  schroedn
  * InducedMutation, TargetedMutation, GenomicSegment changes
  *
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
 import gov.nih.nci.camod.domain.AnimalAvailability;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.AnimalModelSearchResult;
 import gov.nih.nci.camod.domain.Availability;
 import gov.nih.nci.camod.domain.CarcinogenExposure;
 import gov.nih.nci.camod.domain.CellLine;
 import gov.nih.nci.camod.domain.EngineeredGene;
 import gov.nih.nci.camod.domain.GeneDelivery;
 import gov.nih.nci.camod.domain.GenomicSegment;
 import gov.nih.nci.camod.domain.Genotype;
 import gov.nih.nci.camod.domain.Histopathology;
 import gov.nih.nci.camod.domain.Image;
 import gov.nih.nci.camod.domain.InducedMutation;
 import gov.nih.nci.camod.domain.Log;
 import gov.nih.nci.camod.domain.MicroArrayData;
 import gov.nih.nci.camod.domain.Nomenclature;
 import gov.nih.nci.camod.domain.Person;
 import gov.nih.nci.camod.domain.Phenotype;
 import gov.nih.nci.camod.domain.Publication;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.domain.SpontaneousMutation;
 import gov.nih.nci.camod.domain.Strain;
 import gov.nih.nci.camod.domain.TargetedModification;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.domain.Transgene;
 import gov.nih.nci.camod.domain.TransientInterference;
 import gov.nih.nci.camod.domain.Transplant;
 import gov.nih.nci.camod.service.AnimalModelManager;
 import gov.nih.nci.camod.util.DuplicateUtil;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.AssociatedExpressionData;
 import gov.nih.nci.camod.webapp.form.AvailabilityData;
 import gov.nih.nci.camod.webapp.form.CellLineData;
 import gov.nih.nci.camod.webapp.form.ChemicalDrugData;
 import gov.nih.nci.camod.webapp.form.ClinicalMarkerData;
 import gov.nih.nci.camod.webapp.form.CurationAssignmentData;
 import gov.nih.nci.camod.webapp.form.EngineeredTransgeneData;
 import gov.nih.nci.camod.webapp.form.EnvironmentalFactorData;
 import gov.nih.nci.camod.webapp.form.GeneDeliveryData;
 import gov.nih.nci.camod.webapp.form.GenomicSegmentData;
 import gov.nih.nci.camod.webapp.form.GrowthFactorData;
 import gov.nih.nci.camod.webapp.form.HistopathologyData;
 import gov.nih.nci.camod.webapp.form.HormoneData;
 import gov.nih.nci.camod.webapp.form.ImageData;
 import gov.nih.nci.camod.webapp.form.InducedMutationData;
 import gov.nih.nci.camod.webapp.form.MicroArrayDataData;
 import gov.nih.nci.camod.webapp.form.ModelCharacteristicsData;
 import gov.nih.nci.camod.webapp.form.NutritionalFactorData;
 import gov.nih.nci.camod.webapp.form.PublicationData;
 import gov.nih.nci.camod.webapp.form.RadiationData;
 import gov.nih.nci.camod.webapp.form.SearchData;
 import gov.nih.nci.camod.webapp.form.SpontaneousMutationData;
 import gov.nih.nci.camod.webapp.form.SurgeryData;
 import gov.nih.nci.camod.webapp.form.TargetedModificationData;
 import gov.nih.nci.camod.webapp.form.TherapyData;
 import gov.nih.nci.camod.webapp.form.TransientInterferenceData;
 import gov.nih.nci.camod.webapp.form.TransplantData;
 import gov.nih.nci.camod.webapp.form.ViralTreatmentData;
 import gov.nih.nci.common.persistence.Persist;
 import gov.nih.nci.common.persistence.Search;
 import gov.nih.nci.common.persistence.exception.PersistenceException;
 import gov.nih.nci.common.persistence.hibernate.HibernateUtil;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Manages fetching/saving/updating of animal models
  */
 public class AnimalModelManagerImpl extends BaseManager implements AnimalModelManager
 {
 
     /**
      * Get all of the animal models in the DB
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public List getAll() throws Exception
     {
         log.debug("In AnimalModelManagerImpl.getAll");
         return super.getAll(AnimalModel.class);
     }
     
     /**
      * Get all of the animal models in the DB in batches of size = pageSize
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      
     public List getAll(String inKey, int pageSize) throws Exception
     {
         log.debug("In AnimalModelManagerImpl.getAll(key, pageSize");
 
         return QueryManagerSingleton.instance().getModelsBetweenRange(inKey, pageSize);        
     }    
 	*/
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
     public List getAllByUser(String inUsername) throws Exception
     {
 
         log.debug("In AnimalModelManagerImpl.getAllByUser");
 
         return QueryManagerSingleton.instance().getModelsByUser(inUsername);
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
     public List getAllByState(String inState) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.getAllByState");
 
         // The list of AnimalModels to be returned
         List theAnimalModels = new ArrayList();
 
         // The following two objects are needed for eQBE.
         AnimalModel theAnimalModel = new AnimalModel();
         theAnimalModel.setState(inState);
 
         try
         {
             theAnimalModels = Search.query(theAnimalModel);
         }
         catch (Exception e)
         {
             log.error("Exception occurred in getAllByState", e);
             throw e;
         }
 
         log.debug("Exiting AnimalModelManagerImpl.getAllByState");
 
         return theAnimalModels;
     }
 
     /**
      * Get all of the models of a specific state
      * 
      * @param inState
      *            the state to query for
      * 
      * @return the list of models
      * 
      * @exception Exception
      *                if an error occurred
      */
     public List getAllByStateForPerson(String inState,
                                        Person inPerson) throws Exception
     {
 
         log.debug("In AnimalModelManagerImpl.getAllByStateForPerson");
 
         return QueryManagerSingleton.instance().getModelsByStateForPerson(inState, inPerson);
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
     public AnimalModel get(String id) throws Exception
     {
         log.debug("In AnimalModelManagerImpl.get");
 
         AnimalModel theAnimalModel = (AnimalModel) super.get(id, AnimalModel.class);
         log.debug("theAnimalModel: " + theAnimalModel.toString());
 
         // Set the modified date in case we save a change
         theAnimalModel.getAvailability().setModifiedDate(new Date());
 
         return theAnimalModel;
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
     public void save(AnimalModel inAnimalModel) throws Exception
     {
         log.debug("In AnimalModelManagerImpl.save");
         super.save(inAnimalModel);
     }
 
     /**
      * Do a deep copy of the passed in animal model
      * 
      * @return the list of all animal models
      * 
      * @exception throws
      *                an Exception if an error occurred
      */
     public AnimalModel duplicate(AnimalModel inAnimalModel) throws Exception
     {
         log.debug("In AnimalModelManagerImpl.duplicate");
         
         AnimalModel theDuplicatedModel = (AnimalModel) DuplicateUtil.duplicateBean(inAnimalModel);
 
         String theNewModelDescriptor = theDuplicatedModel.getModelDescriptor() + " (Copy) ";
         theDuplicatedModel.setModelDescriptor(theNewModelDescriptor);
 
         theDuplicatedModel.getAvailability().setModifiedDate(null);
         theDuplicatedModel.getAvailability().setEnteredDate(new Date());
         
         theDuplicatedModel.setState("Incomplete");
         
         log.debug("In AnimalModelManagerImpl.duplicate state" + theDuplicatedModel.getState());
         
         save(theDuplicatedModel);
 
         return theDuplicatedModel;
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
     public void updateAndAddLog(AnimalModel inAnimalModel,
                                 Log inLog) throws Exception
     {
 
         log.debug("Entering updateAndAddLog");
 
         try
         {
 
             // Make sure they get saved together
             HibernateUtil.beginTransaction();
             Persist.save(inAnimalModel);
             Persist.save(inLog);
             HibernateUtil.commitTransaction();
 
         }
         catch (PersistenceException pe)
         {
             HibernateUtil.rollbackTransaction();
             log.error("PersistenceException in save", pe);
             throw pe;
         }
         catch (Exception e)
         {
             HibernateUtil.rollbackTransaction();
             log.error("Exception in save", e);
             throw e;
         }
 
         log.debug("Exiting updateAndAddLog");
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
     public AnimalModel create(ModelCharacteristicsData inModelCharacteristicsData,
                               String inUsername) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.create");
 
         AnimalModel theAnimalModel = new AnimalModel();
 
         log.debug("Exiting AnimalModelManagerImpl.create");
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
     public void update(ModelCharacteristicsData inModelCharacteristicsData,
                        AnimalModel inAnimalModel) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.update");
         log.debug("Updating animal model: " + inAnimalModel.getId());
 
         // Populate w/ the new values and save
         inAnimalModel = populateAnimalModel(inModelCharacteristicsData, null, inAnimalModel);
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.update");
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
     public void remove(String id) throws Exception
     {
         log.debug("In AnimalModelManagerImpl.remove");
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
     public List<AnimalModelSearchResult> search(SearchData inSearchData) throws Exception
     {
 
         log.info("In search");
         List theAnimalModels = QueryManagerSingleton.instance().searchForAnimalModels(inSearchData);
 
         List<AnimalModelSearchResult> theDisplayList = new ArrayList<AnimalModelSearchResult>();
 
         // Add AnimalModel DTO's so that the paganation works quickly
         for (int i = 0, j = theAnimalModels.size(); i < j; i++)
         {
             AnimalModel theAnimalModel = (AnimalModel) theAnimalModels.get(i);
             theDisplayList.add(new AnimalModelSearchResult(theAnimalModel));
         }
         log.debug("In search - theDisplayList.size(): " + theDisplayList.size());
         return theDisplayList;
     }
     
     /**
      * Search for animal models in the admin section based on available criteria:
      * 
      * @throws Exception
      */
     public List<AnimalModelSearchResult> searchAdmin(CurationAssignmentData inCurationAssignmentData) throws Exception
     {
 
         log.debug("In searchAdmin");
         List theAnimalModels = QueryManagerSingleton.instance().searchForAdminAnimalModels(inCurationAssignmentData);
         log.debug("<searchAdmin> theAnimalModels.size(): " + theAnimalModels.size());
 
         List<AnimalModelSearchResult> theDisplayList = new ArrayList<AnimalModelSearchResult>();
 
         // Add AnimalModel DTO's so that the paganation works quickly
         for (int i = 0, j = theAnimalModels.size(); i < j; i++)
         {
             AnimalModel theAnimalModel = (AnimalModel) theAnimalModels.get(i);
             theDisplayList.add(new AnimalModelSearchResult(theAnimalModel));
         }
         log.debug("In searchAdmin - theDisplayList.size(): " + theDisplayList.size());
         return theDisplayList;
     }
 
 	// Populate the model based on the model characteristics form passed in.
 	private AnimalModel populateAnimalModel(
 			ModelCharacteristicsData inModelCharacteristicsData,
 			String inUsername, AnimalModel inAnimalModel) throws Exception {
 
 		log.debug("Entering populateAnimalModel");
 
 		// Handle the person debugrmation
 		if (inUsername != null) {
 			Person theSubmitter = PersonManagerSingleton.instance()
 					.getByUsername(inUsername);
 			if (theSubmitter == null) {
 
 				throw new IllegalArgumentException("Unknown user: "
 						+ inUsername);
 			}
 			inAnimalModel.setSubmitter(theSubmitter);
 		}
 
 		Person thePI = PersonManagerSingleton.instance().getByUsername(
 				inModelCharacteristicsData.getPrincipalInvestigator());
 
 		if (thePI == null) {
 			throw new IllegalArgumentException(
 					"Unknown principal investigator: " + inUsername);
 		}
 
 		inAnimalModel.setPrincipalInvestigator(thePI);
 
 		// Set the animal model debugrmation
 		boolean isToolStrain = inModelCharacteristicsData.getIsToolStrain()
 				.equals("yes") ? true : false;
 		inAnimalModel.setIsToolStrain(new Boolean(isToolStrain));
 		inAnimalModel.setUrl(inModelCharacteristicsData.getUrl());
 		inAnimalModel.setModelDescriptor(inModelCharacteristicsData
 				.getModelDescriptor());
 		inAnimalModel.setExperimentDesign(inModelCharacteristicsData
 				.getExperimentDesign());
 		inAnimalModel.setComments(inModelCharacteristicsData.getComments());
 
 		// Create/reuse the strain object - This method does not set strain when
 		// 'other' is selected (lookup)
 		Strain theNewStrain = StrainManagerSingleton.instance().getOrCreate(
 				inModelCharacteristicsData.getEthinicityStrain(),
 				inModelCharacteristicsData.getOtherEthnicityStrain(),
 				inModelCharacteristicsData.getScientificName());
 
 		log.debug("\n theNewStrain: " + theNewStrain.getName() + ": "
 				+ theNewStrain.getNameAlternEntry());
 		// other option selected
 		if (inModelCharacteristicsData.getEthinicityStrain().equals(
 				Constants.Dropdowns.OTHER_OPTION)) {
 			// Object is returned with uncontrolled vocab set, do not save
 			// 'Other' in DB, send e-mail
 			inAnimalModel.setStrain(theNewStrain);
 			sendEmail(inAnimalModel, inModelCharacteristicsData
 					.getOtherEthnicityStrain(), "EthinicityStrain");
 		} else {
 			// used to setSpecies in AnimalModel now used to setStrain in 2.1
 			inAnimalModel.setStrain(theNewStrain);
 		}
 		
 		// Set developmental stage
 		if (inModelCharacteristicsData.getDevelopmentalStage() != null && inModelCharacteristicsData.getDevelopmentalStage().length() > 0){
 			inAnimalModel.setDevelopmentalStage(inModelCharacteristicsData.getDevelopmentalStage());
 		}
 
 		// Genotype Clear previous entry - add new
 		Genotype theGenotype = null;
 		Set<Genotype> genotypeColl = inAnimalModel.getGenotypeCollection();
 		genotypeColl.clear();
 
 		if (inModelCharacteristicsData.getGenotype() != null && inModelCharacteristicsData.getGenotype().length() > 0) {
 			theGenotype = GenotypeManagerSingleton.instance().getOrCreate(inModelCharacteristicsData.getGenotype());
 			inAnimalModel.addGenotype(theGenotype);
 		}
 
 		// Nomenclature Clear previous entry - add new
 		Nomenclature theNomenclature = null;
 		Set<Nomenclature> nomenclatureColl = inAnimalModel.getNomenclatureCollection();
 		nomenclatureColl.clear();		
 		
 		if (inModelCharacteristicsData.getNomenclature() != null && inModelCharacteristicsData.getNomenclature().length() > 0) {
 			theNomenclature = NomenclatureManagerSingleton.instance()
 					.getOrCreate(inModelCharacteristicsData.getNomenclature());
 			inAnimalModel.addNomenclature(theNomenclature);
 		}
 
 		// Phenotype
 		Phenotype thePhenotype = inAnimalModel.getPhenotype();
 		if (thePhenotype == null) {
 			thePhenotype = new Phenotype();
 		}
 
 		// Get/create the sex distribution
 		if (inModelCharacteristicsData.getType() != null) {
 			SexDistribution theSexDistribution = SexDistributionManagerSingleton
 					.instance().getByType(inModelCharacteristicsData.getType());
 			thePhenotype.setSexDistribution(theSexDistribution);
 		}
 
 		// Create the phenotype
 		thePhenotype
 				.setDescription(inModelCharacteristicsData.getDescription());
 		thePhenotype.setBreedingNotes(inModelCharacteristicsData
 				.getBreedingNotes());
 
 		// Get the availability
 		Availability theAvailability = inAnimalModel.getAvailability();
 
 		// When the model was created
 		if (theAvailability == null) {
 			theAvailability = new Availability();
 		} else {
 			theAvailability.setModifiedDate(new Date());
 		}
 		theAvailability.setEnteredDate(new Date());
 
 		// Convert the date
 		Date theDate = new Date();
 		if (!inModelCharacteristicsData.getReleaseDate().equals("immediately")) {
 
 			// Convert the string to a date. Default to "now" if there are any
 			// errors
 			DateFormat theDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			try {
 				theDate = theDateFormat.parse(inModelCharacteristicsData
 						.getCalendarReleaseDate());
 			} catch (Exception e) {
 				log.error("Error parsing release date, defaulting to now", e);
 			}
 		}
 		theAvailability.setReleaseDate(theDate);
 
 		// Associated the created objects
 		inAnimalModel.setAvailability(theAvailability);
 		inAnimalModel.setPhenotype(thePhenotype);
 
 		log.debug("Exiting populateAnimalModel");
 
 		return inAnimalModel;
 	}
 
     public void addTransplant(AnimalModel inAnimalModel,
     		TransplantData inTransplantData) throws Exception
     {
         log.debug("Entering saveTransplant");
 
         Transplant theTransplant = TransplantManagerSingleton.instance().create(inTransplantData, inAnimalModel);
 
         inAnimalModel.addTransplant(theTransplant);
         save(inAnimalModel);
 
         log.debug("Exiting saveTransplant");
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
     public void addGeneDelivery(AnimalModel inAnimalModel,
                                 GeneDeliveryData inGeneDeliveryData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl> Entering addGeneDelivery");
 
         GeneDelivery theGeneDelivery = GeneDeliveryManagerSingleton.instance().create(inAnimalModel, inGeneDeliveryData);
         inAnimalModel.addGeneDelivery(theGeneDelivery);
         save(inAnimalModel);
 
         log.debug("<AnimalModelManagerImpl> Exiting addGeneDelivery");
     }
 
     /**
      * Add a chemical/drug therapy
      * 
      * @param inAnimalModel
      *            the animal model that has the addCarcinogenExposure
      * @param inChemicalDrugData
      *            the new chemical drug data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       ChemicalDrugData inChemicalDrugData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (chemical/drug)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inChemicalDrugData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (chemical/drug)");
     }
 
     /**
      * Add an environmental factor CarcinogenExposure
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inEnvironmentalFactorData
      *            the data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       EnvironmentalFactorData inEnvironmentalFactorData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (EF)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel,
                                                                                                         inEnvironmentalFactorData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (EF)");
     }
 
     /**
      * Add an Radiation
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inRadiationData
      *            the new radiation data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       RadiationData inRadiationData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (Radiation)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inRadiationData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (Radiation)");
     }
 
     /**
      * Add an ViralTreatment
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inViralTreatmentData
      *            the new viral treatment data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       ViralTreatmentData inViralTreatmentData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (ViralTreatment)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inViralTreatmentData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (ViralTreatment)");
     }
 
     /**
      * Add a growth factor
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inGrowthFactorData
      *            the new growth factor data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       GrowthFactorData inGrowthFactorData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (growth factor)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inGrowthFactorData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (growth factor)");
     }
 
     /**
      * Add a hormone
      * 
      * @param inAnimalModel
      *            the animal model that has the addCarcinogenExposure
      * @param inHormoneData
      *            the new growth factor data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       HormoneData inHormoneData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (hormone)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inHormoneData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (hormone) ");
     }
 
     /**
      * Add a nutritional factor
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inNutritionalFactorData
      *            the new nutrional factor data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       NutritionalFactorData inNutritionalFactorData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (nutritional)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel,
                                                                                                         inNutritionalFactorData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (nutritional)");
     }
 
     /**
      * Add a surgery/other
      * 
      * @param inAnimalModel
      *            the animal model that has the CarcinogenExposure
      * @param inSurgeryData
      *            the new CarcinogenExposure data
      * @throws Exception
      */
     public void addCarcinogenExposure(AnimalModel inAnimalModel,
                                       SurgeryData inSurgeryData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addCarcinogenExposure (surgery/other)");
         CarcinogenExposure theCarcinogenExposure = CarcinogenExposureManagerSingleton.instance().create(inAnimalModel, inSurgeryData);
         inAnimalModel.addCarcinogenExposure(theCarcinogenExposure);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addCarcinogenExposure (surgery/other)");
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
     public void addCellLine(AnimalModel inAnimalModel,
                             CellLineData inCellLineData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl> Entering addCellLine");
         CellLine theCellLine = CellLineManagerSingleton.instance().create(inCellLineData);
         inAnimalModel.addCellLine(theCellLine);
         save(inAnimalModel);
 
         log.debug("<AnimalModelManagerImpl> Exiting addCellLine");
     }
 
     public void addMicroArrayData( AnimalModel inAnimalModel, 
     								MicroArrayDataData inMicroArrayData ) throws Exception
     {
     	log.debug("<AnimalModelManagerImpl> Entering addMicroArrayData");
     	
         MicroArrayData theMicroArrayData = MicroArrayDataManagerSingleton.instance().create(inMicroArrayData);
         inAnimalModel.addMicroArrayData( theMicroArrayData );
         save(inAnimalModel);
     	
     	log.debug("<AnimalModelManagerImpl> Exiting addMicroArrayData");
     }
     
     /**
      * Add a SpontaneousMutation
      */
     public void addGeneticDescription(AnimalModel inAnimalModel,
                                       SpontaneousMutationData inSpontaneousMutationData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl> Entering addGeneticDescription (spontaneousMutation)");
         SpontaneousMutation theSpontaneousMutation = SpontaneousMutationManagerSingleton.instance().create(inSpontaneousMutationData);
         log.debug(theSpontaneousMutation.getName());
         inAnimalModel.addSpontaneousMutation(theSpontaneousMutation);
         save(inAnimalModel);
 
         log.debug("<AnimalModelManagerImpl> Exiting addGeneticDescription (spontaneousMutation)");
     }
 
     /**
      * Add a InducedMutation
      */
     public void addGeneticDescription(AnimalModel inAnimalModel,
                                       InducedMutationData inInducedMutationData) throws Exception
     {
 
         log.debug("Entering addGeneticDescription (inducedMutation)");
 
         InducedMutation theInducedMutation = InducedMutationManagerSingleton.instance().create(inAnimalModel, inInducedMutationData);
         inAnimalModel.addEngineeredGene(theInducedMutation);
         save(inAnimalModel);
 
         log.debug("Exiting addGeneticDescription (inducedMutation)");
     }
 
     /**
      * Add a TargetedModification
      */
     public void addGeneticDescription(AnimalModel inAnimalModel,
                                       TargetedModificationData inTargetedModificationData,
                                       HttpServletRequest request) throws Exception
     {
 
         log.debug("Entering addGeneticDescription (TargetedModification)");
 
         TargetedModification theTargetedModification = TargetedModificationManagerSingleton.instance().create(inAnimalModel,
                                                                                                               inTargetedModificationData,
                                                                                                              request);
        
         inAnimalModel.addEngineeredGene(theTargetedModification);
         save(inAnimalModel);
 
         log.debug("Exiting addGeneticDescription (TargetedModification)");
     }
 
     public void addGeneticDescription(AnimalModel inAnimalModel,
                                       GenomicSegmentData inGenomicSegmentData,
                                       HttpServletRequest request) throws Exception
     {
 
         log.debug("Entering addGeneticDescription (GenomicSegment)");
 
         GenomicSegment theGenomicSegment = GenomicSegmentManagerSingleton.instance().create(inAnimalModel, inGenomicSegmentData, request);
 
         inAnimalModel.addEngineeredGene(theGenomicSegment);
         save(inAnimalModel);
 
         log.debug("Exiting addGeneticDescription (GenomicSegment)");
     }
 
     public void addGeneticDescription(AnimalModel inAnimalModel,
                                       EngineeredTransgeneData inEngineeredTransgeneData,
                                       HttpServletRequest request) throws Exception
     {
 
         log.debug("Entering addGeneticDescription (EngineeredTransgene)");
 
         Transgene theEngineeredTransgene = EngineeredTransgeneManagerSingleton.instance().create(inEngineeredTransgeneData, request);
 
         inAnimalModel.addEngineeredGene(theEngineeredTransgene);
         save(inAnimalModel);
 
         log.debug("Exiting addGeneticDescription (EngineeredTransgene)");
     }
 
     public void addImage(AnimalModel inAnimalModel,
                          ImageData inImageData,
                          String inPath) throws Exception
     {
 
         log.debug("Entering addImage (Image)");
 
         Image theImage = ImageManagerSingleton.instance().create(inAnimalModel, inImageData, inPath,
                                                                  Constants.CaImage.FTPMODELSTORAGEDIRECTORY);
         inAnimalModel.addImage(theImage);
         save(inAnimalModel);
 
         log.debug("Exiting addImage (Image)");
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
 
     public void addTherapy(AnimalModel inAnimalModel,
                            TherapyData inTherapyData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl addTherapy>");
 
         log.debug("Entering AnimalModelManagerImpl.addTherapy");
 
         Therapy theTherapy = TherapyManagerSingleton.instance().create(inAnimalModel, inTherapyData);
         inAnimalModel.addTherapy(theTherapy);
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.addTherapy");
     }
 
     /**
      * Add an Availability
      * 
      * @param inAnimalModel
      *            the animal model that has the Availability
      * @param inAvailabilityData
      *            the new therapy data
      * @throws Exception
      */
     public void addAvailability(AnimalModel inAnimalModel,
                                 AvailabilityData inAvailabilityData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl addAvailability>");
 
         log.debug("Entering AnimalModelManagerImpl.addAvailability");
         AnimalAvailability theAvailability = AvailabilityManagerSingleton.instance().create(inAvailabilityData);
         inAnimalModel.addAnimalAvailability(theAvailability);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addAvailability");
     }
 
     /**
      * Add an Availability
      * 
      * @param inAnimalModel
      *            the animal model that has the Availability
      * @param inAvailabilityData
      *            the new therapy data
      * @throws Exception
      */
 
     public void addInvestigatorAvailability(AnimalModel inAnimalModel,
                                             AvailabilityData inAvailabilityData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addInvestigatorAvailability");
         AnimalAvailability theAvailability = AvailabilityManagerSingleton.instance().createInvestigator(inAvailabilityData);
         inAnimalModel.addAnimalAvailability(theAvailability);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addInvestigatorAvailability");
     }
 
     public void addAssociatedExpression(AnimalModel inAnimalModel,
                                         EngineeredGene inEngineeredGene,
                                         AssociatedExpressionData inAssociatedExpressionData) throws Exception
     {
 
         log.debug("<AnimalModelManagerImpl addAssociatedExpression>");
         log.debug("Entering AnimalModelManagerImpl.addAssociatedExpression");
 
         // addAssociatedExpression (ExpressionFeature)
         EngineeredTransgeneManagerSingleton.instance().createAssocExpression(inAssociatedExpressionData, inEngineeredGene);
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.addAssociatedExpression");
     }
 
     public void addPublication(AnimalModel inAnimalModel,
                                PublicationData inPublicationData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addPublication");
 
         // addAssociatedExpression (ExpressionFeature
         Publication thePublication = PublicationManagerSingleton.instance().create(inPublicationData);
         inAnimalModel.addPublication(thePublication);
 
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.addAssociatedExpression");
     }
 
     public void addHistopathology(AnimalModel inAnimalModel,
                                   HistopathologyData inHistopathologyData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addHistopathology_1");
 
         Histopathology theHistopathology = HistopathologyManagerSingleton.instance().createHistopathology(inAnimalModel, inHistopathologyData);
         inAnimalModel.addHistopathology(theHistopathology);
 
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.addHistopathology");
     }
 
     public void addClinicalMarker(AnimalModel inAnimalModel,
                                   Histopathology inHistopathology,
                                   ClinicalMarkerData inClinicalMarkerData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addHistopathology to inClinicalMarkerData");
 
         ClinicalMarkerManagerSingleton.instance().create(inClinicalMarkerData, inHistopathology);
         save(inAnimalModel);
 
         log.debug("Exiting AnimalModelManagerImpl.addHistopathology to inClinicalMarkerData");
     }
 
     /**
      * Add a TransientInterference
      * 
      * @param inAnimalModel
      *            the animal model that has the TransientInterference
      * @param inMorpholinoData
      *            the new Morpholino data
      * @throws Exception
      */
     public void addTransientInterference(AnimalModel inAnimalModel,
                                          TransientInterferenceData inTransientInterferenceData) throws Exception
     {
 
         log.debug("Entering AnimalModelManagerImpl.addTransientInterference");
         TransientInterference theTransientInterference = TransientInterferenceManagerSingleton.instance().create(inAnimalModel,
                                                                                                                  inTransientInterferenceData);
         inAnimalModel.addTransientInterference(theTransientInterference);
         save(inAnimalModel);
         log.debug("Exiting AnimalModelManagerImpl.addTransientInterference");
     }
 
     private void sendEmail(AnimalModel inAnimalModel,
                            String theUncontrolledVocab,
                            String inType)
     {
 
         // Get the e-mail resource
         Properties camodProperties = new Properties();
         String camodPropertiesFileName = null;
 
         camodPropertiesFileName = System.getProperty("gov.nih.nci.camod.camodProperties");
 
         try
         {
 
             FileInputStream in = new FileInputStream(camodPropertiesFileName);
             camodProperties.load(in);
 
         }
         catch (FileNotFoundException e)
         {
             log.error("Caught exception finding file for properties: ", e);
             e.printStackTrace();
         }
         catch (IOException e)
         {
             log.error("Caught exception finding file for properties: ", e);
             e.printStackTrace();
         }
 
         // String recipients =
         // theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_NOTIFY_KEY);
         String recipients = UserManagerSingleton.instance().getEmailForCoordinator();
 
         StringTokenizer st = new StringTokenizer(recipients, ",");
         String inRecipients[] = new String[st.countTokens()];
         for (int i = 0; i < inRecipients.length; i++)
         {
             inRecipients[i] = st.nextToken();
         }
 
         // String inSubject =
         // theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_SUBJECT_KEY);
         String inSubject = camodProperties.getProperty("model.new_unctrl_vocab_subject");
         String inFrom = inAnimalModel.getSubmitter().getEmailAddress();
 
         // gather message keys and variable values to build the e-mail
         String[] messageKeys = { Constants.Admin.NONCONTROLLED_VOCABULARY };
         Map<String, Object> values = new TreeMap<String, Object>();
         values.put("type", inType);
         values.put("value", theUncontrolledVocab);
         values.put("submitter", inAnimalModel.getSubmitter());
         values.put("model", inAnimalModel.getModelDescriptor());
         values.put("modelstate", inAnimalModel.getState());
 
         // Send the email
         try
         {
             MailUtil.sendMail(inRecipients, inSubject, "", inFrom, messageKeys, values);
         }
         catch (MessagingException e)
         {
             log.error("Caught exception sending mail: ", e);
             e.printStackTrace();
         }
 
     }
 
 }
