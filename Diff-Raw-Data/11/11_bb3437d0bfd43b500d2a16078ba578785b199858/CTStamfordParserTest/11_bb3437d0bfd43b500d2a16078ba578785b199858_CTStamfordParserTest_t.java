 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTStamfordParserTest extends BaseParserTest {
   
   public CTStamfordParserTest() {
     setParser(new CTStamfordParser(), "STAMFORD", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         " * 107 EAGLE DR* * * * * 654 STRUCT* * * 203-274-3396* * * * * 6/11/2012 5:32:43 PM : pos2 : SESTABROOK CALLER AND HIS WIFE WILL BE OUT",
         "ADDR:107 EAGLE DR",
         "CALL:654 STRUCT",
         "PHONE:203-274-3396",
         "DATE:6/11/2012",
         "TIME:17:32:43",
         "CH:pos2",
         "INFO:SESTABROOK CALLER AND HIS WIFE WILL BE OUT");
 
     doTest("T2",
         " * 1035 NEWFIELD AV* * * * * F ALARM* * JEWISH COMM CENTER* 203-322-7900* * * * * 05/29/2012 14:53:36 : pos4 : hkokinchak Landmark: JEWISH COMMUNITY CENTER Cross streets: VINE RD//CLUB RD USA CENT OP 33 800-422-2300 GENERAL FIRE*",
         "ADDR:1035 NEWFIELD AV",
         "MADDR:1035 NEWFIELD AVE",
         "CALL:F ALARM",
         "PLACE:JEWISH COMM CENTER",
         "PHONE:203-322-7900",
         "DATE:05/29/2012",
         "TIME:14:53:36",
         "CH:pos4",
         "X:VINE RD / CLUB RD",
         "INFO:hkokinchak Landmark: JEWISH COMMUNITY CENTER / USA CENT OP 33 800-422-2300 GENERAL FIRE");
 
     doTest("T3",
         " * 2700 BEDFORD ST* C* * * * 654 STRUCT* * * 203- -* * * * * 5/12/2012 10:11:52 PM : pos2 : SESTABROOK FIRE IN APT C. 5/12/2012 10:11:40 PM : pos2 : SESTABROOK FIRE IN APT C. 5/12/2012 10:10:50 PM : pos2 : SESTABROOK Cross streets: LOCUST LN//HIGH RIDGE RD 5/12/2012 10:10:16 PM : pos2 : SESTABROOK: Cross streets: LOCUST:",
         "ADDR:2700 BEDFORD ST",
         "APT:C",
         "CALL:654 STRUCT",
         "PHONE:203- -",
         "DATE:5/12/2012",
         "TIME:22:11:52",
         "CH:pos2",
         "X:LOCUST LN / HIGH RIDGE RD",
         "INFO:SESTABROOK FIRE IN APT C. 5/12/2012 10:11:40 PM : pos2 : SESTABROOK FIRE IN APT C. 5/12/2012 10:10:50 PM : pos2 : SESTABROOK / 5 / 12 / 2012 10: 10: 16 PM : pos2 : SESTABROOK: Cross streets: LOCUST:");
 
     doTest("T4",
         " * 243 TRESSER BLVD* * * * * 644 MED LIFE* * MARRIOTT STAMFORD* 203-357-9555* * * * * 06/18/2012 09:23:23 : pos1 : AMSMITH ** EMD Case Complete ** 06/18/2012 09:23:20 : pos1 : AMSMITH ** EMD Recommended Dispatch ** Response Text: Charlie Dispatch Level: 06C01 CAD Incident Code: 06C01 Key Questions: 56 year old, Male, Co",
         "ADDR:243 TRESSER BLVD",
         "CALL:644 MED LIFE",
         "NAME:MARRIOTT STAMFORD",
         "PHONE:203-357-9555",
         "DATE:06/18/2012",
         "TIME:09:23:23",
         "CH:pos1",
         "INFO:AMSMITH 06/18/2012 09:23:20 : pos1 : AMSMITH Charlie Dispatch Level: 06C01 CAD Incident Code: 06C01 56 year old, Male, Co");
 
     doTest("T5",
         " * 58 E CROSS RD* * * * * 654 STRUCT* * JAMES* 203-595-6454* * * * * 06/18/2012 13:36:31 : pos1 : AMSMITH Cross streets: HOPE ST//HAWKS HILL RD SMELL OF SMOKE OR ELECTRICAL BURNING FROM THE WASHING MACHINE...*",
         "ADDR:58 E CROSS RD",
         "CALL:654 STRUCT",
         "NAME:JAMES",
         "PHONE:203-595-6454",
         "DATE:06/18/2012",
         "TIME:13:36:31",
         "CH:pos1",
         "X:HOPE ST / HAWKS HILL RD",
         "INFO:AMSMITH / SMELL OF SMOKE OR ELECTRICAL BURNING FROM THE WASHING MACHINE");

    doTest("T6",
        "911Fire:* 42 HALLOWEEN BLVD* * * * * 654 STRUCT* * * 203-667-1756* * * * 7/5/2012 11:54:59 PM : pos11 : AMITCHELL SAFETY OFC WARD RESPONDING, NOT ETA... 7/5/2012 11:53:26 PM : pos11 : AMITCHELL UNIT 4, REQUESTING FIRE MARSHALL, AND SAFETY OFFICER... 7/5/2012 11:50:17 PM : pos8 : CZARMSKY EVACUATED 07/05/2012 23:48:46 : pos12 : JMASTRO E3 RIT TEAM 07/05/2012 23:47:30 : pos4 : LJORDHAMO NUMEROUS CALL..PARTIES ALL OUT 07/05/2012 23:47:10 : pos2 : HFALLON ADVISED TO EVACUATE 07/05/2012 23:47:01 : pos2 : HFALLON ANOTHER CALLER SEE FLAMES IN THE BASEMENT 07/05/2012 23:46:21 : pos3 : MSEMMEL Cross streets: GLEASON AV//SAINT MARYS ST FIRE IN THE BASEMENT*",
        "ADDR:42 HALLOWEEN BLVD",
        "CALL:654 STRUCT",
        "PHONE:203-667-1756",
        "DATE:7/5/2012",
        "TIME:23:54:59",
        "CH:pos11",
        "X:GLEASON AV / SAINT MARYS ST",
        "INFO:AMITCHELL SAFETY OFC WARD RESPONDING, NOT ETA... 7/5/2012 11:53:26 PM : pos11 : AMITCHELL UNIT 4, REQUESTING FIRE MARSHALL, AND SAFETY OFFICER... 7/5/2012 11:50:17 PM : pos8 : CZARMSKY EVACUATED 07/05/2012 23:48:46 : pos12 : JMASTRO E3 RIT TEAM 07/05/2012 23:47:30 : pos4 : LJORDHAMO NUMEROUS CALL..PARTIES ALL OUT 07/05/2012 23:47:10 : pos2 : HFALLON ADVISED TO EVACUATE 07/05/2012 23:47:01 : pos2 : HFALLON ANOTHER CALLER SEE FLAMES IN THE BASEMENT 07/05/2012 23:46:21 : pos3 : MSEMMEL / FIRE IN THE BASEMENT");
   }
   
   public static void main(String[] args) {
     new CTStamfordParserTest().generateTests("T1");
   }
 }
