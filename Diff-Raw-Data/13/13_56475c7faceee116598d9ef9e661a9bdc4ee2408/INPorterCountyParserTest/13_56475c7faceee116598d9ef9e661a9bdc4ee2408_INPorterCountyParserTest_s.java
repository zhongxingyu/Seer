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
         "CITY:Liberty Twp",
         "PRI:1",
         "INFO:CAR VS PICKUP, CAR IS IN DITCH");
  }
   
   public static void main(String[] args) {
    new INPorterCountyParserTest().generateTests("T1");
   }
 }
