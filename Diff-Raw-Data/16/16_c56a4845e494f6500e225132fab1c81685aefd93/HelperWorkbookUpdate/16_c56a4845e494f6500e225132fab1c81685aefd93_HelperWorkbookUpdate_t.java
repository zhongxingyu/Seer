 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cimmyt.cril.ibwb.provider.helpers;
 
 import ibfb.domain.core.Study;
 import ibfb.domain.core.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.cimmyt.cril.ibwb.api.AppServices;
 import org.cimmyt.cril.ibwb.api.CommonServices;
 import org.cimmyt.cril.ibwb.domain.Factor;
 import org.cimmyt.cril.ibwb.domain.*;
 import org.cimmyt.cril.ibwb.domain.Variate;
 import org.cimmyt.cril.ibwb.provider.utils.ConverterDomainToDTO;
 
 /**
  * Utility class to update information for a Fieldbook
  *
  * @author TMSANCHEZ
  */
 public class HelperWorkbookUpdate {
 
     private static Logger log = Logger.getLogger(HelperWorkbookUpdate.class);
     /**
      * Workbook to update
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
     private AppServices appServices;
     public static final String NUMERIC_TYPE = "N";
     public List<Factor> factors = new ArrayList<Factor>();
     private List<Factor> factorsDto = new ArrayList<Factor>();
     private List<Variate> variatesDtoConstants = new ArrayList<Variate>();
     private List<Obsunit> obsUnitList = new ArrayList<Obsunit>();
     private List<Obsunit> obsUnitListTrials = new ArrayList<Obsunit>();
     private Obsunit studyObsunit = new Obsunit();
     private Integer effectid;
     public Factor factorStudy = new Factor();
     public Factor factorTrial = new Factor();
     public Factor factorEntry = new Factor();
     public Factor factorPlot = new Factor();
     Map<Integer, Factor> mapaLabes = new HashMap<Integer, Factor>();
     Map<String, Factor> mapaFactorsByName = new HashMap<String, Factor>();
     Map<String, Variate> mapaConstantsByName = new HashMap<String, Variate>();
     public static final String vtype = "MV";
     private org.cimmyt.cril.ibwb.domain.Study dbStudy;
 
     /**
      * Default Constructor
      *
      * @param workbook
      * @param localServices
      * @param appServices
      */
     public HelperWorkbookUpdate(Workbook workbook, CommonServices localServices, AppServices appServices) {
         this.workbook = workbook;
         this.localServices = localServices;
         this.appServices = appServices;
         this.study = workbook.getStudy();
     }
 
     /**
      * Updates all information in a fieldbook
      */
     public void update() {
         // first check if study already exists
         updateStudy();
         readFactors();
         saveOrUpdateLavelsFactorStudy(workbook.getStudyConditions());
         saveOrUpdateLavelsFactorTrials(workbook.getConditionsData());
         readConstants();
         readObsunitsTrials();
         readObsunitStudy();
         saveOrUpdateDataConstatnts(workbook.getConstantsData());
         readObsunitsMeasurement();
         saveOrUpdateVariates();
 
 
     }
 
     private void updateStudy() {
          dbStudy = ConverterDomainToDTO.getStudy(this.study);
         localServices.updateStudy(dbStudy);
     }
 
     private void readFactors() {
 
         HelperContentEffectidAndFactors hceaf = HelperEffect.getEffectidForMeasurementEffectAndFactors(
                 this.appServices,
                 this.study.getStudyid(),
                 this.factorStudy,
                 this.factorTrial,
                 this.factorEntry,
                 this.factorPlot);
         this.effectid = hceaf.getEffectid();
         //this.factorsDto = HelperFactor.getFactorsByEffectid(this.effectid, this.appServices);
         this.factorsDto = appServices.getFactorsByStudyId(study.getStudyid());
 
         factorStudy = hceaf.getFactorStudy();
         factorTrial = hceaf.getFactorTrial();
         factorEntry = hceaf.getFactorEntry();
         factorPlot = hceaf.getFactorPlot();
 
         for (Factor factorDto : this.factorsDto) {
             if (factorDto.getLabelid().equals(factorDto.getFactorid())) {
                 this.mapaLabes.put(factorDto.getLabelid(), factorDto);
             }
             Factor temp = HelperFactor.getFactorFillingFull(
                     factorDto,
                     this.appServices,
                     801);
             mapaFactorsByName.put(temp.getFname(), temp);
             if (temp.getLabelid().equals(factorTrial.getLabelid())) {
                 factorTrial = temp;
             }
         }
     }
 
     private void readConstants() {
         // get all variate from database marked as Traits
         //this.variatesDtoConstants = this.appServices.getListVariateConstants(this.study.getStudyid());
         
         String trialName = "TRIAL_"+ this.study.getStudy();
         Represtn effect = localServices.getReprestnForStudyId(this.study.getStudyid(), trialName);
         // get all variate from database marked as Traits
         this.variatesDtoConstants = this.appServices.getListVariateConstants(effect.getEffectid());
 
         // fill all variate as traits
         for (Variate variateDTO : this.variatesDtoConstants) {
             variateDTO = HelperVariate.getVariateConstantsFillingFull(
                     variateDTO,
                     //                this.localServices,
                     //                this.centralServices,
                     this.appServices,
                     802);
             this.mapaConstantsByName.put(variateDTO.getVname(), variateDTO);
         }
     }
 
     private void readObsunitsTrials() {
         this.obsUnitListTrials = this.appServices.getObsunitListByStudyTrials(study.getStudyid());
     }
     
     private void readObsunitStudy() {
     	this.studyObsunit = this.appServices.getStudyObsunit(study.getStudyid());
     }
 
     private void readObsunitsMeasurement() {
         this.obsUnitList = this.appServices.getObsunitListByStudy(study.getStudyid());
     }
 
     private void saveOrUpdateLavelsFactorStudy(
             List<Condition> conditionsData) {
         //not needed to change, assumption is that the levelN was inserted properly
         int levelNoTemporal = 0;
 
         LevelN levelNT = (LevelN) factorTrial.getLevelByIndex(0);
         levelNoTemporal = levelNT.getLevelNPK().getLevelno();
         for (Condition conditionData : conditionsData) {
             Factor factorTemp = (Factor) this.mapaFactorsByName.get(conditionData.getConditionName());
 
             log.info("Savin level for factor: " + conditionData.getConditionName() + "  with value: " + conditionData.getValue());
             if (conditionData.getDataType().equals(NUMERIC_TYPE)) {
                 if (factorTemp.getLevelByIndex(0) != null) {
                     LevelN levelN = (LevelN) factorTemp.getLevelByIndex(0);
                     levelN.setLvalue(HelperFactor.castingToDouble(conditionData.getValue()));
                     this.localServices.updateLevelN(levelN);
                 } else {
                     LevelN levelN = new LevelN();
                     levelN.setFactorid(factorTemp.getFactorid());
                     if (conditionData.getValue() != null) {
                         levelN.setLvalue(HelperFactor.castingToDouble(conditionData.getValue()));
                     } else {
                         levelN.setLvalue(new Double(0));
                     }
                     LevelNPK levelNPK = new LevelNPK();
                     levelNPK.setLabelid(factorTemp.getLabelid());
                     levelNPK.setLevelno(levelNoTemporal);
                     levelN.setLevelNPK(levelNPK);
                     this.localServices.addLevelN(levelN);
                 }
             } else {
                 if (factorTemp.getLevelByIndex(0) != null) {
                     LevelC levelC = (LevelC) factorTemp.getLevelByIndex(0);
                     levelC.setLvalue(HelperFactor.castingToString(conditionData.getValue()));
                     if (!levelC.getLvalue().isEmpty()) {
                         this.localServices.updateLevelC(levelC);
                     }
                 } else {
                     LevelC levelC = new LevelC();
                     levelC.setFactorid(factorTemp.getFactorid());
 
                     if (conditionData.getValue() != null) {
                         if (conditionData.getValue().toString().isEmpty()) {
                             levelC.setLvalue(" ");
                         } else {
                             levelC.setLvalue((String) conditionData.getValue());
                         }
                     } else if (conditionData.getValue() == null) {
                         levelC.setLvalue(" ");
                     }
                     LevelCPK levelCPK = new LevelCPK();
                     levelCPK.setLabelid(factorTemp.getLabelid());
                     levelCPK.setLevelno(levelNoTemporal);
                     levelC.setLevelCPK(levelCPK);
                     this.localServices.addLevelC(levelC);
                 }
             }
         }
     }
 
     private void saveOrUpdateLavelsFactorTrials(
             List<Condition> conditionsData) {
 
         int instance = 0;
         int levelNoTemporal = 0;
         String nameFactorInitial = "";
 
         //Guardar los levels del trial
 
         if (conditionsData.size() > 0) {
             nameFactorInitial = conditionsData.get(0).getConditionName();
         }
 
         LevelN levelNT = (LevelN) factorTrial.getLevelByIndex(0);
         levelNoTemporal = levelNT.getLevelNPK().getLevelno();
         boolean inicio = true;
 //        levelNoTemporal++;
         for (Condition conditionData : conditionsData) {
             Factor factorTemp = (Factor) this.mapaFactorsByName.get(conditionData.getConditionName());
 
             if (nameFactorInitial.equals(conditionData.getConditionName())) {
                 if (inicio) {
                     inicio = false;
                 } else {
                     instance++;
                     levelNoTemporal--;
                 }
             }
             System.out.println("Instance: " + instance + " levelNo: " + levelNoTemporal);
             log.info("Savin level for factor: " + conditionData.getConditionName() + "  with value: " + conditionData.getValue());
             if (conditionData.getDataType().equals(NUMERIC_TYPE)) {
                 if (factorTemp.getLevelByIndex(instance) != null) {
                     LevelN levelN = (LevelN) factorTemp.getLevelByIndex(instance);
                     levelN.setLvalue(HelperFactor.castingToDouble(conditionData.getValue()));
                     this.localServices.updateLevelN(levelN);
                 } else {
                     LevelN levelN = new LevelN();
                     levelN.setFactorid(factorTemp.getFactorid());
                     if (conditionData.getConditionName().equals(nameFactorInitial)) {
                         Integer tempInstance = conditionData.getInstance();
                         levelN.setLvalue(HelperFactor.castingToDouble(tempInstance));
                     } else {
                         if (conditionData.getValue() != null) {
                             levelN.setLvalue(HelperFactor.castingToDouble(conditionData.getValue()));
                         }
                     }
                     LevelNPK levelNPK = new LevelNPK();
                     levelNPK.setLabelid(factorTemp.getLabelid());
                     levelNPK.setLevelno(levelNoTemporal);
                     levelN.setLevelNPK(levelNPK);
                     this.localServices.addLevelN(levelN);
                 }
             } else {
                 if (factorTemp.getLevelByIndex(instance) != null) {
                     LevelC levelC = (LevelC) factorTemp.getLevelByIndex(instance);
                     levelC.setLvalue(HelperFactor.castingToString(conditionData.getValue()));
                     this.localServices.updateLevelC(levelC);
                 } else {
                     LevelC levelC = new LevelC();
                     levelC.setFactorid(factorTemp.getFactorid());
 
                     if (conditionData.getValue() != null) {
                         if (conditionData.getValue().toString().isEmpty()) {
                             levelC.setLvalue(" ");
                         } else {
                             levelC.setLvalue((String) conditionData.getValue());
                         }
                     } else if (conditionData.getValue() == null) {
                         levelC.setLvalue(" ");
                     }
                     LevelCPK levelCPK = new LevelCPK();
                     levelCPK.setLabelid(factorTemp.getLabelid());
                     levelCPK.setLevelno(levelNoTemporal);
                     levelC.setLevelCPK(levelCPK);
                     this.localServices.addLevelC(levelC);
                 }
             }
 
         }
     }
 
     private void saveOrUpdateDataConstatnts(
             List<Constant> constantsData) {
 
         int instance = 0;
         String nameVariateInitial = "";
         boolean inicio = true;
 
         if (constantsData.size() > 0) {
             nameVariateInitial = constantsData.get(0).getConstantName();
         }
 
         for (Constant constant : constantsData) {
             Variate variateTemp = (Variate) mapaConstantsByName.get(constant.getConstantName());
             if (nameVariateInitial.equals(variateTemp.getVname())) {
                 if (inicio) {
                     inicio = false;
                 } else {
                     instance++;
                 }
             }
             if (variateTemp.getDtype().equals(NUMERIC_TYPE)) {
                 if (variateTemp.getDataByIndex(instance) != null) {
                     DataN dataN = (DataN) variateTemp.getDataByIndex(instance);
                     dataN.setDvalue(HelperFactor.castingToDouble(constant.getValue()));
                     this.localServices.updateDataN(dataN);
                 } else {
                     DataN dataN = new DataN();
                     DataNPK dataNPK = new DataNPK();
                     dataNPK.setOunitid(this.studyObsunit.getOunitid());
                     dataNPK.setVariatid(variateTemp.getVariatid());
                     dataN.setDataNPK(dataNPK);
                     dataN.setDvalue(HelperFactor.castingToDouble(constant.getValue()));
                     this.localServices.addDataN(dataN);
                 }
             } else {
                 if (variateTemp.getDataByIndex(instance) != null) {
                     DataC dataC = (DataC) variateTemp.getDataByIndex(instance);
                     String valueToAdd = HelperFactor.castingToString(constant.getValue());
                     if (valueToAdd != null && !valueToAdd.trim().isEmpty()) {
                         dataC.setDvalue(valueToAdd);
                         this.localServices.updateDataC(dataC);
                     }
                 } else {
                     DataC dataC = new DataC();
                     DataCPK dataCPK = new DataCPK();
                     dataCPK.setOunitid(this.studyObsunit.getOunitid());
                     dataCPK.setVariatid(variateTemp.getVariatid());
                     dataC.setDataCPK(dataCPK);
                     String valueToAdd = HelperFactor.castingToString(constant.getValue());
                     if (valueToAdd != null && !valueToAdd.trim().isEmpty()) {
                         dataC.setDvalue(valueToAdd);
                         this.localServices.addDataC(dataC);
                     }
                 }
             }
         }
     }
 
     private void saveOrUpdateVariates() {
         // get all variate from database marked as Traits     
         
         saveNewTraits();
         
         List<Variate> savedVarietList = savedVarietList = this.appServices.getListVariateTraits(study.getStudyid());
 
         // then put in a map all saved variates for fast retrieval
         Map<String, Variate> savedVariateMap = new HashMap<String, Variate>();
         for (Variate variate : savedVarietList) {
             savedVariateMap.put(variate.getVname(), variate);
         }
 
         //TODO: Agregar llamado para obtener el effectid
 
         // then retrieve all obsutit for thata study
 //        this.obsUnitList = this.appServices.getObsunitListByStudy(study.getStudyid());
 
         int currentObsUnit = 0;
 
         // finally iterate over all measurements and update it
         for (Measurement measurement : workbook.getMeasurements()) {
             Obsunit obsunit = obsUnitList.get(currentObsUnit);
 
             // save dataN and DataC
             for (MeasurementData data : measurement.getMeasurementsData()) {
                 // if data has a value then save it
                 if (data.getValue() != null) {
                     // look for saved variate
                     Variate savedVariate = savedVariateMap.get(data.getVariate().getVariateName());
                     addDataNorDataC(obsunit, data, savedVariate);
                 }
 
             }
 
             currentObsUnit++;
         }
     }
 
     /**
      * Adds a value to DATA_C or DATA_N table
      *
      * @param obsunit
      * @param data
      * @param savedVariate
      */
     private void addDataNorDataC(Obsunit obsunit, MeasurementData data, Variate savedVariate) {
         if (savedVariate != null && data.getValue() != null) {
             if (savedVariate.getDtype().equals("N")) {
                 DataNPK dataNPK = new DataNPK();
                 dataNPK.setOunitid(obsunit.getOunitid());
                 dataNPK.setVariatid(savedVariate.getVariatid());
                 DataN dataN = new DataN();
                 dataN.setDataNPK(dataNPK);
 
                 DataN dataToSave = (DataN) data.getValue();
 
                 dataN.setDvalue(dataToSave.getDvalue());
                 localServices.addOrUpdateDataN(dataN);
             } else if (savedVariate.getDtype().equals("C")) {
                 DataCPK dataCPk = new DataCPK();
                 dataCPk.setOunitid(obsunit.getOunitid());
                 dataCPk.setVariatid(savedVariate.getVariatid());
                 DataC dataC = new DataC();
                 dataC.setDataCPK(dataCPk);
 
                 DataC dataToSave = (DataC) data.getValue();
                 dataC.setDvalue(dataToSave.getDvalue());
                 if (dataC.getDvalue() != null && !dataC.getDvalue().trim().isEmpty()) {
                     localServices.addOrUpdateDataC(dataC);
                 }
             }
         }
     }
 
     private void saveNewTraits() {
         Integer dmsatype;
         String dmsatab;
 
         char traitsType = 'V';
         dmsatype = 802;
         dmsatab = "VARIATE";
 
 
 
         for (ibfb.domain.core.Variate variateDomain : workbook.getVariates()) {
             if (variateDomain.getVariateId() == null) {
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
                 List<TmsMethod> tmsMethodsList = appServices.getListTmsMethod(tmsMethodFilter, 0, 0, false);
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
                 List<Scales> scalesList = appServices.getListScales(scalesFilter, 0, 0, false);
                 if (!scalesList.isEmpty()) {
                     scales = scalesList.get(0);
                 } else {
                     scales = ConverterDomainToDTO.getScales(variateDomain.getScale(), '-');
                     localServices.addScales(scales);
                 }
 
                 //Verificar existencia de traits
                 Traits traitsFilter = new Traits(true);
                 traitsFilter.setTrname(variateDomain.getProperty());
                 List<Traits> traitsList = appServices.getListTraitsOnly(traitsFilter, 0, 0, false);
                 if (!traitsList.isEmpty()) {
                     traits = traitsList.get(0);
                     traits.setTid(appServices.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                 } else {
                     traits = ConverterDomainToDTO.getTraits(variateDomain.getProperty());
                     traits.setTraittype(String.valueOf(traitsType));
                     traits.setTid(appServices.getStoredInId(traits.getTraitid(), scales.getScaleid(), tmsMethod.getTmethid()));
                     localServices.addTraits(traits);
                 }
                 //TODO agregar algoritmo para determinacion del standard scale
 
                 //Verificar existencia de measuredin
                 Measuredin measuredinFilter = new Measuredin(true);
                 measuredinFilter.setScaleid(scales.getScaleid());
                 measuredinFilter.setTraitid(traits.getTraitid());
                 measuredinFilter.setStoredinid(traits.getTid());
                 measuredinFilter.setName(variateDomain.getVariateName());
                 measuredinFilter.setTmethid(tmsMethod.getTmethid());
                 
                 List<Measuredin> measuredinList = appServices.getListMeasuredin(measuredinFilter, 0, 0, false);
                 if (!measuredinList.isEmpty()) {
                     measuredin = measuredinList.get(0);
                 } else {
                     measuredin = ConverterDomainToDTO.getMeasuredin(traits, scales, scales.getScaleid(), tmsMethod,
                             variateDomain.getVariateName(), variateDomain.getDescription(), variateDomain.getDataType());
                     //GCP 4122
                     measuredin.setName(variateDomain.getVariateName());
                     measuredin.setStoredinid(1043); //???
                     localServices.addMeasuredin(measuredin);
                 }
 
                 //Asignando el measuredin en el traits
                 traits.setMeasuredin(measuredin);
                 measuredin.setScales(scales);
                 measuredin.setTmsMethod(tmsMethod);
                 traits.setTid(measuredin.getStoredinid());
                 //Verificar factor
                 variate = ConverterDomainToDTO.getVariate(variateDomain.getVariateName(), variateDomain.getDescription(), variateDomain.getDataType(), dbStudy, traits, tmsMethod);
                 variate.setVtype(vtype);
                 localServices.addVariate(variate);
 
                 //Verificar dmsattr
                 dmsattr = ConverterDomainToDTO.getDmsattr(dmsatype, dmsatab, variate.getVariatid(), variateDomain.getDescription());
 //                localServices.addDmsattr(dmsattr);
 
                 variateDomain.setVariateId(variate.getVariatid());
                 
                Represtn represtn = localServices.getReprestnForStudyId(dbStudy.getStudyid(),Represtn.STEFFECTNAMEMEASUREMENT+"_"+dbStudy.getSname());
                 
                 Veffect veffect = new Veffect();
                 VeffectPK veffectPK = new VeffectPK();
                 veffectPK.setRepresno(represtn.getEffectid());
                 veffectPK.setVariatid(variate.getVariatid());
                 veffect.setVeffectPK(veffectPK);
                 localServices.addVeffect(veffect);                
             }
         }
     }
 }
