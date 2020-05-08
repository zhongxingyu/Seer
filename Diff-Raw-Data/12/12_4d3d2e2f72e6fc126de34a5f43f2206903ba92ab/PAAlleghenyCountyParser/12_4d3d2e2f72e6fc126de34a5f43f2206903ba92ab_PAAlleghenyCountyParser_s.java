 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Allegheny County, PA
 Contact: Anthony Cuda <acuda@wpahs.org>,"Cuda, Anthony" <acuda@ccac.edu>
 Sender: 777*
 ALLEGHENY COUNTY 911 :FRCOM3, F1, POSS FIRE COM - SMELL OF SMOKE/BLDG ENDG, SHARPSBURG TOWERS #902, SHP, at 601 MAIN ST #902, SHP, btwn S MAIN ST and 7TH ST, NFD3, 265002, CALLER REPORTING A STRONG SMELL OF "SCHLAK" PAINT OR AMMONIA IN THE BUILDING. CALLER WAS ADVISED TO EVAC, Units:102EN1, 111EN1, 149EN1, 191EN1,
 ALLEGHENY COUNTY 911 :NGASRES, F1, NATURAL GAS SMELL/LEAK RES BLDG, 40 EASTON RD, FOX, btwn CHAPEL OAK RD and OLD MILL RD, ON THE STREET, IN FRONT OF, NFD3, 157001, Medical ProQA recommends dispatch at this time, Units:102EN1, 157EN1, 172EN1 - From 405 11/11/2010 12:17:38 TXT STOP to opt-out
 ALLEGHENY COUNTY 911 :NGASCOM, F1, NATURAL GAS SMELL/LEAK COM BLDG, FREEPORT MEDICAL ASSOC, ASP, at 241 FREEPORT RD, ASP, SUITE 7-FOX CHAPEL DERMETOLOGY, btwn CENTER AVE and EASTERN AVE, NFD3, 102001, STRONG SMELL OF NATURAL GAS IN THE BLDG, Units:102EN1, 111EN2, 157RQ1, 191TK1, 260RQ1, 265TK1 - From 405 11/10/2010 10:02:01 TXT STOP to opt-out
 ALLEGHENY COUNTY 911 :NGASRES, F1, NATURAL GAS SMELL/LEAK RES BLDG, 262 EMERSON RD, SHA, btwn JORDAN LN and HAWTHORNE RD, NFD3, 260001, NAT TXT STOP to opt-out
 ALLEGHENY COUNTY 911 :NGASOUT, F2, NATURAL GAS SMELL/LEAK OUTSIDE, 22 4TH ST, SHP, btwn SHORT CANAL ST and PILGRIM WAY, NFD3, 265001, STRON TXT STOP to opt-out
 ALLEGHENY COUNTY 911 :QRS0, Q0, E-0 QRS / EMS ASSIST, 43 OAKHURST CIR, ASP, btwn RIVER OAKS DR and RIVER OAKS DR, NFD3, 102001, 74YOF CHEST TXT STOP to opt-out
 ALLEGHENY COUNTY 911 :29B1, F1, TRAFFIC -WITH INJURIES, THOMPSON RUN RD/SUNNY HILL DR, ROS,  <310/ 0>, NFD3, 247006 191001, GARAGE ON FIRE, Units:102EN1, 149EN1, 191EN1, 191TK1, 240SQ1, 247TK1, 259EN1, 260TK1, 312RQ40 - From 405 11/13/2010 06:43:58 TXT STOP to opt-out
 
 Split messages, same send time
 ALLEGHENY COUNTY 911 :09E1A, E0, NOT BREATHING AT ALL -COLD/STIFF IN WARM, 9116 WALNUT ST, PLU, btwn APPLE AVE and PINE ST, EMD1, E48505, 68 YOM NOT BREATHING,
 THINKS HE IS DECEASED, Units:487 - From 504 02/08/2011 19:30:00 TXT STOP to opt-out
 
 :FRVEH, F2, VEHICLE FIRE, 3541 LAKETON RD, PEN, btwn LINDBERG AVE and NELBON AVE, EFD1, 225001
 
 Contact: Justin Hale <jhale715@yahoo.com>
 ALLEGHENY COUNTY 911 :QRS3, Q3, E-3 QRS / EMS ASSIST, 1000 ROSS PARK MALL DR, ROS, btwn ROSS PARK DR and CHERYL DR, CALIFORNIA PIZZA KITCHEN, NFD3, 24700
 
 Contact: Kevin Armstrong <kevinarmstrong72@gmail.com>
 Sender: 4127802418
 ALLEGHENY COUNTY 911 :27D3S, E0, STABBING - CENTRAL WOUNDS, 612 CENTER AVE, ASP, btwn E 6TH ST and W 8TH ST, NMD1, E14004, Medical ProQA recommends dispatch at this time, Units:142, 269 - From 406 05/07/2011 02:34:31 TXT STOP to opt-out
 
 Contact: David Gallagher <ffmedic114@gmail.com>
 ALLEGHENY COUNTY 911 :08B1G, E3, HAZMAT-SMELL OF GAS- ALRT/NO DIFF BREATH, 35 LOCUST ST #1STFL, ETN, btwn ELM WAY and WALNUT ST, NMD1, E16001, Medical ProQA recommends dispatch at this time, Units:171, E160ALL - From 404 05/09/2011 19:48:13 TXT STOP to opt-out
 
 Contact: Matthew Yeckel <yeckel01m@gmail.co
 ALLEGHENY COUNTY 911 :FRILL, F3, ILLEGAL FIRE, 5172 LOMBARDI DR, BWB, btwn SUNNY DR and END, SFD1, 104002, Units:104AST1, 104AST2 - From 701 02/23/2012 13:23:10
 
 Contact: support@active911.com
 (268 Station) 29D2P, F0, TRAFFIC-HIGH MECHANISM (ROLLOVER), BRIDGEVILLE EXIT, SFT, at 54 SB I-79 OFRP MILLERS RUN RD RMP, SFT, btwn END and MILLERS RUN RD, SFD3, 268003, CAR OVERTURNED, Units:268EN1, 268RQ1 - From 703 01/31/2012 19:18:34
 
 */
 
 public class PAAlleghenyCountyParser extends MsgParser {
   
   private static final String MARKER = "ALLEGHENY COUNTY 911 :";
   
   // Run ID consists of one or two 6+ digit numbers
  private static final Pattern UNIT_PTN = Pattern.compile("E?\\d{5,}(?: +E?\\d{5,})*");
   
   public PAAlleghenyCountyParser() {
     super("ALLEGHENY COUNTY", "PA");
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // There are a number of different message markers
     do {
       if (body.startsWith(MARKER)) {
         body = body.substring(MARKER.length()).trim();
         break;
       }
         
       if (body.startsWith(":")) {
         body = body.substring(1).trim();
         break;
       }
       
       if (subject.endsWith(" Station")) break;
         
       return false;
     } while (false);
     
     // Remove trailing stuff that we aren't interested in
     data.expectMore = true;
     int pt = body.indexOf(" - From");
     if (pt >= 0) {
       data.expectMore = false;
       body = body.substring(0, pt).trim();
     }
     pt = body.indexOf(" TXT STOP");
     if (pt >= 0) {
       data.expectMore = false;
       body = body.substring(0, pt).trim();
     }
     
     // Split body into comma separated fields
     String[] flds = body.split(" *, *");
     int fldCnt = flds.length;
     if (fldCnt < 6) return false;
     
     // Fields 0 & 2 make up the call, field 1 is call priority
     data.strCall = flds[0] + " - " + flds[2];
     data.strPriority = flds[1];
     
     // Field 3 is up for grabs
     // Field 4 should be a 3 character town code
     String cityCode = flds[4];
     if (cityCode.length() != 3) return false;
     data.strCity = convertCodes(cityCode, CITY_CODES);
     
     // If field 5 starts with "at" then it contains the address and
     // field 3 contains the place name.  Field 6 probably duplicates
     // the city code and should be skipped
     int ndx = 5;
     if (flds[ndx].startsWith("at ")) {
     		parseAddress(flds[ndx].substring(3).trim(), data);
     		data.strPlace = flds[3];
     		ndx++;
     		if (ndx < fldCnt && flds[ndx].equals(cityCode)) ndx++;
     } 
     
     // Otherwise field 3 contains the address
     else {
       parseAddress(flds[3], data);
     }
     
     // Start looking for a run ID.
     // Once we find it, the field in front of it is the source, anything before
     // that goes into supplemental info
     String source = null;
     while (ndx < fldCnt) {
       String fld = flds[ndx++];
       
       // Except for fields that start with btwn which become cross strees
       if (fld.startsWith("btwn ")) {
         data.strCross = fld.substring(5).trim();
         continue;
       }
      if (UNIT_PTN.matcher(fld).matches()) {
         if (source != null) data.strSource = append(data.strSource, " ", source);
         source = null;
        data.strUnit = append(data.strUnit, " ", fld);
         break;
       }
       if (source != null) {
         if (data.strSupp.length() > 0) data.strSupp += " / ";
         data.strSupp += source;
       }
       
       source = fld;
     }
     if (source != null) {
       if (data.strSupp.length() > 0) data.strSupp += " / ";
       data.strSupp += source;
     }
     
     // Now anything from here until we find a Unit: tag going into 
     // supp info
     if (ndx >= fldCnt) return true;
     while (!flds[ndx].startsWith("Units:")) {
       if (!flds[ndx].startsWith("Medical ProQA")) {
         data.strSupp = append(data.strSupp, " / ", flds[ndx]);
       }
       if (++ndx >= fldCnt) return true;
     }
     
     // Anything from the Unit tag on goes into units
     data.strUnit = append(data.strUnit, " ", flds[ndx].substring(6).trim());
     while (++ndx < fldCnt) {
       data.strUnit = append(data.strUnit, " ", flds[ndx]);
     }
     
     return true;
   }
   
   private static Properties CITY_CODES = buildCodeTable(new String[]{
       "ALE", "ALEPPO",
       "ASP", "ASPINWALL",
       "AVA", "AVALON",
       "BWB", "BALDWIN",
       "BWT", "BALDWIN TOWNSHIP",
       "BAS", "BELL ACRES",
       "BEL", "BELLEVUE",
       "BAV", "BEN AVON",
       "BAH", "BEN AVON HEIGHTS",
       "BPK", "BETHEL PARK",
       "BLA", "BLAWNOX",
       "BKR", "BRACKENRIDGE",
       "BRD", "BRADDOCK",
       "BRH", "BRADDOCK HILLS",
       "BWS", "BRADFORD WOODS",
       "BRE", "BRENTWOOD",
       "BRG", "BRIDGEVILLE",
       "CAR", "CARNEGIE",
       "CSH", "CASTLE SHANNON",
       "CHA", "CHALFANT",
       "CHE", "CHESWICK",
       "CHU", "CHURCHILL",
       "CLA", "CLAIRTON",
       "COL", "COLLIER",
       "COR", "CORAOPOLIS",
       "CRA", "CRAFTON",
       "CRE", "CRESCENT",
       "DOR", "DORMONT",
       "DRA", "DRAVOSBURG",
       "DUQ", "DUQUESNE",
       "EDR", "EAST DEER",
       "EMC", "EAST MCKEESPORT",
       "EPG", "EAST PITTSBURGH",
       "EWD", "EDGEWOOD",
       "EDG", "EDGEWORTH",
       "ELB", "ELIZABETH",
       "ELT", "ELIZABETH TWP",
       "EMS", "EMSWORTH",
       "ETN", "ETNA",
       "FWN", "FAWN",
       "FIN", "FINDLAY",
       "FOR", "FOREST HILLS ",
       "FWD", "FORWARD",
       "FOX", "FOX CHAPEL",
       "FPB", "FRANKLIN PARK",
       "FRZ", "FRAZER",
       "GLA", "GLASSPORT",
       "GLE", "GLENFIELD",
       "GTR", "GREEN TREE",
       "HAM", "HAMPTON",
       "HAR", "HARMAR",
       "HSN", "HARRISON",
       "HAY", "HAYESVILLE",
       "HEI", "HEIDELBERG",
       "HOM", "HOMESTEAD",
       "IND", "INDIANIA",
       "ING", "INGRAM",
       "JEF", "JEFFERSON",
       "KEN", "KENNEDY",
       "KIL", "KILBUCK",
       "LEE", "LEET",
       "LTD", "LEETSDALE",
       "LIB", "LIBERTY ",
       "LIN", "LINCOLN",
       "MAR", "MARSHALL",
       "MCC", "MCCANDLESS",
       "MCD", "MCDONALD ",
       "RKS", "MCKEES ROCKS",
       "MCK", "MCKEESPORT",
       "MIL", "MILLVALE",
       "MON", "MONROEVILLE",
       "MOO", "MOON",
       "MTO", "MT OLIVER",
       "MTL", "MT. LEBANON",
       "MUN", "MUNHALL ",
       "NEV", "NEVILLE ",
       "NBR", "NORTH BRADDOCK",
       "NFT", "NORTH FAYETTE",
       "NVT", "NORTH VERSAILLES",
       "OKD", "OAKDALE",
       "OAK", "OAKMONT",
       "OHA", "O'HARA",
       "OHI", "OHIO",
       "GOB", "GLEN OSBORNE",
       "PEN", "PENN HILLS",
       "PVG", "PENNSBURY VILLAGE",
       "PIN", "PINE",
       "PIT", "PITCAIRN",
       "PGH", "PITTSBURGH",
       "PLE", "PLEASANT HILLS",
       "PLU", "PLUM",
       "PVU", "PORT VUE",
       "RAN", "RANKIN ",
       "RES", "RESERVE ",
       "RCH", "RICHLAND",
       "ROB", "ROBINSON",
       "ROS", "ROSS",
       "ROF", "ROSSLYN FARMS",
       "SCT", "SCOTT",
       "SEW", "SEWICKLEY",
       "SHT", "SEWICKLEY HEIGHTS",
       "SHI", "SEWICKLEY HILLS",
       "SHA", "SHALER",
       "SHP", "SHARPSBURG",
       "SFT", "SOUTH FAYETTE",
       "SPK", "SOUTH PARK ",
       "SVS", "SOUTH VERSAILLES",
       "SPB", "SPRINGDALE",
       "SPT", "SPRINGDALE TWP.",
       "STO", "STOWE",
       "SWS", "SWISSVALE",
       "TAR", "TARENTUM",
       "THO", "THORNBURG",
       "TRA", "TRAFFORD",
       "TCV", "TURTLE CREEK",
       "USC", "UPPER ST. CLAIR",
       "VER", "VERONA",
       "VSB", "VERSAILLES",
       "WAL", "WALL",
       "WDR", "WEST DEER",
       "WEL", "WEST ELIZABETH",
       "WHM", "WEST HOMESTEAD",
       "WES", "WEST MIFFLIN",
       "WVW", "WEST VIEW",
       "WTK", "WHITAKER",
       "WOA", "WHITE OAK",
       "WHI", "WHITEHALL",
       "WIL", "WILKINS",
       "WBG", "WILKINSBURG",
       "WIM", "WILMERDING"
   });
 }
