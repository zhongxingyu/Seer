 package net.anei.cadpage.parsers.CT;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 New Haven County, CT (North Branford & Northford)
 Contact: Jeff Green <greenjeffreya@gmail.com>
 Sender: paging@mail.nbpolicect.org.
 
 1100008173 MEDICAL MEDA 00167  BRANFORD RD HUBBARD RD/TWIN LAKE RD  MED4 503 110910 11:50
 1100008148 MEDICAL MEDD 00009  SALEM ST CLINTONVILLE RD/VILLAGE ST  MED4 R2 110909 17:55
 1100008142 MUTUAL AID 00392  HOPE HILL RD WALLINGFORD    MED4 110909 15:38
 1100008130 AUTOMATIC FIRE ALARM 00051  CIRO RD FOXON RD/DEAD END  E1 ET11 ET22 T1 R11 515 110909 10:07
 1100008128 MEDICAL MEDB 00049  CAPUTO RD FOXON RD ROUTE 80/MILL RD  R1 MED4 515 110909 08:38
 1100007980 BRUSH FIRE TWIN LAKES RD/ LAKE RD   BR1 ET11 502 110904 12:22
 
 Wallinford Mutual aid (WLFD)
 1100008057 MVA W/INJURIES N MAIN/RT68 WLFD A44 110906 17:05
 1100007014 MUTUAL AID 00909  BEAVER HEAD RD , GUILFORD    MED4 110811 01:16
 
 Contact: Chris crotty <nbfd819@gmail.com>
 Sender: paging@nbpolicect.org
 1100010113 MEDICAL MEDC 00254  BRANFORD RD Prem Map -  HARRISON RD/FOXON RD  MED4 R1 111116 23:04\n
 
 Contact: "nemo48@sbcglobal.net" <nemo48@sbcglobal.net>
 Sender: paging@easthavenfire.com
 1100005182 ALPHA MEDICAL 00055 THOMPSON ST  Prem Map -14 PP 65 FOXON RD/GAY ST  (Prem Map -14 PP 65)\4sS5 111120 15:40
 
 */
 
 public class CTNewHavenCountyBParser extends SmartAddressParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
     "WLFD", "WALLINFORD"
   });
   
   private static final Pattern MASTER = Pattern.compile("(\\d{10}) +(.*?) *\\d{6} (\\d\\d:\\d\\d)");
   private static final Pattern UNIT_PTN = Pattern.compile("(?: +(?:\\d{3}|(?:MED|R|T|E|ET|BR|A|S)\\d{1,2}))+$");
  private static final Pattern MAP_PTN = Pattern.compile("^\\d\\d [A-Z]{2} \\d\\d\\b");
   private static final Pattern LEAD_ZERO_PTN = Pattern.compile("^0+(?=\\d)");
   
   public CTNewHavenCountyBParser() {
     super(CITY_CODES, "NORTH BRANFORD", "CT");
   }
   
   @Override
   public String getFilter() {
     return "paging@mail.nbpolicect.org,paging@nbpolicect.org,paging@easthavenfire.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strCallId = match.group(1);
     body = match.group(2);
     data.strTime = match.group(3);
     
     match = UNIT_PTN.matcher(body);
     if (match.find()) {
       data.strUnit = match.group().trim();
       body = body.substring(0, match.start());
     }
     
     String sExtra;
     int pt = body.indexOf(',');
     if (pt >= 0) {
       sExtra = body.substring(pt+1).trim();
       body = body.substring(0,pt).trim();
       Parser p = new Parser(sExtra);
       data.strCity = p.get(' ');
       sExtra = p.get();
       parseAddress(StartType.START_CALL, FLAG_ANCHOR_END | FLAG_START_FLD_REQ, body, data);
     }
     
     else {
       parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, body, data);
       sExtra = getLeft();
     }
    if (sExtra.startsWith("Prem Map -")) {
      sExtra = sExtra.substring(10).trim();
       match = MAP_PTN.matcher(sExtra);
       if (match.find()) {
         data.strMap = match.group();
         sExtra = sExtra.substring(match.end()).trim();
         pt = sExtra.indexOf('(');
         if (pt >= 0) sExtra = sExtra.substring(0,pt).trim();
       }
     }
     data.strCross = sExtra;
     
     match = LEAD_ZERO_PTN.matcher(data.strAddress);
     if (match.find()) {
       data.strAddress = data.strAddress.substring(match.end()).trim();
     }
     return true;
   }
 }
