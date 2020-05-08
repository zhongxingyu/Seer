 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PANorthamptonCountyParser;
 
 import org.junit.Test;
 
 
 public class PANorthamptonCountyParserTest extends BaseParserTest {
   
   public PANorthamptonCountyParserTest() {
     setParser(new PANorthamptonCountyParser(), "NORTHAMPTON COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[e49]ALS >ADVANCED LIFE SUPPORT CALL 602 E 21ST ST Apt: 119 Bldg NORTHAMPTON DIANE ECK Map: Grids:0,0 Cad: 2011-0000086714",
         "UNIT:e49",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:602 E 21ST ST",
         "APT:119 Bldg",
         "CITY:NORTHAMPTON",
         "NAME:DIANE ECK",
         "ID:2011-0000086714");
 
     doTest("T2",
         "[e49]BLS >BASIC LIFE SUPPORT CALL 1323 NEWPORT AVE Apt: REAR Bldg NORTHAMPTON TONY FERRERA Map: Grids:0,0 Cad: 2011-00000",
         "UNIT:e49",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:1323 NEWPORT AVE",
         "APT:REAR Bldg",
         "CITY:NORTHAMPTON",
         "NAME:TONY FERRERA",
         "ID:2011-00000");
 
     doTest("T3",
         "[e49]MVAI >MVA WITH INJURIES 248 AT PENNSVILLE LIGHT LEHIGH TWP NATALIE BRODIANO Cad: 2011-0000086361",
         "UNIT:e49",
         "CALL:MVA WITH INJURIES",
         "ADDR:248 AT PENNSVILLE LIGHT",
         "CITY:LEHIGH TWP",
         "NAME:NATALIE BRODIANO",
         "ID:2011-0000086361");
 
     doTest("T4",
         "[e49]ALS >ADVANCED LIFE SUPPORT CALL 612 E 10TH ST NORTHAMPTON ROBERTS PAMELA Map: Grids:0,0 Cad: 2011-0000086262",
         "UNIT:e49",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:612 E 10TH ST",
         "CITY:NORTHAMPTON",
         "NAME:ROBERTS PAMELA",
         "ID:2011-0000086262");
 
     doTest("T5",
         "[e49]BLS >BASIC LIFE SUPPORT CALL 1001 WASHINGTON AVE Apt: 105 Bldg NORTHAMPTON MEGAN MOREY Map: Grids:0,0 Cad: 2011-000008",
         "UNIT:e49",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:1001 WASHINGTON AVE",
         "APT:105 Bldg",
         "CITY:NORTHAMPTON",
         "NAME:MEGAN MOREY",
         "ID:2011-000008");
 
     doTest("T6",
         "[e49]ALS >ADVANCED LIFE SUPPORT CALL 5962 KEYSTONE DR EAST ALLEN CHRISTINA WIGMER Map: Grids:0,0 Cad: 2011-0000086103",
         "UNIT:e49",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:5962 KEYSTONE DR",
         "CITY:EAST ALLEN",
         "NAME:CHRISTINA WIGMER",
         "ID:2011-0000086103");
 
     doTest("T7",
         "[e49]BLS >BASIC LIFE SUPPORT CALL 1323 NEWPORT AVE NORTHAMPTON TONY CABRERA Map: Grids:0,0 Cad: 2011-0000086010",
         "UNIT:e49",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:1323 NEWPORT AVE",
         "CITY:NORTHAMPTON",
         "NAME:TONY CABRERA",
         "ID:2011-0000086010");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "Subject:#6550\n[f14]MVAU >MVA WITH UNKNOW INJUIRIES WILLOW PARK RD BETHLEHEM TWP P1736 Map: Grids:0,0 Cad: 2011-0000131105",
         "UNIT:f14",
         "CALL:MVA WITH UNKNOW INJUIRIES",
         "ADDR:WILLOW PARK RD",
         "CITY:BETHLEHEM TWP",
         "NAME:P1736",
         "ID:2011-0000131105");
    
   }
   
   public void testParsaer3() {
 
     doTest("T1",
         "*3750: *alert_6JP4@notifync.org /  / [f25]ODOR >ODOR / OTHER THAN SMOKE ARNDT RD FORKS Map: Grids:0,0 Cad: 2011-0000171220 <20110000171220>",
         "UNIT:f25",
         "CALL:ODOR/OTHER THAN SMOKE",
         "ADDR:ARNDT RD",
         "NAME:FORKS",
         "ID:2011-0000171220 <20110000171220>");
 
   }
  
   @Test
   public void testActive911() {
 
     doTest("T1",
         "[#8ICT  Northampton County PA CAD] [e46]BLS >BASIC LIFE SUPPORT CALL 6303 HANOVERVILLE RD EAST ALLEN SARA HILBERT Map: Grids:0,0 Cad: 2012-0000098761\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397037&c=f5d0d for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:6303 HANOVERVILLE RD",
         "CITY:EAST ALLEN",
         "NAME:SARA HILBERT",
         "ID:2012-0000098761");
 
     doTest("T2",
         "[#8IJN  Northampton County PA CAD] [e46]ALS >ADVANCED LIFE SUPPORT CALL 5516 LOIS LN EAST ALLEN HOSER, CHRISTINE Map: Grids:0,0 Cad: 2012-0000099349\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397283&c=8e4b8 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:5516 LOIS LN",
         "CITY:EAST ALLEN",
         "NAME:HOSER, CHRISTINE",
         "ID:2012-0000099349");
 
     doTest("T3",
         "[#8IKZ  Northampton County PA CAD] [e46]ALS >ADVANCED LIFE SUPPORT CALL 251 S GREENBRIAR DR EAST ALLEN HORTON, ROBERT Map: Grids:0,0 Cad: 2012-0000099450\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397331&c=4e113 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:251 S GREENBRIAR DR",
         "CITY:EAST ALLEN",
         "NAME:HORTON, ROBERT",
         "ID:2012-0000099450");
 
     doTest("T4",
         "[#8IMN  Northampton County PA CAD] [e46]MVA >MVA NONE INJURY AIRPORT RD EAST ALLEN 4676 Map: Grids:0,0 Cad: 2012-0000099584\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397391&c=b9536 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
        "CALL:MVA NON INJURY",
         "ADDR:AIRPORT RD",
         "CITY:EAST ALLEN",
         "NAME:4676",
         "ID:2012-0000099584");
 
     doTest("T5",
         "[#8IXF  Northampton County PA CAD] [e46]BLS >BASIC LIFE SUPPORT CALL 478 BLUE MOUNTAIN DR LEHIGH KARMONICK, TERRY Map: Grids:0,0 Cad: 2012-0000100411\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397779&c=77340 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:478 BLUE MOUNTAIN DR",
         "NAME:LEHIGH KARMONICK, TERRY",
         "ID:2012-0000100411");
 
     doTest("T6",
         "[#8J0R  Northampton County PA CAD] [e46]ALS >ADVANCED LIFE SUPPORT CALL 5701 COLONY DR EAST ALLEN MARY JOE DEEGAN Map: Grids:0,0 Cad: 2012-0000100593\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397899&c=90443 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:5701 COLONY DR",
         "CITY:EAST ALLEN",
         "NAME:MARY JOE DEEGAN",
         "ID:2012-0000100593");
 
     doTest("T7",
         "[#8J2X  Northampton County PA CAD] [e46]BLS >BASIC LIFE SUPPORT CALL 5745 COLONY DR EAST ALLEN WENGRYN HELEN Map: Grids:0,0 Cad: 2012-0000100697\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=397977&c=2f09b for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:BASIC LIFE SUPPORT",
         "ADDR:5745 COLONY DR",
         "CITY:EAST ALLEN",
         "NAME:WENGRYN HELEN",
         "ID:2012-0000100697");
 
     doTest("T8",
         "[#8J6H  Northampton County PA CAD] [e46]ALS >ADVANCED LIFE SUPPORT CALL 5897 LEHIGH LN EAST ALLEN WALTON, CECELIA Map: Grids:0,0 Cad: 2012-0000100946\n" +
         "Sent by Northampton County Alert Network powered by Cooper Notification's Roam Secure Alert Network\n" +
         "Visit https://www.notifync.org/am.php?a=398105&c=61154 for the map of the area\n" +
         "--\n" +
         "You received this message because you registered on Northampton County Alert Network . To change your alerting preferences go to www.notifync.org/mygroups.php\n" +
         "Tell a friend/co-worker about Northampton County Alert Network! Forward this message to them and have them register for this free service at www.notifync.org \n",
 
         "UNIT:e46",
         "CALL:ADVANCED LIFE SUPPORT",
         "ADDR:5897 LEHIGH LN",
         "CITY:EAST ALLEN",
         "NAME:WALTON, CECELIA",
         "ID:2012-0000100946");
   }
   
   public static void main(String[] args) {
     new PANorthamptonCountyParserTest().generateTests("T1", "UNIT CALL ADDR APT CITY NAME MAP ID");
   }
 }
 
