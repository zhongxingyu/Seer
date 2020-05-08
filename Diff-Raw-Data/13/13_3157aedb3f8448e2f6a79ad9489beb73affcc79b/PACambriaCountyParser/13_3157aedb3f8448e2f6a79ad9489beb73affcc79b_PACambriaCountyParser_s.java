 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Cambria County, PA
 Contact: jacob berkey <jwb932@gmail.com>
 Sender: alerts@cambria.ealertgov.com
 JO-JOHNTOWN
 
 [29] 11 Time: 15:02:29 Nature: 17A01G-Alpha FALL Location: 216 MAIN ST-JO | Sta 35
 [29] 11 Time: 11:54:47 Nature: 29B01-Bravo VEH ACC WITH INJURIES Location: 110 PLAZA DR-LY | Sta 30, Sta 23, Sta 35
 [29] 11 Time: 08:03:01 Nature: 10C02-Charlie CHEST PAIN Location: 315 LOCUST ST-JO | Sta 35 
 [29] 11 Time: 06:45:29 Nature: 17B03G-Bravo FALL Location: WALNUT ST-JO/MAIN ST-JO | Sta 35
 [28] 11 Time: 21:39:31 N t 06D03A D lt BREATHING PROBLEMS Location: 205 CHANDLER AVE-JO | Sta 35, Sta 21, Sta 36
 
 */
 
 public class PACambriaCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "JO", "JOHNSTOWN"
   });
  // /\\[\\d+\\]
  private static final Pattern MARKER = Pattern.compile("\\d+ ");
   
   public PACambriaCountyParser() {
     super(CITY_CODES, "CAMBRIA COUNTY", "PA",
            "Time:SKIP! Nature:CALL! Location:ADDR/y! Sta:UNIT!");
   }
 
   @Override
  protected boolean parseMsg(String body, Data data) {

    Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     body = body.replace(" | ", " Sta: ");
     return super.parseMsg(body, data);
   }
 }
