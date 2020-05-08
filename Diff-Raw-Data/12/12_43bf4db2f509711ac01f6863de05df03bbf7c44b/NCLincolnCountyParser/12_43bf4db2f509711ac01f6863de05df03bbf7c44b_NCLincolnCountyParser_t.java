 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Lincoln County, NC
 Contact: Jason Reed <jreed@denverfd.com>
 Sender: cad@lincolnsheriff.org
 System: OSSI CAD
 
 4567:CAD:KFD-26D01-26-D-1 SICK PERSON-9999 PALMETTO LN-PANORAMIC LN-TODD LN-[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: You are responding to a patient who is sick (or has a current medical condition). The patient is a <REMOVED PT DATA>, who is conscious and
 4568:CAD:KFD3-06D04-6-D-4 BREATHING PROBLEMS-8888 N NC 16 HWY-DELLING COMMUNICATIONS-CHARTER LN-HAGERS HOLLOW DR-[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: <REMOVED PT DATA>, Conscious, Breathing. Breathing Problems. Clammy. 1.He is completely alert (responding appropriately). 2.He does not have
 4569:CAD:KFD-06D02-6-D-2 BREATHING PROBLEMS-7777 DICK WILSON RD-CAMPGROUND RD-[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: <REMOVED PT DATA>, Conscious, Breathing. Breathing Problems. DIFFICULTY SPEAKING BETWEEN BREATHS. Caller Statement: DIFFICULTY BREATHING AND
 4570:CAD:KLF-201101460-SMOKE INVESTIGATION-N LITTLE EGYPT RD/NC 73 HWY-near the ball field [03/22/11 10:13:09 DISPATCHER] blk smoke near track unkn source [03/22/11 10:12:22]
 4571:CAD:KFD-201101410-67D02-OUTSIDE FIRE-1111 SAILVIEW DR-CREPE RIDGE DR-CHAPEL CREEK DR-[Fire Priority Info] RESPONSE: Delta RESPONDER SCRIPT: Outside Fire. Brush/grass fire.Caller Statement: burning brush . 2.The caller is safe and out of danger. 3.Everyone else is safe and out of
 1221:CAD:DFD-201101274-ASSIST MOTORIST AGENCY OFFICER-5555 KING WILKINSON RD-CALVARY BAPTIST CHURCH-MOUNTAINBROOK LN-DELLING DOWNS DR-Event spawned from LOG CALL FOR RECORDS. [03/10/2011 20:03:33 SAM] TREE ACROSS THE ROAD [03/10/11 20:03:06 SAM]
 1873:CAD:JLFD-201101315-52C03U-ALARMS FOR FIRE-888 OPTIMIST CLUB RD-WOODMONT CARE CENTER-FORNEY CREEK PKWY-WOODS LN-[Fire Priority Info] RESPONSE: Charlie RESPONDER SCRIPT: Alarms. COMMERCIAL structure (Unknown).Caller Statement: FIRE ALARM . 1.The caller is an alarm
 
 Contact: Matt Bandieramonte <mbandieramonte@eastlincolnfd.org>
 2647:CAD:AFD-201102514-69D09- OUTSIDE FIRE-1394 MICK LN-ORCHARD RD-[Fire Priority Info] Key Questions Complete RESPONSE: Delta RESPONDER SCRIPT: 6.A single-level structure is involved. 7.No one is reported to be injured. [05/03/11 20:54:48 SLINGERFELT] [Fire P
 2646:CAD:ELF-31D03-31-D-3 UNCONSCIOUS/FAINTING-6471 NC 73 HWY-EAST LINCOLN HIGH SI-320-S INGLESIDE FARM RD-N LITTLE EGYPT RD-[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: You are responding to a patient who is unconscious (or has fainted). The patient is a 17-year-old female, who is conscious and breathing. No
 2642:CAD:ELF-201102489-29B01-29-B-1 TRAFFIC ACCIDENT-N NC 16 HWY/NC 73 HWY-[Medical Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Age unknown, Female, Conscious, Breathing. Traffic / Transportation Incidents. Injuries. Caller Statement: 2 veh . 1.The incident involves m
 2616:CAD:ELF-201102226-107B01-ASSIST MOTORIST AGENCY OFFICER-5514 NC 73 HWY-SCHRONCE RD-INVERLOCHY RD-JUST BEFORE EAST LINCOLN HIGH SCHOOL [04/19/11 05:22:28 CGRANTLAND] CALLER IS EXTREMELY UPSET [04/19/11 05:21:48 CGRANTLAND] RIGHT HAND SIDE OF RD COMING FROM LINCOLNTON [04/19/11 05:21:39 CGRANT
 
 Contact: J Owens, Andromeda Pet Services <james@dogscantdrive.com>
 915:CAD:ADMN-201103837-29D02m-29-D-2 TRAFFIC ACCIDENT-3068 CAT SQUARE RD-HENRY RD-PALM TREE DR-669 IS OUT WITH MALE AND ROUTINE RESPONSE RAN OVER BY FARM EQU
 
 Contact: Kevin Yount <unionfd622@yahoo.com>
 Sender: 93001043
 455:CAD:UFD-1100005100-29B04-29-B-4 TRAFFIC ACCIDENT-1513 ALF HOOVER RD-REEPSVILLE BAPTIST CHURCH-J RHYNE REEP RD-REEPSVILLE RD-[Medical Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Age unknown, Gender unknown, Consciousness unknown, Breathing status unknown. Traffic / Transportation Incidents. Unknown status/Other cod
 
 Contact: Lisa Dellinger <lisadellinger2@gmail.com>
 Sender: CAD@lincolne911.org
 1112:CAD:FYI: -EMSC-201201013-TRANSFER-433 MCALISTER RD-CMC LINCOLN (NEW)-BEECHNUT ST-MEDICAL CENTER DR-going to brian center/ no special :1of2\n\n\nDISCLAIMER: Pu
 
 Contact: support@active911.com
 [] 1:CAD:HCF-VEHICLE ACC PD ONLY-7046787685-AT&T MOBILITY-SHOAL RD/LAMA LN-VEH IS PARTLY IN RDWY [05/25/12 20:19:16 CHARKEY]\n
 [] 4:CAD:N3F-1200000772-OUTSIDE FIRE-BUFFALO SHOALS RD/BLAIR RD-Event spawned from LARCENY / THEFT. [05/27/2012 18:18:31 CLAFFERTY] {L182} x1 detained [05/27/12 18:09:22 SPITTS] gave to county [05/27/12 18:03:27 SPITTS] {CITY15} red chevy tk right on buffalo\n
 
Contact: active911.com
Agency name: Boger City Fire Department Location: Lincolnton, NC 
Sender: CAD@do-not-reply-lincolne911.org
CAD:BCF-ALLERGIES ALLERGIC REACTION-410 MCALISTER RD-POSSIBLE ALLERGIC REACTION TO BEE STING. TK40 ADV STANDBY ON EMS [07/02/2012 21:07:31 TRAPER] -BEECHNUT ST-MEDICAL CENTER DR
CAD:BCF-17D04-17-D-4 FALLS-441 BUFFALO SHOALS RD-HIGHLAND DR-INDIAN TR-[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: 61 year old, Female, Conscious, Breathing. Falls. Chest or Neck injury (with difficulty breathing). 1.This happened now (less than 6hr
CAD:N3F-1200000490-52C03G-ALARMS FOR FIRE-621 LINCOLN COUNTY PARKWAY EXT-CRATE AND BARREL-FINGER MILL RD-[Fire Priority Info] RESPONSE: Charlie RESPONDER SCRIPT: ALARMS. COMMERCIAL/INDUSTRIAL BUILDING (GENERAL/FIRE).CALLER STATEMENT: FIRE ALARM. 1.THE CALLER IS AN ALARM MONITORING COMPANY. 2.IT I

 */
 
 public class NCLincolnCountyParser extends DispatchOSSIParser {
   
   private static Pattern CODE_PTN = Pattern.compile("\\b\\d{1,2}-[A-Z]-\\d\\b");
   
   public NCLincolnCountyParser() {
     super("LINCOLN COUNTY", "NC",
            "ID: FYI? SRC ID? CODE? CALL ADDR! ( X X? | PLACE X X? | ) INFO+");
   }
   
   @Override
   public String getFilter() {
     return "cad@do-not-reply-lincolne911.org,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
    // The OSSI parser either expects a leading ID field, or does not expect one.  It can't handle our case
    // where it sometimes is there and sometimes isn't.  We fix that by adding a dummy ID if there isn't one.
    if (body.startsWith("CAD:")) body = "0:" + body;
    
     // For medical call, the code is duplicated and the one in the call
     // description has dashes, which we are going to use a field separators
     // Easy solution is to just get rid of it.
     body = CODE_PTN.matcher(body).replaceFirst("");
     
     // Change dashes to regular semicolon field separators
     body = body.replace('-', ';');
     if (! super.parseMsg(body, data)) return false;
     return true;
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{9,}"));
     }
   }
   
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(Pattern.compile("\\d{2,3}[A-Z]\\d{2}[A-Za-z]?"));
     }
   }
   
   
   @Override
   protected Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("CODE")) return new MyCodeField();
     return super.getField(name);
   }
 }
