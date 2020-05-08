 package net.anei.cadpage.parsers.CT;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Windham County, CT (Quinebaug Valley Emergency Communications)
 Contact: Jeffrey Bellavance <jcb294@gmail.com>
 Sender: qvecpaging@qvec.org
 
 (Moosup FD Page) STA94 STA594 MOOS1  UHF-4 PRI 1 person fallen Medical Alarm 1 SARA LN / Moosup (X-STS HIGH ST / DEAD END ) 07:31
 (Moosup FD Page) STA94 STA594 MOOS1  33.88 PRI 1 Sick Person 109 WHIP POOR WILL DR / Moosup (X-STS ) 10:57
 (Moosup FD Page) STA94 STA594 MOOS1  UHF-4 PRI 1 Sick Person BARBER HILL RD / N  MAIN ST Moosup (X-STS ) 17:35
 (Moosup FD Page) STA94 94OFF  33.90 Vehicle Fire I 395 SOUTH SOUTH OF REST AREA / Moosup (X-STS ) 14:24
 (Moosup FD Page) STA94  UHF-4 Wire Down Starting a Small Grass Fire / Tree/Wires Down 201 STARKWEATHER RD / Moosup (X-STS GROVE ST  STERLING HILL RD / PLAINFIELD
 (Moosup FD Page) STA94  33.88 Please Operate 33.88/  Lift Assist 2 N  POTVIN AVE / Moosup (X-STS DAGGETT ST / POTVIN AVE ) 11:34
 (Moosup FD Page) STA94  33.76 OUT BUILDING SMOLDERING Outside/Unknown Fire 399 SNAKE MEADOW RD / SNAKE MEADOW CLUB Moosup (X-STS OLEAROS HILL RD / DEMERS RD )
 (Moosup FD Page) STA94  UHF-4 Alarm Reset / Smoke from Cooking /  Fire Alarm 25 SUNNY ACRES / Moosup (X-STS DAGGETT ST / DEAD END ) 10:19
 (Moosup FD Page) STA94  33.90 PRI 1 ROLL OVER / STA94 MANPOWER REQUESTED / Vehicle Accident I 395 S / EXIT 88 / Plainfield (X-STS ) 11:27
 (Moosup FD Page) QVMedic STA94 STA594 MOOS1  UHF-4 PRI 1 PARTIAL AIR WAS OBSTRUCTIONS / Choking/Airway Problem 67 S MAIN ST / Moosup (X-STS VOSSLER DR / WARD LN
 (Moosup FD Page) STA94 STA594 MOOS2  UHF-4 PRI 1 MOOS AMB 2ND CREW / Vehicle Accident SNAKE MEADOW RD / VALLEY VIEW RD / Moosup (X-STS ) 22:10
 
 Contact: Joshua Maloney <josh.d.maloney@gmail.com>
 STA97 STA594 MOOS2  UHF-4 PRI 1 2ND CREW NEEDED Cardiac Distress 30 PICKETT RD / Central Village (X-STS TEX
Subject:Central Village FD Page\nSTA97 STA594 MOOS2  UHF-4 PRI 1 MOOS AMB 2ND CREW NEEDED / Injured Person 80 S CADY LN / CENTRAL CYCLE TRAC
 
 */
 
 public class CTWindhamCountyParser extends SmartAddressParser {
   
   private static final Pattern PRIORITY_PTN = Pattern.compile("^PRI +(\\d) +");
 
   public CTWindhamCountyParser() {
     super("WINDHAM COUNTY", "CT");
   }
   
   @Override
   public String getFilter() {
     return "qvecpaging@qvec.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     Parser p = new Parser(body);
     data.strUnit = p.getOptional("  ");
     data.strChannel = p.get(' ');
    String sAddr = p.get("(X-STS ");
     data.strCross = p.get(')');
     
     int pt = sAddr.lastIndexOf('/');
     if (pt < 0) return false;
     String sPlaceCity = sAddr.substring(pt+1).trim();
     sAddr = sAddr.substring(0,pt).trim();
     
     Matcher match = PRIORITY_PTN.matcher(sAddr);
     if (match.find()) {
       data.strPriority = match.group(1);
       sAddr = sAddr.substring(match.end()).trim();
     }
     parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, sAddr, data);
     
     pt = sPlaceCity.lastIndexOf(' ');
     if (pt < 0 || CITY_SET.contains(sPlaceCity.toUpperCase())) {
       data.strCity = sPlaceCity;
     } else {
       int firstPt = pt;
       do {
         String sCity = sPlaceCity.substring(pt+1).trim();
         if (CITY_SET.contains(sCity.toUpperCase())) break;
         pt =  sPlaceCity.lastIndexOf(' ', pt-1);
       } while (pt > 0);
      if (pt >= 0) {
        data.strCity = sPlaceCity.substring(pt+1).trim();
        sPlaceCity = sPlaceCity.substring(0, pt).trim();
      }
       if (checkAddress(sPlaceCity) > 0) {
         data.strAddress = data.strAddress + " & " + sPlaceCity;
       } else {
         data.strPlace = sPlaceCity;
       }
     }
     return true;
   }
   
   private static final Set<String> CITY_SET = new HashSet<String>(Arrays.asList(new String[]{
       "ASHFORD",
       "BROOKLYN",
          "EAST BROOKLYN",
       "CANTERBURY",
       "CHAPLIN",
       "EASTFORD",
       "HAMPTON",
       "KILLINGLY",
          "DANIELSON",
       "PLAINFIELD",
          "CENTRAL VILLAGE",
          "MOOSUP",
          "PLAINFIELD VILLAGE",
          "WAUREGAN",
       "POMFRET",
       "PUTNAM",
          "PUTNAM DISTRICT",
       "SCOTLAND",
       "STERLING",
          "ONECO",
       "THOMPSON",
          "NORTH GROSVENOR DALE",
          "QUINEBAUG",
       "WINDHAM",
          "SOUTH WINDHAM",
          "WILLIMANTIC",
       "WOODSTOCK",
          "SOUTH WOODSTOCK"
   }));
 }
