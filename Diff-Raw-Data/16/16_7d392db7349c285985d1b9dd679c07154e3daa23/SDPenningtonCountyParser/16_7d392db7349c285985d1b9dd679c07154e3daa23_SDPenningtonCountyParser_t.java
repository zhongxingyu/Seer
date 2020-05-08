 package net.anei.cadpage.parsers.SD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Pennington county, SD
 Contact: josh van Vlack <joshvanvlack@live.com>
 Sender: dispatch@co.pennington.sd.us
 
 (page ) Unit:29H1 Status:dispatched diabetic 12823 old hill city rd pennco Problem: blood sugar issues. Patient info: 1 patient(s) 58 years old; female
 (page ) Unit:29H1 Status:dispatched seizure 209 Main St Hill City Cafe ProQA Medical Dispatch Message Sent; Dispatch code: 12-D-02 Problem :SE
 (page ) Unit:HC Status:Dispatched FALARM 23835 Highway 385 Mistletoe Ranch pennco fire alarm showing downstairs smoke detector DEPT56. PREM PHONE 605-574-2512
 (page ) Unit:HC Status:dispatched Falarm 602 Main St Hillyo Manor Hill City Pull Station fire alarm prem#605-574-2476
 (page ) Unit:29H2 Status:Dispatched fall 302 Main Street Hill city Elementary Hill City ProQA medical dispatch code 17-b-01G
 (page ) Unit:29H2 Status:dispatched breath 745 chute Rooster dr Matkins trailer court Hill City ProQA medical dispatch message sent; dispatch code: 06
 (page ) Unit:29H2 Status:EMS 133 Main St Alpine Inn hill city unk age/unk specifics/rp will come back to the phone/possible heart attack
 (page ) Unit:29H1 Status:disatched EMS 557 e main street Hill City call at the Hill City Clinic
 (page ) Unit:29H1 Status:dispatched breath 101 main st exxon Hill City 52 year old, male, conscious, breathing, breathing problems. difficulty speaking
 (page ) Unit:29H1 Status:Dispatched Stroke 24185 Tin Horse Trl pennco 61 year old, female, conscious, breathing, abnormal breathing (clear evidence of stroke)
 
Contact: Alexander Ingalls <alexingalls09@gmail.com>
(Page ) Unit:RV Status:DISPATCHED FIRE 300 E  SIGNAL DR NATIONAL WEATHER SERVICERapid City  1/4 ACRE MOVING TO THE WEST TOWARDS THE TALL GRASS.  1/4 ACRE
(Page ) Unit:RV Status:DISPATCHED SUIC 2064 S VALLEY DR PENNCO  RPT**  MALE SUBJ HUNG HIMSELF AT ABOVE LOC  13:10

 */
 
 public class SDPenningtonCountyParser extends FieldProgramParser {
  
   public SDPenningtonCountyParser() {
     super(CITY_LIST, "PENNINGTON COUNTY", "SD",
            "Unit:UNIT! Status:STATUS! Problem:INFO Patient_info:INFO");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@co.pennington.sd.us";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    if (!subject.equalsIgnoreCase("page")) return false;
     return super.parseMsg(body, data);
   }
   
   private static final Pattern CODE_PTN = Pattern.compile("\\b\\d{1,2}-[A-Za-z]-\\d{1,2}[A-Za-z]?\\b");
   private class StatusField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(" ProQA ");
       if (pt > 0) {
         Matcher match = CODE_PTN.matcher(field);
         if (match.find(pt+7)) {
           data.strCode = match.group();
         }
         field = field.substring(0,pt).trim();
       }
      parseAddress(StartType.START_CALL, FLAG_PAD_FIELD | FLAG_IGNORE_AT, field, data);
       data.strPlace = getPadField();
       data.strSupp = getLeft();
       if (data.strCity.equalsIgnoreCase("PENNCO")) data.strCity = "";
     }
     
     @Override
     public String getFieldNames() {
       return "CALL ADDR APT PLACE CITY INFO CODE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("STATUS")) return new StatusField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ASHLAND HEIGHTS",
     "COLONIAL PINE HILLS",
     "CREIGHTON",
     "GREEN VALLEY",
     "HILL CITY",
     "KEYSTONE",
     "NEW UNDERWOOD",
     "OWANKA",
     "QUINN",
     "RAPID CITY",
     "RAPID VALLEY",
     "WALL",
     "WASTA",
     "WICKSVILLE",
     
     "PENNCO"
   };
 }
