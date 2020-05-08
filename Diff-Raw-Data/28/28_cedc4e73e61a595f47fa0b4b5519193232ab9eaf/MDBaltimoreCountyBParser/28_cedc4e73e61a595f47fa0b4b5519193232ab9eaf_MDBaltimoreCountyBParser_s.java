 package net.anei.cadpage.parsers.MD;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Baltimore County, MD
  */
 public class MDBaltimoreCountyBParser extends FieldProgramParser {
   
   public MDBaltimoreCountyBParser() {
     super("BALTIMORE COUNTY", "MD",
           "BOX:BOX CALL:CALL! ADDR:ADDR! UNIT:UNIT! INFO:INFO ID:ID%");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       
       field = UPDATED_LOC.matcher(field).replaceAll("");
       
       // Slit address field by semicolon
       String[] parts = SEPARATOR.split(field, -1);
       if (parts.length > 3) abort();
       
       // Address consists of an optional place followed by the
       // address followed by an optional cross street  and we
       // have to figure just where the address is
       
       // There are a couple tricks, but most reliable is finding
       // and appt marker, so we will look for that first.
       int addrIndex = -1;
       for (int ndx = 0; ndx < parts.length; ndx++) {
         String part = parts[ndx];
         Matcher match = APT_MARKER.matcher(part);
         if (match.find()) {
           String apt = part.substring(match.end());
           parts[ndx] = part.substring(0,match.start());
           String mark = match.group(1);
           char key = mark.charAt(0);
           if (key != 'A' && key != 'R') apt = append(mark, " ", apt);
           data.strApt = apt;
           
           addrIndex = ndx;
           break;
         }
       }
       
       // If that didn't find it, the only ambiguous case is when there are two field parts
       // in which case we will check al kinds of things
       if (addrIndex < 0) {
         if (parts.length == 1) addrIndex = 0;
         else if (parts.length == 3) addrIndex = 1;
         else if (parts[1].contains("*")) addrIndex = 0;
         else if (checkAddress(parts[0]) > checkAddress(parts[1])) addrIndex = 0;
         else addrIndex = 1;
       }
       
       // Sanity check to handle obscure implausible combinations
       if (addrIndex > 1 || parts.length-addrIndex > 2) abort();
       
       // Now put all the parts where they belong
       if (addrIndex > 0) data.strPlace = parts[0];
       parseAddress(parts[addrIndex], data);
       int crossNdx = addrIndex+1;
       if (crossNdx < parts.length) {
         String cross = parts[crossNdx];
         if (cross.startsWith("0 ")) cross = cross.substring(2).trim();
         cross = cross.replace('*', '/');
         data.strCross = cross;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames() + " APT X";
     }
   }
   private static final Pattern SEPARATOR = Pattern.compile(" *; *");
   private static final Pattern UPDATED_LOC = Pattern.compile("/ UPDATED LOC\\b");
   private static final Pattern APT_MARKER = Pattern.compile(" +(APT|ROOM|RM|BLDG|SUITE|CONDO) +");
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       // Break field up into blank delimited tokens
       List<String> unitList = new ArrayList<String>();
       for (String part : field.split(" +")) {
         
         // Skip single digits
         if (part.length() == 1 && Character.isDigit(part.charAt(0))) continue;
         
         // Skip duplicate box number
         if (part.equals(data.strBox)) continue;
         
         // If it starts with FS add to source field
         if (part.startsWith("FS")) {
           data.strSource = append(data.strSource, " ", part);
           continue;
         }
         
         // Add to unit list unless it is already in there somewhere
         if (!unitList.contains(part)) unitList.add(part);
       }
       
       // Finally unpack list into unit field
       StringBuilder sb = new StringBuilder();
       for (String part : unitList) {
         if (sb.length() > 0) sb.append(' ');
         sb.append(part);
       }
       data.strUnit = sb.toString();
     }
     
     @Override
     public String getFieldNames() {
       return "UNIT SRC";
     }
   }
   
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = INFO_TIME_PTN.matcher(field);
       if (match.find()) {
         data.strTime = match.group(1);
         if (match.start() == 0) field = field.substring(match.end());
         else if (match.end() == field.length()) field = field.substring(0,match.start());
         else field = field.substring(0,match.start()) + " " + field.substring(match.end());
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "INFO TIME";
     }
   }
   private static final Pattern INFO_TIME_PTN = Pattern.compile(" *\\b(\\d\\d:\\d\\d:\\d\\d)\\b *");
 
   @Override
   protected Field getField(String name) {
     if (name.equals("BOX")) return new BoxField("[A-Z0-9]\\d{2}-\\d{2}", true);
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }
