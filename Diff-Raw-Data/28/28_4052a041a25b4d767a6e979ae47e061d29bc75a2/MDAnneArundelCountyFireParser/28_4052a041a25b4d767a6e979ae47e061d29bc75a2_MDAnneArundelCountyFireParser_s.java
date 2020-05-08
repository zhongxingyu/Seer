 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Anne Arundel county, MD
 Contact: krist teal <sqdman43@gmail.com>
 
 SEVERN | ANNE ARUNDEL | FIRE ACTIVITY | I-97 & BENFIELD | BENFIELD RAMP CLOSED DUE TO BRUSH FIRE. | 31 |
 GLEN BURNIE | ANNE ARUNDEL | SHOOTING | 1911 CRAIN HIGHWAY | 1 VICTIM SHOT AT THE MOOSE LODGE. | 31 |
 ODENTON | ANNE ARUNDEL | *U/D* | CONWAY RD & MEYERS STATION | MULTI BARNS & STRUCTURES HAVE BEEN LOST. MULTI DWELLINGS AT RISK. | 31 |
 HANOVER | ANNE ARUNDEL | 3RD ALM | 1703 MAPLE AVE | E291 O/L WITH A LARGE BRUSH FIRE. A HOUSE IS NOW ON FIRE. REQ THE 2ND & 3RD ALARM. MULTI EXPOSURE
 SEVERNA PARK | ANNE ARUNDEL | WORKING FIRE | 750 OAK GROVE CIRCLE | Q23 O/L WITH A WORKING BASEMENT FIRE. | 31
 LINTHICUM | ANNE ARUNDEL | AUTO ACCIDENT | AVIATION BLVD & TERMINAL RD | MVC INVOL BWI BUS. 1 INJ. | 31 |
 
 Contact: mike byrd <mar019ipn@gmail.com>
 Sender: alerts@alertpage.ealertgov.com
 DAVIDSONVILLE | ANNE ARUNDEL | *O/T AUTO* | BRICK CHURCH RD NEAR RT 214 | MEDIC 3 O/L SUV ON IT'S SIDE ON A GUARDRAIL.  OCC OUT,
 CALVERTON CO.41 | PRINCE GEORGES | *2ND ALM* | 11338 CHERRY HILL RD | CHIEF 812 REPORTS A WORKING KITCHEN FIRE WITH RESCUES BEIN 
 BELTSVILLE 4101 | PRINCE GEORGES | *WSF* | 11338 CHERRY HILL RD | CMD HAS WORKING FIRE IN AN APT BUILDING | TG9 | MD227 AP16 | 
 
 */
 
 public class MDAnneArundelCountyFireParser extends FieldProgramParser {
   
   private static final Pattern SEPARATOR = Pattern.compile(" *\\| *");
   
   public MDAnneArundelCountyFireParser() {
     super("ANNE ARUNDEL COUNTY", "MD",
            "CITY SKIP CALL ADDR! INFO");
   }
   
   @Override
   public String getFilter() {
     return "alerts@alertpage.ealertgov.com";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     String[] flds = SEPARATOR.split(body);
     if (flds.length < 5) return false;
     if (!flds[1].equals("ANNE ARUNDEL") &&
         !flds[1].equals("PRINCE GEORGES")) return false;
     return parseFields(flds, data);
   }
   
  private static final Pattern UNIT_PTN = Pattern.compile(" +([^ ]+\\d+)$");
   private static final Pattern BOX_PTN = Pattern.compile("\\d+");
   private class MyCityField extends CityField {
     
     @Override
     public void parse(String field, Data data) {
      Matcher match = UNIT_PTN.matcher(field);
       if (match.find()) {
        String sUnit = match.group(1).trim();
         field = field.substring(0,match.start()).trim();
         if (BOX_PTN.matcher(sUnit).matches()) {
           data.strBox = sUnit;
         } else {
           data.strUnit = sUnit;
         }
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "CITY BOX UNIT";
     }
   }
   
   @Override
   public  Field getField(String name) {
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
 }
