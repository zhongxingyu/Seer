 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /*
 Botetourt County, VA
 Contact: Zach Beckner <zach.beckner@gmail.com>
 Sender: SM@botetourtva.us
 System: Southern
 
 SM:167000 INTERSTATE 81 BUCHANAN 12001756 02:03:45 ACCIDENT 10-50 NB SIDE TRACTOR TRAILER WENT DOWN OVER BANK POSS FLIPPED\n\n\nThe information in this e-mail messag
 BCH:1078 BRUGHS MILL RD FINCASTLE 12001840 12:35:42 FLUE FIRE CALLING FROM HER IN LAWS. HOME NO IS 992-4197--SHE CHECKED IF ANYONE WAS HOME AND NO ONE ANSWERED.
 ARG:158300 INTERSTATE 81 FINCASTLE 12001904 16:01:02 ACCIDENT 10-50 NB / HIT THE GUARDRAIL / WAS ALL OVER THE ROAD BEFORE THAT FOR ABOUT 3 MILES / SAYS HE IS NOT
 JBC:TRUCKSTOPS-TRAVEL CENTERS OF AMERICA 2905 LEE HWY TROUTVILLE 12001955 23:03:26 CARDIAC 50 YO M/
 SM:1 ROANOKE RD/CATAWBA RD 12002013 20:11:16 CARDIAC WANT TO MEET AT PAPA JOHNS HAVING CHEST PAINS DIFF BREATHING 70 YOM HAS FLUID ON CHEST/WILL BE IN A\n\n\nThe inf
 
 */
 
 public class VABotetourtCountyParser extends DispatchSouthernParser {
   
   public VABotetourtCountyParser() {
     super(CITY_LIST, "BOTETOURT COUNTY", "VA", DSFLAG_DISPATCH_ID | DSFLAG_LEAD_PLACE);
   }
 
   @Override
   public String getFilter() {
     return "@botetourtva.us";
   }
   
   private static final String[] CITY_LIST = new String[]{
     "BLUE RIDGE",
     "BUCHANAN",
     "CLOVERDALE",
     "DALEVILLE",
     "EAGLE ROCK",
     "FINCASTLE",
     "HOLLINS",
     "ORISKANY",
     "SPRINGWOOD",
     "TROUTVILLE"
   }; 
 }
