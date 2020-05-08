 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Blair county, PA
 Contact: Travis Lunglhofer <tlunglhofer@gmail.com>
 Sender: 28403
 
 Inc: Vehicle Accident w/injuries  Add: MCMAHON RD and 17TH ST  City: LOGAN TWP  Units: 1102 430 519 512 518 433 15A 16A  15A  Lat/Lon: @40.49756 @78.38746  9/7/2011 4:04:59 PM  DR #: 11-0150208 
 Inc: Flooding  Add: 219 N 16TH AVE  City: LOGAN TWP  Units: 18A 1101 16A  16A  Lat/Lon: @40.53965 @78.39608  9/7/2011 8:20:01 AM  DR #: 11-0160247
 Inc: Inside/Outside Invest  Add: 3545 JUNIATA GAP RD  City: LOGAN TWP  Units: 18A  18A  Lat/Lon: @40.54878 @78.42869  9/7/2011 10:26:17 AM  DR #: 11-0180238
 Inc: WIRES DOWN/TREE DOWN  Add: 1100 OLD MILL RUN RD  City: LOGAN TWP  Units: 12A 14A  12A  Lat/Lon: @40.51172 @78.46085  9/4/2011 10:50:24 AM  DR #: 11-0120148
 Inc: Structure Fire  Add: 1632 CAMPUS VIEW DR  City: LOGAN TWP  Units: 18A 1621 1632 512 1811 1851 1211 1221  12A  Lat/Lon: @40.54572 @78.41391  8/25/2011 6:20:44 PM  DR #: 11-0120145
 Inc: FIRE ASSIST  Add: 531 E WOPSONONOCK AVE  City: ALTOONA  Units: 18A  18A  Lat/Lon: @40.53712 @78.40229  8/19/2011 3:03:56 PM  DR #: 11-0180225
 Inc: WIRES DOWN/TREE DOWN  Add: E PLEASANT VALLEY BLVD and BUCKNELL LN  City: LOGAN TWP  Units: 16A  16A  Lat/Lon: @40.53196 @78.36087  8/15/2011 1:49:26 AM  DR #: 11-0160229
 Inc: Vehicle Accident w/injuries  Add: 17TH ST and VALLEY VIEW BLVD  City: LOGAN TWP  Units: 15A 16A 1211  15A  Lat/Lon: @40.49939 @78.39013  8/22/2011 3:52:39 PM  DR #: 11-0150194
 Inc: Structure Fire  Add: 1105 3RD AVE  City: DUNCANSVI  Units: 10A 20A 420 626 421 2001 2021 604 5011 816 5001 2031 1011 5031 1551  15A  Lat/Lon: @40.42410 @78.43086  8/24/2011 6:56:20 PM  DR #: 11-0150196
 
 Contact: Dave Wiser <yzr305@yahoo.com>
 Sender: alerts@blairalerts.com
 (Inc: WIRES DOWN/TREE DOWN) Inc: WIRES DOWN/TREE DOWN \nAdd: 605 5TH ST \nCity: TYRONE BO \nUnits: 400 31A\n31A\nLat/Lon: @40.66879 @78.25321\n9/27/2011 11:05:05 P
 (Inc: Commercial Fire Alarm) Inc: Commercial Fire Alarm \nAdd: 505 3RD ST \nCity: TYRONE BO \nUnits: 400 31A\n31A\nLat/Lon: @40.66619 @78.25446\n10/1/2011 2:43:28
 (Inc: FIRE ASSIST) Inc: FIRE ASSIST \nAdd: STATION 1 \nCity:\2s\nUnits: 9011 31A\n31A\nLat/Lon: @-1.00000 @-1.00000\n10/5/2011 2:27:41 AM \nDR #: 311100256
(Inc: Vehicle Fire) Inc: Vehicle Fire \nAdd: 110 SUNSET HILLS EXT \nCity: SNYDER TW \nUnits: 31A\n31A\nLat/Lon: @40.67871 @78.25442\n10/1/2011 9:35:19 PM \nDR #: 31
 
 */
 
 
 public class PABlairCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "DUNCANSVI",  "DUNCANSVILLE",
      "SNYDER TW",  "SNYDER TWP",
       "TYRONE BO",  "TYRONE"
   });
   
   public PABlairCountyParser() {
     super(CITY_CODES, "BLAIR COUNTY", "PA",
            "Inc:CALL! Add:ADDR! City:CITY! Units:UNIT! Lat/Lon:GPS! DR_#:ID");
   }
   
   @Override
   public String getFilter() {
     return "28403,alerts@blairalerts.com";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = body.replace('\n', ' ');
     return super.parseMsg(body, data);
   }
   
   private static final Pattern DATE_MARK = Pattern.compile(" \\d");
   private class MyGPSField extends GPSField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = DATE_MARK.matcher(field);
       if (match.find()) field = field.substring(0,match.start()).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("GPS")) return new MyGPSField();
     return super.getField(name);
   }
 }
