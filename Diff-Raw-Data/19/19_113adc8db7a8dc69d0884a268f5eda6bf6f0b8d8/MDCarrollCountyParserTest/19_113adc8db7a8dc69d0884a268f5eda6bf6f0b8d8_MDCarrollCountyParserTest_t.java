 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDCarrollCountyParserTest extends BaseParserTest {
   
   public MDCarrollCountyParserTest() {
     setParser(new MDCarrollCountyParser(), "CARROLL COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "(Co12) [!] CT:UC 6513 CARROLL HIGHLANDS RD SYKE BOX:1211 DUE:X127 E123 [66]",
         "SRC:Co12",
         "CALL:UC",
         "ADDR:6513 CARROLL HIGHLANDS RD",
         "CITY:SYKESVILLE",
         "BOX:1211",
         "UNIT:X127 E123");
 
     doTest("T2",
         "(Co12) [!] CT:BLS SHOPPERS FOOD WHSE @6300 GEORGETOWN BLVD SYKE BOX:1224 DUE:X128 [69]",
         "SRC:Co12",
         "CALL:BLS",
         "PLACE:SHOPPERS FOOD WHSE",
         "ADDR:6300 GEORGETOWN BLVD",
         "CITY:SYKESVILLE",
         "BOX:1224",
         "UNIT:X128");
 
     doTest("T3",
         "(Co12) [!] CT:BLS 5584 COMPTON LA SYKE BOX:1214 DUE:X129 [70]",
         "SRC:Co12",
         "CALL:BLS",
         "ADDR:5584 COMPTON LA",
         "CITY:SYKESVILLE",
         "BOX:1214",
         "UNIT:X129");
 
     doTest("T4",
         "[Co12] [!] CT:INVEST KABIK CT / RAILROAD DR E WOOD BOX:1204 DUE:B125 [68]",
         "SRC:Co12",
         "CALL:INVEST",
         "ADDR:KABIK CT & RAILROAD DR E",
         "CITY:WOODBINE",
         "BOX:1204",
         "UNIT:B125");
 
     doTest("T5",
         "(Co12) [!] CT:ALS 1820 VINCENZA DR @MARVIN GARDENS APT C SYKE BOX:1232 DUE:X128 [71]",
         "SRC:Co12",
         "CALL:ALS",
         "ADDR:1820 VINCENZA DR",
         "PLACE:MARVIN GARDENS",
         "APT:C",
         "CITY:SYKESVILLE",
         "BOX:1232",
         "UNIT:X128");
 
     doTest("T6",
         "[Co14] [!] CT:SERVICE 3215 HOOPERS DELIGHT RD NWIN BOX:1411 DUE:U14 [18]",
         "SRC:Co14",
         "CALL:SERVICE",
         "ADDR:3215 HOOPERS DELIGHT RD",
         "CITY:NEW WINDSOR",
         "BOX:1411",
         "UNIT:U14");
 
     doTest("T7",
         "(Co14) [!] CT:BLS 5401 ENTERPRISE ST ; ALLEGRA PRINT SYKE BOX:1416 DUE:X149 [33]",
         "SRC:Co14",
         "CALL:BLS",
         "ADDR:5401 ENTERPRISE ST",
         "PLACE:ALLEGRA PRINT",
         "CITY:SYKESVILLE",
         "BOX:1416",
         "UNIT:X149");
 
     doTest("T8",
         "(Co2) [!] CT:MA BOX 60-18 19907 YORK RD VCR TG44 BOX:BC DUE:T2 [41]",
         "SRC:Co2",
         "CALL:MA - VCR",
         "BOX:60-18",
         "ADDR:19907 YORK RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:T2",
         "INFO:TG44");
 
     doTest("T9",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:ET24 BE432 E23 BE422 E91 TO3 BET431 ET44 BTS434 X29 CS4 [15]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:ET24 BE432 E23 BE422 E91 TO3 BET431 ET44 BTS434 X29 CS4");
 
     doTest("T10",
         "[Co 2] [!] CT:MA 42-2 RT 30/EMORY RD RESCUE TG 46 BOX:BC DUE:E23 [39]",
         "SRC:Co 2",
         "CALL:MA - RESCUE",
         "BOX:42-2",
         "ADDR:RT 30 & EMORY RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG46",
         "UNIT:E23");
 
     doTest("T11",
         "[Co 2] [!] CT:ALS 3900 SHILOH AV HAMP BOX:0218 DUE:BM435 FR2 [44]",
         "SRC:Co 2",
         "CALL:ALS",
         "BOX:0218",
         "ADDR:3900 SHILOH AV",
        "MADDR:3900 SHILOH AVE",
         "CITY:HAMPSTEAD",
         "UNIT:BM435 FR2");
 
     doTest("T12",
         "[Co 2] [!] CT:ALS 1211 N MAIN ST @RESIDENCE@HAMP SCHOL APT 212 HAMP BOX:0220 DUE:X29 FR2 [43]",
         "SRC:Co 2",
         "CALL:ALS",
         "BOX:0220",
         "PLACE:RESIDENCE@HAMP SCHOL",
         "ADDR:1211 N MAIN ST",
         "APT:212",
         "CITY:HAMPSTEAD",
         "UNIT:X29 FR2");
 
     doTest("T13",
         "[Co 2] [!] CT:ALS 3993 BRIDGE FIELD DR HAMP BOX:0208 DUE:X49 FR2 [32]",
         "SRC:Co 2",
         "CALL:ALS",
         "BOX:0208",
         "ADDR:3993 BRIDGE FIELD DR",
         "CITY:HAMPSTEAD",
         "UNIT:X49 FR2");
 
     doTest("T14",
         "[Co 2] [!] CT:ALS 1835 GABLEHAMMER RD WEST BOX:0212 DUE:E43 [29]",
         "SRC:Co 2",
         "CALL:ALS",
         "BOX:0212",
         "ADDR:1835 GABLEHAMMER RD",
         "CITY:WESTMINISTER",
         "UNIT:E43");
 
     doTest("T17",
         "[Co 2] [!] CT:UC 4800 MILLERS STATION RD HAMP BOX:0214 DUE:X29 E23 [91]",
         "SRC:Co 2",
         "CALL:UC",
         "BOX:0214",
         "ADDR:4800 MILLERS STATION RD",
         "CITY:HAMPSTEAD",
         "UNIT:X29 E23");
 
     doTest("T18",
         "[Co 2] [!] CT:BLS 5012 MILLERS STATION RD HAMP BOX:0214 DUE:X49 FR2 [89]",
         "SRC:Co 2",
         "CALL:BLS",
         "BOX:0214",
         "ADDR:5012 MILLERS STATION RD",
         "CITY:HAMPSTEAD",
         "UNIT:X49 FR2");
 
     doTest("T19",
         "[Co 2] [!] CT:BLS 3825 SHILOH AV @HIGHFIELD APTS APT 2 HAMP BOX:0218 DUE:BM435 FR2 [88]",
         "SRC:Co 2",
         "CALL:BLS",
         "BOX:0218",
         "PLACE:HIGHFIELD APTS",
         "ADDR:3825 SHILOH AV",
        "MADDR:3825 SHILOH AVE",
         "APT:2",
         "CITY:HAMPSTEAD",
         "UNIT:BM435 FR2");
 
     doTest("T20",
         "[Co 2] [!] CT:VC HAMPSTEAD PD @1112 S MAIN ST HAMP BOX:0218 DUE:X29 E23 [42]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0218",
         "PLACE:HAMPSTEAD PD",
         "ADDR:1112 S MAIN ST",
         "CITY:HAMPSTEAD",
         "UNIT:X29 E23");
 
     doTest("T21",
         "[Co 2] [!] CT:VC 716 HOUCKSVILLE RD HAMP BOX:0203 DUE:X29 E23 [35]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0203",
         "ADDR:716 HOUCKSVILLE RD",
         "CITY:HAMPSTEAD",
         "UNIT:X29 E23");
 
     doTest("T22",
         "[Co 2] [!] CT:VC 4652 MARKSMAN CT HAMP BOX:0217 DUE:X49 [34]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0217",
         "ADDR:4652 MARKSMAN CT",
         "CITY:HAMPSTEAD",
         "UNIT:X49");
 
     doTest("T23",
         "[Co 2] [!] CT:VC 4652 MARKSMAN CT HAMP BOX:0217 DUE:X29 E23 [33]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0217",
         "ADDR:4652 MARKSMAN CT",
         "CITY:HAMPSTEAD",
         "UNIT:X29 E23");
 
     doTest("T24",
         "[Co 2] [!] CT:VC HAMPSTEAD BYP / RT 482 HAMP BOX:0260 DUE:E43 [01]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0260",
         "ADDR:HAMPSTEAD BYP & RT 482",
         "CITY:HAMPSTEAD",
         "UNIT:E43");
 
     doTest("T25",
         "[Co 2] [!] CT:VC PANTHER DR / RT 482 HAMP BOX:0208 DUE:X29 [99]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0208",
         "ADDR:PANTHER DR & RT 482",
         "CITY:HAMPSTEAD",
         "UNIT:X29");
 
     doTest("T26",
         "[Co 2] [!] CT:VC RT 30 BYP / HANOVER PKE / SOU HAMP BOX:0261 DUE:X29 BM435 E23 BE432 [94]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0261",
         "ADDR:RT 30 BYP & HANOVER PKE & SOU",
         "CITY:HAMPSTEAD",
         "UNIT:X29 BM435 E23 BE432");
 
     doTest("T27",
         "[Co 2] [!] CT:VC RT 30 / / WOLF HILL DR HAMP BOX:0211 DUE:BM435 FR2 E23 BE432 [86]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0211",
         "ADDR:RT 30 & WOLF HILL DR",
         "CITY:HAMPSTEAD",
         "UNIT:BM435 FR2 E23 BE432");
 
     doTest("T28",
         "[Co 2] [!] CT:VC MAPLE GROVE RD / RT 30 MANC BOX:0411 DUE:E23 [80]",
         "SRC:Co 2",
         "CALL:VC",
         "BOX:0411",
         "ADDR:MAPLE GROVE RD & RT 30",
         "CITY:MANCHESTER",
         "UNIT:E23");
 
     doTest("T29",
         "[Co 2] [!] CT:ATR 2360 CONVEY DR MANC BOX:0416 DUE:T2 [14]",
         "SRC:Co 2",
         "CALL:ATR",
         "BOX:0416",
         "ADDR:2360 CONVEY DR",
         "CITY:MANCHESTER",
         "UNIT:T2");
 
     doTest("T30",
         "[Co 2] [!] CT:BF 1231 TECH CT WEST BOX:0307 DUE:E33 E61 E91 E101 RS6 TO3 T2 X37 CS9 [31]",
         "SRC:Co 2",
         "CALL:BF",
         "BOX:0307",
         "ADDR:1231 TECH CT",
         "CITY:WESTMINISTER",
         "UNIT:E33 E61 E91 E101 RS6 TO3 T2 X37 CS9");
 
     doTest("T31",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:BTT446 TT6 [21]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:BTT446 TT6");
 
     doTest("T32",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:X99 [20]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:X99");
 
     doTest("T33",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:ET73 [19]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:ET73");
 
     doTest("T34",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:BE493 E43 RS9 BM435 AU7 BS414 [18]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:BE493 E43 RS9 BM435 AU7 BS414");
 
     doTest("T35",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:E92 BT404 YT49 BR95 [17]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:E92 BT404 YT49 BR95");
 
     doTest("T36",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:CS9 [16]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:CS9");
 
     doTest("T37",
         "[Co 2] [!] CT:HF 1216 WOODLAND CT HAMP BOX:0216 DUE:ET24 BE432 E23 BE422 E91 TO3 BET431 ET44 BTS434 X29 CS4 [15]",
         "SRC:Co 2",
         "CALL:HF",
         "BOX:0216",
         "ADDR:1216 WOODLAND CT",
         "CITY:HAMPSTEAD",
         "UNIT:ET24 BE432 E23 BE422 E91 TO3 BET431 ET44 BTS434 X29 CS4");
 
     doTest("T38",
         "[Co 2] [!] CT:CHIM 4224 UPPER BECKLEYSVILLE RD HAMP BOX:0207 DUE:E23 E43 T2 X29 [97]",
         "SRC:Co 2",
         "CALL:CHIM",
         "BOX:0207",
         "ADDR:4224 UPPER BECKLEYSVILLE RD",
         "CITY:HAMPSTEAD",
         "UNIT:E23 E43 T2 X29");
 
     doTest("T39",
         "[Co 2] [!] CT:BRUSH 100 LEES MILL RD HAMP BOX:0213 DUE:BE422 BR95 [26]",
         "SRC:Co 2",
         "CALL:BRUSH",
         "BOX:0213",
         "ADDR:100 LEES MILL RD",
         "CITY:HAMPSTEAD",
         "UNIT:BE422 BR95");
 
     doTest("T40",
         "[Co 2] [!] CT:BRUSH 100 LEES MILL RD HAMP BOX:0213 DUE:E23 BE432 B25 BB436 [25]",
         "SRC:Co 2",
         "CALL:BRUSH",
         "BOX:0213",
         "ADDR:100 LEES MILL RD",
         "CITY:HAMPSTEAD",
         "UNIT:E23 BE432 B25 BB436");
 
     doTest("T41",
         "[Co 2] [!] CT:LOCAL 1311 BRILHART DR HAMP BOX:0221 DUE:E23 [56]",
         "SRC:Co 2",
         "CALL:LOCAL",
         "BOX:0221",
         "ADDR:1311 BRILHART DR",
         "CITY:HAMPSTEAD",
         "UNIT:E23");
 
     doTest("T42",
         "[Co 2] [!] CT:ALARM 1 4160 GIMLET DR HAMP BOX:0206 DUE:E23 [10]",
         "SRC:Co 2",
         "CALL:ALARM",
         "BOX:0206",
         "ADDR:1 4160 GIMLET DR",
         "CITY:HAMPSTEAD",
         "UNIT:E23");
 
     doTest("T43",
         "[Co 2] [!] CT:TRANS CO 2 @1341 N MAIN ST HAMP BOX:0208 DUE:X99 [23]",
         "SRC:Co 2",
         "CALL:TRANS",
         "BOX:0208",
         "PLACE:CO 2",
         "ADDR:1341 N MAIN ST",
         "CITY:HAMPSTEAD",
         "UNIT:X99");
 
     doTest("T44",
         "[Co 2] [!] CT:TRANS CO 4 @3209 MAIN ST MANC BOX:0404 DUE:TR1 E23 [13]",
         "SRC:Co 2",
         "CALL:TRANS",
         "BOX:0404",
         "PLACE:CO 4",
         "ADDR:3209 MAIN ST",
         "CITY:MANCHESTER",
         "UNIT:TR1 E23");
 
     doTest("T45",
         "[Co 2] [!] CT:SERVICE DAVE RILL RD / NORTHSIDE ST HAMP BOX:0215 DUE:B25 [05]",
         "SRC:Co 2",
         "CALL:SERVICE",
         "BOX:0215",
         "ADDR:DAVE RILL RD & NORTHSIDE ST",
         "CITY:HAMPSTEAD",
         "UNIT:B25");
 
     doTest("T46",
         "[Co 2] [!] CT:SERVICE PANTHER DR / RT 482 HAMP BOX:0208 DUE:B25 [98]",
         "SRC:Co 2",
         "CALL:SERVICE",
         "BOX:0208",
         "ADDR:PANTHER DR & RT 482",
         "CITY:HAMPSTEAD",
         "UNIT:B25");
 
     doTest("T47",
         "[Co 2] [!] CT:PA 4862 UPPER BECKLEYSVILLE RD HAMP BOX:0207 DUE:ET24 [46]",
         "SRC:Co 2",
         "CALL:PA",
         "BOX:0207",
         "ADDR:4862 UPPER BECKLEYSVILLE RD",
         "CITY:HAMPSTEAD",
         "UNIT:ET24");
 
     doTest("T48",
         "[Co 2] [!] CT:MA BOX 60-18 19907 YORK RD VCR TG44 BOX:BC DUE:T2 [41]",
         "SRC:Co 2",
         "CALL:MA - VCR",
         "BOX:60-18",
         "ADDR:19907 YORK RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG44",
         "UNIT:T2");
 
     doTest("T49",
         "[Co 2] [!] CT:MA 42-2 RT 30/ EMORY RD RESCUE TG 46 BOX:BC DUE:E23 [39]",
         "SRC:Co 2",
         "CALL:MA - RESCUE",
         "BOX:42-2",
         "ADDR:RT 30 & EMORY RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG46",
         "UNIT:E23");
 
     doTest("T50",
         "[Co 2] [!] CT:MA 42-2 RT 30/ EMORY RD RESCUE TG 46 BOX:BC DUE:T2 [38]",
         "SRC:Co 2",
         "CALL:MA - RESCUE",
         "BOX:42-2",
         "ADDR:RT 30 & EMORY RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG46",
         "UNIT:T2");
 
     doTest("T51",
         "[Co 2] [!] CT:MA 43-8 4607 MT CARMEL RD CARDIAC ARREST BOX:BC DUE:E23 [36]",
         "SRC:Co 2",
         "CALL:MA - CARDIAC ARREST",
         "BOX:43-8",
         "ADDR:4607 MT CARMEL RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:E23");
 
     doTest("T52",
         "[Co 2] [!] CT:MA BOX 43-15 4313 CHURCH RD UNCONSCIOUS PERSON BOX:BC DUE:X29 E23 [30]",
         "SRC:Co 2",
         "CALL:MA - UNCONSCIOUS PERSON",
         "BOX:43-15",
         "ADDR:4313 CHURCH RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:X29 E23");
     
     doTest("T53",
         "[Co 2] [!] CT:MA 43-6 3948 MT ZION RD / DOVER RD TREE ON FIRE TG 43 BOX:BC DUE:E23 [84]",
         "SRC:Co 2",
         "CALL:MA - TREE ON FIRE",
         "BOX:43-6",
         "ADDR:3948 MT ZION RD",
         "X:DOVER RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG43",
         "UNIT:E23");
   
     doTest("T54",
         "[Co 2] [!] CT:MA 44-35 3323 MT CARMEL RD C/S HUNTERGREEN RD SMOKE ALARM TG 41 BOX:BC DUE:E23 [81]",
         "SRC:Co 2",
         "CALL:MA - SMOKE ALARM",
         "BOX:44-35",
         "ADDR:3323 MT CARMEL RD",
         "X:HUNTERGREEN RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG41",
         "UNIT:E23");
   
     doTest("T55",
         "[Co 2] [!] CT:MA 45-20 3 TIMBER SHED CT / SLAB BRIDGE HOUSE FIRE BOX:BC DUE:T2 [78]",
         "SRC:Co 2",
         "CALL:MA - / SLAB BRIDGE HOUSE FIRE",
         "BOX:45-20",
         "ADDR:3 TIMBER SHED CT",
         "CITY:BALTIMORE COUNTY",
         "UNIT:T2");
   
     doTest("T56",
         "[Co 2] [!] CT:MA 43-1 18110 FALLS RD VCR BOX:BC DUE:T2 E23 X28 [76]",
         "SRC:Co 2",
         "CALL:MA - VCR",
         "BOX:43-1",
         "ADDR:18110 FALLS RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:T2 E23 X28");
   
     doTest("T57",
         "[Co 2] [!] CT:MA BC 43-15 20015 GRAVE RUN RD / HOFFMANVILLE RD TRAILER BOX:BC DUE:E23 RE72 T2 [72]",
         "SRC:Co 2",
         "CALL:MA - TRAILER",
         "BOX:43-15",
         "ADDR:20015 GRAVE RUN RD",
         "X:HOFFMANVILLE RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:E23 RE72 T2");
   
     doTest("T58",
         "[Co 2] [!] CT:MA 43-15 19931 GRAVE RUN RD INJ AFTER FALL TG 43 BOX:BC DUE:E23 [57]",
         "SRC:Co 2",
         "CALL:MA - INJ AFTER FALL",
         "BOX:43-15",
         "ADDR:19931 GRAVE RUN RD",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG43",
         "UNIT:E23");
   
     doTest("T59",
         "[Co 2] [!] CT:MA 60-12 18946 MIDDLETOWN RD / FOX CHASE CT SHED TG 44 BOX:BC DUE:T2 [54]",
         "SRC:Co 2",
         "CALL:MA - SHED",
         "BOX:60-12",
         "ADDR:18946 MIDDLETOWN RD",
         "X:FOX CHASE CT",
         "CITY:BALTIMORE COUNTY",
         "INFO:TG44",
         "UNIT:T2");
   
     doTest("T60",
         "[Co 2] [!] CT:MA BOWIE STATION 14901 HEALTH CTR DR BOX:MC DUE:E23 [45]",
         "SRC:Co 2",
         "CALL:MA - ",
         "PLACE:BOWIE STATION",
         "ADDR:14901 HEALTH CTR DR",
         "CITY:MONTGOMERY COUNTY",
         "UNIT:E23");
   
     doTest("T61",
         "[Co 2]  [!] CT:MA 43-2 16106 DARK HOLLOW RD / TRENTON RD HELO L/Z BOX:BC DUE:E23 [36]",
         "SRC:Co 2",
         "CALL:MA - HELO L / Z",
         "BOX:43-2",
         "ADDR:16106 DARK HOLLOW RD",
         "X:TRENTON RD",
         "CITY:BALTIMORE COUNTY",
         "UNIT:E23");
   
     doTest("T62",
         "[Co 2] [!] CT:MA 43-13 3907 BECKELYSVILLE RD / ABRAMS CT FIRE ALARM BOX:BC DUE:E23 [35]",
         "SRC:Co 2",
         "CALL:MA - FIRE ALARM",
         "BOX:43-13",
         "ADDR:3907 BECKELYSVILLE RD",
         "X:ABRAMS CT",
         "CITY:BALTIMORE COUNTY",
         "UNIT:E23");
   }
   
  @Test
  public void testParser2() {

    doTest("T1",
        
        "Co14 / [!] CT:VC KLEE MILL RD S / LIBERTY RD W SYKE BOX:1416 DUE:X149 E141 [08]\n\n",
        "SRC:Co14",
        "CALL:VC",
        "BOX:1416",
        "ADDR:KLEE MILL RD S & LIBERTY RD W",
        "CITY:SYKESVILLE",
        "UNIT:X149 E141");

  }
  
   public static void main(String[] args) {
     new MDCarrollCountyParserTest().generateTests("T53");
   }
 }
