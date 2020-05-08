 package net.anei.cadpage.parsers.DE;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 New Castle County, DE
 Contact: Jeff Hill <jeff.hill25@gmail.com>
 Sender: rc.322@c-msg.net
 
 (25CAD) [FB] F00 19:58 1 - T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:755 MONTCLAIR DR #2 ,PF *NAAMANS APTS - X:btwn NAAMANS RD ~ SOCIETY DR DESC: smoke fromneighbors
 (25CAD) [FB] F00 12:11 1 - T:69D7 (L3*CHIMNEY FIRE) L:2007 FERNDALE DR ,X1 *WESTWOOD MANOR - X:btwn FORREST RD ~ VEALE RD DESC: chimney.?!?!
  [FB] F00 03:14 1 - T:69D6 (L3*RESIDENTIAL FIRE) L:108 CARLIE RD ,NT *LYNNFIELD - X:btwn HOMEWOOD RD ~ RIDGELAND RD
  [FB] F00 08:06 1 - T:55B2 (L1*ELECTRICAL HAZARD-WIRES DWN) L:ELGIN LA~SHIPLEY RD ,QB *OAK LANE MANOR - X: DESC: WIRES DOWN.?!?!
 (25CAD) [FB] F00 01:38 1 - T:52C3S (L1*COMMERCIAL FIRE ALARM) L:4130 CONCORD PK ,HU06 -- 6TH AVE ELECTRONIC - X:btwn SILVERSIDE RD ~ PASSMORE RD DESC: fire alar
 (25CAD) [FB] F00 08:35 1 - T:69D4O (L3*COMMERCIAL STRUC FIRE W~HZ) L:4238 PHILADELPHIA PK ,HU06 -- CLAYMONT STEEL PLA - LI:PLATE MILL - X:btwn ALCOTT AV ~ NAAMANS RD
 (25CAD) [FB] F00 11:48 1 - T:52C3G (L1*COMMERCIAL FIRE ALARM) L:1801 AUGUSTINE CUT OFF ,HU06 -- ACCENTURE CONSULTI - X:btwn W EIGHTEENTH ST ~ STONE HILL RD DESC
 (25CAD) [FB] F00 22:27 1 - T:52C1G (L1*FIRE ALARM~GENERAL) L:4641 WELDIN RD ,XF -- MARY CAMPBELL CNTR *WELDIN WOODS - X:btwn JACOB WELDIN PL ~ WELDIN CI DESC: F
 
 15:50T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:401 CAMPBELL RD ,KC GREENVILLE - X:KENNETT PK ~ MINKER CT : afa.?!?!
 
 23:34T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:100 ORCHARD LA #1 ,LU *HOLLY OAK - X:WALNUT ST ~ RIDGE RD
 17:33T:69D5 (L3*MULTI-RESIDENTIAL FIRE) L:301 HARBOR DR #10 ,ZY *WATER VIEW COURT APTS - X: high xst: GRUBBS LANDING RD : GREASE FIRE.
 21:43T:M29B1 (MVC) L:CONCORD PK~MT LEBANON RD ,VE *TALLEYVILLE - :—àDSC:MVC—! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y
 15:50T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:401 CAMPBELL RD ,KC *GREENVILLE - X:KENNETT PK ~ MINKER CT : afa.
 13:58T:52B1G (L1*RES FIRE ALARM~GENERAL ALAR) L:102 PONDS LA ,PNGR *PONDS OF GREENVILLE - X:MONTCHANIN RD ~ DEAD-END : afa
 22:31T:M29D5 (MVC--NOT ALERT) L:CONCORD PK~INTERSTATE 95 ,HU06 - LI:FROM 202 ONTO 95 SOUTH - :—àDSC:MVC—! PAT:1 SEX:Male AGE:25Years CON:Y BRE:Y
 09:02T:69D6 (L3*RESIDENTIAL FIRE) L:15 GLENROCK DR ,R5 *RADNOR GREEN - X:BENNING RD ~ PENNSYLVANIA AV : fire
 19:06T:52C3G (L1*COMMERCIAL FIRE ALARM) L:1001 ROCKLAND RD ,HU06 -- DUPONT CC GOLF COU - LI:EQUIPMENT CENTER - X:BLACK GATES RD ~ COUNTRY CLUB DR
 03:51T:55C1 (L1*ELECTRICAL HAZ W~NEAR WATER) L:112 BUCK RD ,HU07 - LI:IN FRONT OF ABV - X:ARDLEIGH DR ~ GREENOCK DR : transformer
 05:26T:71B1 (L1*VEH-FIRE) L:CONCORD PK~PIERCE RD ,GD *DEERHURST - X: : vehicle on fire
 21:42T:M29D2M (PEDESTRIAN STRUCK) L:5350 BRANDYWINE PY ,BRTC -- CHRISTMAS TREE SHO high xst: NAAMANS RD *BRANDYWINE TOWNE CENTER - :—àDSC:SUBJ STRUCK—! PAT:1 SEX:Female AGE:30Years CON:Y BRE:Y
 
 Contact: Rebecca Deem <rdeem328@gmail.com>
 Sender: rc.81@c-msg.net
 Subject:24CAD\n[eFB] F00 21:22 1 - T:M10D2 (CHEST PAINS-DIFF SPEAKING BETW) L:1000 SNOWY EGRET LA ,AGC2 btwn POLE BRIDGE RD ~ BOBWHITE \nCT *AUGUSTINE CREEK II - DESC:??DSC:CHEST PAIN?! PAT:1 SEX:Male AGE:52Years CON:Y BRE:Y\n
 Subject:24CAD\n[eFB] F00 13:27 1 - T:M24B1 (EMERGENCY MATERNITY) L:106 N SIXTH ST ,QG btwn MAIN ST ~ HIGH ST *ODESSA - DESC:??DSC:water\n
 
 Contact: Mike Warner <MWarner@bdabel.com>
 Sender: rc.10@c-msg.net
 (13CAD) [eFB] F00 18:39 1 - T:M24B1 (EMERGENCY MATERNITY) L:16 DEL HAVEN CT ,LU btwn GOVERNOR PRINTZ BL ~ WALNUT ST *HOLLY OAK - DESC:??DSC:MATERNITY 25YOF ~~~ ?! PAT:1 SEX:Female AGE:25Years CON:Y BRE:Y
 
