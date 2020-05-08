 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDCecilCountyBParserTest extends BaseParserTest {
   
   public MDCecilCountyBParserTest() {
     setParser(new MDCecilCountyBParser(), "CECIL COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "FRM:singerly@gmail.com\n" +
         "MSG:1304A\n" +
         "BUILDING FIRE\n" +
         "ELKTON MIDDLE SCHOOL @615 NORT\n" +
         "23:20:22\n" +
         "11021680\n" +
         "23:23:02\n" +
         "CALLER DRIVING BY SCHOOL AND S",
 
         "CODE:1304A",
         "CALL:BUILDING FIRE",
         "PLACE:ELKTON MIDDLE SCHOOL",
         "ADDR:615 NORTH",
         "TIME:23:20:22",
         "ID:11021680",
         "INFO:CALLER DRIVING BY SCHOOL AND S");
 
     doTest("T2",
         "singerly@gmail.com: 0201A\n" +
         "HOUSE \n" +
         "HOUSE FIRE\n" +
         "470 BLAIR SHORE RD\n" +
         "BLAIR LN\n" +
         "WOOD DUCK LN\n" +
         "22:39:45\n" +
         "12001775\n" +
         "22:42:05\n" +
         "SMELL OF SOMETHING BURNING GET",
 
         "CODE:0201A",
         "CALL:HOUSE FIRE",
         "ADDR:470 BLAIR SHORE RD",
         "X:BLAIR LN & WOOD DUCK LN",
         "TIME:22:39:45",
         "ID:12001775",
         "INFO:SMELL OF SOMETHING BURNING GET");
 
     doTest("T3",
         "singerly@gmail.com: 1401\n" +
         "HOUSE \n" +
         "HOUSE FIRE\n" +
         "1030 KIRK RD\n" +
         "BLACKSNAKE RD\n" +
         "PROVIDENCE RD\n" +
         "07:47:54\n" +
         "12001740\n" +
         "07:50:02\n" +
         "CHIMNEY FIRE WITH FLAMES CALLE",
 
         "CODE:1401",
         "CALL:HOUSE FIRE",
         "ADDR:1030 KIRK RD",
         "X:BLACKSNAKE RD & PROVIDENCE RD",
         "TIME:07:47:54",
         "ID:12001740",
         "INFO:CHIMNEY FIRE WITH FLAMES CALLE");
 
     doTest("T4",
         "singerly@gmail.com: 0304\nMVA (M\nMOTOR VEHICLE ACC\nSINGERLY FIRE COMPAN @399 E PU\n00:49:36\n12001778\n00:49:54",
         "CODE:0304",
         "CALL:MOTOR VEHICLE ACC",
         "PLACE:SINGERLY FIRE COMPAN",
         "ADDR:399 E PU",
         "TIME:00:49:36",
         "ID:12001778");
 
     doTest("T5",
         "singerly@gmail.com: 1303\n" +
         "AFA (A\n" +
         "AUTOMATIC FIRE ALARM\n" +
         "515 FLETCHWOOD RD @WAREHOUSE S\n" +
         "OAKMONT DR\n" +
         "OAKRIDGE CT\n" +
         "01:25:41\n" +
         "12001779\n" +
         "01:26:04\n" +
         "WAREHOUSE AREA WATER FLOW, BUS",
 
         "CODE:1303",
         "CALL:AUTOMATIC FIRE ALARM",
         "ADDR:515 FLETCHWOOD RD",
         "PLACE:WAREHOUSE S",
         "X:OAKMONT DR & OAKRIDGE CT",
         "TIME:01:25:41",
         "ID:12001779",
         "INFO:WAREHOUSE AREA WATER FLOW, BUS");
 
     doTest("T6",
         "singerly@gmail.com: 0304\nMVA (M\nMOTOR VEHICLE ACC\nE PULASKI HWY / DELANCY RD\n13:56:29\n12001803\n13:58:00\n2 VEHICLES 2 PATIENTS",
         "CODE:0304",
         "CALL:MOTOR VEHICLE ACC",
         "ADDR:E PULASKI HWY & DELANCY RD",
         "TIME:13:56:29",
         "ID:12001803",
         "INFO:2 VEHICLES 2 PATIENTS");
 
     doTest("T7",
         "singerly@gmail.com: 0305\nMVA (M\nMOTOR VEHICLE ACC\nW PULASKI HWY / LANDING LN\n14:23:22\n12001804\n14:25:09\nPOL",
         "CODE:0305",
         "CALL:MOTOR VEHICLE ACC",
         "ADDR:W PULASKI HWY & LANDING LN",
         "TIME:14:23:22",
         "ID:12001804",
         "INFO:POL");
 
     doTest("T8",
         "singerly@gmail.com: 1302\n" +
         "AFA (A\n" +
         "AUTOMATIC FIRE ALARM\n" +
         "110 JAMES ST @ELKTON HIGH SCHO\n" +
         "07:05:19\n" +
         "12001833\n" +
         "07:06:12\n" +
         "GENERAL ALARM/WILL ATTEMPT TO",
 
         "CODE:1302",
         "CALL:AUTOMATIC FIRE ALARM",
         "ADDR:110 JAMES ST",
         "PLACE:ELKTON HIGH SCHO",
         "TIME:07:05:19",
         "ID:12001833",
         "INFO:GENERAL ALARM/WILL ATTEMPT TO");
 
     doTest("T9",
         "singerly@gmail.com: 1303\n" +
         "AFA (A\n" +
         "AUTOMATIC FIRE ALARM\n" +
         "515 FLETCHWOOD RD @WAREHOUSE S\n" +
         "OAKMONT DR\n" +
         "OAKRIDGE CT\n" +
         "23:02:22\n" +
         "12001882\n" +
         "23:03:48\n" +
         "ZONE 86 WATERFLOW WEARHOUSE 1",
 
         "CODE:1303",
         "CALL:AUTOMATIC FIRE ALARM",
         "ADDR:515 FLETCHWOOD RD",
         "PLACE:WAREHOUSE S",
         "X:OAKMONT DR & OAKRIDGE CT",
         "TIME:23:02:22",
         "ID:12001882",
         "INFO:ZONE 86 WATERFLOW WEARHOUSE 1");
 
     doTest("T10",
         "singerly@gmail.com: 1401\n" +
         "HOUSE \n" +
         "HOUSE FIRE\n" +
         "20 ROLLING GREEN LN\n" +
         "HILLTOP RD\n" +
         "GOMER CT\n" +
         "20:36:11\n" +
         "12002025\n" +
         "20:36:49\n" +
         "FIRE ACROSS STREET BELIEVED T ",
 
         "CODE:1401",
         "CALL:HOUSE FIRE",
         "ADDR:20 ROLLING GREEN LN",
         "X:HILLTOP RD & GOMER CT",
         "TIME:20:36:11",
         "ID:12002025",
         "INFO:FIRE ACROSS STREET BELIEVED T");
 
     doTest("T11",
         "singerly@gmail.com: 1301\n" +
         "MVA (M\n" +
         "MOTOR VEHICLE ACC\n" +
         "E OLD PHILADELPHIA RD / PULASK\n" +
         "23:21:51\n" +
         "12002033\n" +
         "23:22:19\n" +
         "MALE IS INJURIEDTRAFFIC ACCIDE ",
 
         "CODE:1301",
         "CALL:MOTOR VEHICLE ACC",
         "ADDR:E OLD PHILADELPHIA RD & PULASK",
         "TIME:23:21:51",
         "ID:12002033",
         "INFO:MALE IS INJURIEDTRAFFIC ACCIDE");
 
     doTest("T12",
         "singerly@gmail.com: 1405\n" +
         "MVA (M\n" +
         "MOTOR VEHICLE ACC\n" +
         "HIGHS @5655 TELEGRAPH RD\n" +
         "23:22:43\n" +
         "12002034\n" +
         "23:23:56\n" +
         "ACROSS FROM THE HIGHS STORE DR \n" +
         " ",
 
         "CODE:1405",
         "CALL:MOTOR VEHICLE ACC",
         "PLACE:HIGHS",
         "ADDR:5655 TELEGRAPH RD",
         "TIME:23:22:43",
         "ID:12002034",
         "INFO:ACROSS FROM THE HIGHS STORE DR");
  }
  
  @Test
  public void testOOC() {
    setDefaults("", "MD");
    
    doTest("T13",
        "OOC\nEASST \nENGINE ASSIST\n3247 APPLETON\n13:10:14\n12003811\n13:12:15\nGARAGE FIRE ASSIST STATION 22",
        "CODE:OOC",
        "CALL:ENGINE ASSIST",
        "ADDR:3247 APPLETON",
        "TIME:13:10:14",
        "ID:12003811",
        "INFO:GARAGE FIRE ASSIST STATION 22");
    
   }
   
   public static void main(String[] args) {
     new MDCecilCountyBParserTest().generateTests("T1");
   }
 }
