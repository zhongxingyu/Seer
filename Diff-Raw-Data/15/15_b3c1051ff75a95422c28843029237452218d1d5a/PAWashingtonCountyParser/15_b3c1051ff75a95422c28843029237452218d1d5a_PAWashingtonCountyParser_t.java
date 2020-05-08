 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Washington County, PA
 Contact: James Chabalie <jchabalie@cecil3fire.net>
 Sender: company10paging
 Location: 116 GRANT ST MCDO Xstreet: NORTH ST STATION ST TYPE: STRUCTURE FIRE TIME: 23:42:41 Comments: IN THE BACK POSS 112 OR 114
 Location: 50 STONECREST LN CECI Xstreet: MCCONNELL RD DEAD END TYPE: FIRE TIME: 23:40:09 Comments:
 Location: 1647 MILLERS RUN RD MTPL: @KEEPSAKE ARABIANS Xstreet: PLEASANT RD FORT CHERRY RD TYPE: FIRE ALARM TIME: 22:31:54 Comments:
 Location: 22 2ND ST CECI Xstreet: SCHOOL ST BOYCE ST TYPE: STRUCTURE FIRE TIME: 22:40:10 Comments:
 Location: 345 SOUTHPOINTE BLVD CECI: @LOCAL 23 UFCW Xstreet: SOUTHPOINTE BLVD NB SOUTHPOINTE BLVD NB TYPE: FIRE ALARM TIME: 11:39:27 Comments:
 Xstreet: REISSING RD SR 50 TYPE: GRASS OR FIELD FIRE TIME: 15:07:34 Comments: ON REISSING NEAR SR 50
 Xstreet: REISSING RD TAYLOR DR TYPE: VEHICLE ACCIDENT WITH UNKNOWN INJURIES TIME: 05:36:10 Comments:
 Xstreet: SR 980 HIGHLAND DR TYPE: VEHICLE ACCIDENT WITH INJURIES TIME: 14:17:43 Comments: JUST PASSED HIGHLAND HEADED TOWARD CECIL
 
Contact: Brad Simpson <bradsimpson301@gmail.com>
Sender: 7247471210
(911) Location: 200 TANDEM VILLAGE RD NSTR: @PCH 48 CONSULATE BLDG 2 Xstreet: LINDEN CREEK RD DEAD END TYPE: FIRE ALAR
(911) Location: 100 MEDICAL BLVD NSTR: @CANONSBURG HOSPITAL Xstreet: BOONE AVE HILL CHURCH HOUSTON RD TYPE: FIRE ALARM

 */
 
 public class PAWashingtonCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CECI", "CECIL TWP",
       "MCDO", "MCDONALD",
      "MTPL", "MT PLEASANT TWP",
      "NSTR", "NORTH STRAWBANE TWP"
   });
   
   public PAWashingtonCountyParser() {
     super("WASHINGTON COUNTY", "PA",
           "Location:ADDR? Xstreet:X? TYPE:CALL! TIME:SKIP COMMENTS:INFO");
   }
   
   @Override
   public String getFilter() {
    return "company10paging,7247471210";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Regular parseMsg processing, but return failure if we didn't get an address
     // from either a Location or Xstreet
     if (!super.parseMsg(body, data)) return false;
     if (data.strAddress.length() == 0)  return false;
     return true;
   }
   
   // Address field parser
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String fld, Data data) {
       Parser p = new Parser(fld);
       data.strPlace = p.getLastOptional(":");
       if (data.strPlace.startsWith("@")) data.strPlace = data.strPlace.substring(1).trim(); 
       data.strCity = convertCodes(p.getLast(' '), CITY_CODES); 
       super.parse(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY PLACE";
     }
   }
   
   // Cross street field
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       String saveAddr = data.strAddress;
       parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END | FLAG_IMPLIED_INTERSECT, field, data);
       if (saveAddr.length() > 0) {
         data.strCross = data.strAddress;
         data.strAddress = saveAddr;
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
 }
