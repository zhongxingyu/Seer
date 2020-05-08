 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Cabarrus County, NC (variant B)
 Contact: Mark Brown <hzmtinstructor@gmail.com>
 Sender: 930010XX
 
 29:1|ODOR OF SMOKE INSIDE|OPS3|920 CHURCH ST N|ARDSLEY AVE NE|CONCORD PKWY N|2|21KA|10/04/2011 10:24:12|NEMC MAIN BUILDING(CMH)|7047836595|4431|1106269
 30:FYI: |1|COMMERCIAL FIRE ALARM|2460 ROCK HILL CHURCH RD|CLOISTER CT NW|CAVALIER CT NW|3|371|10/04/2011 14:37:43|CLARE BRIDGE (DISCOVERY)|7047200990|1106276
 34:10/05/2011 22:21:57|DWELLING FIRE|199 GOAR ST SW|FERN AVE SW|RONE AVE SW
 37:10/07/2011 22:05:08|DWELLING FIRE|2873 SIGNAL CT SW|PULLMAN ST SW
 77:FYI: |1|RESIDENTIAL FIRE ALARM|764 JUANITA DR SW|JOSEPHINE LN SW|KIM ST SW|(S)YATES MEADOW SD (N)YATES MEADOWS|5|583|10/23/2011 19:55:32|1106744
 
 82:P|BOMB|2|273|90|3200 PATRICK HENRY DR NW|DAVIDSON HWY|10/26/2011 14:48:25|COVENANT CLASSICAL SCHOOL|7047921854|1106799
 89:FYI: |1|RESIDENTIAL FIRE ALARM|4155 KELLYBROOK DR SW|LITTLETON DR SW|LITTLETON DR SW|(S) (N)BRANDON RIDGE|8|843|10/27/2011 12:11:16|FRASER|7047869700|1106822
 88:Update: |1|FIRE VEHICLE ACCIDENT RESPONSE|CENTRAL DR NW/MAPLE AVE NW|7047873400|2|237|10/27/2011 11:26:22|FVACC|7046922840
 163:FYI: |1|COMMERCIAL FIRE ALARM|3700 TAYLOR GLEN LN NW|PITTS SCHOOL RD NW|5|597|11/15/2011 20:43:47|TAYLOR GLEN RETIREMENT COM|7047886510|1107247
 170:Update: |1|COMMERCIAL FIRE ALARM|OPS2|4545 CONCORD PKWY S|CARPENTER CT NW|BRUTON SMITH BLVD|5|593|11/16/2011 13:56:51|BACKING UP CLASSICS|7047889500|ASG|8666668897|1107270
 165:FYI: |1|WORKING FIRE UPGRADE|OPS3|1051 NC HWY 49 S|GLENWOOD DR SW|OLD CHARLOTTE RD SW|8|834|11/16/2011 12:41:58|WHITLEY TRK COMPANY|JJTC INC/ DBA WHITLEY TRUCK|7047862610|1107266
 
1021:FYI: |1|COMMERCIAL FIRE ALARM|250 BISHOP LN|CONCORD LAKE RD|2|21KA|07/23/2012 19:01:22|BRIAN CENTER|7047886400|1205076
1019:07/23/2012 16:08:00|HAZARDOUS MATERIALS|611 VEGA ST NW|SUTRO FOREST DR NW|FALLING LEAF DR NW
1018:FYI: |1|COMMERCIAL FIRE ALARM|5571 CONCORD PKWY S|SANDUSKY BLVD|EVERETTE DR|5|593A|07/23/2012 15:59:21|CMS NATIONWIDE CUP GARAGE|7044554381|1205066

 */
 
 public class NCCabarrusCountyBParser extends FieldProgramParser {
   
  private static final Pattern MARKER = Pattern.compile("^\\d{2,4}:");
   
   public NCCabarrusCountyBParser() {
     super("CABARRUS COUNTY", "NC",
            "( DATETIME CALL CH? ADDR! X+? |" +
             " FYI? DIGIT CALL ( PRI UNIT+? CH? ADDR! X+? | CH? ADDR! X+? INFO? PRI UNIT+? ) DATETIME EXTRA+? ID )");
   }
   
   @Override
   public String getFilter() {
     return "93001";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     return parseFields(body.split("\\|"), data);
   }
   
   private class FYIField extends SkipField {
     public FYIField() {
       setPattern(Pattern.compile("FYI:|Update:"), true);
     }
   }
   
   private class DigitField extends SkipField {
     public DigitField() {
       setPattern(Pattern.compile("[\\dP]"), true);
     }
   }
   
   private class MyChannelField extends ChannelField {
     public MyChannelField() {
       setPattern(Pattern.compile("OPS\\d+"));
     }
   }
   
   private class MyPriorityField extends PriorityField {
     public MyPriorityField() {
       setPattern(Pattern.compile("\\d"), true);
     }
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("\\d[A-Z0-9]{1,3}");
   private class MyUnitField extends UnitField {
     
     public MyUnitField() {
       setPattern(UNIT_PTN, true);
     }
     
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, " ", field);
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d");
   private class MyDateTimeField extends DateTimeField {
     public MyDateTimeField() {
       setPattern(DATE_TIME_PTN, true);
     }
   }
   
   private static final Pattern PHONE_PTN = Pattern.compile("\\d{10}");
   private static final Pattern SKIP_PTN = Pattern.compile("\\d{3,6}");
   private class MyExtraField extends Field {
     @Override
     public void parse(String field, Data data) {
       Matcher match = PHONE_PTN.matcher(field);
       if (match.matches()) {
         data.strPhone = append(data.strPhone, " / ", field);
         return;
       }
       match = SKIP_PTN.matcher(field);
       if (match.matches()) return;
       data.strPlace = append(data.strPlace, " / ", field);
     }
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{7}"));
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("FYI")) return new FYIField();
     if (name.equals("DIGIT")) return new DigitField();
     if (name.equals("CH")) return new MyChannelField();
     if (name.equals("PRI")) return new MyPriorityField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     if (name.equals("EXTRA")) return new MyExtraField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
 }
