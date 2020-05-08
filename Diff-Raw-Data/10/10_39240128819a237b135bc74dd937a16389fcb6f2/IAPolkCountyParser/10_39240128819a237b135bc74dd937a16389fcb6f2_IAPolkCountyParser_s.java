 package net.anei.cadpage.parsers.IA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Polk County, IA (Grimes Fire & Rescue)
 Contact: angela brouwer <abrouwerdcg@gmail.com>
 Sender: 777129998926
 
 .... (Grimes) Location: 500 S JAMES ST GRIM: @SCHOOL - GRIM Type: STILL VEHICLE Caller:  Time: 19:47:24
 .... (Grimes) Location: 101 SE DESTINATION DR GRIMES Type: MEDICAL  Caller: GRIMES FAMILY PHYSICIANS Time: 10:54:07
 .... (Grimes) Location: 200 SW BROOKSIDE DR GRIM,111 Type: MEDICAL  Caller: KENNYBROOK VILLAGE Time: 13:42:46
 .... (Grimes) Location: 5555 NW 55TH AVE JOHN: @CHILDSERVE - JOHN Type: COMMFIRE ALARM Caller: ADT/DBI Time: 17:01:43
 .... (Grimes) Location: 2555 W 1ST ST GRIM: @DALLAS CENTER GRIMES HIGH SCHOOL - GRIM Type: COMMFIRE ALARM Caller: ADT Time: 20:37:32
 
  */
 
 public class IAPolkCountyParser extends FieldProgramParser {
   
   public IAPolkCountyParser() {
     super(CITY_LIST, "POLK COUNTY", "IA",
           "Location:ADDR/S! Type:CALL! Caller:PLACE! Time:TIME!");
   }
   
   @Override
   public String getFilter() {
    return "7771";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (subject.length() == 0) return false;
     data.strSource = subject;
     return super.parseMsg(body, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(": @");
       String place = "";
       if (pt >= 0) {
         place = field.substring(pt+3).trim();
         field = field.substring(0,pt);
       }
       
       pt = field.lastIndexOf(',');
       String apt = "";
       if (pt >= 0) {
         apt = field.substring(pt+1).trim();
         field = field.substring(0,pt);
       }
       
       super.parse(field, data);
       
       if (place.endsWith(data.strCity)) {
         place = place.substring(0,place.length()-data.strCity.length()).trim();
       }
       if (place.endsWith("-")) place = place.substring(0,place.length()-1).trim();
       
       data.strApt = append(data.strApt, " - ", apt);
       data.strPlace = place;
       data.strCity = convertCodes(data.strCity, CITY_CODES);
     }
   }
   
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       data.strPlace = append(data.strPlace, " / ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("PLACE")) return new MyPlaceField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     
     "ALLEMAN",
     "ALTOONA",
     "ANKENY",
     "BONDURANT",
     "CARLISLE",
     "CLIVE",
     "DES MOINES",
     "ELKHART",
     "GRANGER",
     "GRIM",
     "GRIMES",
     "JOHN",
     "JOHNSTON",
     "MITCHELLVILLE",
     "NORWALK",
     "PLEASANT HILL",
     "POLK CITY",
     "RUNNELLS",
     "SHELDAHL",
     "URBANDALE",
     "WEST DES MOINES",
     "WINDSOR HEIGHTS",
     
     "AVON",
     "BERWICK",
     "ENTERPRISE",
     "FARRAR"
   };
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "GRIM", "GRIMES",
       "JOHN", "JOHNSTON"
   });
 }
