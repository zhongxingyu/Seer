 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCCumberlandCountyParserTest extends BaseParserTest {
   
   public NCCumberlandCountyParserTest() {
     setParser(new NCCumberlandCountyParser(), "CUMBERLAND COUNTY", "NC");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "[S] DEERFIELD (N)C4;01/28/2011 13:19:07;POSS DANGEROUS BODY AREA FALL;ST22;441 WAPITI DR;WILDERNESS DR",
         "PLACE:DEERFIELD",
         "UNIT:C4",
         "CALL:POSS DANGEROUS BODY AREA FALL",
         "SRC:ST22",
         "ADDR:441 WAPITI DR",
         "X:WILDERNESS DR");
 
     doTest("T2",
         "[S] SPRING LAKE (PLAT BOOK 11, PAGE 3)+/- (N)C4;01/27/2011 22:09:28;CLAMMY BREATHING PROBLEMS;ST22;115-401 LAKE AVE;S FIRST ST;GATEWAY IN",
         "PLACE:GATEWAY IN",
         "UNIT:C4",
         "CALL:CLAMMY BREATHING PROBLEMS",
         "SRC:ST22",
         "ADDR:115-401 LAKE AVE",
         "X:S FIRST ST");
 
     doTest("T3",
         "[S]  (N)C4;01/27/2011 17:08:27;UNKNOWN STATUS TRAFFIC ACCIDEN;ST22;670 LILLINGTON HWY;HOLLAND DR;WALMART",
         "PLACE:WALMART",
         "UNIT:C4",
         "CALL:UNKNOWN STATUS TRAFFIC ACCIDEN",
         "SRC:ST22",
         "ADDR:670 LILLINGTON HWY",
         "X:HOLLAND DR");
 
     doTest("T4",
         "[S] OVERHILLS PARK (N)C4;01/27/2011 15:20:35;ABNORMAL BREATHING SICK PERSON;ST22;3110 HUNTLEY ST;ROBBIE CIR",
         "PLACE:OVERHILLS PARK",
         "UNIT:C4",
         "CALL:ABNORMAL BREATHING SICK PERSON",
         "SRC:ST22",
         "ADDR:3110 HUNTLEY ST",
         "X:ROBBIE CIR");
 
     doTest("T5",
         "[S] R G CREECH (N)C4;01/27/2011 13:23:42;WIRES DOWN (NO SMOKE OR ARC);ST22;200 RUPE ST;SCARBOROUGH ST",
         "PLACE:R G CREECH",
         "UNIT:C4",
         "CALL:WIRES DOWN (NO SMOKE OR ARC)",
         "SRC:ST22",
         "ADDR:200 RUPE ST",
         "X:SCARBOROUGH ST");
 
     doTest("T6",
         "CAD:(S) (N)C4;03/04/2011 21:18:43;UNKNOWN OUTSIDE FIRE;ST22;2700-BLK BRAGG BLVD",
         "UNIT:C4",
         "CALL:UNKNOWN OUTSIDE FIRE",
         "SRC:ST22",
         "ADDR:2700-BLK BRAGG BLVD");
 
     doTest("T7",
         "CAD:DIST: 174.69 FT (S)UPTON TYSON (BELRIDGR DR) (N)C10;05/20/2011 08:34:01;CLAMMY CHEST PAIN;ST18;3570 LACEWOOD CT;NC HWY 87 S",
         "PLACE:UPTON TYSON (BELRIDGR DR)",
         "UNIT:C10",
         "CALL:CLAMMY CHEST PAIN",
         "SRC:ST18",
         "ADDR:3570 LACEWOOD CT",
         "X:NC HWY 87 S");
 
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(Station 13) 2011 07:30:53;ALERT WITH ABNORMAL BREATHING;ST13;2881 STRICKLAND BRIDGE RD;CENTURY CIR",
         "CALL:ALERT WITH ABNORMAL BREATHING",
         "SRC:ST13",
         "ADDR:2881 STRICKLAND BRIDGE RD",
         "X:CENTURY CIR");
 
    doTest("T2",
        "(Station 13) 08/2011 18:00:08;RESIDENTIAL ALARM;0431,2131,ST13,ST19;5681 CAMDEN RD;STATE ST",
        "CALL:RESIDENTIAL ALARM",
        "SRC:0431,2131,ST13,ST19",
        "ADDR:5681 CAMDEN RD",
        "X:STATE ST");

   }
   
 
   public static void main(String[] args) {
    new NCCumberlandCountyParserTest().generateTests("T2");
   }
 }
