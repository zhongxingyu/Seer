 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Prince Georges County, MD
 for Prince Georges, Anne Arundel, and Charles counties. and Howard County
 Contact: charles woodland <lollipopjuggs@gmail.com>
 Contact: Russ <rcfireblitz@gmail.com>
 System: Fireblitz
 From: @fireblitz.com
 
 55: BUILDING FIRE\nE809 TK809 BO804\n4637 37TH ST (ALLISON ST & DEAD END)\n12/07 04:51\nhttp://fireblitz.com/09/9.html
 0-2 H12: PI\nS11 R11\nPG INDIAN HEAD HWY/FARMINGTON RD WEST\nNotes: WANTS ENGINE AND AMBULANCE FROM STATION 11\n12/07 06:14\nhttp://fireblitz.com/11/0.html
 26: INVESTIGATION\nE817\n6036 PARKLAND CT (OLD SILVER HILL RD & OLD SILVER HILL RD)\n12/06 22:40\nhttp://fireblitz.com/17/9.html
 48: FD ACCIDENT\nSQ818 B802\nANNAPOLIS RD/PRINCESS GARDEN PY (9006 ANNAPOLIS RD/5900 PRINCESS GARD\n12/07 07:02\nhttp://fireblitz.com/18/6.html
 24: OVERDOSE - ALS\nA824 MD847, CHARLIE OD/NOT ALRT\n1605 AIRPORT LN (SCHALL RD & BERRY RD)\n12/07 07:14\nhttp://fireblitz.com/24/8.html
 27: INJURED/SICK\nA827\n6304 MAXWELL DR #4 (CARSWELL AV & MORRIS AV)\n12/07 05:14\nhttp://fireblitz.com/27/2.html
 46: BUILDING FIRE\nE833 TK833 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/33/9.html
 46: BUILDING FIRE\nE837 TK837 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/37/5.html
 49: MEDIC LOCAL\nA849\n13218 DEERFIELD RD (DEAD END & MATTHEWS CT)\n12/06 23:00\nhttp://fireblitz.com/49/1.html
 48: TOWNHOUSE FIRE\nE818 BO802\n9903 BREEZY KNOLL CT [DEAD END & GREEN HAVEN RD]\n12/23 23:32\nhttp://fireblitz.com/18/8.shtm
 
 Contact: Ricky Poole <handgunhunting@gmail.com>
 45: PIA-HIGHWAY\nSQ827\nNB CRAIN HWY SE/NB HEATHERMORE BLVD, PP - <8322/ 0>\n07/05 10:02\nhttp://fireblitz.com/27/0.shtm
 27: PIA-HIGHWAY\nA827 SQ827\nSB CAP BELT IL A HWY/NB BRANCH AVE, PP\n07/06 10:20\nhttp://fireblitz.com/27/5.shtm
 32: HOUSE FIRE\nE827 BO885\n5311 ACORN DR (GLEN OAK DR and WHITE OAK AVE)\n10/01 15:59\nhttp://fireblitz.com/PG/27/3.shtm
 26: APARTMENT FIRE\nSQ827 BO883\n6912 WALKER MILL RD #C2 (KAREN BLVD and SHADY GLEN DR)\n10/01 21:35\nhttp://fireblitz.com/PG/27/0.shtm
 
 Contact: g wise <firedawg2009@gmail.com>
 10-09: SEIZURES\nB13\n8896 FORT SMALLWOOD RD-PA 1 SPIN & TRIM LAUDROMAT (ELIZABETH RD & AMHERST CT)\nhttp://fireblitz.com/AA/13/8.shtm
 
 Contact: Christopher Olson <lilsmokeeater5@yahoo.com>
Sender: @fireblitz.com
 1: MEDIC LOCAL\nE801\n6505 BELCREST RD\n02/10 14:55\nhttp://fireblitz.com/PG/01/5.shtm
 46: PIA\nE837\nNB HARRY S TRUMAN DR/NEW ORCHARD DR\n02/11 11:07\nhttp://fireblitz.com/PG/37/2.shtm
pg37-e@fireblitz.com: 46: MEDIC LOCAL\nE837B\n2513 BAIKAL LOOP (CAPE BARON CT and CAPE TEAL CT)\n03/10 18:15\nhttp://fireblitz.com/PG/37/4.shtm 
 
  */
 
 public class MDPrinceGeorgesCountyFireBizParser extends FieldProgramParser {
   
   private static final Properties COUNTY_CODES = buildCodeTable(new String[]{
       "PG", "PRINCE GEORGES COUNTY",
       "CH", "CHARLES COUNTY",
       "AA", "ANNE ARUNDEL COUNTY",
       "HO", "HOWARD COUNTY",
       "DC", "WASHINGTON"
   });
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "PA", "PASSADENA"
   });
   
   public MDPrinceGeorgesCountyFireBizParser() {
     super("PRINCE GEORGES COUNTY", "MD",
          "MAPCALL! UNIT! ADDR! EXTRA+");
   }
   
   @Override
   public String getFilter() {
     return "@fireblitz.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     String[] lines = body.split("\n");
     if (lines.length <4 || lines.length > 6) return false;
     if (! lines[lines.length-1].startsWith("http://fireblitz.com/")) return false;
     return parseFields(lines, data);
   }
   
   private class MapCallField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       
       // First line contains a station ID & map page followed by a colon and call description
       Parser p = new Parser(field);
       data.strCall = p.getLastOptional(':');
       if (data.strCall.length() == 0) abort();
       String source = p.get('-');
       data.strMap = p.get();
       
       // First two characters *MIGHT* be a county code
       if (source.length() >= 2) {
         String code = source.substring(0,2);
         String county = COUNTY_CODES.getProperty(code);
         if (county != null) {
           data.strCity = county;
           if (code.equals("DC")) data.strState = "DC";
           source = source.substring(2);
         }
       }
       data.strSource = source;
     }
     
     @Override
     public String getFieldNames() {
       return "SRC CITY ST MAP CALL";
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strUnit = p.get(',');
       data.strSupp = p.get();
     }
   }
   
   private static final Pattern CITY_CODE_PTN = Pattern.compile("-([A-Z]{2})\\b");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
     
       // Third line is address, possibly with cross streets in parens
       // or square brackets
       if (field.startsWith("PG ")) field = field.substring(3).trim();
       int pt1 = field.indexOf('(');
       if (pt1 < 0) pt1 = field.indexOf('[');
       String sAddress;
       if (pt1 < 0) {
         sAddress = field;
       } else {
         sAddress = field.substring(0, pt1).trim();
         char cEnd = (field.charAt(pt1)=='(' ? ')' : ']');
         int pt2 = field.indexOf(cEnd, pt1+1);
         if (pt2 < 0) pt2 = field.length();
         data.strCross = field.substring(pt1+1, pt2);
       }
       pt1 = sAddress.indexOf(',');
       if (pt1 >= 0) sAddress = sAddress.substring(0, pt1).trim();
       Matcher match = CITY_CODE_PTN.matcher(sAddress);
       if (match.find()) {
         String code = match.group(1);
         String city = CITY_CODES.getProperty(code);
         if (city != null) {
           data.strCity = city;
           data.strPlace = sAddress.substring(match.end()).trim();
           sAddress = sAddress.substring(0,match.start()).trim();
         }
       }
       super.parse(sAddress, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE X";
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("(\\d\\d/\\d\\d) (\\d\\d:\\d\\d)");
   private class MyExtraField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = DATE_TIME_PTN.matcher(field);
       if (match.matches()) {
         data.strDate = match.group(1);
         data.strTime = match.group(2);
       } else {
         if (field.startsWith("Notes:")) field = field.substring(6).trim();
         data.strSupp = append(data.strSupp, "\n", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "DATE TIME INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("MAPCALL")) return new MapCallField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("EXTRA")) return new MyExtraField();
     return super.getField(name);
   }
 }
