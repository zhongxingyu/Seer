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
System: NEXTGEN
 
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
 
East Haven, CT (in New Haven county)
Contact: "Mark Nimons" <MNimons@easthavenfire.com>
Sender: paging@easthavenfire.com

1100004628 CHARLIE MEDICAL TF1 00057 MAIN ST Prem Map -5 PP 80 SALTONSTALL PKWY/DEBORAH LA (Prem Map -5 PP 80) R1 111017 09:16
1100004627 CHARLIE MEDICAL TF3 00267 RUSSO AVE Map -13 BRENNAN ST/ANN ST S5 111017 03:25
1100004626 MVA WITH INJURIES LAUREL ST/ NORTH HIGH ST E1 R1 C4 S2 111016 23:29
1100004625 CHARLIE MEDICAL TF4A 00038 TALMADGE AVE Prem Map -3 PP165 POND ST/DEAD END (Prem Map -3 PP165) R1 111016 22:00
1100004612 ALPHA MEDICAL 01270 NORTH HIGH ST Prem Map -14 PP 63 CORBIN RD/MAPLE ST S5 111015 21:49
1100004608 DELTA MEDICAL TF1 00152 KIMBERLY AVE Map -7 PARDEE PL/KIMBERLY AVE R1 111015 19:03

 */
 
 public class CTNewHavenCountyBParser extends SmartAddressParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
     "WLFD", "WALLINFORD"
   });
   
   private static final Pattern MASTER = Pattern.compile("(\\d{10}) +(.*?) *\\d{6} (\\d\\d:\\d\\d)");
   private static final Pattern UNIT_PTN = Pattern.compile("(?: +(?:\\d{3}|(?:MED|R|T|E|ET|BR|A|S)\\d{1,2}))+$");
  private static final Pattern MAP_PFX_PTN =Pattern.compile("^(?:Prem )?Map -");
  private static final Pattern MAP_PTN = Pattern.compile("^\\d{1,2} ?[A-Z]{2} ?\\d{1,3}\\b");
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
    match = MAP_PFX_PTN.matcher(sExtra);
    if (match.find()) {
      sExtra = sExtra.substring(match.end()).trim();
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
