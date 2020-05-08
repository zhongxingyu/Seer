 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTNorthBranfordParserTest extends BaseParserTest {
   
   public CTNorthBranfordParserTest() {
     setParser(new CTNorthBranfordParser(), "NORTH BRANFORD", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "1100008173 MEDICAL MEDA 00167  BRANFORD RD HUBBARD RD/TWIN LAKE RD  MED4 503 110910 11:50",
         "ID:1100008173",
         "CALL:MEDICAL MEDA",
         "ADDR:167 BRANFORD RD",
         "X:HUBBARD RD / TWIN LAKE RD",
         "UNIT:MED4 503");
 
     doTest("T2",
         "1100008148 MEDICAL MEDD 00009  SALEM ST CLINTONVILLE RD/VILLAGE ST  MED4 R2 110909 17:55",
         "ID:1100008148",
         "CALL:MEDICAL MEDD",
         "ADDR:9 SALEM ST",
         "X:CLINTONVILLE RD / VILLAGE ST",
         "UNIT:MED4 R2");
 
     doTest("T3",
         "1100008142 MUTUAL AID 00392  HOPE HILL RD WALLINGFORD    MED4 110909 15:38",
         "ID:1100008142",
         "CALL:MUTUAL AID",
         "ADDR:392 HOPE HILL RD",
         "X:WALLINGFORD",
         "UNIT:MED4");
 
     doTest("T4",
         "1100008130 AUTOMATIC FIRE ALARM 00051  CIRO RD FOXON RD/DEAD END  E1 ET11 ET22 T1 R11 515 110909 10:07",
         "ID:1100008130",
         "CALL:AUTOMATIC FIRE ALARM",
         "ADDR:51 CIRO RD",
         "X:FOXON RD / DEAD END",
         "UNIT:E1 ET11 ET22 T1 R11 515");
 
     doTest("T5",
         "1100008128 MEDICAL MEDB 00049  CAPUTO RD FOXON RD ROUTE 80/MILL RD  R1 MED4 515 110909 08:38",
         "ID:1100008128",
         "CALL:MEDICAL MEDB",
         "ADDR:49 CAPUTO RD",
         "X:FOXON RD ROUTE 80 / MILL RD",
         "UNIT:R1 MED4 515");
 
     doTest("T6",
         "1100007980 BRUSH FIRE TWIN LAKES RD/ LAKE RD   BR1 ET11 502 110904 12:22",
         "ID:1100007980",
         "CALL:BRUSH FIRE TWIN",
         "ADDR:LAKES RD & LAKE RD",
         "UNIT:BR1 ET11 502");
   }
   
   @Test
   public void testWLFDParser() {
 
     doTest("T1",
         "1100008057 MVA W/INJURIES N MAIN ST/RT68 WLFD A44 110906 17:05",
         "ID:1100008057",
         "CALL:MVA W/INJURIES",
         "ADDR:N MAIN ST & RT68",
         "CITY:WALLINFORD",
        "UNIT:A44");
 
     doTest("T2",
         "1100007014 MUTUAL AID 00909  BEAVER HEAD RD , GUILFORD    MED4 110811 01:16",
         "ID:1100007014",
         "CALL:MUTUAL AID",
         "ADDR:909 BEAVER HEAD RD",
         "CITY:GUILFORD",
         "UNIT:MED4");
    
   }
   
  @Test
  public void testParser2() {

    doTest("T1",
        "1100010113 MEDICAL MEDC 00254  BRANFORD RD Prem Map -  HARRISON RD/FOXON RD  MED4 R1 111116 23:04\n",
        "ID:1100010113",
        "CALL:MEDICAL MEDC",
        "ADDR:254 BRANFORD RD",
        "X:HARRISON RD / FOXON RD",
        "UNIT:MED4 R1");

  }
  
   public static void main(String[] args) {
     new CTNorthBranfordParserTest().generateTests("T1", "ID CALL ADDR CITY X UNIT");
   }
 }
