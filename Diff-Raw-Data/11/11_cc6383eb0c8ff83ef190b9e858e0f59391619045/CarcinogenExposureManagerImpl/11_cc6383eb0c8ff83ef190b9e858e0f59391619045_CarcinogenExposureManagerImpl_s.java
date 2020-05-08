 /**
  * @author pandyas
  * 
 * $Id: CarcinogenExposureManagerImpl.java,v 1.2 2006-05-04 14:27:20 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2006/04/17 19:11:05  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 package gov.nih.nci.camod.service.impl;
 
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.CarcinogenExposure;
 import gov.nih.nci.camod.domain.EnvironmentalFactor;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.domain.Treatment;
 import gov.nih.nci.camod.service.CarcinogenExposureManager;
 import gov.nih.nci.camod.util.MailUtil;
 import gov.nih.nci.camod.webapp.form.*;
 import gov.nih.nci.camod.webapp.form.cibase.AdministrationData;
 import gov.nih.nci.camod.webapp.form.cibase.AgeGenderData;
 import gov.nih.nci.camod.webapp.form.cibase.DoseData;
 import gov.nih.nci.camod.webapp.form.cibase.NameData;
 import gov.nih.nci.camod.webapp.form.cibase.TreatmentData;
 
 public class CarcinogenExposureManagerImpl extends BaseManager implements CarcinogenExposureManager
 {
     /**
      * Get a specific CarcinogenExposure by id
      * 
      * @param id
      *            the unique id for a CarcinogenExposure
      * 
      * @return the matching CarcinogenExposure object, or null if not found.
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public CarcinogenExposure get(String id) throws Exception
     {
         log.trace("In CarcinogenExposureManagerImpl.get");
         return (CarcinogenExposure) super.get(id, CarcinogenExposure.class);
     }
 
     /**
      * Remove a specific CarcinogenExposure by id
      * 
      * @param id
      *            the unique id for a CarcinogenExposure
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void remove(String id,
                        AnimalModel inAnimalModel) throws Exception
     {
         log.debug("In CarcinogenExposureManagerImpl.remove");
 
         CarcinogenExposure theCarcinogenExposure = get(id);
 
        inAnimalModel.getTherapyCollection().remove(theCarcinogenExposure);
         super.save(inAnimalModel);
     }
 
     /**
      * Save CarcinogenExposure
      * 
      * @param CarcinogenExposure
      *            the CarcinogenExposure to save
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void save(CarcinogenExposure carcinogenExposure) throws Exception
     {
         log.debug("In CarcinogenExposureManagerImpl.save");
         super.save(carcinogenExposure);
     }
 
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inChemicalDrugData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a CarcinogenExposure
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      ChemicalDrugData inChemicalDrugData)
     {
 
         log.debug("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
 
         populateName(inAnimalModel, inChemicalDrugData, theCarcinogenExposure, "Chemical / Drug");
         populateAgeGender(inChemicalDrugData, theCarcinogenExposure);
         populateTreatment(inChemicalDrugData, theCarcinogenExposure);
         populateDose(inChemicalDrugData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inChemicalDrugData, theCarcinogenExposure);
 
         populateChemicalDrug(inChemicalDrugData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inChemicalDrugData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inCarcinogenExposure
      *            the CarcinogenExposure object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        ChemicalDrugData inChemicalDrugData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inChemicalDrugData, inCarcinogenExposure, "Chemical / Drug");
         populateAgeGender(inChemicalDrugData, inCarcinogenExposure);
         populateTreatment(inChemicalDrugData, inCarcinogenExposure);
         populateDose(inChemicalDrugData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inChemicalDrugData, inCarcinogenExposure);
 
         populateChemicalDrug(inChemicalDrugData, inCarcinogenExposure);
         save(inCarcinogenExposure);
     }
 
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inEnvironmentalFactorData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      EnvironmentalFactorData inEnvironmentalFactorData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
 
         populateName(inAnimalModel, inEnvironmentalFactorData, theCarcinogenExposure, "Environment");
         populateAgeGender(inEnvironmentalFactorData, theCarcinogenExposure);
         populateTreatment(inEnvironmentalFactorData, theCarcinogenExposure);
         populateDose(inEnvironmentalFactorData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inEnvironmentalFactorData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inEnvironmentalFactorData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inCarcinogenExposure
      *            the CarcinogenExposure object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        EnvironmentalFactorData inEnvironmentalFactorData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inEnvironmentalFactorData, inCarcinogenExposure, "Environment");
         populateAgeGender(inEnvironmentalFactorData, inCarcinogenExposure);
         populateTreatment(inEnvironmentalFactorData, inCarcinogenExposure);
         populateDose(inEnvironmentalFactorData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inEnvironmentalFactorData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inGrowthFactorData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      GrowthFactorData inGrowthFactorData)
     {

         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
         populateName(inAnimalModel, inGrowthFactorData, theCarcinogenExposure, "Growth Factor");
         populateAgeGender(inGrowthFactorData, theCarcinogenExposure);
         populateTreatment(inGrowthFactorData, theCarcinogenExposure);
         populateDose(inGrowthFactorData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inGrowthFactorData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inGrowthFactorData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        GrowthFactorData inGrowthFactorData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inGrowthFactorData, inCarcinogenExposure, "Growth Factor");
         populateAgeGender(inGrowthFactorData, inCarcinogenExposure);
         populateTreatment(inGrowthFactorData, inCarcinogenExposure);
         populateDose(inGrowthFactorData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inGrowthFactorData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inHormoneData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      HormoneData inHormoneData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
         populateName(inAnimalModel, inHormoneData, theCarcinogenExposure, "Hormone");
         populateAgeGender(inHormoneData, theCarcinogenExposure);
         populateTreatment(inHormoneData, theCarcinogenExposure);
         populateDose(inHormoneData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inHormoneData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inHormoneData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inCarcinogenExposure
      *            the CarcinogenExposure object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        HormoneData inHormoneData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inHormoneData, inCarcinogenExposure, "Hormone");
         populateAgeGender(inHormoneData, inCarcinogenExposure);
         populateTreatment(inHormoneData, inCarcinogenExposure);
         populateDose(inHormoneData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inHormoneData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inNutritionalFactorData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a CarcinogenExposure
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      NutritionalFactorData inNutritionalFactorData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
         populateName(inAnimalModel, inNutritionalFactorData, theCarcinogenExposure, "Nutrition");
         populateTreatment(inNutritionalFactorData, theCarcinogenExposure);
         populateAgeGender(inNutritionalFactorData, theCarcinogenExposure);
         populateDose(inNutritionalFactorData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inNutritionalFactorData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inTherapy
      *            the CarcinogenExposure object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        NutritionalFactorData inNutritionalFactorData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inNutritionalFactorData, inCarcinogenExposure, "Nutrition");
         populateTreatment(inNutritionalFactorData, inCarcinogenExposure);
         populateAgeGender(inNutritionalFactorData, inCarcinogenExposure);
         populateDose(inNutritionalFactorData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inRadiationData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      RadiationData inRadiationData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
 
         populateName(inAnimalModel, inRadiationData, theCarcinogenExposure, "Radiation");
         populateAgeGender(inRadiationData, theCarcinogenExposure);
         populateTreatment(inRadiationData, theCarcinogenExposure);
         populateDose(inRadiationData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inRadiationData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inRadiationData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        RadiationData inRadiationData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inRadiationData, inCarcinogenExposure, "Radiation");
         populateAgeGender(inRadiationData, inCarcinogenExposure);
         populateTreatment(inRadiationData, inCarcinogenExposure);
         populateDose(inRadiationData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inRadiationData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inSurgeryData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      SurgeryData inSurgeryData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
         populateName(inAnimalModel, inSurgeryData, theCarcinogenExposure, "Other");
         populateTreatment(inSurgeryData, theCarcinogenExposure);
         populateAgeGender(inSurgeryData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inSurgeryData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        SurgeryData inSurgeryData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inSurgeryData, inCarcinogenExposure, "Other");
         populateTreatment(inSurgeryData, inCarcinogenExposure);
         populateAgeGender(inSurgeryData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
 
     /**
      * Create a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inViralTreatmentData
      *            the interface to create the CarcinogenExposure object from
      * 
      * @returns a therapy
      */
     public CarcinogenExposure create(AnimalModel inAnimalModel,
                                      ViralTreatmentData inViralTreatmentData)
     {
 
         log.info("In CarcinogenExposureManagerImpl.create");
 
         CarcinogenExposure theCarcinogenExposure = new CarcinogenExposure();
         populateName(inAnimalModel, inViralTreatmentData, theCarcinogenExposure, "Viral");
         populateAgeGender(inViralTreatmentData, theCarcinogenExposure);
         populateTreatment(inViralTreatmentData, theCarcinogenExposure);
         populateDose(inViralTreatmentData, theCarcinogenExposure);
         populateAdministration(inAnimalModel, inViralTreatmentData, theCarcinogenExposure);
 
         return theCarcinogenExposure;
     }
 
     /**
      * Update a CarcinogenExposure object with the correct data filled in.
      * 
      * @param inViralTreatmentData
      *            the interface to update the CarcinogenExposure object from
      * 
      * @param inTherapy
      *            the therapy object to update
      * 
      * @exception Exception
      *                when anything goes wrong.
      */
     public void update(AnimalModel inAnimalModel,
                        ViralTreatmentData inViralTreatmentData,
                        CarcinogenExposure inCarcinogenExposure) throws Exception
     {
 
         log.info("In CarcinogenExposureManagerImpl.update");
 
         populateName(inAnimalModel, inViralTreatmentData, inCarcinogenExposure, "Viral");
         populateAgeGender(inViralTreatmentData, inCarcinogenExposure);
         populateTreatment(inViralTreatmentData, inCarcinogenExposure);
         populateDose(inViralTreatmentData, inCarcinogenExposure);
         populateAdministration(inAnimalModel, inViralTreatmentData, inCarcinogenExposure);
 
         save(inCarcinogenExposure);
     }
 
 
     // ///////////////////////////////////////////////////////
     // Populate methods for the specific interfaces that
     // each interface implements
     // ///////////////////////////////////////////////////////
     private void populateName(AnimalModel inAnimalModel,
                               NameData inNameData,
                               CarcinogenExposure theCarcinogenExposure,
                               String inType)
     {
 
         log.info("In CarcinogenExposureManagerImpl.populateName");
 
         // Set the treatment
         Treatment theTreatment = theCarcinogenExposure.getTreatment();
         if (theTreatment == null)
         {
             theTreatment = new Treatment();
             theCarcinogenExposure.setTreatment(theTreatment);
         }
 
         EnvironmentalFactor theEF = theCarcinogenExposure.getEnvironmentalFactor();
         if (theEF == null)
         {
             theEF = new EnvironmentalFactor();
             theCarcinogenExposure.setEnvironmentalFactor(theEF);
         }
         theEF.setType(inType);
 
         /* Set other name or selected chemical name */
         // anytime the name is "other"
         if (inNameData.getName().equals(Constants.Dropdowns.OTHER_OPTION))
         {
 
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
             // content with
             String[] messageKeys = { Constants.Admin.NONCONTROLLED_VOCABULARY };
             Map<String, Object> values = new TreeMap<String, Object>();
             values.put("type", "CarcinogenExposureName");
             values.put("value", inNameData.getOtherName());
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
 
             theEF.setName(null);
             theEF.setNameUnctrlVocab(inNameData.getOtherName());
         }
         // anytime name is not other, set uncontrolled vocab to null (covers
         // editing)
         else
         {
             log.info("Name is not other");
             theEF.setName(inNameData.getName());
             theEF.setNameUnctrlVocab(null);
         }
 
     }
 
 
     private void populateAgeGender(AgeGenderData inAgeGender,
                                    CarcinogenExposure theCarcinogenExposure)
     {
 
         log.info("In CarcinogenExposureManagerImpl.populateAgeGender");
 
         // Set the treatment
         Treatment theTreatment = theCarcinogenExposure.getTreatment();
         if (theTreatment == null)
         {
             theTreatment = new Treatment();
             theCarcinogenExposure.setTreatment(theTreatment);
         }
 
         // Set the gender
         SexDistribution sexDistribution = SexDistributionManagerSingleton.instance().getByType(inAgeGender.getType());
 
         // save the treatment
         theTreatment.setSexDistribution(sexDistribution);
 
         theTreatment.setAgeAtTreatment(inAgeGender.getAgeAtTreatment());
         theTreatment.setAgeAtTreatmentUnit(inAgeGender.getAgeAtTreatmentUnit());
     }
 
     private void populateTreatment(TreatmentData inTreatment,
                                    CarcinogenExposure theCarcinogenExposure)
     {
 
         log.info("In CarcinogenExposureManagerImpl.populateTreatment");
 
         // Set the treatment
         Treatment theTreatment = theCarcinogenExposure.getTreatment();
         if (theTreatment == null)
         {
             theTreatment = new Treatment();
             theCarcinogenExposure.setTreatment(theTreatment);
         }
 
         // save the treatment
         theTreatment.setRegimen(inTreatment.getRegimen());
     }
 
     private void populateDose(DoseData inDoseData,
                               CarcinogenExposure theCarcinogenExposure)
     {
 
         // Set the treatment
         Treatment theTreatment = theCarcinogenExposure.getTreatment();
         if (theTreatment == null)
         {
             theTreatment = new Treatment();
             theCarcinogenExposure.setTreatment(theTreatment);
         }
 
         theTreatment.setDosage(inDoseData.getDosage());
         theTreatment.setDosageUnit(inDoseData.getDosageUnit());
     }
 
 
     private void populateAdministration(AnimalModel inAnimalModel,
                                         AdministrationData inAdministrationData,
                                         CarcinogenExposure theCarcinogenExposure)
     {
 
         log.info("In CarcinogenExposureManagerImpl.populateAdministration");
 
         if (inAdministrationData.getAdministrativeRoute() != null && inAdministrationData.getAdministrativeRoute().length() > 0)
         {
             // Set the treatment
             Treatment theTreatment = theCarcinogenExposure.getTreatment();
             if (theTreatment == null)
             {
                 theTreatment = new Treatment();
                 theCarcinogenExposure.setTreatment(theTreatment);
             }
         }
 
         /* Set other adminstrative route or selected adminstrative route */
         // anytime admin route is other
         if (inAdministrationData.getAdministrativeRoute().equals(Constants.Dropdowns.OTHER_OPTION))
         {
             log.info("admin route equals other");
 
             theCarcinogenExposure.getTreatment().setAdministrativeRoute(null);
             theCarcinogenExposure.getTreatment().setAdminRouteUnctrlVocab(inAdministrationData.getOtherAdministrativeRoute());
 
             log.info("Sending Notification eMail - new Administrative Route added");
             sendEmail(inAnimalModel, inAdministrationData.getOtherAdministrativeRoute(), "AdministrativeRoute");
 
 
             // anytime admin route is not other, set uncontrolled vocab to null
             // (covers editing)
         }
         else if (inAdministrationData.getAdministrativeRoute() != null)
         {
             log.info("admin route not other or null");
 
             theCarcinogenExposure.getTreatment().setAdministrativeRoute(inAdministrationData.getAdministrativeRoute());
             theCarcinogenExposure.getTreatment().setAdminRouteUnctrlVocab(null);
         }
 
         EnvironmentalFactor theEF = theCarcinogenExposure.getEnvironmentalFactor();
         if (theEF == null)
         {
             theEF = new EnvironmentalFactor();
             theCarcinogenExposure.setEnvironmentalFactor(theEF);
         }
     }
 
 
     private void populateChemicalDrug(ChemicalDrugData inChemicalDrug,
                                       CarcinogenExposure theCarcinogenExposure)
     {
 
         log.info("In CarcinogenExposureManagerImpl.populateChemicalDrug");
 
         EnvironmentalFactor theEF = theCarcinogenExposure.getEnvironmentalFactor();
 
         String theNscNumber = inChemicalDrug.getNscNumber().trim();
         if (theNscNumber != null && theNscNumber.length() > 0)
         {
             try
             {
                 theEF.setNscNumber(Long.valueOf(theNscNumber));
             }
             catch (NumberFormatException e)
             {
                 log.error("Bad NSC number: " + theNscNumber);
             }
         }
         String theCasNumber = inChemicalDrug.getCasNumber().trim();
         if (theCasNumber != null && theCasNumber.length() > 0)
         {
             theEF.setCasNumber(theCasNumber);
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
