 package net.anei.cadpage.parsers.MI;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MIRichmondParserTest extends BaseParserTest {
   
   public MIRichmondParserTest() {
     setParser(new MIRichmondParser(), "", "MI");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "** FIRE ** 1707 WITHEY   VEHICLE FIRE  COLUMBUS",
         "CALL:FIRE - VEHICLE FIRE",
         "ADDR:1707 WITHEY",
         "CITY:COLUMBUS");
 
     doTest("T2",
         "** FIRE RESCUE ** PALMS @ GRATIOT  PIA COLUMBUS",
         "CALL:FIRE RESCUE",
         "ADDR:PALMS & GRATIOT",
         "CITY:COLUMBUS");
 
     doTest("T3",
         "** MEDICAL ** 35388 DIANE LN  HEART ATTACK",
         "CALL:MEDICAL - HEART ATTACK",
         "ADDR:35388 DIANE LN");
 
     doTest("T4",
         "** FIRE RESCUE ** PALAM @ GRATIOT  PIA COLUMBUS",
         "CALL:FIRE RESCUE",
         "ADDR:PALAM & GRATIOT",
         "CITY:COLUMBUS");
 
     doTest("T5",
         "** FIRE ** 9590 GRATIOT  INJURY ACCIDENT  COLUMBUS TWP",
         "CALL:FIRE - INJURY ACCIDENT",
         "ADDR:9590 GRATIOT",
         "CITY:COLUMBUS TWP");
 
     doTest("T6",
         "** RESCUE ** 10973 GRATIOT SUICIDE  CASCO",
         "CALL:RESCUE",
         "ADDR:10973 GRATIOT SUICIDE",
         "CITY:CASCO");
 
     doTest("T7",
         "**FIRE RESCUE**  ROLLOVER PI MAYER/GRATIOT. COLUMBUS TWP",
         "CALL:FIRE RESCUE - ROLLOVER PI",
         "ADDR:MAYER & GRATIOT",
         "CITY:COLUMBUS TWP");
 
     doTest("T8",
         "**FIRE RESCUE**  ROLLOVER PI INTERSECTION MAYER AND GRATIOT",
         "CALL:FIRE RESCUE - ROLLOVER PI INTERSECTION MAYER AND GRATIOT");
 
     doTest("T9",
         "**RESCUE**  CHEST PAINS 35601 WOODSIDE DR",
         "CALL:RESCUE - CHEST PAINS",
         "ADDR:35601 WOODSIDE DR");
 
     doTest("T10",
         "**RESCUE**  CHEST PAINS 35601 WOODSIDE @ FOREST",
         "CALL:RESCUE - CHEST PAINS",
         "ADDR:35601 WOODSIDE & FOREST");
 
     doTest("T11",
         "**FIRE**  SHED FIRE LINDSEY @ PALMS.  SHED CLOSE TO HOME",
         "CALL:FIRE - SHED FIRE - SHED CLOSE TO HOME",
         "ADDR:LINDSEY & PALMS");
 
     doTest("T12",
         "**FIRE**  VEHICLE FIRE 31 MILE @ COUNTY LINE",
         "CALL:FIRE - VEHICLE FIRE",
         "ADDR:31 MILE & COUNTY LINE");
 
     doTest("T13",
         "**FIRE** ODOR INVESTIGATION 34901 DIVISION. SMELL OF NATURAL GAS.",
         "CALL:FIRE - ODOR INVESTIGATION - SMELL OF NATURAL GAS",
         "ADDR:34901 DIVISION");
 
     doTest("T14",
         "**FIRE** MUTUAL AIDE TO MEMPHIS, TANKER AND MANPOWER TO DOLAN & BAUMAN",
         "CALL:FIRE - MUTUAL AIDE TO MEMPHIS - TANKER AND MANPOWER TO",
         "ADDR:DOLAN & BAUMAN");
 
     doTest("T15",
         "**RESCUE**  CPR IN PROGRESS.  35388 DIANE.",
         "CALL:RESCUE - CPR IN PROGRESS",
         "ADDR:35388 DIANE");
 
     doTest("T16",
         "**FIRE** ARMADA RIDGE @ WELDING.  BIRD HOUSE FULLY INVOLVED",
         "CALL:FIRE - ARMADA - BIRD HOUSE FULLY INVOLVED",
         "ADDR:RIDGE & WELDING");
 
     doTest("T17",
         "**RESCUE** FRESHMAN STUFFED IN LOCKER.  DIVISION @ MAIN.",
         "CALL:RESCUE - FRESHMAN STUFFED IN LOCKER",
         "ADDR:DIVISION & MAIN");
 
     doTest("T18",
         "**TEST**  I-94/CHURCH RD,  CASCO. ROLLOVER ACCIDENT",
         "CALL:TEST - ROLLOVER ACCIDENT",
         "ADDR:I-94 & CHURCH RD",
         "CITY:CASCO");

    doTest("T19",
        "* RESCUE * 8296 DIVISION RD  .. FULL ARREST.  COLUMBUS TWP",
        "CALL:RESCUE - FULL ARREST",
        "ADDR:8296 DIVISION RD",
        "CITY:COLUMBUS TWP");
 }
   
   public static void main(String[] args) {
    new MIRichmondParserTest().generateTests("T19", "CALL ADDR CITY");
   }
 }
