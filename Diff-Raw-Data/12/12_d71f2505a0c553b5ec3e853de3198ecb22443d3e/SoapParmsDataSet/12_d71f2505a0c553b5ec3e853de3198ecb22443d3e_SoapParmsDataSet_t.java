 package org.geworkbench.bison.datastructure.complex.pattern;
 
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.util.FilePathnameUtils;
 import org.geworkbench.util.patterns.PatternDB;
 //import polgara.soapPD_wsdl.Parameters;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 
 public class SoapParmsDataSet extends CSAncillaryDataSet {
 
     private PatternDB patternDB;
     private Parameters parms = null;
     boolean dirty = true;
     File dataSetFile = null;
     String label = "Undefined";
     File resultFile;
     /**
      * Link Parameters to real result.
      * @return
      */
     public PatternDB getPatternDB() {
         return patternDB;
     }
 
     public void setPatternDB(PatternDB patternDB) {
         this.patternDB = patternDB;
     }
 
     /**
      * Initate the dataset and assign a result file for that dataset.
      * @param p
      * @param name
      * @param parent
      */
     public SoapParmsDataSet(Parameters p, String name, DSDataSet parent) {
         super(parent, name);
         parms = p;
         String idString =  RandomNumberGenerator.getID();
         setID(idString);
         setLabel(name);
          try {
                    String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();
                     String outputFile = tempFolder;
                      if(parent.getFile().exists()){
                         outputFile+= parent.getFile().getName() + idString +  ".pat";
                      }
              if(!new File(outputFile).exists()){
                  new File(outputFile).createNewFile();
              }
                resultFile = new File(outputFile);
 
         }catch (IOException ie){
                System.out.print("Cannot create the new fil: " + ie.getMessage());
          }
 
     }
 
     public File getDataSetFile() {
         return dataSetFile;
     }
 
     public void setDataSetFile(File _file) {
         dataSetFile = _file;
     }
 
     public String getName() {
         return "Parms S:" + parms.getMinSupport() + ", T:" + parms.getMinTokens() + ", W[" + parms.getMinWTokens() + "," + parms.getWindow() + "]";
     }
 
     public Parameters getParameters() {
         return parms;
     }
 
     public String getDataSetName() {
         return getName();
     }
 
     public boolean equals(Object ads) {
 //        if (ads instanceof SoapParmsDataSet) {
 //            SoapParmsDataSet pds = (SoapParmsDataSet) ads;
 //            if (parms.getComputePValue() != pds.parms.getComputePValue()) return false;
 //            if (parms.getCountSeq() != pds.parms.getCountSeq()) return false;
 //            if (parms.getExact() != pds.parms.getExact()) return false;
 //            if (parms.getExactTokens() != pds.parms.getExactTokens()) return false;
 //            if (parms.getGroupingN() != pds.parms.getGroupingN()) return false;
 //            if (parms.getGroupingType() != pds.parms.getGroupingType()) return false;
 //            if (parms.getMaxPatternNo() != pds.parms.getMaxPatternNo()) return false;
 //            if (parms.getMinPatternNo() != pds.parms.getMinPatternNo()) return false;
 //            if (parms.getMinPer100Support() != pds.parms.getMinPer100Support()) return false;
 //            if (parms.getMinPValue() != pds.parms.getMinPValue()) return false;
 //            if (parms.getMinSupport() != pds.parms.getMinSupport()) return false;
 //            if (parms.getMinTokens() != pds.parms.getMinTokens()) return false;
 //            if (parms.getMinWTokens() != pds.parms.getMinWTokens()) return false;
 //            if (parms.getSimilarityMatrix() != pds.parms.getSimilarityMatrix()) return false;
 //            if (parms.getSimilarityThreshold() != pds.parms.getSimilarityThreshold()) return false;
 //            if (parms.getWindow() != pds.parms.getWindow()) return false;
 //            return true;
 //        }
         return false;
     }
 
     /**
      * writeToFile
      *
      * @param fileName String
      */
     public void writeToFile(String fileName) {
     }
 
     public File getResultFile() {
         return resultFile;
     }
 
     public void setResultFile(File resultFile) {
         this.resultFile = resultFile;
     }
 }
