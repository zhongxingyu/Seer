 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PAMifflinCountyParser;
 
 import org.junit.Test;
 
 
 public class PAMifflinCountyParserTest extends BaseParserTest {
   
   public PAMifflinCountyParserTest() {
     setParser(new PAMifflinCountyParser(), "MIFFLIN COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "(CAD Page for CFS 111710-43) ALS - Uncon/Syncope\n1 DERRY PARK DR\nApt:\nBURNHAM\nCross Streets : 400-412  S LOGAN BLVD",
         "ID:111710-43",
         "CALL:ALS - Uncon/Syncope",
         "ADDR:1 DERRY PARK DR",
         "CITY:BURNHAM",
         "X:400-412  S LOGAN BLVD");
 
     doTest("T2",
         "(CAD Page for CFS 111710-42) URG - Ment/Emot/Psych\n41 INDUSTRIAL PARK RD\nApt: A\nGRANVILLE\nCross Streets : 708-924  LOOP RD * 2120-2456 MIDDLE RD",
         "ID:111710-42",
         "CALL:URG - Ment/Emot/Psych",
         "ADDR:41 INDUSTRIAL PARK RD",
         "APT:A",
         "CITY:GRANVILLE TWP",
         "X:708-924  LOOP RD * 2120-2456 MIDDLE RD");
     
     doTest("T3",
         "(CAD Page for CFS 111710-37) ALS - Breathing Difficulty\n27 ANDERSON ST\nApt:\nBRATTON\nCross Streets : 1-19  MATTAWANA BLVD",
         "ID:111710-37",
         "CALL:ALS - Breathing Difficulty",
         "ADDR:27 ANDERSON ST",
         "CITY:BRATTON TWP",
         "X:1-19  MATTAWANA BLVD");
     
     doTest("T4",
         "(CAD Page for CFS 111710-2) ALS - Motor Vehicle Accident\nROSEWOOD AVE and US HIGHWAY 522 NORTH\nApt: //\nDERRY\nCross Streets : 1-24  ROSEWOOD AVE",
         "ID:111710-2",
         "CALL:ALS - Motor Vehicle Accident",
         "ADDR:ROSEWOOD AVE and US HIGHWAY 522 NORTH",
         "MADDR:ROSEWOOD AVE & US HIGHWAY 522 NORTH",
         "CITY:DERRY TWP",
         "X:1-24  ROSEWOOD AVE");
     
     doTest("T5",
         "(CAD Page for CFS 111610-52) ALS - Breathing Difficulty\n163 SUMMIT DR\nApt:\nDERRY\nCross Streets : 400-406  SIXTH ST * 1-14 CASTLE CT",
         "ID:111610-52",
         "CALL:ALS - Breathing Difficulty",
         "ADDR:163 SUMMIT DR",
         "CITY:DERRY TWP",
         "X:400-406  SIXTH ST * 1-14 CASTLE CT");
     
     doTest("T6",
         "(CAD Page for CFS 111510-56) ALS - Diabetic\n7 MAPLE ST\nApt: ////\nKISTLER\nCross Streets : 150-198  N RIVERSIDE DR * 142-148 PARK RD",
         "ID:111510-56",
         "CALL:ALS - Diabetic",
         "ADDR:7 MAPLE ST",
         "CITY:KISTLER",
         "X:150-198  N RIVERSIDE DR * 142-148 PARK RD");
     
     doTest("T7",
         "FRM:cmessages@co.mifflin.pa.us<Body%3AFRM%3Acmessages@co.mifflin.pa.us>\nSUBJ:CAD Page for CFS 013011-1\nMSG:Residential Fire\n56 RED SHALE LN\nApt:\nBRATTON\nCross Streets : 26-92  CARLISLE GAP RD",
         "ID:013011-1",
         "CALL:Residential Fire",
         "ADDR:56 RED SHALE LN",
         "CITY:BRATTON TWP",
         "X:26-92  CARLISLE GAP RD");
   }
   
   @Test
   public void testJuanityCounty() {
 
     doTest("T1",
         "1 of 2\nFRM:cmessages@co.mifflin.pa.us\nSUBJ:CAD Page for CFS 011512-8\nMSG:ALS - Sick Person\n11 SUNSET RD\nApt: \nLEWISTOWN\nhttp://maps.google.c\n(Con't) 2 of 2\nom/maps?q=+40.58788%20-77.56015\nCross Streets : 1-8  E WILSON AVE * 1-8 HARDING AVE \n(End)",
         "ID:011512-8",
         "CALL:ALS - Sick Person",
         "ADDR:11 SUNSET RD",
         "CITY:LEWISTOWN",
         "X:1-8  E WILSON AVE * 1-8 HARDING AVE");
   }
   
   public static void main(String args[]) {
     new PAMifflinCountyParserTest().generateTests("T1", "ID CALL ADDR APT CITY X");
   }
 }
