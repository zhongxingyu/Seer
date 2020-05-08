 package net.anei.cadpage.parsers.NC;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.CodeSet;
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 public class NCHendersonCountyParser extends SmartAddressParser {
   
   private static final Pattern ID_PTN = Pattern.compile("^(\\d{8}) +");
   private static final Pattern LINE_PTN = Pattern.compile(" Line\\d+=");
   private static final Pattern UNIT_PTN = Pattern.compile(" +([-#,A-Z0-9#]+)$");
   private static final Pattern PHONE_PTN = Pattern.compile(" +(?:(\\d{3}-\\d{3}-\\d{4}(?: *[xX]\\d+)?)|\\d{3}- -)$");
   
   public NCHendersonCountyParser() {
     super(CITY_LIST, "HENDERSON COUNTY", "NC");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // Strip call id from front of text
     Matcher  match = ID_PTN.matcher(body);
     if (!match.find()) return false;
     data.strCallId = match.group(1);
     body = body.substring(match.end());
     
     // and remove the Linenn= terms from end of message
     match = LINE_PTN.matcher(body);
     if (match.find()) {
       int firstPt = match.start();
       int lastPt = match.end();
       while (match.find()) {
         String info = body.substring(lastPt, match.start()).trim();
         lastPt = match.end();
         data.strSupp = append(data.strSupp, " / ", info);
       }
       String info = body.substring(lastPt).trim();
       data.strSupp = append(data.strSupp, " / ", info);
       body = body.substring(0,firstPt).trim();
     }
     
     // Look for and remove UNIT term
     match = UNIT_PTN.matcher(body);
     if (!match.find()) return false;
     data.strUnit = match.group(1);
     body = body.substring(0, match.start());
     
     // Look for and remove PHONE
     match = PHONE_PTN.matcher(body);
     if (match.find()) {
       data.strPhone = getOptGroup(match.group(1));
       body = body.substring(0, match.start());
     }
     
     // Parse the address and city
     // We have to fudge the BLKD RD call because it makes the
     // city look like part of a new road name
    body = body.replace("//", "/").replace("BLKD RD", "BLKD-RD").replace("PWR LN", "PWR-LN").replace("LIFE LN", "LIFE-LN");
     parseAddress(StartType.START_ADDR, FLAG_PAD_FIELD, body, data);
     data.strApt = append(data.strApt, "-", getPadField());
    body = getLeft().replace("BLKD-RD", "BLKD RD").replace("PWR-LN", "PWR LN").replace("LIFE-LN", "LIFE LN");
     
     // Split what is left into call description and name
     data.strCall = CALL_LIST.getCode(body);
     if (data.strCall != null) {
       data.strName = body.substring(data.strCall.length()).trim();
       if (data.strName.endsWith("/")) data.strName = data.strName.substring(0,data.strName.length()-1).trim();
     } else {
       data.strCall = body;
     }
     
     return true;
   }
   
   private static final CodeSet CALL_LIST = new CodeSet(
       "C3 EMERGENCY",
       "C5 HEART ATK",
       "C5A FULL ARR",
       "C5S STROKE",
       "C6 SEIZURE",
       "C7 RESP DIST",
       "C9 VIC DWN",
       "C9S SICKCALL",
       "C10 OVERDOSE",
       "C15 AUTO ACC",
       "C15P PIN IN",
       "C17 STRU FIR",
       "C21A FIR ALR",
       "C21I AST INV",
       "C21L LIFE LN",
       "C21P PWR LN",
       "C21PS PUB SV",
       "C21R BLKD RD",
       "C21S SMK INV",
       "C21T TREEDOW",
       "C23 BRU FIRE",
       "73 MNTL SUBJ",
       "79 DISTURBNC",
       "85 STRND VEH"
   );
   
   private static final String[] CITY_LIST = new String[]{
     
     // Cities & Towns
     "BALFOUR",
     "BARKER HEIGHTS",
     "BAT CAVE",
     "EAST FLAT ROCK",
     "ETOWAH",
     "FLAT ROCK",
     "FLETCHER",
     "FRUITLAND",
     "GERTON",
     "HENDERSONVILLE",
     "HORSE SHOE",
     "LAUREL PARK",
     "MILLS RIVER",
     "MOUNTAIN HOME",
     "MOUNTAIN PAGE",
     "SALUDA",
     "TUXEDO",
     "VALLEY HILL",
     "ZIRCONIA",
     
     // Townships
     "BLUE RIDGE",
     "CLEAR CREEK",
     "CRAB CREEK",
     "EDNEYVILLE",
     "GREEN RIVER",
     "HOOPERS CREEK"
   };
 }
