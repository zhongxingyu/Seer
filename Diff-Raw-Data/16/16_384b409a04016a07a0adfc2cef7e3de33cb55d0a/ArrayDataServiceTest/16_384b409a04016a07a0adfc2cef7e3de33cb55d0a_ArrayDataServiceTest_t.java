 package gov.nih.nci.caintegrator2.application.arraydata;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import gov.nih.nci.caintegrator2.TestArrayDesignFiles;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoStub;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 
 import java.io.File;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class ArrayDataServiceTest {
     
     private CaIntegrator2DaoStub daoStub;
     private ArrayDataService service;
 
     @Before
     public void setUp() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("arraydata-test-config.xml", ArrayDataServiceTest.class); 
         service = (ArrayDataService) context.getBean("arrayDataService"); 
         daoStub = (CaIntegrator2DaoStub) context.getBean("daoStub");
         daoStub.clear();                
     }
 
     @Test
     public void testLoadArrayDesign() throws PlatformLoadingException, AffymetrixCdfReadException {
         checkLoadArrayDesign(TestArrayDesignFiles.YEAST_2_CDF_FILE, TestArrayDesignFiles.YEAST_2_ANNOTATION_FILE);
     }
 
     private void checkLoadArrayDesign(File cdfFile, File annotationFile) throws PlatformLoadingException, AffymetrixCdfReadException {
         Platform platform = ArrayDesignChecker.checkLoadArrayDesign(cdfFile, annotationFile, service);
         assertTrue(daoStub.saveCalled);
         PlatformHelper platformHelper = new PlatformHelper(platform);
         Collection<AbstractReporter> geneReporters = platformHelper.getReporterSet(ReporterTypeEnum.GENE_EXPRESSION_GENE).getReporters();
         assertEquals(4563, geneReporters.size());
         for (AbstractReporter abstractReporter : geneReporters) {
             GeneExpressionReporter geneReporter = (GeneExpressionReporter) abstractReporter;
             Collection<AbstractReporter> probeSets = platformHelper.getReportersForGene(geneReporter.getGene(), ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
             for (AbstractReporter probeSetReporter : probeSets) {
                 GeneExpressionReporter probeSet = (GeneExpressionReporter) probeSetReporter;
                 assertTrue(geneReporter.getGene() == probeSet.getGene());
             }
         }
     }
 
 }
