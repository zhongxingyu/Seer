 package net.anei.cadpage.parsers.WV;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class WVKanawhaCountyParserTest extends BaseParserTest {
   
   public WVKanawhaCountyParserTest() {
     setParser(new WVKanawhaCountyParser(), "KANAWHA COUNTY", "WV");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Message Forwarded by PageGate)  FIRE ALARM reported at 95 RHL BLVD in SOUTH CHARLESTON on 02/10/12 15:00",
         "CALL:FIRE ALARM",
         "ADDR:95 RHL BLVD",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/10/12",
         "TIME:15:00");
 
     doTest("T2",
         "(Message Forwarded by PageGate)  DECREASED LOC (SEMI-UNRESPONSIVE) reported at 2700 MOUNTAINEER BLVD in SOUTH CHARLESTON on 02/10/12 18:43",
         "CALL:DECREASED LOC (SEMI-UNRESPONSIVE)",
         "ADDR:2700 MOUNTAINEER BLVD",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/10/12",
         "TIME:18:43");
 
     doTest("T3",
         "(Message Forwarded by PageGate)  SHORTNESS OF BREATH reported at 5218 1/2 INDIANA ST in SOUTH CHARLESTON on 02/10/12 19:03",
         "CALL:SHORTNESS OF BREATH",
         "ADDR:5218 1/2 INDIANA ST",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/10/12",
         "TIME:19:03");
 
     doTest("T4",
         "(Message Forwarded by PageGate)  FIRE ALARM reported at 3 EAGLE DR in SOUTH CHARLESTON on 02/11/12 12:28",
         "CALL:FIRE ALARM",
         "ADDR:3 EAGLE DR",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/11/12",
         "TIME:12:28");
 
     doTest("T5",
         "(Message Forwarded by PageGate)  AUTO ACCIDENT WITH INJURIES reported at 56 I64 EAST in SOUTH CHARLESTON on 02/11/12 07:57",
         "CALL:AUTO ACCIDENT WITH INJURIES",
         "ADDR:56 I64 EAST",
         "MADDR:56 I 64 EAST",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/11/12",
         "TIME:07:57");
 
     doTest("T6",
         "(Message Forwarded by PageGate)  CARDIAC ARREST reported at 24 MACCORKLE AVE in SOUTH CHARLESTON on 02/11/12 11:56",
         "CALL:CARDIAC ARREST",
         "ADDR:24 MACCORKLE AVE",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/11/12",
         "TIME:11:56");
 
     doTest("T7",
         "(Message Forwarded by PageGate)  CHEST PAINS reported at 4610 YOUNG ST in SOUTH CHARLESTON on 02/10/12 22:35",
         "CALL:CHEST PAINS",
         "ADDR:4610 YOUNG ST",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/10/12",
         "TIME:22:35");
 
     doTest("T8",
         "(Message Forwarded by PageGate)  DOMESTIC WITH INJURIES reported at 4991 COLONIAL PARK DR in SOUTH CHARLESTON on 02/11/12 20:08",
         "CALL:DOMESTIC WITH INJURIES",
         "ADDR:4991 COLONIAL PARK DR",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/11/12",
         "TIME:20:08");
 
     doTest("T9",
         "(Message Forwarded by PageGate)  FIRE ALARM reported at 331 SOUTHRIDGE BLVD in CHARLESTON on 02/12/12 16:36",
         "CALL:FIRE ALARM",
         "ADDR:331 SOUTHRIDGE BLVD",
         "CITY:CHARLESTON",
         "DATE:02/12/12",
         "TIME:16:36");
 
     doTest("T10",
         "(Message Forwarded by PageGate)  HAZARDOUS MATERIALS INCIDENT reported at 46 RHL BLVD in SOUTH CHARLESTON on 02/17/12 09:13",
         "CALL:HAZARDOUS MATERIALS INCIDENT",
         "ADDR:46 RHL BLVD",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/17/12",
         "TIME:09:13");
 
     doTest("T11",
         "(Message Forwarded by PageGate)  UNRESPONSIVE PATIENT reported at 1232 THOMAS RD in SOUTH CHARLESTON on 02/17/12 10:19",
         "CALL:UNRESPONSIVE PATIENT",
         "ADDR:1232 THOMAS RD",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/17/12",
         "TIME:10:19");
 
     doTest("T12",
         "(Message Forwarded by PageGate)  SHORTNESS OF BREATH reported at 4718 KANAWHA AVE in SOUTH CHARLESTON on 02/15/12 07:20",
         "CALL:SHORTNESS OF BREATH",
         "ADDR:4718 KANAWHA AVE",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/15/12",
         "TIME:07:20");
 
     doTest("T13",
         "(Message Forwarded by PageGate)  DIABETIC PATIENT reported at 2905 MACON ST in SOUTH CHARLESTON on 02/15/12 10:45",
         "CALL:DIABETIC PATIENT",
         "ADDR:2905 MACON ST",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/15/12",
         "TIME:10:45");
 
     doTest("T14",
         "(Message Forwarded by PageGate)  INJURED PERSON reported at 15 RIVER WALK MALL in SOUTH CHARLESTON on 02/15/12 11:57",
         "CALL:INJURED PERSON",
         "ADDR:15 RIVER WALK MALL",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/15/12",
         "TIME:11:57");
 
     doTest("T15",
         "(Message Forwarded by PageGate)  UNRESPONSIVE PATIENT reported at 46 RHL BLVD in SOUTH CHARLESTON on 02/17/12 16:10",
         "CALL:UNRESPONSIVE PATIENT",
         "ADDR:46 RHL BLVD",
         "CITY:SOUTH CHARLESTON",
         "DATE:02/17/12",
         "TIME:16:10");
     
   }
   
  @Test
  public void testParser2() {

    doTest("T1",
        "Metro911@metro911.org Msg: Metro911:HIGH WATER / FLOODING ISSUES reported at NEW GOFF MOUNTAIN RD // 1ST AVE S in CROSS LANES on 05/05/12 13:30",
        "CALL:HIGH WATER / FLOODING ISSUES",
        "ADDR:NEW GOFF MOUNTAIN RD & 1ST AVE S",
        "CITY:CROSS LANES",
        "DATE:05/05/12",
        "TIME:13:30");

  }
  
   public static void main(String[] args) {
     new WVKanawhaCountyParserTest().generateTests("T1", "CALL ADDR CITY DATE TIME");
   }
 }
