 package net.anei.cadpage.parsers.SC;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /*
 Greenville County, SC
 Contact: Jrrichards1775@yahoo.com,8644440080@vtext.com
 
 CAD:ACKNOWLEDGE CALL;105 CRIMSON GLORY WAY; TRAV;(S) (N)19V
 CAD:FALLS;105 CRIMSON GLORY WAY;TRAV;MIRANDY CT;TINEKE WAY;(S) (N)19V;NG1
 CAD:FIRE ALARM;8 ANDERS RD;GRVL;OLD MCELHANEY RD;(S) (N)24F;Kristy- AOA: sprinkler trip [04/26/11 12:28:12 THILL] ;NG1
 CAD:ACKNOWLEDGE CALL;8 ANDERS RD; GRVL;(S) (N)24F
 CAD:FIRE ALARM;32 HUNT ST;TRAV;ELIZABETH LN;ELIZABETH LN;(S) (N)17O;NAME: CHARLA SMITH, [04/26/11 13:34:03 THILL] SMOKE ALARM GOING OFF [04/26/11 13:33:49
 CAD:ACKNOWLEDGE CALL;32 HUNT ST; TRAV;(S) (N)17O
 CAD:ACKNOWLEDGE CALL;32 HUNT ST; TRAV;(S) (N)17O
 CAD:FALLS;124 JAMISON RD;TRAV;FARMINGTON TRL;WEBB CREEK CT;(S) (N)18F;NG1
 
 Contact: Kenneth Gosnell <ki4rdl@gmail.com>
 Sender: 8643850725
 CAD:DIABETIC PROBLEM;518 GOFORTH RD;LAND;BUTTER ST;BUTTER ST;(S) (N)4H;GM1
 CAD:ABDOMINAL PAIN;178 DUFFS MOUNTAIN RD;MARI;DIST: 63.24 FT (S) (N)7H;GM1
 
 Contact: Tare Kennedy <tare@owen-kennedy.com>
 Sender: 8643850725
 CAD:FALLS;70 LATIMER RIDGE RD;HONE;CORN RD;(S) (N)86O;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: 76 year old, Male, Conscious, Breathing. Falls.
 CAD:STROKE OR TIA;271 GREY RD;PELZ;DIST: 280.66 ft (S) (N)80V;243-3091 [01/04/12 06:30:45 CCHILDRESS] [Medical Priority Info] RESPONSE: Charlie RESPONDER SCRIPT
 CAD:OUTSIDE FIRE;419 CEDAR FALLS RD;FOUN;SWEET WATER RD;DUNKLIN BRIDGE RD;DIST: 86.71 ft (S) (N)80R;DK1
 CAD:STRUCTURE FIRE;10612 AUGUSTA RD;BELT;BOYS HOME OF THE SOUTH;DAVENTON RD;KERMIT WATSON BLVD;DIST: 715.97 ft (S) (N)86A;DK1
 
 Contact: support@active911.com
 [] CAD:ELECTRICAL HAZARD;16 MOUNTAIN VISTA RD;TAYL;GROCE MEADOW RD;(S) (N)20J;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:FIRE ALARM;3000 E GAP CREEK RD;GRER;GREER HIGH SCHOOL;N HOWELL RD;OLD WAGON RD;(S) (N)22Q;general fire alarm [04/06/12 01:25:05 WFULLER] ;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:FIRE ALARM;4221 N 14 HWY;GRER;SKYLAND ELEMENTARY SCHOOL;SKYLAND DR;SKYLAND SCHOOL RD;(S) (N)15I;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:FIRE ALARM;3192 N MCELHANEY RD;GRER;LAKE CUNNINGHAM RD;N JAMES RD;(S) (N)21N;HALLWAY [04/06/12 18:49:53 TVINSON] HALLWAY SMOKE DETECTOR [04/06/12 18:50:09 TVINSON] ;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:CANCEL;3192 N MCELHANEY RD; GRER;(S) (N)21N\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:FALLS;153 BROOKE LEE CIR;TAYL;HALBROOK DR;PEARLE BROOK LN;DIST: 52.83 FT (S) (N)20I;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:MEDTRANS ON STANDBY;153 BROOKE LEE CIR; TAYL;DIST: 52.83 FT (S) (N)20I\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:MEDTRANS LAUNCHED;153 BROOKE LEE CIR; TAYL;DIST: 52.83 FT (S) (N)20I\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:STRUCTURE FIRE;2 BLUEGRASS CT;TAYL;SPLITRAIL DR;DIST: 60.52 FT (S) (N)14V;LC1\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:WORKING FIRE;2 BLUEGRASS CT; TAYL;DIST: 60.52 FT (S) (N)14V\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:LAW ENFORCEMENT NOTIFIED;2 BLUEGRASS CT; TAYL;DIST: 60.52 FT (S) (N)14V\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 [] CAD:FIRE UNDER CONTROL;2 BLUEGRASS CT; TAYL;DIST: 60.52 FT (S) (N)14V\nCONFIDENTIALITY NOTICE: This e-mail and any files transmitted with it are confidential and may contain information which is legally privileged or otherwise exempt from disclosure. They are intended solely for the use of the individual or entity to whom this e-mail is addressed. If you are not one of the named recipients or otherwise have reason to believe that you have received this message in error, please immediately notify the sender and delete this message immediately from your computer. Any other use, retention, dissemination, forwarding, printing, or copying of this e-mail is strictly prohibited.\n
 
  */
 
 
 public class SCGreenvilleCountyParser extends DispatchOSSIParser {
   
   
   public SCGreenvilleCountyParser() {
     super(CITY_CODES, "GREENVILLE COUNTY", "SC",
            "CALL ADDR CITY X/Z+? MAP NAME:NAME? INFO+");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     return super.parseMsg(body, data);
   }
 
   private class MyMapField extends MapField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       int pt = field.indexOf("(S) (N)");
       if (pt < 0) return false;
       data.strSupp = field.substring(0,pt).trim();
       data.strMap = field.substring(pt+7).trim();
       return true;
     }
     
     @Override
     public String getFieldNames() {
       return "INFO MAP";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("MAP")) return new MyMapField();
     return super.getField(name);
   }
   
   private static Properties CITY_CODES = buildCodeTable(new String[]{
       "BELT", "BELTON",
       "FOUN", "FOUNTAIN INN",
       "GRER", "GREER",
       "GRVL", "GREENVILLE",
       "HONE", "HONES PATH",
       "LAND", "LANDRUM",
       "MARI", "MARIETTA",
       "PELZ", "PELZER",
       "TRAV", "TRAVELERS REST",
       "TAYL", "TAYLORS"
   });
 }