Contact: Matthew Comegys <mcomegys2832@gmail.com>
Sender: rc.302@c-msg.net
28CAD / [eFB] F00 17:23 1 - T:M29D5 (MVC--NOT ALERT) L:113 S DUPONT HY ,4Q -- LONE STAR STEAKHOU btwn CHRISTIANA RD ~ FIFTH AV * 28CAD / HARES CORNER - DESC:\n\n

  */
 
 
 public class DENewCastleCountyAParser extends FieldProgramParser {
   
   private static final Pattern[] MARKERS = new Pattern[]{
    Pattern.compile("^(?:([0-9A-Z]+) / \\[\\w*FB\\] )?F00 \\d\\d:\\d\\d 1 - (?=T:)"),
     Pattern.compile("^\\d\\d:\\d\\d(?=T:)")
   };
   
   private static final Pattern NAKED_BTWN = Pattern.compile("(?<!X: ?)\\bbtwn\\b");
   
   public DENewCastleCountyAParser() {
     super("NEW CASTLE COUNTY", "DE",
            "T:CALL! L:ADDR! X:X DESC:INFO");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     boolean found = false;
     for (Pattern ptn : MARKERS) {
       Matcher match = ptn.matcher(body);
       if (match.find()) {
         found = true;
        if (match.groupCount() >= 1) data.strSource = getOptGroup(match.group(1));
         body = body.substring(match.end());
         break;
       }
     }
     if (!found) return false;
     
    // If we stripped the source from the front of the field (terminated by " / ")
    // Search for and remove that sequence if it appears anywhere else in the 
    // text body, assuming it was a lead for a second message.
    if (data.strSource.length() > 0) {
      body = body.replace(data.strSource + " / ", "");
    }
    
     if (subject.length() > 0) {
       String[] subjects = subject.split("\\|");
       if (! subjects[subjects.length-1].endsWith("FB")) return false;
       if (subjects.length == 2) data.strSource = subjects[0];
     }
     
     body = body.replace('\n', ' ');
     body = body.replaceAll("  +", " ");
     body = body.replace(" :", " DESC:");
     body = NAKED_BTWN.matcher(body).replaceFirst("X:btwn");
     return super.parseMsg(body, data);
   }
   
   private static final Pattern LANE_PTN = Pattern.compile("\\bLA\\b");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String fld, Data data) {
       Parser p = new Parser(fld);
       String sAddress = p.get(',').replaceAll("~","&");
       sAddress = LANE_PTN.matcher(sAddress).replaceAll("LN");
       parseAddress(sAddress, data);
       p.get(' ');
       fld = p.get();
       parsePlace(fld, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " CITY PLACE";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String fld, Data data) {
       Parser p = new Parser(fld);
       super.parse(p.get(" *"), data);
       parsePlace(p.get(), data);
     }
   }
 
   private void parsePlace(String fld, Data data) {
     if (fld.startsWith("*")) {
       fld = fld.substring(1).trim();
     } else if (fld.startsWith("--")) {
       fld = fld.substring(2).trim();
     }
     if (fld.endsWith("-")) {
       fld = fld.substring(0,fld.length()-1).trim();
     }
     if (fld.length() > 0) {
       if (CITY_SET.contains(fld.toUpperCase())) {
         data.strCity = fld;
       } else {
         data.strPlace = fld;
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
 
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private static Set<String> CITY_SET = new HashSet<String>(Arrays.asList(new String[]{
       "ARDENCROFT",
       "ARDENTOWN",
       "BELLEFONTE",
       "CLAYTON",
       "DELAWARE CITY",
       "ELSMERE",
       "MIDDLETOWN",
       "NEW CASTLE",
       "NEWARK",
       "NEWPORT",
       "ODESSA",
       "SMYRNA",
       "TOWNSEND",
       "WILMINGTON",
       "BEAR",
       "BROOKSIDE",
       "CLAYMONT",
       "COLLINS PARK",
       "CHRISTIANA",
       "EDGEMOOR",
       "GLASGOW",
       "GREENVILLE",
       "HOCKESSIN",
       "HOLLY OAK",
       "MARSHALLTON",
       "MINQUADALE",
       "MONTCHANIN",
       "NORTH STAR",
       "OGLETOWN",
       "PIKE CREEK",
       "ROCKLAND",
       "ST. GEORGES",
       "STANTON",
       "WILMINGTON MANOR",
       "WINTERTHUR",
       "WINTERSET"
   }));
 }
 
 
