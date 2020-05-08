 package net.anei.cadpage.parsers.MO;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchGlobalDispatchParser;
 
 /*
 Ray County, MO
 Contact: "Chief O'Dell" <leeodell@woodheightsfire.com>
 Sender: raycountycentraldispatch@gmail.com
 
 RCAD WHFD FALLS 514 ARAPAHOE DR  HOMESTEAD VILLAGE CrossStreets: CHEROKEE DR 0.05 mi W HIGHWAY Y 0.16 mi E Description: BETTY JAMES 85 YO FEMALE\nFAL
 LFD EMS 31004 NOTTINGHAM LN   Description:  CrossStreets: SILVEY RD 6443.06 mi NW SILVEY RD 6443.06 mi NW
 WHFD RCAD FALLS 33408 W 160TH ST  RAY COUNTY Description: 91 yof\nfell from standing position\nconsious and breathing\ncomplaining of l hip pain\nup in 
 WHFD RCAD RCSO DEATH 32534 MAGNOLIA LN  RAY COUNTY Description: 61 yo female\n
 WHFD RCAD TRAUMATIC INJURIES (SPECIFIC) 2062 E RIDGE DR  RAY COUNTY Description: 15 YO MALE\nBICYLCE ACCIDENT\nFELL FROM BIKE\nCONSCIOUS\nBREATHING\nNO S
 WHFD RCAD M43 800 ABDOMINAL PAIN 12595 ORRICK RD  RAY COUNTY Description: 28 YOLD FEMALE \nCONS AND BREATH \nSEVERE STOMACH PAIN LOWER ABDOMEN \nLUPUS
 
 LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 215 E 5TH ST 3A LAWSON Description: 123254415\nPT IS PATRICIA BIDDIX\nHIGH BP\n67 YO FEMALE\nBURNING SE
 LMED1 HEART PROBLEM / AICD 618 SHEPHERD LN   Description: 17 YO Rapid Heart rate\n\n
 LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 316 KINGS DR  LAWSON Description: 90 yof\nill since for in morning\nhigh sugar\nbreathing difficulty\nu
 LMED1 LSQ1 L602 SEIZURE 34030 W 204TH ST  RAY COUNTY Description: camp wilderness retreat\n13/f\nbreathing\ncons\nseizing at this time\n1st seizure for a
 LMED1 LSQ1 L602 LACERATION 202 E MOSS ST  LAWSON Description: puncture wound in l foot\n42 yof\nconsious and breathing\nnausea CrossStreets: S DONIPHAN
 
 800 WHP1 WHT2 WHSQ1 LP4 LP1 LT1 OT2 RFD RCAD WHFD WHR1 FIRE STRUCTURE 11783 SIEGEL CEMETERY RD  RAY COUNTY Description: EXCELSIOR SPRINGS\nOUTSIDE CI
 800 WHB1 WHT2 WHFD WHR1 FIRE NATURAL COVER OR BRUSH 33637 HIGHWAY U  RAY COUNTY Description: SMALL BRUSH FIRE THAT IS NEXT TO A CROP FIELD\nRP IS STA
 WHP1 800 WHFD WHR1 FIRE ALARM 34684 HIGHWAY 10  RAY COUNTY Description: gen fire alarm. covers all zones\n\nkeyholder ron rouse 816-864-4434
 
 107 N RAYMORE ST  WOOD HEIGHTS WHFD RCAD RCSO 800 M42 WHR1 678 Clear: 22:48:39 Available: 23:34:02
 15595 BLACKBERRY TRAIL  WHFD 800 LFD 802 LT1 LP1 WHP1 Dispatch: 5/24/2011 23:15:33 Enroute: 23:15:35 OnScene: 23:23:58
 15141 W COUNTY LINE RD  RAY COUNTY WHFD RCAD 671A 675A 800 M44 WHR1 Clear: 22:20:51 Available: 23:05:20
 
672 671A RCAD WHFD MENTAL 10-96 15041 S BEACH FRONT DR  CRYSTAL LAKES Description: 16 yr old male\n6'1 205\nl CrossStreets: APACHE DR 0.07 mi E EAGLE 

 */
 
 
 public class MORayCountyParser extends DispatchGlobalDispatchParser {
   
   private static final String[] CITY_TABLE = new String[]{
     "CAMDEN",
     "CRYSTAL LAKES",
     "ELMIRA",
     "EXCELSIOR SPRINGS",
     "FLEMING",
     "HARDIN",
     "HENRIETTA",
     "HOMESTEAD",
     "HOMESTEAD VILLAGE",
     "LAWSON",
     "ORRICK",
     "RAYVILLE",
     "RICHMOND",
     "STET",
     "WOODS HEIGHTS",
     "RAY COUNTY"
   };
   
   private static final Pattern STATION_PTN = 
       Pattern.compile("WHFD|LFD|OFD|RFD|RCAD|EXFD|HFD");
   private static final Pattern UNIT_PTN = 
      Pattern.compile("WH[A-Z]\\d|[68]0\\d|67\\d[A-Z]?|LMED\\d|[LOR][A-Z]\\d|MED4\\d|M4\\d|LSQ\\d|RCSO|L\\d{3}|WHSQ\\d|315|400");
   
   public MORayCountyParser() {
     super(CITY_TABLE, "RAY COUNTY", "MO", STATION_PTN, UNIT_PTN);
   }
   
   @Override
   public String getFilter() {
     return "raycountycentraldispatch@gmail.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (body.contains(" Clear:")) return false;
     if (body.contains(" Enroute:")) return false;
     body = body.replace('\n', ' ');
     if (!super.parseMsg(body, data)) return false;
     if (data.strCity.equals("RAY COUNTY")) data.strCity = "";
     return true;
   }
 }
