 package net.anei.cadpage.parsers.TX;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Humble TX, (Harris County)
 Contact: "Wassell, Michael" <MWassell@avfd.com>
 Sender: msg@cfmsg.com
 [AVFD EMS] - Cardiac -- 17111 Dawn Shadows Dr, Humble -- Map: 376H- -- Xst's: Forest Rain Ln Old Arbor -- Units: E-M29 -- 201105590
 [AVFD EMS] - Diabetic Emergency -- 5310 Moonshadows Dr, Humble -- Map: 337S- -- Xst's: Pocito Ct Wellswood Ct -- Units: E-M29 -- 201105542
 [AVFD EMS] - Medical Call -- 6810 Amber Ash Ct, Humble -- Map: 337Q- -- Xst's: Crimson Oak Trl -- Units: E-M19 -- 201105487
 [AVFD FIRE] - Fire Alarm - Residential -- 19222 Pinewood Mist Ln, Humble -- Map: 338W- -- Xst's: Pinewood Canyon Ln Pinewo -- Units: ATFD E-E39 -- 201105452
 [AVFD FIRE] - Motor Vehicle Incident -- E Fm 1960 & w Lake Houston P, Humble -- Map: 337Z- -- Xst's: W Lake Houston Pkwy Atasc -- Units: E-M19 E-E39 - 201105443
 [AVFD FIRE][Repage] - Mvi - Unknown -- Will Clayton Pkwy & atascoci -- Map: 376C- -- Xst's: Atascocita Rd Hunters Ter -- Units: E-M29 E-E39 - 201105192
 (Chief ALT) [AVFD FIRE][Repage] - Mutual Aid / Assist Agency -- 5711 Lakewater Dr -- Map: 338M- -- Water Wonderland -- Xst's: Running Water Dr Waterwoo -- Units: E-B3
 (Chief ALT) [EMS] - Medical Call - 19326 Aquatic Dr, Humble - Map: 378B- - Xst's: Red Sails Pass Rolling Ra - Units: E-M19 - 201139731
 (Chief ALT) [EMS] - Injured Person - 13300 Will Clayton Pkwy, Humble - Map: 377C- - Atascocita High School - Xst's: Tabor Brook Dr Aerobic Av - Units: E-M19 - 2011398
 (Chief ALT) [FIRE] - Mutual Aid / Assist Agency - 142 Sunlit Park Dr, Humble - Map: 375V- - Xst's: Early Autumn Ct - Units: UT91 E-E39 - 201139857
 (Chief ALT) [FIRE] - Controlled Burn - Jodywood Dr & lonesome Woods - Map: 337Z- - Xst's: Lonesome Woods Trl Echo P - Units: E-E19 - 201139902
 (Chief ALT) [EMS] - Medical Call - 7923 Palmer Place Ln, Humble - Map: 337Z- - Xst's: Match Play Dr - Units: E-M19 - 201139926
 (Chief ALT) [EMS] - Assault - 17314 Wigeon Way Dr, Humble - Map: 376F- - Xst's: Shearwater Bend Dr - Units: E-M29 - 201139940

 (Chief ALT) [EMS] - Eye Problems - Moderate - 238 Old Arbor Way, Humble - Map: 376H- - Xst's: Morning Shadows Way Dawn - Units: E-M29 - 201141225
 (Chief ALT) [EMS] - Chest Pain - Clammy - 0218 Powerscourt - Map: 337U- - Units: E-M39 E-E39 E-D59 - 201141272
 (Chief ALT) [FIRE] - Fire Alarm - Commercial - 5250 E Fm 1960, Humble - Map: 337W- - Humble Mini Warehouse - Xst's: Moon Trail Dr Timber Fore - Units: ATFD E-E39 - 20
 
 Status message, Should be handled as General Alert
 (Chief ALT) [AVFD EMS TIMES] - Incident: 201117017 -- Unit: E-M19 Disp 18:20:53 -- Enroute: 18:22:50 -- Arrived: -- Transport: -- At Hosp: -- Available: 18:24:58
 (Chief ALT) [ET] - [ 201141019 ] - UNIT: E-M29 Disp 21:47:04 - ENRT: 21:48:26 - OSN: 21:54:23 - TRAN: - @HOS: - AVAL: 22:22:50

  */
 
 public class TXHumbleParser extends DispatchOSSIParser {
   
   // Pattern to find a single dash delimiter followed by a numeric ID token
   private static Pattern TRAIL_DELIM = Pattern.compile(" - (?=\\d+$ *)");
   
   // Pattern to identify status messages
   private static Pattern INCIDENT_PTN = Pattern.compile(" *Incident: \\d{9} *| *\\[ \\d{9} \\] *");
   
   public TXHumbleParser() {
     super("HUMBLE", "TX",
            "CALL CALL2? ADDRCITY! Map:MAP PLACE Xst's:X Units:UNIT ID");
   }
   
   @Override
   public String getFilter() {
     return "msg@cfmsg.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // Strip off leading single dash
     // If last field delimiter is a single dash, turn it to a double dash
     Parser p = new Parser(subject);
     data.strSource = p.get('|');
     if (data.strSource.equalsIgnoreCase("Chief ALT")) {
       data.strSource = p.get('|');
     }
     if (body.startsWith("- ")) body = body.substring(2).trim();
     body = TRAIL_DELIM.matcher(body).replaceFirst(" - ");
     
     // Split line into double dash delimited fields and process them
     String[] flds = body.split(" -+ ");
     if (INCIDENT_PTN.matcher(flds[0]).matches()) return false;
     return parseFields(flds, data);
   }
  
  private static final Set<String> CALL2_SET = new HashSet<String>(Arrays.asList(new String[]{
      "Residential", "Unknown",
  }));
   private class Call2Field extends CallField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.contains(" ")) return false;
       data.strCall = append(data.strCall, " - ", field);
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL2")) return new Call2Field();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 }
