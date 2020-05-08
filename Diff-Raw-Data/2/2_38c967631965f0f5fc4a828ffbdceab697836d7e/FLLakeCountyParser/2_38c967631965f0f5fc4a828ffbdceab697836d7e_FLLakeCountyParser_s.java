 package net.anei.cadpage.parsers.FL;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Lake County, FL
 Contact: 3522354040@messaging.sprintpcs.com
 Sender: varies
 
 CAD:ST56* Unconscious / Fainting* 805 PLUMOSA AVENUE* FRUITLAND PARK*
 CAD:ST56* Miscellaneous* 303 URICK STREET* FRUITLAND ACRES* APT 4F* FRUITLAND PARK*
 CAD:ST56* Traffic / Transportation Accident* 305 CR 466A* CENTRAL FLORIDA NURSERY* FRUITLAND PARK*
 CAD:ST56* Sick Person* 500 WEST FOUNTAIN STREET* FRUITLAND PARK*
 CAD:PS4* ST56* Sick Person* 201 EAST LAVISTA STREET* NEW LIFE PRESBYTERIAN CHURCH* FRUITLAND PARK*
 
  */
 
 public class FLLakeCountyParser extends FieldProgramParser {
   
   private static final String[] CITY_LIST = new String[] {
     "ASTATULA",
     "CLERMONT",
     "EUSTIS",
     "FRUITLAND PARK",
     "GROVELAND",
     "HOWEY-IN-THE-HILLS",
     "LADY LAKE",
     "LEESBURG",
     "MASCOTTE",
     "MINNEOLA",
     "MONTVERDE",
     "MOUNT DORA",
     "TAVARES",
     "UMATILLA",
     "ALTOONA",
     "ASTOR",
     "BASSVILLE PARK",
     "CITRUS RIDGE",
     "FERNDALE",
     "GRAND ISLAND",
     "LAKE KATHRYN",
     "LAKE MACK-FOREST HILLS",
     "LANIER",
     "LISBON",
     "MOUNT PLYMOUTH",
     "OKAHUMPKA",
     "ORANGE BEND",
     "PAISLEY",
     "PINE LAKES",
     "PITTMAN",
     "SILVER LAKE",
     "SORRENTO",
     "YALAHA"
   };
   
   public FLLakeCountyParser() {
     super(CITY_LIST, "LAKE COUNTY", "FL",
        "CH? UNIT CALL ADDR! MISC+? CITY");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (!body.startsWith("CAD:")) return false;
     body = body.substring(4).trim();
     return parseFields(body.split("\\* "), data);
   }
   
   private static final Pattern CHANNEL_PTN = Pattern.compile("PS.*"); 
   private class MyChannelField extends ChannelField {
     
     public MyChannelField() {
       setPattern(CHANNEL_PTN);
     }
     
   }
   
   private class MiscField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("APT ")) {
         data.strApt = field.substring(4).trim();
       } else {
         data.strName = field;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "NAME APT";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CH")) return new MyChannelField();
     if (name.equals("MISC")) return new MiscField();
     return super.getField(name);
   }
 }
