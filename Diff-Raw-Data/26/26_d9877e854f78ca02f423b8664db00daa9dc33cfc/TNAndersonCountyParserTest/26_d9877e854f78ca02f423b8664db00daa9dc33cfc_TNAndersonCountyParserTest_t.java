 package net.anei.cadpage.parsers.TN;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TNAndersonCountyParserTest extends BaseParserTest {
   
   public TNAndersonCountyParserTest() {
     setParser(new TNAndersonCountyParser(), "ANDERSON COUNTY", "TN");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(E911) Fire - Residential - 782 BRUSHY VALLEY RD ANDERSONVILLE, TN - REC:04:51 DISP:04:51 RESP:04:53 ONSC:05:09 INSRV:08:13 TRK#:178730",
         "CALL:Fire - Residential (END)",
         "ADDR:782 BRUSHY VALLEY RD",
         "CITY:ANDERSONVILLE",
         "INFO:REC:04:51 DISP:04:51 RESP:04:53 ONSC:05:09 INSRV:08:13 TRK#:178730");
 
     doTest("T2",
         "(E911) Fire Alarm-Residential - 1534 MOUNTAIN RD ANDERSONVILLE, TN",
         "CALL:Fire Alarm-Residential",
         "ADDR:1534 MOUNTAIN RD",
         "CITY:ANDERSONVILLE");
 
     doTest("T3",
         "(E911) Fire - Residential - 778 BRUSHY VALLEY RD ANDERSONVILLE, TN",
         "CALL:Fire - Residential",
         "ADDR:778 BRUSHY VALLEY RD",
         "CITY:ANDERSONVILLE");
 
     doTest("T4",
         "(E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN - REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572",
         "CALL:Fire - Other (END)",
         "ADDR:ANDERSONVILLE HWY & BETHEL ROAD",
         "CITY:CLINTON",
         "INFO:REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572");
 
     doTest("T5",
         "(E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN",
         "CALL:Fire - Other",
         "ADDR:ANDERSONVILLE HWY & BETHEL ROAD",
         "CITY:CLINTON");
 
     doTest("T6",
         "(E911) MVA - Hazards - BETHEL RD CLINTON, TN -REC:03:15 DISP:03:17 RESP:03:19 ONSC:03:35 INSRV:04:03 TRK#:178399",
         "CALL:MVA - Hazards (END)",
         "ADDR:BETHEL RD",
         "CITY:CLINTON",
         "INFO:REC:03:15 DISP:03:17 RESP:03:19 ONSC:03:35 INSRV:04:03 TRK#:178399");
 
     doTest("T7",
         "(E911) MVA - Hazards - BETHEL RD CLINTON, TN",
         "CALL:MVA - Hazards",
         "ADDR:BETHEL RD",
         "CITY:CLINTON");
 
     doTest("T8",
         "(E911) CVFD FIRST RESP - 3259 W WOLF VALLEY ROAD CLINTON, TN - FALL INJURIES",
         "SRC:CVFD FIRST RESP",
         "CALL:FALL INJURIES",
         "ADDR:3259 W WOLF VALLEY ROAD",
         "CITY:CLINTON");
 
     doTest("T9",
         "(E911) RESC - I75 SB @ MM 119 CLINTON, TN - MVA",
         "SRC:RESC",
         "CALL:MVA",
         "ADDR:I75 SB & MM 119",
         "CITY:CLINTON");
 
     doTest("T10",
         "(E911) MVA - Injury - I75 SB @ MM 119 CLINTON, TN - REC:02:41 DISP:02:41 RESP:02:45 ONSC: INSRV:02:51 TRK#:179132",
         "CALL:MVA - Injury (END)",
         "ADDR:I75 SB & MM 119",
         "CITY:CLINTON",
         "INFO:REC:02:41 DISP:02:41 RESP:02:45 ONSC: INSRV:02:51 TRK#:179132");
 
     doTest("T11",
         "(E911) MARFVD FIRST RESP - 1508 LAUREL RD CLINTON, TN - DIFFICULTY BREATHING",
         "SRC:MARFVD FIRST RESP",
         "CALL:DIFFICULTY BREATHING",
         "ADDR:1508 LAUREL RD",
         "CITY:CLINTON");
 
     doTest("T12",
         "(E911) EMS Assist - 1508 LAUREL RD CLINTON, TN - REC:20:21 DISP:20:23 RESP:20:24 ONSC:20:31 INSRV:20:39 TRK#:179119",
         "CALL:EMS Assist (END)",
         "ADDR:1508 LAUREL RD",
         "CITY:CLINTON",
         "INFO:REC:20:21 DISP:20:23 RESP:20:24 ONSC:20:31 INSRV:20:39 TRK#:179119");
 
     doTest("T13",
         "(E911) Mutual Aid Request - 838 OLIVER SPRINGS HIGHWAY CLINTON, TN",
         "CALL:Mutual Aid Request",
         "ADDR:838 OLIVER SPRINGS HIGHWAY",
         "CITY:CLINTON");
 
     doTest("T14",
         "(E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN",
         "CALL:Fire - Other",
         "ADDR:ANDERSONVILLE HWY & BETHEL ROAD",
         "CITY:CLINTON");
 
     doTest("T15",
         "(E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN - REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572",
         "CALL:Fire - Other (END)",
         "ADDR:ANDERSONVILLE HWY & BETHEL ROAD",
         "CITY:CLINTON",
         "INFO:REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572");
   
   }
   
  @Test
  public void testParser2() {

    doTest("T1",
        "E911 / MARFVD - FROST BOTTOM ROAD  OLIVER SPRINGS, TN - MVA\n\n",
        "SRC:MARFVD",
        "CALL:MVA",
        "ADDR:FROST BOTTOM ROAD",
        "CITY:OLIVER SPRINGS");

    doTest("T2",
        "E911 / MARFVD FIRST RESP - 1180 DUTCH VALLEY ROAD  CLINTON, TN - DIFFICULTY  BREATHING\n\n",
        "SRC:MARFVD FIRST RESP",
        "CALL:DIFFICULTY  BREATHING",
        "ADDR:1180 DUTCH VALLEY ROAD",
        "CITY:CLINTON");

    doTest("T3",
        "E911 / Fire Alarm-Residential - 351 WOODLAND HILLS ROAD CLINTON, TN - REC:12:11 DISP:12:12 RESP:12:16 ONSC: INSRV:12:17 TRK#:188459\n\n",
        "CALL:Fire Alarm-Residential (END)",
        "ADDR:351 WOODLAND HILLS ROAD",
        "CITY:CLINTON",
        "INFO:REC:12:11 DISP:12:12 RESP:12:16 ONSC: INSRV:12:17 TRK#:188459");
   
  }
  
 
   public static void main(String[] args) {
     new TNAndersonCountyParserTest().generateTests("T1");
   }
 }
