 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mecard.customer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import site.mecard.MeCardPolicy;
 
 /**
  *
  * @author metro
  */
 public class BImportBatTest
 {
     
     public BImportBatTest()
     {
         
     }
 
     @Test
     public void testSomeMethod()
     {
         File f = new File("testfile.bat");
         if (f.exists())
         {
             f.delete();
         }
         BImportBat b = new BImportBat.Builder("testfile.bat")
                 .setBimportPath("C:\\metro\\logs\\Customers\\bimport.exe").server("'Horizon MSSQL Server'")
                 .password("sql54200").user("sa").database("stalbert")
                 .header("C:\\metro\\logs\\Customers\\header.txt").data("C:\\metro\\logs\\Customers\\data.txt").alias("second_id")
                 .format("m41").bType("metro").mType("dom").location("st").setIndexed(true).build();
         f = new File("testfile.bat");
         assertTrue(f.exists());
         
     }
 
     /**
      * Test of getCommandLine method, of class BImportBat.
      */
     @Test
     public void testGetCommandLine() {
         System.out.println("==getCommandLine==");
         BImportBat b = new BImportBat.Builder("testfile.bat")
                 .setBimportPath("C:\\metro\\logs\\Customers\\bimport.exe").server("'Horizon MSSQL Server'")
                 .password("sql54200").user("sa").database("stalbert")
                 .header("C:\\metro\\logs\\Customers\\header.txt").data("C:\\metro\\logs\\Customers\\data.txt").alias("second_id")
                 .format("m41").bType("metro").mType("dom").location("st").setIndexed(true).build();
         System.out.println("cmd:'"+b.getCommandLine()+"'");
         List<String> resultArray = new ArrayList<String>();
         b.getCommandLine(resultArray);
         System.out.println("RSLT_ARRY:" + resultArray);
         
     }
 
     /**
      * Test of getBatchFileName method, of class BImportBat.
      */
     @Test
     public void testGetBatchFileName() 
     {
         System.out.println("===getBatchFileName===");
         BImportBat b = new BImportBat.Builder().server("server")
                 .password("password").user("user").database("database")
                 .header("headerFileName.txt").data("dataFileName").alias("second_id")
                 .format("m41").bType("awb").location("alap").setIndexed(true).build();
         
         assertTrue(b.getBatchFileName().compareTo("obsolete.bat") == 0);
     }
 }
