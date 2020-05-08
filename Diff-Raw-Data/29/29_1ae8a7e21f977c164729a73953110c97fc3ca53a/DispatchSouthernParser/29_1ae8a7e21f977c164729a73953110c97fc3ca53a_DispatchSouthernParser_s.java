 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Southern Software CAD
 
 Jefferson County, TN 
 DISPATCH:1718 BROOKLINE CT JEFFERSON CITY 2011-611597 22:43:08  BREATHING DIFFICULTY
 
 Jackson County, NC 
 Bill:9783 CHARLEYS CREEK RD TUCKASEGEE MDL 26A03 2011009480 22:20:01 SICK PERSON (SPECIFIC DIAGNOSIS) CALLER ADV SHE HAD HEART SURGERY A LITTLE WHILE AGO AND HER
 
 Brunswick County, NC
 349:560 CUMBERLAND ST SE BOLIVIA MDL 09B01 11-020650 17:02:34 Cardiac or Respiratiory Arrest / Death RECV CALL FROM FUNERAL HOME ADVING THAT THEY RECV CALL STATING A FEMALE WAS DEAD AT THIS LOCATION AD
 415:111 SE 32ND ST OAK ISLAND VERIZON WIRELESS 7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Respiratiory Arrest - De
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD
 399:1295 DECATOR ST SW SUPPLY PAM 9106339368 MDL 09E01 11-025419 22:11:30 Cardiac or RespArrest  CALLER ADV THE PT IS STIFF
 428:BAY POINT CT / JENNIS AV FELICIA  9103685110 MDL 27D04 11-026412 21:09:48 Stab-Gunshot-Pen Trauma AT THE END OF  RD .
 426:BAY POINT CT-JENNIS AV FELICIA 9103685110 11-026412 21:07:25 Stab-Gun-Trauma AT THE END OF BAYPOINT RD MALE SUBJ IS CUT
 415:111 SE 32ND ST OAK ISLAND  7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Resp Arrest - Death TRANS BY OKI... ADVD TH
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD,
 
 Person County, NC
 JBROWN:115 CROSS CREEK CT TIMBERLAKE 2011019922 18:27:47 FIRE STOVE
 Jason_White:HURDLE MILLS RD / PAYNES TAVERN RD 2011019901 15:46:01 MVA PI CARD 29 car vs motorcycle
 Shutchings:GUESS RD / HURDLE MILLS RD 2011019810 00:35:44 MVA PI CARD 29
 Shutchings:115 JACOBS-MOORE LN HURDLE MILLS 2011019797 23:05:28 FIRE STRUCTURE FLAMES VISIBLE the house is on fire
 Bmelton:HURDLE MILLS RD / POINDEXTER RD 2011019760 17:21:08 TREE DOWN
 BGILLISPIE:796 WHEELERS CHURCH RD HURDLE MILLS MDL 10C01 2011019661 05:54:13 CHEST PAIN CARD 10
 BROWN:225 CROWN BLVD TIMBERLAKE 2011020040 11:49:49 ALARMS-FIRE design works building 2 general fire alarm adt 8772857397
 
 Chilton County, AL
 120 COUNTY RD 377 BILLINGSLEY 2011040762 18:41:28 MENTAL PERSON/PATIENT CUTTING HIMSELF, ALTERED MENTAL STATUS
 COUNTY RD 57 \ COUNTY RD 445 2011039172 06:20:45 WRECK-UNKNOWN INJURIES CALLER STATES ONE VEHICLE OVERTURNED.. ADV THAT THE SUBJECT IS OUT OF THE VEHICLE
 120 COUNTY RD 377 BILLINGSLEY 2011040762 18:41:28 MENTAL PERSON/PATIENT CUTTING HIMSELF, ALTERED MENTAL STATUS
 100 COUNTY RD 99 LOT 5 VERBENA 2011043298 18:37:55 SHORTNESS OF BREATH GRANDMOTHER ON CHEMO NOT DOING GOOD 
 518 COUNTY RD 221 THORSBY 2011046746 06:20:05 DIABETIC RESIDENCE IS A BEIGE DOUBLE WIDE WITH MAROON SHUTTERS. RED TOYOTA AND BLACK DODGE IN THE BACK YARD
 
 Chattam County, NC
 HEATHER.BOONE:399 LYNDFIELD CLOSE PITTSBORO MDL 10D04 1119468 18:13:33 Chest Pains- Clammy HUSBAND HAVING CHEST PAINS - EMER
 HEATHER.BOONE:791 RED GATE RD PITTSBORO 1119520 05:38:59 medical call HUSBAND OFF BALCONY
 LISA.SCOTT:965 SEAFORTH BEACH RD PITTSBORO 1119461 17:08:12 medical call 10YOM WEARING WHITE SHORTS NAMED TRAY LAST SEEN APPROX 4 MINS AGO
 KIM.HANNER:858 BRUMLEY PITTSBORO MDL 31C02 1119495 22:11:57 Unconscious / Fainting (Near) Fainting episodes an WIFE PASSED OUT, IS CONS NOW
 KIM.HANNER:475 NATURE TRAIL RD CHAPEL HILL 1119504 00:09:15 Convulsions/ Seizures- CONTINUOUS or MULTIPLE seiz PREVIOUS CALL FOR THIS ADDRESS WITH THIS NUMBER GIVEN BY CARY PD UNKNOWN PROBLEM POSSIBLE SEIZURES
 
 Moore County, NC
 [[911 NOTIFICATION] ]  505 PINEHURST ST ABERDEEN 2011024766 19:52:16 F69 STRUCTURE FIRE LOOKS LIKE BACK DECK ON FIRE
 [[911 NOTIFICATION] ]  1 S. VINELAND SOUTH CELL TOWER 2011025029 09:31:20 M25 PSYCHIATRIC/SUICIDE ATTEMPT
 [[911 NOTIFICATION] ]  126 ROBIN HOOD LN ABERDEEN 2011025065 13:37:27 F52 ALARM-FIRE ACTIVATION AC 800 932-3822 OPER SP8
 [[911 NOTIFICATION] ]  632 SAND PIT RD ABERDEEN MDL 07A01 2011025721 11:06:58 M7 BURNS/EXPLOSIONS
 [[911 NOTIFICATION] ]  218 BERRY ST PINE BLUFF 2011026135 14:36:24 F67 OUTSIDE FIRE/WOODS/BRUSH TREE ON FIRE
 [[911 NOTIFICATION] ]  1 E NEW ENGLAND /S PEAR 2011025862 07:31:07 F67 OUTSIDE FIRE/WOODS/BRUSH
 
 Polk County, NC
 polkcounty911:767 THERMAL VIEW DR TRYON SMITH 828817313307:38:26 FALL VICTIM 89 YO FEMALE FROM STORY RD TO SKYUKA RD
 polkcounty911:1426 HAYES RD COLUMBUS 07:20:59 TREE DOWN CONTS FROM S PEAK ST TO PENIEL RD
 polkcounty911:418 S PEAK ST COLUMBUS EARLY, DORIS 8288948807 13:09:13 MEDICAL EMERGEN 86 YO VERY WEAK FROM E MILLS ST AND CONTS AS HAYES RD
 polkcounty911:429 LOCKHART RD TRYON RHONDA 828859020509:57:24 MEDICAL EMERGEN 38 YO LEFT LEG PAINNUMB FOOT FROM W HOWARD ST TO LANIER LN
 polkcounty911:86 GREENVILLE ST SALUDA PAT 8287494411 13:49:40 CHOKING FEMALE CHOKING FOR PAST HOUR//PALE
 polkcounty911:159 LANIER DR COLUMBUS John 8643030507 08:08:08 UNCONSCIOUS 75 yo man with cancer.
 polkcounty911:1 GREEN RIVER COVE RD mill spring carol 8288179441 09:52:03 DOWN POWER LINE FROM S NC 9 HWY TO RUTHERFORD CO LINE
 Polkcounty911:1733 E US 74 HWY before pea ridge rd MILL SPRING 423637001121:49:01 10-50. June 9, 9:48 PM
 polkcounty911:26 KELLY LN MILL SPRING 8288948141 16:50:54 SEIZURES 25 Y/O MALE SEIZING FROM CHIMNEY ROCK RD TO DEAD END. June 10 4:50 PM.
 polkcounty911:935 POLK COUNTY LINE RD RUTHERFORDTON BAILY, PHILLIP 8287489222 14:23:05 VEHICLE FIRE 18 WHEELER LOADED WITH LUMBER ON FIRE FROM ROCK SPRINGS. June 11, 2:21 PM.
 polkcounty911:4861 E NC 108 HWY MILL SPRING SLIMPLEX GRENELL 888746753911:28:38 FIRE ALARM BUILDING ONE FROM HOUSTON RD/WALKER ST TO RUTHERFORD CO LINE. June 14, 11:29 AM.
 polkcounty911:620 BURT BLACKWELL RD MILL SPRING QUANDT, LEONARD & CRYSTAL 8288946023 13:27:52 SMOKE COMPLAINT ACROSS FROM THE ABOVE ADDRESS NEAR DUKE POWER. June 15, 1:28 PM
 polkcounty911:1 COLUMBUS FIRE DEPT 2201 17:54:26 STANDBY REQUEST MSFD STANDBY FOR THEM REF RESOURCES DEPLETED. June 18, 5:55 PM
 polkcounty911:1 GREEN RIVER COVE RD mill spring carol 8288179441 09:52:03 DOWN POWER LINE FROM S NC 9 HWY TO RUTHERFORD CO LINE. June 19 9:53 AM.
 
 Watauga County, NC
 HOWARDS CREEK RD / SUGARLOAF RD FDL 71B01 2011013987 16:02:05 VEHICLE FIRE 421N-RT TATER HILL RD-LT HOWARDS CREEK RD TO SUGARLOAF RD
 524 JONES DR BOONE MDL 01A01 2011013875 02:10:09 ABDOMINAL PAIN 194S-CHESTNUT GRV RD-LT COBBS CREEK RD-1ST RT JONES DR
 131 BIG VALLEY ST BOONE 2011013813 13:15:57 STRUCTURE FIRE 321S-LT DEERFIELD RD-TRAVEL 2.0 MI-RT BIG VALLEY ST INTO DEERFIELD MEADOWS BUSINESS PARK
 200 QUAIL ST BOONE 2011013939 03:11:32 STANDBY
 853 HIDDEN VALLEY DR BOONE MDL 09E01 2011013997 20:13:36 ARREST-ADULT 105S-BESIDE HIDDEN VALLEY MOTEL-LT HIDDEN VALLEY DR
 
 */
 
 public class DispatchSouthernParser extends SmartAddressParser {
   
   // Flag indicating  a leading dispatch name is required
   public static final int DSFLAG_DISPATCH_ID = 0x01;
   
   // Flag indicating a leading dispatch name is optional
   public static final int DSFLAG_OPT_DISPATCH_ID = 0x02;
   
   // Flag indicate a unit designation follows the time stampe
   public static final int DSFLAG_UNIT = 0x04;
   
   // Flag indicating that the call ID is optional
   public static final int DSFLAG_ID_OPTIONAL = 0x08;
   
   private static final Pattern LEAD_PTN = Pattern.compile("^[\\w\\.]+:");
   private static final Pattern ID_TIME_PTN = Pattern.compile("\\b(\\d{2,4}-?\\d{4,8}) \\d\\d:\\d\\d:\\d\\d\\b");
   private static final Pattern OPT_ID_TIME_PTN = Pattern.compile("\\b(?:(\\d{2,4}-?\\d{4,8}) )?\\d\\d:\\d\\d:\\d\\d\\b");
   private static final Pattern CALL_PTN = Pattern.compile("^[A-Z0-9\\- /]+\\b");
 
   private Pattern idTimePattern;
   private boolean leadDispatch;
   private boolean optDispatch;
   private boolean unitId;
   
   public DispatchSouthernParser(String[] cityList, String defCity, String defState) {
     this(cityList, defCity, defState, DSFLAG_DISPATCH_ID);
   }
   
   public DispatchSouthernParser(String[] cityList, String defCity, String defState, int flags) {
     super(cityList, defCity, defState);
     this.leadDispatch = (flags & DSFLAG_DISPATCH_ID) != 0;
     this.optDispatch = (flags & DSFLAG_OPT_DISPATCH_ID) != 0;
     this.unitId = (flags & DSFLAG_UNIT) != 0;
     this.idTimePattern = (flags & DSFLAG_ID_OPTIONAL) != 0 ? OPT_ID_TIME_PTN : ID_TIME_PTN;
   }
 
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     // Message must always start with dispatcher ID, which we promptly discard
     if (leadDispatch || optDispatch) {
       Matcher match = LEAD_PTN.matcher(body);
       if (match.find()) {
         body = body.substring(match.end()).trim();
       } else if (!optDispatch) return false;
     }
     
     // find an ID/Time pattern which splits the message into two fields
     Matcher match = idTimePattern.matcher(body);
     if (!match.find()) return false;
     
     data.strCallId = match.group(1);
     if (data.strCallId == null) data.strCallId = "";
     String sAddr = body.substring(0,match.start()).trim();
     String sExtra = body.substring(match.end()).trim();
     
     parseMain(sAddr, data);
 
     // Second half May contain unit ID,
     // then call description and long call description
     // Call description comes first and contains only upper case letters and numbers
     parseExtra(sExtra, data);
     return true;
   }
 
   protected void parseMain(String sAddr, Data data) {
     // First half contains address, optional place/name, and possibly an MDL call code
     Parser p = new Parser(sAddr);
     data.strCode = p.getLastOptional(" MDL ");
     if (data.strCode.length() == 0) data.strCode =p.getLastOptional(" FDL ");
     sAddr = p.get();
     parseAddress(StartType.START_ADDR, sAddr, data);
     data.strPlace = getLeft();
   }
 
   protected void parseExtra(String sExtra, Data data) {
     Matcher match;
     Parser p;
     if (unitId) {
       p = new Parser(sExtra);
       data.strUnit = p.get(' ');
       sExtra = p.get();
     }
     match = CALL_PTN.matcher(sExtra);
     if (match.find()) {
       String sCall = match.group().trim();
       if (sCall.length() <= 20) {
         data.strCall = sCall;
         sExtra = sExtra.substring(match.end()).trim();
       }
     }
     data.strSupp = sExtra;
   }
 }
