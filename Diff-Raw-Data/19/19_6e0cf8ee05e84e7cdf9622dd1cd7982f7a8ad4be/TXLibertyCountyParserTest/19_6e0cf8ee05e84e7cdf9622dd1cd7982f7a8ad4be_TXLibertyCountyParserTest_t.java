 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TXLibertyCountyParserTest extends BaseParserTest {
   
   public TXLibertyCountyParserTest() {
     setParser(new TXLibertyCountyParser(), "LIBERTY COUNTY", "TX");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[FIRE CALL]  FM834 NEAR BASEBALL FIELDS POSS OUT OF CONTROL GRASS FIRE",
         "CALL:FIRE CALL",
         "ADDR:FM-834",
         "INFO:NEAR BASEBALL FIELDS POSS OUT OF CONTROL GRASS FIRE");
 
     doTest("T2",
         "[EMS]  11346 SH146N 72YOF DIFF BREATHING HISTORY OF CONGESTIVE HEART FAILURE",
         "CALL:EMS",
         "ADDR:11346 SH-146N",
        "MADDR:11346 ST 146",
         "INFO:72YOF DIFF BREATHING HISTORY OF CONGESTIVE HEART FAILURE");
 
     doTest("T3",
         "[10-50]  146N S OF OVER PASS 10-50 OCCUPIED MIGHT CATCH FIRE",
         "CALL:10-50",
         "PLACE:146N S OF OVERPASS 10-50 OCCUPIED MIGHT CATCH FIRE");
 
     doTest("T4",
         "[MVA]  2 vehicle major FM 834 @ CR 2003 by junior high",
         "CALL:MVA",
         "ADDR:FM 834 & CR 2003",
         "MADDR:FM 834 & COUNTY ROAD 2003",
         "INFO:2 vehicle major / by junior high");
 
     doTest("T5",
         "[FIRE CALL] EXXON STATION IN HULL BEHIND ELEMENTARY SCHOOL  ON FIRE POSS EXPLOSIONS",
         "CALL:FIRE CALL",
         "PLACE:EXXON STATION IN HULL BEHIND ELEMENTARY SCHOOL  ON FIRE POSS EXPLOSIONS");
 
     doTest("T6",
         "[MEDICAL CALL]437 CR2002W 72 YOM CHEST PAINS",
         "CALL:MEDICAL CALL",
         "ADDR:437 CR-2002W",
         "MADDR:437 COUNTY ROAD 2002W",
         "INFO:72 YOM CHEST PAINS");
 
     doTest("T7",
         "[EMS]201 CR2517 [KNIGHTS FORREST]\n73 YOM HIGH BP DIFF WALKING",
         "CALL:EMS",
         "ADDR:201 CR-2517",
         "MADDR:201 COUNTY ROAD 2517",
         "PLACE:KNIGHTS FORREST",
         "INFO:73 YOM HIGH BP DIFF WALKING");
 
     doTest("T8",
         "[EMS]668 CR2060\n68YOF DIFF BREATHING",
         "CALL:EMS",
         "ADDR:668 CR-2060",
         "MADDR:668 COUNTY ROAD 2060",
         "INFO:68YOF DIFF BREATHING");
 
     doTest("T9",
         "[CRIME SCENE CLASS]ALL FIRE CHIEFS PLEASE NOTIFY SHERRY WITH THE # OF MEMBERS FROM YOUR DEPT\n" +
         "THAT WILL BE ATTENDING THE CLASS THIS SATURDAY IN HARDIN",
 
         "CALL:CRIME SCENE CLASS",
         "PLACE:ALL FIRE CHIEFS PLEASE NOTIFY SHERRY WITH THE # OF MEMBERS FROM YOUR DEPT THAT WILL BE ATTENDING THE CLASS THIS SATURDAY IN HARDIN");
 
     doTest("T10",
         "[MED CALL]201 CR 2517 (KNIGHTS FOREST) POSS STROKE CANT WALK SLURRED SPEECH ",
         "CALL:MED CALL",
         "ADDR:201 CR 2517",
         "MADDR:201 COUNTY ROAD 2517",
         "PLACE:KNIGHTS FOREST",
         "INFO:POSS STROKE CANT WALK SLURRED SPEECH");
 
     doTest("T11",
         "[FIRE CALL]FM1011 TO OIL TANKS LIGHTENING STRUCK THEM THEY ARE ON FIRE",
         "CALL:FIRE CALL",
         "ADDR:FM-1011",
         "INFO:TO OIL TANKS LIGHTENING STRUCK THEM THEY ARE ON FIRE");
 
     doTest("T12",
         "[MED CALL]CR2552 54 YOM FELL AND HURT BACK HWY 146 T/L CR2550 T/R CR2551 ON CORNER OF\nCR2551 & CR2552",
         "CALL:MED CALL",
         "ADDR:CR-2552",
         "MADDR:COUNTY ROAD 2552",
         "INFO:54 YOM FELL AND HURT BACK HWY 146 T / L CR2550 T / R CR2551 ON CORNER OF CR2551 & CR2552");
 
     doTest("T13",
         "[Crime scene class]Don't forget about the class tomorrow at Hardin High school cafeteria at  \n0800.\n\nCraig",
         "CALL:Crime scene class",
         "PLACE:Don't forget about the class tomorrow at Hardin High school cafeteria at   0800.");
 
     doTest("T14",
         "[FIRE CALL]447 CR2070\nGRASS FIRE",
         "CALL:FIRE CALL",
         "ADDR:447 CR-2070",
         "MADDR:447 COUNTY ROAD 2070",
         "INFO:GRASS FIRE");
 
     doTest("T15",
         "[AGG ASSAULT]261 CR 255O\n40YOM STABBED NEAR WRIST SEVERE BLEEDING",
         "CALL:AGG ASSAULT",
         "ADDR:261 CR 2550",
         "MADDR:261 COUNTY ROAD 2550",
         "INFO:40YOM STABBED NEAR WRIST SEVERE BLEEDING");
 
     doTest("T16",
         "[AGG ASSAULT]STAGE",
         "CALL:AGG ASSAULT",
         "PLACE:STAGE");
 
     doTest("T17",
         "[EMS]16897 SH146N\n78YOM AMPUTATED LEG HAS BEEN BOTHERING HIM FOR A WEEK",
         "CALL:EMS",
         "ADDR:16897 SH-146N",
        "MADDR:16897 ST 146",
         "INFO:78YOM AMPUTATED LEG HAS BEEN BOTHERING HIM FOR A WEEK");
     
   }
   
   public static void main(String[] args) {
     new TXLibertyCountyParserTest().generateTests("T1", "CALL ADDR PLACE INFO");
   }
 }
