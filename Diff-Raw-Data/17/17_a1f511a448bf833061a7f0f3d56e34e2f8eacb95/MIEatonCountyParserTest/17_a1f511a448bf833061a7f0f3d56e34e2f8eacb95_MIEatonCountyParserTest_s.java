 package net.anei.cadpage.parsers.MI;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MIEatonCountyParserTest extends BaseParserTest {
   
   public MIEatonCountyParserTest() {
 	  setParser(new MIEatonCountyParser(), "EATON COUNTY", "MI");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:FYI: ;20110000176;10/04/2011 20:35:39;BRUSH FIRE;W MT HOPE HWY/N SHAYTOWN RD;CALLER PASSERBY [10/04/11 20:36:48 KANTCLIFF] N OF ABOVE ADDRESS",
         "ID:20110000176",
         "DATE:10/04/2011",
         "TIME:20:35:39",
         "CALL:BRUSH FIRE",
         "ADDR:W MT HOPE HWY & N SHAYTOWN RD",
         "INFO:CALLER PASSERBY / N OF ABOVE ADDRESS");
 
     doTest("T2",
         "CAD:FYI: ;20110000177;10/09/2011 09:45:21;MEDICAL ASSIST;3286 E EATON HWY;Event spawned from EMS PRIORITY 2. [10/09/2011 09:45:21",
         "ID:20110000177",
         "DATE:10/09/2011",
         "TIME:09:45:21",
         "CALL:MEDICAL ASSIST",
         "ADDR:3286 E EATON HWY",
         "PRI:2");
 
     doTest("T3",
         "CAD:FYI: ;20110000178;10/10/2011 07:59:43;MEDICAL ASSIST;10763 N SHAYTOWN RD;Event spawned from EMS PRIORITY 3. [10/10/2011 07:59:",
         "ID:20110000178",
         "DATE:10/10/2011",
         "TIME:07:59:43",
         "CALL:MEDICAL ASSIST",
         "ADDR:10763 N SHAYTOWN RD",
         "PRI:3");
 
     doTest("T4",
         "CAD:FYI: ;20110000179;10/11/2011 15:13:37;MEDICAL ASSIST;9040 W GRAND LEDGE HWY;Event spawned from EMS PRIORITY 1",
         "ID:20110000179",
         "DATE:10/11/2011",
         "TIME:15:13:37",
         "CALL:MEDICAL ASSIST",
         "ADDR:9040 W GRAND LEDGE HWY",
         "PRI:1");
 
     doTest("T5",
         "CAD:FYI: ;20110000180;10/11/2011 19:55:29;WIRES DOWN;11600 N IONIA RD;SEMI WENT BY TOOK DOWN WIRE RIPPED METER BOX FROM CALLERS HOUSE",
         "ID:20110000180",
         "DATE:10/11/2011",
         "TIME:19:55:29",
         "CALL:WIRES DOWN",
         "ADDR:11600 N IONIA RD",
         "INFO:SEMI WENT BY TOOK DOWN WIRE RIPPED METER BOX FROM CALLERS HOUSE");
 
     doTest("T6",
         "CAD:FYI: ;20110000485;09/16/2011 20:40:28;MEDICAL ASSIST;1931 GIDNER RD;Event spawned from EMS PRIORITY 1. [09/16/2011 20:40:28 JBLOUNT] {122} REQ MANPOWER [09/",
         "ID:20110000485",
         "DATE:09/16/2011",
         "TIME:20:40:28",
         "CALL:MEDICAL ASSIST",
         "ADDR:1931 GIDNER RD",
         "PRI:1",
         "INFO:{122} REQ MANPOWER");
 
     doTest("T7",
         "CAD:FYI: ;20110000487;09/20/2011 00:38:14;MEDICAL ASSIST;2704 S PERKEY RD;Event spawned from EMS PRIORITY 1. [09/20/2011 00:38:14 KANTCLIFF] [LAW] VICTIM IS TER",
         "ID:20110000487",
         "DATE:09/20/2011",
         "TIME:00:38:14",
         "CALL:MEDICAL ASSIST",
         "ADDR:2704 S PERKEY RD",
         "PRI:1",
         "INFO:VICTIM IS TER");
 
     doTest("T8",
         "CAD:FYI: ;20110000484;09/16/2011 19:23:15;SPECIAL RESCUE;ACKLEY RD/BELL HWY;Event spawned from UNKNOWN PROBLEM. [09/16/2011 19:23:15 JBLOUNT] ONE MAY BE CAUGHT",
         "ID:20110000484",
         "DATE:09/16/2011",
         "TIME:19:23:15",
         "CALL:SPECIAL RESCUE",
         "ADDR:ACKLEY RD & BELL HWY",
         "INFO:Event spawned from UNKNOWN PROBLEM. / ONE MAY BE CAUGHT");
 
     doTest("T9",
         "CAD:FYI: ;20110000471;09/08/2011 14:27:39;MEDICAL ASSIST;414 RAILROAD ST;Event spawned from EMS PRIORITY 1. [09/08/2011 14:27:39 ROWENS] HX LEAKY VALVE IN HEART",
         "ID:20110000471",
         "DATE:09/08/2011",
         "TIME:14:27:39",
         "CALL:MEDICAL ASSIST",
         "ADDR:414 RAILROAD ST",
         "PRI:1",
         "INFO:HX LEAKY VALVE IN HEART");
 
     doTest("T10",
         "CAD:FYI: ;20110000486;09/17/2011 19:48:03;COMPLAINT FIRE INVESTIGATION;800 E SHAW ST;EVERYONE IS OUTSIDE, REF EMS. [09/17/11 19:49:20 JBLOUNT]",
         "ID:20110000486",
         "DATE:09/17/2011",
         "TIME:19:48:03",
         "CALL:COMPLAINT FIRE INVESTIGATION",
         "ADDR:800 E SHAW ST",
         "INFO:EVERYONE IS OUTSIDE, REF EMS.");
 
     doTest("T11",
         "CAD:FYI: ;20110000482;09/15/2011 19:42:53;COMPLAINT FIRE INVESTIGATION;W FIVE POINT HWY/MATTHEWS RD;W OF MATTHEWS [09/15/11 19:43:57 KANTCLIFF] NE SIDE OF THE R",
         "ID:20110000482",
         "DATE:09/15/2011",
         "TIME:19:42:53",
         "CALL:COMPLAINT FIRE INVESTIGATION",
         "ADDR:W FIVE POINT HWY & MATTHEWS RD",
         "INFO:W OF MATTHEWS / NE SIDE OF THE R");
 
     doTest("T12",
         "CAD:FYI: ;20110000481;09/15/2011 08:19:47;COMMERCIAL FIRE ALARM;417 MAYNARD ST;BEEN GOING FOR 5 MINS. [09/15/11 08:21:10 MBIALKOWSKI] CLLR IN THE NORTH WING. GO",
         "ID:20110000481",
         "DATE:09/15/2011",
         "TIME:08:19:47",
         "CALL:COMMERCIAL FIRE ALARM",
         "ADDR:417 MAYNARD ST",
         "INFO:BEEN GOING FOR 5 MINS. / CLLR IN THE NORTH WING. GO");
 
     doTest("T13",
         "CAD:FYI: ;20110000480;09/14/2011 19:20:41;MISCELLANEOUS FIRE RUN;S PERKEY RD/E CLINTON TRL;Event spawned from MISCELLANEOUS INCIDENT. [09/14/2011 19:20:41 CYARG",
         "ID:20110000480",
         "DATE:09/14/2011",
         "TIME:19:20:41",
         "CALL:MISCELLANEOUS FIRE RUN",
         "ADDR:S PERKEY RD & E CLINTON TRL",
         "INFO:Event spawned from MISCELLANEOUS INCIDENT.");
   }
   
   public static void main(String[] args) {
    new MIEatonCountyParserTest().generateTests("T6");
   }
 }
