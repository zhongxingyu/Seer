 package net.anei.cadpage.parsers.VA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /***
 Louisa County, VA
 Contact: Lisa Burruss <medic725@gmail.com>
 Contact: "Jason Slater" <jaiebo@yahoo.com>
 Contact: george hill <glhill1964@gmail.com>
 Sender: 911@louisa.org
 
 (Incident Notification) Call#: -1994 ; EMS-Extremity Injury ; 118 FREESTYLE LN ; ; Box 407 ; Info:  DOG ON SITE...  Closed APCO Case: -1994  APCO Narrative: Is the p
 (Incident Notification) Call#: -2165 ; EMS-Chest Pain/Cardiac Problem ; 16247 JONES FARM RD ; ; Box 509 ; Info:  EMD/PAS  NO DRUGS USED  NOT FAINTED - HE IS WEAK AND
 (Incident Notification) Call#: -2121 ; MVC-Injury ; 2752 BUMPASS RD ; ; Box 306 ; Info:  MEDIC2 CONTACT SPOTSY FOR CLOSER UNIT  CALLING SPOTSYLVANIA FOR ANOTHER EMS
 (Incident Notification) Call#: -1994 ; EMS-Extremity Injury ; 118 FREESTYLE LN ; ; Box 407 ; Info:  DOG ON SITE...  Closed APCO Case: -1994  APCO Narrative: Is the p
 (Incident Notification) Call#: -1991 ; MVC-Injury ; JEFFERSON HWY ; ; Box 501 ; Info:  SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO
 (Incident Notification) Call#: -1979 ; EMS-Chest Pain/Cardiac Problem ; 3411 HOLLY GROVE DR ; ; Box 405 ; Info:  Opened APCO Case: -1979  E911 Info - Class of Servic
 (Incident Notification) Call#: -1763 ; F-Alarm-Residential ; 7265 CROSS COUNTY RD ; ; Box 407 ; Info:  KEYPAD FIRE ALARM GAIL COOLEY TRIED TO CALL WENT TO  VM
 (Incident Notification) Call#: -1668 ; EMS-Pregnancy/Childbirth ; 298 WINDYKNIGHT RD CTOR ; ; Box 509 ; Info:  HAS NO IDEA IF SHE FEELS LIKE SHE WANTS TO PUSH  23 YO
 (Incident Notification) Call#: -1542 ; EMS-Seizure ; 342 SHELTON HILL RD ; ; Box 501 ; Info:  E911 Info - Class of Service: WPH2 Special Response Info: (540) 511-701
 (Incident Notification) Call#: -2095 ; EMS-Seizure ; 172 DAVIS HWY ; DOLLAR GENERAL - MINERAL; Box 201 ; Info:  GREY VAN  3YOM SIEZING
 Subject:Incident Notification\nCall#: -3239 ; EMS-Trauma with Injury ; 17383 JEFFERSON HWY ; ; Box 509 ; Info: \r
 (Incident Notification) Call#: -1991 ; MVC-Injury ; JEFFERSON HWY ; ; Box 501 ; Info:  SPOKE TO VSP DISPATCH -- RESCUE NEEDED -- NO DEPUTY  CALLER WAS A PASSERBY WHO
(Incident Notification) Call#: -3951 ; EMS-Chest Pain/Cardiac Problem ; 1503 BETHANY CHURCH RD ; ; ; Info:  NO HX OF HEART PROBLEMS  NO RECENT INJURY OR TRAUMA  AWAK
 
 ***/
 
 public class VALouisaCountyParser extends FieldProgramParser {
 
   
   public VALouisaCountyParser() {
     super(CITY_CODES, "LOUISA COUNTY", "VA",
           "Call#:ID! CALL! ADDR/S! PLACE BOX! Info:INFO!");
   }
   
   @Override
   public String getFilter() {
     return "911@louisa.org,Dispatch@louisa.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("Incident Notification")) return false;
     int pt = body.indexOf(" Closed APCO Case:");
     if (pt < 0) pt = body.indexOf(" Opened APCO Case:");
     if (pt >= 0) body = body.substring(0,pt).trim();
     return parseFields(body.split(";"), data);
   }
   
   // ID field, confirm has correct form
   private class MyIdField extends IdField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("-")) field = field.substring(1).trim();
       super.parse(field, data);
     }
   }
   
  private class MyBoxField extends BoxField {
    @Override
    public void parse(String field, Data data) {
      if (field.length() == 0) return;
      if (!field.startsWith("Box ")) abort();
      field = field.substring(4).trim();
      super.parse(field, data);
    }
  }
  
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
    if (name.equals("BOX")) return new MyBoxField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CTOR", ""
   });
 }
