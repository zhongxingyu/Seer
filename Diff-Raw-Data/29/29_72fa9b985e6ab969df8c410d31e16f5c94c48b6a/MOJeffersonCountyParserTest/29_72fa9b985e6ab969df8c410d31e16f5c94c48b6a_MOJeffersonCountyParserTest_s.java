 package net.anei.cadpage.parsers.MO;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MOJeffersonCountyParserTest extends BaseParserTest {
   
   public MOJeffersonCountyParserTest() {
     setParser(new MOJeffersonCountyParser(), "JEFFERSON COUNTY", "MO");
   }
 
  
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Location: 201 E MILLER ST DES: @DESOTO RURAL FIRE STATION 1 TYPE CODE: MOVEUP MUP CALLER ADDR: Disp: 6514",
         "ADDR:201 E MILLER ST",
         "CITY:DE SOTO",
         "PLACE:DESOTO RURAL FIRE STATION 1",
         "CALL:MOVEUP MUP",
         "UNIT:6514");
 
     doTest("T2",
         "Location: 110 SEVEN SPRINGS CT TYPE CODE: 1STALRM RES-1ST CALLER ADDR: Disp: 7702,7724",
         "ADDR:110 SEVEN SPRINGS CT",
         "CALL:1STALRM RES-1ST",
         "UNIT:7702,7724");
 
     doTest("T3",
         "Location: @ST CLAIR STATION 2 TYPE CODE: MOVEUP MUP CALLER ADDR: Comments: 470 E NORTH ST Disp: 7724",
         "ADDR:@ST CLAIR STATION 2",
         "CALL:MOVEUP MUP",
         "INFO:470 E NORTH ST",
         "UNIT:7724");
 
     doTest("T4",
         "Location: 12545 WARE RD CNTY: @DESOTO RURAL FIRE STATION 3 TYPE CODE: MOVEUP MUP CALLER ADDR: Disp: 7710",
         "ADDR:12545 WARE RD",
         "PLACE:DESOTO RURAL FIRE STATION 3",
         "CALL:MOVEUP MUP",
         "UNIT:7710");
 
     doTest("T5",
         "Location: 4432 MOCKINGBIRD LN CNTY TYPE CODE: EMS SEIZURES CALLER ADDR: Comments: CARD 65019 Disp: 6513,7537",
         "ADDR:4432 MOCKINGBIRD LN",
         "CALL:EMS SEIZURES",
         "INFO:CARD 65019",
         "UNIT:6513,7537");
 
     doTest("T6",
         "Location: 5165 ATHLONE CT CNTY TYPE CODE: STILL BRUSH CALLER ADDR: Comments: CARD 77011 MAP 89 Disp: 2438,6102,6128,6418,6518,7618,7701,7710,7714,7718,7728,7734",
         "ADDR:5165 ATHLONE CT",
         "CALL:STILL BRUSH",
         "INFO:CARD 77011 MAP 89",
         "UNIT:2438,6102,6128,6418,6518,7618,7701,7710,7714,7718,7728,7734");
   }
   
   public static void main(String[] args) {
     new MOJeffersonCountyParserTest().generateTests("T1");
   }
 }
