 package net.anei.cadpage.parsers;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Abstract message parser class that adds support for "smart" address parsing.
  * That is parsing logic that can be used for text formats that do not clearly 
  * deliminate where the the address portion of the message can be found. 
  *
  */
 public abstract class SmartAddressParser extends MsgParser {
 
   /**
    * Type code indicating what kind of information might preceed the address
    * portion of this message
    */
   public  enum StartType {START_ADDR, START_CALL, START_CALL_PLACE, START_PLACE, START_SKIP};
   
   /**
    * Flag indicating that the start field (either CALL, PLACE, or SKIP) must
    * contain at least one token
    */
   public static final int FLAG_START_FLD_REQ = 0x0001;
   
   /**
    * Flag indicating that an @ mark will be used to mark the start of a
    * place name following the normal address instead of an address
    */
   public static final int FLAG_AT_PLACE = 0x0002;
   
   /**
    * Flag indicating that an @ mark can be used to mark either the start of
    * an address or the start of a place name following an address
    */
   public static final int FLAG_AT_BOTH = 0x0004;
   
   /**
    * Flag indicating that address should extend to end of parsed data field
    */
   public static final int FLAG_ANCHOR_END = 0x0008;
   
   /**
    * Flag indicating that status should always be calculated, even if
    * both beginning and end of the address have been identified
    */
   public static final int FLAG_CHECK_STATUS = 0x0010;
   
   /**
    * Flag indicating that the & or / between street names for an intersection
    * type address may not be present and should be implied
    */
   public static final int FLAG_IMPLIED_INTERSECT = 0x0020;
   
   /**
    * Flag indicating the presence of some kind of pad field between the
    * address proper and the city field.   The field can be retrieved later
    * with a getPadField() method
    */
   public static final int FLAG_PAD_FIELD = 0x0040;
   
   /**
    * Flag indicating that @ and AT tokens should be ignored
    */
   public static final int FLAG_IGNORE_AT = 0x0080;
   
   /**
    * Flag indicating that we only want to parse a cross street
    */
   public static final int FLAG_ONLY_CROSS = 0x0100;
   
   /** 
    * Flag indicating that we only want to parse a city
    */
   public static final int FLAG_ONLY_CITY = 0x0200;
   
   /**
    * Flag indicating that a numeric field following the address should NOT
    * be considered an apartment number.
    */
   public static final int FLAG_NO_IMPLIED_APT =0x0400;
   
   /**
    * Flag indicating there is cross street information following the address
    * which means logic to identify a city followed by a street suffix as as
    * street name instead of a city should be suppressed
    */
   public static final int FLAG_CROSS_FOLLOWS = 0x0800;
   
   /**
    * Flag indicating that there my not be a blank delimiter between the
    * address and the data field in front of it
    */
   public static final int FLAG_START_FLD_NO_DELIM = 0x1000;
   
   /**
    * Flag indicating that we will accept street names with non street suffix.
    * Only use in cases where dispatch routinely drops streets suffixes, this
    * allows a lot of junk to get through
    */
   public static final int FLAG_OPT_STREET_SFX = 0x2000;
   
   private Properties cityCodes = null;
   
   // Main dictionary maps words to a bitmap indicating what is important about that word
   private final Map<String, Integer> dictionary = new HashMap<String, Integer>();
   
   // Bitmask indicating dictionary word is a road suffix
   private static final int ID_ROAD_SFX = 1;
   
   // Bitmask bit indicating dictionary word is a route number prefix
   private static final int ID_ROUTE_PFX_PFX = 2;
   
   // Bitmask bit indicating dictionary word is a direction modifier
   private static final int ID_DIRECTION = 4;
   
   // Bitmask bit indicating dictionary work is an intersection connector
   private static final int ID_CONNECTOR = 8;
   
   // Bitmask bit indicating dictionary word is a cross street selector
   private static final int ID_CROSS_STREET = 0x10;
   
   // Bitmask bit indicating dictionary word is a city name/code
   private static final int ID_CITY = 0x100;
   
   // Bitmask bit indicating numeric token
   private static final int ID_NUMBER = 0x200;
   
   // Bitmask bit indicating start of multiword token
   private static final int ID_MULTIWORD = 0x400;
   
   // Bitmask bit indicating a start address marker
   private static final int ID_AT_MARKER = 0x800;
   
   // Bitmask bit indicating token had a preceding @ character
   private static final int ID_INCL_AT_MARKER = 0x1000;
   
   // Bitmask bit indicating token is an apartment selector
   private static final int ID_APPT = 0x2000;
   
   // Bitmask bit indicating token is a complete single word token
   // both this and the ID_MULTIWORD flag may be set if a token is both
   // a single complete token and the start of a multiword token
   private static final int ID_COMPLETE = 0x4000;
   
   // Bitmask bit indicating token is a route prefix that can be extended
   // by another normal route prefix
   private static final int ID_ROUTE_PFX_EXT =  0x8000;
   
   // Bitmask bit indicating token is an ambiguous road suffix
   private static final int ID_AMBIG_ROAD_SFX = 0x10000;
   
   // Bitmask bit indicating token cannot be part of an address
   private static final int ID_NOT_ADDRESS = 0x20000;
   
   // Bitmask bit indicating token can be an alpha route or highway number
   private static final int ID_ALPHA_ROUTE = 0x40000;
   
   // Bitmask bit indicating token is an optional extra road prefix (OLD, NEW)
   private static final int ID_OPT_ROAD_PFX = 0x80000;
   
   // Bitmask bit indicating token is a road suffix commonly associated with
   // numbered road names
   private static final int ID_NUMBERED_ROAD_SFX = 0x100000;
   
   // Bitmask bit indicating token is a word that is commonly used to qualify
   // other highway names
   private static final int ID_ROAD_QUALIFIER = 0x200000;
   
   // Bitmask indicating dictionary word is either a route number prefix or a
   // route prefix extender
   private static final int ID_ROUTE_PFX = ID_ROUTE_PFX_PFX | ID_ROUTE_PFX_EXT;
   
   // Bitmask indicating dictionary word should be considered as starting a multi-word
   // street name
   private static final int ID_STREET_NAME_PREFIX = 0x400000;
   
   private static final Pattern PAT_HOUSE_NUMBER = Pattern.compile("\\d+(?:-[0-9/]+)?(?:-?(?:[A-Z]|BLK))?", Pattern.CASE_INSENSITIVE);
   
   // List of multiple word cities
   private MultiWordList mWordCities = null;
   
   // List of multiple word street names
   // We keep two of these in case we have to search forward or backward
   private MultiWordList mWordStreetsRev = null;
   private MultiWordList mWordStreetsFwd = null;
   
   // This is a tree set containing all of the expected call descriptions
   // sorted in reverse order because we need to search the tree backward and
   // Android implementation of TreeSet lacks features that make that easy
   private TreeSet<String> callDictionary = null;
   
   public SmartAddressParser(String[] cities, String defCity, String defState) {
     this(cities, defCity, defState, CountryCode.US);
   }
   
   public SmartAddressParser(String[] cities, String defCity, String defState, 
                             CountryCode code) {
     this(defCity, defState, code);
     setupCities(cities);
   }
   
   public SmartAddressParser(Properties cityCodes, String defCity, String defState) {
     this(cityCodes, defCity, defState, CountryCode.US);
   }
   
   public SmartAddressParser(Properties cityCodes, String defCity, String defState, 
                             CountryCode code) {
     this(defCity, defState, code);
     if (cityCodes != null) setupCities(getKeywords(cityCodes));
     this.cityCodes = cityCodes;
   }
   
   public SmartAddressParser(String defCity, String defState) {
     this(defCity, defState, CountryCode.US);
   }
   
   public SmartAddressParser(String defCity, String defState, CountryCode code) {
     super(defCity, defState, code);
     setupDictionary(defState);
   }
 
   /**
    * Set up base dictionary common to all countries
    * @param defState default state
    */
   private void setupDictionary(String defState) {
     setupDictionary(ID_ROAD_SFX, 
         "AVENUE", "AV", "AVE", 
         "STREET", "ST", 
         "PLACE", "PL", 
         "ROAD", "RD", 
         "LANE", "LN",
         "DRIVE", "DR",
         "SQUARE", "SQ",
         "BLVD", "BL", "BLV",
         "PARKWAY", "WAY", "PKWY", "PKY", "PK", "PY", "FWY", "WY", "HW", "EXPW", "PW",
         "CIRCLE", "CIR", "CL", 
         "TRAIL", "TRL", "TR", "TL",
         "PATH",
         "PIKE", "PKE",
         "COURT", "CT",
         "TER", "TERR",
         "HWY",
         "MALL",
         "GTWY", "GATEWAY",
         "PLAZ", "PLAZA",
         "TURNPIKE", "TPKE", "TPK",
         "PASS",
         "EST",
         "RUN",
         "GRN",
         "LOOP",
         "TERRACE", "TRC", "TRCE",
         "ESTATES", "ESTS",
         "CUTOFF");
     if ((getMapFlags() & MAP_FLG_SUPPR_LA) == 0)  setupDictionary(ID_ROAD_SFX, "LA");
     
     setupDictionary(ID_AMBIG_ROAD_SFX, 
         "PLACE", "TRAIL", "PATH", "PIKE", "COURT", "MALL", "TURNPIKE", "PASS", 
         "RUN", "LANE", "PARK", "POINT", "RIDGE", "CREEK", "MILL", "BRIDGE", "HILLS",
         "HILL", "TRACE", "STREET", "MILE", "BAY");
     
     setupDictionary(ID_NUMBERED_ROAD_SFX, 
         "AVENUE", "AV", "AVE", 
         "STREET", "ST", 
         "PLACE", "PL",
         "ROAD", "RD");
     
     setupDictionary(ID_ROAD_QUALIFIER, "OLD");
         
     setupDictionary(ID_ROUTE_PFX_EXT, "RT", "RTE", "ROUTE", "HW", "HWY", "HIGHWAY", "ROAD", "RD");
     setupDictionary(ID_ROUTE_PFX_PFX, "STATE", "ST", "SR", "SRT", "US", "FS", "INTERSTATE", "I", "STHWY", "SH", "USHWY", "USHY", "CO", "CR", "CORD", "COUNTY", "FM");
     setupDictionary(ID_ROUTE_PFX_PFX, new String[]{defState});
     setupDictionary(ID_DIRECTION, "N", "NE", "E", "SE", "S", "SW", "W", "NW", "NB", "EB", "SB", "WB", "EXT");
     setupDictionary(ID_OPT_ROAD_PFX, "OLD", "NEW", "UPPER", "LOWER");
     setupDictionary(ID_CONNECTOR, "AND", "/", "&");
     setupDictionary(ID_AT_MARKER, "AT", "@");
     
     // C/S should be in this list, but it gets changed before we parse stuff
     setupDictionary(ID_CROSS_STREET, "XS:", "X:");
     setupDictionary(ID_APPT, "APT:", "APT", "APTS", "#", "SP", "RM", "SUITE", "STE", "SUITE:", "ROOM", "ROOM:", "LOT");
     setupDictionary(ID_STREET_NAME_PREFIX, "LAKE", "MT", "MOUNT", "SUNKEN");
     
     // Add any country specific words
     switch (getCountryCode()) {
     case UK:
       setupDictionary(ID_ROAD_SFX,
           "CLOSE",
           "GREEN",
           "CRESCENT");
       break;
     }
   }
   
   /**
    * Add full length names of directions to direction dictionary tables
    */
   protected void addExtendedDirections() {
     setupDictionary(ID_DIRECTION, "NORTH", "SOUTH", "EAST", "WEST");
   }
   
   private String[] getKeywords(Properties table) {
     String[] result = new String[table.size()];
     int ndx = 0;
     for (Enumeration<?> e = table.propertyNames(); e.hasMoreElements(); ) {
       result[ndx++] = (String)e.nextElement();
     }
     return result;
   }
   
   /**
    * Set up predefined city code tables or lists
    */
   private void setupCities(String[] cities) {
     mWordCities = new MultiWordList(+1, ID_CITY, ID_MULTIWORD, ID_COMPLETE, cities);
   }
   
   private void setupDictionary(int bitMask, String ... args) {
     for (String arg : args) {
       int newMask = bitMask;
       Integer oldMask = dictionary.get(arg);
       if (oldMask != null) newMask |= oldMask;
       dictionary.put(arg, newMask);
     }
   }
 
   /**
    * Look up a word on our internal dictionary
    * @param word word to be checked
    * @return true if word is found in dictionary
    */
   public boolean isDictionaryWord(String word) {
     return dictionary.containsKey(word);
   }
   
   /**
    * Setup predefined call list
    * @param callList list of predefined calls
    */
   protected void setupCallList(String ... callList) {
     if (callList != null) {
       callDictionary = new TreeSet<String>(new Comparator<String>(){
         @Override
         public int compare(String str1, String str2) {
           return -str1.compareTo(str2);
         }});
     }
     for (String call : callList) callDictionary.add(call + " ");
   }
   
   /**
    * Set up preloaded multi-word street names
    * @param names list of multi-word street names.  with no street suffixes
    */
   protected void setupMultiWordStreets(String ... names) {
     mWordStreetsFwd = new MultiWordList(+1, 0, ID_MULTIWORD, 0, names);
     mWordStreetsRev = new MultiWordList(-1, 0, ID_MULTIWORD, 0, names);
   }
 
   
   
   // Parser working variables
   private int flags;
   private String[] tokens;
   private int[] tokenType;
   private int startAddress = -1;
   private int lastCity = -1;
   private int startNdx = 0;
   
   // Have to save last result for backward compatibility with some calls
   private Result lastResult = null;
   
   /**
    * Determine if a string looks like a valid address
    * @param address Address string to be checked
    * @return zero if string is not recognized as valid address, otherwise a
    * numeric value in which higher values indicate better addresses
    */
   protected int checkAddress(String address) {
     return checkAddress(address, 0);
   }
   
   /**
    * Determine if a string looks like a valid address
    * @param address Address string to be checked
    * @param extra number of extra tokens (presumably city names) that can
    * be ignored at the end of the line
    * @return zero if string is not recognized as valid address, otherwise a
    * numeric value in which higher values indicate better addresses
    */
   protected int checkAddress(String address, int extra) {
     return parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS, address).getStatus(extra);
   }
 
   /**
    * Parse address line
    * @param sType indicates what we now about start of address field
    *         START_ADDR - field starts with the address
    *         START_CALL - field starts with call description followed by address
    *         START_CALL_PLACE - field starts will call description possibly followed by place name
    *         START_PLACE - field starts with a place name
    *         START_SKIP - field starts with something we aren't interested in,
    *                      followed by address
    * @param address address field to be parsed
    * @param data data object to be filled with information from parsed address field.
    */
   protected void parseAddress(StartType sType, String address, MsgInfo.Data data) {
     parseAddress(sType, 0, address, data);
   }
 
   /**
    * Parse address line
    * @param sType indicates what we now about start of address field
    *         START_ADDR - field starts with the address
    *         START_CALL - field starts with call description followed by address
    *         START_CALL_PLACE - field starts will call description possibly followed by place name
    *         START_PLACE - field starts with a place name
    *         START_SKIP - field starts with something we aren't interested in,
    *                      followed by address
    * @param flags - Special processing flags                     
    * @param address address field to be parsed
    * @param data data object to be filled with information from parsed address field.
    */
   protected void parseAddress(StartType sType, int flags, String address, MsgInfo.Data data) {
     Result result = parseAddress(sType, flags, address);
     result.getData(data);
   }
   
   
   /**
    * Determine if the string that we just parsed looks like an address
    * @return zero if string was not recognized as valid address, otherwise a
    * numeric value in which higher values indicate better addresses
    */
   protected int getStatus() {
     return lastResult.getStatus();
   }
   
   /**
    * parse address and return a result object that can be used to build
    * the parsed data fields at a later time
    * @param sType indicates what we now about start of address field
    *         START_ADDR - field starts with the address
    *         START_CALL - field starts with call description followed by address
    *         START_CALL_PLACE - field starts will call description possibly followed by place name
    *         START_PLACE - field starts with a place name
    *         START_SKIP - field starts with something we aren't interested in,
    *                      followed by address
    * @param flags - Special processing flags                     
    * @param address address field to be parsed
    * @return integer indicating how good this address is, higher number mean better fit
    */
   protected Result parseAddress(StartType sType, String address) {
     return parseAddress(sType, 0, address);
   }
   
   /**
    * parse address and return a result object that can be used to build
    * the parsed data fields at a later time
    * @param sType indicates what we now about start of address field
    *         START_ADDR - field starts with the address
    *         START_CALL - field starts with call description followed by address
    *         START_CALL_PLACE - field starts will call description possibly followed by place name
    *         START_PLACE - field starts with a place name
    *         START_SKIP - field starts with something we aren't interested in,
    *                      followed by address
    * @param flags - Special processing flags                     
    * @param address address field to be parsed
    * @return integer indicating how good this address is, higher number mean better fit
    */
   protected Result parseAddress(StartType sType, int flags, String address) {
     Result result = parseAddressInternal(sType, flags, address);
     
     // If we were really parsing cross streets, switch address index to cross street index
     if (isFlagSet(FLAG_ONLY_CROSS)) {
       result.crossField = result.addressField;
       result.addressField = null;
     }
     return result;
   }
   
   private static final Pattern DOT_PATTERN = Pattern.compile("\\.(?!\\d)");
   private Result parseAddressInternal(StartType sType, int flags, String address) {
     this.flags = flags;
     lastCity = -1;
     Result result = new Result();
     lastResult = result;
 
     // If we have a call dictionary, and address starts with a call, search
     // the dictionary to see if address line starts with matching call
     if ((sType == StartType.START_CALL || sType == StartType.START_CALL_PLACE) 
          && callDictionary != null) {
       
       // Search the call dictionary sorted set for the highest entry less than or
       // equal to message body.  If the body starts with this string, we have a
       // match.  If not, we have to keep searching backward through the sorted set
       // for the entry less than or equal to the message body
       
       // We reversed the tree order so we can accomplish this trick without
       // needing a backward read feature, with Android seems to be lacking
       String firstWord = new Parser(address).get(' ');
       SortedSet<String> tail =  callDictionary.tailSet(address);
       for (String call : tail) {
         if (address.toUpperCase().startsWith(call)) {
           
           // We have a match.  Store the call (without the trailing space)
           // in the result call prefix.  Remove the call prefix from the address
           // line, and set the start type to start with the address
           result.callPrefix = address.substring(0,call.length()-1);
           address = address.substring(call.length()).trim();
           if (address.startsWith("@")) address = address.substring(2).trim();
           if (address.startsWith("REPORTED AT ")) address = address.substring(12).trim();
           sType = (sType == StartType.START_CALL_PLACE ? StartType.START_PLACE : StartType.START_ADDR);
           this.flags &= ~FLAG_START_FLD_REQ;
         }
         
         // If the prospective call no longer starts with the first word
         // of the address line, we are not going to find anything
         if (!call.startsWith(firstWord)) break;
       }
     }
 
     // Save the result address type
     result.startType = sType;
     
     // Before we do anything else, see if we can find some GPS coordinates
     // in this address.  If we do find them, pack them so they will parse
     // into a single token
     String gpsCoords = null;
     Matcher match = MsgInfo.GPS_PATTERN.matcher(address);
     if (match.find()) {
       gpsCoords = match.group(1) + ',' + match.group(2); 
       address = address.substring(0, match.start()) + gpsCoords 
                   + address.substring(match.end());
     }
 
     // We have a lot of pre parsing adjustments that must be made to normal
     // address strings, but get skipped if we have already identified this
     // as a GSP coordinate address
     else {
       
       // Make sure any / or & character will parse by itself
       // Before we do that we have to protect the C/S cross street indicator
       // Another disaster that needs to be prevented is breaking up AT&T into
       // AT and an &
       // Ditto for 1/2
       boolean att = address.contains("AT&T");
       if (att) address = address.replaceAll("AT&T", "AT%T");
       boolean half = address.contains("1/2");
       if (half) address = address.replaceAll("1/2", "1%2");
       address = address.replaceAll(" C/S:? ", " XS: ").replace("/", " / ").replace("&", " & ").trim();
       if (att) address = address.replaceAll("AT%T", "AT&T");
       if (half) address = address.replaceAll("1%2", "1/2");
       
       // Make sure any colon keyword parsers by itself
       address = address.replaceAll(":", ": ");
       
       // Periods used with abbreviations also cause trouble.  Just get rid of all periods
       // Except for periods followed by a digit which are presumably decimal points
       address = DOT_PATTERN.matcher(address).replaceAll("");
     }
 
     // Check for null string
     result.status = 0;
     address = address.trim();
     if (address.length() == 0) return result;
 
     // Set up token list and types
     setTokenTypes(sType, address, result);
     
     // If we are looking for a city and nothing else, parseToCity can find it
     if (isFlagSet(FLAG_ONLY_CITY) && ! isFlagSet(FLAG_ONLY_CROSS)) {
       if (parseStartToCity(0, result)) result.status = 4;
       return result;
     }
     
     // Now comes the hard part.
     
     // We have a number of basic patters that we will recognize
     // Try each one until we find one that works
     result.status = 4;
     if (parseTrivialAddress(result)) return result;
     if (!isFlagSet(FLAG_ONLY_CROSS)) {
       if (parseGPSCoords(result, gpsCoords)) return result;
       result.status = 3;
       if (parseSimpleAddress(result)) return result;
     }
     result.status = 2;
     if (parseIntersection(result)) return result;
     result.status = 1;
     if (parseNakedRoad(result)) {
       // If the naked road pattern detected an implied intersection symbol
       // promote the result status to intersection
       if (result.insertAmp >= 0) result.status++; 
       return result;
     }
     
     // If all else fails, use the fallback parser
     parseFallback(sType, result);
     result.status = 0;
     return result;
   }
 
   /**
    * Called after address has been parsed with FLAG_PAD_FIELD flag
    * @return pad field between address and city
    */
   public String getPadField() {
     return lastResult.getPadField();
   }
 
   /**
    * Called after address has been parsed
    * @return the part of the line after the address
    */
   public String getLeft() {
     return lastResult.getLeft();
   }
   
   /**
    * We handle the dead simple case where the address starts at the 
    * beginning of the text and we have found a city to mark the end
    * of the address (would that life were always this simple
    */
   private boolean parseTrivialAddress(Result result) {
     
     // If caller has locked both ends of the address, and wants an address status
     // Or we are supposed to insert implied intersection symbols
     // Or we should parse out a pad field
     // then return failure status so one of the other address parsers can make
     // some kind of reasonableness check on this
     if (isFlagSet(FLAG_CHECK_STATUS|FLAG_IMPLIED_INTERSECT|FLAG_PAD_FIELD)) return false;
     
     // OK, we have to have at least 2 items before the city
     // Unless we are parsing a cross street instead of a real address, in which
     // case we allow it to be empty
     if (startAddress < 0) return false;
     int reserve = (isFlagSet(FLAG_ONLY_CROSS) ? 0 : 2);
     return parseAddressToCity(startAddress, startAddress+reserve, result);
   }
 
   /**
    * Handle special case where the prospective address contains a token
    * that contains valid GPS coordinates
    */
   private boolean parseGPSCoords(Result result, String gpsCoords) {
     
     // If no GPS coordinates were found, return false
     if (gpsCoords == null) return false;
     
     // We have already packed the GPS coordinates into a single token
     // now find that token
     int gpsNdx = 0;
     for (; gpsNdx < tokens.length; gpsNdx++) {
       if (tokens[gpsNdx].contains(gpsCoords)) break;
     }
     if (gpsNdx >= tokens.length) throw new RuntimeException("GPS coordinates not found");
 
     // Expand address to beginning or end of token string if required by
     // constraints
     int start = (result.startType == StartType.START_ADDR ? 0 : gpsNdx);
     int end  = (isFlagSet(FLAG_ANCHOR_END) ? tokens.length : gpsNdx+1);
     if (result.startField != null) result.startField.end(start);
     result.addressField = new FieldSpec(start, end);
     return true;
   }
 
   /**
    * Look for the basic address looking like
    *     <number> <street name> <street suffix>
    */
   private boolean parseSimpleAddress(Result result) {
     
     result.reset();
     boolean padField = isFlagSet(FLAG_PAD_FIELD);
     
     // Look for a numeric field which we assume is the house number
     // If field starts with address this has to be the first token
     // Exception, numeric fields that follow a route prefix are part
     // of a road name and cannot be a house number.
     // Exception to exception, CO is a valid route prefix, but it could also
     // be an abbreviation for COMPANY in a prefix name.  So we won't count it
     // as a route prefix
     int sAddr = startAddress >= 0 ? startAddress : startNdx;
     int sEnd = -1;
     boolean flexAt = isFlagSet(FLAG_AT_BOTH);
     boolean locked = false;
     while (true) {
       while (true) {
         
         if (sAddr >= tokens.length) return false;
         if (isType(sAddr, ID_CROSS_STREET)) return false;
         if (isHouseNumber(sAddr) && !isType(sAddr+1, ID_NOT_ADDRESS | ID_CONNECTOR)) {
           if (sAddr > 0 && isType(sAddr-1, ID_ROUTE_PFX) && 
               !tokens[sAddr-1].equalsIgnoreCase("CO")) return false;
           if (isType(sAddr+1, ID_NUMBERED_ROAD_SFX)) {
             if (isType(sAddr+1, ID_ROUTE_PFX)) {
               if (isType(sAddr+2, ID_NUMBER|ID_ALPHA_ROUTE)) break;
               if (isType(sAddr+2, ID_ROUTE_PFX_EXT) && isType(sAddr+3, ID_NUMBER|ID_ALPHA_ROUTE)) break;
               
               // Well, this certainly looks like it is a numbered street or route
               // rather than a house number.  But there are districts with streets
               // named for saints, so if the next token is ST, give it a chance
               // to be a valid street name
               if (tokens[sAddr+1].equalsIgnoreCase("ST")) {
                 sEnd = findRoadEnd(sAddr+1, 0);
                 if (sEnd > 0) break;
               }
               return false;
             }
             return false;
           }
           break;
         }
         if (startAddress >= 0 || locked) return false;
         if (flexAt && isType(sAddr, ID_INCL_AT_MARKER)) return false;
         sAddr++;
         if (flexAt && isType(sAddr, ID_AT_MARKER)) locked = true;
       }
       
       // If we found a city beyond this start point, just use that as the terminator
       if (! padField && parseAddressToCity(sAddr, (sEnd > 0 ? sEnd : sAddr+2), result)) {
         if (locked && result.startField != null) result.startField.fldEnd--;
         return true;
       }
       
       // Otherwise, see if we can find a road starting from the next token
       if (sEnd < 0) sEnd = findRoadEnd(sAddr+1, 2);
       if (sEnd > 0) break;
       
       // This isn't what we are looking for
       // Increment the search index and look for something else
       sAddr++;
     }
     
     // We have found what we need to have found and we are going
     // to be successful
     result.addressField = new FieldSpec(sAddr, sEnd);
     if (result.startField != null) result.startField.optionalEnd(locked ? sAddr-1 : sAddr);
     result.endAll = sEnd;
     
     // Special case - simple address can have intersection marker followed by
     // cross stream info
     if (isType(sEnd, ID_CONNECTOR)) {
       findCrossStreetEnd(sEnd + 1, result);
     }
     
     // But there might be some additional cross street info we can parse
     findCrossStreet(result);
     
     // If we are looking for a pad field between the address and city
     // now is when we try to find that city
     if (padField) parsePadToCity(result.endAll, result);
     return (result.endAll == tokens.length || !isFlagSet(FLAG_ANCHOR_END));
   }
 
   /**
    * Look for intersection adddress with the basic form of
    *    <roadname> <road sfx> <connector> <roadname> <road sfx>
    * @return true if successful
    */
   private boolean parseIntersection(Result result) {
     
     result.reset();
     boolean padField = isFlagSet(FLAG_PAD_FIELD);
     
     // First lets figure out where the address starts
     int sAddr;
     int ndx;
     
     boolean flexAt = isFlagSet(FLAG_AT_BOTH);
     boolean atStart = false;
 
     // If address starts at beginning of field, find end of address and
     // confirm that it starts with a road followed by a connector
     if (startAddress >= 0) {
       ndx = startIntersection(startAddress);
       if (ndx < 0) return false;
       sAddr = startAddress;
     }
     
     // Otherwise, scan forward looking for a <road-sfx> <connector> combination
     // Then back up 2 places assuming the road consists of one token.
     else {
       int start = startNdx;
       ndx = start;
       while (true) {
         if (flexAt && isType(ndx, ID_AT_MARKER)) atStart = true;
         ndx++;
         if (ndx >= tokens.length) return false;
         if (isType(ndx, ID_CROSS_STREET)) return false;
 
         if (atStart || flexAt && isType(ndx, ID_INCL_AT_MARKER)) {
           sAddr = ndx;
           ndx = startIntersection(sAddr);;
           if (ndx < 0) return false;
           break;
         }
         if (ndx-start >= 1 && isType(ndx, ID_CONNECTOR)) {
           sAddr = ndx-1;
           if (sAddr > start && isType(sAddr, ID_DIRECTION)) sAddr--;
           
           if (sAddr >= start && isRoadToken(sAddr)) break;
           
           if (sAddr > start && isType(sAddr, ID_ROAD_SFX)) {
             sAddr--;
             if (isType(sAddr,ID_NOT_ADDRESS)) continue;
             if (sAddr > start && isType(sAddr, ID_AMBIG_ROAD_SFX) && !isType(sAddr-1,ID_NOT_ADDRESS)) sAddr--;
             break;
           }
           
           if (sAddr > start && isType(sAddr-1, ID_ROUTE_PFX) & isType(sAddr, ID_NUMBER | ID_ALPHA_ROUTE)) {
             sAddr--;
             if (sAddr > start && 
                 isType(sAddr, ID_ROUTE_PFX_EXT) && 
                 isType(sAddr-1, ID_ROUTE_PFX_PFX)) sAddr--;
             break;
           }
           
           if (isFlagSet(FLAG_OPT_STREET_SFX) && !isType(sAddr,ID_NOT_ADDRESS)) break; 
         }
       }
       
       // If road is preceded by a direction, include that
       sAddr = stretchRoadPrefix(start, sAddr);
     }
     
     // When we get here, 
     // saddr points to beginning of address
     // ndx points to the first connector after the first road name.
 
     // If there is a city terminating the address, just parse up to it
     if (!padField && parseAddressToCity(sAddr, ndx+2, result)) {
       if (atStart && result.startField != null) result.startField.fldEnd--;
       return true;
     }
     
     // Otherwise find end of second road
     ndx = findRoadEnd(ndx+1, 2);
     if (ndx < 0) return false;
     
     // If we found that, we have a successful intersection parse
     result.addressField = new FieldSpec(sAddr, ndx);
     if (startAddress < 0) result.startField.end(atStart ? sAddr - 1 : sAddr); 
     result.endAll = ndx;
     
     // But there might be some additional cross street info we can parse
     findCrossStreet(result);
     
     // If we are looking for a pad field between the address and city
     // now is when we try to find that city
     if (padField) parsePadToCity(result.endAll, result);
 
     return (result.endAll == tokens.length || !isFlagSet(FLAG_ANCHOR_END));
   }
   
   /**
    * Try to parse first part intersection starting at specified index
    * @param sAddr starting index
    * @return index of connector if successful, otherwise -1
    */
   private int startIntersection(int sAddr) {
     int ndx = findRoadEnd(sAddr, 2);
     if (ndx < 0) return -1;
     if (! isType(ndx, ID_CONNECTOR)) return -1;
     return ndx;
   }
 
   /**
    * Look for simple road without a house number or intersection
    *    <roadname> <roadsfx>
    * @return true if found 
    */
   private boolean parseNakedRoad(Result result) {
     
     result.reset();
     boolean flexAt = isFlagSet(FLAG_AT_BOTH);
     boolean atStart = false;
     boolean padField = isFlagSet(FLAG_PAD_FIELD);
 
     // If address starts at beginning of field, find end of address and
     // Don't have to look for city because we wouldn't be here if both startAddr
     // and city was found
 
     int sAddr, ndx;
     if (startAddress >= 0) {
       sAddr = ndx = startAddress;
       while (isType(ndx, ID_OPT_ROAD_PFX)) ndx++;
       ndx = findRoadEnd(ndx, 0);
       if (ndx < 0) return false;
     }
     
     // Otherwise, scan forward looking for a <road-sfx>
     //            that isn't the start of a <route-pfx> <number> combination
     // or number preceded by a <route-pfx>
     // or an ambiguous <road-sfx> followed by a second <road-sfx>
     else {
       int start = startNdx;
       ndx = start;
       while (true) {
         if (flexAt && isType(ndx, ID_AT_MARKER)) atStart = true;
         ndx++;
         sAddr = ndx - 1;
         if (ndx >= tokens.length) return false;
         if (isType(ndx, ID_CROSS_STREET)) return false;
         if (atStart || flexAt && isType(ndx, ID_INCL_AT_MARKER)) {
           start = sAddr = ndx;
           ndx = findRoadEnd(sAddr, 0);
           ndx--;
           if (ndx < 0) return false;
           break;
         }
         
         if (isType(ndx, ID_ROAD_SFX) && !isType(sAddr, ID_NOT_ADDRESS | ID_CONNECTOR)) {
           boolean startHwy = 
               (isType(ndx, ID_ROUTE_PFX) && isType(ndx+1, ID_NUMBER | ID_ALPHA_ROUTE)) ||
               (isType(ndx, ID_ROUTE_PFX_PFX) && isType(ndx+1, ID_ROUTE_PFX_EXT) && isType(ndx+2, ID_NUMBER)) ||
               (isType(ndx, ID_AMBIG_ROAD_SFX) && (isType(ndx+1, ID_ROAD_SFX)));
           
           if (!startHwy) break; 
         }
         if (ndx > start && isType(ndx, ID_NUMBER | ID_ALPHA_ROUTE) && isType(ndx-1, ID_ROUTE_PFX)) break;
         if (isRoadToken(ndx)) {
           sAddr = ndx;
           break;
         }
       }
       
       // See if this is a two part route name
       while (sAddr > start && !isType(sAddr-1, ID_NOT_ADDRESS | ID_CONNECTOR) && 
           (isType(sAddr, ID_ROUTE_PFX_EXT) && isType(sAddr-1, ID_ROUTE_PFX_PFX) ||
            isType(sAddr, ID_AMBIG_ROAD_SFX))) {
         sAddr--;
       }
 
       // If the previous token is a direction, back up one more to include that.
       sAddr = stretchRoadPrefix(start, sAddr);
       
       // increment end pointer past the road terminator
       // If the following token is a direction, increment end pointer past that too
       ndx++;
       if (isType(ndx, ID_DIRECTION)) ndx++;
       result.startField.end(atStart ? sAddr-1 : sAddr);
     }
     
     // When we get here, 
     // startAddress points to beginning of address
     // ndx points past the end of the road
     
     // We have a naked road parse
     result.addressField = new FieldSpec(sAddr, ndx);
     result.endAll = ndx;
     
     // If we are looking for implied interestection markers see if we can
     // find another street name immediately following this one.  If we can
     // then set the implied & colum to the end of the first street and set
     // the end of the address field to the end of the second street.
     if (isFlagSet(FLAG_IMPLIED_INTERSECT)) {
       ndx = findRoadEnd(ndx, 0);
       if (ndx >= 0) {
         result.insertAmp = result.endAll;
         result.addressField.end(ndx);
         result.endAll = ndx;
         
         // A direction token between the two streets is ambiguous.  The
         // initial road parsing logic would have assigned it to the first road
         // If we find that the first road ends with a direction, but the second
         // road does not, then reassign the direction token from the end of the
         // first road to the beginning of the second
         if (isType(result.insertAmp-1, ID_DIRECTION) &&
             !isType(result.endAll-1, ID_DIRECTION)) result.insertAmp--;
       }
     }
     
     // See if we can parse out to a city
     if (!padField && parseAddressToCity(sAddr, result.endAll, result)) return true;
     
     // Nope, see if we can find some cross street info
     findCrossStreet(result);
     
     // If we are looking for a pad field between the address and city
     // now is when we try to find that city
     if (padField) parsePadToCity(result.endAll, result);
 
     return (result.endAll == tokens.length || !isFlagSet(FLAG_ANCHOR_END));
   }
 
   /**
    * Fallback parser used when nothing else works
    * @param sType
    */
   private void parseFallback(StartType sType, Result result) {
 
     result.reset();
     boolean crossOnly = isFlagSet(FLAG_ONLY_CROSS);
     
     // Total failure, assign the entire field to either the call description
     // or the address
     int endAddr = result.tokens.length;
     
     // Check for any invalid tokens.  If we find any they mark the end of any
     // possible address
     if (!isFlagSet(FLAG_ANCHOR_END)) {
       for (int ndx = 0; ndx < endAddr; ndx++) {
         if (isType(ndx, ID_NOT_ADDRESS)) {
           endAddr = ndx;
           break;
         }
       }
     }
     result.endAll = endAddr;
     
     if (!crossOnly) { 
       
       // If there is a cross street indicator, use it to set up the cross street
       for (int ndx = 0; ndx<endAddr; ndx++) {
         if (isType(ndx, ID_CROSS_STREET)) {
           result.crossField = new FieldSpec(ndx+1, endAddr);
           endAddr = ndx;
         }
       }
       
       // if @ is used as a fixed start address marker (default operation)
       // then we would have used one to set startAddress and no longer need
       // to worry about it
       
       // If @ is being used as an Optional start address marker, startAddress
       // would not be set and we have to look through the token string to
       // see if we find a @ marker
       int stIndex = (isFlagSet(FLAG_START_FLD_REQ) ? 1 : 0);
       if (startAddress < 0 && isFlagSet(FLAG_AT_BOTH)) {
         for (int ndx = stIndex; ndx < endAddr; ndx++) {
           if (isType(ndx, ID_AT_MARKER)) {
             result.addressField = new FieldSpec(ndx+1, endAddr);
             endAddr = ndx;
             break;
           } else if (isType(ndx, ID_INCL_AT_MARKER)) {
             result.addressField = new FieldSpec(ndx, endAddr);
             endAddr = ndx;
             break;
           }
         }
       }
       
       // We are really getting desperate, but see if there is a valid house number
       // in here, and if there is set the start address to it
       if (result.addressField == null && startAddress < 0) {
         for (int ndx = stIndex; ndx < endAddr-1; ndx++) {
           if (isHouseNumber(ndx)) {
             result.addressField = new FieldSpec(ndx, endAddr);
             endAddr = ndx;
             break;
           }
         }
       }
     }
     
     // If we still don't have an address, see if this is an address or
     // skip start type.  If so, assign all of the remaining line as an address
     if (result.addressField == null) {
       int sAddr = startAddress;
       if (sAddr < 0 && sType == StartType.START_SKIP) sAddr = 0;
       if (sAddr >= 0) {
         result.addressField =  new FieldSpec(sAddr, endAddr);
         endAddr = sAddr;
       }
     }
 
     // However we managed to come up with an address, see if we can pick out a city
     // at the end of it
     if (!crossOnly && result.addressField != null) {
       int sAddr = result.addressField.fldStart;
       parseAddressToCity(sAddr, sAddr+2, result);
     }
     
     if (result.startField != null && startAddress < 0) result.startField.end(endAddr);
     
     
     // We aren't done yet.
     // if @ are being used to mark a place name, see if we can find one
     // in what we have for an address and split the rest into a place name
    if (isFlagSet(FLAG_AT_BOTH | FLAG_AT_PLACE)) {
       for (int ndx = result.addressField.fldStart; ndx<result.addressField.fldEnd; ndx++) {
         if (isType(ndx, ID_AT_MARKER)) {
           result.addressField.fldStart = ndx+1;
           if (result.startField != null) result.startField.end(ndx);
           break;
         } else if (isType(ndx, ID_INCL_AT_MARKER)) {
           result.addressField.fldStart = ndx;
           if (result.startField != null) result.startField.end(ndx);
           break;
         }
       }
     }
   }
   
   /**
    * Called when we found the end of a road name and are guessing at where
    * it might start
    * @param start Hard limit on where address can start
    * @param sAddr index of current start of tentative road name
    * @return index of presumed road name
    */
   private int stretchRoadPrefix(int start, int sAddr) {
     
     // Check our preloaded multiple word street name list
     if (mWordStreetsRev != null) {
       int tmp = mWordStreetsRev.findEndSequence(sAddr);
       if (tmp >= 0 && tmp >= start) sAddr = tmp;
     }
     
     // If road starts with a common street name prefix, back up one place
     if (sAddr > start && isType(sAddr-1, ID_STREET_NAME_PREFIX)) sAddr--;
     
     // Look up to 3 tokens back to see if we find a direction token
     // Stop search if we encounter a /, lest we confuse a W/INJURY
     // as part of a road name
     for (int j = 1; j<= 3; j++) {
       int ndx = sAddr - j;
       if (ndx < start) break;
       if (tokens[ndx].equals("/")) break;
       if (isType(ndx, ID_NOT_ADDRESS | ID_CONNECTOR)) break;
       if (isType(ndx, ID_DIRECTION)) return sAddr-j; 
     }
     
     // No luck, see if the previous token is a possible road prefix
     if (sAddr > start && isType(sAddr-1, ID_OPT_ROAD_PFX)) sAddr--;
     
     // If road starts with a direction, back up one place
     if (sAddr > start && isType(sAddr, ID_DIRECTION)) sAddr--;
     return sAddr;
   }
   
   /**
    * Parse address field to city
    * @param stAddr prospective start of address field
    * @param srcNdx index to start searching
    * @param result Result object where results will be saved
    * @return true if city was found and all pertinent information was found.
    * False if not city was found and  nothing was changed in result
    */
   private boolean parseAddressToCity(int stAddr, int srcNdx, Result result) {
     FieldSpec addressField = new FieldSpec(stAddr);
     if (!parseToCity(addressField, srcNdx, result)) return false;
     result.addressField = addressField;
     if (result.startField != null) result.startField.optionalEnd(stAddr);
     return true;
   }
   
   /**
    * Parse pad field to city
    * @param start prospective start of pad field
    * @param result Result object where results will be saved
    * @return true if city was found and all pertinent information was found.
    * False if not city was found and  nothing was changed in result
    */
   private boolean parsePadToCity(int start, Result result) {
     FieldSpec padField = new FieldSpec(start);
     if (!parseToCity(padField, start, result)) return false;
     result.padField = padField;
     return true;
   }
   
   /**
    * Parse start field to City
    * @param start prospective start of pad field
    * @param result Result object where results will be saved
    * @return true if city was found and all pertinent information was found.
    * False if not city was found and  nothing was changed in result
    */
   private boolean parseStartToCity(int start, Result result) {
     return parseToCity(result.startField, start, result);
   }
     
   
   /**
    * See if we can parse an address from a known starting point to a city
    * @param fldSpec previous field being parsed
    * @param srcNdx start looking for city here
    * @return true if  city was identified and all fields have been set
    */
   private boolean parseToCity(FieldSpec fldSpec, int srcNdx, Result result) {
     
     // If we are doing a cross only parse without a city, answer is always no
     boolean crossOnly = isFlagSet(FLAG_ONLY_CROSS);
     if (crossOnly && !isFlagSet(FLAG_ONLY_CITY)) return false;
     
     // If FLAG_ANCHOR_END is set, we are going to parse this to the
     // end of the line without looking for a city
     boolean anchorEnd = isFlagSet(FLAG_ANCHOR_END);
     boolean parseToEnd = anchorEnd & ! isFlagSet(FLAG_CHECK_STATUS);
     boolean padField = isFlagSet(FLAG_PAD_FIELD);
 
     if (srcNdx >= tokens.length) return false;
     if (!parseToEnd && lastCity < srcNdx) return false;
     
     boolean flexAt = isFlagSet(FLAG_AT_PLACE | FLAG_AT_BOTH);
     
     // Notice: If the FLAG_PAD_FIELD was set, some of these fields might have
     // been found before the PAD field, in which case we don't want to disturb
     // them.
     FieldSpec placeField = null;
     FieldSpec aptField = null;
     FieldSpec crossField = null;
     boolean aptToken = false;
     
     FieldSpec startField = new FieldSpec(0);
     
     FieldSpec lastField = startField;
     for (int ndx = srcNdx; ndx < tokens.length; ndx++) {
       
       // Is there a city here?
       int endCity = findEndCity(ndx);
       if (endCity >= 0) {
         if (!anchorEnd || endCity == tokens.length) {
           lastField.end(ndx);
           if (placeField != null) result.placeField = placeField;
           if (aptField != null) {
             result.aptToken = aptToken;
             result.aptField = aptField;
           }
           if (crossField != null) result.crossField = crossField;
           result.cityField = new FieldSpec(ndx, endCity);
           result.endAll = endCity;
           if (fldSpec != null) fldSpec.end(startField.fldEnd);
           
           // If there is a pad field, it might have the misfortune to be a 
           // place name that includes a local city name.  So we will call
           // ourselves recursively in an attempt to find another city name
           // behind this one
           if (padField && endCity < tokens.length) {
             parseToCity(fldSpec, endCity+1, result);
           }
           return true;
         }
       }
       
       // Only check for fun stuff if it isn't inside a pad field
       if (!padField) {
         
         if (isType(ndx, ID_NOT_ADDRESS)) return false;
         
         // Check for place name
         if (flexAt && placeField == null) {
           if (isType(ndx, ID_AT_MARKER)) {
             lastField.end(ndx);
             lastField = placeField = new FieldSpec(ndx+1);
           } else if (isType(ndx, ID_INCL_AT_MARKER)) {
             lastField.end(ndx);
             lastField = placeField = new FieldSpec(ndx);
           }
         }
 
         // Special stuff not checked for in cross street only parse
         if (!crossOnly) {
 
         // Check for apartment marker
           if (aptField == null) {
             if (isType(ndx, ID_APPT)) {
               lastField.end(ndx);
               lastField = aptField = new FieldSpec(ndx+1);
             }
             else if (isAptToken(tokens[ndx], false)) {
               aptToken = true;
               lastField.end(ndx);
               lastField = aptField = new FieldSpec(ndx);
             }
           }
           
           
           // Check for cross street marker
           if (crossField == null && isType(ndx, ID_CROSS_STREET)) {
             lastField.end(ndx);
             lastField = crossField = new FieldSpec(ndx+1);
           }
         }
       }
     }
     
     // If we are parsing to end of field, return successful status
     if (parseToEnd) {
       lastField.end(tokens.length);
       if (placeField != null) result.placeField = placeField;
       if (aptField != null) {
         result.aptField = aptField;
         result.aptToken = aptToken;
       }
       if (crossField != null) result.crossField = crossField;
       result.endAll = tokens.length;
       if (fldSpec != null) fldSpec.end(startField.fldEnd);
       return true;
     }
     return false;
   }
   
   /**
    * Find the end of a city that starts at the current index
    * @param ndx current index
    * @return one past the last token in city if city was found,
    * -1 if this is not a city.
    */
   private int findEndCity(int ndx) {
     
     // If there is no city list, obviously there is no city
     if (mWordCities == null) return -1;
 
     // See if we can find the end of a city sequence
     // If not, then return -1;
     int endNdx = mWordCities.findEndSequence(ndx);
     if (endNdx < 0) return -1;
     
     // We did find a city, but if it is followed by a road suffix, disqualify it
     if (isType(endNdx, ID_ROAD_SFX)) return -1;
     
     // A road suffix one or two tokens past the end of the city also disqualifies it
     // Except some times there really is cross street information following
     // the address, in which case just ignore all the above
     if (!isFlagSet(FLAG_CROSS_FOLLOWS)) {
       if (isType(endNdx+1, ID_ROAD_SFX) || isType(endNdx+2, ID_ROAD_SFX)) return -1;
     }
     
     // Looks good, lets return this
     return endNdx;
   }
 
   /**
    * See if we can find some additional cross street information after
    * the end of a successfully parsed address
    */
   private void findCrossStreet(Result result) {
     
     if (isFlagSet(FLAG_ONLY_CROSS)) return;
     
     if (result.endAll >= tokens.length) return;
     
     // check for optional place marker
     if (isFlagSet(FLAG_AT_PLACE | FLAG_AT_BOTH)) {
       if (isType(result.endAll, ID_AT_MARKER)) {
         result.endAll++;
         result.placeField = new FieldSpec(result.endAll);
       }
       else if (isType(result.endAll, ID_INCL_AT_MARKER)) {
         result.placeField = new FieldSpec(result.endAll);
       }
       
       // Alas, we don't have any way to guess at how long the place name
       // might be.  Default to one word, unless we find another marker
       // further down the road
       if (result.placeField != null) {
         result.endAll++;
         for (int ndx = result.endAll; ndx < tokens.length; ndx++) {
           if (isType(result.endAll, ID_NOT_ADDRESS)) break;
           if (isType(ndx, ID_APPT | ID_CROSS_STREET) ||
               isAptToken(ndx)) {
             result.endAll = ndx;
             break;
           }
         }
         result.placeField.end(result.endAll);
       }
     }
     
     // First lets look for an apartment
     if (isType(result.endAll, ID_APPT)) {
       if (++result.endAll < tokens.length) {
         result.aptField = new FieldSpec(result.endAll, ++result.endAll);
       }
     }
     
     else if (isAptToken(result.endAll)) {
       result.aptToken = true;
       result.aptField = new FieldSpec(result.endAll, ++result.endAll);
     }
     
     // Now see if we are at a cross street indicator.  If not, nothing to find
     if (! isType(result.endAll, ID_CROSS_STREET)) return;
     
     findCrossStreetEnd(result.endAll+1, result);
   }
 
   /**
    * Locate end of cross street field
    * @param sCross
    * @param result
    */
   private void findCrossStreetEnd(int sCross, Result result) {
 
     int sEnd = sCross;
     int ndx = sEnd;
     while (true) {
       
       // And try to find another road
       ndx = findRoadEnd(ndx, 2);
       if (ndx < 0) break;
       sEnd = ndx;
       
       // If this road was terminated by a connector
       // Loop back and see if we can find another cross street
       if (!isType(sEnd, ID_CONNECTOR)) break;
       ndx++;
     }
     
     // Success, save the cross street location
     if (sEnd > sCross) {
       result.crossField = new FieldSpec(sCross, sEnd);
       result.endAll = sEnd;
     }
   }
 
   /**
    * See if we can identify a road name starting at a given index
    * @param start starting index
    * @param option - option controlling how we will deal with a suffixless street search
    *                  0 - No suffixless street names accepted
    *                  1 - only multiword suffixless street names accepted
    *                  2 - any suffixless street name accepted
    * @return index of token past end of road name if successful, -1 otherwise
    */
   private int findRoadEnd(int start, int option) {
     
     // If this starts with a street direction, skip over it
     if (isType(start, ID_DIRECTION)) start++;
     
     // Ditto for a street prefix
     if (isType(start, ID_OPT_ROAD_PFX)) start++;
     
     // If we are out of tokens, the answer is no
     if (start >= tokens.length) return -1; 
     
     // Compute the failure index that we return if we fail to find a proper road end.
     // If we are accepting roads without a street suffix, we will compute the default
     // value assuming this is a suffixless street name.  If not, the failure return is
     // always -1;
     int failIndex = -1;
     if (option > 1 && isFlagSet(FLAG_OPT_STREET_SFX)) {
       if (isType(start, ID_NOT_ADDRESS | ID_CONNECTOR)) return -1;
       if (mWordStreetsFwd != null) {
         failIndex = mWordStreetsFwd.findEndSequence(start);
       } else {
         failIndex = start+1;
       }
       if (option < 2 && failIndex - start < 2) failIndex = -1;
     }
     
     // Dummy loop that we can break out of when we find a road end
     int end;
     do {
       
       if (isType(start, ID_NOT_ADDRESS | ID_CONNECTOR)) return failIndex;
       
       // A stand alone road token can terminate the road search, but it must
       // be the first thing in the search sequence
       if (isRoadToken(start)) {
         end = start+1;
         break;
       }
       
       // See if this is a numbered highway
       if (isType(start, ID_ROUTE_PFX)) {
         end = start + 1;
         if (isType(start, ID_ROUTE_PFX_PFX) && isType(start+1, ID_ROUTE_PFX_EXT)) 
              if (!tokens[start].equalsIgnoreCase("STATE") || !tokens[start+1].equals("RD")) end++;
         if (isType(end, ID_NUMBER | ID_ALPHA_ROUTE)) {
           
           // Yet another special case Texas FM number highways can be terminated
           // with a street suffix :(
           if (tokens[start].equalsIgnoreCase("FM")) {
             if (isType(end+1, ID_ROAD_SFX)) end++;
           }
           end++;
           break;
         }
       }
       
       // OK, OK, if we find a number followed by a connector, we will consider
       // it a numbered highway (sheesh)
       if (isType(start, ID_NUMBER) && isType(start+1, ID_CONNECTOR)) return start+1;
       
       // Still no luck,
       // start looking for a street suffix (or cross street indicator
       // If we have to pass more than two tokens before finding, give up
       end = start;
       boolean good = false;
       while (++end - start <= 3) {
         
         // An intersection marker marks the end of things
         if (isType(end, ID_CONNECTOR)) return failIndex;
 
         good = true;
         if (isType(end, ID_ROAD_SFX) &&
             (! (isType(end, ID_AMBIG_ROAD_SFX) && isType(end+1, ID_ROAD_SFX)))) {
           end++; 
           break; 
         }
         if (isType(end, ID_CROSS_STREET)) break;
         
         good = false;
       }
       
       if (!good) return failIndex;
       
     } while (false);
     
     // If road is followed by a direction, include that
     if (isType(end, ID_DIRECTION)) end++;
     return end;
   }
 
   private void setTokenTypes(StartType sType, String address, Result result) {
     // Parse line into tokens and categorize each token
     // While we are doing this, identify the index of the last city
     // And see if we have a keyword flagging the start of the address
     startAddress =  -1;
     lastCity = -1;
     startNdx = isFlagSet(FLAG_START_FLD_REQ) ? 1 : 0;
     result.tokens = tokens = address.trim().split("\\s+");
     tokenType = new int[tokens.length];
     boolean flexAt = isFlagSet(FLAG_AT_PLACE | FLAG_AT_BOTH);
     boolean ignoreAt = isFlagSet(FLAG_IGNORE_AT);
     
     if (sType == StartType.START_ADDR) startAddress = 0; 
     else result.startField = new FieldSpec(0); 
     
     boolean setStart = (startAddress < 0 || flexAt);
     boolean pastAddr = false; 
     for (int ndx = 0; ndx < tokens.length; ndx++) {
       setType(ndx, setStart, pastAddr);
       if (isType(ndx, ID_CROSS_STREET | ID_APPT)) {
         pastAddr = true;
         setStart = false;
       }
       if (setStart && !flexAt && !ignoreAt) {
         if (isType(ndx, ID_AT_MARKER)) {
           result.startField.end(ndx);
           startAddress = ndx + 1;
         } else if (isType(ndx, ID_INCL_AT_MARKER)) {
           result.startField.end(ndx);
           startAddress = ndx;
         }
       }
       if (isType(ndx, ID_CITY)) lastCity = ndx;
     }
   }
 
   // Identify token type
   private static final Pattern BAD_CHARS = Pattern.compile("[\\(\\)\\[\\],]");
   private void setType(int ndx, boolean checkAt, boolean pastAddr) {
     String token = tokens[ndx];
     
     // If token contains any illegal characters, flag it as a non-address token
     // and bail out.  This is only a problem if we are still in the address proper
     // If we have passed the address and are now in apt or cross fields, illegal
     // character tokens are OK
     if (!pastAddr && BAD_CHARS.matcher(token).find()) {
       tokenType[ndx] |= ID_NOT_ADDRESS;
       if (isFlagSet(FLAG_ANCHOR_END)) startNdx = ndx+1;
       return;
     }
     
     // If token is longer than 1 char and starts with an @
     // Strip off the @ and add the ID_INCL_START_MARK flag
     int mask = 0;
     if (checkAt && token.length() > 1 && token.charAt(0) == '@') {
       tokens[ndx] = token = token.substring(1);
       mask = ID_INCL_AT_MARKER;
     }
     
     // If token is in dictionary, return the associated type code
     // City codes are not permitted to follow intersection connectrs, cross
     // street markers, or at markers for fear thay might be a street with the
     // same name as a city
     Integer iType = dictionary.get(token.toUpperCase());
     if (iType != null) {
       mask |= iType;
       if (ndx > 0 && isType(ndx-1, ID_CONNECTOR | ID_CROSS_STREET | ID_AT_MARKER) ||
           ndx > 1 && isType(ndx-2, ID_CONNECTOR | ID_CROSS_STREET | ID_AT_MARKER) && isType(ndx-1, ID_DIRECTION)) {
         mask &= ~ID_CITY;
       }
     }
     
     else if (NUMERIC.matcher(token).matches()) {
       mask |= ID_NUMBER;
     }
     
     // Some states use alpha route numbers.  This token is a candidate if
     // it hasn't been designated as anything else
     // it is one or two characters long
     // all of the characters are letters
     // it is not a common 2 letter word
     if (mask == 0 && Character.isLetter(token.charAt(0))) {
       if (token.length() == 1 ||
          token.length() == 2 && Character.isLetter(token.charAt(1)) &&
          !token.equals("IN") && !token.equals("OF")) {
         mask |= ID_ALPHA_ROUTE;
       }
     }
     tokenType[ndx] =  mask;
   }
   
   private boolean isType(int ndx, int mask) {
     if (ndx >= tokenType.length) return false; 
     return (tokenType[ndx] & mask) != 0;
   }
   
   private boolean isFlagSet(int mask) {
     return (flags & mask) != 0;
   }
   
   // Determine if token is a single standalone road token
   // such as I-234, or US50, or RT250NB :(
   private boolean isRoadToken(int ndx) {
     
     // If illegal char, answer is no
     if (isType(ndx, ID_NOT_ADDRESS | ID_CONNECTOR)) return false;
     
     // Get the string token
     if (ndx >= tokenType.length) return false; 
     String token = tokens[ndx];
     
     // See how many letters there are at start of token
     int pt = 0;
     while (pt < token.length() && Character.isLetter(token.charAt(pt))) pt++;
     
     // If zero, or if entire token is letters, the answer is no
     if (pt == 0 || pt == token.length()) return false;
     
     // If next character is a dash, skip over it
     int pta = pt;
     
     int ptBreak = pt;
     boolean dashFound = (token.charAt(pt) == '-');
     if (dashFound) {
       pt++;
       ptBreak = -1;
     }
     
     // If next character is not a digit, answer is no
     if (pt >= token.length() || ! Character.isDigit(token.charAt(pt))) return false;
     
     // Shift the alpha portion upper case and see if what is left is a route prefix
     String pfx = token.substring(0, pta).toUpperCase();
     Integer mask = dictionary.get(pfx);
     if (mask == null || (mask&ID_ROUTE_PFX) == 0) return false;
     
     // Looks promising.  Now scan over any digits and see what we have left
     do pt++; while (pt < token.length() && Character.isDigit(token.charAt(pt)));
     
     // If we are at end of string, answer is yes
     // Otherwise, the answer depends on if the remaining suffix looks like some
     // kind of highway qualifier (NB = north bound for example)
     if (pt != token.length()) {
       String sfx = token.substring(pt).toUpperCase();
       
       do {
         if (sfx.length() == 2 && sfx.charAt(1) == 'B' &&
             "NESW".indexOf(sfx.charAt(0)) >= 0) break;
 
         if (sfx.length() == 1 && 
             "NESW".indexOf(sfx.charAt(0)) >= 0) break;
         
         // Nothing recognized, return false
         return false;
         
       } while (false);
     }
     
     // OK, we've concluded this is a valid single word road name.  Now we
     // need to clean up some things Google can't handle
     
     // Google doesn't handle some prefixes with a dash separator, so just to
     // be on the safe side, we always insert one if it wasn't there initially
     // Unless this is a RT prefix, in which cas the dash just doesn't work at all
     if (ptBreak >= 0 && !pfx.equals("RT")) {
       tokens[ndx] = tokens[ndx].substring(0, ptBreak) + "-" + tokens[ndx].substring(ptBreak);
     }
     
     // And Google doesn't like the SRT prefix, change it it to ST
     if (pfx.equals("SRT")) {
       tokens[ndx] = "ST" + tokens[ndx].substring(3);
     }
     return true;
   }
   
   // Determine if token is a valid house number
   private boolean isHouseNumber(int ndx) {
     
     // If numeric token, answer is yes
     if (isType(ndx, ID_NUMBER)) return true;
     
     // Try it against the numeric street number pattern
     // which allows a trailing letter qualifier
     if (ndx >= tokens.length) return false;
     if (PAT_HOUSE_NUMBER.matcher(tokens[ndx]).matches()) return true;
     return false;
     
   }
   
   // Determine if token at index is a standalone apartment number
   private boolean isAptToken(int ndx) {
     if (ndx >= tokens.length) return false;
     return isAptToken(tokens[ndx], !isFlagSet(FLAG_NO_IMPLIED_APT));
   }
   
   /**
    * Determine if token is a valid single apartment token
    * @param token token to be checked
    * @param numberOK true if tokens that start with a digit should be considered valid
    * @return true if this is an apartment token
    */
   private boolean isAptToken(String token, boolean numberOK) {
     int pt = getAptBreak(token);
     if (pt < 0) return false;
     if (token.length()-pt > 4) return false;
     if (pt == 0) return numberOK;
     String prefix = token.substring(0,pt).toUpperCase();
     Integer flags = dictionary.get(prefix);
     return flags != null && (flags & ID_APPT) != 0;
   }
 
   /**
    * Extract the Apt value from a token that has previously been
    * identified as a single word apartment token
    * @param token token to be checked
    * @return apartment value
    */
   private String getAptValue(String token) {
     int pt = getAptBreak(token);
     return token.substring(pt);
   }
   
   /**
    * Identify the break between the Apt keyword and value in a token
    * that we are trying to identify as a single word apt token
    * @param token Token to be checked
    * @return offset into token of start of apartment value
    */
   private int getAptBreak(String token) {
     if (token.startsWith("#")) return 1;
     Matcher match = DIGIT.matcher(token);
     if (match.find()) return match.start();
     return -1;
   }
   private static final Pattern DIGIT = Pattern.compile("\\d");
   
   /**
    * This class contains a searchable list of multi word items.  It will be
    * used to keep two lists.  One of multi-word cities, and another of multi-word
    * streets.
    */
   private class MultiWordList {
     
     private int dir;
     private int idFlag;
     private int incompFlag;
     private int completeFlag;
     private List<String[]> wordList = null;
     
     /**
      * Create a multiword list
      * @param dir search direction +1 for forward search, -1 for backward search
      * @param idFlag token flag used to objects in this list.  If zero any
      * token will be considered to be at least a single word match
      * @param incompFlag token flag used to mark beginning of multiword sequences
      * @param completeFlag token flag used to mark complete single word sequence.
      * Not used if idFlag is zero
      * @param nameList list of possibly multiword names to be added to list
      */
     public MultiWordList(int dir, int idFlag, int incompFlag, int completeFlag, String[] nameList) {
       this.dir = (dir < 0 ? -1 : 1);
       this.incompFlag = incompFlag;
       this.completeFlag = completeFlag;
       
       int flags1 = idFlag | incompFlag;
       int flags2 = idFlag | completeFlag;
 
       // Run thorough the city list
       for (String name : nameList) {
         
         // If city name does not contain a blank, we are not interested in it
         if (name.contains(" ")) {
           
           // Break up city name into token list.
           // If this a backward search list, reverse the tokenList word order
           if (wordList == null) wordList = new ArrayList<String[]>();
           String[] tokenList = name.split(" +");
           if (dir < 0) {
             for (int ndx = 0; ndx < tokenList.length/2; ndx++) {
               int ndx2 = tokenList.length-ndx-1;
               String tmp = tokenList[ndx];
               tokenList[ndx] = tokenList[ndx2];
               tokenList[ndx2] = tmp;
             }
           }
           
           // Add the token list to the word list
           wordList.add(tokenList);
           
           // And add the first token to the dictionary as an incomplete city
           if (flags1 != 0) setupDictionary(flags1, tokenList[0]);
         }
         
         // Otherwise, we just add this to dictionary as a normal city
         else {
           if (flags2 != 0) setupDictionary(flags2, name);
         }
       }
     }
     
     /**
      * Search for a single or multi-word sequence begining at specfied index
      * @param ndx specified token index
      * @return if no sequence found, return -1.
      * If search direction is forward returns one past the end of a found sequence
      * If search direction is backward, returns the beginning of found sequence
      */
     public int findEndSequence(int ndx) {
       
       // If there is an ID token flag, and isn't set for this index, look no further 
       if (idFlag != 0 && !isType(ndx, idFlag)) return -1;
       
       // If this is flagged as the start of a multi-word entry, see if
       // we find a match in the muti-word list
       // If there are multiple matches, pick the longest
       int foundLen = 0;
       if (isType(ndx, incompFlag)) {
         for (String[] tokenList : wordList) {
           boolean match = true;
           for (int j = 0; j< tokenList.length; j++) {
             int jj = ndx + j*dir;
             if (jj < 0 || jj >= tokens.length ||
                 ! tokenList[j].equalsIgnoreCase(tokens[jj])) {
               match = false;
               break;
             }
           }
           if (match) {
             if (tokenList.length > foundLen) foundLen = tokenList.length;
           }
         }
       }
 
       // If we didn't find a multiword match, see if this is flagged as a complete
       // single word match.  If there was no completeFlag, than any single word
       // item will be considered a match.  If no singleword match, return -1
       if (foundLen == 0) {
         if (completeFlag == 0 || isType(ndx, completeFlag)) foundLen = 1;
         else return -1;
       }
       
       // Compute the end (or beginning) index of what we have found and return it.
       int endNdx = ndx + foundLen*dir;
       if (dir < 0) endNdx++;
       return endNdx;
     }
   }
   
   private static class FieldSpec {
     int fldStart;
     int fldEnd;
     
     public FieldSpec(int fldStart) {
       this(fldStart, -1);
     }
 
     public FieldSpec(int fldStart, int fldEnd) {
       this.fldStart = fldStart;
       this.fldEnd = fldEnd;
     }
     
     public void end(int fldEnd) {
       this.fldEnd = fldEnd;
     }
 
     public void optionalEnd(int fldEnd) {
       if (this.fldEnd < 0) this.fldEnd = fldEnd;
     }
   }
   
   public class Result {
     private String[] tokens;
     private StartType startType;
     private int status = -1;
     private String callPrefix = null;
     private FieldSpec startField = null;
     private FieldSpec addressField = null;
     private FieldSpec placeField = null;
     private boolean aptToken = false;
     private FieldSpec aptField = null;
     private FieldSpec crossField = null;
     private FieldSpec padField = null;
     private FieldSpec cityField = null;
     private int insertAmp = -1;
     int endAll = -1;
     
     public void reset() {
       if (startAddress < 0) startField.end(-1);
       addressField = null;
       placeField = null;
       aptToken = false;
       aptField = null;
       crossField = null;
       padField = null;
       cityField = null;
       insertAmp = -1;
       endAll = -1;
     }
     
     /**
      * @return result status
      */
     public int getStatus() {
       return status;
     }
     
     /**
      * Determine Determine how good an address this parse call found
      * @param extra number of extra tokens (presumably city names) that can
      * be ignored at the end of the line
      * @return zero if string is not recognized as valid address, otherwise a
      * numeric value in which higher values indicate better addresses
      */
     public int getStatus(int extra) {
       if (tokens == null) return 0;
       if (tokens.length-endAll > extra) return 0;
       return status;
     }
   
     /**
      * Fill data object with information from parsed line
      */
     public void getData(MsgInfo.Data data) {
       
       if (addressField != null) data.strAddress = buildData(addressField, 2);
       if (placeField != null) data.strPlace = buildData(placeField, 0);
       if (aptField != null) {
         data.strApt = buildData(aptField, 0);
         if (aptToken) data.strApt = getAptValue(data.strApt);
       }
       if (crossField != null) data.strCross = buildData(crossField, 1);
       if (cityField != null) {
         data.strCity = buildData(cityField, 0);
         if (cityCodes != null) data.strCity = convertCodes(data.strCity, cityCodes);
       }
       
       // If prefix ends with some variation of "REPORTED AT" drop the 
       // REPORTED (AT has already been dropped)
       if (startField != null && startField.fldEnd > 0 && isType(startField.fldEnd, ID_AT_MARKER) &&
           tokens[startField.fldEnd-1].equalsIgnoreCase("REPORTED")) startField.fldEnd--;
       
       // Before we figure out with to do with the leading start field, see if some of it
       // should be stripped off and added to the address
       // If result status indicates that we did not find a street number, check the start
       // field for any trailing digits.  If we find any, strip them off and prepend them to
       // the address field
       String startFld = buildData(startField, 0);
       if (isFlagSet(FLAG_START_FLD_NO_DELIM) && status < 3) {
         int pt = startFld.length();
         while (pt > 0 && Character.isDigit(startFld.charAt(pt-1))) pt--;
         if (pt < startFld.length()) {
           data.strAddress = append(startFld.substring(pt), " ", data.strAddress);
           startFld = startFld.substring(0,pt).trim();
         }
       }
       
       if (callPrefix != null) data.strCall = callPrefix;
       switch (startType) {
       case START_CALL:
       case START_CALL_PLACE:
         data.strCall = append(data.strCall, " / ", startFld.replaceAll(" / ", "/"));
         break;
       case START_PLACE:
         if (data.strPlace.length() == 0) {
           data.strPlace = startFld.replaceAll(" / ", "/");
         }
         break;
       }
     }
     
     /**
      * @return the pad field (if any) that lies between the address property and
      * the city field
      */
     public String getPadField() {
       return buildData(padField, 0);
     }
 
     /**
      * Called after address has been parsed
      * @return the part of the line after the address
      */
     public String getLeft() {
       if (endAll < 0) return "";
       return buildData(endAll, tokens.length, 0);
     }
     
     /**
      * @return whatever was identifying as being in front of the address
      */
     public String getAddressPrefix() {
       return buildData(startField, 0);
     }
     
     /**
      * @return everything from start of identified address to end of string
      */
     public String getFullAddress() {
       if (addressField ==  null) return null;
       return buildData(addressField.fldStart, endAll, 0);
     }
     
     private String buildData(FieldSpec spec, int addrCode) {
       if (spec == null) return "";
       return buildData(spec.fldStart, spec.fldEnd, addrCode);
     }
   
     /**
      * Construct data field from the token sequence from given start and end position
      * @param start starting token index
      * @param end ending token index
      * @param addr code indicating what should be inserted between street names
      *         0 nothing or leave existing slash
      *         1 - slash
      *         2 - ampersand
      * @return Constructed data field.
      */
     private String buildData(int start, int end, int addrCode) {
       
       StringBuilder sb = new StringBuilder();
       for (int ndx = start; ndx < end; ndx++) {
         if (ndx != start) sb.append(' ');
         if (ndx == insertAmp) {
           sb.append(new String[]{"", "/ ", "& "}[addrCode]);
         }
         String token = tokens[ndx];
         if (addrCode>1 && token.equals("/")) token = "&";
         sb.append(token);
       }
       return sb.toString();
     }
   }
 }
