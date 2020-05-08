 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.co.brenn.xlwrap;
 
 import java.io.File;
 import java.io.IOException;
import jxl.Cell;
import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 
 /**
  *
  * @author Christian
  */
 public class ExcelCopy {
     
     public static void main(String[] args) throws IOException, BiffException, WriteException {
        Workbook ontology = Workbook.getWorkbook(new File("D:/Programs/Xlwrap-Brenn/cyab/Ontology.xls"));
        //Workbook data = Workbook.getWorkbook(new File("D:/Programs/Xlwrap-Brenn/cyab/Test.xls"));
        WritableWorkbook copy = Workbook.createWorkbook(new File("D:/Programs/Xlwrap-Brenn/cyab/Copy.xls"), ontology);
        
        WritableSheet metaData = copy.getSheet(0);
        WritableCell cell = metaData.getWritableCell(1, 1);
        System.out.println(cell);
        System.out.println(cell.getContents());
        System.out.println(cell.getClass());
        if (cell instanceof Label){
            System.out.println("Label:");
        }
        WritableCell newCell = cell.copyTo(2, 2);
        metaData.addCell(newCell);
         /*
         */
         copy.write();
         copy.close();
     }
 }
