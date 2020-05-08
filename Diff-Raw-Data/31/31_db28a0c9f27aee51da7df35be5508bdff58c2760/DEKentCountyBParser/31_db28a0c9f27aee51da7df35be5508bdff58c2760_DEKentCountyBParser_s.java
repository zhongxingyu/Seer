 package net.anei.cadpage.parsers.DE;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchAegisParser;
 
 /**
  * Kent County, DE
  */
 public class DEKentCountyBParser extends DispatchAegisParser {
   
   private static final Pattern MISSING_DELIM = Pattern.compile("(?<!-) +(?=Cross Sts:|Xst's:|Caller:)"); 
   
   private boolean good;
   
   private String select;
   
   public DEKentCountyBParser() {
     super(CITY_LIST, "KENT COUNTY", "DE",
            "( SELECT/B Unit:UNIT! Status:ADDR/S2CP! Venue:CITY! Dev/Sub:MAP | CODE? CALL+? ADDR2! PLACE ) Xst's:X Caller:NAME");
   }
   
   @Override
   public String getSelectValue() {
     return select;
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!body.startsWith("-")) return false;
     good = subject.length() > 0;
     if (!good) subject = "Chief ALT|";
     
     if (body.startsWith("- Unit:")) {
       select = "B";
       if (!super.parseMsg(body, data)) return false;
     } else {
       select = "A"; 
       body = MISSING_DELIM.matcher(body).replaceAll(" - ");
       body = body.replace(" - Cross Sts:", " - Xst's:");
       if (!super.parseMsg(subject,  body, data)) return false;
     }
     return good;
   }
   
   // Call field concatenates with dashes
   // And extract possible leading code
   private static final Pattern CODE_PTN = Pattern.compile("^(\\d{1,2}[A-Z]\\d{1,2}) ");
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       if (data.strCode.length() == 0) {
         Matcher match = CODE_PTN.matcher(field);
         if (match.find()) {
           data.strCode = match.group(1);
           field = field.substring(match.end()).trim();
         }
       }
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   // Address field may have comma delimited city name
   private static final Pattern ADDRESS_CITY_DELIM = Pattern.compile(",|\\bVenue:");
   private class MyAddressField extends AddressField {
     
     @Override
     public boolean checkParse(String field, Data data) {
 
       // We always look for a call description in front of this field that will be appended
       // to an existing call description.  If there is no existing call description, the
       // call description must be here
       int flags = FLAG_ANCHOR_END;
       String prevCall = data.strCall;
       if (prevCall.length() == 0) flags |= FLAG_START_FLD_REQ;
       
       // Check for trailing place name separated by colon
       int pt = field.lastIndexOf(':');
       if (pt >= 0) {
         data.strPlace = field.substring(pt+1).trim();
         field = field.substring(0,pt).trim();
       }
       
       // See if there is a comma separating the city from the address
       // If there is, this is a confirmed address field
       Matcher  match = ADDRESS_CITY_DELIM.matcher(field);
       if (match.find()) {
         String city = field.substring(match.end()).trim();
         if (!CITY_SET.contains(city.toUpperCase())) return false;
         field = field.substring(0,match.start()).trim();
         data.strCall = "";
         parseAddress(StartType.START_CALL, flags, field, data);
         data.strCall = append(prevCall, " - ", data.strCall);
         data.strCity = city;
         return true;
       }
       
       
       // We will have to try parsing this.  If we find a city, it is considered good
       else {
         Result res = parseAddress(StartType.START_CALL, flags, field);
         if (res.getCity().length() == 0) return false;
         data.strCall = "";
         res.getData(data);
         data.strCall = append(prevCall, " - ", data.strCall);
         return true;
       }
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " CITY";
     }
   }
   
   public class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       good = true;
       if (field.equals("None")) return;
       super.parse(field,  data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CODE")) return new CodeField("\\d{1,2}[A-Z]\\d{1,2}");
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR2")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     
     // Cities
     "DOVER",
     "HARRINGTON",
     "MILFORD",
     
     // Towns
     "BOWERS",
     "CAMDEN",
     "CHESWOLD",
     "CLAYTON",
     "FARMINGTON",
     "FELTON",
     "FREDERICA",
     "HARTLY",
     "HOUSTON",
     "KENTON",
     "LEIPSIC",
     "LITTLE CREEK",
     "MAGNOLIA",
     "SMYRNA",
     "VIOLA",
     "WOODSIDE",
     "WYOMING",
     
     // Census - designated places
     "DOVER AIR FORCE BASE",
     "HIGHLAND ACRES",
     "KENT ACRES",
     "RISING SUN-LEBANON",
     "RIVERVIEW",
     "RODNEY VILLAGE",
     "WOODSIDE EAST",
     "[EDIT]OTHER LOCALITIES",
     "ANDREWVILLE",
     "BERRYTOWN",
     "LITTLE HEAVEN",
     "MARYDEL"
   };
   
   private static final Set<String> CITY_SET = new HashSet<String>(Arrays.asList(CITY_LIST));
 }
