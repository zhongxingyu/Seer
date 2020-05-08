 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PAErieCountyAParserTest;
 
 import org.junit.Test;
 
 
 public class PAErieCountyAParserTest extends BaseParserTest {
   
   public PAErieCountyAParserTest() {
     setParser(new PAErieCountyAParser(), "ERIE COUNTY", "PA");
   }
 
  
   
   @Test
   public void testParser() {
     doTest("T1",
         "ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 8165 PLATZ RD XS: MARKET RD FAIRVIEW TWP LIST JOHN C Map:2034 Grids:, Cad: 2011-0000044804",
         "SRC:ERIE911",
         "CALL:STRUC FIRE-SINGLE RESIDENTIAL",
         "ADDR:8165 PLATZ RD",
         "X:MARKET RD",
         "NAME:LIST JOHN C",
         "CITY:FAIRVIEW TWP",
         "MAP:2034",
         "ID:2011-0000044804");
     
     doTest("T2",
         "ERIE911:55B2P >ELEC HAZ/PWR REPT DISCONNECTED 7656 MAPLE DR XS: CHESTNUT ST FAIRVIEW TWP MUSANTE, JANET Map:2202 Grids:, Cad: 2011-0000045114",
         "SRC:ERIE911",
         "CALL:ELEC HAZ/PWR REPT DISCONNECTED",
         "ADDR:7656 MAPLE DR",
         "X:CHESTNUT ST",
         "NAME:MUSANTE, JANET",
         "CITY:FAIRVIEW TWP",        
         "MAP:2202",
         "ID:2011-0000045114");
 
     doTest("T3",
         "ERIE911:29B4 >MVA - UNKNOWN STATUS 17 I 90 EB XS: I 90 EB RAMP EXIT 16 FAIRVIEW TWP LORD, ISAAC Map:1888 Grids:, Cad: 2011-0000043981",
         "SRC:ERIE911",
         "CALL:MVA - UNKNOWN STATUS",
         "ADDR:17 I 90 EB",
         "X:I 90 EB RAMP EXIT 16",
         "NAME:LORD, ISAAC",
         "CITY:FAIRVIEW TWP",
         "MAP:1888",
         "ID:2011-0000043981");
         
     doTest("T4",
         "ERIE911:29B4 >MVA - UNKNOWN STATUS W LAKE RD&WHITEHALL PL XS: LORD RD FAIRVIEW TWP WOOD, RODNEY Cad: 2011-0000042496",
         "SRC:ERIE911",
         "CALL:MVA - UNKNOWN STATUS",
         "ADDR:W LAKE RD & WHITEHALL PL",
         "X:LORD RD",
         "CITY:FAIRVIEW TWP",
         "NAME:WOOD, RODNEY",
         "ID:2011-0000042496");
         
     doTest("T5",
         "ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 6683 OTTEN CT FAIRVIEW TESTI JULIE Cad: 2011-00000399770",
         "SRC:ERIE911",
         "CALL:STRUC FIRE-SINGLE RESIDENTIAL",
         "ADDR:6683 OTTEN CT",
         "CITY:FAIRVIEW",
         "NAME:TESTI JULIE",
         "ID:2011-00000399770");
         
     doTest("T6",
         "ERIE911:52C3G >FIRE/GENERAL ALARM-COMM STRUC 7301 KLIER DR XS: UNFAIRVIEW FAIRVIEW TWP DAN Map:2302 Grids:, Cad: 2011-0000040143",
         "SRC:ERIE911",
         "CALL:FIRE/GENERAL ALARM-COMM STRUC",
         "ADDR:7301 KLIER DR",
         "X:UNFAIRVIEW",
         "NAME:DAN",
         "MAP:2302",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000040143");
     
     doTest("T7",
         "ERIE911:10D4 >CHEST PAIN 5757 W RIDGE RD XS: MILLFAIR RD FAIRVIEW TWP NICOLE Map:1988 Grids:, Cad: 2011-0000047247",
         "SRC:ERIE911",
         "CALL:CHEST PAIN",
         "ADDR:5757 W RIDGE RD",
         "X:MILLFAIR RD",
         "NAME:NICOLE",
         "MAP:1988",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000047247");
     
     doTest("T8",
         "ERIE911:17D3 >FALLS 7648 WELCANA DR XS: LYNANN LN FAIRVIEW TWP SANDELL, CECELIA Map:2213 Grids:, Cad: 2011-0000047240",
         "SRC:ERIE911",
         "CALL:FALLS",
         "ADDR:7648 WELCANA DR",
         "X:LYNANN LN",
         "NAME:SANDELL, CECELIA",
         "MAP:2213",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000047240");
     
     doTest("T9",
         "ERIE911:26A1 >SICK PERSON 8300 W RIDGE RD XS: DOBLER RD FAIRVIEW TWP WIECZOREK, BOB Map:2185 Grids:, Cad: 2011-0000046184",
         "SRC:ERIE911",
         "CALL:SICK PERSON",
         "ADDR:8300 W RIDGE RD",
         "X:DOBLER RD",
         "NAME:WIECZOREK, BOB",
         "MAP:2185",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046184");
     
     doTest("T10",
         "ERIE911:13A1 >DIABETIC PROBLEMS 8475 MIDDLE RD XS: BLAIR RD FAIRVIEW TWP SEAN Map:2174 Grids:, Cad: 2011-0000046843",
         "SRC:ERIE911",
         "CALL:DIABETIC PROBLEMS",
         "ADDR:8475 MIDDLE RD",
         "X:BLAIR RD",
         "NAME:SEAN",
         "MAP:2174",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046843");
     
     doTest("T11",
         "ERIE911:52B1H >RES (SINGLE) HEAT DETECTOR 1530 TAYLOR RIDGE CT FAIRVIEW TWP ADT/DIONNA Map:2540 Grids:, Cad: 2011-0000046825",
         "SRC:ERIE911",
         "CALL:RES (SINGLE) HEAT DETECTOR",
         "ADDR:1530 TAYLOR RIDGE CT",
         "NAME:ADT / DIONNA",
         "MAP:2540",
         "CITY:FAIRVIEW TWP",
         "ID:2011-0000046825");
 
     doTest("T12",
         "ERIE911:6C1 >BREATHING PROBLEMS 817 POTOMAC AVE XS: W LAKE RD MILLCREEK TWP WATTS, BETTY Map:9214 Grids:, Cad: 2011-0000076275",
         "SRC:ERIE911",
         "CALL:BREATHING PROBLEMS",
         "ADDR:817 POTOMAC AVE",
         "X:W LAKE RD",
         "NAME:WATTS, BETTY",
         "MAP:9214",
         "CITY:MILLCREEK TWP",
         "ID:2011-0000076275");
 
     doTest("T13",
         "ERIE911:HASKINS RD IS NOW OPEN",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:HASKINS RD IS NOW OPEN");
 
     doTest("T14",
         "ERIE911:ACTIVE SHOOTER INCIDENTS - MANDATORY TRAINING - SEPT 6,7 OR 8TH. EIGHT HOUR COURSE. REQUIRED TO ATTEND ONE OF THE DAYS.",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:ACTIVE SHOOTER INCIDENTS - MANDATORY TRAINING - SEPT 6,7 OR 8TH. EIGHT HOUR COURSE. REQUIRED TO ATTEND ONE OF THE DAYS.");
 
     doTest("T15",
         "ERIE911:SAMPSON RD NOW OPEN......",
         "SRC:ERIE911",
         "CALL:GENERAL ALERT",
         "PLACE:SAMPSON RD NOW OPEN......");
 
     doTest("T16",
         "ERIE911:32B1 >UNKNOWN PROBLEM 10793 ETTER RD XS: LAKE PLEASANT RD GREENE TWP DUSILA,CANDY Map:277 Grids:, Cad: 2011-0000090035",
         "SRC:ERIE911",
         "CALL:UNKNOWN PROBLEM",
         "ADDR:10793 ETTER RD",
         "X:LAKE PLEASANT RD",
         "CITY:GREENE TWP",
         "NAME:DUSILA,CANDY",
         "MAP:277",
         "ID:2011-0000090035");
 
     doTest("T17",
         "ERIE911:29D2N2>MVA -EJECTION- HIGH MECHANISM PLUM RD VENANGO TWP ADAM Map:489 Grids:, Cad: 2011-0000096580",
         "SRC:ERIE911",
         "CALL:MVA -EJECTION- HIGH MECHANISM",
         "ADDR:PLUM RD",
         "CITY:VENANGO TWP",
         "NAME:ADAM",
         "MAP:489",
         "ID:2011-0000096580");
 
     doTest("T18",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:21B1 &gt;HEMORRHAGE / LACERATIONS 4242 ASBURY RD XS: THOROUGHBRED LOOP MILLCREEK TWP\n",
         "SRC:West Ridge Fire",
         "CALL:HEMORRHAGE/LACERATIONS",
         "ADDR:4242 ASBURY RD",
         "CITY:MILLCREEK TWP",
         "X:THOROUGHBRED LOOP");
 
     doTest("T19",
         "FRM:messaging@iamresponding.com\nSUBJ:West Ridge Fire\nMSG:9E1 &gt;CARDIAC/RESP ARREST / DEATH 4411 FOREST GLEN DR XS: W 38TH ST MILLCREEK TWP\n",
         "SRC:West Ridge Fire",
         "CALL:CARDIAC/RESP ARREST/DEATH",
         "ADDR:4411 FOREST GLEN DR",
         "CITY:MILLCREEK TWP",
         "X:W 38TH ST");
  
   }
   
   public static void main(String[] args) {
    new PAErieCountyAParserTest().generateTests("T19", "SRC CALL ADDR CITY X NAME MAP PLACE ID");
   }
 }
