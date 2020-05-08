 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.VA.VAFranklinCountyParser;
 
 import org.junit.Test;
 
 
 public class VAFranklinCountyParserTest extends BaseParserTest {
   
   public VAFranklinCountyParserTest() {
     setParser(new VAFranklinCountyParser(), "FRANKLIN COUNTY", "VA");
   }
   
   @Test
   public void testBoonesMillParser() {
 
     doTest("T1",
         "MAILBOX:S07 EMS-DIABETIC 400 HAZELRIDGE LN BML CFS# 2011-031035 CROSS: JUBAL EARLY HWY/DEAD END",
         "SRC:S07",
         "CALL:EMS-DIABETIC",
         "ADDR:400 HAZELRIDGE LN",
         "CITY:Boones Mill",
         "ID:2011-031035",
         "X:JUBAL EARLY HWY/DEAD END");
 
     doTest("T2",
         "MAILBOX:S03 EMS-ALLERGIC REACTION 2855 FIVE MILE MOUNTAIN RD CAL CFS# 2011-031477 CROSS: TURNERS CREEK RD/FLOYD COUNTY LINE",
         "SRC:S03",
         "CALL:EMS-ALLERGIC REACTION",
         "ADDR:2855 FIVE MILE MOUNTAIN RD",
         "CITY:Callaway",
         "ID:2011-031477",
         "X:TURNERS CREEK RD/FLOYD COUNTY LINE");
   }
   
   @Test
   public void testFerrumParser() {
 
     doTest("T1",
         "MAILBOX:S03 EMS-OTHER/DEFINE 660 FERRUM SCHOOL RD FER CFS# 2011-031307 CROSS: FRANKLIN ST W/FRANKLIN ST E",
         "SRC:S03",
         "CALL:EMS-OTHER/DEFINE",
         "ADDR:660 FERRUM SCHOOL RD",
         "CITY:Ferrum",
         "ID:2011-031307",
         "X:FRANKLIN ST W/FRANKLIN ST E");
 
     doTest("T2",
         "MAILBOX:S03 EMS-UNCONSCIOUS 845 HENRY RD FER CFS# 2011-031311 CROSS: FRANCE LN/DILLY VALLEY LN",
         "SRC:S03",
         "CALL:EMS-UNCONSCIOUS",
         "ADDR:845 HENRY RD",
         "CITY:Ferrum",
         "ID:2011-031311",
         "X:FRANCE LN/DILLY VALLEY LN");
 
     doTest("T3",
         "MAILBOX:S03 FIRE-ALARM COMMERCIAL 165 MUSEUM DR FER CFS# 2011-031312 CROSS: FRANKLIN ST/DEAD END",
         "SRC:S03",
         "CALL:FIRE-ALARM COMMERCIAL",
         "ADDR:165 MUSEUM DR",
         "CITY:Ferrum",
         "ID:2011-031312",
         "X:FRANKLIN ST/DEAD END");
 
     doTest("T4",
         "MAILBOX:S05 ACCIDENT-INJURY FRANKLIN ST & OLD FERRUM RD FER CFS# 2011-031319",
         "SRC:S05",
         "CALL:ACCIDENT-INJURY",
         "ADDR:FRANKLIN ST & OLD FERRUM RD",
         "CITY:Ferrum",
         "ID:2011-031319");
 
     doTest("T5",
         "MAILBOX:S03 ASSAULT 80 MULLINS COURT LN 16 FER CFS# 2011-031404 CROSS: WAIDSBORO RD/DEAD END",
         "SRC:S03",
         "CALL:ASSAULT",
         "ADDR:80 MULLINS COURT LN 16",
         "CITY:Ferrum",
         "ID:2011-031404",
         "X:WAIDSBORO RD/DEAD END");
 
     doTest("T6",
         "MAILBOX:S03 FIRE-ALARM COMMERCIAL 10300 FRANKLIN ST FER CFS# 2011-031440 CROSS: FERRUM MOUNTAIN RD/FERRUM SCHOOL RD",
         "SRC:S03",
         "CALL:FIRE-ALARM COMMERCIAL",
         "ADDR:10300 FRANKLIN ST",
         "CITY:Ferrum",
         "ID:2011-031440",
         "X:FERRUM MOUNTAIN RD/FERRUM SCHOOL RD");
 
     doTest("T7",
         "MAILBOX:S03 FIRE-ALARM COMMERCIAL 325 FERRUM MOUNTAIN RD FER CFS# 2011-031489 CROSS: ARTHUR CIR/WHETSTONE RD",
         "SRC:S03",
         "CALL:FIRE-ALARM COMMERCIAL",
         "ADDR:325 FERRUM MOUNTAIN RD",
         "CITY:Ferrum",
         "ID:2011-031489",
         "X:ARTHUR CIR/WHETSTONE RD");
   }
   
   @Test
   public void testGladeHillParser() {
 
     doTest("T1",
         "MAILBOX:S04 WELLBEING CHECK 560 FARM VIEW RD LOT 2 GLA CFS# 2011-031424 CROSS: GREENWAY RD/PINE GROVE RD",
         "SRC:S04",
         "CALL:WELLBEING CHECK",
         "ADDR:560 FARM VIEW RD LOT 2",
         "CITY:Glade Hill",
         "ID:2011-031424",
         "X:GREENWAY RD/PINE GROVE RD");
   }
   
   @Test
   public void testHenryParser() {
 
     doTest("T1",
         "MAILBOX:S03 EMS-HEAD INJURY 94 OVERBROOK DR HEN CFS# 2011-031420 CROSS: COTTONWOOD DR/DEAD END",
         "SRC:S03",
         "CALL:EMS-HEAD INJURY",
         "ADDR:94 OVERBROOK DR",
         "CITY:Henry",
         "ID:2011-031420",
         "X:COTTONWOOD DR/DEAD END");
 
     doTest("T2",
         "MAILBOX:S08 EMS-LACERATION 105 DAWNRIDGE LN HEN CFS# 2011-031452 CROSS: KNOB CHURCH RD/DEAD END",
         "SRC:S08",
         "CALL:EMS-LACERATION",
         "ADDR:105 DAWNRIDGE LN",
         "CITY:Henry",
         "ID:2011-031452",
         "X:KNOB CHURCH RD/DEAD END");
   }
   
   @Test
   public void testPenHookParser() {
 
     doTest("T1",
         "MAILBOX:S15 EMS-OTHER/DEFINE 460 SAGO RD PEN CFS# 2011-031255 CROSS: CIRCLE CREEK RD/WALKER RD",
         "SRC:S15",
         "CALL:EMS-OTHER/DEFINE",
         "ADDR:460 SAGO RD",
         "CITY:Penhook",
         "ID:2011-031255",
         "X:CIRCLE CREEK RD/WALKER RD");
 
     doTest("T2",
         "MAILBOX:S06 EMS-CHEST PAIN 6586 CIRCLE CREEK RD PEN CFS# 2011-031500 CROSS: SAGO RD/PITTSYL CO LINE",
         "SRC:S06",
         "CALL:EMS-CHEST PAIN",
         "ADDR:6586 CIRCLE CREEK RD",
         "CITY:Penhook",
         "ID:2011-031500",
         "X:SAGO RD/PITTSYL CO LINE");
   }
   
   @Test
   public void testRockMountParser() {
 
     doTest("T1",
         "MAILBOX:S02 EMS-SEIZURE 000735 EAST COURT ST RMT CFS# 2011-031028 CROSS: DONALD AVE/POWER DAM RD",
         "SRC:S02",
         "CALL:EMS-SEIZURE",
         "ADDR:000735 EAST COURT ST",
         "CITY:Rocky Mount",
         "ID:2011-031028",
         "X:DONALD AVE/POWER DAM RD");
 
     doTest("T2",
         "MAILBOX:S02 EMS-MENTAL 103 JAMESTOWN RD RMT CFS# 2011-031037 CROSS: GRASSY HILL RD/BLUE BEND RD",
         "SRC:S02",
         "CALL:EMS-MENTAL",
         "ADDR:103 JAMESTOWN RD",
         "CITY:Rocky Mount",
         "ID:2011-031037",
         "X:GRASSY HILL RD/BLUE BEND RD");
 
     doTest("T3",
         "MAILBOX:S02 EMS-DOA 329 DOE RUN RD RMT CFS# 2011-031047 CROSS: SHAMROCK RD/TRIPPLE CREEK RD",
         "SRC:S02",
         "CALL:EMS-DOA",
         "ADDR:329 DOE RUN RD",
         "CITY:Rocky Mount",
         "ID:2011-031047",
         "X:SHAMROCK RD/TRIPPLE CREEK RD");
 
     doTest("T4",
         "MAILBOX:S02 EMS-UNCONSCIOUS VIRGIL H GOODE HWY & GLEN MEAD RMT CFS# 2011-031058",
         "SRC:S02",
         "CALL:EMS-UNCONSCIOUS",
         "ADDR:VIRGIL H GOODE HWY & GLEN MEAD",
         "CITY:Rocky Mount",
         "ID:2011-031058");
 
     doTest("T5",
         "MAILBOX:S07 ACCIDENT-INJURY VIRGIL H GOODE HWY & BONBROOK M RMT CFS# 2011-031074",
         "SRC:S07",
         "CALL:ACCIDENT-INJURY",
         "ADDR:VIRGIL H GOODE HWY & BONBROOK M",
         "CITY:Rocky Mount",
         "ID:2011-031074");
 
     doTest("T6",
         "MAILBOX:S02 EMS-PATIENT FALLEN 300 HATCHER ST RMT CFS# 2011-031083 CROSS: BUCKNER ST/FRANKLIN ST",
         "SRC:S02",
         "CALL:EMS-PATIENT FALLEN",
         "ADDR:300 HATCHER ST",
         "CITY:Rocky Mount",
         "ID:2011-031083",
         "X:BUCKNER ST/FRANKLIN ST");
 
     doTest("T7",
         "MAILBOX:S04 EMS-PATIENT IN PAIN 720 ORCHARD AVE RMT CFS# 2011-031066 CROSS: MARTIN ST/SOUTH MAIN ST",
         "SRC:S04",
         "CALL:EMS-PATIENT IN PAIN",
         "ADDR:720 ORCHARD AVE",
         "CITY:Rocky Mount",
         "ID:2011-031066",
         "X:MARTIN ST/SOUTH MAIN ST");
 
     doTest("T8",
         "MAILBOX:S08 ACCIDENT-INJURY VIRGIL H GOODE HWY & CROOKED OA RMT CFS# 2011-031356",
         "SRC:S08",
         "CALL:ACCIDENT-INJURY",
         "ADDR:VIRGIL H GOODE HWY & CROOKED OA",
         "CITY:Rocky Mount",
         "ID:2011-031356");
 
     doTest("T9",
         "MAILBOX:S02 FIRE-STRUCTURE 2405 FRANKLIN ST RMT CFS# 2011-031354 CROSS: TOWN LIMITS/SIX MILE POST RD",
         "SRC:S02",
         "CALL:FIRE-STRUCTURE",
         "ADDR:2405 FRANKLIN ST",
         "CITY:Rocky Mount",
         "ID:2011-031354",
         "X:TOWN LIMITS/SIX MILE POST RD");
   }
   
   @Test
   public void testUnionHallParser() {
 
     doTest("T1",
         "MAILBOX:S02 DRUNK DRIVER 11729 OLD FRANKLIN TPK UHL CFS# 2011-031446 CROSS: BERGER LOOP/KEMP FORD RD",
         "SRC:S02",
         "CALL:DRUNK DRIVER",
         "ADDR:11729 OLD FRANKLIN TPK",
         "CITY:Union Hall",
         "ID:2011-031446",
         "X:BERGER LOOP/KEMP FORD RD");
   }
   
   @Test
   public void testWirtzParser() {
 
     doTest("T1",
         "MAILBOX:S15 FIRE-STRUCTURE 150 WINDSOR POINT LP WIR CFS# 2011-031065 CROSS: PONDEROSA DR W/PONDEROSA DR E",
         "SRC:S15",
         "CALL:FIRE-STRUCTURE",
         "ADDR:150 WINDSOR POINT LP",
         "CITY:Wirtz",
         "ID:2011-031065",
         "X:PONDEROSA DR W/PONDEROSA DR E");
 
     doTest("T2",
         "MAILBOX:S02 EMS-OTHER/DEFINE 3835 WIRTZ RD WIR CFS# 2011-031294 CROSS: ALEAN RD/WILKERSON RD",
         "SRC:S02",
         "CALL:EMS-OTHER/DEFINE",
         "ADDR:3835 WIRTZ RD",
         "CITY:Wirtz",
         "ID:2011-031294",
         "X:ALEAN RD/WILKERSON RD");
 
     doTest("T3",
         "MAILBOX:S07 EMS-UNCONSCIOUS 302 DUKE DR WIR CFS# 2011-031425 CROSS: RIDGEVIEW ST/DEAD END",
         "SRC:S07",
         "CALL:EMS-UNCONSCIOUS",
         "ADDR:302 DUKE DR",
         "CITY:Wirtz",
         "ID:2011-031425",
         "X:RIDGEVIEW ST/DEAD END");
   }
   
   @Test
   public void testBassettParser() {
 
     doTest("T1",
         "MAILBOX:S08 EMS-PATIENT FALLEN 2809 FORK MOUNTAIN RD BAS CFS# 2011-031043 CROSS: FAWNDALE RD/VIRGIL H GOODE HWY    ",
         "SRC:S08",
         "CALL:EMS-PATIENT FALLEN",
         "ADDR:2809 FORK MOUNTAIN RD",
         "CITY:Bassett",
         "ID:2011-031043",
         "X:FAWNDALE RD/VIRGIL H GOODE HWY");
 
     doTest("T2",
         "MAILBOX:S08 WELLBEING CHECK 313 FAWNDALE RD BAS CFS# 2011-031281 CROSS: COUNTRY RIDGE RD/FORK MOUNTAIN RD",
         "SRC:S08",
         "CALL:WELLBEING CHECK",
         "ADDR:313 FAWNDALE RD",
         "CITY:Bassett",
         "ID:2011-031281",
         "X:COUNTRY RIDGE RD/FORK MOUNTAIN RD");
   }
   
   @Test
   public void testHardyParser() {
 
     doTest("T1",
         "MAILBOX:S15 EMS-PATIENT IN PAIN 001635 MOREWOOD RD HAR CFS# 2011-031025 CROSS: INDIAN POINTE DR/CHESTNUT POINTE DR",
         "SRC:S15",
         "CALL:EMS-PATIENT IN PAIN",
         "ADDR:001635 MOREWOOD RD",
         "CITY:Hardy",
         "ID:2011-031025",
         "X:INDIAN POINTE DR/CHESTNUT POINTE DR");
 
     doTest("T2",
         "MAILBOX:S09 EMS-PATIENT FALLEN 6633 EDWARDSVILLE RD HAR CFS# 2011-031084 CROSS: WINTERHILL DR/ROBIN LN",
         "SRC:S09",
         "CALL:EMS-PATIENT FALLEN",
         "ADDR:6633 EDWARDSVILLE RD",
         "CITY:Hardy",
         "ID:2011-031084",
         "X:WINTERHILL DR/ROBIN LN");
 
     doTest("T3",
         "MAILBOX:S10 EMS-STROKE 80 WESTLAKE RD HAR CFS# 2011-031067 CROSS: B T WASHINGTON HWY/PROFESSIONAL DR",
         "SRC:S10",
         "CALL:EMS-STROKE",
         "ADDR:80 WESTLAKE RD",
         "CITY:Hardy",
         "ID:2011-031067",
         "X:B T WASHINGTON HWY/PROFESSIONAL DR");
 
     doTest("T4",
         "MAILBOX:S15 EMS-PATIENT IN PAIN 545 FARMINGTON RD HAR CFS# 2011-031401 CROSS: POTEET RD/EDWARDSVILLE RD",
         "SRC:S15",
         "CALL:EMS-PATIENT IN PAIN",
         "ADDR:545 FARMINGTON RD",
         "CITY:Hardy",
         "ID:2011-031401",
         "X:POTEET RD/EDWARDSVILLE RD");
   }
   
   @Test
   public void testMartinsvilleParser() {
 
     doTest("T1",
         "MAILBOX:S06 EMS-DIFFICULTY BREATHING 6342 COOPERS MOUNTAIN RD MAR CFS# 2011-031030 CROSS: SUTTON HOLLOW RD/SONTAG RD",
         "SRC:S06",
         "CALL:EMS-DIFFICULTY BREATHING",
         "ADDR:6342 COOPERS MOUNTAIN RD",
         "CITY:Martinsville",
         "ID:2011-031030",
         "X:SUTTON HOLLOW RD/SONTAG RD");
 
     doTest("T2",
         "MAILBOX:S06 ACCIDENT-INJURY 755 SNOW CREEK RD MAR CFS# 2011-031081 CROSS: HENRY COUNTY LINE/SHADY GROVE RD",
         "SRC:S06",
         "CALL:ACCIDENT-INJURY",
         "ADDR:755 SNOW CREEK RD",
         "CITY:Martinsville",
         "ID:2011-031081",
         "X:HENRY COUNTY LINE/SHADY GROVE RD");
   }
   
   @Test
   public void testMonetaParser() {
 
     doTest("T1",
         "MAILBOX:S10 EMS-PATIENT FALLEN 1195 MERRIMAN WAY RD MON CFS# 2011-031456 CROSS: PERIWINKLE RD/LANDFALL DR",
         "SRC:S10",
         "CALL:EMS-PATIENT FALLEN",
         "ADDR:1195 MERRIMAN WAY RD",
         "CITY:Moneta",
         "ID:2011-031456",
         "X:PERIWINKLE RD/LANDFALL DR");
   }
   
   @Test
   public void testSquad7Parser() {
 
     doTest("T1",
         "MAILBOX:S07 EMS-OTHER/DEFINE 18360 VIRGIL H GOODE HWY 124 RMT CFS# 2010-030542 CROSS: SHADY LN/BLACKWATER RIVER",
         "SRC:S07",
         "CALL:EMS-OTHER/DEFINE",
         "ADDR:18360 VIRGIL H GOODE HWY 124",
         "CITY:Rocky Mount",
         "ID:2010-030542",
         "X:SHADY LN/BLACKWATER RIVER");
 
     doTest("T2",
         "MAILBOX:S07 EMS-CHEST PAIN 413 WOODDALE DR RMT CFS# 2010-030355 CROSS: VIRGIL H GOODE HWY/DEAD END",
         "SRC:S07",
         "CALL:EMS-CHEST PAIN",
         "ADDR:413 WOODDALE DR",
         "CITY:Rocky Mount",
         "ID:2010-030355",
         "X:VIRGIL H GOODE HWY/DEAD END");
 
     doTest("T3",
         "MAILBOX:S07 EMS-PATIENT FALLEN 3005 GREEN LEVEL RD RMT CFS# 2010-030541 CROSS: GRASSY HILL RD/LITTLE MOUNTAIN DR",
         "SRC:S07",
         "CALL:EMS-PATIENT FALLEN",
         "ADDR:3005 GREEN LEVEL RD",
         "CITY:Rocky Mount",
         "ID:2010-030541",
         "X:GRASSY HILL RD/LITTLE MOUNTAIN DR");
 
     doTest("T4",
         "MAILBOX:S07 EMS-CARDIAC VIRGIL H GOODE HWY & LINK ST RMT CFS# 2010-030580",
         "SRC:S07",
         "CALL:EMS-CARDIAC",
         "ADDR:VIRGIL H GOODE HWY & LINK ST",
         "CITY:Rocky Mount",
         "ID:2010-030580");
 
     doTest("T5",
         "MAILBOX:S07 EMS-HIGH BLOOD PRESSURE 1808 BETHLEHEM RD BML CFS# 2010-030643 CROSS: BETHANY RD/DILLONS MILL RD",
         "SRC:S07",
         "CALL:EMS-HIGH BLOOD PRESSURE",
         "ADDR:1808 BETHLEHEM RD",
         "CITY:Boones Mill",
         "ID:2010-030643",
         "X:BETHANY RD/DILLONS MILL RD");
 
     doTest("T6",
         "MAILBOX:C07 FIRE-ALARM COMMERCIAL 3325 GRASSY HILL RD RMT CFS# 2010-033866 CROSS: JAMESTOWN RD/CALLAWAY RD",
         "SRC:C07",
         "CALL:FIRE-ALARM COMMERCIAL",
         "ADDR:3325 GRASSY HILL RD",
         "CITY:Rocky Mount",
         "ID:2010-033866",
         "X:JAMESTOWN RD/CALLAWAY RD");
   }
   
   @Test
   public void testSquad2Parser() {
 
     doTest("T1",
         "S: M:MAILBOX:S02 ACCIDENT-INJURY 1450 WIRTZ CFS# 2011-028395\n",
         "SRC:S02",
         "CALL:ACCIDENT-INJURY",
         "ADDR:1450 WIRTZ",
         "ID:2011-028395");
   }
   
   @Test
   public void testSquad3Parser() {
 
     doTest("T1",
         "MAILBOX:C03 FIRE-STRUCTURE 2405 FRANKLIN ST RMT CFS# 2011-031354 CROSS: TOWN LIMITS/SIX MILE POST RD",
         "SRC:C03",
         "CALL:FIRE-STRUCTURE",
         "ADDR:2405 FRANKLIN ST",
         "CITY:Rocky Mount",
         "ID:2011-031354",
         "X:TOWN LIMITS/SIX MILE POST RD");
 
     doTest("T1",
         "MAILBOX:C03 ACCIDENT-INJURY FRANKLIN ST & OLD FERRUM RD FER CFS# 2011-031319",
         "SRC:C03",
         "CALL:ACCIDENT-INJURY",
         "ADDR:FRANKLIN ST & OLD FERRUM RD",
         "CITY:Ferrum",
         "ID:2011-031319");
   }
   
   public static void main(String args[]) {
     new VAFranklinCountyParserTest().generateTests("T1");
   }
 }
   
