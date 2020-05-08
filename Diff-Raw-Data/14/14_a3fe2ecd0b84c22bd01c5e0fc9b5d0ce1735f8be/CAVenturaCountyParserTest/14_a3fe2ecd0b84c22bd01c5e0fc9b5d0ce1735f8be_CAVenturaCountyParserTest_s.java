 package net.anei.cadpage.parsers.CA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CAVenturaCountyParserTest extends BaseParserTest {
   
   public CAVenturaCountyParserTest() {
     setParser(new CAVenturaCountyParser(), "VENTURA COUNTY", "CA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[FCC Page]  Incident Dispatch:  E43    BREATHING  2090 Yosemite Ave              11H1-B1 2 / 3  11-0032427",
         "UNIT:E43",
         "CALL:BREATHING",
         "ADDR:2090 Yosemite Ave",
         "MAP:11H1-B1",
         "CH:2 / 3",
         "ID:11-0032427");
 
     doTest("T2",
         "[FCC Page]  Incident Dispatch:  E43    BREATHING  1756 Chaps Ct                  11H1-C4 2 / 3  10-0072877",
         "UNIT:E43",
         "CALL:BREATHING",
         "ADDR:1756 Chaps Ct",
         "MAP:11H1-C4",
         "CH:2 / 3",
         "ID:10-0072877");
 
     doTest("T3",
         "[FCC Page]  Incident Dispatch:  E43    BREATHING  2012 N Tam Ct                  11H1-B3 2 / 3  11-0032306",
         "UNIT:E43",
         "CALL:BREATHING",
         "ADDR:2012 N Tam Ct",
         "MAP:11H1-B3",
         "CH:2 / 3",
         "ID:11-0032306");
 
     doTest("T4",
         "[FCC Page]  Incident Dispatch:  E43    PUBLIC SER 2692 Fallen Leaf Ct            11F3-E2 2 / 3  11-0032318",
         "UNIT:E43",
         "CALL:PUBLIC SER",
         "ADDR:2692 Fallen Leaf Ct",
         "MAP:11F3-E2",
         "CH:2 / 3",
         "ID:11-0032318");
 
     doTest("T5",
         "[FCC Page]  Incident Dispatch:  E43    FIRE MISC  1700 Tapo St                   11G2-B6 2 / 3  11-0032394",
         "UNIT:E43",
         "CALL:FIRE MISC",
         "ADDR:1700 Tapo St",
         "MAP:11G2-B6",
         "CH:2 / 3",
         "ID:11-0032394");
 
     doTest("T6",
         "[FCC Page]  Incident Dispatch:  E43    VEHICLE FI Wb 118 At / Kuehner Dr         11H1-A4 2 / 3  11-0032367",
         "UNIT:E43",
         "CALL:VEHICLE FI",
         "ADDR:Wb 118 At & Kuehner Dr",
         "MADDR:118 At",
         "MAP:11H1-A4",
         "CH:2 / 3",
         "ID:11-0032367");
   }
   
   public static void main(String[] args) {
     new CAVenturaCountyParserTest().generateTests("T1", "UNIT CALL ADDR MAP CH ID");
   }
 }
