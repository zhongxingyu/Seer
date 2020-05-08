 package net.anei.cadpage.parsers.SC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchPrintrakParser;
 
 /*
 Spartanburg County, SC
 Contact: Richard Brown <firefighterjacob@gmail.com>
 Sender: 911info@spartanburgcounty.org
 System: Motorola CAD premeire
 
 GLFD - CF/14072 TYP: ..FUEL SPILL........ AD: DOGWOOD CLUB RD&S PINE ST CMT1: <<< FUEL SPILL >>> CMT2: <<< SEND GLENDALE AND PACOLET ON ALL STRUCTURE
 GLFD - CF/13375 TYP: COMMERC FIRE ALARM. AD: 2720 COUNTRY CLUB RD LOC: PARK PLACE ASSISTED LIVING CMT1: <<< SEND GLENDALE AND PACOLET ON ALL STRUCTURE
 GLFD - CF/13440 TYP: FIRE ALARM(RESDENTL) AD: 200 JOHNSON LAKE RD LOC: MURF/RESD/5831013 CMT1: <<< SEND GLENDALE AND PACOLET ON ALL STRUCTURE FIRES &
 GLFD - CF/09993 TYP: MVA W/INJURY..... AD: CLIFTON GLENDALE RD&GLENDALE A CMT1: <<< SEND GLENDALE AND PACOLET ON ALL STRUCTURE FIRES & ALARMS >>> CMT2
 GLFD - CF/10647 TYP: BRUSH FIRE......... AD: 170 FRETWELL RD CMT1: <<< SEND GLENDALE AND PACOLET ON ALL STRUCTURE FIRES & ALARMS >>> CMT2: <<< BRUSH,
 
 Contact: Andrew Couchey <abcouchey@gmail.com>
 BSFD - CF/20546 TYP: COMMERC FIRE ALARM. AD: 2251 OLD FURNACE RD LOC: BOILING SPRINGS HIGH CMT1: << BOILING SPRINGS FIRE DISTRICT >> CMT2: <<< COMMERC
 
  */
 
 public class SCSpartanburgCountyParser extends DispatchPrintrakParser {
   
   public SCSpartanburgCountyParser() {
     super("SPARTANBURG COUNTY", "SC");
   }
   
   @Override
   public String getFilter() {
     return "911info@spartanburgcounty.org";
   }
   
   // Our source field ha a station source and a call ID
   private class MySourceField extends SourceField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strSource = p.get("-");
       data.strCallId = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "SRC ID";
     }
   }
   
   // Call fields have silly ... brackets
   private static final Pattern CALL_PTN = Pattern.compile("\\.*(.*?)\\.*");
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = CALL_PTN.matcher(field);
       if (match.matches()) field = match.group(1);
       super.parse(field, data);
     }
   }
   
   // Comment fields have silly angle brackets
   private static final Pattern INFO_PTN = Pattern.compile("<* *(.*?) *>*");
   private class MyInfoField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = INFO_PTN.matcher(field);
       if (match.matches()) field = match.group(1);
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("SRC")) return new MySourceField();
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }
