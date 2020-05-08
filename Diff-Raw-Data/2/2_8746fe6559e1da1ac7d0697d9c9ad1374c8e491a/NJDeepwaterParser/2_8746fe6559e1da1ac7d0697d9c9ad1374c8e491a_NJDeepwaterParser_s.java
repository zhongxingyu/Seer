 package net.anei.cadpage.parsers.NJ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Deepwater, NJ in Salem county
 Contact: "m.warner1@comcast.net" <m.warner1@comcast.net>
 Sender: msg@cfmsg.com
 
 (Chief ALT) [STA:7] -- ALS -- 118 E Pittsfield St Pennsville Township NJ - Cross STS:Sparks Ave -- Caller: DAULTON 
 (Chief ALT) [STA:7] -- Motor Vehicle Accident -- 1163 S Broadway Way Pennsville Township NJ - Cross STS:Freas Rd -- Caller: JACK KUGLER 9352515
 (Chief ALT) [STA:7] -- MVC -- Njtp South Mm 1-4.5 Njtp Carneys Point Township NJ -- Caller: SHAWN HAMMELL 6178168
 (Chief ALT) [STA:7] -- Fire Alarm System -- 117 N Broadway Way Pennsville Township NJ - Cross STS:Brandriff Ave -- Caller: VERIZON / KAREN 6158005
 (Chief ALT) [STA:7] -- Out Of County Assist -- Delaware Memorial Brg New Castle DE -- Caller: COUNTY
 
  */
 
 public class NJDeepwaterParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" --? ");
   
   public NJDeepwaterParser() {
     super(CITY_LIST, "DEEPWATER", "NJ",
            "CALL! ADDR/S! Cross_STS:X? Caller:NAME");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // Drop calls from New Castle County, DE (B)
     if (body.contains(" EMD:")) return false;
    
     if (!subject.startsWith("Chief ALT|")) return false;
     data.strSource = subject.substring(10).trim();
     
     return parseFields(DELIM.split(body), data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 
   private static final Pattern STATE_PTN = Pattern.compile(" +([A-Z]{2})$");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       super.parse(field, data);
       Matcher match = STATE_PTN.matcher(data.strCity);
       if (match.find()) {
         data.strState = match.group(1);
         if (data.strState.equals("NJ")) data.strState = "";
         data.strCity = data.strCity.substring(0, match.start()).trim();
       }
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " ST";
     }
   }
   
   private static final Pattern PHONE_PTN = Pattern.compile(" +(\\d{7,})$");
   private class MyNameField extends NameField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = PHONE_PTN.matcher(field);
       if (match.find()) {
         data.strPhone = match.group(1);
         field = field.substring(0,match.start());
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "NAME PHONE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("NAME")) return new MyNameField();
     return super.getField(name);
   }
   
   private static String[] CITY_LIST = new String[]{
     "NEW CASTLE DE",
     "CARNEYS POINT TOWNSHIP NJ",
     "PENNSVILLE TOWNSHIP NJ"
   };
 }
 
 
