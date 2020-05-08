 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xsqconvertergit;
 
 import java.io.File;
 import java.util.Map;
 
 /**
  *
  * @author root
  */
 public class ProcessingOptions {
     
     
     //general
     private File XSQFile;
     private File outputDir;
     private long chunkSize;
     
     private FastQDialect fastQDialect;    
     private Boolean useBarcodeInOutputName = false;
     
     private Boolean overwriteExistingOutput = false;
     
     private Integer readLenghtOutputCutoff;
     
     //subset     
     private Boolean barCodeSubset = false;   
     private Map<Integer, Integer> barcodesSubsetList;
     
     private Boolean libraryNameSubset = false;;
     private Map<String, String> libraryNamesSubsetList;   
     
     //mate pair barcode
     private Boolean matePairBarcodeRun = false;
     private Map<String, String> matePairBarcodeMap;
     private Integer MPBCMismatchesAllowed;
     private Integer matePairBarCodeLength;
     private MatePairBarcodeLocationEnum matePairBarcodeLocationEnum;
     
 
     public File getOutputDir() {
         return outputDir;
     }
 
     public void setOutputDir(File outputDir) {
         this.outputDir = outputDir;
     }
 
     public long getChunkSize() {
         return chunkSize;
     }
 
     public void setChunkSize(long chunkSize) {
         this.chunkSize = chunkSize;
     }
 
     public FastQDialect getFastQDialect() {
         return fastQDialect;
     }
 
     public void setFastQDialect(FastQDialect fastQDialect) {
         this.fastQDialect = fastQDialect;
     }    
     
 
     public Boolean getBarCodeSubset() {
         return barCodeSubset;
     }
 
     public void setBarCodeSubset(Boolean barCodeSubset) {
         this.barCodeSubset = barCodeSubset;
     }
 
     public Map<String, String> getMatePairBarcodeMap() {
         return matePairBarcodeMap;
     }
 
     public void setMatePairBarcodeMap(Map<String, String> matePairBarcodeMap) {
         this.matePairBarcodeMap = matePairBarcodeMap;
     }
 
     public Integer getMPBCMismatchesAllowed() {
         return MPBCMismatchesAllowed;
     }
 
     public void setMPBCMismatchesAllowed(Integer MPBCMismatchesAllowed) {
         this.MPBCMismatchesAllowed = MPBCMismatchesAllowed;
     }
 
     public Boolean getMatePairBarcodeRun() {
         return matePairBarcodeRun;
     }
 
     public void setMatePairBarcodeRun(Boolean matePairBarcodeRun) {
         this.matePairBarcodeRun = matePairBarcodeRun;
     }
 
     public File getXSQFile() {
         return XSQFile;
     }
 
     public void setXSQFile(File XSQFile) {
         this.XSQFile = XSQFile;
     }
 
     public Map<Integer, Integer> getBarcodesSubsetList() {
         return barcodesSubsetList;
     }
 
     public void setBarcodesSubsetList(Map<Integer, Integer> barcodesSubsetList) {
         this.barcodesSubsetList = barcodesSubsetList;
     }
 
     public Boolean getLibraryNameSubset() {
         return libraryNameSubset;
     }
 
     public void setLibraryNameSubset(Boolean libraryNameSubset) {
         this.libraryNameSubset = libraryNameSubset;
     }
 
     public Map<String, String> getLibraryNamesSubsetList() {
         return libraryNamesSubsetList;
     }
 
     public void setLibraryNamesSubsetList(Map<String, String> libraryNamesSubsetList) {
         this.libraryNamesSubsetList = libraryNamesSubsetList;
     }
 
     public Boolean getUseBarcodeInOutputName() {
         return useBarcodeInOutputName;
     }
 
     public void setUseBarcodeInOutputName(Boolean useBarcodeInOutputName) {
         this.useBarcodeInOutputName = useBarcodeInOutputName;
     }
 
     public Integer getMatePairBarCodeLength() {
         return matePairBarCodeLength;
     }
 
     public void setMatePairBarCodeLength(Integer matePairBarCodeLength) {
         this.matePairBarCodeLength = matePairBarCodeLength;
     }
 
     public MatePairBarcodeLocationEnum getMatePairBarcodeLocationEnum() {
         return matePairBarcodeLocationEnum;
     }
 
     public void setMatePairBarcodeLocationEnum(MatePairBarcodeLocationEnum matePairBarcodeLocationEnum) {
         this.matePairBarcodeLocationEnum = matePairBarcodeLocationEnum;
     }
     
     
 
     public Boolean getOverwriteExistingOutput() {
         return overwriteExistingOutput;
     }
 
     public void setOverwriteExistingOutput(Boolean overwriteExistingOutput) {
         this.overwriteExistingOutput = overwriteExistingOutput;
     }
 
     public Integer getReadLenghtOutputCutoff() {
         return readLenghtOutputCutoff;
     }
 
     public void setReadLenghtOutputCutoff(Integer readLenghtOutputCutoff) {
         this.readLenghtOutputCutoff = readLenghtOutputCutoff;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
           
             
             
     
     
     
     
     
     
     
     
     
 }
