 package net.anei.cadpage.parsers;
 
 import org.junit.Test;
 
 
 public class INMadisonCountyParserTest extends BaseParserTest {
   
   public INMadisonCountyParserTest() {
     setParser(new INMadisonCountyParser(), "MADISON COUNTY", "IN");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "Unit:ST40 UnitSts: Loc:6302 APOLLO DR Venue:RICHLA TWP Inc:Brush Fire Date:10/17/2010 Time:18:49 ON GEMINI / 1 ST HOUSE SOUTH EAST END Addtl:TRASH FIRE ON THE GROUND/WHI MODULAR WHI VAN SITTING IN DRIVE",
         "UNIT:ST40",
         "ADDR:6302 APOLLO DR",
         "CALL:Brush Fire",
         "INFO:ON GEMINI / 1 ST HOUSE SOUTH EAST END TRASH FIRE ON THE GROUND/WHI MODULAR WHI VAN SITTING IN DRIVE");
     
     doTest("T2",
         "Unit:AM45 UnitSts: Loc:6197 N SR 9 Venue:RICHLA TWP Inc:EMS Call Date:10/17/2010 Time:01:12 56 YOM / ADV HE DOESNT FEEL GOOD / VOMITING A Addtl:ND NO APPITITE ALL DAY / WILL BE WATING OUTSI DE RESD:",
         "UNIT:AM45",
         "ADDR:6197 N SR 9",
         "CALL:EMS Call",
         "INFO:56 YOM / ADV HE DOESNT FEEL GOOD / VOMITING A ND NO APPITITE ALL DAY / WILL BE WATING OUTSI DE RESD:");
 
     doTest("T3",
         "Unit:ST40 UnitSts: Loc:E 400N/100E Venue:RICHLA TWP Inc:Brush Fire Date:10/15/2010 Time:14:33 OPEN BURN BETWEEEN 100E AND ALEX PIKE/ S SIDE Addtl: OF ROAD",
         "UNIT:ST40",
         "ADDR:E 400N & 100E",
         "CALL:Brush Fire",
         "INFO:OPEN BURN BETWEEEN 100E AND ALEX PIKE/ S SIDE OF ROAD");
 
     doTest("T4",
         "Unit:ST40 UnitSts: Loc:3551 N SR 9 Venue:LAFAYE TWP TOM WOOD HONDA Inc:Accidnt PI Date:10/14/2010 Time:07:43 UNKNOWN 2 VEHS Addtl:",
         "UNIT:ST40",
         "ADDR:3551 N SR 9",
         "CALL:Accidnt PI",
         "INFO:UNKNOWN 2 VEHS");
         
     doTest("T5",
         "Unit:AM45 UnitSts: Loc:124 DARIN CT Venue:RICHLA TWP Inc:SEIZURE Date:10/29/2010 Time:19:31 18 MO/POSS SEIZURE Addtl:",
         "UNIT:AM45",
         "ADDR:124 DARIN CT",
         "CALL:SEIZURE",
         "INFO:18 MO/POSS SEIZURE");
     doTest("T6",
         "Unit:EMA1 Loc:3727 N 200E Venue:RICHLA TWP Inc:Accidnt PI Date:12/09/2010 Time:10:31 UNKNOWN  CAR VS POL ",
         "UNIT:EMA1",
        "ADDR:3727 N 200E Venue",
         "CALL:Accidnt PI",
         "INFO:UNKNOWN  CAR VS POL"
         );
   }
 }
