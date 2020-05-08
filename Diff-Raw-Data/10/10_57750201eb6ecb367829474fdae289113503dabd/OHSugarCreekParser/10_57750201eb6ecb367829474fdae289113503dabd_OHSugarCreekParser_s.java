 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /***
 "Ct:EMS MINOR       Loc:4336 STATE RT 725            Apt:      XSt:BAYBERRY COVE D LAKEMAN DR      Grid:003F Units:72                                        Rmk:"
We use Cisco Public Safetys ECAD program to page out CFS events.  CAD does send out multiple pages per event  one for the call and each responding unit.  Below are sample texts for review:
 "Ct:FIRE ALARM - RE Loc:4334 KILLIAN CT              Apt:      XSt:BELFAST DR                      Grid:0001 Units:M71   B70   P219                          Rmk:"
 "Ct:EMS MINOR       Loc:I 675 N/8MM                  Apt:      XSt:WILMINGTON PIKE FEEDWIRE RD     Grid:0045 Units:72                                        Rmk:"
 "Ct:TRAFFIC ACCIDEN Loc:FEEDWIRE RD/WINSHIRE TER     Apt:      XSt:LITTLE SUGARCRE LEDGESTONE BLVD Grid:0002 Units:C70   B70   E71   M72   P228              Rmk:"
 "Ct:OTHER FIRE      Loc:5980 WILMINGTON PIKE         Apt:      XSt:CLYO RD         WILMINGTON PIKE Grid:003A Units:72                                        Rmk:"
 ***/
 
 public class OHSugarCreekParser extends SmsMsgParser {
 
  private static final String[]SugarCreekkeywords = new String[]{"Ct", "Loc", "Apt", "XSt", "Grid", "Units"};
 
   @Override
   public boolean isPageMsg(String body) {
     return isPageMsg(body, SugarCreekkeywords);
   }
 
   @Override
   protected void parse(String body, Data data) {
     data.defState = "OH";
     data.defCity = "SugarCreek";
    body = body.replaceAll("\t", ",");
     Properties props = parseMessage(body, SugarCreekkeywords);
     data.strCall = props.getProperty("Ct", "");
     parseAddress(props.getProperty("Loc", ""), data);
     data.strApt = props.getProperty("Apt", "");
     data.strCross = props.getProperty("XSt", "");
     data.strUnit = props.getProperty("Units", "");
     data.strBox = props.getProperty("Grid", "");
     data.strSupp = props.getProperty("Rmk","");
     
   }
 }
