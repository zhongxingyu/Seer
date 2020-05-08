 package net.anei.cadpage.parsers.NY;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Suffolk County NY (Port Jefferson Volunteer Ambulance)
 Contact: Jason Lazerus <jlazerus@pjvac.org>
 Sender: paging@pjvac.org
 
 UNKNOWN PROBLEM - STANDING, SITTING UP, MOVING OR TALKING code: 32-B-1 at: 3 STANDISH DR c/s: CHIPPENDALE DR d/t: 06/30 00:01:30
 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - NON SUICIDAL AND ALERT code: 25-A-1 at: 60 N COUNTRY RD c/s: PINE HILL RD d/t: 06/30 19:53:51
 SICK - NO PRIORITY SYMPTOMS (COMPLAINT CONDITIONS 2-11 NOT IDENTIFIED) code: 26-A-1 at: 11 BURNETT CT c/s: LOUDEN LP d/t: 06/30 11:27:10
 BURNS (SCALDS) / EXPLOSION - FIRE ALARM (UNKNOWN SITUATION) code: 7-A-2 at: 11 BRADDOCK CT c/s: STRATHMORE CT d/t: 06/28 19:37:00
 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - UNKNOWN STATUS / OTHER CODES NOT APPLICABLE code: 25-B-6V at: 99 CLIFF RD c/s: CROOKED OAK RD d/t: 06/26 07:27:08
 RESPIRATORY - DIFFICULTY SPEAKING BETWEEN BREATHS code: 6-D-2 at: 640 BELLE TERRE RD c/s: PRINCESS TREE CT d/t: 06/27 15:57:11
 BLEEDING / LACERATIONS - NOT ALERT code: 21-D-2 at: 70 N COUNTRY RD c/s: PINE HILL RD d/t: 06/24 15:58:51
 MOTOR VEHICLE ACCIDENT - NOT ALERT code: 29-D-5 at: CANAL RD c/s: OSBORNE AV d/t: 06/25 13:15:03
 
 Contact: Kevin Leedham <kl53rfd@gmail.com>
 Sender: ridge@rednmxcad.com
 30-D-2 TRAUMATIC INJURY (SPECIFIC)-NOT ALERT at: 6 REDBROOK LN c/s: RANDALL RD & FARMHOUSE LN d/t: 10/09 18:41
 29-B-1 MOTOR VEHICLE ACCIDENT-INJURIES at:  c/s: MEDFORD RD & MIDDLE COUNTRY RD d/t: 10/09 09:31
 1-C-5 ABDOMINAL PAINS-MALES WITH PAIN ABOVE NAVEL >=35 at: 44 RIDGE RD c/s: MIDDLE COUNTRY RD & SHARON CT d/t: 10/08 14:21
 17-A-1G FALLS-NOT DANGEROUS BODY AREA at: 15D GUILFORD CT c/s:  & BRIDGEWATER DR d/t: 10/10 09:53
 25-B-6 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICI-UNKNOWN STATUS / OTHER CODES NOT APPLICA at: 262 EDINBURGH DR c/s: BROWNFIELD DR & KINGSTON 
29-B-1 MOTOR VEHICLE ACCIDENT-INJURIES at:  c/s: SB WILLIAM FLOYD PKWY & MORICHES MIDDLE ISLAND RD d/t: 10/27 09:13
 
  */
 
 public class NYSuffolkCountyDParser extends FieldProgramParser {
   
   private static final Pattern CODE_PTN = Pattern.compile("^(\\d{1,2}-[A-Z]-\\d{1,2}[A-Z]?) ");
   
   public NYSuffolkCountyDParser() {
     super("SUFFOLK COUNTY","NY",
     "CALL! code:CODE? at:ADDR! c/s:X! d/t:SKIP");
   }
 
   @Override
   public String getFilter() {
     return "paging@pjvac.org,";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    
     if (!super.parseMsg(body, data)) return false;
    
     if (data.strCode.length() == 0) {
       Matcher match = CODE_PTN.matcher(data.strCall);
       if (match.find()) {
         data.strCode = match.group(1);
         data.strCall = data.strCall.substring(match.end()).trim();
       }
     }
    
    if (data.strAddress.length() == 0) {
      String sAddr = data.strCross;
      data.strCross = "";
      parseAddress(sAddr, data);
    }
     return true;
   }
 }
