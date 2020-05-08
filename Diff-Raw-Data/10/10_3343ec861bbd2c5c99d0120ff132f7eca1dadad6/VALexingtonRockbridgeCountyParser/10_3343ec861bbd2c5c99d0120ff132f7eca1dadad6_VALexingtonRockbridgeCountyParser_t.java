 package net.anei.cadpage.parsers.VA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Lexington & Rockbridge Counties VA
 Contact: Robert Jennette <robert.jennette@lexingtonfire.org>
 Sender: @c-msg.net
 
 [LCAD] Type: EMS-TRNSPORT Loc: 429 W NELSON ST Units: LXRS1, City: LEXINGTON 1/27/2011 2:10:08 PM SPORT. BRUSHY 1\nMessage Created 1/27/2011 2:10:00 PM
 [LCAD] Type: EMS-PAIN Loc: GREEN HILL APT Units: LXRS1, City: LEXINGTON 1/27/2011 1:22:32 AM **Nearest Address: 475 FOREST GROVE RD, LEXINGTON49 YOF HAVING A PANIC ATTACK & CAN`T FEEL LEGS.OPERATE BRUSHY 1\nMessage Created 1/27/2011 1:22:00 AM
 [LCAD] Type: EMS-FALL Loc: 431 W NELSON ST Units: CO09A,LR17, City: LEXINGTON 1/26/2011 5:39:37 PM **Nearest Address: 1305 ENFIELD RD, LEXINGTON38 YO MALE HIT A ROCK WALL GASHES SWELLING[01/26/2011 17:39:45 : pos1 : LHUCH]SLIDING WITH THE KIDS\nMessage Created 1/26/2011 5:45:00 PM
 [LCAD] Type: EMS-FALL Loc: 431 W NELSON ST Units: LXRS1, City: LEXINGTON 1/26/2011 5:39:37 PM **Nearest Address: 1305 ENFIELD RD, LEXINGTON38 YO MALE HIT A ROCK WALL GASHES SWELLING\nMessage Created 1/26/2011 5:39:00 PM
 [LCAD] Type: ACCIDENT MVC Loc: 60 WILLOW SPRINGS RD Units: LXRS2, City: LEXINGTON 1/26/2011 2:39:40 PM BLEEDING FROM HEAD AND ARM OTHER SIDE OF DAYS INN D2\nMessage Created 1/26/2011 2:39:00 PM
 [LCAD] Type: ACCIDENT MVC Loc: MAURY RIVER RD Units: CO09A, City: LEXINGTON 1/26/2011 2:37:32 PM **Nearest Address: 1305 ENFIELD RD, LEXINGTONSINGLE VEHICLE OVERTURNED, APPROX 2 MILES LONGS CAMPGROUND\nMessage Created 1/26/2011 2:37:00 PM
 [LCAD] Type: ACCIDENT MVC Loc: 81 SB 201 SB Units: B75,EM1,LXRS1,R73, City: FAIRFIELD 1/26/2011 2:23:09 PM **Nearest Address: NoneALTIMA GREY FEMALE BACK PAIN\nMessage Created 1/26/2011 2:34:00 PM
 [LCAD] Type: ACCIDENT MVC Loc: 60 W 1/2 W DAYS INN AND SHELL STATION Units: CO09A,LXRS1, City: LEXINGTON 1/26/2011 12:55:37 PM **Nearest Address: NoneSILVER VAN SLID OFF ROAD OVER EMBANKMENT ON IT HIT TREES UNKNOWN INJURIES\nMessage Created 1/26/2011 12:56:00 PM
 [LCAD] Type: EMS-HEM Loc: 160 KENDAL DR Units: LXRS1, City: LEXINGTON 1/26/2011 11:00:36 AM D FOR MALE RECTAL BLEED BORDEN CENTER RM106\nMessage Created 1/26/2011 11:00:00 AM
 
 Contact: Dave Wheeler <wheelerdh@gmail.com>
 S:LCAD M:[LCAD] Type: ACCIDENT MVC Loc: I-81 N BOUND Units: CO09A,LXRS1, City: LEXINGTON 8/14/2011 2:36:17 PM **Nearest Address: \n\n
 
Contact: Brent <brentaust18@gmail.com>
Sender: 411911
[CAD] Type: ACCIDENT MVC Loc: RD 11 CHURCH Units: FAIR1, City: FAIRFIELD 7/20/2012 3:16:56 AM 079.266657ALI Y Coordinate: 037.896663

  */
 
 public class VALexingtonRockbridgeCountyParser extends FieldProgramParser {
   
   private static final Pattern DATE_TIME_PTN = 
     Pattern.compile(" \\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{2}:\\d{2} [A-P]M ");
   private static final Pattern SQ_BRACKETS_PTN = Pattern.compile(" *\\[.*\\] *");
   
   public VALexingtonRockbridgeCountyParser() {
     super("", "VA",
            "Type:CALL! Loc:ADDR! Units:UNIT! City:CITY!");
   }
   
   @Override
   public String getFilter() {
    return "@c-msg.net,411911";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    if (!subject.contains("CAD")) return false;
     
     // Strip off data field from end of message
     int pt = body.indexOf("\nMessage");
     if (pt > 0) body = body.substring(0, pt).trim();
     
     Matcher match = DATE_TIME_PTN.matcher(body);
     if (!match.find()) return false;
     String extra = body.substring(match.end()).trim();
     body = body.substring(0,match.start());
     
     // And parse what is left
     if (!parseMsg(body, data)) return false;
     
     // OK, now lets see what to make of the extra data
     String address2 = null;
     if (extra.startsWith("**Nearest Address:")) {
       extra = extra.substring(18).trim();
       if (extra.startsWith("None")) {
         extra = extra.substring(4).trim();
       } else {
         Parser p = new Parser(extra);
         address2 = p.get(',');
         extra = p.get();
         if (extra.startsWith(data.strCity)) extra = extra.substring(data.strCity.length()).trim();
       }
     }
     
     if (address2 != null && !address2.equals("None") && address2.length() > 0) {
       if (checkAddress(data.strAddress) == 0) {
         data.strPlace = data.strAddress;
         data.strAddress = address2;
       }
     }
     
     extra = SQ_BRACKETS_PTN.matcher(extra).replaceAll(" ");
     data.strSupp = extra;
     return true;
   }
 
   @Override
   public String getProgram() {
     return super.getProgram() + " PLACE INFO";
   }
 }
