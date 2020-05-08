 package net.anei.cadpage.parsers.NY;
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 UNKNOWN PROBLEM - STANDING, SITTING UP, MOVING OR TALKING code: 32-B-1 at: 3 STANDISH DR c/s: CHIPPENDALE DR d/t: 06/30 00:01:30
 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - NON SUICIDAL AND ALERT code: 25-A-1 at: 60 N COUNTRY RD c/s: PINE HILL RD d/t: 06/30 19:53:51
 SICK - NO PRIORITY SYMPTOMS (COMPLAINT CONDITIONS 2-11 NOT IDENTIFIED) code: 26-A-1 at: 11 BURNETT CT c/s: LOUDEN LP d/t: 06/30 11:27:10
 BURNS (SCALDS) / EXPLOSION - FIRE ALARM (UNKNOWN SITUATION) code: 7-A-2 at: 11 BRADDOCK CT c/s: STRATHMORE CT d/t: 06/28 19:37:00
 PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE - UNKNOWN STATUS / OTHER CODES NOT APPLICABLE code: 25-B-6V at: 99 CLIFF RD c/s: CROOKED OAK RD d/t: 06/26 07:27:08
 RESPIRATORY - DIFFICULTY SPEAKING BETWEEN BREATHS code: 6-D-2 at: 640 BELLE TERRE RD c/s: PRINCESS TREE CT d/t: 06/27 15:57:11
 BLEEDING / LACERATIONS - NOT ALERT code: 21-D-2 at: 70 N COUNTRY RD c/s: PINE HILL RD d/t: 06/24 15:58:51
 MOTOR VEHICLE ACCIDENT - NOT ALERT code: 29-D-5 at: CANAL RD c/s: OSBORNE AV d/t: 06/25 13:15:03
  */
 
 public class NYSuffolkCountyDParser extends FieldProgramParser {
   
   public NYSuffolkCountyDParser() {
     super("SUFFOLK COUNTY","NY",
    "INFO code:CALL! at:ADDR/SP! cs:X! dt:INFO+");
   }
 
   @Override
   public String getFilter() {
     return "paging@pjvac.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    body = body.replace("c/s", "cs");
    body = body.replace("d/t", "dt");
    return true;
   }
 }
