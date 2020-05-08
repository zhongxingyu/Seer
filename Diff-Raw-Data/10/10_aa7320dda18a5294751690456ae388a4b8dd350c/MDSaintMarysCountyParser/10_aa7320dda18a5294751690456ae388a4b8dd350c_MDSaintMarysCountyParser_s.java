 package net.anei.cadpage.parsers.MD;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Saint Marys County, MD
 Contact: "Jason Adams" <jadams@bdvfd.org> or jadams@bdvfd.org or 2405384664@vzwpix.com
 Contact: Brian Brown <brian.brown86@gmail.com>
 Contact: patrick woodburn <patlvfd@gmail.com>
 Contact: Latoya Beaumont <beaumontlt.lb@gmail.com>
 Contact: "scherry3626@aol.com" <scherry3626@aol.com>
 Contact: Patrick Stanley <work3750@gmail.com>
 Contact: john douglass <jdouglassx@gmail.com>
 Contact: Crystal Jackson <crazyredheadmd@gmail.com>
 Contact: Doug Walmsley <dcwalmsley@md.metrocast.net>
 Contact: tim vallandingham <blayre0190@gmail.com>
 Contact: Richard Werring <rbwerring@gmail.com>
 Contact: Patrick Stanley <work3750@gmail.com>
 Contact: dan kenney <dan@motovationcycles.com>
 Sender: mplus@co.saint-marys.md.us,mplus@STMARYSMD.COM
 System: Pro QA Medical & Pro QA Fire
 
 21:10:05*Personal Injury Accident*22607 THREE NOTCH RD INTERSECTN*MACARTHUR BL*LEXINGTON PARK*CO9 CO3 SQ3 CO39 CO79 A796*N/B LANES TWO VEHICLES AT LEAST ONE INJURY*
 14:41:22*Personal Injury Accident*21050 WILLOWS RD INTERSECTN*ABBERLY CREST LN*LEXINGTON PARK*CO3 CO39 CO6R*
 15:24:31*Working House Fire*20242 POINT LOOKOUT RD*OLD GREAT MILLS RD*INDIAN BRIDGE RD*GREAT MILLS*CO3 CO9 CO6 CO1 CO5 CO39 CO6R*
 21:11:30*Commercial Building Fire*46528 VALLEY CT APT3019*SPRING VALLEY DR*DEAD END*LEXINGTON PARK*CO3 CO13 CO9 TK3 CO7 TK7 CO39*Using ProQA Fire*
 23:14:28*Heart Problems*22521 IVERSON DR UNIT3*AMBER DR*CUL DE SAC*CALIFORNIA*CO9*55YOF C/A/B; RAPID HEART RATE AND WEAK; HX DIABETES;*
 ((37593) CAD ) 22:12:45*CO Detector With Symptons*21353 FOXGLOVE CT*DEAD END*BAYWOODS RD*HERMANVILLE*CO3 CO39*Using ProQA Fire*
 ((44333) CAD ) 00:35:39*CHIMNEY FIRE*25120 DOVE POINT LN*PARSONS MILL RD*DEAD END*LOVEVILLE*CO1 TK1 CO7*Using ProQA Fire*
 ((60350) CAD ) 18:34:06*Breathing Difficulties*22030 OXFORD CT APT*GLOUCESTER CT*DEAD END*LEXINGTON PARK*ALS CO39 CO6R*66YOF HIGH BLOOD
 ((46589) CAD) 14:02:26*Stroke**APT A2**22027 OXFORD CT APTA2*GLOUCESTER CT*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical*
 ((46677) CAD) 13:59:56*Chest Pain*40452 MEDLEYS LN*LAUREL GROVE RD*LOVEVILLE RD*OAKVILLE*CO79 ALS*subject has pacemaker*
 ((46589) CAD) 13:29:15*Breathing Difficulties**ST MARYS NURSING CENTER**21585 PEABODY ST RM441A*HOLLYWOOD RD*DEAD END*LEONARDTOWN*CO19 CO79 ALS*hx copd*
 ((46589) CAD) 12:47:33*Chest Pain*17498 GRAYSON RD*BEACHVILLE RD*VILLA RD*ST INIGOES*CO49R ALS*Using ProQA Medical*
 ((46589) CAD) 12:14:40*Sick Person**NEWTOWNE VILLAGE APTS**22810 DORSEY ST APT309*CONNELY CT*DEAD END*LEONARDTOWN*CO19 CO79 A799*Using ProQA Medical*
 ((46589) CAD) 12:08:40*Sick Person*46104 SALTMARSH CT*WEST WESTBURY BL*DEAD END*LEXINGTON PARK*CO39 A397*Using ProQA Medical*
 ((47017) CAD ) 21:24:45*Seizures/Convulsions*THREE OAK CENTER*46905 LEI DR*THREE NOTCH RD*SOUTH CORAL DR*LEXINGTON PARK*ALS CO39*Using ProQA
 ( CAD ) 18:49:29*Sick Person*BRETON MEDICAL GROUP BLDG #3*22576 MACARTHUR BL SUITE 354*HALSEY CT*THREE NOTCH RD*SAN SOUCI*CO39 A387*Using ProQA Me(14072) CAD ) 08:27:10*Sick Person*BANK OF AMERICA*21800 NORTH SHANGRI LA DR*THREE NOTCH RD*GREAT MILLS RD*LEXINGTON PARK*CO39 A397*Using ProQA
 ((49169) CAD ) 15:07:57*Sick Person*46656 FLOWER OF THE FOREST RD*WILLOWS RD*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical* Eff Body:15:07:57*Sick Person*46656 FLOWER OF THE FOREST RD*WILLOWS RD*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical*
 ((49639) CAD ) 17:18:40*Mutual Aid*14386 SOUTH SOLOMONS ISLAND RD*CO9 CO7 CO3*
 ((23645) CAD ) 07:33:05*Mutual Aid EMS*401 EP WORTH CT*CO9*302; unresponsive*
 ((19239) CAD ) 20:51:10*Chest Pain*19673 NORTH SNOW HILL MANOR RD*SOUTH SNOW HILL MANOR RD*LYARD RD*ST MARYS CITY*CO39 A398 ALS*PT. DOES HAVE A PACEMAKER
 ((20390) CAD ) 09:06:30*Sick Person*CAMPUS CENTER*47600 MILL FIELD DR*POINT LOOKOUT RD*DEAD END*ST MARYS CITY*CO39*2ND FLOOR;*
 ((64277) CAD ) 09:48:02*Commercial Fire Alarm*ESPERANZA MIDDLE SCHOOL*22790 MAPLE RD*THREE NOTCH RD*TOWN CREEK*CO9 CO3 TWR9*gen alarm*
 ((57079) CAD ) 16:18:26*Breathing Difficulties*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39 A397 ALS M3*RM102B-FRONT
 ((61165) CAD ) 07:09:07*Sick Person*46369 HATTONS REST LN*DEAD END*POINT LOOKOUT RD*PARK HALL*CO39*Using ProQA Medical*
 ((60056) CAD ) 18:14:19*Traumatic Injuries*21121 ACE LN*WINDING WY*SHORT WY*LORD CALVERT TRLPK*CO39*Using ProQA Medical*
 ((59734) CAD ) 16:59:32*Eye Problems/Injuries*LA QUINTA INN*22769 THREE NOTCH RD*CHANCELLORS RUN RD*GUNSTON DR*CALIFORNIA*CO39*at the front desk*
 ((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b
 ((3871) CAD ) 15:18:55*Seizures/Convulsions*PARK HALL ELEMENTARY*20343 HERMANVILLE RD*PIPER CT*DIXON CT*PARK HALL*A397 CO39 ALS M3*COME IN OFF OF ROUTE5*
 ((3306) CAD ) 13:33:38*Allergic Reaction/Bee Stings*GREAT MILLS HIGH SCHOOL*21130 GREAT MILLS RD*CYPRESS WY*TRI COMMUNITY WY*GREAT MILLS*ALS CO39*Using P
 ((3130) CAD ) 12:57:41*Falls/Traumatic*ESPERANZA MIDDLE SCHOOL*22790 MAPLE RD*THREE NOTCH RD*ELM CT*TOWN CREEK*A389 CO39*USE THE MAIN ENTRANCE*
 ((2455) CAD ) 10:10:53*Falls/Traumatic*BRETON MEDICAL GROUP BLDG #3*22576 MACARTHUR BL SUITE 354*HALSEY CT*THREE NOTCH RD*SAN SOUCI*CO39 A389*56yof C/A/B
 ((1741) CAD ) 05:36:56*Falls/Traumatic*CALLAWAY SHELL*20943 POINT LOOKOUT RD APT3*PINEY POINT RD*HUNTING QUARTER DR*CALLAWAY*CO39*44YOM C/A/B; CUT TO N
 ((64130) CAD ) 18:24:21*Sick Person*SOUTH RIDGE*13425 POINT LOOKOUT RD APT2*CURLEYS RD*EVERGREEN ESTATES LN*RIDGE*CO39*29 yom side pains*
 ((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b
 ((12416) CAD ) 18:36:28*Chest Pain*45848 KRYSTAL LN*TRI COMMUNITY WY*BRIDGETT LN*LORD CALVERT TRLPK*ALS CO39*Using ProQA Medical*
 ((62650) CAD ) 17:06:15*Personal Injury Accident*GOV THOMAS JOHNSON BRIDGE*46100 PATUXENT BEACH RD*NORTH PATUXENT BEACH RD*DEAD
 ((557) CAD ) 23:33:54*Breathing Difficulties*CEDAR LANE APARTMENTS*22680 CEDAR LANE CT APT2204*POINT LOOKOUT RD*CEDAR LANE RD*LEONARDTOWN*ALS CO19*74YOM
 ((25655) CAD ) 19:03:50*Mutual Aid*CALVERT CO*CO9 CO7 CO3*Box 3-03*
 ((25495) CAD ) 19:03:50*Mutual Aid*CALVERT CO*CO9 CO7 CO3*Box 3-03*
 ((25420) CAD ) 18:44:03*Vehicle Fire*NICOLET PARK*21777 BUNKER HILL DR*DEAD END*LEXINGTON PARK*CO3*Using ProQA Fire*
 ((20896) CAD ) 14:43:34*OUTSIDE FIRE*21139 THREE NOTCH RD INTERSECTN*HERMANVILLE RD*HERMANVILLE*CO3*APPROX 1 MILE IN FROM 235*
 ((24505) CAD ) 12:13:18*1050 PD Structure*21481 SYDNEY DR*SELL DR*LEXINGTON PARK*CO3 SQ3*VEHICLE INTO A HOUSE*
 ((33038) CAD ) 01:57:14*Hazardous Condition*APT 311 FOXCHASE*45910 FOXCHASE DR APT311*LEXINGTON DR*GREAT MILLS*CO9*Using ProQA Fire*
 ((33120) CAD ) 03:41:27*CO Detector No Symptons*45713 SUMMER LN*DEAD END*LORD CALVERT TRLPK*CO3*Using ProQA Fire*
 ((38992) CAD ) 16:54:15*Tree Down*46449 SUE DR*PRATHER DR*LEXINGTON PARK*CO3*TREE IN THE POWER LINES/LINES ARE SPARKING/LEANING ON HOUSE*
 ((10171) CAD ) 14:06:23*OUTSIDE FIRE*44761 KING WY*SURREY WY*WILDEWOOD*CO9*Using ProQA Fire*
 ((10596) CAD ) 05:36:57*Psychiatric/Suicide Attempt*BURCH MART CHARLOTTE HALL*30295 THREE NOTCH RD*MT WOLF RD*CHAR HALL*CO29*CALLER IS INSIDE THE SHELL S
 ((16151) CAD ) 17:53:28*Heart Problems*CHARLOTTE HALL VETERANS HOME*29449 CHARLOTTE HALL RD*CHARLOTTE HALL SCHOOL RD*CHAR HALL*ALS CO29*Wing 3B 308B*
 ((15958) CAD ) 16:50:03*Sick Person*26875 THREE NOTCH RD*LEELAND RD*LAUREL GROVE*ALS CO29*house on the left*
 ((15830) CAD ) 15:13:56*Falls/Traumatic*CHARLOTTE HALL TRANSFER STAT*37766 NEW MARKET TURNER RD*ELIZA WY*NEW MARKET*CO29*50 year old, Male, Conscious, Br
 ((18992) CAD ) 07:21:50*Personal Injury Accident*ADF BINGO*29062 THREE NOTCH RD*NEW MARKET VILLAGE RD*NEW MARKET*CO2 SQ2 CO29 EMS42*
 ((26213) CAD ) 22:12:56*Sick Person*39990 MRS GRAVES RD*GEORGE G PL*ORAVILLE*CO29*43 year old, Male, Conscious, Breathing.*
 ((39425) CAD ) 07:18:31*Sick Person*CHARLOTTE HALL VETERANS HOME*29449 CHARLOTTE HALL RD*CHARLOTTE HALL SCHOOL RD*CHAR HALL*ALS CO29*Room 149B 1D*
 ((10199) CAD ) 18:20:24*Breathing Difficulties*APT 856 LOCUST RIDGE*44792 LOCUST RIDGE CT*SURREY WY*WILDEWOOD*CO79 A797 ALS CO9*59 year old, Male, Consci
 ((10056) CAD ) 17:21:51*House Fire*20915 DEER WOOD PARK DR*REDMOND RD*CHESTNUT HILLS*CO9*e132 -4*
 ((10896) CAD ) 20:50:43*Personal Injury Accident*26488 YOWAISKI MILL RD*WEST SPICER DR*COUNTRY LAKES*CO2 SQ2 CO29 CO59*PARKED CAR*
 ((17525) CAD ) 15:15:46*Miscellaneous*ECC*23090 LEONARD HALL DR*HOLLYWOOD RD*LEONARDTOWN*CO19 CO29 CO39 CO49R CO59 CO6R*Severe thunderstorm warning till
 ((14501) CAD ) 14:57:30*Traumatic Injuries*26174 T WOOD DR*DEAD END*MECHANICSVILLE*CO29 CO59 ALS*FEMALE BELIEVES SHE INJURED HER RIB ON THURSDAY AT PHYSI
 ((22502) CAD ) 05:56:37*Breathing Difficulties*18360 THREE NOTCH RD*TOMS WY*ST JAMES*CO39*80 year old, Male, Conscious, Breathing.*
 ((34827) CAD ) 07:20:02*Falls/Traumatic*22518 ARMSWORTHY CT*CUL DE SAC*SAN SOUCI*ST38*84 year old, Female, Conscious, Breathing.*
 ((14717) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in
 ((14833) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in
 ((15045) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in
 ((55034) CAD ) 01:13:28*Personal Injury Accident*27161 YOWAISKI MILL RD*TANYARD DR*COUNTRY LAKES*CO2 SQ2 CO29 EMS42 ALS*single vehicle overturned; across
 ((10967) CAD ) 06:28:12*Sick Person*APT 843 LOCUST RIDGE*44790 LOCUST RIDGE CT UNIT843*SURREY WY*WILDEWOOD*ST38*86 year old, Female, Conscious, Breathing
 ((30978) CAD ) 02:32:58*Personal Injury Accident*38805 FOLEY MATTINGLY RD*DEAD END*HELEN*CO2 SQ2 CO29 A299 CO19 A196*17 YOF C/A/B CAR BACKED INTO HER POS
 ((41568) CAD ) 11:45:22*Breathing Difficulties*22085 ST LEONARDS CIR*ST GEORGES DR*GREAT MILLS*ST38 ST39 CO79 ALS*VOMMITING BLOOD*
 ((62956) CAD ) 21:27:30*Unconscious Person/Fainting*45345 BAREFOOT DR*GARRISON DR*BAREFOOT ACRES*ST38 A387 ALS*73 year old, Male, Unconscious, Breathing.
 ((28689) CAD ) 23:33:56*Unconscious Person/Fainting*2532 GUM WY*GREENTREE SOUTH DR*GLEN FOREST NAWC*ST39 A389 ALS*
 ((43652) CAD ) 12:29:09*Breathing Difficulties*23263 BY THE MILL RD*ALS ST38*60 year old, Female, Conscious, Breathing.*
 ((50741) CAD ) 14:51:00*Seizures/Convulsions*20601 WHITE POINT RD*KNOTTS LANDING WY*MEDLEYS NECK*ST39 CO79*semi concious had seizure in front yard*
 ((54436) CAD ) 07:32:15*Mutual Aid EMS*12455 BLACKSMITH DR INTERSECTN*CALVERT COUNTY*ST38 A389*TROUBLE BREATHING*
 ((52100) CAD ) 12:20:08*Mutual Aid EMS*11740 ASBUSY CIRCLE INTERSECTN*CALVERT COUNTY*ST38*APT1203-TROUBLE BREATHING*
 ((49925) CAD ) 17:16:22*Mutual Aid EMS*13650 KERSEY WY*ST38*302-CVA*
 
Unknown
 Contact: 2402989668@vtext.com
 (27758 CAD ) 20:38:46*ODOR UNKNOWN*49154 WYNNE RD*WOODLAWN LN*RIDGE*CO4 CO14*Odor (Strange / Unknown).  Odor inside.Caller Statement:*
 
  */
 
 
 public class MDSaintMarysCountyParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("\\b\\d\\d:\\d\\d:\\d\\d\\*");
   private static final Pattern PLACE = Pattern.compile("\\*\\*([^*]+)\\*\\*");
   
   public MDSaintMarysCountyParser() {
     super("SAINT MARYS COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
     return "mplus@co.saint-marys.md.us,mplus@STMARYSMD.COM,777166";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
    body = body.substring(match.start());
     
     // Special case, field delimited by double starts is a place name
     // that should be removed from the message string
     match = PLACE.matcher(body);
     if (match.find()) {
       data.strPlace = body.substring(match.start(1), match.end(1));
       body = body.substring(0, match.start()+1) + body.substring(match.end());
     }
     
     String[] flds = body.split("\\*+");
     if (flds.length < 4) return false;
     
     Result lastResult = null;
     String lastFld = null;
     boolean mutualAid = true;
     int ndx = 0;
     for (String fld : flds) {
       fld = fld.trim();
       
       switch (ndx++) {
       
       case 0:
         data.strTime = fld;
         break;
       
       case 1:
         // Call description
         data.strCall = fld;
         mutualAid = fld.startsWith("Mutual Aid");
         break;
         
       case 2:
         // Address line
         
         // If line ends with intersection, it is positively the
         // address field.  Any previously found field goes into the place
         // field, and we process the next intersecting address field.
         if (fld.endsWith(" INTERSECTN")) {
           if (lastFld != null) data.strPlace = lastFld;
           parseAddress(StartType.START_ADDR, fld.substring(0, fld.length()-11), data);
           data.strApt = append(data.strApt, "-", getLeft());
           break;
         }
         
         // If mutual aid call, this is the only address
         // don't bother looking for a place field
         if (mutualAid) {
           parseAddress(fld, data);
           break;
         }
         
         // Otherwise parse the address.  We always parse the first two
         // fields to see which one has the best address
         Result result = parseAddress(StartType.START_ADDR, fld);
         if (lastResult == null) {
           lastFld = fld;
           lastResult = result;
           ndx--;
           break;
         }
         
         // If this field looks better than the previous one
         // treat the prev field as a place and and parse this an address;
         if (lastResult.getStatus() < result.getStatus()) {
           data.strPlace = lastFld;
           result.getData(data);
           data.strApt = append(data.strApt, "-", getLeft());
           break;
         }
         
         // If the previous field looks like the better than this one
         // parse the previous address and drop through to treat this
         // one as the first cross street
         lastResult.getData(data);
         data.strApt = lastResult.getLeft();
         ndx++;
         
       case 3:
         // Cross streets * City
 
         // If identified city, process city field and progress to next UNIT field
         fld = fld.toUpperCase();
         if (CITY_LIST.contains(fld)) {
           data.strCity = fld;
           String newCity = CITY_CHANGES.getProperty(fld);
           if (newCity != null) {
             if (!newCity.equals("CHAR HALL") && data.strPlace.length() == 0) {
               data.strPlace = data.strCity;
             }
             data.strCity = newCity;
           }
           break;
         }
         
         // If identified unit field, drop through to next field
         if (isUnitField(fld)) {
           ndx++;
         }
         
         // Otherwise accumulate cross street and repeat this field
         else {
           data.strCross = append(data.strCross, " / ", fld);
           ndx--;
           break;
         }
         
       case 4:
         // Units
         data.strUnit = fld;
         break;
         
       case 5:
         // Description
         data.strSupp = append(data.strSupp, " / ", fld);
         ndx--;
         break;
       }
     }
     
     return true;
   }
   
   /*
    * Determine if field is city or unit field
    */
   private static final Pattern UNIT_PTN = Pattern.compile("[A-Z]+[0-9]+|ALS");
   private boolean isUnitField(String field) {
     for (String unit : field.split(" +")) {
       if (!UNIT_PTN.matcher(unit).matches()) return false;
     }
     return true;
   }
   
   private static Set<String> CITY_LIST = new HashSet<String>(Arrays.asList(new String[]{
       "CALIFORNIA",
       "CEDAR COVE",
       "CEDARCOVE",
       "CHAR HALL",
       "CHARLOTTE HALL",
       "CHESTNUT HILLS",
       "GOLDEN BEACH",
       "LEXINGTON PARK",
       "ABELL",
       "AVENUE",
       "BAREFOOT ACRES",
       "BEACHVILLE-ST INIGOES",
       "BUSHWOOD",
       "CALLAWAY",
       "CHAPTICO",
       "CLEMENTS",
       "COLTONS POINT",
       "COUNTRY LAKES",
       "COMPTON",
       "DAMERON",
       "DRAYDEN",
       "ESPERANZA FARMS",
       "FIRST COLONY",
       "GLEN FOREST NAWC",
       "GREAT MILLS",
       "HELEN",
       "HERMANVILLE",
       "HOLLYWOOD",
       "LAUREL GROVE",
       "LEONARDTOWN",
       "LEXINGTON PARK",
       "LORD CALVERT TRLPK",
       "LOVEVILLE",
       "MADDOX",
       "MECHANICSVILLE",
       "MEDLEYS NECK",
       "MORGANZA",
       "NEW MARKET",
       "OAKVILLE",
       "ORAVILLE",
       "PARK HALL",
       "PINEY POINT",
       "REDGATE",
       "RIDGE",
       "SAN SOUCI",
       "SCOTLAND",
       "SOUTH HAMPTON",
       "SPRING RIDGE",
       "ST INIGOES",
       "ST JAMES",
       "ST MARYS CITY",
       "TALL TIMBERS",
       "TOWN CREEK",
       "VALLEY LEE",
       "WILDEWOOD",
       
       "CALVERT COUNTY"
   }));
   
   private static final Properties CITY_CHANGES = buildCodeTable(new String[]{
       "CHAR HALL", "CHARLOTTE HALL",
       
       "BAREFOOT ACRES", "CALIFORNIA",
       "ESPERANZA FARMS","CALIFORNIA",
       "FIRST COLONY",   "CALIFORNIA",
       "SAN SOUCI",      "CALIFORNIA",
       "TOWN CREEK",     "CALIFORNIA",
 
       "CEDAR COVE",   "LEXINGTON PARK",
       "CEDARCOVE",    "LEXINGTON PARK",
       "GLEN FOREST NAWC", "LEXINGTON PARK",
       "LORD CALVERT TRLPK", "LEXINGTON PARK",
       "HERMANVILLE",  "LEXINGTON PARK",
       "SOUTH HAMPTON","LEXINGTON PARK",
       "SPRING RIDGE", "LEXINGTON PARK",
       "ST JAMES",     "LEXINGTON PARK",
       
       "MEDLEYS NECK", "LEONARDTOWN"
   });
 }
