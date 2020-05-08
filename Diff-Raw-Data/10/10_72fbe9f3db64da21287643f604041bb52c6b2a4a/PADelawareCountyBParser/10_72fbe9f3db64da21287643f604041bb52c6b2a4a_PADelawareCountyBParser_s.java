 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 public class PADelawareCountyBParser extends FieldProgramParser {
   
   private static final Pattern CAP_PTN = Pattern.compile("^(\\d{7}) ");
   
   private AddressField addressField;
   
   private boolean crossAddress;
   
   public PADelawareCountyBParser() {
     super(CITY_CODES, "DELAWARE COUNTY", "PA",
            "ADDR/S X1:XADDR? X2:X? Nature:CALL! Time:TIME Notes:INFO? Inc:ID");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     Matcher match = CAP_PTN.matcher(body);
     if (match.find()) {
       data.strUnit = match.group(1);
       body = body.substring(match.end()).trim();
     }
     
     crossAddress = false;
     return super.parseMsg(body.replace('\n', ' '), data);
   }
   
   @Override
   public String getProgram() {
     return "UNIT " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strPlace = p.getLastOptional(':');
       if (data.strPlace.startsWith("@")) data.strPlace = data.strPlace.substring(1).trim();
       data.strApt = p.getLastOptional(',');
       super.parse(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY APT PLACE";
     }
   }
   
   private class CrossAddressField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.length() == 0) {
         crossAddress = true;
         addressField.parse(field, data);
       } else {
         super.parse(field, data);
       }
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (crossAddress) {
         data.strAddress = append(data.strAddress, " & ", field);
       } else {
         super.parse(field, data);
       }
     }
   }
   
   private class MyIdField extends IdField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(" Beat ");
       if (pt >= 0) {
         data.strBox = field.substring(pt+6).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "ID BOX";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (addressField == null) addressField = new MyAddressField();
     if (name.equals("ADDR")) return addressField;
     if (name.equals("XADDR")) return new CrossAddressField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "AL", "ALDAN",
       "AS", "ASTON TWP",
       "BE", "BETHEL TWP",
       "BM", "BIRMINGHAM TWP",
       "BR", "BROOKHAVEN",
       "CC", "CHESTER",
       "CF", "CHADDS FORD TWP",
       "CH", "CHESTER HEIGHTS",
       "CL", "CLIFTON HEIGHTS",
       "CN", "CONCORD TWP",
       "CO", "COLLINGDALE",
       "CW", "COLWYN",
       "DB", "DARBY",
       "DT", "DARBY TWP",
       "ED", "EDDYSTONE",
       "EG", "EDGEMONT TWP",
       "EL", "EAST LANSDOWNE",
      "ES", "ESSINGTON", // SECTION OF TINNICUM TWP
       "FL", "FOLCROFT",
       "GL", "GLENOLDEN",
       "HV", "HAVERFORD TWP",
       "LA", "LANSDOWNE",
       "LC", "LOWER CHICHESTER TWP",
      "LS", "LESTER", // SECTION OF TINNICUM TWP
       "MB", "MILLBOURNE",
       "MD", "MIDDLETOWN TWP",
       "ME", "MEDIA",
       "MH", "MARCUS HOOK",
       "MO", "MORTON",
       "MP", "MARPLE TWP",
       "NP", "NETHER PROVIDENCE TWP",
       "NT", "NEWTOWN TWP",
       "NW", "NORWOOD",
       "PK", "PARKSIDE",
       "PP", "PROSPECT PARK",
       "RN", "RADNOR TWP",
       "RP", "RIDLEY PARK",
       "RT", "RIDLEY TWP",
       "RU", "RUTLEDGE",
       "RV", "ROSE VALLEY",
       "SH", "SHARON HILL",
       "SP", "SPRINGFIELD TWP",
       "SW", "SWARTHMORE",
       "TB", "THORNBURY TWP",
       "TC", "CHESTER TWP",
       "TD", "TREDYFFRIN TWP",
      "TN", "TINNICUM TWP",
       "TR", "TRAINER",
       "UC", "UPPER CHICHESTER TWP",
       "UD", "UPPER DARBY TWP",
       "UL", "UPLAND",
       "UP", "UPPER PROVIDENCE TWP",
       "YE", "YEADON",
       
       "NCC", "NEW CASTLE COUNTY"
     
   });
 }
