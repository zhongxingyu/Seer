 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline;
 
 import bwa_picard_gatk_pipeline.readGroup.ReadGroup;
 import bwa_picard_gatk_pipeline.enums.TargetEnum;
 import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
 import bwa_picard_gatk_pipeline.readGroup.ReadGroupIluminaPE;
 import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolidFragment;
 import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolidPE;
 import bwa_picard_gatk_pipeline.sge.gatk.realignJob.GATKRealignIndelsJob;
 import bwa_picard_gatk_pipeline.sge.picard.mergeBAM.PicardMergeBamJob;
 import bwa_picard_gatk_pipeline.sge.QualimapJob;
 import bwa_picard_gatk_pipeline.sge.gatk.baseQualRecalJob.GATKBaseQualRecalJob;
 import bwa_picard_gatk_pipeline.sge.picard.dedupJob.PicardDedupBamJob;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FilenameUtils;
 import org.ggf.drmaa.DrmaaException;
 
 /**
  *
  * @author Wim Spee
  */
 public class Sample {
 
     private String name;
     private File sampleOutputDir;
     private GlobalConfiguration globalConfiguration;
     //the possible input readgroups
     private List<ReadGroupSolidFragment> solidFragmentReadGroups;
     private List<ReadGroupSolidPE> solidPEReadGroups;
     private List<ReadGroupIluminaPE> iluminaPEReadGroups;
    private List<ReadGroupIluminaPE> iluminaFragmentReadGroups;
     private List<ReadGroup> allReadGroups;
     private List<File> readGroupBamFiles;
     //the output  
     //bam
     private File mergedBamFile;
     private File mergedBamFileDedup;
     private File mergedBamDedupRealigned;    
     private File mergedBamDedupRealignedRecalibrated;    
    
 
     public void startProcessing() {
 
         initalizeUnsetList();           //initialize the unset list to empty list  
 
         sampleOutputDir = new File(globalConfiguration.getBaseOutputDir(), name);
         sampleOutputDir.mkdir();
 
         allReadGroups.addAll(solidFragmentReadGroups);
         allReadGroups.addAll(solidPEReadGroups);
         allReadGroups.addAll(iluminaPEReadGroups);
         allReadGroups.addAll(iluminaFragmentReadGroups);
 
         //process the readGroups is any were set
         for (ReadGroup readGroup : allReadGroups) {
 
             readGroup.createOutputDir(sampleOutputDir);
             readGroup.setGlobalConfiguration(globalConfiguration);
             readGroup.setSample(name);
             readGroup.startProcessing();
         }
 
         //do the sample processing
         try {
             //merge readgroup bam files
             if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_BAM.getRank()) {
                 mergeReadGroupBamFiles();
                 runQualimap();
 
             }
             if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.DEDUP_BAM.getRank()) {
                 markDuplicates();
             }
             if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.REALIGN_BAM.getRank()) {
                 realignBam();
             }
              if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.BQSR_BAM.getRank()) {
                 recalibrateBam();
             }
             
            
 
 
         } catch (IOException ex) {
             Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
         } catch (DrmaaException ex) {
             Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JobFaillureException ex) {
             Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
 
         //call snps 
 
         //cal sv's         
     }
 
     private void initalizeUnsetList() {
         if (solidFragmentReadGroups == null) {
             solidFragmentReadGroups = new ArrayList<ReadGroupSolidFragment>();
         }
         if (solidPEReadGroups == null) {
             solidPEReadGroups = new ArrayList<ReadGroupSolidPE>();
         }
         if (iluminaPEReadGroups == null) {
             iluminaPEReadGroups = new ArrayList<ReadGroupIluminaPE>();
         }
         
         if (iluminaFragmentReadGroups == null) {
            iluminaFragmentReadGroups = new ArrayList<ReadGroupIluminaPE>();
         }
         
         if (allReadGroups == null) {
             allReadGroups = new ArrayList<ReadGroup>();
         }
         if (readGroupBamFiles == null) {
             readGroupBamFiles = new ArrayList<File>();
         }      
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
         this.globalConfiguration = globalConfiguration;
     }
 
     private void mergeReadGroupBamFiles() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
 
 
         for (ReadGroup readGroup : allReadGroups) {
             readGroupBamFiles.add(readGroup.getReadGroupBam());
         }
 
         if (readGroupBamFiles.size() == 1) {
             mergedBamFile = readGroupBamFiles.get(0);
         }
 
         if (readGroupBamFiles.size() > 1) {
             try {
                 mergedBamFile = new File(sampleOutputDir, name + ".bam");
 
 //                PicardBamMerger picardBamMerger = new PicardBamMerger();
 //                picardBamMerger.mergeBamFilesUsingPicard(readGroupBamFiles, mergedBamFile, globalConfiguration.getTmpDir());
 
                 File picardMergeSam = new File(globalConfiguration.getPicardDirectory(), "MergeSamFiles.jar");
 
                 PicardMergeBamJob picardMergeBamJob = new PicardMergeBamJob(readGroupBamFiles, mergedBamFile, null, globalConfiguration.getTmpDir(), picardMergeSam);
                 if(globalConfiguration.getOffline())
                 {
                     picardMergeBamJob.executeOffline();
                     picardMergeBamJob.waitForOfflineExecution();
                 }
                 else
                 {
                     picardMergeBamJob.submit();
                     picardMergeBamJob.waitFor();
                 }               
               
                 
               
             } catch (IOException ex) {
                 Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
     }
 
     private void runQualimap() throws IOException, InterruptedException, DrmaaException {
 
 
         if (mergedBamFile == null) {
             return;
         }
 
         File qualimapReport = new File(sampleOutputDir, name + "_qualimap.pdf");
         QualimapJob qualimapJob = new QualimapJob(mergedBamFile, qualimapReport, globalConfiguration, null);
 
         if(globalConfiguration.getOffline())
         {
             qualimapJob.executeOffline();
         }
         else
         {
             qualimapJob.submit();
         }              
     }
 
     private void markDuplicates() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
         //if a merged bam file was created by processing the readgroups or was set by json 
         //deduplicate the bam
         if (mergedBamFile == null) {
             return;
         }
 
         mergedBamFileDedup = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamFile.getName()) + "_dedup.bam");
 
         PicardDedupBamJob picardDedupBamJob = new PicardDedupBamJob(mergedBamFile, mergedBamFileDedup, globalConfiguration);
 
         if (globalConfiguration.getOffline()) {
             picardDedupBamJob.executeOffline();
             picardDedupBamJob.waitForOfflineExecution();
 
         } else {
             picardDedupBamJob.submit();
             picardDedupBamJob.waitFor();
         }
 
 
     }
 
     private void realignBam() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
 
         if (mergedBamFileDedup == null) {
             return;
         }
 
         mergedBamDedupRealigned = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamFileDedup.getName()) + "_realigned.bam");
 
         GATKRealignIndelsJob gATKRealignIndelsJob = new GATKRealignIndelsJob(mergedBamFileDedup, mergedBamDedupRealigned, globalConfiguration);
 
         if (globalConfiguration.getOffline()) {
             gATKRealignIndelsJob.executeOffline();
             gATKRealignIndelsJob.waitForOfflineExecution();
 
         } else {
             gATKRealignIndelsJob.submit();
             gATKRealignIndelsJob.waitFor();
         }
     }
     
     private void recalibrateBam() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
          if (mergedBamDedupRealigned == null) {
             return;
         }
 
         mergedBamDedupRealignedRecalibrated = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamFileDedup.getName()) + "_recalibrated.bam");
 
         GATKBaseQualRecalJob gATKBQSRJob = new GATKBaseQualRecalJob(mergedBamDedupRealigned, mergedBamDedupRealignedRecalibrated, globalConfiguration);
 
         if (globalConfiguration.getOffline()) {
             gATKBQSRJob.executeOffline();
             gATKBQSRJob.waitForOfflineExecution();
 
         } else {
             gATKBQSRJob.submit();
             gATKBQSRJob.waitFor();
         }
     }
 
    
 
     public List<File> getReadGroupBamFiles() {
         return readGroupBamFiles;
     }
 
     public void setReadGroupBamFiles(List<File> readGroupBamFiles) {
         this.readGroupBamFiles = readGroupBamFiles;
     }
 
     public File getMergedBamFile() {
         return mergedBamFile;
     }
 
     public void setMergedBamFile(File mergedBamFile) {
         this.mergedBamFile = mergedBamFile;
     }
 
     public File getMergedBamFileDedup() {
         return mergedBamFileDedup;
     }
 
     public void setMergedBamFileDedup(File mergedBamFileDedup) {
         this.mergedBamFileDedup = mergedBamFileDedup;
     }
 
     public File getMergedBamDedupRealigned() {
         return mergedBamDedupRealigned;
     }
 
     public void setMergedBamDedupRealigned(File mergedBamDedupRealigned) {
         this.mergedBamDedupRealigned = mergedBamDedupRealigned;
     }   
 
     public List<ReadGroupSolidFragment> getSolidFragmentReadGroups() {
         return solidFragmentReadGroups;
     }
 
     public void setSolidFragmentReadGroups(List<ReadGroupSolidFragment> solidFragmentReadGroups) {
         this.solidFragmentReadGroups = solidFragmentReadGroups;
     }
 
     public List<ReadGroupSolidPE> getSolidPEReadGroups() {
         return solidPEReadGroups;
     }
 
     public void setSolidPEReadGroups(List<ReadGroupSolidPE> solidPEReadGroups) {
         this.solidPEReadGroups = solidPEReadGroups;
     }
 
     public List<ReadGroupIluminaPE> getIluminaPEReadGroups() {
         return iluminaPEReadGroups;
     }
 
     public void setIluminaPEReadGroups(List<ReadGroupIluminaPE> iluminaPEReadGroups) {
         this.iluminaPEReadGroups = iluminaPEReadGroups;
     }
 
    public List<ReadGroupIluminaPE> getIluminaFragmentReadGroups() {
         return iluminaFragmentReadGroups;
     }
 
    public void setIluminaFragmentReadGroups(List<ReadGroupIluminaPE> iluminaFragmentReadGroups) {
         this.iluminaFragmentReadGroups = iluminaFragmentReadGroups;
     }
     
     
 
     public List<ReadGroup> getAllReadGroups() {
         return allReadGroups;
     }
 
     public void setAllReadGroups(List<ReadGroup> allReadGroups) {
         this.allReadGroups = allReadGroups;
     }
 
     
 
     
     
     
 
    
 
     
 }
