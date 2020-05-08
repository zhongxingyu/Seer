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
         "MADDR:2399 S ALSTON AVE",
         "UNIT:M8",
         "INFO:CODE 2 RESPONSE / {M8} SEND ADDTL MEDIC UNIT",
         "DATE:06/30/11",
         "TIME:15:48:22");
 
     doTest("T2",
         "CAD:BACK PAIN;5499 SUTTERIDGE CT/LYON FARM DR;[Medical Priority Info] PROBLEM: back pain # PATS: 1 AGE: 50 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes [07/05/11",
         "CALL:BACK PAIN",
         "ADDR:5499 SUTTERIDGE CT & LYON FARM DR",
         "MADDR:5499 SUTTERIDGE CT",
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
         "INFO:CHEST PAIN .. / EMPLOYEE INJ .. / KNOLL CIR / LEONARDO DR",
         "DATE:07/05/11",
         "TIME:12:37:45");
 
     doTest("T5",
         "CAD:CHEST PAIN;2238 W NC 54 HWY;CELESTE CIR",
         "CALL:CHEST PAIN",
         "ADDR:2238 W NC 54 HWY",
         "MADDR:2238 W NC 54",
         "X:CELESTE CIR");
 
     doTest("T6",
         "CAD:TRANSPORTATION ACCIDENT;4117 EMPEROR BLVD;**************OPS1 [07/05/11 06:06:46 ARCH] ;S MIAMI BLVD;SWABIA CT",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:4117 EMPEROR BLVD",
         "CH:OPS1",
         "INFO:S MIAMI BLVD / SWABIA CT",
         "DATE:07/05/11",
         "TIME:06:06:46");
 
     doTest("T7",
         "CAD:TRANSPORTATION ACCIDENT;28163 I40 E/EXIT 283;BLK 4DR VOLVO, BLK VW JETTER, BLK 2 DR JETTA [07/04/11 20:06:12 GAY]",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:28163 I40 E & EXIT 283",
         "MADDR:28163 I 40 E",
         "INFO:BLK 4DR VOLVO, BLK VW JETTER, BLK 2 DR JETTA",
         "DATE:07/04/11",
         "TIME:20:06:12");
 
     doTest("T8",
         "CAD:SICK PERSON;101 GREEN CEDAR LN;[Medical Priority Info] RESPONSE: Charlie RESPONDER SCRIPT: Unknown status/Other codes not applicable (Unknown when the symptoms",
         "CALL:SICK PERSON",
         "ADDR:101 GREEN CEDAR LN",
         "INFO:Unknown status/Other codes not applicable (Unknown when the symptoms");
 
     doTest("T9",
         "CAD:TRANSPORTATION ACCIDENT;6405 FAYETTEVILLE RD;THE CALLER WORKS INSIDE AND WAS NOT INVOLVED [07/04/11 14:35:13 BELLD] in the parking lot involving a gold toyota",
         "CALL:TRANSPORTATION ACCIDENT",
         "ADDR:6405 FAYETTEVILLE RD",
         "INFO:THE CALLER WORKS INSIDE AND WAS NOT INVOLVED / in the parking lot involving a gold toyota",
         "DATE:07/04/11",
         "TIME:14:35:13");
 
     doTest("T10",
         "CAD:FALL;1101 EXCHANGE PL;[Medical Priority Info] PROBLEM: fell # PATS: 1 AGE: 80 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes [07/02/11 13:35:49 HINESLEY] ;MER",
         "CALL:FALL",
         "ADDR:1101 EXCHANGE PL",
         "INFO:fell # PATS: 1 AGE: 80 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes / MER",
         "DATE:07/02/11",
         "TIME:13:35:49");
 
     doTest("T11",
         "CAD:DIABETIC PROBLEM;5219 PAGE RD;CREEKSTONE DR;TERRACE PINE DR",
         "CALL:DIABETIC PROBLEM",
         "ADDR:5219 PAGE RD",
         "X:CREEKSTONE DR & TERRACE PINE DR");
 
     doTest("T12",
         "(CAD:) ASST PD SEIZURE;1125 W NC 54 HWY;[LAW] {A324} CON BREATHING AND ALERT [09/10/11 01:52:07 MITCHELLM] Event spawned from INTOXICATED DRIVER. [09/10/2011 01:51",
         "CALL:ASST PD SEIZURE",
         "ADDR:1125 W NC 54 HWY",
         "MADDR:1125 W NC 54",
         "UNIT:A324",
         "INFO:CON BREATHING AND ALERT / Event spawned from INTOXICATED DRIVER.",
         "DATE:09/10/11",
         "TIME:01:52:07");
 
     doTest("T13",
         "CAD:SICK PERSON;6234 DELLO ST;***caller req no sirens*** [03/06/12 02:05:55 HOWARDS] ;DONLEE DR;DOYLE RD",
         "CALL:SICK PERSON",
         "ADDR:6234 DELLO ST",
         "INFO:***caller req no sirens*** / DONLEE DR / DOYLE RD",
         "DATE:03/06/12",
         "TIME:02:05:55");
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "(CAD:ALLERGIES;249 COUNTRY CLUB DR;GREEN LANE DR;GREEN LANE DR)  ",
         "CALL:ALLERGIES",
         "ADDR:249 COUNTRY CLUB DR",
         "X:GREEN LANE DR & GREEN LANE DR");
 
     doTest("T2",
         "(CAD:FALL;5600 N ROXBORO ST;GIVEN BY CALLER  [05/26/12 05)  47:42 ROBINSONC] 9194795652 [05/26/12 05:47:34 ROBINSONC] ;GOODWIN RD;N ROXBORO RD",
         "CALL:FALL",
         "ADDR:5600 N ROXBORO ST",
         "INFO:GIVEN BY CALLER / 9194795652 / GOODWIN RD / N ROXBORO RD",
         "DATE:05/26/12",
         "TIME:05:47:42");
 
     doTest("T3",
         "(CAD:EYE PROBLEM;5600 N ROXBORO ST;no eval  [05/27/12 15)  59:18 TANKSLEY] ;GOODWIN RD;N ROXBORO RD",
         "CALL:EYE PROBLEM",
         "ADDR:5600 N ROXBORO ST",
         "INFO:no eval / GOODWIN RD / N ROXBORO RD",
         "DATE:05/27/12",
         "TIME:15:59:18");
 
     doTest("T4",
         "(CAD:BREATHING PROBLEM;2202 MILTON RD;WHITT RD;EDSEL RD)  ",
         "CALL:BREATHING PROBLEM",
         "ADDR:2202 MILTON RD",
         "X:WHITT RD & EDSEL RD");
 
     doTest("T5",
         "(CAD:)  DIABETIC PROBLEM;5600 N ROXBORO ST;patient has not been evaluated by nurse or doctor- [05/27/12 12:06:40 SNYDERS] ;GOODWIN RD;N ROXBORO RD",
         "CALL:DIABETIC PROBLEM",
         "ADDR:5600 N ROXBORO ST",
         "INFO:patient has not been evaluated by nurse or doctor- / GOODWIN RD / N ROXBORO RD",
         "DATE:05/27/12",
         "TIME:12:06:40");
 
     doTest("T6",
         "(CAD:UNCONSCIOUS;5617 LAUREL CREST DR;GOODWIN RD)  ",
         "CALL:UNCONSCIOUS",
         "ADDR:5617 LAUREL CREST DR",
         "X:GOODWIN RD");
 
     doTest("T7",
         "(CAD:FIRE ALARM;6014 GUESS RD;OPS1  [05/27/12 09)  48:49 WEAVERM] hall smoke detector [05/27/12 09:48:40 MILLS] ;CARDENS CREEK DR;MILTON RD",
         "CALL:FIRE ALARM",
         "ADDR:6014 GUESS RD",
         "CH:OPS1",
         "INFO:hall smoke detector / CARDENS CREEK DR / MILTON RD",
         "DATE:05/27/12",
         "TIME:09:48:49");
 
     doTest("T8",
         "(CAD:UNCONSCIOUS;5616 RUSSELL RD;FITZFORD CT;LAKE VISTA DR)  ",
         "CALL:UNCONSCIOUS",
         "ADDR:5616 RUSSELL RD",
         "X:FITZFORD CT & LAKE VISTA DR");
 
     doTest("T9",
         "(CAD:)  STRUCTURE FIRE;7121 BLALOCK RD;UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE [05/25/12 16:21:27 SMITHK] OPS2 O [05/25/12 16:20:44 SMITHK] ACROSS THE STREET [05/25/12 16:20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE ",
         "CALL:STRUCTURE FIRE",
         "ADDR:7121 BLALOCK RD",
         "INFO:UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE / OPS2 O / ACROSS THE STREET / house on fire / WILLARDVILLE",
         "DATE:05/25/12",
         "TIME:16:21:27");
 
     doTest("T10",
         "(CAD:STRUCTURE FIRE;7121 BLALOCK RD;ACROSS THE STREET  [05/25/12 16)  20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE STATION RD;SEINEVIEW LN",
         "CALL:STRUCTURE FIRE",
         "ADDR:7121 BLALOCK RD",
         "X:ACROSS THE STREET",
         "INFO:house on fire / WILLARDVILLE STATION RD / SEINEVIEW LN",
         "DATE:05/25/12",
         "TIME:16:20:26");
 
     doTest("T11",
         "(CAD:ALLERGIES;249 COUNTRY CLUB DR;GREEN LANE DR;GREEN LANE DR) ",
         "CALL:ALLERGIES",
         "ADDR:249 COUNTRY CLUB DR",
         "X:GREEN LANE DR & GREEN LANE DR");
 
     doTest("T12",
         "(CAD:FALL;5600 N ROXBORO ST;GIVEN BY CALLER  [05/26/12 05) 47:42 ROBINSONC] 9194795652 [05/26/12 05:47:34 ROBINSONC] ;GOODWIN RD;N ROXBORO RD",
         "CALL:FALL",
         "ADDR:5600 N ROXBORO ST",
         "INFO:GIVEN BY CALLER / 9194795652 / GOODWIN RD / N ROXBORO RD",
         "DATE:05/26/12",
         "TIME:05:47:42");
 
     doTest("T13",
         "(CAD:EYE PROBLEM;5600 N ROXBORO ST;no eval  [05/27/12 15) 59:18 TANKSLEY] ;GOODWIN RD;N ROXBORO RD",
         "CALL:EYE PROBLEM",
         "ADDR:5600 N ROXBORO ST",
         "INFO:no eval / GOODWIN RD / N ROXBORO RD",
         "DATE:05/27/12",
         "TIME:15:59:18");
 
     doTest("T14",
         "(CAD:BREATHING PROBLEM;2202 MILTON RD;WHITT RD;EDSEL RD) ",
         "CALL:BREATHING PROBLEM",
         "ADDR:2202 MILTON RD",
         "X:WHITT RD & EDSEL RD");
 
     doTest("T15",
         "(CAD:) DIABETIC PROBLEM;5600 N ROXBORO ST;patient has not been evaluated by nurse or doctor- [05/27/12 12:06:40 SNYDERS] ;GOODWIN RD;N ROXBORO RD",
         "CALL:DIABETIC PROBLEM",
         "ADDR:5600 N ROXBORO ST",
         "INFO:patient has not been evaluated by nurse or doctor- / GOODWIN RD / N ROXBORO RD",
         "DATE:05/27/12",
         "TIME:12:06:40");
 
     doTest("T16",
         "(CAD:UNCONSCIOUS;5617 LAUREL CREST DR;GOODWIN RD) ",
         "CALL:UNCONSCIOUS",
         "ADDR:5617 LAUREL CREST DR",
         "X:GOODWIN RD");
 
     doTest("T17",
         "(CAD:FIRE ALARM;6014 GUESS RD;OPS1  [05/27/12 09) 48:49 WEAVERM] hall smoke detector [05/27/12 09:48:40 MILLS] ;CARDENS CREEK DR;MILTON RD",
         "CALL:FIRE ALARM",
         "ADDR:6014 GUESS RD",
         "CH:OPS1",
         "INFO:hall smoke detector / CARDENS CREEK DR / MILTON RD",
         "DATE:05/27/12",
         "TIME:09:48:49");
 
     doTest("T18",
         "(CAD:) TRASH FIRE;800 KNIGHT DR;UNK WHAT MATERIAL IS BURNING IN THE BARRELL- [05/29/12 10:56:20 SNYDERS] CALLER WAS ADVISED TO CALL WHEN HIS NEIGHBOR IS BURNING A FIRE IN THE BACK OF HIS HOUSE, NOT THREATENING A STRUCTURE [05/29/;GEOFFRY RD;PR",
         "CALL:TRASH FIRE",
         "ADDR:800 KNIGHT DR",
         "INFO:UNK WHAT MATERIAL IS BURNING IN THE BARRELL- / CALLER WAS ADVISED TO CALL WHEN HIS NEIGHBOR IS BURNING A FIRE IN THE BACK OF HIS HOUSE, NOT THREATENING A STRUCTURE",
         "DATE:05/29/12",
         "TIME:10:56:20");
 
     doTest("T19",
         "(CAD:) STRUCTURE FIRE;7121 BLALOCK RD;UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE [05/25/12 16:21:27 SMITHK] OPS2 O [05/25/12 16:20:44 SMITHK] ACROSS THE STREET [05/25/12 16:20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE ",
         "CALL:STRUCTURE FIRE",
         "ADDR:7121 BLALOCK RD",
         "INFO:UNABLE TO CONFIRM IF ANYONE IS IN THE HOUSE / OPS2 O / ACROSS THE STREET / house on fire / WILLARDVILLE",
         "DATE:05/25/12",
         "TIME:16:21:27");
 
     doTest("T20",
         "(CAD:STRUCTURE FIRE;7121 BLALOCK RD;ACROSS THE STREET  [05/25/12 16) 20:26 SMITHK] house on fire [05/25/12 16:20:19 SMITHK] ;WILLARDVILLE STATION RD;SEINEVIEW LN",
         "CALL:STRUCTURE FIRE",
         "ADDR:7121 BLALOCK RD",
         "X:ACROSS THE STREET",
         "INFO:house on fire / WILLARDVILLE STATION RD / SEINEVIEW LN",
         "DATE:05/25/12",
         "TIME:16:20:26");
 
     doTest("T21",
         "(CAD:SICK PERSON;429 CONTINENTAL DR;attempting to get further  [05/28/12 18) 21:03 DILLARD] male in the frt yard not feeling good -- [05/28/12 18:20:50 DILLARD] ;LARK LN;GREEN LANE DR",
         "CALL:SICK PERSON",
         "ADDR:429 CONTINENTAL DR",
         "INFO:attempting to get further / male in the frt yard not feeling good -- / LARK LN / GREEN LANE DR",
         "DATE:05/28/12",
         "TIME:18:21:03");
 
     doTest("T22",
         "(CAD:INVESTIGATION;109 CAPTAINS CT;EITHER 107 OR 109  [06/02/12 18) 46:18 GLOSSON] open fire.... [06/02/12 18:46:01 GLOSSON] ;CRAIG RD",
         "CALL:INVESTIGATION",
         "ADDR:109 CAPTAINS CT",
         "INFO:EITHER 107 OR 109 / open fire.... / CRAIG RD",
         "DATE:06/02/12",
         "TIME:18:46:18");
 
    doTest("T23",
        "[CAD:SICK PERSON;1207 TORREDGE RD;[Medical Priority Info] PROBLEM] mom has fever and throwing up # PATS: 1 AGE: 85 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes [06/19/12 15:19:27 HOLP] 919-477-3013 ph number per ANI [0;GLENOAKS DR;COLLINS D",
        "CALL:SICK PERSON",
        "ADDR:1207 TORREDGE RD",
        "INFO:mom has fever and throwing up # PATS: 1 AGE: 85 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes / 919-477-3013 ph number per ANI",
        "DATE:06/19/12",
        "TIME:15:19:27");

   }
   
   public static void main(String[] args) {
     new NCDurhamCountyParserTest().generateTests("T1");
   }
 }
