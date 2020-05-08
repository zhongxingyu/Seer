 package net.anei.cadpage.parsers.AL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.AL.ALDothanParserTest;
 
 import org.junit.Test;
 
 
 public class ALDothanParserTest extends BaseParserTest {
   
   public ALDothanParserTest() {
     setParser(new ALDothanParser(), "DOTHAN", "AL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "15:49:38/SC-Service Call Non-Emergency/801 E LAFAYETTE ST/DOTHAN/14523968/ref lines fell at front door, see c404/",
         "CALL:SC-Service Call Non-Emergency",
         "ADDR:801 E LAFAYETTE ST",
         "CITY:DOTHAN",
         "ID:14523968",
         "INFO:ref lines fell at front door, see c404");
 
     doTest("T2",
         "16:31:23/SC-Service Call Non-Emergency/401 W INEZ RD/DOTHAN/TANGLEWOOD APARTMENTS/24135869/POWER LINES DOWN AT ENTRANCE, POSS SPARKING, RELAYED BY/",
         "CALL:SC-Service Call Non-Emergency",
         "ADDR:401 W INEZ RD",
         "CITY:DOTHAN",
         "PLACE:TANGLEWOOD APARTMENTS",
         "ID:24135869",
         "INFO:POWER LINES DOWN AT ENTRANCE, POSS SPARKING, RELAYED BY");
 
     doTest("T3",
         "18:35:57/MAID-Mutual Aid Request/5499 MONTGOMERY HWY INTERSECTN/DOTHAN/JOHN D ODOM RD/63981245/ECHO RESCUE --/",
         "CALL:MAID-Mutual Aid Request",
         "ADDR:5499 MONTGOMERY HWY",
         "CITY:DOTHAN",
         "X:JOHN D ODOM RD",
         "ID:63981245",
         "INFO:ECHO RESCUE --");
 
     doTest("T4",
         "18:43:58/S38B-Medical Call-Code III-Emergy/107 RIDGE RD/DOTHAN/95163482/63 YOM HEAD INJURY, S24/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:107 RIDGE RD",
         "CITY:DOTHAN",
         "ID:95163482",
         "INFO:63 YOM HEAD INJURY, S24");
 
     doTest("T5",
         "19:03:20/S22CF-Assault-Code II/1486 KINSEY RD/DOTHAN/51943628/ARM HURT, WAS PUSHED/",
         "CALL:S22CF-Assault-Code II",
         "ADDR:1486 KINSEY RD",
         "CITY:DOTHAN",
         "ID:51943628",
         "INFO:ARM HURT, WAS PUSHED");
 
     doTest("T6",
         "19:08:26/S38B-Medical Call-Code III-Emergy/1303 SUMMIT ST/DOTHAN/14529368/WEAKNESS, PAIN/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:1303 SUMMIT ST",
         "CITY:DOTHAN",
         "ID:14529368",
         "INFO:WEAKNESS, PAIN");
 
     doTest("T7",
         "19:17:49/OF-Outdoor Fire-Level 1/1241 KINSEY RD APT186/DOTHAN/JOHNSON 186/51943628/SPARKS SHOOTING FROM POLE/",
         "CALL:OF-Outdoor Fire-Level 1",
         "ADDR:1241 KINSEY RD",
         "APT:186",
         "CITY:DOTHAN",
         "PLACE:JOHNSON 186",
         "ID:51943628",
         "INFO:SPARKS SHOOTING FROM POLE");
 
     doTest("T8",
         "19:58:39/SC-Service Call Non-Emergency/175 BUFORD LN/DOTHAN/LAKEWOOD FIRE STATION/24318569/A/O -- DO NOT RESPOND/",
         "CALL:SC-Service Call Non-Emergency",
         "ADDR:175 BUFORD LN",
         "CITY:DOTHAN",
         "PLACE:LAKEWOOD FIRE STATION",
         "ID:24318569",
         "INFO:A / O -- DO NOT RESPOND");
 
     doTest("T9",
         "20:11:48/S8BF-MVC-Code III/2299 JOHN D ODOM RD INTERSECTN/DOTHAN/MURPHY MILL RD/63891245/unk further~~ pr disconnected/",
         "CALL:S8BF-MVC-Code III",
         "ADDR:2299 JOHN D ODOM RD",
         "CITY:DOTHAN",
         "X:MURPHY MILL RD",
         "ID:63891245",
         "INFO:unk further~~ pr disconnected");
 
     doTest("T10",
         "20:15:39/S38B-Medical Call-Code III-Emergy/403 S APPLETREE ST APTJ31/DOTHAN/MARVIN LEWIS J31/14523968/pt low sugar/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:403 S APPLETREE ST",
         "APT:J31",
         "CITY:DOTHAN",
         "PLACE:MARVIN LEWIS J31",
         "ID:14523968",
         "INFO:pt low sugar");
 
     doTest("T11",
         "20:14:11/S38B-Medical Call-Code III-Emergy/210 N SAINT ANDREWS ST/DOTHAN/CRIMINAL JUSTICE BUILDING/15439268/INMATE -- 31 YOM CP -- HX: 2 HEART SURGERIES/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:210 N SAINT ANDREWS ST",
         "CITY:DOTHAN",
         "PLACE:CRIMINAL JUSTICE BUILDING",
         "ID:15439268",
         "INFO:INMATE -- 31 YOM CP -- HX: 2 HEART SURGERIES");
 
     doTest("T12",
         "11:22:21/SC-Service Call Non-Emergency/200 S APPLETREE ST APT3/DOTHAN/14523968/47 YOF, PT FELL,REQUEST LIFT ASSISTANCE/",
         "CALL:SC-Service Call Non-Emergency",
         "ADDR:200 S APPLETREE ST",
         "APT:3",
         "CITY:DOTHAN",
         "ID:14523968",
         "INFO:47 YOF, PT FELL,REQUEST LIFT ASSISTANCE");
 
     doTest("T13",
         "12:49:37/S8CF-MVC-Code II/3800 ROSS CLARK CIR INTERSECTN/DOTHAN/DENTON RD/39618452/VEHS PULLED INTO P/L OF THE OLD FOOD WORLD P/L/",
         "CALL:S8CF-MVC-Code II",
         "ADDR:3800 ROSS CLARK CIR",
         "CITY:DOTHAN",
         "X:DENTON RD",
         "ID:39618452",
         "INFO:VEHS PULLED INTO P / L OF THE OLD FOOD WORLD P / L");
 
     doTest("T14",
         "15:53:10/S8BF-MVC-Code III/2399 S PARK AVE INTERSECTN/DOTHAN/WOODLAND DR/24135869/pt c/o of neck pain...inv s5/",
         "CALL:S8BF-MVC-Code III",
         "ADDR:2399 S PARK AVE",
         "CITY:DOTHAN",
         "X:WOODLAND DR",
         "ID:24135869",
         "INFO:pt c / o of neck pain...inv s5");
 
     doTest("T15",
         "18:04:21/S38A-Medical Call-Code III Crit-Emg/1 SAWGRASS DR INTERSECTN/DOTHAN/WESTBROOK RD/83621945/53 YOF C/O CHEST PAIN, N/V, SHAKING ~ OCCP >>>/",
         "CALL:S38A-Medical Call-Code III Crit-Emg",
         "ADDR:1 SAWGRASS DR",
         "CITY:DOTHAN",
         "X:WESTBROOK RD",
         "ID:83621945",
         "INFO:53 YOF C / O CHEST PAIN, N / V, SHAKING ~ OCCP >>>");
 
     doTest("T16",
         "18:09:20/S38B-Medical Call-Code III-Emergy/6007 W MAIN ST STE1/DOTHAN/WIREGRASS SHRINE CLUB/83621945/REF FEMALE PT FALLEN~FROM STANDING POSITION~~BLEEDING FR",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:6007 W MAIN ST",
         "APT:STE1",
         "CITY:DOTHAN",
         "PLACE:WIREGRASS SHRINE CLUB",
         "ID:83621945",
         "INFO:REF FEMALE PT FALLEN~FROM STANDING POSITION~~BLEEDING FR");
 
     doTest("T17",
         "18:51:49/S22CF-Assault-Code II/2233 WESTGATE PKY APTL81/DOTHAN/WESTGATE VILLAGE L81/96315842/INV JUVS --- 6 YOM, BLEEDING FROM NOSE/",
         "CALL:S22CF-Assault-Code II",
         "ADDR:2233 WESTGATE PKY",
         "APT:L81",
         "CITY:DOTHAN",
         "PLACE:WESTGATE VILLAGE L81",
         "ID:96315842",
         "INFO:INV JUVS --- 6 YOM, BLEEDING FROM NOSE");
 
     doTest("T18",
         "20:52:18/S38B-Medical Call-Code III-Emergy/4310 MONTGOMERY HWY STE1/DOTHAN/WALMART NS/63981245/26 yom fallen on water / c/o back pain //",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:4310 MONTGOMERY HWY",
         "APT:STE1",
         "CITY:DOTHAN",
         "PLACE:WALMART NS",
         "ID:63981245",
         "INFO:26 yom fallen on water / c / o back pain");
 
     doTest("T19",
         "21:37:04/S38A-Medical Call-Code III Crit-Emg/2031 THIRD AVE APTK51/DOTHAN/GRADYS WALK K51/41253896/58 YOF C/O HEAD PAIN, RADIATING THRU NECK & LEFT SHOULDER",
         "CALL:S38A-Medical Call-Code III Crit-Emg",
         "ADDR:2031 THIRD AVE",
         "APT:K51",
         "CITY:DOTHAN",
         "PLACE:GRADYS WALK K51",
         "ID:41253896",
         "INFO:58 YOF C / O HEAD PAIN, RADIATING THRU NECK & LEFT SHOULDER");
 
     doTest("T20",
         "23:20:14/S22BF-Assault-Code III/1304 CORNELL AVE APTE26/DOTHAN/ROCK CREEK E26/36198425/pr refused/",
         "CALL:S22BF-Assault-Code III",
         "ADDR:1304 CORNELL AVE",
         "APT:E26",
         "CITY:DOTHAN",
         "PLACE:ROCK CREEK E26",
         "ID:36198425",
         "INFO:pr refused");
 
     doTest("T21",
         "00:07:44/S38B-Medical Call-Code III-Emergy/102 JOHNSON CIR APT34/DOTHAN/JOHNSON 34/51943628/48 YOF POSS STROKE...FALLEN FROM STANDING POSITION/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:102 JOHNSON CIR",
         "APT:34",
         "CITY:DOTHAN",
         "PLACE:JOHNSON 34",
         "ID:51943628",
         "INFO:48 YOF POSS STROKE...FALLEN FROM STANDING POSITION");
 
     doTest("T22",
         "00:07:44/S38B-Medical Call-Code III-Emergy/102 JOHNSON CIR APT34/DOTHAN/JOHNSON 34/51943628/48 YOF POSS STROKE...FALLEN FROM STANDING POSITION/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:102 JOHNSON CIR",
         "APT:34",
         "CITY:DOTHAN",
         "PLACE:JOHNSON 34",
         "ID:51943628",
         "INFO:48 YOF POSS STROKE...FALLEN FROM STANDING POSITION");
 
     doTest("T23",
         "05:04:51/MAID-Mutual Aid Request/5028 REEVES ST INTERSECTN/DOTHAN/CITY LIMITS/95163482/ref s38b 49 yom actively seizing, 1040 w/abbeville rescue/",
         "CALL:MAID-Mutual Aid Request",
         "ADDR:5028 REEVES ST",
         "CITY:DOTHAN",
         "PLACE:CITY LIMITS",
         "ID:95163482",
         "INFO:ref s38b 49 yom actively seizing, 1040 w / abbeville rescue");
 
     doTest("T24",
         "10:25:33/S8CF-MVC-Code II/1 COLUMBIA HWY INTERSECTN/DOTHAN/E MAIN ST/15439268/",
         "CALL:S8CF-MVC-Code II",
         "ADDR:1 COLUMBIA HWY",
         "CITY:DOTHAN",
         "X:E MAIN ST",
         "ID:15439268");
 
     doTest("T25",
         "14:52:12/S8BF-MVC-Code III/2410 ROSS CLARK CIR INTERSECTN/DOTHAN/S PARK AVE/24135869/PT JUST RELEASED FROM HOSPITAL, UNK IF INJURIED/",
         "CALL:S8BF-MVC-Code III",
         "ADDR:2410 ROSS CLARK CIR",
         "CITY:DOTHAN",
         "X:S PARK AVE",
         "ID:24135869",
         "INFO:PT JUST RELEASED FROM HOSPITAL, UNK IF INJURIED");
 
     doTest("T26",
         "11:15:34/S38B-Medical Call-Code III-Emergy/6726 E HIGHWAY 134/DOTHAN/769381254/elderly male appears disoriented ~~ 10-5 by ozark/dale co/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:6726 E HIGHWAY 134",
         "CITY:DOTHAN",
         "ID:769381254",
         "INFO:elderly male appears disoriented ~~ 10-5 by ozark / dale co");
     
     doTest("T27",
         "09:48:02/S38B-Medical Call-Code III-Emergy/101 LOCKWYNN TRC/DOTHAN/68392145/20YOF SWEATING UNK FURTHER RELAYED BY THIRD PARTY/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:101 LOCKWYNN TRC",
         "CITY:DOTHAN",
         "ID:68392145",
         "INFO:20YOF SWEATING UNK FURTHER RELAYED BY THIRD PARTY");
 
     doTest("T28",
         "14:33:21/S38B-Medical Call-Code III-Emergy/342 S SAINT ANDREWS ST APT216/DOTHAN/VAUGHN TOWERS 216/14523968/58YOF  PAIN ALL OVER/",
         "CALL:S38B-Medical Call-Code III-Emergy",
         "ADDR:342 S SAINT ANDREWS ST",
         "APT:216",
         "CITY:DOTHAN",
         "PLACE:VAUGHN TOWERS 216",
         "ID:14523968",
         "INFO:58YOF  PAIN ALL OVER");
 
     doTest("T29",
         "00:30:09/MAID-Mutual Aid Request/17230 E STATE HWY 52 BLK/COLUMBIA/COFDA/F-COFD/",
         "CALL:MAID-Mutual Aid Request",
         "ADDR:17230 E STATE HWY 52",
         "APT:BLK",
         "CITY:COLUMBIA",
         "PLACE:COFDA/F-COFD");
 
     doTest("T30",
         "16:52:57/OF-Outdoor Fire-Level 1/100 PEMCO DR/DOTHAN/PEMCO/769381254/BEHIND HANGER 15 GRASS IN FIELD.. TRANSFORMER BLOWN/",
         "CALL:OF-Outdoor Fire-Level 1",
         "ADDR:100 PEMCO DR",
         "CITY:DOTHAN",
         "PLACE:PEMCO",
         "ID:769381254",
         "INFO:BEHIND HANGER 15 GRASS IN FIELD.. TRANSFORMER BLOWN");
 
     doTest("T31",
         "15:21:59/S38C-Medical Call-Code II-Non Emgy/115 W ADAMS ST STE2/DOTHAN/MAGISTRATES/DOTHAN/15394628/",
         "CALL:S38C-Medical Call-Code II-Non Emgy",
         "ADDR:115 W ADAMS ST",
         "APT:STE2",
         "CITY:DOTHAN",
         "PLACE:MAGISTRATES/DOTHAN",
         "ID:15394628");
 
     doTest("T32",
         "12:52:08/S38A-Medical Call-Code III Crit-Emg/25 GOVERNOR DR/DOTHAN/WALLACE COLLEGE/769381254/PREGNANT PT ~~ LIGHT HEADED ~~ WALLACE HALL BLDG 25 UNK AGE/",
         "CALL:S38A-Medical Call-Code III Crit-Emg",
         "ADDR:25 GOVERNOR DR",
         "CITY:DOTHAN",
         "PLACE:WALLACE COLLEGE",
         "ID:769381254",
         "INFO:PREGNANT PT ~~ LIGHT HEADED ~~ WALLACE HALL BLDG 25 UNK AGE");
 
   }
   
   
   public static void main(String[] args) {
    new ALDothanParserTest().generateTests("T32");
   }
 }
