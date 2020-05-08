 package net.anei.cadpage.parsers.VA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Agusta County, VA
 Contact: Josh Sprouse <firemedic1812@gmail.com>
 Contact: Christopher Botkin <botkincs@gmail.com>
 Sender:cad@co.augusta.va.us
 
 CAD: Chest Pain:81 Fountain Cave RD:RT 605 - Wonderly / 10203: Cave Hill LN:Rockfish RD
 CAD: OB EMERGENCY:WEYERS CAVE SHELL/ TEXACO:149 WEYERS CAVE RD:P O BOX 184:TRIANGLE DR:SR 222:CNTY-1
 CAD:LANDING ZONE:BEVERLEY MANOR ELEMENTARY SCH:116 CEDAR GREEN RD:STEVENS LN:PARKERSBURG TPKE
 CAD:AUTOMOBILE ACCIDENT:1519 PARKERSBURG TPKE:159-03:NUCKOLES LN:EIDSON HILL LN:CNTY-2
 CAD:CONTROL BURN INVESTIGATION:300-BLK TRINITY POINT RD:PARKERSBURG TPKE:MILLERS HOLLOW LN:CNTY-2
 CAD:AUTOMOBILE ACCIDENT:3426 MORRIS MILL RD:AREA OF:GALLOPING HILLS RD:SHELLEY LN
 CAD:ELECTRICAL FIRE - DLOC:135 CRAWFORD DR:CRAWFORD MANOR - 11104:MADISON DR:WAYBURN ST:CNTY-
 CAD:CONTROL BURN INVESTIGATION:3445 OLD GREENVILLE RD:RT 613 / 245-04:GLORY LN:CHESTNUT RIDGE RD:CNTY-2
 CAD:AUTOMOBILE ACCIDENT:I81NB AREA MM 217.9
 
  */
 
 public class VAAugustaCountyParser extends DispatchOSSIParser {
   
   private static final Pattern MAP_PTN = Pattern.compile("\\b\\d{3}-?\\d{2}$");
   
   public VAAugustaCountyParser() {
     super("AUGUSTA COUNTY", "VA",
           "CALL! PLACE? ADDR/S! MAP? EXTRA? X X INFO");
   }
   
   @Override
   public String getFilter() {
     return "cad@co.augusta.va.us";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     return super.parseMsg(body.replace(':',';').replace("CAD;","CAD:"), data);
   }
 
   // Map field recognizes and isolates a trailing map pattern
   private class MyMapField extends MapField {
 
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       Matcher match = MAP_PTN.matcher(field);
       if (!match.find()) return false;
       field = match.group();
       if (field.indexOf('-') < 0) {
         field = field.substring(0,3) + '-' + field.substring(3);
       }
       parse(field, data);
       return true;
     }
   }
   
   // Extra field contains all kinds of sloppy stuff
   private class ExtraField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       String tmp = field.toUpperCase();
       if (tmp.startsWith("PO ") || tmp.startsWith("P O ")) return;
       if (data.strPlace.length() == 0) data.strPlace = field;
       else data.strSupp = append(data.strSupp, " / ", field);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("MAP")) return new MyMapField();
     if (name.equals("EXTRA")) return new ExtraField();
     return super.getField(name);
   }
   
   
 }
