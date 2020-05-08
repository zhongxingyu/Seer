 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Hoke County, NC
 Contact:Dean Barefoot <muddybrute@gmail.com>
 Sender: Hoke@hokecounty.org
 
 Hoke Co 911:795 TC JONES RD* * * * * **EMD* * * * * * * * 09/15/2011 14:36:26 : pos4 : mblack3325 ** EMD Case Entry Finished ** Chief Complaint Number: 19 Key Q
 Hoke Co 911:385 SUNSET LAKE RD* * * * * **EMD* * * * * * * * 09/19/2011 06:20:35 : pos2 : SCOOPER9403 Cross streets: 7100 ARABIA RD//DEAD END Cross streets: 205
 Hoke Co 911:6020 PHILLIPI CHURCH RD* * * * * **26 SICK PERS* * * * * * * * 09/18/2011 00:57:12 : pos1 : LGRADY7274 ** EMD Case Complete ** Key Questions: 46 yea
 Hoke Co 911:6020 PHILLIPI CHURCH RD* * * * * **EMD* * * * * * * * 09/18/2011 00:56:49 : pos1 : LGRADY7274 ** EMD Recommended Dispatch ** Response Text: Alpha Di
 Hoke Co 911:216 ROLLING PINES CT* * * * * **01 ABD PAIN* * * * * * * * 09/17/2011 19:54:51 : pos2 : SCOOPER9403 205 10-8 09/17/2011 19:54:38 : pos2 : SCOOPER940
 Hoke Co 911:216 ROLLING PINES CT* * * * * **EMD* * * * * * * * 09/17/2011 19:37:54 : pos1 : LGRADY7274 ** EMD Recommended Dispatch ** Response Text: Alpha Dispa
 Hoke Co 911:265 BETTY`S TRL* * * * * **EMD* * * * * * * * 09/16/2011 19:14:15 : pos2 : SCOOPER9403 Cross streets: 580 EVERITT RD//DEAD END WILL BE IN THE VERY E
 Hoke Co 911:730 GALATIA CHURCH RD* * * * * **EMD* * * * * * * * 09/15/2011 13:51:27 : pos4 : mblack3325 Cross streets: 5365 ROCKFISH RD//CUMBERLAND CO LINE*
 
 */
 
 public class NCHokeCountyParser extends MsgParser {
   
   private static final Pattern MASTER = 
       Pattern.compile("Hoke Co 911:([^\\*]+)\\*(?: \\*)*\\*([^\\*]+)\\*(?: \\*)+ *\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d : (pos\\d+)(?: : [A-Za-z]+\\d+ (.*))?");
   
   public NCHokeCountyParser() {
     super("HOKE COUNTY", "NC");
   }
   
   @Override
   public String getFilter() {
     return "Hoke@hokecounty.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     parseAddress(match.group(1).trim(), data);
     data.strCall = match.group(2).trim();
     data.strChannel =  match.group(3).trim();
     String sExtra = getOptGroup(match.group(4));
     int pt = sExtra.indexOf("Cross streets:");
     if (pt >= 0) {
      sExtra = sExtra.substring(pt+14).trim();
       pt = sExtra.indexOf(" Cross");
       if (pt >= 0) sExtra = sExtra.substring(0,pt).trim();
       data.strCross = sExtra;
     }
     return true;
   }
 }
