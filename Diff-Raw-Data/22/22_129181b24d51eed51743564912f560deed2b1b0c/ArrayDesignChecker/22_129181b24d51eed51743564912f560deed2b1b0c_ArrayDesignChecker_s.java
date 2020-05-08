 package gov.nih.nci.caintegrator2.application.arraydata;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.DnaAnalysisReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Gene;
 import gov.nih.nci.caintegrator2.domain.genomic.GeneExpressionReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.PlatformConfiguration;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 
 public class ArrayDesignChecker {
 
     public static Platform checkLoadAffymetrixSnpArrayDesign(File[] cdfs, AffymetrixDnaPlatformSource source, ArrayDataService service) throws PlatformLoadingException, AffymetrixCdfReadException {
         AffymetrixCdfReader cdfReaders[] = getCdfReaders(cdfs);
         Platform platform = retrievePlatform(source, service);
         if (platform.getId() == null) {
             platform.setId(1L);
         }
         assertNotNull(platform);
         assertEquals(source.getPlatformName(), platform.getName());
         assertEquals(PlatformVendorEnum.AFFYMETRIX, platform.getVendor());
         for (ReporterList reporterList : platform.getReporterLists()) {
             assertNotNull(reporterList.getName());
             assertNotNull(reporterList.getGenomeVersion());
         }
         for (AffymetrixCdfReader affymetrixCdfReader : cdfReaders) {
             assertTrue(platform.getReporterListListing().contains(affymetrixCdfReader.getCdfData().getChipType()));
             affymetrixCdfReader.close();
         }
         checkSnpReporters(platform);
         return platform;
     }
 
     private static AffymetrixCdfReader[] getCdfReaders(File[] cdfs) throws AffymetrixCdfReadException {
         AffymetrixCdfReader[] readers = new AffymetrixCdfReader[cdfs.length];
         for (int i = 0; i < cdfs.length; i++) {
             readers[i] = AffymetrixCdfReader.create(cdfs[i]);
         }
         return readers;
     }
 
     private static void checkSnpReporters(Platform platform) {
         PlatformHelper platformHelper = new PlatformHelper(platform);
         for (AbstractReporter abstractReporter : platformHelper.getAllReportersByType((ReporterTypeEnum.DNA_ANALYSIS_REPORTER))) {
             DnaAnalysisReporter reporter = (DnaAnalysisReporter) abstractReporter;
             assertFalse(StringUtils.isBlank(reporter.getName()));
             assertNotNull(reporter.getAlleleA());
             assertNotNull(reporter.getAlleleB());
         }
     }
 
     public static Platform checkLoadAffymetrixExpressionArrayDesign(File cdfFile, File annotationFile, ArrayDataService service) 
     throws PlatformLoadingException, AffymetrixCdfReadException {
         AffymetrixCdfReader cdfReader = AffymetrixCdfReader.create(cdfFile);
         AffymetrixExpressionPlatformSource source = new AffymetrixExpressionPlatformSource(annotationFile);
         Platform platform = retrievePlatform(source, service);
         if (platform.getId() == null) {
             platform.setId(1L);
         }
         checkPlatform(platform, cdfReader);
         checkProbeSetReporters(platform, cdfReader);
         checkGeneReporters(platform);
         cdfReader.close();
         return platform;
     }
 
     public static Platform checkLoadAgilentExpressionArrayDesign(String platformName, File annotationFile, ArrayDataService service) 
     throws PlatformLoadingException {
         AgilentExpressionPlatformSource source = new AgilentExpressionPlatformSource(annotationFile, platformName,
                 annotationFile.getName());
         Platform platform = retrievePlatform(source, service);
         if (platform.getId() == null) {
             platform.setId(1L);
         }
         checkGeneReporters(platform);
         return platform;
     }
 
     public static Platform checkLoadAgilentCopyNumberArrayDesign(String platformName, File annotationFile, ArrayDataService service) 
     throws PlatformLoadingException {
         AgilentDnaPlatformSource source = new AgilentDnaPlatformSource(annotationFile, platformName,
                 annotationFile.getName());
         Platform platform = retrievePlatform(source, service);
         if (platform.getId() == null) {
             platform.setId(1L);
         }
         checkDnaReporters(platform);
         return platform;
     }
 
     private static void checkDnaReporters(Platform platform) {
         PlatformHelper platformHelper = new PlatformHelper(platform);
         for (AbstractReporter abstractReporter : platformHelper.getAllReportersByType((ReporterTypeEnum.DNA_ANALYSIS_REPORTER))) {
             DnaAnalysisReporter reporter = (DnaAnalysisReporter) abstractReporter;
             assertFalse(StringUtils.isBlank(reporter.getName()));
         }
     }
 
     private static void checkGeneReporters(Platform platform) {
         PlatformHelper platformHelper = new PlatformHelper(platform);
         Set<ReporterList> geneReporters = platformHelper.getReporterLists(ReporterTypeEnum.GENE_EXPRESSION_GENE);
         for (ReporterList reporterList : geneReporters) {
             assertEquals(platform, reporterList.getPlatform());
             Set<String> geneSymbols = new HashSet<String>();
             for (AbstractReporter reporter : reporterList.getReporters()) {
                 assertEquals(1, reporter.getGenes().size());
                 Gene gene = reporter.getGenes().iterator().next();
                 assertEquals(reporter.getName(), gene.getSymbol());
                 geneSymbols.add(gene.getSymbol());
             }
             assertEquals(reporterList.getReporters().size(), geneSymbols.size());
         }
     }
 
     private static void checkProbeSetReporters(Platform platform, AffymetrixCdfReader cdfReader) {
         PlatformHelper platformHelper = new PlatformHelper(platform);
         Set<ReporterList> probeSetReporters = platformHelper.getReporterLists(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET);
         for (ReporterList reporterList : probeSetReporters) {
             assertEquals(platform, reporterList.getPlatform());
             assertEquals(cdfReader.getCdfData().getHeader().getNumProbeSets(), reporterList.getReporters().size());
             for (AbstractReporter abstractReporter : reporterList.getReporters()) {
                 GeneExpressionReporter reporter = (GeneExpressionReporter) abstractReporter;
                 assertFalse(StringUtils.isBlank(reporter.getName()));
             }
         }
     }
 
     private static void checkPlatform(Platform platform, AffymetrixCdfReader cdfReader) {
         assertNotNull(platform);
         assertEquals(cdfReader.getCdfData().getChipType(), platform.getName());
         assertEquals(PlatformVendorEnum.AFFYMETRIX, platform.getVendor());
         assertEquals(2, platform.getReporterLists().size());
     }
     
     private static Platform retrievePlatform(AbstractPlatformSource source, ArrayDataService service) {
        return service.loadArrayDesign(new PlatformConfiguration(source), null).getPlatform();
 
     }
 
 }
