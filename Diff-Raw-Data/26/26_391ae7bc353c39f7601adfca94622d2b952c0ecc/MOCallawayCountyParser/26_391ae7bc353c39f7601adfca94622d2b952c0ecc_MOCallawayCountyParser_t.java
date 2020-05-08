 package net.anei.cadpage.parsers.MO;
 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 public class MOCallawayCountyParser extends FieldProgramParser {
  
   public MOCallawayCountyParser() {
     super("CALLAWAY COUNTY", "MO",
           "IDCALL! Address:ADDR! Reporting_Person:NAME! Phone:PHONE! Detail:INFO");
   }
   
   @Override
   public String getFilter() {
     return "EnterpolAlerts@cceoc.org";
   }
   
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.startsWith("NEW")) return false;
     body = body.replaceAll(" Phone:", "\nPhone:");
     if (! parseFields(body.split("\n"), data)) return false;
     if (data.strCity.equalsIgnoreCase("CALLAWAY COUNTY")) data.strCity = "";
     return true;
   }
   
  private static final Pattern ID_CODE_CALL_PTN = Pattern.compile("NEW[A-Z]{3} +#(\\d\\d-\\d+|OUTS) +(\\d[A-Z]?)+ +- +(.*)");;
   private class MyIdCallField extends Field {
     @Override
     public void parse(String field, Data data) {
       Matcher match = ID_CODE_CALL_PTN.matcher(field);
       if (!match.matches()) abort();
       data.strCallId = match.group(1);
       data.strCode = match.group(2);
       data.strCall = match.group(3).trim();
     }
     
     @Override
     public String getFieldNames() {
       return "ID CODE CALL";
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strCity = p.getLastOptional(',');
       data.strPlace = p.getOptional(" - ");
       String sAddr = p.get('(');
       data.strApt = append(p.get(')'), " - ", p.get());
       if (data.strCity.length() == 0 && data.strPlace.length() > 0) {
         data.strCity = sAddr;
         sAddr = data.strPlace;
         data.strPlace = "";
       }
       super.parse(sAddr, data);
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames() + " APT CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("IDCALL")) return new MyIdCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 
   @Override
   public String adjustMapAddress(String sAddress) {
     return ST_PTN.matcher(sAddress).replaceAll("MO");
   }
   private static final Pattern ST_PTN = Pattern.compile("\\b(?:ST HWY|SR)(?= +([A-Z])\\1?| +A[A-F])\\b", Pattern.CASE_INSENSITIVE);
 }
