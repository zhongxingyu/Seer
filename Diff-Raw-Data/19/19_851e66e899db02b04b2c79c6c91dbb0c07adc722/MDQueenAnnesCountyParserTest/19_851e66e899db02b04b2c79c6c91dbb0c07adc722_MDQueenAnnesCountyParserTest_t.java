 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDQueenAnnesCountyParser;
 
 import org.junit.Test;
 
 
 public class MDQueenAnnesCountyParserTest extends BaseParserTest {
   
   public MDQueenAnnesCountyParserTest() {
     setParser(new MDQueenAnnesCountyParser(), "QUEEN ANNES COUNTY", "MD");
   }
   
   @Test
   public void testCentreVilleParser() {
     
     doTest("T1",
         "qac911:*D 4-2 COMMERCIAL BLDG FIRE DUNKIN DONUTS @500 CHURCH HILL RD COMMERCIAL BOX Q04",
         "CALL:4-2 COMMERCIAL BLDG FIRE DUNKIN DONUTS",
         "ADDR:500 CHURCH HILL RD",
         "INFO:COMMERCIAL",
         "BOX:Q04");
     
     doTest("T2",
         "qac911:*D 4-5 MVC W/ENTRAPMENT RT301NB / RT304 RESCUE BOX Q04",
         "CALL:4-5 MVC W/ENTRAPMENT",
         "ADDR:RT301NB & RT304",
         "MADDR:RT 301 & RT 304",
         "INFO:RESCUE",
         "BOX:Q04");
 
     doTest("T3",
         "qac911:*D 5-1 LG BRUSH/GRASS FIRE RT405 / WINDSWEPT FARM LN BRUSH BOX Q05",
         "CALL:5-1 LG BRUSH/GRASS FIRE",
         "ADDR:RT405 & WINDSWEPT FARM LN",
         "MADDR:RT 405 & WINDSWEPT FARM LN",
         "INFO:BRUSH",
         "BOX:Q05");
     
     doTest("T4",
         "qac911:*D 4-5 UNCONSCIOUS/FAINTING RT301NB / ROLLING BRIDGE RD UNCONSCIOUS Q04",
         "CALL:4-5 UNCONSCIOUS/FAINTING",
         "ADDR:RT301NB & ROLLING BRIDGE RD",
         "MADDR:RT 301 & ROLLING BRIDGE RD",
         "INFO:UNCONSCIOUS",
         "BOX:Q04");
     
     doTest("T5",
         "qac911:*D 5-2 MVC W/ROLLOVER RT301NB / HAYDEN RD RESCUE BOX Q05",
         "CALL:5-2 MVC W/ROLLOVER",
         "ADDR:RT301NB & HAYDEN RD",
         "MADDR:RT 301 & HAYDEN RD",
         "INFO:RESCUE",
         "BOX:Q05");
     
     doTest("T6",
         "qac911:*D 4-7 MVC W/ROLLOVER 1627 RUTHSBURG RD RESCUE BOX Q04",
         "CALL:4-7 MVC W/ROLLOVER",
         "ADDR:1627 RUTHSBURG RD",
         "INFO:RESCUE",
         "BOX:Q04");
     
     doTest("T7",
         "(text message) qac911:*D 4-2 CO ALARM 212 NORTHFIELD WAY LOCAL BOX Q04",
         "CALL:4-2 CO ALARM",
         "ADDR:212 NORTHFIELD WAY",
         "INFO:LOCAL",
         "BOX:Q04");
     
     doTest("T8",
         "(Text Message) QA911com:*D 4-1 UNKNOWN PROBLEM 206 ORCHESTRA PL LIFE STATUS ? Q04",
         "CALL:4-1 UNKNOWN PROBLEM",
         "ADDR:206 ORCHESTRA PL",
         "INFO:LIFE STATUS ?",
         "BOX:Q04");
 
     doTest("T9",
         "(Text Message) QA911com:*D 1-14 FALLS 1401 CALVERT RD POSS DANG BODY AREA Q01",
         "CALL:1-14 FALLS",
         "ADDR:1401 CALVERT RD",
         "INFO:POSS DANG BODY AREA",
         "BOX:Q01");
 
     doTest("T10",
         "(Text Message) QA911com:*G KM06 MUTUAL AID MEDICAL 300 WASHINGTON AVE MUTUAL AID ONLY KM06",
         "CALL:KM06 MUTUAL AID MEDICAL",
         "ADDR:300 WASHINGTON AVE",
         "INFO:MUTUAL AID ONLY",
         "BOX:KM06");
 
   }
   
   @Test
   public void testKentIslandParser() {
 
     doTest("T1",
         "[Text Message]  QA911com:*D 1-15 CHEST PAINS 1630 MAIN ST BREATHING NORMAL>35 Q01",
         "CALL:1-15 CHEST PAINS",
         "ADDR:1630 MAIN ST",
         "INFO:BREATHING NORMAL>35",
         "BOX:Q01");
 
     doTest("T2",
         "[Text Message]  QA911com:*D 1-9 SICK PERSON 402 CASTLE MARINA RD NOT ALERT Q01",
         "CALL:1-9 SICK PERSON",
         "ADDR:402 CASTLE MARINA RD",
         "INFO:NOT ALERT",
         "BOX:Q01");
 
     doTest("T3",
         "[Text Message]  QA911com:*D 1-3 HEART PROBLEMS 115 SALLITT DR ABNORMAL BREATHING Q01",
         "CALL:1-3 HEART PROBLEMS",
         "ADDR:115 SALLITT DR",
         "INFO:ABNORMAL BREATHING",
         "BOX:Q01");
 
     doTest("T4",
         "[Text Message]  QA911com:*D 1-10 SEIZURES 1918 ANCHORAGE DR CONTINUOUS/MULTIPLE Q01",
         "CALL:1-10 SEIZURES",
         "ADDR:1918 ANCHORAGE DR",
         "INFO:CONTINUOUS / MULTIPLE",
         "BOX:Q01");
 
     doTest("T5",
         "[Text Message]  QA911com:*D 1-9 ODOR OF SMOKE INSIDE 44 F QUEEN NEVA CT COMMERCIAL BOX Q01",
         "CALL:1-9 ODOR OF SMOKE INSIDE",
         "ADDR:44 F QUEEN NEVA CT",
         "INFO:COMMERCIAL",
         "BOX:Q01");
 
     doTest("T6",
         "[Text Message]  QA911com:*D 1-15 HEART PROBLEMS 1630 MAIN ST CARDIAC HISTORY Q01",
         "CALL:1-15 HEART PROBLEMS",
         "ADDR:1630 MAIN ST",
         "INFO:CARDIAC HISTORY",
         "BOX:Q01");
 
     doTest("T7",
         "[Text Message]  QA911com:*D 1-4 DROWNING/DIVE ACCDNT 624 ROMANCOKE RD WATER RESCUE BOX Q01",
         "CALL:1-4 DROWNING/DIVE ACCDNT",
         "ADDR:624 ROMANCOKE RD",
         "INFO:WATER RESCUE",
         "BOX:Q01");
     
   }
   
   @Test
   public void testSuldersvilleParser() {
 
     doTest("T1",
         "(Text Message) QA911com:*D 6-1 CHEST PAINS 104 CHARLES ST DIFF SPEAKING Q06",
         "CALL:6-1 CHEST PAINS",
         "ADDR:104 CHARLES ST",
         "INFO:DIFF SPEAKING",
         "BOX:Q06");
   }
   
   @Test
   public void testParser4() {
 
     doTest("T1",
         "(CAD) [CAD] QA911com:*D 4-1 COMMERCIAL BLDG FIRE 160 COURSEVALL DR @QAC PLANNING & ZONING COMMERCIAL BOX Q04",
         "CALL:4-1 COMMERCIAL BLDG FIRE",
         "ADDR:160 COURSEVALL DR",
         "PLACE:QAC PLANNING & ZONING COMMERCIAL",
         "BOX:Q04");
   }
   
   @Test
   public void testParser5() {
 
     doTest("T1",
         "[CAD] D 4-11 ABDOMINAL PAINS 1130 BURRISSVILLE RD Q04",
         "CALL:4-11 ABDOMINAL PAINS",
         "ADDR:1130 BURRISSVILLE RD",
         "BOX:Q04");
 
   }
   
   @Test
   public void testJahurlock() {
 
     doTest("T1",
         "[CAD] D KENT MUTUAL AID MEDICAL 30564 CHESTERVILLE BRIDGE RD MUTUAL AID ONLY KENT",
         "CALL:KENT MUTUAL AID MEDICAL",
         "CITY:KENT",
         "ADDR:30564 CHESTERVILLE BRIDGE RD",
         "INFO:MUTUAL AID ONLY KENT");
 
    doTest("T2",
        "[CAD] D 7-5 UNKNOWN PROBLEM 304 MERGANSER DR MEDICAL ALARM Q07",
        "CALL:7-5 UNKNOWN PROBLEM",
        "ADDR:304 MERGANSER DR",
        "INFO:MEDICAL ALARM",
        "BOX:Q07");

  }
  
  @Test
  public void testFfmedicmo() {

    doTest("T1",
        "1-9 HEART PROBLEMS 1630 MAIN ST CARDIAC HISTORY Q01",
        "CALL:1-9 HEART PROBLEMS",
        "ADDR:1630 MAIN ST",
        "INFO:CARDIAC HISTORY",
        "BOX:Q01");

   }
   
   public static void main(String[] args) {
     new MDQueenAnnesCountyParserTest().generateTests("T1", "CALL CITY ADDR PLACE INFO BOX");
   }
 }
