 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline;
 
 import bwa_picard_gatk_pipeline.enums.TagEnum;
 import bwa_picard_gatk_pipeline.enums.TargetEnum;
 import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
 import bwa_picard_gatk_pipeline.exceptions.MappingException;
 import bwa_picard_gatk_pipeline.exceptions.TagProcessingException;
 import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
 import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
 import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
 import bwa_picard_gatk_pipeline.sge.BwaSolidMappingJob;
 import bwa_picard_gatk_pipeline.sge.PicardMergeBamJob;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.picard.sam.PicardGetReadCount;
 import net.sf.picard.sam.PicardBamMerger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.ggf.drmaa.DrmaaException;
 
 /**
  *
  * @author Wim Spee
  */
 public class Tag {
 
     //the required input for processing the tag
     private TagEnum name;
     private ReadGroup readGroup;
     private File outputDirTag;
     
     //the possible input for the processing of the tag
     private List<CsFastaFilePair> csfastaFiles;
     private List<FastQFile> fastQFiles;
     private File existingFastqChunkDir;
     private List<File> existingFastqChunksList;
     private File existingBamChunkDir;
     private List<File> existingBamChunksList;
     
     //the output of the processing
     private List<FastQChunk> fastQChunks;
     private List<File> bamChunks;
     private File mergedBamFile;
     
     private String[] bamExtension = new String[]{"bam"};
     private String[] fastqExtension = new String[]{"fastq"};
 
     public void startProcessing() throws TagProcessingException {
         try {
 
             readGroup.getLog().append("Started processing of read group " + name.toString());
 
 
             initalizeUnsetList();               //initialize the unset list to empty list and setup existing fastq chunks and existing bam chunks list
             lookupCsFastaAndQualFiles();        //lookup the csfasta and qual files for given csfasta paths
             convertCSFastaToFastQChunks();      //convert csfasta to fastq chunks
             convertFastQFilesToFastQChunks();   //if fastq files were given split them to fastq chunks
 
             if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.CHUNKS_BAM.getRank()) {
 
                 mapFastqFiles();     //map fastq chunks to bam files        
 
                 if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.TAG_BAM.getRank()) {
                     mergeBamChunks();               //merge the bam chunks
                     checkAllReadsAreAcountedFor();  //check all the reads are accounted for
                 }
             }
 
         }catch (MappingException ex) {
             readGroup.getLog().append("Could not map fastq chunks: " + ex.getMessage());
             throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
         } catch (IOException ex) {
             readGroup.getLog().append("Could not find a csfasta, fastq or bam file: " + ex.getMessage());
             throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
         } catch (InterruptedException ex) {
             Logger.getLogger(Tag.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DrmaaException ex) {
             Logger.getLogger(Tag.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JobFaillureException ex) {
             Logger.getLogger(Tag.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
         }
 
     }
 
     private void initalizeUnsetList() {
 
         if (csfastaFiles == null) {
             csfastaFiles = new ArrayList<CsFastaFilePair>();
         }
         if (fastQFiles == null) {
             fastQFiles = new ArrayList<FastQFile>();
         }
         if (fastQChunks == null) {
             fastQChunks = new ArrayList<FastQChunk>();
         }
         if (bamChunks == null) {
             bamChunks = new ArrayList<File>();
         }
 
         if (existingFastqChunkDir != null) {
             existingFastqChunksList = (List<File>) FileUtils.listFiles(existingFastqChunkDir, fastqExtension, false);
             System.out.println("Found existing "+existingFastqChunksList.size()+" fastq chunks");
             readGroup.getLog().append("Found existing "+existingFastqChunksList.size()+" fastq chunks");
             
         } else {
             existingFastqChunksList = new ArrayList<File>();
         }
 
         if (existingBamChunkDir != null) {
             existingBamChunksList = (List<File>) FileUtils.listFiles(existingBamChunkDir, bamExtension, false);
             System.out.println("Found existing "+existingBamChunksList.size()+" bam chunks");
             readGroup.getLog().append("Found existing "+existingBamChunksList.size()+" bam chunks");
         } else {
             existingBamChunksList = new ArrayList<File>();
         }
     }
     
      private void lookupCsFastaAndQualFiles() throws IOException {
         for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
             csFastaFilePair.lookupCsFastaFile();
             csFastaFilePair.lookupQualFile();
         }
     }
 
     private void convertCSFastaToFastQChunks() throws IOException {
 
         System.out.println("Converting csFasta to fastq ");
         List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();
 
         for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
 
             fastQChunksConverted.addAll(csFastaFilePair.convertToFastQ(outputDirTag, readGroup.getId(), readGroup.getGlobalConfiguration().getChunkSize()));
             readGroup.getLog().append(csFastaFilePair.toString());
         }
 
         readGroup.getLog().append("Converted " + csfastaFiles.size() + " csFastaFilePairs to " + fastQChunksConverted.size() + " Fastq chunks");
         fastQChunks.addAll(fastQChunksConverted);
     }
 
     private void convertFastQFilesToFastQChunks() throws FileNotFoundException, IOException {
         readGroup.getLog().append("Start splitting fastqFiles");
         List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();
 
         for (FastQFile fastQFile : fastQFiles) {
             fastQChunksConverted.addAll(fastQFile.splitFastQFile(readGroup.getGlobalConfiguration().getChunkSize(), outputDirTag, readGroup.getId()));
             readGroup.getLog().append(fastQFile.toString());
         }
 
         readGroup.getLog().append("Splitted " + fastQFiles.size() + " fastQFiles to " + fastQChunksConverted.size() + " fastq chunks");
         fastQChunks.addAll(fastQChunksConverted);
     }
 
     private List<BwaSolidMappingJob> createMappingJobs() throws IOException {
         List<BwaSolidMappingJob> bwaMappingJobs = new ArrayList<BwaSolidMappingJob>();
 
         for (FastQChunk fastQChunk : fastQChunks) {
             File bamFile = new File(outputDirTag, FilenameUtils.getBaseName(fastQChunk.getFastqFile().getPath()) + ".bam");
             bamChunks.add(bamFile);
 
             BwaSolidMappingJob bwaMappingJob = new BwaSolidMappingJob(fastQChunk.getFastqFile(), bamFile, readGroup);
             bwaMappingJobs.add(bwaMappingJob);
         }
         return bwaMappingJobs;
     }
     
      private void mapFastqFiles() throws MappingException, InterruptedException, IOException, DrmaaException, JobFaillureException {
 
         System.out.println("Starting submitting of mapping jobs");
         List<BwaSolidMappingJob> bwaMappingJobs = createMappingJobs();
 
         if (readGroup.getGlobalConfiguration().getOffline()) {
             mapOffline(bwaMappingJobs);
         } else {
             submitMappingJobs(bwaMappingJobs);
             waitForMappingJobs(bwaMappingJobs);
         }
 
     }
 
     private void submitMappingJobs(List<BwaSolidMappingJob> mappingJobs) throws DrmaaException {
         readGroup.getLog().append("Submitting "+mappingJobs.size() + " mapping jobs");
         for (BwaSolidMappingJob bwaMappingJob : mappingJobs) {
             bwaMappingJob.submit();
         }
     }   
 
     private void mapOffline(List<BwaSolidMappingJob> mappingJobs) throws MappingException, IOException, InterruptedException {
         readGroup.getLog().append("Executing "+mappingJobs.size() + " mapping jobs offline");
         for (BwaSolidMappingJob bwaSolidMappingJob : mappingJobs) {            
                 bwaSolidMappingJob.executeOffline();
         }
     }
 
     private void waitForMappingJobs(List<BwaSolidMappingJob> bwaMappingJobs) throws DrmaaException, JobFaillureException {
 
         for (BwaSolidMappingJob bwaMappingJob : bwaMappingJobs) {
             bwaMappingJob.waitFor();
         }
     }
 
     private void mergeBamChunks() throws IOException, InterruptedException {
 
        if (bamChunks.isEmpty()) { return; } //return if there are no bamchunks 
        
        File mergedBamDir = new File(outputDirTag, "MergedBam");
        mergedBamDir.mkdir();
        mergedBamFile = new File(mergedBamDir, readGroup.getId()+"_"+name.toString()+".bam");
        
 //       PicardBamMerger picardBamMerger = new PicardBamMerger();
 //       picardBamMerger.mergeBamFilesUsingPicard(bamChunks, mergedBamFile, readGroup.getGlobalConfiguration().getTmpDir());
        
        PicardMergeBamJob picardMergeBamJob = new PicardMergeBamJob(bamChunks, mergedBamFile, null, readGroup.getGlobalConfiguration());
        picardMergeBamJob.executeOffline();
 
     } 
     
 
     private void checkAllReadsAreAcountedFor() throws MappingException {
 
         if (fastQChunks.isEmpty()) {
             return;
         } //return if fastq chunks are empty and no mapping could have been done
 
         Long fastQRecords = getReadsInChunks();
         PicardGetReadCount picardGetReadCount = new PicardGetReadCount();
         Long readInBamFile = picardGetReadCount.getReadCount(mergedBamFile);
 
         if (fastQRecords.equals(readInBamFile)) {
             readGroup.getLog().append("Merged bam file and fastq contain same amount of reads: " + fastQRecords.toString());
         } else {
             readGroup.getLog().append("Merged bam file and fastq do not contain same amount of reads. Fastq: " + fastQRecords.toString() + " Bam: " + readInBamFile.toString());
             throw new MappingException("Merged bam file and fastq do not contain same amount of reads. Fastq: " + fastQRecords.toString() + " Bam: " + readInBamFile.toString());
 
         }
     }
     
     private Long getReadsInChunks() {
         Long counter = new Long(0);
         for (FastQChunk fastQChunk : fastQChunks) {
             counter = counter + fastQChunk.getRecordNr();
         }
 
         return counter;
 
     }
 
     //getters and setters used by the program
     public void setReadGroup(ReadGroup readGroup) {
         this.readGroup = readGroup;
     }
 
     public File getMergedBamFile() {
         return mergedBamFile;
     }
 
     public void createOutputDir(File readGroupOutputDir) {
         outputDirTag = new File(readGroupOutputDir, name.toString());
         outputDirTag.mkdir();
     }
 
     //Getter and setters, used mainly by Jackson to construct this tag from a Json object     
     public TagEnum getName() {
         return name;
     }
 
     public void setName(TagEnum name) {
         this.name = name;
     }
 
     public List<CsFastaFilePair> getCsfastaFiles() {
         return csfastaFiles;
     }
 
     public void setCsfastaFiles(List<CsFastaFilePair> csfastaFiles) {
         this.csfastaFiles = csfastaFiles;
     }
 
     public List<FastQFile> getFastQFiles() {
         return fastQFiles;
     }
 
     public void setFastQFiles(List<FastQFile> fastQFiles) {
         this.fastQFiles = fastQFiles;
     }
 
     public File getExistingFastqChunkDir() {
         return existingFastqChunkDir;
     }
 
     public void setExistingFastqChunkDir(File existingFastqChunkDir) {
         this.existingFastqChunkDir = existingFastqChunkDir;
     }
 
     public File getExistingBamChunkDir() {
         return existingBamChunkDir;
     }
 
     public void setExistingBamChunkDir(File existingBamChunkDir) {
         this.existingBamChunkDir = existingBamChunkDir;
     }
 }
