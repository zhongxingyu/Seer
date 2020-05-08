 package net.anei.cadpage.parsers.IA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class IAPolkCountyParserTest extends BaseParserTest {
   
   public IAPolkCountyParserTest() {
     setParser(new IAPolkCountyParser(), "POLK COUNTY", "IA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         ".... (Grimes) Location: 500 S JAMES ST GRIM: @SCHOOL - GRIM Type: STILL VEHICLE Caller:  Time: 19:47:24",
         "SRC:Grimes",
         "ADDR:500 S JAMES ST",
         "CITY:GRIMES",
         "CALL:STILL VEHICLE",
         "PLACE:SCHOOL",
         "TIME:19:47:24");
 
     doTest("T2",
         ".... (Grimes) Location: 101 SE DESTINATION DR GRIMES Type: MEDICAL  Caller: GRIMES FAMILY PHYSICIANS Time: 10:54:07",
         "SRC:Grimes",
         "ADDR:101 SE DESTINATION DR",
         "CITY:GRIMES",
         "CALL:MEDICAL",
         "PLACE:GRIMES FAMILY PHYSICIANS",
         "TIME:10:54:07");
 
     doTest("T3",
         ".... (Grimes) Location: 200 SW BROOKSIDE DR GRIM,111 Type: MEDICAL  Caller: KENNYBROOK VILLAGE Time: 13:42:46",
         "SRC:Grimes",
         "ADDR:200 SW BROOKSIDE DR",
         "APT:111",
         "CITY:GRIMES",
         "CALL:MEDICAL",
         "PLACE:KENNYBROOK VILLAGE",
         "TIME:13:42:46");
 
     doTest("T4",
         ".... (Grimes) Location: 5555 NW 55TH AVE JOHN: @CHILDSERVE - JOHN Type: COMMFIRE ALARM Caller: ADT/DBI Time: 17:01:43",
         "SRC:Grimes",
         "ADDR:5555 NW 55TH AVE",
         "CITY:JOHNSTON",
         "CALL:COMMFIRE ALARM",
         "PLACE:CHILDSERVE / ADT/DBI",
         "TIME:17:01:43");
 
     doTest("T5",
         ".... (Grimes) Location: 2555 W 1ST ST GRIM: @DALLAS CENTER GRIMES HIGH SCHOOL - GRIM Type: COMMFIRE ALARM Caller: ADT Time: 20:37:32",
         "SRC:Grimes",
         "ADDR:2555 W 1ST ST",
         "CITY:GRIMES",
         "CALL:COMMFIRE ALARM",
         "PLACE:DALLAS CENTER GRIMES HIGH SCHOOL / ADT",
         "TIME:20:37:32");
 
   }
   
   public static void main(String[] args) {
     new IAPolkCountyParserTest().generateTests("T1");
   }
 }
