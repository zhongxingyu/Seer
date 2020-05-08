 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cimmyt.cril.ibwb.provider.helpers;
 
 import ibfb.domain.core.Condition;
 
 import ibfb.domain.core.Constant;
 import ibfb.domain.core.Measurement;
 import ibfb.domain.core.MeasurementData;
 import ibfb.domain.core.Workbook;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.cimmyt.cril.ibwb.api.AppServices;
 import org.cimmyt.cril.ibwb.api.CommonServices;
 import org.cimmyt.cril.ibwb.domain.DataC;
 import org.cimmyt.cril.ibwb.domain.DataCPK;
 import org.cimmyt.cril.ibwb.domain.DataN;
 import org.cimmyt.cril.ibwb.domain.DataNPK;
 import org.cimmyt.cril.ibwb.domain.Dmsattr;
 import org.cimmyt.cril.ibwb.domain.Effect;
 import org.cimmyt.cril.ibwb.domain.EffectPK;
 import org.cimmyt.cril.ibwb.domain.Factor;
 import org.cimmyt.cril.ibwb.domain.LevelN;
 import org.cimmyt.cril.ibwb.domain.Measuredin;
 import org.cimmyt.cril.ibwb.domain.Obsunit;
 import org.cimmyt.cril.ibwb.domain.Oindex;
 import org.cimmyt.cril.ibwb.domain.OindexPK;
 import org.cimmyt.cril.ibwb.domain.Represtn;
 import org.cimmyt.cril.ibwb.domain.Scales;
 import org.cimmyt.cril.ibwb.domain.Steffect;
 import org.cimmyt.cril.ibwb.domain.Study;
 import org.cimmyt.cril.ibwb.domain.TmsMethod;
 import org.cimmyt.cril.ibwb.domain.Traits;
 import org.cimmyt.cril.ibwb.domain.Variate;
 import org.cimmyt.cril.ibwb.domain.Veffect;
 import org.cimmyt.cril.ibwb.domain.VeffectPK;
 import org.cimmyt.cril.ibwb.provider.utils.ChadoSchemaUtil;
 import org.cimmyt.cril.ibwb.provider.utils.ConverterDomainToDTO;
 
 /**
  *
  * @author MasterGama
  */
 public class HelperWorkbook {
 
     private static Logger log = Logger.getLogger(HelperWorkbook.class);
 //    public static final String ENTRY_LABEL = "ENTRY";
 //    public static final String PLOT_LABEL = "PLOT";
     public static final String vtype = "MV";
     private static String labelStudy = "STUDY";
     private static String steffectNameStudy = "STUDY";
     private static String steffectNameTrial = "TRIAL";
     private static String steffectNameMeasurement = "MEASUREMENT EFECT";
 //    private static String labelTrial = "TRIAL";
 //    private static String labelEntry = ENTRY_LABEL;
 //    private static String labelPlot = PLOT_LABEL;
 //    private static final String REPLICATION = "REPLICATION";
 //    private static final String FIELD_PLOT = "FIELD PLOT";
 //    private static final String BLOCK = "BLOCK";
     public static final String NUMERIC_TYPE = "N";
     /**
      * Workbook with all data (factors, constants, condition, etc)
      */
     private Workbook workbook;
     /**
      * Reference to local services
      */
     private CommonServices localServices;
     /**
      * DTO Study object (hibernate) to store in DB
      */
     private Study study;
     /**
      * Proxy to application services to perform operation in local and central
      */
     private AppServices servicioApp;
     /**
      * Contains all DTO Factors from Template, used to easy retrieving
      */
     private Map<String, Factor> mapAllFactors = new HashMap<String, Factor>();
     /**
      * Contains all DTO Factors (same order as in template)
      */
     private List<Factor> listAllFactors = new ArrayList<Factor>();
     /**
      * Contains all DTO Factors from Template only studyConditions, used to easy
      * retrieving
      */
     private List<Factor> listGroupStudyConditionFactor = new ArrayList<Factor>();
     /**
      * Contains all DTO Factors Conditions (same order as in template)
      */
     private List<Factor> listGroupConditionFactors = new ArrayList<Factor>();
     /**
      * All DTO HEADER Factors form template CONDITOIN=LABEL or FACTOR = LABEL
      * that groups all child items
      */
     private Map<String, Factor> mapGroupAllFactors = new HashMap<String, Factor>();
     /**
      * Contains DTO Factors form template (FACTOR section), used to easy
      * retrieving
      */
     private List<Factor> listGroupAllFactors = new ArrayList<Factor>();
     /**
      *
      */
     private List<List<Factor>> represtnFactors = new ArrayList<List<Factor>>();
     private Map<String, Factor> mapStudyFactors = new HashMap<String, Factor>();
     private List<Factor> listStudyFactors = new ArrayList<Factor>();
     private Map<String, Factor> mapTrialFactors = new HashMap<String, Factor>();
     private List<Factor> listTrialFactors = new ArrayList<Factor>();
     private Map<String, Factor> mapOtherFactors = new HashMap<String, Factor>();
     private List<Factor> listOtherFactors = new ArrayList<Factor>();
     private Map<String, Factor> mapEmtryFactors = new HashMap<String, Factor>();
     private List<Factor> listEntryFactors = new ArrayList<Factor>();
     private Map<String, Factor> mapPlotFactors = new HashMap<String, Factor>();
     private List<Factor> listPlotFactors = new ArrayList<Factor>();
     private Map<String, Factor> mapOnlyFactors = new HashMap<String, Factor>();
     private List<Factor> listOnlyFactors = new ArrayList<Factor>();
     private Map<String, Variate> mapConstantsVariates = new HashMap<String, Variate>();
     private List<Variate> listConstantsVariates = new ArrayList<Variate>();
     private Map<String, Variate> mapVariatesPure = new HashMap<String, Variate>();
     private List<Variate> listVariatesPure = new ArrayList<Variate>();
     private List<Obsunit> listObsunit = new ArrayList<Obsunit>();
     private List<Obsunit> listObsunitMeasurement = new ArrayList<Obsunit>();
     private Map<Integer, Represtn> mapReprestns = new HashMap<Integer, Represtn>();
     private Map<Integer, HelperIteratorObsnuitLevels> mapa = new HashMap<Integer, HelperIteratorObsnuitLevels>();
     private List<Integer> listaJumpers = new ArrayList<Integer>();
 
     public HelperWorkbook(Workbook workbook,
             CommonServices serviciosLocal,
             AppServices servicioApp) {
         this.workbook = workbook;
         this.localServices = serviciosLocal;
         this.servicioApp = servicioApp;
     }
 
     /**
      * Save all Study into database
      */
     public void saveStudy() {
 
         log.info("Savin study....");
         addStudyToDatabase();
         log.info("Savin study DONE!");
 
         //TODO revisar las dependencias contra steffect para el guardado contra
         //trial conditions, study conditions y todas las dependencias
 
         /**
          * Variable to identify current FACTORID
          */
         Integer factorCabecera = null;
 
 
         //=======================
         // Saving factors
         //=======================
         log.info("Saving Factor study....");
         factorCabecera = saveFactorStudy();
         log.info("Saving Factor study DONE!");
 
         log.info("Saving Factors for study....");
         factorCabecera = saveFactorsStudy(factorCabecera);
         log.info("Saving Factors for study DONE!");
 
         //NEW SCHEMA GCP-4378 saving dataset factors.  factors will not be saved yet
         log.info("Saving Factors for trial....");
         factorCabecera = saveFactorsTrial(factorCabecera);
         log.info("Saving Factors for trial DONE!");
 
         log.info("Saving Factors for each factor....");
         factorCabecera = saveFactorsFactor(factorCabecera);
         log.info("Saving Factors for each factor DONE!");
 
         //VARIATES at the STUDY LEVEL
         log.info("Saving Variates for each constant....");
         saveVariatesConstants();
         log.info("Saving Variates for each constant DONE!");
 
         //VARIATES at the DATASET LEVEL - Measurement Dataset
         log.info("Saving Variates for each constant....");
         saveVariatesVariate();
         log.info("Saving Variates for each constant DONE!");
 
         
         //=======================
         //Guardando STEFECTS
         //=======================
         //GCP-4378 no dataset needs to be created for steffects that represents study level factors
         //log.info("Saving Steffects for each study....");
         //List<Steffect> steffects = saveStefectsStudy();
         //log.info("Saving Steffects for each study DONE!");
         List<Steffect> steffects = new ArrayList<Steffect>();
         
         log.info("Saving Steffects for each trial....");
         saveStefectsTrial(steffects);
         log.info("Saving Steffects for each trial DONE!");
 
         log.info("Saving Steffects for each Measurement....");
         saveStefectsMeasurement(steffects);
         log.info("Saving Steffects for each Measurement DONE!");
 
         //=======================
         //Guardando REPRESTN
         //=======================
         log.info("Saving represtn....");
         List<Represtn> represtns = saveReprestn(steffects);
         log.info("Saving represtn DONE!");
 
         //=======================
         //Guardando VEFFECT
         //=======================
         //GCP-4378 not used anymore, commented out bec it's causing an issue
         //since we removed the study dataset.
         //log.info("Saving VEFFECT....");
         //saveVefects(represtns);
         //log.info("Saving VEFFECT DONE!");
 
         //=======================
         //Guardar effect
         //=======================
         log.info("Saving effect....");
         saveEffects(represtns);
         log.info("Saving effect DONE!");
 
         //=======================
         //Guardando levels
         //=======================
         // Get next ID for levels
         log.info("Recovery next levelNo....");
         //Integer levelNo = this.localServices.getNextLevelNo();
         log.info("Recovery next levelNo DONE!");
 
 
         //Integer levelNoNdGeoLocationId = this.localServices.addNdGeolocation();
 
         //Guardando levels for study
         log.info("Saving levels for study....");
         //levelno should be study id
         Integer levelNo = study.getStudyid();
         Integer studyNdExperimentId = saveLevelsStudy(levelNo, 1);
         log.info("Saving levels for study DONE!");
 
         //insert geolocation - daniel
         //level no = geolocation id
         //Guardando levels for Trials
         log.info("Saving levels for trial....");
         //levelNo is the nd_geolocationId
         List<Integer> levelNoNdGeolocationIds = new ArrayList<Integer>();
         List<Integer> trialNdExperimentIds = HelperFactor.saveLavelsFactorTrials(
                 getMapTrialFactors(),
                 workbook.getConditionsData(),
                 workbook.getInstanceNumber(),
                 levelNoNdGeolocationIds,
                 this.localServices);
         log.info("Saving levels for trial DONE!");
         //insert stock record - daniel
 
         //Salvar levels para grupos de convinaciones de PLOT
         log.info("Saving levels for plots....");
         //Integer levelNoNdExperimentId = this.localServices.addNdExperiment(levelNoNdGeoLocationId, 1155);
         //saveLevelsPlots(levelNo);
         List<Integer> ndExperimentIds = saveLevelsPlots(levelNoNdGeolocationIds);
         log.info("Saving levels for plots DONE!");
         List<Integer> allExperimentIds = new ArrayList<Integer>();
         int index = 0;
         for (Integer levelNoNdGeolocationId : levelNoNdGeolocationIds) {
             log.info("Saving levels for entrys....");
 	        HelperFactor.saveLavelsFactorsEntrys(
 	                getListEntryFactors(),
 	                workbook.getGermplasmData(),
 	                ndExperimentIds,
 	                index,
 	                levelNoNdGeolocationId,
 	                this.localServices);
 	        if(ndExperimentIds!=null || !ndExperimentIds.isEmpty()) { 
             	allExperimentIds.addAll(ndExperimentIds);
	        }
 	        log.info("Saving levels for entrys DONE!");
         
         }
 
         /*      move to upper part
          //Salvar levels para grupos de convinaciones de PLOT
          log.info("Saving levels for plots....");
          Integer levelNoNdExperimentId = this.localServices.addNdExperiment(levelNoNdGeoLocationId, 1155);
          //saveLevelsPlots(levelNo);
          saveLevelsPlots(levelNoNdExperimentId);
          log.info("Saving levels for plots DONE!");
          */
         //=======================
         //Salvar obsunit con todas la convinaciones datos
         //=======================
         List<Obsunit> obsunits = saveObsunit(represtns, trialNdExperimentIds, allExperimentIds);
 
 
         saveDataConstatnts(studyNdExperimentId);
 
         //=======================
         //Salvar oindex con todas la convinaciones datos
         //=======================
 
         saveOindex3(obsunits, represtns, this.localServices, workbook.getMeasurements());
 
     }
 
     /**
      * Save Study DTO into database and assign it STUDYID identifier used by all
      * factors and variates references
      *
      * @return
      */
     public void addStudyToDatabase() {
         //Armando el objeto StudyDTO con Study de Workbook
         study = ConverterDomainToDTO.getStudy(workbook.getStudy());
 
         if (study.getStudyid() == null) {
             localServices.addStudy(study);
         } else if (servicioApp.getStudy(study.getStudyid()) == null) {
             localServices.addStudy(study);
         }
     }
 
     /**
      *
      * Saves a factor study that groups all items for a study
      *
      * @return FactorId that group all factor for the study
      */
     public Integer saveFactorStudy() {
         Integer factorCabecera = null;
         Factor factor;
 
         // check if exists study conditions
         if (!workbook.getStudyConditions().isEmpty()) {
             Condition condition = workbook.getStudyConditions().get(0);
             // If template contains a condition with "STUDY"
             // then will be added later
             if (condition.getConditionName().equals("STUDY")) {//Si el primero es STUDY se agregara al agregar los condition
                 factorCabecera = 0;
             } else {
                 // Then add first study condition
                 factor = HelperFactor.saveFactorStudy(
                         study,
                         servicioApp,
                         localServices);
 
                 // add added factors to maps and lists
                 mapStudyFactors.put(factor.getFname(), factor);
                 listStudyFactors.add(factor);
                 mapAllFactors.put(factor.getFname(), factor);
                 listAllFactors.add(factor);
                 mapGroupAllFactors.put(factor.getFname(), factor);
                 listGroupAllFactors.add(factor);
                 factorCabecera = factor.getLabelid();
             }
         }
         return factorCabecera;
     }
 
     /**
      * Add the factors of the study to the database. Also checks if TMETHOD,
      * SCALES and TRAITS already exists. If don't exists then add new TMETHOD,
      * SCALES and TRATIS to local database. And create the relationship between
      * TRAIT and MEASUREDIN
      *
      * @param factorCabecera
      * @return FactorId
      */
     public Integer saveFactorsStudy(
             Integer factorCabecera) {
         Factor factor;
         char traitsType;
         Integer dmsatype;
         String dmsatab;
 
         traitsType = 'S';
         dmsatype = 801;
         dmsatab = "FACTOR";
 
         for (Condition condition : workbook.getStudyConditions()) {
             TmsMethod tmsMethod;
             Scales scales;
             Traits traits = new Traits();
             Measuredin measuredin;
             Dmsattr dmsattr;
 
             // Check if Method already exists
             TmsMethod tmsMethodFilter = new TmsMethod(true);
             // to search method by name
             tmsMethodFilter.setTmname(condition.getMethod());
             // method seach 
             List<TmsMethod> tmsMethodsList = servicioApp.getListTmsMethod(tmsMethodFilter, 0, 0, false);
             // if method found then retrieve it
             if (!tmsMethodsList.isEmpty()) {
                 // retrieve method from list
                 tmsMethod = tmsMethodsList.get(0);
             } else {
                 // method not found, then add to database
                 tmsMethod = ConverterDomainToDTO.getTmsMethod(condition.getMethod());
                 // add a new method
                 localServices.addTmsMethod(tmsMethod);
             }
 
             // Check if Scale already exists
             Scales scalesFilter = new Scales(true);
             // to search scale by name
             scalesFilter.setScname(condition.getScale());
             // serch method
             List<Scales> scalesList = servicioApp.getListScales(scalesFilter, 0, 0, false);
             // if scale found then retrive it
             if (!scalesList.isEmpty()) {
                 // retrive scale from list
                 scales = scalesList.get(0);
             } else {
                 // scale don't exists in database
                 scales = ConverterDomainToDTO.getScales(condition.getScale(), '-');
                 // add a new scale
                 localServices.addScales(scales);
             }
 
             // Check if Trait already exists in database
             Traits traitsFilter = new Traits(true);
             // to search trait by name
             traitsFilter.setTrname(condition.getProperty());
             // search trait in database
             List<Traits> traitsList = servicioApp.getListTraitsOnly(traitsFilter, 0, 0, false);
             // if trait found then retrive it 
             if (!traitsList.isEmpty()) {
                 // retrive trait from list
                 traits = traitsList.get(0);
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 
             } else {
                 // trait don't existe, then add to database
                 traits = ConverterDomainToDTO.getTraits(condition.getProperty());
                 traits.setTraittype(String.valueOf(traitsType));
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 // add trait to database
                 localServices.addTraits(traits);
             }
 
 
 
             //TODO agregar algoritmo para determinacion del standard scale
 
             //Verificar existencia de measuredin
             Measuredin measuredinFilter = new Measuredin(true);
             measuredinFilter.setScaleid(scales.getScaleid());
             measuredinFilter.setTraitid(traits.getTraitid());
             measuredinFilter.setTmethid(tmsMethod.getTmethid());
             measuredinFilter.setStoredinid(traits.getTid());
             measuredinFilter.setName(condition.getConditionName());
             
             List<Measuredin> measuredinList = servicioApp.getListMeasuredin(measuredinFilter, 0, 0, false);
             if (!measuredinList.isEmpty()) {
                 measuredin = measuredinList.get(0);
             } else {
                 measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,condition.getConditionName(),condition.getDataType());
                 measuredin.setStoredinid(ChadoSchemaUtil.STUDY_VAR_TYPE);
                 traits.setTid(measuredin.getStoredinid());
                 localServices.addMeasuredin(measuredin);
             }
 
             //Asignando el measuredin en el traits
             traits.setMeasuredin(measuredin);
             measuredin.setScales(scales);
             measuredin.setTmsMethod(tmsMethod);
             traits.setTid(measuredin.getStoredinid());
             
             //Verificar factor
             factor = ConverterDomainToDTO.getFactor(condition.getConditionName(), condition.getDataType(), study, traits, tmsMethod);
             factor.setFactorid(factorCabecera);//Asignando el factorid
             localServices.addFactor(factor);
 
             //Verificar si es factor encabezado
             if (condition.getConditionName().equals(condition.getLabel())) {
                 factorCabecera = factor.getLabelid();
                 factor.setFactorid(factorCabecera);
                 localServices.updateFactor(factor);
                 mapGroupAllFactors.put(factor.getFname(), factor);
                 listGroupAllFactors.add(factor);
             }
 
 
             //Verificar dmsattr
             dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, factor.getLabelid(), condition.getDescription());
 //            localServices.addDmsattr(dmsattr);
 
             mapStudyFactors.put(factor.getFname(), factor);
             listStudyFactors.add(factor);
             mapAllFactors.put(factor.getFname(), factor);
             listAllFactors.add(factor);
         }
         return factorCabecera;
     }
 
     /**
      * Add the factors of the trial to the database Also checks if TMETHOD,
      * SCALES and TRAITS already exists. If don't exists then add new TMETHOD,
      * SCALES and TRATIS to local database. And create the relationship between
      * TRAIT and MEASUREDIN
      *
      * @param factorCabecera
      * @return FactorId
      */
     public Integer saveFactorsTrial(Integer factorCabecera) {
         Factor factor;
         char traitsType;
         Integer dmsatype;
         String dmsatab;
 
         traitsType = 'T';
         dmsatype = 801;
         dmsatab = "FACTOR";
 
         for (Condition condition : workbook.getConditions()) {
             TmsMethod tmsMethod;
             Scales scales;
             Traits traits = new Traits();
             Measuredin measuredin;
             Dmsattr dmsattr;
 
             // Check if Method already exists
             TmsMethod tmsMethodFilter = new TmsMethod(true);
             // to search method by name
             tmsMethodFilter.setTmname(condition.getMethod());
             // method seach 
             List<TmsMethod> tmsMethodsList = servicioApp.getListTmsMethod(tmsMethodFilter, 0, 0, false);
             // if method found then retrieve it
             if (!tmsMethodsList.isEmpty()) {
                 // retrieve method from list
                 tmsMethod = tmsMethodsList.get(0);
             } else {
                 // method not found, then add to database
                 tmsMethod = ConverterDomainToDTO.getTmsMethod(condition.getMethod());
                 // add a new method
                 localServices.addTmsMethod(tmsMethod);
             }
 
             //Verificar existencia de scales
             Scales scalesFilter = new Scales(true);
             scalesFilter.setScname(condition.getScale());
             List<Scales> scalesList = servicioApp.getListScales(scalesFilter, 0, 0, false);
             if (!scalesList.isEmpty()) {
                 scales = scalesList.get(0);
             } else {
                 scales = ConverterDomainToDTO.getScales(condition.getScale(), '-');
                 localServices.addScales(scales);
             }
 
             //Verificar existencia de traits
             Traits traitsFilter = new Traits(true);
             traitsFilter.setTrname(condition.getProperty());
             List<Traits> traitsList = servicioApp.getListTraitsOnly(traitsFilter, 0, 0, false);
             if (!traitsList.isEmpty()) {
                 traits = traitsList.get(0);
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
             } else {
                 traits = ConverterDomainToDTO.getTraits(condition.getProperty());
                 traits.setTraittype(String.valueOf(traitsType));
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 localServices.addTraits(traits);
             }
 
             //TODO agregar algoritmo para determinacion del standard scale
 
             //Verificar existencia de measuredin
             Measuredin measuredinFilter = new Measuredin(true);
             measuredinFilter.setScaleid(scales.getScaleid());
             measuredinFilter.setTraitid(traits.getTraitid());
             measuredinFilter.setTmethid(tmsMethod.getTmethid());
             measuredinFilter.setStoredinid(traits.getTid());
             measuredinFilter.setName(condition.getConditionName());
             
             List<Measuredin> measuredinList = servicioApp.getListMeasuredin(measuredinFilter, 0, 0, false);
             if (!measuredinList.isEmpty()) {
                 measuredin = measuredinList.get(0);
             } else {
                 measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,condition.getConditionName(),condition.getDataType());
                 measuredin.setStoredinid(ChadoSchemaUtil.TRIAL_ENVT_VAR_TYPE);
                 traits.setTid(measuredin.getStoredinid());
                 localServices.addMeasuredin(measuredin);
             }
 
             //Asignando el measuredin en el traits
             traits.setMeasuredin(measuredin);
             measuredin.setScales(scales);
             measuredin.setTmsMethod(tmsMethod);
             traits.setTid(measuredin.getStoredinid());
             //Verificar factor
             factor = ConverterDomainToDTO.getFactor(condition.getConditionName(), condition.getDataType(), study, traits, tmsMethod);
             factor.setFactorid(factorCabecera);//Asignando el factorid
             //localServices.addFactor(factor);
 
             //Verificar si es factor encabezado
             if (condition.getConditionName().equals(condition.getLabel())) {
                 factorCabecera = factor.getLabelid();
                 factor.setFactorid(factorCabecera);
                 //localServices.updateFactor(factor);
                 mapGroupAllFactors.put(factor.getFname(), factor);
                 listGroupAllFactors.add(factor);
             }
 
             //Verificar dmsattr
             dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, factor.getLabelid(), condition.getDescription());
 //            localServices.addDmsattr(dmsattr);
 
             mapTrialFactors.put(factor.getFname(), factor);
             listTrialFactors.add(factor);
             mapAllFactors.put(factor.getFname(), factor);
             listAllFactors.add(factor);
         }
         return factorCabecera;
     }
 
     /**
      * Add the factors of the otherFactors to the database Also categorize
      * FACTOR in PLOT FACTORS and ENTRY FACTORS
      *
      * @param factorCabecera
      * @return FactorId
      */
     public Integer saveFactorsFactor(Integer factorCabecera) {
         Factor factor;
         char traitsType;
         Integer dmsatype;
         String dmsatab;
 
         traitsType = 'F';
         dmsatype = 801;
         dmsatab = "FACTOR";
 
         for (ibfb.domain.core.Factor factorDomain : workbook.getFactors()) {
             TmsMethod tmsMethod;
             Scales scales;
             Traits traits = new Traits();
             Measuredin measuredin;
             Dmsattr dmsattr;
             int storedInType = ChadoSchemaUtil.getStoredInVariableType(
                     factorDomain.getProperty(), factorDomain.getScale());
 
             // Check if Method already exists
             TmsMethod tmsMethodFilter = new TmsMethod(true);
             // to search method by name
             tmsMethodFilter.setTmname(factorDomain.getMethod());
             // method seach 
             List<TmsMethod> tmsMethodsList = servicioApp.getListTmsMethod(tmsMethodFilter, 0, 0, false);
             // if method found then retrieve it
             if (!tmsMethodsList.isEmpty()) {
                 // retrieve method from list
                 tmsMethod = tmsMethodsList.get(0);
             } else {
                 // method not found, then add to database
                 tmsMethod = ConverterDomainToDTO.getTmsMethod(factorDomain.getMethod());
                 // add a new method
                 localServices.addTmsMethod(tmsMethod);
             }
 
             //Verificar existencia de scales
             Scales scalesFilter = new Scales(true);
             scalesFilter.setScname(factorDomain.getScale());
             List<Scales> scalesList = servicioApp.getListScales(scalesFilter, 0, 0, false);
             if (!scalesList.isEmpty()) {
                 scales = scalesList.get(0);
             } else {
                 scales = ConverterDomainToDTO.getScales(factorDomain.getScale(), '-');
                 localServices.addScales(scales);
             }
 
             //Verificar existencia de traits
             Traits traitsFilter = new Traits(true);
             traitsFilter.setTrname(factorDomain.getProperty());
             List<Traits> traitsList = servicioApp.getListTraitsOnly(traitsFilter, 0, 0, false);
             if (!traitsList.isEmpty()) {
                 traits = traitsList.get(0);
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
             } else {
                 traits = ConverterDomainToDTO.getTraits(factorDomain.getProperty());
                 traits.setTraittype(String.valueOf(traitsType));
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 localServices.addTraits(traits);
             }
 
             //TODO agregar algoritmo para determinacion del standard scale
 
             //Verificar existencia de measuredin
             Measuredin measuredinFilter = new Measuredin(true);
             measuredinFilter.setScaleid(scales.getScaleid());
             measuredinFilter.setTraitid(traits.getTraitid());
             measuredinFilter.setTmethid(tmsMethod.getTmethid());
             measuredinFilter.setStoredinid(traits.getTid());
             measuredinFilter.setName(factorDomain.getFactorName());
             List<Measuredin> measuredinList = servicioApp.getListMeasuredin(measuredinFilter, 0, 0, false);
             if (!measuredinList.isEmpty()) {
                 measuredin = measuredinList.get(0);
             } else {
                 measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,factorDomain.getFactorName(),factorDomain.getDataType());
                 measuredin.setStoredinid(storedInType); 
                 traits.setTid(measuredin.getStoredinid());
                 localServices.addMeasuredin(measuredin);
             }
 
             //Asignando el measuredin en el traits
             traits.setMeasuredin(measuredin);
             measuredin.setScales(scales);
             measuredin.setTmsMethod(tmsMethod);
             traits.setTid(measuredin.getStoredinid());
             //Verificar factor
             factor = ConverterDomainToDTO.getFactor(factorDomain.getFactorName(), factorDomain.getDataType(), study, traits, tmsMethod);
             factor.setFactorid(factorCabecera);//Asignando el factorid
             //localServices.addFactor(factor);
 
             //Verificar si es factor encabezado
             if (factorDomain.getFactorName().equals(factorDomain.getLabel())) {
                 factorCabecera = factor.getLabelid();
                 factor.setFactorid(factorCabecera);
                 //localServices.updateFactor(factor);
                 mapGroupAllFactors.put(factor.getFname(), factor);
                 listGroupAllFactors.add(factor);
             }
 
             //Verificar dmsattr
             dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, factor.getLabelid(), factorDomain.getDescription());
 //            localServices.addDmsattr(dmsattr);
 
             // Categorize each factor used to identify different groups (ENTRY and PLOT)
             if (factorDomain.getLabel().equals(workbook.getEntryLabel())) {//ENTRY_LABEL
                 mapEmtryFactors.put(factor.getFname(), factor);
                 listEntryFactors.add(factor);
             } else if (factorDomain.getLabel().equals(workbook.getPlotLabel())) {//PLOT_LABEL
                 mapPlotFactors.put(factor.getFname(), factor);
                 listPlotFactors.add(factor);
             } else {
                 mapOtherFactors.put(factor.getFname(), factor);
                 listOtherFactors.add(factor);
             }
             mapAllFactors.put(factor.getFname(), factor);
             mapOnlyFactors.put(factor.getFname(), factor);
             listAllFactors.add(factor);
             listOnlyFactors.add(factor);
         }
         return factorCabecera;
     }
 
     /**
      * Add the variates of the constants to the database
      */
     public void saveVariatesConstants() {
         char traitsType;
         Integer dmsatype;
         String dmsatab;
 
         traitsType = 'C';
         dmsatype = 802;
         dmsatab = "VARIATE";
 
         for (ibfb.domain.core.Constant constant : workbook.getConstants()) {
             TmsMethod tmsMethod;
             Scales scales;
             Traits traits = new Traits();
             Measuredin measuredin;
             Variate variate;
             Dmsattr dmsattr;
 
             // Check if Method already exists
             TmsMethod tmsMethodFilter = new TmsMethod(true);
             // to search method by name
             tmsMethodFilter.setTmname(constant.getMethod());
             // method seach 
             List<TmsMethod> tmsMethodsList = servicioApp.getListTmsMethod(tmsMethodFilter, 0, 0, false);
             // if method found then retrieve it
             if (!tmsMethodsList.isEmpty()) {
                 // retrieve method from list
                 tmsMethod = tmsMethodsList.get(0);
             } else {
                 // method not found, then add to database
                 tmsMethod = ConverterDomainToDTO.getTmsMethod(constant.getMethod());
                 // add a new method
                 localServices.addTmsMethod(tmsMethod);
             }
 
             //Verificar existencia de scales
             Scales scalesFilter = new Scales(true);
             scalesFilter.setScname(constant.getScale());
             List<Scales> scalesList = servicioApp.getListScales(scalesFilter, 0, 0, false);
             if (!scalesList.isEmpty()) {
                 scales = scalesList.get(0);
             } else {
                 scales = ConverterDomainToDTO.getScales(constant.getScale(), '-');
                 localServices.addScales(scales);
             }
 
             //Verificar existencia de traits
             Traits traitsFilter = new Traits(true);
             traitsFilter.setTrname(constant.getProperty());
             List<Traits> traitsList = servicioApp.getListTraitsOnly(traitsFilter, 0, 0, false);
             if (!traitsList.isEmpty()) {
                 traits = traitsList.get(0);
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
             } else {
                 traits = ConverterDomainToDTO.getTraits(constant.getProperty());
                 traits.setTraittype(String.valueOf(traitsType));
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 localServices.addTraits(traits);
             }
 
             //TODO agregar algoritmo para determinacion del standard scale
 
             //Verificar existencia de measuredin
             Measuredin measuredinFilter = new Measuredin(true);
             measuredinFilter.setScaleid(scales.getScaleid());
             measuredinFilter.setTraitid(traits.getTraitid());
             measuredinFilter.setTmethid(tmsMethod.getTmethid());
             measuredinFilter.setStoredinid(traits.getTid());
             measuredinFilter.setName(constant.getConstantName());
             List<Measuredin> measuredinList = servicioApp.getListMeasuredin(measuredinFilter, 0, 0, false);
             if (!measuredinList.isEmpty()) {
                 measuredin = measuredinList.get(0);
             } else {
                 measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,constant.getConstantName(), constant.getDataType());
                 measuredin.setStoredinid(ChadoSchemaUtil.OBSERVATION_VARIATE_TYPE);
                 traits.setTid(measuredin.getStoredinid());
                 localServices.addMeasuredin(measuredin);
             }
 
             //Asignando el measuredin en el traits
             traits.setMeasuredin(measuredin);
             measuredin.setScales(scales);
             measuredin.setTmsMethod(tmsMethod);
             traits.setTid(measuredin.getStoredinid());
             //Verificar variate
             variate = ConverterDomainToDTO.getVariate(constant.getConstantName(), constant.getDataType(), study, traits, tmsMethod);
             variate.setVtype(vtype);
             localServices.addVariate(variate);
 
             //Verificar dmsattr
             dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, variate.getVariatid(), constant.getDescription());
 //            localServices.addDmsattr(dmsattr);
 
 
             mapConstantsVariates.put(variate.getVname(), variate);
             listConstantsVariates.add(variate);
             constant.setVariateId(variate.getVariatid());
             // ADD TO VEFFECT variate ID and  represNO from REPRESTN table for each constant variate
         }
     }
 
     /**
      * Add the variates of the variates to the database
      */
     public void saveVariatesVariate() {
         char traitsType;
         Integer dmsatype;
         String dmsatab;
 
         traitsType = 'V';
         dmsatype = 802;
         dmsatab = "VARIATE";
 
         for (ibfb.domain.core.Variate variateDomain : workbook.getVariates()) {
             TmsMethod tmsMethod;
             Scales scales;
             Traits traits = new Traits();
             Measuredin measuredin;
             Variate variate;
             Dmsattr dmsattr;
 
             // Check if Method already exists
             TmsMethod tmsMethodFilter = new TmsMethod(true);
             // to search method by name
             tmsMethodFilter.setTmname(variateDomain.getMethod());
             // method seach 
             List<TmsMethod> tmsMethodsList = servicioApp.getListTmsMethod(tmsMethodFilter, 0, 0, false);
             // if method found then retrieve it
             if (!tmsMethodsList.isEmpty()) {
                 // retrieve method from list
                 tmsMethod = tmsMethodsList.get(0);
             } else {
                 // method not found, then add to database
                 tmsMethod = ConverterDomainToDTO.getTmsMethod(variateDomain.getMethod());
                 // add a new method
                 localServices.addTmsMethod(tmsMethod);
             }
 
             //Verificar existencia de scales
             Scales scalesFilter = new Scales(true);
             scalesFilter.setScname(variateDomain.getScale());
             List<Scales> scalesList = servicioApp.getListScales(scalesFilter, 0, 0, false);
             if (!scalesList.isEmpty()) {
                 scales = scalesList.get(0);
             } else {
                 scales = ConverterDomainToDTO.getScales(variateDomain.getScale(), '-');
                 localServices.addScales(scales);
             }
 
             //Verificar existencia de traits
             Traits traitsFilter = new Traits(true);
             traitsFilter.setTrname(variateDomain.getProperty());
             List<Traits> traitsList = servicioApp.getListTraitsOnly(traitsFilter, 0, 0, false);
             if (!traitsList.isEmpty()) {
                 traits = traitsList.get(0);
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
             } else {
                 traits = ConverterDomainToDTO.getTraits(variateDomain.getProperty());
                 traits.setTraittype(String.valueOf(traitsType));
                 traits.setTid(servicioApp.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 localServices.addTraits(traits);
             }
 
             //TODO agregar algoritmo para determinacion del standard scale
 
             //Verificar existencia de measuredin
             Measuredin measuredinFilter = new Measuredin(true);
             measuredinFilter.setScaleid(scales.getScaleid());
             measuredinFilter.setTraitid(traits.getTraitid());
             measuredinFilter.setTmethid(tmsMethod.getTmethid());
             measuredinFilter.setStoredinid(traits.getTid());
             measuredinFilter.setName(variateDomain.getVariateName());
             List<Measuredin> measuredinList = servicioApp.getListMeasuredin(measuredinFilter, 0, 0, false);
             if (!measuredinList.isEmpty()) {
                 measuredin = measuredinList.get(0);
             } else {
                 measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,variateDomain.getVariateName(),variateDomain.getDataType());
                 measuredin.setStoredinid(ChadoSchemaUtil.OBSERVATION_VARIATE_TYPE);
                 traits.setTid(measuredin.getStoredinid());
                 localServices.addMeasuredin(measuredin);
             }
 
             //Asignando el measuredin en el traits
             traits.setMeasuredin(measuredin);
             measuredin.setScales(scales);
             measuredin.setTmsMethod(tmsMethod);
             traits.setTid(measuredin.getStoredinid());
             //Verificar factor
             variate = ConverterDomainToDTO.getVariate(variateDomain.getVariateName(), variateDomain.getDataType(), study, traits, tmsMethod);
             variate.setVtype(vtype);
             //localServices.addVariate(variate);
 
             //Verificar dmsattr
             //dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, variate.getVariatid(), variateDomain.getDescription());
 //            localServices.addDmsattr(dmsattr);
 
             mapVariatesPure.put(variate.getVname(), variate);
             listVariatesPure.add(variate);
             variateDomain.setVariateId(variate.getVariatid());
             // ADD TO VEFFECT variate ID and  represNO from REPRESTN table for each constant variate
         }
     }
 
     /**
      * Saves Study Effect into database
      *
      * @return
      */
     public List<Steffect> saveStefectsStudy() {
         List<Steffect> steffects = new ArrayList<Steffect>();
         
         Steffect steffect = new Steffect();
         steffect.setStudyid(study.getStudyid());
         
         steffect.setEffectname(steffectNameStudy+"_"+study.getSname());
         localServices.addSteffect(steffect);
         steffects.add(steffect);
 
         if (listStudyFactors.size() > 0) {
             Factor factorTemp = listGroupAllFactors.get(0);
 
             listGroupStudyConditionFactor.add(factorTemp);
             listGroupConditionFactors.add(factorTemp);
 
             represtnFactors.add(listGroupStudyConditionFactor);
         }
         return steffects;
     }
 
     /**
      *
      * @param steffects
      */
     public void saveStefectsTrial(List<Steffect> steffects) {
 
         Steffect steffect = new Steffect();
         steffect.setStudyid(study.getStudyid());
         steffect.setEffectname(steffectNameTrial+"_"+study.getSname());
         localServices.addSteffect(steffect);
         steffects.add(steffect);
 
         if (listTrialFactors.size() > 0) {
             Factor factorTemp = listGroupAllFactors.get(1);
 
             listGroupConditionFactors.add(factorTemp);
 
             represtnFactors.add(listGroupConditionFactors);
         }
         
         //save the dataset factors
         for (Factor factor : this.listTrialFactors) {
             factor.setStudyid(steffect.getEffectid());
             localServices.addFactor(factor);
         }
  
     }
 
     /**
      *
      * @param steffects
      */
     public void saveStefectsMeasurement(List<Steffect> steffects) {
         Steffect steffectt = new Steffect();
         steffectt.setStudyid(study.getStudyid());
         // TODO: ajusstar los nombres de los factores a la combinacion
         steffectt.setEffectname(steffectNameMeasurement+"_"+study.getSname());
         localServices.addSteffect(steffectt);
         steffects.add(steffectt);
         represtnFactors.add(listGroupAllFactors);
         
         //save dataset factors
         for (Factor factor : this.listEntryFactors) {
             factor.setStudyid(steffectt.getEffectid());
             localServices.addFactor(factor);
         }
         for (Factor factor : this.listPlotFactors) {
             factor.setStudyid(steffectt.getEffectid());
             localServices.addFactor(factor);
         }
         for (Factor factor : this.listOtherFactors) {
             factor.setStudyid(steffectt.getEffectid());
             localServices.addFactor(factor);
         }        
         
         //save variates
         for (Variate variate : this.listVariatesPure) {
             variate.setStudyid(steffectt.getEffectid());
             localServices.addVariate(variate);
         }
     }
 
     /**
      * Stores data into REPRESTN by iterate over steffects
      *
      * @param steffects
      * @return
      */
     public List<Represtn> saveReprestn(List<Steffect> steffects) {
         List<Represtn> represtns = new ArrayList<Represtn>();
 
         for (Steffect steffect : steffects) {
             Represtn represtn = new Represtn();
             represtn.setEffectid(steffect.getEffectid());
             represtn.setRepresname(steffect.getEffectname());
 
             if (represtn.getRepresname().equals(steffectNameStudy+"_"+study.getSname())) {
                 List<Factor> factorsTemp = new ArrayList<Factor>(0);
                 factorsTemp.add(mapGroupAllFactors.get(Workbook.STUDY));
                 represtn.setFactors(factorsTemp);
             } else if (represtn.getRepresname().equals(steffectNameTrial+"_"+study.getSname())) {
                 represtn.setFactors(listGroupConditionFactors);
             } else {
                 represtn.setFactors(listGroupAllFactors);
             }
 
             localServices.addReprestn(represtn);
             represtns.add(represtn);
             mapReprestns.put(represtn.getEffectid(), represtn);
         }
         //Asignando Group factors a represtn
 //        int i = 0;
 //        for (Represtn represtn : represtns) {
 //            represtn.setFactors(represtnFactors.get(i));
 //            i++;
 //        }
         return represtns;
     }
 
     public void saveVefects(List<Represtn> represtns) {
         Integer idConstants = represtns.get(1).getRepresno();
         Integer idVariates = represtns.get(2).getRepresno();
         for (Variate variate : listConstantsVariates) {
             Veffect veffect = new Veffect();
             VeffectPK veffectPK = new VeffectPK();
             veffectPK.setRepresno(idConstants);
             veffectPK.setVariatid(variate.getVariatid());
             veffect.setVeffectPK(veffectPK);
             localServices.addVeffect(veffect);
         }
         for (Variate variate : listVariatesPure) {
             Veffect veffect = new Veffect();
             VeffectPK veffectPK = new VeffectPK();
             veffectPK.setRepresno(idVariates);
             veffectPK.setVariatid(variate.getVariatid());
             veffect.setVeffectPK(veffectPK);
             localServices.addVeffect(veffect);
         }
     }
 
     /**
      * Save effects of study
      *
      * @param represtns
      */
     public void saveEffects(List<Represtn> represtns) {
         List<Effect> effects = new ArrayList<Effect>();
         for (Represtn represtn : represtns) {
             for (Factor factorTemp : represtn.getFactors()) {
                 Effect effect = new Effect();
                 EffectPK effectPK = new EffectPK(
                         represtn.getRepresno(),
                         factorTemp.getFactorid(),
                         represtn.getEffectid());
                 effect.setEffectPK(effectPK);
 
                 localServices.addEffect(effect);
                 effects.add(effect);
             }
         }
     }
 
     /**
      * Save levels of study only for fist block STUDY conditions
      *
      * @param levelNo
      * @return
      */
     public Integer saveLevelsStudy(Integer levelNo, Integer levelNoNdGeoLocationId) {
         Factor factorStudy = mapStudyFactors.get(labelStudy);
         // Save all levels for study
         HelperFactor.saveLevel(factorStudy, levelNo, study.getSname(), localServices);
 
         Integer ndExperimentId = localServices.addNdExperiment(levelNoNdGeoLocationId, 1010);
         localServices.addOindex(ndExperimentId, study.getStudyid());
         
         
         // Save level for each factor in study condition
         for (ibfb.domain.core.Condition condition : workbook.getStudyConditions()) {
             Factor factorStudyTemp = mapStudyFactors.get(condition.getConditionName());
             if (condition.getValue() == null) {
                 // BEGIN assuming studyConditions collection never containts a conditionName with
                 // STUDY condition as NAME
                 //if (condition.getConditionName().equals("STUDY")) {
                 //    HelperFactor.saveLevel(factorStudyTemp, levelNo, study.getSname(), localServices);
                 //} else 
                 // END ASSUMING
 // TODO: ajusstar los nombres de los factores a la combinacion
                 if (condition.getConditionName().equals("TID")) {
                     HelperFactor.saveLevel(factorStudyTemp, levelNo, study.getStudyid(), localServices);
                 } else {
 //                    if (factorStudyTemp.getLtype().equals("N")) {
 //                        HelperFactor.saveLevel(factorStudyTemp, levelNo, study.getStudyid(), localServices);
 //                    } else {
 //                        HelperFactor.saveLevel(factorStudyTemp, levelNo, study.getSname(), localServices);
 //                    }
                 }
             } else {
                 HelperFactor.saveLevel(factorStudyTemp, levelNo, condition.getValue(), localServices);
             }
         }
         HelperFactor.addLevels(listStudyFactors.get(0).getFactorid(), levelNo, localServices);
         levelNo--;
         return ndExperimentId;
     }
 
     /**
      * Save levels of plots
      *
      * @param levelNo
      */
     public List<Integer> saveLevelsPlots(List<Integer> levelNoNdGeoLocationIds) {
         Factor factorDeHeader = new Factor();
         List<String> listHeaders = workbook.getMeasurementHeaders();
         boolean agregado = false;
         List<Integer> ndExperimentIds = new ArrayList<Integer>();
         //for (Factor factorGroupTemp : listPlotFactors) {
         int noOfTrials = levelNoNdGeoLocationIds.size();
         int noOfPlots = workbook.getMeasurementsRep().size();
         int div = noOfPlots/noOfTrials;
         int ctr = 0;
         int index = 0;
         System.out.println("COMPARE MEASUREMENTSREP AND GERMPLAMSDATA SIZE: "+ workbook.getMeasurementsRep().size() +
         		" --- " + workbook.getGermplasmData().size());
         for (Measurement measurement : workbook.getMeasurementsRep()) {
         	if(ctr==div) {//get next geolocatioid
         		index++;
         		ctr = 0;
         	}
         	ctr++;
             Integer levelNo = localServices.addNdExperiment(levelNoNdGeoLocationIds.get(index), 1155);
             ndExperimentIds.add(levelNo);
             System.out.println("saveLevelsPlots - new ndExperimentId: "+ levelNo);
             for (String header : listHeaders) {
                 factorDeHeader = mapPlotFactors.get(header);
 // TODO: ajusstar los nombres de los factores a la combinacion
                 if (factorDeHeader != null) {
                     if (factorDeHeader.getFname().equals(workbook.getPlotLabel())) {
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getPlot(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getRepLabel())) {//"REP"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getReplication(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getBlockLabel())) {//"BLOCK"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getBlock(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getRowLabel())) {//"ROW"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getRow(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getColLabel())) {//"COL"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getColumn(),
                                 localServices);
                         agregado = true;
                     } else {
                         System.out.println("Unrecognized");
                     }
                 }
 
 
             }
             if (agregado) {
                 //HelperFactor.addLevels(factorGroupTemp.getFactorid(), levelNo, localServices);
                 //HelperFactor.addLevels(factorDeHeader.getFactorid(), levelNo, localServices);
                 //levelNo--;
                 agregado = false;
             }
         }
         return ndExperimentIds;
         //}
     }
 
     public void saveLevelsPlotsOld(Integer levelNo) {
         Factor factorDeHeader = new Factor();
         List<String> listHeaders = workbook.getMeasurementHeaders();
         boolean agregado = false;
         //for (Factor factorGroupTemp : listPlotFactors) {
         for (Measurement measurement : workbook.getMeasurementsRep()) {
             for (String header : listHeaders) {
                 factorDeHeader = mapPlotFactors.get(header);
                 // TODO: ajusstar los nombres de los factores a la combinacion
                 if (factorDeHeader != null) {
                     if (factorDeHeader.getFname().equals(workbook.getPlotLabel())) {
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getPlot(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getRepLabel())) {//"REP"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getReplication(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getBlockLabel())) {//"BLOCK"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getBlock(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getRowLabel())) {//"ROW"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getRow(),
                                 localServices);
                         agregado = true;
                     } else if (factorDeHeader.getFname().equals(workbook.getColLabel())) {//"COL"
                         HelperFactor.saveLevel(factorDeHeader,
                                 levelNo,
                                 measurement.getColumn(),
                                 localServices);
                         agregado = true;
                     } else {
                         System.out.println("Unrecognized");
                     }
                 }
 
 
             }
             if (agregado) {
                 //HelperFactor.addLevels(factorGroupTemp.getFactorid(), levelNo, localServices);
                 //HelperFactor.addLevels(factorDeHeader.getFactorid(), levelNo, localServices);
                 levelNo--;
                 agregado = false;
             }
         }
         //}
     }
 
     /**
      * Insert into OBSUNIT all records from Represtn
      *
      * @param represtns
      * @return
      */
     public List<Obsunit> saveObsunit(
             List<Represtn> represtns,
             List<Integer> trialNdExperimentIds,
             List<Integer> experimentIds) {
         int iterando = 0;
         Obsunit obsunit;
         List<Obsunit> obsunits = new ArrayList<Obsunit>();
         /*if (mapAllFactors.get(Workbook.STUDY) != null) {
             obsunit = new Obsunit();
             //GCP-4378 study level variates
             //obsunit.setEffectid(represtns.get(iterando).getEffectid());
             obsunit.setEffectid(study.getStudyid());
             //iterando++;
             localServices.addObsunit(obsunit);
             obsunits.add(obsunit);
         }*/
 
         //if (mapAllFactors.get(Workbook.TRIAL_LABEL) != null) {
         if (mapAllFactors.get(workbook.getTrialLabel()) != null || mapAllFactors.get("TRIAL") != null) {
             for (int numberInstance = 0; numberInstance < workbook.getInstanceNumber().intValue(); numberInstance++) {
                 obsunit = new Obsunit();
                 obsunit.setEffectid(represtns.get(iterando).getEffectid());
                 obsunit.setOunitid(trialNdExperimentIds.get(numberInstance));
                 //localServices.addObsunit(obsunit);
                 obsunits.add(obsunit);
                 listObsunit.add(obsunit);
 
             }
             iterando++;
             
         }
         int i = 0;
         for (Measurement measurement : workbook.getMeasurements()) {
         	obsunit = new Obsunit();
             obsunit.setEffectid(represtns.get(iterando).getEffectid());
             obsunit.setOunitid(experimentIds.get(i++));
             //localServices.addObsunit(obsunit);
             obsunits.add(obsunit);
             listObsunitMeasurement.add(obsunit);
 
             // save dataN and DataC
             for (MeasurementData data : measurement.getMeasurementsData()) {
                 // look for saved variate
                 Variate savedVariate = mapVariatesPure.get(data.getVariate().getVariateName());
                 addDataNorDataC(obsunit, data, savedVariate);
 
             }
         }
         return obsunits;
     }
 
     /**
      * Adds a value to DATA_C or DATA_N table
      *
      * @param obsunit
      * @param data
      * @param savedVariate
      */
     private void addDataNorDataC(Obsunit obsunit, MeasurementData data, Variate savedVariate) {
         if (savedVariate != null) {
             //if (savedVariate.getDtype().equals("N")) {
             if (data.getDataType().equals("N")) {
                 DataNPK dataNPK = new DataNPK();
                 dataNPK.setOunitid(obsunit.getOunitid());
                 dataNPK.setVariatid(savedVariate.getVariatid());
                 DataN dataN = new DataN();
                 dataN.setDataNPK(dataNPK);
                 if (data.getValue() != null) {
                     DataN dataToSave = (DataN) data.getValue();
                     dataN.setDvalue(dataToSave.getDvalue());
                 } else {
                     dataN.setDvalue(new Double(0));
                 }
                 if (data.getValue() != null) {
                     localServices.addDataN(dataN);
                 }
             } else if (data.getDataType().equals("C")) {
                 DataCPK dataCPk = new DataCPK();
                 dataCPk.setOunitid(obsunit.getOunitid());
                 dataCPk.setVariatid(savedVariate.getVariatid());
                 DataC dataC = new DataC();
                 dataC.setDataCPK(dataCPk);
                 if (data.getValue() != null) {
                     DataC dataToSave = (DataC) data.getValue();
                     dataC.setDvalue(dataToSave.getDvalue());
                 } else {
                     dataC.setDvalue(" ");
                 }
                 if (data.getValue() != null) {
                     if (dataC.getDvalue() != null && !dataC.getDvalue().trim().isEmpty()) {
                         localServices.addDataC(dataC);
                     }
                 }
             }
         }
     }
 
     public void saveDataConstatnts(Integer ndExperimentId) {
 
         int instance = 0;
         instance--;
         String nameFactorInitial = "";
 
         if (workbook.getConstantsData().size() > 0) {
             nameFactorInitial = workbook.getConstantsData().get(0).getConstantName();
         }
 
         for (Constant constant : workbook.getConstantsData()) {
             Variate variateTemp = (Variate) mapConstantsVariates.get(constant.getConstantName());
             if (nameFactorInitial.equals(variateTemp.getVname())) {
                 instance++;
             }
             if (variateTemp.getDtype().equals(NUMERIC_TYPE)) {
                 DataN dataN = new DataN();
                 DataNPK dataNPK = new DataNPK();
                 dataNPK.setOunitid(ndExperimentId);
                 dataNPK.setVariatid(variateTemp.getVariatid());
                 dataN.setDataNPK(dataNPK);
                 dataN.setDvalue(HelperFactor.castingToDouble(constant.getValue()));
                 localServices.addDataN(dataN);
             } else {
                 DataC dataC = new DataC();
                 DataCPK dataCPK = new DataCPK();
                 dataCPK.setOunitid(ndExperimentId);
                 dataCPK.setVariatid(variateTemp.getVariatid());
                 dataC.setDataCPK(dataCPK);
                 String valueToAdd = HelperFactor.castingToString(constant.getValue());
                 if (valueToAdd != null && !valueToAdd.trim().isEmpty()) {
                     dataC.setDvalue(valueToAdd);
                     localServices.addDataC(dataC);
                 }
             }
         }
     }
 /*
     public void saveOindex(
             List<Obsunit> obsunits,
             List<Represtn> represtns,
             CommonServices serviciosLocal) {
 
         int iterando = 0;
         int iterandoEntry = 0;
         int repeticiones = 0;
         int repeticion = 0;
         Oindex oindex;
         Obsunit obsunitTemp = new Obsunit();
         List<Oindex> oindexs = new ArrayList<Oindex>();
 
         Represtn represtn = represtns.get(iterando);
 
 
 
         for (Obsunit obsunit : obsunits) {
 
             if (oindexs.size() > 0 && represtn.getRepresname().equals(steffectNameStudy+"_"+study.getSname())) {//represtn.getFactors().size() == 2 && repeticiones == 0
                 iterando++;
                 if (represtns.get(iterando - 1).getFactors().size() == 1 && repeticiones == 0) {
                     represtn = represtns.get(iterando);
                     repeticiones = represtn.getRepeticiones(steffectNameTrial+"_"+study.getSname()) - 1;
                 }
             } else {
                 if (repeticiones != 0) {
                     if (repeticion == repeticiones) {
                         iterando++;
                         represtn = represtns.get(iterando);
                     } else if (represtn.getRepresname().equals(steffectNameMeasurement+"_"+study.getSname())) {
                         if (repeticion > repeticiones) {
                             repeticion = -1;
                             iterandoEntry = 0;
                         }
 //                        else if(iterandoEntry > 0){
 //                            iterandoEntry = 0;
 //                        }
                     }
                     repeticion++;
                 }
             }
             for (Factor factorTemp : represtn.getFactors()) {
 
                 oindex = new Oindex();
 
                 if (represtn.getRepresname().equals(steffectNameMeasurement+"_"+study.getSname())) {
                     //if (factorTemp.getFname().equals(Workbook.ENTRY_LABEL)) {
                     if (factorTemp.getFname().equals(workbook.getEntryLabel())) {
                         OindexPK oindexPK = new OindexPK(
                                 obsunit.getOunitid(),
                                 factorTemp.getFactorid(),
                                 factorTemp.getLevelNo(iterandoEntry),
                                 represtn.getRepresno());
                         oindex.setOindexPK(oindexPK);
                         serviciosLocal.addOindex(oindex);
                         oindexs.add(oindex);
                         iterandoEntry++;
                     } else {
                         //TODO : iterar en levels
                         OindexPK oindexPK = new OindexPK(
                                 obsunit.getOunitid(),
                                 factorTemp.getFactorid(),
                                 factorTemp.getLevelNo(),
                                 represtn.getRepresno());
                         oindex.setOindexPK(oindexPK);
                         serviciosLocal.addOindex(oindex);
                         oindexs.add(oindex);
                     }
                 } else {
                     if (factorTemp.getFname().equals(Workbook.STUDY)) {
                         OindexPK oindexPK = new OindexPK(
                                 obsunit.getOunitid(),
                                 factorTemp.getFactorid(),
                                 factorTemp.getLevelNo(0),
                                 represtn.getRepresno());
                         oindex.setOindexPK(oindexPK);
                         serviciosLocal.addOindex(oindex);
                         oindexs.add(oindex);
                     } else {
                         log.info("Saving Oindex for factor" + factorTemp.getFname() + " with labelid = " + factorTemp.getLabelid());
                         OindexPK oindexPK = new OindexPK(
                                 obsunit.getOunitid(),
                                 factorTemp.getFactorid(),
                                 factorTemp.getLevelNo(repeticion),
                                 represtn.getRepresno());
                         oindex.setOindexPK(oindexPK);
                         serviciosLocal.addOindex(oindex);
                         oindexs.add(oindex);
                         log.info("Saving Oindex for factor" + factorTemp.getFname() + "DONE....");
                     }
                 }
             }
         }
     }
 */
     public void saveOindex2(
             List<Obsunit> obsunits,
             List<Represtn> represtns,
             CommonServices serviciosLocal) {
         Integer anteriorEffectid = 0;
         boolean incremento = false;
         for (Obsunit obsunit : obsunits) {
             Represtn represtn = mapReprestns.get(obsunit.getEffectid());
 
             if (anteriorEffectid != obsunit.getEffectid()) {
                 mapearFactores(represtn);
             }
 
             for (Factor factorTemp : represtn.getFactors()) {
                 HelperIteratorObsnuitLevels hiol = this.mapa.get(factorTemp.getFactorid());
                 for (int i = 0; i < factorTemp.getSizeLevels(); i++) {
 
                     if (!hiol.isUsado()) {
 //                        System.out.println("nameFactor " + factorTemp.getFname() + " jumper: " + represtn.getFactors().indexOf(factorTemp));
                         //if (factorTemp.getFname().equals(Workbook.TRIAL_LABEL) || factorTemp.getFname().equals(Workbook.ENTRY_LABEL)) {
                         if (factorTemp.getFname().equals(workbook.getTrialLabel()) || factorTemp.getFname().equals(workbook.getEntryLabel())) {
                             System.out.println(
                                     obsunit.getOunitid()
                                     + "\t"
                                     + factorTemp.getFactorid()
                                     + "\t"
                                     + factorTemp.getLevelNo(this.listaJumpers.get(represtn.getFactors().indexOf(factorTemp)))
                                     + "\t"
                                     + represtn.getRepresno());
 
                         } else {
                             System.out.println(
                                     obsunit.getOunitid()
                                     + "\t"
                                     + factorTemp.getFactorid()
                                     + "\t"
                                     + factorTemp.getLevelNo()
                                     + "\t"
                                     + represtn.getRepresno());
                         }
                         hiol.setUsado(true);
                         //if ((factorTemp.getFname().equals(Workbook.TRIAL_LABEL) || factorTemp.getFname().equals(Workbook.ENTRY_LABEL)) && !incremento) {
                         if ((factorTemp.getFname().equals(workbook.getTrialLabel()) || factorTemp.getFname().equals(workbook.getEntryLabel())) && !incremento) {
                             incrementar(represtn);
                             incremento = true;
                         }
 
 //                        System.out.println("nameFactor " + factorTemp.getFname() + " jumper: " + this.listaJumpers.get(represtn.getFactors().indexOf(factorTemp)));
 
                     }
                 }
                 limpiarUsados();
                 incremento = false;
             }
 
             anteriorEffectid = obsunit.getEffectid();
         }
     }
 
     public void saveOindex3(
             List<Obsunit> obsunits,
             List<Represtn> represtns,
             CommonServices serviciosLocal,
             List<Measurement> measurements) {
 /*        Represtn represtn = represtns.get(0);
         Factor factorTemp = represtn.getFactors().get(0);
         Obsunit obsunitTemp = obsunits.get(0);
         Oindex oindex = new Oindex();
         OindexPK oindexPK = new OindexPK(
                 obsunitTemp.getOunitid(),
                 factorTemp.getFactorid(),
                 factorTemp.getLevelNo(0),
                 represtn.getRepresno());
         oindex.setOindexPK(oindexPK);
         serviciosLocal.addOindex(oindex);
 */
         int i = 0;
         
         for (Obsunit obsunit : listObsunit) {
             Represtn represtnTrials = mapReprestns.get(obsunit.getEffectid());
             for (Factor factor : represtnTrials.getFactors()) {
                 if (factor.getFname().equals(Workbook.STUDY)) {
                     //serviciosLocal.addOindex(trialExperimentId, obsunit.getEffectid());
                 } else if (factor.getFname().equals(workbook.getTrialLabel()) || factor.getFname().equals("TRIAL")) {
                     serviciosLocal.addOindex(obsunit.getOunitid(), obsunit.getEffectid());
                 }
             }
          }
 
 
         for (Measurement measurement : measurements) {
             Obsunit obsunit = listObsunitMeasurement.get(measurements.indexOf(measurement));
             Represtn represtnMeasurements = mapReprestns.get(obsunit.getEffectid());
             serviciosLocal.addOindex(obsunit.getOunitid(), obsunit.getEffectid());
  /*           for (Factor factor : represtnMeasurements.getFactors()) {
 
                 if (factor.getFname().equals(Workbook.STUDY)) {
                     //serviciosLocal.addOindex(obsunit.getOunitid(), represtnMeasurements.getRepresno());
                 } else if (factor.getFname().equals(workbook.getTrialLabel()) || factor.getFname().equals("TRIAL")) {
                     serviciosLocal.addOindex(obsunit.getOunitid(), obsunit.getEffectid());
                 } else if (factor.getFname().equals(workbook.getEntryLabel()) || factor.getFname().equals("ENTRY")) {
                     serviciosLocal.addOindex(obsunit.getOunitid(), obsunit.getEffectid());
                 } else if (factor.getFname().equals(workbook.getPlotLabel()) || factor.getFname().equals("PLOT")) {
                     serviciosLocal.addOindex(obsunit.getOunitid(), obsunit.getEffectid());
                 }
             }*/
            i++;
         }
     }
 
     private void incrementar(Represtn represtn) {
 
         Integer acarreo = 0;
 
         for (int i = represtn.getFactors().size() - 1; i > 0; i--) {
             Factor factorTemp = (Factor) represtn.getFactors().get(i);
             //if (factorTemp.getFname().equals(Workbook.TRIAL_LABEL) || factorTemp.getFname().equals(Workbook.ENTRY_LABEL) || factorTemp.getFname().equals(Workbook.STUDY)) {
             if (factorTemp.getFname().equals(workbook.getTrialLabel()) || factorTemp.getFname().equals(workbook.getEntryLabel()) || factorTemp.getFname().equals(Workbook.STUDY)) {
                 Integer posicionActual = represtn.getFactors().indexOf(factorTemp);
                 Integer temp = this.listaJumpers.get(posicionActual);
 
                 if (acarreo > 0) {
                     temp += 2;
                     if (temp >= factorTemp.getSizeLevels()) {
                         acarreo = 1;
                         this.listaJumpers.set(represtn.getFactors().indexOf(factorTemp), 0);
                     } else {
                         this.listaJumpers.set(represtn.getFactors().indexOf(factorTemp), temp);
                         break;
                     }
                 } else {
                     if (temp + 1 >= factorTemp.getSizeLevels()) {
                         acarreo++;
                         this.listaJumpers.set(represtn.getFactors().indexOf(factorTemp), 0);
                     } else {
                         temp += 1;
                         this.listaJumpers.set(represtn.getFactors().indexOf(factorTemp), temp);
                         break;
                     }
                 }
             }
             i++;
         }
 
     }
 
     private void limpiarUsados() {
         for (HelperIteratorObsnuitLevels hiol : this.mapa.values()) {
             hiol.setUsado(false);
         }
     }
 
     private void mapearFactores(Represtn represtn) {
         this.listaJumpers = new ArrayList<Integer>();
         for (Factor factorTemp : represtn.getFactors()) {
             HelperIteratorObsnuitLevels hiol = new HelperIteratorObsnuitLevels();
 
             hiol.setFin(factorTemp.getSizeLevels());
             hiol.setJumper(0);
             hiol.setUsado(false);
 
             this.mapa.put(factorTemp.getFactorid(), hiol);
             //if (factorTemp.getFname().equals(Workbook.TRIAL_LABEL) || factorTemp.getFname().equals(Workbook.ENTRY_LABEL) || factorTemp.getFname().equals(Workbook.STUDY)) {
             if (factorTemp.getFname().equals(workbook.getTrialLabel()) || factorTemp.getFname().equals(workbook.getEntryLabel()) || factorTemp.getFname().equals(Workbook.STUDY)) {
                 this.listaJumpers.add(0);
             }
         }
     }
 
     public Map<String, Factor> getMapTrialFactors() {
         return mapTrialFactors;
     }
 
     public List<Factor> getListEntryFactors() {
         return listEntryFactors;
     }
 
     public List<Factor> getListOnlyFactors() {
         return listOnlyFactors;
     }
 }
