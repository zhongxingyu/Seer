 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Nash County, NC
 Contact: Patrick Jackson <patjackson52@gmail.com>
 
 NASH911:* 7400 LANCASTER STORE RD* * * SPRING HOPE* * ALARM-FIRE* * SEC CENTRAL* * * * * * *
 NASH911:* 6658 RED OAK RD* * * BATTLEBORO* * ALARM-FIRE* * ADT SECURITY* * * * * * *
 NASH911:* 2311 DUKE RD* * * NASHVILLE* * HEMORRHA-H* * EVANS* * * * * * *
 NASH911:* 5526 SYKES RD* * * NASHVILLE* * STROKE-H* * MILLS, URDIS* * * * * * *
 NASH911:* 7164 RACE TRACK RD* * * CASTALIA* * STRUCTURE* * DAVID TALBOT* * * * * * *
 NASH911:* 5988 SPRING CREEK RD* * * CASTALIA* * ALLERGY-H* * 704* * * * * * *
 NASH911:* SYKES RD* * * SPRING HOPE* * *FIRE* * SCOTT PARKER* * * * * * *
 NASH911:* 7671 EDWARDS RD* * * CASTALIA* * ALARM-FIRE* * GUARDIAN PROTECTION* * * * * * *
 NASH911:* 4306 SWIFT CREEK SCHOOL RD* * * WHITAKERS* * STRUCTURE* * ALLTEL WIRELESS* * * * * * *
 NASH911:* 5388 TAYLORS STORE RD* * * NASHVILLE* * OUTSIDE FI* * HEDGEPETH, AVERINE* * * * * * *
 
Contact: Kevin Thomas <emtffkev@gmail.com>
Sender: NASH911@NASHCOUNTYNC.GOV
  / NASH911:* 5557 N HALIFAX RD* * * BATTLEBORO* * MVA PIN-H* * LARNELL HARDING* * * * * * *\n

 */
 
 public class NCNashCountyParser extends DispatchOSSIParser {
   
   public NCNashCountyParser() {
     super("NASH COUNTY", "NC",
            "SKIP ADDR! SKIP SKIP CITY! SKIP SKIP? CALL! SKIP PLNAME! SKIP SKIP SKIP SKIP SKIP SKIP");
   }
   
   @Override
  public String getFilter() {
    return "nash911@nashcountync.gov";
  }
  
  @Override
   public boolean parseMsg(String body, Data data) {
    if (body.startsWith("/ ")) body = body.substring(2).trim();
     if (!body.startsWith("NASH911:")) return false;
     body = body + " ";
     return parseFields(body.split("\\* "), data);
   }
   
   private class MyCallField extends CallField {
     public MyCallField() {
       setPattern(Pattern.compile(".+"));
     }
   }
   
   private class PlaceNameField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       if (field.contains(",")) {
         data.strName = field;
       } else {
         data.strPlace = field;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "NAME PLACE";
     }
     
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("PLNAME")) return new PlaceNameField();
     return super.getField(name);
   }
 }
