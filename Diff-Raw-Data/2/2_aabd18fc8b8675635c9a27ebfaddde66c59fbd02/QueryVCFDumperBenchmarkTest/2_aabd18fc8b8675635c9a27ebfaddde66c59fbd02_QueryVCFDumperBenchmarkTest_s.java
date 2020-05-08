 package com.github.seqware.queryengine.system.test;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.log4j.Logger;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.Benchmarking;
 import com.github.seqware.queryengine.Constants;
 import com.github.seqware.queryengine.Constants.OVERLAP_STRATEGY;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 import com.github.seqware.queryengine.model.Reference;
 import com.github.seqware.queryengine.system.ReferenceCreator;
 import com.github.seqware.queryengine.system.exporters.QueryVCFDumper;
 import com.github.seqware.queryengine.system.importers.SOFeatureImporter;
 import com.github.seqware.queryengine.util.SGID;
 
 public class QueryVCFDumperBenchmarkTest implements Benchmarking{
 	
     private Configuration config;
 	private static String randomRef = null;
     private static Reference reference = null;
 	private static SGID originalSet = null;
 	private static List<File> testingFiles = new ArrayList<File>();
 	private static final String DOWNLOAD_DIR = "/home/seqware/";
 	private static String FIRST_QUERY; 
 	private static String SECOND_QUERY; 
 	private static String THIRD_QUERY; 
 	private static String FOURTH_QUERY; 
 	private static long start, stop;
 	private static float diff;
 	private static List<Float> runQueryTimings = new ArrayList<Float>();
 	private static HashMap<String, List<Float>> allSingleScanRangeQueryTimings = new HashMap<String,List<Float>>();
 	private static HashMap<String, List<Float>> allMultiScanRangeQueryTimings = new HashMap<String,List<Float>>();
 	private static Float importTimingBinning;
 	private static Float importTimingNaiveOverlaps;
     private static File outputFile;
     
 	/**Set this to true if you wish to use the smaller file (faster test) or larger file (longer test)**/
 	private static boolean QUICK_TEST = false;
 
 	@BeforeClass
 	public static void setUpTest(){
 		try{
 			if (QUICK_TEST == false){
 				
 				/**This file contains 500,000 lines**/
 				String vcf = "https://dl.dropboxusercontent.com/u/3238966/ALL.wgs.VQSR_V2_GLs_polarized_biallelic.20101123.indels.sites500000Lines.vcf.gz";
 				
 				/**This file contains 1,000,000 lines**/
//				String vcf = "https://dl.dropboxusercontent.com/u/3238966/ALL.wgs.VQSR_V2_GLs_polarized_biallelic.20101123.indels.sites1MillionLines.vcf.gz";
 				
 				/**This file contains 2,000,000 lines**/
 				//String vcf = "https://dl.dropboxusercontent.com/u/3238966/ALL.wgs.VQSR_V2_GLs_polarized_biallelic.20101123.indels.sites.vcf.gz";
 				
 				/**This file contains 4,000,000 lines**/
 		        //String vcf = "http://ftp.1000genomes.ebi.ac.uk/vol1/ftp/phase1/analysis_results/consensus_call_sets/indels/ALL.wgs.VQSR_V2_GLs_polarized_biallelic.20101123.indels.sites.vcf.gz";
 				
 		        testingFiles = download(vcf);
 		        FIRST_QUERY = "start>=61800882 && stop <=81800882";
 		        SECOND_QUERY = "start>=61800882 && stop <=81800882 && (seqid==\"X\" || seqid==\"19\")";
 		        THIRD_QUERY = "start>=61800882 && stop <=81800882 || start >= 6180882 && stop <= 9180082";
 		        FOURTH_QUERY = "(start>=61800882 && stop <=81800882 || start >= 6180882 && stop <= 9180082) && (seqid==\"X\" || seqid==\"19\")";
 			} else if (QUICK_TEST == true){
 		        testingFiles.add(new File("/home/seqware/gitroot/queryengine/seqware-queryengine-backend/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/consequences_annotated.vcf"));
 		        FIRST_QUERY = "seqid==\"21\" ";
 		        SECOND_QUERY = "seqid==\"21\" && start >= 20000000 && stop <= 30000000";
 		        THIRD_QUERY = "seqid==\"21\" && start >= 20000000 && stop <= 30000000 || start >=40000000 && stop <=40200000";
 		        FOURTH_QUERY = "seqid==\"21\" && (start >= 20000000 && stop <= 30000000 || start >=40000000 && stop <=40200000)";
 			}
 	        outputFile = null;
 	        try {
 	            outputFile = File.createTempFile("output", "txt");
 	        } catch (IOException ex) {
 	            Logger.getLogger(QueryVCFDumperTest.class.getName()).fatal(null, ex);
 	            Assert.fail("Could not create output for test");
 	        }
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testBinning(){
 		try{
 			setOverlapStrategy(Constants.OVERLAP_STRATEGY.BINNING);
 	        System.out.println("Setting OVERLAP_STRATEGY => " + Constants.OVERLAP_STRATEGY.BINNING.toString() + "\n");
 
 			start = new Date().getTime();
 			importToBackend(testingFiles);
 			stop = new Date().getTime();
 			diff = ((stop - start) / 1000);
 			importTimingBinning = diff;
 			
 			Constants.MULTIPLE_SCAN_RANGES = false;
 			System.out.println("Setting MULTIPLE_SCAN_RANGES => " + Constants.MULTIPLE_SCAN_RANGES + "\n");
 			runQueryTimings = runQueries();
 	        allSingleScanRangeQueryTimings.put(Constants.OVERLAP_STRATEGY.BINNING.toString(), runQueryTimings);
 	        
 			Constants.MULTIPLE_SCAN_RANGES = true;
 			System.out.println("Setting MULTIPLE_SCAN_RANGES => " + Constants.MULTIPLE_SCAN_RANGES + "\n");
 			runQueryTimings = runQueries();
 			allMultiScanRangeQueryTimings.put(Constants.OVERLAP_STRATEGY.BINNING.toString(), runQueryTimings);
 			
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testNaiveOverlaps(){
 		try{
 			setOverlapStrategy(Constants.OVERLAP_STRATEGY.NAIVE_OVERLAPS);
 			System.out.println("Setting OVERLAP_STRATEGY => " + Constants.OVERLAP_STRATEGY.NAIVE_OVERLAPS.toString() + "\n");
 
 			start = new Date().getTime();
 			importToBackend(testingFiles);
 			stop = new Date().getTime();
 			diff = ((stop - start) / 1000);
 			importTimingNaiveOverlaps = diff;
 			
 			Constants.MULTIPLE_SCAN_RANGES = false;
 			System.out.println("Setting MULTIPLE_SCAN_RANGES => " + Constants.MULTIPLE_SCAN_RANGES + "\n");
 			runQueryTimings = runQueries();
 	        allSingleScanRangeQueryTimings.put(Constants.OVERLAP_STRATEGY.NAIVE_OVERLAPS.toString(), runQueryTimings);
 	        
 			Constants.MULTIPLE_SCAN_RANGES = true;
 			System.out.println("Setting MULTIPLE_SCAN_RANGES => " + Constants.MULTIPLE_SCAN_RANGES + "\n");
 			runQueryTimings = runQueries();
 	        allMultiScanRangeQueryTimings.put(Constants.OVERLAP_STRATEGY.NAIVE_OVERLAPS.toString(), runQueryTimings);
 			
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testGenerateReport(){
 		try{
 			generateReport();
 			resetAllTables();
 			System.out.println("Done!");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void setOverlapStrategy(OVERLAP_STRATEGY strategy){
 		Constants.OVERLAP_MODE = strategy;
 	}
 	
 	public void generateReport(){
 		int i;
 		float thisSet = 0;
 		float singleTotal = 0;
 		float multiTotal= 0;
 		float total = 0;
 		System.out.println("\n");
 		System.out.println("Import timing for Binning: " + String.valueOf(importTimingBinning) + "s" + "\t (" + importTimingBinning/60 + "min)" + "\n");
 		System.out.println("Import timing for Naive Overlaps: " + String.valueOf(importTimingNaiveOverlaps)+ "s" + "\t (" + importTimingNaiveOverlaps/60 + "min)" + "\n");
 		System.out.println("MULTIPLE SCAN RANGES = FALSE" );
 		for (Entry<String, List<Float>> e : allSingleScanRangeQueryTimings.entrySet()){
 			i=0;
 			System.out.println("\t" + "Using " + e.getKey() + ": ");
 			for (Float f : e.getValue()){
 				i++;
 				System.out.println("\t" + "\t" + "Time to complete Test #" + String.valueOf(i) + ": " + f + "s" + "\t (" + f/60 + "min)");
 				thisSet += f;
 			}
 			System.out.println("\t" + "Time to complete this set: " + thisSet + "s" + "\t (" + thisSet/60 + "min)");
 			singleTotal += thisSet;
 			thisSet = 0;
 		}
 		System.out.println("\t" + "Time to complete MULTIPLE SCAN RANGES = FALSE: " + singleTotal + "s" + "\t (" + singleTotal/60 + "min)");
 		System.out.println("\n");
 		
 		System.out.println("MULTIPLE SCAN RANGES = TRUE");
 		for (Entry<String, List<Float>> e : allMultiScanRangeQueryTimings.entrySet()){
 			i=0;
 			System.out.println("\t" + "Using " + e.getKey() + ": ");
 			for (Float f : e.getValue()){
 				i++;
 				System.out.println("\t" + "\t" + "Time to complete Test #" + String.valueOf(i) + ": " + f + "s" + "\t (" + f/60 + "min)");
 				thisSet += f;
 			}
 			System.out.println("\t" + "Time to complete this set: " + thisSet + "s" + "\t (" + thisSet/60 + "min)");
 			multiTotal += thisSet;
 			thisSet = 0;
 		}
 		System.out.println("\t" + "Time to complete MULTIPLE SCAN RANGES = TRUE: " + multiTotal + "s" + "\t (" + multiTotal/60 + "min)");
 		System.out.println("\n");
 		
 		total = singleTotal + multiTotal;
 		System.out.println("\t" + "**Time to complete all tests: " + total + "s" + "\t (" + total/60 + "min)");
 		System.out.println("");
 		System.out.println("Queries used:");
 		System.out.println("\t" + "Test #1: " + FIRST_QUERY);
 		System.out.println("\t" + "Test #2: " + SECOND_QUERY);
 		System.out.println("\t" + "Test #3: " + THIRD_QUERY);
 		System.out.println("\t" + "Test #4: " + FOURTH_QUERY);
 		System.out.println("\n");
 	}
 	
 	public void resetAllTables(){
         this.config = HBaseConfiguration.create();
 		try{
 			System.out.println("Closing tables....");
 			HBaseAdmin hba = new HBaseAdmin(config);
 			hba.disableTables("b.*");
 			hba.deleteTables("b.*");
 			hba.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
     private static void downloadFile(String file, File downloadDir, List<File> filesToReturnGZCompressed) throws IOException, MalformedURLException, URISyntaxException {
         URL newURL = new URL(file);
         String name = newURL.toString().substring(newURL.toString().lastIndexOf("/"));
         File targetFile = new File(downloadDir, name);
         if (!targetFile.exists()){
             System.out.println("Downloading " + newURL.getFile() + " to " + targetFile.getAbsolutePath());
             FileUtils.copyURLToFile(newURL, targetFile);
         }     
         filesToReturnGZCompressed.add(targetFile);
     }
     
     private static List<File> download(String file) {
         List<File> filesToReturnGZCompressed = new ArrayList<File>();
         List<File> filesToReturnGZUnCompressed = new ArrayList<File>();
         // always use the same directory so we do not re-download on repeated runs
         File downloadDir = new File(DOWNLOAD_DIR);
         try {
             downloadFile(file, downloadDir, filesToReturnGZCompressed);
         } catch (URISyntaxException ex) {
             throw new RuntimeException(ex);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
         for (File thisGZCompressedFile : filesToReturnGZCompressed){
         	try{
 	        	File thisGZUncompressedFile = new File("");
 	        	thisGZUncompressedFile = gzDecompressor(thisGZCompressedFile);
 	        	Logger.getLogger(QueryVCFDumperBenchmarkTest.class).info("CompressedFile: " + thisGZCompressedFile.getAbsolutePath());
 	        	Logger.getLogger(QueryVCFDumperBenchmarkTest.class).info("DeCompressedFile: " + thisGZUncompressedFile.getAbsolutePath());
 	        	System.out.println("Done!\n");
 	        	filesToReturnGZUnCompressed.add(thisGZUncompressedFile);
         	} catch (Exception e){
         		e.printStackTrace();
         	}
         }
         return filesToReturnGZUnCompressed;
     }
 	
     private static void importToBackend(List<File> file){
     	try{
     			//Use first file only for now
     			File f = file.get(0);
 	    		Assert.assertTrue("Cannot read VCF file for test", f.exists() && f.canRead());
 	            List<String> argList = new ArrayList<String>();
 	            randomRef = "Random_ref_" + new BigInteger(20, new SecureRandom()).toString(32);
 	            SGID refID = ReferenceCreator.mainMethod(new String[]{randomRef});
 	            reference = SWQEFactory.getQueryInterface().getAtomBySGID(Reference.class, refID);
 	            
 	            argList.addAll(Arrays.asList(new String[]{"-w", "VCFVariantImportWorker",
 	                    "-i", f.getAbsolutePath(),
 	                    "-r", reference.getSGID().getRowKey()}));
 	            
 	    		System.out.println("Importing " + testingFiles.get(0).getName() + " to database.\n");
 	            
 	            originalSet = SOFeatureImporter.runMain(argList.toArray(new String[argList.size()]));
 	            Assert.assertTrue("Could not import VCF for test", originalSet != null);
     	} catch (Exception e){
     		e.printStackTrace();
     	}
     }
     
     private static File gzDecompressor(File filePathGZ) throws IOException{
   	  String filename = filePathGZ
   				.getName()
   				.substring(0, filePathGZ.getName().lastIndexOf("."));
   	  byte[] buf = 
   			  new byte[1024];
       int len;
       File thisGZUncompressedFile;
   	  String outFilename = DOWNLOAD_DIR + filename;
   	  FileInputStream instream = 
   			  new FileInputStream(filePathGZ);
         GZIPInputStream ginstream = 
       		  new GZIPInputStream(instream);
         FileOutputStream outstream = 
       		  new FileOutputStream(outFilename);
         System.out.println("Decompressing... " + filePathGZ);
         while ((len = ginstream.read(buf)) > 0) 
        {
          outstream.write(buf, 0, len);
        }
         outstream.close();
         ginstream.close();
         thisGZUncompressedFile = new File(outFilename);
         
   	  return thisGZUncompressedFile;
     }
 
     private void testFirstQuery(){
         File keyValueFile = null;
         try {
             keyValueFile = File.createTempFile("keyValue", "txt");
         } catch (IOException ex) {
             Logger.getLogger(QueryVCFDumperTest.class.getName()).fatal(null, ex);
             Assert.fail("Could not create output for test");
         }
 
         List<String> argList = new ArrayList<String>();
         argList.addAll(Arrays.asList(new String[]{"-f", originalSet.getRowKey(),
                     "-k", keyValueFile.getAbsolutePath(), "-s", FIRST_QUERY,
                     "-o", outputFile.getAbsolutePath()}));
         
         Stack<SGID> runMain = QueryVCFDumper.runMain(argList.toArray(new String[argList.size()]));
     }
     
     private void testSecondQuery(){
         File keyValueFile = null;
         try {
             keyValueFile = File.createTempFile("keyValue", "txt");
         } catch (IOException ex) {
             Logger.getLogger(QueryVCFDumperTest.class.getName()).fatal(null, ex);
             Assert.fail("Could not create output for test");
         }
 
         List<String> argList = new ArrayList<String>();
         argList.addAll(Arrays.asList(new String[]{"-f", originalSet.getRowKey(),
                     "-k", keyValueFile.getAbsolutePath(), "-s", SECOND_QUERY,
                     "-o", outputFile.getAbsolutePath()}));
         Stack<SGID> runMain = QueryVCFDumper.runMain(argList.toArray(new String[argList.size()]));
     }
     
     private void testThirdQuery(){
         File keyValueFile = null;
         try {
             keyValueFile = File.createTempFile("keyValue", "txt");
         } catch (IOException ex) {
             Logger.getLogger(QueryVCFDumperTest.class.getName()).fatal(null, ex);
             Assert.fail("Could not create output for test");
         }
 
         List<String> argList = new ArrayList<String>();
         argList.addAll(Arrays.asList(new String[]{"-f", originalSet.getRowKey(),
                     "-k", keyValueFile.getAbsolutePath(), "-s", THIRD_QUERY,
                     "-o", outputFile.getAbsolutePath()}));
         Stack<SGID> runMain = QueryVCFDumper.runMain(argList.toArray(new String[argList.size()]));
     }
     
     private void testFourthQuery(){
         File keyValueFile = null;
         try {
             keyValueFile = File.createTempFile("keyValue", "txt");
         } catch (IOException ex) {
             Logger.getLogger(QueryVCFDumperTest.class.getName()).fatal(null, ex);
             Assert.fail("Could not create output for test");
         }
 
         List<String> argList = new ArrayList<String>();
         argList.addAll(Arrays.asList(new String[]{"-f", originalSet.getRowKey(),
                     "-k", keyValueFile.getAbsolutePath(), "-s", FOURTH_QUERY,
                     "-o", outputFile.getAbsolutePath()}));
         Stack<SGID> runMain = QueryVCFDumper.runMain(argList.toArray(new String[argList.size()]));
     }
 
     private List<Float> runQueries(){
     	List<Float> runQueryTimings = new ArrayList<Float>();
     	
     	System.out.println("Running first query....\n");
 		start = new Date().getTime();
     	testFirstQuery();
         stop = new Date().getTime();
         diff = ((stop - start) / 1000) ;
         runQueryTimings.add(diff);
         
         System.out.println("Running second query....\n");
 		start = new Date().getTime();
     	testSecondQuery();
         stop = new Date().getTime();
         diff = ((stop - start) / 1000) ;
         runQueryTimings.add(diff);
         
         System.out.println("Running third query....\n");
 		start = new Date().getTime();
     	testThirdQuery();
         stop = new Date().getTime();
         diff = ((stop - start) / 1000) ;
         runQueryTimings.add(diff);
         
         System.out.println("Running fourth query....\n");
 		start = new Date().getTime();
     	testFourthQuery();
         stop = new Date().getTime();
         diff = ((stop - start) / 1000) ;
         runQueryTimings.add(diff);
         
         return runQueryTimings;
     }
 }
