 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class VABotetourtCountyParserTest extends BaseParserTest {
   
   public VABotetourtCountyParserTest() {
     setParser(new VABotetourtCountyParser(), "BOTETOURT COUNTY", "VA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "SM:167000 INTERSTATE 81 BUCHANAN 12001756 02:03:45 ACCIDENT 10-50 NB SIDE TRACTOR TRAILER WENT DOWN OVER BANK POSS FLIPPED\n\n\n" +
         "The information in this e-mail messag",
 
         "ADDR:167000 INTERSTATE 81",
         "CITY:BUCHANAN",
         "ID:12001756",
         "TIME:02:03:45",
         "INFO:ACCIDENT 10-50 NB SIDE TRACTOR TRAILER WENT DOWN OVER BANK POSS FLIPPED\n\n\nThe information in this e-mail messag");
 
     doTest("T2",
         "BCH:1078 BRUGHS MILL RD FINCASTLE 12001840 12:35:42 FLUE FIRE CALLING FROM HER IN LAWS. HOME NO IS 992-4197--SHE CHECKED IF ANYONE WAS HOME AND NO ONE ANSWERED.",
         "ADDR:1078 BRUGHS MILL RD",
         "CITY:FINCASTLE",
         "ID:12001840",
         "TIME:12:35:42",
         "INFO:FLUE FIRE CALLING FROM HER IN LAWS. HOME NO IS 992-4197--SHE CHECKED IF ANYONE WAS HOME AND NO ONE ANSWERED.");
 
     doTest("T3",
         "ARG:158300 INTERSTATE 81 FINCASTLE 12001904 16:01:02 ACCIDENT 10-50 NB / HIT THE GUARDRAIL / WAS ALL OVER THE ROAD BEFORE THAT FOR ABOUT 3 MILES / SAYS HE IS NOT",
         "ADDR:158300 INTERSTATE 81",
         "CITY:FINCASTLE",
         "ID:12001904",
         "TIME:16:01:02",
         "INFO:ACCIDENT 10-50 NB / HIT THE GUARDRAIL / WAS ALL OVER THE ROAD BEFORE THAT FOR ABOUT 3 MILES / SAYS HE IS NOT");
 
     doTest("T4",
         "JBC:TRUCKSTOPS-TRAVEL CENTERS OF AMERICA 2905 LEE HWY TROUTVILLE 12001955 23:03:26 CARDIAC 50 YO M/",
         "PLACE:TRUCKSTOPS-TRAVEL CENTERS OF AMERICA",
         "ADDR:2905 LEE HWY",
         "CITY:TROUTVILLE",
         "ID:12001955",
         "TIME:23:03:26",
         "CALL:CARDIAC 50 YO M",
         "INFO:/");
 
     doTest("T5",
         "SM:1 ROANOKE RD/CATAWBA RD 12002013 20:11:16 CARDIAC WANT TO MEET AT PAPA JOHNS HAVING CHEST PAINS DIFF BREATHING 70 YOM HAS FLUID ON CHEST/WILL BE IN A\n\n\n" +
         "The inf",
 
         "ADDR:1 ROANOKE RD",
         "X:CATAWBA RD",
         "ID:12002013",
         "TIME:20:11:16",
         "INFO:CARDIAC WANT TO MEET AT PAPA JOHNS HAVING CHEST PAINS DIFF BREATHING 70 YOM HAS FLUID ON CHEST/WILL BE IN A\n\n\nThe inf");
   }
   
   public static void main(String[] args) {
     new VABotetourtCountyParserTest().generateTests("T1", "PLACE ADDR X CITY ID TIME CALL INFO");
   }
 }
