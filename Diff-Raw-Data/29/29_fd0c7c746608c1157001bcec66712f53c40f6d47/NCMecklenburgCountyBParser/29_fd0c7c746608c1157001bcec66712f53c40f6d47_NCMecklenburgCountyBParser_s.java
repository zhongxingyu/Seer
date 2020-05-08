 package net.anei.cadpage.parsers.NC;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Mecklenburg County, NC (alternate)
 Contact: Jeremy Shimberg <jshimberg@carolina.rr.com>,7042398801@vtext.com
 Sender: @minthillvfd.com
 
 6817 Wilson Grove Rd Charlie 10- Chest Pain Kuck Rd/Central Dr Mh112 Map - 353353/B9 06202011-392  [72]
 7119 Lancashire Dr Charlie 23- Overdose/Ingestion/Poison Ravenglass Ln/Cairnsmore Pl Mh212 Map - 353353/C9 06202011-374
 13538 Idlefield Ln Charlie 30- Traumatic injuries,specifcIdle Dr/Mills End Cr Mh212 Map - 381381/D8 06202011-335  [67]
 7500 Bondhaven Dr Charlie 42- Fire/Police support Kew Tr/Lanterntree Ln Mh212 Map - 353353/C6 06212011-239  [96]
 10545 Blair Rd Mint Hill Urgent Care Charlie 23- Overdose/Ingestion/Poison Truelight Church Rd/Connell Rd MHL8 Map - 35
 6700 Burkandt Rd 75M SYNCOPAL EPISODE WITH HX OAlpha 31- Unconsciousness/Fainting Wilson Woods Dr/Fieldwood Rd MHL8 Map
 
 Contact: John Stroup <j.stroup@northmeckrescue.org>
 Sender: paging@rcscom.com
 Subject:Incoming Message\n15503 N Old Statesville Rd North Meck Rescue Charlie 12- Convulsion/Seizure 60 Foot St/Unnamed St Nmr1 Map - 266266/
 
 Contact:  Soundwood <soundwood@msn.com>
 Contact: bill elwood <elwoodaudio@gmail.com>
 From: 334@c-msg.net
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 10210 Couloak Dr SUIT E Charlie 28- Stroke/CVA Mt Holly-Huntersvill Rd/Dunn Commons Pk COOR4 Map - 292292/J9 12302011-096
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 6700 Pleasant Oaks Cr Charlie 28- Stroke/CVA Songbird Ln/Catalina Ln COOR3 Map - 321321/E1 12282011-371
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 10611 Mount Holly Rd ifo residence Charlie 29- Traffic accident Chattaroy Dr/Latta Av COOF3 Map - 320320/D3 12272011-012
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 4322 Oakdale Rd 81yom/dehydration/silent approBravo-BLS COLD 26BC-Sick person Simpson Rd/E I-485\n(Con't) 2 of 2\nINNER HY COOR3 Map - 293293/E8 12312011-085(End)
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 2709 Kelly Rd Delta 17- Falls/Back injury(traumtc)Pleasant Grove Rd/Weston Woods Ln COOR2 Map - 293293/D1012302011-271
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 10210 Couloak Dr SUIT E Charlie 28- Stroke/CVA Mt Holly-Huntersvill Rd/Dunn Commons Pk COOR4 Map - 292292/J9 12302011-096
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 6700 Pleasant Oaks Cr Charlie 28- Stroke/CVA Songbird Ln/Catalina Ln COOR3 Map - 321321/E1 12282011-371
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 10611 Mount Holly Rd ifo residence Charlie 29- Traffic accident Chattaroy Dr/Latta Av COOF3 Map - 320320/D3 12272011-012
 FRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 2721 Nance Cove Rd Charlie 13- Diabetic problems Millwood Cr/Tall Meadow Rd COOR1 Map - 292292/H6 01052012-163
 1 of 2\nFRM:rc.334@c-msg.net\nSUBJ:cCAD\nMSG:[!] 10607 Bur Oak Dr Fire - Emergency 52C-Alarm-Carbon Monoxide Falling Stream Dr/Double Cedar Dr\n(Con't) 2 of 2\nCOOF3 Map - 292292/E9 01062012-101(End)
 

 
 */
 
 public class NCMecklenburgCountyBParser extends SmartAddressParser {
   
   private static final Set<String> PRI_VALUES = new HashSet<String>(Arrays.asList(new String[]{"Charlie", "Delta", "Fire - Emergency"}));
  private static final Pattern CODE_PTN = Pattern.compile(" (\\d{2}[A-Z]{0,2}-)");
   private static final Pattern UNIT_PTN = Pattern.compile("\\b[A-Z][A-Za-z]{1,4}\\d{1,3}\\b");
   
   
   public NCMecklenburgCountyBParser() {
     super("MECKLENBURG COUNTY", "NC");
   }
   
   @Override
   public String getFilter() {
    return "@minthillvfd.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // Rule out A variant message
     if (body.length() >= 100) {
       String sPriCode = substring(body,70,100);
       if (PRI_VALUES.contains(sPriCode)) return false;
     }
 
     Matcher match = CODE_PTN.matcher(body);
     if (!match.find()) return false;
     
     data.strCode = match.group(1);
     String part1 = body.substring(0,match.start()).trim();
     String part2 = body.substring(match.end()).trim();
     
     Parser p = new Parser(part2);
     part2 = p.get("Map -");
     data.strMap = p.get(' ');
     data.strCallId = p.get(' ');
     
     match = UNIT_PTN.matcher(part2);
     if (!match.find()) return false;
     data.strUnit = part2.substring(match.start()).trim();
     part2 = part2.substring(0,match.start()).trim();
     parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, part2, data);
     data.strCross = data.strAddress;
     data.strAddress = "";
     
     int pt = part1.lastIndexOf(' ');
     if (pt >= 0) {
       data.strPriority = part1.substring(pt+1, pt+2);
       part1 = part1.substring(0,pt).trim();
     }
     parseAddress(StartType.START_ADDR, part1, data);
    data.strSupp = getLeft();
     
     return true;
   }
 
 }
