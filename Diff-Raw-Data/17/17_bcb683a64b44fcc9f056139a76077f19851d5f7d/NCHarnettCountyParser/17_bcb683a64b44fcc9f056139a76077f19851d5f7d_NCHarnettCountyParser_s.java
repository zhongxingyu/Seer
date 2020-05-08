 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Harnett County, NC
 Contact: Beverly Williams <bwilliams.co9@gmail.com>
 Contact: Keith combs <keithcombs2007@gmail.com>
 Contact: Michael Moston <mikemoston@gmail.com>
 Contact: edward woodall <ewoodall1971@gmail.com>
 Sender: cadpage@harnett.org
 
 1102-012074*8326 NC 27 W***LILLINGTON***RESIDENTIAL STRUCTURE FIRE****1175****
 S: M:1107-061052*3058 CRAWFORD RD***ERWIN***MOTOR VEHICLE ACCIDENT****1186****\n
 FRM:cadpage@harnett.org\nMSG:1108-070330*1995 BETHEL BAPTIST RD***SPRING LAKE***ELECTRICAL HAZARD****1120****
 1109-079514*117 SCRANTON CT***CAMERON***EMS DEAD ON ARRIVAL CODE 3****1109****
 
 */
 
 public class NCHarnettCountyParser extends FieldProgramParser {
   
   public NCHarnettCountyParser() {
     super("HARNETT COUNTY", "NC",
           "ID ADDR CITY CALL SEQ!");
   }
   
   @Override
   public String getFilter() {
     return "cadpage@harnett.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     String flds[] = body.split("\\*+");
    if (flds.length < 5) return false;
     return parseFields(flds, data);
   }
   
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{4}-\\d{6}"), true);
     }
   }
   
   private class SeqField extends SkipField {
     public SeqField() {
       setPattern(Pattern.compile("\\d{4}"), true);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("SEQ")) return new SeqField();
     return super.getField(name);
   }
 }
