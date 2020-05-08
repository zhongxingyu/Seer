 package net.anei.cadpage.parsers.MO;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MORayCountyParserTest extends BaseParserTest {
   
   public MORayCountyParserTest() {
     setParser(new MORayCountyParser(), "RAY COUNTY", "MO");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "RCAD WHFD FALLS 514 ARAPAHOE DR  HOMESTEAD VILLAGE CrossStreets: CHEROKEE DR 0.05 mi W HIGHWAY Y 0.16 mi E Description: BETTY JAMES 85 YO FEMALE\nFAL",
         "SRC:RCAD WHFD",
         "CALL:FALLS",
         "ADDR:514 ARAPAHOE DR",
         "CITY:HOMESTEAD VILLAGE",
         "INFO:BETTY JAMES 85 YO FEMALE FAL",
         "X:CHEROKEE DR 0.05 mi W HIGHWAY Y 0.16 mi E");
 
     doTest("T2",
         "LFD EMS 31004 NOTTINGHAM LN   Description:  CrossStreets: SILVEY RD 6443.06 mi NW SILVEY RD 6443.06 mi NW",
         "SRC:LFD",
         "CALL:EMS",
         "ADDR:31004 NOTTINGHAM LN",
         "X:SILVEY RD 6443.06 mi NW SILVEY RD 6443.06 mi NW");
 
     doTest("T3",
         "WHFD RCAD FALLS 33408 W 160TH ST  RAY COUNTY Description: 91 yof\nfell from standing position\nconsious and breathing\ncomplaining of l hip pain\nup in",
         "SRC:WHFD RCAD",
         "CALL:FALLS",
         "ADDR:33408 W 160TH ST",
         "INFO:91 yof fell from standing position consious and breathing complaining of l hip pain up in");
 
     doTest("T4",
         "WHFD RCAD RCSO DEATH 32534 MAGNOLIA LN  RAY COUNTY Description: 61 yo female\n",
         "SRC:WHFD RCAD",
         "UNIT:RCSO",
         "CALL:DEATH",
         "ADDR:32534 MAGNOLIA LN",
         "INFO:61 yo female");
 
     doTest("T5",
         "WHFD RCAD TRAUMATIC INJURIES (SPECIFIC) 2062 E RIDGE DR  RAY COUNTY Description: 15 YO MALE\nBICYLCE ACCIDENT\nFELL FROM BIKE\nCONSCIOUS\nBREATHING\nNO S",
         "SRC:WHFD RCAD",
         "CALL:TRAUMATIC INJURIES (SPECIFIC)",
         "ADDR:2062 E RIDGE DR",
         "INFO:15 YO MALE BICYLCE ACCIDENT FELL FROM BIKE CONSCIOUS BREATHING NO S");
 
     doTest("T6",
         "WHFD RCAD M43 800 ABDOMINAL PAIN 12595 ORRICK RD  RAY COUNTY Description: 28 YOLD FEMALE \nCONS AND BREATH \nSEVERE STOMACH PAIN LOWER ABDOMEN \nLUPUS",
         "SRC:WHFD RCAD",
         "UNIT:M43 800",
         "CALL:ABDOMINAL PAIN",
         "ADDR:12595 ORRICK RD",
         "INFO:28 YOLD FEMALE  CONS AND BREATH  SEVERE STOMACH PAIN LOWER ABDOMEN  LUPUS");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 215 E 5TH ST 3A LAWSON Description: 123254415\nPT IS PATRICIA BIDDIX\nHIGH BP\n67 YO FEMALE\nBURNING SE",
         "UNIT:LMED1 LSQ1 L602",
         "CALL:SICK PERSON (SPECIFIC DIAGNOSIS)",
         "ADDR:215 E 5TH ST 3A",
         "CITY:LAWSON",
         "INFO:123254415 PT IS PATRICIA BIDDIX HIGH BP 67 YO FEMALE BURNING SE");
 
     doTest("T2",
         "LMED1 HEART PROBLEM / AICD 618 SHEPHERD LN   Description: 17 YO Rapid Heart rate\n\n",
         "UNIT:LMED1",
         "CALL:HEART PROBLEM/AICD",
         "ADDR:618 SHEPHERD LN",
         "INFO:17 YO Rapid Heart rate");
 
     doTest("T3",
         "LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 316 KINGS DR  LAWSON Description: 90 yof\nill since for in morning\nhigh sugar\nbreathing difficulty\nu",
         "UNIT:LMED1 LSQ1 L602",
         "CALL:SICK PERSON (SPECIFIC DIAGNOSIS)",
         "ADDR:316 KINGS DR",
         "CITY:LAWSON",
         "INFO:90 yof ill since for in morning high sugar breathing difficulty u");
 
     doTest("T4",
         "LMED1 LSQ1 L602 SEIZURE 34030 W 204TH ST  RAY COUNTY Description: camp wilderness retreat\n13/f\nbreathing\ncons\nseizing at this time\n1st seizure for a",
         "UNIT:LMED1 LSQ1 L602",
         "CALL:SEIZURE",
         "ADDR:34030 W 204TH ST",
         "INFO:camp wilderness retreat 13/f breathing cons seizing at this time 1st seizure for a");
 
     doTest("T5",
         "LMED1 LSQ1 L602 LACERATION 202 E MOSS ST  LAWSON Description: puncture wound in l foot\n42 yof\nconsious and breathing\nnausea CrossStreets: S DONIPHAN",
         "UNIT:LMED1 LSQ1 L602",
         "CALL:LACERATION",
         "ADDR:202 E MOSS ST",
         "CITY:LAWSON",
         "INFO:puncture wound in l foot 42 yof consious and breathing nausea",
         "X:S DONIPHAN");
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "800 WHP1 WHT2 WHSQ1 LP4 LP1 LT1 OT2 RFD RCAD WHFD WHR1 FIRE STRUCTURE 11783 SIEGEL CEMETERY RD  RAY COUNTY Description: EXCELSIOR SPRINGS\nOUTSIDE CI",
         "SRC:RFD RCAD WHFD",
         "UNIT:800 WHP1 WHT2 WHSQ1 LP4 LP1 LT1 OT2 WHR1",
         "CALL:FIRE STRUCTURE",
         "ADDR:11783 SIEGEL CEMETERY RD",
         "INFO:EXCELSIOR SPRINGS OUTSIDE CI");
 
     doTest("T2",
         "800 WHB1 WHT2 WHFD WHR1 FIRE NATURAL COVER OR BRUSH 33637 HIGHWAY U  RAY COUNTY Description: SMALL BRUSH FIRE THAT IS NEXT TO A CROP FIELD\nRP IS STA",
         "SRC:WHFD",
         "UNIT:800 WHB1 WHT2 WHR1",
         "CALL:FIRE NATURAL COVER OR BRUSH",
         "ADDR:33637 HIGHWAY U",
         "INFO:SMALL BRUSH FIRE THAT IS NEXT TO A CROP FIELD RP IS STA");
 
     doTest("T3",
         "WHP1 800 WHFD WHR1 FIRE ALARM 34684 HIGHWAY 10  RAY COUNTY Description: gen fire alarm. covers all zones\n\nkeyholder ron rouse 816-864-4434",
         "SRC:WHFD",
         "UNIT:WHP1 800 WHR1",
         "CALL:FIRE ALARM",
         "ADDR:34684 HIGHWAY 10",
         "INFO:gen fire alarm. covers all zones  keyholder ron rouse 816-864-4434");
   }
   
   public void testParser4() {
     doBadTest("107 N RAYMORE ST  WOOD HEIGHTS WHFD RCAD RCSO 800 M42 WHR1 678 Clear: 22:48:39 Available: 23:34:02");
     doBadTest("15595 BLACKBERRY TRAIL  WHFD 800 LFD 802 LT1 LP1 WHP1 Dispatch: 5/24/2011 23:15:33 Enroute: 23:15:35 OnScene: 23:23:58");
     doBadTest("15141 W COUNTY LINE RD  RAY COUNTY WHFD RCAD 671A 675A 800 M44 WHR1 Clear: 22:20:51 Available: 23:05:20");
 
   }
 
   public static void main(String[] args) {
     new MORayCountyParserTest().generateTests("T1");
   }
 }
