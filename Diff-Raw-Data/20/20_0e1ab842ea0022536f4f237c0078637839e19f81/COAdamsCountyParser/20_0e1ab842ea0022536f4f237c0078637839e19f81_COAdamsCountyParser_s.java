 package net.anei.cadpage.parsers.CO;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Adams County, CO
 Contact: "Matt Kehoe" <mjkehoe@gmail.com>
 Sender: ipspage@adcom911.org
 
 Subject:IPS I/Page Notification E 64TH AVE/MONACO ST ADAM CCPD 09:51:48 TYPE CODE: ACCI CALLER NAME: TIME: 09:51:48 Comments: -104.90947
 Subject:IPS I/Page Notification 6950 NIAGARA ST ADAM CCPD 11:04:10 TYPE CODE: EMS CALLER NAME: FELIX - 13YO TIME: 11:04:10 Comments: 15YO
 Subject:IPS I/Page Notification 10220 BRIGHTON RD ADAM CCPD:DIVERSIFIES TRUCK AND RV 17:22:11 TYPE CODE: NONSTR CALLER NAME: BOB TIME: 17:
 Subject:IPS I/Page Notification US HIGHWAY 85 NB/E 77TH AVE ADAM ADAM 03:34:04 TYPE CODE: NONSTR CALLER NAME: ASHLEY TIME: 03:34:04 Commen
 Subject:IPS I/Page Notification 9900 E 102ND AVE ADAM CCPD:USF 06:06:49 TYPE CODE: FRALRM CALLER NAME: ADT TIME: 06:06:49 Comments: WATER
 Subject:IPS I/Page Notification\n26900 E COLFAX AVE ARAP ARAP,454: @FOX RIDGE FARMS 17:19:15 TYPE CODE: EMS CALLER NAME:  TIME: 17:19:15 Co
 
 Contact: Derrick Keeton <derrickkeeton@gmail.com>
 IPS I/Page Notification / 10433 SALIDA ST ADAM CCPD 06:51:58 TYPE CODE: FRALRM CALLER NAME:  TIME: 06:51:58\n\n\n
 
  */
 
 
 public class COAdamsCountyParser extends SmartAddressParser {
   
   private static final Pattern CAD_MARKER = 
         Pattern.compile("^(?:Subject:)?IPS I/Page Notification (?:/ )?");
   private static final String[] KEYWORDS = 
     new String[]{"LOC", "TYPE CODE", "CALLER NAME", "TIME", "Comments"};
   
   private static final Pattern TIME_MARK = Pattern.compile(" +\\d\\d:\\d\\d:\\d\\d$");
   
   private static final Properties CITY_TABLE = buildCodeTable(new String[]{
       "ADAM CCPD", "",
       "ARAP ARAP", "ARAPAHOE COUNTY"
   });
   
   public COAdamsCountyParser() {
     super(CITY_TABLE, "ADAMS COUNTY", "CO");
   }
   
   @Override
   public String getFilter() {
     return "ipspage@adcom911.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (subject.length() > 0) body = "Subject:" + subject + ' ' + body;
 
     Matcher match = CAD_MARKER.matcher(body);
     if (!match.find()) return false;
     body = "LOC:" + body.substring(match.end()).trim();
     
     Properties props = parseMessage(body, KEYWORDS);
     String sAddr = props.getProperty("LOC", "");
     match = TIME_MARK.matcher(sAddr);
     if (match.find()) sAddr = sAddr.substring(0, match.start());
     int pt = sAddr.lastIndexOf(':');
     if (pt >= 0) {
       data.strPlace = sAddr.substring(pt+1).trim();
       sAddr = sAddr.substring(0,pt).trim();
       if (data.strPlace.startsWith("@")) {
         data.strPlace = data.strPlace.substring(1).trim();
       }
     }
     parseAddress(StartType.START_ADDR, sAddr.replace(',', ' '), data);
     
     data.strCall = props.getProperty("TYPE CODE", "");
     
     // There is a comment field, but it never seems to have enough info to
     // make it worth parsing
     
     return true;
   }
 }
