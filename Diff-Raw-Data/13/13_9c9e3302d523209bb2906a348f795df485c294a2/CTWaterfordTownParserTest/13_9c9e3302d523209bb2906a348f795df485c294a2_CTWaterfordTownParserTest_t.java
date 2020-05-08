 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTWaterfordTownParserTest extends BaseParserTest {
   
   public CTWaterfordTownParserTest() {
     setParser(new CTWaterfordTownParser(), "WATERFORD TWP", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "2012-009336*\n16 SCENIC VIEW DRIVE*\n*\n*\nWATERFORD*\n*\nEMS BLS*\nAMBULANCE CALL - BLS*\n*\n*\nCO4*\n*\n*\n*\n*",
         "ID:2012-009336",
         "ADDR:16 SCENIC VIEW DRIVE",
         "CITY:WATERFORD",
         "CALL:AMBULANCE CALL - BLS",
         "UNIT:CO4");
 
     doTest("T2",
         "2012-009419*\n" +
         "INTERSTATE 395 (X77-MONT)*\n" +
         "*\n" +
         "*\n" +
         "WATERFORD*\n" +
         "*\n" +
         "MVA HIGH*\n" +
         "VEHICLE ACCIDENT - HIGHWAY/SPEED*\n" +
         "*\n" +
         "*\n" +
         "49,CO4,CO5,MED11,W500,W51,W57*\n" +
         "*\n" +
         "*\n" +
         "*\n" +
         "*",
 
         "ID:2012-009419",
         "ADDR:INTERSTATE 395 (X77-MONT)",
         "CITY:WATERFORD",
         "CALL:VEHICLE ACCIDENT - HIGHWAY/SPEED",
         "UNIT:49,CO4,CO5,MED11,W500,W51,W57");
 
     doTest("T3",
         "2012-009444*\n" +
         "14 KINGFISHER WAY*\n" +
         "*\n" +
         "*\n" +
         "WATERFORD*\n" +
         "*\n" +
         "CARB MONOX*\n" +
         "CARBON MONOXIDE INCIDENT*\n" +
         "NOLDA,ZACH*\n" +
         "*\n" +
         "CO1,CO4,W100,W11,W400*\n" +
         "*\n" +
         "*\n" +
         "*\n" +
         "*",
 
         "ID:2012-009444",
         "ADDR:14 KINGFISHER WAY",
         "CITY:WATERFORD",
         "CALL:CARBON MONOXIDE INCIDENT",
         "NAME:NOLDA,ZACH",
         "UNIT:CO1,CO4,W100,W11,W400");
 
     doTest("T4",
         "2012-009566*\n16 CENTER STREET*\nB*\n*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nDE*SANTO,ROBERT S*\n*\nCO4*\n*\n*\n*\n*",
         "ID:2012-009566",
         "ADDR:16 CENTER STREET",
         "APT:B",
         "CITY:WATERFORD",
         "CALL:AMBULANCE CALL - ALS",
         "NAME:DE*SANTO,ROBERT S",
         "UNIT:CO4");
 
     doTest("T5",
         "2012-009600*\n" +
         "45 NORWICH ROAD*\n" +
         "*\n" +
         "*\n" +
         "QUAKER HILL*\n" +
         "*\n" +
         "STRUCTURE*\n" +
         "STRUCTURE/BUILDING FIRE*\n" +
         "CUMBERLAND FARMS*\n" +
         "*\n" +
         "CO2,CO4,CO5,W200,W21,W25,W51,W94*\n" +
         "*\n" +
         "*\n" +
         "*\n" +
         "*",
 
         "ID:2012-009600",
         "ADDR:45 NORWICH ROAD",
         "CITY:QUAKER HILL",
         "CALL:STRUCTURE/BUILDING FIRE",
         "NAME:CUMBERLAND FARMS",
         "UNIT:CO2,CO4,CO5,W200,W21,W25,W51,W94");
 
     doTest("T6",
         "2012-009697*\n277 NIANTIC RIVER ROAD*\n*\n*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nBARRY,THOMAS R*\n*\nCO4*\n*\n*\n*\n*",
         "ID:2012-009697",
         "ADDR:277 NIANTIC RIVER ROAD",
         "CITY:WATERFORD",
         "CALL:AMBULANCE CALL - ALS",
         "NAME:BARRY,THOMAS R",
         "UNIT:CO4");
 
     doTest("T7",
         "2012-009759*\n14 HIGH RIDGE DRIVE*\n*\n*\nWATERFORD*\n*\nEMS BLS*\nAMBULANCE CALL - BLS*\nRISLEY,R A*\n*\nCO1,CO4,W100,W400*\n*\n*\n*\n*",
         "ID:2012-009759",
         "ADDR:14 HIGH RIDGE DRIVE",
         "CITY:WATERFORD",
         "CALL:AMBULANCE CALL - BLS",
         "NAME:RISLEY,R A",
         "UNIT:CO1,CO4,W100,W400");
 
     doTest("T8",
         "2012-009851*\n" +
         "745 VAUXHALL STREET EXTENSION*\n" +
         "*\n" +
         "*\n" +
         "WATERFORD*\n" +
         "*\n" +
         "MVA WITH*\n" +
         "VEHICLE ACCIDENT WITH INJURIES*\n" +
         "Danielle Chaude*\n" +
         "*\n" +
         "CO4,CO5,MED11,W200,W500*\n" +
         "*\n" +
         "*\n" +
         "*\n" +
         "*",
 
         "ID:2012-009851",
         "ADDR:745 VAUXHALL STREET EXTENSION",
         "CITY:WATERFORD",
         "CALL:VEHICLE ACCIDENT WITH INJURIES",
         "NAME:Danielle Chaude",
         "UNIT:CO4,CO5,MED11,W200,W500");
 
     doTest("T9",
         "2012-009905*\n563 BOSTON POST ROAD*\n14*\n2*\nWATERFORD*\n*\nEMS ALS*\nAMBULANCE CALL - ALS*\nBlue Anchor*\n*\nCO4*\n*\n*\n*\n*",
         "ID:2012-009905",
         "ADDR:563 BOSTON POST ROAD",
         "APT:14-2",
         "CITY:WATERFORD",
         "CALL:AMBULANCE CALL - ALS",
         "NAME:Blue Anchor",
         "UNIT:CO4");
 
     doTest("T10",
         "2012-009851*\n" +
         "745 VAUXHALL STREET EXTENSION*\n" +
         "*\n" +
         "*\n" +
         "WATERFORD*\n" +
         "*\n" +
         "MVA WITH*\n" +
         "VEHICLE ACCIDENT WITH INJURIES*\n" +
         "Danielle Chaude*\n" +
         "*\n" +
         "CO4,CO5,MED11,W200,W500*\n" +
         "*\n" +
         "*\n" +
         "*\n" +
         "*",
 
         "ID:2012-009851",
         "ADDR:745 VAUXHALL STREET EXTENSION",
         "CITY:WATERFORD",
         "CALL:VEHICLE ACCIDENT WITH INJURIES",
         "NAME:Danielle Chaude",
         "UNIT:CO4,CO5,MED11,W200,W500");
 
   }
   
  @Test
  public void testJaredDiPerrio() {

    doTest("T1",
        "  / 2012-010577* BOSTON POST ROAD* * * WATERFORD* * MVA WITH* VEHICLE ACCIDENT WITH INJURIES* * * CO1,CO4,W100,W11,W400* * * * *\n",
        "ID:2012-010577",
        "ADDR:BOSTON POST ROAD",
        "CITY:WATERFORD",
        "CALL:VEHICLE ACCIDENT WITH INJURIES",
        "UNIT:CO1,CO4,W100,W11,W400");

  }
  
   public static void main(String[] args) {
     new CTWaterfordTownParserTest().generateTests("T1");
   }
 }
