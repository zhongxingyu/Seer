 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYRocklandCountyBParserTest extends BaseParserTest {
   
   public NYRocklandCountyBParserTest() {
     setParser(new NYRocklandCountyBParser(), "ROCKLAND COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "15  -  STRUCTURE FIRE    94 ORANGE TRNPK  CROSS: MUNICIPAL PLZ / LIBERTY ROCK RD 17:03 30 - MANN",
         "UNIT:15",
         "CALL:STRUCTURE FIRE",
         "ADDR:94 ORANGE TRNPK",
         "X:MUNICIPAL PLZ / LIBERTY ROCK RD");
 
     doTest("T2",
         "15  -  UNKNOWN TYPE - OUTSIDE    265 ORANGE TRNPK  CROSS: UNKNOWN / PARK AV 13:40 50 - HUMPHREY",
         "UNIT:15",
         "CALL:UNKNOWN TYPE - OUTSIDE",
         "ADDR:265 ORANGE TRNPK",
         "X:UNKNOWN / PARK AV");
 
     doTest("T3",
         "15  -  VEHICLE FIRE   TWAY 34.6 NB  CROSS: UNKNOWN / NEW YORK STATE THWY N, NEW YORK STATE THRUWAY - 87 N 13:40 90 - FRIDHANDLER",
         "UNIT:15",
         "CALL:VEHICLE FIRE",
         "ADDR:TWAY 34.6 NB",
         "MADDR:TWAY 34.6 & UNKNOWN",
         "X:UNKNOWN / NEW YORK STATE THWY N, NEW YORK STATE THRUWAY - 87 N");
 
     doTest("T4",
         "15  -  VEHICLE FIRE   TWAY - SLOATSBURG REST AREA  CROSS: NEW YORK STATE THRUWAY - 87 N, NEW YORK STATE THRUWAY - 87 / NEW YORK STATE THRUWAY - 87, NEW YORK STATE THRUWAY - 87 N 17:13 22- BERTOLACCI",
         "UNIT:15",
         "CALL:VEHICLE FIRE",
         "ADDR:TWAY - SLOATSBURG REST AREA",
        "MADDR:TWAY - SLOATSBURG REST AREA & NEW YORK STATE THRUWAY - 87 N",
         "X:NEW YORK STATE THRUWAY - 87 N, NEW YORK STATE THRUWAY - 87 / NEW YORK STATE THRUWAY - 87, NEW YORK STATE THRUWAY - 87 N");
 
     doTest("T5",
         "15  -  FLOODING CONDITION    23 SEVEN LAKES DR  CROSS: ALLEN LN / WALDRON TERR 07:37 71 - O'KEEFFE",
         "UNIT:15",
         "CALL:FLOODING CONDITION",
         "ADDR:23 SEVEN LAKES DR",
         "X:ALLEN LN / WALDRON TERR");
 
     doTest("T6",
         "15  -  BRUSH/MULCH/RUBBISH OUTSIDE    18 COLONIAL AV  CROSS: POTHAT ST / RICHARD ST 15:12 35 - BATES",
         "UNIT:15",
         "CALL:BRUSH/MULCH/RUBBISH OUTSIDE",
         "ADDR:18 COLONIAL AV",
         "MADDR:18 COLONIAL AVE",
         "X:POTHAT ST / RICHARD ST");
 
     doTest("T7",
         "15  -  VEHICLE FIRE    2 STERLING MINE RD  CROSS: ARCADIA CT, CRANBERRY RD / ARCADIA CT 19:40 10 - DELUCIA",
         "UNIT:15",
         "CALL:VEHICLE FIRE",
         "ADDR:2 STERLING MINE RD",
         "X:ARCADIA CT, CRANBERRY RD / ARCADIA CT");
 
     doTest("T8",
         "15  -  OVEN/APPLIANCE/COOKING FIRE    151 ORANGE TRNPK  CROSS: LEDGE RD / POST RD 18:07 22- BERTOLACCI",
         "UNIT:15",
         "CALL:OVEN/APPLIANCE/COOKING FIRE",
         "ADDR:151 ORANGE TRNPK",
         "X:LEDGE RD / POST RD");
 
     doTest("T9",
         "15  -  ASSIST POLICE OR EMS    15 RICHARD ST  CROSS: COLONIAL AV / STERLING AV 06:50 71 - O'KEEFFE",
         "UNIT:15",
         "CALL:ASSIST POLICE OR EMS",
         "ADDR:15 RICHARD ST",
         "X:COLONIAL AV / STERLING AV");
 
 
     doTest("T10",
         "15  -  AUTOMATIC ALARM   1131780 - ARDEN HILLS NURSERY 185 ORANGE TRNPK  CROSS: WASHINGTON AV / LEDGE RD 17:50 10 - DELUCIA",
         "UNIT:15",
         "CALL:AUTOMATIC ALARM",
         "ID:1131780",
         "PLACE:ARDEN HILLS NURSERY",
         "ADDR:185 ORANGE TRNPK",
         "X:WASHINGTON AV / LEDGE RD");
 
     doTest("T11",
         "15  -  AUTOMATIC ALARM   1130653 - ST JOSEPH'S HOME 125 SISTERS SERVANTS LN  CROSS: UNKNOWN / CEMETERY RD 15:31 10 - DELUCIA",
         "UNIT:15",
         "CALL:AUTOMATIC ALARM",
         "ID:1130653",
         "PLACE:ST JOSEPH'S HOME",
         "ADDR:125 SISTERS SERVANTS LN",
         "X:UNKNOWN / CEMETERY RD");
 
     doTest("T12",
         "15 - BRUSH/MULCH/RUBBISH OUTSIDE 171 ORANGE TRNPK CROSS: LEDGE RD / POST RD 15:51 55 - LENIHAN",
         "UNIT:15",
         "CALL:BRUSH/MULCH/RUBBISH OUTSIDE",
         "ADDR:171 ORANGE TRNPK",
         "X:LEDGE RD / POST RD");
   }
   
   public static void main(String[] args) {
     new NYRocklandCountyBParserTest().generateTests("T13", "UNIT CALL ID PLACE ADDR X");
   }
 }
