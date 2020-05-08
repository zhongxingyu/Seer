 package net.anei.cadpage.parsers.NJ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Sussex County, NJ (Bryam TWP FD)  (Sparta Dispatch - not entire county but 8 departments)
 Contact:Todd Rudloff <btfd43capt2@gmail.com>
 
 (I-B2011-004928) ALARMF @ 172 LACKAWANNA DRIVE  , BYRAM - PROTECTIVE SERVICES (800-633-2677) OPERATOR 1663. GENERAL FIRE.
 [I-B2010-009210]  ALARMF @ 7 WATERLOO ROAD  , BYRAM - ALL AMERICAN CRAFT. ALARMS PLUS (800-932-3822) OPERATOR 1224. UTILITY ROOM
 [I-B2011-004688]  FIRE @ 33 SLEEPY HOLLOW ROAD  , BYRAM - CALLER REPORTS HIS LIVING ROOM IS SMOKING.
 (I-B2011-003217) FIREPD @ 4 MOUNTAIN AVENUE  , BYRAM - CALLER REPORTS THICK BLACK SMOKE COMING FROM THE RESIDENCE.
 [I-B2011-000701]  FIRE @ 228 TOMAHAWK TRAIL  , BYRAM - STRUCTURE FIRE
 
 Contact: Michael Rafferty <mpraff15@yahoo.com>
 KBROWN@andpd (I-2011-000118) MVA-F @ DECKER POND ROAD/SUNSET DRIVE  , GREEN TWP - CAR VS GUARDRAIL - MINOR INJURIES.
 KBROWN@andpd (I-2011-000121) MVA-F @ HIBLER ROAD/WINTERMUTE ROAD  , GREEN TWP - CALLER REPORTING MOTORCYCLE MVA -- CALLER STATES APPEARS NO L
 dcrater@andpd (I-2011-000117) ASSIST-F @  OUT OF TOWN  ,  - 44 KISHPAUGH RD FULLY INVOLVED STRUCTURE
 kwilson@andpd (I-2011-000099) BURN-F @ 1 SCENIC DRIVE  , GREEN TWP - permit: A-3251 burning all day ** DO NOT RESPOND**
 jcasella@andpd (I-2011-000122) MVA-F @ 21 SUTTON ROAD  , GREEN TWP - CAR VS TREE
 jragsdale@andpd (I-2011-000108) TRANSF @ 71 WOLFS CORNER ROAD  , GREEN TWP - MUNICIPAL BLDG CALLED- HAS PASSERBY ADV OF TRANSFORMER FIRE hasEML = false;
 
 Contact: mpraff15 <mpraff15@gmail.com>
 Sender: 6245
 kbrown@andpd (I-2011-000155) PUMP @ 6 RAMSEY COURT  , FRELINGHUYSEN - WARREN COUNTY COMMUNICATIONS REQUESTING A PUMP OUT FOR THE FOOT OF WATER IN BASEMENT.
jcasella@andpd (I-2011-000194) MVA-F @  ROUTE 94  , GREEN TWP - \nin front of wilbur's country storE
 
  */
 
 
 public class NJSussexCountyParser extends SmartAddressParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("I-[A-Z]?\\d{4}-\\d{6}");
   private static final Pattern MASTER_PTN = 
     Pattern.compile("([-A-Z ]+) @ ([^,]+)  , ([^-]*) - (.*)"); 
   
   public NJSussexCountyParser() {
     super("SUSSEX COUNTY", "NJ");
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (! SUBJECT_PTN.matcher(subject).matches()) return false;
     data.strCallId = subject;
    body = body.replace('\n', ' ');
     Matcher match = MASTER_PTN.matcher(body);
     if (!match.matches()) return false;
     data.strCall = match.group(1).trim();
     parseAddress(match.group(2).trim(), data);
     data.strCity = match.group(3).trim();
     String sInfo = match.group(4).trim();
     if (data.strAddress.equals("OUT OF TOWN")) {
       data.strAddress = "";
       parseAddress(StartType.START_ADDR, sInfo, data);
       data.strSupp = getLeft();
       data.defCity = "";
       data.defState = "";
     } else {
       data.strSupp = sInfo;
     }
     return true;
   }
 }
