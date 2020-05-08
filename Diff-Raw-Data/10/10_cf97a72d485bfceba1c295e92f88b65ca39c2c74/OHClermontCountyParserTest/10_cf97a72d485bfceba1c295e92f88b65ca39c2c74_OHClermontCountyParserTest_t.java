 package net.anei.cadpage.parsers.OH;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class OHClermontCountyParserTest extends BaseParserTest {
   
   public OHClermontCountyParserTest() {
     setParser(new OHClermontCountyParser(), "CLERMONT COUNTY", "OH");
   }
   
   @Test
   public void testBad() {
     doBadTest("EVENT: FSTRUC LOC:966 E LEGENDARY RUN Cad: 2011-0000069848 DSP >20:41 Case: 0000000534 Disp: ER >20:43 FG6 CLR >20:49");
     doBadTest("  / EVENT: 09E01 LOC:1286 PEBBLE BROOKE TRL Cad: 2011-0000055550 DSP >13:22 ER >13:24 CLR >13:26\n");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "FSTRUC>STRUCTURE FIRE 1890 SR 232 XS: LAUREL POINT ISABEL RD LAUREL BUTLER, SHAWN Map: Grids:, Cad: 2011-0000014744",
         "CALL:STRUCTURE FIRE",
         "ADDR:1890 SR 232",
         "X:LAUREL POINT ISABEL RD",
         "CITY:LAUREL",
         "NAME:BUTLER, SHAWN",
         "ID:2011-0000014744");
 
     doTest("T2",
         "MED >MED GENERIC DO NOT DELETE 2458 SR 756 XS: SR 743 WASHINGTON TOWNSHIP BETTLES,LINDA Map: Grids:, Cad: 2011-0000015125",
         "CALL:MED GENERIC DO NOT DELETE",
         "ADDR:2458 SR 756",
         "X:SR 743",
         
         "CITY:WASHINGTON TOWNSHIP",
         "NAME:BETTLES,LINDA",
         "ID:2011-0000015125");
 
     doTest("T3",
         "FDUMP >DUMPSTER FIRE 2601 WILDWOOD LN XS: SR 222 TATE TOWNSHIP WIRELESS-VERIZON(XYP) Map: Grids:, Cad: 2011-0000015892",
         "CALL:DUMPSTER FIRE",
         "ADDR:2601 WILDWOOD LN",
         "X:SR 222",
         "CITY:TATE TOWNSHIP",
         "NAME:WIRELESS-VERIZON(XYP)",
         "ID:2011-0000015892");
 
     doTest("T4",
         "06D02 >BREATHING PROBLEMS 2458 SR 756 XS: SR 743 WASHINGTON TOWNSHIP LESTER,CURTIS Map: Grids:, Cad: 2011-0000016117",
         "CALL:BREATHING PROBLEMS",
         "ADDR:2458 SR 756",
         "X:SR 743",
         "CITY:WASHINGTON TOWNSHIP",
         "NAME:LESTER,CURTIS",
         "ID:2011-0000016117");
 
     doTest("T5",
         "29B01 >TRAFF OR TRANSPT ACC/MVA W/INJ BETHEL HYGIENE RD&amp;SR 133 XS: SR 133 TATE TOWNSHIP Cad: 2011-0000016983",
         "CALL:TRAFF OR TRANSPT ACC/MVA W/INJ BETHEL",
         "ADDR:HYGIENE RD & SR 133",
         "X:SR 133",
         "CITY:TATE TOWNSHIP",
         "ID:2011-0000016983");
 
     doTest("T6",
         "29B01 >TRAFF OR TRANSPT ACC/MVA W/INJ LAUREL PT ISABEL RD&BECKELHYMER RD XS: BECKELHYMER RD WASHINGTON TOWNSHIP HAYMOND,LINDA S Cad: 2011-0000017231",
         "CALL:TRAFF OR TRANSPT ACC/MVA W/INJ LAUREL PT",
         "ADDR:ISABEL RD & BECKELHYMER RD",
         "X:BECKELHYMER RD",
         "CITY:WASHINGTON TOWNSHIP",
         "NAME:HAYMOND,LINDA S",
         "ID:2011-0000017231");
 
     doTest("T7",
         "FWIRES>WIRES DOWN E OSBORNE ST&N EAST ST XS: N EAST ST BETHEL JEFF W/RUMPKE Cad: 2011-0000017232",
         "CALL:WIRES DOWN",
         "ADDR:E OSBORNE ST & N EAST ST",
         "X:N EAST ST",
         "CITY:BETHEL",
         "NAME:JEFF W / RUMPKE",
         "ID:2011-0000017232");
 
     doTest("T8",
         "FSTRUC>STRUCTURE FIRE 966 E LEGENDARY RUN XS: ABERDEEN RIDGE PIERCE TOWNSHIP HANSBAUER,DAVID & LINDA Map: Grids:, Cad: 2011-0000069848",
         "CALL:STRUCTURE FIRE",
         "ADDR:966 E LEGENDARY RUN",
         "X:ABERDEEN RIDGE",
         "CITY:PIERCE TOWNSHIP",
         "NAME:HANSBAUER,DAVID & LINDA",
         "ID:2011-0000069848");
 
     doTest("T9",
         "04B01 >ASSAULT/SEXUAL ASSAULT 5728 GAVEY WAY XS: SR 131 WAYNE TOWNSHIP JEFFERS,JORDAN Map: Grids:, Cad: 2011-0000077041",
         "CALL:ASSAULT/SEXUAL ASSAULT",
         "ADDR:5728 GAVEY WAY",
         "X:SR 131",
         "CITY:WAYNE TOWNSHIP",
         "NAME:JEFFERS,JORDAN",
         "ID:2011-0000077041");
 
     doTest("T10",
         "FALARM>FIRE ALARM 1466 HWY 50 XS: BARRETT RD STONELICK TOWNSHIP CINTAS FIRE PROTECTION-55 Map: Grids:, Cad: 2011-0000077411",
         "CALL:FIRE ALARM",
         "ADDR:1466 HWY 50",
         "X:BARRETT RD",
         "CITY:STONELICK TOWNSHIP",
         "NAME:CINTAS FIRE PROTECTION-55",
         "ID:2011-0000077411");
 
     doTest("T11",
         "MED >MED GENERIC DO NOT DELETE 2800 LINK SIDE DR Apt: 4 Bldg XS: ST ANDREWS DR PIERCE TOWNSHIP WETZELL,CONNIE Map: Grids:, Cad: 2011-0000082440",
         "CALL:MED GENERIC DO NOT DELETE",
         "ADDR:2800 LINK SIDE DR",
         "APT:4 Bldg",
         "X:ST ANDREWS DR",
         "CITY:PIERCE TOWNSHIP",
         "NAME:WETZELL,CONNIE",
         "ID:2011-0000082440");

    doTest("T12",
        "31C02 >UNCONSCIOUS/FAINTING (NEAR) 5877 WOLFPEN PLEASANT HILL RD Apt: 316 Bldg XS: TRAVERSE CREEK DR MIAMI TWP RYAN, CHRISTY Map: Grids:, Cad: 2012-0000002524",
        "CALL:UNCONSCIOUS/FAINTING (NEAR)",
        "ADDR:5877 WOLFPEN PLEASANT HILL RD",
        "APT:316 Bldg",
        "X:TRAVERSE CREEK DR",
        "CITY:MIAMI TWP",
        "NAME:RYAN, CHRISTY",
        "ID:2012-0000002524");
     
   }
   
   public static void main(String[] args) {
     new OHClermontCountyParserTest().generateTests("T11", "CALL ADDR APT X CITY NAME ID");
   }
 }
