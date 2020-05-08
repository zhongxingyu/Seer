 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Madison County, NY
 Contact: "Markes, David R." <drmarkes@cazenovia.edu>
 Sender: cazfd@fdcms.com   and   cazfire1@windstream.net
 
 (Dispatch) Dispatch ** 15:16 ** CHEST PAIN ** 3578 MUTTON HILL RD , FENNER ( FRANCIS RD / WYSS R ** - ** KENT, GEORGE SR ** 315-655-2533 **
 (Dispatch) Dispatch ** 17:21 ** ALARM - FIRE - COLLEGE ** 22 SULLIVAN , CAZENOVIA VILLAGE ( GREEN /) ** - ** CAZENOVIA COLLE ** 315-655-7271 **
 Dispatch ** 21:35 ** BREATHING PROBLEM ** @HUBBARD HALL (5 SULLIVAN ST (CAZENOVIA VILLAGE ) ** HUBBARD HALL- **  **  **
 Dispatch ** 21:42 ** WATER PROBLEM ** 3243 ROUTE 20 , NELSON ** - ** michael manion ** 6559325 **
 Dispatch ** 16:22 ** BREATHING PROBLEM ** 4188 MEADOW HILL RD , CAZENOVIA ( / ROUTE 20) ** - **  **  **
 Dispatch ** 21:42 ** WATER PROBLEM ** 3243 ROUTE 20 , NELSON ** - ** michael manion ** 6559325 **
 
Contact: Erick Haas <erick.haas@live.com>
Sender: lfdfire@verizon.net
FRM:lfdfire@verizon.net\nMSG:13:54 ** MVA - UNKNOWN ** OXBOW RD \\ CLOCKVILLE RD (, LINCOLN) ** - **  **  **
FRM:lfdfire@verizon.net\nMSG:00:20 ** UNCONSCIOUS/FAINTING ** 6765 OXBOW RD , LINCOLN ( INGALLS CORNER ** - ** HUGHES, DUANE ** 315-697-261 ** 
FRM:lfdfire@verizon.net\nMSG:07:04 ** BREATHING PROBLEM ** 6998 NELSON RD , LINCOLN ** - ** ROSEKRANS DOUGL ** 315-697-986 ** 

 */
 
 
 public class NYMadisonCountyAParser extends FieldProgramParser {
 
  private static final Pattern MARKER = Pattern.compile("^(?:Dispatch \\*\\* )?\\d\\d:\\d\\d \\*\\* ");
  private static final Pattern BACKSLASH_PTN = Pattern.compile("\\\\+");
   
   public NYMadisonCountyAParser() {
     super("MADISON COUNTY", "NY",
            "CALL ADDR PLACE NAME PHONE");
   }
   
   @Override
   public String getFilter() {
    return "cazfd@fdcms.com,cazfire1@windstream.net,lfdfire@verizon.net";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     String[] flds = body.split("\\*\\*");
     if (flds.length < 5) return false;
     return parseFields(flds, data);
   }
   
   // Address field does all kinds of strange things
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       
       // We only count the left parens and ignore right parens
       field = field.replace(")", "");
       
       // Fields starting with @ have a completely different structure
       if (field.startsWith("@")) {
         String[] flds = field.split("\\(");
         if (flds.length < 2) abort();
         data.strPlace = flds[0].substring(1).trim();
         parseAddress(flds[1].trim(), data);
         if (flds.length > 2) data.strCity = flds[2].trim();
         if (flds.length > 3) data.strCross = flds[3].trim();
       }
       
       // Regular field format
       else {
         Parser p = new Parser(field);
         String sAddr = p.getOptional(',');
        if (sAddr.endsWith("(")) sAddr = sAddr.substring(0,sAddr.length()-1);
         if (sAddr.length() == 0) abort();
        sAddr = BACKSLASH_PTN.matcher(sAddr).replaceAll("&");
         parseAddress(sAddr, data);
         data.strCity = p.get('(');
         String sCross = p.get();
         if (sCross.startsWith("/")) sCross = sCross.substring(1).trim();
         if (sCross.endsWith("/")) sCross = sCross.substring(0, sCross.length()-1).trim();
         data.strCross = sCross;
       }
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE ADDR CITY X";
     }
   }
   
   // Place field needs to strip off trailing dash
   // and is complete ignored if place was set in address field parsing
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       if (data.strPlace.length() > 0) return;
       if (field.endsWith("-")) field = field.substring(0, field.length()-1).trim();
       super.parse(field, data);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("PLACE")) return new MyPlaceField();
     return super.getField(name);
   }
   
 }
 	
