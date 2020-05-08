 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 public class NYNassauCountyDParser extends FieldProgramParser {
   
   private static final Pattern ID_PTN = Pattern.compile("^(\\d{4}-\\d{6}) ");
   private static final Pattern ID_PTN2 = Pattern.compile("\\b(\\d{4}-\\d{6})\\b");
   
   public NYNassauCountyDParser() {
     super("NASSAU COUNTY", "NY",
            "ADDR/SP! CS:X! TOA:SPEC");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = ID_PTN.matcher(body);
     if (match.find()) {
       data.strCallId = match.group(1);
       body = body.substring(match.end()).trim();
     }
     
     // Call description is in front of text bracketed by three asterisks
     int pt1 = body.indexOf("***");
     if (pt1 < 0) return false;
    int pt2 = body.indexOf("*** ", pt1+2);
     if (pt2 < 0) return false;
     data.strCall = append(body.substring(0,pt1).trim(), " / ", body.substring(pt1+3, pt2).trim());
     
     body = body.substring(pt2+4).trim();
     return super.parseMsg(body, data);
   }
   
   private class SpecField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strTime = p.get(' ');
       data.strDate = p.get(' ').replace('-', '/');
       field = p.get();
       
       Matcher match = ID_PTN2.matcher(field);
       if (match.find()) {
         data.strCallId = match.group(1);
         field = field.substring(0,match.start()) + field.substring(match.end());
         field = field.trim().replaceAll("  +", " ");
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "X DATE TIME INFO ID";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("SPEC")) return new SpecField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID CALL " + super.getProgram();
   }
 }
 
 
