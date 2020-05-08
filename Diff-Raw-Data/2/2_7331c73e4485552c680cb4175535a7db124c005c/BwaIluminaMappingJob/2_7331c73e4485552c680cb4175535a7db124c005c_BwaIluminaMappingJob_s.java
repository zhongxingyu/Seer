 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob;
 
 import bwa_picard_gatk_pipeline.readGroup.ReadGroupIlumina;
 import bwa_picard_gatk_pipeline.sge.Job;
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FilenameUtils;
 
 /**
  *
  * @author wim
  */
 public class BwaIluminaMappingJob extends Job{
     
     private String sgeName;
     private File firstReadsFastqFile;
     private File secondReadsFastqFile;
     private File bamFile;
     
     private ReadGroupIlumina readGroup;
 
     public BwaIluminaMappingJob(File firstReadsFastqFile, File secondReadsFastqFile,  File bamFile,  ReadGroupIlumina readGroup) throws IOException {
 
         super(FilenameUtils.removeExtension(firstReadsFastqFile.getAbsolutePath()) + ".sh");
 
         this.firstReadsFastqFile = firstReadsFastqFile;
         this.secondReadsFastqFile = secondReadsFastqFile;
         this.readGroup = readGroup;
         this.bamFile = bamFile;
         
         //if there is no second fastqFile (ie fragment data)
         if(secondReadsFastqFile== null)
         {
             addCommandsFragment();
         }
         else // if there is a second fastFile (ie PE)
         {
             addCommandsPE();
         }
 
         
 
         sgeName = "BWA_" + firstReadsFastqFile.getName();
         close();
     }
 
     @Override
     public String getSGEName() {
         return sgeName;
     }
 
     private void addCommandsFragment() throws IOException {
         
         File referenceFile = readGroup.getGlobalConfiguration().getReferenceFile();
         File referenceIndex = new File(referenceFile.getAbsolutePath() + ".fai");
         File samtoolsFile = new File("/usr/local/samtools/samtools");
         File bwaFile = readGroup.getGlobalConfiguration().getBWA();      
         String bwaOptions = "aln -l 25 -k 2 ";
         
         
         String baseNameFirst = FilenameUtils.getBaseName(firstReadsFastqFile.getPath());
         File tmpDir = new File("/tmp/" + baseNameFirst);
         
         File copiedFirstFastqFile = new File(tmpDir, firstReadsFastqFile.getName());
         File bwaOutputFirstFile = new File(tmpDir, baseNameFirst + ".out");          
         
         File samFile = new File(tmpDir, baseNameFirst + ".sam");
         File tmpBamFile = new File(tmpDir, baseNameFirst + ".bam" );
        
         File bamFileSorted = new File(tmpDir, baseNameFirst + "_sorted.bam");
         
         File parentDir = firstReadsFastqFile.getParentFile();
         File logFile = new File(parentDir, baseNameFirst + ".log");
 
         String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
 
         
 
         //add sge hostname and date information to log
         addCommand("uname -n " + appendAlloutputToLog);
         addCommand("\n");
         addCommand("date " + appendAlloutputToLog);
         addCommand("\n");
         //create a tmp dir
         addCommand("mkdir " + tmpDir + appendAlloutputToLog);
         addCommand("\n");
         //copy the fastQFile to the tmp dir
         addCommand("echo starting copying of fastq file " + appendAlloutputToLog);
         addCommand("cp " + firstReadsFastqFile.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
         addCommand("\n");
         //map using bwa
         addCommand("echo starting mapping of fastq file " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
         addCommand(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getAbsolutePath() + " " + copiedFirstFastqFile.getAbsolutePath() + " > " + bwaOutputFirstFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
         addCommand("\n");
         //create sam file from output
         addCommand("echo starting converting to sam " + appendAlloutputToLog);
         addCommand("date " + appendAlloutputToLog);
         addCommand(bwaFile.getPath() + " samse -r \"@RG\\tID:" + readGroup.getId()+ "\\tPL:ILLUMINA\\tLB:"+  readGroup.getLibrary() + "\\tSM:" + readGroup.getSample() + "\\tDS:" + readGroup.getDescription()+ "\" " +referenceFile.getAbsolutePath()+ " "  + bwaOutputFirstFile.getAbsolutePath() + " " + copiedFirstFastqFile.getAbsolutePath() + " > " + samFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
         addCommand("\n");
         //create bam file from sam file
         addCommand("echo starting converting to bam " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog );
         addCommand(samtoolsFile.getPath() + " import " + referenceIndex.getAbsolutePath() + " " + samFile.getAbsolutePath() + " " + tmpBamFile.getAbsolutePath() + appendAlloutputToLog);
         addCommand("\n");
         //sort the bam file
         addCommand("echo starting sorting of bam " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
         addCommand(samtoolsFile.getPath() + " sort " + tmpBamFile.getAbsolutePath() + " " + FilenameUtils.removeExtension(bamFileSorted.getAbsolutePath()) + appendAlloutputToLog);
         addCommand("\n");
         //copy the bamFile back to the server
         addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
         addCommand("date " + appendAlloutputToLog);
         addCommand("cp " + bamFileSorted.getAbsolutePath() + " " + bamFile.getAbsolutePath());
         addCommand("\n");
         //remove the tmp dir from the sge host
         addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
         addCommand("\n");
         addCommand("echo finished " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
     }
     
     
     private void addCommandsPE() throws IOException {
         
         
         File referenceFile = readGroup.getGlobalConfiguration().getReferenceFile();
         File referenceIndex = new File(referenceFile.getAbsolutePath() + ".fai");
         File samtoolsFile = new File("/usr/local/samtools/samtools");
         File bwaFile = readGroup.getGlobalConfiguration().getBWA();        
         String bwaOptions = "aln -l 25 -k 2 ";
         
         String baseNameFirst = FilenameUtils.getBaseName(firstReadsFastqFile.getPath());
         String baseNameSecond = FilenameUtils.getBaseName(secondReadsFastqFile.getPath());
         
         File tmpDir = new File("/tmp/" + baseNameFirst);
         
         File copiedFirstFastqFile = new File(tmpDir, firstReadsFastqFile.getName());
         File copiedSecondFastqFile = new File(tmpDir, secondReadsFastqFile.getName());
         File bwaOutputFirstFile = new File(tmpDir, baseNameFirst + ".out");          
         File bwaOutputSecondFile = new File(tmpDir, baseNameSecond + ".out");  
         
         File pairedSamFile = new File(tmpDir, baseNameFirst + "paired.sam");
         File tmpPairedBamFile = new File(tmpDir, baseNameFirst + "paired.bam" );
        
         File pairedBamFileSorted = new File(tmpDir, baseNameFirst + "paired_sorted.bam");        
 
         File parentDir = firstReadsFastqFile.getParentFile();
         File logFile = new File(parentDir, baseNameFirst + ".log");        
 
         String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
 
         //add sge hostname and date information to log
         addCommand("uname -n " + appendAlloutputToLog);
         addCommand("\n");
         addCommand("date " + appendAlloutputToLog);
         addCommand("\n");
         //create a tmp dir
         addCommand("mkdir " + tmpDir + appendAlloutputToLog);
         addCommand("\n");
         //copy the fastQFile to the tmp dir
         addCommand("echo starting copying of fastq file " + appendAlloutputToLog);
         addCommand("cp " + firstReadsFastqFile.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
         addCommand("cp " + secondReadsFastqFile.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
         
         addCommand("\n");
         //map using bwa
         addCommand("echo starting mapping of first fastq file " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
         addCommand(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getAbsolutePath() + " " + copiedFirstFastqFile.getAbsolutePath() + " > " + bwaOutputFirstFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
         addCommand("\n");
          addCommand("echo starting mapping of second fastq file " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
         addCommand(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getAbsolutePath() + " " + copiedSecondFastqFile.getAbsolutePath() + " > " + bwaOutputSecondFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
         addCommand("\n");
         //create sam file from output
         addCommand("echo starting converting to sam " + appendAlloutputToLog);
         addCommand("date " + appendAlloutputToLog);
        addCommand(bwaFile.getPath() + " sampe -r \"@RG\\tID:" + readGroup.getId()+ "\\tPL:SOLID\\tLB:"+  readGroup.getLibrary() + "\\tSM:" + readGroup.getSample() + "\\tDS:" + readGroup.getDescription()+ "\" " +referenceFile.getAbsolutePath()+ " "  + bwaOutputFirstFile.getAbsolutePath() + " " + bwaOutputSecondFile.getAbsolutePath()+ " " + copiedFirstFastqFile.getAbsolutePath() + " "+ copiedSecondFastqFile.getAbsolutePath() +  " > " + pairedSamFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
         addCommand("\n");
         //create bam file from sam file
         addCommand("echo starting converting to bam " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog );
         addCommand(samtoolsFile.getPath() + " import " + referenceIndex.getAbsolutePath() + " " + pairedSamFile.getAbsolutePath() + " " + tmpPairedBamFile.getAbsolutePath() + appendAlloutputToLog);
         addCommand("\n");
         //sort the bam file
         addCommand("echo starting sorting of bam " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
         addCommand(samtoolsFile.getPath() + " sort " + tmpPairedBamFile.getAbsolutePath() + " " + FilenameUtils.removeExtension(pairedBamFileSorted.getAbsolutePath()) + appendAlloutputToLog);
         addCommand("\n");
         //copy the bamFile back to the server
         addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
         addCommand("date " + appendAlloutputToLog);
         addCommand("cp " + pairedBamFileSorted.getAbsolutePath() + " " + bamFile.getAbsolutePath());
         addCommand("\n");
         //remove the tmp dir from the sge host
         addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
         addCommand("\n");
         addCommand("echo finished " + appendAlloutputToLog);
         addCommand("date  " + appendAlloutputToLog);
     }
     
     
     
     
     
     
 }
