 package net.anei.cadpage.parsers.CT;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CTLitchfieldCountyParserTest extends BaseParserTest {
   
   public CTLitchfieldCountyParserTest() {
     setParser(new CTLitchfieldCountyParser(), "LITCHFIELD COUNTY", "CT");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(LCD) MEDIC 4 RESPOND TO 30 PARK LANE EAST   NEW MILFORD, ,   UNCONSCIOUS, 31-D-3 :09:22",
         "SRC:MEDIC 4",
         "ADDR:30 PARK LANE EAST",
         "CITY:NEW MILFORD",
         "CALL:UNCONSCIOUS",
         "CODE:31-D-3");
 
     doTest("T2",
         "FRM:textmsg@lcd911.com\nSUBJ:LCD\nMSG:MEDIC 4 RESPOND TO 80 CONNELLY RD   NEW MILFORD, , 80 M DIFF BREATH, 6-C-1 :12:02",
         "SRC:MEDIC 4",
         "ADDR:80 CONNELLY RD",
         "CITY:NEW MILFORD",
         "CALL:80 M DIFF BREATH",
         "CODE:6-C-1");
 
     doTest("T3",
         "(LCD) MEDIC 4 RESPOND TO 70 CHAPEL HILL RD   SHERMAN, , 92Y M DIFF BREATH, 6-D-2 :12:36",
         "SRC:MEDIC 4",
         "ADDR:70 CHAPEL HILL RD",
         "CITY:SHERMAN",
         "CALL:92Y M DIFF BREATH",
         "CODE:6-D-2");
     
     doTest("T4",
         "(LCD) MEDIC 4 RESPOND TO  WELLERS BRIDGE RD   HEMLOCK ROXBURY, ,   TRAFFIC ACCIDENT,  :10:44",
         "SRC:MEDIC 4",
         "ADDR:WELLERS BRIDGE RD",
         "CITY:ROXBURY",
         "CALL:TRAFFIC ACCIDENT");
 
     doTest("T5",
         "FRM:textmsg@lcd911.com\nSUBJ:LCD\nMSG:BRIDGEWATER FIRE RESPOND TO 209 NORTHROP ST BADER RESIDENCE  BRIDGEWATER, , CO ALARM ,:15:00",
         "SRC:BRIDGEWATER FIRE",
         "ADDR:209 NORTHROP ST",
         "PLACE:BADER RESIDENCE",
         "CITY:BRIDGEWATER",
         "CALL:CO ALARM");
 
     doTest("T6",
         "(LCD) MEDIC 4 RESPOND TO 33 HARRY BROOK VILLAGE   NEW MILFORD, , 63 M HEART PROBLEM, 19-D-4 :18:31",
         "SRC:MEDIC 4",
         "ADDR:33 HARRY BROOK VILLAGE",
         "CITY:NEW MILFORD",
         "CALL:63 M HEART PROBLEM",
         "CODE:19-D-4");
 
     doTest("T7",
         "(LCD) MEDIC 4 RESPOND TO  WELLSVILLE AVE AREA OF CANTERBURY ARMS  NEW MILFORD, , 53 F DIABETIC, 13-C-1 :19:43",
         "SRC:MEDIC 4",
         "ADDR:WELLSVILLE AVE",
         "PLACE:AREA OF CANTERBURY ARMS",
         "CITY:NEW MILFORD",
         "CALL:53 F DIABETIC",
         "CODE:13-C-1");
 
     doTest("T8",
         "(LCD) MEDIC 4 RESPOND TO 33 HARRY BROOK VILLAGE   NEW MILFORD, , 63 M HEART PROBLEM, 19-D-4 :18:31",
         "SRC:MEDIC 4",
         "ADDR:33 HARRY BROOK VILLAGE",
         "CITY:NEW MILFORD",
         "CALL:63 M HEART PROBLEM",
         "CODE:19-D-4");
 
     doTest("T9",
         "(LCD) MEDIC 4 RESPOND TO 1 GLEN AYRE DR   NEW MILFORD, , 60 F SICK PERSON, 26-C-1 :10:36",
         "SRC:MEDIC 4",
         "ADDR:1 GLEN AYRE DR",
         "CITY:NEW MILFORD",
         "CALL:60 F SICK PERSON",
         "CODE:26-C-1");
 
     doTest("T10",
         "(LCD) MEDIC 4 RESPOND TO 11 OLD PARK LANE RD NEW MILFORD MEDICAL GROUP  NEW MILFORD, ,   HEART PROBLEM, 19-C-4 :10:57",
         "SRC:MEDIC 4",
         "ADDR:11 OLD PARK LANE RD",
         "PLACE:NEW MILFORD MEDICAL GROUP",
         "CITY:NEW MILFORD",
         "CALL:HEART PROBLEM",
         "CODE:19-C-4");
 
     doTest("T11",
         "(LCD) MEDIC 4 RESPOND TO 1 OLD STATE RD   NEW MILFORD, ,   DIFF BREATH, 6-C-1 :11:31",
         "SRC:MEDIC 4",
         "ADDR:1 OLD STATE RD",
         "CITY:NEW MILFORD",
         "CALL:DIFF BREATH",
         "CODE:6-C-1");
 
     doTest("T12",
         "(LCD) MEDIC 4 RESPOND TO 58 PICKETT DISTRICT RD KIMBERLY CLARK, NORTH GATE  NEW MILFORD, , 40 M SEIZURE, 12-D-2 :14:56",
         "SRC:MEDIC 4",
         "ADDR:58 PICKETT DISTRICT RD",
         "PLACE:KIMBERLY CLARK, NORTH GATE",
         "CITY:NEW MILFORD",
         "CALL:40 M SEIZURE",
         "CODE:12-D-2");
 
     doTest("T13",
         "(LCD) MEDIC 4 RESPOND TO 20 MAUWEEHOO HL PARKER RESIDENCE  SHERMAN, , 19Y F UNCONSCIOUS, 31-D-3 :16:30",
         "SRC:MEDIC 4",
         "ADDR:20 MAUWEEHOO HILL",
         "PLACE:PARKER RESIDENCE",
         "CITY:SHERMAN",
         "CALL:19Y F UNCONSCIOUS",
         "CODE:31-D-3");
 
     doTest("T14",
         "(LCD) MEDIC 4 RESPOND TO 104 DANBURY RD HOME DEPOT PARKING LOT  NEW MILFORD, , 79 F FALL, 17-D-3 :18:35",
         "SRC:MEDIC 4",
         "ADDR:104 DANBURY RD",
         "PLACE:HOME DEPOT PARKING LOT",
         "CITY:NEW MILFORD",
         "CALL:79 F FALL",
         "CODE:17-D-3");
 
     doTest("T15",
         "(LCD) HARWINTON FIRE RESPOND TO 160 LITCHFIELD RD THOMASTON SAVINGS BANK HARWINTON, , ODOR OF GAS ,:13:50",
         "SRC:HARWINTON FIRE",
         "ADDR:160 LITCHFIELD RD",
         "PLACE:THOMASTON SAVINGS BANK",
         "CITY:HARWINTON",
         "CALL:ODOR OF GAS");
 
     doTest("T16",
         "(LCD) HARWINTON FIRE RESPOND TO 285 BURLINGTON RD   HARWINTON, , TRAFFIC ACCIDENT ,:15:12",
         "SRC:HARWINTON FIRE",
         "ADDR:285 BURLINGTON RD",
         "CITY:HARWINTON",
         "CALL:TRAFFIC ACCIDENT");
 
     doTest("T17",
         "(LCD) HARWINTON FIRE RESPOND TO 119 HARMONY HILL RD   HARWINTON, , TRANSFORMER FIRE ,:23:12",
         "SRC:HARWINTON FIRE",
         "ADDR:119 HARMONY HILL RD",
         "CITY:HARWINTON",
         "CALL:TRANSFORMER FIRE");
 
     doTest("T18",
         "(LCD) NEW HARTFORD FIRE RESPOND TO 1111 W HILL RD RESIDENCE NEW HARTFORD, , ACTIVATED FIRE ALARM ,:10:49",
         "SRC:NEW HARTFORD FIRE",
         "ADDR:1111 W HILL RD",
         "PLACE:RESIDENCE",
         "CITY:NEW HARTFORD",
         "CALL:ACTIVATED FIRE ALARM");
 
     doTest("T19",
         "(LCD) MEDIC 4 RESPOND TO 46 MAPLE ST CANTERBURY UNIT   KENT, THE KENT, 72Y F INTERFACILITY, 33-C-1 :11:09",
         "SRC:MEDIC 4",
         "ADDR:46 MAPLE ST",
         "PLACE:THE KENT",
         "APT:CANTERBURY UNIT",
         "CITY:KENT",
         "CALL:72Y F INTERFACILITY",
         "CODE:33-C-1");
 }
   
   public static void main(String[] args) {
     new CTLitchfieldCountyParserTest().generateTests("T19", "SRC ADDR PLACE APT CITY CALL CODE");
   }
 }
