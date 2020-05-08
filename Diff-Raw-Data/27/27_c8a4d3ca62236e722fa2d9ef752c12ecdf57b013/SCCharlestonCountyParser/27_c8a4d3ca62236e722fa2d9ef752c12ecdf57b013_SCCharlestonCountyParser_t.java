 package net.anei.cadpage.parsers.SC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 
 public class SCCharlestonCountyParser extends FieldProgramParser {
   
   public SCCharlestonCountyParser() {
     super("CHARLESTON COUNTY", "SC",
           "( PREFIX Address:ADDR! X_Street:X Cmd_Channel:CH | ADDR/SC! X_Street:X Cmd_Channel:CH! Units_Assigned:UNIT )");
   }
   
   @Override
   public String getFilter() {
    return "@charlestoncounty.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // See if we can parse this as a fixed field message
     if (parseFixedFieldMsg(subject, body, data)) return true;
     body = body.replace(" Op Channel:", " Cmd Channel:").replace(" Cmnd Channel:", " Cmd Channel:");
     if (! super.parseMsg(body, data)) return false;
     if (data.strCall.length() == 0) return false;
     if (data.strAddress.length() == 0) return false;
     return true;
   }
 
   private boolean parseFixedFieldMsg(String subject, String body, Data data) {
     if (!subject.equals("Dispatch Info")) return false;
     if (body.length() < 87) return false;
     if (!body.substring(34,43).equals("District ")) return false;
     if (!body.substring(84,87).equals("XS:")) return false;
     if (body.length() >= 130 &&
         !body.substring(121,130).equals("Apt/Bldg:")) return false;
     if (body.length() >= 187 &&
         !body.substring(173,187).equals("Location Name:")) return false;
     if (body.length() < 130) data.expectMore = true;
     
     data.strUnit = getField(body, 0, 14);
     data.strCallId = getField(body, 14,34);
     data.strSource = getField(body, 43, 46);
     data.strCall = getField(body, 46, 54);
     data.strCode = getField(body, 54, 64);
     parseAddress(getField(body, 64, 84), data);
     data.strCross = getField(body, 87, 121);
     data.strApt = getField(body, 130, 143);
     data.strSupp = getField(body, 143, 173);
     data.strPlace = getField(body, 187, 9999);
     
     return true;
   }
   
   @Override
   public String getProgram() {
     return "UNIT ID SRC CALL CODE ADDR X APT INFO PLACE CH";
   }
   
   private static String getField(String body, int start, int end) {
     int len = body.length();
     if (start > len) start = len;
     if (end > len) end = len;
     return body.substring(start, end).trim();
   }
 
   private static final Pattern PREFIX_PTN = 
       Pattern.compile("(\\d{4}-\\d{7}) District (\\d{2}) (.*)");
   private class PrefixField extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       Matcher match = PREFIX_PTN.matcher(field);
       if (!match.matches()) return false;
       data.strCallId = match.group(1);
       data.strSource = match.group(2);
       parse(match.group(3), data);
       return true;
     }
 
     @Override
     public String getFieldNames() {
       return "ID SRC";
     }
     
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("PREFIX")) return new PrefixField();
     return super.getField(name);
   }
 }
