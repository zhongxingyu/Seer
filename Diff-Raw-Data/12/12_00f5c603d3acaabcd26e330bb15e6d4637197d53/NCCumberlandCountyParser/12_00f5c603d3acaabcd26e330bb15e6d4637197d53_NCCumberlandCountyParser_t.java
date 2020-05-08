 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 public class NCCumberlandCountyParser extends FieldProgramParser {
   
   private static final Pattern MARKER = Pattern.compile("(?:\\(S\\)) *(.*?) *\\(N\\) *");
   
   public NCCumberlandCountyParser() {
     super("CUMBERLAND COUNTY", "NC",
           "UNIT? PLACE? DATETIME CALL UNIT2? ADDR! X PLACE");
   }
   
   @Override
   public String getFilter() {
     return "cad@co.cumberland.nc.us,messaging@iamresponding.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (subject.endsWith("|N")) {
       subject = subject.substring(0,subject.length()-2);
       body = "(N) " + body;
     }
     if (subject.equals("S")) body = "(S)" + body;
     
     Matcher match = MARKER.matcher(body);
     if (match.find()) {
       data.strPlace = match.group(1).trim();
       body = body.substring(match.end());
     } else {
       int pt = body.indexOf("(N)");
       if (pt >= 0) body = body.substring(pt+3).trim();
     }
     
     return parseFields(body.split(";"), data);
   }
   
   private class MyUnitField extends UnitField {
     public MyUnitField() {
       setPattern(Pattern.compile("[A-Z][0-9]+"));
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("(?:(.+?) +)??(?:([0-9/]+) +)?(\\b\\d\\d:\\d\\d:\\d\\d)");
   private static final Pattern DATE_PTN = Pattern.compile("\\d\\d/\\d\\d/\\d{4}");
   private class MyDateTimeField extends DateTimeField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       Matcher match = DATE_TIME_PTN.matcher(field);
       if (!match.matches()) return false;
       data.strPlace = append(data.strPlace, " / ", getOptGroup(match.group(1)));
       String sDate = match.group(2);
       data.strTime = match.group(3);
       if (sDate != null && DATE_PTN.matcher(sDate).matches()) data.strDate = sDate;
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       if (!checkParse(field, data)) abort();
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE DATE TIME";
     }
   }
   
   private class MyUnitField2 extends UnitField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.contains(" ")) return false;
       parse(field, data);
       return true;
     }
     
     @Override
     public void parse(String field, Data data) {
       StringBuilder src = new StringBuilder();
       StringBuilder unit = new StringBuilder(data.strUnit);
       for (String token : field.split(",")) {
         StringBuilder sb = (token.startsWith("ST") ? src : unit);
         if (sb.length() > 0) sb.append(',');
         sb.append(token);
       }
       data.strSource = src.toString();
       data.strUnit = unit.toString();
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     if (name.equals("UNIT2")) return new MyUnitField2();
     return super.getField(name);
   }
 }
