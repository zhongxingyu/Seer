 /**
  * 
 * $Id: XenograftManagerImpl.java,v 1.29 2006-05-22 18:14:36 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.28  2006/05/22 15:02:27  pandyas
 * Fixed Xenograft so organ is reused/created each time
 *
  * Revision 1.27  2006/05/19 18:50:37  pandyas
  * defect #225 - Add clearOrgan functionality to Xenograft screen
  *
  * Revision 1.26  2006/05/19 16:39:43  pandyas
  * Defect #249 - add other to species on the Xenograft screen
  *
  * Revision 1.25  2006/04/20 18:11:30  pandyas
  * Cleaned up Species or Strain save of Other in DB
  *
  * Revision 1.24  2006/04/19 17:38:26  pandyas
  * Removed TODO text
  *
  * Revision 1.23  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.22  2005/12/12 17:33:37  georgeda
  * Defect #265, store host/origin species in correct places
  *
  * Revision 1.21  2005/12/01 13:43:36  georgeda
  * Defect #226, reuse Taxon objects and do not delete them from Database
  *
  * Revision 1.20  2005/11/28 22:54:11  pandyas
  * Defect #186: Added organ/tissue to Xenograft page, modified search page to display multiple Xenografts with headers, modified XenograftManagerImpl so it does not create or save an organ object if not organ is selected
  *
  * Revision 1.19  2005/11/28 13:46:53  georgeda
  * Defect #207, handle nulls for pages w/ uncontrolled vocab
  *
  * Revision 1.18  2005/11/16 15:31:05  georgeda
  * Defect #41. Clean up of email functionality
  *
  * Revision 1.17  2005/11/14 14:19:37  georgeda
  * Cleanup
  *
  * Revision 1.16  2005/11/11 16:27:44  pandyas
  * added javadocs
  *
  * 
  */
 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.Organ;
 import gov.nih.nci.camod.domain.Strain;
 import gov.nih.nci.camod.domain.Xenograft;
 import gov.nih.nci.camod.service.XenograftManager;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.XenograftData;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 /**
  * @author rajputs
  */
 public class XenograftManagerImpl extends BaseManager implements XenograftManager
 {
 
     /**
      * Get all Xenograft objects
      * 
      * 
      * @return the matching Xenograft objects, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */    
     public List getAll() throws Exception
     {
         log.trace("In XenograftManagerImpl.getAll");
         return super.getAll(Xenograft.class);
     }
 
     /**
      * Get a specific Xenograft by id
      * 
      * @param id
      *            the unique id for a Xenograft
      * 
      * @return the matching Xenograft object, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */    
     public Xenograft get(String id) throws Exception
     {
         log.trace("In XenograftManagerImpl.get");
         return (Xenograft) super.get(id, Xenograft.class);
     }
 
     public void save(Xenograft xenograft) throws Exception
     {
         log.trace("In XenograftManagerImpl.save");
         super.save(xenograft);
     }
 
     public void remove(String id,
                        AnimalModel inAnimalModel) throws Exception
     {
         log.trace("In XenograftManagerImpl.remove");
 
         inAnimalModel.getXenograftCollection().remove(get(id));
         super.save(inAnimalModel);
     }
 
     public Xenograft create(XenograftData inXenograftData,
                             AnimalModel inAnimalModel) throws Exception
     {
 
         log.trace("<XenograftManagerImpl> Entering XenograftManagerImpl.create");
 
         Xenograft theXenograft = new Xenograft();
         populateSpeciesStrain(inXenograftData, theXenograft, inAnimalModel);
         populateOrgan(inXenograftData, theXenograft);
         populateXenograft(inXenograftData, theXenograft, inAnimalModel);
 
         log.info("<XenograftManagerImpl> Exiting XenograftManagerImpl.create");
 
         return theXenograft;
     }
 
     public void update(XenograftData inXenograftData,
                        Xenograft inXenograft,
                        AnimalModel inAnimalModel) throws Exception
     {
         log.info("Entering XenograftManagerImpl.update XenograftId: " + inXenograft.getId());
 
         // Populate w/ the new values and save
         populateSpeciesStrain(inXenograftData, inXenograft, inAnimalModel);
         populateOrgan(inXenograftData, inXenograft);
         populateXenograft(inXenograftData, inXenograft, inAnimalModel);
         save(inXenograft);
 
         log.info("Exiting XenograftManagerImpl.update");
     }
 
     private void populateXenograft(XenograftData inXenograftData,
                                    Xenograft inXenograft,
                                    AnimalModel inAnimalModel) throws Exception
     {
 
         log.info("Entering XenograftManagerImpl.populateXenograft");
 
         /* Set xenograftName */
         inXenograft.setXenograftName(inXenograftData.getXenograftName());
 
         /* Set other adminstrative site or selected adminstrative site */
         // save directly in administrativeSite column of table
         if (inXenograftData.getAdministrativeSite().equals(Constants.Dropdowns.OTHER_OPTION))
         {
            // Do not save other in the DB
            inXenograft.setAdminSiteUnctrlVocab(inXenograftData.getOtherAdministrativeSite());
 
             // Send e-mail for other administrativeSite
             sendEmail(inAnimalModel, inXenograftData.getOtherAdministrativeSite(), "AdministrativeSite");
         }
         else
         {
             inXenograft.setAdministrativeSite(inXenograftData.getAdministrativeSite());
         }
         inXenograft.setGeneticManipulation(inXenograftData.getGeneticManipulation());
         inXenograft.setModificationDescription(inXenograftData.getModificationDescription());
         inXenograft.setParentalCellLineName(inXenograftData.getParentalCellLineName());
         inXenograft.setAtccNumber(inXenograftData.getAtccNumber());
         inXenograft.setCellAmount(inXenograftData.getCellAmount());
         inXenograft.setGrowthPeriod(inXenograftData.getGrowthPeriod());
 
 
         // anytime the graft type is "other"
         if (inXenograftData.getGraftType().equals(Constants.Dropdowns.OTHER_OPTION))
         {
             // Set Graft type
             inXenograft.setGraftType(null);
             inXenograft.setGraftTypeUnctrlVocab(inXenograftData.getOtherGraftType());
 
             // Send e-mail for other Graft Type
             sendEmail(inAnimalModel, inXenograftData.getOtherGraftType(), "GraftType");
 
         }
         // anytime graft type is not other set uncontrolled vocab to null
         // (covers editing)
         else
         {
             inXenograft.setGraftType(inXenograftData.getGraftType());
             inXenograft.setGraftTypeUnctrlVocab(null);
         }
 
         log.info("Exiting XenograftManagerImpl.populateXenograft");
     }
 
     private void populateSpeciesStrain(XenograftData inXenograftData,
                                        Xenograft inXenograft,
                                        AnimalModel inAnimalModel) throws Exception
     {
 
         // Use Species to create strain
         Strain theStrain = StrainManagerSingleton.instance().getOrCreate(inXenograftData.getDonorEthinicityStrain(),
                                                                          inXenograftData.getOtherDonorEthinicityStrain(),
                                                                          inXenograftData.getDonorScientificName(),
                                                                          inXenograftData.getOtherDonorScientificName());
 
         // other option selected for species - send e-mail
         if (inXenograftData.getDonorScientificName().equals(Constants.Dropdowns.OTHER_OPTION))
         {
             // Send e-mail for other donor species
             sendEmail(inAnimalModel, inXenograftData.getOtherDonorScientificName(), "Donor Species");
         }
         // other option selected for strain - send e-mail
         if (inXenograftData.getDonorEthinicityStrain().equals(Constants.Dropdowns.OTHER_OPTION))
         {
             // Send e-mail for other donor species
             sendEmail(inAnimalModel, inXenograftData.getOtherDonorEthinicityStrain(), "Donor Strain");
         }
 
         log.info("\n <populateSpeciesStrain> theSpecies is NOT other: ");
         inXenograft.setStrain(theStrain);
 
     }
 
     private void populateOrgan(XenograftData inXenograftData,
                                Xenograft inXenograft) throws Exception
     {
 
         // reuse/create Organ by matching concept code
         Organ theOrgan = OrganManagerSingleton.instance().getOrCreate(inXenograftData.getOrganTissueCode(),
                                                                       inXenograftData.getOrganTissueName());
 
         /*
          * Add a Organ to AnimalModel with correct IDs, conceptCode, only if
          * organ is selected by user - no need to check for existing organ in 2.1
          */
         if (inXenograftData.getOrganTissueCode() != null && inXenograftData.getOrganTissueCode().length() > 0)
         {
             inXenograft.setOrgan(theOrgan);
         }
         //blank out organ, clear button functionality during editing
         else
         {
             log.info("Setting object to null - clear organ: ");
             inXenograft.setOrgan(null);
         }
 
     }
 
     private void sendEmail(AnimalModel inAnimalModel,
                            String theUncontrolledVocab,
                            String inType)
     {
         // Get the e-mail resource
         ResourceBundle theBundle = ResourceBundle.getBundle("camod");
 
         // Iterate through all the reciepts in the config file
         String recipients = theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_NOTIFY_KEY);
         StringTokenizer st = new StringTokenizer(recipients, ",");
         String inRecipients[] = new String[st.countTokens()];
         for (int i = 0; i < inRecipients.length; i++)
         {
             inRecipients[i] = st.nextToken();
         }
 
         String inSubject = theBundle.getString(Constants.BundleKeys.NEW_UNCONTROLLED_VOCAB_SUBJECT_KEY);
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
         catch (Exception e)
         {
             log.error("Caught exception sending mail: ", e);
             e.printStackTrace();
         }
     }
 }
