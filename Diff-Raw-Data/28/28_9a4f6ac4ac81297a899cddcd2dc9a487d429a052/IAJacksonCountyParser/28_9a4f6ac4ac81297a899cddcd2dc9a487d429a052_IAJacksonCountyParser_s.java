 package net.anei.cadpage.parsers.IA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 // We do have a DispatchSieldwarePareser class, but it can't handle the 
 // special case where a newline separates the place and city fields.  So we
 // don't use it for this one.
 public class IAJacksonCountyParser extends FieldProgramParser {
   
   private static final Pattern CALL_ID_PATTERN =
       Pattern.compile("^(\\d\\d-\\d{6}) ");
   
   public IAJacksonCountyParser() {
     super(CITY_LIST, "JACKSON COUNTY", "IA",
           "CALL! ADDR! X ( CITY | PLACECITY | PLACE CITY ) UNIT");
   }
   
   @Override
   public String getFilter() {
     return "swmail@maquoketaia.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = CALL_ID_PATTERN.matcher(body);
     if (! match.find()) return false;
     data.strCallId = match.group(1);
     body = body.substring(match.end()).trim();
     return parseFields(body.split("\n"), data);
   }
   
   private class PlaceCityField extends PlaceField {
     
     private CityField cityField = new CityField();
     
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       
       // Odd convention city is sometimes separated from place field by double
       // blanks instead of a newline
       Parser p = new Parser(field);
       String city = p.getLastOptional("  ");
       if (city.length() == 0 || !cityField.checkParse(city, data)) return false;
       super.parse(p.get(), data);
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("PLACECITY")) return new PlaceCityField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ANDREW",
     "BALDWIN",
     "BELLEVUE",
     "LA MOTTE",
     "MAQUOKETA",
     "MILES",
     "MONMOUTH",
     "PRESTON",
     "SABULA",
     "ST DONATUS",
     "SPRAGUEVILLE",
     "SPRINGBROOK",
 
     "CANTON",
     "GREEN ISLAND",
 
     "ALMA",
     "AMOY",
     "AMERICA",
     "BRIDGEPORT",
     "BROOKFIELD",
     "BUCKHORN",
     "CANTON",
     "CARROLLPORT",
     "CENTERVILLE",
     "CHARLESTON",
     "CHARKSTOWN",
     "COBB",
     "COLOMA",
     "COPPER CREEK",
     "COTTONVILLE",
     "CRABB",
     "CRABB'S MILL",
     "CRABBTOWN",
     "DEVENTERSVILLE",
     "DUGGAN",
     "DUKE",
     "EMELINE",
     "FREMONT",
     "FULTON",
     "GARRY OWEN",
     "GORDON'S FERRY",
     "HICKORY GROVE",
     "HIGGINSPORT",
     "HUGO",
     "HURSTVILLE",
     "FAIRFIELD",
     "FARMERS CREEK",
     "FRANKLIN",
     "IRON HILL",
     "EAST IRON HILLS",
     "ISABEL",
     "LAINSVILLE",
     "LOWELL",
     "MILLROCK",
     "MOUNT ALGOR",
     "NASHVILLE",
     "NEW CASTLE",
     "NEW ROCHESTER",
     "NORTH MAQUOKETA",
     "OTTER CREEK",
     "OZARK",
     "PASS",
     "PRAIRIE SPRINGS",
     "ROLLEY",
     "SILSBEE",
     "SMITHS FERRY",
     "SPRINGFIELD",
     "SPRUCE MILLS",
     "STERLING",
     "SULLIVAN",
     "SUMMER HILL",
     "SYLVA",
     "TETES DES MORTS",
     "UNION CENTER",
     "VAN BUREN",
     "WAGONERSBURGH",
     "WATERFORD",
     "WICKLIFFE",
 
     "BELLEVUE TWP",
     "BRANDON TWP",
     "BUTLER TWP",
     "FAIRFIELD TWP",
     "FARMERS CREEK TWP",
     "IOWA TWP",
     "JACKSON TWP",
     "MAQUOKETA TWP",
     "MONMOUTH TWP",
     "OTTER CREEK TWP",
     "PERRY TWP",
     "PRAIRIE SPRINGS TWP",
     "RICHLAND TWP",
     "SOUTH FORK TWP",
     "TETE DES MORTS TWP",
     "UNION TWP",
     "VAN BUREN TWP",
     "WASHINGTON TWP",
   };
 }
