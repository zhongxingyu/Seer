 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ILRockIslandCountyParserTest extends BaseParserTest {
   
   public ILRockIslandCountyParserTest() {
     setParser(new ILRockIslandCountyParser(), "ROCK ISLAND COUNTY", "IL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "@RIFD: 15221:;CRASH PERSONAL INJURIES;24TH ST/18TH AV",
         "SRC:RIFD",
         "ID:15221",
         "CALL:CRASH PERSONAL INJURIES",
         "ADDR:24TH ST & 18TH AV",
         "MADDR:24TH ST & 18TH AVE");
 
     doTest("T2",
         "@RIFD: 15226:;FIRE OTHER;1323 14TH ST",
         "SRC:RIFD",
         "ID:15226",
         "CALL:FIRE OTHER",
         "ADDR:1323 14TH ST");
 
     doTest("T3",
         "@RIFD: 15227:;FIRE ALARM;3605 11TH AV",
         "SRC:RIFD",
         "ID:15227",
         "CALL:FIRE ALARM",
         "ADDR:3605 11TH AV",
         "MADDR:3605 11TH AVE");
 
     doTest("T4",
         "@RIFD: 16193:;MEDICAL CALL;2300-46 79TH AV W",
         "SRC:RIFD",
         "ID:16193",
         "CALL:MEDICAL CALL",
         "ADDR:2300-46 79TH AV W",
         "MADDR:2300 79TH AVE W");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "S: M:67:FYI: ;STRUCTURE FIRE/GENERAL;2932 RODMAN AV\n",
         "ID:67",
         "CALL:STRUCTURE FIRE/GENERAL",
         "ADDR:2932 RODMAN AV",
         "MADDR:2932 RODMAN AVE");
 
     doTest("T2",
         "S: M:68:CANCEL;2932 RODMAN AV; RIA\n",
         "ID:68",
         "CALL:CANCEL - RIA",
         "ADDR:2932 RODMAN AV",
         "MADDR:2932 RODMAN AVE");
 
     doTest("T3",
         "S: M:69:Update: ;FIRE MUTUAL AID;2932 RODMAN AV\n",
         "ID:69",
         "CALL:FIRE MUTUAL AID",
         "ADDR:2932 RODMAN AV",
         "MADDR:2932 RODMAN AVE");
 
    doTest("T4",
        "@RIFD: 17437:;MEDICAL CALL;767 30TH ST;RI",
        "SRC:RIFD",
        "ID:17437",
        "CALL:MEDICAL CALL",
        "ADDR:767 30TH ST",
        "CITY:ROCK ISLAND");

   }
   
   public static void main(String[] args) {
    new ILRockIslandCountyParserTest().generateTests("T1", "SRC ID CALL ADDR CITY");
   }
 }
