 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 
 
 public class NCOnslowCountyParser extends DispatchOSSIParser {
   
   private static final Pattern CODE_PTN = Pattern.compile("\\d{1,2}[A-Z]\\d{1,2}[A-Z]?"); 
   
   private String selectValue;
   
   public NCOnslowCountyParser() {
     super(CITY_CODES, "ONSLOW COUNTY", "NC",
           "( ASSIST ADDR CITY DIST? INFO+ | ADDR ( SELECT/EMS PLACE? UNIT UNIT? CALL CODE | PLACE? CALL SRC UNIT2! ) )");
   }
   
   @Override
   public String getFilter() {
     return "CAD@onslowcountync.gov";
   }
   
   @Override
   protected boolean parseFields(String[] fields, Data data) {
     if (fields.length == 0) return false;
    selectValue = (CODE_PTN.matcher(fields[fields.length-1]).matches() ? "EMS" : "FIRE");
     return super.parseFields(fields, data);
   }
 
   @Override
   protected String getSelectValue() {
     return selectValue;
   }
   
   private class AssistField extends CallField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.startsWith("ASSIST ")) return false;
       super.parse(field, data);
       return true;
     }
   }
   
   private class MyUnitField extends UnitField {
     public MyUnitField() {
       setPattern(Pattern.compile("(?:UNIT +)?\\d{2,4}[A-Z]?"), true);
     }
     
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("UNIT ")) field = field.substring(5).trim();
       data.strUnit = append(data.strUnit, "-", field);
     }
   }
   
   private class MyCodeField extends CodeField {
     public MyCodeField() {
       setPattern(CODE_PTN, true);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ASSIST")) return new AssistField();
     if (name.equals("DIST")) return new PlaceField("DIST:.*");
     if (name.equals("SRC")) return new SourceField("[A-Z]{1,2}FD", true);
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("CODE")) return new MyCodeField();
     if (name.equals("UNIT2")) return new UnitField("\\d{1,2}[A-Z]", true);
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "RICH", "RICHLANDS"
   });
 }
