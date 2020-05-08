 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDBaltimoreCountyParserTest extends BaseParserTest {
   
   public MDBaltimoreCountyParserTest() {
     setParser(new MDBaltimoreCountyParser(), "BALTIMORE COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Station 41 ALERT!!) UNCONSCIOUS DIABETIC\n046-04\nE461 E18 M465 IV415 CCM129 EMS5\n9341 EDWAY CI\n\nIncident Number (120911179)",
         "SRC:41",
         "CALL:UNCONSCIOUS DIABETIC",
         "MAP:046-04",
         "UNIT:E461 E18 M465 IV415 CCM129 EMS5",
         "ADDR:9341 EDWAY CI",
         "MADDR:9341 EDWAY CIR",
         "ID:120911179");
 
     doTest("T2",
         "(Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n041-21\nIV415\n300 CANTATA CT APT 302, 21136, MD\n\nIncident Number (120911036)",
         "SRC:41",
         "CALL:FALL INJURY NON DANGEROUS",
         "MAP:041-21",
         "UNIT:IV415",
         "ADDR:300 CANTATA CT",
         "APT:302",
         "CITY:21136",
         "ID:120911036");
 
     doTest("T3",
         "(Station 41 ALERT!!) BUILDING FIRE\n" +
         "056-06\n" +
         "BC21 E56 E412 E311 E401 T313 T404\n" +
         "11602 REISTERSTOWN RD, 21136, MD\n\n" +
         "Incident Number (120910568)",
 
         "SRC:41",
         "CALL:BUILDING FIRE",
         "MAP:056-06",
         "UNIT:BC21 E56 E412 E311 E401 T313 T404",
         "ADDR:11602 REISTERSTOWN RD",
         "CITY:21136",
         "ID:120910568");
 
     doTest("T4",
         "(Station 41 ALERT!!) UNKNOWN ILLNESS OR INJURY\n041-21\nA415 M56\n300 CANTATA CT APT 314, 21136, MD\n\nIncident Number (120910566)",
         "SRC:41",
         "CALL:UNKNOWN ILLNESS OR INJURY",
         "MAP:041-21",
         "UNIT:A415 M56",
         "ADDR:300 CANTATA CT",
         "APT:314",
         "CITY:21136",
         "ID:120910566");
 
     doTest("T5",
         "(Station 41 ALERT!!) CP  CLAMMY\n041-06\nM415 CCM139\n744 COCKEYS MILL RD, 21136, MD\n\nIncident Number (120910541)",
         "SRC:41",
         "CALL:CP  CLAMMY",
         "MAP:041-06",
         "UNIT:M415 CCM139",
         "ADDR:744 COCKEYS MILL RD",
         "CITY:21136",
         "ID:120910541");
 
     doTest("T6",
         "(Station 41 ALERT!!) STROKE  NOT ALERT\n049-10\nM395 M415\n14032 CUBA RD\n\nIncident Number (120901630)",
         "SRC:41",
         "CALL:STROKE  NOT ALERT",
         "MAP:049-10",
         "UNIT:M395 M415",
         "ADDR:14032 CUBA RD",
         "ID:120901630");
 
     doTest("T7",
         "(Station 41 ALERT!!) FALL  POSS DANGEROUS AREA\n056-06\nA415 IV465\n11722 REISTERSTOWN RD, 21136, MD\n\nIncident Number (120901441)",
         "SRC:41",
         "CALL:FALL  POSS DANGEROUS AREA",
         "MAP:056-06",
         "UNIT:A415 IV465",
         "ADDR:11722 REISTERSTOWN RD",
         "CITY:21136",
         "ID:120901441");
 
     doTest("T8",
         "(Station 41 ALERT!!) INJ-DANGROUS BDY AREA\n041-03\nM56 SU418\nSTOCKSDALE AV & POWER LINE, 21136, MD\n\nIncident Number (120901111)",
         "SRC:41",
         "CALL:INJ-DANGROUS BDY AREA",
         "MAP:041-03",
         "UNIT:M56 SU418",
         "ADDR:STOCKSDALE AV & POWER LINE",
         "MADDR:STOCKSDALE AVE & POWER LINE",
         "CITY:21136",
         "ID:120901111");
 
     doTest("T9",
         "(Station 41 ALERT!!) SICK SUBJECT NOT ALERT\n041-01\nM415 M56\n10 VILLAGE CENTER RD, 21136, MD\n\nIncident Number (120901433)",
         "SRC:41",
         "CALL:SICK SUBJECT NOT ALERT",
         "MAP:041-01",
         "UNIT:M415 M56",
         "ADDR:10 VILLAGE CENTER RD",
         "CITY:21136",
         "ID:120901433");
 
     doTest("T10",
         "(Station 41 ALERT!!) 1050PI\n041-01\nA415 M315 E413\nFRANKLIN ES @  33 COCKEYS MILL RD, 21136, MD\n\nIncident Number (120900817)",
         "SRC:41",
         "CALL:1050PI",
         "MAP:041-01",
         "UNIT:A415 M315 E413",
         "PLACE:FRANKLIN ES",
         "ADDR:33 COCKEYS MILL RD",
         "CITY:21136",
         "ID:120900817");
 
     doTest("T11",
         "(Station 41 ALERT!!) 1050PI ROLL-OVER\n041-20\nA415 M315 E413\nI795 & WESTMINSTER PK, 21136, MD\n\nIncident Number (120900400)",
         "SRC:41",
         "CALL:1050PI ROLL-OVER",
         "MAP:041-20",
         "UNIT:A415 M315 E413",
         "ADDR:I795 & WESTMINSTER PK",
         "MADDR:I 795 & WESTMINSTER PIKE",
         "CITY:21136",
         "ID:120900400");
 
     doTest("T12",
         "(Station 41 ALERT!!) TRBR-W/DIFF SPEAKING\n056-05\nIV415 M2 EMS5\n300 STONECASTLE AV, 21136, MD\n\nIncident Number (120911287)",
         "SRC:41",
         "CALL:TRBR-W/DIFF SPEAKING",
         "MAP:056-05",
         "UNIT:IV415 M2 EMS5",
         "ADDR:300 STONECASTLE AV",
         "MADDR:300 STONECASTLE AVE",
         "CITY:21136",
         "ID:120911287");
 
     doTest("T13",
         "(Station 41 ALERT!!) ABD PAIN  NOT ALERT\n056-08\nA315 IV415\n111 EMBLETON RD, 21117, MD\n\nIncident Number (120911354)",
         "SRC:41",
         "CALL:ABD PAIN  NOT ALERT",
         "MAP:056-08",
         "UNIT:A315 IV415",
         "ADDR:111 EMBLETON RD",
         "CITY:21117",
         "ID:120911354");
 
     doTest("T14",
         "(Station 41 ALERT!!) 1050PI ROLL-OVER\n041-08\nA415 M56 E412\n5 BRIAN DANIEL CT, 21136, MD\n\nIncident Number (120920186)",
         "SRC:41",
         "CALL:1050PI ROLL-OVER",
         "MAP:041-08",
         "UNIT:A415 M56 E412",
         "ADDR:5 BRIAN DANIEL CT",
         "CITY:21136",
         "ID:120920186");
 
     doTest("T15",
         "(Station 41 ALERT!!) BLEEDING SUBJ W/TRBR\n056-06\nM415 M19\n11 WOODENBRIDGE CT, 21136, MD\n\nIncident Number (120921328)",
         "SRC:41",
         "CALL:BLEEDING SUBJ W/TRBR",
         "MAP:056-06",
         "UNIT:M415 M19",
         "ADDR:11 WOODENBRIDGE CT",
         "CITY:21136",
         "ID:120921328");
 
     doTest("T16",
         "(Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n041-21\nA415 M56\n300 CANTATA CT APT 302, 21136, MD\n\nIncident Number (120921264)",
         "SRC:41",
         "CALL:FALL INJURY NON DANGEROUS",
         "MAP:041-21",
         "UNIT:A415 M56",
         "ADDR:300 CANTATA CT",
         "APT:302",
         "CITY:21136",
         "ID:120921264");
 
     doTest("T17",
         "(Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n056-04\nE56 A415 IV505\n828 IVYDALE AV, 21136, MD\n\nIncident Number (120951716)",
         "SRC:41",
         "CALL:FALL INJURY NON DANGEROUS",
         "MAP:056-04",
         "UNIT:E56 A415 IV505",
         "ADDR:828 IVYDALE AV",
         "MADDR:828 IVYDALE AVE",
         "CITY:21136",
         "ID:120951716");
 
     doTest("T18",
         "(Station 41 ALERT!!) LOCK OUT DETAIL/NON EMERG\n041-23\nE412\n505 DEACON BROOK CI, 21136, MD\n\nIncident Number (120980427)",
         "SRC:41",
         "CALL:LOCK OUT DETAIL/NON EMERG",
         "MAP:041-23",
         "UNIT:E412",
         "ADDR:505 DEACON BROOK CI",
         "MADDR:505 DEACON BROOK CIR",
         "CITY:21136",
         "ID:120980427");
 
     doTest("T19",
         "(Station 41 ALERT!!) BUILDING FIRE\nCAR-43\nE432 S414 T404 E412\n643 LUCABAUGH MILL RD\n\nIncident Number (120971868)",
         "SRC:41",
         "CALL:BUILDING FIRE",
         "MAP:CAR-43",
         "UNIT:E432 S414 T404 E412",
         "ADDR:643 LUCABAUGH MILL RD",
         "ID:120971868");
 
     doTest("T20",
         "(Station 41 ALERT!!) EMOT ILL CONS/BREATHING\n041-23\nM415 M315\n400 VALLEY MEADOW CI APT B1, 21136, MD\n\nIncident Number (120990430)",
         "SRC:41",
         "CALL:EMOT ILL CONS/BREATHING",
         "MAP:041-23",
         "UNIT:M415 M315",
         "ADDR:400 VALLEY MEADOW CI",
         "MADDR:400 VALLEY MEADOW CIR",
         "APT:B1",
         "CITY:21136",
         "ID:120990430");
 
     doTest("T21",
         "(Station 41 ALERT!!) FAINTING  ALERT W/TRBR\n041-23\nM415 M56\n406 DEACON BROOK CI, 21136, MD\n\nIncident Number (120990933)",
         "SRC:41",
         "CALL:FAINTING  ALERT W/TRBR",
         "MAP:041-23",
         "UNIT:M415 M56",
         "ADDR:406 DEACON BROOK CI",
         "MADDR:406 DEACON BROOK CIR",
         "CITY:21136",
         "ID:120990933");
   }
   
   public static void main(String[] args) {
     new MDBaltimoreCountyParserTest().generateTests("T1");
   }
 }
