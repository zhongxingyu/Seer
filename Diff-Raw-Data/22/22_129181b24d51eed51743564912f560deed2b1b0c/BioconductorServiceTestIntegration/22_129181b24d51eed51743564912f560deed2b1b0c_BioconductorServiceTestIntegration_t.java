 package gov.nih.nci.caintegrator2.external.bioconductor;
 
 import gov.nih.nci.caintegrator2.TestArrayDesignFiles;
 import gov.nih.nci.caintegrator2.TestDataFiles;
 import gov.nih.nci.caintegrator2.application.arraydata.AffymetrixDnaPlatformSource;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValueType;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformHelper;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformLoadingException;
 import gov.nih.nci.caintegrator2.application.study.CopyNumberDataConfiguration;
 import gov.nih.nci.caintegrator2.application.study.PublicAffymetrixCopyNumberChpParser;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.DnaAnalysisReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.PlatformConfiguration;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 public class BioconductorServiceTestIntegration extends AbstractTransactionalSpringContextTests {
 
     private BioconductorServiceImpl service = new BioconductorServiceImpl();
     private ArrayDataService arrayDataService;
     private CaIntegrator2Dao dao;
 
     public BioconductorServiceTestIntegration() {
         setDefaultRollback(true);
     }
     
     protected String[] getConfigLocations() {
         return new String[] {"classpath*:/**/service-test-integration-config.xml"};
     }
     
     @Test
     public void testAddSegmentationData() throws ConnectionException, PlatformLoadingException, DataRetrievalException {
         Platform platform = getOrCreatePlatform();
         List<DnaAnalysisReporter> reporters = getReporters(platform);
         CopyNumberData copyNumberData = new CopyNumberData(reporters);
         ArrayData arrayData = new ArrayData();
         arrayData.setId(0L);
         copyNumberData.addCopyNumberData(arrayData, getValues(TestDataFiles.HIND_COPY_NUMBER_CHP_FILE, reporters));
         CopyNumberDataConfiguration configuration = new CopyNumberDataConfiguration();
         ServerConnectionProfile server = new ServerConnectionProfile();
         server.setUrl("http://ncias-d227-v.nci.nih.gov:8080/wsrf/services/cagrid/CaDNAcopy");
         configuration.setCaDNACopyService(server);
         configuration.setChangePointSignificanceLevel(0.0);
         configuration.setEarlyStoppingCriterion(0.0);
         configuration.setPermutationReplicates(0);
         configuration.setRandomNumberSeed(0);
         service.addSegmentationData(copyNumberData, configuration);
         assertEquals(23, arrayData.getSegmentDatas().size());
     }
 
     @SuppressWarnings("unchecked")
     private float[] getValues(File chpFile, List<DnaAnalysisReporter> reporters) throws DataRetrievalException {
         PublicAffymetrixCopyNumberChpParser parser = new PublicAffymetrixCopyNumberChpParser(chpFile);
         ArrayData arrayData = new ArrayData();
         List untypedReporters = reporters;
         ArrayDataValues values = new ArrayDataValues(untypedReporters);
         parser.parse(values, arrayData);
         return values.getFloatValues(arrayData, ArrayDataValueType.COPY_NUMBER_LOG2_RATIO);
     }
 
     @SuppressWarnings("unchecked")
     private List<DnaAnalysisReporter> getReporters(Platform platform) {
         PlatformHelper helper = new PlatformHelper(platform);
         List list = helper.getAllReportersByType(ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
         return list;
     }
 
     private Platform getOrCreatePlatform() throws PlatformLoadingException {
         Platform platform = dao.getPlatform("Mapping50K_Hind240");
         if (platform == null) {
             List<File> files = new ArrayList<File>();
             files.add(TestArrayDesignFiles.MAPPING_50K_HIND_ANNOTATION_FILE);
             AffymetrixDnaPlatformSource source = new AffymetrixDnaPlatformSource(files, "Mapping50K_Hind240");
            PlatformConfiguration configuration = new PlatformConfiguration(source);
            getArrayDataService().savePlatformConfiguration(configuration);
            platform = getArrayDataService().loadArrayDesign(configuration, null).getPlatform();
         }
         return platform;
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
     /**
      * @return the dao
      */
     public CaIntegrator2Dao getDao() {
         return dao;
     }
 
     /**
      * @param dao the dao to set
      */
     public void setDao(CaIntegrator2Dao dao) {
         this.dao = dao;
     }
 
 }
