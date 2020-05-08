 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYJeffersonCountyParser;
 
 import org.junit.Test;
 
 
 public class NYJeffersonCountyParserTest extends BaseParserTest {
   
   public NYJeffersonCountyParserTest() {
     setParser(new NYJeffersonCountyParser(), "JEFFERSON COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "BREATHING PROBL|18077 REED POINT RD:ORLEANS(T)|77 YO MALE DIFF BREATHING",
         "CALL:BREATHING PROBL",
         "ADDR:18077 REED POINT RD",
         "CITY:ORLEANS",
         "INFO:77 YO MALE DIFF BREATHING");
 
     doTest("T2",
         "ABDOMINAL PAIN|18081 REED POINT RD:ORLEANS(T)|79F  :ProQA Medical Case Entry Finished You are responding to a patient with abdominal pain.  The patient is a  79-year-old female, who is conscious and breathing.  Abdominal Pain / Problems.  Caller Statement: ABDOMINAL PAIN.",
         "CALL:ABDOMINAL PAIN",
         "ADDR:18081 REED POINT RD",
         "CITY:ORLEANS",
         "INFO:79F  :ProQA Medical Case Entry Finished You are responding to a patient with abdominal pain.  The patient is a  79-year-old female, who is conscious and breathing.  Abdominal Pain / Problems.  Caller Statement: ABDOMINAL PAIN.");
 
     doTest("T3",
         "ASSIST-EMS|I 81 MM 176 N:ALEXANDRIA(T)|1 mile south of 50 in the north bound lane   vehicle appears to be stuck in mediun    unkn9ow if occupied  :3D63 REQUESTING EMS RESPOND.  ONE FEMALE OCCUPANT COMPLAINING OF WRIST PAIN.",
         "CALL:ASSIST-EMS",
         "ADDR:I 81 MM 176 N",
         "CITY:ALEXANDRIA",
         "INFO:1 mile south of 50 in the north bound lane   vehicle appears to be stuck in mediun    unkn9ow if occupied  :3D63 REQUESTING EMS RESPOND.  ONE FEMALE OCCUPANT COMPLAINING OF WRIST PAIN.");
 
     doTest("T4",
         "CHEST PAIN|622 JOHN ST:CLAYTON(V)|59 FEMALE/CARDIAC ISSUES",
         "CALL:CHEST PAIN",
         "ADDR:622 JOHN ST",
         "CITY:CLAYTON",
         "INFO:59 FEMALE/CARDIAC ISSUES");
 
     doTest("T5",
         "MVA-PI|SHIMEL RD & STATE ROUTE 411:ORLEANS(T)|1 VEH OFF ROAD - MINOR INJURY",
         "CALL:MVA-PI",
         "ADDR:SHIMEL RD & STATE ROUTE 411",
         "CITY:ORLEANS",
         "INFO:1 VEH OFF ROAD - MINOR INJURY");
 
     doTest("T6",
         "FIRE ALARM|37382 ORLEANS CEMETERY RD:ORLEANS(T)|CAPUTO RESIDENCE GENERAL SMOKE ALARM",
         "CALL:FIRE ALARM",
         "ADDR:37382 ORLEANS CEMETERY RD",
         "CITY:ORLEANS",
         "INFO:CAPUTO RESIDENCE GENERAL SMOKE ALARM");
 
     doTest("T7",
         "(DISPATCH:4392) MVA-PI|I 81 MM 176 N:ALEXANDRIA(T)|SPUN OUT IN THE MIDDLE OF DRIVING LANE YELLOW SEDAN NORTHBOUND LANE. CALL",
         "CALL:MVA-PI",
         "ADDR:I 81 MM 176 N",
         "CITY:ALEXANDRIA",
         "INFO:SPUN OUT IN THE MIDDLE OF DRIVING LANE YELLOW SEDAN NORTHBOUND LANE. CALL");
 
     doTest("T8",
         "FALL|34226 CARTER STREET RD:ORLEANS(T)|  CALLBACK=(315)489-3188 LAT=  LON=",
         "CALL:FALL",
         "ADDR:34226 CARTER STREET RD",
         "CITY:ORLEANS",
         "INFO:CALLBACK=(315)489-3188 LAT=  LON=");
 
     doTest("T9",
         "((LVFD) DISPATCH:4391,27FR) SICK PERSON|30950 STATE ROUTE 180  ;NORTHER NY AGRICULTRUAL MUSEUM:ORLEANS(T)|",
         "CALL:SICK PERSON",
         "ADDR:30950 STATE ROUTE 180",
         "PLACE:NORTHER NY AGRICULTRUAL MUSEUM",
         "CITY:ORLEANS");

    doTest("T10",
        "sentto-76513067-101-1314862564-3159559896=vtext.com@returns.groups.yahoo.com ([carthageambulance] DISPATCH:1191) FALL|1045 WEST ST:CARTHAGE(V)|83 Y/F FELL OUT OF BED BROKEN NOSE AN",
        "CALL:FALL",
        "ADDR:1045 WEST ST",
        "CITY:CARTHAGE",
        "INFO:83 Y/F FELL OUT OF BED BROKEN NOSE AN");
 }
   
   public static void main(String[] args) {
     new NYJeffersonCountyParserTest().generateTests("T10");
   }
 }
