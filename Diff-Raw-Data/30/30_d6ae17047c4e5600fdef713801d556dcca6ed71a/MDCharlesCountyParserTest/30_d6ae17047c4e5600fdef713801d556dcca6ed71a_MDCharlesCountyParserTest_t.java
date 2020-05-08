 package net.anei.cadpage.parsers.MD;
 
 
 import net.anei.cadpage.TestManagePreferences;
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 public class MDCharlesCountyParserTest extends BaseParserTest {
   
   public MDCharlesCountyParserTest() {
     setParser(new MDCharlesCountyParser(), "CHARLES COUNTY", "MD");
     TestManagePreferences pmgr = getPreferences();
     pmgr.setTestOverrideFilter(false);
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(*CAD*) [CAD] UNDETERMINED FIRE MIDDLETOWN RD / LYLES PL 3H11-9G1 CALLER LIVES ON CADWELL ST AND CAN SEE A LARGE FIRE THRU THE WOODS...UNSURE WHAT IT IS F103490",
         "CALL:UNDETERMINED FIRE",
         "ADDR:MIDDLETOWN RD & LYLES PL");
 
     doTest("T2",
         "(*CAD*) [CAD] TROUBLE BREATHING, RESPIRATORY DISTRESS, EMS, ALS, 6C, 6D 3465 LAUREL DR 7 H3 65 year old, Female, Conscious, Breathing. Breathing Problems. Abnor",
         "CALL:TROUBLE BREATHING, RESPIRATORY DISTRESS",
         "UNIT:EMS, ALS, 6C, 6D",
         "ADDR:3465 LAUREL DR",
         "MAP:7 H3",
         "INFO:65 year old, Female, Conscious, Breathing. Breathing Problems. Abnor");
 
     doTest("T3",
         "(*CAD*) [CAD] CO DETECTOR, CARBON MONOXIDE, 8A 2204 JASON CT 7 K2 CALLER ADVISED HIS BSMT CO DETECTOR IS GOING OFF. F103510001 1022578 02:01",
         "CALL:CO DETECTOR, CARBON MONOXIDE",
         "UNIT:8A",
         "ADDR:2204 JASON CT",
         "MAP:7 K2",
         "INFO:CALLER ADVISED HIS BSMT CO DETECTOR IS GOING OFF.",
         "ID:F103510001");
 
     doTest("T4",
         "(*CAD*) [CAD] UNKNOWN MEDICAL ALARM, EMS, BLS, 32B 3487 HIGHGROVE DR 8 H5 Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown",
         "CALL:UNKNOWN MEDICAL ALARM",
         "UNIT:EMS, BLS, 32B",
         "ADDR:3487 HIGHGROVE DR",
         "MAP:8 H5",
         "INFO:Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown");
 
     doTest("T5",
         "(*CAD*) [CAD] 10-50 PI, EMS, BLS, 29A HAWTHORNE RD / CHICAMUXEN RD 7 F11 Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown n",
         "CALL:10-50 PI",
         "UNIT:EMS, BLS, 29A",
         "ADDR:HAWTHORNE RD & CHICAMUXEN RD",
         "MAP:7 F11",
         "INFO:Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Unknown n");
 
     doTest("T6",
         "(*CAD*) [CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 130 KALMIA CT 17 F6 DRYER ON FIRE / ADVISED TO GET OUT F103550006 1022816 12:07",
         "CALL:MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT",
         "ADDR:130 KALMIA CT",
         "MAP:17 F6",
         "INFO:DRYER ON FIRE / ADVISED TO GET OUT",
         "ID:F103550006");
 
     doTest("T7",
         "Subject:*CAD*\n[CAD] GAS LEAK INSIDE\nA S/F SINGLE FAMILY DWELLING 5748 OAK CT 7 E11 SMELLS GAS INSIDE HOME AND IN GARAGE F103640012 102",
         "CALL:GAS LEAK INSIDE A S/F SINGLE FAMILY DWELLING",
         "ADDR:5748 OAK CT",
         "MAP:7 E11",
         "INFO:SMELLS GAS INSIDE HOME AND IN GARAGE",
         "ID:F103640012");
 
     doTest("T8",
         "Subject:*CAD*\n[CAD] 10-50 PI EXTRICATION, TRAPPED, PINNED, EMS, ALS, 29B, 29D 9275 IRONSIDES RD RT 425, MT HOPE ELEM SCHOOL A/O 23 H4-",
         "CALL:10-50 PI EXTRICATION, TRAPPED, PINNED",
         "UNIT:EMS, ALS, 29B, 29D",
         "ADDR:9275 IRONSIDES RD RT 425",
         "PLACE:MT HOPE ELEM SCHOOL A/O",
         "MAP:23 H4",
         "INFO:-");
 
     doTest("T9",
         "Subject:*CAD*\n[CAD] GAS LEAK\nINSIDE A S/F SINGLE FAMILY DWELLING 5748 OAK CT 7 E11 SMELLS GAS INSIDE HOME AND IN GARAGE F103640012 102",
         "CALL:GAS LEAK INSIDE A S/F SINGLE FAMILY DWELLING",
         "ADDR:5748 OAK CT",
         "MAP:7 E11",
         "INFO:SMELLS GAS INSIDE HOME AND IN GARAGE",
         "ID:F103640012");
 
     doTest("T10",
         "Subject:*CAD*\n[CAD] 10-50 PI FIRE EMS APPARATUS TRUCK AMBULANCE HAWTHORNE RD / MANOR LA 15 J3 10-17 ADV SHE WAS ASSAULTED BY HER FRIEN",
         "CALL:10-50 PI FIRE",
         "UNIT:EMS APPARATUS TRUCK AMBULANCE",
         "ADDR:HAWTHORNE RD & MANOR LA",
         "MAP:15 J3",
         "INFO:10-17 ADV SHE WAS ASSAULTED BY HER FRIEN");
 
     doTest("T11",
         "Subject:*CAD*\n[CAD] 10-50 PI, EMS, BLS, 29A HAWTHORNE RD / CHICAMUXEN RD 7 F11 Age unknown, Gender unknown, Consciousness unknown, Bre",
         "CALL:10-50 PI",
         "UNIT:EMS, BLS, 29A",
         "ADDR:HAWTHORNE RD & CHICAMUXEN RD",
         "MAP:7 F11",
         "INFO:Age unknown, Gender unknown, Consciousness unknown, Bre");
 
     doTest("T12",
         "Subject:*CAD*\n[CAD] COMMERCIAL BUILDING FIRE, STRUCTURE, BLDG, 4975 HAWTHORNE RD, PENNYS TAVERN NEXT TO 7 C7 NEXT TO BLDG, CHILD CARE",
         "CALL:COMMERCIAL BUILDING FIRE, STRUCTURE, BLDG",
         "ADDR:4975 HAWTHORNE RD",
         "PLACE:PENNYS TAVERN NEXT TO",
         "MAP:7 C7",
         "INFO:NEXT TO BLDG, CHILD CARE");
 
     doTest("T13",
         "Subject:*CAD*\n[CAD] UNDETERMINED FIRE INNSBRUCK CT / MASONS SPRING RD 14 K7 CALLER REPORTING LARGE FIRE VISIBLE A/F INNBRUCK CT,UNK WH",
         "CALL:UNDETERMINED FIRE",
         "ADDR:INNSBRUCK CT & MASONS SPRING RD",
         "MAP:14 K7",
         "INFO:CALLER REPORTING LARGE FIRE VISIBLE A/F INNBRUCK CT,UNK WH");
 
     doTest("T14",
         "Subject:*CAD*\n[CAD] 10-50 PI MOTORCYCLE, EMS, ALS, ATV, BICYCLE, BIKE, 29B, 29D RIPLEY RD, 2 MILES FROM ANNAPOLIS WOODS RD 15 F6 Age u",
         "CALL:10-50 PI",
         "UNIT:MOTORCYCLE, EMS, ALS, ATV, BICYCLE, BIKE, 29B, 29D",
         "ADDR:RIPLEY RD",
         "PLACE:2 MILES FROM ANNAPOLIS WOODS RD",
         "MAP:15 F6",
         "INFO:Age u");
 
     doTest("T15",
         "Subject:*CAD*\n[CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 1 HICKORY LN, APT #307 17 G6 CALLER ADVISED HAS ODOR OF SMOKE",
         "CALL:MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT",
         "ADDR:1 HICKORY LN",
         "APT:#307",
         "MAP:17 G6",
         "INFO:CALLER ADVISED HAS ODOR OF SMOKE");
 
     doTest("T16",
         "Subject:*CAD*\n[CAD] MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT, 28 FAIRHILL LN 6 H6 ODER OF SMOKE IN EQUIPMENT ROOM. F10338000",
         "CALL:MULTI FAMILY HOUSE FIRE, TOWNHOUSE, M/F, APARTMENT",
         "ADDR:28 FAIRHILL LN",
         "MAP:6 H6",
         "INFO:ODER OF SMOKE IN EQUIPMENT ROOM. F10338000");
 
     doTest("T17",
         "Subject:*CAD*\n[CAD] AFA - SINGLE FAMILY DWELLING, FIRE ALARM, S/F HOUSE 6445 MASON SPRINGS RD 15 A4 HALLWAY S/D, PHONE WAS PUT DOWN, N",
         "CALL:AFA - SINGLE FAMILY DWELLING, FIRE ALARM, S/F HOUSE",
         "ADDR:6445 MASON SPRINGS RD",
         "MAP:15 A4",
         "INFO:HALLWAY S/D, PHONE WAS PUT DOWN, N");
 
     doTest("T18",
         "(*CAD*) [CAD] ASSIST THE AMBULANCE, MISC 10200 LA PLATA RD (RT 488), CCNH 109B 17 H6-K4 NEEDED FOR MANPOWER F110610005 1103803 11:10",
         "CALL:ASSIST THE",
         "UNIT:AMBULANCE, MISC",
         "ADDR:10200 LA PLATA RD (RT 488)",
         "PLACE:CCNH 109B",
         "MAP:17 H6-K4",
         "INFO:NEEDED FOR MANPOWER",
         "ID:F110610005");
   
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "10-50 PI MOTORCYCLE, EMS, ALS, ATV, BICYCLE, BIKE, 29B, 29D LEONARDTOWN RD / BILLINGSLEY RD 11 B8 Age unknown, Male, Conscious, Breathing.",
         "CALL:10-50 PI",
         "UNIT:MOTORCYCLE, EMS, ALS, ATV, BICYCLE, BIKE, 29B, 29D",
         "ADDR:LEONARDTOWN RD & BILLINGSLEY RD",
         "MAP:11 B8",
         "INFO:Age unknown, Male, Conscious, Breathing.");
 
     doTest("T2",
         "TREE DOWN, MISC ROCK POINT RD / CEDAR LA 36 H5 TREE ACROSS ENTIRE RDWY F111240003 1107622 06:14",
         "CALL:TREE DOWN",
         "UNIT:MISC",
         "ADDR:ROCK POINT RD & CEDAR LA",
         "MAP:36 H5",
         "INFO:TREE ACROSS ENTIRE RDWY",
         "ID:F111240003");
 
     doTest("T3",
         "ANIMAL BITE, ATTACK , EMS ,BLS 3A, 3B 17971 CYPRESS DR 38 G4 3 year old, Male, Conscious, Breathing. Animal Bites / Attacks. SERIOUS hemorrhage. E111180034 1107296",
         "CALL:ANIMAL BITE, ATTACK , EMS ,BLS",
         "UNIT:3A, 3B",
         "ADDR:17971 CYPRESS DR",
         "MAP:38 G4",
         "INFO:3 year old, Male, Conscious, Breathing. Animal Bites / Attacks. SERIOUS hemorrhage. E111180034 1107296");
 
     doTest("T5",
         "FILL IN, MISCELLANEOUS TRANSFER 9765 BEL ALTON NEWTOWN RD, STATION 10 26 D6-F4 F111180003 1107250 05:19",
         "CALL:FILL IN, MISCELLANEOUS TRANSFER",
         "ADDR:9765 BEL ALTON NEWTOWN RD",
         "PLACE:STATION 10",
         "MAP:26 D6-F4",
         "ID:F111180003");
 
     doTest("T6",
         "DETACHED SHED / GARAGE FIRE 10855 CHARLES ST, BURCHS GARAGE 17 K12 OUT OF CONTROL BRUSH FIRE..DEPUTY FLICK ON SCENE F111180002 1107249 06:00",
         "CALL:DETACHED SHED/GARAGE FIRE",
         "ADDR:10855 CHARLES ST",
         "PLACE:BURCHS GARAGE",
         "MAP:17 K12",
         "INFO:OUT OF CONTROL BRUSH FIRE..DEPUTY FLICK ON SCENE",
         "ID:F111180002");
 
     doTest("T7",
         "10-50 UNDETERMINED, EMS, BLS 29A SWAN POINT RD / ROCK PT RD 36 G11 ACCIDENT IS ON ROCK PT RD IAO SWAN PT RD, UNKNOWN INJURIES, SINGLE VEH INTO TELEPHONE POLE E1107[Class 1,2 & 3 days] ROCK POINT RD / DELOZIER FARM RD 34 A13 CALLER ADVISED FIRE IN FIELD SOMEONE IS TRYING TO PUT IT OUT F110710003 11044",
         "CALL:10-50 UNDETERMINED",
         "UNIT:EMS, BLS 29A",
         "ADDR:SWAN POINT RD & ROCK PT RD",
         "MAP:36 G11",
         "INFO:ACCIDENT IS ON ROCK PT RD IAO SWAN PT RD, UNKNOWN INJURIES, SINGLE VEH INTO TELEPHONE POLE E1107[Class 1,2 & 3 days] ROCK POINT RD / DELOZIER FARM RD 34 A13 CALLER ADVISED FIRE IN FIELD SOMEONE IS TRYING TO PUT IT OUT",
         "ID:F110710003");
 
     doTest("T8",
         "STRUC, ODOR OF SMOKE - NO FIRE, M/F DWELLING, TOWNHOUSE, APARTMENT, COMMERCIAL BLDG, MULTI FAMILY HOUSE, BUILDING KG COUNTY, 16257 DALGREN RD",
         "CALL:STRUC, ODOR OF SMOKE - NO FIRE, M/F DWELLING, TOWNHOUSE, APARTMENT, COMMERCIAL BLDG, MULTI FAMILY HOUSE, BUILDING KG COUNTY",
         "ADDR:16257 DALGREN RD");
 
     doTest("T9",
         "BRUSH, WOODS, FIELD FIRE (Class 1,2 & 3 days) CHARLES ST / COOKSEY RD 27 E1 GRASS ON FIRE  F111260014 this is donnie from Charles county md",
         "CALL:BRUSH, WOODS, FIELD FIRE (Class 1, 2 & 3 days)",
         "ADDR:CHARLES ST & COOKSEY RD",
         "MAP:27 E1",
         "INFO:GRASS ON FIRE",
         "ID:F111260014");
 
     doTest("T10",
         "10-50 PI, EMS, BLS, 29A FAULKNER RD / CRAIN HWY 26 D8 19 year old, Female, Conscious, Breathing. Traffic / Transportation Incidents. Unknown sta",
         "CALL:10-50 PI",
         "UNIT:EMS, BLS, 29A",
         "ADDR:FAULKNER RD & CRAIN HWY",
         "MAP:26 D8",
         "INFO:19 year old, Female, Conscious, Breathing. Traffic / Transportation Incidents. Unknown sta");
 
     doTest("T11",
         "BRUSH, WOODS, FIELD FIRE (Class 1,2 & 3 days) CHARLES ST / COOKSEY RD 27 E1 GRASS ON FIRE POSSIBLY WIRES DOWN F111260014 1107782 16:49",
         "CALL:BRUSH, WOODS, FIELD FIRE (Class 1, 2 & 3 days)",
         "ADDR:CHARLES ST & COOKSEY RD",
         "MAP:27 E1",
         "INFO:GRASS ON FIRE POSSIBLY WIRES DOWN",
         "ID:F111260014");
 
     doTest("T12",
         "FILL IN, MISCELLANEOUS TRANSFER 911 WASHINGTON AVE, STATION 1 17 E3-E4 ENGINE TO STATION 1 F111300021 1108052 19:50",
         "CALL:FILL IN, MISCELLANEOUS TRANSFER",
         "ADDR:911 WASHINGTON AVE",
         "PLACE:STATION 1",
         "MAP:17 E3-E4",
         "INFO:ENGINE TO STATION 1",
         "ID:F111300021");
 
     doTest("T13",
         "HEMORRHAGE, NOSEBLEED, EMS, LACERATION BLS, 21A, 21B 28 GLYMONT RD 7 B6 89 year old, Female, Conscious, Breathing. Hemorrhage / Lacerations. Blood thinner",
         "CALL:HEMORRHAGE, NOSEBLEED, EMS, LACERATION",
         "UNIT:BLS, 21A, 21B",
         "ADDR:28 GLYMONT RD",
         "MAP:7 B6",
         "INFO:89 year old, Female, Conscious, Breathing. Hemorrhage / Lacerations. Blood thinner");
  
   }
     
   public static void main(String[] args) {
     new MDCharlesCountyParserTest().generateTests("T13", "CALL UNIT ADDR PLACE CODE MAP INFO ID");
   }
 
 }
