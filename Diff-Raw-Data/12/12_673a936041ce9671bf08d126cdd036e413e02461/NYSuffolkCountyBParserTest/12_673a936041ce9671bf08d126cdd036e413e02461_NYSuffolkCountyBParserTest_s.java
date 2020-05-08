 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYSuffolkCountyBParser;
 
 import org.junit.Test;
 
 
 public class NYSuffolkCountyBParserTest extends BaseParserTest {
   
   public NYSuffolkCountyBParserTest() {
     setParser(new NYSuffolkCountyBParser(), "SUFFOLK COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "*** 13 - Structure Fire *** 147 CHERUBINA LN CS: LEADER AVE  / SKIDMORE RD TOA: 22:37 09/22/10 OIL BURNER NORTH BABYLON FC 2010-002398 HY: 8' 11",
         "CALL:13 - Structure Fire",
         "ADDR:147 CHERUBINA LN",
         "X:LEADER AVE  / SKIDMORE RD",
         "TIME:22:37",
         "DATE:09/22/10",
         "SRC:NORTH BABYLON FC",
         "ID:2010-002398",
         "INFO:OIL BURNER HY: 8' 11");
 
     doTest("T2",
         "*** 13 - Structure Fire *** 514 MOUNT PL CS: ESSEX ST  / LAKEWAY DR TOA: 19:55 09/22/10 NORTH BABYLON FC 2010-002393 HY: 12' 533 MOUNT PL @ ESSE",
         "CALL:13 - Structure Fire",
         "ADDR:514 MOUNT PL",
         "X:ESSEX ST  / LAKEWAY DR",
         "TIME:19:55",
         "DATE:09/22/10",
         "SRC:NORTH BABYLON FC",
         "ID:2010-002393",
         "INFO:HY: 12' 533 MOUNT PL @ ESSE");
 
     doTest("T3",
         "*** 2nd/16 - Rescue *** 733 HIGHRIDGE RD CS: OCONNER RD  / NARLAND LN TOA: 20:46 09/22/10 a/m pysch emer NORTH BABYLON FC 2010-002395",
         "CALL:2nd/16 - Rescue",
         "ADDR:733 HIGHRIDGE RD",
         "X:OCONNER RD  / NARLAND LN",
         "TIME:20:46",
         "DATE:09/22/10",
         "SRC:NORTH BABYLON FC",
         "ID:2010-002395",
         "INFO:a/m pysch emer");
 
     doTest("T4",
         "*** 23 - Miscellaneous Fire *** SR CITZ APTS (5 BLDGS) COMPLEX 15 WEEKS RD CS: DEER PARK AVE  / MULHOLLAND DR TOA: 11:07 09/23/10 INVEST NORTH B",
         "CALL:23 - Miscellaneous Fire",
         "PLACE:SR CITZ APTS (5 BLDGS) COMPLEX",
         "ADDR:15 WEEKS RD",
         "X:DEER PARK AVE  / MULHOLLAND DR",
         "TIME:11:07",
         "DATE:09/23/10",
         "INFO:INVEST NORTH B");
 
     doTest("T5",
         "*** 24/16 - Mutual Aid *** 27 COOLIDGE AVE CS: RT 110  / COOLIDGE AVE TOA: 06:54 10/20/10 40 Y/F ABDOMINAL PAINS  AMITYVILLE FD 2010-000228",
         "CALL:24/16 - Mutual Aid",
         "ADDR:27 COOLIDGE AVE",
         "X:RT 110  / COOLIDGE AVE",
         "TIME:06:54",
         "DATE:10/20/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000228",
         "INFO:40 Y/F ABDOMINAL PAINS");
 
     doTest("T6",
         "*** 24/16 - Mutual Aid *** 22 ELGIN RD CS: DE  / COOLIDGE AVE TOA: 11:38 10/18/10 E/F UNABLE TO MOVE  **FULL CREW NEEDED** AMITYVILLE FD 2010-00",
         "CALL:24/16 - Mutual Aid",
         "ADDR:22 ELGIN RD",
         "X:DE  / COOLIDGE AVE",
         "TIME:11:38",
         "DATE:10/18/10",
         "SRC:AMITYVILLE FD",
         "INFO:E/F UNABLE TO MOVE **FULL CREW NEEDED** 2010-00");
 
     doTest("T7",
         "*** 24/MV - Mutual Aid ***  MONTAUK HWY CS: WILSON AVE TOA: 11:43 10/20/10 AMITYVILLE FD 2010-000229",
         "CALL:24/MV - Mutual Aid",
         "ADDR:MONTAUK HWY",
         "MADDR:MONTAUK HWY & WILSON AVE",
         "X:WILSON AVE",
         "TIME:11:43",
         "DATE:10/20/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000229");
 
     doTest("T8",
         "*** 24/13 - Mutual Aid *** 42 NATHALIE AVE CS: CAMPBELL ST  / MOORE ST TOA: 12:04 10/04/10 POSS OCCUPANTS WITH IN  AMITYVILLE FD 2010-000226",
         "CALL:24/13 - Mutual Aid",
         "ADDR:42 NATHALIE AVE",
         "X:CAMPBELL ST  / MOORE ST",
         "TIME:12:04",
         "DATE:10/04/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000226",
         "INFO:POSS OCCUPANTS WITH IN");
 
     doTest("T9",
         "*** 3/16 - Rescue *** 204 VAN BUREN ST CS: LEWIS AVE  / BELMONT AVE TOA: 10:04 10/29/10 E/F UNRESPONSIVE  **EMT NEEDED** NO",
         "CALL:3/16 - Rescue",
         "ADDR:204 VAN BUREN ST",
         "X:LEWIS AVE  / BELMONT AVE",
         "TIME:10:04",
         "DATE:10/29/10",
         "INFO:E/F UNRESPONSIVE **EMT NEEDED** NO");
 
     doTest("T10",
         "*** 16 - Rescue *** 41 WILLIAMS AVE CS: CAMPBELL ST  / MOORE ST TOA: 18:01 10/25/10 E/F UNCONCIOUS-NOT BREATHING AMITYVILLE FD 2010-000231",
         "CALL:16 - Rescue",
         "ADDR:41 WILLIAMS AVE",
         "X:CAMPBELL ST  / MOORE ST",
         "TIME:18:01",
         "DATE:10/25/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000231",
         "INFO:E/F UNCONCIOUS-NOT BREATHING");
 
     doTest("T11",
         "*** 16 - Rescue *** 100 PARK AVE CS: IRELAND PL  / CEDAR ST TOA: 05:28 10/30/10 E/M  AMITYVILLE FD 2010-000235",
         "CALL:16 - Rescue",
         "ADDR:100 PARK AVE",
         "X:IRELAND PL  / CEDAR ST",
         "TIME:05:28",
         "DATE:10/30/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000235",
         "INFO:E/M");
 
     doTest("T12",
         "*** 16 - Rescue *** 47 DIXON AVE CS: RT 110  / ALBANY AVE TOA: 20:28 10/27/10 77Y/O FEMALE CHOKING AMITYVILLE FD 2010-000233",
         "CALL:16 - Rescue",
         "ADDR:47 DIXON AVE",
         "X:RT 110  / ALBANY AVE",
         "TIME:20:28",
         "DATE:10/27/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000233",
         "INFO:77Y/O FEMALE CHOKING");
 
     doTest("T13",
         "*** 24/16 - Mutual Aid *** CHURCH OF GOD 102 COOLIDGE AVE CS: ROSEWOODAVE  / STEELE PL TOA: 17:49 10/28/10 VOV AMITYVILLE FD 2010-000234",
         "CALL:24/16 - Mutual Aid",
         "PLACE:CHURCH OF GOD",
         "ADDR:102 COOLIDGE AVE",
         "X:ROSEWOODAVE  / STEELE PL",
         "TIME:17:49",
         "DATE:10/28/10",
         "SRC:AMITYVILLE FD",
         "ID:2010-000234",
         "INFO:VOV");
 
     doTest("T14",
         "*** 16 - Rescue *** 99 OVERTON ST CS: JEFFERSON AVE  / DEER PARK AVE TOA: 15:06 11/25/10 E/F SICK DEER PARK FIRE DISTRICT",
         "CALL:16 - Rescue",
         "ADDR:99 OVERTON ST",
         "X:JEFFERSON AVE  / DEER PARK AVE",
         "TIME:15:06",
         "DATE:11/25/10",
         "SRC:DEER PARK FIRE DISTRICT",
         "INFO:E/F SICK");
 
     doTest("T15",
         "*** 23 - Miscellaneous Fire *** 14 BROOKLYN ST CS: CARLLS PA  / CAYUGA AVE TOA: 15:59 11/25/10 A/F/A  DEER PARK FIRE DISTRICT",
         "CALL:23 - Miscellaneous Fire",
         "ADDR:14 BROOKLYN ST",
         "X:CARLLS PA  / CAYUGA AVE",
         "TIME:15:59",
         "DATE:11/25/10",
         "SRC:DEER PARK FIRE DISTRICT",
         "INFO:A/F/A");
 
     doTest("T16",
         "*** 2nd/16 - Rescue *** INVALID 185 W 7TH ST CS: PARK AVE  / CENTRAL AVE TOA: 10:35 12/13/10 80 YR FEM VOMITTING WEAK DEER PARK FIRE DISTRICT",
         "CALL:2nd/16 - Rescue",
         "PLACE:INVALID",
         "ADDR:185 W 7TH ST",
         "X:PARK AVE  / CENTRAL AVE",
         "TIME:10:35",
         "DATE:12/13/10",
         "SRC:DEER PARK FIRE DISTRICT",
         "INFO:80 YR FEM VOMITTING WEAK");
 
     doTest("T17",
         "*** 16 - Rescue *** 189 W 10TH ST CS: PARK AVE  / CENTRAL AVE TOA: 07:53 12/13/10 A/F SYNCOPAL DEER PARK FIRE DISTRICT",
         "CALL:16 - Rescue",
         "ADDR:189 W 10TH ST",
         "X:PARK AVE  / CENTRAL AVE",
         "TIME:07:53",
         "DATE:12/13/10",
         "SRC:DEER PARK FIRE DISTRICT",
         "INFO:A/F SYNCOPAL");
 
     doTest("T18",
         "*** 16 - Rescue *** 111 LIBERTY ST CS: DEER PARK AVE  / PINE ACRES BLVD TOA: 20:06 12/12/10 82YR MALE CHEST PAIN DEER PARK FIRE DISTRICT",
         "CALL:16 - Rescue",
         "ADDR:111 LIBERTY ST",
         "X:DEER PARK AVE  / PINE ACRES BLVD",
         "TIME:20:06",
         "DATE:12/12/10",
         "SRC:DEER PARK FIRE DISTRICT",
         "INFO:82YR MALE CHEST PAIN");
 
     doTest("T19",
         "***23- Wires/Electrical Hazard*** THEATRE THREE* 412 MAIN ST PORT JEFFERSON CS: SPRING ST  / MAPLE PL TOA: 14:18 01/02/11 PT JEFFERSON 2011-000003 PJFD\",",
         "CALL:23- Wires/Electrical Hazard",
         "PLACE:THEATRE THREE",
         "ADDR:412 MAIN ST",
         "CITY:PORT JEFFERSON",
         "X:SPRING ST  / MAPLE PL",
         "TIME:14:18",
         "DATE:01/02/11",
         "SRC:PT JEFFERSON",
         "ID:2011-000003",
         "INFO:PJFD\",");
 
     doTest("T20",
         "*** 30 - RESCUE *** U.S.A. SKATING RINK 1276 HICKSVILLE RD CS: SUFFOLK AVE  / DOGWOOD LN A-2 TOA: 20:12 03/30/11 2011-000250 Hazmat",
         "CALL:30 - RESCUE",
         "PLACE:USA SKATING RINK",
         "ADDR:1276 HICKSVILLE RD",
         "X:SUFFOLK AVE  / DOGWOOD LN A-2",
         "TIME:20:12",
         "DATE:03/30/11",
         "ID:2011-000250",
         "INFO:Hazmat");
 
     doTest("T21",
         "*** 16 - Rescue *** 30 DEER SHORE SQ CS: DEER PARK AVE  / BAY SHORE RD TOA: 13:04 04/02/11 F/M STROKE DEER PARK FIRE",
         "CALL:16 - Rescue",
         "ADDR:30 DEER SHORE SQ",
         "X:DEER PARK AVE  / BAY SHORE RD",
         "TIME:13:04",
         "DATE:04/02/11",
         "INFO:F/M STROKE DEER PARK FIRE");
 
     doTest("T22",
         "*** 16/23-Rescue/Miscellaneous *** 1015 GRAND BLVD CS: E INDUSTRY CT  / CORBIN AVE TOA: 12:09 04/02/11 3 Y/O POSSINI",
         "CALL:16/23-Rescue/Miscellaneous",
         "ADDR:1015 GRAND BLVD",
         "X:E INDUSTRY CT  / CORBIN AVE",
         "TIME:12:09",
         "DATE:04/02/11",
         "INFO:3 Y/O POSSINI");
 
     doTest("T23",
         "*** 16 - Rescue *** 1015 GRAND BLVD CS: E INDUSTRY CT  / CORBIN AVE TOA: 12:09 04/02/11 3 Y/O POSSINING  DEER PARK F",
         "CALL:16 - Rescue",
         "ADDR:1015 GRAND BLVD",
         "X:E INDUSTRY CT  / CORBIN AVE",
         "TIME:12:09",
         "DATE:04/02/11",
         "INFO:3 Y/O POSSINING DEER PARK F");
 
     doTest("T24",
         "*** 16 - Rescue *** 162 OAKLAND AVE CS: W 6TH ST  / W 5TH ST TOA: 10:20 04/02/11 A/F INF FROM A FALL   DEER PARK FIR",
         "CALL:16 - Rescue",
         "ADDR:162 OAKLAND AVE",
         "X:W 6TH ST  / W 5TH ST",
         "TIME:10:20",
         "DATE:04/02/11",
         "INFO:A/F INF FROM A FALL DEER PARK FIR");
 
     doTest("T25",
         "  / *** 23 - Miscellaneous Fire *** 18 SILVERPINE DR CS: ATNA DR  - PROSPECT ST W TOA: 12:10 07-17-11 2011-001109\n",
         "CALL:23 - Miscellaneous Fire",
         "ADDR:18 SILVERPINE DR",
         "X:ATNA DR  - PROSPECT ST W",
         "TIME:12:10",
         "DATE:07-17-11",
         "ID:2011-001109");
 
     doTest("T26",
         "*** 2nd/16 - Rescue *** WENDY'S NBFC 1236 DEER PARK AVE CS: WOODS RD  - AUGUST RD TOA: 18:07 09-12-11 <07091211> a/f inj from fall NORTH BABYLON FC 2011-00 <201100>",
         "CALL:2nd/16 - Rescue",
         "PLACE:WENDY'S NBFC",
         "ADDR:1236 DEER PARK AVE",
         "X:WOODS RD  - AUGUST RD",
         "TIME:18:07",
         "DATE:09-12-11",
         "SRC:NORTH BABYLON FC",
         "INFO:a/f inj from fall 2011-00");
 
     doTest("T27",
         "*** MV - Motor Vehicle Accident *** SS PY & DEER PARK AV Ext 39 SOUTHERN STATE PARKWAY CS: DEER PARK AVE TOA: 17:49 09-12-11 <49091211> EASTBOUND NORTH BAB",
         "CALL:MV - Motor Vehicle Accident",
         "PLACE:SS PY & DEER PARK AV Ext",
         "ADDR:39 SOUTHERN STATE PARKWAY",
         "X:DEER PARK AVE",
         "TIME:17:49",
         "DATE:09-12-11",
         "INFO:EASTBOUND NORTH BAB");
 
     doTest("T28",
         "*** 23 - Miscellaneous Fire *** 7 NON-AMBULATORY PTS ON 2ND FLOOR 15 BROOKSIDE AVE CS: O  - O DEER PARK AVE",
         "CALL:23 - Miscellaneous Fire",
         "ADDR:7 NON-AMBULATORY PTS ON 2ND FLOOR 15 BROOKSIDE AVE",
         "X:O  - O DEER PARK AVE");
 
     doTest("T29",
         "*** 2nd/16 - Rescue *** 1171 LITTLE EAST NECK RD CS: DELAWARE RD  - PHOENIX RD TOA: 09:29 09-12-11 <29091211> NORTH BABYLON FC 2011-002275 <2011002275>",
         "CALL:2nd/16 - Rescue",
         "ADDR:1171 LITTLE EAST NECK RD",
         "X:DELAWARE RD  - PHOENIX RD",
         "TIME:09:29",
         "DATE:09-12-11",
         "SRC:NORTH BABYLON FC",
         "ID:2011-002275");
 
     doTest("T30",
         "*** 16 - Rescue *** 101 ELKTON LN CS: MAYBROOK RD  - AUGUST RD TOA: 08:52 09-12-11 84 <5209121184> Y/F BODY PAINS  NORTH BABYLON FC 2011-002274<2011002274>",
         "CALL:16 - Rescue",
         "ADDR:101 ELKTON LN",
         "X:MAYBROOK RD  - AUGUST RD",
         "TIME:08:52",
         "DATE:09-12-11",
         "SRC:NORTH BABYLON FC",
         "ID:2011-002274",
         "INFO:84 Y/F BODY PAINS");
 
     doTest("T31",
         "*** 2nd/16 - Rescue *** 224 STATE AVE CS: MOUNT AVE  - LUND ST TOA: 18:48 09-17-11 E/F ABD PAIN WYANDANCH/WHEATLEY HTS 2011-001835",
         "CALL:2nd/16 - Rescue",
         "ADDR:224 STATE AVE",
         "X:MOUNT AVE  - LUND ST",
         "TIME:18:48",
         "DATE:09-17-11",
         "ID:2011-001835",
         "INFO:E/F ABD PAIN WYANDANCH/WHEATLEY HTS");
 
     doTest("T32",
         "*** 16 ***  BROADWAY CS: PARK AVE ADTML: 31-D-3 TOA: 16:14 10-20-11 2011-004568 TYPE: UNCONSCIOUS / FAINTING (NEAR) I/V/O NURSERY E/M",
         "CALL:16 - UNCONSCIOUS / FAINTING",
         "ADDR:BROADWAY",
         "MADDR:BROADWAY & PARK AVE",
         "X:PARK AVE",
         "CODE:31-D-3",
         "TIME:16:14",
         "DATE:10-20-11",
         "ID:2011-004568",
         "INFO:(NEAR) I/V/O NURSERY E/M");
 
     doTest("T33",
         "*** 16 *** 24 FOXHURST RD CS: DIX HILLS RD TOA: 16:16 10-29-11 2011-004718",
         "CALL:16",
         "ADDR:24 FOXHURST RD",
         "X:DIX HILLS RD",
         "TIME:16:16",
         "DATE:10-29-11",
         "ID:2011-004718");
 
     doTest("T34",
         "*** 16 *** 180 E PULASKI RD CS: ALBERMARLE ST ADTML: 10-C-1 TOA: 15:43 01-26-12 2012-000393 TYPE: CHEST PAIN LOC: 180 E PULASKI RD HU",
         "CALL:16 - CHEST PAIN",
         "ADDR:180 E PULASKI RD",
         "X:ALBERMARLE ST",
         "CODE:10-C-1",
         "TIME:15:43",
         "DATE:01-26-12",
         "ID:2012-000393");
 
     doTest("T35",
         "*** 16 ***  JONES LN CS: W JERICHO TRNP ADTML: 29-B-1 TOA: 14:04 01-26-12 2012-000392 TYPE: MOTOR VEHICLE ACCIDENT  IN PARKING LOT CR",
         "CALL:16 - MOTOR VEHICLE ACCIDENT",
         "ADDR:JONES LN",
         "MADDR:JONES LN & W JERICHO TRNP",
         "X:W JERICHO TRNP",
         "CODE:29-B-1",
         "TIME:14:04",
         "DATE:01-26-12",
         "ID:2012-000392",
         "INFO:IN PARKING LOT CR");
 
     doTest("T36",
         "*** 16 *** 379 NEW YORK AV CS: ELM ST ADTML: 17-D-3 TOA: 13:28 01-26-12 2012-000391 TYPE: FALLS LOC: 379 NEW YORK AV HUNTIN :@FOX'S  ",
         "CALL:16 - FALLS",
         "ADDR:379 NEW YORK AV",
         "MADDR:379 NEW YORK AVE",
         "X:ELM ST",
         "CODE:17-D-3",
         "TIME:13:28",
         "DATE:01-26-12",
         "ID:2012-000391");
 
     doTest("T37",
         "*** 16 *** 257 E 17 CS: LENOX RD ADTML: 17-A-1 TOA: 10:16 02-05-12 2012-000535 TYPE: FALLS LOC: 257A E 17 ST HUNTIS   CROSS: LENOX RD",
         "CALL:16 - FALLS",
         "ADDR:257 E 17",
         "X:LENOX RD",
         "CODE:17-A-1",
         "TIME:10:16",
         "DATE:02-05-12",
         "ID:2012-000535");
 
     doTest("T38",
         "*** 16 *** 2 MATHER CT CS: STRATTON DR ADTML: 6-C-1 TOA: 08:00 02-05-12 2012-000533 TYPE: RESPIRATORY LOC: 2 MATHER CT HUNTIS   CROSS",
         "CALL:16 - RESPIRATORY",
         "ADDR:2 MATHER CT",
         "X:STRATTON DR",
         "CODE:6-C-1",
         "TIME:08:00",
         "DATE:02-05-12",
         "ID:2012-000533");
   }
   
   public static void main(String[] args) {
    new NYSuffolkCountyBParserTest().generateTests("T39");
   }
 }
