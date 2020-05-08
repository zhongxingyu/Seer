 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser.StartType;
 
 import org.junit.Test;
 
 
 public class SmartAddressParserTest extends BaseParserTest {
   
   private static final StartType ADDR = StartType.START_ADDR;
   private static final StartType CALL = StartType.START_CALL;
   private static final StartType PLACE = StartType.START_PLACE;
   private static final StartType SKIP = StartType.START_SKIP;
   
   private static final String[] CITY_LIST = new String[]{"KENSBURG", "KEN TOWN"};
   private static final String DEF_STATE = "XX";
 
   private TestParser parser;
   
   public SmartAddressParserTest() {
     parser = new TestParser(CITY_LIST, DEF_STATE);
     setParser(parser, "", "");
   }
   
   @Test
   public void testProblems() {
     
     doTest(CALL, TestParser.FLAG_START_FLD_REQ, 
         "1073 SMOKE 1421 BEVERLY DR 5495253 NONA DRIVE",
         "CALL:1073 SMOKE",
         "ADDR:1421 BEVERLY DR");
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
         "ADDR:500 st123");
     doTest(SKIP, "TRY 123 JOHN SMITH ST N FOR SIZE",
         "ADDR:123 JOHN SMITH ST N");
     doTest(SKIP, "OR MAYBE 200 US30 N BACK",
         "ADDR:200 US30 N");
     doTest(SKIP, "NUMBER 25 MASKING ADDRESS AT 143 N JUNIOR ST DOWNTOWN",
         "ADDR:143 N JUNIOR ST");
     doTest(SKIP, "DRIVING ON 3224 XX 456",
         "ADDR:3224 XX 456");
     doTest(SKIP, "BUT USING 123 N THIRD DR N JUST ISNT RIGHT",
         "ADDR:123 N THIRD DR");
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
         "X:JACKSON ST");
   }
   
   @Test
   public void testIntersections() {
     doTest(ADDR, "BLACK ST & WEST HILLS RD AFTER HOURS",
         "ADDR:BLACK ST & WEST HILLS RD");
     doTest(SKIP, "CAN WE FIND HWY 20 AND WEST HILLS RD KENSBURG BLACK",
         "ADDR:HWY 20 & WEST HILLS RD",
         "CITY:KENSBURG");
     doTest(SKIP, "CAN WE FIND W HILLS RD AND HWY 20 KENSBURG",
         "ADDR:W HILLS RD & HWY 20",
         "CITY:KENSBURG");
     doTest(SKIP, "NOW SEE IF CAN WE FIND HWY 20 AND WEST HILLS RD WITHOUT A CITY",
         "ADDR:HWY 20 & WEST HILLS RD");
     doTest(SKIP, "NOW SEE IF CAN WE FIND W HILLS RD/HWY 20 WITHOUT A CITY",
         "ADDR:W HILLS RD & HWY 20");
     doTest(SKIP, "LOOKING FOR I-90 & US231 SOMEWHERE",
         "ADDR:I-90 & US231");
     doTest(SKIP, "WHAT ABOUT BLACK ST N & W SOMERSET RD ANYONE",
         "ADDR:BLACK ST N & W SOMERSET RD");
     doTest(SKIP, "SOMEWHERE NEAR US50 N & BLACK ST DOWN",
         "ADDR:US50 N & BLACK ST");
     doTest(SKIP, "THIS IS N JOHNSON AVE S & BLACK ST VERY BAD FORM",
         "ADDR:JOHNSON AVE S & BLACK ST");
     doTest(SKIP, "ACCIDENT W/INJURY SRT24&SAINT MARYS RD KEN TOWN UNDER",
         "ADDR:SRT24 & SAINT MARYS RD",
         "CITY:KEN TOWN");
   }
   
   @Test
   public void testNakedRoad() {
     doTest(ADDR, "BLAKSLY CREEK RD IN CHICAGO",
     		"ADDR:BLAKSLY CREEK RD");
     doTest(SKIP, "WHERE CAN I FIND BLAKSLY CREEK RD IN CHICAGO",
         "ADDR:CREEK RD");
     doTest(SKIP, "WHERE CAN I FIND N BLAKSLY CREEK RD IN KENSBURG",
         "ADDR:CREEK RD IN",
         "CITY:KENSBURG");
     doTest(ADDR, "US 26 IN CHICAGO",
         "ADDR:US 26");
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
         "ADDR:us123");
     doTest(SKIP, "MAYBE BLACK ST N WILL WORK",
         "ADDR:BLACK ST N");
     doTest(SKIP, "HOW ABOUT US-150 N DUDE",
         "ADDR:US-150 N");
     doTest(SKIP, "BAD BOY N BIGHILL RD S BOB",
         "ADDR:N BIGHILL RD");
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
   }
   
   @Test 
   public void testSlashDesc() {
     doTest(CALL, "MVA W/ROLLOVER 500 N SUMMER ST",
         "CALL:MVA W/ROLLOVER",
         "ADDR:500 N SUMMER ST");
   }
   
   @Test
   public void testRoadTokens() {
     doTest(SKIP, "AT US-30", "ADDR:US-30");
     doTest(SKIP, "AT us30", "ADDR:us30");
     doTest(SKIP, "AT ST30NB", "ADDR:ST30");
     doTest(SKIP, "AT I105WB", "ADDR:I105");
     
     doTest(SKIP, "AT ST30A", "ADDR:AT ST30A");
     doTest(SKIP, "AT ST30NBA", "ADDR:AT ST30NBA");
   }
   
   private void doTest(StartType sType, String test, String ... result) {
     doTest(sType, 0, test, result);
   }
   
   private void doTest(StartType sType, int flags, String test, String ... result) {
     parser.setStartTypeFlags(sType, flags);
     doTest("", test, result);
   }
   
   private static class TestParser extends SmartAddressParser {
     
     private StartType startType = StartType.START_ADDR;
     private int flags;
     
     public TestParser(String[] cities, String state) {
       super(cities, state);
     }
     
     public void setStartTypeFlags(StartType startType, int flags) {
       this.startType = startType;
       this.flags = flags;
     }
 
     @Override
     public boolean isPageMsg(String msgText) {
       return true;
     }
 
     @Override
     protected void parse(String message, Data data) {
       parseAddress(startType, flags, message, data);
     }
   }
 
 }
