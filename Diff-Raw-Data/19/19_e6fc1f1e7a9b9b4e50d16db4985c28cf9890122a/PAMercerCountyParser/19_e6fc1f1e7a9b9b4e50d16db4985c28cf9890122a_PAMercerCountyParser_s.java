 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Mercer County, PA
 "Kane, Thomas J." <KANETJ1@gcc.edu>
 Sender: 9-1-1"@mcc.co.mercer.pa.us
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: N BROAD ST/LINCOLN AVE GROV EID: 2360315 TYPE CODE: MVU CALLER NAME: LARRY GRACE CALLER ADDR: 522 E MAIN ST E
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 666 FLOWER ST GROV EID: 2363268 TYPE CODE: COM CALLER NAME:  CALLER ADDR: 109 GEORGE JUNIOR RD PINE TIME: 20:
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 597 CLARKSVILLE RD COOL: alias 597 RT 258 0 COOL EID: 2366343 TYPE CODE: HF CALLER NAME: 
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 129 E PINE ST GROV: @PENN GROVE HOTEL EID: 2368267 TYPE CODE: FALM CALLER NAME: VECTOR-42
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: FOREST DR/LINCOLN AVE GROV EID: 2368281 TYPE CODE: TREE CALLER NAME: LUKE SPEARS CALLER A
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: N CENTER ST/W CHESTNUT ST GROV EID: 2370662 TYPE CODE: MVF CALLER NAME: TAMMY OCONNER CAL
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 209 W MERCER ST EID: 2351183 TYPE CODE: MUT CALLER NAME: BUTLER CALLER ADDR:  TIM
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 55 FRANKLIN ST STON EID: 2351363 TYPE CODE: CHIM CALLER NAME: BARTHOLOMEW, BRENDA
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 150 LATONKA DR JACK EID: 2353811 TYPE CODE: HF CALLER NAME: MARKS, KATHLEEN CALLE
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 819 SUNSET AVE GROV EID: 2354410 TYPE CODE: ELO CALLER NAME: MONTGOMERY BEVERLY C
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 201 ERIE ST GROV: @ALLIED NEWS / THE HERALD EID: 2354451 TYPE CODE: ELI CALLER NA
 "9-1-1"@mcc.co.mercer.pa.us (IPS I/Page Notification) Location: 123 W MAIN ST GROV: @GROVE CITY BOROUGH EID: 2354882 TYPE CODE: BRU CALLER NAME: C
 
  */
 
 
 public class PAMercerCountyParser extends FieldProgramParser {
   
   public PAMercerCountyParser() {
     super(CITY_CODES, "MERCER COUNTY", "PA",
            "Location:ADDR/S! EID:ID! TYPE_CODE:CALL! CALLER_NAME:NAME CALLER_ADDR:SKIP TIME:TIME");
   }
   
   @Override
   public String getFilter() {
    return "\"9-1-1\"@mcc.co.mercer.pa.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("IPS I/Page Notification")) return false;
     return super.parseMsg(body, data);
   }
   
   private static final Pattern PLACE_MARKER = Pattern.compile(" *:? *@ *| *: *alias *");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       String sPlace = "";
       Matcher match = PLACE_MARKER.matcher(field);
       if (match.find()) {
         sPlace = field.substring(match.end()).trim();
         field = field.substring(0,match.start()).trim();
       }
       super.parse(field, data);
       
       int pt = sPlace.lastIndexOf(' ');
       if (pt >= 0) {
         String city = CITY_CODES.getProperty(sPlace.substring(pt+1));
         if (city != null) {
           sPlace = sPlace.substring(0,pt).trim();
           if (data.strCity.length() == 0) data.strCity = city;
         }
       }
       data.strPlace = sPlace;
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       if (field.length() >= 2 && field.charAt(1) == '-') {
         data.strPriority = field.substring(0,1).trim();
         field = field.substring(2).trim();
       }
       data.strCall = convertCodes(field, CALL_CODES);
     }
     
     @Override
     public String getFieldNames() {
       return "PRI CALL";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CALL")) return new MyCallField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "COOL", "COOLSPRING TWP",
       "GROV", "GROVE CITY",
       "JACK", "JACKSON TWP",
       "STON", "STONEBORO"
   });
   
   private static final Properties CALL_CODES = buildCodeTable(new String[]{
       "MVU", "Motor Vehicle Crash w/ unknown injuries",
       "MVI", " Motor Vehicle Crash w/ injuries",
       "MVE", "Motor Vehicle Crash w. trap",
       "MVA", "Motor Vehicle Crash w. no injuries",
       "HF", "House fire",
       "COM", "Commercial Fire",
       "ELO", "Electric Lines down",
       "ELI", "Electric Fire",
       "CHIM", "Chimney Fire",
       "BRU", "Brush Fire",
       "MUT", "Mutual Aid",
       "TREE", "Tree down",
       "FALM", "Fire alarm"
   });
 }
