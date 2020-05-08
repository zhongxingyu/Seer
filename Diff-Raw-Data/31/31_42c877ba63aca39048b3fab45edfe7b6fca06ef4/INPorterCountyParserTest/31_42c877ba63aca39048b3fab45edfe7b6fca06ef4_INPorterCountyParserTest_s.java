 package net.anei.cadpage.parsers.IN;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class INPorterCountyParserTest extends BaseParserTest {
   
   public INPorterCountyParserTest() {
     setParser(new INPorterCountyParser(), "PORTER COUNTY", "IN");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Subject:CAD Page\n\n#110089211 - \nF35P2:\nEMS AST-P2 @ 380 N 400 E,WGT\nCROSS: btwn AUGUSTINE DR and E 400 N\npcdisp@porterco-ps.orgGRP:WF\nPRI:2\ncomment: Medical P",
         "ID:110089211",
         "UNIT:F35P2",
         "CALL:EMS AST-P2",
         "ADDR:380 N 400 E",
         "CITY:Washington Twp",
         "X:AUGUSTINE DR and E 400 N",
         "PRI:2",
         "INFO:Medical P");
 
     doTest("T2",
         "Subject: CAD Page\n\n#110085268 - \nF35:\nMISC. AST EMS @ 4 WILLIAMSBURG DR,WGT\nCROSS: btwn DEAD END and VIRGINIA CT\nGRP:WF\nPRI:4\ncomment: UN",
         "ID:110085268",
         "UNIT:F35",
         "CALL:MISC. AST EMS",
         "ADDR:4 WILLIAMSBURG DR",
         "CITY:Washington Twp",
         "X:DEAD END and VIRGINIA CT",
         "PRI:4",
         "INFO:UN");
 
     doTest("T3",
         "Subject: CAD Page\n\n#110085607 -\nF35P2:\nEMS AST-P2 @ 342 N 325 E,WGT\nCROSS: btwn LONG PRAIRIE RD and STREAMWOOD DR GRP:WF\nPRI:2\ncomment:",
         "ID:110085607",
         "UNIT:F35P2",
         "CALL:EMS AST-P2",
         "ADDR:342 N 325 E",
         "CITY:Washington Twp",
        "X:LONG PRAIRIE RD and STREAMWOOD DR GRP:WF",
         "PRI:2");
 
     doTest("T4",
         "Subject: CAD Page\n\n#110086732 - \nF3:\nELECTRICAL FIRE @ 3705 VALE PARK RD,WGT\nCROSS: btwn ST RD 49 and BARTZ RD\nGRP:WF\nPRI:1\ncomment: 2 PO",
         "ID:110086732",
         "UNIT:F3",
         "CALL:ELECTRICAL FIRE",
         "ADDR:3705 VALE PARK RD",
         "CITY:Washington Twp",
         "X:ST RD 49 and BARTZ RD",
         "PRI:1",
         "INFO:2 PO");
 
     doTest("T5",
         "Subject: CAD Page\n\n#110086764 - \nF33:\nMANPOWER AST EMS @ 13 WILLIAMSBURG DR,WGT\nCROSS: btwn DEAD END and JAMESTOWN AV\nGRP:WF\nPRI:1\ncommen",
         "ID:110086764",
         "UNIT:F33",
         "CALL:MANPOWER AST EMS",
         "ADDR:13 WILLIAMSBURG DR",
         "CITY:Washington Twp",
         "X:DEAD END and JAMESTOWN AV",
         "PRI:1");
 
     doTest("T6",
         "Subject: CAD Page\n\n#110087784 - \nF35P2:\nEMS AST-P2 @ WASHINGTON TWP HIGH SCHOOL,WGT\n - at 383 E ST RD 2,WGT\nCROSS: btwn E EVANS AV and N",
         "ID:110087784",
         "UNIT:F35P2",
         "CALL:EMS AST-P2",
         "PLACE:WASHINGTON TWP HIGH SCHOOL",
         "ADDR:383 E ST RD 2",
         "MADDR:383 E ST 2",
         "CITY:Washington Twp",
         "X:E EVANS AV and N");
 
     doTest("T7",
         "Subject: CAD Page\n\n#110088677 - \nF41:\nMUT.AID/RESP. TO SCN @ 177 HEATHERHILL DR,CTT\nCROSS: btwn DEAD END and HACKNEY LN\nGRP:WF\nPRI:1\ncomm",
         "ID:110088677",
         "UNIT:F41",
         "CALL:MUT.AID/RESP. TO SCN",
         "ADDR:177 HEATHERHILL DR",
         "CITY:Center Twp",
         "X:DEAD END and HACKNEY LN",
         "PRI:1");
 
     doTest("T8",
         "Subject: CAD Page\n\n#110089211 - \nF35P2:\nEMS AST-P2 @ 380 N 400 E,WGT\nCROSS: btwn AUGUSTINE DR and E 400 N\nGRP:WF\nPRI:2\ncomment: Medical P",
         "ID:110089211",
         "UNIT:F35P2",
         "CALL:EMS AST-P2",
         "ADDR:380 N 400 E",
         "CITY:Washington Twp",
         "X:AUGUSTINE DR and E 400 N",
         "PRI:2",
         "INFO:Medical P");
 
     doTest("T9",
         "Subject: CAD Page\n\n#110089866 -\nE29\nMV CRASH/TRANS.ACC @ E US HWY 6 / EDMOND CT, LBT - < 0/ 801>\nGRP:PM\nPRI:1\ncomment: CAR VS PICKUP, CAR IS IN DITCH",
         "ID:110089866",
         "UNIT:E29",
         "CALL:MV CRASH/TRANS.ACC",
         "ADDR:E US HWY 6 & EDMOND CT",
         "MADDR:E US 6 & EDMOND CT",
         "CITY:Liberty Twp",
         "PRI:1",
         "INFO:CAR VS PICKUP, CAR IS IN DITCH");
 
     doTest("T10",
         "Subject:CAD Page\n\n#110120370 - \nF5:\nBRUSH/GRASS FIRE @ E ENGEL DR / CAIN DR,WGT\n-  < 3900/ 1>\nGRP:WF\nPRI:1\ncomment: OPEN BURN IN AREA/",
         "ID:110120370",
         "UNIT:F5",
         "CALL:BRUSH/GRASS FIRE",
         "ADDR:E ENGEL DR & CAIN DR",
         "CITY:Washington Twp",
         "PRI:1",
         "INFO:OPEN BURN IN AREA/");
 
     doTest("T11",
         "Subject:CAD Page\n\n#110127635 - \nF35:\nMISC. AST EMS @ PILOT TRAVEL CENTER,WGT\n- at 4105 E MORTHLAND DR,WGT\nCROSS: near DEAD END\nGRP:WF\nP",
         "ID:110127635",
         "UNIT:F35",
         "CALL:MISC. AST EMS",
         "PLACE:PILOT TRAVEL CENTER",
         "ADDR:4105 E MORTHLAND DR",
         "CITY:Washington Twp",
        "X:near DEAD END");
 
     doTest("T12",
         "Subject:CAD Page\n\n#110137072 - \nF35P1:\nEMS AST-P1 @ 429 E ST RD 2,WGT\nCROSS: btwn N 400 E and N 450 E\nGRP:WF\nPRI:1\ncomment: Medical ProQ",
         "ID:110137072",
         "UNIT:F35P1",
         "CALL:EMS AST-P1",
         "ADDR:429 E ST RD 2",
         "MADDR:429 E ST 2",
         "CITY:Washington Twp",
         "X:N 400 E and N 450 E",
         "PRI:1",
         "INFO:Medical ProQ");
 
     doTest("T13",
         "Subject:CAD Page\n\n#110166809 - \nF35P1:\nEMS AST-P1 @ 53 ASPEN RD,OGD\nCROSS: btwn BITTERSWEET LN and OGDEN RD\nGRP:OF\nPRI:1\ncomment: Medica ",
         "ID:110166809",
         "UNIT:F35P1",
         "CALL:EMS AST-P1",
         "ADDR:53 ASPEN RD",
         "CITY:Ogden Dunes",
         "X:BITTERSWEET LN and OGDEN RD",
         "PRI:1",
         "INFO:Medica");
 
     doTest("T14",
         "Subject:CAD Page\n\n" +
         "#120036045 - \n" +
         "F35:\n" +
         "MISC. AST EMS @ 253-1 N ST RD 2,WGT\n" +
         "CROSS: btwn RIGG RD and E 300 N\n" +
         "GRP:WF\n" +
         "PRI:1\n" +
         "comment: COMPLAINI\r",
 
         "ID:120036045",
         "UNIT:F35",
         "CALL:MISC. AST EMS",
         "ADDR:253-1 N ST RD 2",
         "MADDR:253 N ST 2",
         "CITY:Washington Twp",
         "X:RIGG RD and E 300 N",
         "PRI:1",
         "INFO:COMPLAINI");
 
     doTest("T15",
         "(CAD Page) #120048030 - \n" +
         "E32:\n" +
         "UNK.PROB.(MAN DOWN) @ MM22,BHB\n" +
         "- at 240 I94,BHB\n" +
         "CROSS: btwn I94&US20 D and ST RD 149\n" +
         "GRP:PM\n" +
         "PRI:1\n" +
         "comment: I 94 WESTBOUND 22 M",
 
         "ID:120048030",
         "UNIT:E32",
         "CALL:UNK.PROB.(MAN DOWN)",
         "PLACE:MM22",
         "ADDR:240 I94",
         "MADDR:240 I 94",
         "CITY:Burns Harbor",
         "X:I94&US20 D and ST RD 149",
         "PRI:1",
         "INFO:I 94 WESTBOUND 22 M");
   }
   
   public static void main(String[] args) {
     new INPorterCountyParserTest().generateTests("T1");
   }
 }
