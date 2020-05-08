 package net.anei.cadpage.parsers.NJ;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Camden County, NJ
 Contact: "N.dalonzo" <nfdfire45@comcast.net>
 Sender: cccademail@camdencounty.com 
 
 (Dispatch SD45) DWELLING\n312 9TH ST ,14\n#:\nX:CENTER/CHERRY\nZN:14A\nCP:  2011-03-31 10:44:12\nMI#:110073123\nRES#:SD45
 (Dispatch SD45) M.V.A\nROUTE 295 SB RAMP TO 76 SB ALJO CUR ,04\n#:\nX:/\nZN:04Q\nCP:ROUTE 295 SB RAMP T  2011-03-31 09:06:36\nMI#:110073062 
 (Dispatch SD45) DIABETIC\n217 BLACK HORSE PK N ,25\n#:4\nX:CARLISLE/LAKEVIEW\nZN:25B\nCP:MAX GROUP PHILLY CO  2011-03-30 13:23:26\nMI#:11007
 (Dispatch SD45) DWELLING\nBURLINGTON ST S/MONMOUTH ST ,14\n#:\nX:/\nZN:14A\nCP:  2011-03-30 10:33:28\nMI#:110072287\nRES#:SD45
 
Contact: Steve Kane <captsteevo@gmail.com>
(Dispatch QT60) APARTMENT  \n1800 LAUREL RD ,22   \n#:114  \nX:BLACKWOOD CLEMENTON/  \nZN:22B  \nCP:STONINGTON COURT AP  2012-03-09 07:24:49  \nMI#:

 */
 
 public class NJCamdenCountyAParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("Dispatch (.*)"); 
   
   public NJCamdenCountyAParser() {
     super("CAMDEN COUNTY", "NJ",
           "CALL ADDR/SXP! #:APT X:X! ZN:MAP! CP:PLACE MI:ID RES:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "cccademail@camdencounty.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     Matcher match = SUBJECT_PTN.matcher(subject);
     if (!match.matches()) return false;
     data.strUnit = match.group(1);
     
     body = body.replace("MI#:", "MI:").replace("RES#:", "RES:");
     return parseFields(body.split("\n"), data) && data.strAddress.length() > 0;
   }
   
   private static final Pattern CITY_PTN = Pattern.compile(",(\\d\\d)(?: \\d\\d)?$");
   private class MyAddressField extends AddressField {
 
     @Override
     public void parse(String field, Data data) {
       Matcher match = CITY_PTN.matcher(field);
       if (match.find()) {
         Integer ndx = Integer.parseInt(match.group(1));
         if (ndx > 0 && ndx <= CITY_LIST.length) {
           data.strCity = CITY_LIST[ndx-1]; 
         }
         field = field.substring(0,match.start()).trim();
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY"; 
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (! field.equals("/")) super.parse(field, data);
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("\\b\\d{4}-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d$");
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = DATE_TIME_PTN.matcher(field);
       if (match.find()) field = field.substring(0,match.start()).trim(); 
       data.strPlace = append(data.strPlace, " ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("PLACE")) return new MyPlaceField();
     return super.getField(name);
   }
   
   // X - remove empty /
   // Place strip off date/time
 
   private static final String[] CITY_LIST = new String[]{
     /* 01 */ "Audubon",
     /* 02 */ "Audubon Park",
     /* 03 */ "Barrington",
     /* 04 */ "Bellmawr",
     /* 05 */ "Berlin",
     /* 06 */ "Berlin Twp",
     /* 07 */ "Brooklawn",
     /* 08 */ "Camden City",
     /* 09 */ "Chesilhurst",
     /* 10 */ "Clementon",
     /* 11 */ "Collingswood",
     /* 12 */ "Cherry Hill",
     /* 13 */ "Gibbsboro",
     /* 14 */ "Gloucester City",
     /* 15 */ "Gloucester Twp",
     /* 16 */ "Haddon Twp",
     /* 17 */ "Haddonfield",
     /* 18 */ "Haddon Heights",
     /* 19 */ "Hi-Nella",
     /* 20 */ "Laurel Springs",
     /* 21 */ "Lawnside",
     /* 22 */ "Lindenwold",
     /* 23 */ "Magnolia",
     /* 24 */ "Merchantville",
     /* 25 */ "Mount Ephraim",
     /* 26 */ "Oaklyn",
     /* 27 */ "Pennsauken Twp",
     /* 28 */ "Pine Hill",
     /* 29 */ "Pine Valley",
     /* 30 */ "Runnemede",
     /* 31 */ "Somerdale",
     /* 32 */ "Stratford",
     /* 33 */ "Tavistock",
     /* 34 */ "Voorhees Twp",
     /* 35 */ "Waterford Twp",
     /* 36 */ "Winslow Twp",
     /* 37 */ "Woodlyne"
   };
 
 }
