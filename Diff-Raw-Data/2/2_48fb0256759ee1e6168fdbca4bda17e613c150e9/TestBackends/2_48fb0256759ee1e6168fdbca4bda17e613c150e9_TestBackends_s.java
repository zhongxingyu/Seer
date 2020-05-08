 package io.seqware.queryengine.sandbox.testing;
 
 import io.seqware.queryengine.sandbox.testing.impl.ADAMBackendTest;
 import io.seqware.queryengine.sandbox.testing.impl.NoOpBackendTest;
 import io.seqware.queryengine.sandbox.testing.plugins.Feature;
 import io.seqware.queryengine.sandbox.testing.plugins.FeaturePluginInterface;
 import io.seqware.queryengine.sandbox.testing.plugins.FeatureSet;
 import io.seqware.queryengine.sandbox.testing.plugins.ReadPluginInterface;
 import io.seqware.queryengine.sandbox.testing.plugins.ReadSet;
 import io.seqware.queryengine.sandbox.testing.plugins.Reads;
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.junit.Assert;
 
 /**
  * TestBackends
  *
  * This tool is designed to iterate over the available backends and call them
  * one-by-one, producing data structures that can be turned into reports.
  *
  * Steps: 1) pick 6 VCF (chr21) and BAM files (pair) from 1000 genome project
  *
  * 2) for each backend, setup
  *
  * 3) load each of the 6 VCF (FeatureSet) and BAM (ReadSet)
  *
  * 4) then do 2-3 queries using the JSON format to specify key/value tags and
  * feature/readsets
  *
  * TODO: agree on the format of the file being returned... look like VCF and BAM
  * (SAM)
  *
  * 5) call each of the plugins 2-3 times with different JSON
  *
  * TODO: write plugins or make spec
  *
  * Data set 1:
  *
  * 6: teardown the backend
  *
  *
  *
  */
 public class TestBackends {
 
     private static void testBackend(BackendTestInterface backend, boolean browseReport, String[] args) throws RuntimeException, IOException {
         PrintWriter output = null;
         File tempFile = null;
         try {
 
             // data to download
             // use the same donor order in each array so BAM and VCF can be matched up
             // assumes there is a bam index named *.bam.bai
             String[] bams = new String[]{
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12156/cg_data/NA12156_lcl_SRR801819.mapped.COMPLETE_GENOMICS.CGworkflow2_2_evidenceOnly.CEU.high_coverage.20130401.bam",
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12155/cg_data/NA12155_lcl_SRR801818.mapped.COMPLETE_GENOMICS.CGworkflow2_2_evidenceOnly.CEU.high_coverage.20130401.bam",
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA07029/cg_data/NA07029_lcl_SRR800229.mapped.COMPLETE_GENOMICS.CGworkflow2_2_evidenceOnly.CEU.high_coverage.20130401.bam"
             };
             // assumes there is a vcf index named *.vcf.gz.tbi
             String[] vcfs = new String[]{
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12156/cg_data/NA12156_lcl_SRR801819.wgs.COMPLETE_GENOMICS.20121201.snps_indels_svs_meis.high_coverage.genotypes.vcf.gz",
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12155/cg_data/NA12155_lcl_SRR801818.wgs.COMPLETE_GENOMICS.20121201.snps_indels_svs_meis.high_coverage.genotypes.vcf.gz",
                 "ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA07029/cg_data/NA07029_lcl_SRR800229.wgs.COMPLETE_GENOMICS.20121201.snps_indels_svs_meis.high_coverage.genotypes.vcf.gz"
             };
             // now download
             List<File> localBams = download(bams, ".bai");
             List<File> localVCFs = download(vcfs, ".tbi");
 
 
             // read the settings file which is an INI
             HashMap<String, String> settings = (args != null && args.length > 0 ? readSettingsFile(args[0]) : null);
 
             // the test backends
             ArrayList<BackendTestInterface> backends = new ArrayList<>();
             backends.add(backend);
             tempFile = File.createTempFile("report", "html");
             // output file
             output = new PrintWriter(tempFile, "UTF-8");
             fillOutHeader(output);
 
             // so this is the heart of the testing process
             for (BackendTestInterface b : backends) {
 
                 output.write("<h1>" + b.getName() + "</h1>");
 
                 // get some initial docs 
                 Assert.assertTrue("introduction documents not generated", b.getIntroductionDocs().getKv().containsKey(BackendTestInterface.DOCS));
                 output.write(b.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));
 
                 // setup the backend
                 check(b.setupBackend(settings));
 
                 // iterate over the featureSets
                 ArrayList<String> featureSets = new ArrayList<>();
                 for (File vcfPath : localVCFs) {
                     ReturnValue loadFeatureSet = b.loadFeatureSet(vcfPath.getAbsolutePath());
                     Assert.assertTrue("feature set id not returned", loadFeatureSet.getKv().containsKey(BackendTestInterface.FEATURE_SET_ID));
                     check(loadFeatureSet);
                     featureSets.add(loadFeatureSet.getKv().get(BackendTestInterface.FEATURE_SET_ID));
                 }
 
                 // iterate over the readSets
                 ArrayList<String> readSets = new ArrayList<>();
                 for (File bamPath : localBams) {
                     ReturnValue loadReadSet = b.loadReadSet(bamPath.getAbsolutePath());
                     Assert.assertTrue("read set id not returned", loadReadSet.getKv().containsKey(BackendTestInterface.READ_SET_ID));
                     check(loadReadSet);
                     readSets.add(loadReadSet.getKv().get(BackendTestInterface.READ_SET_ID));
                 }
 
                 // query the features
                 output.write(testFeatureSets(featureSets, b));
 
                 // query the reads
                 output.write(testReadSets(readSets, b));
                 // TODO: run the plugins
                 // need to iterate over the available plugins
                 // TODO: we need to define a way to enumerate plugins, @ServiceInterface?
                 
                 // run the plugin with a blank query, meaning no pre-filtering of reads
                ReturnValue runReadPlugin = b.runPlugin("", SimpleFeaturesCountPlugin.class);
                 simpleFileCheck(runReadPlugin, BackendTestInterface.PLUGIN_RESULT_FILE);
                 // do some tests on the content of the plugin results, in this case a count of reads
                 
                 // run the plugin with a blank query, meaning no pre-filtering of features
                 ReturnValue runFeaturePlugin = b.runPlugin("", SimpleFeaturesCountPlugin.class);
                 simpleFileCheck(runFeaturePlugin, BackendTestInterface.PLUGIN_RESULT_FILE);
                 // do some tests on the content of the plugin results, in this case a count of reads
                 
                 // and then call b.runPlugin();
                 // final docs
                 ReturnValue conclusionDocs = b.getConclusionDocs();
                 Assert.assertTrue("conclusion docs not generated", conclusionDocs.getKv().containsKey(BackendTestInterface.DOCS));
                 output.write(conclusionDocs.getKv().get(BackendTestInterface.DOCS));
 
                 // teardown
                 check(b.teardownBackend(settings));
             }
 
             fillOutFooter(output);
 
         } catch (Exception ex) {
             Logger.getLogger(TestBackends.class.getName()).log(Level.SEVERE, null, ex);
             throw new RuntimeException(ex);
         } finally {
             IOUtils.closeQuietly(output);
 
             if (tempFile != null && browseReport) {
                 Desktop.getDesktop().browse((tempFile.toURI()));
             }
         }
     }
 
     private static void downloadFile(String file, File downloadDir, List<File> filesToReturn) throws IOException, MalformedURLException, URISyntaxException {
         URL newURL = new URL(file);
         String name = newURL.toString().substring(newURL.toString().lastIndexOf("/"));
         File targetFile = new File(downloadDir, name);
         if (!targetFile.exists()){
             System.out.println("Downloading " + newURL.getFile() + " to " + targetFile.getAbsolutePath());
             FileUtils.copyURLToFile(newURL, targetFile);
         }     
         filesToReturn.add(targetFile);
     }
 
     private static void simpleFileCheck(ReturnValue features, String returnKey) {
         String resultFile = features.getKv().get(returnKey);
         Assert.assertTrue("plugin results do not contain an expected result file", resultFile != null);
         Assert.assertTrue("result file does not exist", (new File(resultFile).exists()));
     }
     
     @Test
     public void testNoOpBackEnd() {
         try {
             testBackend(new NoOpBackendTest(), false, null);
         } catch (RuntimeException | IOException e) {
             Assert.assertTrue(false);
         }
     }
 
     /**
      * This tool assumes: "java TestBackends settings.ini"
      *
      * @param args
      */
     public static void main(String[] args) throws IOException {
         testBackend(new ADAMBackendTest(), true, args);
     }
 
     private static void fillOutHeader(PrintWriter o) {
         o.write("<html><body>");
     }
 
     private static void fillOutFooter(PrintWriter o) {
         o.write("</body></html>");
     }
 
     /**
      * TODO: this is where it should do heavy lifting of testing featureSet
      * queries using the getFeatures() method for each of the featureSets loaded
      * in the backend. You should do multiple JSON queries and these should cut
      * across featureSets. Work will need to be done to agree with the output
      * format.
      *
      * @param featureSets
      * @return
      */
     private static String testFeatureSets(ArrayList<String> featureSets, BackendTestInterface b) {
         try {
             // blank query should return all features from all feature sets
             ReturnValue features = b.getFeatures("");
             simpleFileCheck(features, BackendTestInterface.QUERY_RESULT_FILE);
             // query should return all features from chromosome 22 across all feature sets
             features = b.getFeatures("{\"regions\":{[\"chr22\"]}}");
             simpleFileCheck(features, BackendTestInterface.QUERY_RESULT_FILE);           
         } catch (JSONException|IOException ex) {
             throw new RuntimeException(ex);
         } 
         return ("<p>Featureset query testing completed</p>");
     }
 
     /**
      * TODO: this is where it should do heavy lifting of testing readSet queries
      * using the getReads() method for each of the readSets loaded in the
      * backend. You should do multiple JSON queries and these should cut across
      * readSets. Work will need to be done to agree with the output format.
      *
      * @param featureSets
      * @return
      */
     private static String testReadSets(ArrayList<String> readSets, BackendTestInterface b) {
           // blank query should return all features from all read sets
         ReturnValue features = b.getReads("");
         simpleFileCheck(features, BackendTestInterface.QUERY_RESULT_FILE);
         // query should return all features from chromosome 22 across all read sets
         features = b.getReads("{\"regions\":{[\"chr22\"]}}");
         simpleFileCheck(features, BackendTestInterface.QUERY_RESULT_FILE);
         return ("<p>Readset query testing completed</p>");
     }
 
     /**
      * TODO
      *
      * @param string
      */
     private static HashMap<String, String> readSettingsFile(String iniFile) {
         // need to parse the ini file passed in
         return (new HashMap<>());
     }
 
     /**
      * TODO: probably want a better check here, more sophisticated
      *
      * @param setupBackend
      */
     private static ReturnValue check(ReturnValue rv) {
         if (rv.getState() != ReturnValue.SUCCESS) {
             System.err.println("BOOM! Something bad happened! Error value: " + rv.getState());
             throw new RuntimeException(String.valueOf(rv.getState()));
         }
         return (rv);
     }
 
     /**
      * TODO
      *
      * @param bams
      * @return
      */
     private static List<File> download(String[] files, String indexExtension) {
         // need to download the files (and indexes) to a local directory then
         // populate a return String[] with thier local paths
         List<File> filesToReturn = new ArrayList<>();
         // always use the same directory so we do not re-download on repeated runs
         File downloadDir = new File("download_data");
         for (String file : files) {
             try {
                 downloadFile(file, downloadDir, filesToReturn);
                 downloadFile(file + indexExtension, downloadDir, filesToReturn);
                 // repeat download but with indexExtension
             } catch (MalformedURLException|URISyntaxException ex) {
                 throw new RuntimeException(ex);
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
         }
         return filesToReturn;
     }
     
     public class SimpleReadsCountPlugin extends AbstractPlugin<Reads, ReadSet> implements ReadPluginInterface{
     }
     
     public class SimpleFeaturesCountPlugin extends AbstractPlugin<Feature, FeatureSet> implements FeaturePluginInterface{
     }
     
     public abstract class AbstractPlugin <UNIT, SET>{
         public final String count = "COUNT";
         
         public void map(long position, Map<SET, Collection<UNIT>> reads, Map<String, String> output) {
             if (!output.containsKey(count)){
                 output.put(count, String.valueOf(0));
             }
             for(Collection<UNIT> readCollection  :reads.values()){
                 Integer currentCount = Integer.valueOf(output.get(count));
                 int nextCount = currentCount += readCollection.size();
                 output.put(count, String.valueOf(nextCount));
             }
         }
 
         public void reduce(String key, Iterable<String> values, Map<String, String> output) {
                 Integer currentCount = Integer.valueOf(output.get(count));
                 for(String value : values){
                     currentCount = currentCount += 1;
                 }
                 output.put(count, String.valueOf(currentCount));
         }
     }
     
 }
