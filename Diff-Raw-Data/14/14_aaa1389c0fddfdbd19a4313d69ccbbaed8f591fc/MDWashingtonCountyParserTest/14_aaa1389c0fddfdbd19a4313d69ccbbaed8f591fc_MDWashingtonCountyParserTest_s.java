 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDWashingtonCountyParserTest extends BaseParserTest {
   
   public MDWashingtonCountyParserTest() {
     setParser(new MDWashingtonCountyParser(), "WASHINGTON COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "MAPLEVILLE RD / CHEWSVILLE RD - PIC, PERS INJURY COLLISION - CO16,RSQE7,R79,SO20 - 1106957 15:38",
         "ADDR:MAPLEVILLE RD & CHEWSVILLE RD",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:CO16,RSQE7,R79,SO20",
         "ID:1106957",
         "TIME:15:38");
 
     doTest("T2",
         "73 S MAIN ST - FALLS,BACK INJURIES - R79 - 1106940 12:07 2nd ALERT!",
         "ADDR:73 S MAIN ST",
         "CALL:FALLS,BACK INJURIES",
         "UNIT:R79",
         "ID:1106940",
         "INFO:2nd ALERT!",
         "TIME:12:07");
 
     doTest("T3",
         "14518 MARYLAND AVE - STROKE - FAA4,R79,CO9 - 1106937 11:31",
         "ADDR:14518 MARYLAND AVE",
         "CALL:STROKE",
         "UNIT:FAA4,R79,CO9",
         "ID:1106937",
         "TIME:11:31");
 
     doTest("T4",
         "13730 MAUGANSVILLE RD, MAUGANSVILLE FIRE COMPANY - TRANSFER OF UNIT, - CO26,CO9 - 1106889 16:55",
         "ADDR:13730 MAUGANSVILLE RD",
         "PLACE:MAUGANSVILLE FIRE COMPANY",
         "CALL:TRANSFER OF UNIT,",
         "UNIT:CO26,CO9",
         "ID:1106889",
         "TIME:16:55");
 
     doTest("T5",
         "LEHMANS MILL RD - WATER RESCUE - - CO9,RS13,R75,BT20,SO20,RS2001,D20,M753,RSQE91,DC9,R79 - 1106823 23:27 2nd ALERT!",
         "ADDR:LEHMANS MILL RD",
         "CALL:WATER RESCUE",
         "UNIT:CO9,RS13,R75,BT20,SO20,RS2001,D20,M753,RSQE91,DC9,R79",
         "ID:1106823",
         "INFO:2nd ALERT!",
         "TIME:23:27");
 
     doTest("T6",
         "FRE CO, N FRANKLINVILLE RD/RT15 - THURMONT - WATER RESCUE - - RSQE7 - TAC 30 1106797 21:01",
         "ADDR:N FRANKLINVILLE RD & RT15",
         "MADDR:N FRANKLINVILLE RD & RT 15",
         "CITY:THURMONT",
         "CALL:WATER RESCUE",
         "UNIT:RSQE7",
         "ID:1106797",
         "INFO:TAC 30",
         "TIME:21:01");
 
     doTest("T7",
         "FRE CO, 15204 QUIRAUK SCHOOL RD - NAT GAS LEAK - CO7,R79,P791,RSQE7 - FIRE 60 1106779 19:49",
         "ADDR:15204 QUIRAUK SCHOOL RD",
         "CITY:FREDERICK COUNTY",
         "CALL:NAT GAS LEAK",
         "UNIT:CO7,R79,P791,RSQE7",
         "ID:1106779",
         "INFO:FIRE 60",
         "TIME:19:49");
 
     doTest("T8",
         "21348 OLD FORGE RD, ST PETERS BEARD LUTHERAN CHURCH - AUTOMATIC FIRE ALARM - COMMERCIAL , IND - CO9 - 1106753 16:45",
         "ADDR:21348 OLD FORGE RD",
         "PLACE:ST PETERS BEARD LUTHERAN CHURCH",
         "CALL:AUTOMATIC FIRE ALARM - COMMERCIAL , IND",
         "UNIT:CO9",
         "ID:1106753",
         "TIME:16:45");
 
     doTest("T9",
         "INTERSTATE 70, EXIT35 I70 EB - PIC, PERS INJURY COLLISION - CO16,CO10,R75,R79,E101,P791,P751,SUP160,DC10,RS10,E161,CA75 - 1106746 16:21",
         "ADDR:INTERSTATE 70",
         "MADDR:EXIT35 I70 EB,INTERSTATE 70",
         "PLACE:EXIT35 I70 EB",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:CO16,CO10,R75,R79,E101,P791,P751,SUP160,DC10,RS10,E161,CA75",
         "ID:1106746",
         "TIME:16:21");
 
     doTest("T10",
         "22507 JEFFERSON BLVD, AC&T SMITHSBURG - EYE PROBLEMS,INJURIES - R79 - 1106670 16:20",
         "ADDR:22507 JEFFERSON BLVD",
         "PLACE:AC&T SMITHSBURG",
         "CALL:EYE PROBLEMS,INJURIES",
         "UNIT:R79",
         "ID:1106670",
         "TIME:16:20");
 
     doTest("T11",
         "20354 LEITERSBURG PIKE, PEN MAR TRAILER SALES INC - NAT GAS ODOR - CO9 - 1106635 00:13",
         "ADDR:20354 LEITERSBURG PIKE",
         "PLACE:PEN MAR TRAILER SALES INC",
         "CALL:NAT GAS ODOR",
         "UNIT:CO9",
         "ID:1106635",
         "TIME:00:13");
 
     doTest("T12",
         "LEITERSBURG PIKE / MILLERS CHURCH RD - PIC, PERS INJURY COLLISION - CO9,R75,P753,R79 - 1106553 22:22",
         "ADDR:LEITERSBURG PIKE & MILLERS CHURCH RD",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:CO9,R75,P753,R79",
         "ID:1106553",
         "TIME:22:22");
 
     doTest("T13",
         "MAPLEVILLE RD / JEFFERSON BLVD - PIC, PERS INJURY COLLISION - CO7,R79,M791,SUP70,P792,C79,RSQE7 - 1106339 12:40",
         "ADDR:MAPLEVILLE RD & JEFFERSON BLVD",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:CO7,R79,M791,SUP70,P792,C79,RSQE7",
         "ID:1106339",
         "TIME:12:40");
 
     doTest("T14",
         "INTERSTATE 70, 32MM I70 AT REST AREA - ABDOMINAL PAIN - PROBLEMS - R75,R79,CO10 - 1106419 16:28\nDo Not REPLY to this email!!!",
         "ADDR:INTERSTATE 70",
         "MADDR:32MM I70 AT REST AREA,INTERSTATE 70",
         "PLACE:32MM I70 AT REST AREA",
         "CALL:ABDOMINAL PAIN - PROBLEMS",
         "UNIT:R75,R79,CO10",
         "ID:1106419",
         "TIME:16:28");
 
     doTest("T15",
         "CHEWSVILLE RD / WHITE HALL RD, AREA - SMOKE INVESTIGATION - CO16,SO20,CO7,RS20,E161,E74,ET7 - 1117561 07:46\nDo Not REPLY to this email!!!",
         "ADDR:CHEWSVILLE RD & WHITE HALL RD",
         "PLACE:AREA",
         "CALL:SMOKE INVESTIGATION",
         "UNIT:CO16,SO20,CO7,RS20,E161,E74,ET7",
         "ID:1117561",
         "TIME:07:46");
    
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "S:WCo M:[!] 136 WISHING STAR CT  ,STAB  ,GUNSHOT  ,R75  ,CO10  ,1120268 23:48\n\n\n",
         "SRC:WCo",
         "ADDR:136 WISHING STAR CT",
         "CALL:STAB - GUNSHOT",
         "UNIT:R75,CO10",
         "ID:1120268",
         "TIME:23:48");
 
     doTest("T2",
         "S:WCo M:[!] 11403 STONECROFT CT  , STONECROFT APARTMENTS BUILDING A APT 209  ,CHEST PAIN  ,R75  ,CO10  ,1120271 02:16",
         "SRC:WCo",
         "ADDR:11403 STONECROFT CT",
         "CALL:STONECROFT APARTMENTS BUILDING A APT 209 - CHEST PAIN",
         "UNIT:R75,CO10",
         "ID:1120271",
         "TIME:02:16");
 
     doTest("T3",
         "S:WCo M:[!] 1800 DUAL HWY  , SUSQUEHANNA BANK STE 100 REAR OF  ,SMOKE INVESTIGATION  ,E03  ,CO10  ,UT3  ,1120229 07:12",
         "SRC:WCo",
         "ADDR:1800 DUAL HWY",
         "CALL:SUSQUEHANNA BANK STE 100 REAR OF - SMOKE INVESTIGATION",
         "UNIT:E03,CO10,UT3",
         "ID:1120229",
         "TIME:07:12");
 
     doTest("T4",
         "S:WCo M:[!] 300 N COLONIAL DR  , APT E  ,HOUSE FIRE  ,CO10  ,E03  ,E01  ,TRK1  ,S75  ,R75  ,SO20  ,1120236 09:48",
         "SRC:WCo",
         "ADDR:300 N COLONIAL DR",
         "CALL:APT E - HOUSE FIRE",
         "UNIT:CO10,E03,E01,TRK1,S75,R75,SO20",
         "ID:1120236",
         "TIME:09:48");
 
     doTest("T5",
         "S:WCo M:[!] 136 WISHING STAR CT  ,STAB  ,GUNSHOT  ,R75  ,CO10  ,1120268 23:48",
         "SRC:WCo",
         "ADDR:136 WISHING STAR CT",
         "CALL:STAB - GUNSHOT",
         "UNIT:R75,CO10",
         "ID:1120268",
         "TIME:23:48");
 
     doTest("T6",
         "S:WCo M:[!] 17567 YORK RD  , WESTERN SIZZLIN STEAK HOUSE  ,ODOR OF SMOKE  ,CO26  ,CO10  ,CO2  ,TWR26  ,RS10  ,R26  ,1120262 22:1",
         "SRC:WCo",
         "ADDR:17567 YORK RD",
         "CALL:WESTERN SIZZLIN STEAK HOUSE - ODOR OF SMOKE",
         "UNIT:CO26,CO10,CO2,TWR26,RS10,R26",
         "ID:1120262");
     
   }
   
   @Test
   public void testparser3() {
 
     doTest("T1",
         
         "WCo / [!] 1396 S POTOMAC ST, SHEETZ - PIC, PERS INJURY COLLISION - E05,R75,UT3 - 1200168 10:39\n\n",
         "SRC:WCo",
         "ADDR:1396 S POTOMAC ST",
         "PLACE:SHEETZ",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:E05,R75,UT3",
         "ID:1200168",
         "TIME:10:39");
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "Subject: WCo\n" +
         "[!] 17850 GARLAND GROH BLVD, SUPER WALMART CENTER - Accident - Property Damage - 9C16 - Traffic / Transportation Accident (Crash). 11:57",
 
         "SRC:WCo",
         "ADDR:17850 GARLAND GROH BLVD",
         "PLACE:SUPER WALMART CENTER",
         "CALL:Accident - Property Damage",
         "UNIT:9C16",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:11:57");
 
     doTest("T2",
         "Subject: WCo\n[!] INTERSTATE 81 / I70 - PIC, PERS INJURY COLLISION - CO26,CO2,RSQE2,R26,R2 - 1208763 11:59",
         "SRC:WCo",
         "ADDR:INTERSTATE 81 & I70",
         "MADDR:INTERSTATE 81 & I 70",
         "CALL:PIC, PERS INJURY COLLISION",
         "UNIT:CO26,CO2,RSQE2,R26,R2",
         "ID:1208763",
         "TIME:11:59");
 
     doTest("T3",
         "Subject: WCo\n" +
         "[!] VIRGINIA AVE / W WILSON BLVD - Accident - Property Damage - 9C21 - Traffic / Transportation Accident (Crash). 12:10",
 
         "SRC:WCo",
         "ADDR:VIRGINIA AVE & W WILSON BLVD",
         "CALL:Accident - Property Damage",
         "UNIT:9C21",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:12:10");
 
     doTest("T4",
         "Subject: WCo\n" +
         "[!] 222 OTHO HOLLAND DR - UNCONSCIOUSNESS,FAINTING - R26 - 87 year old, Female, Unconscious, Breathing. - **ALS HOT*- Unconscious – Effective breathing 1208767 12:41 2nd ALERT!",
 
         "SRC:WCo",
         "ADDR:222 OTHO HOLLAND DR",
         "CALL:UNCONSCIOUSNESS,FAINTING",
         "UNIT:R26",
         "INFO:87 year old, Female, Unconscious, Breathing. - **ALS HOT*- Unconscious – Effective breathing - 2nd ALERT!",
         "ID:1208767",
         "TIME:12:41");
 
     doTest("T5",
         "Subject: WCo\n[!] 365 WOODPOINT AVE - PSYCHIATRIC,SUICIDE ATTEMPT - R26,E04 - Mental Disorder (Behavioral Problems). 1208777 13:32",
         "SRC:WCo",
         "ADDR:365 WOODPOINT AVE",
         "CALL:PSYCHIATRIC,SUICIDE ATTEMPT",
         "UNIT:R26,E04",
         "INFO:Mental Disorder (Behavioral Problems).",
         "ID:1208777",
         "TIME:13:32");
 
     doTest("T6",
         "Subject: WCo\n" +
         "[!] 11346 LAKESIDE DR - FALLS,BACK INJURIES - R49,CO26 - 85 year old, Male, Conscious, Breathing. - **BLS-ALPHA *** On ground or floor - PUBLIC ASSIST (no injuries and no priority symptoms) 1208778 13:36",
 
         "SRC:WCo",
         "ADDR:11346 LAKESIDE DR",
         "CALL:FALLS,BACK INJURIES",
         "UNIT:R49,CO26",
         "INFO:85 year old, Male, Conscious, Breathing. - **BLS-ALPHA *** On ground or floor - PUBLIC ASSIST (no injuries and no priority symptoms)",
         "ID:1208778",
         "TIME:13:36");
 
     doTest("T7",
         "Subject: WCo\n" +
         "[!] 1321 S POTOMAC ST, E RUSSELL HICKS MIDDLE SCHOOL - Accident - Property Damage - 9C20 - Traffic / Transportation Accident (Crash). 16:01",
 
         "SRC:WCo",
         "ADDR:1321 S POTOMAC ST",
         "PLACE:E RUSSELL HICKS MIDDLE SCHOOL",
         "CALL:Accident - Property Damage",
         "UNIT:9C20",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:16:01");
 
     doTest("T8",
         "Subject: WCo\n[!] JONATHAN ST / CHURCH ST - Accident - Property Damage - 9E32 - 16:01",
         "SRC:WCo",
         "ADDR:JONATHAN ST & CHURCH ST",
         "CALL:Accident - Property Damage",
         "UNIT:9E32",
         "TIME:16:01");
 
     doTest("T9",
         "Subject: WCo\n" +
         "[!] 14 N POTOMAC ST - OUTSIDE FIRE - E01,UT3 - ProQA Fire abort reason: DISCONNECTED CALL. Outside Fire. 1208845 11:22",
 
         "SRC:WCo",
         "ADDR:14 N POTOMAC ST",
         "CALL:OUTSIDE FIRE",
         "UNIT:E01,UT3",
         "INFO:ProQA Fire abort reason: DISCONNECTED CALL. Outside Fire.",
         "ID:1208845",
         "TIME:11:22");
 
     doTest("T10",
         "Subject: WCo\n" +
         "[!] 18 DOWNSVILLE RD - CHEST PAIN - R75,P751,R26,E05 - 81 year old, Male, Conscious, Breathing. - **ALS HOT*-Abnormal breathing 1208790 16:29",
 
         "SRC:WCo",
         "ADDR:18 DOWNSVILLE RD",
         "CALL:CHEST PAIN",
         "UNIT:R75,P751,R26,E05",
         "INFO:81 year old, Male, Conscious, Breathing. - **ALS HOT*-Abnormal breathing",
         "ID:1208790",
         "TIME:16:29");
 
     doTest("T11",
         "Subject: WCo\n" +
         "[!] 601 DUAL HWY, SUPER SHOE STORE - Accident - Property Damage - 9A13 - Traffic / Transportation Accident (Crash). 16:41",
 
         "SRC:WCo",
         "ADDR:601 DUAL HWY",
         "PLACE:SUPER SHOE STORE",
         "CALL:Accident - Property Damage",
         "UNIT:9A13",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:16:41");
 
     doTest("T12",
         "Subject: WCo\n" +
         "[!] 16533 TAMMANY LN - MEDICAL EMERGENCY - R26 - 83 year old, Female, Conscious, Breathing. - **BLS-ALPHA *** No priority symptoms (complaint conditions 2–11 not identified) 1208833 06:06",
 
         "SRC:WCo",
         "ADDR:16533 TAMMANY LN",
         "CALL:MEDICAL EMERGENCY",
         "UNIT:R26",
         "INFO:83 year old, Female, Conscious, Breathing. - **BLS-ALPHA *** No priority symptoms (complaint conditions 2–11 not identified)",
         "ID:1208833",
         "TIME:06:06");
 
     doTest("T13",
         "Subject: CAD\n" +
         "[!] 895 W WASHINGTON ST, IFO - FUEL,GAS,OIL,ANTIFREEZE,DIESEL SPILL - E04,UT3 - Fuel Spill. - UNKNOWN SITUATION (Investigation)-Outside 1208801 17:43",
 
         "ADDR:895 W WASHINGTON ST",
         "PLACE:IFO",
         "CALL:FUEL,GAS,OIL,ANTIFREEZE,DIESEL SPILL",
         "UNIT:E04,UT3",
         "INFO:Fuel Spill. - UNKNOWN SITUATION (Investigation)-Outside",
         "ID:1208801",
         "TIME:17:43");
 
     doTest("T14",
         "Subject: WCo\n" +
         "[!] 16843 LONGFELLOW CT - FALLS,BACK INJURIES - R26 - 86 year old, Female, Conscious, Breathing. - **BLS HOT*** On ground or floor - Unknown status/Other codes not applicable 1208807 18:41",
 
         "SRC:WCo",
         "ADDR:16843 LONGFELLOW CT",
         "CALL:FALLS,BACK INJURIES",
         "UNIT:R26",
         "INFO:86 year old, Female, Conscious, Breathing. - **BLS HOT*** On ground or floor - Unknown status/Other codes not applicable",
         "ID:1208807",
         "TIME:18:41");
 
     doTest("T15",
         "Subject: WCo\n" +
         "[!] 151 N BURHANS BLVD, GOODWILL INDUSTRIES - Accident - Property Damage - 9A17 - Traffic / Transportation Accident (Crash). 18:59",
 
         "SRC:WCo",
         "ADDR:151 N BURHANS BLVD",
         "PLACE:GOODWILL INDUSTRIES",
         "CALL:Accident - Property Damage",
         "UNIT:9A17",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:18:59");
 
     doTest("T16",
         "Subject: WCo\n" +
         "[!] 300 NORTHERN AVE, LONGMEADOW APTS. BLDG 3 APT A - UNSPECIFIED ALARM - E02,CO27,TRK1,UT3,BC1 - Alarms. - HIGH RISE-UNKNOWN 1208818 21:25",
 
         "SRC:WCo",
         "ADDR:300 NORTHERN AVE",
         "PLACE:LONGMEADOW APTS. BLDG 3 APT A",
         "CALL:UNSPECIFIED ALARM",
         "UNIT:E02,CO27,TRK1,UT3,BC1",
         "INFO:Alarms. - HIGH RISE-UNKNOWN",
         "ID:1208818",
         "TIME:21:25");
 
     doTest("T17",
         "Subject: WCo\n" +
         "[!] 10138 ST GEORGE CIR - CONVULSIONS,SEIZURE - R26,CO10 - 54 year old, Male, Unconscious, Breathing. - **ALS HOT*-CONTINUOUS or MULTIPLE seizure 1208819 21:38",
 
         "SRC:WCo",
         "ADDR:10138 ST GEORGE CIR",
         "CALL:CONVULSIONS,SEIZURE",
         "UNIT:R26,CO10",
         "INFO:54 year old, Male, Unconscious, Breathing. - **ALS HOT*-CONTINUOUS or MULTIPLE seizure",
         "ID:1208819",
         "TIME:21:38");
 
     doTest("T18",
         "Subject: CAD\n" +
         "[!] 860 PINE ST - UNCONSCIOUSNESS,FAINTING - S754,E05,UT3 - 44 year old, Male, Unconscious, Breathing. - **ALS HOT*- Unconscious – AGONAL/INEFFECTIVE BREATHING 1208821 00:09",
 
         "ADDR:860 PINE ST",
         "CALL:UNCONSCIOUSNESS,FAINTING",
         "UNIT:S754,E05,UT3",
         "INFO:44 year old, Male, Unconscious, Breathing. - **ALS HOT*- Unconscious – AGONAL/INEFFECTIVE BREATHING",
         "ID:1208821",
         "TIME:00:09");
 
     doTest("T19",
         "Subject: CAD\n" +
         "[!] 12927 LITTLE ELLIOTT DR APT 9 - CARDIAC,RESPIRATORY ARREST - R75,E03,UT3 - 26 year old, Male, Unconscious, Not breathing. - **ALS HOT*-ECHO- Not breathing at all 1208824 00:45",
 
         "ADDR:12927 LITTLE ELLIOTT DR",
         "CALL:CARDIAC,RESPIRATORY ARREST",
         "UNIT:R75,E03,UT3",
         "INFO:26 year old, Male, Unconscious, Not breathing. - **ALS HOT*-ECHO- Not breathing at all",
         "APT:9",
         "ID:1208824",
         "TIME:00:45");
 
     doTest("T20",
         "Subject: WCo\n[!] SHARPSBURG PIKE - UNCONSCIOUSNESS,FAINTING - R26 - 1208825 01:39",
         "SRC:WCo",
         "ADDR:SHARPSBURG PIKE",
         "CALL:UNCONSCIOUSNESS,FAINTING",
         "UNIT:R26",
         "ID:1208825",
         "TIME:01:39");
 
     doTest("T21",
         "Subject: WCo\n[!] 443 S BURHANS BLVD, HAGERSTOWN MOPED - Accident - Property Damage - 9C21 - 11:47",
         "SRC:WCo",
         "ADDR:443 S BURHANS BLVD",
         "PLACE:HAGERSTOWN MOPED",
         "CALL:Accident - Property Damage",
         "UNIT:9C21",
         "TIME:11:47");
 
     doTest("T22",
         "Subject: CAD\n" +
         "[!] 243 SUMMIT AVE, - CHEST PAIN - R75,E02,UT3 - 42 year old, Female, Conscious, Breathing. - **ALS HOT*- Breathing normally =>35 1208849 11:54",
 
         "ADDR:243 SUMMIT AVE",
         "CALL:CHEST PAIN",
         "UNIT:R75,E02,UT3",
         "INFO:42 year old, Female, Conscious, Breathing. - **ALS HOT*- Breathing normally =>35",
         "ID:1208849",
         "TIME:11:54");
 
     doTest("T23",
         "Subject: WCo\n[!] 38 E NORTH AVE - Accident - Property Damage - 9C17 - 1. Traffic / Transportation Accident (Crash). 12:08",
         "SRC:WCo",
         "ADDR:38 E NORTH AVE",
         "CALL:Accident - Property Damage",
         "UNIT:9C17",
         "INFO:1. Traffic / Transportation Accident (Crash).",
         "TIME:12:08");
 
     doTest("T24",
         "Subject: WCo\n" +
         "[!] 148 SUNFLOWER DR - DIABETIC PROBLEMS - R26 - 83 year old, Female, Conscious, Breathing. - **BLS-ALPHA**- Alert and behaving normally 1208783 14:48",
 
         "SRC:WCo",
         "ADDR:148 SUNFLOWER DR",
         "CALL:DIABETIC PROBLEMS",
         "UNIT:R26",
         "INFO:83 year old, Female, Conscious, Breathing. - **BLS-ALPHA**- Alert and behaving normally",
         "ID:1208783",
         "TIME:14:48");
 
     doTest("T25",
         "Subject: CAD\n" +
         "[!] 11110 MEDICAL CAMPUS RD, ROBINWOOD URGENT CARE- STE.147(BLU) - STROKE - R75,P756,CO10,UT3 - 80 year old, Male, Conscious, Breathing. - *ALS HOT*- Sudden weakness or numbness (one side)- (No test evidence (Less than 3 hrs) 1208754 10:05",
 
         "ADDR:11110 MEDICAL CAMPUS RD",
         "PLACE:ROBINWOOD URGENT CARE- STE.147(BLU)",
         "CALL:STROKE",
         "UNIT:R75,P756,CO10,UT3",
         "INFO:80 year old, Male, Conscious, Breathing. - *ALS HOT*- Sudden weakness or numbness (one side)- (No test evidence (Less than 3 hrs)",
         "ID:1208754",
         "TIME:10:05");
 
     doTest("T26",
         "Subject: WCo\n[!] 234 JONATHAN ST, THREE MINUTE CAR WASH - Accident - Property Damage - 9C15 - 11:00",
         "SRC:WCo",
         "ADDR:234 JONATHAN ST",
         "PLACE:THREE MINUTE CAR WASH",
         "CALL:Accident - Property Damage",
         "UNIT:9C15",
         "TIME:11:00");
 
     doTest("T27",
         "Subject: WCo\n[!] 517 CHURCH ST - Accident - Property Damage - 9C17 - Traffic / Transportation Accident (Crash). 08:52",
         "SRC:WCo",
         "ADDR:517 CHURCH ST",
         "CALL:Accident - Property Damage",
         "UNIT:9C17",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:08:52");
 
     doTest("T28",
         "Subject: WCo\n[!] 50 W WASHINGTON ST, UNIVERSITY OF MARYLAND-HAGERSTOWN - Accident - Property Damage - 9718 - 11:38",
         "SRC:WCo",
         "ADDR:50 W WASHINGTON ST",
         "PLACE:UNIVERSITY OF MARYLAND-HAGERSTOWN",
         "CALL:Accident - Property Damage",
         "UNIT:9718",
         "TIME:11:38");
 
     doTest("T29",
         "Subject: WCo\n" +
         "[!] DUAL HWY, AMES SHOPPING CENTER - Accident - Property Damage - 9C17 - Traffic / Transportation Accident (Crash). 08:24",
 
         "SRC:WCo",
         "ADDR:DUAL HWY",
         "MADDR:AMES SHOPPING CENTER,DUAL HWY",
         "PLACE:AMES SHOPPING CENTER",
         "CALL:Accident - Property Damage",
         "UNIT:9C17",
         "INFO:Traffic / Transportation Accident (Crash).",
         "TIME:08:24");
    
   }
   
   public static void main(String[] args) {
     new MDWashingtonCountyParserTest().generateTests("T1");
   }
 }
