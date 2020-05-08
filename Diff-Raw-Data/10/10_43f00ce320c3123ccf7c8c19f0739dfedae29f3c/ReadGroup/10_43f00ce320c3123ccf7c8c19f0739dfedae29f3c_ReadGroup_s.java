 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline.readGroup;
 
 import bwa_picard_gatk_pipeline.GlobalConfiguration;
 import bwa_picard_gatk_pipeline.enums.TargetEnum;
 import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
 import bwa_picard_gatk_pipeline.exceptions.MappingException;
 import bwa_picard_gatk_pipeline.sge.Job;
 import bwa_picard_gatk_pipeline.sge.QualimapJob;
 import bwa_picard_gatk_pipeline.sge.picard.mergeBAM.PicardMergeBamJob;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.picard.sam.PicardGetReadCount;
 import org.apache.commons.io.FilenameUtils;
 import org.ggf.drmaa.DrmaaException;
 
 /**
  *
  * @author wim
  */
 public abstract class ReadGroup {
 
     protected String id;
     protected String library;
     protected String sample;
     protected String description;
     //the output of the processing    
     protected List<File> bamChunks;
     protected File readGroupBam;
     //the file extensions    
     protected String[] bamExtension = new String[]{"bam"};
     protected String[] fastqExtension = new String[]{"fastq"};
     protected GlobalConfiguration gc;
     protected File readGroupOutputDir;
     protected ReadGroupLogFile log;
 
     public void createOutputDir(File sampleOutputDir) {
         readGroupOutputDir = new File(sampleOutputDir, id);
         readGroupOutputDir.mkdir();
 
     }
 
     public void startProcessing() {
 
         log = new ReadGroupLogFile(readGroupOutputDir, id);
 
         initalizeUnsetList();           //initialize the unset list to empty list  
         try {
 
             prepareReadsForMapping();
 
             if (gc.getTargetEnum().getRank() >= TargetEnum.CHUNKS_BAM.getRank()) {
                 mapFastqFiles();     //map fastq chunks to bam files     
             }
 
             if (gc.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) {
 
                 mergeBamChunks();               //merge the bam chunks
                 checkAllReadsAreAcountedFor();  //check all the reads are accounted for
                 runQualimap(readGroupBam);
             }
 
         } catch (IOException ex) {
             Logger.getLogger(ReadGroupSolidFragment.class.getName()).log(Level.SEVERE, null, ex);
         } catch (MappingException ex) {
             Logger.getLogger(ReadGroupSolidFragment.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(ReadGroupSolidFragment.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DrmaaException ex) {
             Logger.getLogger(ReadGroupSolidFragment.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JobFaillureException ex) {
             Logger.getLogger(ReadGroupSolidFragment.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         log.close();
     }
 
     private void initalizeUnsetList() {
 
 
         if (bamChunks == null) {
             bamChunks = new ArrayList<File>();
         }
     }
 
     protected abstract void prepareReadsForMapping() throws IOException;
 
     protected void mapFastqFiles() throws MappingException, InterruptedException, IOException, DrmaaException, JobFaillureException {
 
         System.out.println("Starting submitting of mapping jobs");
         List<Job> bwaMappingJobs = createMappingJobs();
 
         if (gc.getOffline()) {
             mapOffline(bwaMappingJobs);
         } else {
             submitMappingJobs(bwaMappingJobs);
             waitForMappingJobs(bwaMappingJobs);
         }
     }
 
     protected abstract List<Job> createMappingJobs() throws IOException;
 
     protected void submitMappingJobs(List<Job> mappingJobs) throws DrmaaException {
         getLog().append("Submitting " + mappingJobs.size() + " mapping jobs");
         for (Job bwaMappingJob : mappingJobs) {
             bwaMappingJob.submit();
         }
     }
 
     protected void mapOffline(List<Job> mappingJobs) throws MappingException, IOException, InterruptedException {
         getLog().append("Executing " + mappingJobs.size() + " mapping jobs offline");
         for (Job bwaSolidMappingJob : mappingJobs) {
             bwaSolidMappingJob.executeOffline();
             bwaSolidMappingJob.waitForOfflineExecution();
         }
     }
 
     protected void waitForMappingJobs(List<Job> bwaMappingJobs) throws DrmaaException, JobFaillureException {
 
         for (Job bwaMappingJob : bwaMappingJobs) {
             bwaMappingJob.waitFor();
         }
     }
 
     protected void mergeBamChunks() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
 
         if (bamChunks.isEmpty()) {
             return;
         } //return if there are no bamchunks 
 
 
         File mergedBamDir = new File(readGroupOutputDir, "MergedBam");
         mergedBamDir.mkdir();
         readGroupBam = new File(mergedBamDir, id + "_" + ".bam");
 
         File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");
 
         PicardMergeBamJob picardMergeBamJob = new PicardMergeBamJob(bamChunks, readGroupBam, null, gc.getTmpDir(), picardMergeSam);
         if (gc.getOffline()) {
             picardMergeBamJob.executeOffline();
             picardMergeBamJob.waitForOfflineExecution();
         } else {
             picardMergeBamJob.submit();
             picardMergeBamJob.waitFor();
         }
 
     }
 
     protected void runQualimap(File bam) throws IOException, InterruptedException, DrmaaException {
 
         File qualimapReport = new File(bam.getParentFile(), FilenameUtils.getBaseName(bam.getName()) + "_qualimap.pdf");
         QualimapJob qualimapJob = new QualimapJob(bam, qualimapReport, gc, null);
         
         if(gc.getOffline())
         {
             qualimapJob.executeOffline();
         }
         else
         {
             qualimapJob.submit();
         }              
     }
 
     protected void checkAllReadsAreAcountedFor() throws MappingException {
 
         Long inputReadsNr = getReadsInChunks();
 
        if (inputReadsNr == new Long(-1)) {
             return;
         }   //return if processing did not start with reads
         PicardGetReadCount picardGetReadCount = new PicardGetReadCount();
         Long readInBamFile = picardGetReadCount.getReadCount(readGroupBam);
 
         if (inputReadsNr.equals(readInBamFile)) {
             getLog().append("Merged bam file and fastq contain same amount of reads: " + inputReadsNr.toString());
         } else {
             getLog().append("Merged bam file and fastq do not contain same amount of reads. Fastq: " + inputReadsNr.toString() + " Bam: " + readInBamFile.toString());
             throw new MappingException("Merged bam file and fastq do not contain same amount of reads. Fastq: " + inputReadsNr.toString() + " Bam: " + readInBamFile.toString());
 
         }
     }
 
     protected abstract Long getReadsInChunks();
 
     public File getReadGroupOutputDir() {
         return readGroupOutputDir;
     }
 
     public ReadGroupLogFile getLog() {
         return log;
     }
 
     public GlobalConfiguration getGlobalConfiguration() {
         return gc;
     }
 
     public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
         this.gc = globalConfiguration;
     }
 
     public File getReadGroupBam() {
         return readGroupBam;
     }
 
     //getters and setters
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getLibrary() {
         return library;
     }
 
     public void setLibrary(String library) {
         this.library = library;
     }
 
     public String getSample() {
         return sample;
     }
 
     public void setSample(String sample) {
         this.sample = sample;
     }
 }
