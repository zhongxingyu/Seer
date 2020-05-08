 package net.anei.cadpage.parsers.CA;
 
 import java.util.Arrays;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Shasta County, CA (was Bella Vista)
 Contact: Cody Lande <cody.lande@gmail.com>
 Sender: btrinitybob+caf_=5307373826=vtext.com@gmail.com
 
 (CAD Page) FIRE, SMOKE CHECK; 21199 HWY 299 E ,BELLAVISTA ; ; 11764 BLK REBECCA LN; Map: 3225; Inc# 009012; VOL33; neighbor burning
 (CAD Page) MED, TRAFFIC COLLISION; 21044 HWY 299 E / 11760 NEAL LN ,BELLAVISTA ; ; ; Map: 3225; Inc# 009230; B2413 VOL33 E2462; VEH
 (CAD Page) MEDICAL; 21018BLK  JOANNA WY ,BELLAVISTA ; 21017 JOANNA; 11650 BLK HELENA DR; Map: 3225; Inc# 009041; VOL33; 69f, loc
 (CAD Page) MEDICAL; 9000 VISTA DEL SIERRA DR / 6378 PARK RIDGE DR ,STA43 ; 6433 VISTA DEL SIERRA DR; ; Map: 3675; Inc# 008927; VOL33
 (CAD Page) FIRE, VEH COMMERCIAL; 13575 WOODMAN LN ,BELLAVISTA ; ; 24101 BLK AJAX LN; Map: 3077; Inc# 008705; B2409 VOL33 WT33 E2485
 (CAD Page) MEDICAL; 21940 OLD ALTURAS RD / 21480 SEVEN LAKES LN ,BELLAVISTA ; ; ; Map: 3225; Inc# 008684; VOL33; chrylsis charter sc
 (CAD Page) MEDICAL; ,BELLAVISTA ; 21376 FAITH;  DEAD-END; Map: 3225; Inc# 008945; VOL33 E2462; eld m poss stroke
 (CAD Page) MEDICAL; 12169 KERN DR ,BELLAVISTA ; ; 21616 BLK KATHY LN; Map: 3225; Inc# 008957; VOL33 E2462; 68 f dxbr
 (CAD Page) MEDICAL; 11894 BEST LN ,BELLAVISTA ; ;  DEAD-END; Map: 3225; Inc# 009298; VOL33 E2452; 91 YOM CHEST PAIN
 
 Semicolon separated
 Call desc
 Address
 City or Station?
 cross address #1
 cross address #2
 Map: <map>
 INC# <call #>
 <units>
 <info>
 
 BELLAVISTA
 STA43
  */
 
 
 public class CAShastaCountyParser extends SmartAddressParser {
   
   public CAShastaCountyParser() {
     super("SHASTA COUNTY", "CA");
   }
   
   @Override
   public String getFilter() {
    return "vtext.com@gmail.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (!subject.equals("CAD Page")) return false;
    
     String[] flds = body.trim().split(" *; *");
     if (flds.length < 6) return false;
     
     String[] addressList = new String[3];
     
     data.strCall = flds[0];
     Parser p = new Parser(flds[1]);
     addressList[0] = p.get(',');
     data.strSource = p.get();
     addressList[1] = flds[2];
     addressList[2] = flds[3];
     
     // Life gets complicated, we have three address fields, but the best address
     // is not necessarily the first.  Sort them by address quality, the first
     // one will be the address, others will be cross streets
     AddrStat[] statList = new AddrStat[3];
     for (int j = 0; j< 3; j++) {
       statList[j] = new AddrStat(addressList[j]);
     }
     Arrays.sort(statList);
     parseAddress(statList[0].address, data);
 
     data.strCross = statList[1].address;
     if (statList[2].status >= 0) data.strCross = data.strCross + " & " + statList[2].address;
     
     
     // Parse rest of fields
     if (!flds[4].startsWith("Map: ")) return false;
     data.strMap = flds[4].substring(5).trim();
     
     if (!flds[5].startsWith("Inc# ")) return false;
     data.strCallId = flds[5].substring(5).trim();
     
     if (flds.length <= 6) return true;
     data.strUnit = flds[6];
     
     if (flds.length <= 7) return true;
     data.strSupp =flds[7];
     
     return true;
   }
   
   private class AddrStat implements Comparable<AddrStat>{
     String address;
     int status;
     
     AddrStat(String address) {
       this.address = address;
       this.status = -1;
       if (address.length() == 0) return;
       
       this.status = checkAddress(address);
       if (!address.contains("BLK")) this.status += 100;
       if (!address.contains("/")) this.status += 10;
     }
 
     @Override
     public int compareTo(AddrStat adst) {
       return -(this.status - adst.status);
     }
   }
 }
