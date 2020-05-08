 package net.anei.cadpage.parsers.general;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.general.GeneralParser;
 
 import org.junit.Test;
 
 
 public class GeneralParserTest extends BaseParserTest {
   
   public GeneralParserTest() {
     setParser(new GeneralParser(), "", "");
   }
   
   @Test
   public void testProblem() {
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "2010-189098*1211 PECAN ST***CONOVER**OVERDOSE*********",
         "ID:2010-189098",
         "ADDR:1211 PECAN ST",
         "CALL:CONOVER",
         "INFO:OVERDOSE");
     
     doTest("T2",
         "DEPTWK     TYP: MVA- UNKNOWN STATUS AD: RTE 17 W&DARK DR W OFF CTY: WALLKILL CN: RICHARD CMT1: CALLER REPORTS A 2 CAR MV",
         "CALL:MVA- UNKNOWN STATUS",
         "ADDR:RTE 17 W & DARK DR W",
         "CITY:WALLKILL",
         "PLACE:RICHARD",
         "INFO:OFF / CALLER REPORTS A 2 CAR MV");
     
     doTest("T3",
         "S: M:CAD:4 HICKORY TREE RD;B11;VERIZON;(Z)MOTOR VEH ACCIDENT INJURY;LOWER GRASSY BRANCH RD;JULIANNE PL",
         "ADDR:4 HICKORY TREE RD",
         "UNIT:B11",
         "CALL:VERIZON",
         "INFO:(Z)MOTOR VEH ACCIDENT INJURY",
         "X:LOWER GRASSY BRANCH RD & JULIANNE PL");
     
     doTest("T4",
         "([cad13] ) Call:30A01-TRAUMA NOT DAN,775 GATEWAY DR SE Apt:CLUBHS,X-St:SYCOLIN RD SE VAN,M6132 ACO13,Box:2013 ,ADC:515",
         "CALL:30A01-TRAUMA NOT DAN",
         "ADDR:775 GATEWAY DR SE",
         "APT:CLUBHS",
         "X:SYCOLIN RD SE VAN",
         "UNIT:M6132 ACO13",
         "BOX:515");
 
     doTest("T5",
         "MBLS-MEDICAL Location: 160 RICHARD DR DAYTON XENIA RD / ROBERT LN XENIA TWP Time: 17:31 Units: M51  Common",
         "CALL:MBLS-MEDICAL",
         "ADDR:160 RICHARD DR",
         "INFO:DAYTON XENIA RD / ROBERT LN XENIA TWP",
         "TIME:17:31",
         "UNIT:M51  Common");
     
     doTest("T6",
         "ST3000 TYP: SMOKE IN DWELLING AD: 2008 ELBOW LN CTY: LOWER MACUNGIE CN: LINDA STROHL CMT1: **SMOKE IN THE DWELLING CMT2: SMOKE COMING FROM THE EXHAUST PIPE OF HEATING SYSTEM FILLING BASEMENT WIT TIME: 06:58 UNTS: FD/30CHIEFS FD/ST30 FD/A6681 FD/ST66 FD/R3041 FD/E3012 FD/TK3031 FD/E3112 XST: 4486 REDWOOD LN XST2: HESS CIR",
         "CALL:SMOKE IN DWELLING",
         "ADDR:2008 ELBOW LN",
         "CITY:LOWER MACUNGIE",
         "INFO:SMOKE IN THE DWELLING / SMOKE COMING FROM THE EXHAUST PIPE OF HEATING SYSTEM FILLING BASEMENT WIT",
         "TIME:06:58",
         "PLACE:LINDA STROHL",
         "UNIT:FD/30CHIEFS FD/ST30 FD/A6681 FD/ST66 FD/R3041 FD/E3012 FD/TK3031 FD/E3112",
         "X:4486 REDWOOD LN & HESS CIR");
     
     doTest("T7",
         "IPS I/Page Notification / Loc: 7583 WALL TRIANA HWY MDCO: @URGENT MEDCARE EVT#: C1009726 TYPE: M TIME: 13:36:55",
         "CALL:IPS I/Page Notification /",
         "ADDR:7583 WALL TRIANA HWY",
         "INFO:MDCO: @URGENT MEDCARE",
         "ID:C1009726",
         "TIME:13:36:55");
     
     doTest("T8",
         "(*CAD*) [CAD] CO DETECTOR, CARBON MONOXIDE, 8A 2204 JASON CT 7 K2 CALLER ADVISED HIS BSMT CO DETECTOR IS GOING OFF. F103510001 1022578 02:01",
         "CALL:CARBON MONOXIDE",
         "ADDR:8A 2204 JASON CT",
         "INFO:7 K2 CALLER ADVISED HIS BSMT CO DETECTOR IS GOING OFF F103510001 1022578",
         "TIME:02:01");
     
     doTest("T9",
         "CAD:16 BLUE HERON DR;B213;BECKHAM, LINDA & MALACHY;FALLS;RIVER CANE DR",
         "ADDR:16 BLUE HERON DR",
         "UNIT:B213",
         "CALL:BECKHAM",
         "X:LINDA & MALACHY & RIVER CANE DR",
         "INFO:FALLS");
     
     doTest("T10",
           "(CAD Page) FIRE, SMOKE CHECK; 21199 HWY 299 E ,BELLAVISTA ; ; 11764 BLK REBECCA LN; Map: 3225; Inc# 009012; VOL33; neighbor burning",
           "CALL:SMOKE CHECK",
           "ADDR:21199 HWY 299 E",
           "INFO:BELLAVISTA; neighbor burning",
           "X:11764 BLK REBECCA LN",
           "MAP:3225",
           "ID:009012",
           "UNIT:VOL33");
     
     doTest("T11",
           "Inc: SICK PERSON Add: 352 STATE ST\nCity: ST_MARYS\nXSt: ANTHONY RD * RIDGWAY ST MARYS RD\nAgency: ST MARYS AMB", 
           "CALL:SICK PERSON",
           "ADDR:352 STATE ST",
           "CITY:ST_MARYS",
           "X:ANTHONY RD",
           "INFO:RIDGWAY ST MARYS RD",
           "SRC:ST MARYS AMB"); 
     
     doTest("T12",
         "DEPTWK     TYP: MEDICAL ASSISTANCE AD: 455 SCHUTT ROAD EXT CTY: WALLKILL CN: MOBILE LIFE CMT1: EVALUATION FROM MVA CMT2: Ori",
         "CALL:MEDICAL ASSISTANCE",
         "ADDR:455 SCHUTT ROAD EXT",
         "MADDR:455 SCHUTT ROAD",
         "CITY:WALLKILL",
         "PLACE:MOBILE LIFE",
         "INFO:EVALUATION FROM MVA / Ori");
     
     doTest("T13",
         "DEPTWK     TYP: HEMORHAGE/LACERATION BLD: 10 APT: 1001 AD: 203 GENUNG ST CTY: WALLKILL CN: BERNADETTE CMT1: MLACERATION TO H",
         "CALL:HEMORHAGE/LACERATION",
         "APT:10-1001",
         "ADDR:203 GENUNG ST",
         "CITY:WALLKILL",
         "PLACE:BERNADETTE",
         "INFO:MLACERATION TO H");
     
     doTest("T14",
         "Subject:FredCo\n[CAD] CT: HOUSE / APPLIANCE FIRE (STRUCTURE PRE-ALERT) 6715 MEADOW LAWN CIR",
         "CALL:HOUSE/APPLIANCE FIRE (STRUCTURE PRE-ALERT)",
         "ADDR:6715 MEADOW LAWN CIR");
     
     doTest("T15",
         "10116004231 TYP: SEIZURES AD: 1051 FIFTH ST CTY: FT MYERS BEACH LOC: LIGHTHOUSE RESORT TIKI BAR CN: VERIZON WIRELESS CMT1: CONVULSIONS /",
         "ID:10116004231",
         "CALL:SEIZURES",
         "ADDR:1051 FIFTH ST",
         "CITY:FT MYERS BEACH",
         "PLACE:LIGHTHOUSE RESORT TIKI BAR",
         "NAME:VERIZON WIRELESS",
         "INFO:CONVULSIONS /");
     
     doTest("T16",
         "DEPTWK     TYP: FAINTING/UNCON APT: 240 AD: 563 ST RTE 211 E CTY: WALLKILL CN: LISA CMT1: MPASSED OUT IN THE TUB 44 YEAR OLD",
         "CALL:FAINTING/UNCON",
         "APT:240",
         "ADDR:563 ST RTE 211 E",
         "MADDR:563 STATE 211 E",
         "CITY:WALLKILL",
         "PLACE:LISA",
         "INFO:MPASSED OUT IN THE TUB 44 YEAR OLD");
     
     doTest("T17",
         "10121005439 TYP: FAINTING AD: 450 OLD SAN CARLOS BLVD CTY: FT MYERS BEACH LOC: FRONT LOBBY CN: METRO PCS CMT1: UNCONSCIOUS / FAINTING (NE",
         "ID:10121005439",
         "CALL:FAINTING",
         "ADDR:450 OLD SAN CARLOS BLVD",
         "CITY:FT MYERS BEACH",
         "PLACE:FRONT LOBBY",
         "NAME:METRO PCS",
         "INFO:UNCONSCIOUS / FAINTING (NE");
     
     doTest("T18",
         "1101-005780*565 ARLIE LN***LILLINGTON***GRASS/FIELD FIRE****1144****",
         "ID:1101-005780",
         "ADDR:565 ARLIE LN",
         "CALL:LILLINGTON",
         "INFO:GRASS/FIELD FIRE * 1144");
     
     doTest("T19",
         "Loc: 118 S 4TH ST YNGWD_B X-sts: CHESTNUT ST & LOCUST ST  Inc#:F11000861 NATURE: FIRE  CALLER: ANTHONY GALADIAY TOC: 19:08:40 Comments: FLAMES COMING OUT OF THE WALL  Disp: E26-1",
         "ADDR:118 S 4TH ST",
         "INFO:YNGWD_B / FLAMES COMING OUT OF THE WALL",
         "X:CHESTNUT ST & LOCUST ST",
         "ID:F11000861",
         "CALL:FIRE",
         "NAME:ANTHONY GALADIAY",
         "TIME:19:08:40",
         "SRC:E26-1");
     
     doTest("T20",
         "(From 911 Center) 02/14/11 16:26 5 CEDAR LN BRUTUS Cardiac",
         "DATE:02/14/11",
         "TIME:16:26",
         "CALL:BRUTUS Cardiac",
         "ADDR:5 CEDAR LN");
     
     doTest("T21",
         "FSTRUC>STRUCTURE FIRE 1890 SR 232 XS: LAUREL POINT ISABEL RD LAUREL BUTLER, SHAWN Map: Grids:, Cad: 2011-0000014744<br>",
         "CALL:FSTRUC>STRUCTURE FIRE",
         "ADDR:1890 SR 232",
         "MADDR:1890 STATE 232",
         "X:LAUREL POINT ISABEL RD LAUREL BUTLER",
         "INFO:SHAWN",
         "ID:2011-0000014744");
     
     doTest("T22",
         "CAD:WCRS;424;02/23/2011 14:14:08;ACCIDENT W/INJURY;750 FISHING CREEK ARBOR RD",
         "ID:424",
         "DATE:02/23/2011",
         "TIME:14:14:08",
         "CALL:ACCIDENT W/INJURY",
         "ADDR:750 FISHING CREEK ARBOR RD");
     
     doTest("T23",
         "(*CAD*) [CAD] Sta: 78AST   Loc: 7229 Seashore Hwy   --- Falls-BLS",
         "SRC:78AST",
         "ADDR:7229 Seashore Hwy",
         "INFO:--- Falls-BLS");
     
     doTest("T24",
         "(Dispatch Message) ALARMS-CO DETECTOR        \n90 FALLS CREEK TR \nGUARDIAN ALARM 866.883.3100\nCO ALARM SOUNDING, SIGNAL COMES TO ALARM CENTER\nOPR/STACY",
         "CALL:ALARMS-CO DETECTOR",
         "ADDR:90 FALLS CREEK TR",
         "INFO:GUARDIAN ALARM 866.883.3100\n CO ALARM SOUNDING, SIGNAL COMES TO ALARM CENTER\n OPR/STACY");
     
     doTest("T25",
         "(CAD Page) 11-009351|02:57:00|:8 - EMS|\n@ 12040 SR 362 UNIT 19 MINSTER BETW CANAL #1 RD & COUNTY LINE|",
         "ID:11-009351",
         "TIME:02:57:00",
         "CALL::8 - EMS",
         "ADDR:12040 SR 362",
         "MADDR:12040 STATE 362",
         "INFO:UNIT 19 MINSTER BETW CANAL #1 RD & COUNTY LINE");
     
     doTest("T26",
         "(CVA-CLASS 1) MANHEIM TWP~106 DELANCY PL~RUTLEDGE AVE~DEAD END~QRS204~08:55:30^",
         "CALL:MANHEIM TWP",
         "ADDR:106 DELANCY PL",
         "X:RUTLEDGE AVE",
         "INFO:DEAD END ~ ^",
         "UNIT:QRS204",
         "TIME:08:55:30");
     
     doTest("T27",
         ".... (CCVFD) STRUCTURE FIRE(69D01) 12102 STEEPLEWAY BLVD  (409A) (%GRA2%) COMMENTS:12102 STEEPLEWAY BLVD: cross streets: \nWEST RD &amp; THOROUGHBRED DR",
         "CALL:STRUCTURE FIRE(69D01)",
         "ADDR:12102 STEEPLEWAY BLVD",
         "INFO:(409A) (%GRA2%) / 12102 STEEPLEWAY BLVD: cross streets:",
         "X:WEST RD & THOROUGHBRED DR");
     
     doTest("T28",
         "Dispatch\nSta:28-3\nType:ALRM\nLoc:158 KENTON AVE                \nVen:PITMAN\nDsp-\nClr-",
         "SRC:28-3",
         "CALL:ALRM",
         "ADDR:158 KENTON AVE",
         "CITY:PITMAN",
         "INFO:Dsp-\n Clr-");
     
     doTest("T29",
         "REF 3791 S CRANBERRY BLVD XST PRINCE LN, VEHICLE ACCIDENT WITH EXTRACTION,2 PTS ALL PT'S STABLE , FIRE DISPATCH 2641 /UNIT ON SCENE- BN8,E81,NPF3,",
         "CALL:REF",
         "ADDR:3791 S CRANBERRY BLVD",
         "INFO:XST PRINCE LN, VEHICLE ACCIDENT WITH EXTRACTION, 2 PTS ALL PT'S STABLE, FIRE DISPATCH 2641 /UNIT ON SCENE- BN8, NPF3",
         "UNIT:E81");
     
     doTest("T30",
         "(Dispatch) 03/23/12 * 11:11:49 * HOUSE FIRE * * 64 RICHARDS LA,88   (NV) *  *  * ** X-STRT DARIEN RD - COVERED * NCC *  *  *  *  * Dispatch *",
         "DATE:03/23/12",
         "TIME:11:11:49",
         "CALL:HOUSE FIRE",
         "ADDR:64 RICHARDS LA",
         "MADDR:64 RICHARDS LN",
         "INFO:88   (NV) * X-STRT DARIEN RD - COVERED * NCC * Dispatch");
 
     doTest("T31",
         "911-CENTER:52C03 >FIRE ALARM COMMERCIAL/INDUST 101 MAPLE DR XS: ROUTE 322 ELK TWP MC ALARMS 6077869897 Map: Grids:G-10, Cad: 2012-0000012260",
         "CALL:52C03 >FIRE ALARM COMMERCIAL/INDUST",
         "ADDR:101 MAPLE DR",
         "X:ROUTE 322 ELK TWP MC ALARMS 6077869897",
         "MAP:G-10",
         "ID:2012-0000012260");
     
   }
   
 }
