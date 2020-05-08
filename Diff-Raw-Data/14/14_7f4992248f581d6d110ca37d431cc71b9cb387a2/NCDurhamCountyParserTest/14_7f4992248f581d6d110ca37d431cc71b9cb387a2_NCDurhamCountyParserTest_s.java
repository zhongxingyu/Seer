 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCDurhamCountyParserTest extends BaseParserTest {
   
   public NCDurhamCountyParserTest() {
     setParser(new NCDurhamCountyParser(), "DURHAM COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:TRANSPORTATION ACCIDENT;2399 S ALSTON AVE/RIDDLE RD;{M8} CODE 2 RESPONSE [06/30/11 15:48:22 GRIFFINT] {M8} SEND ADDTL MEDIC UNIT [06/30/11 15:46:11 GRIFFINT] [",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:2399 S ALSTON AVE & RIDDLE RD",
        "INFO:{M8} CODE 2 RESPONSE / {M8} SEND ADDTL MEDIC UNIT");
 
     doTest("T2",
         "CAD:BACK PAIN;5499 SUTTERIDGE CT/LYON FARM DR;[Medical Priority Info] PROBLEM: back pain # PATS: 1 AGE: 50 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes [07/05/11",
         "CALL:BACK PAIN",
         "ADDR:5499 SUTTERIDGE CT & LYON FARM DR",
         "INFO:back pain # PATS: 1 AGE: 50 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes");
 
     doTest("T3",
         "CAD:PREGNANCY;5438 NEW HOPE COMMONS DR;MT MORIAH RD;HOFFLER LN",
         "CALL:PREGNANCY",
         "ADDR:5438 NEW HOPE COMMONS DR",
         "X:MT MORIAH RD & HOFFLER LN");
 
     doTest("T4",
         "CAD:CHEST PAIN;8210 RENAISSANCE PKWY;CHEST PAIN .. [07/05/11 12:37:45 GARRETTG] EMPLOYEE INJ .. [07/05/11 12:37:15 GARRETTG] ;KNOLL CIR;LEONARDO DR",
         "CALL:CHEST PAIN",
         "ADDR:8210 RENAISSANCE PKWY",
         "INFO:CHEST PAIN .. / EMPLOYEE INJ .. / KNOLL CIR / LEONARDO DR");
 
     doTest("T5",
         "CAD:CHEST PAIN;2238 W NC 54 HWY;CELESTE CIR",
         "CALL:CHEST PAIN",
         "ADDR:2238 W NC 54 HWY",
         "X:CELESTE CIR");
 
     doTest("T6",
         "CAD:TRANSPORTATION ACCIDENT;4117 EMPEROR BLVD;**************OPS1 [07/05/11 06:06:46 ARCH] ;S MIAMI BLVD;SWABIA CT",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:4117 EMPEROR BLVD",
         "CH:OPS1",
         "INFO:S MIAMI BLVD / SWABIA CT");
 
     doTest("T7",
         "CAD:TRANSPORTATION ACCIDENT;28163 I40 E/EXIT 283;BLK 4DR VOLVO, BLK VW JETTER, BLK 2 DR JETTA [07/04/11 20:06:12 GAY]",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:28163 I40 E & EXIT 283",
         "INFO:BLK 4DR VOLVO, BLK VW JETTER, BLK 2 DR JETTA");
 
     doTest("T8",
         "CAD:SICK PERSON;101 GREEN CEDAR LN;[Medical Priority Info] RESPONSE: Charlie RESPONDER SCRIPT: Unknown status/Other codes not applicable (Unknown when the symptoms",
         "CALL:SICK PERSON",
         "ADDR:101 GREEN CEDAR LN",
         "INFO:Unknown status/Other codes not applicable (Unknown when the symptoms");
 
     doTest("T9",
         "CAD:TRANSPORTATION ACCIDENT;6405 FAYETTEVILLE RD;THE CALLER WORKS INSIDE AND WAS NOT INVOLVED [07/04/11 14:35:13 BELLD] in the parking lot involving a gold toyota",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:6405 FAYETTEVILLE RD",
         "INFO:THE CALLER WORKS INSIDE AND WAS NOT INVOLVED / in the parking lot involving a gold toyota");
 
     doTest("T10",
         "CAD:FALL;1101 EXCHANGE PL;[Medical Priority Info] PROBLEM: fell # PATS: 1 AGE: 80 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes [07/02/11 13:35:49 HINESLEY] ;MER",
         "CALL:FALL",
         "ADDR:1101 EXCHANGE PL",
         "INFO:fell # PATS: 1 AGE: 80 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes / MER");
 
     doTest("T11",
         "CAD:DIABETIC PROBLEM;5219 PAGE RD;CREEKSTONE DR;TERRACE PINE DR",
         "CALL:DIABETIC PROBLEM",
         "ADDR:5219 PAGE RD",
         "X:CREEKSTONE DR & TERRACE PINE DR");
   }
   
   public static void main(String[] args) {
    new NCDurhamCountyParserTest().generateTests("T1");
   }
 }
