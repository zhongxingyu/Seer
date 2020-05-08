 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYNassauCountyAParser;
 
 import org.junit.Test;
 
 
 public class NYNassauCountyAParserTest extends BaseParserTest {
   
   public NYNassauCountyAParserTest() {
     setParser(new NYNassauCountyAParser(), "NASSAU COUNTY", "NY");
   }
   
   @Test
   public void testParser() {

    doTest("T4",
        "***MVA***  SERVICE ROAD N [OLD WESTBURY] c/s: RED GROUND ROAD/ ADTNL:  GRID: N-15 TOA: 15:45 11/30/2010",
        "CALL:MVA - SERVICE",
        "PLACE:ROAD N",
        "CITY:OLD WESTBURY",
        "X:RED GROUND ROAD/",
        "MAP:N-15");

    
     
     doTest("T1",
         "***SIG 9***  AMBU  27 PINE DR N [ROSLYN] c/s: PINE (S) DR/VERITY LA ADTNL: DIFF BREATHING GRID: K-14 TOA: 21:16 11/30/2010",
         "CALL:SIG 9 - AMBU",
         "ADDR:27 PINE DR N",
         "CITY:ROSLYN",
         "X:PINE (S) DR/VERITY LA",
         "INFO:DIFF BREATHING",
         "MAP:K-14");
 
     doTest("T2",
         "***CHIEF***  AUTO BAR FRITES 400 WHEATLEY PLAZ [GREENVALE] c/s: GLEN COVE ROAD/NORTHERN BLVD ADTNL: ZONE 20 GRID: M-13 TOA: 18:28 11/30/2010",
         "CALL:CHIEF - AUTO",
         "PLACE:BAR FRITES",
         "ADDR:400 WHEATLEY PLAZ",
         "CITY:GREENVALE",
         "X:GLEN COVE ROAD/NORTHERN BLVD",
         "INFO:ZONE 20",
         "MAP:M-13");
 
     doTest("T3",
         "***SIG 9***  AMBU BURGER KING 110 NORTHERN BLVD [GREENVALE] c/s: TOWN PATH/GLEN COVE ROAD ADTNL: FEMALE FEELING FAINT GRID: M-13 TOA: 16:19 11/30/2010",
         "CALL:SIG 9 - AMBU",
         "PLACE:BURGER KING",
         "ADDR:110 NORTHERN BLVD",
         "CITY:GREENVALE",
         "X:TOWN PATH/GLEN COVE ROAD",
         "INFO:FEMALE FEELING FAINT",
         "MAP:M-13");
 
     doTest("T4",
         "***MVA***  SERVICE ROAD N [OLD WESTBURY] c/s: RED GROUND ROAD/ ADTNL:  GRID: N-15 TOA: 15:45 11/30/2010",
         "CALL:MVA - SERVICE",
         "PLACE:ROAD N",
         "CITY:OLD WESTBURY",
         "X:RED GROUND ROAD/",
         "MAP:N-15");
 
     doTest("T5",
         "***SIG 8***  AUTO HARBOR HILLS PHARMACY 310 GLEN COVE ROAD [EAST HILLS] c/s: KENNEDY ROAD/RED GROUND ROAD ADTNL: WATERFLOW/565375563 GRID: N-14 TOA: 13:25",
         "CALL:SIG 8 - AUTO",
         "PLACE:HARBOR HILLS PHARMACY",
         "ADDR:310 GLEN COVE ROAD",
         "CITY:EAST HILLS",
         "X:KENNEDY ROAD/RED GROUND ROAD",
         "INFO:WATERFLOW/565375563",
         "MAP:N-14");
 
     doTest("T6",
         "***SIG 8***  PROP  15 POPLAR DR [EAST HILLS] c/s: CHESTNUT DR/APPLETREE LA ADTNL: BBQ NEXT TO DECK GRID: L-14 TOA: 18:10 11/29/2010",
         "CALL:SIG 8 - PROP",
         "ADDR:15 POPLAR DR",
         "CITY:EAST HILLS",
         "X:CHESTNUT DR/APPLETREE LA",
         "INFO:BBQ NEXT TO DECK",
         "MAP:L-14");
 
     doTest("T7",
         "***SIG 8***  AUTO  27 WAGON ROAD [EAST HILLS] c/s: SALEM ROAD/WINTHROP ROAD ADTNL: 56A2582/ GOLDFETTER RES GRID: M-14 TOA: 21:24 12/04/2010",
         "CALL:SIG 8 - AUTO",
         "ADDR:27 WAGON ROAD",
         "CITY:EAST HILLS",
         "X:SALEM ROAD/WINTHROP ROAD",
         "INFO:56A2582/ GOLDFETTER RES",
         "MAP:M-14");
 
     doTest("T8",
         "***SIG 9***  AMBU SUN HARBOR MANOR HEALTH FACILITY 255 WARNER AVE [ROSLYN] c/s: .CARLYLE PL/.ST MARKS PL ADTNL: 3RD FL-3 NORTH-CHEST PAINS GRID: L-15 TOA:",
           "CALL:SIG 9 - AMBU",
           "PLACE:SUN HARBOR MANOR HEALTH FACILITY",
           "ADDR:255 WARNER AVE",
           "CITY:ROSLYN",
           "X:.CARLYLE PL/.ST MARKS PL",
           "INFO:3RD FL-3 NORTH-CHEST PAINS",
           "MAP:L-15");
 
     doTest("T9",
         "***CHIEF***  AUTO  1506 OLD NORTHERN BLVD [ROSLYN] c/s: BRYANT AVE/NORTHERN BLVD ADTNL: FRATELLI MARKET PL-5612934-SMK DET GRID: L-13 TOA: 16:59 12/04/201",
         "CALL:CHIEF - AUTO",
         "ADDR:1506 OLD NORTHERN BLVD",
         "CITY:ROSLYN",
         "X:BRYANT AVE/NORTHERN BLVD",
         "INFO:FRATELLI MARKET PL-5612934-SMK DET",
         "MAP:L-13");
 
     doTest("T10",
         "***GENERAL***  BUIL GREAT SHAPES 190 MINEOLA AVE [ROSLYN HEIGHTS] c/s: HIGH ST/POWERHOUSE ROAD ADTNL: ODOR OF BURNING IN BLD-GREAT SHAPES GRID: K-15 TOA:",
           "CALL:GENERAL - BUIL",
           "PLACE:GREAT SHAPES",
           "ADDR:190 MINEOLA AVE",
           "CITY:ROSLYN HEIGHTS",
           "X:HIGH ST/POWERHOUSE ROAD",
           "INFO:ODOR OF BURNING IN BLD-GREAT SHAPES",
           "MAP:K-15");
 
     doTest("T11",
         "***SIG 8***  AUTO PIERCE DAY CAMP 37 MINEOLA AVE [ROSLYN] c/s: REGENT PL/GLEN AVE ADTNL: MANSION/NO PERMIT GRID: K-14 TOA: 08:32 12/07/2010",
         "CALL:SIG 8 - AUTO",
         "PLACE:PIERCE DAY CAMP",
         "ADDR:37 MINEOLA AVE",
         "CITY:ROSLYN",
         "X:REGENT PL/GLEN AVE",
         "INFO:MANSION/NO PERMIT",
         "MAP:K-14");
 
     doTest("T12",
         "***CHIEF***  INVES  52 THE HEMLOCKS [ROSLYN ESTATES] c/s: SOUTH BRANCH/THE PINES ADTNL: SOMETHING BEEPING IN HOUSE GRID: J-15 TOA: 08:07 12/07/2010",
         "CALL:CHIEF - INVES",
         "ADDR:52 THE HEMLOCKS",
         "CITY:ROSLYN ESTATES",
         "X:SOUTH BRANCH/THE PINES",
         "INFO:SOMETHING BEEPING IN HOUSE",
         "MAP:J-15");
     
     doTest("T6",
         "***MUTUAID***  MAF SUNY 223 STORE HILL ROAD N [OLD WESTBURY] c/s: B GATE ROAD/ ADTNL: FAS TRUCK--WANG HALL TOA: 06:17 12/18/2010",
         "CALL:MUTUAID - MAF",
         "PLACE:SUNY",
         "ADDR:223 STORE HILL ROAD N",
         "CITY:OLD WESTBURY",
         "X:B GATE ROAD/",
         "INFO:FAS TRUCK--WANG HALL");
    
   }
 }
