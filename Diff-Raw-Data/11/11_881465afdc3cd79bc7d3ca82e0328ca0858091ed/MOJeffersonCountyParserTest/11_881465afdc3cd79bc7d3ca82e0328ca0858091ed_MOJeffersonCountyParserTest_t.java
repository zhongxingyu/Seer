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
         "Location: @PACIFIC STATION 2 Call Type: MOVEUP MUP Comments: 7376 HIGHWAY O RP's Address: Pri: 4 Area: 77 Disp: 7724",
         "PLACE:PACIFIC STATION 2",
         "CALL:MOVEUP MUP",
         "ADDR:7376 HIGHWAY O",
         "PRI:4",
         "MAP:77",
         "UNIT:7724");
 
     doTest("T2",
         "Location: 16 SYCAMORE GREEN ACRES CNTY Call Type: STILL CHKBLDG Comments: CARD 77272 MAP 77 RP's Address: 16 SYCAMORE GREEN ACRES Pri: 3 Area: 77 Disp: 7710",
         "ADDR:16 SYCAMORE GREEN ACRES",
         "CALL:STILL CHKBLDG",
         "INFO:CARD 77272 MAP 77 RP's ADDR:16 SYCAMORE GREEN ACRES",
         "PRI:3",
         "MAP:77",
         "UNIT:7710");
 
     doTest("T3",
         "Location: OLD MORSE MILL RD/STATE RD EE CNTY Call Type: EMS TRAUMATIC_INJURI Comments: CARD 77323 MAP 61 RP's Address: Pri: 1 Area: 77 Disp: 5437,7714",
         "ADDR:OLD MORSE MILL RD & STATE RD EE",
         "CALL:EMS TRAUMATIC_INJURI",
         "INFO:CARD 77323 MAP 61",
         "PRI:1",
         "MAP:77",
         "UNIT:5437,7714");
 
     doTest("T4",
         "Location: 7890 DITTMER RIDGE RD CNTY:ST MARTINS CHURCH Call Type: ALARMSND FIREALMC Comments: CARD 77159 MAP 64 RP's Address: Pri: 2 Area: 77 Disp: 7724,7710",
         "ADDR:7890 DITTMER RIDGE RD",
         "PLACE:ST MARTINS CHURCH",
         "CALL:ALARMSND FIREALMC",
         "INFO:CARD 77159 MAP 64",
         "PRI:2",
         "MAP:77",
         "UNIT:7724,7710");
 
     doTest("T5",
         "Location: GRAHAM RD/CEDAR HILL RD CNTY: IN THE AREA Call Type: STILL ILLBURN Comments: 77047 25 and 25a RP's Address: Pri: 4 Area: 77 Disp: 7710",
         "ADDR:GRAHAM RD & CEDAR HILL RD",
         "PLACE:IN THE AREA",
         "CALL:STILL ILLBURN",
         "INFO:77047 25 and 25a",
         "PRI:4",
         "MAP:77",
         "UNIT:7710");
 
     doTest("T6",
         "Location: 4132 SLEEPY HOLLOW LN CNTY Call Type: 1STALRM RES-1ST Comments: CARD 77413 MAP 85 RP's Address: 4132 SLEEPY HOLLOW LN Pri: 2 Area: 77 Disp: 0002/0002 2433,2434,5437,6413,7713,7723,7734,7702,6434,7710",
         "ADDR:4132 SLEEPY HOLLOW LN",
         "CALL:1STALRM RES-1ST",
         "INFO:CARD 77413 MAP 85 RP's ADDR:4132 SLEEPY HOLLOW LN",
         "PRI:2",
         "MAP:77",
         "UNIT:0002/0002 2433,2434,5437,6413,7713,7723,7734,7702,6434,7710");

    doTest("T7",
        "Location: BANDSTAND LN/INDUSTRIAL DR CNTY Call Type: STILL CHKAREA RP's Address: Pri: 3 Area: 77 Disp: 7710",
        "ADDR:BANDSTAND LN & INDUSTRIAL DR",
        "CALL:STILL CHKAREA",
        "PRI:3",
        "MAP:77",
        "UNIT:7710");
   }
   
   public static void main(String[] args) {
    new MOJeffersonCountyParserTest().generateTests("T7");
   }
 }
