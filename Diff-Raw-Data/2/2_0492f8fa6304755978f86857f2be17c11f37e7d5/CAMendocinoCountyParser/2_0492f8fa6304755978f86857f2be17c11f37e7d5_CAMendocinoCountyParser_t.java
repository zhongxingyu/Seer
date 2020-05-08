 package net.anei.cadpage.parsers.CA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Mendocino County, CA
 Contact: Mike Maynard <capttwiggy@gmail.com>
 Sender: meucad@fire.ca.gov
 System: Alturas CAD
 
 (CAD Page) OTHER MISC. DISPLAY; =L(39.107949,-123.087224) ,UKV (COW MOUNTAIN); Inc# X: -123 5.23344  Y: 39 6.47694 B1113; testing with b1113
 MEDICAL, BRAVO; COVELO FIREHOUSE @ 75900 HWY 162 ,COVL ; Inc# 004042X: -123 14.8924  Y: 39 47.2236 B1113 CVL E1151; 42 YOF DIFF BREATHING
 MEDICAL, BRAVO; 76415 LOVELL ST ,COVL ; Inc# 004038X: -123 15.1425  Y: 39 47.5964 B1113 CVL E1151; 97 YOF GLF BRUISING ON THIGH
 PA, AGENCY; ROUND VALLEY AIRPORT @ =L(39.789877,-123.267513) ,COVL ; Inc# 004014X: -123 16.0507  Y: 39 47.3926 B1113 CVL E1151; wht 1 eng sieries 22 a small plain tale #  N1463C see if plane has landed   and advise mcso
 FIRE, RESIDENTIAL ALARM; 31110BLK  BRUSH CREEK RD ,FORTB ; Inc# 004013X: -123 46.6691  Y: 39 25.7574 B1113 BRG E1152 ADMIN; 31171 living room smoke dector
 FIRE, RESIDENTIAL; 341 MACMILLAN DR ,HOPL ; Inc# 004007X: -123 7.29456  Y: 38 58.0572 B1113 HOP T96 HEL101 E1153 E1165 E1453 E1161 CCR4 DOZ1242 ADMIN; structure fire multi caller
 FIRE, WILDLAND; 32151 PUDDING CREEK RD ,FORTB ; Inc# 004005X: -123 47.5662  Y: 39 27.0477 B1113 BRG E1152 E1156 E1154 E1168 PFK1 HEL101 ADMIN; brow shirt wht sleeves older jen 60-70 wb on pud creek rd sta
 MED, TRAFFIC COLLISION; HWY 101 MP 100.25 @ =L(39.930402,-123.762749) ,PIERC ; Inc# 003522X: -123 45.7649  Y: 39 55.8241 B1113; testing cad paging
 MED, TRAFFIC COLLISION; 802 S MAIN ST / 2 HWY 20 ,WILL ; Inc# 003522X: -123 21.0982  Y: 39 24.2470 B1113; testing cad paging
 MEDICAL, BRAVO; =L(39.126853,-123.129230) ,UKV ; Inc# 003823X: -123 7.7538  Y: 39 7.61118 B1113; mill creek by upper pond adult male leg inj 3rd party report
 FIRE, VEH PASSENGER; SQUAW ROCK @ =L(38.912401,-123.056112) ,HOPL ; Inc# 003858X: -123 3.36672  Y: 38 54.7440 B1113 HOP CLD ADMIN; big rig fire - chp responding
 
  */
 
 public class CAMendocinoCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "COVL",  "COVELO",
       "FORTB", "FORT BRAGG",
       "HOPL",  "HOPLAND",
       "PIERC", "PIERCY",
       "UKV",   "",
       "WILL",  "WILLITS"
   });
   
   public CAMendocinoCountyParser() {
    super(CITY_CODES, "MENDOCINO COUNTY", "CA",
            "CALL ADDR! Inc:IDGPSUNIT! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "meucad@fire.ca.gov";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = body.replace(" Inc# ", " Inc: ");
     return parseFields(body.split(";"), data);
   }
   
   private static final Pattern TRAIL_PARENS = Pattern.compile("\\(([^\\(]*?)\\)$");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = TRAIL_PARENS.matcher(field);
       if (match.find()) {
         data.strPlace = match.group(1);
         field = field.substring(0,match.start()).trim();
       }
       Parser p = new Parser(field);
       data.strPlace = append(p.getOptional('@'), " / ", data.strPlace);
       data.strCity = convertCodes(p.getLastOptional(','), CITY_CODES);
       parseAddress(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE ADDR CITY";
     }
   }
   
   private static final Pattern ID_GPS_UNIT_PTN =
     Pattern.compile("(\\d*) *(X: [- 0-9.]+ Y: [- 0-9.]+)(.*)");
   private class IdGpsUnitField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       Matcher match = ID_GPS_UNIT_PTN.matcher(field);
       if (match.matches()) {
         data.strCallId = match.group(1).trim();
         data.strGPSLoc = match.group(2).trim();
         data.strUnit = match.group(3).trim();
       }
     }
     
     @Override
     public String  getFieldNames() {
       return "ID GPS UNIT";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("IDGPSUNIT")) return new IdGpsUnitField();
     return super.getField(name);
   }
 }
