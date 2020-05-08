 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /**
  * Guilford County, NC
  */
 public class NCGuilfordCountyParser extends DispatchOSSIParser {
   
   private static final Pattern MARKER = Pattern.compile("^[\\w@\\-\\.]+? *[\n:](?=CAD:)");
   
   public NCGuilfordCountyParser() {
     super("GUILFORD COUNTY", "NC",
           "FYI? ( CALL2 ADDR! | PRI/Z MUTUAL ADDR! | ( SRC SRC PRI | PRI ) CODE? CALL ADDR! ) XINFO+");
   }
   
   @Override
   public String getFilter() {
     return "@edispatches.com,93001,firedistrict13@listserve.com,CAD@greensboro-nc.gov";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (match.find()) {
       body = body.substring(match.end()).trim();
     }
     
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     if (!super.parseMsg(body, data)) return false;
     
     // If out of county mutual aid call, cancel the default county
     if (data.strCall.equals("MUTUAL")) data.defCity = "";
     return true;
   }
 
 
 
   private class Call2Field extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() < 6) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class MutualField extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.equals("MUTUAL")) return false;
       parse(field, data);
       return true;
     }
   }
   
   private class MySourceField extends SourceField {
     @Override
     public void parse(String field, Data data) {
       data.strSource = append(data.strSource, "-", field);
     }
   }
   
   private class CrossInfoField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       if (field.equalsIgnoreCase("ROCK")) {
         data.strCity = "ROCKINGHAM COUNTY";
       } else if (checkAddress(field) > 0) {
         data.strCross = append(data.strCross, " & ", field);
       } else {
         data.strSupp = append(data.strSupp, " / ", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "X CITY INFO";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL2")) return new Call2Field();
     if (name.equals("MUTUAL")) return new MutualField();
     if (name.equals("SRC")) return new MySourceField();
     if (name.equals("PRI")) return new PriorityField("\\d", true);
     if (name.equals("CODE")) return new CodeField("\\d\\d[A-Z]\\d\\d[A-Za-z]?");
     if (name.equals("XINFO")) return new CrossInfoField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
 }
