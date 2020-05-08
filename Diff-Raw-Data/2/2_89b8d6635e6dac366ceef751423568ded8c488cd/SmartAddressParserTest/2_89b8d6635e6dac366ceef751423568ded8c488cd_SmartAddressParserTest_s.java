 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.parsers.SmartAddressParser.StartType;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 
 public class SmartAddressParserTest extends BaseParserTest {
   
   private static final StartType ADDR = StartType.START_ADDR;
   private static final StartType CALL = StartType.START_CALL;
   private static final StartType CALL_PLACE = StartType.START_CALL_PLACE;
   private static final StartType PLACE = StartType.START_PLACE;
   private static final StartType SKIP = StartType.START_SKIP;
   
   private static final int FLAG_START_FLD_REQ = SmartAddressParser.FLAG_START_FLD_REQ;
   private static final int FLAG_AT_BOTH = SmartAddressParser.FLAG_AT_BOTH;
   private static final int FLAG_ANCHOR_END = SmartAddressParser.FLAG_ANCHOR_END;
   private static final int FLAG_IMPLIED_INTERSECT = SmartAddressParser.FLAG_IMPLIED_INTERSECT;
   private static final int FLAG_PAD_FIELD = SmartAddressParser.FLAG_PAD_FIELD;
   private static final int FLAG_CHECK_STATUS = SmartAddressParser.FLAG_CHECK_STATUS;
   private static final int FLAG_ONLY_CITY = SmartAddressParser.FLAG_ONLY_CITY;
   private static final int FLAG_ONLY_CROSS = SmartAddressParser.FLAG_ONLY_CROSS;
   private static final int FLAG_CROSS_FOLLOWS = SmartAddressParser.FLAG_CROSS_FOLLOWS;
   private static final int FLAG_NO_IMPLIED_APT = SmartAddressParser.FLAG_NO_IMPLIED_APT;
   private static final int FLAG_START_FLD_NO_DELIM = SmartAddressParser.FLAG_START_FLD_NO_DELIM;
   private static final int FLAG_OPT_STREET_SFX = SmartAddressParser.FLAG_OPT_STREET_SFX;
   
   private static final String[] CITY_LIST = new String[]{"KENSBURG", "KEN TOWN", "9999"};
   private static final String DEF_CITY = "STATE OF MIND";
   private static final String DEF_STATE = "XX";
 
   private TestParser parser;
   
   public SmartAddressParserTest() {
     parser = new TestParser(CITY_LIST, DEF_CITY, DEF_STATE);
     parser.setupMultiWordStreets("JEAN GOUL", "PAUL G GETTY");
     setParser(parser, DEF_CITY, DEF_STATE);
   }
   
   @Test
   public void testProblem() {
   }
   
   @Test
   public void testProblems() {
     
     doTest(ADDR, "22030 OXFORD CT APT",
         "ADDR:22030 OXFORD CT");
     
     doTest(ADDR, FLAG_ANCHOR_END | FLAG_CHECK_STATUS, "1053 OLD COUNTRY RD PEARLE VISION CENTER",
         "ADDR:1053 OLD COUNTRY RD PEARLE VISION CENTER");
     
     doTest(CALL, FLAG_PAD_FIELD | FLAG_ANCHOR_END, "Not Breathing 263 MORGANS CHOICE RD ken town",
         "CALL:Not Breathing",
         "ADDR:263 MORGANS CHOICE RD",
         "CITY:ken town");
     
     doTest(PLACE, "167000 INTERSTATE 81 KENSBURG",
         "ADDR:167000 INTERSTATE 81",
         "CITY:KENSBURG");
     
     doTest(PLACE, "5009 PINE VIEW DR / PINE WOOD DR 7-23",
         "ADDR:5009 PINE VIEW DR",
         "X:PINE WOOD DR",
         "APT:7-23");
     
     doTest(ADDR, FLAG_CROSS_FOLLOWS, "WHITE TAIL DR / FAIRWAY DR 9999 FAIRWAY DR//SOMETHING IN THE MIDDLE OF THE STREET IS ON FIRE/UNK WHAT",
         "ADDR:WHITE TAIL DR & FAIRWAY DR",
         "CITY:9999");
     
     doTest(ADDR, FLAG_CROSS_FOLLOWS, "POCONO MOUNTAIN WEST 9999 PANTHER LN          OFFICE EXT 81400  // GENRAL FIRE ALARM",
                  "ADDR:POCONO MOUNTAIN WEST",
                  "CITY:9999");
     
     doTest(CALL, "MVA ROLLOVER/20 HWY SERGEANT RD",
                  "CALL:MVA ROLLOVER /",
                  "ADDR:20 HWY SERGEANT RD");
     
     doTest(ADDR, FLAG_NO_IMPLIED_APT, "9922 OLD STATE RD 90 YO FEMALE DIFF BREATHING",
                  "ADDR:9922 OLD STATE RD");
     
     doTest(ADDR, "4368 OLD HIGHWAY 71 SAGAMOUNT BIBLE CAMP",
                  "ADDR:4368 OLD HIGHWAY 71");
     
     doTest(ADDR, "22576 MACARTHUR BL SUITE 354",
            "ADDR:22576 MACARTHUR BL",
            "APT:354");
     
     doTest(ADDR, "200 N 11 ST SUITE: APT 5 KENSBURG",
            "ADDR:200 N 11 ST",
            "APT:APT 5",
            "CITY:KENSBURG");
     
     doTest(CALL, FLAG_START_FLD_REQ, "CHEST PAIN - BREATH NORMAL=>35 5125 W RIDGE RD",
            "CALL:CHEST PAIN - BREATH NORMAL=>35",
            "ADDR:5125 W RIDGE RD");
     
     doTest(CALL, "FALLS (GRD/FLR) POSS DANGER BO 9184 OLD WATTSBURG RD XS: HORSESHOE DR KEN TOWN",
            "CALL:FALLS (GRD/FLR) POSS DANGER BO",
            "ADDR:9184 OLD WATTSBURG RD",
            "X:HORSESHOE DR",
            "CITY:KEN TOWN");
         
     doTest(CALL, "FIRE ALARM COMMERCIAL/INDUST 101 MAPLE DR XS: ROUTE 322 ELK TWP MC ALARMS",
            "CALL:FIRE ALARM COMMERCIAL/INDUST", 
            "ADDR:101 MAPLE DR",
            "X:ROUTE 322");
      
     doTest(CALL,"EMS-UNCONSCIOUS VIRGIL H GOODE HWY & GLEN MEADOW HWY",
           "CALL:EMS-UNCONSCIOUS",
           "ADDR:VIRGIL H GOODE HWY & GLEN MEADOW HWY");
     
     doTest(PLACE, FLAG_AT_BOTH | FLAG_ANCHOR_END, "515 FLETCHWOOD RD @WAREHOUSE S",
            "ADDR:515 FLETCHWOOD RD",
            "PLACE:WAREHOUSE S");
     
     doTest(CALL, "MVA-TRAPPED MULTI PT/ADD RESPO LAKE PLEASANT RD&TATE RD EXTRA",
           "CALL:MVA-TRAPPED MULTI PT/ADD RESPO",
           "ADDR:LAKE PLEASANT RD & TATE RD");
 
     doTest(SKIP, "707-1/2 5TH AV SE UNCONSCIOUS",
            "ADDR:707-1/2 5TH AV SE");
     
     doTest(CALL, "LOCATION CLOVER CREEK RD/DEAD INDIAN MEMORIAL RD",
         "CALL:LOCATION",
         "ADDR:CLOVER CREEK RD & DEAD INDIAN MEMORIAL RD");
     
     doTest(CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, 
         "Please Operate 33.88/  Lift Assist 2 N  POTVIN AVE",
         "CALL:Please Operate 33.88/Lift Assist",
         "ADDR:2 N POTVIN AVE");
 
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @N JEFFERSON ST AWAY",
         "CALL:BIG HOME",
         "ADDR:N JEFFERSON ST");
     
     doTest(ADDR, "22085 ST LEONARDS CIR EXTRA",
         "ADDR:22085 ST LEONARDS CIR");
     
     doTest(CALL, "AMH KLEIN W RD&FOREST N RD MVA 2 CAR MVA",
         "CALL:AMH",
         "ADDR:KLEIN W RD & FOREST N RD");
    
     doTest(ADDR, FLAG_PAD_FIELD, "76 YARDNER ST Suite:UNIT29 KENSBURG EXTRA",
         "ADDR:76 YARDNER ST",
         "APT:UNIT29",
         "CITY:KENSBURG");
     
     doTest(CALL, "563 ST RTE 211 E",
         "ADDR:563 ST RTE 211 E");
     
     doTest(CALL, FLAG_PAD_FIELD, "29D2L MVC Motorcycle/Bicycle 375 GATEWAY SOUTH BLVD SONIC - SOUTH KENSBURG EXTRA",
         "CALL:29D2L MVC Motorcycle/Bicycle",
         "ADDR:375 GATEWAY SOUTH BLVD",
         "SRC:SONIC - SOUTH",
         "CITY:KENSBURG");
 
     doTest(CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, "80 - Carbon Monoxide/Hazmat 1064 NE 75 RD Apt. 112 KEN TOWN",
         "CALL:80 - Carbon Monoxide/Hazmat",
         "ADDR:1064 NE 75 RD",
         "APT:112",
         "CITY:KEN TOWN");
     
     doTest(CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, "FIRE GRASS SE DD HWY & SE 341 RD KEN TOWN",
         "CALL:FIRE GRASS",
         "ADDR:SE DD HWY & SE 341 RD",
         "CITY:KEN TOWN");
 
     doTest(CALL, FLAG_ANCHOR_END, "MOTOR VEHICLE ACCIDENT WITH RESCUE  HIGHWAY A & HIGHWAY 50 KEN TOWN",
         "CALL:MOTOR VEHICLE ACCIDENT WITH RESCUE",
         "ADDR:HIGHWAY A & HIGHWAY 50",
         "CITY:KEN TOWN");
     
     doTest(PLACE, FLAG_ANCHOR_END, "NEW HYDE PARK DINER 160 HILLSIDE AVE C/S: 1ST (N) ST / 2ND (N) ST",
         "PLACE:NEW HYDE PARK DINER",
         "ADDR:160 HILLSIDE AVE",
         "X:1ST (N) ST / 2ND (N) ST");
     
     doTest(CALL, FLAG_ANCHOR_END, "NEXT TO 4574",
         "CALL:NEXT TO 4574");
     
     doTest(CALL, "F-ACCIDENT W/ INJURIES BOLDEN RD XS: COOPER RD & PITTSBURGH RD KENSBURG STUFF",
         "CALL:F-ACCIDENT W/INJURIES",
         "ADDR:BOLDEN RD",
         "X:COOPER RD & PITTSBURGH RD",
         "CITY:KENSBURG");
     
     doTest(PLACE, "RT 30/EMORY RD RESCUE",
         "ADDR:RT 30 & EMORY RD");
     
     doTest(SKIP, "LA LA LA COUNTRY PLACE CT & W 16TH ST BEYOND",
         "ADDR:COUNTRY PLACE CT & W 16TH ST");
     
     doTest(ADDR, "COUNTRY PLACE CT / RENAISSANCE WOOD CT XENIA TWP",
         "ADDR:COUNTRY PLACE CT & RENAISSANCE WOOD CT");
     
     doTest(ADDR, "22030 OXFORD CT APT KEN TOWN",
         "ADDR:22030 OXFORD CT",
         "CITY:KEN TOWN");
     
     doTest(CALL, FLAG_AT_BOTH,
         "VEHICLE FIRE HARDEES @800 WOODS RD KENSBURG",
         "CALL:VEHICLE FIRE HARDEES",
         "ADDR:800 WOODS RD",
         "CITY:KENSBURG");
    
     doTest(CALL,
         "***SIG 8***  AUTO PIERCE DAY CAMP 37 MINEOLA AVE [ROSLYN] c/s: REGENT PL/GLEN AVE ADTNL: MANSION/NO PERMIT GRID: K-14 TOA: 08:32 12/07/2010",
         "CALL:***SIG 8*** AUTO PIERCE DAY CAMP",
         "ADDR:37 MINEOLA AVE");
     
     doTest(CALL, TestParser.FLAG_START_FLD_REQ, 
         "1073 SMOKE 1421 BEVERLY DR BOSA NONA DRIVE",
         "CALL:1073 SMOKE",
         "ADDR:1421 BEVERLY DR");
   }
   
   @Test
   public void testRoadCityConflicts() {
     doTest(ADDR, "300 KENSBURG ST KENSBURG EXTRA",
         "ADDR:300 KENSBURG ST",
         "CITY:KENSBURG");
     doTest(ADDR, "100 BLACK ST XS: KENSBURG RD KENSBURG EXTRA",
         "ADDR:100 BLACK ST",
         "X:KENSBURG RD",
         "CITY:KENSBURG");
     doTest(ADDR, "KENSBURG RD & KENSBURG AVE KENSBURG EXTRA",
         "ADDR:KENSBURG RD & KENSBURG AVE",
         "CITY:KENSBURG");
     doTest(ADDR, "300 N KENSBURG ST KENSBURG EXTRA",
         "ADDR:300 N KENSBURG ST",
         "CITY:KENSBURG");
     doTest(ADDR, FLAG_CROSS_FOLLOWS, "41 HOLTZCLAW ST KENSBURG ORANGE ST X WINFIELD ST",
         "ADDR:41 HOLTZCLAW ST",
         "CITY:KENSBURG");
   }
   
   @Test
   public void testDoubleRoutes() {
 
     doTest(ADDR, "134 US HIGHWAY 85 NB EXTRA",
         "ADDR:134 US HIGHWAY 85 NB");
     doTest(ADDR, "US HIGHWAY 85 NB/E 77TH AVE ADAM ADAM",
         "ADDR:US HIGHWAY 85 NB & E 77TH AVE");
     doTest(ADDR, "ST HWY 85 EXTRA",
         "ADDR:ST HWY 85");
     doTest(CALL, "BAD STUFF 134 US HIGHWAY 85 NB EXTRA",
         "CALL:BAD STUFF",
         "ADDR:134 US HIGHWAY 85 NB");
     doTest(CALL, "BAD STUFF US HIGHWAY 85 NB/E 77TH AVE ADAM ADAM",
         "CALL:BAD STUFF",
         "ADDR:US HIGHWAY 85 NB & E 77TH AVE");
     doTest(CALL, "BAD STUFF ST HWY 85 EXTRA",
         "CALL:BAD STUFF",
         "ADDR:ST HWY 85");
     doTest(CALL, "CALL 123 COUNTY ROAD 45 EXTRA",
         "CALL:CALL",
         "ADDR:123 COUNTY ROAD 45");
 
   }
   
   @Test
   public void testTrivialAddress() {
     doTest(ADDR, "MY HOME IN KENSBURG LOOKS NICE", 
         "ADDR:MY HOME IN", 
         "CITY:KENSBURG");
     doTest(ADDR, "My home in Kensburg looks nice",
         "ADDR:My home in",
         "CITY:Kensburg");
     doTest(ADDR, "My home in ken town is better",
         "ADDR:My home in",
         "CITY:ken town");
     doTest(ADDR, "Big st/Lost rd ken town",
         "ADDR:Big st & Lost rd",
         "CITY:ken town");
   }
   
   @Test
   public void testSimpleAddress() {
     doTest(ADDR, "1000 N JACK DANIEL LN MORE JUNK",
         "ADDR:1000 N JACK DANIEL LN");
     doTest(ADDR, "1000 HWY 20 MORE JUNK",
        "ADDR:1000 HWY 20");
     doTest(CALL, "BAD BLOOD HAPPENS 1000 N JACK DANIEL LN MORE JUNK",
         "CALL:BAD BLOOD HAPPENS",
         "ADDR:1000 N JACK DANIEL LN");
     doTest(PLACE, "MARY AND ST JOHNS 1000 N JACK DANIEL LN MORE JUNK",
         "PLACE:MARY AND ST JOHNS",
         "ADDR:1000 N JACK DANIEL LN");
     doTest(SKIP, "MARY AND ST JOHNS 1000 N JACK DANIEL LN MORE JUNK",
         "ADDR:1000 N JACK DANIEL LN");
     doTest(SKIP, "SOME DAY OVER THE RAINBOW 100 BLUEBIRDS SING IN KENSBURG HIGH",
         "ADDR:100 BLUEBIRDS SING IN",
         "CITY:KENSBURG");
     doTest(SKIP, "WHERE 24 BLOOD KEN TOWN STORY",
         "ADDR:24 BLOOD",
         "CITY:KEN TOWN");
     doTest(SKIP, "BAD TOWN KEN AT 100 KEN TOWN RD KENSBURG EXTRA",
         "ADDR:100 KEN TOWN RD",
         "CITY:KENSBURG");
     doTest(SKIP, "BAD TOWN KEN AT 100 KENSBURG RD KEN TOWN EXTRA",
         "ADDR:100 KENSBURG RD",
         "CITY:KEN TOWN");
     doTest(SKIP, "BARK PLACE 500 US-30 DOWNTOWN",
         "ADDR:500 US-30");
     doTest(SKIP, "BARK PLACE 500 st123 downstairs",
         "ADDR:500 st-123");
     doTest(SKIP, "TRY 123 JOHN SMITH ST N FOR SIZE",
         "ADDR:123 JOHN SMITH ST N");
     doTest(SKIP, "OR MAYBE 200 US30 N BACK",
         "ADDR:200 US-30 N");
     doTest(SKIP, "NUMBER 25 MASKING ADDRESS AT 143 N JUNIOR ST DOWNTOWN",
         "ADDR:143 N JUNIOR ST");
     doTest(SKIP, "DRIVING ON 3224 XX 456",
         "ADDR:3224 XX 456");
     doTest(SKIP, "BUT USING 123 N THIRD DR N JUST ISNT RIGHT",
         "ADDR:123 N THIRD DR N");
     doTest(ADDR, "2000 COUNTRY PLACE CT EXTRA", 
         "ADDR:2000 COUNTRY PLACE CT");
     doTest(SKIP, "JUNK STUFF 2000 COUNTRY PLACE CT EXTRA", 
         "ADDR:2000 COUNTRY PLACE CT");
     doTest(SKIP, "JUNK STUFF 2000-2100 COUNTRY PLACE CT EXTRA", 
     "ADDR:2000-2100 COUNTRY PLACE CT");
   }
   
   @Test
   public void testSimpleAddressWithCrossStreets() {
     doTest(SKIP, "JUNK STUFF 2000 COUNTRY PLACE / N BLACK ST EXTRA",
            "ADDR:2000 COUNTRY PLACE",
            "X:N BLACK ST");
   }
   
   @Test
   public void testCrossStreets() {
     doTest(ADDR, "1000 SUNBURY ONCE XS: ACROSS TOWN KENSBURG JUNK",
         "ADDR:1000 SUNBURY ONCE",
         "X:ACROSS TOWN",
         "CITY:KENSBURG");
     doTest(ADDR, "1000 SUNBURY ONCE XS: ACROSS TOWN KEN TOWN JUNK",
         "ADDR:1000 SUNBURY ONCE",
         "X:ACROSS TOWN",
         "CITY:KEN TOWN");
     doTest(SKIP, "WAIT TILL 500 NORTH LN X: JACKSON ST BELOW SUNDANCE",
         "ADDR:500 NORTH LN",
         "X:JACKSON ST");
     doTest(SKIP, "WAIT TILL 500 NORTH LN X: JACKSON ST & N JOHNSTON RD BELOW SUNDANCE",
         "ADDR:500 NORTH LN",
         "X:JACKSON ST & N JOHNSTON RD");
     doTest(SKIP, "WAIT TILL 500 NORTH LN X: JACKSON ST & N SWEET HILL RD BELOW SUNDANCE",
         "ADDR:500 NORTH LN",
         "X:JACKSON ST & N SWEET HILL RD");
     doTest(SKIP, "WAIT TILL 500 NORTH LN X: JACKSON ST & BAD SWEET HILL RD BELOW SUNDANCE",
         "ADDR:500 NORTH LN",
         "X:JACKSON ST & BAD SWEET HILL RD");
     doTest(SKIP, "WAIT TILL 500 NORTH LN X: JACKSON ST & TOO BAD SWEET HILL RD BELOW SUNDANCE",
         "ADDR:500 NORTH LN",
         "X:JACKSON ST");
   }
   
   @Test
   public void testIntersections() {
     doTest(ADDR, "BLACK ST & WEST BRIGHT RD AFTER HOURS",
         "ADDR:BLACK ST & WEST BRIGHT RD");
     doTest(ADDR, "COUNTRY PLACE CT & W 16TH ST BEYOND",
         "ADDR:COUNTRY PLACE CT & W 16TH ST");
     doTest(SKIP, "CAN WE FIND HWY 20 AND WEST BRIGHT RD KENSBURG BLACK",
         "ADDR:HWY 20 AND WEST BRIGHT RD",
         "CITY:KENSBURG");
     doTest(SKIP, "CAN WE FIND W BRIGHT RD AND HWY 20 KENSBURG",
         "ADDR:W BRIGHT RD AND HWY 20",
         "CITY:KENSBURG");
     doTest(SKIP, "NOW SEE IF CAN WE FIND HWY 20 AND WEST BRIGHT RD WITHOUT A CITY",
         "ADDR:HWY 20 AND WEST BRIGHT RD");
     doTest(SKIP, "NOW SEE IF CAN WE FIND W BRIGHT RD/HWY 20 WITHOUT A CITY",
         "ADDR:W BRIGHT RD & HWY 20");
     doTest(SKIP, "LOOKING FOR I-90 & US231 SOMEWHERE",
         "ADDR:I-90 & US-231");
     doTest(SKIP, "WHAT ABOUT BLACK ST N & W SOMERSET RD ANYONE",
         "ADDR:BLACK ST N & W SOMERSET RD");
     doTest(SKIP, "SOMEWHERE NEAR US50 N & BLACK ST DOWN",
         "ADDR:US-50 N & BLACK ST");
     doTest(SKIP, "THIS IS N JOHNSON AVE S & BLACK ST VERY BAD FORM",
         "ADDR:N JOHNSON AVE S & BLACK ST");
     doTest(SKIP, "ACCIDENT W/INJURY SRT24&SAINT MARYS RD KEN TOWN UNDER",
         "ADDR:ST-24 & SAINT MARYS RD",
         "CITY:KEN TOWN");
     doTest(CALL, "MVA-UKN INJURY SW TUALATIN VALLEY HW/SW331ST AV",
         "CALL:MVA-UKN INJURY",
         "ADDR:SW TUALATIN VALLEY HW & SW331ST AV");
     doTest(SKIP, "LA LA LA COUNTRY PLACE CT & W 16TH ST BEYOND",
         "ADDR:COUNTRY PLACE CT & W 16TH ST");
   }
   
   @Test
   public void testNakedRoad() {
     doTest(ADDR, "BLAKSLY CREEK RD IN CHICAGO",
     		"ADDR:BLAKSLY CREEK RD");
     doTest(SKIP, "WHERE CAN I FIND BLAKSLY CREEK RD IN CHICAGO",
         "ADDR:BLAKSLY CREEK RD");
     doTest(SKIP, "WHERE CAN I FIND N BLAKSLY CREEK RD IN KENSBURG",
         "ADDR:N BLAKSLY CREEK RD IN",
         "CITY:KENSBURG");
     doTest(SKIP, "WHERE CAN I FIND N SWEET BLAKSLY CREEK RD IN KENSBURG",
         "ADDR:N SWEET BLAKSLY CREEK RD IN",
         "CITY:KENSBURG");
     doTest(SKIP, "WHERE CAN I FIND N BIG SWEET BLAKSLY BROWN RD IN KENSBURG",
         "ADDR:BROWN RD IN",
         "CITY:KENSBURG");
     doTest(ADDR, "US 26 IN CHICAGO",
         "ADDR:US 26");
     doTest(ADDR, "COUNTRY PLACE CT DOWNTOWN",
         "ADDR:COUNTRY PLACE CT");
     doTest(SKIP, "WHERE CAN I FIND I 506 IN CHICAGO",
         "ADDR:I 506");
     doTest(SKIP, "WHERE CAN I FIND ST 101 SOMEWHERE OUT WEST",
         "ADDR:ST 101");
     doTest(SKIP, "WHERE IS BLAKSLY CREED RD X: BLUE LN IN KENSBURG",
         "ADDR:CREED RD",
         "X:BLUE LN IN",
         "CITY:KENSBURG");
     doTest(SKIP, "WHERE IS ST 345 X: PINE ST IN CHICAGO",
         "ADDR:ST 345",
         "X:PINE ST");
     doTest(SKIP, "CAN YOU FIND I-25 OUT THERE SOMEWHERE",
         "ADDR:I-25");
     doTest(SKIP, "WHAT ABOUT us123 PERHAPS",
         "ADDR:us-123");
     doTest(SKIP, "MAYBE BLACK ST N WILL WORK",
         "ADDR:BLACK ST N");
     doTest(SKIP, "HOW ABOUT US-150 N DUDE",
         "ADDR:US-150 N");
     doTest(SKIP, "BAD BOY N BIGHILL RD S BOB",
         "ADDR:N BIGHILL RD S");
     doTest(SKIP, "LOGSTON COUNTRY PLACE CT DOWNTOWN",
         "ADDR:COUNTRY PLACE CT");
   }
   
   @Test
   public void testParseFallback() {
     doTest(ADDR, "SOMETIMES THINGS JUST DONT WORK",
         "ADDR:SOMETIMES THINGS JUST DONT WORK");
     doTest(SKIP, "SOMETIMES THINGS JUST DONT WORK",
         "ADDR:SOMETIMES THINGS JUST DONT WORK");
     doTest(CALL, "SOMETIMES THINGS JUST DONT WORK",
         "CALL:SOMETIMES THINGS JUST DONT WORK");
     doTest(PLACE, "SOMETIMES THINGS JUST DONT WORK",
         "PLACE:SOMETIMES THINGS JUST DONT WORK");
     doTest(ADDR, FLAG_ANCHOR_END, "BAD ADDRESS KEN TOWN",
         "ADDR:BAD ADDRESS",
         "CITY:KEN TOWN");
     doTest(CALL, "WHERE IS MY 34 BOOK",
         "CALL:WHERE IS MY",
         "ADDR:34 BOOK");
   }
   
   @Test
   public void testAtAddress() {
     doTest(PLACE, "BIG HOME AT KENS LAST HOUSE KEN TOWN",
         "PLACE:BIG HOME",
         "ADDR:KENS LAST HOUSE",
         "CITY:KEN TOWN");
     doTest(PLACE, "BIG HOME @BAD 1000 N THIRD AVE",
         "PLACE:BIG HOME",
         "ADDR:BAD 1000 N THIRD AVE");
     doTest(PLACE, "BIG HOME @ BAD JACKSON RD & HAVEL DR AWAY",
         "PLACE:BIG HOME",
         "ADDR:BAD JACKSON RD & HAVEL DR");
     doTest(CALL, "BIG FIRE REPORTED AT BAD 200 NE JOHNSON DR",
         "CALL:BIG FIRE",
         "ADDR:BAD 200 NE JOHNSON DR");
     doTest(CALL, "Not breathing at all at 258 GLEN DR SOMEWHERE",
         "CALL:Not breathing at all",
         "ADDR:258 GLEN DR");
     doTest(CALL, "EMERGENCY RUN 31 SCROGGS RD XS: LOUISA CHAPEL @ 1020 KENSBURG EXTRA",
         "CALL:EMERGENCY RUN",
         "ADDR:31 SCROGGS RD",
         "X:LOUISA CHAPEL @ 1020",
         "CITY:KENSBURG");
   }
   
   @Test
   public void testFlexAtAddress() {
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @ KENS LAST HOUSE @ BIG RED KEN TOWN",
         "CALL:BIG HOME",
         "ADDR:KENS LAST HOUSE",
         "PLACE:BIG RED",
         "CITY:KEN TOWN");
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @KENS LAST HOUSE @BIG RED KEN TOWN",
         "CALL:BIG HOME",
         "ADDR:KENS LAST HOUSE",
         "PLACE:BIG RED",
         "CITY:KEN TOWN");
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @1000 N THIRD AVE",
         "CALL:BIG HOME",
         "ADDR:1000 N THIRD AVE");
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @JACKSON RD & HAVEL DR AWAY",
         "CALL:BIG HOME",
         "ADDR:JACKSON RD & HAVEL DR");
     doTest(CALL, FLAG_AT_BOTH, "BIG HOME @N JEFFERSON ST AWAY",
         "CALL:BIG HOME",
         "ADDR:N JEFFERSON ST");
     doTest(CALL, FLAG_AT_BOTH, "PROBLEM 250 N 28TH ST @COLUMBIA PICTURES APT 5 AWAY",
         "CALL:PROBLEM",
         "ADDR:250 N 28TH ST",
         "PLACE:COLUMBIA PICTURES",
         "APT:5");
     doTest(CALL, FLAG_AT_BOTH, "PROBLEM 250 N 28TH ST @ COLUMBIA PICTURES APT 5 AWAY",
         "CALL:PROBLEM",
         "ADDR:250 N 28TH ST",
         "PLACE:COLUMBIA PICTURES",
         "APT:5");
     doTest(CALL, FLAG_AT_BOTH, "MY PROBLEM ANDERSON RD/PETER ST @BIG TOP XS: JOHN RD AND JILL ST AWAY",
         "CALL:MY PROBLEM",
         "ADDR:ANDERSON RD & PETER ST",
         "PLACE:BIG TOP",
         "X:JOHN RD AND JILL ST");
     doTest(CALL, FLAG_AT_BOTH, "MY PROBLEM ANDERSON RD/PETER ST @ BIG TOP XS: JOHN RD AND JILL ST AWAY",
         "CALL:MY PROBLEM",
         "ADDR:ANDERSON RD & PETER ST",
         "PLACE:BIG TOP",
         "X:JOHN RD AND JILL ST");
     doTest(CALL, FLAG_AT_BOTH, "YOUR PROBLEM N GREEN HILL RD @CLEMINTINE JOHNSON",
         "CALL:YOUR PROBLEM",
         "ADDR:N GREEN HILL RD",
         "PLACE:CLEMINTINE");
     doTest(CALL, FLAG_AT_BOTH, "YOUR PROBLEM N GREEN HILL RD @ CLEMINTINE JOHNSON",
         "CALL:YOUR PROBLEM",
         "ADDR:N GREEN HILL RD",
         "PLACE:CLEMINTINE");
   }
   
   @Test
   public void testEndAnchor() {
     doTest(PLACE, FLAG_ANCHOR_END, "BLACK STREET 500 W TOWNSEND BLOCK APT 50",
     		"PLACE:BLACK STREET",
     		"ADDR:500 W TOWNSEND BLOCK",
     		"APT:50");
     doTest(PLACE, FLAG_ANCHOR_END, "BLACK STREET 500 W TOWNSEND BLOCK KEN TOWN",
         "PLACE:BLACK STREET",
         "ADDR:500 W TOWNSEND BLOCK",
         "CITY:KEN TOWN");
     doTest(PLACE, FLAG_ANCHOR_END, "BLACK STREET 500 W TOWNSEND BLOCK KEN TOWN WEST",
         "PLACE:BLACK STREET",
         "ADDR:500 W TOWNSEND BLOCK KEN TOWN WEST");
   }
   
   @Test
   public void testAptNumbers() {
     doTest(ADDR, "1500 BLACK ST APT: 10J XX XS: JOHN & MARY KENSBURG HOME",
         "ADDR:1500 BLACK ST",
         "APT:10J XX",
         "X:JOHN & MARY",
         "CITY:KENSBURG");
     
     doTest(ADDR, "1500 BLACK ST APT: 6XX BELOW",
         "ADDR:1500 BLACK ST",
         "APT:6XX");
     
     doTest(ADDR, "1500 BLACK ST 44A BELOW",
         "ADDR:1500 BLACK ST",
         "APT:44A");
     
     doTest(ADDR, "1500 BLACK ST #20 BELOW",
         "ADDR:1500 BLACK ST",
         "APT:20");
   }
   
   @Test 
   public void testSlashDesc() {
     doTest(CALL, "MVA W/ROLLOVER 500 N SUMMER ST",
         "CALL:MVA W/ROLLOVER",
         "ADDR:500 N SUMMER ST");
   }
   
   @Test
   public void testRoadTokens() {
     doTest(SKIP, "BAD US-30", "ADDR:US-30");
     doTest(SKIP, "BAD us30", "ADDR:us-30");
     doTest(SKIP, "BAD ST30NB", "ADDR:ST-30NB");
     doTest(SKIP, "BAD I105WB", "ADDR:I-105WB");
     doTest(SKIP, "BAD CO7", "ADDR:CO-7");
     
     doTest(SKIP, "BAD ST30A", "ADDR:BAD ST30A");
     doTest(SKIP, "BAD ST30NBA", "ADDR:BAD ST30NBA");
   }
   
   @Test
   public void testHouseNumbers() {
     doTest(SKIP, "BAD 100 BLACK ST", "ADDR:100 BLACK ST");
     doTest(SKIP, "BAD 100-A BLACK ST", "ADDR:100-A BLACK ST");
     doTest(SKIP, "BAD 100B BLACK ST", "ADDR:100B BLACK ST");
     doTest(SKIP, "BAD 100AB BLACK ST", "ADDR:BLACK ST");
   }
   
   @Test
   public void testCheckAddress() {
     assertEquals(3, parser.checkAddress("400 N BLACK HAWK DR"));
     assertEquals(0, parser.checkAddress("400 N BLACK HAWK DR EXTRA"));
     assertEquals(2, parser.checkAddress("US 50 & SE JEFFERSON ST"));
     assertEquals(0, parser.checkAddress("US 50 & SE JEFFERSON ST EXTRA"));
     assertEquals(1, parser.checkAddress("SMITH RD"));
     assertEquals(0, parser.checkAddress("SMITH RD EXTRA"));
     assertEquals(0, parser.checkAddress("."));
     assertEquals(1, parser.checkAddress("OLD RT 2"));
   }
   
   @Test
   public void testCheckStatus() {
     assertEquals(0, parser.parseAddress(ADDR, FLAG_ANCHOR_END | FLAG_CHECK_STATUS, "715 CAMDEN CT DRYER ON FIRE").getStatus());
   }
 
   @Test
   public void testGpsCoords() {
     doTest(CALL, "VEHICLE ACCIDENT LL(-77:23:59.6013,39:21:53.1520) DOWN TOWN",
         "CALL:VEHICLE ACCIDENT",
         "ADDR:LL(-77:23:59.6013,39:21:53.1520)");
     doTest(ADDR, "VEHICLE ACCIDENT LL(-77:23:59.6013 39:21:53.1520) DOWN TOWN",
         "ADDR:VEHICLE ACCIDENT LL(-77:23:59.6013,39:21:53.1520)");
     doTest(CALL, FLAG_ANCHOR_END,
         "VEHICLE ACCIDENT LL(-77.42356013 39.23521520) DOWN TOWN",
         "CALL:VEHICLE ACCIDENT",
         "ADDR:LL(-77.42356013,39.23521520) DOWN TOWN");
   }
 
   @Test
   public void testImpliedIntersection() {
     doTest(SKIP, FLAG_IMPLIED_INTERSECT, "RANDOM JUNK BLOOD RD AUNT SALLY AV EXTRA",
         "ADDR:BLOOD RD & AUNT SALLY AV");
     doTest(SKIP, FLAG_IMPLIED_INTERSECT, "RANDOM JUNK BLOOD RD N AUNT SALLY AV EXTRA",
         "ADDR:BLOOD RD & N AUNT SALLY AV");
     doTest(ADDR, FLAG_IMPLIED_INTERSECT | FLAG_ANCHOR_END, "JUNK BLOOD RD N AUNT SALLY AV",
         "ADDR:JUNK BLOOD RD & N AUNT SALLY AV");
     doTest(CALL, FLAG_IMPLIED_INTERSECT, "Accident - Injury 148 AVE SE 33 ST SE BIG BAD JOHNS",
         "CALL:Accident - Injury",
         "ADDR:148 AVE SE & 33 ST SE");
   }
   
   @Test
   public void  testPadField() {
     doTest(ADDR, FLAG_PAD_FIELD, "100 SE JEFFERSON ST BARK PLACE KENSBURG",
         "ADDR:100 SE JEFFERSON ST",
         "SRC:BARK PLACE",
         "CITY:KENSBURG");
     doTest(ADDR, FLAG_PAD_FIELD | FLAG_ANCHOR_END, "SE JEFFERSON ST & BLACK RD BARK PLACE",
         "ADDR:SE JEFFERSON ST & BLACK RD",
         "SRC:BARK PLACE");
     doTest(SKIP, FLAG_PAD_FIELD, "BAD STUFF PETERSBURG RD BARK PLACE KENSBURG",
         "ADDR:PETERSBURG RD",
         "SRC:BARK PLACE",
         "CITY:KENSBURG");
     doTest(ADDR, FLAG_PAD_FIELD, "65 S WILLIAMS ST  Pad:APT 102 KENSBURG",
         "ADDR:65 S WILLIAMS ST",
         "SRC:Pad: APT 102",
         "CITY:KENSBURG");
     doTest(ADDR, FLAG_PAD_FIELD, "NOT AN ADDRESS KEN TOWN",
         "ADDR:NOT AN ADDRESS",
         "CITY:KEN TOWN");
     doTest(CALL, FLAG_PAD_FIELD, "Dispatched fall 302 Main Street Ken Town Elementary Ken Town bad news",
         "CALL:Dispatched fall",
         "ADDR:302 Main Street",
         "SRC:Ken Town Elementary",
         "CITY:Ken Town");
   }
   
   @Test
   public void testBadCross() {
     doTest(CALL, "SUNSHINE CITY XS: 100 BLACK ST",
         "CALL:SUNSHINE CITY",
         "X:100 BLACK ST");
     doTest(CALL, "SUNSHINE CITY XS: BLACK RD & 10 ST",
         "CALL:SUNSHINE CITY",
         "X:BLACK RD & 10 ST");
   }
   
   @Test
   public void testConfusedNameRoad() {
     doTest(ADDR, "1234 S KENSBURG ISLAND RD EXTRA",
         "ADDR:1234 S KENSBURG ISLAND RD");
     doTest(ADDR, "S 15 ST & N KENSBURG ISLAND RD EXTRA",
         "ADDR:S 15 ST & N KENSBURG ISLAND RD");
     doTest(ADDR, "N KENSBURG ISLAND RD EXTRA",
         "ADDR:N KENSBURG ISLAND RD");
   }
   
   @Test
   public void testNotAddressTokens() {
     doTest(CALL, "CALL 1234 () RD",
         "CALL:CALL 1234");
     doTest(CALL, "N () BLACK DR",
           "CALL:N ()",
           "ADDR:BLACK DR");
     doTest(CALL, "CALL N JOHNS HWY & E () ST",
           "CALL:CALL",
           "ADDR:N JOHNS HWY");
     doTest(CALL, "MVA [MVA] PARK AVE EXTRA",
            "CALL:MVA [MVA]",
            "ADDR:PARK AVE");
     doTest(CALL, "MVA [MVA] PARK AVE AND BLACK ST EXTRA",
         "CALL:MVA [MVA]",
         "ADDR:PARK AVE AND BLACK ST");
   }
   
   @Test
   public void testAlphaRoutes() {
     doTest(CALL, "CALL 123 RT A EXTRA",
         "CALL:CALL",
         "ADDR:123 RT A");
     doTest(CALL, "CALL 123 RT AB EXTRA",
         "CALL:CALL",
         "ADDR:123 RT AB");
     doTest(CALL, "CALL 123 RT US EXTRA",
         "CALL:CALL",
         "ADDR:123 RT US EXTRA");
     doTest(CALL, "CALL RT A & US B EXTRA",
         "CALL:CALL",
         "ADDR:RT A & US B");
     doTest(CALL, "CALL RT C EXTRA",
         "CALL:CALL",
         "ADDR:RT C");
   }
   
   @Test
   public void testRoadPrefix() {
     doTest(CALL, "BAD STUFF OLD NEWPORT RD EXTRA",
         "CALL:BAD STUFF",
         "ADDR:OLD NEWPORT RD");
   }
 
   @Test
   public void testCrossOnly() {
     doTest(CALL, FLAG_ONLY_CITY, "Help me get to ken town tonight",
         "CALL:Help me get to",
         "CITY:ken town");
     
     doTest(CALL, FLAG_ONLY_CROSS, "ALARM N JONES ST & BIG TOWN AVE 101 SOMEWHERE IN KENSBURG",
         "CALL:ALARM",
         "X:N JONES ST & BIG TOWN AVE");
     
     doTest(CALL, FLAG_ONLY_CROSS, "ALARM BIG TOWN AVE 101 SOMEWHERE IN KENSBURG",
         "CALL:ALARM BIG",
         "X:TOWN AVE");
     
     doTest(CALL, FLAG_ONLY_CROSS | FLAG_ONLY_CITY, "ALARM N JONES ST & BIG TOWN AVE SOMEWHERE IN KENSBURG EXTRA",
         "CALL:ALARM",
         "X:N JONES ST & BIG TOWN AVE SOMEWHERE IN",
         "CITY:KENSBURG");
     
     doTest(CALL, FLAG_ONLY_CROSS | FLAG_ONLY_CITY, "ALARM BIG TOWN AVE SOMEWHERE IN KENSBURG EXTRA",
         "CALL:ALARM BIG",
         "X:TOWN AVE SOMEWHERE IN",
         "CITY:KENSBURG");
     
   }
   
   @Test
   public void testMultiWordStreets() {
     doTest(CALL, "STRUCTURE FIRE JEAN GOUL AVE SOME MORE STUFF",
            "CALL:STRUCTURE FIRE",
            "ADDR:JEAN GOUL AVE");
     doTest(CALL, "STRUCTURE FIRE PAUL G GETTY RD & LOST CREEK RD IGNORE EVERYTHING ELSE",
            "CALL:STRUCTURE FIRE",
            "ADDR:PAUL G GETTY RD & LOST CREEK RD");
   }
   
   @Test
   public void testNoDelim() {
     doTest(CALL, FLAG_START_FLD_NO_DELIM, "FIRE ALARM231 BLACK ST",
          "CALL:FIRE ALARM",
          "ADDR:231 BLACK ST");
 
     doTest(CALL, FLAG_START_FLD_NO_DELIM, "FIRE ALARM6 231 BLACK ST",
          "CALL:FIRE ALARM6",
          "ADDR:231 BLACK ST");
 
     doTest(CALL, FLAG_START_FLD_NO_DELIM, "FIRE ALARM BLACK ST",
          "CALL:FIRE ALARM",
          "ADDR:BLACK ST");
   }
   
   @Test
   public void testCallPrefixList() {
     doTest(CALL, FLAG_ANCHOR_END, "BLUE EYED GIRL W/GLASSES AND NOTHING ELSE",
            "CALL:BLUE EYED GIRL W/GLASSES",
            "ADDR:AND NOTHING ELSE");
     
     doTest(CALL, FLAG_ANCHOR_END, "BLUE EYED GIRL W/O GLASSES AND NOTHING ELSE",
         "CALL:BLUE EYED GIRL",
         "ADDR:W & O GLASSES AND NOTHING ELSE");
     
     doTest(CALL, FLAG_ANCHOR_END, "BLUE EYED SUNSHINE 500 N 30 ST",
         "CALL:BLUE EYED SUNSHINE",
         "ADDR:500 N 30 ST");
     
     doTest(CALL_PLACE, "EMS-UNCONSCIOUS 100 BLACK ST",
         "CALL:EMS-UNCONSCIOUS",
         "ADDR:100 BLACK ST");
     
     doTest(CALL_PLACE, "EMS-UNCONSCIOUS JOES BAR 100 BLACK ST",
         "CALL:EMS-UNCONSCIOUS",
         "PLACE:JOES BAR",
         "ADDR:100 BLACK ST");
   }
   
   @Test
   public void testOptStreetSfx() {
     doTest(SKIP, FLAG_OPT_STREET_SFX, "SUNSHINE CITY 300 BLACK CAT",
           "ADDR:300 BLACK");
     doTest(SKIP, FLAG_OPT_STREET_SFX, "SUNSHINE CITY 300 PAUL G GETTY CAT",
         "ADDR:300 PAUL G GETTY");
     doTest(SKIP, FLAG_OPT_STREET_SFX, "SUNSHINE CITY JEAN GOUL & PAUL G GETTY DUMB STUFF",
         "ADDR:JEAN GOUL & PAUL G GETTY");
     doTest(SKIP, FLAG_OPT_STREET_SFX, "25376 potomac elderly male fell hit head is conscious",
         "ADDR:25376 potomac");
     doTest(ADDR, FLAG_OPT_STREET_SFX, "w lake and n hagadorn   washdown   at scene of pda",
         "ADDR:w lake and n hagadorn");
   }
   
   @Override
   public void testBadMsg() {
   }
   
   private void doTest(StartType sType, String test, String ... result) {
     doTest(sType, 0, test, result);
   }
   
   private void doTest(StartType sType, int flags, String test, String ... result) {
     parser.setStartTypeFlags(sType, flags);
     doTest("", false, test, result);
   }
   
   private static class TestParser extends SmartAddressParser {
     
     private StartType startType = StartType.START_ADDR;
     private int flags;
     
     public TestParser(String[] cities, String city,  String state) {
       super(cities, city, state);
       setupCallList(
           "BLUE EYED GIRL W/GLASSES",
           "BLUE EYED GIRL",
           "EMS-UNCONSCIOUS"
       );
     }
     
     public void setStartTypeFlags(StartType startType, int flags) {
       this.startType = startType;
       this.flags = flags;
     }
 
     @Override
     public boolean parseMsg(String message, Data data) {
       parseAddress(startType, flags, message, data);
       data.strSource = getPadField();
       return true;
     }
   }
 }
