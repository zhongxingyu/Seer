 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAWashingtonCountyParserTest extends BaseParserTest {
   
   public PAWashingtonCountyParserTest() {
     setParser(new PAWashingtonCountyParser(), "WASHINGTON COUNTY", "PA");
   }
   
  @Test
   public void testParser1() {
 
     doTest("T1",
         "Location: 116 GRANT ST MCDO Xstreet: NORTH ST STATION ST TYPE: STRUCTURE FIRE TIME: 23:42:41 Comments: IN THE BACK POSS 112 OR 114",
         "ADDR:116 GRANT ST",
         "CITY:MCDONALD",
         "X:NORTH ST & STATION ST",
         "CALL:STRUCTURE FIRE");
 
     doTest("T2",
         "Location: 50 STONECREST LN CECI Xstreet: MCCONNELL RD DEAD END TYPE: FIRE TIME: 23:40:09 Comments:",
         "ADDR:50 STONECREST LN",
         "CITY:CECIL TWP",
         "X:MCCONNELL RD DEAD END",
         "CALL:FIRE");
 
     doTest("T3",
         "Location: 1647 MILLERS RUN RD MTPL: @KEEPSAKE ARABIANS Xstreet: PLEASANT RD FORT CHERRY RD TYPE: FIRE ALARM TIME: 22:31:54 Comments:",
         "ADDR:1647 MILLERS RUN RD",
         "CITY:MT PLEASANT TWP",
         "PLACE:KEEPSAKE ARABIANS",
         "X:PLEASANT RD & FORT CHERRY RD",
         "CALL:FIRE ALARM");
 
     doTest("T4",
         "Location: 22 2ND ST CECI Xstreet: SCHOOL ST BOYCE ST TYPE: STRUCTURE FIRE TIME: 22:40:10 Comments:",
         "ADDR:22 2ND ST",
         "CITY:CECIL TWP",
         "X:SCHOOL ST & BOYCE ST",
         "CALL:STRUCTURE FIRE");
 
     doTest("T5",
         "Location: 345 SOUTHPOINTE BLVD CECI: @LOCAL 23 UFCW Xstreet: SOUTHPOINTE BLVD NB SOUTHPOINTE BLVD NB TYPE: FIRE ALARM TIME: 11:39:27 Comments:",
         "ADDR:345 SOUTHPOINTE BLVD",
         "CITY:CECIL TWP",
         "PLACE:LOCAL 23 UFCW",
         "X:SOUTHPOINTE BLVD NB & SOUTHPOINTE BLVD NB",
         "CALL:FIRE ALARM");
 
     doTest("T6",
         "Xstreet: REISSING RD SR 50 TYPE: GRASS OR FIELD FIRE TIME: 15:07:34 Comments: ON REISSING NEAR SR 50",
         "ADDR:REISSING RD & SR 50",
         "MADDR:REISSING RD & ST 50",
         "CALL:GRASS OR FIELD FIRE");
 
     doTest("T7",
         "Xstreet: REISSING RD TAYLOR DR TYPE: VEHICLE ACCIDENT WITH UNKNOWN INJURIES TIME: 05:36:10 Comments:",
         "ADDR:REISSING RD & TAYLOR DR",
         "CALL:VEHICLE ACCIDENT WITH UNKNOWN INJURIES");
 
     doTest("T8",
         "Xstreet: SR 980 HIGHLAND DR TYPE: VEHICLE ACCIDENT WITH INJURIES TIME: 14:17:43 Comments: JUST PASSED HIGHLAND HEADED TOWARD CECIL",
         "ADDR:SR 980 & HIGHLAND DR",
         "MADDR:ST 980 & HIGHLAND DR",
         "CALL:VEHICLE ACCIDENT WITH INJURIES");
    
   }
   
  @Test
   public void testParser2() {
 
     doTest("T1",
         "(911) Location: 200 TANDEM VILLAGE RD NSTR: @PCH 48 CONSULATE BLDG 2 Xstreet: LINDEN CREEK RD DEAD END TYPE: FIRE ALAR",
         "ADDR:200 TANDEM VILLAGE RD",
         "CITY:NORTH STRABANE TWP",
         "PLACE:PCH 48 CONSULATE BLDG 2",
         "X:LINDEN CREEK RD DEAD END",
         "CALL:FIRE ALAR");
 
     doTest("T2",
         "(911) Location: 200 TANDEM VILLAGE RD NSTR: @PCH 48 CONSULATE BLDG 2 Xstreet: LINDEN CREEK RD DEAD END TYPE: FIRE ALAR",
         "ADDR:200 TANDEM VILLAGE RD",
         "CITY:NORTH STRABANE TWP",
         "PLACE:PCH 48 CONSULATE BLDG 2",
         "X:LINDEN CREEK RD DEAD END",
         "CALL:FIRE ALAR");
 
     doTest("T3",
         "(911) Location: 100 MEDICAL BLVD NSTR: @CANONSBURG HOSPITAL Xstreet: BOONE AVE HILL CHURCH HOUSTON RD TYPE: FIRE ALARM",
         "ADDR:100 MEDICAL BLVD",
         "CITY:NORTH STRABANE TWP",
         "PLACE:CANONSBURG HOSPITAL",
         "X:BOONE AVE & HILL CHURCH HOUSTON RD",
         "CALL:FIRE ALARM");
   
   }
   
  @Test
   public void testParser3() {
 
     doTest("T1",
         "Subject:911\nLocation: 213 EXPRESS WAY LONG: EST Xstreet: EXPRESS WAY EB DALLY RD TYPE: VEHICLE ACCIDENT WITH UNKNOWN INJURIES TIM",
         "ADDR:213 EXPRESS WAY",
         "CITY:LONG BRANCH",
         "PLACE:EST",
         "X:EXPRESS WAY & EB DALLY RD",
         "CALL:VEHICLE ACCIDENT WITH UNKNOWN INJURIES");
 
     doTest("T2",
         "Subject:911\nLocation: 6 RAILROAD ST ALLE Xstreet: FRANKLIN ST HAMILTON ST TYPE: MUTUAL AID REQUEST TIME: 11:34:09 Comments:",
         "ADDR:6 RAILROAD ST",
         "CITY:ALLENPORT",
         "X:FRANKLIN ST & HAMILTON ST",
         "CALL:MUTUAL AID REQUEST");
 
     doTest("T3",
         "Subject:911\nLocation: 213 EXPRESS WAY LONG: EST Xstreet: EXPRESS WAY EB DALLY RD TYPE: VEHICLE ACCIDENT WITH UNKNOWN INJURIES TIM",
         "ADDR:213 EXPRESS WAY",
         "CITY:LONG BRANCH",
         "PLACE:EST",
         "X:EXPRESS WAY & EB DALLY RD",
         "CALL:VEHICLE ACCIDENT WITH UNKNOWN INJURIES");
 
     doTest("T4",
         "Subject:911\nLocation: 3 HIGH RD ROSC: EST Xstreet: MOUNT TABOR RD LATTA HOLLOW RD TYPE: UNCONSCIOUS PT / IS BREATHING TIME: 03:21",
         "ADDR:3 HIGH RD",
         "CITY:ROSCOE",
         "PLACE:EST",
         "X:MOUNT TABOR RD & LATTA HOLLOW RD",
         "CALL:UNCONSCIOUS PT / IS BREATHING");
   }
   
  @Test
   public void testParser4() {
     
     doTest("T1",
         "1 of 2\nFRM:WashCo911 \nSUBJ:911\nMSG:Location: 791 LOCUST AVE EX SSTR: @PCH 44 HAWTHORNE WOODS ASSISTED LIVING Xstreet: BERRY RD VERONA DR TYPE:\n(Con't) 2 of 2\nUNCONSCIOUS PT / IS BREATHING TIME: 08:26:06 Comments: 1ST FLOOR HEALTH ROOM\n(End)",
         "ADDR:791 LOCUST AVE EX",
         "MADDR:791 LOCUST AVE",
         "CITY:SOUTH STRABANE TWP",
         "PLACE:PCH 44 HAWTHORNE WOODS ASSISTED LIVING",
         "X:BERRY RD & VERONA DR",
         "CALL:UNCONSCIOUS PT / IS BREATHING");
   }
   
  @Test
   public void testVictorFedrick() {
 
     doTest("T1",
         "Subject:911\n" +
         "Location: 2 WHEELING PITTSBURGH STEEL DR ALLE: @AMROX Xstreet: MILL RD J F KENNEDY ST TYPE: FIRE TIME: 15:07:31 Comments: at the wheeling pittsburgh plant",
 
         "ADDR:2 WHEELING PITTSBURGH STEEL DR",
         "CITY:ALLENPORT",
         "PLACE:AMROX",
         "X:MILL RD & J F KENNEDY ST",
         "CALL:FIRE");
 
     doTest("T2",
         "Subject:911\nLocation: 5 CENTER ST ALLE Xstreet: ANDERSON ST SR 88 TYPE: ODOR OF NATURAL GAS TIME: 21:39:50 Comments:",
         "ADDR:5 CENTER ST",
         "CITY:ALLENPORT",
         "X:ANDERSON ST & SR 88",
         "CALL:ODOR OF NATURAL GAS");
 
     doTest("T3",
         "Subject:911\n" +
         "Location: 1850 MAIN ST ALLE: @FD 40 ALLENPORT Xstreet: MALLARD ST WASHINGTON ST TYPE: VEHICLE ACCIDENT NO INJURIES TIME: 16:33:09 Comments:",
 
         "ADDR:1850 MAIN ST",
         "CITY:ALLENPORT",
         "PLACE:FD 40 ALLENPORT",
         "X:MALLARD ST & WASHINGTON ST",
         "CALL:VEHICLE ACCIDENT NO INJURIES");
 
     doTest("T4",
         "Subject:911\nLocation: 446 BOW ST STOC Xstreet: LOCUST ST WALNUT ST TYPE: STRUCTURE FIRE TIME: 08:56:53 Comments:",
         "ADDR:446 BOW ST",
         "CITY:STOCKDALE",
         "X:LOCUST ST & WALNUT ST",
         "CALL:STRUCTURE FIRE");
 
     doTest("T5",
         "Subject:911\nLocation: 1859 MAIN ST ALLE Xstreet: WASHINGTON ST BRIDGE ST TYPE: STRUCTURE FIRE TIME: 15:19:45 Comments:",
         "ADDR:1859 MAIN ST",
         "CITY:ALLENPORT",
         "X:WASHINGTON ST & BRIDGE ST",
         "CALL:STRUCTURE FIRE");
 
     doTest("T6",
         "Subject:911\n" +
         "Location: 214 DALLY RD LONG Xstreet: ROCK DR CREST DR TYPE: FIRE ALARM TIME: 20:32:03 Comments: WAGNER/MACK RES - 412-716-7984",
 
         "ADDR:214 DALLY RD",
         "CITY:LONG BRANCH",
         "X:ROCK DR & CREST DR",
         "CALL:FIRE ALARM");
 
   }
   
   public static void main(String[] args) {
     new PAWashingtonCountyParserTest().generateTests("T1");
   }
 }
