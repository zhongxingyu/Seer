 package org.geworkbench.util.sequences;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.FileWriter;
 
import org.geworkbench.util.FilePathnameUtils;

 /**
  * Created by IntelliJ IDEA.
  * User: xiaoqing
  * Date: Apr 20, 2007
  * Time: 12:42:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PDBFileGenerator {
 
     public static final String NEW_LINE = System.getProperty("line.separator");
     public static File generatePDBFileFromSwissModel(File swissmodelFile) {
 
        String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();
         if (swissmodelFile.exists() && swissmodelFile.length() > 0) {
            File pdbFile = new File(tempFolder + swissmodelFile.getName() + ".pdb");
             try {
                 FileReader fr = new FileReader(swissmodelFile);
                 BufferedReader br = new BufferedReader(fr);
                 FileWriter fw = new FileWriter(pdbFile);
                 String line = null;
                 while ((line = br.readLine()) != null) {
                     if (line.trim().startsWith("ATOM")) {
                         fw.write(line + NEW_LINE);
                     }
                 }
                 fw.close();
                 return pdbFile;
             } catch (Exception e) {
 
             }
         }
 
         return null;
     }
 
 }
