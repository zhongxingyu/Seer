 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYSuffolkCountyCParser;
 
 import org.junit.Test;
 
 
 public class NYSuffolkCountyCParserTest extends BaseParserTest {
   
   public NYSuffolkCountyCParserTest() {
     setParser(new NYSuffolkCountyCParser(), "SUFFOLK COUNTY", "NY");
   }
   
   @Test
   public void testNotPage() {
     doBadTest("I'm at the store");
     doBadTest("I'm at a meeting right now. . .");
   }
   
   @Test
   public void testHolbrookParser() {
     
     doTest("T1",
         "MOTOR VEHICLE ACCIDENT . . MVA 29D4 at intersection of GREENBELT PKWY and PATCHOGUE-HOLBROOK RD. . 10:48:57",
         "CALL:MOTOR VEHICLE ACCIDENT - MVA 29D4",
         "ADDR:GREENBELT PKWY and PATCHOGUE-HOLBROOK RD");
     
     doTest("T2",
         "WIRES . . POWER CONDENSER FIRE at intersection of PATCHOGUE-HOLBROOK RD and FURROWS RD. . 07:14:36",
         "CALL:WIRES - POWER CONDENSER FIRE",
         "ADDR:PATCHOGUE-HOLBROOK RD and FURROWS RD");
     
     doTest("T3",
         "WORKING STRUCTURE FIRE at 223 SPRINGMEADOW DR. . 16:22:21",
         "CALL:WORKING STRUCTURE FIRE",
         "ADDR:223 SPRINGMEADOW DR");
         
     doTest("T4",
         "AMBULANCE CALL . . E/M RESP 6D2 at 76 LINCOLN AVE. . 17:48:14",
         "CALL:AMBULANCE CALL - E/M RESP 6D2",
         "ADDR:76 LINCOLN AVE");
 
     doTest("T5",
         "AMBULANCE CALL: 63 YOF RESP. 6-D-2 at 1585 CLAAS AVE, Holbrook . . 20:49:28",
         "CALL:AMBULANCE CALL",
         "INFO:63 YOF RESP.",
         "CODE:6-D-2",
         "ADDR:1585 CLAAS AVE",
         "CITY:Holbrook");
     
     doTest("T6",
         "CARBON MONOXIDE ALARM: ALARM GOING OFF - NEG SYMPTOMS at 53 TIMBER RIDGE DR, Holbrook . . 22:46:56",
         "CALL:CARBON MONOXIDE ALARM",
         "INFO:ALARM GOING OFF - NEG SYMPTOMS",
         "ADDR:53 TIMBER RIDGE DR",
         "CITY:Holbrook");
 
     doTest("T7",
         "FUEL SPILL: SHERIFFS ON SCENE REQUESTING at E/B SUNRISE HWY SRVC / BROADWAY AVE, HOLBROOK . . 23:06:50",
         "CALL:FUEL SPILL",
         "INFO:SHERIFFS ON SCENE REQUESTING",
         "ADDR:E & B SUNRISE HWY SRVC & BROADWAY AVE",
         "CITY:HOLBROOK");
     
     doTest("T8",
         "CARBON MONOXIDE ALARM: CO ALARM at 53 TIMBER RIDGE DR, HOLBROOK  c/s: SPRINGMEADOW DR . . 00:15:25",
         "CALL:CARBON MONOXIDE ALARM",
         "INFO:CO ALARM",
         "ADDR:53 TIMBER RIDGE DR",
         "CITY:HOLBROOK",
         "X:SPRINGMEADOW DR");
     
     doTest("T9",
         "MUTUAL AID - STRUCTURE FIRE at 62 POTOMAC  c/s: GRUNDY . . 15:08:02",
         "CALL:MUTUAL AID - STRUCTURE FIRE",
         "ADDR:62 POTOMAC",
         "X:GRUNDY");
     
     doTest("T10",
         "MOTOR VEHICLE ACCIDENT at LINCOLN AVE / VETERANS MEMORIAL HWY, HOLBROOK . . 12:16:23",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "ADDR:LINCOLN AVE & VETERANS MEMORIAL HWY",
         "CITY:HOLBROOK");
     
     doTest("T11",
         "MISC: UNKNOWN TYPE FIRE - SOUTH END OF MUSKET DR at MUSKET DR / CANNON DR, HOLBROOK . . 20:58:10",
         "CALL:MISC",
         "INFO:UNKNOWN TYPE FIRE - SOUTH END OF MUSKET DR",
         "ADDR:MUSKET DR & CANNON DR",
         "CITY:HOLBROOK");
     
     doTest("T12",
         "MUTUAL AID - STRUCTURE FIRE: 1 LADDER TO THE SCENE at 456 WAVERLY AVE  c/s: BUCKLEY RD . . 13:56:21",
         "CALL:MUTUAL AID - STRUCTURE FIRE",
         "INFO:1 LADDER TO THE SCENE",
         "ADDR:456 WAVERLY AVE",
         "X:BUCKLEY RD");
     
     doTest("T13",
         "AMBULANCE CALL: E/F FALL 17B1  at 229 SPRINGMEADOW DR #G, HOLBROOK  c/s: BEECH LN . . 08:39:55",
         "CALL:AMBULANCE CALL",
         "INFO:E/F FALL",
         "CODE:17B1",
         "ADDR:229 SPRINGMEADOW DR",
         "APT:G",
         "CITY:HOLBROOK",
         "X:BEECH LN");
     
     doTest("T14",
         "VEHICLE FIRE: CAR FIRE - MULTIPLE CALLS at 24 ANNANDALE ROAD  c/s: INVERNESS ROAD . . 06:07:57",
         "CALL:VEHICLE FIRE",
         "INFO:CAR FIRE - MULTIPLE CALLS",
         "ADDR:24 ANNANDALE ROAD",
         "X:INVERNESS ROAD");
     
     doTest("T15",
         "Hazmat: gas pumps knocked over at 125 Crooked hill road, Brentwood  c/s: Wicks road   O: 7-11 store . . 11:29:34",
         "CALL:Hazmat",
         "INFO:gas pumps knocked over",
         "ADDR:125 Crooked hill road",
         "CITY:Brentwood",
         "X:Wicks road",
         "PLACE:7-11 store");
   }
   
   @Test
   public void testFarmingdaleParser() {
     
     doTest("T1",
         "Signal 9 . . Stroke (CVA) at 64 DOUD ST. . 11:31:21",
         "CALL:Signal 9 - Stroke (CVA)",
         "ADDR:64 DOUD ST");
         
     doTest("T2",
         "General Alarm . . SMOKE ODOR IN AREA at 36 WAVERLY PL. . 17:04:46",
         "CALL:General Alarm - SMOKE ODOR IN AREA",
         "ADDR:36 WAVERLY PL");
         
     doTest("T3",
         "House Fire  Electrical Fire at 23 BEECHWOOD ST. . 18:24:03",
         "CALL:House Fire Electrical Fire",
         "ADDR:23 BEECHWOOD ST");
         
     doTest("T4",
         "Motor Vehicle Accident . . With Aided at intersection of MAIN ST and FULTON ST. . 11:46:09",
         "CALL:Motor Vehicle Accident - With Aided",
         "ADDR:MAIN ST and FULTON ST");
         
     doTest("T5",
         "Signal 9 . . Cardiac / Respiratory Arrest at 610 CONKLIN ST. . 06:20:05",
         "CALL:Signal 9 - Cardiac / Respiratory Arrest",
         "ADDR:610 CONKLIN ST");
    
   }
   
   @Test
   public void testSetauketParser() {
     
     doTest("T1",
         "AMBULANCE, CONVULSIONS / SEIZURES: 12-D-2 44 YOF SEIZURES at 43 NEAL PATH, SETAUKET   O: FAIRFIELD GABLES            TRUSS . . 19:39:51",
         "CALL:AMBULANCE, CONVULSIONS / SEIZURES",
         "ADDR:43 NEAL PATH",
         "CITY:SETAUKET",
         "PLACE:FAIRFIELD GABLES TRUSS",
         "CODE:12-D-2",
         "INFO:44 YOF SEIZURES");
 
     doTest("T2",
         "AMBULANCE, SICK PERSON (SPECIFIC DIAGNOSIS): 56 YOF SICK 26-A-5 IFO BUILDING at 5000 ROUTE 347, East Setauket  c/s: ARROWHEAD LN SOUTH   O: KOHLS . . 14:54:20",
         "CALL:AMBULANCE, SICK PERSON (SPECIFIC DIAGNOSIS)",
         "ADDR:5000 ROUTE 347",
         "CITY:East Setauket",
         "X:ARROWHEAD LN SOUTH",
         "PLACE:KOHLS",
         "CODE:26-A-5",
         "INFO:56 YOF SICK IFO BUILDING");
     
     doTest("T3",
         "MISC CALL, CHIEFS INVESTIGATION: GENERAL FIRE ALARM 52C04 Residential (multiple) at 700H HEALTH SCIENCES DR, STONY BROOK  c/s: NICOLLS RD   O: CHAPIN APTS - S: 26-A-16 at 20 FAWN LN W, SOUTH SETAUKET  c/s: LONGBOW LN . . 00:20:06",
         "CALL:MISC CALL, CHIEFS INVESTIGATION",
         "ADDR:700H HEALTH SCIENCES DR",
         "CITY:STONY BROOK",
         "X:NICOLLS RD",
         "PLACE:CHAPIN APTS",
         "INFO:GENERAL FIRE ALARM Residential (multiple)",
         "CODE:52C04");
         
     doTest("T4",
         "AMBULANCE, HEMORRHAGE / LACERATIONS: 21-A-1 - 61 YOM - CUT TO HAND at 8 HOLLY LN, EAST SETAUKET  c/s: CRANE NECK RD . . 16:57:21",
         "CALL:AMBULANCE, HEMORRHAGE / LACERATIONS",
         "ADDR:8 HOLLY LN",
         "CITY:EAST SETAUKET",
         "X:CRANE NECK RD",
         "CODE:21-A-1",
         "INFO:61 YOM - CUT TO HAND");
         
     doTest("T5",
         "MISC CALL WITH RESCUE, MVA: 29-B-1U - INJURIES at C/O, Setauket  c/s: WIRELESS RD . . 16:08:02",
         "CALL:MISC CALL WITH RESCUE, MVA",
         "ADDR:C/O",
         "CITY:Setauket",
         "X:WIRELESS RD",
         "CODE:29-B-1U",
         "INFO:INJURIES");
     
   }
   
   @Test
   public void testRockyPointParser() {
     
     doTest("T1",
         "Automatic Alarm, Residential at 38 CHERYL DR, SHOREHAM  c/s: COBBLESTONE DR . . 08:23:03",
         "CALL:Automatic Alarm, Residential",
         "ADDR:38 CHERYL DR",
         "CITY:SHOREHAM",
         "X:COBBLESTONE DR");
 
     doTest("T2",
         "Breathing Problems, Abnormal breathing at 151 BROADWAY, ROCKY POINT  c/s: CLUBHOUSE DR . . 12:42:23\n",
         "CALL:Breathing Problems, Abnormal breathing",
         "ADDR:151 BROADWAY",
         "CITY:ROCKY POINT",
         "X:CLUBHOUSE DR");
     
     doTest("T3",
         "Breathing Problems, Difficulty Speaking Between Breaths at 6 CROWN RD, ROCKY POINT  c/s: FISH RD . . 16:09:05\n",
         "CALL:Breathing Problems, Difficulty Speaking Between Breaths",
         "ADDR:6 CROWN RD",
         "CITY:ROCKY POINT",
         "X:FISH RD");
       
     doTest("T4",
         "Cardiac or Respiratory Arrest, Not breathing at all at 258 GLEN DR, SHOREHAM  c/s: LEISURE DR . . 14:31:40\n",
         "CALL:Cardiac or Respiratory Arrest, Not breathing at all",
         "ADDR:258 GLEN DR",
         "CITY:SHOREHAM",
         "X:LEISURE DR");
   }
   
   @Test
   public void testSouthamptonParser() {
     
     doTest("T1",
         "ALARM - GENERAL at 64 COUNTY RD 39, SOUTHAMPTON  c/s: NORTH SEA RD   O: HAMPTONS CENTER FOR REHAB . . 11:09:34",
         "CALL:ALARM - GENERAL",
         "ADDR:64 COUNTY RD 39",
         "CITY:SOUTHAMPTON",
         "X:NORTH SEA RD",
         "PLACE:HAMPTONS CENTER FOR REHAB");
     
     doTest("T2",
         "STRUCTURE FIRE, RESIDENTIAL at 133 North Magee St, Southampton  c/s: Sebonic Inlet Rd . . 16:42:18",
         "CALL:STRUCTURE FIRE, RESIDENTIAL",
         "ADDR:133 North Magee St",
         "CITY:Southampton",
         "X:Sebonic Inlet Rd");
     
     doTest("T3",
         "MISC ALARM, GAS LEAK at 271 Gin . . 09:24:18",
         "CALL:MISC ALARM, GAS LEAK",
         "ADDR:271 Gin");
 
     doTest("T4",
         "ALARM - GENERAL at 151 WINDMILL LN, SOUTHAMPTON  c/s: JAGGER LN   O: SVPD HQ . . 12:02:10",
         "CALL:ALARM - GENERAL",
         "ADDR:151 WINDMILL LN",
         "CITY:SOUTHAMPTON",
         "X:JAGGER LN",
         "PLACE:SVPD HQ");
     
     doTest("T5",
         "STRUCTURE FIRE, RESIDENTIAL at HILL ST / CAPTAINS NECK LN, SOUTHAMPTON . . 15:36:34",
         "CALL:STRUCTURE FIRE, RESIDENTIAL",
         "ADDR:HILL ST & CAPTAINS NECK LN",
         "CITY:SOUTHAMPTON");
     
   }
   
   @Test
   public void testBrentwood() {
 
     doTest("T1",
         "General Alarm, Auto Fire Alarm at 601 SUFFOLK AVE #201, BRENTWOOD  c/s: ADAMS AVE   O: LONG ISLAND EYE SURGICAL CARE . . 08:56:41",
         "CALL:General Alarm, Auto Fire Alarm",
         "ADDR:601 SUFFOLK AVE",
         "APT:201",
         "CITY:BRENTWOOD",
         "X:ADAMS AVE",
         "PLACE:LONG ISLAND EYE SURGICAL CARE");
     
   }
   
   @Test
   public void testHuntingtonManor() {
 
     doTest("T1",
         "STRUCTURE FIRE at 7 HARE PLACE. . 15:27:04",
         "CALL:STRUCTURE FIRE",
         "ADDR:7 HARE PLACE");
   }
   
   @Test
   public void testStJames() {
 
     doTest("T1",
         "Ambulance Call, Unknown Problem: 32-B-2 at 911 FENWAY ROAD, St. James  c/s: FAIRFIELD DRIVE . . 12:21:46",
         "CALL:Ambulance Call, Unknown Problem",
         "CODE:32-B-2",
         "ADDR:911 FENWAY ROAD",
         "CITY:St. James",
         "X:FAIRFIELD DRIVE");
   }
   
   @Test
   public void testHamptonBays() {
 
     doTest("T1",
         "Ambulance Call: mva 1 injury at 338 West Montauk Highway, Hampton Bays  c/s: Bess Lane . . 13:45:27",
         "CALL:Ambulance Call",
         "INFO:mva 1 injury",
         "ADDR:338 West Montauk Highway",
         "CITY:Hampton Bays",
         "X:Bess Lane");
 
     doTest("T2",
         "Ambulance Call: a/m inj fall main lifeguard stand at 280 Dune Road, Hampton Bays  c/s: Road I   O: Ponquogue Beach Pavillion . . 13:04:55",
         "CALL:Ambulance Call",
         "INFO:a/m inj fall main lifeguard stand",
         "ADDR:280 Dune Road",
         "CITY:Hampton Bays",
         "X:Road I",
         "PLACE:Ponquogue Beach Pavillion");
 
     doTest("T3",
         "Ambulance Call, Unconscious / Syncope: 63 YOM UNC... at 500 PECONIC STREET #335A  c/s: LOUIS KOSSUTH AVENUE . . 15:23:11",
         "CALL:Ambulance Call, Unconscious / Syncope",
         "INFO:63 YOM UNC...",
         "ADDR:500 PECONIC STREET",
         "APT:335A",
         "X:LOUIS KOSSUTH AVENUE");
 
     doTest("T4",
         "Sick Person (Specific Diagnosis), New Onset of Immobility at 534 LEISURE DR, SHOREHAM  c/s: LAUREL DR . . 13:00:36",
         "CALL:Sick Person (Specific Diagnosis), New Onset of Immobility",
         "ADDR:534 LEISURE DR",
         "CITY:SHOREHAM",
         "X:LAUREL DR");
   }
   
  @Test
  public void testGreenLawn() {

    doTest("T1",
        "Driver and/or EMT Needed for 2508 PAUMANACK VILLAGE DR . . 13:16:53",
        "CALL:Driver and/or EMT Needed for",
        "ADDR:2508 PAUMANACK VILLAGE DR");

  }
  
   
   public static void main(String[] args) {
     new NYSuffolkCountyCParserTest().generateTests("T4", "CALL INFO ADDR CITY X PLACE");
   }
 
 }
