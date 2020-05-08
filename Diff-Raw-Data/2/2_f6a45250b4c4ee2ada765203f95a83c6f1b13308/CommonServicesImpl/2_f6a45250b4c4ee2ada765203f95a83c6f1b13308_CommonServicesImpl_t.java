 package org.cimmyt.cril.ibwb.provider;
 
 import ibfb.domain.core.Measurement;
 
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.cimmyt.cril.ibwb.api.*;
 import org.cimmyt.cril.ibwb.api.dao.utils.ValidatingDataType;
 import org.cimmyt.cril.ibwb.domain.*;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
import org.cimmyt.cril.ibwb.domain.constants.TypeDB;
 import org.cimmyt.cril.ibwb.domain.inventory.InventoryData;
 import org.cimmyt.cril.ibwb.domain.util.WheatData;
 
 import org.cimmyt.cril.ibwb.provider.dao.*;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  *
  * @author jgcamarena
  */
 public class CommonServicesImpl implements CommonServices {
 
     private String accessType;
     private String accessUrlDms;
     private String accessUrlGms;
     private AtributsDAO atributsDAO;
     private BibrefsDAO bibrefsDAO;
     private ChangesDAO changesDAO;
     private CntryDAO cntryDAO;
     private DataCDAO dataCDAO;
     private DataNDAO dataNDAO;
     private DataTDAO dataTDAO;
     private DatattrDAO datattrDAO;
     private DmsattrDAO dmsattrDAO;
     private DMSReaderDAO dMSReaderDAO;
     private DudfldsDAO dudfldsDAO;
     private EffectDAO effectDAO;
     private FactorDAO factorDAO;
     private GeorefDAO georefDAO;
     private GermplsmDAO germplsmDAO;
     private InstitutDAO institutDAO;
     private InstlnDAO instlnDAO;
     private LevelCDAO levelCDAO;
     private LevelNDAO levelNDAO;
     private LevelTDAO levelTDAO;
     private LevelsDAO levelsDAO;
     private ListdataDAO listdataDAO;
     private ListnmsDAO listnmsDAO;
     private LocationDAO locationDAO;
     private LocdesDAO locdesDAO;
     private MeasuredinDAO measuredinDAO;
     private MethodsDAO methodsDAO;
     private NamesDAO namesDAO;
     private ObsunitDAO obsunitDAO;
     private OindexDAO oindexDAO;
     private PersonsDAO personsDAO;
     private ProgntrsDAO progntrsDAO;
     private ReprestnDAO represtnDAO;
     private ScaleDAO scaleDAO;
     private ScalesDAO scalesDAO;
     private ScaleconDAO scaleconDAO;
     private ScaledisDAO scaledisDAO;
     private ScaletabDAO scaletabDAO;
     private SndivsDAO sndivsDAO;
     private SteffectDAO steffectDAO;
     private StudyDAO studyDAO;
     private TmethodDAO tmethodDAO;
     private TmsMethodDAO tmsMethodDAO;
     private TraitDAO traitDAO;
     private TraitsDAO traitsDAO;
     private UdfldsDAO udfldsDAO;
     private UsersDAO usersDAO;
     private VariateDAO variateDAO;
     private VeffectDAO veffectDAO;
     private TmsScaleConDAO tmsScaleConDAO;
     private TmsScaleDisDAO tmsScaleDisDAO;
     private ImsLabelOtherInfoDAO imsLabelOtherinfoDAO;
     private ImsLabelInfoDAO imsLabelinfoDAO;
     private ImsLotDAO imsLotDAO;
     private ImsTransactionDAO imsTransactionDAO;
     private ContinuousConversionDAO continuousConversionDAO;
     private ContinuousFunctionDAO continuousFunctionDAO;
     private DiscreteConversionDAO discreteConversionDAO;
     private TransformationsDAO transformationsDAO;
     private TmsConsistencyChecksDAO tmsConsistencyChecksDAO;
     
     private CvDAO cvDAO;
     private CvtermDAO cvtermDAO;
     private CvtermRelationshipDAO cvtermRelationshipDAO;
     private CvtermpropDAO cvtermpropDAO;
     private CvtermsynonymDAO cvtermsynonymDAO;
     private NdExperimentDAO ndExperimentDAO;
     private NdExperimentPhenotypeDAO ndExperimentPhenotypeDAO;
     private NdExperimentProjectDAO ndExperimentProjectDAO;
     private NdExperimentStockDAO ndExperimentStockDAO;
     private NdExperimentpropDAO ndExperimentpropDAO;
     private NdGeolocationDAO ndGeolocationDAO;
     private NdGeolocationpropDAO ndGeolocationpropDAO;
     private PhenotypeDAO phenotypeDAO;
     private ProjectDAO projectDAO;
     private ProjectRelationshipDAO projectRelationshipDAO;
     private ProjectpropDAO projectpropDAO;
     private StockDAO stockDAO;
     private StockpropDAO stockpropDAO;
     private UtilityDAO utilityDAO;
 
     public static CommonServices getCommonServices() {
         ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
         CommonServices servicios = (CommonServices) context.getBean("ibWorbenchServiceImpl");
         return servicios;
     }
 
 //-----------------------------------Atributs---------------------------
 
     @Override
     public void addAtributs(Atributs atributs) {
         this.atributsDAO.create(atributs);
     }
 
     @Override
     public void updateAtributs(Atributs atributs) {
         this.atributsDAO.update(atributs);
     }
 
     @Override
     public void deleteAtributs(Atributs atributs) {
         this.atributsDAO.delete(atributs);
     }
 
     @Override
     public Atributs getAtributs(Atributs atributs) {
         return atributsDAO.findById(atributs.getAid());
     }
 
 
     @Override
     public Atributs getAtributs(Integer idAtributs) {
         return atributsDAO.findById(idAtributs);
     }
 
     @Override
     public List<Atributs> getAtributsList() {
         return atributsDAO.findAll();
     }
 
     @Override
     public int getTotalAtributs(Atributs atributs) {
         return this.atributsDAO.getTotal(atributs);
     }
 
     @Override
     public List<Atributs> getListAtributs(Atributs filter, int start, int pageSize, boolean paged) {
         return atributsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Bibrefs---------------------------
 
     @Override
     public void addBibrefs(Bibrefs bibrefs) {
         this.bibrefsDAO.create(bibrefs);
     }
 
     @Override
     public void updateBibrefs(Bibrefs bibrefs) {
         this.bibrefsDAO.update(bibrefs);
     }
 
     @Override
     public void deleteBibrefs(Bibrefs bibrefs) {
         this.bibrefsDAO.delete(bibrefs);
     }
 
     @Override
     public Bibrefs getBibrefs(Bibrefs bibrefs) {
         return bibrefsDAO.findById(bibrefs.getRefid());
     }
 
     @Override
     public Bibrefs getBibrefs(Integer idBibrefs) {
         return bibrefsDAO.findById(idBibrefs);
     }
 
     @Override
     public List<Bibrefs> getBibrefsList() {
         return bibrefsDAO.findAll();
     }
 
     @Override
     public int getTotalBibrefs(Bibrefs bibrefs) {
         return this.bibrefsDAO.getTotal(bibrefs);
     }
 
     @Override
     public List<Bibrefs> getListBibrefs(Bibrefs filter, int start, int pageSize, boolean paged) {
         return bibrefsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Changes---------------------------
 
     @Override
     public void addChanges(Changes changes) {
         this.changesDAO.create(changes);
     }
 
     @Override
     public void updateChanges(Changes changes) {
         this.changesDAO.update(changes);
     }
 
     @Override
     public void deleteChanges(Changes changes) {
         this.changesDAO.delete(changes);
     }
 
     @Override
     public Changes getChanges(Changes changes) {
         return this.changesDAO.findById(changes.getCid());
     }
 
     @Override
     public Changes getChanges(Integer idChanges) {
         return this.changesDAO.findById(idChanges);
     }
 
     @Override
     public List<Changes> getChangesList() {
         return changesDAO.findAll();
     }
 
     @Override
     public int getTotalChanges(Changes changes) {
         return this.changesDAO.getTotal(changes);
     }
 
     @Override
     public List<Changes> getListChanges(Changes filter, int start, int pageSize, boolean paged) {
         return changesDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Cntry---------------------------
     @Override
     public void addCntry(Cntry cntry) {
         this.cntryDAO.create(cntry);
     }
 
     @Override
     public void updateCntry(Cntry cntry) {
         this.cntryDAO.update(cntry);
     }
 
     @Override
     public void deleteCntry(Cntry cntry) {
         this.cntryDAO.delete(cntry);
     }
 
     @Override
     public Cntry getCntry(Cntry cntry) {
         return this.cntryDAO.findById(cntry.getCntryid());
     }
 
     @Override
     public Cntry getCntry(Integer idCntry) {
         return this.cntryDAO.findById(idCntry);
     }
 
     @Override
     public List<Cntry> getCntryList() {
         return cntryDAO.findAll();
     }
 
     @Override
     public int getTotalCntry(Cntry cntry) {
         return this.cntryDAO.getTotal(cntry);
     }
 
     @Override
     public List<Cntry> getListCntry(Cntry filter, int start, int pageSize, boolean paged) {
         return cntryDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------DataC---------------------------
     @Override
     public void addDataC(DataC dataC) {
         this.dataCDAO.create(dataC);
     }
 
     @Override
     public void addOrUpdateDataC(DataC dataC) {
     	//this.dataCDAO.addOrUpdate(dataC);
     	if(isLocal()) {
 	    	HashMap map = new HashMap();
 	    	map.put("ounitid",dataC.getDataCPK().getOunitid());
 	    	map.put("variatid",dataC.getDataCPK().getVariatid());
 	    	map.put("dvalue", dataC.getDvalue());
 	    	this.utilityDAO.callStoredProcedureForUpdate("addOrUpdateDataC",map);
     	}
     }
 
     @Override
     public void updateDataC(DataC dataC) {
         this.dataCDAO.update(dataC);
     }
 
     @Override
     public void deleteDataC(DataC dataC) {
         this.dataCDAO.delete(dataC);
     }
 
 //    @Override
 //    public DataC getDataC(DataC dataC) {
 //        return this.dataCDAO.findById(dataC.getDataCPK());
 //    }
 //    @Override
 //    public DataC getDataC(Integer idDataC) {
 //        throw new UnsupportedOperationException("Not supported yet.");
 //    }
     @Override
     public List<DataC> getDataCList() {
         //return dataCDAO.findAll();
          return this.utilityDAO.callStoredProcedureForList(DataC.class, "getDataCList", 
         		 new HashMap(), new String[]{},new String[]{"ounitid", "variatid", "dvalue"});
     }
 
     @Override
     public int getTotalDataC(DataC dataC) {
         return this.dataCDAO.getTotal(dataC);
     }
 
     @Override
     public List<DataC> getListDataC(DataC filter, int start, int pageSize, boolean paged) {
         //return dataCDAO.getList(filter, start, pageSize, paged);
 
         HashMap params = new HashMap();
         params.put("variatid", filter.getDataCPK().getVariatid());
         params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(DataC.class, "getListDataC", params,
                 new String[]{"variatid", "iscentral"},
         		new String[]{"ounitid", "variatid", "dvalue"});
 
     }
 
     /**
      * Retrieve a list of DATA_n records by its Effect ID
      *
      * @param effectId
      * @return
      */
     @Override
     public List<DataC> getDataCByEffectId(final Integer effectId) {
 
         HashMap params = new HashMap();
        params.put("effectid", effectId);
        params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(DataC.class, "getDataCByEffectId", params,
                 new String[]{"effectid", "iscentral"},
         		new String[]{"ounitid", "variatid", "dvalue"});
 
         //return dataCDAO.getDataNByEffectId(effectId);
     }
 //-----------------------------------DataN---------------------------
 
     @Override
     public void addDataN(DataN dataN) {
         this.dataNDAO.create(dataN);
     }
 
     @Override
     public void addOrUpdateDataN(DataN dataN) {
         this.dataNDAO.addOrUpdate(dataN);
     }
 
     @Override
     public void updateDataN(DataN dataN) {
         this.dataNDAO.update(dataN);
     }
 
     @Override
     public void deleteDataN(DataN dataN) {
         this.dataNDAO.delete(dataN);
     }
 
 //    @Override
 //    public DataN getDataN(DataN dataN) {
 //        return this.dataNDAO.findById(dataN.getDataCPK());
 //    }
 //    @Override
 //    public DataN getDataN(Integer idDataN) {
 //        return this.dataNDAO.findById(dataN.getDataCPK());
 //    }
     @Override
     public List<DataN> getDataNList() {
         //return dataNDAO.findAll();
        return this.utilityDAO.callStoredProcedureForList(DataN.class, "getDataNList", new HashMap(),
                new String[]{},
     		   new String[]{"ounitid", "variatid", "dvalue"});
     }
 
     @Override
     public int getTotalDataN(DataN dataN) {
         return this.dataNDAO.getTotal(dataN);
     }
 
     @Override
     public List<DataN> getListDataN(DataN filter, int start, int pageSize, boolean paged) {
         //return dataNDAO.getList(filter, start, pageSize, paged);
         //does not do pagination
 
         HashMap params = new HashMap();
        params.put("variatid", filter.getDataNPK().getVariatid());
        params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
 
         return this.utilityDAO.callStoredProcedureForList(DataN.class, "getListDataN",params,
                 new String[]{"variatid", "iscentral"},
      		   new String[]{"ounitid", "variatid", "dvalue"});
 
     }
 
     /**
      * Retrieve a list of DATA_n records by its Effect ID
      *
      * @param effectId
      * @return
      */
     @Override
     public List<DataN> getDataNByEffectId(final Integer effectId) {
 
         HashMap params = new HashMap();
        params.put("effectid", effectId);
        params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(DataN.class, "getDataNByEffectId", params,
                 new String[]{"effectid", "iscentral"},
      		   new String[]{"ounitid", "variatid", "dvalue"});
 
     }
 
 //-----------------------------------DataT---------------------------
     @Override
     public void addDataT(DataT dataT) {
         this.dataTDAO.create(dataT);
     }
 
     @Override
     public void updateDataT(DataT dataT) {
         this.dataTDAO.update(dataT);
     }
 
     @Override
     public void deleteDataT(DataT dataT) {
         this.dataTDAO.delete(dataT);
     }
 
 //    @Override
 //    public DataT getDataT(DataT dataT) {
 //        throw new UnsupportedOperationException("Not supported yet.");
 //    }
 //    @Override
 //    public DataT getDataT(Integer idDataT) {
 //        throw new UnsupportedOperationException("Not supported yet.");
 //    }
     @Override
     public List<DataT> getDataTList() {
         return dataTDAO.findAll();
     }
 
     @Override
     public int getTotalDataT(DataT dataT) {
         return this.dataTDAO.getTotal(dataT);
     }
 
     @Override
     public List<DataT> getListDataT(DataT filter, int start, int pageSize, boolean paged) {
         return dataTDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Datattr---------------------------
     @Override
     public void addDatattr(Datattr datattr) {
         this.datattrDAO.create(datattr);
     }
 
     @Override
     public void updateDatattr(Datattr datattr) {
         this.datattrDAO.update(datattr);
     }
 
     @Override
     public void deleteDatattr(Datattr datattr) {
         this.datattrDAO.delete(datattr);
     }
 
     @Override
     public Datattr getDatattr(Datattr datattr) {
         return this.datattrDAO.findById(datattr.getDattrid());
     }
 
     @Override
     public Datattr getDatattr(Integer idDatattr) {
         return this.datattrDAO.findById(idDatattr);
     }
 
     @Override
     public List<Datattr> getDatattrList() {
         return datattrDAO.findAll();
     }
 
     @Override
     public int getTotalDatattr(Datattr datattr) {
         return this.datattrDAO.getTotal(datattr);
     }
 
     @Override
     public List<Datattr> getListDatattr(Datattr filter, int start, int pageSize, boolean paged) {
         return datattrDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Dmsattr---------------------------
     @Override
     public void addDmsattr(Dmsattr dmsattr) {
         this.dmsattrDAO.create(dmsattr);
     }
 
     @Override
     public void updateDmsattr(Dmsattr dmsattr) {
         this.dmsattrDAO.update(dmsattr);
     }
 
     @Override
     public void deleteDmsattr(Dmsattr dmsattr) {
         this.dmsattrDAO.delete(dmsattr);
     }
 
     @Override
     public Dmsattr getDmsattr(Dmsattr dmsattr) {
         return this.dmsattrDAO.findById(dmsattr.getDmsatid());
     }
 
     @Override
     public Dmsattr getDmsattr(Integer idDmsattr) {
         return this.dmsattrDAO.findById(idDmsattr);
     }
 
     public Dmsattr getDmsattrByDmsatrecAndDmsatype(Dmsattr dmsattr) {
         //return dmsattrDAO.getDmsattrByDmsatrecAndDmsatype(dmsattr);
     	return this.utilityDAO.callStoredProcedureForObject(dmsattr, "getDmsattrByDmsatrecAndDmsatype", new String[]{"dmsatrec","dmsatype"}, 
     			new String[] {"dmsatid", "dmsatyp", "dmsatab", "dmsatrec", "dmsatval"});
     }
 
     @Override
     public List<Dmsattr> getDmsattrList() {
         return dmsattrDAO.findAll();
     }
 
     @Override
     public int getTotalDmsattr(Dmsattr dmsattr) {
         return this.dmsattrDAO.getTotal(dmsattr);
     }
 
     @Override
     public List<Dmsattr> getListDmsattr(Dmsattr filter, int start, int pageSize, boolean paged) {
         return dmsattrDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Dudflds---------------------------
     @Override
     public void addDudflds(Dudflds dudflds) {
         this.dudfldsDAO.create(dudflds);
     }
 
     @Override
     public void updateDudflds(Dudflds dudflds) {
         this.dudfldsDAO.update(dudflds);
     }
 
     @Override
     public void deleteDudflds(Dudflds dudflds) {
         this.dudfldsDAO.delete(dudflds);
     }
 
     @Override
     public Dudflds getDudflds(Dudflds dudflds) {
         return this.dudfldsDAO.findById(dudflds.getFldno());
     }
 
     @Override
     public Dudflds getDudflds(Integer idDudflds) {
         return this.dudfldsDAO.findById(idDudflds);
     }
 
     @Override
     public List<Dudflds> getDudfldsList() {
         return dudfldsDAO.findAll();
     }
 
     @Override
     public int getTotalDudflds(Dudflds dudflds) {
         return this.dudfldsDAO.getTotal(dudflds);
     }
 
     @Override
     public List<Dudflds> getListDudflds(Dudflds filter, int start, int pageSize, boolean paged) {
         return dudfldsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Effect---------------------------
     @Override
     public void addEffect(Effect effect) {
         this.effectDAO.create(effect);
     }
 
     @Override
     public void updateEffect(Effect effect) {
         this.effectDAO.update(effect);
     }
 
     @Override
     public void deleteEffect(Effect effect) {
         this.effectDAO.delete(effect);
     }
 
 //    @Override
 //    public Effect getEffect(Effect effect) {
 //        return this.dudfldsDAO.findById(effect.getFldno());
 //    }
 //    @Override
 //    public Effect getEffect(Integer idEffect) {
 //        return this.dudfldsDAO.findById(dudflds.getFldno());
 //    }
     @Override
     public List<Effect> getEffectList() {
         return effectDAO.findAll();
     }
 
     @Override
     public int getTotalEffect(Effect effect) {
         return this.effectDAO.getTotal(effect);
     }
 
     @Override
     public List<Effect> getListEffect(Effect filter, int start, int pageSize, boolean paged) {
         return effectDAO.getList(filter, start, pageSize, paged);
     }
 
     public List<Effect> getEffectsByEffectsids(final List effectsIds) {
         return effectDAO.getEffectsByEffectsids(effectsIds);
     }
 
 //-----------------------------------Factor---------------------------
     @Override
     public void addFactor(Factor factor) {
         this.factorDAO.create(factor);
     }
 
     @Override
     public void updateFactor(Factor factor) {
         this.factorDAO.update(factor);
     }
 
     @Override
     public void deleteFactor(Factor factor) {
         this.factorDAO.delete(factor);
     }
 
     @Override
     public Factor getFactor(Factor factor) {
         return this.factorDAO.findById(factor.getLabelid());
     }
 
     @Override
     public Factor getFactor(Integer idFactor) {
         return this.factorDAO.findById(idFactor);
     }
 
     @Override
     public List<Factor> getFactorList() {
         return factorDAO.findAll();
     }
 
     @Override
     public int getTotalFactor(Factor factor) {
         return this.factorDAO.getTotal(factor);
     }
 
     @Override
     public List<Factor> getListFactor(Factor filter, int start, int pageSize, boolean paged) {
         return factorDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Return a list of grouping factors by study id
      *
      * @param studyid ID for study
      * @return list of factor or empty list if study id not found
      */
     public List<Factor> getFactorsForStudy(final Integer studyid) {
         return factorDAO.getFactorsForStudy(studyid);
     }
 
     public List<Factor> getFactorConvinacionesTraitScaleMethod() {
         return factorDAO.getFactorConvinacionesTraitScaleMethod();
     }
 
     public List<Factor> getMainFactorsByStudyid(Integer studyid) {
         //return this.factorDAO.getMainFactorsByStudyid(studyid);
     	Factor factor = new Factor();
     	factor.setStudyid(studyid);
     	if(isLocal()) {
     		factor.setIslocal(1);
     	} else {
     		factor.setIslocal(0);
     	}
     	return this.utilityDAO.callStoredProcedureForList(factor, "getMainFactorsByStudyid", new String[]{"studyid","islocal"}, 
     			new String[] {"labelid", "studyid", "fname", "traitid", "scaleid", "tmethid", "vtype", "dtype", "tid"});
     	
     	
     }
 
     public List<Factor> getGroupFactorsByStudyidAndFactorid(Integer studyid, Integer factorid) {
         return this.factorDAO.getGroupFactorsByStudyidAndFactorid(studyid, factorid);
     }
 
     public List<Factor> getFactorsByFactorsids(List factorIds) {
         return this.factorDAO.getFactorsByFactorsids(factorIds);
     }
 
     public Factor getFactorByStudyidAndFname(Integer studyid, String fname) {
         return this.factorDAO.getFactorByStudyidAndFname(studyid, fname);
     }
 
 //-----------------------------------Georef---------------------------
     @Override
     public void addGeoref(Georef georef) {
         this.georefDAO.create(georef);
     }
 
     @Override
     public void updateGeoref(Georef georef) {
         this.georefDAO.update(georef);
     }
 
     @Override
     public void deleteGeoref(Georef georef) {
         this.georefDAO.delete(georef);
     }
 
     @Override
     public Georef getGeoref(Georef georef) {
         return this.georefDAO.findById(georef.getLocid());
     }
 
     @Override
     public Georef getGeoref(Integer idGeoref) {
         return this.georefDAO.findById(idGeoref);
     }
 
     @Override
     public List<Georef> getGeorefList() {
         return georefDAO.findAll();
     }
 
     @Override
     public int getTotalGeoref(Georef georef) {
         return this.georefDAO.getTotal(georef);
     }
 
     @Override
     public List<Georef> getListGeoref(Georef filter, int start, int pageSize, boolean paged) {
         return georefDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Germplsm---------------------------
     @Override
     public void addGermplsm(Germplsm germplsm) {
         this.germplsmDAO.create(germplsm);
     }
 
     @Override
     public void updateGermplsm(Germplsm germplsm) {
         this.germplsmDAO.update(germplsm);
     }
 
     @Override
     public void deleteGermplsm(Germplsm germplsm) {
         this.germplsmDAO.delete(germplsm);
     }
 
     @Override
     public Germplsm getGermplsm(Germplsm germplsm) {
         return this.germplsmDAO.findById(germplsm.getGid());
     }
 
     @Override
     public Germplsm getGermplsm(Integer idGermplsm) {
         return this.germplsmDAO.findById(idGermplsm);
     }
 
     @Override
     public List<Germplsm> getGermplsmList() {
         return germplsmDAO.findAll();
     }
 
     @Override
     public int getTotalGermplsm(Germplsm germplsm) {
         return this.germplsmDAO.getTotal(germplsm);
     }
 
     @Override
     public List<Germplsm> getListGermplsm(Germplsm filter, int start, int pageSize, boolean paged) {
         return germplsmDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Institut---------------------------
     @Override
     public void addInstitut(Institut institut) {
         this.institutDAO.create(institut);
     }
 
     @Override
     public void updateInstitut(Institut institut) {
         this.institutDAO.update(institut);
     }
 
     @Override
     public void deleteInstitut(Institut institut) {
         this.institutDAO.delete(institut);
     }
 
     @Override
     public Institut getInstitut(Institut institut) {
         return this.institutDAO.findById(institut.getInstitid());
     }
 
     @Override
     public Institut getInstitut(Integer idInstitut) {
         return this.institutDAO.findById(idInstitut);
     }
 
     @Override
     public List<Institut> getInstitutList() {
         return institutDAO.findAll();
     }
 
     @Override
     public int getTotalInstitut(Institut institut) {
         return this.institutDAO.getTotal(institut);
     }
 
     @Override
     public List<Institut> getListInstitut(Institut filter, int start, int pageSize, boolean paged) {
         return institutDAO.getList(filter, start, pageSize, paged);
     }
 
     @Override
     public List<Institut> getInstitutionsByCity(final String city) {
         return institutDAO.getInstitutionsByCity(city);
     }
 //-----------------------------------Instln---------------------------
 
     @Override
     public void addInstln(Instln instln) {
         this.instlnDAO.create(instln);
     }
 
     @Override
     public void updateInstln(Instln instln) {
         this.instlnDAO.update(instln);
     }
 
     @Override
     public void deleteInstln(Instln instln) {
         this.instlnDAO.delete(instln);
     }
 
     @Override
     public Instln getInstln(Instln instln) {
         return this.instlnDAO.findById(instln.getAdmin());
     }
 
     @Override
     public Instln getInstln(Integer idInstln) {
         return this.instlnDAO.findById(idInstln);
     }
 
     @Override
     public List<Instln> getInstlnList() {
         return instlnDAO.findAll();
     }
 
     @Override
     public int getTotalInstln(Instln instln) {
         return this.instlnDAO.getTotal(instln);
     }
 
     @Override
     public List<Instln> getListInstln(Instln filter, int start, int pageSize, boolean paged) {
         return instlnDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------LevelC---------------------------
     @Override
     public void addLevelC(LevelC levelC) {
         this.levelCDAO.create(levelC);
     }
 
     @Override
     public void updateLevelC(LevelC levelC) {
         this.levelCDAO.update(levelC);
     }
 
     @Override
     public void deleteLevelC(LevelC levelC) {
         this.levelCDAO.delete(levelC);
     }
 
     @Override
     public LevelC getLevelC(LevelC levelC) {
         return this.levelCDAO.findById(levelC.getLevelCPK().getLevelno());
     }
 
     @Override
     public LevelC getLevelC(Integer levelno) {
         return this.levelCDAO.findById(levelno);
     }
 
     @Override
     public List<LevelC> getLevelCList() {
         return levelCDAO.findAll();
     }
 
     @Override
     public int getTotalLevelC(LevelC levelC) {
         return this.levelCDAO.getTotal(levelC);
     }
 
     @Override
     public List<LevelC> getListLevelC(LevelC filter, int start, int pageSize, boolean paged) {
         return levelCDAO.getList(filter, start, pageSize, paged);
     }
 
     public List<LevelC> getLevelsCByLabelid(Integer labelid) {
         //return levelCDAO.getLevelsCByLabelid(labelid);
         HashMap params = new HashMap();
         params.put("labelid", labelid);
         params.put("isnumeric", new Integer(0));
         params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(LevelC.class, "getLevelsByLabelId", params,
                    new String[]{"labelid", "isnumeric", "iscentral"},
       		   new String[]{"labelid", "factorid", "levelno","lvalue"});
  
     }
 
 //-----------------------------------LevelN---------------------------
     @Override
     public void addLevelN(LevelN levelN) {
         this.levelNDAO.create(levelN);
     }
 
     @Override
     public void updateLevelN(LevelN levelN) {
         this.levelNDAO.update(levelN);
     }
 
     @Override
     public void deleteLevelN(LevelN levelN) {
         this.levelNDAO.delete(levelN);
     }
 
 //    @Override
 //    public LevelN getLevelN(LevelN levelN) {
 //        return this.levelNDAO.findById(levelN.getAdmin());
 //    }
 //
 //    @Override
 //    public LevelN getLevelN(Integer idLevelN) {
 //        return this.levelNDAO.findById(idLevelN);
 //    }
     @Override
     public List<LevelN> getLevelNList() {
         return levelNDAO.findAll();
     }
 
     @Override
     public int getTotalLevelN(LevelN levelN) {
         return this.levelNDAO.getTotal(levelN);
     }
 
     @Override
     public List<LevelN> getListLevelN(LevelN filter, int start, int pageSize, boolean paged) {
         return levelNDAO.getList(filter, start, pageSize, paged);
     }
 
     public List<LevelN> getLevelnByLabelid(Integer labelid) {
         //return levelNDAO.getLevelsnByLabelid(labelid);
         HashMap params = new HashMap();
         params.put("labelid", labelid);
         params.put("isnumeric", new Integer(1));
         params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(LevelN.class, "getLevelsByLabelId", params,
                    new String[]{"labelid", "isnumeric", "iscentral"},
       		   new String[]{"labelid", "factorid", "levelno","lvalue"});
         
     }
 
 //-----------------------------------LevelT---------------------------
     @Override
     public void addLevelT(LevelT levelT) {
         this.levelTDAO.create(levelT);
     }
 
     @Override
     public void updateLevelT(LevelT levelT) {
         this.levelTDAO.update(levelT);
     }
 
     @Override
     public void deleteLevelT(LevelT levelT) {
         this.levelTDAO.delete(levelT);
     }
 
 //    public LevelT getLevelT(LevelT levelT) {
 //        return this.levelNDAO.findById(levelN.getAdmin());
 //    }
 //
 //    public LevelT getLevelT(Integer idLevelT) {
 //        return this.levelNDAO.findById(levelN.getAdmin());
 //    }
     @Override
     public List<LevelT> getLevelTList() {
         return levelTDAO.findAll();
     }
 
     @Override
     public int getTotalLevelT(LevelT levelT) {
         return this.levelTDAO.getTotal(levelT);
     }
 
     @Override
     public List<LevelT> getListLevelT(LevelT filter, int start, int pageSize, boolean paged) {
         return levelTDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Levels---------------------------
     @Override
     public void addLevels(Levels levels) {
         this.levelsDAO.create(levels);
     }
 
     @Override
     public void updateLevels(Levels levels) {
         this.levelsDAO.update(levels);
     }
 
     @Override
     public void deleteLevels(Levels levels) {
         this.levelsDAO.delete(levels);
     }
 
     public Integer getNextLevelNo() {
         return this.levelsDAO.getNextLevelNo();
     }
 
 //    public Levels getLevels(Levels levels) {
 //        return this.levelsDAO.findById(levels.getAdmin());
 //    }
 //
 //    public Levels getLevels(Integer idLevels) {
 //        return this.levelsDAO.findById(levelN.getAdmin());
 //    }
     @Override
     public List<Levels> getLevelsList() {
         return levelsDAO.findAll();
     }
 
     @Override
     public int getTotalLevels(Levels levels) {
         return this.levelsDAO.getTotal(levels);
     }
 
     @Override
     public List<Levels> getListLevels(Levels filter, int start, int pageSize, boolean paged) {
         return levelsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Listdata---------------------------
     @Override
     public void addListdata(Listdata listdata) {
         this.listdataDAO.create(listdata);
     }
 
     @Override
     public void updateListdata(Listdata listdata) {
         this.listdataDAO.update(listdata);
     }
 
     @Override
     public void deleteListdata(Listdata listdata) {
         this.listdataDAO.logicalDelete(listdata);
     }
 
 //    public Listdata getListdata(Listdata listdata) {
 //        return this.listdataDAO.findById(listdata.getListdataPK());
 //    }
 //
 //    public Listdata getListdata(Integer idListdata) {
 //        return this.listdataDAO.findById(levels.getAdmin());
 //    }
     @Override
     public List<Listdata> getListdataList() {
         return listdataDAO.findAll();
     }
 
     @Override
     public int getTotalListdata(Listdata listdata) {
         return this.listdataDAO.getTotalRegistros(listdata);
     }
 
     @Override
     public List<Listdata> getListListdata(Listdata filter, int start, int pageSize, boolean paged) {
         return listdataDAO.getListPorFiltro(filter, start, pageSize);
     }
 
     @Override
     public List<Listdata> getListListdataFiltro(Listdata filter, int start, int pageSize, boolean paged) {
         return listdataDAO.getList(filter, start, pageSize, paged);
     }
 
     public List<Listdata> getListdataByIdlistnms(Integer idListnms, TypeDB typeDB) {
         List<Integer> lrecIdList = new ArrayList<Integer>();
         List<Dmsattr> dmsattrList = new ArrayList<Dmsattr>();
         if (typeDB.equals(TypeDB.IWIS)) {
             lrecIdList = listdataDAO.getLRecidListByListId(idListnms);
             dmsattrList = dmsattrDAO.getDmsAttributesByListId(idListnms, lrecIdList);
         }
         return listdataDAO.getListdataByIdlistnms(idListnms, dmsattrList);
     }
 
 //-----------------------------------Listnms---------------------------
     @Override
     public void addListnms(Listnms listnms) {
         this.listnmsDAO.create(listnms);
     }
 
     @Override
     public void updateListnms(Listnms listnms) {
         this.listnmsDAO.update(listnms);
     }
 
     @Override
     public void deleteListnms(Listnms listnms) {
         // first delete all entries
         this.listdataDAO.logicalDeleteAllEntries(listnms.getListid());
         this.listnmsDAO.logicalDelete(listnms);
     }
 
     public Listnms getListnms(Listnms listnms) {
         return this.listnmsDAO.findById(listnms.getListid());
     }
 
     public Listnms getListnms(Integer idListnms) {
         return this.listnmsDAO.findById(idListnms);
     }
 
     @Override
     public List<Listnms> getListnmsList() {
         return listnmsDAO.findAll();
     }
 
     @Override
     public int getTotalListnms(Listnms listnms) {
         return this.listnmsDAO.getTotal(listnms);
     }
 
     @Override
     public List<Listnms> getListListnms(Listnms filter, int start, int pageSize, boolean paged) {
         return listnmsDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Checks if a List already exists in local
      *
      * @param listName
      * @return
      */
     @Override
     public boolean existGermplasmListInLocal(String listName) {
         return listnmsDAO.existGermplasmListName(listName);
     }
 //-----------------------------------Location---------------------------
 
     @Override
     public void addLocation(Location location) {
         this.locationDAO.create(location);
     }
 
     @Override
     public void updateLocation(Location location) {
         this.locationDAO.update(location);
     }
 
     @Override
     public void deleteLocation(Location location) {
         this.locationDAO.delete(location);
     }
 
     public Location getLocation(Location location) {
         return this.locationDAO.findById(location.getLocid());
     }
 
     public Location getLocation(Integer idLocation) {
         return this.locationDAO.findById(idLocation);
     }
 
     @Override
     public List<Location> getLocationList() {
         return locationDAO.findAll();
     }
 
     @Override
     public int getTotalLocation(Location location) {
         return this.locationDAO.getTotal(location);
     }
 
     @Override
     public List<Location> getListLocation(Location filter, int start, int pageSize, boolean paged) {
         return locationDAO.getList(filter, start, pageSize, paged);
     }
 
     @Override
     public List<Location> getLocationsByCountryLocidRange(final Integer countryId, final Integer fromLocid, final Integer toLocid) {
         return locationDAO.getLocationsByCountryLocidRange(countryId, fromLocid, toLocid);
     }
 
 //-----------------------------------Locdes---------------------------
     @Override
     public void addLocdes(Locdes locdes) {
         this.locdesDAO.create(locdes);
     }
 
     @Override
     public void updateLocdes(Locdes locdes) {
         this.locdesDAO.update(locdes);
     }
 
     @Override
     public void deleteLocdes(Locdes locdes) {
         this.locdesDAO.delete(locdes);
     }
 
     public Locdes getLocdes(Locdes locdes) {
         return this.locdesDAO.findById(locdes.getLocid());
     }
 
     public Locdes getLocdes(Integer idLocdes) {
         return this.locdesDAO.findById(idLocdes);
     }
 
     @Override
     public List<Locdes> getLocdesList() {
         return locdesDAO.findAll();
     }
 
     @Override
     public int getTotalLocdes(Locdes locdes) {
         return this.locdesDAO.getTotal(locdes);
     }
 
     @Override
     public List<Locdes> getListLocdes(Locdes filter, int start, int pageSize, boolean paged) {
         return locdesDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Measuredin---------------------------
     @Override
     public void addMeasuredin(Measuredin measuredin) {
         this.measuredinDAO.create(measuredin);
     }
 
     @Override
     public void updateMeasuredin(Measuredin measuredin) {
         this.measuredinDAO.update(measuredin);
     }
 
     @Override
     public void deleteMeasuredin(Measuredin measuredin) {
         this.measuredinDAO.delete(measuredin);
     }
 
     public Measuredin getMeasuredin(Measuredin measuredin) {
         return this.measuredinDAO.findById(measuredin.getMeasuredinid());
     }
 
     public Measuredin getMeasuredin(Integer idMeasuredin) {
         return this.measuredinDAO.findById(idMeasuredin);
     }
 
     public Measuredin getMeasuredinByTraitidScaleidTmethid(Measuredin measuredin) {
         //return this.measuredinDAO.getMeasuredinByTraitidScaleidTmethid(measuredin);
         
         return this.utilityDAO.callStoredProcedureForObject(measuredin, "getMeasuredinByTraitidScaleidTmethid", 
                 new String[] {"traitid", "scaleid", "tmethid"}, 
                 new String[] {"measuredinid", "traitid", "scaleid", "standardscale", "report", "formula", "tmethid"});
     }
 
     public Measuredin getMeasuredinByTraitidAndScaleid(Measuredin measuredin) {
         return this.measuredinDAO.getMeasuredinByTraitidAndScaleid(measuredin);
     }
 
     @Override
     public List<Measuredin> getMeasuredinList() {
         return measuredinDAO.findAll();
     }
 
     @Override
     public int getTotalMeasuredin(Measuredin measuredin) {
         return this.measuredinDAO.getTotal(measuredin);
     }
 
     @Override
     public List<Measuredin> getListMeasuredin(Measuredin filter, int start, int pageSize, boolean paged) {
     	return this.utilityDAO.callStoredProcedureForListPaged(filter, paged, start, pageSize, "getListMeasuredIn", 
 				new String[]{"measuredinid","traitid", "tmethid", "scaleid", "standardscale"},
 				new String[]{"measuredinid","traitid", "tmethid", "scaleid", "standardscale"});
     }
 
     @Override
     public List<Measuredin> getMeasuredInListByTrait(Integer traitId) {
         return this.measuredinDAO.getMeasuredInListByTrait(traitId);
     }
 
     @Override
     public void addOrUpdateMeasuredIn(Measuredin measuredin) {
         this.measuredinDAO.addOrUpdate(measuredin);
     }
 
 //-----------------------------------Methods---------------------------
     @Override
     public void addMethods(Methods methods) {
         this.methodsDAO.create(methods);
     }
 
     @Override
     public void updateMethods(Methods methods) {
         this.methodsDAO.update(methods);
     }
 
     @Override
     public void deleteMethods(Methods methods) {
         this.methodsDAO.delete(methods);
     }
 
     public Methods getMethods(Methods methods) {
         return this.methodsDAO.findById(methods.getMid());
     }
 
     public Methods getMethods(Integer idMethods) {
         return this.methodsDAO.findById(idMethods);
     }
 
     @Override
     public List<Methods> getMethodsList() {
         return methodsDAO.findAll();
     }
 
     @Override
     public int getTotalMethods(Methods methods) {
         return this.methodsDAO.getTotal(methods);
     }
 
     @Override
     public List<Methods> getListMethods(Methods filter, int start, int pageSize, boolean paged) {
         return methodsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Names---------------------------
     @Override
     public void addNames(Names names) {
         this.namesDAO.create(names);
     }
 
     @Override
     public void updateNames(Names names) {
         this.namesDAO.update(names);
     }
 
     @Override
     public void deleteNames(Names names) {
         this.namesDAO.delete(names);
     }
 
     public Names getNames(Names names) {
         return this.namesDAO.findById(names.getNid());
     }
 
     public Names getNames(Integer idNames) {
         return this.namesDAO.findById(idNames);
     }
 
     @Override
     public List<Names> getNamesList() {
         return namesDAO.findAll();
     }
 
     @Override
     public int getTotalNames(Names names) {
         return this.namesDAO.getTotal(names);
     }
 
     @Override
     public List<Names> getListNames(Names filter, int start, int pageSize, boolean paged) {
         return namesDAO.getList(filter, start, pageSize, paged);
     }
 
     public String getNextMaxForBCID(String cadena, Integer ntype) {
         return namesDAO.getNextMaxForBCID(cadena, ntype);
     }
 
     public Names getNamesByGid(Germplsm germplasm, Boolean preferido) {
         return namesDAO.getNamesByGid(germplasm, preferido);
     }
 
     public Integer getMaxForSelection(String cadena, Integer ntype) {
         return namesDAO.getMaxForSelection(cadena, ntype);
     }
 
     public Listnms getNamesCentral(final Listnms listnms) {
         return namesDAO.getNamesCentral(listnms);
     }
 
     public Listnms getNamesLocal(final Listnms listnms) {
         return namesDAO.getNamesLocal(listnms);
     }
 
     /**
      * Gets a list for Wheat Data (cimmyt) related to BCID, Selection history 1.
      * It looks for all elements in names where gid are used by a list
      *
      * @param listId
      * @return Gets a list for Wheat Data (cimmyt)
      */
     @Override
     public List<WheatData> getDataForCimmytWheat(final Integer listId) {
         return namesDAO.getDataForCimmytWheat(listId);
     }
 
 //-----------------------------------Obsunit---------------------------
     @Override
     public void addObsunit(Obsunit obsunit) {
         //daniel
         //test with 10085
         Integer id = this.utilityDAO.getNextMin("nd_experiment");
         obsunit.setOunitid(id);
         this.utilityDAO.callStoredProcedureForUpdate(obsunit, "addObsunit", new String[]{"ounitid", "effectid"});
         //System.out.println(isCentral()+"========================= id is "+id);
         //this.obsunitDAO.create(obsunit);
     }
 
     @Override
     public void updateObsunit(Obsunit obsunit) {
         this.obsunitDAO.update(obsunit);
     }
 
     @Override
     public void deleteObsunit(Obsunit obsunit) {
         this.obsunitDAO.delete(obsunit);
     }
 
     public Obsunit getObsunit(Obsunit obsunit) {
         return this.obsunitDAO.findById(obsunit.getOunitid());
     }
 
     public Obsunit getObsunit(Integer idObsunit) {
         return this.obsunitDAO.findById(idObsunit);
     }
 
     @Override
     public List<Obsunit> getObsunitList() {
 
         return this.utilityDAO.callStoredProcedureForList(Obsunit.class, "getObsunitList", new HashMap(),
                 new String[]{},
      		   new String[]{"ounitid", "variatid", "dvalue"});
         //return obsunitDAO.findAll();
     }
 
     @Override
     public int getTotalObsunit(Obsunit obsunit) {
         return this.obsunitDAO.getTotal(obsunit);
     }
 
     @Override
     public List<Obsunit> getListObsunit(Obsunit filter, int start, int pageSize, boolean paged) {
         return obsunitDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Get number of rows for an effect id. For example you can retrieve row
      * number for Measurement Effect
      *
      * @param effectId
      * @return
      */
     @Override
     public int getObservationsCount(final Integer effectId) {
         return obsunitDAO.getObservationsCount(effectId);
     }
 
     /**
      * Gets a list of observations unit for a effect id
      *
      * @param effectId Effect Id to search
      * @return List of observations units or empty list
      */
     @Override
     public List<Obsunit> getObsunitListByEffectid(final Integer effectId) {
 //        return obsunitDAO.getObsunitListByEffectid(effectId);
         HashMap params = new HashMap();
         params.put("iscentral", isCentral()?new Integer(1): new Integer(0));
         params.put("effectid", effectId);
         return this.utilityDAO.callStoredProcedureForList(Obsunit.class, "getObsunitListByEffectid", params,
                 new String[]{"effectid", "iscentral"},
              		   new String[]{"ounitid", "variatid", "dvalue"});
         //getObsunitListByEffectid
     }
 //-----------------------------------Oindex---------------------------
 
     @Override
     public void addOindex(Oindex oindex) {
         this.oindexDAO.create(oindex);
     }
 
     @Override
     public void updateOindex(Oindex oindex) {
         this.oindexDAO.update(oindex);
     }
 
     @Override
     public void deleteOindex(Oindex oindex) {
         this.oindexDAO.delete(oindex);
     }
 
 //    public Oindex getOindex(Oindex oindex) {
 //        return this.obsunitDAO.findById(obsunit.getOunitid());
 //    }
 //
 //    public Oindex getOindex(Integer idOindex) {
 //        return this.obsunitDAO.findById(obsunit.getOunitid());
 //    }
     @Override
     public List<Oindex> getOindexList() {
         return oindexDAO.findAll();
     }
 
     @Override
     public int getTotalOindex(Oindex oindex) {
         return this.oindexDAO.getTotal(oindex);
     }
 
     @Override
     public List<Oindex> getListOindex(Oindex filter, int start, int pageSize, boolean paged) {
         return oindexDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Return a list of Oindex by it represno
      *
      * @param represno respresno to search
      * @return List of Oindex or empty list if not records match
      */
     public List<Oindex> getOindexListByRepresno(final Integer represno) {
         return oindexDAO.getOindexListByRepresno(represno);
     }
 //-----------------------------------Persons---------------------------
 
     @Override
     public void addPersons(Persons persons) {
         this.personsDAO.create(persons);
     }
 
     @Override
     public void updatePersons(Persons persons) {
         this.personsDAO.update(persons);
     }
 
     @Override
     public void deletePersons(Persons persons) {
         this.personsDAO.delete(persons);
     }
 
     public Persons getPersons(Persons persons) {
         return this.personsDAO.findById(persons.getPersonid());
     }
 
     public Persons getPersons(Integer idPersons) {
         return this.personsDAO.findById(idPersons);
     }
 
     @Override
     public List<Persons> getPersonsList() {
         return personsDAO.findAll();
     }
 
     @Override
     public int getTotalPersons(Persons persons) {
         return this.personsDAO.getTotal(persons);
     }
 
     @Override
     public List<Persons> getListPersons(Persons filter, int start, int pageSize, boolean paged) {
         return personsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Progntrs---------------------------
     @Override
     public void addProgntrs(Progntrs progntrs) {
         this.progntrsDAO.create(progntrs);
     }
 
     @Override
     public void updateProgntrs(Progntrs progntrs) {
         this.progntrsDAO.update(progntrs);
     }
 
     @Override
     public void deleteProgntrs(Progntrs progntrs) {
         this.progntrsDAO.delete(progntrs);
     }
 
 //    public Progntrs getProgntrs(Progntrs progntrs) {
 //        return this.progntrsDAO.findById(progntrs.get);
 //    }
 //
 //    public Progntrs getProgntrs(Integer idProgntrs) {
 //        return this.progntrsDAO.findById(idProgntrs);
 //    }
     @Override
     public List<Progntrs> getProgntrsList() {
         return progntrsDAO.findAll();
     }
 
     @Override
     public int getTotalProgntrs(Progntrs progntrs) {
         return this.progntrsDAO.getTotal(progntrs);
     }
 
     @Override
     public List<Progntrs> getListProgntrs(Progntrs filter, int start, int pageSize, boolean paged) {
         return progntrsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Represtn---------------------------
     @Override
     public void addReprestn(Represtn represtn) {
         this.represtnDAO.create(represtn);
     }
 
     @Override
     public void updateReprestn(Represtn represtn) {
         this.represtnDAO.update(represtn);
     }
 
     @Override
     public void deleteReprestn(Represtn represtn) {
         this.represtnDAO.delete(represtn);
     }
 
     public Represtn getReprestn(Represtn represtn) {
         return this.represtnDAO.findById(represtn.getRepresno());
     }
 
     public Represtn getReprestn(Integer idReprestn) {
         return this.represtnDAO.findById(idReprestn);
     }
 
     @Override
     public List<Represtn> getReprestnList() {
         return represtnDAO.findAll();
     }
 
     @Override
     public int getTotalReprestn(Represtn represtn) {
         return this.represtnDAO.getTotal(represtn);
     }
 
     @Override
     public List<Represtn> getListReprestn(Represtn filter, int start, int pageSize, boolean paged) {
         return represtnDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Gests a Representation object for a Study
      *
      * @param studyId Study number
      * @param represName Representation name to find
      * @return
      */
     @Override
     public Represtn getReprestnForStudyId(final Integer studyId, String represName) {
         return represtnDAO.getReprestnForStudyId(studyId, represName);
     }
 //-----------------------------------Scale---------------------------
     @Override
     public void addScale(Scale scale) {
         this.scaleDAO.create(scale);
     }
 
     @Override
     public void updateScale(Scale scale) {
         this.scaleDAO.update(scale);
     }
 
     @Override
     public void deleteScale(Scale scale) {
         this.scaleDAO.delete(scale);
     }
 
     public Scale getScale(Scale scale) {
         return this.scaleDAO.findById(scale.getScaleid());
     }
 
     public Scale getScale(Integer idScale) {
         return this.scaleDAO.findById(idScale);
     }
 
     public Integer getScaleTotalGroup() {
         return this.scaleDAO.getTotalDiferentes();
     }
 
     @Override
     public List<Scale> getScaleList() {
         return scaleDAO.findAll();
     }
 
     @Override
     public int getTotalScale(Scale scale) {
         return this.scaleDAO.getTotal(scale);
     }
 
     @Override
     public List<Scale> getListScale(Scale filter, int start, int pageSize, boolean paged) {
         //return scaleDAO.getList(filter, start, pageSize, paged);
         HashMap params = new HashMap();
         params.put("traitid", filter.getTraitid());
         params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(Scale.class, "getListScale", params,
                 new String[]{"traitid", "iscentral"},
       		   new String[]{"scaleid", "scname", "traitid","sctype"});
     }
 
     @Override
     public List<Scale> getListScaleGroups() {
         //return scaleDAO.getScaleGroups();
         List<Scale> resultLst = new ArrayList<Scale>();
         List<Scale> resultLstTemp = this.getListScaleAll();
         HashMap<String,Scale> diffScales = new HashMap<String, Scale>();
         for (Scale scale:resultLstTemp) {
             diffScales.put(scale.getScname()+scale.getSctype(), scale);
         }
 
         for (Scale scale: diffScales.values()) {
             resultLst.add(scale);
         }
         return  resultLst;
     }
 
     public void migrateScaleToTmsscalesDirect() {
         scaleDAO.migrateScalesDirect();
     }
 
     public List<Scale> getListScaleAll() {
         //return scaleDAO.getScaleAll();
         HashMap params = new HashMap();
         params.put("iscentral", isCentral() ? new Integer(1) : new Integer(0));
         return this.utilityDAO.callStoredProcedureForList(Scale.class, "getListScaleAll", params,
                 new String[]{ "iscentral"},
        		   new String[]{"scaleid", "scname", "traitid","sctype"});
     }
 
     //-----------------------------------Scales---------------------------
     @Override
     public void addScales(Scales scales) {
         //this.scalesDAO.create(scales);
         HashMap params = new HashMap();
         params.put("cvidin", 1030);
         params.put("cvname", scales.getScname());
         params.put("cvdesc", scales.getScname());
         this.utilityDAO.callStoredProcedureForUpdate("addCvterm", params);
     }
 
     @Override
     public void updateScales(Scales scales) {
 
         //this.scalesDAO.update(scales);
         HashMap params = new HashMap();
        params.put("cvtermid", scales.getScaleid());
        params.put("cvname", scales.getScname());
        params.put("cvdesc", scales.getScname());
        this.utilityDAO.callStoredProcedureForUpdate("updateCvterm", params);
     }
 
     @Override
     public void deleteScales(Scales scales) {
         this.scalesDAO.delete(scales);
     }
 
     public Scales getScales(Scales scales) {
         return this.scalesDAO.findById(scales.getScaleid());
     }
 
     public Scales getScales(Integer idScale) {
         return this.scalesDAO.findById(idScale);
     }
 
     @Override
     public List<Scales> getScalesList() {
         //return scalesDAO.findAll();
         return this.utilityDAO.callStoredProcedureForList(Scales.class, "getScalesList",
             				new HashMap(),new String[]{},
             				new String[]{"scaleid","scname", "sctype"});
     }
 
     @Override
     public int getTotalScales(Scales scales) {
         return this.scalesDAO.getTotal(scales);
     }
 
     @Override
     public List<Scales> getListScales(Scales filter, int start, int pageSize, boolean paged) {
     	//return scalesDAO.getList(filter, start, pageSize, paged);
     	if (filter.getGlobalsearch() == null) {
     		return this.utilityDAO.callStoredProcedureForListPaged(filter, paged, start, pageSize, "getScales",  
     				new String[]{"scaleid","scname", "sctype"},
     				new String[]{"scaleid","scname", "sctype"});
     	} else if(ValidatingDataType.isNumeric(filter.getGlobalsearch())){
     		return this.utilityDAO.callStoredProcedureForListPaged(filter, paged, start, pageSize, "searchScales", 
     				new String[]{"scaleid","scname", "sctype"},
     				new String[]{"scaleid","scname", "sctype"});
     	} else {
     		Scales copy = new Scales();
     		try {
 				PropertyUtils.copyProperties(copy,filter);
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			}
     		copy.setScaleid(null);
     		return this.utilityDAO.callStoredProcedureForListPaged(copy, paged, start, pageSize, "searchScales", 
     				new String[]{"scaleid","scname", "sctype"},
     				new String[]{"scaleid","scname", "sctype"});
     	}
     }
 
     public Scales getScalesByScnameAndSctype(Scales scales) {
         //return scalesDAO.getScalesByScnameAndSctype(scales);
         //no need for ordering since it reutrns only 1 object
         return this.utilityDAO.callStoredProcedureForObject(scales, "getScalesByScnameAndSctype",
                                     new String[]{"scname", "sctype"},
                     				new String[]{"scaleid","scname", "sctype"});
     }
 
     public Scales getScalesByScname(Scales scales) {
         return scalesDAO.getScalesByScname(scales);
     }
 
 //-----------------------------------Scalecon---------------------------
     @Override
     public void addScalecon(Scalecon scalecon) {
         this.scaleconDAO.create(scalecon);
     }
 
     @Override
     public void updateScalecon(Scalecon scalecon) {
         this.scaleconDAO.update(scalecon);
     }
 
     @Override
     public void deleteScalecon(Scalecon scalecon) {
         this.scaleconDAO.delete(scalecon);
     }
 
 //    public Scalecon getScalecon(Scalecon scalecon) {
 //        return this.scaleDAO.findById(scale.getScaleid());
 //    }
 //
 //    public Scalecon getScalecon(Integer idScalecon) {
 //        return this.scaleDAO.findById(scale.getScaleid());
 //    }
     @Override
     public List<Scalecon> getScaleconList() {
         return scaleconDAO.findAll();
     }
 
     @Override
     public int getTotalScalecon(Scalecon scalecon) {
         return this.scaleconDAO.getTotal(scalecon);
     }
 
     @Override
     public List<Scalecon> getListScalecon(Scalecon filter, int start, int pageSize, boolean paged) {
         return scaleconDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Scaledis---------------------------
     @Override
     public void addScaledis(Scaledis scaledis) {
         this.scaledisDAO.create(scaledis);
     }
 
     @Override
     public void updateScaledis(Scaledis scaledis) {
         this.scaledisDAO.update(scaledis);
     }
 
     @Override
     public void deleteScaledis(Scaledis scaledis) {
         this.scaledisDAO.delete(scaledis);
     }
 
 //    public Scaledis getScaledis(Scaledis scaledis) {
 //        return this.scaledisDAO.findById(scaledis.getScaleid());
 //    }
 //
 //    public Scaledis getScaledis(Integer idScaledis) {
 //        return this.scaledisDAO.findById(idScaledis);
 //    }
     @Override
     public List<Scaledis> getScaledisList() {
         return scaledisDAO.findAll();
     }
 
     @Override
     public int getTotalScaledis(Scaledis scaledis) {
         return this.scaledisDAO.getTotal(scaledis);
     }
 
     @Override
     public List<Scaledis> getListScaledis(Scaledis filter, int start, int pageSize, boolean paged) {
         return scaledisDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Scaletab---------------------------
     @Override
     public void addScaletab(Scaletab scaletab) {
         this.scaletabDAO.create(scaletab);
     }
 
     @Override
     public void updateScaletab(Scaletab scaletab) {
         this.scaletabDAO.update(scaletab);
     }
 
     @Override
     public void deleteScaletab(Scaletab scaletab) {
         this.scaletabDAO.delete(scaletab);
     }
 
     public Scaletab getScaletab(Scaletab scaletab) {
         return this.scaletabDAO.findById(scaletab.getScaleid());
     }
 
     public Scaletab getScaletab(Integer idScaletab) {
         return this.scaletabDAO.findById(idScaletab);
     }
 
     @Override
     public List<Scaletab> getScaletabList() {
         return scaletabDAO.findAll();
     }
 
     @Override
     public int getTotalScaletab(Scaletab scaletab) {
         return this.scaletabDAO.getTotal(scaletab);
     }
 
     @Override
     public List<Scaletab> getListScaletab(Scaletab filter, int start, int pageSize, boolean paged) {
         return scaletabDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Sndivs---------------------------
     @Override
     public void addSndivs(Sndivs sndivs) {
         this.sndivsDAO.create(sndivs);
     }
 
     @Override
     public void updateSndivs(Sndivs sndivs) {
         this.sndivsDAO.update(sndivs);
     }
 
     @Override
     public void deleteSndivs(Sndivs sndivs) {
         this.sndivsDAO.delete(sndivs);
     }
 
     public Sndivs getSndivs(Sndivs sndivs) {
         return this.sndivsDAO.findById(sndivs.getSnlid());
     }
 
     public Sndivs getSndivs(Integer idSndivs) {
         return this.sndivsDAO.findById(idSndivs);
     }
 
     @Override
     public List<Sndivs> getSndivsList() {
         return sndivsDAO.findAll();
     }
 
     @Override
     public int getTotalSndivs(Sndivs sndivs) {
         return this.sndivsDAO.getTotal(sndivs);
     }
 
     @Override
     public List<Sndivs> getListSndivs(Sndivs filter, int start, int pageSize, boolean paged) {
         return sndivsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Steffect---------------------------
     @Override
     public void addSteffect(Steffect steffect) {
         this.steffectDAO.create(steffect);
     }
 
     @Override
     public void updateSteffect(Steffect steffect) {
         this.steffectDAO.update(steffect);
     }
 
     @Override
     public void deleteSteffect(Steffect steffect) {
         this.steffectDAO.delete(steffect);
     }
 
     public Steffect getSteffect(Steffect steffect) {
         return this.steffectDAO.findById(steffect.getEffectid());
     }
 
     public Steffect getSteffect(Integer idSteffect) {
         return this.steffectDAO.findById(idSteffect);
     }
 
     @Override
     public List<Steffect> getSteffectList() {
         return steffectDAO.findAll();
     }
 
     @Override
     public int getTotalSteffect(Steffect steffect) {
         return this.steffectDAO.getTotal(steffect);
     }
 
     @Override
     public List<Steffect> getListSteffect(Steffect filter, int start, int pageSize, boolean paged) {
         //only needed is the studyid
         //return steffectDAO.getList(filter, start, pageSize, paged);
 
         HashMap params = new HashMap();
         params.put("studyid", filter.getStudyid());
         List list =  this.utilityDAO.callStoredProcedureForList(Steffect.class, "getListSteffect", params, new String[]{"studyid"},new String[]{"effectid", "studyid", "effectname"} );
         if(list != null)
             return list;
         return new ArrayList();
 
     }
 
     public List<Steffect> getSteffectByStudyid(Integer studyid) {
         //not being use, please confirm
         return steffectDAO.getSteffectByStudyid(studyid);
     }
 
     public List<Integer> getEffectidsByStudyid(Integer studyid) {
         //return steffectDAO.getEffectidsByStudyid(studyid);
         HashMap params = new HashMap();
         params.put("studyid", studyid);
         params.put("iscentral", isCentral()?new Integer(1) : new Integer(0));
         //List list =  this.utilityDAO.callStoredProcedureForList(Integer.class, "getEffectidsByStudyid", params);
         List list =  this.utilityDAO.callStoredProcedureForList(Steffect.class, "getEffectidsByStudyid", params, new String[]{"studyid", "iscentral"},new String[]{"effectid"} );
         if(list != null){
             List temp = new ArrayList();
             for(int i = 0 ; i < list.size() ; i++){
                 Steffect eff = (Steffect) list.get(i);
                 temp.add(eff.getEffectid());
             }
             return temp;
         }
 
         return new ArrayList();
 
 
     }
 
 //-----------------------------------Study---------------------------
     @Override
     public void addStudy(Study study) {
         //this.studyDAO.create(study);
     	if(isLocal()) {
     		this.utilityDAO.callStoredProcedureForUpdate(study, "addOrUpdateStudy", 
                 "studyid","sname","pmkey","title","objectiv",
                 "investid","stype","sdate","edate","userid","sstatus","shierarchy");
     	}
     }
 
     @Override
     public void updateStudy(Study study) {
         //this.studyDAO.update(study);
     	if(isLocal()) {
 	    	this.utilityDAO.callStoredProcedureForUpdate(study, "addOrUpdateStudy", 
 	    			"studyid","sname","pmkey","title","objectiv",
 	                "investid","stype","sdate","edate","userid","sstatus","shierarchy");
     	}
     }
 
     @Override
     public void deleteStudy(Study study) {
         //this.studyDAO.logicalDelete(study);
     	if(isLocal()) {
     		utilityDAO.callStoredProcedureForUpdate(study, "deleteStudy", "studyid");
     	}
     }
 
     public Study getStudy(Study study) {
         //return this.studyDAO.findById(study.getStudyid());
     	return getStudy(study.getStudyid());
     }
 
     public Study getStudy(Integer idStudy) {
     	Study study = new Study();
     	study.setStudyid(idStudy);
         //this.studyDAO.findById(idStudy);
     	return utilityDAO.callStoredProcedureForObject(study, "getStudyById", new String[]{"studyid"},
        		   new String[]{"studyid", "sname", "pmkey","title","objectiv","investid","stype","sdate","edate","userid","sstatus","shierarchy"});
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<Study> getStudyList() {
     	//return studyDAO.findAll();
     	Study study = new Study();
     	return utilityDAO.callStoredProcedureForList(study, "getStudyList",null,
     			new String[]{"studyid", "sname", "pmkey","title","objectiv","investid","stype","sdate","edate","userid","sstatus","shierarchy"});
         
     }
 
     @Override
     public int getTotalStudy(Study study) {
         return this.studyDAO.getTotal(study);
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<Study> getListStudy(Study filter, int start, int pageSize, boolean paged) {
         //return studyDAO.getList(filter, start, pageSize, paged);
     	return utilityDAO.callStoredProcedureForListPaged(filter, paged,
     				start, pageSize, "getStudy",
     				new String[]{"studyid","sname","pmkey","title","objectiv",
                     "investid","stype","sdate","edate","userid","sstatus","shierarchy"},
             		new String[]{"studyid", "sname", "pmkey","title","objectiv",
     				"investid","stype","sdate","edate","userid","sstatus","shierarchy"});
     	
     }
 
     public ResultSet getTrialRandomization(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial) {
         return dMSReaderDAO.getTrialRandomization(studyId, trialFactorId, factoresPrincipales, factoresSalida, nombreTrial);
     }
 
     public List<Measurement> getTrialRandomizationVeryFast(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial) {
         return dMSReaderDAO.getTrialRandomizationVeryFast(studyId, trialFactorId, factoresPrincipales, factoresSalida, nombreTrial);
     }
 
     public ResultSet getTrialRandomizationFast(
             Integer studyId,
             Integer trialFactorId,
             List<String> factoresPrincipales,
             List<String> factoresSalida,
             String nombreTrial) {
         return dMSReaderDAO.getTrialRandomizationFast(studyId, trialFactorId, factoresPrincipales, factoresSalida, nombreTrial);
     }
 
     public StudySearch getListGermplasmAndPlotByStudyidAndTrial(
             StudySearch studySearch) {
         return dMSReaderDAO.getListGermplasmAndPlotByStudyidAndTrial(studySearch);
     }
 
     public StudySearch getListGermplasmAndPlotByStudyidAndTrial(
             StudySearch studySearch,
             List<String> factorsKey,
             List<String> factorsReturn) {
         return dMSReaderDAO.getListGermplasmAndPlotByStudyidAndTrial(
                 studySearch,
                 factorsKey,
                 factorsReturn);
     }
 
     public List<Study> getStudysOnlyTrial() {
         return studyDAO.getStudysOnlyTrial();
     }
 
 //-----------------------------------Tmethod---------------------------
     @Override
     public void addTmethod(Tmethod tmethod) {
         this.tmethodDAO.create(tmethod);
     }
 
     @Override
     public void updateTmethod(Tmethod tmethod) {
         this.tmethodDAO.update(tmethod);
     }
 
     @Override
     public void deleteTmethod(Tmethod tmethod) {
         this.tmethodDAO.delete(tmethod);
     }
 
     public Tmethod getTmethod(Tmethod tmethod) {
         return this.tmethodDAO.findById(tmethod.getTmethid());
     }
 
     public Tmethod getTmethod(Integer idTmethod) {
         return this.tmethodDAO.findById(idTmethod);
     }
 
     @Override
     public List<Tmethod> getTmethodList() {
         return tmethodDAO.findAll();
     }
 
     @Override
     public int getTotalTmethod(Tmethod tmethod) {
         return this.tmethodDAO.getTotal(tmethod);
     }
 
     @Override
     public List<Tmethod> getListTmethod(Tmethod filter, int start, int pageSize, boolean paged) {
         return tmethodDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------TmsMethod---------------------------
     @Override
     public void addTmsMethod(TmsMethod tmsMethod) {
 
         //this.tmsMethodDAO.create(tmsMethod);
         HashMap params = new HashMap();
         params.put("cvidin", 1020);
         params.put("cvname", tmsMethod.getTmname());
         params.put("cvdesc", tmsMethod.getTmdesc());
         this.utilityDAO.callStoredProcedureForUpdate("addCvterm", params);
     }
 
     @Override
     public void updateTmsMethod(TmsMethod tmsMethod) {
         //this.tmsMethodDAO.update(tmsMethod);
         HashMap params = new HashMap();
        params.put("cvtermid", tmsMethod.getTmethid());
        params.put("cvname", tmsMethod.getTmname());
        params.put("cvdesc", tmsMethod.getTmdesc());
        this.utilityDAO.callStoredProcedureForUpdate("updateCvterm", params);
     }
 
     @Override
     public void deleteTmsMethod(TmsMethod tmsMethod) {
         this.tmsMethodDAO.delete(tmsMethod);
     }
 
     public TmsMethod getTmsMethod(TmsMethod tmsMethod) {
         return this.tmsMethodDAO.findById(tmsMethod.getTmethid());
     }
 
     public TmsMethod getTmsMethod(Integer idTmethod) {
         return this.tmsMethodDAO.findById(idTmethod);
     }
 
     @Override
     public List<TmsMethod> getTmsMethodList() {
         return tmsMethodDAO.findAll();
     }
 
     @Override
     public int getTotalTmsMethod(TmsMethod tmsMethod) {
         return this.tmsMethodDAO.getTotal(tmsMethod);
     }
 
     @Override
     public List<TmsMethod> getListTmsMethod(TmsMethod filter, int start, int pageSize, boolean paged) {
     	return this.utilityDAO.callStoredProcedureForListPaged(filter, paged, start, pageSize, "getListTmsMethod", 
 				new String[]{"tmethid","tmname", "tmdesc"},
 				new String[]{"tmethid","tmname", "tmdesc"});
     }
 
 //-----------------------------------TmsScaleCon---------------------------
     @Override
     public void addTmsScaleCon(TmsScaleCon tmsScaleCon) {
         this.tmsScaleConDAO.create(tmsScaleCon);
     }
 
     /**
      * Adds or updates an Object TmsScaleCon to database
      *
      * @param tmsScaleCon Objeto a agregar
      */
     @Override
     public void addOrUpdateTmsScaleCon(TmsScaleCon tmsScaleCon) {
         tmsScaleConDAO.addOrUpdate(tmsScaleCon);
     }
 
     @Override
     public void updateTmsScaleCon(TmsScaleCon tmsScaleCon) {
         this.tmsScaleConDAO.update(tmsScaleCon);
     }
 
     @Override
     public void deleteTmsScaleCon(TmsScaleCon tmsScaleCon) {
         this.tmsScaleConDAO.delete(tmsScaleCon);
     }
 
     @Override
     public TmsScaleCon getTmsScaleCon(TmsScaleCon tmsScaleCon) {
         return this.tmsScaleConDAO.findById(tmsScaleCon.getTmsscaleconid());
     }
 
     @Override
     public TmsScaleCon getTmsScaleCon(Integer idTmethod) {
         return this.tmsScaleConDAO.findById(idTmethod);
     }
 
     @Override
     public List<TmsScaleCon> getTmsScaleConList() {
         return tmsScaleConDAO.findAll();
     }
 
     @Override
     public int getTotalTmsScaleCon(TmsScaleCon tmsScaleCon) {
         return this.tmsScaleConDAO.getTotal(tmsScaleCon);
     }
 
     @Override
     public List<TmsScaleCon> getListTmsScaleCon(TmsScaleCon filter, int start, int pageSize, boolean paged) {
         return tmsScaleConDAO.getList(filter, start, pageSize, paged);
     }
 
     @Override
     public List<TmsScaleCon> getTmsScaleConByMeasuredinId(final Integer measuredindid) {
         return tmsScaleConDAO.getTmsScaleConByMeasuredinId(measuredindid);
     }
 
     /**
      * Gets a ScaleCon by Measured In ID
      *
      * @param Measured In ID
      * @return TmsScaleCon Object if found, if not it returns NULL
      */
     @Override
     public TmsScaleCon getScaleConByMeasuredinId(final Integer measuredinId) {
         return tmsScaleConDAO.getScaleConByMeasuredinId(measuredinId);
     }
 
     //-----------------------------------TmsScaleDis---------------------------
     @Override
     public void addTmsScaleDis(TmsScaleDis tmsScaleDis) {
         this.tmsScaleDisDAO.create(tmsScaleDis);
     }
 
     /**
      * Adds or updates an Object TmsScaleCon to database
      *
      * @param tmsScaleCon Objeto a agregar
      */
     @Override
     public void addOrUpdateTmsScaleDis(TmsScaleDis tmsScaleDis) {
         tmsScaleDisDAO.addOrUpdate(tmsScaleDis);
     }
 
     @Override
     public void updateTmsScaleDis(TmsScaleDis tmsScaleDis) {
         this.tmsScaleDisDAO.update(tmsScaleDis);
     }
 
     @Override
     public void deleteTmsScaleDis(TmsScaleDis tmsScaleDis) {
         this.tmsScaleDisDAO.delete(tmsScaleDis);
     }
 
     @Override
     public TmsScaleDis getTmsScaleDis(TmsScaleDis tmsScaleDis) {
         return this.tmsScaleDisDAO.findById(tmsScaleDis.getTmsscaledisid());
     }
 
     @Override
     public TmsScaleDis getTmsScaleDis(Integer idTmethod) {
         return this.tmsScaleDisDAO.findById(idTmethod);
     }
 
     @Override
     public List<TmsScaleDis> getTmsScaleDisList() {
         return tmsScaleDisDAO.findAll();
     }
 
     @Override
     public int getTotalTmsScaleDis(TmsScaleDis tmsScaleDis) {
         return this.tmsScaleDisDAO.getTotal(tmsScaleDis);
     }
 
     @Override
     public List<TmsScaleDis> getListTmsScaleDis(TmsScaleDis filter, int start, int pageSize, boolean paged) {
         return tmsScaleDisDAO.getList(filter, start, pageSize, paged);
     }
 
     @Override
     public List<TmsScaleDis> getTmsScaleDisByMeasuredinId(final Integer measuredindid) {
         return tmsScaleDisDAO.getTmsScaleDisByMeasuredinId(measuredindid);
     }
 
     /**
      * Gets a ScaleCon by Measured In ID
      *
      * @param Measured In ID
      * @return TmsScaleCon Object if found, if not it returns NULL
      */
     @Override
     public TmsScaleDis getScaleDisByMeasuredinId(final Integer measuredinId) {
         return tmsScaleDisDAO.getScaleDisByMeasuredinId(measuredinId);
     }
 
     //-----------------------------------Trait---------------------------
     @Override
     public void addTrait(Trait trait) {
         this.traitDAO.create(trait);
     }
 
     @Override
     public void updateTrait(Trait trait) {
         this.traitDAO.update(trait);
     }
 
     @Override
     public void deleteTrait(Trait trait) {
         this.traitDAO.delete(trait);
     }
 
     public Trait getTrait(Trait trait) {
         return this.traitDAO.findById(trait.getTid());
     }
 
     public Trait getTrait(Integer idTrait) {
         return this.traitDAO.findById(idTrait);
     }
 
     @Override
     public List<Trait> getTraitList() {
     	return this.utilityDAO.callStoredProcedureForList(Trait.class, "getTraitList", 
     			new HashMap(), new String[]{},
                 new String[]{"tid", "traitid", "trname", "trdesc", "nstat", "traitgroup"});
     }
 
     @Override
     public int getTotalTrait(Trait trait) {
         return this.traitDAO.getTotal(trait);
     }
 
     @Override
     public List<Trait> getListTrait(Trait filter, int start, int pageSize, boolean paged) {
         //return traitDAO.getList(filter, start, pageSize, paged);
         return traitDAO.getTraitsWithScales();
     }
 
     //-----------------------------------Traits---------------------------
     @Override
     public void addTraits(Traits traits) {
         this.traitsDAO.create(traits);
     }
 
     @Override
     public List<String> getTraitGroups() {
 
         return this.utilityDAO.callStoredProcedureForList(String.class, "getTraitGroups", 
        		 new HashMap(), new String[]{},new String[]{"traitgroup"});
     }
 
     @Override
     public void updateTraits(Traits traits) {
         this.traitsDAO.update(traits);
     }
 
     @Override
     public void deleteTraits(Traits traits) {
         this.traitsDAO.delete(traits);
     }
 
     public Traits getTraits(Traits traits) {
         return getTraits(traits.getTraitid());
     }
 
     public Traits getTraits(Integer idTrait) {
     	Traits traits = new Traits();
     	traits.setTraitid(idTrait);
     	return utilityDAO.callStoredProcedureForObject(traits, "getTraitsById", new String[]{"traitid"},
     			new String[]{"tid", "traitid", "trname", "trdesc", "nstat", "traitgroup"});
     }
 
     public Traits getTraitsByTraitid(Integer idTrait) {
         return getTraits(idTrait);
     }
 
     @Override
     public List<Traits> getTraitsList() {
         return traitsDAO.findAll();
     }
 
     @Override
     public int getTotalTraits(Traits traits) {
         return this.traitsDAO.getTotal(traits);
     }
 
     @Override
     public List<Traits> getListTraits(Traits filter, int start, int pageSize, boolean paged) {
         return traitsDAO.getList(filter, start, pageSize, paged);
     }
 
     public Traits getTraitsByTrname(Traits traits) {
         return traitsDAO.getTraitsByTrname(traits);
     }
 
 //-----------------------------------Udflds---------------------------
     @Override
     public void addUdflds(Udflds udflds) {
         this.udfldsDAO.create(udflds);
     }
 
     @Override
     public void updateUdflds(Udflds udflds) {
         this.udfldsDAO.update(udflds);
     }
 
     @Override
     public void deleteUdflds(Udflds udflds) {
         this.udfldsDAO.delete(udflds);
     }
 
     public Udflds getUdflds(Udflds udflds) {
         return this.udfldsDAO.findById(udflds.getFldno());
     }
 
     public Udflds getUdflds(Integer idUdflds) {
         return this.udfldsDAO.findById(idUdflds);
     }
 
     @Override
     public List<Udflds> getUdfldsList() {
         return udfldsDAO.findAll();
     }
 
     @Override
     public int getTotalUdflds(Udflds udflds) {
         return this.udfldsDAO.getTotal(udflds);
     }
 
     @Override
     public List<Udflds> getListUdflds(Udflds filter, int start, int pageSize, boolean paged) {
         return udfldsDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------Users---------------------------
     @Override
     public void addUsers(Users users) {
         this.usersDAO.create(users);
     }
 
     @Override
     public void updateUsers(Users users) {
         this.usersDAO.update(users);
     }
 
     @Override
     public void deleteUsers(Users users) {
         this.usersDAO.delete(users);
     }
 
     public Users getUsers(Users users) {
         return this.usersDAO.findById(users.getUserid());
     }
 
     public Users getUsers(Integer idUsers) {
         return this.usersDAO.findById(idUsers);
     }
 
     @Override
     public List<Users> getUsersList() {
         return usersDAO.findAll();
     }
 
     @Override
     public int getTotalUsers(Users users) {
         return this.usersDAO.getTotal(users);
     }
 
     @Override
     public List<Users> getListUsers(Users filter, int start, int pageSize, boolean paged) {
         return usersDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * get ID for logged user according to following parameters USTATUS = 1 UACC
      * = 100 LOCAL ICIS ADMINISTRATOR UTYPE = 422 LOCAL DATABASE ADMINISTRATOR
      *
      * @return
      */
     @Override
     public Integer getLoggedUserId() {
         return usersDAO.getLoggedUserId();
     }
 
 //-----------------------------------Variate---------------------------
     @Override
     public void addVariate(Variate variate) {
         this.variateDAO.create(variate);
     }
 
     @Override
     public void updateVariate(Variate variate) {
         this.variateDAO.update(variate);
     }
 
     @Override
     public void deleteVariate(Variate variate) {
         this.variateDAO.delete(variate);
     }
 
     public Variate getVariate(Variate variate) {
         //not being use based on tracing - daniel jao
         //return this.variateDAO.findById(variate.getVariatid());
         return getVariate(variate.getVariatid());
     }
 
     public Variate getVariate(Integer idVariate) {
     	//return this.variateDAO.findById(idVariate);
     	Variate variate = new Variate();
     	variate.setVariatid(idVariate);
     	return this.utilityDAO.callStoredProcedureForObject(variate, "getVariateById", new String[]{"variatid"},
         		   new String[]{"variatid", "studyid", "vname","traitid","scaleid","tmethid","dtype","vtype","tid"});
     }
 
     @Override
     public List<Variate> getVariateList() {
         return variateDAO.findAll();
     }
 
     @Override
     public int getTotalVariate(Variate variate) {
         return this.variateDAO.getTotal(variate);
     }
 
     @Override
     public List<Variate> getListVariate(Variate filter, int start, int pageSize, boolean paged) {
         return variateDAO.getList(filter, start, pageSize, paged);
     }
 
     public List<Variate> getVariateConvinacionesTraitScaleMethod() {
         return variateDAO.getVariateConvinacionesTraitScaleMethod();
     }
 
 //-----------------------------------Veffect---------------------------
     @Override
     public void addVeffect(Veffect veffect) {
         this.veffectDAO.create(veffect);
     }
 
     @Override
     public void updateVeffect(Veffect veffect) {
         this.veffectDAO.update(veffect);
     }
 
     @Override
     public void deleteVeffect(Veffect veffect) {
         this.veffectDAO.delete(veffect);
     }
 
 //    public Veffect getVeffect(Veffect veffect) {
 //        return this.veffectDAO.findById(veffect.get);
 //    }
 //
 //    public Veffect getVeffect(Integer idVeffect) {
 //        return this.veffectDAO.findById(variate.getVariatid());
 //    }
     @Override
     public List<Veffect> getVeffectList() {
         return veffectDAO.findAll();
     }
 
     @Override
     public int getTotalVeffect(Veffect veffect) {
         return this.veffectDAO.getTotal(veffect);
     }
 
     @Override
     public List<Veffect> getListVeffect(Veffect filter, int start, int pageSize, boolean paged) {
         return veffectDAO.getList(filter, start, pageSize, paged);
     }
 
 //---------------------------------------------Seters and Getter of DAO
     public AtributsDAO getAtributsDAO() {
         return atributsDAO;
     }
 
     public void setAtributsDAO(AtributsDAO atributsDAO) {
         this.atributsDAO = atributsDAO;
     }
 
     public BibrefsDAO getBibrefsDAO() {
         return bibrefsDAO;
     }
 
     public void setBibrefsDAO(BibrefsDAO bibrefsDAO) {
         this.bibrefsDAO = bibrefsDAO;
     }
 
     public ChangesDAO getChangesDAO() {
         return changesDAO;
     }
 
     public void setChangesDAO(ChangesDAO changesDAO) {
         this.changesDAO = changesDAO;
     }
 
     public CntryDAO getCntryDAO() {
         return cntryDAO;
     }
 
     public void setCntryDAO(CntryDAO cntryDAO) {
         this.cntryDAO = cntryDAO;
     }
 
     public DataCDAO getDataCDAO() {
         return dataCDAO;
     }
 
     public void setDataCDAO(DataCDAO dataCDAO) {
         this.dataCDAO = dataCDAO;
     }
 
     public DataNDAO getDataNDAO() {
         return dataNDAO;
     }
 
     public void setDataNDAO(DataNDAO dataNDAO) {
         this.dataNDAO = dataNDAO;
     }
 
     public DataTDAO getDataTDAO() {
         return dataTDAO;
     }
 
     public void setDataTDAO(DataTDAO dataTDAO) {
         this.dataTDAO = dataTDAO;
     }
 
     public DatattrDAO getDatattrDAO() {
         return datattrDAO;
     }
 
     public void setDatattrDAO(DatattrDAO datattrDAO) {
         this.datattrDAO = datattrDAO;
     }
 
     public DmsattrDAO getDmsattrDAO() {
         return dmsattrDAO;
     }
 
     public void setDmsattrDAO(DmsattrDAO dmsattrDAO) {
         this.dmsattrDAO = dmsattrDAO;
     }
 
     public DudfldsDAO getDudfldsDAO() {
         return dudfldsDAO;
     }
 
     public void setDudfldsDAO(DudfldsDAO dudfldsDAO) {
         this.dudfldsDAO = dudfldsDAO;
     }
 
     public EffectDAO getEffectDAO() {
         return effectDAO;
     }
 
     public void setEffectDAO(EffectDAO effectDAO) {
         this.effectDAO = effectDAO;
     }
 
     public FactorDAO getFactorDAO() {
         return factorDAO;
     }
 
     public void setFactorDAO(FactorDAO factorDAO) {
         this.factorDAO = factorDAO;
     }
 
     public GeorefDAO getGeorefDAO() {
         return georefDAO;
     }
 
     public void setGeorefDAO(GeorefDAO georefDAO) {
         this.georefDAO = georefDAO;
     }
 
     public GermplsmDAO getGermplsmDAO() {
         return germplsmDAO;
     }
 
     public void setGermplsmDAO(GermplsmDAO germplsmDAO) {
         this.germplsmDAO = germplsmDAO;
     }
 
     public InstitutDAO getInstitutDAO() {
         return institutDAO;
     }
 
     public void setInstitutDAO(InstitutDAO institutDAO) {
         this.institutDAO = institutDAO;
     }
 
     public InstlnDAO getInstlnDAO() {
         return instlnDAO;
     }
 
     public void setInstlnDAO(InstlnDAO instlnDAO) {
         this.instlnDAO = instlnDAO;
     }
 
     public LevelCDAO getLevelCDAO() {
         return levelCDAO;
     }
 
     public void setLevelCDAO(LevelCDAO levelCDAO) {
         this.levelCDAO = levelCDAO;
     }
 
     public LevelNDAO getLevelNDAO() {
         return levelNDAO;
     }
 
     public void setLevelNDAO(LevelNDAO levelNDAO) {
         this.levelNDAO = levelNDAO;
     }
 
     public LevelTDAO getLevelTDAO() {
         return levelTDAO;
     }
 
     public void setLevelTDAO(LevelTDAO levelTDAO) {
         this.levelTDAO = levelTDAO;
     }
 
     public LevelsDAO getLevelsDAO() {
         return levelsDAO;
     }
 
     public void setLevelsDAO(LevelsDAO levelsDAO) {
         this.levelsDAO = levelsDAO;
     }
 
     public ListdataDAO getListdataDAO() {
         return listdataDAO;
     }
 
     public void setListdataDAO(ListdataDAO listdataDAO) {
         this.listdataDAO = listdataDAO;
     }
 
     public ListnmsDAO getListnmsDAO() {
         return listnmsDAO;
     }
 
     public void setListnmsDAO(ListnmsDAO listnmsDAO) {
         this.listnmsDAO = listnmsDAO;
     }
 
     public LocationDAO getLocationDAO() {
         return locationDAO;
     }
 
     public void setLocationDAO(LocationDAO locationDAO) {
         this.locationDAO = locationDAO;
     }
 
     public LocdesDAO getLocdesDAO() {
         return locdesDAO;
     }
 
     public void setLocdesDAO(LocdesDAO locdesDAO) {
         this.locdesDAO = locdesDAO;
     }
 
     public MethodsDAO getMethodsDAO() {
         return methodsDAO;
     }
 
     public void setMethodsDAO(MethodsDAO methodsDAO) {
         this.methodsDAO = methodsDAO;
     }
 
     public NamesDAO getNamesDAO() {
         return namesDAO;
     }
 
     public void setNamesDAO(NamesDAO namesDAO) {
         this.namesDAO = namesDAO;
     }
 
     public ObsunitDAO getObsunitDAO() {
         return obsunitDAO;
     }
 
     public void setObsunitDAO(ObsunitDAO obsunitDAO) {
         this.obsunitDAO = obsunitDAO;
     }
 
     public OindexDAO getOindexDAO() {
         return oindexDAO;
     }
 
     public void setOindexDAO(OindexDAO oindexDAO) {
         this.oindexDAO = oindexDAO;
     }
 
     public PersonsDAO getPersonsDAO() {
         return personsDAO;
     }
 
     public void setPersonsDAO(PersonsDAO personsDAO) {
         this.personsDAO = personsDAO;
     }
 
     public ProgntrsDAO getProgntrsDAO() {
         return progntrsDAO;
     }
 
     public void setProgntrsDAO(ProgntrsDAO progntrsDAO) {
         this.progntrsDAO = progntrsDAO;
     }
 
     public ReprestnDAO getReprestnDAO() {
         return represtnDAO;
     }
 
     public void setReprestnDAO(ReprestnDAO represtnDAO) {
         this.represtnDAO = represtnDAO;
     }
 
     public ScaleDAO getScaleDAO() {
         return scaleDAO;
     }
 
     public void setScaleDAO(ScaleDAO scaleDAO) {
         this.scaleDAO = scaleDAO;
     }
 
     public ScaleconDAO getScaleconDAO() {
         return scaleconDAO;
     }
 
     public void setScaleconDAO(ScaleconDAO scaleconDAO) {
         this.scaleconDAO = scaleconDAO;
     }
 
     public ScaledisDAO getScaledisDAO() {
         return scaledisDAO;
     }
 
     public void setScaledisDAO(ScaledisDAO scaledisDAO) {
         this.scaledisDAO = scaledisDAO;
     }
 
     public ScaletabDAO getScaletabDAO() {
         return scaletabDAO;
     }
 
     public void setScaletabDAO(ScaletabDAO scaletabDAO) {
         this.scaletabDAO = scaletabDAO;
     }
 
     public SndivsDAO getSndivsDAO() {
         return sndivsDAO;
     }
 
     public void setSndivsDAO(SndivsDAO sndivsDAO) {
         this.sndivsDAO = sndivsDAO;
     }
 
     public SteffectDAO getSteffectDAO() {
         return steffectDAO;
     }
 
     public void setSteffectDAO(SteffectDAO steffectDAO) {
         this.steffectDAO = steffectDAO;
     }
 
     public StudyDAO getStudyDAO() {
         return studyDAO;
     }
 
     public void setStudyDAO(StudyDAO studyDAO) {
         this.studyDAO = studyDAO;
     }
 
     public TmethodDAO getTmethodDAO() {
         return tmethodDAO;
     }
 
     public void setTmethodDAO(TmethodDAO tmethodDAO) {
         this.tmethodDAO = tmethodDAO;
     }
 
     public TmsMethodDAO getTmsMethodDAO() {
         return tmsMethodDAO;
     }
 
     public void setTmsMethodDAO(TmsMethodDAO tmsMethodDAO) {
         this.tmsMethodDAO = tmsMethodDAO;
     }
 
     public TmsScaleConDAO getTmsScaleConDAO() {
         return tmsScaleConDAO;
     }
 
     public void setTmsScaleConDAO(TmsScaleConDAO tmsScaleConDAO) {
         this.tmsScaleConDAO = tmsScaleConDAO;
     }
 
     public TmsScaleDisDAO getTmsScaleDisDAO() {
         return tmsScaleDisDAO;
     }
 
     public void setTmsScaleDisDAO(TmsScaleDisDAO tmsScaleDisDAO) {
         this.tmsScaleDisDAO = tmsScaleDisDAO;
     }
 
     public TraitDAO getTraitDAO() {
         return traitDAO;
     }
 
     public void setTraitDAO(TraitDAO traitDAO) {
         this.traitDAO = traitDAO;
     }
 
     public UdfldsDAO getUdfldsDAO() {
         return udfldsDAO;
     }
 
     public void setUdfldsDAO(UdfldsDAO udfldsDAO) {
         this.udfldsDAO = udfldsDAO;
     }
 
     public UsersDAO getUsersDAO() {
         return usersDAO;
     }
 
     public void setUsersDAO(UsersDAO usersDAO) {
         this.usersDAO = usersDAO;
     }
 
     public VariateDAO getVariateDAO() {
         return variateDAO;
     }
 
     public void setVariateDAO(VariateDAO variateDAO) {
         this.variateDAO = variateDAO;
     }
 
     public VeffectDAO getVeffectDAO() {
         return veffectDAO;
     }
 
     public void setVeffectDAO(VeffectDAO veffectDAO) {
         this.veffectDAO = veffectDAO;
     }
 
     public MeasuredinDAO getMeasuredinDAO() {
         return measuredinDAO;
     }
 
     public void setMeasuredinDAO(MeasuredinDAO measuredinDAO) {
         this.measuredinDAO = measuredinDAO;
     }
 
     public ScalesDAO getScalesDAO() {
         return scalesDAO;
     }
 
     public void setScalesDAO(ScalesDAO scalesDAO) {
         this.scalesDAO = scalesDAO;
     }
 
     public TraitsDAO getTraitsDAO() {
         return traitsDAO;
     }
 
     public void setTraitsDAO(TraitsDAO traitsDAO) {
         this.traitsDAO = traitsDAO;
     }
 
     public void setImsLabelOtherinfoDAO(ImsLabelOtherInfoDAO imsLabelOtherinfoDAO) {
         this.imsLabelOtherinfoDAO = imsLabelOtherinfoDAO;
     }
 
     public void setImsLabelinfoDAO(ImsLabelInfoDAO imsLabelinfoDAO) {
         this.imsLabelinfoDAO = imsLabelinfoDAO;
     }
 
     public void setImsLotDAO(ImsLotDAO imsLotDAO) {
         this.imsLotDAO = imsLotDAO;
     }
 
     public void setImsTransactionDAO(ImsTransactionDAO imsTransactionDAO) {
         this.imsTransactionDAO = imsTransactionDAO;
     }
 
     /**
      * Checks if Tratis, Scales and Measuredin tables already exists in database
      *
      * @return
      * <code>true</code> if exists,
      * <code>false</code> if does not exist.
      */
     @Override
     public boolean existsTratisTable() {
         return traitsDAO.existsTratisTable();
     }
 
     /**
      * Create TraTratis, Scales and Measuredin
      */
     @Override
     public void createTraitsTables() {
         traitsDAO.createTraitsTables();
     }
 
     /**
      * Return a list of Variates where variate ID are stored in VEFFECT table
      * according to represno ID
      *
      * @param represenoId represno ID for resprestn number
      * @return list of Variates
      */
     public List<Variate> getVarieteFromVeffects(final Integer represenoId) {
         return variateDAO.getVarieteFromVeffects(represenoId);
     }
 
     /**
      * @return the dMSReaderDAO
      */
     public DMSReaderDAO getdMSReaderDAO() {
         return dMSReaderDAO;
     }
 
     /**
      * @param dMSReaderDAO the dMSReaderDAO to set
      */
     public void setdMSReaderDAO(DMSReaderDAO dMSReaderDAO) {
         this.dMSReaderDAO = dMSReaderDAO;
     }
 
     /**
      * @return the accessType
      */
     public String getAccessType() {
         return accessType;
     }
 
     /**
      * @param accessType the accessType to set
      */
     public void setAccessType(String accessType) {
         this.accessType = accessType;
     }
 
     public boolean isLocal() {
         return accessType.equals("local");
     }
 
     public boolean isCentral() {
         return accessType.equals("central");
     }
 
 //-----------------------------------ImsLabelOtherinfo---------------------------
     @Override
     public void addImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo) {
         this.imsLabelOtherinfoDAO.create(imsLabelOtherinfo);
     }
 
     @Override
     public void updateImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo) {
         this.imsLabelOtherinfoDAO.update(imsLabelOtherinfo);
     }
 
     @Override
     public void deleteImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo) {
         this.imsLabelOtherinfoDAO.delete(imsLabelOtherinfo);
     }
 
     @Override
     public List<ImsLabelOtherinfo> getImsLabelOtherinfoList() {
         return imsLabelOtherinfoDAO.findAll();
     }
 
     @Override
     public int getTotalImsLabelOtherinfo(ImsLabelOtherinfo imsLabelOtherinfo) {
         return this.imsLabelOtherinfoDAO.getTotal(imsLabelOtherinfo);
     }
 
     @Override
     public List<ImsLabelOtherinfo> getListImsLabelOtherinfo(ImsLabelOtherinfo filter, int start, int pageSize, boolean paged) {
         return imsLabelOtherinfoDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------ImsLabelinfo---------------------------
     @Override
     public void addImsLabelinfo(ImsLabelinfo imsLabelinfo) {
         this.imsLabelinfoDAO.create(imsLabelinfo);
     }
 
     @Override
     public void updateImsLabelinfo(ImsLabelinfo imsLabelinfo) {
         this.imsLabelinfoDAO.update(imsLabelinfo);
     }
 
     @Override
     public void deleteImsLabelinfo(ImsLabelinfo imsLabelinfo) {
         this.imsLabelinfoDAO.delete(imsLabelinfo);
     }
 
     @Override
     public List<ImsLabelinfo> getImsLabelinfoList() {
         return imsLabelinfoDAO.findAll();
     }
 
     @Override
     public int getTotalImsLabelinfo(ImsLabelinfo imsLabelinfo) {
         return this.imsLabelinfoDAO.getTotal(imsLabelinfo);
     }
 
     @Override
     public List<ImsLabelinfo> getListImsLabelinfo(ImsLabelinfo filter, int start, int pageSize, boolean paged) {
         return imsLabelinfoDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------ImsLot---------------------------
     @Override
     public void addImsLot(ImsLot imsLot) {
         this.imsLotDAO.create(imsLot);
     }
 
     @Override
     public void updateImsLot(ImsLot imsLot) {
         this.imsLotDAO.update(imsLot);
     }
 
     @Override
     public void deleteImsLot(ImsLot imsLot) {
         this.imsLotDAO.delete(imsLot);
     }
 
     @Override
     public List<ImsLot> getImsLotList() {
         return imsLotDAO.findAll();
     }
 
     @Override
     public int getTotalImsLot(ImsLot imsLot) {
         return this.imsLotDAO.getTotal(imsLot);
     }
 
     @Override
     public List<ImsLot> getListImsLot(ImsLot filter, int start, int pageSize, boolean paged) {
         return imsLotDAO.getList(filter, start, pageSize, paged);
     }
 
 //-----------------------------------ImsTransaction---------------------------
     @Override
     public void addImsTransaction(ImsTransaction imsTransaction) {
         this.imsTransactionDAO.create(imsTransaction);
     }
 
     @Override
     public void updateImsTransaction(ImsTransaction imsTransaction) {
         this.imsTransactionDAO.update(imsTransaction);
     }
 
     @Override
     public void deleteImsTransaction(ImsTransaction imsTransaction) {
         this.imsTransactionDAO.delete(imsTransaction);
     }
 
     @Override
     public List<ImsTransaction> getImsTransactionList() {
         return imsTransactionDAO.findAll();
     }
 
     @Override
     public int getTotalImsTransaction(ImsTransaction imsTransaction) {
         return this.imsTransactionDAO.getTotal(imsTransaction);
     }
 
     @Override
     public List<ImsTransaction> getListImsTransaction(ImsTransaction filter, int start, int pageSize, boolean paged) {
         return imsTransactionDAO.getList(filter, start, pageSize, paged);
     }
 
     /**
      * Get information data from a List
      *
      * @param listId Id for LIST
      * @return
      */
     @Override
     public List<InventoryData> getInventoryDataFromList(final Integer listId) {
         return imsTransactionDAO.getInventoryDataFromList(listId);
     }
 
     /**
      * Gets a different list of Location ID for that list
      *
      * @param listId
      * @return
      */
     @Override
     public List<Integer> locationsForInventoryList(final Integer listId) {
         return imsTransactionDAO.locationsForInventoryList(listId);
     }
 
     /**
      * Gets a different list of Scales ID for that list
      *
      * @param listId
      * @return
      */
     @Override
     public List<Integer> scalesForInventoryList(final Integer listId) {
         return imsTransactionDAO.scalesForInventoryList(listId);
     }
 
     //-----------------------------------ContinuousConversion---------------------------
     @Override
     public void addContinuousConversion(ContinuousConversion continuousConversion) {
         this.getContinuousConversionDAO().create(continuousConversion);
     }
 
     @Override
     public void updateContinuousConversion(ContinuousConversion continuousConversion) {
         this.getContinuousConversionDAO().update(continuousConversion);
     }
 
     @Override
     public void deleteContinuousConversion(ContinuousConversion continuousConversion) {
         this.getContinuousConversionDAO().delete(continuousConversion);
     }
 
     @Override
     public ContinuousConversion getContinuousConversion(ContinuousConversion continuousConversion) {
         return this.getContinuousConversionDAO().findById(continuousConversion.getTransid());
     }
 
     @Override
     public ContinuousConversion getContinuousConversion(Integer transid) {
         return this.getContinuousConversionDAO().findById(transid);
     }
 
     @Override
     public List<ContinuousConversion> getContinuousConversionList() {
         return this.getContinuousConversionDAO().findAll();
     }
 
     @Override
     public int getTotalContinuousConversion(ContinuousConversion continuousConversion) {
         return this.getContinuousConversionDAO().getTotal(continuousConversion);
     }
 
     @Override
     public List<ContinuousConversion> getListContinuousConversion(ContinuousConversion filter, int start, int pageSize, boolean paged) {
         return this.getContinuousConversionDAO().getList(filter, start, pageSize, paged);
     }
 
     @Override
     public boolean existsTableContinuousConversion() {
         return this.getContinuousConversionDAO().existsTable();
     }
 
     @Override
     public void createTableContinuousConversion() {
         this.getContinuousConversionDAO().createTable();
     }
 
     //-----------------------------------ContinuousFunction---------------------------
     @Override
     public void addContinuousFunction(ContinuousFunction continuousFunction) {
         this.getContinuousFunctionDAO().create(continuousFunction);
     }
 
     @Override
     public void updateContinuousFunction(ContinuousFunction continuousFunction) {
         this.getContinuousFunctionDAO().update(continuousFunction);
     }
 
     @Override
     public void deleteContinuousFunction(ContinuousFunction continuousFunction) {
         this.getContinuousFunctionDAO().delete(continuousFunction);
     }
 
     @Override
     public ContinuousFunction getContinuousFunction(ContinuousFunction continuousFunction) {
         return this.getContinuousFunctionDAO().findById(continuousFunction.getTransid());
     }
 
     @Override
     public ContinuousFunction getContinuousFunction(Integer transid) {
         return this.getContinuousFunctionDAO().findById(transid);
     }
 
     @Override
     public List<ContinuousFunction> getContinuousFunctionList() {
         return this.getContinuousFunctionDAO().findAll();
     }
 
     @Override
     public int getTotalContinuousFunction(ContinuousFunction continuousFunction) {
         return this.getContinuousFunctionDAO().getTotal(continuousFunction);
     }
 
     @Override
     public List<ContinuousFunction> getListContinuousFunction(ContinuousFunction filter, int start, int pageSize, boolean paged) {
         return this.getContinuousFunctionDAO().getList(filter, start, pageSize, paged);
     }
 
     @Override
     public boolean existsTableContinuousFunction() {
         return this.getContinuousFunctionDAO().existsTable();
     }
 
     @Override
     public void createTableContinuousFunction() {
         this.getContinuousFunctionDAO().createTable();
     }
 
     //-----------------------------------DiscreteConversion---------------------------
     @Override
     public void addDiscreteConversion(DiscreteConversion discreteConversion) {
         this.getDiscreteConversionDAO().create(discreteConversion);
     }
 
     @Override
     public void updateDiscreteConversion(DiscreteConversion discreteConversion) {
         this.getDiscreteConversionDAO().update(discreteConversion);
     }
 
     @Override
     public void deleteDiscreteConversion(DiscreteConversion discreteConversion) {
         this.getDiscreteConversionDAO().delete(discreteConversion);
     }
 
     @Override
     public DiscreteConversion getDiscreteConversion(DiscreteConversion discreteConversion) {
         //not being use
         return this.getDiscreteConversionDAO().findById(discreteConversion.getTransid());
     }
 
     @Override
     public DiscreteConversion getDiscreteConversion(Integer transid) {
 
        DiscreteConversion discreteConversion = new DiscreteConversion();
        discreteConversion.setTransid(transid);
        return this.utilityDAO.callStoredProcedureForObject(discreteConversion, "getDiscreteConversion", new String[] {"transid"}, new String[] {"transid", "value1", "value2"} );
 
        // return this.getDiscreteConversionDAO().findById(transid);
     }
 
     @Override
     public List<DiscreteConversion> getDiscreteConversionList() {
         //not being use
         return this.getDiscreteConversionDAO().findAll();
     }
 
     @Override
     public int getTotalDiscreteConversion(DiscreteConversion discreteConversion) {
         //not being use
         return this.getDiscreteConversionDAO().getTotal(discreteConversion);
     }
 
     @Override
     public List<DiscreteConversion> getListDiscreteConversion(DiscreteConversion filter, int start, int pageSize, boolean paged) {
         return this.getDiscreteConversionDAO().getList(filter, start, pageSize, paged);
     }
 
     @Override
     public boolean existsTableDiscreteConversion() {
         return this.getDiscreteConversionDAO().existsTable();
     }
 
     @Override
     public void createTableDiscreteConversion() {
         this.getDiscreteConversionDAO().createTable();
     }
 
     //-----------------------------------Transformations---------------------------
     @Override
     public void addTransformations(Transformations transformations) {
         this.getTransformationsDAO().create(transformations);
     }
 
     @Override
     public void updateTransformations(Transformations transformations) {
         this.getTransformationsDAO().update(transformations);
     }
 
     @Override
     public void deleteTransformations(Transformations transformations) {
         this.getTransformationsDAO().delete(transformations);
     }
 
     @Override
     public Transformations getTransformations(Transformations transformations) {
         return this.getTransformationsDAO().findById(transformations.getTransid());
     }
 
     @Override
     public Transformations getTransformations(Integer transid) {
         return this.getTransformationsDAO().findById(transid);
     }
 
     @Override
     public List<Transformations> getTransformationsList() {
         return this.getTransformationsDAO().findAll();
     }
 
     @Override
     public int getTotalTransformations(Transformations transformations) {
         return this.getTransformationsDAO().getTotal(transformations);
     }
 
     @Override
     public List<Transformations> getListTransformations(Transformations filter, int start, int pageSize, boolean paged) {
         return this.getTransformationsDAO().getList(filter, start, pageSize, paged);
     }
 
     @Override
     public boolean existsTableTransformations() {
         return this.getTransformationsDAO().existsTable();
     }
 
     @Override
     public void createTableTransformations() {
         this.getTransformationsDAO().createTable();
     }
 
     //-----------------------------------TmsConsistencyChecks---------------------------
     @Override
     public void addTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks) {
         this.getTmsConsistencyChecksDAO().create(tmsConsistencyChecks);
     }
 
     @Override
     public void updateTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks) {
         this.getTmsConsistencyChecksDAO().update(tmsConsistencyChecks);
     }
 
     @Override
     public void deleteTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks) {
         this.getTmsConsistencyChecksDAO().delete(tmsConsistencyChecks);
     }
 
     @Override
     public TmsConsistencyChecks getTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks) {
         return this.getTmsConsistencyChecksDAO().findById(tmsConsistencyChecks.getImplicationid());
     }
 
     @Override
     public TmsConsistencyChecks getTmsConsistencyChecks(Integer transid) {
         return this.getTmsConsistencyChecksDAO().findById(transid);
     }
 
     @Override
     public List<TmsConsistencyChecks> getTmsConsistencyChecksList() {
         return this.getTmsConsistencyChecksDAO().findAll();
     }
 
     @Override
     public int getTotalTmsConsistencyChecks(TmsConsistencyChecks tmsConsistencyChecks) {
         return this.getTmsConsistencyChecksDAO().getTotal(tmsConsistencyChecks);
     }
 
     @Override
     public List<TmsConsistencyChecks> getListTmsConsistencyChecks(TmsConsistencyChecks filter, int start, int pageSize, boolean paged) {
         return this.getTmsConsistencyChecksDAO().getList(filter, start, pageSize, paged);
     }
 
     @Override
     public boolean existsTableTmsConsistencyChecks() {
         return this.getTmsConsistencyChecksDAO().existsTable();
     }
 
     @Override
     public void createTableTmsConsistencyChecks() {
         this.getTmsConsistencyChecksDAO().createTable();
     }
 
     /**
      * @return the continuousConversionDAO
      */
     public ContinuousConversionDAO getContinuousConversionDAO() {
         return continuousConversionDAO;
     }
 
     /**
      * @param continuousConversionDAO the continuousConversionDAO to set
      */
     public void setContinuousConversionDAO(ContinuousConversionDAO continuousConversionDAO) {
         this.continuousConversionDAO = continuousConversionDAO;
     }
 
     /**
      * @return the continuousFunctionDAO
      */
     public ContinuousFunctionDAO getContinuousFunctionDAO() {
         return continuousFunctionDAO;
     }
 
     /**
      * @param continuousFunctionDAO the continuousFunctionDAO to set
      */
     public void setContinuousFunctionDAO(ContinuousFunctionDAO continuousFunctionDAO) {
         this.continuousFunctionDAO = continuousFunctionDAO;
     }
 
     /**
      * @return the discreteConversionDAO
      */
     public DiscreteConversionDAO getDiscreteConversionDAO() {
         return discreteConversionDAO;
     }
 
     /**
      * @param discreteConversionDAO the discreteConversionDAO to set
      */
     public void setDiscreteConversionDAO(DiscreteConversionDAO discreteConversionDAO) {
         this.discreteConversionDAO = discreteConversionDAO;
     }
 
     /**
      * @return the transformationsDAO
      */
     public TransformationsDAO getTransformationsDAO() {
         return transformationsDAO;
     }
 
     /**
      * @param transformationsDAO the transformationsDAO to set
      */
     public void setTransformationsDAO(TransformationsDAO transformationsDAO) {
         this.transformationsDAO = transformationsDAO;
     }
     
     
 
     /**
      * @return the accessUrlDms
      */
     public String getAccessUrlDms() {
         return accessUrlDms;
     }
 
     /**
      * @param accessUrlDms the accessUrlDms to set
      */
     public void setAccessUrlDms(String accessUrlDms) {
         this.accessUrlDms = accessUrlDms;
     }
 
     /**
      * @return the accessUrlGms
      */
     public String getAccessUrlGms() {
         return accessUrlGms;
     }
 
     /**
      * @param accessUrlGms the accessUrlGms to set
      */
     public void setAccessUrlGms(String accessUrlGms) {
         this.accessUrlGms = accessUrlGms;
     }
 
     /**
      * @return the tmsConsistencyChecksDAO
      */
     public TmsConsistencyChecksDAO getTmsConsistencyChecksDAO() {
         return tmsConsistencyChecksDAO;
     }
 
     /**
      * @param tmsConsistencyChecksDAO the tmsConsistencyChecksDAO to set
      */
     public void setTmsConsistencyChecksDAO(TmsConsistencyChecksDAO tmsConsistencyChecksDAO) {
         this.tmsConsistencyChecksDAO = tmsConsistencyChecksDAO;
     }
     
     
     /**
      * Gets a list of Udffields accoding to a table and a field related
      *
      * @param tableName Table name
      * @param fieldName Field name
      * @return List of Udflds objects
      */
     @Override
     public List<Udflds> getUdfldsList(final String tableName, final String fieldName) {
         return this.udfldsDAO.getUdfldsList(tableName, fieldName);
     }
 
     /**
      * @return the cvDAO
      */
     public CvDAO getCvDAO() {
         return cvDAO;
     }
 
     /**
      * @param cvDAO the cvDAO to set
      */
     public void setCvDAO(CvDAO cvDAO) {
         this.cvDAO = cvDAO;
     }
 
     /**
      * @return the cvtermDAO
      */
     public CvtermDAO getCvtermDAO() {
         return cvtermDAO;
     }
 
     /**
      * @param cvtermDAO the cvtermDAO to set
      */
     public void setCvtermDAO(CvtermDAO cvtermDAO) {
         this.cvtermDAO = cvtermDAO;
     }
 
     /**
      * @return the cvtermRelationshipDAO
      */
     public CvtermRelationshipDAO getCvtermRelationshipDAO() {
         return cvtermRelationshipDAO;
     }
 
     /**
      * @param cvtermRelationshipDAO the cvtermRelationshipDAO to set
      */
     public void setCvtermRelationshipDAO(CvtermRelationshipDAO cvtermRelationshipDAO) {
         this.cvtermRelationshipDAO = cvtermRelationshipDAO;
     }
 
     /**
      * @return the cvtermpropDAO
      */
     public CvtermpropDAO getCvtermpropDAO() {
         return cvtermpropDAO;
     }
 
     /**
      * @param cvtermpropDAO the cvtermpropDAO to set
      */
     public void setCvtermpropDAO(CvtermpropDAO cvtermpropDAO) {
         this.cvtermpropDAO = cvtermpropDAO;
     }
 
     /**
      * @return the cvtermsynonymDAO
      */
     public CvtermsynonymDAO getCvtermsynonymDAO() {
         return cvtermsynonymDAO;
     }
 
     /**
      * @param cvtermsynonymDAO the cvtermsynonymDAO to set
      */
     public void setCvtermsynonymDAO(CvtermsynonymDAO cvtermsynonymDAO) {
         this.cvtermsynonymDAO = cvtermsynonymDAO;
     }
 
     /**
      * @return the ndExperimentDAO
      */
     public NdExperimentDAO getNdExperimentDAO() {
         return ndExperimentDAO;
     }
 
     /**
      * @param ndExperimentDAO the ndExperimentDAO to set
      */
     public void setNdExperimentDAO(NdExperimentDAO ndExperimentDAO) {
         this.ndExperimentDAO = ndExperimentDAO;
     }
 
     /**
      * @return the ndExperimentPhenotypeDAO
      */
     public NdExperimentPhenotypeDAO getNdExperimentPhenotypeDAO() {
         return ndExperimentPhenotypeDAO;
     }
 
     /**
      * @param ndExperimentPhenotypeDAO the ndExperimentPhenotypeDAO to set
      */
     public void setNdExperimentPhenotypeDAO(NdExperimentPhenotypeDAO ndExperimentPhenotypeDAO) {
         this.ndExperimentPhenotypeDAO = ndExperimentPhenotypeDAO;
     }
 
     /**
      * @return the ndExperimentProjectDAO
      */
     public NdExperimentProjectDAO getNdExperimentProjectDAO() {
         return ndExperimentProjectDAO;
     }
 
     /**
      * @param ndExperimentProjectDAO the ndExperimentProjectDAO to set
      */
     public void setNdExperimentProjectDAO(NdExperimentProjectDAO ndExperimentProjectDAO) {
         this.ndExperimentProjectDAO = ndExperimentProjectDAO;
     }
 
     /**
      * @return the ndExperimentStockDAO
      */
     public NdExperimentStockDAO getNdExperimentStockDAO() {
         return ndExperimentStockDAO;
     }
 
     /**
      * @param ndExperimentStockDAO the ndExperimentStockDAO to set
      */
     public void setNdExperimentStockDAO(NdExperimentStockDAO ndExperimentStockDAO) {
         this.ndExperimentStockDAO = ndExperimentStockDAO;
     }
 
     /**
      * @return the ndExperimentpropDAO
      */
     public NdExperimentpropDAO getNdExperimentpropDAO() {
         return ndExperimentpropDAO;
     }
 
     /**
      * @param ndExperimentpropDAO the ndExperimentpropDAO to set
      */
     public void setNdExperimentpropDAO(NdExperimentpropDAO ndExperimentpropDAO) {
         this.ndExperimentpropDAO = ndExperimentpropDAO;
     }
 
     /**
      * @return the ndGeolocationDAO
      */
     public NdGeolocationDAO getNdGeolocationDAO() {
         return ndGeolocationDAO;
     }
 
     /**
      * @param ndGeolocationDAO the ndGeolocationDAO to set
      */
     public void setNdGeolocationDAO(NdGeolocationDAO ndGeolocationDAO) {
         this.ndGeolocationDAO = ndGeolocationDAO;
     }
 
     /**
      * @return the ndGeolocationpropDAO
      */
     public NdGeolocationpropDAO getNdGeolocationpropDAO() {
         return ndGeolocationpropDAO;
     }
 
     /**
      * @param ndGeolocationpropDAO the ndGeolocationpropDAO to set
      */
     public void setNdGeolocationpropDAO(NdGeolocationpropDAO ndGeolocationpropDAO) {
         this.ndGeolocationpropDAO = ndGeolocationpropDAO;
     }
 
     /**
      * @return the phenotypeDAO
      */
     public PhenotypeDAO getPhenotypeDAO() {
         return phenotypeDAO;
     }
 
     /**
      * @param phenotypeDAO the phenotypeDAO to set
      */
     public void setPhenotypeDAO(PhenotypeDAO phenotypeDAO) {
         this.phenotypeDAO = phenotypeDAO;
     }
 
     /**
      * @return the projectDAO
      */
     public ProjectDAO getProjectDAO() {
         return projectDAO;
     }
 
     /**
      * @param projectDAO the projectDAO to set
      */
     public void setProjectDAO(ProjectDAO projectDAO) {
         this.projectDAO = projectDAO;
     }
 
     /**
      * @return the projectRelationshipDAO
      */
     public ProjectRelationshipDAO getProjectRelationshipDAO() {
         return projectRelationshipDAO;
     }
 
     /**
      * @param projectRelationshipDAO the projectRelationshipDAO to set
      */
     public void setProjectRelationshipDAO(ProjectRelationshipDAO projectRelationshipDAO) {
         this.projectRelationshipDAO = projectRelationshipDAO;
     }
 
     /**
      * @return the projectpropDAO
      */
     public ProjectpropDAO getProjectpropDAO() {
         return projectpropDAO;
     }
 
     /**
      * @param projectpropDAO the projectpropDAO to set
      */
     public void setProjectpropDAO(ProjectpropDAO projectpropDAO) {
         this.projectpropDAO = projectpropDAO;
     }
 
     /**
      * @return the stockDAO
      */
     public StockDAO getStockDAO() {
         return stockDAO;
     }
 
     /**
      * @param stockDAO the stockDAO to set
      */
     public void setStockDAO(StockDAO stockDAO) {
         this.stockDAO = stockDAO;
     }
 
     /**
      * @return the stockpropDAO
      */
     public StockpropDAO getStockpropDAO() {
         return stockpropDAO;
     }
 
     /**
      * @param stockpropDAO the stockpropDAO to set
      */
     public void setStockpropDAO(StockpropDAO stockpropDAO) {
         this.stockpropDAO = stockpropDAO;
     }
 
     /**
      * @return the utilityDAO
      */
     public UtilityDAO getUtilityDAO() {
         return utilityDAO;
     }
 
     /**
      * @param utilityDAO the utilityDAO to set
      */
     public void setUtilityDAO(UtilityDAO utilityDAO) {
         this.utilityDAO = utilityDAO;
     }
     
     
 }
