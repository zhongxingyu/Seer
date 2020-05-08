 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Frederick County, MD
  */
 public class MDFrederickCountyParser extends FieldProgramParser {
 
   public MDFrederickCountyParser(){
     super(CITY_CODES, "FREDERICK COUNTY", "MD",
           "CT:ADDR! TIME:TIME? ESZ:BOX? MAP:MAP Disp:UNIT");
   }
 
   @Override
   public String getFilter() {
     return "www.codemessaging.net,CAD@psb.net,@c-msg.net,messaging@iamresponding.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (subject.length() > 0) {
       String[] subjects = subject.split("\\|");
       if (subjects.length > 2) return false;
       if (subjects.length == 2) {
         if (subjects[0].equals("CAD")) subject = subjects[1];
         else if (subjects[1].equals("CAD")) subject = subjects[0];
         else return false;
       }
       data.strSource = subject;
     }
     
     if (!super.parseMsg(body, data)) return false;
     
     // Parser is getting pretty sloppy, anything starting with CT: will pass
     // this.  Let's make sure they have at least one additional field
     return data.strTime.length() > 0 ||
             data.strBox.length() > 0 ||
             data.strMap.length() > 0 ||
             data.strUnit.length() > 0;
   }
   
   // Address field gets complicated
   private static final Pattern CITY_PTN = Pattern.compile(" ([A-Z0-9]{4}(?: CO)?):(?: @)?");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
 
       // Everything changes with a mutual aid call
       // should be followed by a city: address
       int ipt = field.indexOf(" @MA ");
       if (ipt >= 0) {
         data.strCall = "Mutual Aid: " + field.substring(0, ipt).trim();
         int ipt2 = field.indexOf(':', ipt);
         if (ipt2 < 0) {
           data.strCity = convertCodes(field.substring(ipt+5).trim(), CITY_CODES);
         } else {
           data.strCity = convertCodes(field.substring(ipt+5, ipt2).trim(), CITY_CODES);
           field = field.substring(ipt2+1).trim().replaceAll("@", "");
           parseAddress(field, data);
         }
       }
   
       else {
         
         // Not mutual aid.
         // See if we can find a city: marker
         Matcher match = CITY_PTN.matcher(field);
         String sCity = null;
         if (match.find()) {
           sCity = CITY_CODES.getProperty(match.group(1));
           if (sCity != null) {
             data.strCity = sCity;
             
             // If we find one, things get complicated...
             // Sometimes it marks the boundary between the call description and address
             ///Sometimes it marks the boundary between the address and a place name
             String part1 = field.substring(0,match.start()).trim();
             String part2 = field.substring(match.end()).trim();
             Result res = parseAddress(StartType.START_ADDR, part2);
             if (res.getStatus() > 0) {
               data.strCall = part1;
               res.getData(data);
               data.strPlace =  res.getLeft();
             } else {
               parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, part1, data);
               data.strPlace = part2;
             }
           }
         }
         
         // If we didn't find a city parse a generic call/address/place field
         if (sCity == null) {
           int pt = field.indexOf(": @");
           if (pt >= 0) {
             data.strPlace = field.substring(pt+3).trim();
             field = field.substring(0,pt);
             parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ANCHOR_END, field, data);
           } else {
             parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, field, data);
             data.strPlace = getLeft();
           }
         }
       }
       
       // If call is to Franklin or Adams county, set state to PA
       if (data.strCity.equals("Franklin County") || data.strCity.equals("Adams County")) {
         data.strState = "PA";
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CALL ADDR APT CITY ST PLACE";
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       if (field.contains("[") && field.contains("]")) {
         field = field.substring(0,field.indexOf("[")).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private static final Properties CITY_CODES = 
     buildCodeTable(new String[]{
         "ADAM","Adams County", //PA
         "ADAM CO", "Adams County",
        "ADCO","Adams County", //PA
         "BRUN","Brunswick",   
         "CARRCO", "Carroll County",
         "CEMB","Emmitsburg",
         "CFR1","Frederick City",
         "CFR2","Frederick City",
         "CFR3","Frederick City",
         "CFR4","Frederick City",
         "CIJM", "New Market",
         "CMON", "Monrovia",
         "CNMA","New Market",
         "CMTY","Mt Airy",
         "CSAB","Sabillasville",
         "CTHU","Thurmont",
         "CWAL","Walkersville",
         "EMMB", "Emmitsburg",
         "FRAN CO", "Franklin County",
         "FRE1", "Frederick City",
         "FRE2", "Frederick City",
         "MTAY", "Mt Airy",
         "NEWM", "New Market",
         "THUR","Thurmont",
         "WOOD","Woodsboro",
   });
 }
