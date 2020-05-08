 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline;
 
 import bwa_picard_gatk_pipeline.GSON.JSONConfig;
 import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
 import bwa_picard_gatk_pipeline.enums.TargetEnum;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 /**
  *
  * @author Wim spee
  */
 public class CommandLineClass {
 
     static File outputDir;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         //Define all options
         Options options = new Options();
         
         //general options
         options.addOption("i", "input", true, "the Json config file describing the samples, read groups, tags and files to process. ");
         options.addOption("o", "output", true, "base output directory. Subdirectories will be created in this dir for each sample, read group and tag. ");
         options.addOption("h", "help", false, "print this message");
         options.addOption("t", "target", true, "target point of the pipeline. One of the following:  FASTQ, CHUNKS_BAM, TAG_BAM, READGROUP_BAM, SAMPLE_BAM,  DEDUP_BAM, REALIGN_BAM, BQSR_BAM, SAMPLE_RAW_VCF, SAMPLE_ANNOTATED_VCF ");
         options.addOption("r", "reference", true, "reference file. fai and BWA indexes should be next to this file.");
         options.addOption("c", "chunk size", true, "chunk size for mapping. Default is 1.000.000 .");
         options.addOption("f", "offline", false, "do all the processing without using the Sun Grid Engine Cluster. This option is mainly for development and debugging purposes, running a real dataset offline will take to long. Default is false");
         options.addOption("m", "tmp-dir", true, "Temporary directory to use for merging bam files. To save IO  and network traffic it is wise to use a directory on the cluster master were the pipeline controller is running. Default is /tmp/ ");
         
         //picl options
         options.addOption("p", "picl", true, "Locatoin of Picl on the SGE cluster for pairing SOLID bam files. Default is /data_fedor8/common_scripts/Picl/picl");
         
         //picard options
         options.addOption("s", "picard", true, "Location on the SGE cluster of the Picard. Default is /data_fedor8/common_scripts/picard/picard-tools-1.89/picard-tools-1.89/");
         
         //bwa options
         options.addOption("cs_bwa", true, "Location of the last version of BWA that supports color space (0.5.9). Default is /usr/local/bwa/0.5.9/bwa");
         options.addOption("bwa", true, "Location of BWA. Default is /home/sge_share_fedor8/common_scripts/bwa/bwa-0.7.5a/bwa");
         options.addOption("bwa_mem", false, "Use bwa-mem for aligning instead of bwa-aln. Default is false (ie use bwa aln)");
         options.addOption("sam", true, "Location of samtools. Default is /usr/local/samtools/samtools");
         
         //gatk options
         options.addOption("g", "gatk", true, "Location of GATK. Default is /data_fedor8/common_scripts/GATK/GATK_2.6_3/GenomeAnalysisTK-2.6-3-gdee51c4/GenomeAnalysisTK.jar ");
         options.addOption("realign_known_indels", true, "Optional location of a vcf file with known indels which can be used to improve indel realignment. Can be supplied multiple times. The chromosome names and lenght should exaclty match the chromosomes in the reference that was used for mapping.  ");
         options.addOption("bqsr_known_variants", true, "Location of a vcf or bed file with known snp or indel variants for GATK BQSR. Can be supplied multiple times.  The chromosome names and lenght should exaclty match the chromosomes in the reference that was used for mapping.  ");
         options.addOption("gatk_vc", true, "GATK variant caller. Either UnifiedGenotyper or HaplotypeCaller . Default is UnifiedGenotyper");
         options.addOption("gatk_threads", true, "Number of threads that GATK should use on a SGE compute node. Default is 8, when doing offline processing number of threads is always set to 1.");
         options.addOption("gatk_mem", true, "Max memory that GATK should use on a SGE compute node. Default is 32, when doing offline processing max memory is always set to 2.");
         options.addOption("x", "call-reference", false, "Have GATK also output all the reference calls to VCF. Default is false");
         options.addOption("gatk_ms", false, "Have GATK do multi-sample calling. Default is false");
         
         //qualimap options
         options.addOption("q", "qualimap", true, "Location of qualimap. Default is /data_fedor8/common_scripts/qualimap/qualimap_v0.7.1/qualimap ");
         options.addOption("qualimap_threads", true, "Number of threads that Qualimap should use on a SGE compute node. Default is 8, when doing offline processing number of threads is always set to 1.");
         options.addOption("qualimap_mem", true, "Max memory that Qualimap should use on a SGE compute node. Default is 32, when doing offline processing max memory is always set to 2.");
         
         //fastqc options
          options.addOption("fastQC", true, "Location of fastQC. Default is /data_fedor8/common_scripts/FastQC/FastQC_v0.10.1/fastqc ");
         
         CommandLineParser parser = new GnuParser();
         CommandLine cmd = null;
         try {
             cmd = parser.parse(options, args);
         } catch (ParseException ex) {
             ex.printStackTrace();
             System.out.println("Could not parse arguments");
         }
         
         if (cmd.getOptions().length == 0) {
             printHelp(options);
         }            
         
 
         if (cmd.hasOption("h")) {
             printHelp(options);
         }              
 
         outputDir = new File(cmd.getOptionValue("o"));
         outputDir.mkdirs();
 
         //set all options
         GlobalConfiguration globalConfiguration = new GlobalConfiguration();
         
         //general options
         File JsonConfigFile = new File(cmd.getOptionValue("i"));
         globalConfiguration.setBaseOutputDir(outputDir);
         globalConfiguration.setChunkSize(new Integer(cmd.getOptionValue("c", "1000000")));
         globalConfiguration.setReferenceFile(new File(cmd.getOptionValue("r")));
         
         if (cmd.hasOption("f")) {
             globalConfiguration.setOffline(true);            
         } else {
             globalConfiguration.setOffline(false);
         }
         
         String targetString = cmd.getOptionValue("t");
         globalConfiguration.setTargetEnum(TargetEnum.valueOf(targetString));
         
         globalConfiguration.setTmpDir(new File(cmd.getOptionValue("tmp-dir", "/tmp")));
         
 
         //bwa options
         globalConfiguration.setColorSpaceBWA(new File(cmd.getOptionValue("cs-bwa", "/usr/local/bwa/0.5.9/bwa")));
         globalConfiguration.setBWA(new File(cmd.getOptionValue("bwa", "/data_fedor8/common_scripts/bwa/bwa-0.7.5a/bwa")));
         globalConfiguration.setSamtools(new File(cmd.getOptionValue("bwa", "/usr/local/samtools/samtools")));
         if (cmd.hasOption("bwa_mem")) {
             globalConfiguration.setUseBWAMEM(true);
         }
         else{
             globalConfiguration.setUseBWAMEM(false);
         }
         
         
         //picl options
         globalConfiguration.setPicl(new File(cmd.getOptionValue("p", "/data_fedor8/common_scripts/Picl/picl")));
         
         //picard options
         globalConfiguration.setPicardDirectory(new File(cmd.getOptionValue("s", "/data_fedor8/common_scripts/picard/picard-tools-1.89/picard-tools-1.89/")));        
         
         //gakt options
         globalConfiguration.setGatk(new File(cmd.getOptionValue("g", "/data_fedor8/common_scripts/GATK/GATK_2.6_3/GenomeAnalysisTK-2.6-3-gdee51c4/GenomeAnalysisTK.jar")));
         globalConfiguration.setGatkSGEThreads(new Integer(cmd.getOptionValue("gatk_threads", "8")));
         globalConfiguration.setGatkSGEMemory(new Integer(cmd.getOptionValue("gatk-mem", "32")));
         if (cmd.hasOption("x")) {
             globalConfiguration.setGatkCallReference(true);
         }
         else{
             globalConfiguration.setGatkCallReference(false);
         }
         
         //known indel files for indel realignment 
         if (cmd.hasOption("realign_known_indels")) {
             List<File> knownIndelRealignFiles = new ArrayList<File>();
             for(String knownIndelPath : cmd.getOptionValues("realign_known_indels"))
             {
                 knownIndelRealignFiles.add(new File(knownIndelPath));                
             }
             globalConfiguration.setRealignKnownIndels(knownIndelRealignFiles);            
             
         }    
         //known variants files for gatk bqsr
          if (cmd.hasOption("bqsr_known_variants")) {
             List<File> knownBQSRVariants = new ArrayList<File>();
             for(String knownIndelPath : cmd.getOptionValues("bqsr_known_variants"))
             {
                 knownBQSRVariants.add(new File(knownIndelPath));                
             }
             globalConfiguration.setBqsrKnownVariants(knownBQSRVariants);    
         }      
         
         GATKVariantCallers  caller;
         if(cmd.hasOption("gatk_vc"))
         {
             caller = GATKVariantCallers.valueOf(cmd.getOptionValue("gatk_vc"));
         }
         else
         {
             caller = GATKVariantCallers.UnifiedGenotyper;
         }
         globalConfiguration.setgATKVariantCaller(caller);
         
         if(cmd.hasOption("gatk_ms"))
         {
             globalConfiguration.setMultiSampleCalling(true);
         }
         else
         {
             globalConfiguration.setMultiSampleCalling(false);
         }
 
         //qualimap options
         globalConfiguration.setQualiMap(new File(cmd.getOptionValue("q", "/data_fedor8/common_scripts/qualimap/qualimap_v0.7.1/qualimap")));
         globalConfiguration.setQualimapSGEThreads(new Integer(cmd.getOptionValue("qualimap-threads", "8")));
         globalConfiguration.setQualimapSGEMemory(new Integer(cmd.getOptionValue("qualimap-mem", "32")));
         
         //fastqc
        globalConfiguration.setFastqQCFile(new File(cmd.getOptionValue("fastQC", "/data_fedor8/common_scripts/FastQC/FastQC_v0.10.1/fastqc")));
         
 
         List<Sample> samples = new ArrayList<Sample>();
 
         ObjectMapper mapper = new ObjectMapper();
         try {
             JSONConfig jsconConfig = mapper.readValue(JsonConfigFile, JSONConfig.class); // 'src' can be File, InputStream, Reader, String
 
             samples = jsconConfig.getSamples();
 
             for (Sample sample : samples) {
                 sample.setGlobalConfiguration(globalConfiguration);
                 sample.startProcessing();
             }
             
             Variants variants = new Variants(samples, globalConfiguration);
             
             if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_RAW_VCF.getRank()) {
                 variants.callRawSNPs();
             }
             if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_ANNOTATED_VCF.getRank()) {
                 //variants.annotateRawSNPs();
             }                    
 
 
         } catch (IOException ex) {
             Logger.getLogger(CommandLineClass.class.getName()).log(Level.SEVERE, null, ex);
 
         }
     }
 
     private static void printHelp(Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("BWA Picard GATK pipeline.  ", options);
         System.exit(1);
     }
 }
