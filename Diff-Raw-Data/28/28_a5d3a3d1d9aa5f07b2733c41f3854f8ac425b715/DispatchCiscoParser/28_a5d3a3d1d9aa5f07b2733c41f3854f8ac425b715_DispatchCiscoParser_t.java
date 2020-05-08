 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /***
 Sugarcreek, OH
 "Ct:EMS MINOR       Loc:4336 STATE RT 725            Apt:      XSt:BAYBERRY COVE D LAKEMAN DR      Grid:003F Units:72                                        Rmk:"
 We use Cisco Public Safety�s ECAD program to page out CFS events.  CAD does send out multiple pages per event � one for the call and each responding unit.  Below are sample texts for review:
 "Ct:FIRE ALARM - RE Loc:4334 KILLIAN CT              Apt:      XSt:BELFAST DR                      Grid:0001 Units:M71   B70   P219                          Rmk:"
 "Ct:EMS MINOR       Loc:I 675 N/8MM                  Apt:      XSt:WILMINGTON PIKE FEEDWIRE RD     Grid:0045 Units:72                                        Rmk:"
 "Ct:TRAFFIC ACCIDEN Loc:FEEDWIRE RD/WINSHIRE TER     Apt:      XSt:LITTLE SUGARCRE LEDGESTONE BLVD Grid:0002 Units:C70   B70   E71   M72   P228              Rmk:"
 "Ct:OTHER FIRE      Loc:5980 WILMINGTON PIKE         Apt:      XSt:CLYO RD         WILMINGTON PIKE Grid:003A Units:72                                        Rmk:"
Ct:EMS MINOR       Loc:6244 WILMINGTON PIKE         Apt:      XSt:CLYO RD         CENTER POINT DR Grid:003B Units:E74   M72   R70   P241 
 
 Vigo County, IN
 Ct:FALL Loc:3301 ST MARY'S RD Apt: XSt:BLOOMTOWN RD US HWY 150 Grid:112D Units:E-42 TC-1 Rmk:92 YOLD FML; FELL; FACE DOWN ON FLOOR -
 Ct:FALL Loc:309 N CHURCH ST Apt: XSt:PARIS AV Grid:0WTH Units:E-41 TC-2 Rmk:65 Years F ;fall
 Ct:CHEST PAIN Loc:500 W PARIS AV Apt: XSt:6TH ST WTH 4TH ST WTH Grid:0WTH Units:E-41 TC-1 Rmk:
 Ct:CHOKING Loc:2925 N MAPLE PL Apt: XSt:LOCUST AV Grid:0126 Units:E-42 TC-1 Rmk:18 Months F ;CHOKING
 Ct:MVA-UNKNOWN-INJ Loc:US HWY 40/INTERSTATE 70 Apt: XSt: Grid:0169 Units:E-41 Rmk:SEMI AND PASSENGER CAR//UNKNOWN Injuries
 
 ***/
 
 public class DispatchCiscoParser extends SmartAddressParser {
   
   public DispatchCiscoParser(Properties cityCodes, String defCity, String defState) {
     super(cityCodes, defCity, defState);
   }
 
   private static final String[]KEYWORDS = 
     new String[]{"Ct", "Loc", "Apt", "XSt", "Grid", "Units", "Rmk"};
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     body = body.replaceAll(" +", " ");
     Properties props = parseMessage(body, KEYWORDS);
    data.strCall = props.getProperty("Ct");
    if (data.strCall == null) return false;
     data.strCall = data.strCall.trim();
    
    String sAddress = props.getProperty("Loc");
    if (sAddress == null) return false;
     parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, props.getProperty("Loc", ""), data);
     data.strApt = props.getProperty("Apt", "");
     data.strCross = props.getProperty("XSt", "");
     data.strMap = props.getProperty("Grid", "");
     data.strUnit = props.getProperty("Units", "");
     data.strSupp = props.getProperty("Rmk","");
     return true;
   }
 }
