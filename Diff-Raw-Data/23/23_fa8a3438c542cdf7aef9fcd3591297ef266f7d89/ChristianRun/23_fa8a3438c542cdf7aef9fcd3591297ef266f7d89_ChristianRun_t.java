 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.co.brenn.xlwrap;
 
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import java.io.IOException;
 import uk.co.brenn.xlwrap.expr.func.BrennRegister;
 
 /**
  *
  * @author christian
  */
 public class ChristianRun {
 
     //The file: bit is required by xlwrap whiuch can alos handle http urls.
    static private String  DROPBOX = "file:c:/Dropbox/FishLink XLWrap data/";
 
     private static void loadXLS(String name) throws XLWrapException, XLWrapEOFException, XLWrapMapException, IOException{
         //Adjust this file to the your local path
         WorkbookWrite.setRoots(DROPBOX + "Meta Data/",DROPBOX + "Raw Data/");
         WorkbookWrite mapWrite = new WorkbookWrite(name);
         mapWrite.writeMap();
         mapWrite.runMap();
     }
     public static void main(String[] args) throws XLWrapException, XLWrapEOFException, IOException, XLWrapMapException {
         BrennRegister.register();
         //loadXLS("CumbriaTarnsPart1MetaData.xls");
         //loadXLS("FBA_TarnsMetaData.xls");
         //loadXLS("RecordsMetaData.xls");
         loadXLS("SpeciesMetaData.xls");
         loadXLS("TarnschemFinalMetaData.xls");
 
     }
 
 }
