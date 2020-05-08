 package net.anei.cadpage.parsers.GA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class GACamdenCountyParserTest extends BaseParserTest {
   
   public GACamdenCountyParserTest() {
     setParser(new GACamdenCountyParser(), "CAMDEN COUNTY", "GA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "astudstill@co.camden.ga.us:2011-101695* HIGHWAY 17 STEFFANS* * * KINGSLAND* * Traffic Stop* TRAFFIC STOP* * * 1128,1140,1150,1152,509,514,523,532,LS3,R3* * Med",
         "ID:2011-101695",
         "ADDR:HIGHWAY 17",
         "MADDR:STEFFANS,HIGHWAY 17",
         "CITY:KINGSLAND",
         "PLACE:STEFFANS",
         "CALL:TRAFFIC STOP",
         "UNIT:1128,1140,1150,1152,509,514,523,532,LS3,R3",
         "INFO:Med");
 
     doTest("T2",
         "astudstill@co.camden.ga.us:2011-104696* 4059 MARTIN LUTHER KING BLVD* N4* * 514,541,546,ENG4,LS4,MED4 KINGSLAND* * INJURY* INJURY* 509,ENG5,LS3* * Medical: No",
         "ID:2011-104696",
         "ADDR:4059 MARTIN LUTHER KING BLVD",
         "APT:N4",
         "CITY:KINGSLAND",
         "CALL:INJURY",
         "UNIT:509,ENG5,LS3",
         "INFO:Medical: No");
 
     doTest("T3",
         "astudstill@co.camden.ga.us:2011-101995* 405 & HIGHWAY 40 OLD WAFFLE HOUSE* * * KINGSLAND* * ACCIDENT* ACCIDENT* ELAINE* 386-208-4465* 514,541,546,ENG4,LS4,MED4",
         "ID:2011-101995",
         "ADDR:405 & HIGHWAY 40",
         "CITY:KINGSLAND",
         "PLACE:OLD WAFFLE HOUSE",
         "CALL:ACCIDENT",
         "NAME:ELAINE",
         "PHONE:386-208-4465",
         "UNIT:514,541,546,ENG4,LS4,MED4");
 
     doTest("T4",
         "astudstill@co.camden.ga.us:2011-104843* 429 EAGLE BLVD* * * KINGSLAND* * PERSON SICK* PERSON SICK* * 540,ENG4,LS4* * Medical: No* Haz",
         "ID:2011-104843",
         "ADDR:429 EAGLE BLVD",
         "CITY:KINGSLAND",
         "CALL:PERSON SICK",
         "UNIT:540,ENG4,LS4",
         "INFO:Medical: No / Haz");
 
     doTest("T5",
         "214 REDWOOD ST* * * KINGSLAND* * PERSON SICK* PERSON SICK* MS FAGEN*912-269-6157* LS3,R3* * Medical: No* Hazards: No*",
         "ADDR:214 REDWOOD ST",
         "CITY:KINGSLAND",
         "CALL:PERSON SICK",
         "NAME:MS FAGEN",
         "PHONE:912-269-6157",
         "UNIT:LS3,R3",
         "INFO:Medical: No / Hazards: No");
 
     doTest("T6",
         "astudstill@co.camden.ga.us:2011-181161* ADVANCE COLLISION CENTER* * * KINGSLAND* * WIRE DOWN* WIRE DOWN* JOHN* 316-258-1559* ENG4,LS4* * Medical: No* Haz",
         "ID:2011-181161",
         "ADDR:ADVANCE COLLISION CENTER",
         "CITY:KINGSLAND",
         "CALL:WIRE DOWN",
         "NAME:JOHN",
         "PHONE:316-258-1559",
         "UNIT:ENG4,LS4",
         "INFO:Medical: No / Haz");
 
     doTest("T7",
         "astudstill@co.camden.ga.us:2012-037466* BOONE AND SUMMERBROOK* * * KINGSLAND* * INVESTIGATE* INVESTIGATE SUSPICIOUS PERSON/VEHICLE* JUAN RODRIGUEZ* 912-8",
         "ID:2012-037466",
         "ADDR:BOONE AND SUMMERBROOK",
         "CITY:KINGSLAND",
         "CALL:INVESTIGATE SUSPICIOUS PERSON/VEHICLE",
         "NAME:JUAN RODRIGUEZ",
         "PHONE:912-8");
  
   }
   
   public static void main(String[] args) {
    new GACamdenCountyParserTest().generateTests("T6");
   }
 }
