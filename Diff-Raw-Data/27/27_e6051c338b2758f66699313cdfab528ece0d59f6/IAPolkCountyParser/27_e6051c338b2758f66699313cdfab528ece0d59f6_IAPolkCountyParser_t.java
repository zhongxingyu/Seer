 package net.anei.cadpage.parsers.IA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Polk County, IA (Grimes Fire & Rescue)
  */
 public class IAPolkCountyParser extends FieldProgramParser {
   
   public IAPolkCountyParser() {
     super(CITY_CODES, "POLK COUNTY", "IA",
           "Location:ADDR/S Type:CALL! Caller:PLACE? Time:TIME!");
   }
   
   @Override
   public String getFilter() {
     return "777,888,messaging@iamresponding.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (subject.length() > 0) data.strSource = subject;
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
         int pt1 = field.lastIndexOf(' ');
         int pt2 = place.lastIndexOf(' ');
         if (pt1 >= 0 && pt2 >= 0 && field.substring(pt1).equals(place.substring(pt2))) {
           place = place.substring(0,pt2).trim();
           if (place.endsWith("-")) place = place.substring(0,place.length()-1).trim();
         }
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
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ALLE", "ALLEMAN",
       "ALLT", "ALLEN TWP",
       "ALTO", "ALTOONA",
       "ANKE", "ANKENY",
       "BEAV", "BEAVER TWP",
       "BLOO", "BLOOMFIELD TWP",
       "BOND", "BONDURANT ",
       "CAMP", "CAMP TWP",
       "CARL", "CARLISLE",
       "CLAY", "CLAY TWP",
       "CLIV", "CLIVE",
       "CROC", "CROCKER TWP",
       "DELA", "DELAWARE TWP",
       "DESM", "DES MOINES",
       "DOUG", "DOUGLAS TWP",
       "ELKH", "ELKHART",
       "ELKT", "ELKHART TWP",
       "FOUR", "FOURMILE TWP",
       "FRAN", "FRANKLIN TWP",
       "GRAN", "GRANGER",
       "GRIM", "GRIMES",
       "GRIMES", "GRIMES",
       "JEFF", "JEFFERSON TWP",
       "JOHN", "JOHNSTON",
       "LINC", "LINCOLN TWP",
       "MADI", "MADISON TWP",
       "MITC", "MITCHELLVILLE",
       "PLEA", "PLEASANT HILL",
       "POLK", "POLK CITY",
       "RUNN", "RUNNELLS",
       "SAYL", "SAYLOR TWP",
       "UNIO", "UNION TWP",
       "URBA", "URBANDALE",
       "WALN", "WALNUT TWP",
       "WASH", "WASHINGTON TWP",
       "WEBS", "WEBSTER TWP",
       "WDSM", "WEST DES MOINES",
       "WIND", "WINDSOR HEIGHTS"
   });
 }
