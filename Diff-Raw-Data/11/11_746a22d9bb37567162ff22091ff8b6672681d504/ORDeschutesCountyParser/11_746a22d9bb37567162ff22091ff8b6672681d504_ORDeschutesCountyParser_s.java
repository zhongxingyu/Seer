 package net.anei.cadpage.parsers.OR;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Deschutes County, OR
 Contact:joanne day <frankday12@gmail.com>
 Sender: 911@deschutes.org
 
 Chest Pain Breathing normally => 35   -C-STA500-9395 SW SCOUT CAMP TRL-Map 131208-11:38:0
 Sick Person Vomiting   -A-STA500-13852 SW CANYON DR-Map 131216-18:47:56
 DEAD BODY/DEATH R3-R3-STA500-14537 SW STALLION DR-Map 131223-10:23:46
 Falls Unk status Ground-B-STA500-13345 SW CINDER DR-Map 131215-00:30:04
 Traffic Accidents HIGH MECH - Rollover-D2-STA500-SW BADGER RD/SW CHINOOK DR--13:11:56
 Sick Person Abnormal breathing -C-512,571-8431 SW BASALT DR-Map 131216-10:46:05
 Sick Person Vomiting   -A-STA500-13812 SW CANYON DR-Map 131216-18:47:56
 
 Contact: Dustin Miller <ff3650dm@gmail.com>
 STRUCTURE FIRE-SF-422,412,421,471,531,431,447,510,4PC-1995 NW LOWER BRIDGE WAY-Map 1413NW-19:04:36 
 
 */
 
 
 public class ORDeschutesCountyParser extends FieldProgramParser {
   
   public ORDeschutesCountyParser() {
     super("DESCHUTES COUNTY", "OR",
           "CALL PRI UNITSRC ADDR! MAP?");
   }
   
   @Override
   public String getFilter() {
     return "911@deschutes.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Normally broken by dash field separators.
     // but a dash with a space on both side is probably a "REAL" dash and
     // needs to be projected from our parsing breaks
     body = body.replaceAll(" - ", "__");
     String[] flds = body.split("-");
     if (flds.length < 5) return false;
     return parseFields(flds, data);
   }
   
   // Call field, replace double underscores with dash
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       super.parse(field.replace("__", " - "), data);
     }
   }
   
   // Priority has to match form
   private class MyPriorityField extends PriorityField {
     public MyPriorityField() {
      setPattern(Pattern.compile("[A-Z]\\d*|SF"), true);
     }
   }
   
   private static final Pattern STATION_PAT = Pattern.compile("STA\\d+");
   private class UnitSourceField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       if (STATION_PAT.matcher(field).matches()) {
         data.strSource = field;
       } else  {
         data.strUnit = field;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "SRC UNIT";
     }
   }
   
   private class MyMapField extends MapField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("Map ")) return false;
       super.parse(field.substring(4).trim(), data);
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("PRI")) return new MyPriorityField();
     if (name.equals("UNITSRC")) return new UnitSourceField();
     if (name.equals("MAP")) return new MyMapField();
     return super.getField(name);
   }
 }
