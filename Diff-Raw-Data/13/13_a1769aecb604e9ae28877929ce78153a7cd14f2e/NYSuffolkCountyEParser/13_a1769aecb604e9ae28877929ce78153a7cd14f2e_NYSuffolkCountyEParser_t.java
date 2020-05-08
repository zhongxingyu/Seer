 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Suffolk County, NY (alternate)
 Contact: Brandon Tishim <tishimb@gmail.com>
 Sender: Bob@relaycom.com
 (FROM RELAY) 33 BAY RUN OFF OF HERRICKS LN JAMESPORT\nELDERLY MALE SICK
 (FROM RELAY) 415 OSTRANDER AVE, INJURY FROM A FALL
 (FROM RELAY) 95 OSTRANDER AVE, DIFFICULTY BREATHING
 
 Contact: Bill Wilkinson <billthemedic@gmail.com>
 Sender: Bob@relaycom.com
 (FROM RELAY) 236 MAIN RD AQUEBOGUE; DIRECTLY ACROSS FROM FEDUN REALITY 92 Y/O FEMALE \nCHEST PAINS
 (FROM RELAY) 2185 SPLISH SPLASH DRIVE FEMALE POSSIBLE STROKE
 (FROM RELAY) 178  PECONIC BAY BLVD IN AQUEBOGUE   AT THE  REEVE RESIDENCE SUBJECT \nSEIZURES
 (FROM RELAY) 1053 OLD COUNTRY RD PEARLE VISION CENTER  FEMALE HEAD INJURY FROM A FALL
 (FROM RELAY) 70 SCHULTZ RD MALE HEAD INJURY FROM A FALL ,, ACROSS FROM THE SPORTSMAN \nKENNEL IN MANORVILLE
 (FROM RELAY) 1272 EAST MAIN ST  DR. BROOK'S OFC  MALE  FAINTING EPISODES
 (MSG FROM RELAY) MICHAEL'S 1440 OLD COUNTRY RD. FEMALE. GENERAL WEAKNESS.
 
 Contact: Jim Lull <jlull65@gmail.com>
 Sender: Bob@relaycom.com
 /RELAY /201 TROT BROOK LANE FEMALE FACIAL INJURY\n
 /FROM RELAY /1035 PARKWAY\nFEMALE WITH CHEST PAINS\n
 /FROM RELAY /641 NORTHVILLE TPKE & FISHEL\nFEMALE IS SICK\n
 /FROM RELAY /MERCY HIGH SCHOOL\nYOUNG ADULT HEAD INJURY\nWEIGHT ROOM\n
 /FROM RELAY /ATLANTIS MARINE WORLD\nGIFT SHOP  FEMALE INJURIES FROM A FALL\n
 /FROM RELAY /819 E MAIN\nDEPT OF SOCIAL SVCS\nPOSSIBLE OVER DOSE\n
 /FROM RELAY /UNITED COMPREHENSIVE CARE\n170 OLD COUNTRY RD\nADULT MALE TROUBLE BREATHING\n
 /FROM RELAY /TANGER 2 STE 101 NY&CO\nFEMALE SUBJECT ILL\n
 /MSG TO RELAY /NORTHVILLE TURNPIKE & EAST AVE: INJURY NECK & BACK:MVA\n
 /FROM RELAY /641 DOCTOR'S PATH APT 7  FEMALE SICK\n
 /FROM RELAY /1145 MIDDLE RD  RIVERHEAD LANDINGS  APT 7A  ELDERLY FEMALE INJURY FROM A \nFALL\n
 /FRTOM RELAY /20 EAST MAIN ST   CULINARY ART SCHOOL   FEMALE  FOOD ALLERGY\n
 
RELAY TRAILER PARK RIVER RD 1ST TRAILER ON RIGHT HAND SIDE POSSIBLE HEART
FROM RELAY 1279 EAST MAIN EASTERN CARDIOLOGY MALE CHEST PAINS
FROM RELAY TANGER 2 STE 1401 EPIC JEANS 40ISH FEMALE PASSING OUT
FROM RELAY DEAD AIR PAGE
FROM RELAY 629 W MAIN ST BY GAS STATION
FROM RELAY 35 OPEN DRIVE ADULT FEMALE SICK
FROM RELAY 1106 WEST MAIN FEMALE DIFFICULTY BREATHING
FROM RELAY 1145 MIDDLE ROAD APARTMENT 7M ELDERLY FEMALE POSSIBLE HEART
FROM RELAY 1355 ROANOKE AVE APT 8 FEMALE ABDOMINAL

  */
 
 public class NYSuffolkCountyEParser extends SmartAddressParser {
 
  private static final Pattern MARKER = Pattern.compile("^(?:/[A-Z ]*RELAY */|(?:FROM )?RELAY )");
   private static final Pattern DELIM = Pattern.compile(" *\n *|  +| *(?<!XS)[,:]+ *");
   private static final Pattern OFF_OF = Pattern.compile("\\bOFF OF\\b");
   
   public NYSuffolkCountyEParser() {
     super(CITY_LIST, "SUFFOLK COUNTY","NY");
   }
 
   @Override
   public String getFilter() {
     return "Bob@relaycom.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     do {
       Matcher match = MARKER.matcher(body);
       if (match.find()) {
         body = body.substring(match.end()).trim();
         break;
       }
       
       if (subject.contains("FROM RELAY")) break;
       
       return false;
     } while (false);
     
     // Punctuation is too unreliable to count on
     body = body.replace(".", "");
     body = OFF_OF.matcher(body).replaceAll("XS:");
     
     String[] flds = DELIM.split(body);
     if (flds.length == 1) {
       
       // Feed everything to smart parser
       parseAddress(StartType.START_PLACE, FLAG_IGNORE_AT, body, data);
       data.strSupp = getLeft();
       
       // Clean up loose ends
       String sAddr = data.strAddress;
       if (sAddr.endsWith(" IN")) data.strAddress = sAddr.substring(0, sAddr.length()-3).trim();
       
       if (data.strPlace.length() > 0 && data.strAddress.length() == 0 && data.strSupp.length() == 0) {
         data.strSupp = data.strPlace;
         data.strPlace = "";
       }
     } 
     
     else {
       int pt = 0;
       Result res = parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END | FLAG_CHECK_STATUS, flds[0]);
       if (flds.length > 2) {
         Result res2 = parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END | FLAG_CHECK_STATUS, flds[1]);
         if (res2.getStatus() > res.getStatus()) {
           data.strPlace = flds[0];
           res = res2;
           pt = 1;
         } 
       }
       res.getData(data);
       
       for (pt++; pt < flds.length; pt++) {
         data.strSupp = append(data.strSupp, " - ", flds[pt]);
       }
     }
     
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
       "AQUEBOGUE",
       "JAMESPORT"
   };
 }
