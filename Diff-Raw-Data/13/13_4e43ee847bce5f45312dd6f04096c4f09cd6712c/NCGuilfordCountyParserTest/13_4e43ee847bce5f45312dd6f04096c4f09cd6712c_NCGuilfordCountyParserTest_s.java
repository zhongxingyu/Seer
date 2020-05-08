 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCGuilfordCountyParserTest extends BaseParserTest {
   
   public NCGuilfordCountyParserTest() {
     setParser(new NCGuilfordCountyParser(), "GUILFORD COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "[summerfieldfiredist Jan24-10:01]  CAD:SMFD;ST09;1;52C03C;FIRAL;7607 BLUE SAGE CT",
         "SRC:summerfieldfiredist",
         "INFO:SMFD / ST09 / 1",
         "CODE:52C03C",
         "CALL:FIRAL",
         "ADDR:7607 BLUE SAGE CT");
 
     doTest("T2",
         "[summerfieldfiredist Jan19-18:15]  CAD:SMFD;ST29;1;50PI;SCALESVILLE RD/LAKE BRANDT RD",
         "SRC:summerfieldfiredist",
         "INFO:SMFD / ST29 / 1",
         "CALL:50PI",
         "ADDR:SCALESVILLE RD & LAKE BRANDT RD");
 
     setDefaults("","NC");
     doTest("T3",
         "[summerfieldfiredist Jan18-19:08]  CAD:1;MUTUAL;150 WAYNOR DR;GALLENT RD;STRUCTURE FIRE",
         "SRC:summerfieldfiredist",
         "CALL:MUTUAL",
         "ADDR:150 WAYNOR DR",
         "X:GALLENT RD",
         "INFO:STRUCTURE FIRE");
     setDefaults("GUILFORD COUNTY", "NC");
 
     doTest("T4",
         "(summerfieldfiredist Jan18-17:00) CAD:SMFD;ST29;1;25D01;PSYCH;8579 HUDSON JAMES RD;WRIGHTS FARM LN",
         "SRC:summerfieldfiredist",
         "INFO:SMFD / ST29 / 1",
         "CODE:25D01",
         "CALL:PSYCH",
         "ADDR:8579 HUDSON JAMES RD",
         "X:WRIGHTS FARM LN");
 
     doTest("T5",
         "[summerfieldfiredist Jan18-03:16]  CAD:SMFD;ST39;1;29B01;50PI;PLAINFIELD RD/LAKE BRANDT RD;BETWEEN LAKE BRANDT AND CHURCH ST",
         "SRC:summerfieldfiredist",
         "INFO:SMFD / ST39 / 1 / BETWEEN LAKE BRANDT AND CHURCH ST",
         "CODE:29B01",
         "CALL:50PI",
         "ADDR:PLAINFIELD RD & LAKE BRANDT RD");
 
     doTest("T6",
         "(summerfieldfiredist Jan21-03:25) CAD:SMFD;ST09;1;67B01;OSFIR;2523 PLEASANT RIDGE RD;SADDLEWOOD DR;DIST: 35.58 FT",
         "INFO:SMFD / ST09 / 1 / DIST: 35.58 FT",
         "SRC:summerfieldfiredist",
         "CODE:67B01",
         "CALL:OSFIR",
         "ADDR:2523 PLEASANT RIDGE RD",
         "X:SADDLEWOOD DR");
 
     doTest("T7",
         "(summerfieldfiredist Jan23-06:49) CAD:SMFD;ST39;1;17B03;FALL;6251 LAKE BRANDT RD;HI-LO LN",
         "SRC:summerfieldfiredist",
         "INFO:SMFD / ST39 / 1",
         "CODE:17B03",
         "CALL:FALL",
         "ADDR:6251 LAKE BRANDT RD",
         "X:HI-LO LN");
 
     doTest("T8",
         "[summerfieldfiredist Jan23-19:20]  CAD:OKRD;ST51;1;69D11;STRUC;4529 PEEPLES RD;RIDING TRAIL CT",
         "SRC:summerfieldfiredist",
         "INFO:OKRD / ST51 / 1",
         "CODE:69D11",
         "CALL:STRUC",
         "ADDR:4529 PEEPLES RD",
         "X:RIDING TRAIL CT");
 
     doTest("T9",
         "[summerfieldfiredist Jan23-19:18]  CAD:OKRD;ST51;1;STRUC;4529 PEEPLES RD;RIDING TRAIL CT",
         "SRC:summerfieldfiredist",
         "INFO:OKRD / ST51 / 1",
         "CALL:STRUC",
         "ADDR:4529 PEEPLES RD",
         "X:RIDING TRAIL CT");
 
     doTest("T10",
         "[summerfieldfiredist Jan23-20:23]  CAD:OKRD;ST51;1;69D06;STRUC;5371 NORTHWEST SCHOOL RD;TRAIL PIEDMONT GREENWAY",
         "SRC:summerfieldfiredist",
         "INFO:OKRD / ST51 / 1 / TRAIL PIEDMONT GREENWAY",
         "CODE:69D06",
         "CALL:STRUC",
         "ADDR:5371 NORTHWEST SCHOOL RD");
 
     doTest("T11",
         "(summerfieldfiredist Jan24-10:31) CAD:UNDER CONTROL;7607 BLUE SAGE CT; SU",
         "SRC:summerfieldfiredist",
         "CALL:UNDER CONTROL",
         "ADDR:7607 BLUE SAGE CT",
         "INFO:SU");
 
     doTest("T12",
         "(summerfieldfiredist Jan23-11:25) CAD:UNDER CONTROL;6308 PEPPERMILL DR; OAK",
         "SRC:summerfieldfiredist",
         "CALL:UNDER CONTROL",
         "ADDR:6308 PEPPERMILL DR",
         "INFO:OAK");
 
     doTest("T13",
         "(summerfieldfiredist Jan23-19:48) CAD:UNDER CONTROL;4529 PEEPLES RD; OAK",
         "SRC:summerfieldfiredist",
         "CALL:UNDER CONTROL",
         "ADDR:4529 PEEPLES RD",
         "INFO:OAK");
 
     doTest("T14",
         "(summerfieldfiredist Jun10-01:00) summerfieldfiredist\nCAD:UNDER CONTROL;5322 BUNCH RD; SU",
         "SRC:summerfieldfiredist",
         "ADDR:5322 BUNCH RD",
         "CALL:UNDER CONTROL",
         "INFO:SU");
 
     doTest("T15",
         "(summerfieldfiredist Jun10-19:27) summerfieldfiredist\nCAD:FD13;ST58;1;60C02;GAS;210 JAMES DOAK PKWY;BRIXHAM DR",
         "SRC:summerfieldfiredist",
         "ADDR:210 JAMES DOAK PKWY",
         "INFO:FD13 / ST58 / 1",
         "CODE:60C02",
         "CALL:GAS",
         "X:BRIXHAM DR");
 
     doTest("T16",
         "CAD@greensboro-nc.gov:CAD:CODE 47;7700 NEWSOME CT; B;CARDIAC / RESPIRATORY ARREST",
         "SRC:CAD@greensboro-nc.gov",
         "ADDR:7700 NEWSOME CT",
         "CALL:CODE 47",
         "INFO:B / CARDIAC / RESPIRATORY ARREST");
 
     doTest("T17",
         "FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun11-00:23\nMSG:summerfieldfiredist\nCAD:UNDER CONTROL;210 JAMES DOAK PKWY; G\n",
         "SRC:summerfieldfiredist",
         "ADDR:210 JAMES DOAK PKWY",
         "INFO:G",
         "CALL:UNDER CONTROL");
 
     doTest("T18",
         "1 of 2\nFRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-16:22\nMSG:summerfieldfiredist\nCAD:OPA4;OPA4;1;MEDEM;5570 US 220 N;M AND M\n(Con't) 2 of 2\nTIRE AND AUTOMOTIVE;SPOTSWOOD RD\n(End)",
         "SRC:summerfieldfiredist",
         "ADDR:5570 US 220 N",
         "INFO:OPA4 / OPA4 / 1 / M AND M TIRE AND AUTOMOTIVE",
         "CALL:MEDEM",
         "X:SPOTSWOOD RD");
 
     doTest("T19",
         "FRM:dispatch@edispatches.com\nSUBJ:summerfieldfiredist Jun17-14:57\nMSG:summerfieldfiredist\nCAD:SMFD;ST39;1;09E02;CARDIA;5718 OAK TREE RD;AIR HARBOR RD\n",
         "SRC:summerfieldfiredist",
         "ADDR:5718 OAK TREE RD",
         "INFO:SMFD / ST39 / 1",
         "CODE:09E02",
         "CALL:CARDIA",
         "X:AIR HARBOR RD");
 
     doTest("T20",
         "CAD:UNDER CONTROL;878 CROSSCREEK RD; K ;. [27]",
         "ADDR:878 CROSSCREEK RD",
         "INFO:K / .",
         "CALL:UNDER CONTROL");
 
     doTest("T21",
         "Subject:summerfieldfiredist Aug06-16:44\nsummerfieldfiredist \nCAD:SMFD;ST09;1;31D02;UNCON;7912 SUMMERFIELD RD;FAIRGROVE BAPTIST CHURCH;",
         "SRC:summerfieldfiredist",
         "ADDR:7912 SUMMERFIELD RD",
         "INFO:SMFD / ST09 / 1 / FAIRGROVE BAPTIST CHURCH",
         "CODE:31D02",
         "CALL:UNCON");
 
     doTest("T22",
         "Subject:summerfieldfiredist Aug06-06:57\nsummerfieldfiredist \nCAD:OKRD;ST15;1;69D03;STRUC;8005 MARKETPLACE DR;TRACTOR SUPPLY CO;NC 68 N",
         "SRC:summerfieldfiredist",
         "ADDR:8005 MARKETPLACE DR",
         "INFO:OKRD / ST15 / 1 / TRACTOR SUPPLY CO",
         "CODE:69D03",
         "CALL:STRUC",
         "X:NC 68 N");
 
     doTest("T23",
         "Subject:summerfieldfiredist Aug08-11:36\nsummerfieldfiredist \nCAD:SMFD;ST39;1;19D01;HEART;1007 NC 150 W;SPEAR CLINIC;LAKE BRANDT RD;ROO",
         "SRC:summerfieldfiredist",
         "ADDR:1007 NC 150 W",
         "INFO:SMFD / ST39 / 1 / SPEAR CLINIC / ROO",
         "CODE:19D01",
         "CALL:HEART",
         "X:LAKE BRANDT RD");
 
     doTest("T24",
         "Subject:summerfieldfiredist Aug23-03:00\nsummerfieldfiredist\nCAD:SMFD;ST29;1;18C04;HEAD;8562 HUDSON JAMES RD;WRIGHTS FARM LN\n",
         "SRC:summerfieldfiredist",
         "ADDR:8562 HUDSON JAMES RD",
         "INFO:SMFD / ST29 / 1",
         "CODE:18C04",
         "CALL:HEAD",
         "X:WRIGHTS FARM LN");
 
     doTest("T25",
         "Subject:summerfieldfiredist Aug23-02:49\nsummerfieldfiredist\nCAD:SMFD;ST09;1;25B03;PSYCH;7619 BRISBANE DR;OAK RIDGE RD\n",
         "SRC:summerfieldfiredist",
         "ADDR:7619 BRISBANE DR",
         "INFO:SMFD / ST09 / 1",
         "CODE:25B03",
         "CALL:PSYCH",
         "X:OAK RIDGE RD");
 
     doTest("T26",
         "Subject:summerfieldfiredist Aug21-19:49\nsummerfieldfiredist\nCAD:SMFD;ST09;1;23C02A;OVDOSE;2307 SCALESVILLE RD;ROBINSON RD;HOUSE\n",
         "SRC:summerfieldfiredist",
         "ADDR:2307 SCALESVILLE RD",
         "INFO:SMFD / ST09 / 1 / HOUSE",
         "CODE:23C02A",
         "CALL:OVDOSE",
         "X:ROBINSON RD");
 
     doTest("T27",
         "Subject:summerfieldfiredist Aug21-16:02\nsummerfieldfiredist\nCAD:OKRD;ST51;1;STRUC;6401 W BELGRAVE TER\n",
         "SRC:summerfieldfiredist",
         "ADDR:6401 W BELGRAVE TER",
         "INFO:OKRD / ST51 / 1",
         "CALL:STRUC");
 
   }
   
 
   public static void main(String[] args) {
    new NCGuilfordCountyParserTest().generateTests("T25");
   }
 }
