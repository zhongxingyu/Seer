 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 [86 EMS] -- 31C2 -- 33384 Hartford Ct Lewes
 [86 EMS] -- 33A1 -- 36101 Seaside Blvd Rehoboth
 [86 EMS] -- 17A1 -- 18958 Coastal Hwy Lewes
 [86 Fire] -- F - Auto Fire Alarm -- Hallmark, Parcel Post, Safeway -- 19266 Coastal Hwy Rehoboth -- INC #602
 [86 Fire] -- F - Standby -- Bethany Beach Station 70 Rehoboth Beach -- INC #599
 [83 FIRE] -- Traffic/TransportationAcdntALS -- 228 W Dupont Hwy Millsboro 19966 -- Food Lion (83)
 [83 FIRE] -- Vehicle Fire -- 26026 Patriots Way Georgetown 19947 -- Sussex Central High School
 [83 FIRE] -- Carbon Monoxide Detector -- 28 Patterson Dr Georgetown 19947 
 [83 FIRE] -- Washdown (Fuel Spill) -- 26972 - 28044 Zoar Rd Georgetown 19947
 [83 FIRE] -- Traffic/TransportationAcdntBLS -- 0 Dupont Blvd & Av Of Honor Georgetown 19947
 [83 FIRE] -- Structure Fire -- 0 JOHNSON RD & JESTICE FARM RD Laurel 19956
 [83 FIRE] -- Cardiac/Resp./Death-ALS -- 28723 Woodcrest Dr Harbeson 19951
 
 [81 FIRE] -- Traffic/TransportaionAcdntBLS -- 0 Discount Land Rd & Sussex Hw
 [81 EMS] -- Chest Pain-ALS -- 34544 Doe Dr Laurel 19956
 [81 FIRE] -- Cardiac/Res./Death-ALS -- 815 S Central Ave Laurel 19956 -- Laurel Middle / intermediate n
 [81 EMS] -- Abdominal Pain-BLS -- 30594 SUssex Hwy Laurel 19956 -- Relax Inn
 [81 EMS] -- Unconcious/Fainting(Near)-ALS -- 31574 White Ave Laurel 19956
 [81 EMS] -- Falls-BLS -- 609 Little Creek Dr Laurel 19956
 
 Specific to Seaford
 [87 EMS] -- 1A Abdominal Pains (ALS) -- 6412 Woodduck Dr Seaford de, 19973
 [87 FIRE] -- Smoke Investigation -- 6341 Baker Rd Seaford de, 19973
 [87 EMS] -- 6A Breathing Problems (ALS) -- 24642 Shufelt Rd Seaford de, 19973
 [87 FIRE] -- Assist Other Agency -- Hickory Hill Rd/ Guard Rd Seaford de, 19973
 [87 EMS] -- 33A Interfacility (ALS) -- 715 E King St 204a Seaford de, 19973
 [87 EMS] -- 13A Diabetic Problems (ALS) -- 24101 Dove Rd Seaford de, 19973
 
  */
 
 
 public class DESussexCountyParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "DE";
   private static final String DEF_CITY = "SUSSEX COUNTY";
   
   private static final String[] CITY_LIST = new String[] {
     "BETHANY BEACH", "BETHEL", "BLADES", "BRIDGEVILLE", "DAGSBORO", "DELMAR",
     "DEWEY BEACH", "ELENDALE", "FENWICK ISLAND", "FRANKFORD", "GEORGETOWN",
     "GREENWOOD", "HENLOPEN ACRES", "LAUREL", "LEWES", "MILFORD", "MILLSBORO",
     "MILLVILLE", "MILTON", "OCEAN VIEW", "REHOBOTH BEACH", "SEAFORD", "SELBYVILLE",
     "SLAUGHTER BEACH", "SOUTH BETHANY"
   };
                                                        
   
   public DESussexCountyParser() {
     super(CITY_LIST, DEF_STATE);
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     if (!body.contains("] -- ")) return false;
 
     data.defState = DEF_STATE;
     data.defCity = DEF_CITY;
 
     // First divide up the call
     String strBody[] = body.split("--");
     if (strBody.length < 3) return false;
 
     // Always ignore the 0 field.
     // field 1 -s the call description
     data.strCall = strBody[1].trim();
 
     // See how many fields we have
     int fldCnt = strBody.length;
 
     // If more than 3 and the last fields starts with "INC ", ignore it
     if (fldCnt > 3 && strBody[fldCnt-1].trim().startsWith("INC ")) fldCnt--;
 
     // If we are down to 3 fields, the last one is the address line and
     // nothing needs to be worried about
     String sAddress;
     if (fldCnt == 3) {
       sAddress = strBody[2].trim();
     }
 
     // Otherwise we have big problems, one of lines 3 and 4 is the address
     // line and one is a place name.  But we don't know which is which.
     else {
       String[] flds = new String[]{strBody[2].trim(), strBody[3].trim()};
       int ndx = findBestAddress(flds);
       sAddress = flds[ndx];
       data.strPlace = flds[1-ndx];
     }
 
     // Sometimes the address has a leading zero for unknown reasons
     // which needs to be removed
     if (sAddress.length() < 2) return false;
     if (sAddress.startsWith("0 ")) sAddress = sAddress.substring(2).trim();
     
     // Run the smart parser on this
     parseAddress(StartType.START_ADDR, sAddress, data);
       
       // Hopefully it found a city, if not we will have to parse one out
       // of what is left
       if (data.strCity.length() == 0) {
         
         // If the last token in what is left is a 5 digit zip code, strip it off
         sAddress = getLeft();
         Parser p = new Parser(sAddress);
         String last = p.getLastOptional(' ');
         if (last.length() == 5 && NUMERIC.matcher(last).matches()) sAddress = p.get();
         
         // If last character is a comma, get rid of it
         
         // If the new last token is "DE" with or without a trailing comma, strip it off 
         p = new Parser(sAddress);
         last = p.getLastOptional(' ');
         if (last.endsWith(",")) last = last.substring(0, last.length()-1);
         if (last.equalsIgnoreCase("DE")) sAddress = p.get();
       
         // Whatever is left is our city
         data.strCity = sAddress;
       }
 
     return true;
   }
 
   /**
    * Examine list of possible address fields and return the index of the
    * most likely candidate
    * @param flds array of possible address fields
    * @return index of best candidate
    */
   private int findBestAddress(String[] flds) {
     
     // Phase 1, if it starts with a leading zero, it is probably an address
     for (int ndx = 0; ndx<flds.length; ndx++) {
       if (flds[ndx].startsWith("0 ")) return ndx;
     }
     
     // Phase 2, if it ends with a 5 digit zip code, it is probably an address
     for (int ndx = 0; ndx<flds.length; ndx++) {
       Parser p = new Parser(flds[ndx]);
       String last = p.getLastOptional(' ');
       if (last.length() == 5 && NUMERIC.matcher(last).matches()) return ndx;
     }
     
     // Phase 3, we have to ask the address parser for its opinion
     int bestNdx = 0;
     int bestScore = -1;
     for (int ndx = 0; ndx<flds.length; ndx++) {
       int score = checkAddress(flds[ndx], 1);
       if (score > bestScore) {
         bestNdx = ndx;
         bestScore = score;
       }
     }
     
     return bestNdx;
   }
 }
 
 
