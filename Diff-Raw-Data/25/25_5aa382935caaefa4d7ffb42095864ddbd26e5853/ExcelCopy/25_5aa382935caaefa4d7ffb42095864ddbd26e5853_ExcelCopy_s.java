 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.co.brenn.xlwrap;
 
 import java.io.File;
 import java.io.IOException;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 
 /**
  *
  * @author Christian
  */
 public class ExcelCopy {
     
     public static void main(String[] args) throws IOException, BiffException, WriteException {
        Workbook workbook = Workbook.getWorkbook(new File("D:/Programs/Xlwrap-Brenn/cyab/Ontology.xls"));
        WritableWorkbook copy = Workbook.createWorkbook(new File("D:/Programs/Xlwrap-Brenn/cyab/Copy.xls"), workbook);
         copy.write();
         copy.close();
     }
 }
