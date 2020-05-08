 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline.readGroup;
 
 import bwa_picard_gatk_pipeline.GlobalConfiguration;
 import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
 import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
 import bwa_picard_gatk_pipeline.sge.Job;
 import bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob.BwaIluminaMappingJob;
 import bwa_picard_gatk_pipeline.sge.ilumina.BWAmem.mappingJob.BwaMemIluminaMappingJob;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 
 /**
  *
  * @author wim
  */
 public class ReadGroupIluminaPE extends ReadGroupIlumina {
 
     private FastQFile firstReadsFastQFile;
     private FastQFile secondReadsFastQFile;
     private File fastqChunkDir;
     private String fastqChunkDirFileNameToMatch;
     private List<FastQChunk> firstReadsChunks;
     private List<FastQChunk> secondReadsChunks;
 
     @Override
     protected void prepareReadsForMapping() throws IOException {
 
         firstReadsChunks = new ArrayList<FastQChunk>();
         secondReadsChunks = new ArrayList<FastQChunk>();
 
 
         if (firstReadsFastQFile != null) {
             firstReadsFastQFile.initializeFastqReader();
             secondReadsFastQFile.initializeFastqReader();
 
             firstReadsChunks = firstReadsFastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir, id);
             secondReadsChunks = secondReadsFastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir, id);
         }
 
         if (fastqChunkDir != null) {
             addExistingChunks();
         }
 
     }
 
     private void addExistingChunks() {
 
         String[] fastqExtensions = new String[]{"fastq", "fq", "gz"};
         List<File> fastqFiles = (List<File>) FileUtils.listFiles(fastqChunkDir, fastqExtensions, true);
         List<File> existingFirstFileChunks = new ArrayList<File>();
         List<File> existingSecondFileChunks = new ArrayList<File>();
         for (File fastqFile : fastqFiles) {
             //skip files not matching the filename part, in the case that multiple readGroups or lanes are in the same fastqChunkDir
             if (!fastqFile.getName().contains(fastqChunkDirFileNameToMatch)) {
                 continue;
             }
 
             if (fastqFile.getName().contains("_R1_")) {
                 existingFirstFileChunks.add(fastqFile);
             }
             if (fastqFile.getName().contains("_R2_")) {
                 existingSecondFileChunks.add(fastqFile);
             }
         }
 
         Collections.sort(existingFirstFileChunks);
         Collections.sort(existingSecondFileChunks);
 
         for (File firstFastQFileChunk : existingFirstFileChunks) {
             firstReadsChunks.add(new FastQChunk(firstFastQFileChunk));
         }
 
         for (File secondFastQFileChunk : existingSecondFileChunks) {
             secondReadsChunks.add(new FastQChunk(secondFastQFileChunk));
         }
 
     }
 
     @Override
     protected List<Job> createMappingJobs() throws IOException {
 
         List<Job> bwaMappingJobs = new ArrayList<Job>();
 
         for (int x = 0; x < firstReadsChunks.size(); x++) {
 
             File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(firstReadsChunks.get(x).getFastqFile().getPath()) + ".bam");
             bamChunks.add(bamFile);
             Job bwaIluminaMappingJob = null;
             if (gc.getUseBWAMEM()) {
                 bwaIluminaMappingJob = new BwaMemIluminaMappingJob(firstReadsChunks.get(x).getFastqFile(), secondReadsChunks.get(x).getFastqFile(), bamFile, this);
             } else {
                 bwaIluminaMappingJob = new BwaIluminaMappingJob(firstReadsChunks.get(x).getFastqFile(), secondReadsChunks.get(x).getFastqFile(), bamFile, this);
             }
 
             bwaMappingJobs.add(bwaIluminaMappingJob);
         }
 
         return bwaMappingJobs;
 
     }
 
     @Override
     protected Long getReadsInChunks() {
 
         Long counter = new Long(-1);
 
         //only try to count reads in chunks when there is no existing chunks dir. Otherwise return -1
        if (fastqChunkDir == null) 
         {
             for (FastQChunk fastQChunk : firstReadsChunks) {
                 counter = counter + fastQChunk.getRecordNr();
             }
 
             for (FastQChunk fastQChunk : secondReadsChunks) {
                 counter = counter + fastQChunk.getRecordNr();
             }
         }
 
         return counter;
     }
 
     public FastQFile getFirstReadsFastQFile() {
         return firstReadsFastQFile;
     }
 
     public void setFirstReadsFastQFile(FastQFile firstReadsFastQFile) {
         this.firstReadsFastQFile = firstReadsFastQFile;
     }
 
     public FastQFile getSecondReadsFastQFile() {
         return secondReadsFastQFile;
     }
 
     public void setSecondReadsFastQFile(FastQFile secondReadsFastQFile) {
         this.secondReadsFastQFile = secondReadsFastQFile;
     }
 
     public File getFastqChunkDir() {
         return fastqChunkDir;
     }
 
     public void setFastqChunkDir(File fastqChunkDir) {
         this.fastqChunkDir = fastqChunkDir;
     }
 
     public String getFastqChunkDirFileNameToMatch() {
         return fastqChunkDirFileNameToMatch;
     }
 
     public void setFastqChunkDirFileNameToMatch(String fastqChunkDirFileNameToMatch) {
         this.fastqChunkDirFileNameToMatch = fastqChunkDirFileNameToMatch;
     }
 }
