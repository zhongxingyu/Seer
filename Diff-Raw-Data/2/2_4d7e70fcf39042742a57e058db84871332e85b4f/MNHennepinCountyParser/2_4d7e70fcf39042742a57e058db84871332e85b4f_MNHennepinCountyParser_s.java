 package net.anei.cadpage.parsers.MN;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /**
  * This class parses messages from some as yet unidentified CAD software vendor
  **/
 
 /*
 Hennepin county, MN
 Contact: basarajoseph@gmail.com <basarajoseph@gmail.com>
 Sender: sheriff@cadpaging.co.hennepin.mm.us
 
 NAME-KENDRA;  LOC-4001 STINSON BLVD NE [#404;  EVTYPE-STROKE;  COMMENTS-POSSIBLE STROKE --- USE MAIN ENTRANCE TO THE 4TH FL
 NAME-BRINATTE,HELEN;  LOC-2626 KENZIE TER [#114 [@WALKER ON KENZIE APT;  EVTYPE-ILL;  COMMENTS-2626 KENZIE TER : cross stre
 NAME-;  LOC-2600 39TH AVE NE [#100 [@SILVER LAKE CLINIC;  EVTYPE-BREATH;  COMMENTS-2600 39TH AVE NE : cross streets : SILVE
 NAME-MRS SILGE;  LOC-3804 HIGHCREST RD [#109 [@LAKEHILL APT;  EVTYPE-ASTFIR;  COMMENTS-3804 HIGHCREST RD : cross streets : 
 NAME-areson, dale;  LOC-2512 SILVER LA [#204 [@EQUINOX APT;  EVTYPE-FALL;  COMMENTS-2512 SILVER LA : cross streets : SILVER
 NAME-SARAH;  LOC-3701 CHANDLER DR [#312 [@CHANDLER PLACE APT;  EVTYPE-ILL;  COMMENTS-3701 CHANDLER DR : cross streets : DIA
 NAME-;  LOC-3700 FOSS RD [#152;  EVTYPE-ILL;  COMMENTS-3700 FOSS RD : cross streets : CHANDLER DR  75 YO FEMALE - ILL
 NAME-STANLEY SEC;  LOC-2804 29TH AVE NE;  EVTYPE-ALMHSE;  COMMENTS-2804 29TH AVE NE: cross streets : COOLIDGE ST NE & WILSO
 NAME-DIANE;  LOC-2600 KENZIE TER [@AUTUMN WOODS APT#208;  EVTYPE-FALL;  COMMENTS-2600 KENZIE TER : cross streets : LOWRY GR
 NAME-ERIC;  LOC-3201 DIAMOND EIGHT TER [#106 [@DIAMOND EIGHT TERRACE APT;  EVTYPE-CHOKE;  COMMENTS-3201 DIAMOND EIGHT TER :
 NAME-;  LOC-3114 RANKIN RD;  EVTYPE-ALLERG;  COMMENTS-3114 RANKIN RD : cross streets : 32ND AVE NE & TOWNVIEW AVE  thinks s
 NAME-PEKAREK;  LOC-3008 RANKIN RD;  EVTYPE-HEART;  COMMENTS-3008 RANKIN RD : cross streets : EAST GATE RD & CROFT DR  43 YO
 
 */
 
 public class MNHennepinCountyParser extends FieldProgramParser {
   
   private static final Pattern FIELD_PTN = Pattern.compile("; *([A-Z]+)-");
   
   public MNHennepinCountyParser() {
     super("HENNEPIN COUNTY", "MN",
           "NAME LOC:ADDR APT:APT PLACE:PLACE EVTYPE:CALL COMMENTS:INFO cross_streets:X");
   }
   
   @Override
   public String getFilter() {
    return "sheriff@cadpaging.co.hennepin.mm.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!body.startsWith("NAME-")) return false;
     body = body.substring(5).trim();
     
     // Convert wierd field delimiters to standard colon terminated keywords
     body = FIELD_PTN.matcher(body).replaceAll(" $1: ");
     body = body.replace(" [#", " APT:").replace(" [@", " PLACE:").replace("cross streets :", "cross streets:");
     return super.parseMsg(body, data);
   }
   
   // Ignore info field if same as address
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(":")) field = field.substring(0,field.length()-1).trim();
       if (field.equals(data.strAddress)) return;
       super.parse(field, data);
     }
   }
   
   // Cross stree field may have more info at end separated by double blanks
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       super.parse(p.get("  "), data);
       data.strSupp = append(data.strSupp, " / ", p.get());
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
 }
